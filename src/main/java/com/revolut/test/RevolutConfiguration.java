package com.revolut.test;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;
import lombok.Data;

@Data
public class RevolutConfiguration extends Configuration {

    @NotNull
    private Integer lockTimeout;
}
