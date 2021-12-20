package statsCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.model.TeamGlobalStats;

public class SingleSeasonStats {
	
	
	public static Map<String, Object> compute(List<Matches> matches, String team)
	{
		
		Map<String, Object> result = new HashMap();
		
		
		result.put("Round of 16",null);
		result.put("quarterfinals",null);
		result.put("semifinals",null);
		result.put("finals",null);
		
		//Map<String, Integer> ll = new HashMap< >();
	
		//Iterable<Matches> groupStage = matches.stream().filter(o -> o.getCompetitionPhase().contains("GROUP")).collect(Collectors.toList());
		
		Iterable<Matches> roundOf16 = matches.stream().filter(o -> o.getCompetitionPhase().contains("Round of 16")).collect(Collectors.toList());
		Iterable<Matches> quarterfinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Quarterfinals")).collect(Collectors.toList());
		Iterable<Matches> semifinals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Semifinals")).collect(Collectors.toList());
		List<Matches> finals = matches.stream().filter(o -> o.getCompetitionPhase().contains("Final")).sorted((x1,x2)-> x2.getSeason().compareTo(x1.getSeason())).collect(Collectors.toList());
		
	
		
		//System.out.println("\nGroup");
		//System.out.println(groupStage);
		System.out.println("\nroundOf16");
		System.out.println(roundOf16);
		System.out.println("\nQuartefinals");
		System.out.println(quarterfinals);
		System.out.println("\nSemifinals");
		System.out.println(semifinals);
		System.out.println("\nFinals");
	
		
		TeamGlobalStats teamGlobalStats = new TeamGlobalStats();
		teamGlobalStats.setFinalsPlayed(finals.size());
		
		int winsInFinal = 0;
		for(Matches finale : finals)
		{
			if(finale.getWinner().equalsIgnoreCase(team))
				winsInFinal++;
		}
		
		teamGlobalStats.setTotalWinsCL(winsInFinal);
		teamGlobalStats.setFinalsPlayed(finals.size());
		
		
		result.put("final",finals);
		result.put("finalStats", teamGlobalStats);
		
		return result;
	
		
	}

}
