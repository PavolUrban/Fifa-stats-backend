package com.javasampleapproach.springrest.mysql.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
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
}
