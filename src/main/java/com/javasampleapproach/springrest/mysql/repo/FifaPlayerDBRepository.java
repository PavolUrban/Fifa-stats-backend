package com.javasampleapproach.springrest.mysql.repo;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import org.springframework.data.repository.CrudRepository;

public interface FifaPlayerDBRepository  extends CrudRepository<FifaPlayerDB, Long> {
}
