package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerDialogStats;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerStatsPerSeason;
import com.javasampleapproach.springrest.mysql.model.PlayerStatsInSeason;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/fifaPlayer")
public class FifaPlayerController {

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;


    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @GetMapping("/getStats/{playerName}")
    public FifaPlayerDialogStats getAllStatsForSpecifiedFifaPlayerNEWEST(@PathVariable("playerName") String playerName) {

        FifaPlayerDialogStats allStats = new FifaPlayerDialogStats();
        allStats.setName(playerName);

        FifaPlayerDB fifaPlayer = fifaPlayerDBRepository.findByPlayerName(playerName);
        List<PlayerStatsInSeason> allRecordsForCurrentPlayer = recordsInMatchesRepository.findRecordsRelatedToPlayer(fifaPlayer.getId());
        List<String> playersSeasonsList = allRecordsForCurrentPlayer.stream().map(stat-> stat.getSeason()).distinct().collect(Collectors.toList());

        playersSeasonsList.forEach(season->{
            List<PlayerStatsInSeason> recordsInCurrentSeason = allRecordsForCurrentPlayer.stream().filter(stat-> stat.getSeason().equalsIgnoreCase(season)).collect(Collectors.toList());
            FifaPlayerStatsPerSeason playerStatsPerSeason = new FifaPlayerStatsPerSeason();
            playerStatsPerSeason.setSeasonName(season);

            // todo adjust this in future - it is possible that player has scored for 2 teams in single season, only one team is supported now + i would like to have stored each logo only once - if player played 10 seasons for LFC than only one LFC logo will be sent to FE
            String teamname = recordsInCurrentSeason.stream().map(rec-> rec.getTeamName()).findFirst().orElse("Inspect this, it is not possible to have no teamname here");
            playerStatsPerSeason.setTeamname(teamname);

            recordsInCurrentSeason.forEach(singleRecord->{
                // todo poriesit aj penalty - preklopit records v tabulke tak aby za kazdy gol bolo viac zaznamov aj ked nie je minuta
                if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_GOAL)){
                    if(singleRecord.getNumberOfGoalsForOldFormat()!=null){
                        playerStatsPerSeason.setGoalsCount(playerStatsPerSeason.getGoalsCount() + singleRecord.getNumberOfGoalsForOldFormat());
                    } else {
                        playerStatsPerSeason.setGoalsCount(playerStatsPerSeason.getGoalsCount() + 1);
                    }
                } else if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_YELLOW_CARD)){
                    playerStatsPerSeason.setYellowCardsCount(playerStatsPerSeason.getYellowCardsCount() + 1);
                } else if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_RED_CARD)){
                    playerStatsPerSeason.setRedCardsCount(playerStatsPerSeason.getRedCardsCount() + 1);
                }
            });

            allStats.getPlayerStatsPerSeason().add(playerStatsPerSeason);
        });

        FifaPlayerStatsPerSeason totalCount = new FifaPlayerStatsPerSeason();
        totalCount.setSeasonName("Total");

        allStats.getPlayerStatsPerSeason().forEach(season->{
            totalCount.setGoalsCount(totalCount.getGoalsCount() + season.getGoalsCount());
            totalCount.setYellowCardsCount(totalCount.getYellowCardsCount() + season.getYellowCardsCount());
            totalCount.setRedCardsCount(totalCount.getRedCardsCount() + season.getRedCardsCount());
        });

        allStats.getPlayerStatsPerSeason().add(totalCount);

        return allStats;
    }
}
