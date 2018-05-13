package com.revolut.test.api;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revolut.test.api.support.TransactionState;

import lombok.Data;
import lombok.NonNull;

@Data
public class Transaction {

    private static final AtomicLong idGenerator = new AtomicLong();

    private Long id;
    @JsonIgnore
    private Long version;
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
    private String reference;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ZonedDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ZonedDateTime completedAt;

    public Transaction() {
        id = idGenerator.incrementAndGet();
        state = TransactionState.PENDING;
        createdAt = ZonedDateTime.now();
    }

    public Transaction(Transaction another) {
        setId(another.getId());
        setSrcAccountId(another.getSrcAccountId());
        setDestAccountId(another.getDestAccountId());
        setAmount(another.getAmount());
        setCurrency(another.getCurrency());
        setState(another.getState());
        setVersion(another.getVersion());
        setReference(another.getReference());
        setCreatedAt(another.getCreatedAt());
        setCompletedAt(another.getCompletedAt());
    }
}
