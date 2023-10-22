package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.AccountDto;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.ErrorCode;
import jakarta.persistence.SqlResultSetMapping;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("성공적으로 거래 완료")
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionId("transactionId")
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.useBalance(
                13L, "1000000000", 200L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());

        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());

        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
    }


    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우")
    void useBalanceExceed() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Pobi")
                .build();

        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1231231231")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        AccountException accountException = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(
                        1L, "1231231231", 100000L
                )
        );

        verify(transactionRepository, times(0)).save(any());
        assertEquals(accountException.getErrorCode(), ErrorCode.AMOUNT_EXCEED_BALANCE);
    }

    @Test
    @DisplayName("유저를 찾을 수 없음")
    void useBalanceUserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌를 찾을 수 없음")
    void useBalanceAccountNotFound() {
        // 유저는 있는데 계좌 번호가 틀린 상황
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // given
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        // 포비의 id로 "1000000000"번 계좌로 거래 요청
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌의 소유주가 아님")
    void useBalanceUserUnmatched() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        AccountUser other = AccountUser.builder()
                .id(2L)
                .name("other")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountNumber("1231231231")
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountUser(other)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1231231231", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_UNMATCHED);
    }


    @Test
    @DisplayName("이미 해지된 계좌")
    void useBalanceAlreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountNumber("1231231231")
                .balance(10000L)
                .accountStatus(AccountStatus.UNREGISTERED)
                .accountUser(user)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1231231231", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_ALREADY_DELETED);
    }

    @Test
    @DisplayName("이미 해지된 계좌")
    void cancelAcountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountNumber("1231231231")
                .balance(10000L)
                .accountStatus(AccountStatus.UNREGISTERED)
                .accountUser(user)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1231231231", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_ALREADY_DELETED);
    }


    @Test
    @DisplayName("계정을 찾지 못함")
    void cancelFailedAccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1231231231")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(S)
                .transactionAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1231231231", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("취소 요청 계좌와 사용자의 계좌가 다름")
    void cancelFailedAccountUnmatched() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        AccountUser other = AccountUser.builder()
                .id(2L)
                .name("other")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1231231231")
                .build();

        Account accountNotUsed = Account.builder()
                .id(2L)
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1231231232")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(S)
                .transactionAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUsed));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId",
                        "1231231231",
                        1000L
                )
        );

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.TRANSACTION_UNMATCHED);
    }

    @Test
    @DisplayName("거래 금액과 취소 금액이 다름")
    void cancelFailedValueUnmatched() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1231231231")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(S)
                .transactionAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId",
                        "1231231231",
                        1200L
                )
        );

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.CANCEL_MUST_FULLY);
    }

    @Test
    @DisplayName("1년이 지난 거래 취소 요청")
    void cancelFailedTooOldOrder() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("user")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1231231231")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(S)
                // minusYears(1)만 할 경우 테스트 통과가 되지 않는 경우가 있음
                .transactionAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId",
                        "1231231231",
                        1000L
                )
        );

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.TOO_OLD_ORDER_TO_CANCEL);
    }

    @Test
    @DisplayName("거래를 찾지 못함")
    void cancelFailedTransactionNotFound() {
        // given

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1231231231", 1000L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.TRANSACTION_NOT_FOUND);
    }


    @Test
    @DisplayName("성공적으로 거래 취소 완료")
    void successCancelBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(S)
                .transactionAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionId("transactionIdForCancel")
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(10000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.cancelBalance(
                "transactionId", "1000000000", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());

        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(10000L + 1000L, captor.getValue().getBalanceSnapshot());

        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
    }


    @Test
    void successQueryTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000012")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(S)
                .transactionAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        // when
        TransactionDto transactionDto = transactionService.queryTransaction("id");

        // then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("원거래 조회 실패")
    void queryTransactionNotFound() {
        // given

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.TRANSACTION_NOT_FOUND);
    }
}