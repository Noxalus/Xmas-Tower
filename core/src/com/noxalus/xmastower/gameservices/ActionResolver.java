package com.noxalus.xmastower.gameservices;

public interface ActionResolver {

	public enum Achievement {
		ACHIEVEMENT_1_M,
		ACHIEVEMENT_10_M,
		ACHIEVEMENT_100_M,
		ACHIEVEMENT_1_KM,
		ACHIEVEMENT_ITS_TOO_SMALL,
		ACHIEVEMENT_ITS_TOO_BIG,
		ACHIEVEMENT_IM_FEELING_DIZZY
	}

	public boolean getSignedInGPGS();
	public void loginGPGS();
	public void submitScoreGPGS(int score);
	public void unlockAchievementGPGS(Achievement achievement);
	public void getLeaderboardGPGS();
	public void getAchievementsGPGS();
}
