package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchEventDetail {
    private String playerName;
    private String recordType;
    private Integer teamRecordId;
    private int minute;
    private String minuteLabel;
   // private int recordCount;
}
