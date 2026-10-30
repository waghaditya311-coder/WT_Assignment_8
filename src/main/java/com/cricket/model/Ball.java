package com.cricket.model;

/**
 * Represents a single delivery (ball) in a cricket match.
 * Tracks all details about the ball outcome including runs, extras, and wickets.
 */
public class Ball {

    private int overNumber;      // 0-indexed over number
    private int ballNumber;      // Legal ball number in over (1-6)
    private int runs;            // Runs scored off this ball
    private boolean wicket;
    private boolean wide;
    private boolean noBall;
    private boolean four;
    private boolean six;
    private String wicketType;   // Bowled, Caught, LBW, Stumped, Run Out, Caught Behind
    private String batsman;
    private String bowler;
    private String commentary;   // Ball description text

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getOverNumber() { return overNumber; }
    public void setOverNumber(int overNumber) { this.overNumber = overNumber; }

    public int getBallNumber() { return ballNumber; }
    public void setBallNumber(int ballNumber) { this.ballNumber = ballNumber; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public boolean isWicket() { return wicket; }
    public void setWicket(boolean wicket) { this.wicket = wicket; }

    public boolean isWide() { return wide; }
    public void setWide(boolean wide) { this.wide = wide; }

    public boolean isNoBall() { return noBall; }
    public void setNoBall(boolean noBall) { this.noBall = noBall; }

    public boolean isFour() { return four; }
    public void setFour(boolean four) { this.four = four; }

    public boolean isSix() { return six; }
    public void setSix(boolean six) { this.six = six; }

    public String getWicketType() { return wicketType; }
    public void setWicketType(String wicketType) { this.wicketType = wicketType; }

    public String getBatsman() { return batsman; }
    public void setBatsman(String batsman) { this.batsman = batsman; }

    public String getBowler() { return bowler; }
    public void setBowler(String bowler) { this.bowler = bowler; }

    public String getCommentary() { return commentary; }
    public void setCommentary(String commentary) { this.commentary = commentary; }

    /**
     * Returns a short label for the ball (used in over visualization).
     * W = wicket, Wd = wide, Nb = no-ball, or the runs scored.
     */
    public String getLabel() {
        if (wicket) return "W";
        if (wide) return "Wd";
        if (noBall) return "Nb";
        if (six) return "6";
        if (four) return "4";
        return String.valueOf(runs);
    }

    /**
     * Returns a CSS class name for color-coded ball display.
     */
    public String getBallClass() {
        if (wicket) return "wicket";
        if (wide || noBall) return "extra";
        if (six) return "six";
        if (four) return "four";
        if (runs == 0) return "dot";
        return "runs";
    }
}
