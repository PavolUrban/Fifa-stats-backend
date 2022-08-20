package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamForTrophyRoom {
    private String teamName;
    private int winCountCL = 0;
    private int winCountEL = 0;
    private Integer winCountTotal = 0;
    private int runnersUpCL = 0;
    private int runnersUpEL = 0;
    private int runnersUpTotal = 0;

    public TeamForTrophyRoom(String teamName){
        this.teamName = teamName;
    }
}
