package com.revolut.test;

import static com.revolut.test.TestHelper.assertBalance;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.revolut.test.api.Account;
import com.revolut.test.resources.support.Transfer;

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
    void testAccountResource() {
        Long[] ids = EXTENSION.client()
                              .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                              .request()
                              .get(Long[].class);
        assertThat(ids).isEmpty();

        Account origAccount = new Account();
        origAccount.setBalance(BigDecimal.valueOf(12.3));
        Response response = EXTENSION.client()
                                     .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                                     .request()
                                     .post(Entity.json(origAccount));

        assertThat(response.getStatus()).isEqualTo(201);
        String accountUrl = response.getHeaderString("Location");
        Account retrievedAccount = EXTENSION.client().target(accountUrl).request().get(Account.class);
        assertThat(origAccount).isEqualToIgnoringGivenFields(retrievedAccount, "id");

        ids = EXTENSION.client()
                       .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                       .request()
                       .get(Long[].class);
        assertThat(ids).hasSize(1);
        assertThat(accountUrl).endsWith("/" + ids[0]);
    }

    @Test
    void testTransactionResource() {
        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(100));
        ((RevolutApplication) EXTENSION.getApplication()).accountRepository.save(from);
        Account to = new Account();
        to.setBalance(BigDecimal.valueOf(0));
        ((RevolutApplication) EXTENSION.getApplication()).accountRepository.save(to);

        Transfer transfer = new Transfer();
        transfer.setFrom(from.getId());
        transfer.setTo(to.getId());
        transfer.setAmount(BigDecimal.valueOf(12.3));
        transfer.setCurrency(from.getCurrency());
        Response response = EXTENSION.client()
                                     .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/transactions")
                                     .request()
                                     .post(Entity.json(transfer));

        assertThat(response.getStatus()).isEqualTo(201);
        assertBalance(87.7, from.getBalance());
        assertBalance(12.3, to.getBalance());

        transfer.setAmount(BigDecimal.valueOf(100));
        response = EXTENSION.client()
                            .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/transactions")
                            .request()
                            .post(Entity.json(transfer));
        System.out.println("response = " + response);
    }
}
