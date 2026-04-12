package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.MatchStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchStatsRepository extends JpaRepository<MatchStats, Long> {

    Optional<MatchStats> findByMatch_Id(Long matchId);
}