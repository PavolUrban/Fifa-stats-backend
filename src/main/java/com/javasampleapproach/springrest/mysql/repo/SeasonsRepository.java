package com.javasampleapproach.springrest.mysql.repo;

import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.model.Seasons;

public interface SeasonsRepository extends CrudRepository<Seasons, String> {

}
