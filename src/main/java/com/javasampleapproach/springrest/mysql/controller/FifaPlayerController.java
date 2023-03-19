package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.model.FifaPlayerStatsPerSeasonWrapper;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerWithRecord;
import com.javasampleapproach.springrest.mysql.model.fifa_player.FifaPlayerCoreDTO;
import com.javasampleapproach.springrest.mysql.model.individual_records.IndividualRecordsRequest;
import com.javasampleapproach.springrest.mysql.services.FifaPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/fifaPlayer")
public class FifaPlayerController {

    @Autowired
    FifaPlayerService fifaPlayerService;

    @GetMapping("/getPlayersByName/{nameSubstring}")
    public List<FifaPlayerCoreDTO> getPlayerByName(@PathVariable("nameSubstring") String nameSubstring) {
        return fifaPlayerService.getPlayersByName(nameSubstring);
    }

    @GetMapping("/getStatsForPlayer/{playerId}")
    public FifaPlayerStatsPerSeasonWrapper getStatsForPlayer(@PathVariable("playerId") Long playerId) {
        return fifaPlayerService.getPlayerStats(playerId);
    }

    @PostMapping("/getPlayersWithRecord")
    public List<FifaPlayerWithRecord> getPlayersWithRecord(@RequestBody IndividualRecordsRequest recordsRequest){
        return fifaPlayerService.getPlayersWithRecord(recordsRequest);
    }
}
