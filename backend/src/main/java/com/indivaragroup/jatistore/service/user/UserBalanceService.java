package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserBalanceResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBalanceService {

    private static final String MOCK_GATEWAY_BASE_URL = "https://mock.apidog.com/m1/1332593-1333794-default";
    private final RestTemplate restTemplate = new RestTemplate();

    public RestApiResponse<UserBalanceResponse> getBalance() throws CoreThrowHandler {
        log.info("Fetching user balance from mock payment gateway");
        try {
            String url = MOCK_GATEWAY_BASE_URL + "/api/wallet/balance";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"SUCCESS".equals(response.get("status"))) {
                log.error("Failed to fetch balance, gateway returned null or non-SUCCESS status. Response: {}", response);
                throw new CoreThrowHandler(RestApiError.USR_0014);
            }

            Object amountObj = response.get("amount");
            BigDecimal balance = amountObj instanceof Number
                ? new BigDecimal(amountObj.toString())
                : BigDecimal.ZERO;

            log.info("Successfully fetched user balance. Amount: {}", balance);
            return RestApiResponse.<UserBalanceResponse>builder()
                    .restApiResponseHttpCode(HttpStatus.OK.value())
                    .restApiResponseHttpStatus(HttpStatus.OK.getReasonPhrase())
                    .restApiResponseMessage("Wallet balance retrieved successfully.")
                    .restApiResponseData(new UserBalanceResponse(balance))
                    .restApiResponseTimestamp(Instant.now())
                    .restApiResponseRequestId(MDC.get("requestId"))
                    .build();

        } catch (CoreThrowHandler cth) {
            throw cth;
        } catch (Exception e) {
            log.error("Exception occurred while fetching user balance: {}", e.getMessage(), e);
            throw new CoreThrowHandler(RestApiError.USR_0014);
        }
    }
}
