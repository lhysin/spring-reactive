package io.lhysin.reactive.handler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import io.lhysin.reactive.converter.PointToPointResConverter;
import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.repository.PointRepository;
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

    public Mono<Point> findById(String id) {
        return pointRepository.findById(id);
    }

    public Flux<Point> saveAll(Flux<Point> points) {
        return pointRepository.saveAll(points);
    }

    public Flux<PointRes> findByUserId(String userId) {
        return pointRepository.findByUserId(userId)
            .flatMap(pointToPointResConverter::convert);
    }

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

    public Mono<BigDecimal> findAvailablePoint(String userId) {
        return pointRepository.findByUserIdAndCompleteIsFalseAndExpiredAtGreaterThan(userId, LocalDateTime.now())
            .map(point -> Optional.ofNullable(point.getAmount()).orElse(BigDecimal.ZERO)
                .subtract(Optional.ofNullable(point.getConsumedAmount()).orElse(BigDecimal.ZERO)))
            .reduce(BigDecimal::add);
    }

    public Flux<Point> completeExpiredPoint() {
        return pointRepository.findByCompleteIsFalseAndExpiredAtLessThan(LocalDateTime.now())
            .flatMap(point -> {
                point.completePoint();
                return pointRepository.save(point);
            });
    }
}
