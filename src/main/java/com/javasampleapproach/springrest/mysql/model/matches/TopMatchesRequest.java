package com.javasampleapproach.springrest.mysql.model.matches;

import lombok.Data;

@Data
public class TopMatchesRequest {
    String recordType;
    String selectedPlayer; // KOTLIK or PAVOL JAY
    String selectedCompetition;
    Long teamId;
}