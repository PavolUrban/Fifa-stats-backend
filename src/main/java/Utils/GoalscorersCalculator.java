package Utils;

import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.TeamStats;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GoalscorersCalculator {


    public static void addHomeOrAwayGoalscorersProperly(boolean updatingScoredGoals, Map<String, Integer> goalsByMinutesCount , Matches m, String goalscorersAsString, TeamStats team)
    {
        // check for multiple goalscorers - multiple goalscorers are separated by ;
        if (goalscorersAsString.contains(";")) {
            String[] allGoalscorers = goalscorersAsString.split(";");

            for (String goalScorer : allGoalscorers)
            {
                // in some old FIFA seasons there are not minutes with goalscorers, only number of goals in match by player
                if (MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) {
                    recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(updatingScoredGoals, m, goalScorer, team);
                } else {
                    recalculateGoalsAndUpdateGoalscorersMap(updatingScoredGoals, goalsByMinutesCount, m, goalScorer, team);
                }
            }
        } else {
            // '/' is used for no goalscorer - if string contains this character no one has scored so no one will be added to goalscorers list
            if (!goalscorersAsString.equalsIgnoreCase("/")) {
                if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) {
                    recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(updatingScoredGoals, m, goalscorersAsString, team);
                }
                else {
                    recalculateGoalsAndUpdateGoalscorersMap(updatingScoredGoals, goalsByMinutesCount, m, goalscorersAsString, team);
                }
            }
        }
    }

    private static void recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(boolean updatingScoredGoals, Matches m, String goalScorer, TeamStats team) {
        // by default goalscorer must scored at least 1 goal
        int numberOfGoals = 1;
        String goalscorerName;

        // goalscorer is in format 4*Name, where number is number of goals in current match, if goalscorers has only one goal it is in format Name
        if(goalScorer.contains("*")) {
            String[] info = goalScorer.split("\\*");
            numberOfGoals = Integer.parseInt(info[0]);
            goalscorerName = info[1];
        } else{
            goalscorerName = goalScorer;
        }

        //team is send only for players who scored for current club, oponents scorers are skipped
        if(updatingScoredGoals) {
            // for EL or CL
            updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get(m.getCompetition()), goalscorerName, numberOfGoals);
            // for total
            updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get("Total"), goalscorerName, numberOfGoals);
            team.setUnknownTimeGoals(team.getUnknownTimeGoals() + numberOfGoals);
        } else {
            team.setUnknownConcededGoalsTime(team.getUnknownConcededGoalsTime() + numberOfGoals);
        }
    }

    private static void recalculateGoalsAndUpdateGoalscorersMap(boolean updatingScoredGoals, Map<String, Integer> goalsByMinutesCount, Matches m, String goalScorer, TeamStats team)
    {
        String[] fullInfo = goalScorer.split(" ");
        String goalscorerName = fullInfo[1];

        // by default goalscorer must scored at least 1 goal
        int numberOfGoals = 1;

        // goalscorer has multiple goals in this match
        if (fullInfo[0].contains(","))
        {
            String[] minutes = fullInfo[0].split(",");

            for (int i = 0; i < minutes.length; i++) {
                insertNewScoringTimeOrUpdateExisting(goalsByMinutesCount, minutes[i]); // scored map
            }
            numberOfGoals = minutes.length;
        } else {
            insertNewScoringTimeOrUpdateExisting(goalsByMinutesCount, fullInfo[0]); // scored map
        }

        //team is send only for players who scored for current club, oponents scorers are skipped
        if(updatingScoredGoals) {
            // for EL or CL
            updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get(m.getCompetition()), goalscorerName, numberOfGoals);

            // for total
            updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get("Total"), goalscorerName, numberOfGoals);
        }
    }

    private static void updateOrInsertGoalscorerToProperCompetition(Map<String, Integer> map, String goalscorerName, int numberOfGoals) {
        if (map.containsKey(goalscorerName)) {
            map.put(goalscorerName, map.get(goalscorerName) + numberOfGoals);
        } else {
            map.put(goalscorerName, numberOfGoals);
        }
    }

    private static void insertNewScoringTimeOrUpdateExisting(Map<String, Integer> goalsMinutesCountMap, String goalTime){
        if (goalsMinutesCountMap.containsKey(goalTime)) {
            goalsMinutesCountMap.put(goalTime, goalsMinutesCountMap.get(goalTime) + 1);
        } else {
            goalsMinutesCountMap.put(goalTime, 1);
        }
    }

    private void printGoalscorersMap(Map<String, Integer> mapToPrint){
        for (Map.Entry<String,Integer> entry : mapToPrint.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
    }

    public static Map<String, Integer> sortMap(Map<String, Integer> mapToSort){
        Map<String, Integer> sortedMap = mapToSort.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return sortedMap;
    }

}
