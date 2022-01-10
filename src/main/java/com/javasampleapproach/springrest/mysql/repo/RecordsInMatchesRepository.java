package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.RecordsInMatchesLite;
import org.hibernate.annotations.NamedQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface RecordsInMatchesRepository extends CrudRepository<RecordsInMatches, Long> {

    public List<RecordsInMatches> findByMatchIdOrderByMinuteOfRecord(int matchID);
    public List<RecordsInMatches> findByTypeOfRecordOrTypeOfRecord(String typeOfRecord1, String typeOfRecord2);



    @Query(
            value =
            "SELECT rec " +
            "FROM RecordsInMatches rec " +
            "JOIN Matches m ON m.id = rec.matchId " +
            "WHERE (:competition is null or m.competition = :competition) AND (:teamName is null or rec.teamName = :teamName) AND (rec.typeOfRecord = :typeOfRecord1 OR rec.typeOfRecord = :typeOfRecord2 )" )

    public List<RecordsInMatches> getRecordsByCompetition(@Param("competition") String competition, @Param("teamName") String teamname, @Param("typeOfRecord1") String typeOfRecord1, @Param("typeOfRecord2") String typeOfRecord2);
}
