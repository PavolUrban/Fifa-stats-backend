package Utils;

import com.javasampleapproach.springrest.mysql.entities.Matches;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Utils.MyUtils.*;
import static Utils.MyUtils.KOTLIK;

public class HelperMethods {

    public static Map<String, Integer> setnumberOfPlayersWins(List<Matches> matches) {

        Map<String, Integer> winsByPlayers = new HashMap<>();
        winsByPlayers.put(PAVOL_JAY, 0);
        winsByPlayers.put(KOTLIK, 0);
        winsByPlayers.put("Draws", 0);

        for(Matches m: matches) {
            String winner = getWinnerPlayer(m);

            if(winner.equalsIgnoreCase(RESULT_DRAW)) //draw
                winsByPlayers.put("Draws", winsByPlayers.get("Draws") + 1);

            else if(winner.equalsIgnoreCase(PAVOL_JAY))
                winsByPlayers.put(PAVOL_JAY, winsByPlayers.get(PAVOL_JAY) + 1);

            else
                winsByPlayers.put(KOTLIK, winsByPlayers.get(KOTLIK) + 1);
        }

        return winsByPlayers;
    }

//    // todo latest fix this
//    public static String whoIsWinnerOfMatch(Matches match) {
//        return PAVOL_JAY;
////        if (match.getWinnerId() == DRAW_RESULT_ID){
////            return RESULT_DRAW;
////        } else if ((match.getIdHomeTeam() == match.getWinnerId() && match.getPlayerH().equalsIgnoreCase(PAVOL_JAY)) ||
////                (match.getIdAwayTeam() == match.getWinnerId() && match.getPlayerA().equalsIgnoreCase(PAVOL_JAY) ) ) {
////            return PAVOL_JAY;
////        } else {
////            return KOTLIK;
////        }
//    }

    public static String getWinnerPlayer(Matches match) {
        if (match.getWinnerId() == DRAW_RESULT_ID){
            return RESULT_DRAW;
        } else if ((match.getHomeTeam().getId() == match.getWinnerId() && match.getPlayerH().equalsIgnoreCase(PAVOL_JAY)) ||
                (match.getAwayTeam().getId() == match.getWinnerId() && match.getPlayerA().equalsIgnoreCase(PAVOL_JAY) ) ) {
            return PAVOL_JAY;
        } else {
            return KOTLIK;
        }
    }


}
