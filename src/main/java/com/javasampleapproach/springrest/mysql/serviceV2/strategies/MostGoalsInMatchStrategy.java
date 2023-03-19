package com.javasampleapproach.springrest.mysql.serviceV2.strategies;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import com.javasampleapproach.springrest.mysql.serviceV2.MatchesServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MostGoalsInMatchStrategy implements TopMatchesStrategy {

    @Autowired
    MatchesRepository matchesRepository;

    @Override
    public String getStrategyType() {
        return MyUtils.MOST_GOALS_IN_MATCH;
    }

    @Override
    public List<Matches> getMatches(String playerName, String competition, Long teamId) {
        return matchesRepository.getMatchesWithMostGoals(competition, teamId);
    }
}
