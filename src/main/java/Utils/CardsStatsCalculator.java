package Utils;

import com.javasampleapproach.springrest.mysql.model.FifaPlayer;
import com.javasampleapproach.springrest.mysql.entities.Matches;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CardsStatsCalculator {

    public static void getRedAndYellowCards(Matches match, Map<String, List<FifaPlayer>> playersCardsStatisticsPerCompetition, String cardsRecord, String competition, String teamName, String cardType) {
        String[] playersWithYellow = cardsRecord.split("-");

        if(teamName == null) {
//         System.out.println("moj pripad, statistiky musia byt pocitane globalne a nie pre tym");
            checkMultiplePlayersForCurrentStat(match, competition, playersWithYellow[0], playersCardsStatisticsPerCompetition, cardType, match.getHometeam());
            checkMultiplePlayersForCurrentStat(match, competition, playersWithYellow[1], playersCardsStatisticsPerCompetition, cardType, match.getAwayteam());
        }
        else if (match.getHometeam().equalsIgnoreCase(teamName)) {
            checkMultiplePlayersForCurrentStat(match, competition, playersWithYellow[0], playersCardsStatisticsPerCompetition, cardType, match.getHometeam());
        } else {
            checkMultiplePlayersForCurrentStat(match, competition, playersWithYellow[1], playersCardsStatisticsPerCompetition, cardType, match.getAwayteam());
        }
    }

    private static void checkMultiplePlayersForCurrentStat (Matches m, String competition, String statToCalculate, Map<String, List<FifaPlayer>> playersCardsStatisticsPerCompetition, String cardType, String teamname)
    {
        // check for multiple players separated by delimiter
        if (statToCalculate.contains(";")) {
            String[] allPlayersWithCurrentStat = statToCalculate.split(";");

            for (String player : allPlayersWithCurrentStat) {
                setStatForCorrectFifaDataFormat(m, competition, player, playersCardsStatisticsPerCompetition, cardType, teamname);
            }
        } else {
            // '/' is used for no goalscorer - if string contains this character no record for this stat is provided
            if ( !statToCalculate.equalsIgnoreCase("/") ) {
                setStatForCorrectFifaDataFormat(m, competition, statToCalculate, playersCardsStatisticsPerCompetition, cardType, teamname);
            }
        }
    }


    private static void setStatForCorrectFifaDataFormat (Matches m, String competition, String player, Map<String, List<FifaPlayer>> playersCardsStatisticsPerCompetition, String cardType, String teamName) {
        // in some old FIFA seasons there are not minutes provides within stats

        System.out.println("Prave robim na " + m);
        if (MyUtils.seasonsWithGoalscorersWithoutMinutes.contains(m.getSeason())) {
            updateStats(playersCardsStatisticsPerCompetition, competition, player, cardType, teamName);
        } else {
            String[] minuteAndName = player.split(" ");
            String playerName = minuteAndName[1];
            updateStats(playersCardsStatisticsPerCompetition, competition, playerName, cardType, teamName);
        }
    }

    private static void updateStats(Map<String, List<FifaPlayer>> playersCardsStatisticsPerCompetition, String competition, String playerName, String cardType, String teamName){
        String formattedName = playerName.replaceAll("\\."," ");

        // update list EL or CL
        List<FifaPlayer> allPlayersInCompetition = playersCardsStatisticsPerCompetition.get(competition);
        updateProperCompetition(allPlayersInCompetition, formattedName, cardType, teamName);

        // update total list
        List<FifaPlayer> allPlayers = playersCardsStatisticsPerCompetition.get("Total");
        updateProperCompetition(allPlayers, formattedName, cardType, teamName);
    }

    private static void updateProperCompetition(List<FifaPlayer> playersList, String playerName, String cardType, String teamName) {
        FifaPlayer existingPlayer = playersList.stream().filter(p-> p.getName().equalsIgnoreCase(playerName)).findAny().orElse(null);

        if(existingPlayer == null){
            FifaPlayer newPlayer = new FifaPlayer();
            newPlayer.setName(playerName);
            newPlayer.setCardsTotal(1);
            newPlayer.getCardsByTeams().add(teamName);
            addYellowOrRedProperly(newPlayer, cardType);
            playersList.add(newPlayer);
        } else {
            existingPlayer.setCardsTotal(existingPlayer.getCardsTotal() + 1);
            existingPlayer.getCardsByTeams().add(teamName);
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

    public static void sortCardsMap(Map<String, List<FifaPlayer>> playersCardsStatisticsPerCompetition){
        Collections.sort(playersCardsStatisticsPerCompetition.get("Total"), Comparator.comparingInt(FifaPlayer::getCardsTotal).thenComparing(FifaPlayer::getRedCards).reversed());
        Collections.sort(playersCardsStatisticsPerCompetition.get("CL"), Comparator.comparingInt(FifaPlayer::getCardsTotal).thenComparing(FifaPlayer::getRedCards).reversed());
        Collections.sort(playersCardsStatisticsPerCompetition.get("EL"), Comparator.comparingInt(FifaPlayer::getCardsTotal).thenComparing(FifaPlayer::getRedCards).reversed());
    }
}
