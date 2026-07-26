package com.indivaragroup.jatistore.service.flashsale;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;

    public FlashSale getActiveFlashSaleEvent() {
        Instant now = Instant.now();
        log.info("Checking active flash sale from database at UTC: {}", now);

        Optional<FlashSale> activeFlashSale = flashSaleRepository.findActiveFlashSale(now);

        if (activeFlashSale.isPresent()) {
            log.info("Active flash sale found: {}", activeFlashSale.get().getName());
            return activeFlashSale.get();
        } else {
            log.warn("No active flash sale found in database for time: {}", now);
            return null;
        }
    }

    public FlashSale getUpcomingFlashSale() {
        Instant now = Instant.now();
        
        List<FlashSale> available = flashSaleRepository.findAvailableFlashSales(now);
        if (!available.isEmpty()) {
            FlashSale next = available.get(0);
            log.info("Upcoming flash sale found: {}", next.getName());
            return next;
        }
        return null;
    }
}