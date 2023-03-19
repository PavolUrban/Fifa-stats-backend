package com.javasampleapproach.springrest.mysql.serviceV2.strategies;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BiggestDrawsStrategy implements TopMatchesStrategy {

    @Autowired
    MatchesRepository matchesRepository;

    @Override
    public String getStrategyType() {
        return MyUtils.BIGGEST_DRAWS;
    }

    @Override
    public List<Matches> getMatches(String playerName, String competition, Long teamId) {
        return matchesRepository.getBiggestDraws(competition, teamId);
    }
}