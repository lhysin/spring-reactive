package io.lhysin.point.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.lhysin.point.converter.CreatePointReqToPointConverter;
import io.lhysin.point.converter.PointToPointResConverter;
import io.lhysin.point.document.Point;
import io.lhysin.point.dto.CancelPointReq;
import io.lhysin.point.dto.CreatePointReq;
import io.lhysin.point.dto.PointRes;
import io.lhysin.point.dto.UsePointReq;
import io.lhysin.point.repository.PointRepository;
import io.lhysin.point.type.PointTransactionType;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final CreatePointReqToPointConverter createPointReqToPointConverter;
    private final PointToPointResConverter pointToPointResConverter;

    public Mono<Point> createPoint(CreatePointReq req) {
        return pointRepository.save(createPointReqToPointConverter.convert(req));
    }

    public Flux<PointRes> findByUserIdAndNotExpired(String userId, Pageable pageable) {
        return pointRepository.findByUserIdAndExpiredAtGreaterThanOrderByCreatedAtDesc(
                userId,
                LocalDateTime.now(), pageable)
            .flatMap(pointToPointResConverter::convert);
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
                    if (reqAmountLong > (amount - consumedAmount)) {
                        currentConsumeAmount = reqAmountLong - (amount - consumedAmount);
                    }

                    point.addConsumedAmount(new BigDecimal(reqAmountLong - currentConsumeAmount));
                    reqAmount.set(reqAmount.get().subtract(point.getConsumedAmount()));

                    point.updateCompletePoint();
                    point.getPointTransactions().add(
                        Point.PointTransaction.builder()
                            .amount(point.getConsumedAmount())
                            .pointTransactionType(PointTransactionType.USE)
                            .createdAt(LocalDateTime.now())
                            .createdBy(req.getCreatedBy())
                            .build());

                    return point;
                });

            return pointRepository.saveAll(pointFlux);
        });
    }

    public Flux<Point> cancelPoint(CancelPointReq req) {
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
                    return pointRepository.findByUserIdAndExpiredAtGreaterThanOrderByCreatedAtDesc(
                        req.getUserId(),
                        LocalDateTime.now(), page);

                }).filter(point -> {
                    // pass element
                    // check remain consume point amount.
                    return point.getConsumedAmount().compareTo(BigDecimal.ZERO) > 0;
                }).takeWhile(point -> {
                    // prevent loop
                    // check until request amount is positive.
                    return reqAmount.get().compareTo(BigDecimal.ZERO) > 0;
                }).map(point -> {
                    long amount = point.getAmount().longValue();
                    long consumedAmount = point.getConsumedAmount().longValue();
                    long reqAmountLong = reqAmount.get().longValue();

                    long currentCancelAmount = reqAmountLong;
                    if (reqAmountLong > consumedAmount) {
                        currentCancelAmount = reqAmountLong - consumedAmount;
                    }

                    point.subtractConsumedAmount(new BigDecimal(reqAmountLong - currentCancelAmount));
                    reqAmount.set(new BigDecimal(reqAmountLong - currentCancelAmount));

                    point.updateCompletePoint();
                    point.getPointTransactions().add(
                        Point.PointTransaction.builder()
                            .amount(point.getConsumedAmount())
                            .pointTransactionType(PointTransactionType.CANCEL)
                            .createdAt(LocalDateTime.now())
                            .createdBy(req.getCreatedBy())
                            .build());

                    return point;
                });

            return pointRepository.saveAll(pointFlux);
        });
    }

    public Mono<BigDecimal> findAvailablePointAmountByUserId(String userId) {
        return pointRepository.count().flatMap(aLong -> {

            int count = Math.toIntExact(aLong);
            int pageSize = 10;
            AtomicInteger pageNumber = new AtomicInteger(0);

            if(aLong < 1) {
                return Mono.just(BigDecimal.ZERO);
            }

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

    public Flux<Point> completeExpiredPoint() {
        return pointRepository.findByCompleteIsFalseAndExpiredAtLessThan(LocalDateTime.now())
            .flatMap(point -> {
                point.updateCompletePoint();
                return pointRepository.save(point);
            });
    }

    // test
    public Flux<PointRes> findByUserId(String userId) {
        return pointRepository.findByUserId(userId)
            .flatMap(pointToPointResConverter::convert);
    }
}
