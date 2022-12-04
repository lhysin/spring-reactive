package io.lhysin.reactive.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import io.lhysin.reactive.document.Point;
import reactor.core.publisher.Flux;

@Repository
public interface PointRepository extends ReactiveMongoRepository<Point, String> {

    Flux<Point> findByUserId(String userId);

    Flux<Point> findByUserIdAndExpiredAtGreaterThanOrderByCreatedAtDesc(String userId, LocalDateTime now,
        Pageable pageable);

    Flux<Point> findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThanOrderByCreatedAtAsc(String userId,
        LocalDateTime now, Pageable pageable);

    //@Query(value = "{ userId : ?0, complete : false, expiredAt : { $gt : { $date : ?1}}}", fields="{ pointTransactions: 0 }")
    // ignore field pointTransactions
    @Query(fields = "{ pointTransactions: 0 }")
    Flux<Point> findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThan(String userId, LocalDateTime now,
        Pageable pageable);

    // ignore field pointTransactions
    Flux<Point> findByCompleteIsFalseAndExpiredAtLessThan(LocalDateTime now);
}