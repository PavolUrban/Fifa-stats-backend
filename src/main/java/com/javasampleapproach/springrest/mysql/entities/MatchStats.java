package com.javasampleapproach.springrest.mysql.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "match_stats")
public class MatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Matches match;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "venue")
    private String venue;

    @Column(name = "total_shots_home")
    private Integer totalShotsHome;

    @Column(name = "total_shots_away")
    private Integer totalShotsAway;

    @Column(name = "shots_on_target_home")
    private Integer shotsOnTargetHome;

    @Column(name = "shots_on_target_away")
    private Integer shotsOnTargetAway;

    /** Possession in percent, e.g. 55.5 */
    @Column(name = "possession_home")
    private Double possessionHome;

    @Column(name = "possession_away")
    private Double possessionAway;

    @Column(name = "fouls_home")
    private Integer foulsHome;

    @Column(name = "fouls_away")
    private Integer foulsAway;

    @Column(name = "expected_goals_home")
    private Double expectedGoalsHome;

    @Column(name = "expected_goals_away")
    private Double expectedGoalsAway;
}