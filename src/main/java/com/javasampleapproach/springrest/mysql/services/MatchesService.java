package com.javasampleapproach.springrest.mysql.services;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchesService {

    @Autowired
    MatchesRepository matchesRepository;

    public List<Matches> getTeamMatchesById(long teamId){
        return matchesRepository.getAllMatchesForTeam(teamId);
    }

    public String getFirstSeasonInCompetition(String teamName, String competition) {
        return matchesRepository.firstSeasonInCompetition(teamName, competition);
    }

}
