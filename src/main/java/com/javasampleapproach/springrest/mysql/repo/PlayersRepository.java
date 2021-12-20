package com.javasampleapproach.springrest.mysql.repo;

import org.springframework.data.repository.CrudRepository;

import com.javasampleapproach.springrest.mysql.entities.Player;

public interface PlayersRepository extends CrudRepository<Player, String>{

}
