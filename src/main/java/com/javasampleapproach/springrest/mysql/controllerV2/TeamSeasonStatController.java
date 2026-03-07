package com.javasampleapproach.springrest.mysql.controllerV2;

import com.javasampleapproach.springrest.mysql.entities.TeamSeasonStat;
import com.javasampleapproach.springrest.mysql.services.TeamSeasonStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/team-season-stats")
public class TeamSeasonStatController {


    @Autowired
    TeamSeasonStatService teamSeasonStatService;

    @PostMapping
    public void createTeamSeasonStatsForAllTeamsAndSeasons() {
        teamSeasonStatService.recalculateGlobalStats();
    }

    @GetMapping
    public ResponseEntity<Page<TeamSeasonStat>> getTeamStats(
            @RequestParam(required = false, defaultValue = "ALL") String season,
            // PageableDefault zabezpečí, že ak frontend nepošle sort/page, použijú sa tieto hodnoty (najviac bodov, prvá strana, 20 záznamov)
            @PageableDefault(page = 0, size = 20, sort = {"points", "goalDifference"}, direction = Sort.Direction.DESC) Pageable pageable) {

        // Vyhľadávanie pomocou repozitára
        Page<TeamSeasonStat> statsPage = teamSeasonStatService.findByCompetition(season,  pageable);

        return ResponseEntity.ok(statsPage);
    }
}
