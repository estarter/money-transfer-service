package com.revolut.test.resources;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Data;

@Data
public class Transfer {
    private Long from;
    private Long to;
    private BigDecimal amount;
    private Currency currency;

}
