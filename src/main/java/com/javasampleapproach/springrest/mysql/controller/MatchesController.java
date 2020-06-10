package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.TeamGlobalStats;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;

import statsCreator.SingleSeasonStats;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/matches")
public class MatchesController {
	
	@Autowired
	MatchesRepository matchesRepository;
	
	@GetMapping("/getMatches")
	public List<Matches> getAllCustomers() {

		List<Matches> matches = new ArrayList<>();
		matchesRepository.findAll().forEach(matches::add);
		
		return matches;
	}

	
	@GetMapping("/getMatchesForTeam/{teamname}")
	public Map<String, Object> getMatchesForCustomTeam(@PathVariable("teamname") String teamName) {

		List<Matches> matches = new ArrayList<>();
//		matches = matchesRepository.getAllMatchesForTeam(teamName);
	
		matches = matchesRepository.findByHometeamOrAwayteam(teamName, teamName);

		//SingleSeasonStats.compute(matches, teamName);
		
		
	Map<String, Object> result = new HashMap();
		
		
		result.put("osemfinals",null);
		result.put("quarterfinals",null);
		result.put("semifinals",null);
		result.put("finals",null);
		
		//Map<String, Integer> ll = new HashMap< >();
	
		//Iterable<Matches> groupStage = matches.stream().filter(o -> o.getCompetitionPhase().contains("GROUP")).collect(Collectors.toList());
		
		List<Matches> osemfinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Osemfinals")).collect(Collectors.toList());
		Iterable<Matches> quarterfinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Quarterfinals")).collect(Collectors.toList());
		Iterable<Matches> semifinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Semifinals")).collect(Collectors.toList());
		List<Matches> finals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Final")).sorted((x1,x2)-> x2.getSeason().compareTo(x1.getSeason())).collect(Collectors.toList());
		
	
		for(Matches osemfinal: osemfinals)
		{
			
			System.out.println(osemfinal.getSeason() + "... "+osemfinal.getHometeam() + " vs "+osemfinal.getAwayteam());
		}
		
		
		//System.out.println("\nGroup");
		//System.out.println(groupStage);
		System.out.println("\nOsemfinals");
		System.out.println(osemfinals);
		System.out.println("\nQuartefinals");
		System.out.println(quarterfinals);
		System.out.println("\nSemifinals");
		System.out.println(semifinals);
		System.out.println("\nFinals");
	
		
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

		
		
		
		//return matches;
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
	
	@PostMapping(value = "/newmatch/create")
	public Matches createMatch(@RequestBody Matches match) {
		

		Matches newMatch = matchesRepository.save(new Matches(match.getHometeam(), match.getAwayteam(), match.getScorehome(), match.getScoreaway(), match.getCompetition(), match.getCompetitionPhase()));
		return newMatch;
		
	}
	
	
	
	
	

}