package com.javasampleapproach.springrest.mysql.serviceV2.strategies;

import com.javasampleapproach.springrest.mysql.entities.Matches;

import java.util.List;

public interface TopMatchesStrategy {
    public String getStrategyType();
    public List<Matches> getMatches(String playerName, String competition, Long teamId);
}
