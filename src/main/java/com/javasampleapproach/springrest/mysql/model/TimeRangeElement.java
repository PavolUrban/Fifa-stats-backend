package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangeElement {
    private String label;
    private int lowBorder;
    private int upBorder;
    private int numberOfGoals;
    private int numberOfConcededGoals;
}
