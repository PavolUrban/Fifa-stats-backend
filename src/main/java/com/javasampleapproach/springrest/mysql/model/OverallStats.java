package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class OverallStats {
    // Checked - this class is fully used

    /* numbers in arrays order:
       [0] - group stage
       [1] - playoffs
       [2] - total
     */
    private List<H2HPlayers> h2hPlayers = new ArrayList<>();
    private List<Integer> matchesCount = new ArrayList<>();
    private List<Integer> goalsCount = new ArrayList<>();
    private List<Integer> yellowCardsCount = new ArrayList<>();
    private List<Integer> redCardsCount = new ArrayList<>();
    private String winnerTeam;
    private String winnerPlayer;
}