package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.domain.Account;
import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.dto.UseBalance.Response;
import com.example.account.dto.UseBalance.Request;
import com.example.account.exception.AccountException;
import com.example.account.repository.TransactionRepository;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.beans.IntrospectionException;


/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 *
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    @AccountLock
    public Response useBalance(@Valid @RequestBody UseBalance.Request request)
            throws InterruptedException {
        try {
            Thread.sleep(5000L);
            return Response.from(
                    transactionService.useBalance(
                            request.getUserId(),
                            request.getAccountNumber(),
                            request.getAmount()
                    )
            );
        } catch (AccountException e) {
            log.error("Failed to use balance. " + e.getErrorMessage());

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(@Valid @RequestBody CancelBalance.Request request) {
        try {
            return CancelBalance.Response.from(
                transactionService.cancelBalance(
                        request.getTransactionId(),
                        request.getAccountNumber(),
                        request.getAmount())
            );
        } catch (AccountException e) {
            log.error("Failed to cancelBalance. " + e.getErrorMessage());

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId) {
            return QueryTransactionResponse.from(
                    transactionService.queryTransaction(transactionId)
            );
    }
}