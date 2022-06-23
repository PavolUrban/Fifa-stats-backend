package com.javasampleapproach.springrest.mysql.entities;

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

	//rework this and use relations

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	
	@Column(name = "competition")
	private String competition;
	
	@Column(name = "competitionphase")
	private String competitionPhase;


// TODO both constructors probably can be deleted

	public Matches(String hometeam, String awayTeam, int scoreHome, int scoreAway, String playerH, String playerA, String season, String competition, String competitionPhase)
	{
		this.hometeam = hometeam;
		this.awayteam = awayTeam;
		this.scorehome = scoreHome;
		this.scoreaway = scoreAway;
		this.playerH = playerH;
		this.playerA = playerA;
		this.season = season;
		this.competition = competition;
		this.competitionPhase = competitionPhase;
	}

	public Matches(Matches matchWithoutId){
		this.hometeam = matchWithoutId.getHometeam();
		this.awayteam = matchWithoutId.getAwayteam();
		this.scorehome = matchWithoutId.getScorehome();
		this.scoreaway = matchWithoutId.getScoreaway();
		this.playerH = matchWithoutId.getPlayerH();
		this.playerA = matchWithoutId.getPlayerA();
		this.season = matchWithoutId.getSeason();
		this.competition = matchWithoutId.getCompetition();
		this.competitionPhase = matchWithoutId.getCompetitionPhase();
	}


}
