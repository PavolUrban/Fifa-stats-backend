package com.javasampleapproach.springrest.mysql.controller;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerDialogStats;
import com.javasampleapproach.springrest.mysql.model.FifaPlayerStatsPerSeason;
import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/fifaPlayer")
public class FifaPlayerController {

    @Autowired
    MatchesRepository matchesRepository;

    @Autowired
    FileRepository fileRepository;

    // todo use record in matches to calculate stats here!

    @GetMapping("/getStats/{playerName}")
    public FifaPlayerDialogStats getAllStatsForSpecifiedFifaPlayer(@PathVariable("playerName") String playerName) {

        System.out.println("checking player " + playerName);
        Set<String> seasonsRelatedToPlayer = new HashSet<>();

        List<Matches> relatedMatches = matchesRepository.getMatchesRelatedToSpecifiedPlayer(playerName, playerName, playerName);

        FifaPlayerDialogStats allStats = new FifaPlayerDialogStats();
        allStats.setName(playerName);
//        FifaPlayerDialogStats dialogStats = new FifaPlayerDialogStats();

        relatedMatches.forEach(match-> {
            if(doesStatContainPlayerName(match.getGoalscorers(), playerName)){
                checkIfCurrentlyProcessedSeasonIsAlreadyInList(allStats, match, match.getGoalscorers(), playerName);
                FifaPlayerStatsPerSeason seasonToUpdate = getSeasonToUpdate(allStats, match);

                // todo dorobit aj by competition takto je nejasne kde dal vlastne gol, + nie je osetreny pripad ked dal v sezone gol za 2 timy + pridat aj OG + pridat order matches, dorobit view
                seasonToUpdate.setGoalsCount(seasonToUpdate.getGoalsCount() + getNumberOfGoalsToAdd(match.getGoalscorers(),playerName, match));
            }

            if(doesStatContainPlayerName(match.getYellowcards(), playerName)){
                checkIfCurrentlyProcessedSeasonIsAlreadyInList(allStats, match, match.getYellowcards(), playerName);
                //season has to exist at this moment
                FifaPlayerStatsPerSeason seasonToUpdate = getSeasonToUpdate(allStats, match);
                seasonToUpdate.setYellowCardsCount(seasonToUpdate.getYellowCardsCount() +1);
            }

            if(doesStatContainPlayerName(match.getRedcards(), playerName)){
                checkIfCurrentlyProcessedSeasonIsAlreadyInList(allStats, match, match.getRedcards(), playerName);
                //season has to exist at this moment
                FifaPlayerStatsPerSeason seasonToUpdate = getSeasonToUpdate(allStats, match);
                seasonToUpdate.setRedCardsCount(seasonToUpdate.getRedCardsCount() +1);
            }

        });

        FifaPlayerStatsPerSeason totalCount = new FifaPlayerStatsPerSeason();
        totalCount.setSeasonName("Total");
        totalCount.setTeamname("");

        allStats.getPlayerStatsPerSeason().forEach(season->{

            totalCount.setGoalsCount(totalCount.getGoalsCount() + season.getGoalsCount());
            totalCount.setYellowCardsCount(totalCount.getYellowCardsCount() + season.getYellowCardsCount());
            totalCount.setRedCardsCount(totalCount.getRedCardsCount() + season.getRedCardsCount());
//            System.out.println("Season");
//            System.out.println(season.getSeasonName() + " matches related ");
     //       season.getAllMatches().forEach(match-> System.out.println(match.getId()));
            System.out.println();
        });

        allStats.getPlayerStatsPerSeason().add(totalCount);

        return allStats;
    }

    //TODO  return correct goals count
    private int getNumberOfGoalsToAdd(String stat, String playerName, Matches match){
        String playersTeam = getCorrectPlayerTeam(stat, playerName, match);
        int numberOfGoals;
        String[] homeAway = stat.split("-");
        if(match.getHometeam().equalsIgnoreCase(playersTeam)){
            System.out.println("je v domacej casti " + homeAway[0]);
            numberOfGoals = toRename(homeAway[0], playerName, match);

        } else {
            System.out.println("je v hostujucej casti " + homeAway[1]);
            numberOfGoals = toRename(homeAway[1], playerName, match);
        }
        System.out.println();
        return numberOfGoals;
    }

    private int toRename(String goalscorersList, String playerName, Matches match){
        String specifiedPlayerRecord = null;
        int numberOfGoals = 1;

        if(goalscorersList.contains(";")){
            String[] properGoalscorersList = goalscorersList.split(";");

            for(int i=0;i<properGoalscorersList.length;i++){
                if(properGoalscorersList[i].contains(playerName)){
                    specifiedPlayerRecord = properGoalscorersList[i];
                    break;
                }
            }
            //console log.. tu stara vs nova
        } else {
           specifiedPlayerRecord = goalscorersList;
        }

        System.out.println("toto je on " + specifiedPlayerRecord);

        if (MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(match.getSeason())) {

            if(specifiedPlayerRecord.contains("*")){
                String[] numberOfGoalsAndPlayerName = specifiedPlayerRecord.split("\\*");
                numberOfGoals = Integer.parseInt(numberOfGoalsAndPlayerName[0]);
            }
        } else {
            if(specifiedPlayerRecord.contains(",")){
                String[] minutes = specifiedPlayerRecord.split(",");
                numberOfGoals = minutes.length;
            }
        }

        System.out.println("tolkoto golov pricitam "+ numberOfGoals);

        return numberOfGoals;
    }

    private FifaPlayerStatsPerSeason getSeasonToUpdate(FifaPlayerDialogStats allStats, Matches match){
        return allStats.getPlayerStatsPerSeason().stream().filter(singleSeason-> singleSeason.getSeasonName().equalsIgnoreCase(match.getSeason())).findFirst().orElse(null);
    }

    void checkIfCurrentlyProcessedSeasonIsAlreadyInList(FifaPlayerDialogStats allStats, Matches match, String statToCheck, String playerName){
        long count  = allStats.getPlayerStatsPerSeason().stream().filter(singleSeason-> singleSeason.getSeasonName().equalsIgnoreCase(match.getSeason())).count();
        if(count == 0){
            FifaPlayerStatsPerSeason statsPerSeason = new FifaPlayerStatsPerSeason();
            statsPerSeason.setSeasonName(match.getSeason());


            statsPerSeason.setTeamname(getCorrectPlayerTeam(statToCheck, playerName, match));
            FileModel logo = fileRepository.findByTeamname(statsPerSeason.getTeamname());
            statsPerSeason.setLogoUrl(logo.getPic());
            statsPerSeason.getAllMatches().add(match);
            allStats.getPlayerStatsPerSeason().add(statsPerSeason);


        } else if(count ==1){
            FifaPlayerStatsPerSeason seasonToUpdate = allStats.getPlayerStatsPerSeason().stream().filter(singleSeason-> singleSeason.getSeasonName().equalsIgnoreCase(match.getSeason())).findFirst().orElse(null);
            seasonToUpdate.getAllMatches().add(match);
        } else {
            System.out.println("TODO Throw error here. Only 0 or 1 is acceptable");
        }
    }

    private String getCorrectPlayerTeam(String statToCheck, String playerName, Matches match){
        String [] homeOrAway = statToCheck.split("-");
        if(homeOrAway[0].contains(playerName)){
            return match.getHometeam();
        } else {
            return match.getAwayteam();
        }
    }

    private boolean doesStatContainPlayerName(String stat, String playerName){

        if(stat == null) {
            return false;
        } else if(stat.contains(playerName)) {
            return true;
        } else {
            return false;
        }
    }


    /*
        TODO finish this
        Note: this functionality is only temporary - it is used to switch from goalscorers in matches table to separate table for goalscorers
     */

    @Autowired
    FifaPlayerDBRepository fifaPlayerDBRepository;

    @GetMapping("/generateImprovedFifaPlayers/insert")
    public void generateImprovedFifaPlayers(){
        List<Matches> matches = new ArrayList<>();
        matchesRepository.findAll().iterator().forEachRemaining(matches::add);

        matches.forEach(match->{
            // todo here check, goalscorers, yelllow and red cards and insert every new name into table (if name already there, do nothing)
        }
        );

        FifaPlayerDB test = new FifaPlayerDB();
        test.setPlayerName("test");
        test.setPlayerPosition("GK");
        //fifaPlayerDBRepository.save(test);
    }

}
