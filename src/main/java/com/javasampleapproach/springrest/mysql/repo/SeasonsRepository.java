package com.javasampleapproach.springrest.mysql.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.entities.Seasons;

import java.util.List;

public interface SeasonsRepository extends CrudRepository<Seasons, String> {

    @Query("SELECT s.season FROM Seasons s order by s.season")
    List<String> getAvailableSeasonsList();

}
