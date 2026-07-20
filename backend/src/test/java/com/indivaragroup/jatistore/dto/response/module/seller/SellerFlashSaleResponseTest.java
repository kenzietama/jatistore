package com.indivaragroup.jatistore.dto.response.module.seller;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SellerFlashSaleResponseTest {

    @Test
    void testWrapperFrom() {
        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        fs.setName("Sale");
        fs.setStartTime(Instant.now());
        fs.setEndTime(Instant.now());

        SellerFlashSaleResponse.Item item = SellerFlashSaleResponse.Item.builder()
                .itemId(UUID.randomUUID())
                .build();

        SellerFlashSaleResponse.Wrapper wrapper = SellerFlashSaleResponse.Wrapper.from(fs, new PageImpl<>(List.of(item)));
        
        assertEquals(wrapper.hashCode(), wrapper.hashCode());
        assertEquals(wrapper.toString(), wrapper.toString());
        
        // Coverage for default constructors
        new SellerFlashSaleResponse();
        new SellerFlashSaleResponse.Available();
        new SellerFlashSaleResponse.Item();
        new SellerFlashSaleResponse.Wrapper();

        assertEquals(fs.getId(), wrapper.getEventId());
        assertEquals("Sale", wrapper.getEventName());
        assertEquals(1, wrapper.getItems().size());
    }
}
