package com.javasampleapproach.springrest.mysql.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchesDTO {
    private long id;
    private String hometeam;
    private String awayteam;
    private int scorehome;
    private int scoreaway;
    private String winner;
    private String season;
    private String playerH;
    private String playerA;
    private String competition;
    private String competitionPhase;
    private String winnerPlayer;
}
