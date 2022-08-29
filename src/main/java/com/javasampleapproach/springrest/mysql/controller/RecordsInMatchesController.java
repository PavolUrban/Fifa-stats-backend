package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.model.TimeRangeElement;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
                TimeRangeElement tre;
                if(record.getMinuteOfRecord() > 90) {
                    System.out.println("pozor na tento " + record.getId()); // zatial davam k poslednemu
                    tre = gdm.getAllRanges().get(gdm.getAllRanges().size() - 1);
                } else {
                    tre = gdm.getAllRanges().stream().filter(range -> record.getMinuteOfRecord() <= range.getUpBorder() && record.getMinuteOfRecord() >= range.getLowBorder()).findFirst().orElse(null);
                }

                if(record.getTeamName().equalsIgnoreCase(teamName)){
                    tre.setNumberOfGoals(tre.getNumberOfGoals() + 1);
                } else {
                    tre.setNumberOfConcededGoals(tre.getNumberOfConcededGoals() + 1);
                }
            }
        });

        // Prepare arrays to display in GUI
        gdm.getAllRanges().forEach(range -> {
            gdm.getConcededGoalsList().add(range.getNumberOfConcededGoals());
            gdm.getScoredGoalsList().add(range.getNumberOfGoals());
            gdm.getMinutesAsLabels().add(range.getLabel());
        });

        return gdm;
    }

}
