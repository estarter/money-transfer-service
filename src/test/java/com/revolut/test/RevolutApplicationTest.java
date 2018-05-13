package com.revolut.test;

import static com.revolut.test.TestHelper.assertBalance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Currency;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.revolut.test.api.Account;
import com.revolut.test.api.Transaction;
import com.revolut.test.api.support.TransactionState;
import com.revolut.test.db.AccountRepository;
import com.revolut.test.db.TransactionRepository;
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

    @Nested
    class TransactionResource {

        private AccountRepository accountRepository;
        private TransactionRepository transactionRepository;
        private Account from;
        private Account to;
        private Transfer transfer;

        @BeforeEach
        void setUp() {
            accountRepository = ((RevolutApplication) EXTENSION.getApplication()).accountRepository;
            transactionRepository = ((RevolutApplication) EXTENSION.getApplication()).transactionRepository;
            from = new Account();
            from.setBalance(BigDecimal.valueOf(100));
            accountRepository.save(from);
            to = new Account();
            to.setBalance(BigDecimal.valueOf(0));
            accountRepository.save(to);

            transfer = new Transfer();
            transfer.setFrom(from.getId());
            transfer.setTo(to.getId());
            transfer.setAmount(BigDecimal.valueOf(12.3));
            transfer.setCurrency(from.getCurrency());
            transfer.setReference("myref1");
        }

        @Test
        void testSuccessfulTransaction() {
            TransferResult result = EXTENSION.client()
                                             .target("http://localhost:" + EXTENSION.getLocalPort()
                                                     + "/api/transactions")
                                             .request(MediaType.APPLICATION_JSON_TYPE)
                                             .post(Entity.json(transfer), TransferResult.class);

            assertThat(result.getState()).isEqualTo(TransactionState.COMPLETED);
            assertBalance(87.7, accountRepository.get(from.getId()).getBalance());
            assertBalance(12.3, accountRepository.get(to.getId()).getBalance());
            assertThat(result.getId()).isNotNull();
            assertDateTime(result.getCreatedAt());
            assertDateTime(result.getCompletedAt());

            /* get transaction */
            Transaction transaction = EXTENSION.client()
                                               .target("http://localhost:" + EXTENSION.getLocalPort()
                                                       + "/api/transactions/" + result.getId())
                                               .request(MediaType.APPLICATION_JSON_TYPE)
                                               .get(Transaction.class);
            assertThat(transaction).hasFieldOrPropertyWithValue("srcAccountId", from.getId())
                                   .hasFieldOrPropertyWithValue("destAccountId", to.getId())
                                   .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(12.3))
                                   .hasFieldOrPropertyWithValue("currency", Currency.getInstance("CHF"))
                                   .hasFieldOrPropertyWithValue("state", TransactionState.COMPLETED)
                                   .hasFieldOrPropertyWithValue("reference", "myref1")
                                   .hasNoNullFieldsOrPropertiesExcept("version");
        }

        @Test
        void testFailedTransaction() {
            /* perform failing transaction */
            transfer.setAmount(BigDecimal.valueOf(1000));
            Response response2 = EXTENSION.client()
                                          .target("http://localhost:" + EXTENSION.getLocalPort() + "/api/transactions")
                                          .request()
                                          .post(Entity.json(transfer));
            assertThat(response2.getStatus()).isEqualTo(400);

            Transaction transaction = EXTENSION.client()
                                               .target("http://localhost:" + EXTENSION.getLocalPort()
                                                       + "/api/transactions/" + transactionRepository.getLatest()
                                                                                                     .getId())
                                               .request(MediaType.APPLICATION_JSON_TYPE)
                                               .get(Transaction.class);
            assertThat(transaction).hasFieldOrPropertyWithValue("srcAccountId", from.getId())
                                   .hasFieldOrPropertyWithValue("destAccountId", to.getId())
                                   .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1000))
                                   .hasFieldOrPropertyWithValue("currency", Currency.getInstance("CHF"))
                                   .hasFieldOrPropertyWithValue("state", TransactionState.FAILED)
                                   .hasFieldOrPropertyWithValue("reference", "myref1")
                                   .hasNoNullFieldsOrPropertiesExcept("completedAt", "version");
        }
    }

    private void assertDateTime(ZonedDateTime datetime) {
        assertThat(datetime).isNotNull();
        assertThat(datetime).isCloseTo(ZonedDateTime.now(), byLessThan(1, ChronoUnit.SECONDS));
    }
}
