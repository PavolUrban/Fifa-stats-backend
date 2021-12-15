package com.javasampleapproach.springrest.mysql.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TeamStats {

	List<Matches> matches;
	Map<String, Map<String, List<Matches>>> finalMatches;

	// values that are not directly presented in db and must be computed
	Map<String, Map<String, Integer>> matchesStats;

	// player related stats
	Map<String, Map<String, Integer>> goalScorers; // todo make this something similar to playersCardsStatisticsPerCompetition
	Map<String, List<FifaPlayer>> playersCardsStatisticsPerCompetition;

	// timeOfGoal, numberOfGoalsInThatMinute
	Map<String, Integer> goalsByMinutesCount;
	Map<String, Integer> concededGoalsByMinutesCount;

	private int unknownTimeGoals = 0;
	private int unknownConcededGoalsTime = 0;
	List<TimeRangeElement> goalsPerTimeRanges;

	private List<FileModel> oponentsLogos;

	FileModel fm;

		
	public TeamStats()
	{
		this.matches = new ArrayList<>();
		this.finalMatches = new HashMap<>();
		this.matchesStats = new HashMap<>();

		this.goalScorers = new HashMap<>();
		initializePlayerRelatedStats(this.goalScorers);

		this.playersCardsStatisticsPerCompetition = new HashMap<>();
		this.playersCardsStatisticsPerCompetition.put("Total", new ArrayList<>());
		this.playersCardsStatisticsPerCompetition.put("CL", new ArrayList<>());
		this.playersCardsStatisticsPerCompetition.put("EL", new ArrayList<>());

		this.goalsByMinutesCount = new HashMap<>();
		this.concededGoalsByMinutesCount = new HashMap<>();

		this.goalsPerTimeRanges = new ArrayList<>();
		this.oponentsLogos = new ArrayList<>();

		initializeMatchesStats("CL");
		initializeMatchesStats("EL");

		this.finalMatches.put("CL", new HashMap<>());
		this.finalMatches.put("EL", new HashMap<>());
		this.finalMatches.put("Total", new HashMap<>());
	}


	private void initializeMatchesStats(String competition){
		this.matchesStats.put(competition, new HashMap<>());
		this.matchesStats.get(competition).put("Wins", 0);
		this.matchesStats.get(competition).put("Losses", 0);
		this.matchesStats.get(competition).put("Draws", 0);
		this.matchesStats.get(competition).put("GoalsScored", 0);
		this.matchesStats.get(competition).put("GoalsConceded", 0);
		this.matchesStats.get(competition).put("Seasons", 0);
	}

	// TODO - maybe rename? this is not very accurate
	private void initializePlayerRelatedStats(Map<String, Map<String, Integer>> playerRelatedStat) {
		playerRelatedStat.put("CL", new HashMap<>());
		playerRelatedStat.put("EL", new HashMap<>());
		playerRelatedStat.put("Total", new HashMap<>());
	}

}
