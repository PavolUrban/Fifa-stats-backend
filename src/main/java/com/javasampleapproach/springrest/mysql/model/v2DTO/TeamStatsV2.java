package com.javasampleapproach.springrest.mysql.model.v2DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamStatsV2 {
    public int wins;
    public int losses;
    public int draws;
    public int goalsScored;
    public int goalsConceded;
    public int goalDiff;
    public int matchesCount;
    public int finalMatchesCount;
    public int titlesCount;
    public int runnersUpCount;
    public long yellowCards;
    public long redCards;
    public long penaltyGoals;
    @Builder.Default
    public Set<String> seasonsList = new HashSet<>();

}
