package Utils;

import com.javasampleapproach.springrest.mysql.controller.PlayersController;
import com.javasampleapproach.springrest.mysql.entities.Matches;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Utils.MyUtils.*;
import static Utils.MyUtils.KOTLIK;

public class HelperMethods {

    public static Map<String, Integer> setnumberOfPlayersWins(List<Matches> matches)
    {
        PlayersController playersStatsController  = new PlayersController();

        Map<String, Integer> winsByPlayers = new HashMap<String, Integer>();

        winsByPlayers.put(PAVOL_JAY, 0);
        winsByPlayers.put(KOTLIK, 0);
        winsByPlayers.put("Draws", 0);


        for(Matches m: matches)
        {
            String winner = playersStatsController.whoIsWinnerOfMatch(m, PAVOL_JAY, KOTLIK);

            if(winner.equalsIgnoreCase(RESULT_DRAW)) //draw
                winsByPlayers.put("Draws", winsByPlayers.get("Draws") + 1);

            else if(winner.equalsIgnoreCase(PAVOL_JAY))
                winsByPlayers.put(PAVOL_JAY, winsByPlayers.get(PAVOL_JAY) + 1);

            else
                winsByPlayers.put(KOTLIK, winsByPlayers.get(KOTLIK) + 1);
        }


        return winsByPlayers;
    }
}
