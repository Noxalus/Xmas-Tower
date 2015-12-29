package com.noxalus.xmastower.gameservices;

public interface ActionResolver {

	public enum Achievement {
		ACHIEVEMENT_1_M,
		ACHIEVEMENT_1_5_M,
		ACHIEVEMENT_2_M,
		ACHIEVEMENT_2_5_M,
		ACHIEVEMENT_3_M,
		ACHIEVEMENT_3_5_M,
		ACHIEVEMENT_4_M,
		ACHIEVEMENT_4_5_M,
		ACHIEVEMENT_5_M,
		ACHIEVEMENT_7_5_M,
		ACHIEVEMENT_10_M,
		ACHIEVEMENT_ITS_TOO_SMALL,
		ACHIEVEMENT_ITS_TOO_BIG,
		ACHIEVEMENT_IM_FEELING_DIZZY,
		ACHIEVEMENT_HIDDEN_SPAWN
	}

	public boolean getSignedInGPGS();
	public void loginGPGS();
	public void submitScoreGPGS(int score);
	public void unlockAchievementGPGS(Achievement achievement);
	public void getLeaderboardGPGS();
	public void getAchievementsGPGS();
}
