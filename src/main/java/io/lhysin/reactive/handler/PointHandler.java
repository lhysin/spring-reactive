package io.lhysin.reactive.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import java.math.BigDecimal;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.CreatePointReq;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.dto.UsePointReq;
import io.lhysin.reactive.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointHandler {

    private final PointService pointService;

    public Mono<ServerResponse> createPoint(ServerRequest req) {
        Mono<Point> pointMono = req.bodyToMono(CreatePointReq.class)
            .flatMap(pointService::createPoint);
        return status(HttpStatus.CREATED)
            .body(pointMono, Void.class);
    }

    public Mono<ServerResponse> usePoint(ServerRequest req) {
        String userId = req.pathVariable("userId");
        Flux<Point> pointFlux = req.bodyToMono(UsePointReq.class)
            .flatMapMany(pointService::usePoint);

        return status(HttpStatus.NO_CONTENT)
            .body(pointFlux, Void.class);
    }

    public Mono<ServerResponse> cancelPoint(ServerRequest req) {
        String userId = req.pathVariable("userId");
        Flux<Point> pointFlux = req.bodyToMono(UsePointReq.class)
            // TODO
            .flatMapMany(usePointReq -> Flux.just(Point.builder().build()));

        return status(HttpStatus.NO_CONTENT)
            .body(pointFlux, Void.class);
    }

    public Mono<ServerResponse> findByUserIdAndNotExpired(ServerRequest req) {
        String userId = req.pathVariable("userId");
        // TODO Pageable
        Pageable pageable = PageRequest.of(0, 10);
        Flux<PointRes> pointFlux = pointService.findByUserIdAndNotExpired(userId, pageable);

        return ok()
            .body(pointFlux, PointRes.class);
    }

    public Mono<ServerResponse> findAvailablePointAmountByUserId(ServerRequest req) {
        String userId = req.pathVariable("userId");
        Mono<BigDecimal> availablePointAmount = pointService.findAvailablePointAmountByUserId(userId);

        return ok()
            .body(availablePointAmount, BigDecimal.class)
            .switchIfEmpty(ServerResponse.notFound().build());
    }

}
