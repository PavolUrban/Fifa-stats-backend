package com.javasampleapproach.springrest.mysql.model.matches;

import Utils.MyUtils;
import lombok.Data;

import java.util.List;

@Data
public class DataToCreateMatchV2 {
    private List<String> competitionsList = MyUtils.competitionsList;
    private List<String> playerNamesList = MyUtils.playerNamesList;
    private List<String> allCompetitionPhases = MyUtils.europeanLeagueStagesList; // european league has all the available phases
    private List<String> seasonsList;
    private List<TeamDTOCreateDialog> teams;
}
