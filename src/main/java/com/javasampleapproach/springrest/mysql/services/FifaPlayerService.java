package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerWithRecord;
import com.javasampleapproach.springrest.mysql.model.PlayerWithCards;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.individual_records.IndividualRecordsRequest;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FifaPlayerService {

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @Autowired
    RecordsInMatchesService recordsInMatchesService;

    @Autowired
    TeamService teamService;

    @Autowired
    SeasonsService seasonsService;

    public List<FifaPlayerDB> getPlayersByName(final String nameSubstring) {
        return fifaPlayerDBRepository.findByPlayerNameContainingIgnoreCase(nameSubstring);
    }

    public List<FifaPlayerDB> findByIdIn(Set<Long> ids){
        return fifaPlayerDBRepository.findByIdIn(ids);
    }

    // todo extract common functionality with getGoalscorers - check if this function is use everywhere and classes do not implement custom functionality for it
    public List<PlayerWithCards> getCards(final String competition, final Integer teamId){
        List<RecordsInMatches> allCards = recordsInMatchesService.getRecordsByCompetition(competition, teamId, MyUtils.RECORD_TYPE_RED_CARD, MyUtils.RECORD_TYPE_YELLOW_CARD);
        List<Long> allIds = allCards.stream().map(card -> card.getPlayer().getId()).collect(Collectors.toList());
        Set<Long> distinctIDs = new HashSet<>(allIds);
        Iterable<FifaPlayerDB> allPlayers = findByIdIn(distinctIDs);

        List<PlayerWithCards> allPlayersWithCard = new ArrayList<>();

        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allCards.stream().filter(goals-> goals.getPlayer().getId() == player.getId()).collect(Collectors.toList());
            PlayerWithCards playerWithCard = new PlayerWithCards();
            playerWithCard.setName(player.getPlayerName());

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

            Set<String> teamsPlayerScoredFor = new HashSet<>();

            // todo simplify this get distinct list of teams
            recordsRelatedToPlayer.forEach(record->{
                Optional<Team> team = teamService.findById(record.getTeam().getId());
                teamsPlayerScoredFor.add(team.get().getTeamName());
            });

            goalscorer.setTotalGoalsCount(recordsRelatedToPlayer.size());
            goalscorer.setTeamPlayerScoredFor(teamsPlayerScoredFor.stream().findAny().orElse(null));
            goalscorer.setNumberOfTeamsPlayerScoredFor(teamsPlayerScoredFor.size());
            allGoalscorers.add(goalscorer);
        });


        allGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));


        return allGoalscorers;
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
