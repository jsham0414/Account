package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder                               // 자식 객체가 부모 객체의 필드를 빌더 패턴으로 사용할 수 있게 해준다.
@Entity                                     // 프로그램 실행시 자동으로 테이블이 생성 된다.
public class Account extends BaseEntity {
    @ManyToOne                              // 관계
    private AccountUser accountUser;

    private String accountNumber;

    @Enumerated(EnumType.STRING)            // 데이터에 enum이 String 형식으로 저장된다. db에 insert시 enum값이 아닐 경우 오류 발생
    private AccountStatus accountStatus;

    private Long balance;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }

        balance -= amount;
    }
    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(ErrorCode.INVALIDED_REQUEST);
        }

        balance += amount;
    }
}
