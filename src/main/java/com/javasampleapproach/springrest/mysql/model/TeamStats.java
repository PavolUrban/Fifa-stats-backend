package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TeamStats {
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;
    private int goalsScored = 0;
    private int goalsConceded = 0;
    private int goalDiff = 0;
    private int matchesCount = 0;
    private int finalMatchesCount = 0;
    private int titlesCount = 0;
    private int runnersUpCount = 0;

    private Set<String> seasonsList = new HashSet<>();

    public void incrementWins(int number){
        this.wins = this.wins + number;
    }

    public void incrementDraws(int number){
        this.draws = this.draws + number;
    }

    public void incrementLosses(int number){
        this.losses = this.losses + number;
    }

    public void incrementGoalsScored(int number){
        this.goalsScored = this.goalsScored + number;
    }

    public void incrementGoalsConceded(int number){
        this.goalsConceded = this.goalsConceded + number;
    }

    public void incrementMatchesCount(int number){
        this.matchesCount = this.matchesCount + number;
    }

    public void incrementFinalMatchesCount(int number){
        this.finalMatchesCount = this.finalMatchesCount + number;
    }

    public void incrementTitlesCount(int number){
        this.titlesCount = this.titlesCount + number;
    }

    public void incrementRunnersUpCount(int number){
        this.runnersUpCount = this.runnersUpCount + number;
    }

    public void calculateGoalDiff(){
        this.goalDiff = this.goalsScored - this.goalsConceded;
    }
}
