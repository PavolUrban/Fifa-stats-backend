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

    // --- 7. AKTUÁLNA FORMA ---

    /** Počet po sebe idúcich výhier od posledného zápasu. */
    @Column(name = "current_win_streak", columnDefinition = "INT DEFAULT 0")
    private Integer currentWinStreak;

    /** Počet po sebe idúcich zápasov bez prehry (výhry + remízy). */
    @Column(name = "current_unbeaten_streak", columnDefinition = "INT DEFAULT 0")
    private Integer currentUnbeatenStreak;

    /** Počet po sebe idúcich prehier od posledného zápasu. */
    @Column(name = "current_loss_streak", columnDefinition = "INT DEFAULT 0")
    private Integer currentLossStreak;

    /** Počet po sebe idúcich zápasov bez výhry (prehry + remízy). */
    @Column(name = "current_without_win_streak", columnDefinition = "INT DEFAULT 0")
    private Integer currentWithoutWinStreak;

    // --- 8. HISTORICKY NAJDLHŠIE SÉRIE ---

    /** Historicky najdlhšia séria výhier. */
    @Column(name = "longest_win_streak", columnDefinition = "INT DEFAULT 0")
    private int longestWinStreak;

    /** Historicky najdlhšia séria bez prehry (výhry + remízy). */
    @Column(name = "longest_unbeaten_streak", columnDefinition = "INT DEFAULT 0")
    private int longestUnbeatenStreak;

    /** Historicky najdlhšia séria prehier. */
    @Column(name = "longest_loss_streak", columnDefinition = "INT DEFAULT 0")
    private int longestLossStreak;

    /** Historicky najdlhšia séria bez výhry (prehry + remízy). */
    @Column(name = "longest_without_win_streak", columnDefinition = "INT DEFAULT 0")
    private int longestWithoutWinStreak;

    // --- 9. PRIEMERNÉ ŠTATISTIKY (na zápas) ---

    @Column(name = "avg_wins", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgWins;

    @Column(name = "avg_draws", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgDraws;

    @Column(name = "avg_losses", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgLosses;

    @Column(name = "avg_points", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgPoints;

    @Column(name = "avg_goals_scored", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgGoalsScored;

    @Column(name = "avg_goals_conceded", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgGoalsConceded;

    @Column(name = "avg_penalty_goals_scored", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgPenaltyGoalsScored;

    @Column(name = "avg_yellow_cards", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgYellowCards;

    @Column(name = "avg_red_cards", columnDefinition = "DOUBLE DEFAULT 0")
    private double avgRedCards;

    /** Percentuálny podiel zápasov s čistým kontom (0–100). */
    @Column(name = "clean_sheets_percentage", columnDefinition = "DOUBLE DEFAULT 0")
    private double cleanSheetsPercentage;

    // --- 10. HRÁČI ---

    /** Počet zápasov, ktoré za tento tím odohral Kotlik. */
    @Column(name = "matches_by_kotlik", columnDefinition = "INT DEFAULT 0")
    private int matchesByKotlik;

    /** Počet zápasov, ktoré za tento tím odohral Pavol Jay. */
    @Column(name = "matches_by_pavol_jay", columnDefinition = "INT DEFAULT 0")
    private int matchesByPavolJay;

    // --- 11. ÚČASŤ V SÚŤAŽI ---

    /**
     * Počet sezón (účastí) v danej súťaži.
     * Pre CL/EL = počet distinct sezón v tej súťaži.
     * Pre ALL = počet distinct sezón (CL+EL dokopy, sezóna počítaná raz).
     */
    @Column(name = "competition_appearances", columnDefinition = "INT DEFAULT 0")
    private int competitionAppearances;

    /**
     * Aktuálna séria po sebe idúcich sezón bez prerušenia v danej súťaži
     * (počítané od poslednej sezóny smerom do minulosti).
     */
    @Column(name = "consecutive_competition_appearances", columnDefinition = "INT DEFAULT 0")
    private int consecutiveCompetitionAppearances;

    //TODO urban stats
    // potrebujem este per season toto poukladat - teraz mam len all
    // chcem most goals scored in single match
    // chcem most goals conceded in single match
    // kolko zapasov v rade skorovali
    // kolko zapasov v rade neskorovali
    // kolko uz chybaju v LM/EL - len ak sa niekedy zucastnili
}