package io.lhysin.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class PointWebTests {

    @Autowired
    private ApplicationContext context;

    @Test
    public void webClientTest() {
        WebTestClient testClient = WebTestClient
            .bindToApplicationContext(context)
            .build();

        testClient.get().uri("/points/asd")
            .exchange()
            .expectStatus().isOk();

    }
}
