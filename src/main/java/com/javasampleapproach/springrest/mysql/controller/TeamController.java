package com.javasampleapproach.springrest.mysql.controller;


import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.TeamStatsWithMatches;
import com.javasampleapproach.springrest.mysql.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/teams")
public class TeamController {

	@Autowired
	TeamService teamService;

	@GetMapping("/getAllTeamNames")
	public List<String> getAllTeamNames() {
		return teamService.getAllTeamNames();
	}
	
	// todo rename - no logo in the url
	@GetMapping("/getAllTeamsWithLogo/{recalculate}")
	public List<Team> getAllTeams(@PathVariable("recalculate") boolean recalculate) {
		return teamService.getAllTeams(recalculate);
	}

	// TODO replace teamname by Id
	@GetMapping("/getTeamStats/{teamname}")
	public TeamStatsWithMatches getTeamStats(@PathVariable("teamname") String teamName) {
		return teamService.getTeamStats(teamName);
	}

	@GetMapping("/getGlobalTeamStats")
	public List<Team> allGlobalTeamStats() {
		return teamService.allGlobalTeamStats();
	}

	// TODO check this + use service!
	@PostMapping(value = "/create")
	public void postCustomer(@RequestBody Team team) {
		System.out.println("Ukladam team "+ team.getTeamName()+ " z krajiny " +team.getCountry());
		//	Team newTeam =  teamRepository.save(new Team(team.getTeamName(), "2010", "2015", team.getCountry()));
	}

	// TODO check if this one is used and fix it - use service !
	@PutMapping("/update/{tName}")
	public ResponseEntity<Team> updateTeam(@PathVariable("tName") String tName, @RequestBody Team team) {
//		Optional<Team> teamToUpdate = Optional.of(teamRepository.findByTeamName(tName));
//
//		if(teamToUpdate.isPresent()) {
//			Team newTeam = teamToUpdate.get();
//			newTeam.setTeamName(team.getTeamName());
//			newTeam.setFirstSeasonCL(team.getFirstSeasonCL());
//			newTeam.setFirstSeasonEL(team.getFirstSeasonEL());
//			newTeam.setCountry(team.getCountry());
//			return new ResponseEntity<>(teamRepository.save(newTeam), HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
		System.out.println("nothing is updated atm");
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
