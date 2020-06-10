package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopTeam {
	private String teamname;
	private Integer wins;
	private Integer losses;
	private Integer draws;
	private Integer matches;
	private Integer goalsScored;
	private Integer goalsConceded;
	private Integer goalDiff;
	private String country;
	
	FileModel fm;
}
