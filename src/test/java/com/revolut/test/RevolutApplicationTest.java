package com.revolut.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.revolut.test.api.Account;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(DropwizardExtensionsSupport.class)
class RevolutApplicationTest {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test.yml");

    public static final DropwizardAppExtension<RevolutConfiguration> EXTENSION = new DropwizardAppExtension<>(
            RevolutApplication.class, CONFIG_PATH);

    @Test
    public void testAccountResource() {
        Long[] ids = EXTENSION.client()
                                   .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                                   .request()
                                   .get(Long[].class);
        assertThat(ids).isEmpty();

        Account origAccount = Account.builder()
                                 .name("test1")
                                 .balance(BigDecimal.valueOf(12.3))
                                 .currency(Currency.getInstance("CHF"))
                                 .build();
        Response response = EXTENSION.client()
                                  .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                                  .request()
                                  .post(Entity.json(origAccount));

        String accountUrl = response.getHeaderString("Location");
        Account retrievedAccount = EXTENSION.client()
                                            .target(accountUrl)
                                            .request()
                                            .get(Account.class);
        assertThat(origAccount).isEqualToIgnoringGivenFields(retrievedAccount, "accountId");

        ids = EXTENSION.client()
                       .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                       .request()
                       .get(Long[].class);
        assertThat(ids).hasSize(1);
        assertThat(accountUrl).endsWith("/" + ids[0]);
    }

}