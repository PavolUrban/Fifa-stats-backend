package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayOffMatch;
import com.javasampleapproach.springrest.mysql.model.TableTeam;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;

import static Utils.MyUtils.KOTLIK;
import static Utils.MyUtils.PAVOL_JAY;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/completeSeasons")
public class SeasonsController {
	
	@Autowired
	MatchesRepository matchesRepository;
	
	@Autowired
	FileRepository fileRepository;

	@GetMapping("/getAllPhases/{season}/{competition}")
	public Object getAllPhasesForSeasondAndCompetition(@PathVariable("season") String season, @PathVariable("competition") String competition) {
		List<FileModel> allLogos = new ArrayList<>();
		Map<String, Object> finalTablesWithStats = new HashMap<String, Object>();
		GlobalStatsController globalStats = new GlobalStatsController();
		Map<String, List<TableTeam>> groupsWithTeams = new HashMap<String, List<TableTeam>>();

		/*  *********** GROUP STAGE *********** */
		List<Matches> matches = matchesRepository.getAllMatchesBySeasonAndCompetitionGroupStage(season, competition);

		Set<String> groupNames = new HashSet<>();
		matches.stream().filter(p ->  groupNames.add(p.getCompetitionPhase())).collect(Collectors.toList());

		Map<String, Object> groupStatsForPlayers = new HashMap<>();
		Map<String, Object> groupGoalscorers = new HashMap<>();
		
		for(String groupName : groupNames) {
			
			Set<String> teamNamesInCurrentGroup = new HashSet<>();

			matches.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(groupName)).forEach(a->{
				teamNamesInCurrentGroup.add(a.getHometeam());
				teamNamesInCurrentGroup.add(a.getAwayteam());
			});

			//goalscorers !!
			List<Goalscorer> goalscorers = globalStats.getAllGoalscorers(matches.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(groupName)).collect(Collectors.toList()));
			groupGoalscorers.put(groupName, goalscorers);

			//player stats PavolJay vs Kotlik
			Map<String, Integer> winsByPlayersInCurrentGroup = new HashMap<String, Integer>();
			winsByPlayersInCurrentGroup.put("Pavol Jay", 0);
			winsByPlayersInCurrentGroup.put("Kotlik", 0);
			winsByPlayersInCurrentGroup.put("Draws", 0);
			
			
			List<TableTeam> allTeamsInCurrentGroup = new ArrayList();
			for(String teamName : teamNamesInCurrentGroup) {
				TableTeam tableTeam = new TableTeam();
				List<Matches> allMatchesByTeam = matches.stream().filter(s -> teamName.equalsIgnoreCase(s.getHometeam()) || s.getAwayteam().equalsIgnoreCase(teamName) ).collect(Collectors.toList());
			
				Map<String, Integer> winsByPlayersByCurrentTeam = setnumberOfPlayersWins(allMatchesByTeam);
				winsByPlayersInCurrentGroup.put("Pavol Jay", winsByPlayersByCurrentTeam.get("Pavol Jay") + winsByPlayersInCurrentGroup.get("Pavol Jay"));
				winsByPlayersInCurrentGroup.put("Kotlik", winsByPlayersByCurrentTeam.get("Kotlik") + winsByPlayersInCurrentGroup.get("Kotlik"));
				winsByPlayersInCurrentGroup.put("Draws", winsByPlayersByCurrentTeam.get("Draws") + winsByPlayersInCurrentGroup.get("Draws"));
				
				
				int sumScoredHome = allMatchesByTeam.stream().filter(o -> o.getHometeam().equalsIgnoreCase(teamName)).mapToInt(o -> o.getScorehome()).sum();
				int sumScoredAway = allMatchesByTeam.stream().filter(o -> o.getAwayteam().equalsIgnoreCase(teamName)).mapToInt(o -> o.getScoreaway()).sum();
				int sumConcededHome = allMatchesByTeam.stream().filter(o -> o.getHometeam().equalsIgnoreCase(teamName)).mapToInt(o -> o.getScoreaway()).sum();
				int sumConcededAway = allMatchesByTeam.stream().filter(o -> o.getAwayteam().equalsIgnoreCase(teamName)).mapToInt(o -> o.getScorehome()).sum();
				
				long wins = allMatchesByTeam.stream().filter(p-> p.getWinner().equalsIgnoreCase(teamName)).count();
				long draws = allMatchesByTeam.stream().filter(p-> p.getWinner().equalsIgnoreCase("D")).count();
				long losses = allMatchesByTeam.size()- wins - draws;
					
				tableTeam.setTeamname(teamName);
				tableTeam.setWins((int) wins);
				tableTeam.setDraws((int) draws);
				tableTeam.setLosses((int) losses);
				tableTeam.setMatches(tableTeam.getWins() + tableTeam.getDraws() + tableTeam.getLosses());
				tableTeam.setGoalsScored(sumScoredHome + sumScoredAway);
				tableTeam.setGoalsConceded(sumConcededHome + sumConcededAway);
				tableTeam.setPoints(tableTeam.getWins()*3 + tableTeam.getDraws()*1);

				FileModel test = fileRepository.findByTeamname(teamName);
				if(test !=null)
				{
					tableTeam.setLogo(test);
					allLogos.add(test);
				}

				//todo for this table TeamsOwnerBySeason is prepared - USE IT SOON!
				long currentTeamMatchesByPavolJay = allMatchesByTeam
						.stream()
						.filter(match->
										(match.getHometeam().equalsIgnoreCase(teamName) && match.getPlayerH().equalsIgnoreCase(PAVOL_JAY)) ||
										(match.getAwayteam().equalsIgnoreCase(teamName) && match.getPlayerA().equalsIgnoreCase(PAVOL_JAY))
						).count();

				double playedByPavolJayPercentage = currentTeamMatchesByPavolJay/ (allMatchesByTeam.size() *1.0);

				if(playedByPavolJayPercentage>0.6){
					tableTeam.setOwnedByPlayer(PAVOL_JAY);
				} else {
					tableTeam.setOwnedByPlayer(KOTLIK);
				}
				
				allTeamsInCurrentGroup.add(tableTeam);
			}
			
			//players bilance
			winsByPlayersInCurrentGroup.put("Pavol Jay", winsByPlayersInCurrentGroup.get("Pavol Jay")/2);
			winsByPlayersInCurrentGroup.put("Kotlik", winsByPlayersInCurrentGroup.get("Kotlik")/2);
			winsByPlayersInCurrentGroup.put("Draws", winsByPlayersInCurrentGroup.get("Draws")/2);	
			groupStatsForPlayers.put(groupName, winsByPlayersInCurrentGroup);
			
			Collections.sort(allTeamsInCurrentGroup, (o1, o2) -> o2.getPoints().compareTo(o1.getPoints()));
			
			groupsWithTeams.put(groupName, allTeamsInCurrentGroup);
		
		}
	
		//TODO fix this redundant call of playoffs
    	Map<String, List<PlayOffMatch>> playOffs = getPlayOffs(season, competition);

    	List<Matches> matchesPO = matchesRepository.getAllMatchesBySeasonAndCompetitionPlayOffs(season, competition);
    	int goalsPlayOffStage = matchesPO.stream().mapToInt(m -> m.getScorehome() + m.getScoreaway()).sum();
    	
    	
		List<Goalscorer> goalscorersPO = globalStats.getAllGoalscorers(matchesPO); //TODO this is complete list of play off goalscorers -> just store it somewhere to not do it again??

    	
    	
		int goalsGroupStage = matches.stream().mapToInt(m -> m.getScorehome() + m.getScoreaway()).sum();
		
		
		Matches finalMatch = matchesPO.stream().filter(m-> m.getCompetitionPhase().equalsIgnoreCase("Final")).findFirst().orElse(null);
		PlayersController playersStatsController  = new PlayersController();
	
		String winner = "unknown";
		if(finalMatch != null)
		{
			winner = playersStatsController.whoIsWinnerOfMatch(finalMatch, "Pavol Jay", "Kotlik");
			
		}
		
		finalTablesWithStats.put("CLWinnerPlayer", winner);
		finalTablesWithStats.put("StatsByGroups", groupStatsForPlayers);
		finalTablesWithStats.put("Tables", groupsWithTeams);
		finalTablesWithStats.put("Goalscorers", groupGoalscorers);
		finalTablesWithStats.put("MatchesCount", setCountsForDifferentStages(matches.size(), matchesPO.size()));
		finalTablesWithStats.put("GoalsCount", setCountsForDifferentStages(goalsGroupStage, goalsPlayOffStage));
		finalTablesWithStats.put("PlayOffs", playOffs);
		
		finalTablesWithStats.put("Final", finalMatch);
		
		if(finalMatch != null) {
			finalTablesWithStats.put("WinnerTeamLogo", fileRepository.findByTeamname(finalMatch.getWinner()));
		}

		
//		finalTablesWithStats.put("TotalGoalscorersGroupStage", getTotalGoalscorersFromGroups(groupGoalscorers, allLogos));
		
		
		List<Goalscorer> groupStageGoalscorers = helperFunction(groupGoalscorers);
		
		finalTablesWithStats.put("TotalGoalscorersGroupStage", getTotalGoalscorers(groupStageGoalscorers, allLogos));
		finalTablesWithStats.put("TotalGoalscorersPlayOffs", getTotalGoalscorers(goalscorersPO, allLogos));
		
		
		
		List<Goalscorer> goalScorersAllPhases = createTotalGoalscorersAllPhases(groupStageGoalscorers, goalscorersPO); // here is every player who score at least goal no matter if it was only in GS or PO
		finalTablesWithStats.put("TotalGoalscorersAllPhases", getTotalGoalscorers(goalScorersAllPhases, allLogos));
		
		
		
		return finalTablesWithStats;
	}
	
	
	
	//remember both param list must be sorted!!
	private List<Goalscorer> createTotalGoalscorersAllPhases(List<Goalscorer> groupStage, List<Goalscorer> playOffs)
	{
		List<Goalscorer> allPhasesGoalscorers = new ArrayList<Goalscorer>();
		
		for(Goalscorer g : groupStage)
		{
			Goalscorer gInPlayOffs = playOffs.stream().filter(p->p.getName().equalsIgnoreCase(g.getName())).findAny().orElse(null);
			
			if(gInPlayOffs != null) // if player scored in groups and play offs update his goals and add him in all cases to final list
			{
				Goalscorer scorerInGroupsAndPo = new Goalscorer();
				scorerInGroupsAndPo.setName(g.getName());
				scorerInGroupsAndPo.setTotalGoalsCount(g.getTotalGoalsCount() + gInPlayOffs.getTotalGoalsCount());
				scorerInGroupsAndPo.setGoalsByTeams(g.getGoalsByTeams()); //TODO tuto treba updatnut, nebude to sediet v dialogu
				allPhasesGoalscorers.add(scorerInGroupsAndPo);
			}
			
			else
				allPhasesGoalscorers.add(g);
		}
		
		//we must check also if someone got to total goarscorers only by goals from 
		for(Goalscorer gPO : playOffs)
		{
			Goalscorer gAlreadyAdded = allPhasesGoalscorers.stream().filter(a-> a.getName().equalsIgnoreCase(gPO.getName())).findAny().orElse(null); 
			
			//if this player is not in allPhasesGoalscorers, add him
			if(gAlreadyAdded == null)
				allPhasesGoalscorers.add(gPO);
		}
		
		
		Collections.sort(allPhasesGoalscorers, (o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));
		
		return allPhasesGoalscorers;
	}
	
	private List<Integer> setCountsForDifferentStages(int groupCount, int playOffCount)
	{
		List<Integer> counts = new ArrayList<>();
		counts.add(groupCount);
		counts.add(playOffCount);
		
		return counts;
	}
	
	
	//this runs when array with goalscorers is sorted
	private List<Goalscorer> getTotalGoalscorers(List<Goalscorer> goalscorers, List<FileModel>logos)
	{
		
		List<Goalscorer> finalGoalscorers = goalscorers;
		
		
		//TODO this is used multiple times add it to function
		  for(Goalscorer top : finalGoalscorers)
		   {
			   
			   String goalscorersTeam = top.getGoalsByTeams().keySet().stream().findFirst().get();
			   FileModel currentLogo = logos.stream().filter(l -> l.getTeamname().equalsIgnoreCase(goalscorersTeam)).findFirst().orElse(null);
			   if(currentLogo == null) {
				   currentLogo = fileRepository.findByTeamname(goalscorersTeam);
			   }
			   top.setTeamLogo(currentLogo);
		   }
		
		return finalGoalscorers;
	}
	
	//TODO rename and merge with getTotalGoalscorersFromGroups
	private List<Goalscorer> helperFunction(Map<String, Object> groupGoalscorers)
	{
			List<Goalscorer> finalList = new ArrayList<>();
		
		  for (String name : groupGoalscorers.keySet())  
		   {
			   	List<Goalscorer> gg = (List<Goalscorer>) groupGoalscorers.get(name);
				gg.stream().forEach(g->finalList.add(g));
		   }
		  
		   Collections.sort(finalList, (o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));
		  
		  return finalList;
	}
	
	
	//this goes throug groups e.g. A-H and get best goalscorers among these groups
	private List<Goalscorer> getTotalGoalscorersFromGroups(Map<String, Object> groupGoalscorers, List<FileModel> logos)
	{
		
		List<Goalscorer> topGoalscorers = new ArrayList<>();
		
	
	   for (String name : groupGoalscorers.keySet())  
	   {
		   	List<Goalscorer> gg = (List<Goalscorer>) groupGoalscorers.get(name);
			Collections.sort(gg, (o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));
	
			for(Goalscorer g : gg)
			{
				if(topGoalscorers.size()<3) // add top 3 Goalscorers automatically
					topGoalscorers.add(g);
				
				else
				{
					 int minTopGoals = topGoalscorers.stream().mapToInt(Goalscorer::getTotalGoalsCount).min().orElse(-1);
					 
					 if(g.getTotalGoalsCount()>minTopGoals) // if current goalscorer has more goals then min in Top goalscorers, remove all with min and add current
					 {
						 topGoalscorers.removeIf(obj -> obj.getTotalGoalsCount() == minTopGoals);
						 topGoalscorers.add(g);
					 }
					 
					 else if(g.getTotalGoalsCount() == minTopGoals) //if goalscorers has at least min top goals add him and do not remove anything
						 topGoalscorers.add(g);
				}
			}

	   }
		   
	   Collections.sort(topGoalscorers, (o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));
	   
	   for(Goalscorer top : topGoalscorers)
	   {
		   
		   String goalscorersTeam = top.getGoalsByTeams().keySet().stream().findFirst().get();
		   FileModel currentLogo = logos.stream().filter(l -> l.getTeamname().equalsIgnoreCase(goalscorersTeam)).findFirst().orElse(null);
		   top.setTeamLogo(currentLogo);
	   }
	   
	   return topGoalscorers;
	}
	
	@GetMapping("/getAllPlayOff/{season}/{competition}")
	private Map<String, List<PlayOffMatch>> getPlayOffs(@PathVariable("season") String season, @PathVariable("competition") String competition)
	{
		List<Matches> matches = matchesRepository.getAllMatchesBySeasonAndCompetitionPlayOffs(season, competition);

		Set<String> phases = new HashSet<String>();
		
		matches.stream().filter(p ->  phases.add(p.getCompetitionPhase())).collect(Collectors.toList());
		
		
		Map<String, List<PlayOffMatch>> matchesInAllPhases = new HashMap<String, List<PlayOffMatch>>();
		
		for(String phase: phases)
		{
			
			List<Matches> matchesInCurrentPhase = new ArrayList<>();
			matches.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(phase)).filter(o -> matchesInCurrentPhase.add(o)).collect(Collectors.toList());
			
			List<PlayOffMatch> playOffMatches = new ArrayList<>();
			
			if( !phase.equalsIgnoreCase("Final") )// final is played only as single match not classic play off with two matches
			{
				
				int addedMatches = 0;
				
				for(Matches m : matchesInCurrentPhase)
				{
					
					
					if(addedMatches >= matchesInCurrentPhase.size())
						break;
					
					Matches match1 = matchesInCurrentPhase.get(addedMatches);
					Matches match2 = matchesInCurrentPhase.get(addedMatches+1);
					

					ArrayList<String> teamNames = new ArrayList<>(Arrays.asList(match1.getHometeam(), match1.getAwayteam()));
					String qualifiedTeam = whoIsQualified(match1, match2);
					String nonQualifiedTeam = getNonQualified(teamNames, qualifiedTeam);
			
					PlayOffMatch playOff = new PlayOffMatch(match1, match2, qualifiedTeam, nonQualifiedTeam);
					
					playOff.setQualifiedTeamGoals(getPlayOffGoalsForTeam(playOff, qualifiedTeam));
					playOff.setNonQualifiedTeamGoals(getPlayOffGoalsForTeam(playOff, nonQualifiedTeam));
					
					playOffMatches.add(playOff);
					addedMatches = addedMatches + 2;
				}
			}
			
			
			matchesInAllPhases.put(phase, playOffMatches);
			
		}
		
		
		return matchesInAllPhases;
		
	}

	
	private int getPlayOffGoalsForTeam(PlayOffMatch pom, String teamToGetScore) 
	{
		int score = 0;
		
		if(teamToGetScore.equalsIgnoreCase(pom.getFirstMatch().getHometeam()))
			score = pom.getFirstMatch().getScorehome() + pom.getSecondMatch().getScoreaway();
		
		else if(teamToGetScore.equalsIgnoreCase(pom.getFirstMatch().getAwayteam()))
			score = pom.getFirstMatch().getScoreaway() + pom.getSecondMatch().getScorehome();
		
		return score;
	}
	
	
	
	private Map<String, Integer> setnumberOfPlayersWins(List<Matches> matches)
	{
		PlayersController playersStatsController  = new PlayersController();
		
		Map<String, Integer> winsByPlayers = new HashMap<String, Integer>();
		
		winsByPlayers.put("Pavol Jay", 0);
		winsByPlayers.put("Kotlik", 0);
		winsByPlayers.put("Draws", 0);
		
		
		for(Matches m: matches)
		{
			String winner = playersStatsController.whoIsWinnerOfMatch(m, "Pavol Jay", "Kotlik");
			
			if(winner.equalsIgnoreCase("D")) //draw
				winsByPlayers.put("Draws", winsByPlayers.get("Draws") + 1);
			
			else if(winner.equalsIgnoreCase("Pavol Jay"))
				winsByPlayers.put("Pavol Jay", winsByPlayers.get("Pavol Jay") + 1);
			
			else
				winsByPlayers.put("Kotlik", winsByPlayers.get("Kotlik") + 1);
		}
			
		
		return winsByPlayers;
	}
	
	private String getNonQualified(List<String>teamNames, String qualified)
	{
		teamNames.remove(qualified);
		
		return teamNames.get(0);
	}
	
	//TODO check if this function works when two teams have equall scores from 2 matches!!!
	private String whoIsQualified(Matches match1, Matches match2)
	{
		String qualifiedTeam = "unknown";
		
		int firstTeamGoals = match1.getScorehome() + match2.getScoreaway();
		int secondTeamGoals = match2.getScorehome() + match1.getScoreaway() ;
		
		if(firstTeamGoals > secondTeamGoals)
			qualifiedTeam = match1.getHometeam();
		
		else if(secondTeamGoals > firstTeamGoals)
			qualifiedTeam = match2.getHometeam();
		
		else if(firstTeamGoals == secondTeamGoals)
		{
			if(match1.getScoreaway() > match2.getScoreaway())
				qualifiedTeam = match1.getAwayteam();
			
			else
				qualifiedTeam = match2.getAwayteam();
		}
		
		return qualifiedTeam;
	}
}
