package com.javasampleapproach.springrest.mysql.services;

import Utils.HelperMethods;
import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MatchesService {

    @Autowired
    MatchesRepository matchesRepository;

    @Autowired
    SeasonsService seasonsService;



    // todo merge empty filters
    public List<Matches> getAllMatches() {
        List<Matches> matches = (List<Matches>) matchesRepository.findAll();
        return setWinners(matches);
    }

    // todo merge
    public List<Matches> getMatchesByCompetitionPhase(String competitionPhase) {
        List<Matches> matches = matchesRepository.findByCompetitionPhase(competitionPhase);
        return setWinners(matches);
    }

    // todo merge
    public List<Matches> getMatchesByCompetitionPhaseAndSeason(String competitionPhase, String competition) {
        List<Matches> matches = matchesRepository.findByCompetitionPhaseAndCompetitionOrderBySeason(competitionPhase, competition);
        return setWinners(matches);
    }



    public Matches findMatchById(Long matchId) {
        return matchesRepository.findById(matchId).orElse(null);
    }


    // todo rework-rename
    public Map<String, Object> getCustomGroupMatches(String firstTeam, String secondTeam) {
// todo latest
//        long firstTeamId = teamService.findByTeamName(firstTeam).getId();
//        long secondTeamId = teamService.findByTeamName(secondTeam).getId();
//
//        List<Matches> finalList = new ArrayList<>();
//        matchesRepository.findByIdHomeTeamAndIdAwayTeam(firstTeamId, secondTeamId).forEach(finalList::add);
//        matchesRepository.findByIdHomeTeamAndIdAwayTeam(secondTeamId, firstTeamId).forEach(finalList::add);
//        finalList.sort(Comparator.comparing(Matches::getSeason).thenComparing(Matches::getCompetitionPhase));
//        List<Team> allTeams = teamService.getAllTeams();
//
//        Map<String, Object> response = new HashMap<>();
//
//        // todo use here something like bilance? it should be already used at other places
//        Map<String, Integer> playersStats = new HashMap<>();
//        playersStats.put(MyUtils.PAVOL_JAY, 0);
//        playersStats.put(MyUtils.KOTLIK, 0);
//        playersStats.put(MyUtils.RESULT_DRAW, 0);
//
//        Map<String, Integer> overallStats = new HashMap<>();
//        overallStats.put(firstTeam, 0);
//        overallStats.put(secondTeam, 0);
//        overallStats.put(MyUtils.RESULT_DRAW, 0);
//
//        for (Matches match : finalList) {
//            //players statistics
//            String winner = HelperMethods.whoIsWinnerOfMatch(match);
//            playersStats.put(winner, playersStats.get(winner) + 1);
//
//            //teams statistics
//            String winnerTeamName = match.getWinnerId() == -1 ? MyUtils.RESULT_DRAW : teamService.getTeamNameById(allTeams, match.getWinnerId());
//            overallStats.put(winnerTeamName, overallStats.get(winnerTeamName) + 1);
//        }
//
//
//        response.put("playersStats", convertMapToList(playersStats, MyUtils.PAVOL_JAY, MyUtils.KOTLIK));
//        response.put("matches", finalList);
//        response.put("overallStats", convertMapToList(overallStats, firstTeam, secondTeam));


      //  return response;
        return  null;
    }


    // todo is this needed?
    public Map<String, List<String>> getCompetitionPhasesAndSeasonsList() {

        Map<String, List<String>> competitionPhases = new HashMap<>();

        competitionPhases.put("competitionsPhasesCL", MyUtils.championsLeagueStagesListWithDefault);
        competitionPhases.put("competitionsPhasesEL", MyUtils.europeanLeagueStagesListWithDefault);
        competitionPhases.put("defaultCompetitionPhases", MyUtils.europeanLeagueStagesListWithDefault);

        List<String> allSeasons = seasonsService.getAvailableSeasonsList();
        allSeasons.add(0, MyUtils.ALL_SEASONS);
        competitionPhases.put("seasonsList", allSeasons);

        return competitionPhases;
    }

    private List<Matches> setWinners(List<Matches> matches) {
        matches.forEach(match -> {
            match.setWinnerPlayer(HelperMethods.whoIsWinnerOfMatch(match));
        });
        return matches;
    }
}
