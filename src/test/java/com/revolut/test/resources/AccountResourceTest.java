package com.revolut.test.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

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
        Account account = Account.builder()
                                 .name("test1")
                                 .balance(BigDecimal.valueOf(10.2))
                                 .currency(Currency.getInstance("CHF"))
                                 .build();

        assertThat(accountRepository.getAll()).isEmpty();
        accountRepository.add(account);
        assertThat(accountRepository.getAll()).contains(account.getAccountId());
        assertThat(accountRepository.get(account.getAccountId())).isEqualTo(account);
    }

}