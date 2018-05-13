package com.revolut.test.resources.support;

import java.time.format.DateTimeFormatter;

import com.revolut.test.api.Transaction;
import com.revolut.test.api.support.TransactionState;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransferResult {

    private Long id;
    private TransactionState state;
    private String createdAt;
    private String completedAt;

    public TransferResult(Transaction transaction) {
        setId(transaction.getId());
        setState(transaction.getState());

        if (transaction.getCreatedAt() != null) {
            setCreatedAt(transaction.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        if (transaction.getCompletedAt() != null) {
            setCompletedAt(transaction.getCompletedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

}
