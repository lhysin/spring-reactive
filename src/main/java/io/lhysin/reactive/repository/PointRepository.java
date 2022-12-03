package io.lhysin.reactive.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import io.lhysin.reactive.document.Point;
import reactor.core.publisher.Flux;

@Repository
public interface PointRepository extends ReactiveMongoRepository<Point, String> {
    Flux<Point> findByUserId(String userId);

    // ignore field pointTransactions
    @Query(fields="{ pointTransactions: 0 }")
    Flux<Point> findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThan(String userId, LocalDateTime now);

    // ignore field pointTransactions
    Flux<Point> findByCompleteIsFalseAndExpiredAtLessThan(LocalDateTime now);
}