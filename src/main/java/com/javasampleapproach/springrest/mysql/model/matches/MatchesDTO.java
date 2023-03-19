package com.javasampleapproach.springrest.mysql.model.matches;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchesDTO {
    private Long id;
    private String homeTeam;
    private Long idHomeTeam;
    private String awayTeam;
    private Long idAwayTeam;
    private int scorehome;
    private int scoreaway;
    private long winnerId;
    private String season;
    private String playerH;
    private String playerA;
    private String competition;
    private String competitionPhase;
    private String winnerPlayer;
}
