package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.*;
import com.javasampleapproach.springrest.mysql.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/matches")
public class MatchesController {
	
	@Autowired
	MatchesRepository matchesRepository;

	@Autowired
	SeasonsRepository seasonsRepository;

	@Autowired
	RecordsInMatchesRepository recordsInMatchesRepository;

	@Autowired
	FifaPlayerDBRepository fifaPlayerDBRepository;
	
	@GetMapping("/getMatches")
	public List<Matches> getAllCustomers() {

		List<Matches> matches = new ArrayList<>();
		matchesRepository.findAll().forEach(matches::add);
		
		return matches;
	}

	
	@GetMapping("/getMatchesForTeam/{teamname}")
	public Map<String, Object> getMatchesForCustomTeam(@PathVariable("teamname") String teamName) {

		List<Matches> matches = matchesRepository.findByHometeamOrAwayteam(teamName, teamName);

		Map<String, Object> result = new HashMap();
		result.put("Round of 16",null);
		result.put("quarterfinals",null);
		result.put("semifinals",null);
		result.put("finals",null);

		List<Matches> roundOf16 = matches.stream().filter(o -> o.getCompetitionPhase().contains("Round of 16")).collect(Collectors.toList());
		Iterable<Matches> quarterfinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Quarterfinals")).collect(Collectors.toList());
		Iterable<Matches> semifinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Semifinals")).collect(Collectors.toList());
		List<Matches> finals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Final")).sorted((x1,x2)-> x2.getSeason().compareTo(x1.getSeason())).collect(Collectors.toList());

		TeamGlobalStats teamGlobalStats = new TeamGlobalStats();
		teamGlobalStats.setFinalsPlayed(finals.size());
		
		int winsInFinal = 0;
		for(Matches finale : finals)
		{
			if(finale.getWinner().equalsIgnoreCase(teamName))
				winsInFinal++;
		}
		
		teamGlobalStats.setTotalWinsCL(winsInFinal);
		teamGlobalStats.setFinalsPlayed(finals.size());
		
		
		result.put("final",finals);
		result.put("finalStats", teamGlobalStats);
		
		return result;
	}
	
	
	@GetMapping("/getAllMatchesBySeason/{season}")
	public List<Matches> getAllMatchesBySeason(@PathVariable("season") String season) {

		List<Matches> matches = new ArrayList<>();
		
		
		matches = matchesRepository.findBySeason(season);
		
		
		
		System.out.println("Matches in season "+season + " is "+matches.size()+" and teamname was ");
		
//		SingleSeasonStats.compute(matches);
		
		return matches;
	}
	
	
	@GetMapping("/getMatchesForTeamBySeason/{teamname}/{season}")
	public List<Matches> getMatchesForCustomTeamNew(@PathVariable("teamname") String teamname, @PathVariable("season") String season) {

		List<Matches> matches = new ArrayList<>();
		
		
		matches = matchesRepository.findBySeasonAndHometeamOrSeasonAndAwayteam(season,teamname,season, teamname);
		
		System.out.println("Called and matches length is "+matches.size()+" and teamname was "+teamname);
		
		//SingleSeasonStats.compute(matches);
		
		return matches;
	}

	// TODO add some validation - e.g if provided goalscorers match up witch score
	// todo zrejme zjednotit s updateexisting
	@PostMapping(value = "newmatch/create")
	public Matches createMatch(@RequestBody Matches match) {

		setWinningTeam(match);

		System.out.println("toto chcem ulozit");
		System.out.println(match);


		// todo 23.6.2020 zatial nic neukladam
		//Matches newMatch = matchesRepository.save(match);
		//saveMatchesRecords(match);

		return null;
	}

	@PutMapping(value = "/update/existingMatch")
	public Matches updateExistingMatch(@RequestBody Matches match) {

		setWinningTeam(match);

		System.out.println("toto chcem ulozit");
		System.out.println(match);
		// saveMatchesRecords(match);
		// Matches newMatch = matchesRepository.save(match);

		return null;
	}


	//TODO urban toto primarne dorobit
	@GetMapping(value = "/getMatchDetails/{matchId}/{hometeam}/{awayteam}")
	public MatchDetail getMatchDetails(@PathVariable("matchId") int matchId, @PathVariable("hometeam") String hometeam, @PathVariable("awayteam") String awayteam){
		MatchDetail md = new MatchDetail();

		List<RecordsInMatches> rims = recordsInMatchesRepository.findByMatchIdOrderByMinuteOfRecord(matchId);
		rims.forEach(rim->{
			System.out.println(rim);
		});

		//tu mam len IDcka a potrebujem ziskat mena hracov
		List<Long> ids = rims.stream().map(rim-> rim.getPlayerId()).collect(Collectors.toList());
		List<FifaPlayerDB> players = fifaPlayerDBRepository.findByIdIn(ids);

		System.out.println("tito hraci nieco spravili v zapase");
		players.forEach(player->{
			System.out.println(player);
		});
		String goals;
		List<RecordsInMatches> homegoals = rims.stream().filter(rim-> rim.getTeamName().equalsIgnoreCase(hometeam) && (rim.getTypeOfRecord().equalsIgnoreCase("G") || rim.getTypeOfRecord().contains("OG|"))).collect(Collectors.toList());
		System.out.println("goly domaci");
		System.out.println(homegoals);

		//chce to mat list objektov kde bude player name a list minut v ktorych dal gol
		homegoals.forEach(hg->{
			String homePlayerWithGoal = getPlayerNameById(hg.getPlayerId(), players);
			System.out.println(homePlayerWithGoal);
		});

		return md;
	}

	private String getPlayerNameById(long id, List<FifaPlayerDB> players){
		String name = "UNKNOWN";
		FifaPlayerDB playerDB = players.stream().filter(p-> p.getId() == id).findFirst().orElse(null);

		if(playerDB!=null){
			name = playerDB.getPlayerName();
		}

		return name;
	}


	private void setWinningTeam(Matches match){
		if (match.getScorehome() > match.getScoreaway()) {
			match.setWinner(match.getHometeam());
		} else if (match.getScorehome() == match.getScoreaway()) {
			match.setWinner(MyUtils.RESULT_DRAW);
		} else {
			match.setWinner(match.getAwayteam());
		}
	}
	
	
	@GetMapping("/getCustomGroupMatches/{competition}/{season}/{competitionPhase}")
	public List<Matches> getCustomGroupMatches(@PathVariable("competition") String competition, @PathVariable("season") String season,  @PathVariable("competitionPhase") String competitionPhase) {
		return matchesRepository.findByCompetitionAndSeasonAndCompetitionPhase(competition, season, competitionPhase);
	}

	private List<Integer> convertMapToList(Map<String, Integer> playersStats, String homePlayerOrTeam, String awayPlayerOrTeam)
	{
		List<Integer> intValues = new ArrayList<Integer>();
		
		intValues.add(playersStats.get(homePlayerOrTeam));
		intValues.add(playersStats.get("D")); //D stands always for draw
		intValues.add(playersStats.get(awayPlayerOrTeam));
		
		return intValues;
	}
	
	
	
	
	@GetMapping("/getH2HStats/{firstTeam}/{secondTeam}")
	public Map<String, Object> getCustomGroupMatches(@PathVariable("firstTeam") String firstTeam, @PathVariable("secondTeam") String secondTeam) {
		
		List<Matches> finalList = new ArrayList<>();
		matchesRepository.findByHometeamAndAwayteam(firstTeam, secondTeam).forEach(finalList::add);
		matchesRepository.findByHometeamAndAwayteam(secondTeam, firstTeam).forEach(finalList::add);
		
		finalList.sort(Comparator.comparing(Matches::getSeason).thenComparing(Matches::getCompetitionPhase));
		
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		Map<String, Integer> playersStats = new HashMap<String, Integer>();
		playersStats.put("Pavol Jay", 0);
		playersStats.put("Kotlik", 0);
		playersStats.put("D", 0); //TODO nejak zjednotit raz je remiza D inokedy "Draws"
		
		PlayersController playersController = new PlayersController();
		
		Map<String, Integer> overallStats = new HashMap<String, Integer>();
		overallStats.put(firstTeam, 0);
		overallStats.put(secondTeam, 0);
		overallStats.put("D", 0);

		for(Matches match : finalList) {
			//players statistics
			String winner = playersController.whoIsWinnerOfMatch(match, "Pavol Jay", "Kotlik");
			playersStats.put(winner, playersStats.get(winner) + 1);
			
			//teams statistics
			overallStats.put(match.getWinner(), overallStats.get(match.getWinner()) + 1);
		}
		
		response.put("playersStats", convertMapToList(playersStats, "Pavol Jay", "Kotlik"));
		response.put("matches", finalList);
		response.put("overallStats",convertMapToList(overallStats,firstTeam, secondTeam));

		
		return response;
	}

	@GetMapping("/getDataToCreateMatch/")
	public Map<String, List<String>> getDataToCreateMatch(){

		Map<String, List<String>> dataToCreateMatch = new HashMap<>();

		dataToCreateMatch.put("competitionsList", MyUtils.competitionsList);
		dataToCreateMatch.put("playerNamesList", MyUtils.playerNamesList);
		dataToCreateMatch.put("competitionsPhasesCL", MyUtils.championsLeagueStagesList);
		dataToCreateMatch.put("competitionsPhasesEL", MyUtils.europeanLeagueStagesList);
		dataToCreateMatch.put("seasonsList", seasonsRepository.getAvailableSeasonsList());

		return  dataToCreateMatch;
	}

	@GetMapping("/getCompetitionPhasesAndSeasonsList/")
	public Map<String, List<String>> getCompetitionPhasesAndSeasonsList(){

		Map<String, List<String>> competitionPhases = new HashMap<>();

		competitionPhases.put("competitionsPhasesCL", MyUtils.championsLeagueStagesListWithDefault);
		competitionPhases.put("competitionsPhasesEL", MyUtils.europeanLeagueStagesListWithDefault);
		competitionPhases.put("defaultCompetitionPhases", MyUtils.europeanLeagueStagesListWithDefault);

		List<String> allSeasons = seasonsRepository.getAvailableSeasonsList();
		allSeasons.add(0,MyUtils.ALL_SEASONS);
		competitionPhases.put("seasonsList", allSeasons);

		return competitionPhases;
	}

	@GetMapping("/getFilteredMatches/{season}/{competition}/{competitionPhase}/{teamName}")
	public List<Matches> getFilteredMatches(@PathVariable("season") String season, @PathVariable("competition") String competition, @PathVariable("competitionPhase") String competitionPhase,
										 @PathVariable("teamName") String teamName) {

		if (season.equalsIgnoreCase(MyUtils.ALL_SEASONS)) {
			season = null;
		}

		if (competition.equalsIgnoreCase("All competitions")) {
			competition = null;
		}

		if (competitionPhase.equalsIgnoreCase(MyUtils.ALL_PHASES)) {
			competitionPhase = null;
		}

		List<Matches> matches = matchesRepository.getMatchesWithCustomFilters(season, competition, competitionPhase);

		if (teamName == null || teamName.equalsIgnoreCase("null")) {
			return matches;
		} else {
			List<Matches> matchesForSelectedTeam = matches.stream().filter(match -> match.getHometeam().equalsIgnoreCase(teamName) || match.getAwayteam().equalsIgnoreCase(teamName)).collect(Collectors.toList());
			return matchesForSelectedTeam;
		}
	}
}
