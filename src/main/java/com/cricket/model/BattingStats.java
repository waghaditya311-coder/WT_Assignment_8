package com.cricket.model;

/**
 * Batting statistics for a player in a specific innings.
 */
public class BattingStats {

    private String playerName;
    private int runs = 0;
    private int balls = 0;
    private int fours = 0;
    private int sixes = 0;
    private boolean out = false;
    private String dismissalInfo = "not out";  // e.g. "caught b Mitchell Starc"

    public BattingStats() {}

    public BattingStats(String playerName) {
        this.playerName = playerName;
    }

    // ─── Computed ────────────────────────────────────────────────────────────

    /** Strike rate = (runs / balls) * 100 */
    public double getStrikeRate() {
        return balls > 0 ? Math.round((runs * 100.0 / balls) * 10.0) / 10.0 : 0.0;
    }

    /** Returns dismissal string or "not out" */
    public String getStatus() {
        return out ? dismissalInfo : "not out";
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public int getBalls() { return balls; }
    public void setBalls(int balls) { this.balls = balls; }

    public int getFours() { return fours; }
    public void setFours(int fours) { this.fours = fours; }

    public int getSixes() { return sixes; }
    public void setSixes(int sixes) { this.sixes = sixes; }

    public boolean isOut() { return out; }
    public void setOut(boolean out) { this.out = out; }

    public String getDismissalInfo() { return dismissalInfo; }
    public void setDismissalInfo(String dismissalInfo) { this.dismissalInfo = dismissalInfo; }
}
