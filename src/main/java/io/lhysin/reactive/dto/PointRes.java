package io.lhysin.reactive.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.lhysin.reactive.type.PointCreatedType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PointRes {
    private String userId;
    private BigDecimal amount;
    private PointCreatedType pointCreatedType;
    private LocalDateTime expiredAt;
    private String createdBy;
    private LocalDateTime createdAt;
}
