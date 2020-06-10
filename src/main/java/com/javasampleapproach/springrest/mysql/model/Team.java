package com.javasampleapproach.springrest.mysql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team")
public class Team {
	
	
	@Id
	@Column(name = "teamname")
	private String teamName;
	
	@Column(name = "firstseasonCL")
	private String firstSeasonCL;
	
	
	@Column(name = "firstseasonEL")
	private String firstSeasonEL;
	
	@Column(name = "country")
	private String country;	
}
