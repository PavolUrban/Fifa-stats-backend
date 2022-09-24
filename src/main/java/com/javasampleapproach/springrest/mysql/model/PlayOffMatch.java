package com.javasampleapproach.springrest.mysql.model;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PlayOffMatch {

	List<Matches> matchesList = new ArrayList<>();
	String qualifiedTeam;
	int qualifiedTeamGoals;
	String nonQualifiedTeam;
	int nonQualifiedTeamGoals;
	String qualifiedPlayer;
	long qualifiedTeamId;
	long nonQualifiedTeamId;

	public PlayOffMatch(List<Matches> matchesList, String qualifiedTeam, long qualifiedTeamId, String nonQualifiedTeam, long nonQualifiedTeamId, String qualifiedPlayer)
	{
		this.matchesList = matchesList;
		this.qualifiedTeam  = qualifiedTeam;
		this.nonQualifiedTeam = nonQualifiedTeam;
		this.qualifiedPlayer = qualifiedPlayer;
		this.qualifiedTeamId = qualifiedTeamId;
		this.nonQualifiedTeamId = nonQualifiedTeamId;
	}
}
