package com.javasampleapproach.springrest.mysql.services;

import Utils.HelperMethods;
import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.MatchDetail;
import com.javasampleapproach.springrest.mysql.model.MatchEventDetail;
import com.javasampleapproach.springrest.mysql.model.MatchesDTO;
import com.javasampleapproach.springrest.mysql.model.TeamGlobalStats;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static Utils.MyUtils.OLD_FORMAT;
import static Utils.MyUtils.RECORD_TYPE_GOAL;

@Service
public class MatchesService {

    @Autowired
    MatchesRepository matchesRepository;

    @Autowired
    TeamService teamService;

    @Autowired
    RecordsInMatchesService recordsInMatchesService;

    @Autowired
    SeasonsService seasonsService;

    @Autowired
    FifaPlayerService fifaPlayerService;

    @Autowired
    ModelMapper modelMapper;

    // todo latest
    public List<Matches> getTeamMatchesById(long teamId){
        return null;//matchesRepository.getAllMatchesForTeam(teamId);
    }

    // todo latest
    public String getFirstSeasonInCompetition(String teamName, String competition) {
        //return matchesRepository.firstSeasonInCompetition(teamName, competition);
        return null;
    }

    // todo latest
    public Map<String, Object> getMatchesForCustomTeam(String teamName) {
//        long teamId = teamService.findByTeamName(teamName).getId();
//        List<Matches> matches = matchesRepository.findByIdHomeTeamOrIdAwayTeam(teamId, teamId);
//
//        Map<String, Object> result = new HashMap();
//        result.put("Round of 16", null);
//        result.put("quarterfinals", null);
//        result.put("semifinals", null);
//        result.put("finals", null);
//
//        List<Matches> roundOf16 = matches.stream().filter(o -> o.getCompetitionPhase().contains("Round of 16")).collect(Collectors.toList());
//        Iterable<Matches> quarterfinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Quarterfinals")).collect(Collectors.toList());
//        Iterable<Matches> semifinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Semifinals")).collect(Collectors.toList());
//        List<Matches> finals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Final")).sorted((x1, x2) -> x2.getSeason().compareTo(x1.getSeason())).collect(Collectors.toList());
//
//        TeamGlobalStats teamGlobalStats = new TeamGlobalStats();
//        teamGlobalStats.setFinalsPlayed(finals.size());
//
//        int winsInFinal = 0;
//        for (Matches finale : finals) {
//            if (finale.getWinnerId() == teamId)
//                winsInFinal++;
//        }
//
//        teamGlobalStats.setTotalWinsCL(winsInFinal);
//        teamGlobalStats.setFinalsPlayed(finals.size());
//
//
//        result.put("final", finals);
//        result.put("finalStats", teamGlobalStats);
//
//        return result;
        return null;
    }

    // todo merge empty filters
    public List<Matches> getAllMatches() {
        List<Matches> matches = (List<Matches>) matchesRepository.findAll();
        return setWinners(matches);
    }

    // todo merge with other similar methods send variables
    public List<Matches> getAllMatchesBySeason(String season) {
        return matchesRepository.findBySeason(season);
    }

    // todo latest
    public List<Matches> getMatchesForCustomTeamNew(String teamName, String season) {
       // List<Matches> matches = matchesRepository.findBySeasonAndHometeamOrSeasonAndAwayteam(season, teamName, season, teamName);
        // return setWinners(matches);
        return null;
    }

    // todo merge
    public List<MatchesDTO> getCustomGroupMatches(String competition, String season, String competitionPhase) {
        List<Matches> matches = matchesRepository.findByCompetitionAndSeasonAndCompetitionPhase(competition, season, competitionPhase);
        List<MatchesDTO> matchesDTOList = new ArrayList<>();
        matches.forEach(match -> {
           MatchesDTO matchDto =  modelMapper.map(match, MatchesDTO.class);
            matchesDTOList.add(matchDto);
        });
        return matchesDTOList;
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

    // todo
    public List<Matches> getAllMatchesBySeasonAndCompetitionPlayOffs(String season, String competition) {
        List<Matches> matches = matchesRepository.getAllMatchesBySeasonAndCompetitionPlayOffs(season, competition);
        return setWinners(matches);
    }

    public List<Matches> getAllMatchesBySeasonAndCompetitionGroupStage(String season, String competition) {
        List<Matches> matches = matchesRepository.getAllMatchesBySeasonAndCompetitionGroupStage(season, competition);
        return setWinners(matches);
    }




    // todo merge
    public List<Matches> getFilteredMatches(String season, String competition, String competitionPhase, String teamName) {

        long teamId = teamService.findByTeamName(teamName).getId();

        if (season.equalsIgnoreCase(MyUtils.ALL_SEASONS)) {
            season = null;
        }

        if (competition.equalsIgnoreCase(MyUtils.ALL_COMPETITIONS)) {
            competition = null;
        }

        if (competitionPhase.equalsIgnoreCase(MyUtils.ALL_PHASES)) {
            competitionPhase = null;
        }
// TODO latest fix
//        List<Matches> matches = matchesRepository.getMatchesWithCustomFilters(season, competition, competitionPhase);


//        if (teamName == null || teamName.equalsIgnoreCase("null")) {
//            return matches;
//        } else {
//            List<Matches> matchesForSelectedTeam = matches.stream().filter(match -> match.getIdHomeTeam() == teamId || match.getIdAwayTeam() == teamId).collect(Collectors.toList());
//            return matchesForSelectedTeam;
//        }

        return null;
    }

    public List<Matches> getTopMatches(String recordType, String selectedPlayer, String selectedCompetition, String teamName) {
        List<Matches> matches = new ArrayList<>();
        Long teamId = null;

        if(selectedPlayer.equalsIgnoreCase(MyUtils.ALL)) {
            selectedPlayer = null;
        }

        if(selectedCompetition.equalsIgnoreCase(MyUtils.ALL)){
            selectedCompetition = null;
        }

        if(!teamName.equalsIgnoreCase(MyUtils.ALL)) {
            teamId = teamService.findByTeamName(teamName).getId();
        }
// TODO latest

//        switch (recordType) {
//            case MyUtils.MOST_GOALS_IN_MATCH:
//                matches = matchesRepository.getMatchesWithMostGoals(selectedCompetition, teamId);
//                break;
//            case MyUtils.BIGGEST_AWAY_WINS:
//                matches = matchesRepository.getBiggestAwayWins(selectedPlayer, selectedCompetition, teamId);
//                break;
//            case MyUtils.BIGGEST_HOME_WINS:
//                matches = matchesRepository.getBiggestHomeWins(selectedPlayer, selectedCompetition, teamId);
//                break;
//            case MyUtils.BIGGEST_DRAWS:
//                matches = matchesRepository.getBiggestDraws(selectedCompetition, teamId);
//                break;
//        }

        return setWinners(matches);
    }


    // TODO add some validation - e.g if provided goalscorers match up witch score
    // todo zrejme zjednotit s updateexisting
    public Matches createMatch(Matches match) {

        //todo latest fix max creation
//        Team homeTeam = teamService.findByTeamName(match.getHometeam());
//        Team awayTeam = teamService.findByTeamName(match.getAwayteam());
//        System.out.println(homeTeam);
//        match.setIdHomeTeam(homeTeam.getId());
//        match.setIdAwayTeam(awayTeam.getId());
//        System.out.println("toto chcem ulozit");
//        System.out.println(match);
//
//        setWinningTeam(match);
//
//        matchesRepository.save(match);

        return null;
    }

    public Matches updateExistingMatch(Matches match) {

        setWinningTeam(match);

        System.out.println("toto chcem ulozit");
        System.out.println(match);
        // saveMatchesRecords(match);
        // Matches newMatch = matchesRepository.save(match);

        return null;
    }

    public MatchDetail getMatchDetails(Long matchId) {
        Matches currentMatch= matchesRepository.findById(matchId).get();
        MatchDetail md = new MatchDetail();

        if (MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(currentMatch.getSeason())) {
            currentMatch.getRecordsInMatches().forEach(recordInMatch -> {
                MatchEventDetail med = new MatchEventDetail();
                med.setPlayerName(recordInMatch.getPlayer().getPlayerName());
                med.setRecordType(recordInMatch.getTypeOfRecord());
                med.setTeamRecordId((int) recordInMatch.getTeam().getId());
                md.getEventsWithoutTime().add(med);
            });
            md.setTypeOfFormat(OLD_FORMAT);
        } else {
            currentMatch.getRecordsInMatches().forEach(recordInMatch -> {
                MatchEventDetail med = new MatchEventDetail();
                med.setPlayerName(recordInMatch.getPlayer().getPlayerName());
                med.setRecordType(recordInMatch.getTypeOfRecord());
                med.setTeamRecordId((int) recordInMatch.getTeam().getId());
                med.setMinute(recordInMatch.getMinuteOfRecord());
                med.setMinuteLabel(recordInMatch.getMinuteOfRecord() > 9 ? recordInMatch.getMinuteOfRecord().toString() + "'" : "0" + recordInMatch.getMinuteOfRecord() + "'");
                addEventToProperHalfTime(md, recordInMatch, med);
                md.getEventsFirstHalf().sort(Comparator.comparing(MatchEventDetail::getMinute));
                md.getEventsSecondHalf().sort(Comparator.comparing(MatchEventDetail::getMinute));
                md.getEventsOverTime().sort(Comparator.comparing(MatchEventDetail::getMinute));
            });
            md.setTypeOfFormat(MyUtils.NEW_FORMAT);
        }

        return md;
    }

    public Matches getMatchById(Long matchId) {
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

    public Map<String, List<String>> getDataToCreateMatch() {

        Map<String, List<String>> dataToCreateMatch = new HashMap<>();

        dataToCreateMatch.put("competitionsList", MyUtils.competitionsList);
        dataToCreateMatch.put("playerNamesList", MyUtils.playerNamesList);
        dataToCreateMatch.put("competitionsPhasesCL", MyUtils.championsLeagueStagesList);
        dataToCreateMatch.put("competitionsPhasesEL", MyUtils.europeanLeagueStagesList);
        dataToCreateMatch.put("seasonsList", seasonsService.getAvailableSeasonsList());

        return dataToCreateMatch;
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

    private void addEventToProperHalfTime(MatchDetail md, RecordsInMatches record, MatchEventDetail med){
        if (record.getMinuteOfRecord() <= 45) {
            md.getEventsFirstHalf().add(med);
        } else if (record.getMinuteOfRecord() <= 90) {
            md.getEventsSecondHalf().add(med);
        } else {
            md.getEventsOverTime().add(med);
        }
    }

    private String getPlayerNameById(long id, List<FifaPlayerDB> players) {
        String name = "UNKNOWN";
        FifaPlayerDB playerDB = players.stream().filter(p -> p.getId() == id).findFirst().orElse(null);

        if (playerDB != null) {
            name = playerDB.getPlayerName();
        }

        return name;
    }

    // todo latest
    private void setWinningTeam(Matches match) {
//        if (match.getScorehome() > match.getScoreaway()) {
//            match.setWinnerId(match.getIdHomeTeam());
//        } else if (match.getScorehome() == match.getScoreaway()) {
//            match.setWinnerId(MyUtils.DRAW_RESULT_ID);
//        } else {
//            match.setWinnerId(match.getIdAwayTeam());
//        }
//
    }

    private List<Integer> convertMapToList(Map<String, Integer> playersStats, String homePlayerOrTeam, String awayPlayerOrTeam) {
        List<Integer> intValues = new ArrayList<Integer>();

        intValues.add(playersStats.get(homePlayerOrTeam));
        intValues.add(playersStats.get("D")); //D stands always for draw
        intValues.add(playersStats.get(awayPlayerOrTeam));

        return intValues;
    }

    // todo get rid of this?
    private String getTeamNameById(List<Team> allTeams, int teamId){
        return allTeams.stream().filter(team-> team.getId() == teamId).map(team -> team.getTeamName()).findFirst().orElse(null);
    }

    private List<Matches> setWinners(List<Matches> matches) {
        matches.forEach(match -> {
            match.setWinnerPlayer(HelperMethods.whoIsWinnerOfMatch(match));
        });
        return matches;
    }
}
