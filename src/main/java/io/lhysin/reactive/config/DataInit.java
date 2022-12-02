package io.lhysin.reactive.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.lhysin.reactive.entity.Account;
import io.lhysin.reactive.repository.AccountReactiveRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class DataInit {

    private final AccountReactiveRepository accountReactiveRepository;

    @PostConstruct
    public void init() {
        accountReactiveRepository.save(new Account(null, "Bill", 12.3)).block();
        Flux<Account> accountFlux = accountReactiveRepository.findAllByValue(12.3);

        Account a = accountFlux.blockFirst();
    }
}