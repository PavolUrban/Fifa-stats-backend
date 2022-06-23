package com.javasampleapproach.springrest.mysql.controller;

import Utils.NewestGoalscorersCalculator;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/goalscorers")
public class GoalscorersController {
    //todo once this is finished - remove all remaining goalscorers function, this should be only class working with goalscorers stats
    // todo in db matches reflect changes- do not store here goals,yc, rc

    // todo idea to future - this should be stored somewhere and only after refresh or something it would be recalculated

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    @GetMapping("/getAllGoalScorers/{competition}/{teamname}")
    public  List<Goalscorer> getAllGoalscorers(@PathVariable("competition") String competition, @PathVariable("teamname") String teamName)
    {
        if(teamName.equalsIgnoreCase("null")){
            teamName = null;
        }

        if(competition.equalsIgnoreCase("Total")){
            competition = null;
        }

        List<RecordsInMatches> allGoals = recordsInMatchesRepository.getRecordsByCompetition(null, null, competition, teamName,"G", "Penalty");

        NewestGoalscorersCalculator ngc = new NewestGoalscorersCalculator(fifaPlayerDBRepository);
        return ngc.getGoalscorers(allGoals);
    }
}
