package com.javasampleapproach.springrest.mysql.repo;


import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.javasampleapproach.springrest.mysql.model.FileModel;
 
@Transactional
public interface FileRepository extends JpaRepository<FileModel, Long>{  
 
	  public FileModel findByTeamname(String teamName);
  
	  @Query("SELECT m FROM FileModel m WHERE m.teamname IN (?1)")
	  List<FileModel> getLogosForAllTeams(Set<String> teamNames);
	
	
}