package com.javasampleapproach.springrest.mysql.controller;


import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.MatchDetail;
import com.javasampleapproach.springrest.mysql.services.MatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/matches")
public class MatchesController {

	@Autowired
	MatchesService matchesService;

	@GetMapping("/getMatches")
	public List<Matches> getAllMatches() {
		return matchesService.getAllMatches();
	}

	@GetMapping("/getMatchesForTeam/{teamname}")
	public Map<String, Object> getMatchesForCustomTeam(@PathVariable("teamname") String teamName) {
		return matchesService.getMatchesForCustomTeam(teamName);
	}


	@GetMapping("/getAllMatchesBySeason/{season}")
	public List<Matches> getAllMatchesBySeason(@PathVariable("season") String season) {
		return matchesService.getAllMatchesBySeason(season);
	}


	@GetMapping("/getMatchesForTeamBySeason/{teamname}/{season}")
	public List<Matches> getMatchesForCustomTeamNew(@PathVariable("teamname") String teamname, @PathVariable("season") String season) {
		return matchesService.getMatchesForCustomTeamNew(teamname, season);
	}

	@PostMapping(value = "newmatch/create")
	public Matches createMatch(@RequestBody Matches match) {
		return matchesService.createMatch(match);
	}

	@PutMapping(value = "/update/existingMatch")
	public Matches updateExistingMatch(@RequestBody Matches match) {
		return matchesService.updateExistingMatch(match);
	}

	//TODO urban toto primarne dorobit
	@GetMapping(value = "/getMatchDetails/{matchId}/{hometeam}/{awayteam}")
	public MatchDetail getMatchDetails(@PathVariable("matchId") Long matchId, @PathVariable("hometeam") String hometeam, @PathVariable("awayteam") String awayteam) {
		return matchesService.getMatchDetails(matchId, hometeam, awayteam);
	}

	@GetMapping(value = "/getMatchById/{matchId}")
	public Matches getMatchById(@PathVariable("matchId") Long matchId) {
		return matchesService.getMatchById(matchId);
	}

	@GetMapping("/getCustomGroupMatches/{competition}/{season}/{competitionPhase}")
	public List<Matches> getCustomGroupMatches(@PathVariable("competition") String competition, @PathVariable("season") String season, @PathVariable("competitionPhase") String competitionPhase) {
		return matchesService.getCustomGroupMatches(competition, season, competitionPhase);
	}


	@GetMapping("/getH2HStats/{firstTeam}/{secondTeam}")
	public Map<String, Object> getCustomGroupMatches(@PathVariable("firstTeam") String firstTeam, @PathVariable("secondTeam") String secondTeam) {
		return matchesService.getCustomGroupMatches(firstTeam, secondTeam);
	}

	@GetMapping("/getDataToCreateMatch/")
	public Map<String, List<String>> getDataToCreateMatch() {
		return matchesService.getDataToCreateMatch();
	}

	// todo if needed move somewhere else?
	@GetMapping("/getCompetitionPhasesAndSeasonsList/")
	public Map<String, List<String>> getCompetitionPhasesAndSeasonsList() {
		return matchesService.getCompetitionPhasesAndSeasonsList();
	}

	@GetMapping("/getFilteredMatches/{season}/{competition}/{competitionPhase}/{teamName}")
	public List<Matches> getFilteredMatches(@PathVariable("season") String season, @PathVariable("competition") String competition, @PathVariable("competitionPhase") String competitionPhase,
											@PathVariable("teamName") String teamName) {
		return matchesService.getFilteredMatches(season, competition, competitionPhase, teamName);
	}

	@GetMapping("/topMatches/{recordType}/{selectedPlayer}/{selectedCompetition}/{teamName}")
	public List<Matches> getTopMatches(@PathVariable("recordType") String recordType, @PathVariable("selectedPlayer") String selectedPlayer, @PathVariable("selectedCompetition") String selectedCompetition, @PathVariable("teamName") String teamName) {
		return matchesService.getTopMatches(recordType, selectedPlayer, selectedCompetition, teamName);
	}
}
