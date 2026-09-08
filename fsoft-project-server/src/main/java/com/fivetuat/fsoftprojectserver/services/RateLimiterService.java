package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.response.RateLimiterResult;

public interface RateLimiterService {

    /**
     * Kiểm tra Rate Limit theo key, giới hạn request và khoảng thời gian (giây).
     *
     * @param key           Key định danh duy nhất trong Redis (ví dụ: rate_limit:ip:127.0.0.1)
     * @param limit         Số request tối đa được phép
     * @param periodSeconds Khoảng thời gian cửa sổ trượt (Sliding Window) tính bằng giây
     * @return RateLimiterResult chứa thông tin allowed, remaining, resetSeconds
     */
    RateLimiterResult checkLimit(String key, long limit, long periodSeconds);
}
