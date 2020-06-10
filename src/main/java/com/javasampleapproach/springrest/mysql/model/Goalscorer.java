package com.javasampleapproach.springrest.mysql.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goalscorer {
	private String name;
	private Integer totalGoalsCount;
	private Map<String, Integer> goalsByTeams;
}
