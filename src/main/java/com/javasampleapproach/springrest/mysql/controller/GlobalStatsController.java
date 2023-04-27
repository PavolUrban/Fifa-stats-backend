package com.javasampleapproach.springrest.mysql.controller;


import Utils.HelperMethods;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.TeamForTrophyRoom;
import com.javasampleapproach.springrest.mysql.model.TitlesCount;
import com.javasampleapproach.springrest.mysql.model.TopTeam;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import com.javasampleapproach.springrest.mysql.serviceV2.MatchesServiceV2;
import com.javasampleapproach.springrest.mysql.services.MatchesService;
import com.javasampleapproach.springrest.mysql.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import Utils.MyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/globalStats")
public class GlobalStatsController {

	@Autowired
	MatchesServiceV2 matchesService;

	@Autowired
	TeamService teamService;

	private String resultOfTeamMatchByTeam(Matches m, long teamId) {
		String resultOfMatch = "unknown";

		if(m.getWinnerId() == MyUtils.DRAW_RESULT_ID)
			resultOfMatch = "draw";
		
		else if(m.getWinnerId() == teamId)
			resultOfMatch = "win";
		
		else
			resultOfMatch = "loss";
		
		
		return resultOfMatch;
		
	}
	
	private List<Integer> getGoals(Matches m, long teamId) {
		List<Integer> teamStats = new ArrayList<>();
		
		if(m.getHomeTeam().getId() == teamId) {
			teamStats.add(m.getScorehome());
			teamStats.add(m.getScoreaway());
		} else if(m.getAwayTeam().getId() == teamId) {
			teamStats.add(m.getScoreaway());
			teamStats.add(m.getScorehome());
		}
		
		return teamStats;
	}
	
	
	// TODO all of this will need to be totally re-worked
	private void doStuff(Map<String, Map<String, Integer>> map, Team team, Matches m) {
		String resultOfMatch = resultOfTeamMatchByTeam(m, team.getId());
		List<Integer> teamGoals = getGoals(m, team.getId());

		if(map.containsKey(team.getTeamName())) {
			Map<String, Integer> teamStats = map.get(team.getTeamName());
			
			//team stats W,L,D
			teamStats.put(resultOfMatch, teamStats.get(resultOfMatch) + 1);
			
			//team stats GS,GC
			teamStats.put("goalsScored", teamStats.get("goalsScored") + teamGoals.get(0)); //0 - goalsScored, 1- goalsConceded
			teamStats.put("goalsConceded", teamStats.get("goalsConceded") + teamGoals.get(1));
		} else {
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
			
			map.put(team.getTeamName(), teamStats);
		}
	}
	
	
	public List<TopTeam> convertMapToListOfBestTeams(Map<String, Map<String, Integer>> map, Iterable<Team> teams) {
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

	// todo latest
	@GetMapping("/getTopTeamStats")
	public	List<TopTeam> getTopTeamStats() {
//		Iterable<Matches> matches = matchesService.getAllMatches();
//		Map<String, Map<String, Integer>> map = new HashMap<>();
//
//		for(Matches m : matches) {
//			Team homeTeam = teamService.findById(m.getIdHomeTeam()).orElse(null);
//			doStuff(map, homeTeam ,m);
//			Team awayTeam = teamService.findById(m.getIdAwayTeam()).orElse(null);
//			doStuff(map, awayTeam ,m);
//		}
//
//		Iterable<Team> teams = teamService.getAllTeamsIterable();
//		List<TopTeam> topTeams = convertMapToListOfBestTeams(map, teams);
//
//		topTeams.sort((o1, o2) -> o2.getWins().compareTo(o1.getWins()));
//
//		return topTeams;

		return null;
	}

	@GetMapping("/winnersList/{competition}")
	public List<MatchesDTO> getWinnersList(@PathVariable("competition") String competition){
		return matchesService.getFilteredMatches(competition,"Final", null, null);
	}

	@GetMapping("/teamTrophiesCount/allCompetitions")
	public List<TeamForTrophyRoom> getTeamTrophiesCount(){

		List<TeamForTrophyRoom> trophyRoom = new ArrayList<>();
		List<MatchesDTO> finalMatches = matchesService.getFilteredMatches(null,"Final", null, null);
		finalMatches.forEach(match-> {
			insertOrUpdateTrophyRoom(trophyRoom, match.getIdHomeTeam(), match);
			insertOrUpdateTrophyRoom(trophyRoom, match.getIdAwayTeam(), match);
		});

		trophyRoom.forEach(team-> {
			team.setWinCountTotal(team.getWinCountCL() + team.getWinCountEL());
			team.setRunnersUpTotal(team.getRunnersUpCL() + team.getRunnersUpEL());
		});

		Collections.sort(trophyRoom, (t1, t2) -> t2.getWinCountTotal().compareTo(t1.getWinCountTotal()));


		return trophyRoom;
	}

	private void insertOrUpdateTrophyRoom (List<TeamForTrophyRoom> trophyRoom, long teamId, MatchesDTO match){
		TeamForTrophyRoom teamAlreadyInRoom = trophyRoom.stream().filter(team-> team.getTeamId() == teamId).findFirst().orElse(null);

		if(teamAlreadyInRoom == null) {
			Team team = teamService.findById(teamId);
			TeamForTrophyRoom newTeam = new TeamForTrophyRoom(teamId);
			newTeam.setTeamName(team.getTeamName());
			addStatsForProperCompetition(newTeam, match);
			trophyRoom.add(newTeam);
		} else {
			addStatsForProperCompetition(teamAlreadyInRoom, match);
		}
	}

	private void addStatsForProperCompetition(TeamForTrophyRoom team, MatchesDTO match){
		if(match.getCompetition().equalsIgnoreCase(MyUtils.CHAMPIONS_LEAGUE)){
			if (match.getWinnerId() == team.getTeamId()) {
				team.setWinCountCL(team.getWinCountCL() + 1);
			} else {
				team.setRunnersUpCL(team.getRunnersUpCL() + 1);
			}
		} else {
			if (match.getWinnerId() == team.getTeamId()) {
				team.setWinCountEL(team.getWinCountEL() + 1);
			} else {
				team.setRunnersUpEL(team.getRunnersUpEL() + 1);
			}
		}
	}

	@GetMapping("/trophyRoom")
	public List<TitlesCount> getTrophyRoom(){
		List<MatchesDTO> finalMatches = matchesService.getFilteredMatches(null,"Final", null, null);

		TitlesCount tcPavol = new TitlesCount();
		tcPavol.setPlayerName("Pavol Jay");

		TitlesCount tcKotlik =  new TitlesCount();
		tcKotlik.setPlayerName("Kotlik");

		calculateTitlesForPlayerPerCompetition(finalMatches, tcPavol, tcKotlik);

		return Arrays.asList(tcKotlik, tcPavol);
	}

	private void calculateTitlesForPlayerPerCompetition(List<MatchesDTO> finalMatches, TitlesCount tcPavol, TitlesCount tcKotlik){
		finalMatches.forEach(match->{
			if(match.getWinnerPlayer().equalsIgnoreCase(MyUtils.PAVOL_JAY)){
				addTitleInProperCompetition(match, tcPavol);
			} else {
				addTitleInProperCompetition(match, tcKotlik);
			}
		});
	}

	private void addTitleInProperCompetition(MatchesDTO match, TitlesCount tc){
		if(match.getCompetition().equalsIgnoreCase("CL")){
			tc.setTitlesCountCL(tc.getTitlesCountCL() + 1);
		} else {
			tc.setTitlesCountEL(tc.getTitlesCountEL() + 1);
		}
	}
	
}
