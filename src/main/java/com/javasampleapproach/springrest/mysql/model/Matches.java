package com.javasampleapproach.springrest.mysql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "matches")
public class Matches {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "hometeam")
	private String hometeam;
	
	@Column(name = "awayteam")
	private String awayteam;
	
	@Column(name = "scorehome")
	private int scorehome;
	
	@Column(name = "scoreaway")
	private int scoreaway;
	
	@Column(name = "winner")
	private String winner;
	
	@Column(name = "season")
	private String season;
	
	@Column(name = "playerh")
	private String playerH;
	
	@Column(name = "playera")
	private String playerA;
	
	@Column(name = "goalscorers")
	private String goalscorers;
	
	@Column(name = "yellowcards")
	private String yellowcards;
	
	@Column(name = "redcards")
	private String redcards;
	
	
	@Column(name = "competition")
	private String competition;
	
	@Column(name = "competitionphase")
	private String competitionPhase;

	
	
	
	public Matches(String hometeam, String awayTeam, int scoreHome, int scoreAway, String competition, String competitionPhase)
	{
		this.hometeam = hometeam;
		this.awayteam = awayTeam;
		this.scorehome = scoreHome;
		this.scoreaway = scoreAway;
		this.competition = competition;
		this.competitionPhase = competitionPhase;
		
	}
	
	
}
