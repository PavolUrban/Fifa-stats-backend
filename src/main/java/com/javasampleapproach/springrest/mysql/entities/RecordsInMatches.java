package com.javasampleapproach.springrest.mysql.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "recordsinmatches")
public class RecordsInMatches {

    //rework this and use relations, add constraints
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "matchId")
    private int matchId;

    @Column(name = "playerId")
    private long playerId;

    @Column(name = "playerTeamId")
    private Integer playerTeamId; // for which team player played in match

    @Column(name = "teamRecordId")
    private Integer teamRecordId; // for which team player made a contribution (e.g. own goal he would score for opposition team)

    @Column(name = "typeOfRecord")
    private String typeOfRecord;

    @Column(name = "minuteOfRecord")
    private Integer minuteOfRecord;
}
