package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Team;
import com.javasampleapproach.springrest.mysql.model.TeamDto;
import com.javasampleapproach.springrest.mysql.model.TeamStats;
import com.javasampleapproach.springrest.mysql.model.TeamStatsWithMatches;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import com.javasampleapproach.springrest.mysql.repo.TeamRepository;
import com.javasampleapproach.springrest.mysql.serviceV2.MatchesServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    MatchesServiceV2 matchesService;

    public TeamStatsWithMatches getTeamStats(final long teamId) {
        final Team currentTeam = findById(teamId);
        final TeamStatsWithMatches teamStats = new TeamStatsWithMatches();
        teamStats.setTeamName(currentTeam.getTeamName());
        teamStats.setTeamId(teamId);
        final List<MatchesDTO> teamMatches = matchesService.getFilteredMatches(null, null, null, currentTeam.getId());
        teamStats.setMatches(teamMatches);

        for(MatchesDTO m : teamMatches)  {
            if(m.getCompetition().equalsIgnoreCase(MyUtils.CHAMPIONS_LEAGUE)){
                setWDLGoalsAndSeasons(m, teamStats.getTeamStatsCL(), teamId);
            } else {
                setWDLGoalsAndSeasons(m, teamStats.getTeamStatsEL(), teamId);
            }
        }

        setBilance(teamStats);
        teamStats.getTeamStatsEL().calculateGoalDiff();
        teamStats.getTeamStatsCL().calculateGoalDiff();
        teamStats.calculateTeamStatsTotal();

        return teamStats;
    }

    // TODO simplify this and unify with getAllTeamsIterable
    public List<Team> getAllTeams(){
        // todo do not return team object
        List<Team> allTeams = new ArrayList<>();
        teamRepository.findAll().forEach(allTeams::add);
        return allTeams;
    }


    // todo Check and re-work add mapper
    public List<TeamDto> getAllTeams(boolean recalculate) {

        List<TeamDto> allTeams = new ArrayList<>();

        teamRepository.findAll().forEach(team -> {
            TeamDto tdo = new TeamDto();
            tdo.setTeamName(team.getTeamName());
            tdo.setCountry(team.getCountry());
            tdo.setFirstSeasonCL(team.getFirstSeasonCL());
            tdo.setFirstSeasonEL(team.getFirstSeasonEL());
            tdo.setTeamId(team.getId());
            allTeams.add(tdo);
        });

        // todo add option to recalculat

        //        for(Team t : teams) {
        //            if(recalculate) {
        //                t.setFirstSeasonCL(matchesService.getFirstSeasonInCompetition(t.getTeamName(), MyUtils.CHAMPIONS_LEAGUE));
        //                t.setFirstSeasonEL(matchesService.getFirstSeasonInCompetition(t.getTeamName(), MyUtils.EUROPEAN_LEAGUE));
        //                teamRepository.save(t);
        //            }
        //            // check if this set can be omitted
        //            t.setFirstSeasonCL(setLabelToNeverIfNull(t.getFirstSeasonCL()));
        //            t.setFirstSeasonEL(setLabelToNeverIfNull(t.getFirstSeasonEL()));
        //        }

        return allTeams;
    }

    // re-work with map
    public List<String> getAllTeamNames() {
        List<String> teamNames = new ArrayList<>();
        teamRepository.findAll().forEach(p -> teamNames.add(p.getTeamName()));
        return teamNames;
    }

    public List<Team> allGlobalTeamStats() {
        List<Team> teams = getAllTeams();
        int numberOfTeamsPresentedInCL = 0;
        int numberOfTeamsPresentedInEL = 0;
        for(Team t : teams) {
            if (t.getFirstSeasonCL() == null) {
                t.setFirstSeasonCL("never");
            } else {
                numberOfTeamsPresentedInCL++;
            }

            if (t.getFirstSeasonEL() == null) {
                t.setFirstSeasonEL("never");
            } else {
                numberOfTeamsPresentedInEL++;
            }
        }

        return teams;
    }

    public String getTeamNameById(List<Team> allTeams, Long teamId){
        return allTeams.stream().filter(team-> team.getId() == teamId).map(team -> team.getTeamName()).findFirst().orElse(null);
    }

    public Team findByTeamName(String teamName){
        return teamRepository.findByTeamName(teamName);
    }

    // todo handle null
    public Team findById(Long teamId) {
        return teamRepository.findById(teamId).get();
    }

    private String setLabelToNeverIfNull(String firstSeasonInCompetition){
        if(firstSeasonInCompetition == null) {
            return "never";
        } else {
            return firstSeasonInCompetition;
        }
    }

    private void setBilance(TeamStatsWithMatches team){
        TeamStats statsCL = team.getTeamStatsCL();
        TeamStats statsEL = team.getTeamStatsEL();
        List<Integer> bilance = team.getBilance();

        // W
        bilance.add(statsCL.getWins() + statsEL.getWins());

        // D
        bilance.add(statsCL.getDraws() + statsEL.getDraws());

        // L
        bilance.add(statsCL.getLosses() + statsEL.getLosses());
    }

    private void setWDLGoalsAndSeasons(MatchesDTO m, TeamStats teamStats, long teamId) {
        // W-D-L setter
        if (m.getWinnerId() == MyUtils.DRAW_RESULT_ID) {
            teamStats.incrementDraws(1);
        } else if (m.getWinnerId() == teamId) {
            teamStats.incrementWins(1);
        } else {
            teamStats.incrementLosses(1);
        }

        // GS and GC setter
        if(m.getIdHomeTeam() == teamId) {
            teamStats.incrementGoalsScored(m.getScorehome());
            teamStats.incrementGoalsConceded(m.getScoreaway());
        } else if(m.getIdAwayTeam() == teamId) {
            teamStats.incrementGoalsScored(m.getScoreaway());
            teamStats.incrementGoalsConceded(m.getScorehome());
        }

        // seasons - represented as set - no duplicates
        teamStats.getSeasonsList().add(m.getSeason());

        // matches count
        teamStats.incrementMatchesCount(1);

        // finals
        if(m.getCompetitionPhase().equalsIgnoreCase(MyUtils.FINAL)){
            teamStats.incrementFinalMatchesCount(1);
            if(m.getWinnerId() == teamId){
                teamStats.incrementTitlesCount(1);
            } else {
                teamStats.incrementRunnersUpCount(1);
            }
        }
    }

}
