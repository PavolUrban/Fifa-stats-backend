package com.javasampleapproach.springrest.mysql.serviceV2;

import Utils.HelperMethods;
import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.PlayerInMatch;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.MatchDetail;
import com.javasampleapproach.springrest.mysql.model.MatchEventDetail;
import com.javasampleapproach.springrest.mysql.model.lineup.LineupPlayerDTO;
import com.javasampleapproach.springrest.mysql.model.lineup.PlayerRef;
import com.javasampleapproach.springrest.mysql.model.lineup.TeamLineup;
import com.javasampleapproach.springrest.mysql.model.matches.*;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.PlayerInMatchRepository;
import com.javasampleapproach.springrest.mysql.serviceV2.strategies.TopMatchesStrategy;
import com.javasampleapproach.springrest.mysql.services.SeasonsService;
import com.javasampleapproach.springrest.mysql.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static Utils.MyUtils.OLD_FORMAT;

@Service
public class MatchesServiceV2 {

    @Autowired
    MatchesRepository matchesRepository;

    @Autowired
    PlayerInMatchRepository playerInMatchRepository;

    @Autowired
    SeasonsService seasonsService;

    @Autowired
    TeamService teamService;

    @Autowired
    List<TopMatchesStrategy> topMatchesStrategies;

    public List<MatchesDTO> getFilteredMatches(String competition, String competitionPhase, String season, Long teamId) {
        final String finalCompetition = MyUtils.ALL.equalsIgnoreCase(competition) ? null : competition;
        List<Matches> matches = matchesRepository.getFilteredMatches(finalCompetition, competitionPhase, season, teamId);
        return mapToMatchesDTO(matches);
    }

    public List<Matches> getFilteredMatches(String competition, List<String> competitionPhases, List<String> seasons) {
        final String finalCompetition = MyUtils.ALL.equalsIgnoreCase(competition) || "TOTAL".equalsIgnoreCase(competition) ? null : competition;
        boolean noSeasonFilter = seasons == null || seasons.isEmpty();
        boolean noPhaseFilter = competitionPhases == null || competitionPhases.isEmpty();
        List<String> finalSeasons = noSeasonFilter ? List.of("") : seasons;
        List<String> finalPhases = noPhaseFilter ? List.of("") : competitionPhases;
        return matchesRepository.getFilteredMatches(finalCompetition, finalPhases, noPhaseFilter, finalSeasons, noSeasonFilter);
    }

    public void createOrUpdateMatch(MatchesDTO matchDTO){
        Matches matches = getNewOrExistingMatch(matchDTO.getId());
        mapMatchesDTOtoMatches(matchDTO, matches);
        Matches savedMatch = matchesRepository.save(matches);
        System.out.println("saved match id " + savedMatch.getId());
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

        List<PlayerInMatch> lineupPlayers = playerInMatchRepository.findByMatch_Id(matchId);
        md.setHomeLineup(buildTeamLineup(currentMatch.getHomeTeam(), lineupPlayers));
        md.setAwayLineup(buildTeamLineup(currentMatch.getAwayTeam(), lineupPlayers));

        return md;
    }

    public DataToCreateMatch getDataToCreateMatch() {
        DataToCreateMatch dataToCreateMatch = new DataToCreateMatch();
        dataToCreateMatch.setSeasonsList(seasonsService.getAvailableSeasonsList());
        dataToCreateMatch.setTeamNames(teamService.getAllTeamNames());
        return dataToCreateMatch;
    }

    public DataToCreateMatchV2 getDataToCreateMatchV2() {
        DataToCreateMatchV2 dataToCreateMatch = new DataToCreateMatchV2();
        dataToCreateMatch.setSeasonsList(seasonsService.getAvailableSeasonsList());
        dataToCreateMatch.setTeams(teamService.getTeamsToCreateDialog());
        return dataToCreateMatch;
    }

    private Matches getNewOrExistingMatch(final Long id) {
       return id != null ? matchesRepository.findById(id).get() : new Matches();
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

    private TeamLineup buildTeamLineup(Team team, List<PlayerInMatch> allPlayers) {
        TeamLineup lineup = new TeamLineup();
        lineup.setTeamId(team.getId());
        lineup.setTeamName(team.getTeamName());

        allPlayers.stream()
                .filter(p -> p.getTeam().getId() == team.getId())
                .forEach(p -> {
                    LineupPlayerDTO dto = mapToLineupPlayerDTO(p);
                    if (p.isStarter()) {
                        lineup.getPlayers().add(dto);
                    } else {
                        lineup.getSubstitutes().add(dto);
                    }
                });

        return lineup;
    }

    private LineupPlayerDTO mapToLineupPlayerDTO(PlayerInMatch p) {
        LineupPlayerDTO dto = new LineupPlayerDTO();
        dto.setPlayerId(p.getPlayer().getId());
        dto.setPlayerName(p.getPlayer().getPlayerName());
        dto.setJerseyNumber(p.getJerseyNumber());
        dto.setRating(p.getRating());
        dto.setPosition(p.getPosition());
        dto.setSubstitutedAtMinute(p.getSubstitutionMinute());

        if (p.isStarter() && p.getReplacedBy() != null) {
            dto.setSubstituted(true);
            dto.setReplacedBy(new PlayerRef(p.getReplacedBy().getId(), p.getReplacedBy().getPlayerName()));
        }

        if (!p.isStarter() && p.getReplacedPlayer() != null) {
            dto.setReplacedPlayer(new PlayerRef(p.getReplacedPlayer().getId(), p.getReplacedPlayer().getPlayerName()));
        }

        return dto;
    }

    public List<MatchesDTO> mapToMatchesDTO(List<Matches> matches) {
        return matches.stream().map(this::mapToMatchDTO).toList();
    }

    public MatchesDTO mapToMatchDTO(Matches m) {
        MatchesDTO newMatch = new MatchesDTO();
        newMatch.setId(m.getId());
        newMatch.setHomeTeam(m.getHomeTeam().getTeamName());
        newMatch.setIdHomeTeam(m.getHomeTeam().getId());
        newMatch.setAwayTeam(m.getAwayTeam().getTeamName());
        newMatch.setIdAwayTeam(m.getAwayTeam().getId());
        newMatch.setScorehome(m.getScorehome());
        newMatch.setScoreaway(m.getScoreaway());
        newMatch.setSeason(m.getSeason());
        newMatch.setPlayerH(m.getPlayerH());
        newMatch.setPlayerA(m.getPlayerA());
        newMatch.setCompetition(m.getCompetition());
        newMatch.setCompetitionPhase(m.getCompetitionPhase());
        newMatch.setWinnerId(m.getWinnerId());
        newMatch.setWinnerPlayer(HelperMethods.getWinnerPlayer(m));

        return newMatch;
    }

    public List<MatchesDTO> getTopMatches(final TopMatchesRequest topMatchesRequest) {
        List<MatchesDTO> mappedMatches = new ArrayList<>();
        topMatchesStrategies.stream()
                .filter(strategy-> strategy.getStrategyType().equalsIgnoreCase(topMatchesRequest.getRecordType()))
                .findFirst()
                .ifPresent(strategy -> {
                    List<Matches> topMatches = strategy.getMatches(topMatchesRequest.getSelectedPlayer(), topMatchesRequest.getSelectedCompetition(), topMatchesRequest.getTeamId());
                    mappedMatches.addAll(mapToMatchesDTO(topMatches));
                });
        return mappedMatches;
    }

    public List<Matches> getAllMatchesBySeasonCompetitionAndCompetitionPhaseIn(final String season, final String competition, final List<String> competitionPhases) {
        return matchesRepository.getAllMatchesBySeasonCompetitionAndCompetitionPhaseIn(season, competition, competitionPhases);
    }

    private Matches mapMatchesDTOtoMatches(MatchesDTO matchDTO, Matches match){
        Team homeTeam = teamService.findByTeamName(matchDTO.getHomeTeam());
        Team awayTeam = teamService.findByTeamName(matchDTO.getAwayTeam());

        match.setId(match.getId());
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setScorehome(matchDTO.getScorehome());
        match.setScoreaway(matchDTO.getScoreaway());
        match.setPlayerH(matchDTO.getPlayerH());
        match.setPlayerA(matchDTO.getPlayerA());
        match.setCompetition(matchDTO.getCompetition());
        match.setCompetitionPhase(matchDTO.getCompetitionPhase());
        match.setSeason(matchDTO.getSeason());
        match.setWinnerId(getWinnerTeamId(matchDTO, homeTeam, awayTeam));

        return match;
    }

    private long getWinnerTeamId(MatchesDTO match, Team homeTeam, Team awayTeam) {
        if (match.getScorehome() > match.getScoreaway()) {
            return homeTeam.getId();
        } else if (match.getScorehome() == match.getScoreaway()) {
           return MyUtils.DRAW_RESULT_ID;
        } else {
           return awayTeam.getId();
        }

    }

}
