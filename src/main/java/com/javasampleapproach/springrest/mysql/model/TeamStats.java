package com.javasampleapproach.springrest.mysql.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TeamStats {

		// values that are not directly presented in db and must be computed
		Map<String, Map<String, Integer>> matchesStats;

		Map<String, Map<String, List<Matches>>> finalMatches;

		// player related stats
		Map<String, Map<String, Integer>> goalScorers;
		Map<String, Map<String, Integer>> yellowCards;
		Map<String, Map<String, Integer>> redCards;

		//timeOfGoal, numberOfGoalsInThatMinute
		Map<String, Integer> goalsByMinutesCount;
		Map<String, Integer> concededGoalsByMinutesCount;
		private int unknownTimeGoals = 0;
		List<TimeRangeElement> goalsPerTimeRanges;

		private List<FileModel> oponentsLogos;


	FileModel fm;
		List<Matches> matches;
		
	public TeamStats()
	{
		this.matchesStats = new HashMap<>();
		this.matches = new ArrayList<>();
		this.goalScorers = new HashMap<>();
		this.yellowCards = new HashMap<>();
		this.redCards = new HashMap<>();
		this.goalsByMinutesCount = new HashMap<>();
		this.concededGoalsByMinutesCount = new HashMap<>();
		this.goalsPerTimeRanges = new ArrayList<>();
		this.finalMatches = new HashMap<>();
		this.oponentsLogos = new ArrayList<>();

		initializeMatchesStats("CL");
		initializeMatchesStats("EL");

		initializePlayerRelatedStats(this.goalScorers);
		initializePlayerRelatedStats(this.yellowCards);
		initializePlayerRelatedStats(this.redCards);

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
