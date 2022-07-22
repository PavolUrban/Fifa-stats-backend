package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.model.PlayerGoalscorer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface FifaPlayerDBRepository  extends CrudRepository<FifaPlayerDB, Long> {
    FifaPlayerDB findByPlayerName(String playerName);
    List<FifaPlayerDB> findByPlayerNameIn(List<String> playerNames);
    List<FifaPlayerDB> findByIdIn(Set<Long> ids);



}
