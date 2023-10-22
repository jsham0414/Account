package com.example.account.domain;

import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Transaction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;
    @Enumerated(EnumType.STRING)
    @Nullable
    private ErrorCode errorCode;

    @ManyToOne
    private Account account;
    private Long amount;
    private Long balanceSnapshot;

    private String transactionId;
    private LocalDateTime transactionAt;
}
