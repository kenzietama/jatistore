package com.indivaragroup.jatistore.service.payment;

import com.indivaragroup.jatistore.dto.request.payment.CardChargeRequest;
import com.indivaragroup.jatistore.dto.request.payment.WalletChargeRequest;
import com.indivaragroup.jatistore.dto.response.payment.CardChargeResponse;
import com.indivaragroup.jatistore.dto.response.payment.WalletChargeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class PaymentGatewayClientTest {

    private PaymentGatewayClient paymentGatewayClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        paymentGatewayClient = new PaymentGatewayClient();
        
        // Mock RestClient to use a RestTemplate we can intercept with MockRestServiceServer
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        
        RestClient mockRestClient = RestClient.builder()
                .baseUrl("https://mock.apidog.com/m1/1332593-1333794-default")
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory())
                .messageConverters(converters -> converters.addAll(restTemplate.getMessageConverters()))
                .requestInterceptor((request, body, execution) -> {
                    // Forward requests to RestTemplate to be caught by MockRestServiceServer
                    return restTemplate.execute(request.getURI(), request.getMethod(), req -> {
                        req.getHeaders().addAll(request.getHeaders());
                        req.getBody().write(body);
                    }, res -> res);
                })
                .build();
                
        // Let's just use mock server by replacing the client's restClient with a test one.
        RestClient testClient = RestClient.builder(restTemplate).baseUrl("https://mock.apidog.com/m1/1332593-1333794-default").build();
        ReflectionTestUtils.setField(paymentGatewayClient, "restClient", testClient);
    }

    @Test
    void chargeCard_ShouldReturnResponse() {
        mockServer.expect(requestTo("https://mock.apidog.com/m1/1332593-1333794-default/api/card/charge"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":\"SUCCESS\"}"));

        CardChargeRequest request = new CardChargeRequest("123", "User", "12/25", new BigDecimal("100"), "123");
        CardChargeResponse response = paymentGatewayClient.chargeCard(request);
        
        assertNotNull(response);
        mockServer.verify();
    }

    @Test
    void chargeWallet_ShouldReturnResponse() {
        mockServer.expect(requestTo("https://mock.apidog.com/m1/1332593-1333794-default/api/wallet/charge"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":\"SUCCESS\"}"));

        WalletChargeRequest request = new WalletChargeRequest(new BigDecimal("100"));
        WalletChargeResponse response = paymentGatewayClient.chargeWallet(request);
        
        assertNotNull(response);
        mockServer.verify();
    }

    @Test
    void simulatePayout_ShouldReturnTransactionId() {
        String result = paymentGatewayClient.simulatePayout(new BigDecimal("100"));
        assertNotNull(result);
        assertTrue(result.startsWith("TRF-"));
    }
    
    @Test
    void simulatePayout_InterruptedException() {
        Thread.currentThread().interrupt();
        String result = paymentGatewayClient.simulatePayout(new BigDecimal("100"));
        assertNotNull(result);
        assertTrue(result.startsWith("TRF-"));
    }
}
