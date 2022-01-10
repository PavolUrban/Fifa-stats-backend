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

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayerStats;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.PlayersRepository;

import static Utils.MyUtils.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/playerStats")
public class PlayersController {

	@Autowired
	PlayersRepository playersRepository;

	@Autowired
	MatchesRepository matchesRepository;

	@GetMapping("/getGlobalStats")
	public Map<String, PlayerStats> getGlobalStats() {

		Map<String, PlayerStats> stats = new HashMap<>();

		List<Matches> matches = (List<Matches>) matchesRepository.findAll();

		Map<String, Integer> winnersCount = new HashMap<>();
		winnersCount.put(RESULT_DRAW, 0);
		winnersCount.put(PAVOL_JAY, 0);
		winnersCount.put(KOTLIK, 0);

		PlayerStats pavolJay = new PlayerStats();
		PlayerStats kotlik = new PlayerStats();

		for (Matches m : matches) {
			String winnerName = whoIsWinnerOfMatch(m, PAVOL_JAY, KOTLIK);
			winnersCount.put(winnerName, winnersCount.get(winnerName) + 1);

			List<Integer> goalsScoredANdConceeeded = getGoalsScoredAndConceded(m, pavolJay, kotlik);
			int goalsScored = goalsScoredANdConceeeded.get(0);
			int goalsConceded = goalsScoredANdConceeeded.get(1);
			setGoalsScoredAndConcededForPlayer(pavolJay, goalsScored, goalsConceded);
			setGoalsScoredAndConcededForPlayer(kotlik, goalsConceded, goalsScored);
		}

		prepareStats(pavolJay, winnersCount, PAVOL_JAY, KOTLIK);
		prepareStats(kotlik, winnersCount, KOTLIK, PAVOL_JAY);

		stats.put(PAVOL_JAY, pavolJay);
		stats.put(KOTLIK, kotlik);

		return stats;
	}

	public String whoIsWinnerOfMatch(Matches match, String playerFirst, String playerSecond) {
		String winner;

		if (match.getWinner().equalsIgnoreCase(RESULT_DRAW))
			winner = RESULT_DRAW;

		else if ((match.getHometeam().equalsIgnoreCase(match.getWinner()) && (match.getPlayerH().equalsIgnoreCase(playerFirst))) ||
				(match.getAwayteam().equalsIgnoreCase(match.getWinner())) && (match.getPlayerA().equalsIgnoreCase(playerFirst)))
			winner = playerFirst;

		else
			winner = playerSecond;

		return winner;
	}

	// this function is calculated for PAVOL_JAY -> scored goals by him is conceded goals by KOTLIK etc.
	private List<Integer> getGoalsScoredAndConceded(Matches match, PlayerStats mainPLayer, PlayerStats opositionPlayer) {
		int goalsScored = 0;
		int goalsConceded = 0;

		if (match.getPlayerH().equalsIgnoreCase(PAVOL_JAY)) {
			goalsScored = match.getScorehome();
			goalsConceded = match.getScoreaway();
			getCardsIfNotNull(match, 0,1, mainPLayer, opositionPlayer);
		} else if (match.getPlayerA().equalsIgnoreCase(PAVOL_JAY)) {
			goalsScored = match.getScoreaway();
			goalsConceded = match.getScorehome();
			getCardsIfNotNull(match, 1,0, mainPLayer, opositionPlayer);
		}

		return Arrays.asList(goalsScored, goalsConceded);
	}

	// todo this function contains duplicate code - remove it
	private void getCardsIfNotNull(Matches match, int mainPlayerIndex, int opositionPlayerIndex, PlayerStats mainPlayer, PlayerStats opositionPlayer) {
		if(match.getYellowcards() != null) {
			String[] yellowCards = match.getYellowcards().split("-");
			String cardsForCurrentPlayer = yellowCards[mainPlayerIndex];
			String cardsForOpositionPlayer = yellowCards[opositionPlayerIndex];
			mainPlayer.setNumberOfYellowCards( mainPlayer.getNumberOfYellowCards() + addProperCardCount(cardsForCurrentPlayer));
			opositionPlayer.setNumberOfYellowCards(opositionPlayer.getNumberOfYellowCards() + addProperCardCount(cardsForOpositionPlayer));
		}

		if(match.getRedcards() != null) {
			String[] redCards = match.getRedcards().split("-");
			String cardsForCurrentPlayer = redCards[mainPlayerIndex];
			String cardsForOpositionPlayer = redCards[opositionPlayerIndex];
			mainPlayer.setNumberOfRedCards( mainPlayer.getNumberOfRedCards() + addProperCardCount(cardsForCurrentPlayer));
			opositionPlayer.setNumberOfRedCards( opositionPlayer.getNumberOfRedCards() + addProperCardCount(cardsForOpositionPlayer));
		}
	}


	private static int addProperCardCount (String allCurrentCards)
	{
		int numberOfCardsToAdd = 0;

		// check for multiple players separated by delimiter
		if (allCurrentCards.contains(";")) {
			String[] playersWithCurrentCard = allCurrentCards.split(";");
			numberOfCardsToAdd = playersWithCurrentCard.length;
		} else {
			// '/' is used for no goalscorer - if string contains this character no record for this stat is provided
			if ( !allCurrentCards.equalsIgnoreCase("/") ) {
				numberOfCardsToAdd =1;
			}
		}

		return numberOfCardsToAdd;
	}

	private void prepareStats(PlayerStats player, Map<String, Integer> winnersCount, String playerWinner, String playerLooser) {
		ArrayList<Integer> totalStats = new ArrayList<>(Arrays.asList(winnersCount.get(playerWinner), winnersCount.get(RESULT_DRAW), winnersCount.get(playerLooser)));
		player.setWins(totalStats.get(0));
		player.setDraws(totalStats.get(1));
		player.setLosses(totalStats.get(2));
		player.setTotalBilance(totalStats);
	}

	private void setGoalsScoredAndConcededForPlayer(PlayerStats player, int goalsScored, int goalsConceded) {
		player.setGoalsScored(player.getGoalsScored() + goalsScored);
		player.setGoalsConceded(player.getGoalsConceded() + goalsConceded);
	}
}
