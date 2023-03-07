package com.javasampleapproach.springrest.mysql.entities;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team")
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "teamname")
	private String teamName;
	
	@Column(name = "firstseasonCL")
	private String firstSeasonCL;
	
	@Column(name = "firstseasonEL")
	private String firstSeasonEL;
	
	@Column(name = "country")
	private String country;

	@OneToMany(fetch = FetchType.LAZY, mappedBy="team")
	@JsonManagedReference
	private Set<RecordsInMatches> recordsInMatches;

	@OneToMany(mappedBy="homeTeam", fetch = FetchType.LAZY)
	private List<Matches> homeMatches;

	@OneToMany(mappedBy="awayTeam", fetch = FetchType.LAZY)
	private List<Matches> awayMatches;
}
