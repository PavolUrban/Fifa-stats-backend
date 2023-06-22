package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goalscorer {
	private long playerId;
	private String name;
	private Integer totalGoalsCount = 0;
	private int numberOfTeamsPlayerScoredFor;
	private String teamPlayerScoredFor; // this will be as map in future - goalsByTeams
}
