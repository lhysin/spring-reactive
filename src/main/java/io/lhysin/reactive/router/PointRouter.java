package io.lhysin.reactive.router;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.lhysin.reactive.handler.PointHandler;

@Configuration
public class PointRouter {

    @Bean
    public RouterFunction<ServerResponse> point(PointHandler pointHandler) {

        return route(
            POST("/points/{userId}"), pointHandler::createPoint)

            .andRoute(
                PUT("/points/{userId}/use"), pointHandler::usePoint)

            .andRoute(
                PATCH("/points/{userId}/cancel"), pointHandler::cancelPoint)

            .andRoute(
                GET("/points/{userId}")
                    .and(accept(APPLICATION_JSON)), pointHandler::findByUserIdAndNotExpired)

            .andRoute(
                GET("/points/{userId}/summary")
                    .and(accept(APPLICATION_JSON)), pointHandler::findAvailablePointAmountByUserId);

    }

}
