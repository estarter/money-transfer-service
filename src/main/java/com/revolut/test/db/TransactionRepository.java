package com.revolut.test.db;

import com.revolut.test.api.Account;
import com.revolut.test.api.Transaction;
import com.revolut.test.api.support.TransactionState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionRepository extends AbstractRepository<Transaction> {

    private final AccountRepository accountRepository;

    public TransactionRepository(Integer lockTimeout, AccountRepository accountRepository) {
        super(lockTimeout);
        this.accountRepository = accountRepository;
    }

    public Transaction getLatest() {
        Long id = store.keySet().stream().sorted().findFirst().orElse(-1L);
        return store.get(id);
    }

    public void process(Transaction transaction) {
        try {
            log.info("start processing transaction {}", transaction.getId());
            save(transaction);
            processTransaction(transaction);
            log.info("processed transaction {}", transaction.getId());
        } catch (RuntimeException e) {
            transaction.setState(TransactionState.FAILED);
            log.info("fail processing transaction {}", transaction.getId(), e);
            throw e;
        } finally {
            unlock(transaction);
        }
    }

    private void processTransaction(Transaction transaction) {
        Account src = accountRepository.get(transaction.getSrcAccountId());
        Account dest = accountRepository.get(transaction.getDestAccountId());

        if (!src.getCurrency().equals(dest.getCurrency())) {
            throw new IllegalArgumentException("Source and destination accounts must be of the same currency.");
        }
        if (!src.getCurrency().equals(transaction.getCurrency())) {
            throw new IllegalArgumentException("Transaction currency must be the same as of source account.");
        }

        boolean locked = accountRepository.lock(transaction.getSrcAccountId());
        locked = locked && accountRepository.lock(transaction.getDestAccountId());
        locked = locked && this.lock(transaction.getId());
        if (!locked) {
            throw new IllegalArgumentException("Error while acquiring locks");
        }

        if (src.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new IllegalArgumentException("Source account holds insufficient amount.");
        }

        src.setBalance(src.getBalance().subtract(transaction.getAmount()));
        dest.setBalance(dest.getBalance().add(transaction.getAmount()));
        transaction.setState(TransactionState.EXECUTED);
    }

    private void unlock(Transaction transaction) {
        try {
            accountRepository.unlock(transaction.getSrcAccountId());
        } catch (RuntimeException e) {
            log.error("Can't unlock account {}", transaction.getSrcAccountId(), e);
        }
        try {
            accountRepository.unlock(transaction.getDestAccountId());
        } catch (RuntimeException e) {
            log.error("Can't unlock account {}", transaction.getDestAccountId(), e);
        }
        try {
            this.unlock(transaction.getId());
        } catch (RuntimeException e) {
            log.error("Can't unlock transaction {}", transaction.getId(), e);
        }
    }
}
