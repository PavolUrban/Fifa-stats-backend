package com.javasampleapproach.springrest.mysql.controller;


import com.javasampleapproach.springrest.mysql.entities.TeamSeasonStat;
import com.javasampleapproach.springrest.mysql.model.v2DTO.TeamInfoV2;
import com.javasampleapproach.springrest.mysql.services.TeamService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/team-detail")
public class TeamDetailController {

    TeamService teamService;

    TeamDetailController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/getTeamStats/{teamId}")
    public TeamSeasonStat getTeamStats(@PathVariable("teamId") long teamId, @RequestParam("competition") String competition) {
        return teamService.getTeamStatsByCompetition(teamId, competition);
    }

    @GetMapping("/getTeamInfo/{teamId}")
    public TeamInfoV2 getTeamStats(@PathVariable("teamId") long teamId) {
        return teamService.getTeamInfoById(teamId);
    }
}
