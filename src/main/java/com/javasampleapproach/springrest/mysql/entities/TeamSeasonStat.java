package com.javasampleapproach.springrest.mysql.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "team_season_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamSeasonStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 1. IDENTIFIKÁTORY ---
    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "season", nullable = false)
    private String season; // napr. "2023/2024"

    @Column(name = "competition", nullable = false)
    private String competition; // "CL", "EL", alebo "ALL"

    // --- 2. ZÁKLADNÉ ZÁPASOVÉ ŠTATISTIKY ---
    @Column(name = "matches_played")
    private int matchesPlayed;

    @Column(name = "wins")
    private int wins;

    @Column(name = "draws")
    private int draws;

    @Column(name = "losses")
    private int losses;

    @Column(name = "points")
    private int points; // Výhry * 3 + Remízy * 1

    // --- 3. GÓLOVÉ ŠTATISTIKY ---
    @Column(name = "goals_scored")
    private int goalsScored;

    @Column(name = "goals_conceded")
    private int goalsConceded;

    @Column(name = "goal_difference")
    private int goalDifference; // goalsScored - goalsConceded

    // --- 4. POKROČILÉ ZÁPASOVÉ ŠTATISTIKY ---
    @Column(name = "clean_sheets")
    private int cleanSheets; // Zápasy s 0 obdržanými gólmi

    @Column(name = "failed_to_score")
    private int failedToScore; // Zápasy s 0 strelenými gólmi

    // --- 5. ŠPECIFICKÉ GÓLY ---
    @Column(name = "penalty_goals_scored")
    private int penaltyGoalsScored; // Premenené penalty (tímom)

    @Column(name = "penalty_goals_conceded")
    private int penaltyGoalsConceded; // Inkasované góly z penált (od súpera)

    @Column(name = "own_goals_benefited")
    private int ownGoalsBenefited; // Prijaté darčeky (vlastné góly súpera)

    @Column(name = "own_goals_committed")
    private int ownGoalsCommitted; // Rozdané darčeky (vlastné góly vlastných hráčov)

    // --- 6. DISCIPLÍNA ---
    @Column(name = "yellow_cards")
    private int yellowCards;

    @Column(name = "red_cards")
    private int redCards;

    @Column(name = "finals_played")
    private int finalsPlayed; // default hodnota pre nové objekty

    @Column(name = "finals_won")
    private int finalsWon;
}