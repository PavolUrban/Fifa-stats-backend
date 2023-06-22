package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.PlayerStatsInSeason;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface RecordsInMatchesRepository extends CrudRepository<RecordsInMatches, Long> {

    List<RecordsInMatches> findByMatchIdOrderByMinuteOfRecord(int matchID);

    @Query(
            value =
            "SELECT rec " +
            "FROM RecordsInMatches rec " +
            "JOIN Matches m ON m.id = rec.match.id " +
            "WHERE (:competitionPhase is null or m.competitionPhase = :competitionPhase) AND (:season is null or m.season = :season) AND (:competition is null or m.competition = :competition) AND (:teamId is null or rec.team.id = :teamId) AND (rec.typeOfRecord IN :typeOfRecord)"
    )
    List<RecordsInMatches> getRecordsByCompetition(@Param("competitionPhase") String competitionPhase, @Param("season") String season, @Param("competition") String competition, @Param("teamId") Long teamId, @Param("typeOfRecord") List<String> typeOfRecord);

    @Query(value =
                    "SELECT rec " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.match.id " +
                    "WHERE m.competitionPhase LIKE 'GROUP%' AND m.season = :season AND m.competition = :competition AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )")
    List<RecordsInMatches> getGroupStageRecordsBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                      @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);

    @Query(value =
            "SELECT rec " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.match.id " +
                    "WHERE (m.competitionPhase LIKE '%inal%' OR m.competitionPhase LIKE '%Round of%') AND m.season = :season AND m.competition = :competition AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )")
    List<RecordsInMatches> getPlayOffsRecordsBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                    @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);

    @Query(value =
            "SELECT count(rec) " +
            "FROM RecordsInMatches rec " +
            "JOIN Matches m ON m.id = rec.match.id " +
            "WHERE m.competitionPhase LIKE 'GROUP%' AND m.season = :season AND m.competition = :competition AND rec.typeOfRecord = :typeOfRecord1 ")
    int getGroupStageCardsCountBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                      @Param("typeOfRecord1") String typeOfRecord1);

    @Query(value =
            "SELECT count(rec) " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.match.id " +
                    "WHERE (m.competitionPhase LIKE '%inal%' OR m.competitionPhase LIKE '%Round of%') AND m.season = :season AND m.competition = :competition AND rec.typeOfRecord = :typeOfRecord1 ")
    int getPlayOffsCardsCountBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                      @Param("typeOfRecord1") String typeOfRecord1);




    @Query(value =
            "SELECT rec " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.match.id " +
                    "WHERE m.season = :season AND m.competition = :competition AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )")
    List<RecordsInMatches> getTotalGoalscorersBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                                    @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);


//    @Query(value = "SELECT user.firstname AS firstname, user.lastname AS lastname FROM SD_User user WHERE id = ?1", nativeQuery = true)
//    NameOnly findByNativeQuery(Integer id);

//    @Query(value = "" +
//            "SELECT new com.javasampleapproach.springrest.mysql.model.PlayerStatsInSeason(rim.typeOfRecord, rim.playerTeamId, m.season, t.teamName) " +
//            "FROM RecordsInMatches rim " +
//            "JOIN Matches m ON m.id = rim.match.id " +
//            "JOIN Team t ON t.id = rim.playerTeamId " +
//            "WHERE rim.playerId = ?1 AND rim.typeOfRecord NOT LIKE 'OG|%' " +
//            "ORDER BY m.season")
//    List<PlayerStatsInSeason> findRecordsRelatedToPlayer(long id);


    @Query(value = "" +
            "SELECT rim.* FROM MATCHES m " +
            "JOIN RecordsInMatches rim ON m.id = rim.match_id " +
            "WHERE (m.idHomeTeam = :teamId OR m.idAwayTeam = :teamId) AND (rim.type_of_record = 'G' OR rim.type_of_record = 'Penalty')", nativeQuery = true)
    List<RecordsInMatches> getScoredAndConcededGoalsByTeam(Integer teamId);

}
