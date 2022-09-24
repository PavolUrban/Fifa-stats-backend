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

	@Autowired
	TeamController teamController;

	private String resultOfTeamMatchByTeam(Matches m, long teamId)
	{
		String resultOfMatch = "unknown";

		if(m.getWinnerId() == MyUtils.drawResultId)
			resultOfMatch = "draw";
		
		else if(m.getWinnerId() == teamId)
			resultOfMatch = "win";
		
		else
			resultOfMatch = "loss";
		
		
		return resultOfMatch;
		
	}
	
	private List<Integer> getGoals(Matches m, long teamId)
	{
		List<Integer> teamStats = new ArrayList<>();
		
		if(m.getIdHomeTeam() == teamId)
		{
			teamStats.add(m.getScorehome());
			teamStats.add(m.getScoreaway());
		}	
		
		else if(m.getIdAwayTeam() == teamId)
		{
			teamStats.add(m.getScoreaway());
			teamStats.add(m.getScorehome());
		}
		
		return teamStats;
	}
	
	
	// TODO all of this will need to be totally re-worked
	private void doStuff(Map<String, Map<String, Integer>> map, Team team, Matches m)
	{

		String resultOfMatch = resultOfTeamMatchByTeam(m, team.getId());
		List<Integer> teamGoals = getGoals(m, team.getId());


		
		if(map.containsKey(team.getTeamName()))
		{
			Map<String, Integer> teamStats = map.get(team.getTeamName());
			
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
			
			map.put(team.getTeamName(), teamStats);
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

		List<Team> allTeams = teamController.getAllTeams();

		for(Matches m : matches) {
			Team homeTeam = teamRepository.findById(m.getIdHomeTeam()).orElse(null);
			doStuff(map, homeTeam ,m);
			Team awayTeam = teamRepository.findById(m.getIdAwayTeam()).orElse(null);
			doStuff(map, awayTeam ,m);
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

	@GetMapping("/teamTrophiesCount/allCompetitions")
	public List<TeamForTrophyRoom> getTeamTrophiesCount(){

		List<TeamForTrophyRoom> trophyRoom = new ArrayList<>();
		List<Matches> finalMatches = matchesRepository.findByCompetitionPhase("Final");
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

	private void insertOrUpdateTrophyRoom (List<TeamForTrophyRoom> trophyRoom, long teamId, Matches match){
		TeamForTrophyRoom teamAlreadyInRoom = trophyRoom.stream().filter(team-> team.getTeamId() == teamId).findFirst().orElse(null);
		if(teamAlreadyInRoom == null) {
			TeamForTrophyRoom newTeam = new TeamForTrophyRoom(teamId);
			addStatsForProperCompetition(newTeam, match);
			trophyRoom.add(newTeam);
		} else {
			addStatsForProperCompetition(teamAlreadyInRoom, match);
		}
	}

	private void addStatsForProperCompetition(TeamForTrophyRoom team, Matches match){
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
