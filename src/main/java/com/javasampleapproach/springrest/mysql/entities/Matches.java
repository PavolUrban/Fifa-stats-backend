package com.javasampleapproach.springrest.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "matches")
public class Matches {

	//rework this and use relations

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "scorehome")
	private int scorehome;
	
	@Column(name = "scoreaway")
	private int scoreaway;

	@Column(name = "winner")
	private String winner; // TODO delete this once it will be removed from the DB

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idhometeam", referencedColumnName = "id")
	private Team homeTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idawayteam", referencedColumnName = "id")
	private Team awayTeam;

	@Column(name = "winnerid")
	private long winnerId; // todo rework to teamId

	@OneToMany(fetch = FetchType.LAZY, mappedBy="match")
	private List<RecordsInMatches> recordsInMatches;

	private String winnerPlayer;
}
