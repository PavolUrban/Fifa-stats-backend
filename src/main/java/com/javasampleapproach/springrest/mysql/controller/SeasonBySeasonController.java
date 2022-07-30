package com.javasampleapproach.springrest.mysql.controller;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.SeasonBySeason;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/seasonBySeason")
public class SeasonBySeasonController {

    @Autowired
    MatchesRepository matchesRepository;

    // todo stats h2h per season at least total for now.. inn the future separated CL, EL, TOtal
    @GetMapping("/getH2H")
    public SeasonBySeason getH2HSeasonBySeason() {

        Iterable<Matches> allMatches = matchesRepository.findAll();


        return null;
    }


}
