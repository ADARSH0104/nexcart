package com.ecom.product.inventory.service;

import com.ecom.product.inventory.dto.*;
import com.ecom.product.inventory.exception.ConcurrentModificationException;
import com.ecom.product.inventory.exception.InsufficientStockException;
import com.ecom.product.inventory.exception.InvalidInventoryStateException;
import com.ecom.product.inventory.exception.InventoryNotFoundException;
import com.ecom.product.inventory.model.InventoryLog;
import com.ecom.product.inventory.model.InventoryStatusEnum;
import com.ecom.product.inventory.model.SellerMetrics;
import com.ecom.product.inventory.repository.InventoryLogRepository;
import com.ecom.product.inventory.repository.InventoryRepository;
import com.ecom.product.inventory.repository.SellerMetricsRepository;
import com.ecom.product.product.model.Product;
import com.ecom.product.inventory.model.Inventory;
import com.ecom.product.product.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class InventoryWritePlatformServiceImpl implements InventoryWritePlatformService {
    private static final Logger log = LoggerFactory.getLogger(InventoryWritePlatformServiceImpl.class);
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final SellerMetricsRepository sellerMetricsRepository;
    @Value("${inventory.reservation.ttl.minutes}")
    private Long reserveTtl;

    public InventoryWritePlatformServiceImpl(final InventoryRepository inventoryRepository,
                                             final ProductRepository productRepository,
                                             final InventoryLogRepository inventoryLogRepository,
                                             final SellerMetricsRepository sellerMetricsRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.sellerMetricsRepository = sellerMetricsRepository;
    }

    @Transactional
    @Override
    public void createInventory(CreateInventoryRequestDTO requestDTO,Long sellerId) {
        Boolean exist = this.inventoryRepository.existsBySellerIdAndProductId(sellerId, requestDTO.productId());
        if (exist) throw new RuntimeException("Seller is already selling this repository");
        Product product = this.productRepository.findById(requestDTO.productId()).orElseThrow(()->new IllegalArgumentException("Product Not Found"));
        Inventory inventory = new Inventory(
                sellerId,
                product,
                requestDTO.price(),
                0L,
                0L
        );
        this.inventoryRepository.save(inventory);
        SellerMetrics sellerMetrics = getOrCreateSellerMetrics(sellerId);
        sellerMetrics.incrementTotalProducts();
        this.sellerMetricsRepository.save(sellerMetrics);

        InventoryLog createLog = InventoryLog.createInventory(inventory);
        this.inventoryLogRepository.save(createLog);
    }

    @Transactional
    @Override
    public void addStock(Long inventoryId, InventoryManageDTO requestDTO) {
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this inventory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        inventory.addStock(requestDTO.quantity());
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                requestDTO.quantity(),
                0L,
                0L,
                0L,
                0L,
                0L,
                BigDecimal.ZERO
        );

        //Update stock status in product
        Product product = inventory.getProduct();
        if(!product.getInStock() && inventory.getAvailableQuantity()>0){
            product.setInStock(true);
            this.productRepository.save(product);
        }

        //Add log if seller adds quantity
        InventoryLog log = InventoryLog.addStock(inventory, requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Override
    public InventoryStateResponse reserve(Long inventoryId, InventoryRequestDTO requestDTO) {
            int attempt = 0;
            while (attempt < 3) {
                try {
                    return reserveTrx(inventoryId, requestDTO);
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                    attempt++;
                    if (attempt == 3) {
                        log.error(
                                "Reservation failed after retries. inventoryId={} orderId={}",
                                inventoryId,
                                requestDTO.orderId()
                        );
                        return new InventoryStateResponse(
                                requestDTO.orderId(),
                                inventoryId,
                                InventoryStatus.RESERVATION_FAILED
                        );
                    }
                }
            }

        return new InventoryStateResponse(
                requestDTO.orderId(),
                inventoryId,
                InventoryStatus.RESERVATION_FAILED
        );

    }

    @Transactional
    public InventoryStateResponse reserveTrx(Long inventoryId, InventoryRequestDTO requestDTO) {

        //Check if already reserved
        InventoryLog reserveLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RESERVED);
        if (reserveLog != null) {
            return new InventoryStateResponse(
                    requestDTO.orderId(),
                    inventoryId,
                    InventoryStatus.ALREADY_RESERVED
            );
        }

        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this inventory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        try {
            inventory.reserve(requestDTO.quantity());
        } catch (InsufficientStockException e) {

            return new InventoryStateResponse(
                    requestDTO.orderId(),
                    inventoryId,
                    InventoryStatus.NOT_AVAILABLE
            );
        }
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                -requestDTO.quantity(),
                requestDTO.quantity(),
                0L,
                0L,
                0L,
                0L,
                BigDecimal.ZERO
        );

        InventoryLog log = InventoryLog.reserve(inventory, requestDTO.orderId(), requestDTO.quantity(), Instant.now().plus(Duration.ofMinutes(reserveTtl)));
        this.inventoryLogRepository.save(log);
        return new InventoryStateResponse(
                requestDTO.orderId(),
                inventoryId,
                InventoryStatus.RESERVED
        );
    }

    @Transactional
    @Override
    public void release(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog reserveLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RESERVED);
        if (reserveLog == null) throw new InvalidInventoryStateException("The Product is not reserved");
        InventoryLog releaseLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RELEASED);
        if (releaseLog != null) return;

        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        inventory.release(requestDTO.quantity());
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                requestDTO.quantity(),
                -requestDTO.quantity(),
                0L,
                0L,
                0L,
                0L,
                BigDecimal.ZERO
        );

        InventoryLog log = InventoryLog.release(inventory, requestDTO.orderId(), requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void confirm(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog reserveLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RESERVED);
        if (reserveLog == null) throw new InvalidInventoryStateException("The Product is not reserved");
        if (!reserveLog.getQuantity().equals(requestDTO.quantity()))
            throw new InvalidInventoryStateException("The Product is not same as reserved products");
        InventoryLog confirmLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.CONFIRMED);
        if (confirmLog != null) return;

        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        inventory.confirm(requestDTO.quantity());
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                0L,
                -requestDTO.quantity(),
                requestDTO.quantity(),
                0L,
                0L,
                requestDTO.quantity(),
                BigDecimal.ZERO
        );

        InventoryLog log = InventoryLog.confirm(inventory, requestDTO.orderId(), requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void deliver(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog confirmLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.CONFIRMED);
        if (confirmLog == null) throw new InvalidInventoryStateException("Cannot deliver unconfirmed repository");
        if (!confirmLog.getQuantity().equals(requestDTO.quantity()))
            throw new InvalidInventoryStateException("Cannot deliver more or less products than confirmed products");
        InventoryLog deliverLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.DELIVERED);
        if (deliverLog != null) return;

        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        inventory.deliver(requestDTO.quantity());
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                0L,
                0L,
                0L,
                requestDTO.quantity(),
                0L,
                -requestDTO.quantity(),
                inventory.getPrice().multiply(BigDecimal.valueOf(requestDTO.quantity()))
        );

        InventoryLog log = InventoryLog.deliver(inventory, requestDTO.orderId(), requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void returnStock(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog deliveredLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.DELIVERED);
        if (deliveredLog == null) throw new InvalidInventoryStateException("Cannot return undelivered repository");
        if (!deliveredLog.getQuantity().equals(requestDTO.quantity()))
            throw new InvalidInventoryStateException("Cannot return more or less products than confirmed products");
        InventoryLog returnLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RETURNED);
        if (returnLog != null) return;


        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        inventory.returnItems(requestDTO.quantity());
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                requestDTO.quantity(),
                0L,
                -requestDTO.quantity(),
                0L,
                requestDTO.quantity(),
                0L,
                inventory.getPrice().multiply(BigDecimal.valueOf(requestDTO.quantity())).negate()
        );

        InventoryLog log = InventoryLog.returnItems(inventory, requestDTO.orderId(), requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void priceUpdate(Long inventoryId, InventoryManageDTO requestDTO) {
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        inventory.setPrice(requestDTO.price());
        this.inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.priceUpdate(inventory);
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void adjustStock(Long inventoryId, InventoryManageDTO requestDTO) {
        //TODO user this to decrease the stock
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        long previousAvailableQuantity = inventory.getAvailableQuantity();
        inventory.adjust(requestDTO.quantity());
        this.inventoryRepository.save(inventory);
        updateSellerMetrics(
                inventory,
                previousAvailableQuantity,
                requestDTO.quantity(),
                0L,
                0L,
                0L,
                0L,
                0L,
                BigDecimal.ZERO
        );

        InventoryLog log = InventoryLog.adjust(inventory, requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireReservations() {
        //TODO improve scheduled job with pagination and lso add batch reservation and rollback reservations
        //TODO When u add Kafka publish an event which will expire the order when its reserve log expires

        List<InventoryLog> expiredReservations = this.inventoryLogRepository.findByEventAndExpiryAtBefore(InventoryStatusEnum.RESERVED, Instant.now());

        for (InventoryLog reservedLog : expiredReservations) {
            Boolean alreadyExist = this.inventoryLogRepository.existsByInventoryAndOrderIdAndEventIn(
                    reservedLog.getInventory(),
                    reservedLog.getOrderId(),
                    List.of(InventoryStatusEnum.RELEASED, InventoryStatusEnum.CONFIRMED, InventoryStatusEnum.DELIVERED, InventoryStatusEnum.EXPIRED)
            );

            if (alreadyExist.booleanValue()) continue;

            Inventory inventory = reservedLog.getInventory();
            long previousAvailableQuantity = inventory.getAvailableQuantity();
            inventory.release(reservedLog.getQuantity());
            this.inventoryRepository.save(inventory);
            updateSellerMetrics(
                    inventory,
                    previousAvailableQuantity,
                    reservedLog.getQuantity(),
                    -reservedLog.getQuantity(),
                    0L,
                    0L,
                    0L,
                    0L,
                    BigDecimal.ZERO
            );
//            this.inventoryLogRepository.save(InventoryLog.release(reservedLog.getInventory(), reservedLog.getOrderId(), reservedLog.getQuantity()));
            this.inventoryLogRepository.save(InventoryLog.expire(reservedLog.getInventory(), reservedLog.getOrderId(), reservedLog.getQuantity()));
        }

    }

    private void updateSellerMetrics(Inventory inventory,
                                     long previousAvailableQuantity,
                                     long availableDelta,
                                     long reservedDelta,
                                     long soldDelta,
                                     long deliveredDelta,
                                     long returnDelta,
                                     long pendingDelta,
                                     BigDecimal revenueDelta) {
        SellerMetrics sellerMetrics = getOrCreateSellerMetrics(inventory.getSellerId());
        sellerMetrics.applyAvailabilityTransition(previousAvailableQuantity, inventory.getAvailableQuantity());
        sellerMetrics.adjustAvailableQuantity(availableDelta);
        sellerMetrics.adjustReservedQuantity(reservedDelta);
        sellerMetrics.adjustSoldQuantity(soldDelta);
        sellerMetrics.adjustDeliveredQuantity(deliveredDelta);
        sellerMetrics.adjustReturnQuantity(returnDelta);
        sellerMetrics.adjustPendingOrders(pendingDelta);
        sellerMetrics.adjustRevenue(revenueDelta);
        this.sellerMetricsRepository.save(sellerMetrics);
    }

    private SellerMetrics getOrCreateSellerMetrics(Long sellerId) {
        return this.sellerMetricsRepository.findById(sellerId)
                .orElseGet(() -> this.sellerMetricsRepository.save(new SellerMetrics(sellerId)));
    }
}
