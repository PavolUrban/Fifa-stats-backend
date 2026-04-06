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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamSeasonStatService { // Môžeš si to premenovať, ak chceš

    @Autowired
    private TeamSeasonStatRepository statRepository;
    @Autowired
    private MatchesRepository matchRepository;


    public List<TeamSeasonStat> findByCompetition(String competition) {
        return statRepository.findByCompetition(competition);
    }


        @Transactional
    public void recalculateGlobalStats() {

        // 1. Vymažeme kompletne všetky staré štatistiky (keďže robíme globálny prepočet)
        statRepository.deleteAll(); // Ak máš v repe vytvorené truncateTable(), použi to, je to rýchlejšie

        // 2. Vytiahneme si ÚPLNE VŠETKY zápasy z databázy zoradené chronologicky (pre formu)
        List<Matches> matches = matchRepository.findAllChronological();

        Map<String, TeamSeasonStat> statsMap = new HashMap<>();
        // Mapa výsledkov pre každý tím + súťaž (pre výpočet formy)
        Map<String, List<String>> matchResultsMap = new HashMap<>();

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

            // --- Zaznamenáme výsledok pre výpočet formy (chronologické poradie zachované vďaka findAllByOrderByIdAsc) ---
            String homeResult = determineResult(match.getWinnerId(), homeTeamId);
            String awayResult = determineResult(match.getWinnerId(), awayTeamId);
            recordResult(matchResultsMap, homeTeamId + "_" + comp, homeResult);
            recordResult(matchResultsMap, homeTeamId + "_ALL", homeResult);
            recordResult(matchResultsMap, awayTeamId + "_" + comp, awayResult);
            recordResult(matchResultsMap, awayTeamId + "_ALL", awayResult);
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

        // 5. Záverečný prepočet odvodených metrík (Points, Goal Difference, Forma) a uloženie
        for (Map.Entry<String, TeamSeasonStat> entry : statsMap.entrySet()) {
            TeamSeasonStat stat = entry.getValue();
            stat.setPoints((stat.getWins() * 3) + stat.getDraws());
            stat.setGoalDifference(stat.getGoalsScored() - stat.getGoalsConceded());

            List<String> results = matchResultsMap.getOrDefault(entry.getKey(), Collections.emptyList());
            calculateFormStreaks(stat, results);
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

    // -----------------------------------------------------------------------
    // POMOCNÉ METÓDY PRE FORMU
    // -----------------------------------------------------------------------

    /**
     * Vráti "W", "D" alebo "L" z pohľadu daného tímu.
     */
    private String determineResult(long winnerId, long teamId) {
        if (winnerId == MyUtils.DRAW_RESULT_ID) return "D";
        if (winnerId == teamId) return "W";
        return "L";
    }

    /**
     * Pridá výsledok do listu pre daný kľúč (teamId_comp).
     */
    private void recordResult(Map<String, List<String>> map, String key, String result) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(result);
    }

    /**
     * Vypočíta 4 formy streaky z chronologického listu výsledkov ("W"/"D"/"L")
     * a nastaví ich na stat objekt.
     *
     * Príklady:
     *   [W, W, W]     → winStreak=3, unbeaten=3, lossStreak=0, withoutWin=0
     *   [L, W, D, D]  → winStreak=0, unbeaten=3, lossStreak=0, withoutWin=2
     *   [W, D, L, L]  → winStreak=0, unbeaten=0, lossStreak=2, withoutWin=3
     *   [W, L, D]     → winStreak=0, unbeaten=0, lossStreak=0, withoutWin=2 (D,L bez výhry)
     *                    + unbeaten=1 (len D) → záleží na kontexte; oba sú vypočítané
     */
    private void calculateFormStreaks(TeamSeasonStat stat, List<String> results) {
        if (results.isEmpty()) return;

        int winStreak = 0;
        int unbeatenStreak = 0;
        int lossStreak = 0;
        int withoutWinStreak = 0;

        // Ideme od indexu 0 – zápasy sú uložené newest-first (rovnaký sort ako getFilteredMatches)
        for (int i = 0; i < results.size(); i++) {
            if ("W".equals(results.get(i))) winStreak++;
            else break;
        }
        for (int i = 0; i < results.size(); i++) {
            if (!"L".equals(results.get(i))) unbeatenStreak++;  // W alebo D
            else break;
        }
        for (int i = 0; i < results.size(); i++) {
            if ("L".equals(results.get(i))) lossStreak++;
            else break;
        }
        for (int i = 0; i < results.size(); i++) {
            if (!"W".equals(results.get(i))) withoutWinStreak++; // L alebo D
            else break;
        }

        stat.setCurrentWinStreak(winStreak);
        stat.setCurrentUnbeatenStreak(unbeatenStreak);
        stat.setCurrentLossStreak(lossStreak);
        stat.setCurrentWithoutWinStreak(withoutWinStreak);
    }
}