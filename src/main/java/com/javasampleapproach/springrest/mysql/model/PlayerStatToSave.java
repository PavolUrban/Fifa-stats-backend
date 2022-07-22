package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatToSave {
    String playerName;
    String recordType;
    String recordSignature;
    String details;
    String teamName;
    int matchId;
}
