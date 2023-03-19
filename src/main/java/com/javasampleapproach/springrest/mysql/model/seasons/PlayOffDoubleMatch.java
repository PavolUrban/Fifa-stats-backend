package com.javasampleapproach.springrest.mysql.model.seasons;

import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import lombok.Data;

import java.util.List;

@Data
public class PlayOffDoubleMatch {
    private String qualifiedTeam;
    private int qualifiedTeamGoals;
    private String nonQualifiedTeam;
    private int nonQualifiedTeamGoals;
    private String qualifiedPlayer;
    private List<MatchesDTO> homeAwayMatch;
}