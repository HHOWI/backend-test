package com.terra.backendtest.controller;

import com.terra.backendtest.domain.CpuUsageSummary;
import com.terra.backendtest.domain.ServerCpuUsage;
import com.terra.backendtest.service.ServerCpuUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ServerCpuUsageController {

    @Autowired
    private ServerCpuUsageService service;

    // 분 단위 조회
    @Operation(summary = "특정 시간 사이의 분단위 CPU 사용량 조회")
    @GetMapping("/usage/minute")
    public List<ServerCpuUsage> getMinuteUsage(
            @Parameter(description = "시작 시간 (ISO 형식: YYYY-MM-DDT:hh:mm:ss)", example = "2024-05-26T04:20:15")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "종료 시간 (ISO 형식: YYYY-MM-DDT:hh:mm:ss)", example = "2024-05-26T04:30:15")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.getMinuteUsage(start, end);
    }

    // 시 단위 조회
    @Operation(summary = "특정 날짜의 시간단위 CPU 사용량 조회")
    @GetMapping("/usage/hour")
    public List<CpuUsageSummary> getHourlyUsage(
            @Parameter(description = "날짜 (ISO 형식: YYYY-MM-DD", example = "2024-05-26")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getHourlyUsage(date);
    }

    // 일 단위 조회
    @Operation(summary = "특정 두 날짜 사이의 날짜단위 CPU 사용량 조회")
    @GetMapping("/usage/day")
    public List<CpuUsageSummary> getDailyUsage(
            @Parameter(description = "시작 날짜 (ISO 형식: YYYY-MM-DD", example = "2024-05-26")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "종료 날짜 (ISO 형식: YYYY-MM-DD", example = "2024-05-27")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.getDailyUsage(start, end);
    }
}
