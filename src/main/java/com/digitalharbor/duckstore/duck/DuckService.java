
package com.digitalharbor.duckstore.duck;

import com.digitalharbor.duckstore.duck.dto.CreateDuckRequest;
import com.digitalharbor.duckstore.duck.dto.UpdateDuckRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DuckService {

    private final DuckRepository duckRepository;

    public DuckService(DuckRepository duckRepository) {
        this.duckRepository = duckRepository;
    }

    @Transactional(readOnly = true)
    public List<Duck> getAllDucks() {
        return duckRepository.findByDeletedFalseOrderByQuantityAsc();
    }

    @Transactional
    public Duck addDuck(CreateDuckRequest request) {

        BigDecimal price = normalizePrice(request.price());

        String activeKey = buildActiveKey(
                request.color(),
                request.size(),
                price
        );

        // MySQL performs the insert or quantity merge atomically.
        duckRepository.addOrMerge(
                request.color().name(),
                request.size().name(),
                price,
                request.quantity(),
                activeKey
        );

        // Return the inserted or updated active record.
        return duckRepository
                .findByColorAndSizeAndPriceAndDeletedFalse(
                        request.color(),
                        request.size(),
                        price
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Duck was added but could not be retrieved"
                        ));
    }

    @Transactional
    public Duck updateDuck(Long id, UpdateDuckRequest request) {

        Duck duck = duckRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Active duck not found with ID: " + id
                        ));

        BigDecimal newPrice = normalizePrice(request.price());

        boolean duplicateExists =
                duckRepository.existsByColorAndSizeAndPriceAndDeletedFalseAndIdNot(
                        duck.getColor(),
                        duck.getSize(),
                        newPrice,
                        id
                );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "Another active duck already has this color, size and price"
            );
        }

        // Color and size intentionally remain unchanged.
        duck.setPrice(newPrice);
        duck.setQuantity(request.quantity());

        return duckRepository.save(duck);
    }

    @Transactional
    public void deleteDuck(Long id) {

        Duck duck = duckRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Active duck not found with ID: " + id
                        ));

        // Logical deletion: preserve the database record.
        duck.setDeleted(true);

        duckRepository.save(duck);
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildActiveKey(
            DuckColor color,
            DuckSize size,
            BigDecimal price
    ) {
        return color.name() + "|"
                + size.name() + "|"
                + normalizePrice(price).toPlainString();
    }
}

