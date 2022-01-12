package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FifaPlayer {

    private String name;
    private Integer cardsTotal = 0;
    private int redCards = 0;
    private int yellowCards = 0;
    //todo as map, for now I only need to get length of this set
    private Set<String> cardsByTeams= new HashSet<>();

    // TODO - add cards per team + per season
}
