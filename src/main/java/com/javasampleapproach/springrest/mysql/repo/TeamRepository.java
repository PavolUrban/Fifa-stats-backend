package com.javasampleapproach.springrest.mysql.repo;

import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.model.Team;

public interface TeamRepository extends CrudRepository<Team, Long>{
	public Team findByTeamName(String teamName);
}
