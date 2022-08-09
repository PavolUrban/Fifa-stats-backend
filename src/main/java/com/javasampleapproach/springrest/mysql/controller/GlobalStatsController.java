package com.javasampleapproach.springrest.mysql.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.*;
import com.javasampleapproach.springrest.mysql.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import Utils.MyUtils;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/globalStats")
public class GlobalStatsController {
	
	@Autowired
	MatchesRepository matchesRepository;

	@Autowired
	TeamRepository teamRepository;
	
	private String resultOfTeamMatchByTeam(Matches m, String teamname)
	{
		String resultOfMatch = "unknown";
		
		if(m.getWinner().equalsIgnoreCase("D"))
			resultOfMatch = "draw";
		
		else if(m.getWinner().equalsIgnoreCase(teamname))
			resultOfMatch = "win";
		
		else
			resultOfMatch = "loss";
		
		
		return resultOfMatch;
		
	}
	
	private List<Integer> getGoals(Matches m, String teamname)
	{
		List<Integer> teamStats = new ArrayList<Integer>();
		
		if(m.getHometeam().equalsIgnoreCase(teamname))
		{
			teamStats.add(m.getScorehome());
			teamStats.add(m.getScoreaway());
		}	
		
		else if(m.getAwayteam().equalsIgnoreCase(teamname))
		{
			teamStats.add(m.getScoreaway());
			teamStats.add(m.getScorehome());
		}
		
		return teamStats;
	}
	
	
	
	private void doStuff(Map<String, Map<String, Integer>> map, String teamname, Matches m)
	{
		
		String resultOfMatch = resultOfTeamMatchByTeam(m, teamname);
		List<Integer> teamGoals = getGoals(m, teamname);
		
		
		if(map.containsKey(teamname))
		{
			Map<String, Integer> teamStats = map.get(teamname);
			
			//team stats W,L,D
			teamStats.put(resultOfMatch, teamStats.get(resultOfMatch) + 1);
			
			//team stats GS,GC
			teamStats.put("goalsScored", teamStats.get("goalsScored") + teamGoals.get(0)); //0 - goalsScored, 1- goalsConceded
			teamStats.put("goalsConceded", teamStats.get("goalsConceded") + teamGoals.get(1));
		}

		else
		{
			Map<String, Integer> teamStats= new HashMap<String, Integer>();
			
			teamStats.put("win", 0);
			teamStats.put("loss",0);
			teamStats.put("draw",0);
			teamStats.put("goalsScored", 0);
			teamStats.put("goalsConceded", 0);
			
			//to override 0 value team stats W,L,D
			teamStats.put(resultOfMatch, teamStats.get(resultOfMatch) + 1);
			
			//team stats GS, GC
			teamStats.put("goalsScored", teamStats.get("goalsScored") + teamGoals.get(0)); //0 - goalsScored, 1- goalsConceded
			teamStats.put("goalsConceded", teamStats.get("goalsConceded") + teamGoals.get(1));
			
			map.put(teamname, teamStats);
		}
	}
	
	
	public List<TopTeam> convertMapToListOfBestTeams(Map<String, Map<String, Integer>> map, Iterable<Team> teams)
	{
		List<TopTeam> allTeamsStats = new ArrayList<TopTeam>();
		for(String team : map.keySet())
		{
			Map<String,Integer> teamStats = map.get(team);

			int wins = teamStats.get("win");
			int losses = teamStats.get("loss");
			int draws = teamStats.get("draw");
			int goalsScored = teamStats.get("goalsScored");
			int goalsConceded = teamStats.get("goalsConceded");
			int matches = wins + losses + draws;
			int goalDiff = goalsScored - goalsConceded;

			TopTeam currentTeam = new TopTeam(team,wins, losses,draws, matches, goalsScored, goalsConceded, goalDiff, null);

			for(Team t: teams) {
				if (t.getTeamName().equalsIgnoreCase(team)) {
					currentTeam.setCountry(t.getCountry());
					break;
				}
			}
		    allTeamsStats.add(currentTeam);
		}
		
		return allTeamsStats;
	}
	
	@GetMapping("/getTopTeamStats")
	public	List<TopTeam> getTopTeamStats() {

		Iterable<Matches> matches = matchesRepository.findAll();
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String,Integer>>();

		for(Matches m : matches) {
			doStuff(map, m.getHometeam() ,m);
			doStuff(map,m.getAwayteam(),m);
		}
		
		Iterable<Team> teams = teamRepository.findAll();
		List<TopTeam> topTeams = convertMapToListOfBestTeams(map, teams);
		
		topTeams.sort((o1, o2) -> o2.getWins().compareTo(o1.getWins()));
		
		return topTeams;
	}

	@GetMapping("/winnersList/{competition}")
	public List<Matches> getWinnersList(@PathVariable("competition") String competition){

		List<Matches> finalMatches = matchesRepository.findByCompetitionPhaseAndCompetitionOrderBySeason("Final",competition);
		PlayersController pc = new PlayersController();
		finalMatches.forEach(match -> {
			match.setWinnerPlayer(pc.whoIsWinnerOfMatch(match, "Pavol Jay", "Kotlik"));
		});
		return finalMatches;
	}

	@GetMapping("/trophyRoom")
	public List<TitlesCount> getTrophyRoom(){
		List<Matches> finalMatches = matchesRepository.findByCompetitionPhase("Final");

		TitlesCount tcPavol = new TitlesCount();
		tcPavol.setPlayerName("Pavol Jay");

		TitlesCount tcKotlik =  new TitlesCount();
		tcKotlik.setPlayerName("Kotlik");

		calculateTitlesForPlayerPerCompetition(finalMatches, tcPavol, tcKotlik);

		return Arrays.asList(tcKotlik, tcPavol);
	}

	private void calculateTitlesForPlayerPerCompetition(List<Matches> finalMatches, TitlesCount tcPavol, TitlesCount tcKotlik){
		PlayersController pc = new PlayersController();

		finalMatches.forEach(match->{
			if(pc.whoIsWinnerOfMatch(match, "Pavol Jay", "Kotlik").equalsIgnoreCase("Pavol Jay")){
				addTitleInProperCompetition(match, tcPavol);
			} else {
				addTitleInProperCompetition(match, tcKotlik);
			}
		});

	}

	private void addTitleInProperCompetition(Matches match, TitlesCount tc){
		if(match.getCompetition().equalsIgnoreCase("CL")){
			tc.setTitlesCountCL(tc.getTitlesCountCL() + 1);
		} else {
			tc.setTitlesCountEL(tc.getTitlesCountEL() + 1);
		}
	}
	
}
