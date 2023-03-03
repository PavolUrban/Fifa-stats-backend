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
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.H2HPlayers;
import com.javasampleapproach.springrest.mysql.model.OverallStats;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import com.javasampleapproach.springrest.mysql.services.MatchesService;
import com.javasampleapproach.springrest.mysql.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayOffMatch;
import com.javasampleapproach.springrest.mysql.model.TableTeam;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/completeSeasons")
public class SeasonsController {
	// do not import repos here, same for controllers!

	@Autowired
	FifaPlayerDBRepository fifaPlayerDBRepository;

	@Autowired
	RecordsInMatchesRepository recordsInMatchesRepository;

	@Autowired
	TeamService teamService;

	@Autowired
	MatchesService matchesService;

	@GetMapping("/getAllPhases/{season}/{competition}")
	public Object getAllPhasesForSeasonAndCompetition(@PathVariable("season") String season, @PathVariable("competition") String competition) {
		Map<String, Object> finalTablesWithStats = new HashMap<>();
		Map<String, List<TableTeam>> groupsWithTeams = new HashMap<>();
		NewestGoalscorersCalculator ngc = new NewestGoalscorersCalculator(fifaPlayerDBRepository);

		List<Team> allTeams = teamService.getAllTeams();

		/*  *********** GROUP STAGE *********** */
		List<Matches> matchesGroupStage = matchesService.getAllMatchesBySeasonAndCompetitionGroupStage(season, competition);

		Set<String> groupNames = new HashSet<>();
		matchesGroupStage.stream().filter(p ->  groupNames.add(p.getCompetitionPhase())).collect(Collectors.toList());

		Map<String, H2HPlayers> groupStatsForPlayers = new HashMap<>();
		Map<String, Object> groupGoalscorers = new HashMap<>();
		
		for(String groupName : groupNames) {
			
			Set<Long> teamNamesInCurrentGroup = new HashSet<>();

			matchesGroupStage.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(groupName)).forEach(a->{
				teamNamesInCurrentGroup.add(a.getIdHomeTeam());
				teamNamesInCurrentGroup.add(a.getIdAwayTeam());
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
			for(Long teamId : teamNamesInCurrentGroup) {
				TableTeam tableTeam = new TableTeam();
				List<Matches> allMatchesByTeam = matchesGroupStage.stream().filter(s -> teamId == s.getIdHomeTeam() || teamId == s.getIdAwayTeam()).collect(Collectors.toList());
			
				Map<String, Integer> winsByPlayersByCurrentTeam = HelperMethods.setnumberOfPlayersWins(allMatchesByTeam);
				winsByPlayersInCurrentGroup.put("Pavol Jay", winsByPlayersByCurrentTeam.get("Pavol Jay") + winsByPlayersInCurrentGroup.get("Pavol Jay"));
				winsByPlayersInCurrentGroup.put("Kotlik", winsByPlayersByCurrentTeam.get("Kotlik") + winsByPlayersInCurrentGroup.get("Kotlik"));
				winsByPlayersInCurrentGroup.put("Draws", winsByPlayersByCurrentTeam.get("Draws") + winsByPlayersInCurrentGroup.get("Draws"));

				int sumScoredHome = allMatchesByTeam.stream().filter(o -> o.getIdHomeTeam() == teamId).mapToInt(o -> o.getScorehome()).sum();
				int sumScoredAway = allMatchesByTeam.stream().filter(o -> o.getIdAwayTeam() == teamId).mapToInt(o -> o.getScoreaway()).sum();
				int sumConcededHome = allMatchesByTeam.stream().filter(o -> o.getIdHomeTeam() == teamId).mapToInt(o -> o.getScoreaway()).sum();
				int sumConcededAway = allMatchesByTeam.stream().filter(o -> o.getIdAwayTeam() == teamId).mapToInt(o -> o.getScorehome()).sum();
				
				long wins = allMatchesByTeam.stream().filter(p-> p.getWinnerId() == teamId).count();
				long draws = allMatchesByTeam.stream().filter(p-> p.getWinnerId() == MyUtils.drawResultId).count();
				long losses = allMatchesByTeam.size()- wins - draws;

				tableTeam.setTeamname(teamService.getTeamNameById(allTeams, teamId));
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
						.filter(match-> (match.getIdHomeTeam() == teamId && match.getPlayerH().equalsIgnoreCase(MyUtils.PAVOL_JAY)) ||
										(match.getIdAwayTeam() == teamId && match.getPlayerA().equalsIgnoreCase(MyUtils.PAVOL_JAY))
						).count();

				double playedByPavolJayPercentage = currentTeamMatchesByPavolJay/ (allMatchesByTeam.size() * 1.0);

				if(playedByPavolJayPercentage>0.6){
					tableTeam.setOwnedByPlayer(MyUtils.PAVOL_JAY);
				} else {
					tableTeam.setOwnedByPlayer(MyUtils.KOTLIK);
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
    	List<Matches> matchesPO = matchesService.getAllMatchesBySeasonAndCompetitionPlayOffs(season, competition);
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
		overallStats.setWinnerPlayer(finalMatch != null ? HelperMethods.whoIsWinnerOfMatch(finalMatch) : "unknown");



		overallStats.setWinnerTeam(finalMatch != null ? teamService.getTeamNameById(allTeams, finalMatch.getWinnerId()) : "unknown");
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
		h2hPlayOffs.setPavolJay(playoffStats.get(MyUtils.PAVOL_JAY));
		h2hPlayOffs.setKotlik(playoffStats.get(MyUtils.KOTLIK));
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

		List<Team> allTeams = teamService.getAllTeams();

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
					match1.setWinnerPlayer(HelperMethods.whoIsWinnerOfMatch(match1));
					match2.setWinnerPlayer(HelperMethods.whoIsWinnerOfMatch(match2));

					ArrayList<Long> teamIds = new ArrayList<>(Arrays.asList(match1.getIdHomeTeam(), match1.getIdAwayTeam()));
					long qualifiedTeamId = whoIsQualified(match1, match2);
					long nonQualifiedTeamId = getNonQualified(teamIds, qualifiedTeamId);
					String qualifiedPlayer = getQualifiedPlayer(qualifiedTeamId, match1);

					String qualifiedTeamName = teamService.getTeamNameById(allTeams, qualifiedTeamId);
					String nonQualifiedTeamName = teamService.getTeamNameById(allTeams, nonQualifiedTeamId);

					PlayOffMatch playOff = new PlayOffMatch(new ArrayList<>(Arrays.asList(match1, match2)), qualifiedTeamName, qualifiedTeamId, nonQualifiedTeamName, nonQualifiedTeamId, qualifiedPlayer);
					
					playOff.setQualifiedTeamGoals(getPlayOffGoalsForTeam(playOff, qualifiedTeamId));
					playOff.setNonQualifiedTeamGoals(getPlayOffGoalsForTeam(playOff, nonQualifiedTeamId));
					
					playOffMatches.add(playOff);
					addedMatches = addedMatches + 2;
				}
			}
			
			
			matchesInAllPhases.put(phase, playOffMatches);
			
		}
		
		
		return matchesInAllPhases;
		
	}

	// since players can for the same team in both play off matches we can just check one of them to get proper result
	private String getQualifiedPlayer(long qualifiedTeamId, Matches match){
		String qualifiedPlayer;
		if(match.getIdHomeTeam() == qualifiedTeamId){
			qualifiedPlayer = match.getPlayerH();
		} else {
			qualifiedPlayer = match.getPlayerA();
		}

		return qualifiedPlayer;
	}
	
	private int getPlayOffGoalsForTeam(PlayOffMatch pom, long teamIdToGetScore)
	{
		int score = 0;
		if (teamIdToGetScore == pom.getMatchesList().get(0).getIdHomeTeam()) {
			score = pom.getMatchesList().get(0).getScorehome() + pom.getMatchesList().get(1).getScoreaway();
		} else if (teamIdToGetScore == pom.getMatchesList().get(0).getIdAwayTeam()) {
			score = pom.getMatchesList().get(0).getScoreaway() + pom.getMatchesList().get(1).getScorehome();
		}
		return score;
	}
	
	private long getNonQualified(List<Long> teamIds, long qualifiedId) {
		return teamIds.stream().filter(id -> id != qualifiedId).findFirst().orElse(-1L);
	}
	
	//TODO check if this function works when two teams have equall scores from 2 matches!!!
	private long whoIsQualified(Matches match1, Matches match2)
	{
		long qualifiedTeamId = -1;
		
		long firstTeamGoals = match1.getScorehome() + match2.getScoreaway();
		long secondTeamGoals = match2.getScorehome() + match1.getScoreaway() ;
		
		if(firstTeamGoals > secondTeamGoals)
			qualifiedTeamId = match1.getIdHomeTeam();
		
		else if(secondTeamGoals > firstTeamGoals)
			qualifiedTeamId = match2.getIdHomeTeam();
		
		else if(firstTeamGoals == secondTeamGoals)
		{
			if(match1.getScoreaway() > match2.getScoreaway())
				qualifiedTeamId = match1.getIdAwayTeam();
			
			else
				qualifiedTeamId = match2.getIdAwayTeam();
		}
		
		return qualifiedTeamId;
	}
}
