package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.model.NewRecordToSave;
import com.javasampleapproach.springrest.mysql.model.TimeRangeElement;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordsInMatchesService {

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    public GoalDistributionModel getGoalDistributionForTeam(Integer teamId) {
        List<RecordsInMatches> records = recordsInMatchesRepository.getScoredAndConcededGoalsByTeam(teamId);
        GoalDistributionModel gdm = new GoalDistributionModel();
        records.forEach(record-> {

            // old fifa check - no minutes
            if(record.getMinuteOfRecord() == null) {
                if (record.getTeam().getId() == teamId){
                    gdm.setScoredGoalsUnknownTime(gdm.getScoredGoalsUnknownTime() + 1);
                } else {
                    gdm.setConcededGoalsUnknownTime(gdm.getConcededGoalsUnknownTime() + 1);
                }
            } else {
                TimeRangeElement tre;
                // todo goals scored in 90+ mins are added to last interval
                if(record.getMinuteOfRecord() > 90) {
                    tre = gdm.getAllRanges().get(gdm.getAllRanges().size() - 1);
                } else {
                    tre = gdm.getAllRanges().stream().filter(range -> record.getMinuteOfRecord() <= range.getUpBorder() && record.getMinuteOfRecord() >= range.getLowBorder()).findFirst().orElse(null);
                }

                if(record.getTeam().getId()==  teamId){
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

    // todo recordtypes use as array
    public List<RecordsInMatches> getRecordsByCompetition(String competition, Integer teamId, String recordType1, String recordType2){
        return recordsInMatchesRepository.getRecordsByCompetition(null, null, competition, teamId, MyUtils.RECORD_TYPE_YELLOW_CARD, MyUtils.RECORD_TYPE_RED_CARD);
    }

    public void saveNewRecord(RecordsInMatches newRecordToSave){
        recordsInMatchesRepository.save(newRecordToSave);
    }
}
