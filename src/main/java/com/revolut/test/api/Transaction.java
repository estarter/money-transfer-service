package com.revolut.test.api;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.atomic.AtomicLong;

import com.revolut.test.api.support.TransactionState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class Transaction {

    private static final AtomicLong idGenerator = new AtomicLong();

    private Long id;
    @NonNull
    private Long srcAccountId;
    @NonNull
    private Long destAccountId;
    @NonNull
    private BigDecimal amount;
    @NonNull
    private Currency currency;
    @NonNull
    private TransactionState state;

    public Transaction() {
        id = idGenerator.incrementAndGet();
        state = TransactionState.CREATED;
    }
}
