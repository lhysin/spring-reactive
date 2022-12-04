package io.lhysin.reactive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.lhysin.reactive.config.PointDataLoader;
import io.lhysin.reactive.converter.CreatePointReqToPointConverter;
import io.lhysin.reactive.converter.PointToPointResConverter;
import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.dto.UsePointReq;
import io.lhysin.reactive.repository.PointRepository;
import io.lhysin.reactive.service.PointService;
import io.lhysin.reactive.type.PointCreatedType;
import io.lhysin.reactive.type.PointTransactionType;
import io.lhysin.reactive.type.PointType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@DataMongoTest
//@ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ContextConfiguration(classes = {
    SpringReactiveApplication.class,
    PointDataLoader.class,
    PointService.class,
    PointRepository.class,
    PointToPointResConverter.class,
    CreatePointReqToPointConverter.class})

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class PointTests {

    @Autowired
    private PointService pointService;
    @Autowired
    private PointRepository pointRepository;

    @Test
    public void usePointTest() {
        String userId = "testUserID";
        String createdBy = "EVENT_MASTER";

        // found points
        Pageable pageable = PageRequest.of(0, 10);
        pointService.findByUserIdAndNotExpired(userId, pageable)
            .collectList()
            .block()
            .stream()
            .forEach(point -> log.debug("available point : {}", point));

        BigDecimal availablePointAmount = pointService.findAvailablePointAmountByUserId(userId).block();
        log.debug("availablePointAmount : {}", availablePointAmount);

        pointService.usePoint(UsePointReq.builder()
            .userId(userId)
            .amount(new BigDecimal(100))
            .pointTransactionType(PointTransactionType.USE)
            .createdBy(createdBy)
            .build()).collectList().block();

        pointService.usePoint(UsePointReq.builder()
            .userId(userId)
            .amount(new BigDecimal(100))
            .pointTransactionType(PointTransactionType.USE)
            .createdBy(createdBy)
            .build()).collectList().block();

        pointService.usePoint(UsePointReq.builder()
            .userId(userId)
            .amount(new BigDecimal(100))
            .pointTransactionType(PointTransactionType.USE)
            .createdBy(createdBy)
            .build()).collectList().block();

        BigDecimal afterAvailablePointAmount = pointService.findAvailablePointAmountByUserId(userId).block();
        log.debug("afterAvailablePointAmount : {}", afterAvailablePointAmount);

        List<PointRes> finalPoints = pointService.findByUserIdAndNotExpired(userId, pageable).collectList().block();
        log.debug("finalPoints : {}", finalPoints);
    }

    @Test
    public void eeeee() {

        String userId = "testUserID";
        String createdBy = "EVENT_MASTER";

        Flux<Point> points = Flux.fromStream(IntStream.range(1, 3)
            .mapToObj(idx -> {
                List<Point.PointTransaction> trs = IntStream.range(1, 10)
                    .mapToObj(jdx -> Point.PointTransaction.builder()
                        .pointTransactionType(
                            PointTransactionType.values()[new Random().nextInt(PointTransactionType.values().length)])
                        .amount(BigDecimal.valueOf(100L))
                        .createdBy(createdBy)
                        .createdAt(LocalDateTime.now())
                        .build()
                    ).collect(Collectors.toList());

                return Point.builder()
                    .userId(userId)
                    .amount(BigDecimal.valueOf(1000L))
                    .consumedAmount(BigDecimal.ZERO)
                    .complete(false)
                    .pointType(PointType.NORMAL)
                    .pointCreatedType(PointCreatedType.EVENT)
                    .pointTransactions(trs)
                    .expiredAt(LocalDateTime.now().plusYears(idx))
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .build();
            })
        );

        //pointService.saveAll(points).collectList().block();

        List<PointRes> po = pointService.findByUserId(userId).collectList().block();

        BigDecimal availableAmount = pointService.findAvailablePointAmountByUserId(userId).block();

        log.debug("pointService.findAvailablePoint() availableAmount : {}", availableAmount);

        // StepVerifier
        //     .create(pointService.findByUserId(userId).collectList())
        //     .assertNext(pointList -> {
        //         for (Point p : pointList) {
        //             log.debug("created point : {}", p);
        //             for (Point.PointTransaction a : p.getPointTransactions()) {
        //                 log.debug("created pointAction : {}", a);
        //             }
        //         }
        //     });

    }

}
