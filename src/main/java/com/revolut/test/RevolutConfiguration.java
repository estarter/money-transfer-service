package com.revolut.test;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class RevolutConfiguration extends Configuration {

    @NotNull
    private Integer lockTimeout;
}
