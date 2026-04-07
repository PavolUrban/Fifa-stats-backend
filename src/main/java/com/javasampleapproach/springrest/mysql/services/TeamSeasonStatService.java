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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamSeasonStatService { // Môžeš si to premenovať, ak chceš

    private static final String KOTLIK_NAME    = "Kotlik";
    private static final String PAVOL_JAY_NAME = "Pavol Jay";

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
        // Mapa hráčov
        Map<String, Integer> kotlikMatchesMap    = new HashMap<>();
        Map<String, Integer> pavolJayMatchesMap  = new HashMap<>();
        // Mapa sezón (pre competition appearances)
        Map<String, Set<String>> seasonsMap = new HashMap<>();

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

            // --- Sledujeme hráčov ---
            String playerH = match.getPlayerH();
            String playerA = match.getPlayerA();
            if (KOTLIK_NAME.equals(playerH)) {
                kotlikMatchesMap.merge(homeTeamId + "_" + comp, 1, Integer::sum);
                kotlikMatchesMap.merge(homeTeamId + "_ALL",      1, Integer::sum);
            } else if (PAVOL_JAY_NAME.equals(playerH)) {
                pavolJayMatchesMap.merge(homeTeamId + "_" + comp, 1, Integer::sum);
                pavolJayMatchesMap.merge(homeTeamId + "_ALL",      1, Integer::sum);
            }
            if (KOTLIK_NAME.equals(playerA)) {
                kotlikMatchesMap.merge(awayTeamId + "_" + comp, 1, Integer::sum);
                kotlikMatchesMap.merge(awayTeamId + "_ALL",      1, Integer::sum);
            } else if (PAVOL_JAY_NAME.equals(playerA)) {
                pavolJayMatchesMap.merge(awayTeamId + "_" + comp, 1, Integer::sum);
                pavolJayMatchesMap.merge(awayTeamId + "_ALL",      1, Integer::sum);
            }

            // --- Sledujeme sezóny (pre competition appearances) ---
            String season = match.getSeason();
            seasonsMap.computeIfAbsent(homeTeamId + "_" + comp, k -> new HashSet<>()).add(season);
            seasonsMap.computeIfAbsent(homeTeamId + "_ALL",      k -> new HashSet<>()).add(season);
            seasonsMap.computeIfAbsent(awayTeamId + "_" + comp, k -> new HashSet<>()).add(season);
            seasonsMap.computeIfAbsent(awayTeamId + "_ALL",      k -> new HashSet<>()).add(season);
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

        // 5. Záverečný prepočet odvodených metrík a uloženie
        for (Map.Entry<String, TeamSeasonStat> entry : statsMap.entrySet()) {
            String key = entry.getKey();
            TeamSeasonStat stat = entry.getValue();

            stat.setPoints((stat.getWins() * 3) + stat.getDraws());
            stat.setGoalDifference(stat.getGoalsScored() - stat.getGoalsConceded());

            // Forma (current streaks + longest streaks)
            List<String> results = matchResultsMap.getOrDefault(key, Collections.emptyList());
            calculateFormStreaks(stat, results);

            // Hráči
            stat.setMatchesByKotlik(kotlikMatchesMap.getOrDefault(key, 0));
            stat.setMatchesByPavolJay(pavolJayMatchesMap.getOrDefault(key, 0));

            // Competition appearances
            Set<String> appearanceSeasons = seasonsMap.getOrDefault(key, Collections.emptySet());
            stat.setCompetitionAppearances(appearanceSeasons.size());
            stat.setConsecutiveCompetitionAppearances(calculateConsecutiveAppearances(appearanceSeasons));

            // Priemerné štatistiky (na zápas)
            int mp = stat.getMatchesPlayed();
            if (mp > 0) {
                stat.setAvgWins((double) stat.getWins() / mp);
                stat.setAvgDraws((double) stat.getDraws() / mp);
                stat.setAvgLosses((double) stat.getLosses() / mp);
                stat.setAvgPoints((double) stat.getPoints() / mp);
                stat.setAvgGoalsScored((double) stat.getGoalsScored() / mp);
                stat.setAvgGoalsConceded((double) stat.getGoalsConceded() / mp);
                stat.setAvgPenaltyGoalsScored((double) stat.getPenaltyGoalsScored() / mp);
                stat.setAvgYellowCards((double) stat.getYellowCards() / mp);
                stat.setAvgRedCards((double) stat.getRedCards() / mp);
                stat.setCleanSheetsPercentage((double) stat.getCleanSheets() / mp * 100.0);
            }
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
     * Vypočíta 4 aktuálne streaky (current) aj 4 historicky najdlhšie (longest)
     * z chronologického listu výsledkov ("W"/"D"/"L") – poradie newest-first.
     */
    private void calculateFormStreaks(TeamSeasonStat stat, List<String> results) {
        if (results.isEmpty()) return;

        // --- CURRENT STREAKS (od indexu 0 = najnovší zápas) ---
        int winStreak = 0;
        int unbeatenStreak = 0;
        int lossStreak = 0;
        int withoutWinStreak = 0;

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

        // --- LONGEST STREAKS (preskenujeme celý zoznam) ---
        int longestWin = 0, longestUnbeaten = 0, longestLoss = 0, longestWithoutWin = 0;
        int curWin = 0, curUnbeaten = 0, curLoss = 0, curWithoutWin = 0;

        for (String r : results) {
            if ("W".equals(r)) { curWin++;       longestWin       = Math.max(longestWin,       curWin);       } else { curWin = 0; }
            if (!"L".equals(r)) { curUnbeaten++; longestUnbeaten  = Math.max(longestUnbeaten,  curUnbeaten);  } else { curUnbeaten = 0; }
            if ("L".equals(r)) { curLoss++;      longestLoss      = Math.max(longestLoss,      curLoss);      } else { curLoss = 0; }
            if (!"W".equals(r)) { curWithoutWin++; longestWithoutWin = Math.max(longestWithoutWin, curWithoutWin); } else { curWithoutWin = 0; }
        }

        stat.setLongestWinStreak(longestWin);
        stat.setLongestUnbeatenStreak(longestUnbeaten);
        stat.setLongestLossStreak(longestLoss);
        stat.setLongestWithoutWinStreak(longestWithoutWin);
    }

    /**
     * Z množiny sezón (formát "YYYY/YYYY") vypočíta aktuálnu sériu po sebe
     * idúcich sezón bez prerušenia (počítané od poslednej sezóny dozadu).
     */
    private int calculateConsecutiveAppearances(Set<String> seasons) {
        if (seasons.isEmpty()) return 0;

        List<Integer> sortedYears = seasons.stream()
                .map(s -> {
                    try { return Integer.parseInt(s.substring(0, 4)); }
                    catch (Exception e) { return -1; }
                })
                .filter(y -> y > 0)
                .distinct()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());

        if (sortedYears.isEmpty()) return 0;

        int streak = 1;
        for (int i = 1; i < sortedYears.size(); i++) {
            if (sortedYears.get(i) == sortedYears.get(i - 1) - 1) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}