package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalInMinuteModel {
    private String minuteLabel;
    private int scoredGoalsCount;
    private int concededGoalsCount;
}
