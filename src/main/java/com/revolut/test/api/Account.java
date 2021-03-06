package com.revolut.test.api;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;

@Data
public class Account implements DataObject<Long> {

    private static final AtomicLong idGenerator = new AtomicLong();

    private Long id;
    private Long version;
    private BigDecimal balance;
    private Currency currency;
    private String name;

    public Account() {
        id = idGenerator.incrementAndGet();
        balance = BigDecimal.valueOf(0);
        currency = Currency.getInstance("CHF");
        name = UUID.randomUUID().toString();
    }

}
