package com.cricket.model;

/**
 * Bowling statistics for a player in a specific innings.
 */
public class BowlingStats {

    private String playerName;
    private int ballsBowled = 0;
    private int runsConceded = 0;
    private int wickets = 0;
    private int maidens = 0;
    private int wides = 0;
    private int noBalls = 0;

    public BowlingStats() {}

    public BowlingStats(String playerName) {
        this.playerName = playerName;
    }

    // ─── Computed ────────────────────────────────────────────────────────────

    /** Overs format: "3.2" means 3 complete overs + 2 balls */
    public String getOvers() {
        return (ballsBowled / 6) + "." + (ballsBowled % 6);
    }

    /** Economy rate = runs conceded per over */
    public double getEconomy() {
        if (ballsBowled == 0) return 0.0;
        return Math.round((runsConceded * 6.0 / ballsBowled) * 100.0) / 100.0;
    }

    // ─── Helper mutators ─────────────────────────────────────────────────────

    public void addBall() { this.ballsBowled++; }
    public void addRuns(int r) { this.runsConceded += r; }
    public void addWicket() { this.wickets++; }
    public void addWide() { this.wides++; }
    public void addNoBall() { this.noBalls++; }
    public void addMaiden() { this.maidens++; }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getBallsBowled() { return ballsBowled; }
    public void setBallsBowled(int ballsBowled) { this.ballsBowled = ballsBowled; }

    public int getRunsConceded() { return runsConceded; }
    public void setRunsConceded(int runsConceded) { this.runsConceded = runsConceded; }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public int getMaidens() { return maidens; }
    public void setMaidens(int maidens) { this.maidens = maidens; }

    public int getWides() { return wides; }
    public void setWides(int wides) { this.wides = wides; }

    public int getNoBalls() { return noBalls; }
    public void setNoBalls(int noBalls) { this.noBalls = noBalls; }
}
