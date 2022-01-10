package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@NoArgsConstructor
@AllArgsConstructor
public class RecordsInMatchesLite {


    //rim.numberofgoalsforoldformat, rim.minuteOfRecord, rim.TYpeOfRecord, rim.teamname


    private long playerId;



    private String teamName;

    private String typeOfRecord;

    private Integer minuteOfRecord;

    private Integer numberOfGoalsForOldFormat;


    public String getTeamName(){
        return this.teamName;
    };

    public String getTypeOfRecord(){
        return this.typeOfRecord;
    }

    public Integer getMinuteOfRecord(){
        return this.minuteOfRecord;
    }

    public Integer getNumberOfGoalsForOldFormat(){
        return this.numberOfGoalsForOldFormat;
    }

    public Long getPlayerId(){
        return this.playerId;
    }



}
