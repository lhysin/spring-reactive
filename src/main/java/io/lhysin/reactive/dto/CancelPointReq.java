package io.lhysin.reactive.dto;

import java.math.BigDecimal;

import io.lhysin.reactive.type.PointTransactionType;
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
public class CancelPointReq {
    private String userId;
    private BigDecimal amount;
    private String createdBy;
}
