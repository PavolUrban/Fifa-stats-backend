package Utils;

import com.javasampleapproach.springrest.mysql.model.TeamStats;
import com.javasampleapproach.springrest.mysql.model.TimeRangeElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeRangesMapper {

    public static void transformGoalMapToTimeRanges(TeamStats team){

        int wholeRange = 90;
        int stepSize = 5; // todo as variable
        int numberOfRanges = wholeRange/stepSize;

        List<TimeRangeElement> allRanges = new ArrayList<>();

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
            tre.setNumberOfGoals(0);
            allRanges.add(tre);
        }

//		team.getGoalsByMinutesCount().forEach((time, goalsCount)->{
//			int timeAsInt = Integer.parseInt(time);
//
//			allRanges.forEach(range->{
//				if( (timeAsInt > range.getLowBorder()) && (timeAsInt <= range.getUpBorder())){
//					range.setNumberOfGoals(range.getNumberOfGoals() + goalsCount);
//				} else if( (timeAsInt > wholeRange) && (range.getUpBorder() == 90)){
//					// add all goals scored after 90 minute to the last range
//					range.setNumberOfGoals(range.getNumberOfGoals() + goalsCount);
//				}
//			});
//		});

        recalculateToRanges(team.getGoalsByMinutesCount(), allRanges, true, wholeRange);
        recalculateToRanges(team.getConcededGoalsByMinutesCount(), allRanges, false, wholeRange);

        team.setGoalsPerTimeRanges(allRanges);
    }


    private static void recalculateToRanges(Map<String, Integer> mapToRecalculate, List<TimeRangeElement> allRanges, boolean goalsScoredToUpdate, int wholeRange){
        mapToRecalculate.forEach((time, goalsCount)->{
            int timeAsInt = Integer.parseInt(time);

            allRanges.forEach(range->{
                if( (timeAsInt > range.getLowBorder()) && (timeAsInt <= range.getUpBorder())){
                    updateProperGoalsCounter(goalsScoredToUpdate, range, goalsCount);
                } else if( (timeAsInt > wholeRange) && (range.getUpBorder() == 90)){
                    // add all goals scored after 90 minute to the last range
                    updateProperGoalsCounter(goalsScoredToUpdate, range, goalsCount);
                }
            });
        });
    }


    private static void updateProperGoalsCounter(boolean goalsScoredToUpdate, TimeRangeElement tre, int numberOfGoals) {
        if (goalsScoredToUpdate) {
            tre.setNumberOfGoals(tre.getNumberOfGoals() + numberOfGoals);
        } else {
            tre.setNumberOfConcededGoals(tre.getNumberOfConcededGoals() + numberOfGoals);
        }

    }



}
