package com.terra.backendtest.repo;

import com.terra.backendtest.domain.CpuUsageSummary;
import com.terra.backendtest.domain.ServerCpuUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ServerCpuUsageDAO extends JpaRepository<ServerCpuUsage, Long> {
    // 분 단위 조회
    List<ServerCpuUsage> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // 시 단위 최소, 최대, 평균 조회
    @Query("SELECT new com.terra.backendtest.domain.CpuUsageSummary(FUNCTION('DAY', s.timestamp), FUNCTION('HOUR', s.timestamp), MIN(s.cpuUsage), MAX(s.cpuUsage), AVG(s.cpuUsage)) " +
            "FROM ServerCpuUsage s " +
            "WHERE CAST(s.timestamp AS date) = :date " +
            "GROUP BY FUNCTION('DAY', s.timestamp), FUNCTION('HOUR', s.timestamp)")
    List<CpuUsageSummary> findHourlyUsage(@Param("date") LocalDate date);

    // 일 단위 최소, 최대, 평균 조회
    @Query("SELECT new com.terra.backendtest.domain.CpuUsageSummary(FUNCTION('DAY', u.timestamp), null as hour, MIN(u.cpuUsage), MAX(u.cpuUsage), AVG(u.cpuUsage)) " +
            "FROM ServerCpuUsage u " +
            "WHERE u.timestamp BETWEEN :start AND :end " +
            "GROUP BY DAY(u.timestamp) " +
            "ORDER BY DAY(u.timestamp)")
    List<CpuUsageSummary> findDailyUsage(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);




}
