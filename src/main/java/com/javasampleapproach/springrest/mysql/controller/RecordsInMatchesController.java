package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.services.RecordsInMatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/recordsInMatches")
public class RecordsInMatchesController {

    @Autowired
    RecordsInMatchesService recordsInMatchesService;

    @GetMapping("/getGoalDistribution/{teamName}")
    public GoalDistributionModel getGoalDistributionForTeam(@PathVariable("teamName") String teamName) {
        return recordsInMatchesService.getGoalDistributionForTeam(teamName);
    }

}
