package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.repo.SeasonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.TeamGlobalStats;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/matches")
public class MatchesController {
	
	@Autowired
	MatchesRepository matchesRepository;

	@Autowired
	FileRepository fileRepository;

	@Autowired
	SeasonsRepository seasonsRepository;
	
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
		result.put("osemfinals",null);
		result.put("quarterfinals",null);
		result.put("semifinals",null);
		result.put("finals",null);

		List<Matches> osemfinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Osemfinals")).collect(Collectors.toList());
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
	@PostMapping(value = "/newmatch/create")
	public Matches createMatch(@RequestBody Matches match) {

		if (match.getScorehome() > match.getScoreaway()) {
			match.setWinner(match.getHometeam());
		} else if (match.getScorehome() == match.getScoreaway()) {
			match.setWinner(MyUtils.RESULT_DRAW);
		} else {
			match.setWinner(match.getAwayteam());
		}

		if(match.getGoalscorers() == null) {
			match.setGoalscorers("/-/"); //todo check if this is really needed
		}

		Matches newMatch = matchesRepository.save(match);

		return newMatch;
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

		GlobalStatsController globalStats = new GlobalStatsController();
		List<Goalscorer> goalscorers = globalStats.getAllGoalscorers(finalList);

		Map<String, Object> logos = new HashMap<String, Object>();
		
		setLogoIfPresented(firstTeam, logos);
		setLogoIfPresented(secondTeam, logos);
		
		response.put("playersStats", convertMapToList(playersStats, "Pavol Jay", "Kotlik"));
		response.put("matches", finalList);
		response.put("overallStats",convertMapToList(overallStats,firstTeam, secondTeam));
		response.put("goalscorers", goalscorers);
		response.put("logos", logos);
		
		
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

	public void setLogoIfPresented(String teamName, Map<String, Object> logos) {
		FileModel logo = fileRepository.findByTeamname(teamName);
		if(logo !=null)
			logos.put(teamName, logo);
	}


}
