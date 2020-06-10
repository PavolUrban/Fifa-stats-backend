package com.javasampleapproach.springrest.mysql.repo;

import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.model.Player;

public interface PlayersRepository extends CrudRepository<Player, String>{

}
