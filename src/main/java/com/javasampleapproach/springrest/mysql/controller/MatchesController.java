package com.javasampleapproach.springrest.mysql.controller;


import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import com.javasampleapproach.springrest.mysql.model.matches.TopMatchesRequest;
import com.javasampleapproach.springrest.mysql.services.MatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

	@GetMapping("/getH2HStats/{firstTeam}/{secondTeam}")
	public Map<String, Object> getCustomGroupMatches(@PathVariable("firstTeam") String firstTeam, @PathVariable("secondTeam") String secondTeam) {
		return matchesService.getCustomGroupMatches(firstTeam, secondTeam);
	}

	// todo if needed move somewhere else?
	@GetMapping("/getCompetitionPhasesAndSeasonsList/")
	public Map<String, List<String>> getCompetitionPhasesAndSeasonsList() {
		return matchesService.getCompetitionPhasesAndSeasonsList();
	}


}
