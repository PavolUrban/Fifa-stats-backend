package com.javasampleapproach.springrest.mysql.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "recordsinmatches")
public class RecordsInMatches {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "playerid")
    private long playerId;

    @Column(name = "matchid")
    private int matchId;

    @Column(name = "teamname")
    private String teamName;

    @Column(name = "typeofrecord")
    private String typeOfRecord;

    @Column(name = "minuteofrecord")
    private Integer minuteOfRecord;

    @Column(name = "numberofgoalsforoldformat")
    private Integer numberOfGoalsForOldFormat;

    public RecordsInMatches(long playerId, int matchId, String teamName, String typeOfRecord, Integer minuteOfRecord, Integer numberOfGoalsForOldFormat){
        this.playerId = playerId;
        this.matchId = matchId;
        this.teamName = teamName;
        this.typeOfRecord = typeOfRecord;
        this.minuteOfRecord = minuteOfRecord;
        this.numberOfGoalsForOldFormat = numberOfGoalsForOldFormat;
    }

    public RecordsInMatches(long playerId, int matchId, String teamName, String typeOfRecord, int numberOfGoalsForOldFormat){
        this.playerId = playerId;
        this.matchId = matchId;
        this.teamName = teamName;
        this.typeOfRecord = typeOfRecord;
        this.numberOfGoalsForOldFormat = numberOfGoalsForOldFormat;
    }

    public RecordsInMatches(long playerId, int matchId, String teamName, int minuteOfRecord, String typeOfRecord){
        this.playerId = playerId;
        this.matchId = matchId;
        this.teamName = teamName;
        this.typeOfRecord = typeOfRecord;
        this.minuteOfRecord = minuteOfRecord;
    }
}
