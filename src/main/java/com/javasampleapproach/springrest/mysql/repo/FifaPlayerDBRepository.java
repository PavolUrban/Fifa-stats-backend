package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerWithRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface FifaPlayerDBRepository  extends CrudRepository<FifaPlayerDB, Long> {
    
    FifaPlayerDB findByPlayerName(String playerName);

    List<FifaPlayerDB> findByPlayerNameContainingIgnoreCase(final String playerNameSubstring);
    
    List<FifaPlayerDB> findByPlayerNameIn(List<String> playerNames);
    
    List<FifaPlayerDB> findByIdIn(Set<Long> ids);
    
    @Query(value = 
            "SELECT rim.playerId, fp.playerName, COUNT(rim.playerId) AS recordEventCount, rim.matchID, m.hometeam, CONCAT(m.scoreHome, ':', m.scoreAway) AS score, m.awayTeam, rim.teamName, m.season " +
            "FROM RecordsInMatches rim JOIN FifaPlayer fp ON rim.playerId = fp.id JOIN Matches m ON rim.matchId = m.id " +
            "WHERE (:competition is null or m.competition = :competition) AND rim.typeOfRecord = :typeOfRecord " +
            "GROUP BY rim.matchId, rim.playerId, rim.typeOfRecord " +
            "ORDER BY recordEventCount DESC", nativeQuery = true)
    List<FifaPlayerWithRecord> getPlayersWithMostGoals(String competition, String typeOfRecord);

    @Query(value = 
            "SELECT rim.playerId, fp.playerName,rim.numberOfGoalsForOldFormat AS recordEventCount, rim.matchID, m.hometeam, CONCAT(m.scoreHome, ':', m.scoreAway) AS score, m.awayTeam, rim.teamName, m.season  " +
            "FROM RecordsInMatches rim " +
            "JOIN FifaPlayer fp ON rim.playerId = fp.id " +
            "JOIN Matches m ON rim.matchId = m.id " +
            "WHERE rim.typeOfRecord='G' AND rim.numberOfGoalsForOldFormat > 0 " +
            "ORDER BY recordEventCount DESC", nativeQuery = true)
    List<FifaPlayerWithRecord> getPlayersWithMostGoalsOldFormat(String competition, String typeOfRecord);

    @Query(value = 
            "SELECT rim.playerId, fp.playerName, COUNT(rim.playerID) AS recordEventCount, rim.teamName, m.season " +
            "FROM RecordsInMatches rim " +
            "JOIN FifaPlayer fp ON fp.id = rim.playerId " +
            "JOIN Matches m ON m.id = rim.matchId " +
            "WHERE rim.matchId IN (SELECT id FROM Matches WHERE season = :season) AND typeOfRecord = 'G' AND (:competition is null or m.competition = :competition) AND ( (:competitionPhase1 is null or m.competitionPhase LIKE %:competitionPhase1%) OR  (:competitionPhase2 is null or m.competitionPhase LIKE %:competitionPhase2%)) " +
            "GROUP BY rim.playerID ORDER BY recordEventCount DESC " +
            "LIMIT 25", nativeQuery = true)
    List<FifaPlayerWithRecord> getPlayersWithMostGoalsInSeasonNewFormat(String season, String competition, String competitionPhase1, String competitionPhase2);

    @Query(value =
            "SELECT rim.playerId, fp.playerName, SUM(rim.numberOfGoalsForOldFormat) AS recordEventCount, rim.teamName, m.season " +
            "FROM RecordsInMatches rim " +
            "JOIN FifaPlayer fp ON fp.id = rim.playerId " +
            "JOIN Matches m ON m.id = rim.matchId WHERE matchId IN (SELECT id FROM Matches WHERE season = :season) AND typeOfRecord = 'G' AND (:competition is null or m.competition = :competition) AND ( (:competitionPhase1 is null or m.competitionPhase LIKE %:competitionPhase1%) OR  (:competitionPhase2 is null or m.competitionPhase LIKE %:competitionPhase2%)) " +
            "GROUP BY playerID " +
            "ORDER BY recordEventCount DESC LIMIT 25", nativeQuery = true)
    List<FifaPlayerWithRecord> getPlayersWithMostGoalsInSeasonOldFormat(String season, String competition, String competitionPhase1, String competitionPhase2);
}
