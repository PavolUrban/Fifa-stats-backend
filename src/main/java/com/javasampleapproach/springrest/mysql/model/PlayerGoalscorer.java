package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerGoalscorer {
    private String name;
    private Integer goalsCount = 0;
    private Integer numberOfTeamsPlayerScoredFor = 0;
//
//    private String name;
//    private Integer totalGoalsCount;
//    private Map<String, Integer> goalsByTeams;
//    private int numberOfTeamsPlayerScoredFor;
}
