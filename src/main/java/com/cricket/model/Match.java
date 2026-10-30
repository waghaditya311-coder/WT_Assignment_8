package com.cricket.model;

import java.util.List;

/**
 * Top-level match entity representing a cricket match.
 * Contains all match metadata and references to innings data.
 */
public class Match {

    private String id;
    private String team1;          // Full name e.g. "India"
    private String team2;
    private String team1Short;     // Short code e.g. "IND"
    private String team2Short;
    private String team1Flag;      // Emoji flag
    private String team2Flag;
    private String venue;
    private String matchType;      // T20 | ODI | TEST
    private String status;         // LIVE | UPCOMING | COMPLETED
    private String matchDate;
    private int maxOvers;
    private String seriesName;

    // Toss
    private String tossWinner;
    private String tossDecision;   // BAT | BOWL

    // Result (set when status = COMPLETED)
    private String result;

    // Player rosters (ordered batting list)
    private List<String> team1Players;
    private List<String> team2Players;

    // Innings
    private Innings innings1;
    private Innings innings2;
    private int currentInnings = 1;  // 1 or 2

    // ─── Computed ────────────────────────────────────────────────────────────

    /** Short score summary e.g. "145/3 (12.4 Ov)" */
    public String getScoreSummary() {
        Innings live = currentInnings == 2 && innings2 != null ? innings2 : innings1;
        if (live == null) return "Yet to bat";
        return live.getTotalRuns() + "/" + live.getWickets() + " (" + live.getOversString() + " Ov)";
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeam1() { return team1; }
    public void setTeam1(String team1) { this.team1 = team1; }

    public String getTeam2() { return team2; }
    public void setTeam2(String team2) { this.team2 = team2; }

    public String getTeam1Short() { return team1Short; }
    public void setTeam1Short(String team1Short) { this.team1Short = team1Short; }

    public String getTeam2Short() { return team2Short; }
    public void setTeam2Short(String team2Short) { this.team2Short = team2Short; }

    public String getTeam1Flag() { return team1Flag; }
    public void setTeam1Flag(String team1Flag) { this.team1Flag = team1Flag; }

    public String getTeam2Flag() { return team2Flag; }
    public void setTeam2Flag(String team2Flag) { this.team2Flag = team2Flag; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMatchDate() { return matchDate; }
    public void setMatchDate(String matchDate) { this.matchDate = matchDate; }

    public int getMaxOvers() { return maxOvers; }
    public void setMaxOvers(int maxOvers) { this.maxOvers = maxOvers; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getTossWinner() { return tossWinner; }
    public void setTossWinner(String tossWinner) { this.tossWinner = tossWinner; }

    public String getTossDecision() { return tossDecision; }
    public void setTossDecision(String tossDecision) { this.tossDecision = tossDecision; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public List<String> getTeam1Players() { return team1Players; }
    public void setTeam1Players(List<String> team1Players) { this.team1Players = team1Players; }

    public List<String> getTeam2Players() { return team2Players; }
    public void setTeam2Players(List<String> team2Players) { this.team2Players = team2Players; }

    public Innings getInnings1() { return innings1; }
    public void setInnings1(Innings innings1) { this.innings1 = innings1; }

    public Innings getInnings2() { return innings2; }
    public void setInnings2(Innings innings2) { this.innings2 = innings2; }

    public int getCurrentInnings() { return currentInnings; }
    public void setCurrentInnings(int currentInnings) { this.currentInnings = currentInnings; }
}
