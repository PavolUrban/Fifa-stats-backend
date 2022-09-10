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

import Utils.HelperMethods;
import Utils.MyUtils;
import Utils.NewestGoalscorersCalculator;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.H2HPlayers;
import com.javasampleapproach.springrest.mysql.model.OverallStats;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayOffMatch;
import com.javasampleapproach.springrest.mysql.model.TableTeam;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;

import static Utils.MyUtils.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/completeSeasons")
public class SeasonsController {
	
	@Autowired
	MatchesRepository matchesRepository;

	@Autowired
	FifaPlayerDBRepository fifaPlayerDBRepository;

	@Autowired
	RecordsInMatchesRepository recordsInMatchesRepository;

	@Autowired
	PlayersController playersStatsController  = new PlayersController();

	@GetMapping("/getAllPhases/{season}/{competition}")
	public Object getAllPhasesForSeasonAndCompetition(@PathVariable("season") String season, @PathVariable("competition") String competition) {
		Map<String, Object> finalTablesWithStats = new HashMap<>();
		Map<String, List<TableTeam>> groupsWithTeams = new HashMap<>();
		NewestGoalscorersCalculator ngc = new NewestGoalscorersCalculator(fifaPlayerDBRepository);

		/*  *********** GROUP STAGE *********** */
		List<Matches> matchesGroupStage = matchesRepository.getAllMatchesBySeasonAndCompetitionGroupStage(season, competition);

		Set<String> groupNames = new HashSet<>();
		matchesGroupStage.stream().filter(p ->  groupNames.add(p.getCompetitionPhase())).collect(Collectors.toList());

		Map<String, H2HPlayers> groupStatsForPlayers = new HashMap<>();
		Map<String, Object> groupGoalscorers = new HashMap<>();
		
		for(String groupName : groupNames) {
			
			Set<String> teamNamesInCurrentGroup = new HashSet<>();

			matchesGroupStage.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(groupName)).forEach(a->{
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
				List<Matches> allMatchesByTeam = matchesGroupStage.stream().filter(s -> teamName.equalsIgnoreCase(s.getHometeam()) || s.getAwayteam().equalsIgnoreCase(teamName) ).collect(Collectors.toList());
			
				Map<String, Integer> winsByPlayersByCurrentTeam = HelperMethods.setnumberOfPlayersWins(allMatchesByTeam);
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


				//todo for this table TeamsOwnerBySeason is prepared - USE IT SOON!
				long currentTeamMatchesByPavolJay =
						allMatchesByTeam.stream()
						.filter(match-> (match.getHometeam().equalsIgnoreCase(teamName) && match.getPlayerH().equalsIgnoreCase(PAVOL_JAY)) ||
										(match.getAwayteam().equalsIgnoreCase(teamName) && match.getPlayerA().equalsIgnoreCase(PAVOL_JAY))
						).count();

				double playedByPavolJayPercentage = currentTeamMatchesByPavolJay/ (allMatchesByTeam.size() * 1.0);

				if(playedByPavolJayPercentage>0.6){
					tableTeam.setOwnedByPlayer(PAVOL_JAY);
				} else {
					tableTeam.setOwnedByPlayer(KOTLIK);
				}
				
				allTeamsInCurrentGroup.add(tableTeam);
			}
			
			//players bilance
			H2HPlayers h2h = new H2HPlayers();
			h2h.setPavolJay(winsByPlayersInCurrentGroup.get("Pavol Jay")/2);
			h2h.setKotlik(winsByPlayersInCurrentGroup.get("Kotlik")/2);
			h2h.setDraws(winsByPlayersInCurrentGroup.get("Draws")/2);

			groupStatsForPlayers.put(groupName, h2h);
			
			Collections.sort(allTeamsInCurrentGroup, (o1, o2) -> o2.getPoints().compareTo(o1.getPoints()));
			
			groupsWithTeams.put(groupName, allTeamsInCurrentGroup);
		
		}
	
		// PlayOffs
    	List<Matches> matchesPO = matchesRepository.getAllMatchesBySeasonAndCompetitionPlayOffs(season, competition);
		Map<String, List<PlayOffMatch>> playOffs = getPlayOffs(matchesPO);

		Matches finalMatch = matchesPO.stream().filter(m-> m.getCompetitionPhase().equalsIgnoreCase("Final")).findFirst().orElse(null);

		finalTablesWithStats.put("statsByGroups", groupStatsForPlayers);

		finalTablesWithStats.put("groupStageTables", groupsWithTeams);
		finalTablesWithStats.put("goalscorersPerGroup", groupGoalscorers);

		finalTablesWithStats.put("playOffs", playOffs);
		finalTablesWithStats.put("Final", finalMatch);

		// Overall stats
		int goalsPlayOffStage = matchesPO.stream().mapToInt(m -> m.getScorehome() + m.getScoreaway()).sum();
		int goalsGroupStage = matchesGroupStage.stream().mapToInt(m -> m.getScorehome() + m.getScoreaway()).sum();
		Map<String, Integer> playoffStats = HelperMethods.setnumberOfPlayersWins(matchesPO);
		OverallStats overallStats = getOverallStats(matchesGroupStage.size(), matchesPO.size(), goalsGroupStage, goalsPlayOffStage, groupStatsForPlayers, playoffStats);
		overallStats.setWinnerPlayer(finalMatch != null ? playersStatsController.whoIsWinnerOfMatch(finalMatch, PAVOL_JAY, KOTLIK) : "unknown");
		overallStats.setWinnerTeam(finalMatch != null ? finalMatch.getWinner() : "unknown");
		setCardsForOverallStats(overallStats.getYellowCardsCount(), season, competition, "YC");
		setCardsForOverallStats(overallStats.getRedCardsCount(), season, competition, "RC");


		finalTablesWithStats.put("overallStats", overallStats);

		// Goalscorers
		List<RecordsInMatches> topGoalscorersGroupStage = recordsInMatchesRepository.getGroupStageRecordsBySeasonAndCompetition(season,competition,"G", "Penalty");
		finalTablesWithStats.put("totalGoalscorersGroupStage", ngc.getGoalscorers(topGoalscorersGroupStage));

		List<RecordsInMatches> topGoalscorersPlayOff = recordsInMatchesRepository.getPlayOffsRecordsBySeasonAndCompetition(season,competition,"G", "Penalty");
		finalTablesWithStats.put("totalGoalscorersPlayOffs",  ngc.getGoalscorers(topGoalscorersPlayOff));

		List<RecordsInMatches> topGoalscorersAllPhases = recordsInMatchesRepository.getTotalGoalscorersBySeasonAndCompetition(season,competition,"G", "Penalty");
		finalTablesWithStats.put("totalGoalscorersAllPhases",  ngc.getGoalscorers(topGoalscorersAllPhases));

		return finalTablesWithStats;
	}

	private void setCardsForOverallStats(List<Integer> cardsCount, String season, String competition, String cardType){
		int numberOfCardsInGroupStage = recordsInMatchesRepository.getGroupStageCardsCountBySeasonAndCompetition(season, competition,cardType);
		int numberOfCardsInPlayOffs = recordsInMatchesRepository.getPlayOffsCardsCountBySeasonAndCompetition(season, competition, cardType);
		cardsCount.add(numberOfCardsInGroupStage);
		cardsCount.add(numberOfCardsInPlayOffs);
		cardsCount.add(numberOfCardsInGroupStage + numberOfCardsInPlayOffs);
	}

	private OverallStats getOverallStats(int matchesGroupStageCount, int matchesPlayOffsCount, int goalsGroupStageCount, int goalsPlayOffsCount, Map<String, H2HPlayers> groupStatsForPlayers,Map<String, Integer> playoffStats) {
		OverallStats os = new OverallStats();

		// matches counts
		os.getMatchesCount().add(matchesGroupStageCount);
		os.getMatchesCount().add(matchesPlayOffsCount);
		os.getMatchesCount().add(matchesGroupStageCount + matchesPlayOffsCount);

		// goals counts
		os.getGoalsCount().add(goalsGroupStageCount);
		os.getGoalsCount().add(goalsPlayOffsCount);
		os.getGoalsCount().add(goalsGroupStageCount + goalsPlayOffsCount);

		H2HPlayers h2hGroupStage = new H2HPlayers();
		groupStatsForPlayers.forEach((group, h2h)-> {
			h2hGroupStage.setKotlik(h2hGroupStage.getKotlik() + h2h.getKotlik());
			h2hGroupStage.setPavolJay(h2hGroupStage.getPavolJay() + h2h.getPavolJay());
			h2hGroupStage.setDraws(h2hGroupStage.getDraws() + h2h.getDraws());
		});
		os.getH2hPlayers().add(h2hGroupStage);

		// todo this will have to be updated to String, H2H to have stats from distinct playoff stage
		H2HPlayers h2hPlayOffs = new H2HPlayers();
		h2hPlayOffs.setPavolJay(playoffStats.get(PAVOL_JAY));
		h2hPlayOffs.setKotlik(playoffStats.get(KOTLIK));
		h2hPlayOffs.setDraws(playoffStats.get("Draws"));
		os.getH2hPlayers().add(h2hPlayOffs);

		H2HPlayers h2hTotal = new H2HPlayers();
		h2hTotal.setPavolJay(h2hGroupStage.getPavolJay() + h2hPlayOffs.getPavolJay());
		h2hTotal.setKotlik(h2hGroupStage.getKotlik() + h2hPlayOffs.getKotlik());
		h2hTotal.setDraws(h2hGroupStage.getDraws() + h2hPlayOffs.getDraws());
		os.getH2hPlayers().add(h2hTotal);

		return os;
	}


	private Map<String, List<PlayOffMatch>> getPlayOffs(List<Matches> matches)
	{
		Set<String> phases = new HashSet<String>();
		matches.stream().filter(p ->  phases.add(p.getCompetitionPhase())).collect(Collectors.toList());
		Map<String, List<PlayOffMatch>> matchesInAllPhases = new HashMap<String, List<PlayOffMatch>>();
		
		for(String phase: phases) {
			List<Matches> matchesInCurrentPhase = new ArrayList<>();
			matches.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(phase)).filter(o -> matchesInCurrentPhase.add(o)).collect(Collectors.toList());
			
			List<PlayOffMatch> playOffMatches = new ArrayList<>();

			// final is played only as single match not classic play off with two matches
			if( !phase.equalsIgnoreCase("Final") ){
				
				int addedMatches = 0;
				
				for(Matches m : matchesInCurrentPhase) {
					
					
					if(addedMatches >= matchesInCurrentPhase.size())
						break;
					
					Matches match1 = matchesInCurrentPhase.get(addedMatches);
					Matches match2 = matchesInCurrentPhase.get(addedMatches+1);
					PlayersController pc = new PlayersController();
					match1.setWinnerPlayer(pc.whoIsWinnerOfMatch(match1, PAVOL_JAY, KOTLIK));
					match2.setWinnerPlayer(pc.whoIsWinnerOfMatch(match2, PAVOL_JAY, KOTLIK));

					ArrayList<String> teamNames = new ArrayList<>(Arrays.asList(match1.getHometeam(), match1.getAwayteam()));
					String qualifiedTeam = whoIsQualified(match1, match2);
					String nonQualifiedTeam = getNonQualified(teamNames, qualifiedTeam);
					String qualifiedPlayer = getQualifiedPlayer(qualifiedTeam, match1);
			
					PlayOffMatch playOff = new PlayOffMatch(new ArrayList<>(Arrays.asList(match1, match2)), qualifiedTeam, nonQualifiedTeam, qualifiedPlayer);
					
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

	// since players can for the same team in both play off matches we can just check one of them to get proper result
	private String getQualifiedPlayer(String qualifiedTeam, Matches match){
		String qualifiedPlayer;
		if(match.getHometeam().equalsIgnoreCase(qualifiedTeam)){
			qualifiedPlayer = match.getPlayerH();
		} else {
			qualifiedPlayer = match.getPlayerA();
		}

		return qualifiedPlayer;
	}
	
	private int getPlayOffGoalsForTeam(PlayOffMatch pom, String teamToGetScore) 
	{
		int score = 0;
		
		if(teamToGetScore.equalsIgnoreCase(pom.getMatchesList().get(0).getHometeam()))
			score = pom.getMatchesList().get(0).getScorehome() + pom.getMatchesList().get(1).getScoreaway();
		
		else if(teamToGetScore.equalsIgnoreCase(pom.getMatchesList().get(0).getAwayteam()))
			score = pom.getMatchesList().get(0).getScoreaway() + pom.getMatchesList().get(1).getScorehome();
		
		return score;
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
