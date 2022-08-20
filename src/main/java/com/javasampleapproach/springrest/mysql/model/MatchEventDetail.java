package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchEventDetail {
    private String playerName;
    private String recordType;
    private String teamName;
    private String minute;
    private int recordCount;
    private String typeOfFormat;
}
