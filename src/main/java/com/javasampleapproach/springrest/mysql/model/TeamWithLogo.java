package com.javasampleapproach.springrest.mysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamWithLogo extends Team{
	private String teamName;
	private String firstSeasonCL;
	private String firstSeasonEL;
	private String country;
	private FileModel fm;
	
	public TeamWithLogo(String teamName, String firstSeasonCL, String firstSeasonEL, String country) {
	    super(teamName, firstSeasonCL, firstSeasonEL, country);
	}
	
	public TeamWithLogo(Team t)
	{
		this.teamName = t.getTeamName();
		this.firstSeasonCL = t.getFirstSeasonCL();
		this.firstSeasonEL = t.getFirstSeasonEL();
		this.country = t.getCountry();
	}

}
