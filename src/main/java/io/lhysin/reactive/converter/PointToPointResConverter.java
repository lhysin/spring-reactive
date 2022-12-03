package io.lhysin.reactive.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.PointRes;
import reactor.core.publisher.Flux;

@Component
public class PointToPointResConverter implements Converter<Point, Flux<PointRes>> {

    @NonNull
    @Override
    public Flux<PointRes> convert(Point point) {
        return Flux.just(PointRes.builder()
            .userId(point.getUserId())
            .amount(point.getAmount())
            .pointCreatedType(point.getPointCreatedType())
            .createdAt(point.getCreatedAt())
            .createdBy(point.getCreatedBy())
            .expiredAt(point.getExpiredAt())
            .build()
        );
    }
}
