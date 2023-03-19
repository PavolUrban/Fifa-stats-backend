package com.javasampleapproach.springrest.mysql.model.matches;

import lombok.Data;

@Data
public class FilteredMatchesRequest {
    String competition;
    String competitionPhase;
    String season;
    String teamName;
}
