package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
public class GoalDistributionModel {
    private int scoredGoalsUnknownTime = 0;
    private int concededGoalsUnknownTime = 0;
    private ArrayList<GoalInMinuteModel> goalsInMinutesCount = new ArrayList<>();

    public GoalDistributionModel(){
        for (int i = 1 ; i<=91 ;i++){
            GoalInMinuteModel gim;
            if(i == 91) {
                gim = new GoalInMinuteModel("90+", 0, 0);
            } else {
                gim = new GoalInMinuteModel(String.valueOf(i), 0, 0);
            }
            goalsInMinutesCount.add(gim);
        }
    }
}
