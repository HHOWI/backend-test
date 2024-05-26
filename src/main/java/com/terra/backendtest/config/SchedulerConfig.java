package com.terra.backendtest.config;

import com.terra.backendtest.service.ServerCpuUsageService;
import com.terra.backendtest.util.CpuUsageCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulerConfig {

    @Autowired
    private ServerCpuUsageService service;

    @Scheduled(fixedRate = 60000)
    public void collectCpuUsage() {
        try {
            double cpuUsage = CpuUsageCollector.getCpuUsage();
            service.saveCpuUsage(cpuUsage);
        } catch (Exception e) {
            log.error("CPU 사용률 수집에 실패하였습니다", e);
        }

    }
}
