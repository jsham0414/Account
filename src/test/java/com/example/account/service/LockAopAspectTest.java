package com.example.account.service;

import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {
    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LockAopAspect lockAopAspect;

    @Test
    void lockAndUnlock() throws Throwable {
        // given
        ArgumentCaptor<String> lockArgumentCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgumentCaptor =
                ArgumentCaptor.forClass(String.class);

        UseBalance.Request request = UseBalance.Request.builder()
                .userId(1L)
                .accountNumber("1000000000")
                .amount(1000L)
                .build();

        // when
        lockAopAspect.aroundMethod(proceedingJoinPoint, request);

        // then
        verify(lockService, times(1)).lock(lockArgumentCaptor.capture());
        verify(lockService, times(1)).unlock(unlockArgumentCaptor.capture());
        assertEquals("1000000000", lockArgumentCaptor.getValue());
        assertEquals("1000000000", unlockArgumentCaptor.getValue());
    }

    @Test
    void lockAndUnlockEvenIfThrow() throws Throwable {
        // given
        ArgumentCaptor<String> lockArgumentCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgumentCaptor =
                ArgumentCaptor.forClass(String.class);

        UseBalance.Request request = UseBalance.Request.builder()
                .userId(1L)
                .accountNumber("1000000000")
                .amount(1000L)
                .build();

        given(proceedingJoinPoint.proceed())
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        // when
        // AccountException가 던져진다면 통과
        AccountException e = assertThrows(AccountException.class, () -> lockAopAspect.aroundMethod(proceedingJoinPoint, request));

        // then
        verify(lockService, times(1)).lock(lockArgumentCaptor.capture());
        verify(lockService, times(1)).unlock(unlockArgumentCaptor.capture());
        assertEquals("1000000000", lockArgumentCaptor.getValue());
        assertEquals("1000000000", unlockArgumentCaptor.getValue());
    }

}