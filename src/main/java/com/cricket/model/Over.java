package com.cricket.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one over in a cricket innings.
 * Contains all deliveries bowled and aggregated stats.
 */
public class Over {

    private int overNumber;   // 0-indexed
    private String bowler;
    private List<Ball> balls = new ArrayList<>();
    private int runsInOver = 0;
    private int wicketsInOver = 0;

    public Over() {}

    public Over(int overNumber, String bowler) {
        this.overNumber = overNumber;
        this.bowler = bowler;
    }

    // ─── Computed ────────────────────────────────────────────────────────────

    /** Returns true if no runs were scored in this over (excl. extras) */
    public boolean isMaiden() {
        return runsInOver == 0 && balls.size() >= 6;
    }

    /** Display number (1-indexed) */
    public int getDisplayNumber() {
        return overNumber + 1;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getOverNumber() { return overNumber; }
    public void setOverNumber(int overNumber) { this.overNumber = overNumber; }

    public String getBowler() { return bowler; }
    public void setBowler(String bowler) { this.bowler = bowler; }

    public List<Ball> getBalls() { return balls; }
    public void setBalls(List<Ball> balls) { this.balls = balls; }

    public int getRunsInOver() { return runsInOver; }
    public void setRunsInOver(int runsInOver) { this.runsInOver = runsInOver; }

    public int getWicketsInOver() { return wicketsInOver; }
    public void setWicketsInOver(int wicketsInOver) { this.wicketsInOver = wicketsInOver; }
}
