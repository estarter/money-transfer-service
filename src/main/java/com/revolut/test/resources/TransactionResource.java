package com.revolut.test.resources;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.revolut.test.api.Transaction;
import com.revolut.test.db.TransactionRepository;
import com.revolut.test.resources.support.Transfer;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private TransactionRepository transactionRepository;

    public TransactionResource(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GET
    @Timed
    public Set<Long> getAll() {
        return transactionRepository.getAll();
    }

    @POST
    @Timed
    public Response transfer(Transfer transfer, @Context UriInfo uriInfo) {
        Transaction transaction = new Transaction();
        transaction.setSrcAccountId(transfer.getFrom());
        transaction.setDestAccountId(transfer.getTo());
        transaction.setAmount(transfer.getAmount());
        transaction.setCurrency(transfer.getCurrency());

        transactionRepository.process(transaction);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Long.toString(transaction.getId()));
        return Response.created(builder.build()).build();
    }

    @GET
    @Path("/{id}")
    @Timed
    public Transaction getAccount(@PathParam("id") Long id) {
        return transactionRepository.get(id);
    }
}
