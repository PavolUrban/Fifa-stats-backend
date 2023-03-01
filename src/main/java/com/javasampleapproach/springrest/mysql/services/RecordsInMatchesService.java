package com.javasampleapproach.springrest.mysql.services;

import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.model.TimeRangeElement;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class RecordsInMatchesService {

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    public GoalDistributionModel getGoalDistributionForTeam(@PathVariable("teamName") String teamName) {
        List<RecordsInMatches> records = recordsInMatchesRepository.getScoredAndConcededGoalsByTeam(teamName);
        GoalDistributionModel gdm = new GoalDistributionModel();
        records.forEach(record-> {

            // old fifa check - no minutes
            if(record.getNumberOfGoalsForOldFormat() != null) {
                if(record.getTeamName().equalsIgnoreCase(teamName)){
                    gdm.setScoredGoalsUnknownTime(gdm.getScoredGoalsUnknownTime() + record.getNumberOfGoalsForOldFormat());
                } else {
                    gdm.setConcededGoalsUnknownTime(gdm.getConcededGoalsUnknownTime() + record.getNumberOfGoalsForOldFormat());
                }
            } else {
                TimeRangeElement tre;
                // todo goals scored in 90+ mins are added to last interval
                if(record.getMinuteOfRecord() > 90) {
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

        gdm.getAllRanges().forEach(range -> {
            gdm.getConcededGoalsList().add(range.getNumberOfConcededGoals());
            gdm.getScoredGoalsList().add(range.getNumberOfGoals());
            gdm.getMinutesAsLabels().add(range.getLabel());
        });

        return gdm;
    }

    public List<RecordsInMatches> findByMatchIdOrderByMinuteOfRecord(int matchId) {
        return recordsInMatchesRepository.findByMatchIdOrderByMinuteOfRecord(matchId);
    }
}
