package com.javasampleapproach.springrest.mysql.model.records_in_matches;

import lombok.Data;

@Data
public class RecordsInMatchesDTO {
    long matchId;
    long playerId;
    long playerTeamId;
    long teamRecordId;
    String typeOfRecord;
    Integer minuteOfRecord;
}
