package com.javasampleapproach.springrest.mysql.model.seasons;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayOffStage {
    // may be reworked as map
    List<PlayOffRound> playOffRounds;

    public PlayOffStage() {
        this.playOffRounds = new ArrayList<>();
    }
}
