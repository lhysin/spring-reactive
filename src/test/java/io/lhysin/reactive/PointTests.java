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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.lhysin.reactive.converter.PointToPointResConverter;
import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.handler.PointHandler;
import io.lhysin.reactive.type.PointCreatedType;
import io.lhysin.reactive.type.PointTransactionType;
import io.lhysin.reactive.type.PointType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@DataMongoTest
//@ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ContextConfiguration(classes = {SpringReactiveApplication.class, PointHandler.class, PointToPointResConverter.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class PointTests {

    @Autowired
    private PointHandler pointHandler;

    @Test
    public void givenValue_whenFindAllByValue_thenFindAccount() {
        ;

        String userId = "testUserID";
        String createdBy = "EVENT_MASTER";

        Flux<Point> points = Flux.fromStream(IntStream.range(1, 3)
            .mapToObj(idx -> {
                List<Point.PointTransaction> trs = IntStream.range(1, 10)
                    .mapToObj(jdx -> Point.PointTransaction.builder()
                        .pointTransactionType(PointTransactionType.values()[new Random().nextInt(PointTransactionType.values().length)])
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
                    .expiredAt(LocalDateTime.now().plusYears((long)idx))
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .build();
            })
        );

        pointHandler.saveAll(points).collectList().block();

        List<PointRes> po = pointHandler.findByUserId(userId).collectList().block();

        BigDecimal availableAmount = pointHandler.findAvailablePoint(userId).block();

        log.debug("pointHandler.findAvailablePoint() availableAmount : {}", availableAmount);


        // StepVerifier
        //     .create(pointHandler.findByUserId(userId).collectList())
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
