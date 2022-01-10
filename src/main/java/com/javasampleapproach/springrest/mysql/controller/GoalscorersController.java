package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.FifaPlayer;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.PlayerGoalscorer;
import com.javasampleapproach.springrest.mysql.model.RecordsInMatchesLite;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.*;
import java.util.stream.Collectors;

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

        List<RecordsInMatches> allGoals = recordsInMatchesRepository.getRecordsByCompetition(competition, teamName,"G", "Penalty");

        List<Long> allIds = allGoals.stream().map(goal-> goal.getPlayerId()).collect(Collectors.toList());
        System.out.println("allIds size "+allIds.size());
        List<Long> distinctIDs = allIds.stream().distinct().collect(Collectors.toList());
        System.out.println("distinct Ids size "+distinctIDs.size());
        Iterable<FifaPlayerDB> allPlayers = fifaPlayerDBRepository.findByIdIn(distinctIDs);

        List<Goalscorer> allGoalscorers = new ArrayList<>();
        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allGoals.stream().filter(goals-> goals.getPlayerId() == player.getId()).collect(Collectors.toList());
            Goalscorer goalscorer = new Goalscorer();
            goalscorer.setName(player.getPlayerName());
            goalscorer.setTotalGoalsCount(0);

            Set<String> teamsPlayerScoredFor = new HashSet<>();

            recordsRelatedToPlayer.forEach(record->{
                teamsPlayerScoredFor.add(record.getTeamName());
                if(record.getMinuteOfRecord()!=null){
                    goalscorer.setTotalGoalsCount(goalscorer.getTotalGoalsCount() + 1);
                } else {
                    goalscorer.setTotalGoalsCount(goalscorer.getTotalGoalsCount() + record.getNumberOfGoalsForOldFormat());
                }
            });

            goalscorer.setNumberOfTeamsPlayerScoredFor(teamsPlayerScoredFor.size());
            allGoalscorers.add(goalscorer);
        });


        allGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));


        return allGoalscorers;
    }
}
