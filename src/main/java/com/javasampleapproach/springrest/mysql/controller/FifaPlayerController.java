package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.model.*;
import com.javasampleapproach.springrest.mysql.model.individual_records.IndividualRecordsRequest;
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
    FifaPlayerService fifaPlayerService;


    @GetMapping("/getPlayersByName/{nameSubstring}")
    public List<FifaPlayerDB> getPlayerByName(@PathVariable("nameSubstring") String nameSubstring) {
        return fifaPlayerService.getPlayersByName(nameSubstring);
    }


    // rework to use ID
    @GetMapping("/getStats/{playerName}")
    public FifaPlayerDialogStats getAllStatsForSpecifiedFifaPlayerNEWEST(@PathVariable("playerName") String playerName) {

        // TODO fix this
        // pozor vznika problem ked hrac prestupil v ramci sezony!! check haaland - 3g za salzburg + 2za dormund su zobrazene ako 5 za salzburg
        FifaPlayerDialogStats allStats = new FifaPlayerDialogStats();
        allStats.setName(playerName);
        System.out.println("hladam hraca menom " + playerName);
        FifaPlayerDB fifaPlayer = fifaPlayerDBRepository.findByPlayerName(playerName);

        System.out.println(fifaPlayer);

        fifaPlayer.getRecordsInMatches().forEach(records -> System.out.println(records.getMinuteOfRecord() + " " + records.getTypeOfRecord() + " " + records.getMatch().getId()));


//        List<PlayerStatsInSeason> allRecordsForCurrentPlayer = recordsInMatchesRepository.findRecordsRelatedToPlayer(fifaPlayer.getId());
//        System.out.println("toto este mam");
//        System.out.println(allRecordsForCurrentPlayer);
//
//        allRecordsForCurrentPlayer.forEach(a -> System.out.println(a.getPlayerTeamId() + " " + a.getSeason() + " " + a.getTypeOfRecord() + " "+ a.getTeamName()));
//        List<String> playersSeasonsList = allRecordsForCurrentPlayer.stream().map(stat-> stat.getSeason()).distinct().collect(Collectors.toList());
//
//        playersSeasonsList.forEach(season->{
//            List<PlayerStatsInSeason> recordsInCurrentSeason = allRecordsForCurrentPlayer.stream().filter(stat-> stat.getSeason().equalsIgnoreCase(season)).collect(Collectors.toList());
//            FifaPlayerStatsPerSeason playerStatsPerSeason = new FifaPlayerStatsPerSeason();
//            playerStatsPerSeason.setSeasonName(season);
//
//            String teamname = recordsInCurrentSeason.stream().map(rec-> rec.getTeamName()).findFirst().orElse("Inspect this, it is not possible to have no teamname here");
//            playerStatsPerSeason.setTeamname(teamname);
//            Integer teamId = recordsInCurrentSeason.stream().map(rec-> rec.getPlayerTeamId()).findFirst().orElse(null);
//            playerStatsPerSeason.setTeamId(teamId);
//            recordsInCurrentSeason.forEach(singleRecord->{
//
//                if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_GOAL) || singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_PENALTY)){
//                    playerStatsPerSeason.setGoalsCount(playerStatsPerSeason.getGoalsCount() + 1);
//                } else if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_YELLOW_CARD)){
//                    playerStatsPerSeason.setYellowCardsCount(playerStatsPerSeason.getYellowCardsCount() + 1);
//                } else if(singleRecord.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_RED_CARD)){
//                    playerStatsPerSeason.setRedCardsCount(playerStatsPerSeason.getRedCardsCount() + 1);
//                }
//            });
//        System.out.println(playerStatsPerSeason);
//            allStats.getPlayerStatsPerSeason().add(playerStatsPerSeason);
//        });
//
//        FifaPlayerStatsPerSeason totalCount = new FifaPlayerStatsPerSeason();
//        totalCount.setSeasonName("Total");
//
//        allStats.getPlayerStatsPerSeason().forEach(season->{
//            totalCount.setGoalsCount(totalCount.getGoalsCount() + season.getGoalsCount());
//            totalCount.setYellowCardsCount(totalCount.getYellowCardsCount() + season.getYellowCardsCount());
//            totalCount.setRedCardsCount(totalCount.getRedCardsCount() + season.getRedCardsCount());
//        });
//
//        allStats.getPlayerStatsPerSeason().add(totalCount);

        return allStats;
    }


    @PostMapping("/getPlayersWithRecord")
    public List<FifaPlayerWithRecord> getPlayersWithRecord(@RequestBody IndividualRecordsRequest recordsRequest){
        return fifaPlayerService.getPlayersWithRecord(recordsRequest);
    }
}
