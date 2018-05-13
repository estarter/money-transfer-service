package com.revolut.test.db.support;

public class OptimisticLockException  extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }
}
