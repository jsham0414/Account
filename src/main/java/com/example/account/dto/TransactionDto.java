package com.example.account.dto;

import com.example.account.domain.Account;
import com.example.account.domain.Transaction;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {
    private String accountNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResultType;
    private Long amount;
    private Long balanceSnapshot;
    private String transactionId;
    private LocalDateTime transactionAt;

    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .amount(transaction.getAmount())
                .balanceSnapshot(transaction.getBalanceSnapshot())
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType())
                .transactionResultType(transaction.getTransactionResultType())
                .transactionAt(transaction.getTransactionAt())
                .build();
    }
}
