package com.revolut.test.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revolut.test.api.Account;
import com.revolut.test.db.AccountRepository;

class AccountResourceTest {

    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new AccountRepository();
    }

    @Test
    void happyFlowTest() {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(10.2));

        assertThat(accountRepository.getAll()).isEmpty();
        accountRepository.save(account);
        assertThat(accountRepository.getAll()).contains(account.getId());
        assertThat(accountRepository.get(account.getId())).isEqualTo(account);
    }

}