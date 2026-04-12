package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MatchStatsDTO {

    private LocalDate matchDate;
    private String venue;

    private Integer totalShotsHome;
    private Integer totalShotsAway;

    private Integer shotsOnTargetHome;
    private Integer shotsOnTargetAway;

    /** Possession in percent, e.g. 55.5 */
    private Double possessionHome;
    private Double possessionAway;

    private Integer foulsHome;
    private Integer foulsAway;

    private Double expectedGoalsHome;
    private Double expectedGoalsAway;
}