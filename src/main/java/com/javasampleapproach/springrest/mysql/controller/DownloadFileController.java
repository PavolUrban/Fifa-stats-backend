package com.javasampleapproach.springrest.mysql.controller;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.model.View;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;

 
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class DownloadFileController {
  
  @Autowired
  FileRepository fileRepository;
 
  /*
   * List All Files
   */
    @JsonView(View.FileInfo.class)
  @GetMapping("/api/file/all")
  public List<FileModel> getListFiles() {
    return fileRepository.findAll();
  }
  
    /*
     * Download Files
     */
  @GetMapping("/api/file/{id}")
  public FileModel getFile(@PathVariable Long id) {
    Optional<FileModel> fileOptional = fileRepository.findById(id);
    
    FileModel fm = new FileModel(fileOptional.get().getName(), fileOptional.get().getMimetype(), fileOptional.get().getPic());
      
    

    
    return fm;
  }
  
  @GetMapping("/api/fileByName/{teamname}")
  public FileModel getFile(@PathVariable String teamname) {
	  
	  
    Optional<FileModel> fileOptional = Optional.ofNullable(fileRepository.findByTeamname(teamname));
    
    FileModel fm = new FileModel(fileOptional.get().getName(), fileOptional.get().getMimetype(), fileOptional.get().getPic());
      
    

    
    return fm;
  }
  
  
  
  
  
}