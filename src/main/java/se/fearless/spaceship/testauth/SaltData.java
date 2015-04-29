package se.fearless.spaceship.testauth;

public class SaltData {
	private String userSalt;
	private String oneTimeSalt;

	public SaltData(String userSalt, String oneTimeSalt) {
		this.userSalt = userSalt;
		this.oneTimeSalt = oneTimeSalt;
	}

	public String getUserSalt() {
		return userSalt;
	}

	public String getOneTimeSalt() {
		return oneTimeSalt;
	}
}
