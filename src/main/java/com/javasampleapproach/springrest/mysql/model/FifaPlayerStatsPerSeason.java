package com.javasampleapproach.springrest.mysql.model;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FifaPlayerStatsPerSeason {
    String seasonName;
    String teamname; // todo for now working only for one team + logo - others are ignored
    int goalsCount = 0;
    int yellowCardsCount = 0;
    int redCardsCount = 0;
    Set<Matches> allMatches = new HashSet<>();
}
