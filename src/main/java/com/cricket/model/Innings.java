package com.cricket.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete innings in a cricket match.
 * Maintains the live state of batting, bowling, and scoring.
 */
public class Innings {

    private int inningsNumber;     // 1 or 2
    private String battingTeam;
    private String bowlingTeam;

    // Ordered player lists
    private List<String> battingOrder = new ArrayList<>();
    private List<String> bowlingOrder = new ArrayList<>();

    // Score
    private int totalRuns = 0;
    private int wickets = 0;
    private int legalBalls = 0;    // Count of legal deliveries
    private int wides = 0;
    private int noBalls = 0;

    // Ball-by-ball data
    private List<Over> overs = new ArrayList<>();
    private List<Ball> allBalls = new ArrayList<>();

    // Per-player statistics
    private Map<String, BattingStats> battingStats = new LinkedHashMap<>();
    private Map<String, BowlingStats> bowlingStats = new LinkedHashMap<>();

    // Current live-state tracking indices
    private int strikerIdx = 0;
    private int nonStrikerIdx = 1;
    private int nextBatsmanIdx = 2;     // Next batsman to come in
    private int bowlerRotationIdx = 0;  // Cycles through bowlingOrder

    private boolean completed = false;
    private int target = 0;  // Run target for 2nd innings (0 if not set)

    // ─── Computed Getters (Serialized by Jackson) ─────────────────────────

    public String getStriker() {
        if (battingOrder.isEmpty() || strikerIdx >= battingOrder.size()) return "";
        return battingOrder.get(strikerIdx);
    }

    public String getNonStriker() {
        if (battingOrder.isEmpty() || nonStrikerIdx >= battingOrder.size()) return "";
        return battingOrder.get(nonStrikerIdx);
    }

    public String getCurrentBowler() {
        if (bowlingOrder.isEmpty()) return "";
        return bowlingOrder.get(bowlerRotationIdx % bowlingOrder.size());
    }

    /** Current run-rate (runs per over) */
    public double getRunRate() {
        if (legalBalls == 0) return 0.0;
        return Math.round((totalRuns * 6.0 / legalBalls) * 100.0) / 100.0;
    }

    /** Required run-rate for 2nd innings chase */
    public double getRequiredRunRate() {
        if (target == 0 || inningsNumber != 2) return 0.0;
        int runsNeeded = target - totalRuns;
        if (runsNeeded <= 0) return 0.0;
        // This is computed in the controller, placeholder here
        return 0.0;
    }

    /** Overs bowled as string e.g. "8.3" */
    public String getOversString() {
        return (legalBalls / 6) + "." + (legalBalls % 6);
    }

    /** Total extras (wides + no-balls) */
    public int getExtras() {
        return wides + noBalls;
    }

    // ─── Standard Getters & Setters ──────────────────────────────────────────

    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }

    public String getBattingTeam() { return battingTeam; }
    public void setBattingTeam(String battingTeam) { this.battingTeam = battingTeam; }

    public String getBowlingTeam() { return bowlingTeam; }
    public void setBowlingTeam(String bowlingTeam) { this.bowlingTeam = bowlingTeam; }

    public List<String> getBattingOrder() { return battingOrder; }
    public void setBattingOrder(List<String> battingOrder) { this.battingOrder = battingOrder; }

    public List<String> getBowlingOrder() { return bowlingOrder; }
    public void setBowlingOrder(List<String> bowlingOrder) { this.bowlingOrder = bowlingOrder; }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public int getLegalBalls() { return legalBalls; }
    public void setLegalBalls(int legalBalls) { this.legalBalls = legalBalls; }

    public int getWides() { return wides; }
    public void setWides(int wides) { this.wides = wides; }

    public int getNoBalls() { return noBalls; }
    public void setNoBalls(int noBalls) { this.noBalls = noBalls; }

    public List<Over> getOvers() { return overs; }
    public void setOvers(List<Over> overs) { this.overs = overs; }

    public List<Ball> getAllBalls() { return allBalls; }
    public void setAllBalls(List<Ball> allBalls) { this.allBalls = allBalls; }

    public Map<String, BattingStats> getBattingStats() { return battingStats; }
    public void setBattingStats(Map<String, BattingStats> battingStats) { this.battingStats = battingStats; }

    public Map<String, BowlingStats> getBowlingStats() { return bowlingStats; }
    public void setBowlingStats(Map<String, BowlingStats> bowlingStats) { this.bowlingStats = bowlingStats; }

    public int getStrikerIdx() { return strikerIdx; }
    public void setStrikerIdx(int strikerIdx) { this.strikerIdx = strikerIdx; }

    public int getNonStrikerIdx() { return nonStrikerIdx; }
    public void setNonStrikerIdx(int nonStrikerIdx) { this.nonStrikerIdx = nonStrikerIdx; }

    public int getNextBatsmanIdx() { return nextBatsmanIdx; }
    public void setNextBatsmanIdx(int nextBatsmanIdx) { this.nextBatsmanIdx = nextBatsmanIdx; }

    public int getBowlerRotationIdx() { return bowlerRotationIdx; }
    public void setBowlerRotationIdx(int bowlerRotationIdx) { this.bowlerRotationIdx = bowlerRotationIdx; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }
}
