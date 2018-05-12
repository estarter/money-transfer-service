package com.revolut.test.resources;

import static com.revolut.test.TestHelper.assertBalance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.revolut.test.api.Account;
import com.revolut.test.api.Transaction;
import com.revolut.test.api.TransactionState;
import com.revolut.test.db.AccountRepository;
import com.revolut.test.db.TransactionRepository;

class TransactionResourceTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        accountRepository = new AccountRepository(5);
        transactionRepository = new TransactionRepository(5, accountRepository);

        from = new Account();
        from.setBalance(BigDecimal.valueOf(100));
        accountRepository.save(from);
        to = new Account();
        to.setBalance(BigDecimal.valueOf(0));
        accountRepository.save(to);
    }

    @Test
    void itShouldPerformTransfer() {
        transactionRepository.process(makeTransaction(from, to, 20.50));
        assertBalance(79.5, from.getBalance());
        assertBalance(20.5, to.getBalance());

        assertBalance(20.50, transactionRepository.getLatest().getAmount());
        assertThat(transactionRepository.getLatest().getSrcAccountId()).isEqualTo(from.getId());
        assertThat(transactionRepository.getLatest().getDestAccountId()).isEqualTo(to.getId());
        assertThat(transactionRepository.getLatest().getCurrency()).isEqualTo(Currency.getInstance("CHF"));
        assertThat(transactionRepository.getLatest().getState()).isEqualTo(TransactionState.EXECUTED);
    }

    @Test
    void itShouldCancelTransactionIfInsufficientAmount() {
        assertTransactionFailure("Source account holds insufficient amount.",
                () -> transactionRepository.process(makeTransaction(from, to, 110)));
    }

    @Test
    void itShouldCancelTransactionIfSrcAndDestHaveDifferentCurrency() {
        to.setCurrency(Currency.getInstance("USD"));
        assertTransactionFailure("Source and destination accounts must be of the same currency.",
                () -> transactionRepository.process(makeTransaction(from, to, 10)));
    }

    @Test
    void itShouldCancelTransactionIfSrcAndTransactionHaveDifferentCurrency() {
        assertTransactionFailure("Transaction currency must be the same as of source account.", () -> {
            Transaction transaction = makeTransaction(from, to, 10);
            transaction.setCurrency(Currency.getInstance("USD"));
            transactionRepository.process(transaction);
        });
    }

    @Test
    void itShouldCancelTransactionIfSrcDoesNotExist() {
        assertTransactionFailure("Can't find account with id '-1'", () -> {
            Transaction transaction = makeTransaction(from, to, 10);
            transaction.setSrcAccountId(-1L);
            transactionRepository.process(transaction);
        });
        assertTransactionFailure("Can't find account with id '-2'", () -> {
            Transaction transaction = makeTransaction(from, to, 10);
            transaction.setDestAccountId(-2L);
            transactionRepository.process(transaction);
        });
    }

    private void assertTransactionFailure(String s, Executable executable) {
        Throwable exception = assertThrows(Exception.class, executable);
        assertBalance(100, from.getBalance());
        assertBalance(0, to.getBalance());
        assertThat(transactionRepository.getLatest().getState()).isEqualTo(TransactionState.FAILED);
        assertThat(exception.getMessage()).isEqualTo(s);
    }

    protected static Transaction makeTransaction(Account from, Account to, double amount) {
        Transaction transaction = new Transaction();
        transaction.setCurrency(from.getCurrency());
        transaction.setSrcAccountId(from.getId());
        transaction.setDestAccountId(to.getId());
        transaction.setAmount(BigDecimal.valueOf(amount));
        return transaction;
    }
}