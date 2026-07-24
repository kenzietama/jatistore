package com.indivaragroup.jatistore.service.flashsale;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;

    public FlashSale getActiveFlashSaleEvent() {
        Instant now = Instant.now();
        log.info("Mengecek flash sale aktif dari database pada waktu UTC: {}", now);

        Optional<FlashSale> activeFlashSale = flashSaleRepository.findActiveFlashSale(now);

        if (activeFlashSale.isPresent()) {
            log.info("Flash sale aktif ditemukan: {}", activeFlashSale.get().getName());
            return activeFlashSale.get();
        } else {
            log.warn("Tidak ada flash sale aktif yang ditemukan di database untuk waktu: {}", now);
            return null;
        }
    }
}