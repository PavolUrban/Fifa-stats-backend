package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.model.GoalInMinuteModel;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/recordsInMatches")
public class RecordsInMatchesController {

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    @GetMapping("/getGoalDistribution/{teamName}")
    public GoalDistributionModel getAllCustomers(@PathVariable("teamName") String teamName) {

        List<RecordsInMatches> records = recordsInMatchesRepository.getScoredAndConcededGoalsByTeam(teamName);
        GoalDistributionModel gdm = new GoalDistributionModel();
        records.forEach(record-> {
            if(record.getNumberOfGoalsForOldFormat() != null) {
                if(record.getTeamName().equalsIgnoreCase(teamName)){
                    gdm.setScoredGoalsUnknownTime(gdm.getScoredGoalsUnknownTime() + record.getNumberOfGoalsForOldFormat());
                } else {
                    gdm.setConcededGoalsUnknownTime(gdm.getConcededGoalsUnknownTime() + record.getNumberOfGoalsForOldFormat());
                }
            } else {
                GoalInMinuteModel gimm;
                if(record.getMinuteOfRecord() > 90) {
                    gimm = gdm.getGoalsInMinutesCount().stream().filter(model-> model.getMinuteLabel().equalsIgnoreCase("90+")).findFirst().orElse(null);
                } else {
                    gimm = gdm.getGoalsInMinutesCount().stream().filter(model-> model.getMinuteLabel().equalsIgnoreCase(record.getMinuteOfRecord().toString())).findFirst().orElse(null);
                }


                System.out.println("totot je on ");
                System.out.println(gimm);
                if(record.getTeamName().equalsIgnoreCase(teamName)){
                    gimm.setScoredGoalsCount(gimm.getScoredGoalsCount() + 1);
                } else {
                    gimm.setConcededGoalsCount(gimm.getConcededGoalsCount() + 1);
                }
            }
        });

        return gdm;
    }
}
