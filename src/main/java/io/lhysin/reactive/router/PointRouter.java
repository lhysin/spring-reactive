package io.lhysin.reactive.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.handler.PointHandler;

@Configuration
public class PointRouter {
    // @Bean
    // public RouterFunction<ServerResponse> findById(PointHandler pointHandler) {
    //     return route(GET("/points/{id}"), req -> {
    //         Mono<Point> point = pointHandler.findById(req.pathVariable("id"));
    //         return ok().body(point, Point.class);
    //     });
    // }
    @Bean
    public RouterFunction<ServerResponse> findByUserId(PointHandler pointHandler) {
        return route(GET("/points/{userId}"), req -> {
            return ok().body(pointHandler.findByUserId(req.pathVariable("userId")), PointRes.class);
        });
    }
    @Bean
    public RouterFunction<ServerResponse> pointRoutes(PointHandler pointHandler) {
        return route(POST("/points"), req -> {
            return status(HttpStatus.CREATED).body(pointHandler.createInit(), Point.class);
        });
    }

}
