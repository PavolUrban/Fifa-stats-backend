package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.TeamSeasonStat;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.TeamSeasonStatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamSeasonStatService { // Môžeš si to premenovať, ak chceš

    @Autowired
    private TeamSeasonStatRepository statRepository;
    @Autowired
    private MatchesRepository matchRepository;


    public Page<TeamSeasonStat> findByCompetition(String competition, Pageable pageable) {
        return statRepository.findByCompetition(competition, pageable);
    }


        @Transactional
    public void recalculateGlobalStats() {

        // 1. Vymažeme kompletne všetky staré štatistiky (keďže robíme globálny prepočet)
        statRepository.deleteAll(); // Ak máš v repe vytvorené truncateTable(), použi to, je to rýchlejšie

        // 2. Vytiahneme si ÚPLNE VŠETKY zápasy z databázy (historicky)
        List<Matches> matches = (List<Matches>) matchRepository.findAll();

        Map<String, TeamSeasonStat> statsMap = new HashMap<>();

        // Označenie pre našu "sezónu", keďže ide o historické štatistiky
        final String GLOBAL_SEASON_LABEL = "ALL_TIME";

        // 3. SPRACOVANIE ZÁPASOV (Základné štatistiky)
        for (Matches match : matches) {
            String comp = match.getCompetition();
            Long homeTeamId = match.getHomeTeam().getId();
            Long awayTeamId = match.getAwayTeam().getId();
            int homeGoals = match.getScorehome();
            int awayGoals = match.getScoreaway();

            // 1. Zistíme, či ide o finále
            String phase = match.getCompetitionPhase();
            boolean isFinal = phase != null && phase.trim().equalsIgnoreCase("Final");

            // 2. Vytiahneme si ID víťaza (uprav podľa tvojej entity)
            Long winnerId = match.getWinnerId();

            TeamSeasonStat homeStatComp = getOrCreateStat(statsMap, homeTeamId, match.getHomeTeam().getTeamName(), GLOBAL_SEASON_LABEL, comp);
            TeamSeasonStat awayStatComp = getOrCreateStat(statsMap, awayTeamId, match.getAwayTeam().getTeamName(), GLOBAL_SEASON_LABEL, comp);
            TeamSeasonStat homeStatAll = getOrCreateStat(statsMap, homeTeamId, match.getHomeTeam().getTeamName(), GLOBAL_SEASON_LABEL, "ALL");
            TeamSeasonStat awayStatAll = getOrCreateStat(statsMap, awayTeamId, match.getAwayTeam().getTeamName(), GLOBAL_SEASON_LABEL, "ALL");

            // --- Aplikujeme logiku (pridali sme homeTeamId a winnerId do parametrov) ---
            applyMatchResult(homeStatComp, homeTeamId, winnerId, homeGoals, awayGoals, isFinal);
            applyMatchResult(homeStatAll, homeTeamId, winnerId, homeGoals, awayGoals, isFinal);

            // Pre hostí posielame awayTeamId
            applyMatchResult(awayStatComp, awayTeamId, winnerId, awayGoals, homeGoals, isFinal);
            applyMatchResult(awayStatAll, awayTeamId, winnerId, awayGoals, homeGoals, isFinal);
        }


        // Tvoj šikovný stream na vytiahnutie všetkých záznamov
        final List<RecordsInMatches> recordsInMatches = matches.stream()
                .map(Matches::getRecordsInMatches)
                .flatMap(List::stream)
                .toList();

        // 4. SPRACOVANIE ZÁZNAMOV (Karty, Penalty, Vlastné góly)
        for (RecordsInMatches record : recordsInMatches) {
            Matches match = record.getMatch();
            String comp = match.getCompetition();
            long teamId = record.getPlayerTeam().getId();

            Long opponentId = match.getHomeTeam().getId() == teamId ? match.getAwayTeam().getId() : match.getHomeTeam().getId();

            TeamSeasonStat teamStatComp = statsMap.get(teamId + "_" + comp);
            TeamSeasonStat teamStatAll = statsMap.get(teamId + "_ALL");

            TeamSeasonStat opponentStatComp = statsMap.get(opponentId + "_" + comp);
            TeamSeasonStat opponentStatAll = statsMap.get(opponentId + "_ALL");

            String type = record.getTypeOfRecord();

            if ("YC".equals(type)) {
                teamStatComp.setYellowCards(teamStatComp.getYellowCards() + 1);
                teamStatAll.setYellowCards(teamStatAll.getYellowCards() + 1);
            } else if ("RC".equals(type)) {
                teamStatComp.setRedCards(teamStatComp.getRedCards() + 1);
                teamStatAll.setRedCards(teamStatAll.getRedCards() + 1);
            } else if ("Penalty".equals(type)) {
                teamStatComp.setPenaltyGoalsScored(teamStatComp.getPenaltyGoalsScored() + 1);
                teamStatAll.setPenaltyGoalsScored(teamStatAll.getPenaltyGoalsScored() + 1);
                opponentStatComp.setPenaltyGoalsConceded(opponentStatComp.getPenaltyGoalsConceded() + 1);
                opponentStatAll.setPenaltyGoalsConceded(opponentStatAll.getPenaltyGoalsConceded() + 1);
            } else if ("OG".equals(type)) {
                teamStatComp.setOwnGoalsCommitted(teamStatComp.getOwnGoalsCommitted() + 1);
                teamStatAll.setOwnGoalsCommitted(teamStatAll.getOwnGoalsCommitted() + 1);
                opponentStatComp.setOwnGoalsBenefited(opponentStatComp.getOwnGoalsBenefited() + 1);
                opponentStatAll.setOwnGoalsBenefited(opponentStatAll.getOwnGoalsBenefited() + 1);
            }
        }

        // 5. Záverečný prepočet odvodených metrík (Points, Goal Difference) a uloženie
        for (TeamSeasonStat stat : statsMap.values()) {
            stat.setPoints((stat.getWins() * 3) + stat.getDraws());
            stat.setGoalDifference(stat.getGoalsScored() - stat.getGoalsConceded());
        }

        statRepository.saveAll(statsMap.values());

        System.out.println("Globálne historické štatistiky boli úspešne pregenerované!");
    }

    private TeamSeasonStat getOrCreateStat(Map<String, TeamSeasonStat> map, Long teamId, String teamName, String season, String comp) {
        String key = teamId + "_" + comp;
        if (!map.containsKey(key)) {
            TeamSeasonStat newStat = TeamSeasonStat.builder()
                    .teamId(teamId)
                    .teamName(teamName)
                    .season(season) // Tu sa teraz uloží "ALL_TIME"
                    .competition(comp)
                    .build();
            map.put(key, newStat);
        }
        return map.get(key);
    }

    private void applyMatchResult(TeamSeasonStat stat, Long teamId, Long winnerId, int goalsScored, int goalsConceded, boolean isFinal) {
        // Základné počítadlá zápasov a gólov zostávajú rovnaké
        stat.setMatchesPlayed(stat.getMatchesPlayed() + 1);
        stat.setGoalsScored(stat.getGoalsScored() + goalsScored);
        stat.setGoalsConceded(stat.getGoalsConceded() + goalsConceded);


        if (winnerId == MyUtils.DRAW_RESULT_ID) {
            stat.setDraws(stat.getDraws() + 1);
        } else if (winnerId.equals(teamId)) {
            stat.setWins(stat.getWins() + 1);
            if (isFinal) {
                stat.setFinalsWon(stat.getFinalsWon() + 1);
            }
        } else {
            // Ak je niekto iný víťaz, náš tím prehral
            stat.setLosses(stat.getLosses() + 1);
        }


        // --- POKROČILÉ ŠTATISTIKY ---
        if (goalsConceded == 0) {
            stat.setCleanSheets(stat.getCleanSheets() + 1);
        }
        if (goalsScored == 0) {
            stat.setFailedToScore(stat.getFailedToScore() + 1);
        }

        // Ak to bol finálový zápas, pripíšeme účasť
        if (isFinal) {
            stat.setFinalsPlayed(stat.getFinalsPlayed() + 1);
        }
    }
}