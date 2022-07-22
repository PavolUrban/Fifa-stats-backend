package Utils;

import com.javasampleapproach.springrest.mysql.entities.FifaPlayerDB;
import com.javasampleapproach.springrest.mysql.entities.RecordsInMatches;
import com.javasampleapproach.springrest.mysql.model.FifaPlayer;
import com.javasampleapproach.springrest.mysql.model.Goalscorer;
import com.javasampleapproach.springrest.mysql.repo.FifaPlayerDBRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class NewestCardsCalculator {

    @Autowired
    private FifaPlayerDBRepository fifaPlayerDBRepository;

    public NewestCardsCalculator(FifaPlayerDBRepository fifaPlayerDBRepository) {
        this.fifaPlayerDBRepository = fifaPlayerDBRepository;
    }

    public  List<FifaPlayer> getCards(List<RecordsInMatches> allCards){

        List<Long> allIds = allCards.stream().map(cardRecord -> cardRecord.getPlayerId()).collect(Collectors.toList());
        Set<Long> distinctIDs = allIds.stream().collect(Collectors.toSet());
        Iterable<FifaPlayerDB> allPlayers = fifaPlayerDBRepository.findByIdIn(distinctIDs);

        List<FifaPlayer> allPlayersWithCard = new ArrayList<>();

        allPlayers.forEach(player->{
            List<RecordsInMatches> recordsRelatedToPlayer = allCards.stream().filter(goals-> goals.getPlayerId() == player.getId()).collect(Collectors.toList());
            FifaPlayer playerWithCard = new FifaPlayer();
            playerWithCard.setName(player.getPlayerName());



            recordsRelatedToPlayer.forEach(record->{
                if(record.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_YELLOW_CARD)){
                    playerWithCard.setYellowCards(playerWithCard.getYellowCards() + 1);
                } else if(record.getTypeOfRecord().equalsIgnoreCase(MyUtils.RECORD_TYPE_RED_CARD)){
                    playerWithCard.setRedCards(playerWithCard.getRedCards() + 1);
                }
                playerWithCard.setCardsTotal(playerWithCard.getCardsTotal() + 1);
            });

            allPlayersWithCard.add(playerWithCard);
        });


        allPlayersWithCard.sort((o1, o2) -> o2.getCardsTotal().compareTo(o1.getCardsTotal()));


        return allPlayersWithCard;
    }
}
