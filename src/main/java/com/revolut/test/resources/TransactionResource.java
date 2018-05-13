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
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.revolut.test.api.Transaction;
import com.revolut.test.db.TransactionRepository;
import com.revolut.test.resources.support.Transfer;
import com.revolut.test.resources.support.TransferResult;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private final TransactionRepository transactionRepository;

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
        transaction.setReference(transfer.getReference());

        try {
            transactionRepository.process(transaction);

            return Response.status(Response.Status.CREATED).entity(new TransferResult(transaction)).build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Timed
    public Transaction getTransaction(@PathParam("id") Long id) {
        return transactionRepository.get(id);
    }
}
