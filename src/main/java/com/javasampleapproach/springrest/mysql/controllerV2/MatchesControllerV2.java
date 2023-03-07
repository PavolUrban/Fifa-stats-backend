package com.javasampleapproach.springrest.mysql.controllerV2;

import com.javasampleapproach.springrest.mysql.model.MatchesDTO;
import com.javasampleapproach.springrest.mysql.serviceV2.MatchesServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/matchesV2")
public class MatchesControllerV2 {

    @Autowired
    MatchesServiceV2 matchesService;

    @GetMapping("/getMatches")
    public List<MatchesDTO> getAllMatches() {
        return matchesService.getAllMatches();
    }

    // todo rework to receive requestObject
    @GetMapping("/getCustomGroupMatches/{competition}/{season}/{competitionPhase}")
    public List<MatchesDTO> getCustomGroupMatches(@PathVariable("competition") String competition, @PathVariable("season") String season, @PathVariable("competitionPhase") String competitionPhase) {
        // todo urban here
        return matchesService.getCustomGroupMatches(competition, season, competitionPhase);
    }

}
