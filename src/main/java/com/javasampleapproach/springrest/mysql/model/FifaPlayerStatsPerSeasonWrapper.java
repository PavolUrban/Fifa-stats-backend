package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class FifaPlayerStatsPerSeasonWrapper {
    private String playerName;
    private List<FifaPlayerStatsPerSeason> playerStatsPerSeason;

    public FifaPlayerStatsPerSeasonWrapper() {
        this.playerStatsPerSeason = new ArrayList<>();
    }
}
