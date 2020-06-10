package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.Team;
import com.javasampleapproach.springrest.mysql.model.TopTeam;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.TeamRepository;

import Utils.MyUtils;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/globalStats")
public class GlobalStatsController {
	
	@Autowired
	MatchesRepository matchesRepository;
	
	@Autowired
	FileRepository fileRepository;

	@Autowired
	TeamRepository teamRepository;
	
	public void updateOrAddPlayerInMap(Map<String,Map<String, Integer>> playerWithGoals, String player, int numberOfGoals, String team)
	{
		if(playerWithGoals.containsKey(player))
		{
			
			if(playerWithGoals.get(player).containsKey(team))
			{
				playerWithGoals.get(player).put(team, playerWithGoals.get(player).get(team) + numberOfGoals);
//				playerWithGoals.put(player, playerWithGoals.get(player) + numberOfGoals);
			}
			
			else
				playerWithGoals.get(player).put(team, numberOfGoals);
		}
		
		else
		{
			Map<String, Integer> currentTeamAndGoals = new HashMap<>();
			currentTeamAndGoals.put(team, numberOfGoals);
			playerWithGoals.put(player, currentTeamAndGoals);
		}
			
		
	}
	
	public void splitToMinutesAndName(Map<String,Map<String, Integer>> playerWithGoals, String goalScorer, Matches m, String teamname)
	{	
		if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) // on older fifa there are no minutes with goals
		{
			int numberOfGoals = 1; //by default
			String goalscorerName = "unknown!";
			
			// goalscorer is in format 4*Name, where number is number of goals in current match, if goalscorers has only one goal it is in format Name
			if(goalScorer.contains("*")) 
			{
				String[] info = goalScorer.split("\\*");
				numberOfGoals = Integer.parseInt(info[0]);
				goalscorerName = info[1];
			}
			
			else
				goalscorerName = goalScorer;
			
			updateOrAddPlayerInMap(playerWithGoals, goalscorerName, numberOfGoals, teamname);
			
		}
		
		else
		{
			String[] minutesAndName = goalScorer.split(" ");
			String player = minutesAndName[1];
			String minutes = minutesAndName[0];
			
			int numberOfGoals = 1; //by default
			
			if(minutes.contains(",")) //goalscorer has multiple goals in this match
			{
				String[] minutesArray = minutes.split(",");
				numberOfGoals = minutesArray.length;
			}
			
			updateOrAddPlayerInMap(playerWithGoals, player, numberOfGoals, teamname);
		}
		
	}
	
	

	public void addGoalsScorers(Map<String,Map<String, Integer>> playerWithGoals, Matches m, String allGoalscorersWithMinutes, String teamname)
	{
		if(!allGoalscorersWithMinutes.contains("/") ) //&& !m.getSeason().equalsIgnoreCase("FIFA13")
		{
				if(allGoalscorersWithMinutes.contains(";")) //multiple goalscorers
				{
					String[] multipleGoalscorers = allGoalscorersWithMinutes.split(";");
					
					for(String goalScorer : multipleGoalscorers)
					{
						splitToMinutesAndName(playerWithGoals, goalScorer, m, teamname);
					}
				}
				else
				{
					splitToMinutesAndName(playerWithGoals, allGoalscorersWithMinutes,m, teamname);	
				}
			
		}
	}
	
	
	public List<Goalscorer> transforMapToGoalscorersList(Map<String,Map<String, Integer>> playersWithGoalsByTeams)
	{
		List<Goalscorer> listOfGoalscorers = new ArrayList<Goalscorer>();
		

		for(String player : playersWithGoalsByTeams.keySet()) {
		    
			Goalscorer goalscorer = new Goalscorer();
			
			Map<String,Integer> playerGoals = playersWithGoalsByTeams.get(player);

			int goalsTotal = 0;
		    for(String team : playerGoals.keySet()) {
		        int numberOfGoalsForCurrentTeam = playerGoals.get(team);
		        
		        goalsTotal = goalsTotal + numberOfGoalsForCurrentTeam;
		    
		    }
		    goalscorer.setName(player);
		    goalscorer.setGoalsByTeams(playerGoals);
		    goalscorer.setTotalGoalsCount(goalsTotal);
		    
		    listOfGoalscorers.add(goalscorer);
		}
	  
		return listOfGoalscorers;
	}
	
	@GetMapping("/getAllTimeGoalScorers")
	public	List<Goalscorer> getAllGoalscorers() {

		Iterable<Matches> matches = matchesRepository.findAll();
		Map<String,Map<String, Integer>> playerWithGoals = new HashMap<String,Map<String, Integer>>();
		
		for(Matches m : matches)
		{
			if(m.getGoalscorers() != null)
			{
				String[] goalscorers = m.getGoalscorers().split("-");
				addGoalsScorers(playerWithGoals, m, goalscorers[0], m.getHometeam()); //home
				addGoalsScorers(playerWithGoals, m, goalscorers[1], m.getAwayteam()); //away
			}
			
		}
		
		
		List<Goalscorer> finalGoalscorers = transforMapToGoalscorersList(playerWithGoals);
		
		
		finalGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));
		
//		Collections.sort(finalGoalscorers.arrayList, 
//                (o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate()));
//	
		return finalGoalscorers;
	}
	
	
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
	
	
	public List<TopTeam> convertMapToListOfBestTeams(Map<String, Map<String, Integer>> map, List<FileModel> logos, Iterable<Team> teams)
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
			
			TopTeam currentTeam = new TopTeam(team, wins, losses, draws, matches, goalsScored, goalsConceded, goalDiff, null, null);
			
			for(FileModel logo : logos)
			{
				
				if(logo.getTeamname().equalsIgnoreCase(team))
				{
					currentTeam.setFm(logo);
					break;
				}
					
			}
			
			
			for(Team t: teams)
			{
				if(t.getTeamName().equalsIgnoreCase(team))
				{
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
		
		
		for(Matches m : matches)
		{
			doStuff(map, m.getHometeam() ,m);
			doStuff(map,m.getAwayteam(),m);
		}
		
		List<FileModel> logos = fileRepository.findAll();
		Iterable<Team> teams = teamRepository.findAll();
		List<TopTeam> topTeams = convertMapToListOfBestTeams(map, logos, teams);
		
		topTeams.sort((o1, o2) -> o2.getWins().compareTo(o1.getWins()));
		
		return topTeams;
	}
	
}
