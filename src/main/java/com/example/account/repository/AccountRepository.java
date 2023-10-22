package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // 저장소
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 자동으로 생성된 쿼리문으로 결과값들을 가져온다.
    Optional<Account> findFirstByOrderByIdDesc();
    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
