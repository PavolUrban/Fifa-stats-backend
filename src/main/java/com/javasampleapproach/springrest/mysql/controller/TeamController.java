package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.Team;
import com.javasampleapproach.springrest.mysql.model.TeamStats;
import com.javasampleapproach.springrest.mysql.model.TeamWithLogo;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.TeamRepository;

import Utils.MyUtils;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/teams")
public class TeamController {
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	MatchesRepository matchesRepository;
	
	@Autowired
	FileRepository fileRepository;	
	
	@GetMapping("/getAllTeamNames")
	public List<String> getTeamnames() {
		List<String> teamNames = new ArrayList<>();

		teamRepository.findAll().forEach(p -> teamNames.add(p.getTeamName()));
		
		return teamNames;
	}
	
	
	@GetMapping("/getTeams")
	public List<TeamWithLogo> getAllCustomers() {
		System.out.println("Get all teams...");

		List<Team> teams = new ArrayList<>();
		teamRepository.findAll().forEach(teams::add);

		
		List<FileModel> logos = new ArrayList<FileModel>();
		fileRepository.findAll().forEach(logos::add);;
		
		
		List<TeamWithLogo> finalTeams = new ArrayList<TeamWithLogo>();
		for(Team t : teams)
		{
			if (t.getFirstSeasonCL() == null)
				t.setFirstSeasonCL("never");
			
			if (t.getFirstSeasonEL() == null)
				t.setFirstSeasonEL("never");
			
			
			
			TeamWithLogo twl = new TeamWithLogo(t);
			
			for(FileModel logo : logos)
			{
				if(logo.getTeamname().equalsIgnoreCase(t.getTeamName()))
				{
					twl.setFm(logo);
					break;
				}	
			}
			
			finalTeams.add(twl);
		}
		
		return finalTeams;
	}
	
	private void recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(TeamStats team, Matches m, String goalScorer)
	{
		System.out.println("Goalscorer in old fifa Game"+ goalScorer);
		
		
		
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
		
		
		if( team.getGoalScorers().get(m.getCompetition()).containsKey(goalscorerName))
			team.getGoalScorers().get(m.getCompetition()).put(goalscorerName, team.getGoalScorers().get(m.getCompetition()).get(goalscorerName) + numberOfGoals);
		
		else
			team.getGoalScorers().get(m.getCompetition()).put(goalscorerName, numberOfGoals);
		
		
		//for total
		if(team.getGoalScorers().get("Total").containsKey(goalscorerName))
			team.getGoalScorers().get("Total").put(goalscorerName, team.getGoalScorers().get("Total").get(goalscorerName)+numberOfGoals);
		
		else
			team.getGoalScorers().get("Total").put(goalscorerName, numberOfGoals);
		
	}
	
	
	private void recalculateGoalsAndUpdateGoalscorersMap(TeamStats team, Matches m, String goalScorer)
	{
		System.out.println("Goalscorer "+ goalScorer);
		
		String[] fullInfo = goalScorer.split(" ");
		
		String goalscorerName = fullInfo[1];
		
		
		int numberOfGoals = 1; //by default
		
		if(fullInfo[0].contains(",")) //goalscorer has multiple goals in this match
		{
			String[] minutes = fullInfo[0].split(",");
			numberOfGoals = minutes.length;
		}
		
		if( team.getGoalScorers().get(m.getCompetition()).containsKey(goalscorerName))
			team.getGoalScorers().get(m.getCompetition()).put(goalscorerName, team.getGoalScorers().get(m.getCompetition()).get(goalscorerName) + numberOfGoals);
		
		else
			team.getGoalScorers().get(m.getCompetition()).put(goalscorerName, numberOfGoals);
		
		//for total
		if(team.getGoalScorers().get("Total").containsKey(goalscorerName))
			team.getGoalScorers().get("Total").put(goalscorerName, team.getGoalScorers().get("Total").get(goalscorerName)+numberOfGoals);
		
		else
			team.getGoalScorers().get("Total").put(goalscorerName, numberOfGoals);
		
	}
	
	
	
	
	private void addHomeOrAwayGoalscorersProperly(TeamStats team, Matches m, String goalscorersAsString)
	{
		if(goalscorersAsString.contains(";")) // ; is used for separate different goal scorers, so in this match there were multiple goal scorers
		{
			String[] allGoalscorers = goalscorersAsString.split(";");
			
			for(String goalScorer : allGoalscorers)
			{
				if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) // in old FIFA seasons there are not minutes with goalscorers, only number of goals in match by player
				{
					recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(team, m, goalScorer);
				}
				else
				{
					recalculateGoalsAndUpdateGoalscorersMap(team, m, goalScorer);
				}
			}
		}
		
		else //there is 0 or 1 goalscorer
		{
			if( !goalscorersAsString.equalsIgnoreCase("/")) // '/' is used for no goalscorer - if string contains this character no one has scored so no one will be added to goalscorers lis
			{
				if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason()))
				{
					recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(team, m, goalscorersAsString);
				}
				else
				{
					recalculateGoalsAndUpdateGoalscorersMap(team, m, goalscorersAsString);
				}
			}
		
		}
		
	}
	

	@GetMapping("/getTeamStats/{teamname}")
	public TeamStats singleTeamStats(@PathVariable("teamname") String teamName) { //TODO Map a bude to treba iterovat cez zapasy -> kto najviac vyhral atd
		System.out.println("Get all teams...");


		List<Matches> matches = new ArrayList<>();
		
		Set<String> seasonsCL = new LinkedHashSet<>();
		Set<String> seasonsEL = new LinkedHashSet<>();
		
		matches = matchesRepository.getAllMatchesForTeam(teamName);
		
		TeamStats team = new TeamStats();
		
		team.setMatches(matches);
		
		//matches stats CL
		team.getMatchesStats().put("CL", new HashMap<String, Integer>());
		team.getMatchesStats().get("CL").put("Wins", 0);
		team.getMatchesStats().get("CL").put("Losses", 0);
		team.getMatchesStats().get("CL").put("Draws", 0);
		team.getMatchesStats().get("CL").put("GoalsScored", 0);
		team.getMatchesStats().get("CL").put("GoalsConceded", 0);
		team.getMatchesStats().get("CL").put("Seasons", 0);
		
		//matches stats EL
		team.getMatchesStats().put("EL", new HashMap<String, Integer>());
		team.getMatchesStats().get("EL").put("Wins", 0);
		team.getMatchesStats().get("EL").put("Losses", 0);
		team.getMatchesStats().get("EL").put("Draws", 0);
		team.getMatchesStats().get("EL").put("GoalsScored", 0);
		team.getMatchesStats().get("EL").put("GoalsConceded", 0);
		team.getMatchesStats().get("EL").put("Seasons", 0);
		
		//goalscorers
		team.getGoalScorers().put("CL", new HashMap<String, Integer>());
		team.getGoalScorers().put("EL", new HashMap<String, Integer>());
		team.getGoalScorers().put("Total", new HashMap<String, Integer>());
		
		team.getYellowCards().put("CL", new HashMap<String, Integer>());
		team.getYellowCards().put("EL", new HashMap<String, Integer>());
		
		team.getRedCards().put("CL", new HashMap<String, Integer>());
		team.getRedCards().put("EL", new HashMap<String, Integer>());
		


		for(Matches m : matches)
		{			
			
			//wins, draws, losses counter
			if(m.getWinner().equalsIgnoreCase("D"))
				team.getMatchesStats().get(m.getCompetition()).put("Draws", team.getMatchesStats().get(m.getCompetition()).get("Draws") + 1);
			
			else if(m.getWinner().equalsIgnoreCase(teamName))
				team.getMatchesStats().get(m.getCompetition()).put("Wins", team.getMatchesStats().get(m.getCompetition()).get("Wins") + 1);
			
			else
				team.getMatchesStats().get(m.getCompetition()).put("Losses", team.getMatchesStats().get(m.getCompetition()).get("Losses") + 1);
			
			
			//goal scored and conceded counter
			if(m.getHometeam().equalsIgnoreCase(teamName))
			{
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScorehome());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScoreaway());
			}	
				
			else if(m.getAwayteam().equalsIgnoreCase(teamName))
			{
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScoreaway());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScorehome());
			}
			
			
			if(m.getGoalscorers() != null)
			{
				String[] goalscorers = m.getGoalscorers().split("-");
				
				
				if(m.getHometeam().equalsIgnoreCase(teamName))
					addHomeOrAwayGoalscorersProperly(team, m, goalscorers[0]); // 0 - home goalscorers (they are written before character '-') 
				
				else
					addHomeOrAwayGoalscorersProperly(team, m, goalscorers[1]);
				
				System.out.println("");
//				String[] fullLogParts = m.getLog().split(",");
//				
//				goalscorers:64.Rodrigo, 72. Sol - 51.Kennedy,yellowCards:,redCards:
//				
//				String[] goalscorersPart = fullLogParts[0].; //TODO poriesit ked nie su zadane minuty
//				
				
				
			}
			
			
			
			
			if(m.getCompetition().equalsIgnoreCase("CL"))
				seasonsCL.add(m.getSeason());
			
			else if(m.getCompetition().equalsIgnoreCase("EL"))
				seasonsEL.add(m.getSeason());
			
		}
		
		
//		System.out.println("Seasons CL");
//		System.out.println(seasonsCL);
		
		team.getMatchesStats().get("CL").put("Seasons", seasonsCL.size());
		
		System.out.println("Seasons EL");
		System.out.println(seasonsEL);
		
		team.getMatchesStats().get("EL").put("Seasons", seasonsEL.size());
		
		Map<String, Integer> sortedMap = team.getGoalScorers().get("CL").entrySet().stream()
                .sorted(Entry.comparingByValue())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
	
		
		for (Map.Entry<String,Integer> entry : sortedMap.entrySet())  
		{        System.out.println("Key = " + entry.getKey() + 
                             ", Value = " + entry.getValue()); 
			} 
		
		team.getGoalScorers().put("CL", sortedMap);
		
		
		Map<String, Integer> sortedMap2 = team.getGoalScorers().get("EL").entrySet().stream()
                .sorted(Entry.comparingByValue())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
	
		System.out.println("Here is an EL goalscorers");
		for (Map.Entry<String,Integer> entry : sortedMap2.entrySet())  
		{        System.out.println("Key = " + entry.getKey() + 
                             ", Value = " + entry.getValue()); 
			} 
		
		team.getGoalScorers().put("EL", sortedMap2);
		
		FileModel fm = fileRepository.findByTeamname(teamName);
		
		team.setFm(fm);
		
		System.out.println(team.getGoalScorers().get("CL"));
		
		return team;
	}
	
	

	
	@GetMapping("/getGlobalTeamStats")
	public List<Team> allGlobalTeamStats() { //TODO Map a bude to treba iterovat cez zapasy -> kto najviac vyhral atd
		System.out.println("Get all teams...");

		List<Team> teams = new ArrayList<>();
		teamRepository.findAll().forEach(teams::add);

		
		int numberOfTeamsPresentedInCL = 0;
		int numberOfTeamsPresentedInEL = 0;
		for(Team t : teams)
		{
			if (t.getFirstSeasonCL() == null)
				t.setFirstSeasonCL("never");
			else
				numberOfTeamsPresentedInCL++;
			
			if (t.getFirstSeasonEL() == null)
				t.setFirstSeasonEL("never");
			
			else
				numberOfTeamsPresentedInEL++;
			
		}
		
		return teams;
	}
	
	@PostMapping(value = "/create")
	public void postCustomer(@RequestBody Team team) {

		
		System.out.println("Ukladam team "+ team.getTeamName()+ " z krajiny " +team.getCountry());
		Team newTeam =  teamRepository.save(new Team(team.getTeamName(), "2010", "2015", team.getCountry()));
		
//		Team newTeam = teamRepository.save(new Team(team.getTeamName(), team.getFirstSeasonCL(), team.getFirstSeasonEL(), team.getCountry()));
//		return newTeam;
	}
	
	
	@PutMapping("/update/{tName}")
	public ResponseEntity<Team> updateCustomer(@PathVariable("tName") String tName, @RequestBody Team team) {
		

		Optional<Team> teamToUpdate = Optional.of(teamRepository.findByTeamName(tName));
		
		if(teamToUpdate.isPresent())
		{
			Team newTeam = teamToUpdate.get();
			newTeam.setTeamName(team.getTeamName());
			newTeam.setFirstSeasonCL(team.getFirstSeasonCL());
			newTeam.setFirstSeasonEL(team.getFirstSeasonEL());
			newTeam.setCountry(team.getCountry());
			
			return new ResponseEntity<>(teamRepository.save(newTeam), HttpStatus.OK);
		}
		
		else
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	
	}
	
	

}
