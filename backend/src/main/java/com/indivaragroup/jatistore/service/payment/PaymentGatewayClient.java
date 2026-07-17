package com.indivaragroup.jatistore.service.payment;

import com.indivaragroup.jatistore.dto.request.payment.CardChargeRequest;
import com.indivaragroup.jatistore.dto.request.payment.WalletChargeRequest;
import com.indivaragroup.jatistore.dto.response.payment.CardChargeResponse;
import com.indivaragroup.jatistore.dto.response.payment.WalletChargeResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentGatewayClient {

    private final RestClient restClient;

    public PaymentGatewayClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://mock.apidog.com/m1/1332593-1333794-default")
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
}
