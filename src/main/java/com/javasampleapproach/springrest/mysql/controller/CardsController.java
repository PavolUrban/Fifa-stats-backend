package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import Utils.NewestCardsCalculator;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.FifaPlayer;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/cards")
public class CardsController {

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    @GetMapping("/getAllCards/{competition}/{teamname}")
    public List<FifaPlayer> getCardsRecords(@PathVariable("competition") String competition, @PathVariable("teamname") String teamName)
    {
        if(teamName.equalsIgnoreCase("null")) {
            teamName = null;
        }

        if(competition.equalsIgnoreCase("Total")) {
            competition = null;
        }

        List<RecordsInMatches> allCards = recordsInMatchesRepository.getRecordsByCompetition(null, null, competition, teamName, MyUtils.RECORD_TYPE_YELLOW_CARD, MyUtils.RECORD_TYPE_RED_CARD);
        NewestCardsCalculator ngc = new NewestCardsCalculator(fifaPlayerDBRepository);
        return ngc.getCards(allCards);
    }
}
