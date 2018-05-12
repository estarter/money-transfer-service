package com.revolut.test.resources;

import static com.revolut.test.TestHelper.assertBalance;
import static com.revolut.test.resources.TransactionResourceTest.makeTransaction;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revolut.test.api.Account;
import com.revolut.test.api.support.TransactionState;
import com.revolut.test.db.AccountRepository;
import com.revolut.test.db.TransactionRepository;

import lombok.val;

class TransactionResourceConcurrentTest {

    private static final int THREAD_NUMBER = 10;
    private static final int TASK_AMOUNT = 10_000;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        accountRepository = new AccountRepository(100);
        transactionRepository = new TransactionRepository(100, accountRepository);

        from = new Account();
        from.setBalance(BigDecimal.valueOf(TASK_AMOUNT));
        accountRepository.save(from);
        to = new Account();
        to.setBalance(BigDecimal.valueOf(0));
        accountRepository.save(to);
    }

    @Test
    void itShouldPerformConcurrently() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        List<Callable<String>> tasks = IntStream.range(0, TASK_AMOUNT).mapToObj(e -> (Callable<String>) () -> {
            counter.incrementAndGet();
            transactionRepository.process(makeTransaction(from, to, 1.0));
            return null;
        }).collect(Collectors.toList());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);
        executor.invokeAll(tasks);

        assertThat(counter.get()).isEqualTo(TASK_AMOUNT);
        assertThat(transactionRepository.getAll().size()).isEqualTo(TASK_AMOUNT);
        assertBalance(0.0, from.getBalance());
        assertBalance(TASK_AMOUNT, to.getBalance());
        val notExecuted = transactionRepository.getAll()
                                               .stream()
                                               .map(id -> transactionRepository.get(id).getState())
                                               .filter(s -> !s.equals(TransactionState.EXECUTED))
                                               .findAny();
        assertThat(notExecuted).isEmpty();
    }

}