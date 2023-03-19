package com.javasampleapproach.springrest.mysql.model.seasons;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayOffRound {
    private String roundName;
    private List<PlayOffDoubleMatch> duels;
    private H2HV2 h2hPlayers;

    public PlayOffRound() {
        duels = new ArrayList<>();
    }
}
