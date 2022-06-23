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
}
