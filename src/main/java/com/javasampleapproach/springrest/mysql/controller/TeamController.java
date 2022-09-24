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
	public List<String> getAllTeamNames() {
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
	public TeamStatsWithMatches singleTeamStats(@PathVariable("teamname") String teamName) {
		Team team = teamRepository.findByTeamName(teamName);
		List<Matches> matches  = matchesRepository.getAllMatchesForTeam(teamName);

		TeamStatsWithMatches teamStats = new TeamStatsWithMatches();
		teamStats.setTeamName(teamName);
		teamStats.setMatches(matches);

		for(Matches m : matches)  {
			if(m.getCompetition().equalsIgnoreCase(MyUtils.CHAMPIONS_LEAGUE)){
				setWDLGoalsAndSeasons(m, teamStats.getTeamStatsCL(), team.getId());
			} else {
				setWDLGoalsAndSeasons(m, teamStats.getTeamStatsEL(), team.getId());
			}
		}

		setBilance(teamStats);
		teamStats.getTeamStatsEL().calculateGoalDiff();
		teamStats.getTeamStatsCL().calculateGoalDiff();
		teamStats.calculateTeamStatsTotal();


		return teamStats;
	}

	private void setBilance(TeamStatsWithMatches team){
		TeamStats statsCL = team.getTeamStatsCL();
		TeamStats statsEL = team.getTeamStatsEL();
		List<Integer> bilance = team.getBilance();

		// W
		bilance.add(statsCL.getWins() + statsEL.getWins());

		// D
		bilance.add(statsCL.getDraws() + statsEL.getDraws());

		// L
		bilance.add(statsCL.getLosses() + statsEL.getLosses());
	}


	private void setWDLGoalsAndSeasons(Matches m, TeamStats teamStats, long teamId) {
		// W-D-L setter
		if (m.getWinnerId() == MyUtils.drawResultId) {
			teamStats.incrementDraws(1);
		} else if (m.getWinnerId() == teamId) {
			teamStats.incrementWins(1);
		} else {
			teamStats.incrementLosses(1);
		}

		// GS and GC setter
		if(m.getIdHomeTeam() == teamId) {
			teamStats.incrementGoalsScored(m.getScorehome());
			teamStats.incrementGoalsConceded(m.getScoreaway());
		} else if(m.getIdAwayTeam() == teamId) {
			teamStats.incrementGoalsScored(m.getScoreaway());
			teamStats.incrementGoalsConceded(m.getScorehome());
		}

		// seasons - represented as set - no duplicates
		teamStats.getSeasonsList().add(m.getSeason());

		// matches count
		teamStats.incrementMatchesCount(1);

		// finals
		if(m.getCompetitionPhase().equalsIgnoreCase(MyUtils.FINAL)){
			teamStats.incrementFinalMatchesCount(1);
			if(m.getWinnerId() == teamId){
				teamStats.incrementTitlesCount(1);
			} else {
				teamStats.incrementRunnersUpCount(1);
			}
		}
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

	// TODO check this
	@PostMapping(value = "/create")
	public void postCustomer(@RequestBody Team team) {
		System.out.println("Ukladam team "+ team.getTeamName()+ " z krajiny " +team.getCountry());
		//	Team newTeam =  teamRepository.save(new Team(team.getTeamName(), "2010", "2015", team.getCountry()));
	}

	// TODO check if this one is used
	@PutMapping("/update/{tName}")
	public ResponseEntity<Team> updateTeam(@PathVariable("tName") String tName, @RequestBody Team team) {
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

	public List<Team> getAllTeams(){
		List<Team> allTeams = new ArrayList<>();
		teamRepository.findAll().forEach(allTeams::add);
		return allTeams;
	}

	public String getTeamNameById(List<Team> allTeams, Long teamId){
		return allTeams.stream().filter(team-> team.getId() == teamId).map(team -> team.getTeamName()).findFirst().orElse(null);
	}

	public Team findByTeamName(String teamName){
		return teamRepository.findByTeamName(teamName);
	}

}
