package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.PlayerInMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerInMatchRepository extends JpaRepository<PlayerInMatch, Long> {

    List<PlayerInMatch> findByMatch_Id(Long matchId);
}

