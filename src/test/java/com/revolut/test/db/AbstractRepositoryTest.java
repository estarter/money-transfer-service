package com.revolut.test.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.revolut.test.api.Account;
import com.revolut.test.db.support.OptimisticLockException;

class AbstractRepositoryTest {

    AccountRepository repo = new AccountRepository(5);

    @Test
    void itShouldDetachObjectOnSave() {
        Account object = new Account();
        repo.save(object);
        assertEquals(repo.getLatest().getName(), object.getName());
        object.setName("xxxx");
        assertNotEquals(repo.getLatest().getName(), object.getName());
    }

    @Test
    void itShouldDetachObjectOnGet() {
        Account object = new Account();
        repo.save(object);
        object = repo.getLatest();
        assertEquals(repo.getLatest().getName(), object.getName());
        object.setName("xxxx");
        assertNotEquals(repo.getLatest().getName(), object.getName());
    }

    @Test
    void itShouldUpdateVersionOnSave() {
        Account object = new Account();
        repo.save(object);
        assertThat(repo.getLatest().getVersion()).isEqualTo(1L);
        repo.save(object);
        assertThat(repo.getLatest().getVersion()).isEqualTo(2L);
    }

    @Test
    void itShouldOptimisticLock() {
        Account object = new Account();
        repo.save(object);
        Account o1 = repo.getLatest();
        Account o2 = repo.getLatest();
        repo.save(o1);
        Throwable exception = assertThrows(OptimisticLockException.class, () -> repo.save(o2));
        assertThat(exception).hasMessage("Object Account #" + object.getId() + " is out of sync.");
    }

    @Test
    void getLatestShouldWork() {
        Account a1 = repo.save(new Account());
        Account a2 = repo.save(new Account());
        assertThat(repo.getLatest()).isEqualTo(a2);
        assertThat(a1.getId()).isLessThan(a2.getId());
    }
}