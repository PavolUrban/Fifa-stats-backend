package com.javasampleapproach.springrest.mysql.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TeamStats {

	// values that are not directly presented in db and must be computed
		Map<String, Map<String, Integer>> matchesStats;
		
		
		Map<String, Map<String, Integer>> goalScorers;
		Map<String, Map<String, Integer>> yellowCards;
		
		Map<String, Map<String, Integer>> redCards;
		
		FileModel fm;
		
		List<Matches> matches;
		
	public TeamStats()
	{
		this.matchesStats = new HashMap<>();
		this.matches = new ArrayList<>();
		this.goalScorers = new HashMap<String, Map<String,Integer>>();
		this.yellowCards = new HashMap<String, Map<String,Integer>>();
		this.redCards = new HashMap<String, Map<String,Integer>>();	
	}
		
		//TODO logo id
}
