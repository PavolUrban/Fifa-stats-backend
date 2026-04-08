package com.javasampleapproach.springrest.mysql.model.seasons;

import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import lombok.Data;

import java.util.List;

@Data
public class PlayOffDoubleMatch {
    private String qualifiedTeam;
    private Long qualifiedTeamId;
    private int qualifiedTeamGoals;
    private String nonQualifiedTeam;
    private Long nonQualifiedTeamId;
    private int nonQualifiedTeamGoals;
    private String qualifiedPlayer;
    private List<MatchesDTO> homeAwayMatch;
}