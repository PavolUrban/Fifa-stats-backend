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

import Utils.MyUtils;
import Utils.NewestGoalscorersCalculator;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
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

	@Autowired
	FifaPlayerDBRepository fifaPlayerDBRepository;

	@Autowired
	RecordsInMatchesRepository recordsInMatchesRepository;

	@GetMapping("/getAllPhases/{season}/{competition}")
	public Object getAllPhasesForSeasonAndCompetition(@PathVariable("season") String season, @PathVariable("competition") String competition) {
		List<FileModel> allLogos = new ArrayList<>();
		Map<String, Object> finalTablesWithStats = new HashMap<>();
		Map<String, List<TableTeam>> groupsWithTeams = new HashMap<>();
		NewestGoalscorersCalculator ngc = new NewestGoalscorersCalculator(fifaPlayerDBRepository);

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

			// goalscorers per group
			List<RecordsInMatches> allGoalsInCurrentGroup = recordsInMatchesRepository.getRecordsByCompetition(groupName, season, competition, null,"G", "Penalty");
			groupGoalscorers.put(groupName, ngc.getGoalscorers(allGoalsInCurrentGroup));

			//player stats PavolJay vs Kotlik
			Map<String, Integer> winsByPlayersInCurrentGroup = new HashMap<>();
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
		int goalsGroupStage = matches.stream().mapToInt(m -> m.getScorehome() + m.getScoreaway()).sum();
		
		
		Matches finalMatch = matchesPO.stream().filter(m-> m.getCompetitionPhase().equalsIgnoreCase("Final")).findFirst().orElse(null);
		PlayersController playersStatsController  = new PlayersController();
	
		String winner = "unknown";
		if(finalMatch != null) {
			winner = playersStatsController.whoIsWinnerOfMatch(finalMatch, PAVOL_JAY, KOTLIK);
			finalTablesWithStats.put("WinnerTeamLogo", fileRepository.findByTeamname(finalMatch.getWinner()));
		}
		
		finalTablesWithStats.put("CLWinnerPlayer", winner);
		finalTablesWithStats.put("StatsByGroups", groupStatsForPlayers);
		finalTablesWithStats.put("Tables", groupsWithTeams);
		finalTablesWithStats.put("Goalscorers", groupGoalscorers);
		finalTablesWithStats.put("MatchesCount", setCountsForDifferentStages(matches.size(), matchesPO.size()));
		finalTablesWithStats.put("GoalsCount", setCountsForDifferentStages(goalsGroupStage, goalsPlayOffStage));
		finalTablesWithStats.put("PlayOffs", playOffs);
		finalTablesWithStats.put("Final", finalMatch);


		List<RecordsInMatches> topGoalscorersGroupStage = recordsInMatchesRepository.getWholeGroupStageGoalscorersBySeasonAndCompetition(season,competition,"G", "Penalty");
		finalTablesWithStats.put("TotalGoalscorersGroupStage", ngc.getGoalscorers(topGoalscorersGroupStage));

		List<RecordsInMatches> topGoalscorersPlayOff = recordsInMatchesRepository.getWholePlayOffsGoalscorersBySeasonAndCompetition(season,competition,"G", "Penalty");
		finalTablesWithStats.put("TotalGoalscorersPlayOffs",  ngc.getGoalscorers(topGoalscorersPlayOff));

		List<RecordsInMatches> topGoalscorersAllPhases = recordsInMatchesRepository.getTotalGoalscorersBySeasonAndCompetition(season,competition,"G", "Penalty");
		finalTablesWithStats.put("TotalGoalscorersAllPhases",  ngc.getGoalscorers(topGoalscorersAllPhases));

		finalTablesWithStats.put("Logos", allLogos);
		
		return finalTablesWithStats;
	}
	
	

	private List<Integer> setCountsForDifferentStages(int groupCount, int playOffCount) {
		List<Integer> counts = new ArrayList<>();
		counts.add(groupCount);
		counts.add(playOffCount);
		
		return counts;
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
