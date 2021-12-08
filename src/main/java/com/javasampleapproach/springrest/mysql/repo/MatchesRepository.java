package com.javasampleapproach.springrest.mysql.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.model.Matches;

public interface MatchesRepository extends CrudRepository<Matches, Long>{







	@Query("SELECT m FROM Matches m WHERE m.hometeam = ?1 OR m.awayteam = ?1 " +
			"ORDER BY SEASON DESC "+
				", case when competitionphase = 'Final' then 1 " +
					"when competitionphase = 'Semifinals' then 2 " +
					"when competitionphase = 'Quarterfinals' then 3 " +
					"when competitionphase = 'Osemfinals' then 4 " +
					"else 5 " +
				"end asc") //competition, season, competitionPhase,
	List<Matches> getAllMatchesForTeam(String teamname);

	@Query("SELECT m FROM Matches m WHERE m.season = ?1 AND m.competition= ?2 AND m.competitionPhase LIKE 'GROUP%' ORDER BY competitionPhase, id DESC")
	List<Matches> getAllMatchesBySeasonAndCompetitionGroupStage(String season, String competition);
	
	@Query("SELECT m FROM Matches m WHERE m.season = ?1 AND m.competition= ?2 AND m.competitionPhase LIKE '%final%' ORDER BY competitionPhase, id ASC")
	List<Matches> getAllMatchesBySeasonAndCompetitionPlayOffs(String season, String competition);
	
	
	//all matches
	List<Matches> findBySeasonAndHometeamOrSeasonAndAwayteam(String season,String hometeam,String season1, String awayteam);
	
	//all home matches from specified season for single team
	List<Matches> findByHometeamAndSeason(String hometeam, String season);
	
	//all away matches from specified season for single team
	List<Matches> findByAwayteamAndSeason(String hometeam, String season);
	
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
}
