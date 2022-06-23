package com.javasampleapproach.springrest.mysql.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.*;
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

import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.TeamRepository;

import Utils.MyUtils;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/teams")
public class TeamController {
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	MatchesRepository matchesRepository;

	@GetMapping("/getAllTeamNames")
	public List<String> getTeamnames() {
		List<String> teamNames = new ArrayList<>();
		teamRepository.findAll().forEach(p -> teamNames.add(p.getTeamName()));
		return teamNames;
	}
	
	// todo rename - no logo in the url
	@GetMapping("/getAllTeamsWithLogo/{recalculate}")
	public List<Team> getAllTeams(@PathVariable("recalculate") boolean recalculate) {
		List<Team> teams = new ArrayList<>();

		teamRepository.findAll().forEach(teams::add);

		System.out.println("recalculate je nastavene na " +recalculate);

		for(Team t : teams) {
			if(recalculate) {
				t.setFirstSeasonCL(matchesRepository.firstSeasonInCompetition(t.getTeamName(), MyUtils.CHAMPIONS_LEAGUE));
				t.setFirstSeasonEL(matchesRepository.firstSeasonInCompetition(t.getTeamName(), MyUtils.EUROPEAN_LEAGUE));
				teamRepository.save(t);
			}
			t.setFirstSeasonCL(setLabelToNeverIfNull(t.getFirstSeasonCL()));
			t.setFirstSeasonEL(setLabelToNeverIfNull(t.getFirstSeasonEL()));
		}
		
		return teams;
	}

	private String setLabelToNeverIfNull(String firstSeasonInCompetition){
		if(firstSeasonInCompetition == null) {
			return "never";
		} else {
			return firstSeasonInCompetition;
		}
	}

	// TODO check for multiple times used basically the same condition, this may be improved
	@GetMapping("/getTeamStats/{teamname}")
	public TeamStats singleTeamStats(@PathVariable("teamname") String teamName) {
		Set<String> seasonsCL = new LinkedHashSet<>();
		Set<String> seasonsEL = new LinkedHashSet<>();

		List<Matches> matches  = matchesRepository.getAllMatchesForTeam(teamName);
		Team t = teamRepository.findByTeamName(teamName);

		TeamStats team = new TeamStats();
		team.setMatches(matches);

		List<Matches> finalMatches = matches.stream().filter(m->m.getCompetitionPhase().equalsIgnoreCase("Final")).collect(Collectors.toList());
		List<Matches> lostFinalMatches = finalMatches.stream().filter(m-> ! (m.getWinner().equalsIgnoreCase(teamName))).collect(Collectors.toList());
		List<Matches> wonFinalMatches = finalMatches.stream().filter(m-> m.getWinner().equalsIgnoreCase(teamName)).collect(Collectors.toList());

		team.getFinalMatches().get("Total").put("Won", wonFinalMatches);
		team.getFinalMatches().get("Total").put("Lost", lostFinalMatches);
		getMatchesByCompetition(lostFinalMatches, "CL", "Lost", team);
		getMatchesByCompetition(wonFinalMatches, "CL", "Won", team);
		getMatchesByCompetition(lostFinalMatches, "EL", "Lost", team);
		getMatchesByCompetition(wonFinalMatches, "EL", "Won", team);

		for(Matches m : matches)  {

			// wins, draws, losses counter
			if (m.getWinner().equalsIgnoreCase("D")) {
				team.getMatchesStats().get(m.getCompetition()).put("Draws", team.getMatchesStats().get(m.getCompetition()).get("Draws") + 1);
			} else if (m.getWinner().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("Wins", team.getMatchesStats().get(m.getCompetition()).get("Wins") + 1);
			} else {
				team.getMatchesStats().get(m.getCompetition()).put("Losses", team.getMatchesStats().get(m.getCompetition()).get("Losses") + 1);
			}

			// goalScored and goalsConceded counter
			if(m.getHometeam().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScorehome());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScoreaway());
			} else if(m.getAwayteam().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScoreaway());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScorehome());
			}

			// seasons in CL/EL
			if (m.getCompetition().equalsIgnoreCase("CL")) {
				seasonsCL.add(m.getSeason());
			} else if(m.getCompetition().equalsIgnoreCase("EL")) {
				seasonsEL.add(m.getSeason());
			}
		}

		team.getMatchesStats().get("CL").put("Seasons", seasonsCL.size());
		team.getMatchesStats().get("EL").put("Seasons", seasonsEL.size());



		// TODO this may be partially reused transformGoalMapToTimeRanges
		// TimeRangesMapper.transformGoalMapToTimeRanges(team);


		return team;
	}

	// TODO - who has won the most games, most red cards ...
	@GetMapping("/getGlobalTeamStats")
	public List<Team> allGlobalTeamStats() {

		List<Team> teams = new ArrayList<>();
		teamRepository.findAll().forEach(teams::add);


		int numberOfTeamsPresentedInCL = 0;
		int numberOfTeamsPresentedInEL = 0;
		for(Team t : teams)
		{
			if (t.getFirstSeasonCL() == null)
				t.setFirstSeasonCL("never");
			else
				numberOfTeamsPresentedInCL++;

			if (t.getFirstSeasonEL() == null)
				t.setFirstSeasonEL("never");

			else
				numberOfTeamsPresentedInEL++;

		}

		return teams;
	}

	// TODO check if this one is used
	@PostMapping(value = "/create")
	public void postCustomer(@RequestBody Team team) {


		System.out.println("Ukladam team "+ team.getTeamName()+ " z krajiny " +team.getCountry());


			Team newTeam =  teamRepository.save(new Team(team.getTeamName(), "2010", "2015", team.getCountry()));

		// Team newTeam = teamRepository.save(new Team(team.getTeamName(), team.getFirstSeasonCL(), team.getFirstSeasonEL(), team.getCountry()));
		// return newTeam;
	}

	// TODO check if this one is used
	@PutMapping("/update/{tName}")
	public ResponseEntity<Team> updateCustomer(@PathVariable("tName") String tName, @RequestBody Team team) {
		Optional<Team> teamToUpdate = Optional.of(teamRepository.findByTeamName(tName));

		if(teamToUpdate.isPresent()) {
			Team newTeam = teamToUpdate.get();
			newTeam.setTeamName(team.getTeamName());
			newTeam.setFirstSeasonCL(team.getFirstSeasonCL());
			newTeam.setFirstSeasonEL(team.getFirstSeasonEL());
			newTeam.setCountry(team.getCountry());
			return new ResponseEntity<>(teamRepository.save(newTeam), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private void getMatchesByCompetition(List<Matches> matchesToFilter, String competition, String result, TeamStats team){
		List<Matches> filteredMatches = matchesToFilter.stream().filter(m->m.getCompetition().equalsIgnoreCase(competition)).collect(Collectors.toList());
		team.getFinalMatches().get(competition).put(result, filteredMatches);
	}

}
