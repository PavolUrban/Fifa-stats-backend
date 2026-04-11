package com.javasampleapproach.springrest.mysql.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_in_match")
public class PlayerInMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Matches match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private FifaPlayerDB player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    /** true = starter, false = substitute */
    @Column(name = "is_starter", nullable = false)
    private boolean starter;

    @Column(name = "rating")
    private Double rating;

    /** For starters: minute they were substituted off.
     *  For substitutes: minute they came on. */
    @Column(name = "substitution_minute")
    private Integer substitutionMinute;

    /** Player position in this match (e.g. GK, CB, CAM), optional */
    @Column(name = "position")
    private String position;

    /** For starters only: the substitute player who replaced them. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_player_id")
    private FifaPlayerDB replacedBy;

    /** For substitutes only: the starter they replaced. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_player_id")
    private FifaPlayerDB replacedPlayer;
}

