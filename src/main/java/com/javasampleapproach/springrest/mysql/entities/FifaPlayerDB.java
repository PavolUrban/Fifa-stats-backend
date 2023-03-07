package com.javasampleapproach.springrest.mysql.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "fifaplayer")
public class FifaPlayerDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "playername")
    private String playerName;

    @Column(name = "playerposition")
    private String playerPosition;

    @Column(name = "additionalinfo")
    private String additionalInfo;

    @OneToMany(fetch = FetchType.EAGER, mappedBy="player")
    private Set<RecordsInMatches> recordsInMatches;

    //@EqualsAndHashCode(exclude = {"recordsInMatches"})
}
