package com.javasampleapproach.springrest.mysql.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class UploadFileController {

    @Autowired
    FileRepository fileRepository;

    // To save team logo
    @PostMapping("/api/file/upload/{teamname}")
    public String uploadMultipartFile(@PathVariable("teamname") String teamname, @RequestParam("uploadfile") MultipartFile file) {
        try {
            FileModel fileModel = new FileModel(file.getOriginalFilename(), file.getContentType(), file.getBytes(), teamname);
            fileRepository.save(fileModel);
            return HttpStatus.OK.toString();
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST.toString();
        }
    }
}
