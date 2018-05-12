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
import com.revolut.test.api.Account;
import com.revolut.test.db.AccountRepository;

@Path("/api/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    private final AccountRepository accountRepository;

    public AccountResource(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GET
    @Timed
    public Set<Long> getAllAccount() {
        return accountRepository.getAll();
    }

    @POST
    @Timed
    public Response createAccount(Account account, @Context UriInfo uriInfo) {
        accountRepository.save(account);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Long.toString(account.getId()));
        return Response.created(builder.build()).build();
    }

    @GET
    @Path("/{id}")
    @Timed
    public Account getAccount(@PathParam("id") Long id) {
        return accountRepository.get(id);
    }
}
