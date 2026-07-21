package com.indivaragroup.jatistore.service.payment;

import com.indivaragroup.jatistore.dto.request.payment.CardChargeRequest;
import com.indivaragroup.jatistore.dto.request.payment.WalletChargeRequest;
import com.indivaragroup.jatistore.dto.response.payment.CardChargeResponse;
import com.indivaragroup.jatistore.dto.response.payment.WalletChargeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentGatewayClient {

    private final RestClient restClient;

    public PaymentGatewayClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .baseUrl("https://mock.apidog.com/m1/1332593-1333794-default")
                .requestFactory(factory)
                .build();
    }

    public CardChargeResponse chargeCard(CardChargeRequest request) {
        return restClient.post()
                .uri("/api/card/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(CardChargeResponse.class);
    }

    public WalletChargeResponse chargeWallet(WalletChargeRequest request) {
        return restClient.post()
                .uri("/api/wallet/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WalletChargeResponse.class);
    }

    public String simulatePayout(java.math.BigDecimal amount) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (Math.random() < 0.01) {
            throw new org.springframework.web.client.HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Mock APIdog Timeout"
            );
        }

        return "PAYOUT_MOCK_REF_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
