package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SrsForecastDayResponse {
    LocalDate date;   // Ngày dự báo (VD: 2026-08-24)
    long count;       // Số thẻ được scheduled vào đúng ngày đó
}
