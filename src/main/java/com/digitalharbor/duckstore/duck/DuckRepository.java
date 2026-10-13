
package com.digitalharbor.duckstore.duck;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DuckRepository extends JpaRepository<Duck, Long> {

    // List active ducks, sorted by quantity in ascending order.
    List<Duck> findByDeletedFalseOrderByQuantityAsc();
    List<Duck> findByColorAndSizeAndDeletedFalseOrderByPriceAsc(
            DuckColor color,
            DuckSize size
    );
    // Find a duck only if it has not been logically deleted.
    Optional<Duck> findByIdAndDeletedFalse(Long id);

    // Find an active duck with the same color, size, and price.
    Optional<Duck> findByColorAndSizeAndPriceAndDeletedFalse(
            DuckColor color,
            DuckSize size,
            BigDecimal price
    );

    // Check whether another active duck has the same combination.
    boolean existsByColorAndSizeAndPriceAndDeletedFalseAndIdNot(
            DuckColor color,
            DuckSize size,
            BigDecimal price,
            Long id
    );

    // Atomically insert a duck or merge its quantity into an existing row.
    @Modifying
    @Query(value = """
            INSERT INTO ducks
                (color, size, price, quantity, deleted, active_key)
            VALUES
                (:color, :size, :price, :quantity, false, :activeKey)
            ON DUPLICATE KEY UPDATE
                quantity = quantity + :quantity
            """, nativeQuery = true)
    int addOrMerge(
            @Param("color") String color,
            @Param("size") String size,
            @Param("price") BigDecimal price,
            @Param("quantity") Integer quantity,
            @Param("activeKey") String activeKey
    );
}
