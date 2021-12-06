package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayerStats;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.PlayersRepository;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/playerStats")
public class PlayersController {

	@Autowired
	PlayersRepository playersRepository;

	@Autowired
	MatchesRepository matchesRepository;

	private static final String PAVOL_JAY = "Pavol Jay";
	private static final String KOTLIK = "Kotlik";

	public String whoIsWinnerOfMatch(Matches match, String playerFirst, String playerSecond) {
		String winner = "";

		if (match.getWinner().equalsIgnoreCase("D"))
			winner = "D";

		else if ((match.getHometeam().equalsIgnoreCase(match.getWinner()) && (match.getPlayerH().equalsIgnoreCase(playerFirst))) ||
				(match.getAwayteam().equalsIgnoreCase(match.getWinner())) && (match.getPlayerA().equalsIgnoreCase(playerFirst)))
			winner = playerFirst;

		else
			winner = playerSecond;

		return winner;
	}


	//this function is calculated for pavol jay -> scored goals by him is conceded goals by kotlik etc.
	private String getGoalsScored(Matches match) {
		int goalsScored = 0;
		int goalsConceded = 0;

		if (match.getPlayerH().equalsIgnoreCase("Pavol Jay")) {
			goalsScored = match.getScorehome();
			goalsConceded = match.getScoreaway();
		} else if (match.getPlayerA().equalsIgnoreCase("Pavol Jay")) {
			goalsScored = match.getScoreaway();
			goalsConceded = match.getScorehome();
		}

		return goalsScored + "," + goalsConceded;
	}


	@GetMapping("/getGlobalStats")
	public Map<String, PlayerStats> getGlobalStats() {

		Map<String, PlayerStats> stats = new HashMap<>();


		//	List<Player> players = (List<Player>) playersRepository.findAll();
		List<Matches> matches = (List<Matches>) matchesRepository.findAll();

		Map<String, Integer> winnersCount = new HashMap<>();

		winnersCount.put("D", 0);
		winnersCount.put("Pavol Jay", 0);
		winnersCount.put("Kotlik", 0);


		PlayerStats pavolJay = new PlayerStats();
		PlayerStats kotlik = new PlayerStats();

		for (Matches m : matches) {
			String winnerName = whoIsWinnerOfMatch(m, "Pavol Jay", "Kotlik");
			winnersCount.put(winnerName, winnersCount.get(winnerName) + 1);

			String[] goalsScoredAndConceded = getGoalsScored(m).split(",");
			int goalsScored = Integer.parseInt(goalsScoredAndConceded[0]);
			int goalsConceded = Integer.parseInt(goalsScoredAndConceded[1]);

			pavolJay.setGoalsScored(pavolJay.getGoalsScored() + goalsScored);
			pavolJay.setGoalsConceded(pavolJay.getGoalsConceded() + goalsConceded);

			//goals scored by pavol jay is conceded by kotlik and so on
			kotlik.setGoalsScored(kotlik.getGoalsScored() + goalsConceded);
			kotlik.setGoalsConceded(kotlik.getGoalsConceded() + goalsScored);
		}


		ArrayList<Integer> bilancePavolJay = new ArrayList<>(Arrays.asList(winnersCount.get("Pavol Jay"), winnersCount.get("D"), winnersCount.get("Kotlik")));
		ArrayList<Integer> bilanceKotlik = new ArrayList<>(Arrays.asList(winnersCount.get("Kotlik"), winnersCount.get("D"), winnersCount.get("Pavol Jay")));

		pavolJay.setWins(winnersCount.get("Pavol Jay"));
		pavolJay.setLosses(winnersCount.get("Kotlik")); //win of one player is loss of second one
		pavolJay.setDraws(winnersCount.get("D"));
		pavolJay.setTotalBilance(bilancePavolJay);

		kotlik.setWins(winnersCount.get("Kotlik"));
		kotlik.setLosses(winnersCount.get("Pavol Jay"));
		kotlik.setDraws(winnersCount.get("D"));
		kotlik.setTotalBilance(bilanceKotlik);

		stats.put("Pavol Jay", pavolJay);
		stats.put("Kotlik", kotlik);
		return stats;
	}
}
