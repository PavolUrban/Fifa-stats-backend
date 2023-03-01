package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.*;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.SeasonsRepository;
import com.javasampleapproach.springrest.mysql.services.FifaPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/fifaPlayer")
public class FifaPlayerController {

    // remove repos use services!
    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @Autowired
    SeasonsRepository seasonsRepository;

    @Autowired
    FifaPlayerService fifaPlayerService;


    @GetMapping("/getPlayersByName/{nameSubstring}")
    public List<FifaPlayerDB> getPlayerByName(@PathVariable("nameSubstring") String nameSubstring) {
        return fifaPlayerService.getPlayersByName(nameSubstring);
    }


    // todo move to recordsInMatchesController + renamePlayerStatTOSave
    @PostMapping("/savePlayerRecord")
    public List<String> getPlayerNames(@RequestBody NewRecordToSave newRecordToSave){
        System.out.println("TOto chcem ulozit");
        System.out.println(newRecordToSave);

        RecordsInMatches rim = new RecordsInMatches();
        rim.setPlayerId(newRecordToSave.getPlayerId());
        rim.setMatchId(newRecordToSave.getMatchId());
        rim.setTeamName(newRecordToSave.getTeamName());
        // todo rim.setTeamId()
        rim.setTypeOfRecord(newRecordToSave.getRecordType());


        if(newRecordToSave.getRecordSignature().equalsIgnoreCase("minutes")){
            rim.setMinuteOfRecord(newRecordToSave.getNumericDetail());
        } else {
            rim.setNumberOfGoalsForOldFormat(newRecordToSave.getNumericDetail());
        }

        recordsInMatchesRepository.save(rim);
        return null;
    }

    @GetMapping("/getStats/{playerName}")
    public FifaPlayerDialogStats getAllStatsForSpecifiedFifaPlayerNEWEST(@PathVariable("playerName") String playerName) {

        // TODO fix this
        // pozor vznika problem ked hrac prestupil v ramci sezony!! check haaland - 3g za salzburg + 2za dormund su zobrazene ako 5 za salzburg
        FifaPlayerDialogStats allStats = new FifaPlayerDialogStats();
        allStats.setName(playerName);
        System.out.println("hladam hraca menom " + playerName);
        FifaPlayerDB fifaPlayer = fifaPlayerDBRepository.findByPlayerName(playerName);
        System.out.println(fifaPlayer);
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
                if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_GOAL) || singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_PENALTY)){
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


    @GetMapping("/getPlayersWithRecord/{recordType}/{competition}/{competitionPhase}")
    public List<FifaPlayerWithRecord> getPlayersWithRecord(@PathVariable("recordType") String recordType, @PathVariable("competition") String competition,  @PathVariable("competitionPhase") String competitionPhase){
        List<FifaPlayerWithRecord> players = new ArrayList<>();
        String competitionPhase1 = null;
        String competitionPhase2 = null;
        if(competition.equalsIgnoreCase(MyUtils.ALL)) {
            competition = null;
        }

        if(competitionPhase.equalsIgnoreCase(MyUtils.GROUP_STAGE)) {
            competitionPhase1 = MyUtils.GROUP_STAGE_LIKE_VALUE;
            competitionPhase2 = MyUtils.GROUP_STAGE_LIKE_VALUE;
        } else if (competitionPhase.equalsIgnoreCase(MyUtils.PLAY_OFFS_STAGE)) {
            competitionPhase1 = MyUtils.PLAY_OFFS_ROUND_LIKE_VALUE;
            competitionPhase2 = MyUtils.PLAY_OFFS_FINAL_LIKE_VALUE;
        }

        switch (recordType) {
            case MyUtils.PLAYER_MOST_GOALS_SINGLE_GAME:
                players = fifaPlayerDBRepository.getPlayersWithMostGoals(competition, MyUtils.RECORD_TYPE_GOAL);
                players.addAll(fifaPlayerDBRepository.getPlayersWithMostGoalsOldFormat(competition, MyUtils.RECORD_TYPE_GOAL));
                break;
            case MyUtils.PLAYER_MOST_GOALS_SEASON:
                List<String> seasons = seasonsRepository.getAvailableSeasonsList();
                for (String season : seasons) {
                    if(MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(season)){
                        players.addAll(fifaPlayerDBRepository.getPlayersWithMostGoalsInSeasonOldFormat(season, competition, competitionPhase1, competitionPhase2));
                    } else {
                        players.addAll(fifaPlayerDBRepository.getPlayersWithMostGoalsInSeasonNewFormat(season, competition, competitionPhase1, competitionPhase2));
                    }
                }
                break;
        }

        Collections.sort(players, Comparator.comparing(FifaPlayerWithRecord::getRecordEventCount).reversed());

        return players.stream().limit(100).collect(Collectors.toList());
    }
}
