package Utils;

import java.util.Arrays;
import java.util.List;

public class MyUtils {

	public static final String PAVOL_JAY = "Pavol Jay"; //todo use these in whole project
	public static final String KOTLIK = "Kotlik";
	public static final String RESULT_DRAW = "D";
	public static final String ALL = "ALL";

	public static final String CHAMPIONS_LEAGUE = "CL";
	public static final String EUROPEAN_LEAGUE = "EL";

	public static final String ALL_SEASONS = "All seasons";
	public static final String ALL_PHASES = "All phases";
	public static final String ALL_COMPETITIONS = "All competitions";
	public static final String GROUP_A = "GROUP A";
	public static final String GROUP_B = "GROUP B";
	public static final String GROUP_C = "GROUP C";
	public static final String GROUP_D = "GROUP D";
	public static final String GROUP_E = "GROUP E";
	public static final String GROUP_F = "GROUP F";
	public static final String GROUP_G = "GROUP G";
	public static final String GROUP_H = "GROUP H";
	public static final String GROUP_I = "GROUP I";
	public static final String GROUP_J = "GROUP J";
	public static final String GROUP_K = "GROUP K";
	public static final String GROUP_L = "GROUP L";
	public static final String ROUND_OF_32 = "Round of 32";
	public static final String ROUND_OF_16 = "Round of 16";
	public static final String QUARTERFINALS = "Quarterfinals";
	public static final String SEMIFINALS = "Semifinals";
	public static final String FINAL = "Final";

	public static String CARD_TYPE_YELLOW = "yellow";
	public static String CARD_TYPE_RED = "red";

	public static List<String> seasonsWithGoalscorersWithoutMinutes= Arrays.asList("FIFA07","FIFA09","FIFA11","FIFA13");
	public static List<String> championsLeagueStagesList = Arrays.asList(GROUP_A, GROUP_B, GROUP_C, GROUP_D, GROUP_E, GROUP_F, GROUP_G, GROUP_H, ROUND_OF_16, QUARTERFINALS, SEMIFINALS, FINAL);
	public static List<String> europeanLeagueStagesList =  Arrays.asList(GROUP_A, GROUP_B, GROUP_C, GROUP_D, GROUP_E, GROUP_F, GROUP_G, GROUP_H, GROUP_I, GROUP_J, GROUP_K, GROUP_L, ROUND_OF_32, ROUND_OF_16, QUARTERFINALS, SEMIFINALS, FINAL);
	public static List<String> playerNamesList = Arrays.asList(PAVOL_JAY, KOTLIK);
	public static List<String> competitionsList = Arrays.asList(CHAMPIONS_LEAGUE, EUROPEAN_LEAGUE);
	public static List<String> championsLeagueStagesListWithDefault = Arrays.asList(ALL_PHASES, GROUP_A, GROUP_B, GROUP_C, GROUP_D, GROUP_E, GROUP_F, GROUP_G, GROUP_H, ROUND_OF_16, QUARTERFINALS, SEMIFINALS, FINAL);
	public static List<String> europeanLeagueStagesListWithDefault =  Arrays.asList(ALL_PHASES, GROUP_A, GROUP_B, GROUP_C, GROUP_D, GROUP_E, GROUP_F, GROUP_G, GROUP_H, GROUP_I, GROUP_J, GROUP_K, GROUP_L, ROUND_OF_32, ROUND_OF_16, QUARTERFINALS, SEMIFINALS, FINAL);

	public static String RECORD_TYPE_GOAL = "G";
	public static String RECORD_TYPE_PENALTY = "Penalty";
	public static String RECORD_TYPE_OWN_GOAL = "OG";
	public static String RECORD_TYPE_YELLOW_CARD = "YC";
	public static String RECORD_TYPE_RED_CARD = "RC";

	// Historic matches
	public static final String MOST_GOALS_IN_MATCH = "Most goals in match";
	public static final String BIGGEST_AWAY_WINS = "Biggest Away Wins";
	public static final String BIGGEST_HOME_WINS = "Biggest Home Wins";
	public static final String BIGGEST_DRAWS = "Biggest Draws";

	// Players with records
	public static final String PLAYER_MOST_GOALS_SINGLE_GAME = "Most goals in single game";
	public static final String PLAYER_MOST_GOALS_SEASON = "Most goals in the season";

	// format
	public static final String OLD_FORMAT = "Old format";
	public static final String NEW_FORMAT = "New format";

	public static final String GROUP_STAGE = "Group stage";
	public static final String GROUP_STAGE_LIKE_VALUE = "GROUP";
	public static final String PLAY_OFFS_STAGE = "Play offs";
	public static final String PLAY_OFFS_ROUND_LIKE_VALUE = "Round";
	public static final String PLAY_OFFS_FINAL_LIKE_VALUE = "Final";

	public static final int drawResultId = -1;
}
