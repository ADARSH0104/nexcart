package com.ecom.product.product.repository;

import com.ecom.product.product.dto.ProductSearchRequest;
import com.ecom.product.product.dto.ProductSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ProductSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ProductSearchResponse> MAPPER =
            (rs, rowNum) -> new ProductSearchResponse(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("brand"),
                    rs.getBigDecimal("price"),
                    rs.getString("thumbnail_url"),
                    rs.getBoolean("in_stock"),
                    rs.getDouble("score")
            );

    public Page<ProductSearchResponse> search(ProductSearchRequest req) {

        // Main search query
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // Base SELECT with FULLTEXT score
        sql.append("""
            SELECT 
                p.id,
                p.name,
                p.brand,
                MIN(i.price) as price,
                p.thumbnail_url,
                p.in_stock,
                MATCH(p.name, p.brand, p.description) AGAINST (? IN BOOLEAN MODE) AS score
            FROM product p
        """);

        // Add category join if needed
        if (req.category() != null) {
            sql.append(" JOIN product_category pc ON p.id = pc.product_id ");
            sql.append(" JOIN category c ON pc.category_id = c.id ");
        }

        // Always join inventory for pricing
        sql.append(" JOIN inventory i ON p.id = i.product_id ");
        sql.append(" WHERE 1=1 ");

        // FULLTEXT keyword parameter for score calculation in SELECT
        String keyword = "";
        if (req.keyword() != null && !req.keyword().isBlank()) {
            keyword = "+" + req.keyword().trim() + "*";
        }
        params.add(keyword);

        // Apply filters

        // 1. Keyword filter (FULLTEXT)
        if (req.keyword() != null && !req.keyword().isBlank()) {
            sql.append(" AND MATCH(p.name, p.brand, p.description) AGAINST (? IN BOOLEAN MODE) ");
            params.add(keyword);
        }

        // 2. Brand filter
        if (req.brands() != null && !req.brands().isEmpty()) {
            sql.append(" AND p.brand IN (")
                    .append(String.join(",", Collections.nCopies(req.brands().size(), "?")))
                    .append(") ");
            params.addAll(req.brands());
        }

        // 3. Category filter
        if (req.category() != null) {
            sql.append(" AND c.name = ? ");
            params.add(req.category());
        }

        // 4. Min price filter
        if (req.minPrice() != null) {
            sql.append(" AND i.price >= ? ");
            params.add(req.minPrice());
        }

        // 5. Max price filter
        if (req.maxPrice() != null) {
            sql.append(" AND i.price <= ? ");
            params.add(req.maxPrice());
        }

        // 6. Stock filter
        if (req.inStock() != null && req.inStock()) {
            sql.append(" AND p.in_stock = true ");
        }

        // GROUP BY to handle multiple inventory entries per product
        sql.append(" GROUP BY p.id, p.name, p.brand, p.thumbnail_url, p.in_stock ");

        // Sorting
        sql.append(" ORDER BY ");
        if ("price_asc".equals(req.sort())) {
            sql.append(" price ASC");
        } else if ("price_desc".equals(req.sort())) {
            sql.append(" price DESC");
        } else if ("newest".equals(req.sort())) {
            sql.append(" p.created_at DESC");
        } else {
            // Default: relevance score (highest first)
            sql.append(" score DESC, p.created_at DESC");
        }

        // Pagination
        sql.append(" LIMIT ? OFFSET ? ");
        params.add(req.size());
        params.add(req.page() * req.size());

        // Execute main query
        List<ProductSearchResponse> content =
                jdbcTemplate.query(sql.toString(), MAPPER, params.toArray());

        // ===== COUNT QUERY =====
        StringBuilder countSql = new StringBuilder();
        List<Object> countParams = new ArrayList<>();

        countSql.append(" SELECT COUNT(DISTINCT p.id) FROM product p ");

        // Same joins as main query
        if (req.category() != null) {
            countSql.append(" JOIN product_category pc ON p.id = pc.product_id ");
            countSql.append(" JOIN category c ON pc.category_id = c.id ");
        }

        countSql.append(" JOIN inventory i ON p.id = i.product_id ");
        countSql.append(" WHERE 1=1 ");

        // Same filters as main query (no keyword for score in SELECT here)

        // 1. Keyword filter
        if (req.keyword() != null && !req.keyword().isBlank()) {
            countSql.append(" AND MATCH(p.name, p.brand, p.description) AGAINST (? IN BOOLEAN MODE) ");
            countParams.add("+" + req.keyword().trim() + "*");
        }

        // 2. Brand filter
        if (req.brands() != null && !req.brands().isEmpty()) {
            countSql.append(" AND p.brand IN (")
                    .append(String.join(",", Collections.nCopies(req.brands().size(), "?")))
                    .append(") ");
            countParams.addAll(req.brands());
        }

        // 3. Category filter
        if (req.category() != null) {
            countSql.append(" AND c.name = ? ");
            countParams.add(req.category());
        }

        // 4. Min price filter
        if (req.minPrice() != null) {
            countSql.append(" AND i.price >= ? ");
            countParams.add(req.minPrice());
        }

        // 5. Max price filter
        if (req.maxPrice() != null) {
            countSql.append(" AND i.price <= ? ");
            countParams.add(req.maxPrice());
        }

        // 6. Stock filter
        if (req.inStock() != null && req.inStock()) {
            countSql.append(" AND p.in_stock = true ");
        }

        // Execute count query
        Long total = jdbcTemplate.queryForObject(
                countSql.toString(),
                Long.class,
                countParams.toArray()
        );

        return new PageImpl<>(
                content,
                PageRequest.of(req.page(), req.size()),
                total != null ? total : 0L
        );
    }


//    public Page<ProductSearchResponse> searchWithoutFullText(ProductSearchRequest req) {
//
//        StringBuilder sql = new StringBuilder("""
//        SELECT
//            p.id,
//            p.name,
//            p.brand,
//            i.price,
//            p.thumbnail_url,
//            p.in_stock,
//            0 AS score
//        FROM product p
//        WHERE 1=1
//    """);
//
//        List<Object> params = new ArrayList<>();
//
//        // Keyword using LIKE
//        if (req.keyword() != null && !req.keyword().isBlank()) {
//            sql.append("""
//            AND (
//                LOWER(p.name) LIKE ?
//                OR LOWER(p.brand) LIKE ?
//                OR LOWER(p.description) LIKE ?
//            )
//        """);
//
//            String pattern = "%" + req.keyword().toLowerCase() + "%";
//            params.add(pattern);
//            params.add(pattern);
//            params.add(pattern);
//        }
//
//        // Brand filter
//        if (req.brands() != null && !req.brands().isEmpty()) {
//            sql.append(" AND p.brand IN (")
//                    .append(String.join(",", Collections.nCopies(req.brands().size(), "?")))
//                    .append(")");
//            params.addAll(req.brands());
//        }
//
//        // Category filter
//        if (req.category() != null) {
//            sql.append(" AND p.category = ?");
//            params.add(req.category());
//        }
//
//        // Price filters
//        if (req.minPrice() != null) {
//            sql.append(" AND i.price >= ?");
//            params.add(req.minPrice());
//        }
//
//        if (req.maxPrice() != null) {
//            sql.append(" AND i.price <= ?");
//            params.add(req.maxPrice());
//        }
//
//        // Stock filter
//        if (req.inStock() != null && req.inStock()) {
//            sql.append(" AND p.in_stock = true");
//        }
//
//        // Sorting
//        if ("price_asc".equals(req.sort())) {
//            sql.append(" ORDER BY i.price ASC");
//        } else if ("price_desc".equals(req.sort())) {
//            sql.append(" ORDER BY i.price DESC");
//        } else if ("newest".equals(req.sort())) {
//            sql.append(" ORDER BY p.created_at DESC");
//        } else {
//            sql.append(" ORDER BY p.created_at DESC");
//        }
//
//        // Pagination
//        sql.append(" LIMIT ? OFFSET ?");
//        params.add(req.size());
//        params.add(req.page() * req.size());
//
//        List<ProductSearchResponse> content =
//                jdbcTemplate.query(sql.toString(), MAPPER, params.toArray());
//
//        // Count query
//        StringBuilder countSql = new StringBuilder("""
//        SELECT COUNT(*) FROM product p WHERE 1=1
//    """);
//
//        List<Object> countParams = new ArrayList<>();
//
//        if (req.keyword() != null && !req.keyword().isBlank()) {
//            countSql.append("""
//            AND (
//                LOWER(p.name) LIKE ?
//                OR LOWER(p.brand) LIKE ?
//                OR LOWER(p.description) LIKE ?
//            )
//        """);
//
//            String pattern = "%" + req.keyword().toLowerCase() + "%";
//            countParams.add(pattern);
//            countParams.add(pattern);
//            countParams.add(pattern);
//        }
//
//        if (req.brands() != null && !req.brands().isEmpty()) {
//            countSql.append(" AND p.brand IN (")
//                    .append(String.join(",", Collections.nCopies(req.brands().size(), "?")))
//                    .append(")");
//            countParams.addAll(req.brands());
//        }
//
//        if (req.category() != null) {
//            countSql.append(" AND p.category = ?");
//            countParams.add(req.category());
//        }
//
//        if (req.minPrice() != null) {
//            countSql.append(" AND i.price >= ?");
//            countParams.add(req.minPrice());
//        }
//
//        if (req.maxPrice() != null) {
//            countSql.append(" AND i.price <= ?");
//            countParams.add(req.maxPrice());
//        }
//
//        if (req.inStock() != null && req.inStock()) {
//            countSql.append(" AND p.in_stock = true");
//        }
//
//        Long total = jdbcTemplate.queryForObject(
//                countSql.toString(),
//                Long.class,
//                countParams.toArray()
//        );
//
//        return new PageImpl<>(
//                content,
//                PageRequest.of(req.page(), req.size()),
//                total
//        );
//    }
}