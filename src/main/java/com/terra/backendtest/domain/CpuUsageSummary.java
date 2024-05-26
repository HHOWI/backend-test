package com.terra.backendtest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CpuUsageSummary {
    private int day;
    private Object hour;
    private double min;
    private double max;
    private double avg;


}
