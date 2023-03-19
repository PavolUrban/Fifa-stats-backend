package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerStatsPerSeason;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerStatsPerSeasonWrapper;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerWithRecord;
import com.javasampleapproach.springrest.mysql.model.PlayerWithCards;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.fifa_player.FifaPlayerCoreDTO;
import com.javasampleapproach.springrest.mysql.model.individual_records.IndividualRecordsRequest;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class FifaPlayerService {

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @Autowired
    RecordsInMatchesService recordsInMatchesService;

    @Autowired
    TeamService teamService;

    @Autowired
    ModelMapper modelMapper;

    public List<FifaPlayerCoreDTO> getPlayersByName(final String nameSubstring) {
        List<FifaPlayerDB> fifaPlayers = fifaPlayerDBRepository.findByPlayerNameContainingIgnoreCase(nameSubstring);
        return fifaPlayers.stream()
                .map(player -> modelMapper.map(player, FifaPlayerCoreDTO.class))
                .collect(Collectors.toList());
    }

    public List<FifaPlayerDB> findByIdIn(Set<Long> ids){
        return fifaPlayerDBRepository.findByIdIn(ids);
    }

    public FifaPlayerStatsPerSeasonWrapper getPlayerStats(final Long playerId) {
        final FifaPlayerStatsPerSeasonWrapper allStats = new FifaPlayerStatsPerSeasonWrapper();
        final FifaPlayerDB fifaPlayer = fifaPlayerDBRepository.findById(playerId).get();
        allStats.setPlayerName(fifaPlayer.getPlayerName());
        System.out.println("player" + fifaPlayer.getPlayerName());

        final Map<String, List<RecordsInMatches>> recordsPerSeason = fifaPlayer.getRecordsInMatches().stream()
                .collect(groupingBy(record-> record.getMatch().getSeason()));

        recordsPerSeason.forEach((season, recordsInSeason) -> {
            final FifaPlayerStatsPerSeason playerStatsPerSeason = new FifaPlayerStatsPerSeason();
            playerStatsPerSeason.setSeasonName(season);

            final Team currentSeasonTeam = recordsInSeason.stream().findFirst().get().getPlayerTeam();
            playerStatsPerSeason.setTeamname(currentSeasonTeam.getTeamName());
            playerStatsPerSeason.setTeamId(currentSeasonTeam.getId());

            final Map<String, List<RecordsInMatches>> recordsInCurrentSeasonPerType = recordsInSeason.stream().collect(groupingBy(RecordsInMatches::getTypeOfRecord));

            recordsInCurrentSeasonPerType.forEach((recordType, recordsByType) -> {
                if (MyUtils.RECORD_TYPE_GOAL.equalsIgnoreCase(recordType) || MyUtils.RECORD_TYPE_PENALTY.equalsIgnoreCase(recordType)) {
                    playerStatsPerSeason.setGoalsCount(playerStatsPerSeason.getGoalsCount() + recordsByType.size());
                } else if (MyUtils.RECORD_TYPE_YELLOW_CARD.equalsIgnoreCase(recordType)) {
                    playerStatsPerSeason.setYellowCardsCount(recordsByType.size());
                } else if (MyUtils.RECORD_TYPE_RED_CARD.equalsIgnoreCase(recordType)) {
                    playerStatsPerSeason.setRedCardsCount(recordsByType.size());
                }
            });

            allStats.getPlayerStatsPerSeason().add(playerStatsPerSeason);
        });

        allStats.getPlayerStatsPerSeason().sort(Comparator.comparing(FifaPlayerStatsPerSeason::getSeasonName));

        final FifaPlayerStatsPerSeason totalCount = new FifaPlayerStatsPerSeason();
        totalCount.setSeasonName("Total");

        final int goalsCount = allStats.getPlayerStatsPerSeason().stream().mapToInt(FifaPlayerStatsPerSeason::getGoalsCount).sum();
        totalCount.setGoalsCount(goalsCount);

        final int yellowCardsCount = allStats.getPlayerStatsPerSeason().stream().mapToInt(FifaPlayerStatsPerSeason::getYellowCardsCount).sum();
        totalCount.setYellowCardsCount(yellowCardsCount);

        final int redCardsCount = allStats.getPlayerStatsPerSeason().stream().mapToInt(FifaPlayerStatsPerSeason::getRedCardsCount).sum();
        totalCount.setRedCardsCount(redCardsCount);

        allStats.getPlayerStatsPerSeason().add(totalCount);

        return allStats;
    }


    // todo extract common functionality with getGoalscorers - check if this function is use everywhere and classes do not implement custom functionality for it
    public List<PlayerWithCards> getCards(final String competition, final Long teamId){
        List<RecordsInMatches> allCards = recordsInMatchesService.getRecordsByCompetition(competition, teamId, MyUtils.RECORD_TYPE_RED_CARD, MyUtils.RECORD_TYPE_YELLOW_CARD);
        List<Long> allIds = allCards.stream().map(card -> card.getPlayer().getId()).collect(Collectors.toList());
        Set<Long> distinctIDs = new HashSet<>(allIds);
        Iterable<FifaPlayerDB> allPlayers = findByIdIn(distinctIDs);

        List<PlayerWithCards> allPlayersWithCard = new ArrayList<>();

        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allCards.stream().filter(goals-> goals.getPlayer().getId() == player.getId()).collect(Collectors.toList());
            PlayerWithCards playerWithCard = new PlayerWithCards();
            playerWithCard.setName(player.getPlayerName());
            playerWithCard.setPlayerId(player.getId());

            recordsRelatedToPlayer.forEach(record->{
                if(record.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_YELLOW_CARD)){
                    playerWithCard.setYellowCards(playerWithCard.getYellowCards() + 1);
                } else if(record.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_RED_CARD)){
                    playerWithCard.setRedCards(playerWithCard.getRedCards() + 1);
                }
                playerWithCard.setCardsTotal(playerWithCard.getCardsTotal() + 1);
            });

            allPlayersWithCard.add(playerWithCard);
        });


        allPlayersWithCard.sort((o1, o2) -> o2.getCardsTotal().compareTo(o1.getCardsTotal()));


        return allPlayersWithCard;
    }

    public List<Goalscorer> getGoalscorers(List<RecordsInMatches> allGoals){
        List<Long> allIds = allGoals.stream().map(goal -> goal.getPlayer().getId()).collect(Collectors.toList());
        Set<Long> distinctIDs = new HashSet<>(allIds);
        Iterable<FifaPlayerDB> allPlayers = fifaPlayerDBRepository.findByIdIn(distinctIDs);

        List<Goalscorer> allGoalscorers = new ArrayList<>();
        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allGoals.stream().filter(goals-> goals.getPlayer().getId() == player.getId()).collect(Collectors.toList());
            Goalscorer goalscorer = new Goalscorer();
            goalscorer.setName(player.getPlayerName());
            goalscorer.setTotalGoalsCount(0);
            goalscorer.setPlayerId(player.getId());

            Set<String> teamsPlayerScoredFor = new HashSet<>();

            // todo simplify this get distinct list of teams
            recordsRelatedToPlayer.forEach(record->{
                Team team = teamService.findById(record.getTeam().getId());
                teamsPlayerScoredFor.add(team.getTeamName());
            });

            goalscorer.setTotalGoalsCount(recordsRelatedToPlayer.size());
            goalscorer.setTeamPlayerScoredFor(teamsPlayerScoredFor.stream().findAny().orElse(null));
            goalscorer.setNumberOfTeamsPlayerScoredFor(teamsPlayerScoredFor.size());
            allGoalscorers.add(goalscorer);
        });


        allGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));


        return allGoalscorers;
    }

    public FifaPlayerDB findPlayerById(final long id) {
        return fifaPlayerDBRepository.findById(id).get();
    }

    public List<FifaPlayerWithRecord> getPlayersWithRecord(IndividualRecordsRequest recordsRequest){

        // todo send object here
//        List<FifaPlayerWithRecord> players = new ArrayList<>();
//        String competitionPhase1 = null;
//        String competitionPhase2 = null;
//        if(recordsRequest.getCompetition().equalsIgnoreCase(MyUtils.ALL)) {
//            recordsRequest.setCompetition(null);
//        }
//
//        if(recordsRequest.getCompetitionPhase().equalsIgnoreCase(MyUtils.GROUP_STAGE)) {
//            competitionPhase1 = MyUtils.GROUP_STAGE_LIKE_VALUE;
//            competitionPhase2 = MyUtils.GROUP_STAGE_LIKE_VALUE;
//        } else if (recordsRequest.getCompetitionPhase().equalsIgnoreCase(MyUtils.PLAY_OFFS_STAGE)) {
//            competitionPhase1 = MyUtils.PLAY_OFFS_ROUND_LIKE_VALUE;
//            competitionPhase2 = MyUtils.PLAY_OFFS_FINAL_LIKE_VALUE;
//        }
//
//        // TODO FIX THIS
//        switch (recordsRequest.getRecordType()) {
//            case MyUtils.PLAYER_MOST_GOALS_SINGLE_GAME:
//                players = fifaPlayerDBRepository.getPlayersWithMostGoals(recordsRequest.getCompetition(), MyUtils.RECORD_TYPE_GOAL);
//                players.addAll(fifaPlayerDBRepository.getPlayersWithMostGoalsOldFormat(recordsRequest.getCompetition(), MyUtils.RECORD_TYPE_GOAL));
//                break;
//            case MyUtils.PLAYER_MOST_GOALS_SEASON:
//                List<String> seasons = seasonsService.getAvailableSeasonsList();
//                for (String season : seasons) {
//                    if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(season)){
//                        players.addAll(fifaPlayerDBRepository.getPlayersWithMostGoalsInSeasonOldFormat(season, recordsRequest.getCompetition(), competitionPhase1, competitionPhase2));
//                    } else {
//                        players.addAll(fifaPlayerDBRepository.getPlayersWithMostGoalsInSeasonNewFormat(season, recordsRequest.getCompetition(), competitionPhase1, competitionPhase2));
//                    }
//                }
//                break;
//        }
//
//        Collections.sort(players, Comparator.comparing(FifaPlayerWithRecord::getRecordEventCount).reversed());
//
//        return players.stream().limit(100).collect(Collectors.toList());



        return null;
    }
}
