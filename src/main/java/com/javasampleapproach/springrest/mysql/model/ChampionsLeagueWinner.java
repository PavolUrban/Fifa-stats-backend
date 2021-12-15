package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionsLeagueWinner {

    private String teamName;
    private String runnerUp;
    private String playerName;
    private String season;
    FileModel teamLogo;
    FileModel runnerUpLogo;

//todo use this for EL and CL in one row
//    private String teamNameEL;
//    private String teamNameCL;
//    private String playerNameEL;
//    private String playerNameCL;
//    FileModel teamLogoEL;
//    FileModel teamLogoCL;
}
