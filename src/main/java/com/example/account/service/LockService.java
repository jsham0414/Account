package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
    스핀락 실습을 위한 클래스
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    @Autowired
    private final RedissonClient redissonClient;

    String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }

    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("trying lock for accountNumber : {}", accountNumber);

        try {
            // 3초동안 스핀락을 건다.
            boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);

            if (!isLock) {
                log.error("Lock acquisition failed.");
                throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK_FAILED);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed.", e);
        }
    }

    public void unlock(String accountNumber) {
        log.debug("Unlock for accountNumber : {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }
}
