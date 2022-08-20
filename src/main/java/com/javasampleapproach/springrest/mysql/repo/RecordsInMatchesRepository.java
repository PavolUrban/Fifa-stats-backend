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
            "JOIN Matches m ON m.id = rec.matchId " +
            "WHERE (:competitionPhase is null or m.competitionPhase = :competitionPhase) AND (:season is null or m.season = :season) AND (:competition is null or m.competition = :competition) AND (:teamName is null or rec.teamName = :teamName) AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )"
    )
    List<RecordsInMatches> getRecordsByCompetition(@Param("competitionPhase") String competitionPhase, @Param("season") String season, @Param("competition") String competition, @Param("teamName") String teamname, @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);

    @Query(value =
                    "SELECT rec " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.matchId " +
                    "WHERE m.competitionPhase LIKE 'GROUP%' AND m.season = :season AND m.competition = :competition AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )")
    List<RecordsInMatches> getGroupStageRecordsBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                      @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);

    @Query(value =
            "SELECT rec " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.matchId " +
                    "WHERE (m.competitionPhase LIKE '%inal%' OR m.competitionPhase LIKE '%Round of%') AND m.season = :season AND m.competition = :competition AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )")
    List<RecordsInMatches> getPlayOffsRecordsBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                    @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);

    @Query(value =
            "SELECT count(rec) " +
            "FROM RecordsInMatches rec " +
            "JOIN Matches m ON m.id = rec.matchId " +
            "WHERE m.competitionPhase LIKE 'GROUP%' AND m.season = :season AND m.competition = :competition AND rec.typeOfRecord = :typeOfRecord1 ")
    int getGroupStageCardsCountBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                      @Param("typeOfRecord1") String typeOfRecord1);

    @Query(value =
            "SELECT count(rec) " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.matchId " +
                    "WHERE (m.competitionPhase LIKE '%inal%' OR m.competitionPhase LIKE '%Round of%') AND m.season = :season AND m.competition = :competition AND rec.typeOfRecord = :typeOfRecord1 ")
    int getPlayOffsCardsCountBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                      @Param("typeOfRecord1") String typeOfRecord1);




    @Query(value =
            "SELECT rec " +
                    "FROM RecordsInMatches rec " +
                    "JOIN Matches m ON m.id = rec.matchId " +
                    "WHERE m.season = :season AND m.competition = :competition AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )")
    List<RecordsInMatches> getTotalGoalscorersBySeasonAndCompetition(@Param("season") String season, @Param("competition") String competition,
                                                                                    @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);


//    @Query(value = "SELECT user.firstname AS firstname, user.lastname AS lastname FROM SD_User user WHERE id = ?1", nativeQuery = true)
//    NameOnly findByNativeQuery(Integer id);

    @Query(value = "SELECT rim.typeOfRecord, rim.numberOfGoalsForOldFormat, rim.teamName, m.season FROM RECORDSINMATCHES rim JOIN Matches m ON m.id = rim.matchId WHERE playerid = ?1 ORDER BY m.season", nativeQuery = true)
    List<PlayerStatsInSeason> findRecordsRelatedToPlayer(long id);

    @Query(value = "SELECT rim.* FROM MATCHES m JOIN RecordsInMatches rim ON m.id = rim.matchId WHERE (m.awayteam = :teamName OR m.hometeam = :teamName) AND rim.typeOfRecord = 'G'", nativeQuery = true)
    List<RecordsInMatches> getScoredAndConcededGoalsByTeam(String teamName);

}
