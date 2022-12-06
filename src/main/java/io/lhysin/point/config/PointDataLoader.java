package io.lhysin.point.config;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.lhysin.point.dto.CreatePointReq;
import io.lhysin.point.service.PointService;
import io.lhysin.point.type.PointCreatedType;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Profile("!prd & !test")
@Component
@RequiredArgsConstructor
public class PointDataLoader implements ApplicationRunner {

    private final PointService pointService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String userId = "testUserID";
        String createdBy = "EVENT_MASTER";

        // crate Point
        Flux.range(0, 10)
            .flatMap(integer -> pointService.createPoint(CreatePointReq.builder()
                .userId(userId)
                .amount(new BigDecimal(200))
                .pointCreatedType(PointCreatedType.EVENT)
                .createdBy(createdBy)
                .build()
            )).collectList().block();
    }
}
