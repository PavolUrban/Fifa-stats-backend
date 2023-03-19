package com.javasampleapproach.springrest.mysql.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "recordsinmatches")
public class RecordsInMatches {

    //rework this and use relations, add constraints
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="matchId", nullable=false)
    private Matches match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="playerId", nullable=false)
    private FifaPlayerDB player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerTeamId", nullable=false)
    private Team playerTeam; // for which team player played in match

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name="teamRecordId", nullable=false)
    private Team team; // for which team player made a contribution (e.g. own goal he would score for opposition team)

    @Column(name = "typeOfRecord")
    private String typeOfRecord;

    @Column(name = "minuteOfRecord")
    private Integer minuteOfRecord;
}
