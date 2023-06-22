package com.javasampleapproach.springrest.mysql.controllerV2;

import com.javasampleapproach.springrest.mysql.model.MatchDetail;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import com.javasampleapproach.springrest.mysql.model.matches.FilteredMatchesRequest;
import com.javasampleapproach.springrest.mysql.model.matches.DataToCreateMatch;
import com.javasampleapproach.springrest.mysql.model.matches.TopMatchesRequest;
import com.javasampleapproach.springrest.mysql.serviceV2.MatchesServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/matchesV2")
public class MatchesControllerV2 {

    @Autowired
    MatchesServiceV2 matchesService;

    @PostMapping("/getFilteredMatches")
    public List<MatchesDTO> getFilteredMatches(@RequestBody FilteredMatchesRequest request) {
        return matchesService.getFilteredMatches(request.getCompetition(), request.getCompetitionPhase(), request.getSeason(), null);
    }

    @GetMapping(value = "/getMatchDetails/{matchId}")
    public MatchDetail getMatchDetails(@PathVariable("matchId") Long matchId) {
        return matchesService.getMatchDetails(matchId);
    }

    @GetMapping("/getDataToCreateMatch")
    public DataToCreateMatch getDataToCreateMatch() {
        return matchesService.getDataToCreateMatch();
    }

    @PostMapping("/createOrUpdateMatch")
    public void createOrUpdateMatch(@RequestBody MatchesDTO match) {
        matchesService.createOrUpdateMatch(match);
    }

    @PostMapping("/getTopMatches")
    public List<MatchesDTO> getTopMatches(@RequestBody TopMatchesRequest topMatchesRequest) {
        return matchesService.getTopMatches(topMatchesRequest);
    }

    // TODO urban latest
    // in season overview add teams with most wins, losses, draws, goals scored and conceeded
    // globalstatscontroller use services, cleanup
    // add h22 and other missing stuff from default Matches controller
    // add not only biggest A/H wins but biggest total wins
    // add goalscorers per Pavol Jay vs Kotlik !!
    // most penalty goalscorers / penalty for teams in single team view and also in global stats
    // goalscorers without penalties
    // most goals, yellowcards, redcards team stats global
    // own goals table

    // most goals by team in group stage, and other stages
    // lowest number of goals in stages
    // most cards in stages
    // fastest goal
    // fastest red card
    // something like for each team on its page - add fifa10 - quaterfinal, fifa11 - group stage ...
    // something like percentage of goals by player vs team in season - how important player was
    // best results of kotlik teams, e.g. most wins in group stage, play off, season , most points, most goals etc.

    // best teams by players e.g. kotlik has most wins with chelsea, scored most goals with blabla etc
}
