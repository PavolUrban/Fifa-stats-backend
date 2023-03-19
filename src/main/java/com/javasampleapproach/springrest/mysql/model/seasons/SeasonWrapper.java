package com.javasampleapproach.springrest.mysql.model.seasons;

import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import lombok.Data;

import java.util.List;

@Data
public class SeasonWrapper {
    private GroupStage groupStage;
    private PlayOffStage playOffs;
    private MatchesDTO finalMatch;
    private List<Goalscorer> topGoalscorersGroupStage;
    private List<Goalscorer> topGoalsScorersPlayOffs;
    private List<Goalscorer> topGoalsScorersTotal;
}
