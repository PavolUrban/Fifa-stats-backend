package com.javasampleapproach.springrest.mysql.controller;

import Utils.HelperMethods;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.H2HPlayers;
import com.javasampleapproach.springrest.mysql.model.OverallStats;
import com.javasampleapproach.springrest.mysql.model.SeasonBySeason;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.SeasonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static Utils.MyUtils.KOTLIK;
import static Utils.MyUtils.PAVOL_JAY;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/seasonBySeason")
public class SeasonBySeasonController {

    @Autowired
    MatchesRepository matchesRepository;

    @Autowired
    SeasonsRepository seasonsRepository;

    DecimalFormat decimalFormat = new DecimalFormat("#.##");

    // todo stats h2h per season at least total for now.. inn the future separated CL, EL, TOtal
    @GetMapping("/getStats")
    public SeasonBySeason getH2HSeasonBySeason() {

        // for simplicity currently we are not separate playoffs and group stage
        List<Matches> groupStageMatches = matchesRepository.findByCompetition("CL");
        List<String> seasonNames = seasonsRepository.getAvailableSeasonsList();

        SeasonBySeason seasonStats = new SeasonBySeason();

        seasonNames.forEach(season -> {
            List<Matches> matchesInCurrentSeason = groupStageMatches.stream().filter(m-> m.getSeason().equalsIgnoreCase(season)).collect(Collectors.toList());
            OverallStats os = new OverallStats();
            os.setSeasonName(season);
            int matchesCount = matchesInCurrentSeason.size();
            os.getMatchesCount().add(matchesCount);
            int goalsInGroupStage = matchesInCurrentSeason.stream().mapToInt(m-> m.getScorehome() + m.getScoreaway()).sum();
            os.getGoalsCount().add(goalsInGroupStage);

            Map<String, Integer> pavolJayVsKotlik = HelperMethods.setnumberOfPlayersWins(matchesInCurrentSeason);
            os.setPavolJay(pavolJayVsKotlik.get(PAVOL_JAY));
            os.setKotlik(pavolJayVsKotlik.get(KOTLIK));
            os.setDraws(pavolJayVsKotlik.get("Draws"));

            if(matchesCount > 0) {
                float avgGoals = (float) goalsInGroupStage/matchesCount;
                float twoDecimalPlaces = Float.valueOf(decimalFormat.format(avgGoals));
                os.getAvgGoalsPerMatch().add(twoDecimalPlaces);
                float avgPavolJayWins =  (float) os.getPavolJay() / matchesCount * 100;
                os.setPavolJayAvg(Float.valueOf(decimalFormat.format(avgPavolJayWins)));
                float avgKotlikWins = (float) os.getKotlik() / matchesCount * 100;
                os.setKotlikAvg(Float.valueOf(decimalFormat.format(avgKotlikWins)));
                float avgDraws = (float) os.getDraws()/matchesCount * 100;
                os.setDrawsAvg(Float.valueOf(decimalFormat.format(avgDraws)));
            } else {
                float twoDecimalPlaces = Float.valueOf(decimalFormat.format(0));
                os.getAvgGoalsPerMatch().add(twoDecimalPlaces);
                os.setPavolJayAvg(Float.valueOf(decimalFormat.format(0)));
                os.setKotlikAvg(Float.valueOf(decimalFormat.format(0)));
                os.setDrawsAvg(Float.valueOf(decimalFormat.format(0)));
            }



            seasonStats.getSeasonBySeasonStats().add(os);
        });


        return seasonStats;
    }


}
