package com.revolut.test.resources.support;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import lombok.Data;

@Data
public class Transfer {

    private UUID from;
    private UUID to;
    private BigDecimal amount;
    private Currency currency;

}
