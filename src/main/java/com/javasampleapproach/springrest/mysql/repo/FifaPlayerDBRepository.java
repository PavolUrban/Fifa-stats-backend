package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.model.PlayerGoalscorer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FifaPlayerDBRepository  extends CrudRepository<FifaPlayerDB, Long> {
    public FifaPlayerDB findByPlayerName(String playerName);
    public List<FifaPlayerDB> findByPlayerNameIn(List<String> playerNames);
    public List<FifaPlayerDB> findByIdIn(List<Long> ids);



}
