package com.javasampleapproach.springrest.mysql.model.lineup;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LineupPlayerDTO {

    private long playerId;
    private String playerName;

    /** Jersey number – optional */
    private Integer jerseyNumber;

    /** Player rating in this match – optional */
    private Double rating;

    /** Player position in this match (e.g. GK, CB, CAM) – optional */
    private String position;

    /** true if this starter was substituted off during the match */
    private boolean isSubstituted;

    /** Minute the substitution happened – optional */
    private Integer substitutedAtMinute;

    /** For starters who were subbed off: the player who replaced them */
    private PlayerRef replacedBy;

    /** For substitutes: the starter they replaced */
    private PlayerRef replacedPlayer;

    /** Whether this player was the team captain in this match */
    private boolean isCaptain;
}

