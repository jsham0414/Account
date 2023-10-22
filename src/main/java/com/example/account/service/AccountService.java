package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.account.domain.AccountStatus.IN_USE;
import static com.example.account.type.ErrorCode.USER_NOT_FOUND;

/**
    트랜잭션의 특징 ACID

    Atomic (원자성) : 모든 작업이 실행되거나 혹은 모두 실행되지 않아야 한다.
    Consistency (일관성) : 모든 트렌잭션이 종료된 후에는 DB의 제약조건을 모두 지키고 있는 상태가 되어야 한다.
    Isolation (격리성) : 트랜잭션은 다른 트랜잭션과 독립적으로 동작해야한다.
    Durability (지속성) : commit을 하게 되면 지속(저장)이 꼭 된다. DB 저장이 실패하더라도 모든 로그를 남겨서 DB에 순차적으로 모두 반영이 되도록 한다.

 */

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 확인한다.
     * 계좌의 번호를 생성한다.
     * 계좌를 저장하고, 정보를 넘긴다.
     *
     * @param userId
     * @param initialBalance
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        validateCreateAccount(accountUser);

        String newAccountNumber = getRandomAccountNumber();
        while (accountRepository.findByAccountNumber(newAccountNumber).isPresent()) {
            newAccountNumber = getRandomAccountNumber();
        }

        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                            .accountUser(accountUser)
                            .accountStatus(IN_USE)
                            .accountNumber(newAccountNumber)
                            .balance(initialBalance)
                            .registeredAt(LocalDateTime.now())
                            .build()
        ));
    }

    String getRandomAccountNumber() {
        StringBuilder sb = new StringBuilder();
        UUID.randomUUID().toString()
                .replace("-", "")
                .chars()
                .forEach(c -> sb.append(Integer.parseInt(c + "")));

        return sb.substring(0, 10);
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnregisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.ACCOUNT_UNMATCHED);
        }

        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.ACCOUNT_DELETE_HAS_BALANCE);
        }
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_COUNT);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }

        return accountRepository.findById(id).get();
    }

    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
