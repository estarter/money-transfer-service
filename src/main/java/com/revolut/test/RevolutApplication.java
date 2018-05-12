package com.revolut.test;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RevolutApplication extends Application<RevolutConfiguration> {

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
    public void run(final RevolutConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
