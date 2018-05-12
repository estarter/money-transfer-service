package com.revolut.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.assertj.core.data.Offset;

public class TestHelper {

    public static void assertBalance(double expected, BigDecimal actual) {
        assertThat(actual).isCloseTo(BigDecimal.valueOf(expected), Offset.offset(BigDecimal.valueOf(0.001)));
    }
}
