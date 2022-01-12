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
	FileRepository fileRepository;

	@Autowired
	TeamRepository teamRepository;

	@Autowired
	SeasonsRepository seasonsRepository;


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
		//System.out.println(m);
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
	
	// pozor ak budem zjednocovat, pocita sa tu aj za kolko timov dal hrac gol
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
			goalscorer.setNumberOfTeamsPlayerScoredFor(playerGoals.size());
		    goalscorer.setTotalGoalsCount(goalsTotal);
		    
		    listOfGoalscorers.add(goalscorer);
		}
	  
		return listOfGoalscorers;
	}


	@GetMapping("/getAllGoalScorers")
	public Map<String, List<Goalscorer>> getAllGoalscorers()
	{
		Map<String, List<Goalscorer>> allGoalscorersByCompetition = new HashMap<>();
		List<Matches> matches = new ArrayList<>();

		matchesRepository.findAll().iterator().forEachRemaining(matches::add);
		List<Goalscorer> goalscorers = getAllGoalscorers(matches);
		allGoalscorersByCompetition.put("Total", goalscorers);

		getGoalscorersByCompetition(allGoalscorersByCompetition, matches, "CL", null);
		getGoalscorersByCompetition(allGoalscorersByCompetition, matches, "EL", null);

		return allGoalscorersByCompetition;
	}

	// TODO ---- method only for temporary usage - to generate fifaplayers into separate tables
	@Autowired
	FifaPlayerDBRepository fifaPlayerDBRepository;

	@PostMapping("/insertGoalscorers")
	public void insertGoalscorers(@RequestBody List<Goalscorer> goalscorersList){


		goalscorersList.forEach(goalscorer -> {
			goalscorer.setName(goalscorer.getName().replaceAll("(OG)","")); // toto budem riesit az v tabulke ako typ golu -vlastny, teraz len vytvaram hracov zaznam
			goalscorer.setName(goalscorer.getName().replaceAll("\\.", " "));


			FifaPlayerDB test = new FifaPlayerDB();
			test.setPlayerName(goalscorer.getName());
			test.setPlayerPosition("FW"); //todo in db
			//System.out.println("Trying to insert  "+ test.getPlayerName());

			//fifaPlayerDBRepository.save(test);
		});
	}
//todo to remove sooon
	@PostMapping("/insertPlayersWithCards")
	public void insertPlayersWithCardsToTable(@RequestBody List<FifaPlayer> players){
		List<String> namesToAdd = players.stream().map(p->p.getName()).collect(Collectors.toList());

		List<FifaPlayerDB> alreadyExistingPlayers = fifaPlayerDBRepository.findByPlayerNameIn(namesToAdd);
		List<String> alreadyExistingNames= alreadyExistingPlayers.stream().map(p->p.getPlayerName()).collect(Collectors.toList());


		namesToAdd.forEach(nameToAdd -> {
			//System.out.println(player.getName());

			boolean playerAlreadyExists = false;

			if(alreadyExistingNames.contains(nameToAdd)){
				playerAlreadyExists = true;
			}

			if(playerAlreadyExists!=true) {
				System.out.println("Tohto mozem pridat "+nameToAdd);

				FifaPlayerDB test = new FifaPlayerDB();
			test.setPlayerName(nameToAdd);
			test.setPlayerPosition("FW"); //todo in db
				System.out.println("Trying to insert  "+ test.getPlayerName());

				//fifaPlayerDBRepository.save(test);
			}

		});
	}

	// todo this class contains a lot of duplicate functionality - get rid of it + it has to be removed to team controller
	@GetMapping("/getSingleTeamGoalScorers/{teamName}")
	public Map<String, List<Goalscorer>> getTeamGoalscorers(@PathVariable("teamName") String teamName)
	{
		Map<String, List<Goalscorer>> allGoalscorersByCompetition = new HashMap<>();

		// total
		List<Matches> matches = matchesRepository.findByHometeamOrAwayteam(teamName, teamName);
		List<Goalscorer> goalscorers = getTeamGoalscorers(matches, teamName);
		allGoalscorersByCompetition.put("Total", goalscorers);

		getGoalscorersByCompetition(allGoalscorersByCompetition, matches, "CL", teamName);
		getGoalscorersByCompetition(allGoalscorersByCompetition, matches, "EL", teamName);

		return allGoalscorersByCompetition;
	}

	private void getGoalscorersByCompetition(Map<String, List<Goalscorer>> allGoalscorersByCompetition, List<Matches> matches, String competitionName, String teamName){
		List<Matches> matchesForCompetition = matches.stream().filter(m->m.getCompetition().equalsIgnoreCase(competitionName)).collect(Collectors.toList());
		List<Goalscorer> goalscorersForCompetition;

		if (teamName != null){
			goalscorersForCompetition = getTeamGoalscorers(matchesForCompetition, teamName);
		} else {
			goalscorersForCompetition = getAllGoalscorers(matchesForCompetition);
		}

		allGoalscorersByCompetition.put(competitionName, goalscorersForCompetition);
	}

	private List<Goalscorer> getTeamGoalscorers(Iterable<Matches> matches, String teamname) {

		Map<String,Map<String, Integer>> playerWithGoals = new HashMap<>();

		for(Matches m : matches)
		{
			if(m.getGoalscorers() != null)
			{
				String[] goalscorers = m.getGoalscorers().split("-");

				if(m.getHometeam().equalsIgnoreCase(teamname)){
					addGoalsScorers(playerWithGoals, m, goalscorers[0], m.getHometeam()); //home
				} else {
					addGoalsScorers(playerWithGoals, m, goalscorers[1], m.getAwayteam()); //away
				}
			}

		}

		List<Goalscorer> finalGoalscorers = transforMapToGoalscorersList(playerWithGoals);

		finalGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));

		return finalGoalscorers;
	}
	
	public	List<Goalscorer> getAllGoalscorers(Iterable<Matches> matches) {

		Map<String,Map<String, Integer>> playerWithGoals = new HashMap<>();

		for(Matches m : matches) {
			if(m.getGoalscorers() != null) {
				String[] goalscorers = m.getGoalscorers().split("-");
				addGoalsScorers(playerWithGoals, m, goalscorers[0], m.getHometeam()); //home
				addGoalsScorers(playerWithGoals, m, goalscorers[1], m.getAwayteam()); //away
			}
		}
		List<Goalscorer> finalGoalscorers = transforMapToGoalscorersList(playerWithGoals);
		finalGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));

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


	@GetMapping("/winnersList/{competition}")
	public List<ChampionsLeagueWinner> getWinnersList(@PathVariable("competition") String competition){

		List<Matches> finalMatches = matchesRepository.findByCompetitionPhaseAndCompetitionOrderBySeason("Final",competition);

		PlayersController pc = new PlayersController();

		List<ChampionsLeagueWinner> winnerList = new ArrayList<>();


// TODO use this for CL and EL displayed together

//		Iterable<Seasons> allSeasonList = seasonsRepository.findAll();
//		allSeasonList.forEach(season-> {
//			List<Matches> matchesFromCurrentSeason = finalMatches.stream().filter(m->m.getSeason().equalsIgnoreCase(season.getSeason())).collect(Collectors.toList());
//
//			ChampionsLeagueWinner clw = new ChampionsLeagueWinner();
//			clw.setSeason(season.getSeason());
//
//			getMatchByCompetition(matchesFromCurrentSeason, "CL", clw);
//			getMatchByCompetition(matchesFromCurrentSeason,"EL", clw);
//
//			winnerList.add(clw);
//
//		});


			finalMatches.forEach(match ->{
			ChampionsLeagueWinner clw = new ChampionsLeagueWinner();
			clw.setPlayerName(pc.whoIsWinnerOfMatch(match, "Pavol Jay", "Kotlik"));
			clw.setSeason(match.getSeason());
			clw.setTeamName(match.getWinner());
			clw.setRunnerUp(getRunnerUp(match));
			clw.setTeamLogo(fileRepository.findByTeamname(match.getWinner()));
			clw.setRunnerUpLogo(fileRepository.findByTeamname(clw.getRunnerUp()));

			winnerList.add(clw);
		});

//		Collections.sort(winnerList, (ChampionsLeagueWinner w1, ChampionsLeagueWinner w2) ->{
//			return w1.getSeason().compareToIgnoreCase(w2.getSeason());
//		});

		return winnerList;
	}

	private String getRunnerUp(Matches match) {
		String runnerUp;
		if(match.getWinner().equalsIgnoreCase(match.getHometeam())) {
			runnerUp = match.getAwayteam();
		} else {
			runnerUp = match.getHometeam();
		}

		return runnerUp;
	}

	//TODO Use this for view with both EL and CL
//	private void getMatchByCompetition(List<Matches> finalMatches, String competition, ChampionsLeagueWinner clw){
//		PlayersController pc = new PlayersController();
//
//		Matches matchBySeasonAndCompetition
//				= finalMatches
//					.stream()
//					.filter(m -> m.getCompetition().equalsIgnoreCase(competition))
//					.findAny().orElse(null);
//
//		if(matchBySeasonAndCompetition != null){
//			if(matchBySeasonAndCompetition.getCompetition().equalsIgnoreCase("CL")){
//				clw.setPlayerNameCL(pc.whoIsWinnerOfMatch(matchBySeasonAndCompetition, "Pavol Jay", "Kotlik"));
//				clw.setTeamNameCL(matchBySeasonAndCompetition.getWinner());
//				clw.setTeamLogoCL(fileRepository.findByTeamname(matchBySeasonAndCompetition.getWinner()));
//			} else {
//				clw.setPlayerNameEL(pc.whoIsWinnerOfMatch(matchBySeasonAndCompetition, "Pavol Jay", "Kotlik"));
//				clw.setTeamNameEL(matchBySeasonAndCompetition.getWinner());
//				clw.setTeamLogoEL(fileRepository.findByTeamname(matchBySeasonAndCompetition.getWinner()));
//			}
//		}
//	}

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
