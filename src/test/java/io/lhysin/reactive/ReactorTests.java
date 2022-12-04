package io.lhysin.reactive;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@Slf4j
class ReactorTests {

    @Test
    public void fluxTest(){

        // partition
        Flux<Integer> integerFlux = Flux.range(0, 10)
            .filter(integer -> integer % 5 == 0);

        Consumer<Object> logging = value -> log.debug("test value!!! : {}", value);

        Mono<Integer> count = Mono.just(15);

        integerFlux.subscribe(logging);

    }

}
