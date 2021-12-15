package Utils;

import com.javasampleapproach.springrest.mysql.model.FifaPlayer;
import com.javasampleapproach.springrest.mysql.model.Matches;
import com.javasampleapproach.springrest.mysql.model.TeamStats;

import java.util.List;

public class CardsStatsCalculator {

    public static void getRedAndYellowCards(Matches match, TeamStats team, String cardsRecord, String competition, String teamName, String cardType) {
        String[] playersWithYellow = cardsRecord.split("-");

        if (match.getHometeam().equalsIgnoreCase(teamName)) {
            checkMultiplePlayersForCurrentStat(match, competition, playersWithYellow[0], team, cardType);
        } else {
            checkMultiplePlayersForCurrentStat(match, competition, playersWithYellow[1], team, cardType);
        }
    }

    private static void checkMultiplePlayersForCurrentStat (Matches m, String competition, String statToCalculate, TeamStats team, String cardType)
    {
        // check for multiple players separated by delimiter
        if (statToCalculate.contains(";")) {
            String[] allPlayersWithCurrentStat = statToCalculate.split(";");

            for (String player : allPlayersWithCurrentStat) {
                setStatForCorrectFifaDataFormat(m, competition, player, team, cardType);
            }
        } else {
            // '/' is used for no goalscorer - if string contains this character no record for this stat is provided
            if ( !statToCalculate.equalsIgnoreCase("/") ) {
                setStatForCorrectFifaDataFormat(m, competition, statToCalculate, team, cardType);
            }
        }
    }


    private static void setStatForCorrectFifaDataFormat (Matches m, String competition, String player, TeamStats team, String cardType) {
        // in some old FIFA seasons there are not minutes provides within stats
        if (MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) {
            // TODO
            System.out.println("Ide o staru sezonu nemam prenho minuty .....TODO");
        } else {
            updateStatsForNewFifaFormat(team, competition, player, cardType);
        }
    }

    private static void updateStatsForNewFifaFormat(TeamStats team, String competition, String playerRecord, String cardType){
        String[] minuteAndName = playerRecord.split(" ");
        String playerName = minuteAndName[1];

        // update list EL or CL
        List<FifaPlayer> allPlayersInCompetition = team.getPlayersCardsStatisticsPerCompetition().get(competition);
        updateProperCompetition(allPlayersInCompetition, playerName, cardType);

        // update total list
        List<FifaPlayer> allPlayers = team.getPlayersCardsStatisticsPerCompetition().get("Total");
        updateProperCompetition(allPlayers, playerName, cardType);
    }

    private static void updateProperCompetition(List<FifaPlayer> playersList, String playerName, String cardType) {
        FifaPlayer existingPlayer = playersList.stream().filter(p-> p.getName().equalsIgnoreCase(playerName)).findAny().orElse(null);

        if(existingPlayer == null){
            FifaPlayer newPlayer = new FifaPlayer();
            newPlayer.setName(playerName);
            newPlayer.setCardsTotal(1);
            addYellowOrRedProperly(newPlayer, cardType);
            playersList.add(newPlayer);
        } else {
            existingPlayer.setCardsTotal(existingPlayer.getCardsTotal() + 1);
            addYellowOrRedProperly(existingPlayer, cardType);
        }
    }

    private static void addYellowOrRedProperly(FifaPlayer player, String cardType){
        if(cardType.equalsIgnoreCase(MyUtils.CARD_TYPE_YELLOW)){
            player.setYellowCards(player.getYellowCards() + 1);
        } else {
            player.setRedCards(player.getRedCards() + 1);
        }
    }
}
