package com.javasampleapproach.springrest.mysql.controller;

import java.sql.Time;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import Utils.CardsStatsCalculator;
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

import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.TeamRepository;

import Utils.MyUtils;
import Utils.CardsStatsCalculator;
import Utils.GoalscorersCalculator;
import Utils.TimeRangesMapper;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/teams")
public class TeamController {
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	MatchesRepository matchesRepository;
	
	@Autowired
	FileRepository fileRepository;

	@GetMapping("/getAllTeamNames")
	public List<String> getTeamnames() {
		List<String> teamNames = new ArrayList<>();
		teamRepository.findAll().forEach(p -> teamNames.add(p.getTeamName()));
		return teamNames;
	}
	
	
	@GetMapping("/getAllTeamsWithLogo")
	public List<TeamWithLogo> getAllCustomers() {
		List<Team> teams = new ArrayList<>();
		List<FileModel> logos = new ArrayList<>();
		List<TeamWithLogo> finalTeams = new ArrayList<>();

		teamRepository.findAll().forEach(teams::add);
		fileRepository.findAll().forEach(logos::add);;

		for(Team t : teams) {
			if (t.getFirstSeasonCL() == null) {
				t.setFirstSeasonCL("never");
			}

			if (t.getFirstSeasonEL() == null) {
				t.setFirstSeasonEL("never");
			}

			TeamWithLogo twl = new TeamWithLogo(t);

			FileModel logo = logos.stream().filter(l->l.getTeamname().equalsIgnoreCase(t.getTeamName())).findFirst().orElse(null);
			twl.setFm(logo);

			finalTeams.add(twl);
		}
		
		return finalTeams;
	}


	// TODO check for multiple times used basically the same condition, this may be improved

	@GetMapping("/getTeamStats/{teamname}")
	public TeamStats singleTeamStats(@PathVariable("teamname") String teamName) {
		Set<String> seasonsCL = new LinkedHashSet<>();
		Set<String> seasonsEL = new LinkedHashSet<>();

		List<Matches> matches  = matchesRepository.getAllMatchesForTeam(teamName);
		FileModel logo = fileRepository.findByTeamname(teamName);

		TeamStats team = new TeamStats();
		team.setMatches(matches);
		team.setFm(logo);

		List<Matches> finalMatches = matches.stream().filter(m->m.getCompetitionPhase().equalsIgnoreCase("Final")).collect(Collectors.toList());
		List<Matches> lostFinalMatches = finalMatches.stream().filter(m-> ! (m.getWinner().equalsIgnoreCase(teamName))).collect(Collectors.toList());
		List<Matches> wonFinalMatches = finalMatches.stream().filter(m-> m.getWinner().equalsIgnoreCase(teamName)).collect(Collectors.toList());

		team.getFinalMatches().get("Total").put("Won", wonFinalMatches);
		team.getFinalMatches().get("Total").put("Lost", lostFinalMatches);
		getMatchesByCompetition(lostFinalMatches, "CL", "Lost", team);
		getMatchesByCompetition(wonFinalMatches, "CL", "Won", team);
		getMatchesByCompetition(lostFinalMatches, "EL", "Lost", team);
		getMatchesByCompetition(wonFinalMatches, "EL", "Won", team);

		Set<String> oponents = new HashSet<>();

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
				oponents.add(m.getAwayteam());
			} else if(m.getAwayteam().equalsIgnoreCase(teamName)) {
				team.getMatchesStats().get(m.getCompetition()).put("GoalsScored", team.getMatchesStats().get(m.getCompetition()).get("GoalsScored") + m.getScoreaway());
				team.getMatchesStats().get(m.getCompetition()).put("GoalsConceded", team.getMatchesStats().get(m.getCompetition()).get("GoalsConceded") + m.getScorehome());
				oponents.add(m.getHometeam());
			}

			// goalscorers
			if(m.getGoalscorers() != null) {
				String[] goalscorers = m.getGoalscorers().split("-");
				if(m.getHometeam().equalsIgnoreCase(teamName)) {
					// 0 - home goalscorers (they are written before character '-')
					GoalscorersCalculator.addHomeOrAwayGoalscorersProperly(true, team.getGoalsByMinutesCount(), m, goalscorers[0], team);
					GoalscorersCalculator.addHomeOrAwayGoalscorersProperly(false, team.getConcededGoalsByMinutesCount(), m, goalscorers[1], team);
				} else {
					GoalscorersCalculator.addHomeOrAwayGoalscorersProperly(true, team.getGoalsByMinutesCount(), m, goalscorers[1], team);
					GoalscorersCalculator.addHomeOrAwayGoalscorersProperly(false, team.getConcededGoalsByMinutesCount(), m, goalscorers[0], team);
				}
			}

			// seasons in CL/EL
			if (m.getCompetition().equalsIgnoreCase("CL")) {
				seasonsCL.add(m.getSeason());
			} else if(m.getCompetition().equalsIgnoreCase("EL")) {
				seasonsEL.add(m.getSeason());
			}

			// yellow cards
			if(m.getYellowcards() != null) {
				CardsStatsCalculator.getRedAndYellowCards(m, team, m.getYellowcards(), m.getCompetition(), teamName, MyUtils.CARD_TYPE_YELLOW);
			}

			//red cards
			if(m.getRedcards() != null) {
				CardsStatsCalculator.getRedAndYellowCards(m, team, m.getRedcards(), m.getCompetition(), teamName, MyUtils.CARD_TYPE_RED);
			}
		}

		team.getMatchesStats().get("CL").put("Seasons", seasonsCL.size());
		team.getMatchesStats().get("EL").put("Seasons", seasonsEL.size());

		Map<String, Integer> sortedMapCL = GoalscorersCalculator.sortMap(team.getGoalScorers().get("CL"));
		//printGoalscorersMap(sortedMapCL);
		team.getGoalScorers().put("CL", sortedMapCL);

		Map<String, Integer> sortedMapEL = GoalscorersCalculator.sortMap(team.getGoalScorers().get("EL"));
		//printGoalscorersMap(sortedMapEL);
		team.getGoalScorers().put("EL", sortedMapEL);

		List<FileModel> oponentsLogos = fileRepository.getLogosForAllTeams(oponents);
		oponentsLogos.add(team.getFm());
		team.setOponentsLogos(oponentsLogos);

		TimeRangesMapper.transformGoalMapToTimeRanges(team);

		Collections.sort(team.getPlayersCardsStatisticsPerCompetition().get("Total"), Comparator.comparingInt(FifaPlayer::getCardsTotal).thenComparing(FifaPlayer::getRedCards).reversed());
		Collections.sort(team.getPlayersCardsStatisticsPerCompetition().get("CL"), Comparator.comparingInt(FifaPlayer::getCardsTotal).thenComparing(FifaPlayer::getRedCards).reversed());
		Collections.sort(team.getPlayersCardsStatisticsPerCompetition().get("EL"), Comparator.comparingInt(FifaPlayer::getCardsTotal).thenComparing(FifaPlayer::getRedCards).reversed());

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
