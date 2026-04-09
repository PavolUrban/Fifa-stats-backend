package com.javasampleapproach.springrest.mysql.services;

import Utils.HelperMethods;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.MatchesPerTeam;
import com.javasampleapproach.springrest.mysql.model.PlayerStats;
import com.javasampleapproach.springrest.mysql.model.TeamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Utils.MyUtils.KOTLIK;
import static Utils.MyUtils.PAVOL_JAY;
import static Utils.MyUtils.RESULT_DRAW;
import static Utils.MyUtils.RECORD_TYPE_YELLOW_CARD;
import static Utils.MyUtils.RECORD_TYPE_RED_CARD;
import static Utils.MyUtils.RECORD_TYPE_PENALTY;
import static Utils.MyUtils.RECORD_TYPE_OWN_GOAL;

@Service
public class PlayerService {

    @Autowired
    MatchesService matchesService;

//    public Map<String, PlayerStats> getGlobalStatsV2(){
//        final List<Matches> matches = matchesService.getAllMatches();
//
//        matches.forEach(match-> {
//            final List<RecordsInMatches> recordsInMatches = match.getRecordsInMatches();
//
//        });
//
//        return null;
//    }

    @Transactional
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
            String winnerName = HelperMethods.getWinnerPlayer(m);
            winnersCount.put(winnerName, winnersCount.get(winnerName) + 1);

            List<Integer> goalsScoredANdConceeeded = getGoalsScoredAndConceded(m);
            int goalsScored = goalsScoredANdConceeeded.get(0);
            int goalsConceded = goalsScoredANdConceeeded.get(1);
            setGoalsScoredAndConcededForPlayer(pavolJay, goalsScored, goalsConceded);
            setGoalsScoredAndConcededForPlayer(kotlik, goalsConceded, goalsScored);

            Team pavolJayTeam = m.getPlayerH().equalsIgnoreCase(PAVOL_JAY) ? m.getHomeTeam() : m.getAwayTeam();
            updateMatchesPerTeam(pavolJay, pavolJayTeam);

            Team kotlikTeam = m.getPlayerH().equalsIgnoreCase(KOTLIK) ? m.getHomeTeam() : m.getAwayTeam();
            updateMatchesPerTeam(kotlik, kotlikTeam);

            processRecordsForMatch(m, pavolJay, kotlik);
        }

        prepareStats(pavolJay, winnersCount, PAVOL_JAY, KOTLIK);
        prepareStats(kotlik, winnersCount, KOTLIK, PAVOL_JAY);

        pavolJay.setTotalNumberOfCards(pavolJay.getNumberOfYellowCards() + pavolJay.getNumberOfRedCards());
        kotlik.setTotalNumberOfCards(kotlik.getNumberOfYellowCards() + kotlik.getNumberOfRedCards());

        pavolJay.setMatchesPerTeam(sortMatchesPerTeamByCount(pavolJay.getMatchesPerTeam()));
        kotlik.setMatchesPerTeam(sortMatchesPerTeamByCount(kotlik.getMatchesPerTeam()));

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

    private void processRecordsForMatch(Matches match, PlayerStats pavolJay, PlayerStats kotlik) {
        List<RecordsInMatches> records = match.getRecordsInMatches();
        if (records == null) return;

        long homeTeamId = match.getHomeTeam().getId();

        for (RecordsInMatches record : records) {
            if (record.getPlayerTeam() == null) continue;
            long playerTeamId = record.getPlayerTeam().getId();

            PlayerStats player = playerTeamId == homeTeamId
                    ? (match.getPlayerH().equalsIgnoreCase(PAVOL_JAY) ? pavolJay : kotlik)
                    : (match.getPlayerA().equalsIgnoreCase(PAVOL_JAY) ? pavolJay : kotlik);

            String type = record.getTypeOfRecord();
            if (RECORD_TYPE_YELLOW_CARD.equals(type)) {
                player.setNumberOfYellowCards(player.getNumberOfYellowCards() + 1);
            } else if (RECORD_TYPE_RED_CARD.equals(type)) {
                player.setNumberOfRedCards(player.getNumberOfRedCards() + 1);
            } else if (RECORD_TYPE_PENALTY.equals(type)) {
                player.setPenaltyGoals(player.getPenaltyGoals() + 1);
            } else if (RECORD_TYPE_OWN_GOAL.equals(type)) {
                player.setOwnGoals(player.getOwnGoals() + 1);
            }
        }
    }

    private Map<Long, MatchesPerTeam> sortMatchesPerTeamByCount(Map<Long, MatchesPerTeam> map) {
        return map.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getCount(), e1.getValue().getCount()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void updateMatchesPerTeam(PlayerStats player, Team team) {
        if (team == null) return;
        long teamId = team.getId();
        Map<Long, MatchesPerTeam> map = player.getMatchesPerTeam();
        if (map.containsKey(teamId)) {
            map.get(teamId).incrementCount();
        } else {
            TeamDto teamDto = toTeamDto(team);
            map.put(teamId, new MatchesPerTeam(teamDto, 1));
        }
    }

    private TeamDto toTeamDto(Team team) {
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setTeamName(team.getTeamName());
        dto.setTeamId(team.getId());
        dto.setFirstSeasonCL(team.getFirstSeasonCL());
        dto.setFirstSeasonEL(team.getFirstSeasonEL());
        dto.setCountry(team.getCountry());
        return dto;
    }
}
