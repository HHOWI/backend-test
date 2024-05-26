package com.terra.backendtest.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class CpuUsageCollector {

    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public static double getCpuUsage() {
        return osBean.getSystemCpuLoad() * 100;
    }
}
