package com.revolut.test.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.revolut.test.api.Account;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountRepository {

    private AtomicLong idGenerator = new AtomicLong();
    private Map<Long, Account> store = new HashMap<>();

    public Account get(Long id) {
        Account result = store.get(id);
        if (result == null)
            throw new ObjectNotFoundException("Can't find account with id '" + id + "'");
        return result;
    }

    public void add(Account account) {
        if (account.getAccountId() != null) {
            log.debug("Don't add account because accountId '{}' is not empty, it should be already in db",
                    account.getAccountId());
            return;
        }
        account.setAccountId(idGenerator.incrementAndGet());
        store.put(account.getAccountId(), account);
    }

    public Set<Long> getAll() {
        return store.keySet();
    }
}
