package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.model.NewRecordToSave;
import com.javasampleapproach.springrest.mysql.services.RecordsInMatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/recordsInMatches")
public class RecordsInMatchesController {

    @Autowired
    RecordsInMatchesService recordsInMatchesService;

    @GetMapping("/getGoalDistribution/{teamId}")
    public GoalDistributionModel getGoalDistributionForTeam(@PathVariable("teamId") Integer teamId) {
        return recordsInMatchesService.getGoalDistributionForTeam(teamId);
    }

    @PostMapping("/saveNewRecord")
    public void saveNewRecordInMatch(@RequestBody RecordsInMatches newRecordToSave){
        recordsInMatchesService.saveNewRecord(newRecordToSave);
    }
}
