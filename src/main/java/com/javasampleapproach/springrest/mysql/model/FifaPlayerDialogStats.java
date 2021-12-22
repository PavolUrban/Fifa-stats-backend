package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class FifaPlayerDialogStats {
    private String name;

    //todo per competition
    private List<FifaPlayerStatsPerSeason> playerStatsPerSeason;

    public FifaPlayerDialogStats(){
        this.playerStatsPerSeason = new ArrayList<>();
    }
}
