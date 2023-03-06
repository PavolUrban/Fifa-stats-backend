package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.PlayerWithCards;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<FifaPlayerDB> getPlayersByName(final String nameSubstring) {
        return fifaPlayerDBRepository.findByPlayerNameContainingIgnoreCase(nameSubstring);
    }

    public List<FifaPlayerDB> findByIdIn(Set<Long> ids){
        return fifaPlayerDBRepository.findByIdIn(ids);
    }

    // todo extract common functionality with getGoalscorers - check if this function is use everywhere and classes do not implement custom functionality for it
    public List<PlayerWithCards> getCards(final String competition, final Integer teamId){
        List<RecordsInMatches> allCards = recordsInMatchesService.getRecordsByCompetition(competition, teamId, MyUtils.RECORD_TYPE_RED_CARD, MyUtils.RECORD_TYPE_YELLOW_CARD);
        List<Long> allIds = allCards.stream().map(RecordsInMatches::getPlayerId).collect(Collectors.toList());
        Set<Long> distinctIDs = new HashSet<>(allIds);
        Iterable<FifaPlayerDB> allPlayers = findByIdIn(distinctIDs);

        List<PlayerWithCards> allPlayersWithCard = new ArrayList<>();

        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allCards.stream().filter(goals-> goals.getPlayerId() == player.getId()).collect(Collectors.toList());
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


        List<Long> allIds = allGoals.stream().map(RecordsInMatches::getPlayerId).collect(Collectors.toList());
        Set<Long> distinctIDs = new HashSet<>(allIds);
        Iterable<FifaPlayerDB> allPlayers = fifaPlayerDBRepository.findByIdIn(distinctIDs);

        List<Goalscorer> allGoalscorers = new ArrayList<>();
        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allGoals.stream().filter(goals-> goals.getPlayerId() == player.getId()).collect(Collectors.toList());
            Goalscorer goalscorer = new Goalscorer();
            goalscorer.setName(player.getPlayerName());
            goalscorer.setTotalGoalsCount(0);

            Set<String> teamsPlayerScoredFor = new HashSet<>();

            // todo simplify this get distinct list of teams
            recordsRelatedToPlayer.forEach(record->{
                Optional<Team> team = teamService.findById(new Long(record.getTeamRecordId()));
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
}
