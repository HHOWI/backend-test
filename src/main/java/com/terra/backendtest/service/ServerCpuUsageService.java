package com.terra.backendtest.service;

import com.terra.backendtest.domain.CpuUsageSummary;
import com.terra.backendtest.domain.ServerCpuUsage;
import com.terra.backendtest.repo.ServerCpuUsageDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServerCpuUsageService {

    @Autowired
    private ServerCpuUsageDAO dao;

    // CPU 사용률을 데이터베이스에 저장
    public void saveCpuUsage(double cpuUsage) {
        ServerCpuUsage serverCpuUsage = new ServerCpuUsage();
        serverCpuUsage.setTimestamp(LocalDateTime.now());
        serverCpuUsage.setCpuUsage(cpuUsage);
        dao.save(serverCpuUsage);
    }

    // 분 단위 조회: 지정한 시간 구간의 분 단위 CPU 사용률을 조회합니다.
    public List<ServerCpuUsage> getMinuteUsage(LocalDateTime start, LocalDateTime end) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        if (start.isBefore(oneWeekAgo)) {
            throw new IllegalArgumentException("최근 1주 데이터만 조회 가능합니다.");
        }
        return dao.findByTimestampBetween(start, end);
    }

    // 시 단위 조회: 지정한 날짜의 시  단위 CPU 최소/최대/평균 사용률을 조회합니다.
    public List<CpuUsageSummary> getHourlyUsage(LocalDate date) {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        if (date.isBefore(threeMonthsAgo)) {
            throw new IllegalArgumentException("최근 3달 데이터만 조회 가능합니다.");
        }
        return dao.findHourlyUsage(date);
    }

    // 일 단위 조회: 지정한 날짜 구간의 일  단위 CPU 최소/최대/평균 사용률을 조회합니다.
    public List<CpuUsageSummary> getDailyUsage(LocalDate start, LocalDate end) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (start.isBefore(oneYearAgo)) {
            throw new IllegalArgumentException("최근 1년 데이터만 조회 가능합니다.");
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusSeconds(1);
        return dao.findDailyUsage(startDateTime, endDateTime);
    }

}
