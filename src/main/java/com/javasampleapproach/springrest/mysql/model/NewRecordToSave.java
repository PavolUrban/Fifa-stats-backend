package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewRecordToSave {
    int matchId;
    int teamId;
    int playerId;
    String teamName; // todo remove once DB is prepared to use ID
    String recordType;
    String recordSignature;
    int numericDetail;// for new fifa it will contain minute, for old fifa we don't have minutes, but we can save that player score 4 goals
}