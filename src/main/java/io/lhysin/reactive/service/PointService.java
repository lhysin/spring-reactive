package io.lhysin.reactive.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.lhysin.reactive.converter.CreatePointReqToPointConverter;
import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.CreatePointReq;
import io.lhysin.reactive.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final CreatePointReqToPointConverter createPointReqToPointConverter;

    public Mono<Point> createPoint(CreatePointReq req) {
        return pointRepository.save(createPointReqToPointConverter.convert(req));
    }

    public Flux<Point> findByUserIdAndNotExpired(String userId, Pageable pageable) {
        return pointRepository.findByUserIdAndExpiredAtGreaterThanOrderByCreatedAtDesc(
            userId,
            LocalDateTime.now(), pageable);
    }
}
