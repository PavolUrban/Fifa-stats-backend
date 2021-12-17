package com.javasampleapproach.springrest.mysql.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.javasampleapproach.springrest.mysql.model.Team;
import com.javasampleapproach.springrest.mysql.repo.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class DownloadFileController {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    TeamRepository teamRepository;

    @GetMapping("/api/matchlogos/{teamname1}/{teamname2}")
    public Map<String, FileModel> getLogosForTeams(@PathVariable String teamname1, @PathVariable String teamname2) {

        Map<String, FileModel> teamLogos = new HashMap<>();

        teamLogos.put(teamname1, fileRepository.findByTeamname(teamname1));
        teamLogos.put(teamname2, fileRepository.findByTeamname(teamname2));

        return teamLogos;
    }

    @GetMapping("/api/getAllLogos")
    public List<FileModel> getAllLogos() {
        return fileRepository.findAll();
    }

    @GetMapping("/api/getSingleTeamLogo/{teamname}")
    public Map<String, Object> getTeamLogo(@PathVariable String teamname) {
        Map<String, Object> detailedInfo = new HashMap<>();

        FileModel teamLogo = fileRepository.findByTeamname(teamname);
        detailedInfo.put("logo", teamLogo);

        Team team = teamRepository.findByTeamName(teamname);
        detailedInfo.put("country", team.getCountry());

        return detailedInfo;
    }

}