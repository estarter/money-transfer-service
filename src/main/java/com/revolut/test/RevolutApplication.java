package com.revolut.test;

import com.revolut.test.db.AccountRepository;
import com.revolut.test.db.TransactionRepository;
import com.revolut.test.health.RevolutHealthCheck;
import com.revolut.test.resources.AccountResource;
import com.revolut.test.resources.TransactionResource;
import com.revolut.test.resources.support.ObjectNotFoundExceptionMapper;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RevolutApplication extends Application<RevolutConfiguration> {

    AccountRepository accountRepository;
    TransactionRepository transactionRepository;

    public static void main(final String[] args) throws Exception {
        new RevolutApplication().run(args);
    }

    @Override
    public String getName() {
        return "Revolut";
    }

    @Override
    public void initialize(final Bootstrap<RevolutConfiguration> bootstrap) {
    }

    @Override
    public void run(final RevolutConfiguration configuration, final Environment environment) {
        log.info("Set lock timeout to {} secs", configuration.getLockTimeout());
        accountRepository = new AccountRepository(configuration.getLockTimeout());
        transactionRepository = new TransactionRepository(configuration.getLockTimeout(), accountRepository);

        environment.healthChecks().register("revolut", new RevolutHealthCheck());
        environment.jersey().register(new ObjectNotFoundExceptionMapper());
        environment.jersey().register(new AccountResource(accountRepository));
        environment.jersey().register(new TransactionResource(transactionRepository));
    }

}
