package com.revolut.test;

import com.revolut.test.db.AccountRepository;
import com.revolut.test.db.TransactionRepository;
import com.revolut.test.health.RevolutHealthCheck;
import com.revolut.test.resources.AccountResource;
import com.revolut.test.resources.ObjectNotFoundExceptionMapper;
import com.revolut.test.resources.TransactionResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RevolutApplication extends Application<RevolutConfiguration> {

    private AccountRepository accountRepository = new AccountRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    public static void main(final String[] args) throws Exception {
        new RevolutApplication().run(args);
    }

    @Override
    public String getName() {
        return "Revolut";
    }

    @Override
    public void initialize(final Bootstrap<RevolutConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final RevolutConfiguration configuration, final Environment environment) {
        environment.healthChecks().register("revolut", new RevolutHealthCheck());
        environment.jersey().register(new ObjectNotFoundExceptionMapper());
        environment.jersey().register(new AccountResource(accountRepository));
        environment.jersey().register(new TransactionResource(transactionRepository));
    }

}
