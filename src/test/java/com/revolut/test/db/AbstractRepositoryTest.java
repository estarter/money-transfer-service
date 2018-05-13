package com.revolut.test.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.revolut.test.api.Account;
import com.revolut.test.db.support.OptimisticLockException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class AbstractRepositoryTest {

    private static final long LOCK_TIMEOUT = 2;
    AccountRepository repo = new AccountRepository(LOCK_TIMEOUT);

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
    void getLatestMethodShouldReturnTheLatestObject() {
        Account a1 = repo.save(new Account());
        Account a2 = repo.save(new Account());
        assertThat(repo.getLatest()).isEqualTo(a2);
        assertThat(a1.getId()).isLessThan(a2.getId());
    }

    @Test
    void checkLock() throws InterruptedException {
        Account obj = repo.save(new Account());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.invokeAll(Arrays.asList(
                () -> {
                    log.info("t1 get lock");
                    assertTrue(repo.lock(obj.getId()));
                    log.info("t1 perform timely task");
                    assertTrue(repo.rowLock.get(obj.getId()).isHeldByCurrentThread());
                    TimeUnit.SECONDS.sleep(LOCK_TIMEOUT + 1);
                    log.info("t1 release lock");
                    repo.unlock(obj.getId());
                    TimeUnit.SECONDS.sleep(1);
                    assertFalse(repo.rowLock.get(obj.getId()).isHeldByCurrentThread());
                    return null;
                },
                () -> {
                    TimeUnit.SECONDS.sleep(1);
                    log.info("t2 get lock - fail");
                    assertFalse(repo.lock(obj.getId()));
                    assertFalse(repo.rowLock.get(obj.getId()).isHeldByCurrentThread());
                    log.info("t2 get lock - ok");
                    assertTrue(repo.lock(obj.getId()));
                    assertTrue(repo.rowLock.get(obj.getId()).isHeldByCurrentThread());
                    return null;
                }
        ));
    }
}