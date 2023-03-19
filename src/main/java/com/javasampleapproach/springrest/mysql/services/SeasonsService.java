package com.javasampleapproach.springrest.mysql.services;

import Utils.MyUtils;
import com.javasampleapproach.springrest.mysql.entities.Matches;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.seasons.GroupStage;
import com.javasampleapproach.springrest.mysql.model.seasons.H2HPlayers;
import com.javasampleapproach.springrest.mysql.model.OverallStats;
import com.javasampleapproach.springrest.mysql.model.seasons.H2HV2;
import com.javasampleapproach.springrest.mysql.model.seasons.PlayOffDoubleMatch;
import com.javasampleapproach.springrest.mysql.model.seasons.PlayOffRound;
import com.javasampleapproach.springrest.mysql.model.seasons.PlayOffStage;
import com.javasampleapproach.springrest.mysql.model.seasons.SeasonWrapper;
import com.javasampleapproach.springrest.mysql.model.seasons.SingleGroup;
import com.javasampleapproach.springrest.mysql.model.seasons.TableTeam;
import com.javasampleapproach.springrest.mysql.model.matches.MatchesDTO;
import com.javasampleapproach.springrest.mysql.repo.RecordsInMatchesRepository;
import com.javasampleapproach.springrest.mysql.repo.SeasonsRepository;
import com.javasampleapproach.springrest.mysql.serviceV2.MatchesServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static Utils.MyUtils.ALL_COMPETITION_PHASES;
import static Utils.MyUtils.ALL_GROUP_PHASES;
import static Utils.MyUtils.ALL_PLAY_OFF_PHASES;
import static Utils.MyUtils.FINAL;
import static java.util.stream.Collectors.groupingBy;

@Service
public class SeasonsService {

    @Autowired
    RecordsInMatchesRepository recordsInMatchesRepository;

    @Autowired
    MatchesServiceV2 matchesServiceV2;

    @Autowired
    SeasonsRepository seasonsRepository;

    @Autowired
    FifaPlayerService fifaPlayerService;

    public SeasonWrapper getAllPhasesForSeasonAndCompetitionV2(final String season, final String competition) {
        final SeasonWrapper wholeSeason = new SeasonWrapper();

        // consider replacing getAllMatchesBySeasonAndCompetitionGroupStage
        final List<Matches> allGroupStageMatches = matchesServiceV2.getAllMatchesBySeasonCompetitionAndCompetitionPhaseIn(season, competition, ALL_GROUP_PHASES);
        final GroupStage groupStage = getGroupStage(allGroupStageMatches);
        wholeSeason.setGroupStage(groupStage);

        final List<Matches> playOffMatches = matchesServiceV2.getAllMatchesBySeasonCompetitionAndCompetitionPhaseIn(season, competition, ALL_PLAY_OFF_PHASES);
        final PlayOffStage playOffs = getPlayOffStage(playOffMatches);
        wholeSeason.setPlayOffs(playOffs);

        // Goalscorers
        final List<RecordsInMatches> allGoalsInGroupStage = getGoalscorerRecordsFromMatchesList(allGroupStageMatches);
        wholeSeason.setTopGoalscorersGroupStage(fifaPlayerService.getGoalscorers(allGoalsInGroupStage));

        final List<RecordsInMatches> allGoalsInPlayOffs = getGoalscorerRecordsFromMatchesList(playOffMatches);
        wholeSeason.setTopGoalsScorersPlayOffs(fifaPlayerService.getGoalscorers(allGoalsInPlayOffs));

        final List<Matches> allMatchesInSeason = matchesServiceV2.getAllMatchesBySeasonCompetitionAndCompetitionPhaseIn(season, competition, ALL_COMPETITION_PHASES);
        final List<RecordsInMatches> allGoalsInSeason = getGoalscorerRecordsFromMatchesList(allMatchesInSeason);
        wholeSeason.setTopGoalsScorersTotal(fifaPlayerService.getGoalscorers(allGoalsInSeason));

        return wholeSeason;
    }

    public List<String> getAvailableSeasonsList() {
        return seasonsRepository.getAvailableSeasonsList();
    }

    private List<RecordsInMatches> getGoalscorerRecordsFromMatchesList(final List<Matches> matchesList) {
        return matchesList
                .stream()
                .flatMap(matches1 -> matches1.getRecordsInMatches()
                        .stream()
                        .filter(r-> r.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_PENALTY) || r.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_GOAL)))
                .collect(Collectors.toList());
    }

    private String getTeamNameById(Long teamId, Matches match) {
        return match.getHomeTeam().getId() == teamId ? match.getHomeTeam().getTeamName() : match.getAwayTeam().getTeamName();
    }

    // add this to calculatedTables sql table and read it easily
    private List<TableTeam> getGroupTable(List<Matches> matchesInGroup) {
        final Set<Long> teamIdsInCurrentGroup = new HashSet<>();
        matchesInGroup.forEach(match ->{
            teamIdsInCurrentGroup.add(match.getHomeTeam().getId());
            teamIdsInCurrentGroup.add(match.getAwayTeam().getId());
        });

        final List<TableTeam> allTeamsInCurrentGroup = new ArrayList<>();
        for(Long teamId : teamIdsInCurrentGroup) {
            final TableTeam tableTeam = new TableTeam();
            final List<Matches> allMatchesByTeam = matchesInGroup.stream()
                    .filter(s -> teamId == s.getHomeTeam().getId() || teamId == s.getAwayTeam().getId())
                    .collect(Collectors.toList());

            int sumScoredHome = allMatchesByTeam.stream().filter(o -> o.getHomeTeam().getId() == teamId).mapToInt(Matches::getScorehome).sum();
            int sumScoredAway = allMatchesByTeam.stream().filter(o -> o.getAwayTeam().getId() == teamId).mapToInt(Matches::getScoreaway).sum();
            int sumConcededHome = allMatchesByTeam.stream().filter(o -> o.getHomeTeam().getId() == teamId).mapToInt(Matches::getScoreaway).sum();
            int sumConcededAway = allMatchesByTeam.stream().filter(o -> o.getAwayTeam().getId() == teamId).mapToInt(Matches::getScorehome).sum();

            long wins = allMatchesByTeam.stream().filter(p-> p.getWinnerId() == teamId).count();
            long draws = allMatchesByTeam.stream().filter(p-> p.getWinnerId() == MyUtils.DRAW_RESULT_ID).count();
            long losses = allMatchesByTeam.size()- wins - draws;

            final String teamName = getTeamNameById(teamId, allMatchesByTeam.get(0));
            tableTeam.setTeamname(teamName);
            tableTeam.setTeamId(teamId);
            tableTeam.setWins((int) wins);
            tableTeam.setDraws((int) draws);
            tableTeam.setLosses((int) losses);
            tableTeam.setMatches(tableTeam.getWins() + tableTeam.getDraws() + tableTeam.getLosses());
            tableTeam.setGoalsScored(sumScoredHome + sumScoredAway);
            tableTeam.setGoalsConceded(sumConcededHome + sumConcededAway);
            tableTeam.setPoints(tableTeam.getWins() * 3 + tableTeam.getDraws());


            //todo for this table TeamsOwnerBySeason is prepared - USE IT SOON!
            long currentTeamMatchesByPavolJay =
                    allMatchesByTeam.stream()
                            .filter(match-> (match.getHomeTeam().getId() == teamId && match.getPlayerH().equalsIgnoreCase(MyUtils.PAVOL_JAY)) ||
                                    (match.getAwayTeam().getId() == teamId && match.getPlayerA().equalsIgnoreCase(MyUtils.PAVOL_JAY))
                            ).count();

            double playedByPavolJayPercentage = currentTeamMatchesByPavolJay/ (allMatchesByTeam.size() * 1.0);

            if (playedByPavolJayPercentage>0.6){
                tableTeam.setOwnedByPlayer(MyUtils.PAVOL_JAY);
            } else {
                tableTeam.setOwnedByPlayer(MyUtils.KOTLIK);
            }

            allTeamsInCurrentGroup.add(tableTeam);
        }

        allTeamsInCurrentGroup.sort((o1, o2) -> o2.getPoints().compareTo(o1.getPoints()));

        return allTeamsInCurrentGroup;
    }

    // todo calculate tables here
    private GroupStage getGroupStage(final List<Matches> allGroupStageMatches) {
        final Map<String, List<Matches>> matchesPerGroup = allGroupStageMatches.stream()
                .collect(groupingBy(Matches::getCompetitionPhase));

        final GroupStage groupStage = new GroupStage();

        matchesPerGroup.forEach((groupName, matches)-> {
            final SingleGroup group = new SingleGroup();
            group.setGroupName(groupName);

            final List<RecordsInMatches> allGoalsInCurrentGroup = getGoalscorerRecordsFromMatchesList(matches);
            group.setGoalscorersList(fifaPlayerService.getGoalscorers(allGoalsInCurrentGroup));

            final List<MatchesDTO> matchesWithWinners = matchesServiceV2.mapToMatchesDTO(matches);
            H2HV2 h2h = new H2HV2();
            long pavolJayWinsCount = matchesWithWinners.stream().filter(match-> match.getWinnerPlayer().equalsIgnoreCase(MyUtils.PAVOL_JAY)).count();
            long kotlikWinsCount = matchesWithWinners.stream().filter(match-> match.getWinnerPlayer().equalsIgnoreCase(MyUtils.KOTLIK)).count();
            long drawsCount = matchesWithWinners.stream().filter(match-> match.getWinnerPlayer().equalsIgnoreCase(MyUtils.RESULT_DRAW)).count();
            h2h.setPavolJay(pavolJayWinsCount);
            h2h.setKotlik(kotlikWinsCount);
            h2h.setDraws(drawsCount);
            group.setH2hPlayers(h2h);

            final List<TableTeam> groupTable = getGroupTable(matches);
            group.setGroupTable(groupTable);

            groupStage.getGroupsList().add(group);
        });


        return groupStage;
    }

    private PlayOffStage getPlayOffStage(List<Matches> playOffMatches) {
        final PlayOffStage playOffsStage = new PlayOffStage();

        final Map<String, List<Matches>> matchesPerPlayOffStage = playOffMatches.stream()
                .filter(matches -> !matches.getCompetitionPhase().equalsIgnoreCase(FINAL))
                .collect(groupingBy(Matches::getCompetitionPhase));

        matchesPerPlayOffStage.forEach((playOffStage, matches)-> {
            final PlayOffRound playOffRound = new PlayOffRound();
            playOffRound.setRoundName(playOffStage);

            List<String> toRenameLeadingTeamNames = new ArrayList<>();
            matches.forEach(match-> {
                String home = match.getHomeTeam().getTeamName();
                String away = match.getAwayTeam().getTeamName();
                if (!toRenameLeadingTeamNames.contains(away)) {
                    toRenameLeadingTeamNames.add(home);
                }
            });

            toRenameLeadingTeamNames.forEach(leadingTeamName-> {
                final List<Matches> matchesForLeadingTeam = matches.stream().filter(m-> m.getHomeTeam().getTeamName().equalsIgnoreCase(leadingTeamName) || m.getAwayTeam().getTeamName().equalsIgnoreCase(leadingTeamName)).collect(Collectors.toList());
                final List<MatchesDTO> mappedMatches = matchesServiceV2.mapToMatchesDTO(matchesForLeadingTeam);
                if (mappedMatches.size() == 2) {
                    final PlayOffDoubleMatch playOffDoubleMatch = getPlayOffDoubleMatchData(mappedMatches.get(0), mappedMatches.get(1));
                    playOffRound.getDuels().add(playOffDoubleMatch);
                    // todo h2h
                }
            });


            playOffsStage.getPlayOffRounds().add(playOffRound);
        });

        return playOffsStage;
    }



    private void setCardsForOverallStats(List<Integer> cardsCount, String season, String competition, String cardType){
        int numberOfCardsInGroupStage = recordsInMatchesRepository.getGroupStageCardsCountBySeasonAndCompetition(season, competition,cardType);
        int numberOfCardsInPlayOffs = recordsInMatchesRepository.getPlayOffsCardsCountBySeasonAndCompetition(season, competition, cardType);
        cardsCount.add(numberOfCardsInGroupStage);
        cardsCount.add(numberOfCardsInPlayOffs);
        cardsCount.add(numberOfCardsInGroupStage + numberOfCardsInPlayOffs);
    }

    private OverallStats getOverallStats(int matchesGroupStageCount, int matchesPlayOffsCount, int goalsGroupStageCount, int goalsPlayOffsCount, Map<String, H2HPlayers> groupStatsForPlayers, Map<String, Integer> playoffStats) {
        OverallStats os = new OverallStats();

        // matches counts
        os.getMatchesCount().add(matchesGroupStageCount);
        os.getMatchesCount().add(matchesPlayOffsCount);
        os.getMatchesCount().add(matchesGroupStageCount + matchesPlayOffsCount);

        // goals counts
        os.getGoalsCount().add(goalsGroupStageCount);
        os.getGoalsCount().add(goalsPlayOffsCount);
        os.getGoalsCount().add(goalsGroupStageCount + goalsPlayOffsCount);

        H2HPlayers h2hGroupStage = new H2HPlayers();
        groupStatsForPlayers.forEach((group, h2h)-> {
            h2hGroupStage.setKotlik(h2hGroupStage.getKotlik() + h2h.getKotlik());
            h2hGroupStage.setPavolJay(h2hGroupStage.getPavolJay() + h2h.getPavolJay());
            h2hGroupStage.setDraws(h2hGroupStage.getDraws() + h2h.getDraws());
        });
        os.getH2hPlayers().add(h2hGroupStage);

        // todo this will have to be updated to String, H2H to have stats from distinct playoff stage
        H2HPlayers h2hPlayOffs = new H2HPlayers();
        h2hPlayOffs.setPavolJay(playoffStats.get(MyUtils.PAVOL_JAY));
        h2hPlayOffs.setKotlik(playoffStats.get(MyUtils.KOTLIK));
        h2hPlayOffs.setDraws(playoffStats.get("Draws"));
        os.getH2hPlayers().add(h2hPlayOffs);

        H2HPlayers h2hTotal = new H2HPlayers();
        h2hTotal.setPavolJay(h2hGroupStage.getPavolJay() + h2hPlayOffs.getPavolJay());
        h2hTotal.setKotlik(h2hGroupStage.getKotlik() + h2hPlayOffs.getKotlik());
        h2hTotal.setDraws(h2hGroupStage.getDraws() + h2hPlayOffs.getDraws());
        os.getH2hPlayers().add(h2hTotal);

        return os;
    }


    // todo latest fix this
	private int getPlayOffGoalsForTeam(MatchesDTO firstMatch, MatchesDTO secondMatch, String teamName) {
		int goalsCount = 0;
		if (teamName.equalsIgnoreCase(firstMatch.getHomeTeam())) {
            goalsCount = firstMatch.getScorehome() + secondMatch.getScoreaway();
		} else if (teamName.equalsIgnoreCase(firstMatch.getAwayTeam())) {
            goalsCount = firstMatch.getScoreaway() + secondMatch.getScorehome();
		}
		return goalsCount;
	}

    private PlayOffDoubleMatch getPlayOffDoubleMatchData(final MatchesDTO match1, final MatchesDTO match2) {
        final PlayOffDoubleMatch playOffDoubleMatch = new PlayOffDoubleMatch();
        final List<String> teamNames = Arrays.asList(match1.getHomeTeam(), match1.getAwayTeam());
        long firstTeamGoals = match1.getScorehome() + match2.getScoreaway();
        long secondTeamGoals = match2.getScorehome() + match1.getScoreaway() ;

        String qualifiedTeamName;
        if(firstTeamGoals > secondTeamGoals) {
            qualifiedTeamName = match1.getHomeTeam();
        } else if(secondTeamGoals > firstTeamGoals) {
            qualifiedTeamName = match2.getHomeTeam();
        } else {
            if(match1.getScoreaway() > match2.getScoreaway()) {
                qualifiedTeamName = match1.getAwayTeam();
            } else {
                qualifiedTeamName = match2.getAwayTeam();
            }
        }

        final String nonQualifiedTeam = teamNames.stream().filter(name-> !name.equalsIgnoreCase(qualifiedTeamName)).findFirst().orElse(null);
        final String qualifiedPlayer = getQualifiedPlayer(qualifiedTeamName, match1);
        final int qualifiedTeamGoalsCount = getPlayOffGoalsForTeam(match1, match2, qualifiedTeamName);
        final int nonQualifiedTeamGoalsCount = getPlayOffGoalsForTeam(match1, match2, nonQualifiedTeam);
        playOffDoubleMatch.setQualifiedTeam(qualifiedTeamName);
        playOffDoubleMatch.setNonQualifiedTeam(nonQualifiedTeam);
        playOffDoubleMatch.setQualifiedPlayer(qualifiedPlayer);
        playOffDoubleMatch.setQualifiedTeamGoals(qualifiedTeamGoalsCount);
        playOffDoubleMatch.setNonQualifiedTeamGoals(nonQualifiedTeamGoalsCount);
        playOffDoubleMatch.setHomeAwayMatch(Arrays.asList(match1, match2));
        return playOffDoubleMatch;
    }

    private String getQualifiedPlayer(final String qualifiedTeamName, final MatchesDTO match){
        String qualifiedPlayer;
        if (match.getHomeTeam().equalsIgnoreCase(qualifiedTeamName)){
            qualifiedPlayer = match.getPlayerH();
        } else {
            qualifiedPlayer = match.getPlayerA();
        }

        return qualifiedPlayer;
    }

}
