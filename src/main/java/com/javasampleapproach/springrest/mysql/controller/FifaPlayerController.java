package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerDialogStats;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerStatsPerSeason;
import com.javasampleapproach.springrest.mysql.model.PlayerStatToSave;
import com.javasampleapproach.springrest.mysql.model.PlayerStatsInSeason;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
//    private long id;
//
//    @Column(name = "playerid")
//    private long playerId;
//
//    @Column(name = "matchid")
//    private int matchId;
//
//    @Column(name = "teamname")
//    private String teamName;
//
//    @Column(name = "typeofrecord")
//    private String typeOfRecord;
//
//    @Column(name = "minuteofrecord")
//    private Integer minuteOfRecord;
//
//    @Column(name = "numberofgoalsforoldformat")
//    private Integer numberOfGoalsForOldFormat;
    @PostMapping("/savePlayerRecord")
    public List<String> getPlayerNames(@RequestBody PlayerStatToSave player){
    System.out.println(" v db hladam hraca menom "+ player.getPlayerName());
       FifaPlayerDB fifaPlayer = fifaPlayerDBRepository.findByPlayerName(player.getPlayerName());
        RecordsInMatches rim = new RecordsInMatches();

        rim.setPlayerId(fifaPlayer.getId());
        rim.setMatchId(player.getMatchId());
        rim.setTeamName(player.getTeamName());
        rim.setTypeOfRecord(player.getRecordType());

        int numericValue = Integer.parseInt(player.getDetails());
        if(player.getRecordSignature().equalsIgnoreCase("minutes")){
            rim.setMinuteOfRecord(numericValue);
        } else {
            rim.setNumberOfGoalsForOldFormat(numericValue);
        }

        recordsInMatchesRepository.save(rim);
        return null;
    }


    @GetMapping("/getPlayerNames")
    public List<String> getPlayerNames(){
        List<String> playerNames = new ArrayList<>();
        fifaPlayerDBRepository.findAll().forEach(fifaPlayerDB -> playerNames.add(fifaPlayerDB.getPlayerName()));

        return playerNames;
    }
    @GetMapping("/getStats/{playerName}")
    public FifaPlayerDialogStats getAllStatsForSpecifiedFifaPlayerNEWEST(@PathVariable("playerName") String playerName) {

        // TODO fix this
        // pozor vznika problem ked hrac prestupil v ramci sezony!! check haaland - 3g za salzburg + 2za dormund su zobrazene ako 5 za salzburg
        FifaPlayerDialogStats allStats = new FifaPlayerDialogStats();
        allStats.setName(playerName);

        FifaPlayerDB fifaPlayer = fifaPlayerDBRepository.findByPlayerName(playerName);
        List<PlayerStatsInSeason> allRecordsForCurrentPlayer = recordsInMatchesRepository.findRecordsRelatedToPlayer(fifaPlayer.getId());
        List<String> playersSeasonsList = allRecordsForCurrentPlayer.stream().map(stat-> stat.getSeason()).distinct().collect(Collectors.toList());

        playersSeasonsList.forEach(season->{
            List<PlayerStatsInSeason> recordsInCurrentSeason = allRecordsForCurrentPlayer.stream().filter(stat-> stat.getSeason().equalsIgnoreCase(season)).collect(Collectors.toList());
            FifaPlayerStatsPerSeason playerStatsPerSeason = new FifaPlayerStatsPerSeason();
            playerStatsPerSeason.setSeasonName(season);

            String teamname = recordsInCurrentSeason.stream().map(rec-> rec.getTeamName()).findFirst().orElse("Inspect this, it is not possible to have no teamname here");
            playerStatsPerSeason.setTeamname(teamname);

            recordsInCurrentSeason.forEach(singleRecord->{
                System.out.println(singleRecord.getTypeOfRecord() + " for " + singleRecord.getTeamName());
                if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_GOAL)){
                    if(singleRecord.getNumberOfGoalsForOldFormat()!=null){
                        System.out.println("old format detected");
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
        System.out.println(playerStatsPerSeason);
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
