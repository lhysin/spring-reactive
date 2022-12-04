package io.lhysin.reactive.handler;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidatorHandler {

    // public <T> void validate(T o) {
    //     Set<ConstraintViolation<T>> validate = validator.validate(o);
    //     if(! validate.isEmpty()) {
    //         ConstraintViolation<T> violation = validate.stream().iterator().next();
    //         throw new ServerWebInputException(violation.toString());
    //     }
    // }
}