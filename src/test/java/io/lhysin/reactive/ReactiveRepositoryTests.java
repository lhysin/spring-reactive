package io.lhysin.reactive;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.lhysin.reactive.document.Account;
import io.lhysin.reactive.repository.AccountReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest
//@ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ContextConfiguration(classes = {SpringReactiveApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReactiveRepositoryTests {

    @Autowired
    private AccountReactiveRepository accountReactiveRepository;

    @Test
    public void givenValue_whenFindAllByValue_thenFindAccount() {
        accountReactiveRepository.save(Account.builder()
            .owner("Bill")
            .value(12.3)
            .build()).block();
        Flux<Account> accountFlux = accountReactiveRepository.findAllByValue(12.3);

        StepVerifier
            .create(accountFlux)
            .assertNext(account -> {
                assertEquals("Bill", account.getOwner());
                assertEquals(Double.valueOf(12.3), account.getValue());
                assertNotNull(account.getId());
            })
            .expectComplete()
            .verify();
    }

}
