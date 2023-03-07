package com.javasampleapproach.springrest.mysql.model;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlayOffMatch {

	// List<Matches> matchesList = new ArrayList<>();
	String qualifiedTeam;
	int qualifiedTeamGoals;
	String nonQualifiedTeam;
	int nonQualifiedTeamGoals;
	String qualifiedPlayer;
//	long qualifiedTeamId;
//	long nonQualifiedTeamId;

//	List<Matches> matchesList, add to constructor?
	public PlayOffMatch( String qualifiedTeam, String nonQualifiedTeam, String qualifiedPlayer)
	{
		this.qualifiedTeam  = qualifiedTeam;
		this.nonQualifiedTeam = nonQualifiedTeam;
		this.qualifiedPlayer = qualifiedPlayer;
	}
}
