package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goalscorer {
	private long playerId;
	private String name;
	private Integer totalGoalsCount;
	private int numberOfTeamsPlayerScoredFor;
	private String teamPlayerScoredFor; // this will be as map in future - goalsByTeams
}
