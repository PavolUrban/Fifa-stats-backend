package com.javasampleapproach.springrest.mysql.model.records_in_matches;

import lombok.Data;

@Data
public class RecordsInMatchesRequest {
    String competition;
    Long teamId;
}

// todo rename to matchesEvents or somethign like that, including db
