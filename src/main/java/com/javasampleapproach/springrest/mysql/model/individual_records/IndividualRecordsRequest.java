package com.javasampleapproach.springrest.mysql.model.individual_records;

import lombok.Data;

@Data
public class IndividualRecordsRequest {
    String recordType;
    String competition;
    String competitionPhase;
}
