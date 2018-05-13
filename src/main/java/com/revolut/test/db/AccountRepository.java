package com.revolut.test.db;

import java.util.UUID;

import com.revolut.test.api.Account;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountRepository extends AbstractRepository<UUID, Account> {

    public AccountRepository(long lockTimeout) {
        super(lockTimeout);
    }
}
