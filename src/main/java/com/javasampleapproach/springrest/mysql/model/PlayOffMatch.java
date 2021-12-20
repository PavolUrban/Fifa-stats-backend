package com.javasampleapproach.springrest.mysql.model;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayOffMatch {

	Matches firstMatch;
	Matches secondMatch;
	
	String qualifiedTeam;
	int qualifiedTeamGoals;
	
	String nonQualifiedTeam;
	int nonQualifiedTeamGoals;
	
	public PlayOffMatch(Matches first, Matches second, String qualifiedTeam, String nonQualifiedTeam)
	{
		this.firstMatch = first;
		this.secondMatch = second;
		this.qualifiedTeam  = qualifiedTeam;
		this.nonQualifiedTeam = nonQualifiedTeam;
	}
}
