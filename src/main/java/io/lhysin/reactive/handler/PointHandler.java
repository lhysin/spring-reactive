package io.lhysin.reactive.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.lhysin.reactive.converter.PointToPointResConverter;
import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.CreatePointReq;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.dto.UsePointReq;
import io.lhysin.reactive.repository.PointRepository;
import io.lhysin.reactive.service.PointService;
import io.lhysin.reactive.type.PointCreatedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointHandler {

    private final PointToPointResConverter pointToPointResConverter;
    private final PointRepository pointRepository;
    private final PointService pointService;

    public Mono<ServerResponse> createPoint(ServerRequest req) {
        Mono<Point> pointMono = req.bodyToMono(CreatePointReq.class)
            .flatMap(pointService::createPoint);
        return status(HttpStatus.CREATED)
            .body(pointMono, Void.class);
    }

    public Mono<ServerResponse> findByUserIdAndNotExpired(ServerRequest req) {
        String userId = req.pathVariable("userId");
        Pageable pageable = PageRequest.of(0, 10);
        Flux<Point> pointFlux = pointService.findByUserIdAndNotExpired(userId,  pageable);

        return ok()
            .body(pointFlux, Point.class);
    }

    public Flux<Point> usePoint(UsePointReq req) {
        return pointRepository.count().flatMapMany(aLong -> {

            int pageSize = 10;
            AtomicInteger pageNumber = new AtomicInteger(0);
            AtomicReference<BigDecimal> reqAmount = new AtomicReference<>(req.getAmount());

            Flux<Point> pointFlux = Flux.range(0, Math.toIntExact(aLong))
                // 0 ~ n => 10, 20, 30 ...
                .filter(value -> value % pageSize == 0)

                // get partitioning flux
                .flatMap(it -> {
                    Pageable page = PageRequest.of(pageNumber.getAndIncrement(), pageSize);
                    return pointRepository.findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThanOrderByCreatedAtAsc(
                        req.getUserId(),
                        LocalDateTime.now(), page);

                }).filter(point -> {
                    // pass element
                    // check available consume point amount. (amount GreaterThan or Equal)
                    return point.getAmount().compareTo(point.getConsumedAmount()) > 0;
                }).takeWhile(point -> {
                    // prevent loop
                    // check until request amount is positive.
                    return reqAmount.get().compareTo(BigDecimal.ZERO) > 0;
                }).map(point -> {
                    long amount = point.getAmount().longValue();
                    long consumedAmount = point.getConsumedAmount().longValue();
                    long reqAmountLong = reqAmount.get().longValue();

                    long currentConsumeAmount = reqAmountLong;
                    if(reqAmountLong > (amount - consumedAmount)) {
                        currentConsumeAmount = reqAmountLong - (amount - consumedAmount);
                    }

                    point.addConsumedAmount(new BigDecimal(currentConsumeAmount));
                    reqAmount.set(new BigDecimal(reqAmountLong - currentConsumeAmount));

                    point.updateCompletePoint();
                    point.getPointTransactions().add(
                        Point.PointTransaction.builder()
                        .amount(point.getConsumedAmount())
                        .pointTransactionType(req.getPointTransactionType())
                        .createdAt(LocalDateTime.now())
                        .createdBy(req.getCreatedBy())
                        .build());

                    return point;
                });

            return pointRepository.saveAll(pointFlux);
        });

    }

    public Flux<Point> saveAll(Flux<Point> points) {
        return pointRepository.saveAll(points);
    }

    public Flux<PointRes> findByUserId(String userId) {
        return pointRepository.findByUserId(userId)
            .flatMap(pointToPointResConverter::convert);
    }

    // public Flux<PointRes> findByUserId(String userId) {
    //     return pointRepository.findByUserId(userId)
    //         .flatMap(pointToPointResConverter::convert);
    // }

    public Flux<Point> createInit() {
        Flux<Point> points = Flux.fromStream(IntStream.range(1, 10)
            .mapToObj(i -> {
                return Point.builder()
                    .userId("userId")
                    .amount(BigDecimal.valueOf(1000L))
                    .pointCreatedType(PointCreatedType.EVENT)
                    .expiredAt(LocalDateTime.now().plusYears(2L))
                    .createdAt(LocalDateTime.now())
                    .createdBy("EVENT_MASTER")
                    .build();
            })
        );
        return pointRepository.saveAll(points);
    }

    public Mono<BigDecimal> findAvailablePointAmountByUserId(String userId) {
        return pointRepository.count().flatMap(aLong -> {

            int count = Math.toIntExact(aLong);
            int pageSize = 10;
            AtomicInteger pageNumber = new AtomicInteger(0);

            return Flux.range(0, count)
                // 0 ~ n => 10, 20, 30 ...
                .filter(value -> value % pageSize == 0)

                // get partitioning flux
                .flatMap(it -> {
                    Pageable page = PageRequest.of(pageNumber.getAndIncrement(), pageSize);
                    return pointRepository.findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThan(userId,
                        LocalDateTime.now(), page);

                    // calculate available point
                }).map(point -> point.getAmount().subtract(point.getConsumedAmount())

                    // reduce available point
                ).reduce(BigDecimal::add);
        });
    }

        // return pointRepository.findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThan(userId, LocalDateTime.now())
        //     .map(point -> Optional.ofNullable(point.getAmount()).orElse(BigDecimal.ZERO)
        //         .subtract(Optional.ofNullable(point.getConsumedAmount()).orElse(BigDecimal.ZERO)))
        //     .reduce(BigDecimal::add);


    public Flux<Point> completeExpiredPoint() {
        return pointRepository.findByCompleteIsFalseAndExpiredAtLessThan(LocalDateTime.now())
            .flatMap(point -> {
                point.updateCompletePoint();
                return pointRepository.save(point);
            });
    }
}
