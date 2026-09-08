package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterResult {
    private boolean allowed;
    private long limit;
    private long remaining;
    private long resetSeconds;
}
