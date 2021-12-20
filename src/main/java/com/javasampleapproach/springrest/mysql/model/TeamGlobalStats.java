package com.javasampleapproach.springrest.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamGlobalStats {

	
	private int finalsPlayed;
	private int totalWinsCL;
	
	private int semifinalsPlayed;
	private int semifinalsQualificationCount;
	
	private int quarterfinalsPlayed;
	private int quarterfinalsQualificationCount;
	
	private int roundOf16Played;
	private int roundOf16PlayedQualificationCount;
	
}
