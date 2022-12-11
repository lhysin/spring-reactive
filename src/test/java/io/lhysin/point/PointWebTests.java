package io.lhysin.point;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import io.lhysin.point.dto.CancelPointReq;
import io.lhysin.point.dto.CreatePointReq;
import io.lhysin.point.dto.PointRes;
import io.lhysin.point.dto.UsePointReq;
import io.lhysin.point.type.PointCreatedType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@SpringBootTest(classes = SpringReactiveApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ActiveProfiles("test")
class PointWebTests {

    @LocalServerPort
    private int port;

    // helper methods to create default instances
    private WebClient createDefaultClient() {
        HttpClient httpClient = HttpClient
            .create()
            .wiretap(true);
        return WebClient.builder()
            .baseUrl("http://localhost:" + port)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Test
    public void webClientTest() {
        WebClient client = createDefaultClient();

        Flux<PointRes> pointResFlux = client.get().uri("/points/testUserID")
            .retrieve()
            .bodyToFlux(PointRes.class);

        List<PointRes> pointResList = pointResFlux.collectList().block();
        log.debug("pointResList : {}", pointResList);
    }

    @Test
    public void createTest() {
        WebClient client = createDefaultClient();
        String userId = "createTest";

        Mono<BigDecimal> availablePointMono = client.get().uri("/points/" + userId + "/summary")
            .retrieve()
            .bodyToMono(BigDecimal.class);

        Mono<Void> createMono = client.post().uri("/points")
            .body(Mono.just(CreatePointReq.builder()
                .userId(userId)
                .amount(new BigDecimal(300))
                .pointCreatedType(PointCreatedTgitype.EVENT)
                .createdBy("MY")
                .build()), CreatePointReq.class)
            .retrieve()
            .bodyToMono(Void.class);

        Flux<Void> create20Req = Flux.range(0, 20)
            .flatMap(integer -> createMono);

        log.debug("available Point!!!! : {}", availablePointMono.block());
        create20Req.collectList().block();
        log.debug("available Point!!!! : {}", availablePointMono.block());

        // TODO usePont and cancel point

        Mono<Void> useMono = client.put().uri("/points/use")
            .body(Mono.just(UsePointReq.builder()
                .userId(userId)
                .amount(new BigDecimal(1500))
                .createdBy("MY")
                .build()), CreatePointReq.class)
            .retrieve()
            .bodyToMono(Void.class);

        useMono.block();
        log.debug("available Point!!!! : {}", availablePointMono.block());


        Mono<Void> cancelMono = client.patch().uri("/points/cancel")
            .body(Mono.just(CancelPointReq.builder()
                .userId(userId)
                .amount(new BigDecimal(1000))
                .createdBy("MY")
                .build()), CreatePointReq.class)
            .retrieve()
            .bodyToMono(Void.class);

        cancelMono.block();
        log.debug("available Point!!!! : {}", availablePointMono.block());

        // TODO required StepVerifier

    }

}
