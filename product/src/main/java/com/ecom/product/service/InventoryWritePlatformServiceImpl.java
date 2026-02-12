package com.ecom.product.service;

import com.ecom.product.dto.CreateInventoryRequestDTO;
import com.ecom.product.dto.InventoryManageDTO;
import com.ecom.product.dto.InventoryRequestDTO;
import com.ecom.product.exception.ConcurrentModificationException;
import com.ecom.product.exception.InvalidInventoryStateException;
import com.ecom.product.exception.InventoryNotFoundException;
import com.ecom.product.model.InventoryLog;
import com.ecom.product.model.InventoryStatusEnum;
import com.ecom.product.model.Product;
import com.ecom.product.model.Inventory;
import com.ecom.product.repository.InventoryLogRepository;
import com.ecom.product.repository.ProductRepository;
import com.ecom.product.repository.InventoryRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class InventoryWritePlatformServiceImpl implements InventoryWritePlatformService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    @Value("${inventory.reservation.ttl.minutes}")
    private Long reserveTtl;

    public InventoryWritePlatformServiceImpl(final InventoryRepository inventoryRepository, final ProductRepository productRepository, final InventoryLogRepository inventoryLogRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    @Transactional
    @Override
    public void createInventory(CreateInventoryRequestDTO requestDTO) {
        Boolean exist = this.inventoryRepository.existsBySellerIdAndProductId(requestDTO.sellerId(), requestDTO.productId());
        if (exist) throw new RuntimeException("Seller is already selling this product");
        Product product = this.productRepository.findById(requestDTO.productId()).orElseThrow(()->new IllegalArgumentException("Product Not Found"));
        Inventory inventory = new Inventory(
                requestDTO.sellerId(),
                product,
                requestDTO.price(),
                0L,
                0L
        );
        this.inventoryRepository.save(inventory);
        InventoryLog createLog = InventoryLog.createInventory(inventory);
        this.inventoryLogRepository.save(createLog);
    }

    @Transactional
    @Override
    public void addStock(Long inventoryId, InventoryManageDTO requestDTO) {
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        inventory.addStock(requestDTO.quantity());
        this.inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.addStock(inventory, requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Override
    public void reserve(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog reserveLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RESERVED);
        if (reserveLog != null) return;
        int attempt = 0;
        while (attempt < 3) {
            try {
                reserveTrx(inventoryId, requestDTO);
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                attempt++;
                if (attempt == 3) {
                    throw new ConcurrentModificationException("Failed to reserve the item: "+e);
                }
            }
        }
    }

    @Transactional
    public void reserveTrx(Long inventoryId, InventoryRequestDTO requestDTO) {
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this inventory id"));
        inventory.reserve(requestDTO.quantity());
        this.inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.reserve(inventory, requestDTO.orderId(), requestDTO.quantity(), Instant.now().plus(Duration.ofMinutes(reserveTtl)));
        this.inventoryLogRepository.save(log);
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
        inventory.release(requestDTO.quantity());
        this.inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.release(inventory, requestDTO.orderId(), requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void confirm(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog reserveLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RESERVED);
        if (reserveLog == null) throw new InvalidInventoryStateException("The Product is not reserved");
        if (reserveLog.getQuantity() != requestDTO.quantity())
            throw new InvalidInventoryStateException("The Product is not same as reserved products");
        InventoryLog confirmLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.CONFIRMED);
        if (confirmLog != null) return;

        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        inventory.confirm(requestDTO.quantity());
        this.inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.confirm(inventory, requestDTO.orderId(), requestDTO.quantity());
        this.inventoryLogRepository.save(log);
    }

    @Transactional
    @Override
    public void returnStock(Long inventoryId, InventoryRequestDTO requestDTO) {
        InventoryLog confirmLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.CONFIRMED);
        if (confirmLog == null) throw new InvalidInventoryStateException("Cannot return unconfirmed product");
        if (confirmLog.getQuantity() != requestDTO.quantity())
            throw new InvalidInventoryStateException("Cannot return more or less products than confirmed products");
        InventoryLog returnLog = this.inventoryLogRepository.findByInventory_IdAndOrderIdAndEvent(inventoryId, requestDTO.orderId(), InventoryStatusEnum.RETURNED);
        if (returnLog != null) return;


        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("No data present with this invnetory id"));
        inventory.returnItems(requestDTO.quantity());
        this.inventoryRepository.save(inventory);

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
        inventory.adjust(requestDTO.quantity());
        this.inventoryRepository.save(inventory);

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
            Boolean alreadyExist = this.inventoryLogRepository.existsByInventoryAndOrderIdAndEventIn(reservedLog.getInventory(), reservedLog.getOrderId(), List.of(InventoryStatusEnum.RELEASED, InventoryStatusEnum.CONFIRMED, InventoryStatusEnum.EXPIRED));

            if (alreadyExist.booleanValue()) continue;

            Inventory inventory = reservedLog.getInventory();
            inventory.release(reservedLog.getQuantity());
            this.inventoryRepository.save(inventory);
            this.inventoryLogRepository.save(InventoryLog.release(reservedLog.getInventory(), reservedLog.getOrderId(), reservedLog.getQuantity()));
            this.inventoryLogRepository.save(InventoryLog.expire(reservedLog.getInventory(), reservedLog.getOrderId(), reservedLog.getQuantity()));
        }

    }
}
