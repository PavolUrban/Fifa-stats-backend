package com.javasampleapproach.springrest.mysql.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerStats {
	
	private int wins;
	private int draws;
	private int losses;
	
	private int goalsScored;
	private int goalsConceded;
	private List<Integer> totalBilance;

	private int numberOfCLTitles;
	private int numberOfELTitles;
	
	
	public PlayerStats()
	{
		this.wins = 0;
		this.draws = 0;
		this.losses = 0;
		this.goalsConceded = 0;
		this.goalsScored = 0;
		this.numberOfCLTitles = 0;
		this.numberOfELTitles = 0;
		this.totalBilance = new ArrayList<>();
	}
}
