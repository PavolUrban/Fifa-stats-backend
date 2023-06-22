package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.GoalDistributionModel;
import com.javasampleapproach.springrest.mysql.model.TimeRangeElement;
import com.javasampleapproach.springrest.mysql.model.records_in_matches.RecordsInMatchesDTO;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RecordsInMatchesService {

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    @Autowired
    MatchesService matchesService;

    @Autowired
    FifaPlayerService fifaPlayerService;

    @Autowired
    TeamService teamService;

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

    // todo recordtypes use as array, send record type as param
    public List<RecordsInMatches> getRecordsByCompetition(final String competitionPhase, final String season,final String competition,final Long teamId, final List<String> recordTypes){
        return recordsInMatchesRepository.getRecordsByCompetition(competitionPhase, season, competition, teamId, recordTypes);
    }

    public void saveNewRecord(RecordsInMatchesDTO newRecordDTO){
        final Matches match = matchesService.findMatchById(newRecordDTO.getMatchId());
        final Team playerTeam = teamService.findById(newRecordDTO.getPlayerTeamId());
        final Team recordTeam = teamService.findById(newRecordDTO.getTeamRecordId());
        final FifaPlayerDB player = fifaPlayerService.findPlayerById(newRecordDTO.getPlayerId());
        final RecordsInMatches newRecord = new RecordsInMatches();
        newRecord.setMatch(match);
        newRecord.setPlayerTeam(playerTeam);
        newRecord.setTeam(recordTeam);
        newRecord.setPlayer(player);
        newRecord.setTypeOfRecord(newRecordDTO.getTypeOfRecord());
        newRecord.setMinuteOfRecord(newRecordDTO.getMinuteOfRecord());

        recordsInMatchesRepository.save(newRecord);
    }
}
