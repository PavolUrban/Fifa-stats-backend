package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class GoalDistributionModel {
    private int scoredGoalsUnknownTime = 0;
    private int concededGoalsUnknownTime = 0;
    private ArrayList<Integer> concededGoalsList = new ArrayList<>();
    private ArrayList<Integer> scoredGoalsList = new ArrayList<>();
    private ArrayList<String> minutesAsLabels = new ArrayList<>();

    int wholeRange = 90;
    int stepSize = 5; // todo as variable
    int numberOfRanges = wholeRange/stepSize;
    List<TimeRangeElement> allRanges = new ArrayList<>();
    
    public GoalDistributionModel(){
        for(int i = 0; i < numberOfRanges; i++ ) {
            if ((i*stepSize) > wholeRange){
                break;
            }
            int lowBorder = i * stepSize;
            int upBorder = i * stepSize + stepSize;
            TimeRangeElement tre = new TimeRangeElement();
            tre.setLabel(lowBorder + " - " + upBorder);
            tre.setLowBorder(lowBorder);
            tre.setUpBorder(upBorder);
            allRanges.add(tre);
        }
    }
}
