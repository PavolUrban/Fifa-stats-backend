package com.javasampleapproach.springrest.mysql.model.seasons;

import lombok.Data;

@Data
public class TableTeam {
	private String teamname;
	private long teamId;
	private int matches; // todo rename to matchesCount
	private int wins;
	private int draws;
	private int losses;
	
	private int goalsScored;
	private int goalsConceded;
	private Integer points;
	private int placeInGroup;
	private String ownedByPlayer;
}