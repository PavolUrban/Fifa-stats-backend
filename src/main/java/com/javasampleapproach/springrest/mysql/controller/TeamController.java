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

import com.javasampleapproach.springrest.mysql.model.*;
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
	
	
	@GetMapping("/getAllTeamsWithLogo")
	public List<TeamWithLogo> getAllCustomers() {
		List<Team> teams = new ArrayList<>();
		List<FileModel> logos = new ArrayList<>();
		List<TeamWithLogo> finalTeams = new ArrayList<>();

		teamRepository.findAll().forEach(teams::add);
		fileRepository.findAll().forEach(logos::add);;

		for(Team t : teams) {
			if (t.getFirstSeasonCL() == null) {
				t.setFirstSeasonCL("never");
			}

			if (t.getFirstSeasonEL() == null) {
				t.setFirstSeasonEL("never");
			}

			TeamWithLogo twl = new TeamWithLogo(t);

			FileModel logo = logos.stream().filter(l->l.getTeamname().equalsIgnoreCase(t.getTeamName())).findFirst().orElse(null);
			twl.setFm(logo);

			finalTeams.add(twl);
		}
		
		return finalTeams;
	}

	private void getMatchesByCompetition(List<Matches> matchesToFilter, String competition, String result, TeamStats team){
		List<Matches> filteredMatches = matchesToFilter.stream().filter(m->m.getCompetition().equalsIgnoreCase(competition)).collect(Collectors.toList());
		team.getFinalMatches().get(competition).put(result, filteredMatches);
	}

	@GetMapping("/getTeamStats/{teamname}")
	public TeamStats singleTeamStats(@PathVariable("teamname") String teamName) {
		Set<String> seasonsCL = new LinkedHashSet<>();
		Set<String> seasonsEL = new LinkedHashSet<>();

		List<Matches> matches  = matchesRepository.getAllMatchesForTeam(teamName);
		FileModel logo = fileRepository.findByTeamname(teamName);

		TeamStats team = new TeamStats();
		team.setMatches(matches);
		team.setFm(logo);

		List<Matches> finalMatches = matches.stream().filter(m->m.getCompetitionPhase().equalsIgnoreCase("Final")).collect(Collectors.toList());
		List<Matches> lostFinalMatches = finalMatches.stream().filter(m-> ! (m.getWinner().equalsIgnoreCase(teamName))).collect(Collectors.toList());
		List<Matches> wonFinalMatches = finalMatches.stream().filter(m-> m.getWinner().equalsIgnoreCase(teamName)).collect(Collectors.toList());


		team.getFinalMatches().get("Total").put("Won", wonFinalMatches);
		team.getFinalMatches().get("Total").put("Lost", lostFinalMatches);

		getMatchesByCompetition(lostFinalMatches, "CL", "Lost", team);
		getMatchesByCompetition(wonFinalMatches, "CL", "Won", team);
		getMatchesByCompetition(lostFinalMatches, "EL", "Lost", team);
		getMatchesByCompetition(wonFinalMatches, "EL", "Won", team);


		for(Matches m : matches)  {

			// wins, draws, losses counter
			if (m.getWinner().equalsIgnoreCase("D")) {
				team.getMatchesStats().get(m.getCompetition()).put("Draws", team.getMatchesStats().get(m.getCompetition()).get("Draws") + 1);
			} else if (m.getWinner().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("Wins", team.getMatchesStats().get(m.getCompetition()).get("Wins") + 1);
			} else {
				team.getMatchesStats().get(m.getCompetition()).put("Losses", team.getMatchesStats().get(m.getCompetition()).get("Losses") + 1);
			}

			// goalScored and goalsConceded counter
			if(m.getHometeam().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScorehome());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScoreaway());
			} else if(m.getAwayteam().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScoreaway());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScorehome());
			}

			// goalscorers
			if(m.getGoalscorers() != null) {
				String[] goalscorers = m.getGoalscorers().split("-");

				if(m.getHometeam().equalsIgnoreCase(teamName)) {
					addHomeOrAwayGoalscorersProperly(team, m, goalscorers[0]); // 0 - home goalscorers (they are written before character '-')
				} else {
					addHomeOrAwayGoalscorersProperly(team, m, goalscorers[1]);
				}
			}

			// seasons in CL/EL
			if (m.getCompetition().equalsIgnoreCase("CL")) {
				seasonsCL.add(m.getSeason());
			} else if(m.getCompetition().equalsIgnoreCase("EL")) {
				seasonsEL.add(m.getSeason());
			}
		}

		team.getMatchesStats().get("CL").put("Seasons", seasonsCL.size());
		team.getMatchesStats().get("EL").put("Seasons", seasonsEL.size());

		Map<String, Integer> sortedMapCL = sortMap(team.getGoalScorers().get("CL"));
		printGoalscorersMap(sortedMapCL);
		team.getGoalScorers().put("CL", sortedMapCL);

		Map<String, Integer> sortedMapEL = sortMap(team.getGoalScorers().get("EL"));
		printGoalscorersMap(sortedMapEL);
		team.getGoalScorers().put("EL", sortedMapEL);


		transformGoalMapToTimeRanges(team);

		return team;
	}

	private void transformGoalMapToTimeRanges(TeamStats team){

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

		team.getGoalsByMinutesCount().forEach((time, goalsCount)->{
			int timeAsInt = Integer.parseInt(time);

			allRanges.forEach(range->{
				if( (timeAsInt > range.getLowBorder()) && (timeAsInt <= range.getUpBorder())){
					range.setNumberOfGoals(range.getNumberOfGoals() + goalsCount);
				} else if( (timeAsInt > wholeRange) && (range.getUpBorder() == 90)){
					// add all goals scored after 90 minute to the last range
					range.setNumberOfGoals(range.getNumberOfGoals() + goalsCount);
				}
			});
		});

		team.setGoalsPerTimeRanges(allRanges);
	}



	// TODO - who has won the most games, most red cards ...
	@GetMapping("/getGlobalTeamStats")
	public List<Team> allGlobalTeamStats() {

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

	// TODO check if this one is used
	@PostMapping(value = "/create")
	public void postCustomer(@RequestBody Team team) {


		System.out.println("Ukladam team "+ team.getTeamName()+ " z krajiny " +team.getCountry());
		Team newTeam =  teamRepository.save(new Team(team.getTeamName(), "2010", "2015", team.getCountry()));

		// Team newTeam = teamRepository.save(new Team(team.getTeamName(), team.getFirstSeasonCL(), team.getFirstSeasonEL(), team.getCountry()));
		// return newTeam;
	}

	// TODO check if this one is used
	@PutMapping("/update/{tName}")
	public ResponseEntity<Team> updateCustomer(@PathVariable("tName") String tName, @RequestBody Team team) {
		Optional<Team> teamToUpdate = Optional.of(teamRepository.findByTeamName(tName));

		if(teamToUpdate.isPresent()) {
			Team newTeam = teamToUpdate.get();
			newTeam.setTeamName(team.getTeamName());
			newTeam.setFirstSeasonCL(team.getFirstSeasonCL());
			newTeam.setFirstSeasonEL(team.getFirstSeasonEL());
			newTeam.setCountry(team.getCountry());
			return new ResponseEntity<>(teamRepository.save(newTeam), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}



	private void addHomeOrAwayGoalscorersProperly(TeamStats team, Matches m, String goalscorersAsString)
	{
		// check for multiple goalscorers - multiple goalscorers are separated by ;
		if (goalscorersAsString.contains(";")) {
			String[] allGoalscorers = goalscorersAsString.split(";");

			for (String goalScorer : allGoalscorers)
			{
				// in some old FIFA seasons there are not minutes with goalscorers, only number of goals in match by player
				if (MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) {
					recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(team, m, goalScorer);
				} else {
					recalculateGoalsAndUpdateGoalscorersMap(team, m, goalScorer);
				}
			}
		} else {
			// '/' is used for no goalscorer - if string contains this character no one has scored so no one will be added to goalscorers list
			if (!goalscorersAsString.equalsIgnoreCase("/")) {
				if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) {
					recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(team, m, goalscorersAsString);
				}
				else {
					recalculateGoalsAndUpdateGoalscorersMap(team, m, goalscorersAsString);
				}
			}
		}
	}
	
	private void recalculateGoalsAndUpdateScorersMapForOlderFifaSeasons(TeamStats team, Matches m, String goalScorer) {
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

		team.setUnknownTimeGoals(team.getUnknownTimeGoals() + numberOfGoals);

		// for EL or CL
		updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get(m.getCompetition()), goalscorerName, numberOfGoals);

		// for total
		updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get("Total"), goalscorerName, numberOfGoals);
	}


	private void recalculateGoalsAndUpdateGoalscorersMap(TeamStats team, Matches m, String goalScorer)
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
				insertNewScoringTimeOrUpdateExisting(minutes[i], team);
			}
			numberOfGoals = minutes.length;
		} else {
			insertNewScoringTimeOrUpdateExisting(fullInfo[0], team);
		}

		// for EL or CL
		updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get(m.getCompetition()), goalscorerName, numberOfGoals);

		// for total
		updateOrInsertGoalscorerToProperCompetition(team.getGoalScorers().get("Total"), goalscorerName, numberOfGoals);
	}

	private void updateOrInsertGoalscorerToProperCompetition(Map<String, Integer> map, String goalscorerName, int numberOfGoals) {
		if (map.containsKey(goalscorerName)) {
			map.put(goalscorerName, map.get(goalscorerName) + numberOfGoals);
		} else {
			map.put(goalscorerName, numberOfGoals);
		}
	}

	private void insertNewScoringTimeOrUpdateExisting(String goalTime, TeamStats team){
		if (team.getGoalsByMinutesCount().containsKey(goalTime)) {
			team.getGoalsByMinutesCount().put(goalTime, team.getGoalsByMinutesCount().get(goalTime) + 1);
		} else {
			team.getGoalsByMinutesCount().put(goalTime, 1);
		}
	}

	private Map<String, Integer> sortMap(Map<String, Integer> mapToSort){
		Map<String, Integer> sortedMap = mapToSort.entrySet().stream()
				.sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap;
	}

	private void printGoalscorersMap(Map<String, Integer> mapToPrint){
		for (Map.Entry<String,Integer> entry : mapToPrint.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
	}

}
