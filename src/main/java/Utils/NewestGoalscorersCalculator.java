package Utils;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NewestGoalscorersCalculator {

    @Autowired
    private FifaPlayerDBRepository fifaPlayerDBRepository;

    public NewestGoalscorersCalculator(FifaPlayerDBRepository fifaPlayerDBRepository) {
        this.fifaPlayerDBRepository = fifaPlayerDBRepository;
    }

    public List<Goalscorer> getGoalscorers(List<RecordsInMatches> allGoals){

        List<Long> allIds = allGoals.stream().map(goal-> goal.getPlayerId()).collect(Collectors.toList());
        List<Long> distinctIDs = allIds.stream().distinct().collect(Collectors.toList());
        Iterable<FifaPlayerDB> allPlayers = fifaPlayerDBRepository.findByIdIn(distinctIDs);

        List<Goalscorer> allGoalscorers = new ArrayList<>();
        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allGoals.stream().filter(goals-> goals.getPlayerId() == player.getId()).collect(Collectors.toList());
            Goalscorer goalscorer = new Goalscorer();
            goalscorer.setName(player.getPlayerName());
            goalscorer.setTotalGoalsCount(0);

            Set<String> teamsPlayerScoredFor = new HashSet<>();

            recordsRelatedToPlayer.forEach(record->{
                teamsPlayerScoredFor.add(record.getTeamName());
                if(record.getMinuteOfRecord()!=null){
                    goalscorer.setTotalGoalsCount(goalscorer.getTotalGoalsCount() + 1);
                } else {
                    goalscorer.setTotalGoalsCount(goalscorer.getTotalGoalsCount() + record.getNumberOfGoalsForOldFormat());
                }
            });

            goalscorer.setTeamPlayerScoredFor(teamsPlayerScoredFor.stream().findAny().orElse(null));
            goalscorer.setNumberOfTeamsPlayerScoredFor(teamsPlayerScoredFor.size());
            allGoalscorers.add(goalscorer);
        });


        allGoalscorers.sort((o1, o2) -> o2.getTotalGoalsCount().compareTo(o1.getTotalGoalsCount()));


        return allGoalscorers;
    }
}
