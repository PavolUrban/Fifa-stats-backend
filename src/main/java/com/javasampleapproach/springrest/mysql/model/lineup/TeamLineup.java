package com.javasampleapproach.springrest.mysql.model.lineup;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TeamLineup {

    private long teamId;
    private String teamName;

    /** Starting eleven */
    private List<LineupPlayerDTO> players = new ArrayList<>();

    /** Substitutes who came on during the match */
    private List<LineupPlayerDTO> substitutes = new ArrayList<>();
}

