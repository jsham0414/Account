package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성")
    void createAccountSuccess() {
        // given
        AccountUser account = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(account));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(account)
                        .accountNumber("1000000012")
                        .build()));

        // save 로직에 대한 테스트
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(account)
                        .accountNumber("1000000015")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 10000L);


        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("계좌 해지")
    void deleteAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));


        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.deleteAccount(1L, "1231231231");


        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("유저와 계좌의 소유주 불일치")
    void deleteAccountUserUnMatch() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        AccountUser harry = AccountUser.builder()
                .id(13L)
                .name("Harry")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));


        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1231231231"));

        // then
        assertEquals(ErrorCode.ACCOUNT_UNMATCHED, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지될 계좌에 잔액이 존재")
    void deleteAccountHasBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(1L)
                        .accountNumber("1000000012")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1231231231"));

        // then
        assertEquals(ErrorCode.ACCOUNT_DELETE_HAS_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지 처리된 계좌")
    void deleteAccountAlreadyRegistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("1000000012")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1231231231"));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_DELETED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌를 찾을 수 없음")
    void deleteAccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1231231231"));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저를 찾을 수 없음")
    void createAccountUserNotFound() {
        // given
        AccountUser account = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 10000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌를 10개 초과해서 생성할 수 없음")
    void createAccountMaxAccount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();

        // user가 null이면 NullPointerException을 던진다.
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_COUNT, exception.getErrorCode());

    }

    @Test
    @DisplayName("계좌 조회 성공")
    void getAccountSuccess() {
        // given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789")
                        .build()));
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        // when
        Account account = accountService.getAccount(4555L);

        // then
        // accountRepository 객체의 findById 메서드가 1번 호출되었는지 검증
        verify(accountRepository, times(1)).findById(anyLong());
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }


    @Test
    @DisplayName("계좌 조회 실패 - 음수로 조회")
    void testFailedMinusAccountNumber() {
        // given
        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountService.getAccount(-4555L));
        // then
        assertEquals("Minus", exception.getMessage());
    }

    @Test
    @DisplayName("유저 아이디로 계좌 불러오기")
    void successGetAccountsByUserId() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        List<Account> accountList = Arrays.asList(
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accountList);

        // when
        List<AccountDto> accountsByUserId = accountService.getAccountsByUserId(pobi.getId());

        // then
        assertEquals(3, accountsByUserId.size());
        assertEquals("1111111111", accountsByUserId.get(0).getAccountNumber());
        assertEquals(1000L, accountsByUserId.get(0).getBalance());
        assertEquals("2222222222", accountsByUserId.get(1).getAccountNumber());
        assertEquals(2000L, accountsByUserId.get(1).getBalance());
        assertEquals("3333333333", accountsByUserId.get(2).getAccountNumber());
        assertEquals(3000L, accountsByUserId.get(2).getBalance());
    }

    @Test
    @DisplayName("유저를 찾을 수 없음")
    void failedToGetAccounts() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.getAccountsByUserId(12L));

        // then
        assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_FOUND);
    }

}