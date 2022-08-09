package com.javasampleapproach.springrest.mysql.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import org.springframework.data.repository.query.Param;

public interface MatchesRepository extends CrudRepository<Matches, Long>{

	@Query("SELECT m FROM Matches m WHERE m.hometeam = ?1 OR m.awayteam = ?1 " +
			"ORDER BY SEASON DESC "+
				", case when competitionphase = 'Final' then 1 " +
					"when competitionphase = 'Semifinals' then 2 " +
					"when competitionphase = 'Quarterfinals' then 3 " +
					"when competitionphase = 'Round of 16' then 4 " +
					"when competitionphase = 'Round of 32' then 5 " +
					"else 6 " +
				"end asc") //competition, season, competitionPhase,
	List<Matches> getAllMatchesForTeam(String teamname);

	@Query("SELECT m FROM Matches m WHERE m.season = ?1 AND m.competition= ?2 AND m.competitionPhase LIKE 'GROUP%' ORDER BY competitionPhase, id DESC")
	List<Matches> getAllMatchesBySeasonAndCompetitionGroupStage(String season, String competition);
	
	@Query("SELECT m FROM Matches m WHERE m.season = ?1 AND m.competition= ?2 AND ( m.competitionPhase LIKE '%final%' OR m.competitionPhase LIKE 'Round%' ) ORDER BY competitionPhase, id ASC")
	List<Matches> getAllMatchesBySeasonAndCompetitionPlayOffs(String season, String competition);


	@Query("SELECT m FROM Matches m WHERE m.competition= ?1 AND m.competitionPhase LIKE 'GROUP%'")
	List<Matches> getGroupStageMatchesByCompetition(String competition);

	@Query("SELECT m FROM Matches m WHERE m.competition= ?1 AND ( m.competitionPhase LIKE '%inal%' OR m.competitionPhase LIKE 'Round%' )")
	List<Matches> getPlayOffsMatchesByCompetition(String season, String competition);

	//all matches
	List<Matches> findBySeasonAndHometeamOrSeasonAndAwayteam(String season,String hometeam,String season1, String awayteam);
	
	//all matches from specified season
	List<Matches> findBySeason(String season);
	
	//all matches from specified season, competition and phase (like GROUP A)
	List<Matches> findByCompetitionAndSeasonAndCompetitionPhase(String competition, String season,String competitionPhase);
		
	List<Matches> findByHometeamAndAwayteam(String hometeam, String awayteam);
	
	//all matches by specified team
	List<Matches> findByHometeamOrAwayteam(String hometeam, String awayteam);

	// all final matches
	List<Matches> findByCompetitionPhase(String competitionPhase);

	// all final matches by competition
	List<Matches> findByCompetitionPhaseAndCompetitionOrderBySeason(String competitionPhase, String competition);

	List<Matches> findByCompetition(String competition);


	//"SELECT c FROM Customer c WHERE (:name is null or c.name = :name) and (:email is null"
	//			+ " or c.email = :email)"
	@Query("SELECT m FROM Matches m where (:season is null or m.season = :season) and (:competition is null or m.competition = :competition) and (:competitionPhase is null or m.competitionPhase = :competitionPhase) " +
			"ORDER BY SEASON DESC "+
			", case when competitionphase = 'Final' then 1 " +
			"when competitionphase = 'Semifinals' then 2 " +
			"when competitionphase = 'Quarterfinals' then 3 " +
			"when competitionphase = 'Round of 16' then 4 " +
			"when competitionphase = 'Round of 32' then 5 " +
			"else 6 " +
			"end asc")
	List<Matches> getMatchesWithCustomFilters(@Param("season") String season,@Param("competition") String competition, @Param("competitionPhase") String competitionPhase);

	@Query("SELECT MIN(m.season) FROM Matches m WHERE (m.awayteam = ?1 OR hometeam = ?1) AND competition = ?2")
	String firstSeasonInCompetition(String teamName, String competition);

}
