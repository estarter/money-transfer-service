package com.revolut.test.db;

import com.revolut.test.api.Account;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountRepository extends AbstractRepository<Account, Long> {

    public AccountRepository(long lockTimeout) {
        super(lockTimeout);
    }
}
