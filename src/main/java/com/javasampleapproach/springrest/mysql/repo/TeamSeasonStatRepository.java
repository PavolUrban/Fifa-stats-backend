package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.entities.TeamSeasonStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface TeamSeasonStatRepository extends CrudRepository<TeamSeasonStat, Long> {
    /**
     * Získa kompletnú ligovú tabuľku pre danú sezónu a súťaž.
     * Automaticky zoradené podľa bodov (zostupne) a následne podľa rozdielu skóre (zostupne).
     * Ideálne na zobrazenie v UI.
     */
    List<TeamSeasonStat> findBySeasonAndCompetitionOrderByPointsDescGoalDifferenceDesc(String season, String competition);

    /**
     * Nájde konkrétnu štatistiku jedného tímu v danej sezóne a súťaži.
     * Užitočné, ak by si niekedy robil inkrementálny update (napr. pripočítanie +1 po zápase).
     */
    Optional<TeamSeasonStat> findByTeamIdAndCompetition(Long teamId, String competition);

    Page<TeamSeasonStat> findByCompetition(String competition, Pageable pageable);

    /**
     * Vymaže všetky štatistiky pre konkrétnu sezónu a súťaž.
     * Použijeme to pri prepočítavaní len jednej ligy.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM TeamSeasonStat t WHERE t.season = :season AND t.competition = :competition")
    void deleteBySeasonAndCompetition(String season, String competition);

    /**
     * TRUNCATE (kompletné premazanie tabuľky).
     * Rýchlejšie ako klasický DELETE. Použiješ pri "Global Button" na kompletný refresh všetkého.
     */
    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE team_season_stats", nativeQuery = true)
    void truncateTable();
}
