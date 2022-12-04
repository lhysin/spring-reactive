package io.lhysin.reactive.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.lhysin.reactive.document.Point;
import io.lhysin.reactive.dto.PointRes;
import io.lhysin.reactive.dto.UsePointReq;
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
    public RouterFunction<ServerResponse> save(PointHandler pointHandler) {
        // return route(POST("/points/{userId}"), req -> {
        //     req.pathVariable("userId");
        //     return req.bodyToMono(CreatePointReq.class)
        //         .flatMap(pointHandler::createPoint)
        //         .flatMap(point ->
        //             status(HttpStatus.CREATED)
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .bodyValue(point)
        //         );
        // });
        return route(POST("/points/{userId}"), pointHandler::createPoint);
    }

    @Bean
    public RouterFunction<ServerResponse> put(PointHandler pointHandler) {
        return route(PUT("/points/{userId}"), req -> {
            req.pathVariable("userId");
            return req.bodyToMono(UsePointReq.class)
                .map(pointHandler::usePoint)
                .flatMap(point ->
                    status(HttpStatus.NO_CONTENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(point)
                );
        });
    }

    @Bean
    public RouterFunction<ServerResponse> findByUserId(PointHandler pointHandler) {
        return route(GET("/points/{userId}"), req -> {
            String userId = req.pathVariable("userId");
            return ok().body(pointHandler.findByUserId(userId), PointRes.class);
        });
        // return route(GET("/points/{userId}"), req ->
        //     req.bodyToMono(String.class)
        //         .map(pointHandler::findByUserId)
        //         .flatMap(pointResFlux -> ok().body(pointResFlux, PointRes.class)));
    }

    @Bean
    public RouterFunction<ServerResponse> emptyRequest(PointHandler pointHandler) {
        return route(POST("/points"), req -> {
            return status(HttpStatus.CREATED).body(pointHandler.createInit(), Point.class);
        });
    }

}
