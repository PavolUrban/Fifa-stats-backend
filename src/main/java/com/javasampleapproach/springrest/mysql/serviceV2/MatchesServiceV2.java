package com.javasampleapproach.springrest.mysql.serviceV2;

import Utils.HelperMethods;
import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.MatchesDTO;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchesServiceV2 {

    @Autowired
    MatchesRepository matchesRepository;

    public List<MatchesDTO> getAllMatches() {
        List<Matches> matches = (List<Matches>) matchesRepository.findAll();
        return doMapping(matches);
    }

    public List<MatchesDTO> getCustomGroupMatches(String competition, String season, String competitionPhase) {
        List<Matches> matches = matchesRepository.findByCompetitionAndSeasonAndCompetitionPhase(competition, season, competitionPhase);
        return doMapping(matches);
    }

    private List<MatchesDTO> doMapping(List<Matches> matches) {
        List<MatchesDTO> newMatches =  new ArrayList<>();

        matches.forEach(m-> {
            MatchesDTO newMatch = new MatchesDTO();
            newMatch.setId(m.getId());
            newMatch.setHometeam(m.getHomeTeam().getTeamName());
            newMatch.setAwayteam(m.getAwayTeam().getTeamName());
            newMatch.setScorehome(m.getScorehome());
            newMatch.setScoreaway(m.getScoreaway());
            newMatch.setSeason(m.getSeason());
            newMatch.setPlayerH(m.getPlayerH());
            newMatch.setPlayerA(m.getPlayerA());
            newMatch.setCompetition(m.getCompetition());
            newMatch.setCompetitionPhase(m.getCompetitionPhase());
            newMatch.setWinnerPlayer(HelperMethods.getWinnerPlayer(m));
            // todo
            // newMatch.setWinner(HelperMethods.whoIsWinnerOfMatchV2(m));
            newMatches.add(newMatch);
        });
        return newMatches;
    }
}
