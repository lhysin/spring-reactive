package io.lhysin.point.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import io.lhysin.point.document.Point;
import io.lhysin.point.dto.PointRes;
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
