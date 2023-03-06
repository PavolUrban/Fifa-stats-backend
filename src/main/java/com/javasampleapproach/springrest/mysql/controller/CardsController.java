package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.model.PlayerWithCards;
import com.javasampleapproach.springrest.mysql.model.records_in_matches.RecordsInMatchesRequest;
import com.javasampleapproach.springrest.mysql.services.FifaPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/cards")
public class CardsController {

    @Autowired
    FifaPlayerService fifaPlayerService;

    // todo merge with getGoalscorers

    @PostMapping("/getAllCards")
    public List<PlayerWithCards> getCardsRecords(@RequestBody RecordsInMatchesRequest recordsInMatchesRequest) {
        return fifaPlayerService.getCards(recordsInMatchesRequest.getCompetition(), recordsInMatchesRequest.getTeamId());
    }
}
