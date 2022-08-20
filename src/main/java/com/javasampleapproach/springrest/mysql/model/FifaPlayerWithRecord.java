package com.javasampleapproach.springrest.mysql.model;

public interface FifaPlayerWithRecord {

    Integer getPlayerId();

    String getPlayerName();

    Integer getRecordEventCount();

    String getMatchId();

    String getHomeTeam();

    String getScore();

    String getAwayTeam();

    String getTeamName();

    String getSeason();
}
