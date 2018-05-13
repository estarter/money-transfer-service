package com.revolut.test;

import static com.revolut.test.TestHelper.assertBalance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.revolut.test.api.Account;
import com.revolut.test.api.support.TransactionState;
import com.revolut.test.db.AccountRepository;
import com.revolut.test.resources.support.Transfer;
import com.revolut.test.resources.support.TransferResult;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(DropwizardExtensionsSupport.class)
class RevolutApplicationTest {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test.yml");

    private static final DropwizardAppExtension<RevolutConfiguration> EXTENSION = new DropwizardAppExtension<>(
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
        assertThat(origAccount).isEqualToIgnoringGivenFields(retrievedAccount, "id", "version");

        ids = EXTENSION.client()
                       .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/accounts")
                       .request()
                       .get(Long[].class);
        assertThat(ids).hasSize(1);
        assertThat(accountUrl).endsWith("/" + ids[0]);
    }

    @Test
    void testTransactionResource() {
        AccountRepository accountRepository = ((RevolutApplication) EXTENSION.getApplication()).accountRepository;
        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(100));
        accountRepository.save(from);
        Account to = new Account();
        to.setBalance(BigDecimal.valueOf(0));
        accountRepository.save(to);

        Transfer transfer = new Transfer();
        transfer.setFrom(from.getId());
        transfer.setTo(to.getId());
        transfer.setAmount(BigDecimal.valueOf(12.3));
        transfer.setCurrency(from.getCurrency());
        transfer.setReference("myref1");
        TransferResult result = EXTENSION.client()
                                         .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/transactions")
                                         .request(MediaType.APPLICATION_JSON_TYPE)
                                         .post(Entity.json(transfer), TransferResult.class);

        assertThat(result.getState()).isEqualTo(TransactionState.COMPLETED);
        assertBalance(87.7, accountRepository.get(from.getId()).getBalance());
        assertBalance(12.3, accountRepository.get(to.getId()).getBalance());
        assertThat(result.getId()).isNotNull();
        assertDateTime(result.getCreatedAt());
        assertDateTime(result.getCompletedAt());

        transfer.setAmount(BigDecimal.valueOf(100));
        Response response2 = EXTENSION.client()
                                      .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/transactions")
                                      .request()
                                      .post(Entity.json(transfer));
        assertThat(response2.getStatus()).isEqualTo(400);
    }

    private void assertDateTime(String datetime) {
        assertThat(datetime).isNotNull();
        assertThat(LocalDateTime.parse(datetime)).isCloseTo(LocalDateTime.now(), byLessThan(1, ChronoUnit.SECONDS));
    }
}
