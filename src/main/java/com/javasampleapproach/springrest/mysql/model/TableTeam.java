package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TableTeam {

	private String teamname;
	private int matches;
	private int wins;
	private int draws;
	private int losses;
	
	private int goalsScored;
	private int goalsConceded;
	private Integer points;
	private int placeInGroup;
	private String ownedByPlayer;

	private FileModel logo;
}
