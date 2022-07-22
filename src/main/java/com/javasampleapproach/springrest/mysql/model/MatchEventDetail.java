package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchEventDetail {
    private String playerName;
    private String recordType;
    private String teamName;
    private Integer minute; // pozor funguje len na fify kde su minuty - musim to upravit
    private String descriptionTimeOrCount;
}
