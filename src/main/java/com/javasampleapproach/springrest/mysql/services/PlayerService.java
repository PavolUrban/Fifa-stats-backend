package com.javasampleapproach.springrest.mysql.services;

import Utils.HelperMethods;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayerStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Utils.MyUtils.KOTLIK;
import static Utils.MyUtils.PAVOL_JAY;
import static Utils.MyUtils.RESULT_DRAW;

@Service
public class PlayerService {

    @Autowired
    MatchesService matchesService;

    public Map<String, PlayerStats> getGlobalStats() {

        Map<String, PlayerStats> stats = new HashMap<>();

        List<Matches> matches = matchesService.getAllMatches();

        Map<String, Integer> winnersCount = new HashMap<>();
        winnersCount.put(RESULT_DRAW, 0);
        winnersCount.put(PAVOL_JAY, 0);
        winnersCount.put(KOTLIK, 0);

        PlayerStats pavolJay = new PlayerStats();
        PlayerStats kotlik = new PlayerStats();

        for (Matches m : matches) {
            String winnerName = HelperMethods.whoIsWinnerOfMatch(m);
            winnersCount.put(winnerName, winnersCount.get(winnerName) + 1);

            List<Integer> goalsScoredANdConceeeded = getGoalsScoredAndConceded(m);
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

    // this function is calculated for PAVOL_JAY -> scored goals by him is conceded goals by KOTLIK etc.
    private List<Integer> getGoalsScoredAndConceded(Matches match) {
        int goalsScored = 0;
        int goalsConceded = 0;

        if (match.getPlayerH().equalsIgnoreCase(PAVOL_JAY)) {
            goalsScored = match.getScorehome();
            goalsConceded = match.getScoreaway();
        } else if (match.getPlayerA().equalsIgnoreCase(PAVOL_JAY)) {
            goalsScored = match.getScoreaway();
            goalsConceded = match.getScorehome();
        }

        return Arrays.asList(goalsScored, goalsConceded);
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
