package com.javasampleapproach.springrest.mysql.model;

import java.util.ArrayList;
import java.util.List;

import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import lombok.Data;

@Data
public class TeamStatsWithMatches {

	private String teamName;
	private long teamId;
	private List<MatchesDTO> matches;
	private TeamStats teamStatsCL;
	private TeamStats teamStatsEL;
	private TeamStats teamStatsTotal;
	private List<Integer> bilance;

	public TeamStatsWithMatches()
	{
		this.matches = new ArrayList<>();
		this.bilance = new ArrayList<>();
		this.teamStatsCL = new TeamStats();
		this.teamStatsEL = new TeamStats();
		this.teamStatsTotal = new TeamStats();
	}

	public void calculateTeamStatsTotal(){
		this.teamStatsTotal.setWins(this.teamStatsCL.getWins() + this.teamStatsEL.getWins());
		this.teamStatsTotal.setLosses(this.teamStatsCL.getLosses() + this.teamStatsEL.getLosses());
		this.teamStatsTotal.setDraws(this.teamStatsCL.getDraws() + this.teamStatsEL.getDraws());
		this.teamStatsTotal.setGoalsScored(this.teamStatsCL.getGoalsScored() + this.teamStatsEL.getGoalsScored());
		this.teamStatsTotal.setGoalsConceded(this.teamStatsCL.getGoalsConceded() + this.teamStatsEL.getGoalsConceded());
		this.teamStatsTotal.setGoalDiff(this.teamStatsCL.getGoalDiff() + this.teamStatsEL.getGoalDiff());
		this.teamStatsTotal.setMatchesCount(this.teamStatsCL.getMatchesCount() + this.teamStatsEL.getMatchesCount());
		this.teamStatsTotal.setFinalMatchesCount(this.teamStatsCL.getFinalMatchesCount() + this.teamStatsEL.getFinalMatchesCount());
		this.teamStatsTotal.setTitlesCount(this.teamStatsCL.getTitlesCount() + this.teamStatsEL.getTitlesCount());
		this.teamStatsTotal.setRunnersUpCount(this.teamStatsCL.getRunnersUpCount() + this.teamStatsEL.getRunnersUpCount());
		this.teamStatsTotal.getSeasonsList().addAll(this.teamStatsCL.getSeasonsList());
		this.teamStatsTotal.getSeasonsList().addAll(this.teamStatsEL.getSeasonsList());
	}


}
