package com.revolut.test.resources.support;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.revolut.test.api.Transaction;
import com.revolut.test.api.support.TransactionState;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransferResult {

    private Long id;
    private TransactionState state;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ZonedDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ZonedDateTime completedAt;

    public TransferResult(Transaction transaction) {
        setId(transaction.getId());
        setState(transaction.getState());
        setCreatedAt(transaction.getCreatedAt());
        setCompletedAt(transaction.getCompletedAt());
    }

}
