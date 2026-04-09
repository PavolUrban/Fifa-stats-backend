package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchesPerTeam {

    private TeamDto team;
    private int count;


    public void incrementCount() {
        this.count++;
    }
}
