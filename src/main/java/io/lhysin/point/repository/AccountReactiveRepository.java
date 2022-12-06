package io.lhysin.point.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import io.lhysin.point.document.Account;
import reactor.core.publisher.Flux;

@Repository
public interface AccountReactiveRepository extends ReactiveMongoRepository<Account, String> {
    Flux<Account> findAllByValue(Double value);
}