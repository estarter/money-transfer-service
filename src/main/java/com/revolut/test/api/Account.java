package com.revolut.test.api;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class Account {

    private Long accountId;
    @NonNull
    private BigDecimal balance;
    @NonNull
    private Currency currency;
    @NonNull
    private String name;

    public Account() {
        balance = BigDecimal.valueOf(0);
        currency = Currency.getInstance("CHF");
        name = UUID.randomUUID().toString();
    }
}
