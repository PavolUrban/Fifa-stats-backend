package com.javasampleapproach.springrest.mysql.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.springrest.mysql.model.FileModel;
import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.PlayOffMatch;
import com.javasampleapproach.springrest.mysql.model.TableTeam;
import com.javasampleapproach.springrest.mysql.repo.FileRepository;
import com.javasampleapproach.springrest.mysql.repo.MatchesRepository;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/completeSeasons")
public class SeasonsController {
	
	@Autowired
	MatchesRepository matchesRepository;
	
	@Autowired
	FileRepository fileRepository;
	
	//completeSeasons/getAllPhases/FIFA20/CL
	@GetMapping("/getAllPhases/{season}/{competition}")
	public Map<String, List<TableTeam>> getAllCustomers(@PathVariable("season") String season, @PathVariable("competition") String competition) {

		Map<String, List<TableTeam>> groupsWithTeams = new HashMap<String, List<TableTeam>>();
		
		List<Matches> matches = matchesRepository.getAllMatchesBySeasonAndCompetitionGroupStage(season, competition);

		Set<String> groupNames = new HashSet<String>();
		
		matches.stream().filter(p ->  groupNames.add(p.getCompetitionPhase())).collect(Collectors.toList());
		
		
		
		
		for(String groupName : groupNames)
		{
			Set<String> set = new HashSet<>(matches.size());
//			matches.stream().filter(p -> p.getCompetitionPhase().equalsIgnoreCase("GROUP A") ?  set.add(p.getAwayteam()) : set.add("picovina")).collect(Collectors.toList());
			matches.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(groupName)).filter(o -> set.add(o.getAwayteam())).collect(Collectors.toList());
			
			List<TableTeam> allTeamsInCurrentGroup = new ArrayList();
			for(String team : set)
			{	
				TableTeam tableTeam = new TableTeam();
				List<Matches> allMatchesByTeam = matches.stream().filter(s -> team.equalsIgnoreCase(s.getHometeam()) || s.getAwayteam().equalsIgnoreCase(team) ).collect(Collectors.toList());
				
				int sumScoredHome = allMatchesByTeam.stream().filter(o -> o.getHometeam().equalsIgnoreCase(team)).mapToInt(o -> o.getScorehome()).sum();
				int sumScoredAway = allMatchesByTeam.stream().filter(o -> o.getAwayteam().equalsIgnoreCase(team)).mapToInt(o -> o.getScoreaway()).sum();
				int sumConcededHome = allMatchesByTeam.stream().filter(o -> o.getHometeam().equalsIgnoreCase(team)).mapToInt(o -> o.getScoreaway()).sum();
				int sumConcededAway = allMatchesByTeam.stream().filter(o -> o.getAwayteam().equalsIgnoreCase(team)).mapToInt(o -> o.getScorehome()).sum();
				
				long wins = allMatchesByTeam.stream().filter(p-> p.getWinner().equalsIgnoreCase(team)).count();
				long draws = allMatchesByTeam.stream().filter(p-> p.getWinner().equalsIgnoreCase("D")).count();
				long losses = allMatchesByTeam.size()- wins - draws;
					
				tableTeam.setTeamname(team);
				tableTeam.setWins((int) wins);
				tableTeam.setDraws((int) draws);
				tableTeam.setLosses((int) losses);
				tableTeam.setMatches(tableTeam.getWins() + tableTeam.getDraws() + tableTeam.getLosses());
				tableTeam.setGoalsScored(sumScoredHome + sumScoredAway);
				tableTeam.setGoalsConceded(sumConcededHome + sumConcededAway);
				tableTeam.setPoints(tableTeam.getWins()*3 + tableTeam.getDraws()*1);
		
				FileModel test = fileRepository.findByTeamname(team);
				if(test !=null)
					tableTeam.setLogo(test);
				
	
				allTeamsInCurrentGroup.add(tableTeam);
			}
			
			Collections.sort(allTeamsInCurrentGroup, (o1, o2) -> o2.getPoints().compareTo(o1.getPoints()));
			
			groupsWithTeams.put(groupName, allTeamsInCurrentGroup);
		
		}
	
		getPlayOffs(season, competition);

		return groupsWithTeams;
	}
	
	@GetMapping("/getAllPlayOff/{season}/{competition}")
	private Map<String, List<PlayOffMatch>> getPlayOffs(@PathVariable("season") String season, @PathVariable("competition") String competition)
	{
		List<Matches> matches = matchesRepository.getAllMatchesBySeasonAndCompetitionPlayOffs(season, competition);

		Set<String> phases = new HashSet<String>();
		
		matches.stream().filter(p ->  phases.add(p.getCompetitionPhase())).collect(Collectors.toList());
		
		
		Map<String, List<PlayOffMatch>> matchesInAllPhases = new HashMap<String, List<PlayOffMatch>>();
		
		for(String phase: phases)
		{
			
			List<Matches> matchesInCurrentPhase = new ArrayList<>();
			matches.stream().filter(p ->  p.getCompetitionPhase().equalsIgnoreCase(phase)).filter(o -> matchesInCurrentPhase.add(o)).collect(Collectors.toList());
			
			List<PlayOffMatch> playOffMatches = new ArrayList<>();
			
			if( !phase.equalsIgnoreCase("Final") )// final is played only as single match not classic play off with two matches
			{
				
				int addedMatches = 0;
				
				for(Matches m : matchesInCurrentPhase)
				{
					
					
					if(addedMatches >= matchesInCurrentPhase.size())
						break;
					
					Matches match1 = matchesInCurrentPhase.get(addedMatches);
					Matches match2 = matchesInCurrentPhase.get(addedMatches+1);
					

					ArrayList<String> teamNames = new ArrayList<>(Arrays.asList(match1.getHometeam(), match1.getAwayteam()));
					String qualifiedTeam = whoIsQualified(match1, match2);
					String nonQualifiedTeam = getNonQualified(teamNames, qualifiedTeam);
			
					PlayOffMatch playOff = new PlayOffMatch(match1, match2, qualifiedTeam, nonQualifiedTeam);
					
					playOff.setQualifiedTeamGoals(getPlayOffGoalsForTeam(playOff, qualifiedTeam));
					playOff.setNonQualifiedTeamGoals(getPlayOffGoalsForTeam(playOff, nonQualifiedTeam));
					
					playOffMatches.add(playOff);
					addedMatches = addedMatches + 2;
				}
			}
			
			
			matchesInAllPhases.put(phase, playOffMatches);
			
		}
		
		
		return matchesInAllPhases;
		
	}

	
	private int getPlayOffGoalsForTeam(PlayOffMatch pom, String teamToGetScore) 
	{
		int score = 0;
		
		if(teamToGetScore.equalsIgnoreCase(pom.getFirstMatch().getHometeam()))
			score = pom.getFirstMatch().getScorehome() + pom.getSecondMatch().getScoreaway();
		
		else if(teamToGetScore.equalsIgnoreCase(pom.getFirstMatch().getAwayteam()))
			score = pom.getFirstMatch().getScoreaway() + pom.getSecondMatch().getScorehome();
		
		return score;
	}
	
	private String getNonQualified(List<String>teamNames, String qualified)
	{
		teamNames.remove(qualified);
		
		return teamNames.get(0);
	}
	
	//TODO check if this function works when two teams have equall scores from 2 matches!!!
	private String whoIsQualified(Matches match1, Matches match2)
	{
		String qualifiedTeam = "unknown";
		
		int firstTeamGoals = match1.getScorehome() + match2.getScoreaway();
		int secondTeamGoals = match2.getScorehome() + match1.getScoreaway() ;
		
		if(firstTeamGoals > secondTeamGoals)
			qualifiedTeam = match1.getHometeam();
		
		else if(secondTeamGoals > firstTeamGoals)
			qualifiedTeam = match2.getHometeam();
		
		else if(firstTeamGoals == secondTeamGoals)
		{
			if(match1.getScoreaway() > match2.getScoreaway())
				qualifiedTeam = match1.getAwayteam();
			
			else
				qualifiedTeam = match2.getAwayteam();
		}
		
		return qualifiedTeam;
	}
}
