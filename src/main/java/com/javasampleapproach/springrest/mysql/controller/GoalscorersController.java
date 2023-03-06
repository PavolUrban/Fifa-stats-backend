package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.records_in_matches.RecordsInMatchesRequest;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
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
@RequestMapping("/goalscorers")
public class GoalscorersController {
    //todo once this is finished - remove all remaining goalscorers function, this should be only class working with goalscorers stats
    // todo in db matches reflect changes- do not store here goals,yc, rc

    // todo idea to future - this should be stored somewhere and only after refresh or something it would be recalculated

    @Autowired
    FifaPlayerService fifaPlayerService;

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    // todo merge with getCards
    @PostMapping("/getAllGoalScorers")
    public List<Goalscorer> getAllGoalscorers(@RequestBody RecordsInMatchesRequest recordsInMatchesRequest) {
        List<RecordsInMatches> allGoals = recordsInMatchesRepository.getRecordsByCompetition(null, null, recordsInMatchesRequest.getCompetition(), recordsInMatchesRequest.getTeamId(), "G", "Penalty");
        return fifaPlayerService.getGoalscorers(allGoals);
    }
}
