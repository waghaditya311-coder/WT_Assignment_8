package com.cricket.service;

import com.cricket.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core simulation engine for cricket ball-by-ball generation.
 * Models realistic ball outcomes based on cricket probability distributions.
 *
 * Outcome Probabilities:
 *   Wide  : 3%   | No-Ball: 1%  | Wicket: 6%
 *   Dot   : 30%  | Single : 25% | Two   : 12%
 *   Three : 3%   | Four   : 13% | Six   : 7%
 */
@Service
public class SimulationService {

    private final Random random = new Random();

    // ─── Commentary Arrays ────────────────────────────────────────────────────

    private static final String[] DOT_COMMENTARY = {
        "Good length delivery. Defended solidly back to the bowler.",
        "Tight line and length, dot ball! Pressure building.",
        "Played and missed! Went past the outside edge!",
        "Forward defensive stroke. No run.",
        "Tapped back to the bowler. Dot ball.",
        "Good bounce! Batsman ducks under it.",
        "Full delivery, driven straight to mid-off. Dot.",
        "Excellent yorker! Squeezed out at the last moment.",
        "Short of length, fended away safely. Dot.",
        "Nipped back in! Beaten on the inside edge. Lucky escape!"
    };

    private static final String[] SINGLE_COMMENTARY = {
        "Pushed to mid-on, batsmen cross for a quick single.",
        "Nudged off the pads to square leg. One run.",
        "Dabbed to third man, they hurry through for one.",
        "Clipped to midwicket, easy single taken.",
        "Driven to cover, they call for a quick one.",
        "Flicked off the hip, one run added.",
        "Punched to extra cover, jog for a single."
    };

    private static final String[] TWO_COMMENTARY = {
        "Driven to long-off, they come back for two!",
        "Punched to deep cover, comfortable two runs.",
        "Flicked to deep square leg, running hard for two.",
        "Cuts it to backward point, good running — two runs!",
        "Steered to third man, fielder cuts off the boundary — only two."
    };

    private static final String[] THREE_COMMENTARY = {
        "Powerful drive! They run hard and steal three!",
        "Flicked away, misfield in the deep — three runs!",
        "Driven wide of long-off, excellent running — three!"
    };

    private static final String[] FOUR_COMMENTARY = {
        "FOUR! Cracking cover drive! Beats the fielder all ends up!",
        "FOUR! Pulled away to the fine leg boundary with authority!",
        "FOUR! Beautiful late cut, races away to third man!",
        "FOUR! Driven straight! Rolls to the long-on boundary!",
        "FOUR! Flicks it off the pads, perfectly placed!",
        "FOUR! Square drive! Beats point and races away!",
        "FOUR! Glanced fine off his pads past the keeper!",
        "FOUR! Cuts hard, too wide for the sweeper to stop!",
        "FOUR! Delightful flick through mid-wicket!",
        "FOUR! Full toss dispatched through the covers!"
    };

    private static final String[] SIX_COMMENTARY = {
        "SIX! Absolutely MASSIVE! That's sailing over long-on into the stands!",
        "SIX! MAXIMUM! Cleared the rope with immense power!",
        "SIX! What a shot! Straight back over the bowler's head!",
        "SIX! Deposited into the crowd at deep mid-wicket!",
        "SIX! Unbelievable! Picked up from outside off and hoicked over square leg!",
        "SIX! Towering hit into the upper tier! Extraordinary!",
        "SIX! Swept off the top of off stump for six!"
    };

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Creates and initializes a new Innings with batting/bowling stats.
     */
    public Innings createInnings(int number, String battingTeam, String bowlingTeam,
                                  List<String> battingOrder, List<String> bowlingPlayers) {
        Innings innings = new Innings();
        innings.setInningsNumber(number);
        innings.setBattingTeam(battingTeam);
        innings.setBowlingTeam(bowlingTeam);
        innings.setBattingOrder(new ArrayList<>(battingOrder));

        // Last 5 players in the bowling side act as bowlers
        int size = bowlingPlayers.size();
        List<String> bowlers = new ArrayList<>(
            bowlingPlayers.subList(Math.max(0, size - 5), size)
        );
        innings.setBowlingOrder(bowlers);

        // Initialize batting stats for all players
        Map<String, BattingStats> bMap = new LinkedHashMap<>();
        for (String p : battingOrder) bMap.put(p, new BattingStats(p));
        innings.setBattingStats(bMap);

        // Initialize bowling stats for all designated bowlers
        Map<String, BowlingStats> wMap = new LinkedHashMap<>();
        for (String p : bowlers) wMap.put(p, new BowlingStats(p));
        innings.setBowlingStats(wMap);

        // Initial state: openers bat, first bowler bowls
        innings.setStrikerIdx(0);
        innings.setNonStrikerIdx(1);
        innings.setNextBatsmanIdx(2);
        innings.setBowlerRotationIdx(0);

        // Create the first over
        Over first = new Over(0, innings.getCurrentBowler());
        innings.getOvers().add(first);

        return innings;
    }

    /**
     * Simulates the next delivery in the given match.
     *
     * @param match the live match
     * @return Ball object describing the delivery, or null if match is over
     */
    public Ball simulateBall(Match match) {
        // Resolve current innings object
        Innings innings = resolveActiveInnings(match);
        if (innings == null) return null;

        String striker = innings.getStriker();
        String bowler  = innings.getCurrentBowler();

        Ball ball = new Ball();
        ball.setBatsman(striker);
        ball.setBowler(bowler);
        ball.setOverNumber(innings.getLegalBalls() / 6);

        double r = random.nextDouble();

        if (r < 0.03) {
            // ── Wide (3%) ──
            applyWide(ball, innings, bowler);

        } else if (r < 0.04) {
            // ── No-Ball (1%) ──
            applyNoBall(ball, innings, bowler);

        } else {
            // ── Legal Delivery ──
            int prevBalls = innings.getLegalBalls();
            innings.setLegalBalls(prevBalls + 1);
            ball.setBallNumber((prevBalls % 6) + 1);

            innings.getBowlingStats().get(bowler).addBall();

            if (r < 0.10) {
                applyWicket(ball, innings, striker, bowler);
            } else if (r < 0.40) {
                applyDot(ball, innings, striker);
            } else if (r < 0.65) {
                applyRuns(ball, innings, striker, bowler, 1, false, false);
                rotateStrike(innings);                       // odd runs → swap
            } else if (r < 0.77) {
                applyRuns(ball, innings, striker, bowler, 2, false, false);
            } else if (r < 0.80) {
                applyRuns(ball, innings, striker, bowler, 3, false, false);
                rotateStrike(innings);                       // odd runs → swap
            } else if (r < 0.93) {
                applyRuns(ball, innings, striker, bowler, 4, true, false);
            } else {
                applyRuns(ball, innings, striker, bowler, 6, false, true);
            }

            // Add to current over before possible rotation
            addToCurrentOver(innings, ball);

            // End of over? (every 6 legal balls)
            if (!innings.isCompleted() && innings.getLegalBalls() % 6 == 0) {
                endOver(innings, match.getMaxOvers());
            }

            // Check if innings has ended
            checkInningsEnd(innings, match);
        }

        // For extras, still record in the over
        if (ball.isWide() || ball.isNoBall()) {
            addToCurrentOver(innings, ball);
        }

        innings.getAllBalls().add(ball);
        return ball;
    }

    // ─── Ball Outcome Handlers ────────────────────────────────────────────────

    private void applyWide(Ball ball, Innings innings, String bowler) {
        ball.setWide(true);
        ball.setRuns(1);
        innings.setTotalRuns(innings.getTotalRuns() + 1);
        innings.setWides(innings.getWides() + 1);

        BowlingStats bws = innings.getBowlingStats().get(bowler);
        bws.addWide();
        bws.addRuns(1);

        ball.setCommentary("WIDE! " + bowler + " strays too far down the leg side. Extra run added.");
    }

    private void applyNoBall(Ball ball, Innings innings, String bowler) {
        ball.setNoBall(true);
        ball.setRuns(1);
        innings.setTotalRuns(innings.getTotalRuns() + 1);
        innings.setNoBalls(innings.getNoBalls() + 1);

        BowlingStats bws = innings.getBowlingStats().get(bowler);
        bws.addNoBall();
        bws.addRuns(1);

        ball.setCommentary("NO BALL! " + bowler + " oversteps. A free hit will be bowled next!");
    }

    private void applyWicket(Ball ball, Innings innings, String striker, String bowler) {
        String[] types = {"Bowled", "Caught", "LBW", "Caught Behind", "Stumped", "Run Out"};
        String type = types[random.nextInt(types.length)];
        ball.setWicket(true);
        ball.setRuns(0);
        ball.setWicketType(type);

        // Update batting stats
        BattingStats bs = innings.getBattingStats().get(striker);
        bs.setOut(true);
        if ("Run Out".equals(type)) {
            bs.setDismissalInfo("run out");
        } else {
            bs.setDismissalInfo(type.toLowerCase() + " b " + bowler);
        }

        // Update bowling stats (only bowler-attributed dismissals)
        if (!"Run Out".equals(type)) {
            innings.getBowlingStats().get(bowler).addWicket();
        }

        innings.setWickets(innings.getWickets() + 1);
        ball.setCommentary(generateWicketCommentary(striker, bowler, type) +
                           "  [" + innings.getWickets() + " down]");

        // Bring next batsman to striker's end
        if (innings.getWickets() < 10 && innings.getNextBatsmanIdx() < innings.getBattingOrder().size()) {
            innings.setStrikerIdx(innings.getNextBatsmanIdx());
            innings.setNextBatsmanIdx(innings.getNextBatsmanIdx() + 1);
        } else {
            innings.setCompleted(true);  // All out
        }
    }

    private void applyDot(Ball ball, Innings innings, String striker) {
        ball.setRuns(0);
        innings.getBattingStats().get(striker).setBalls(
            innings.getBattingStats().get(striker).getBalls() + 1);
        ball.setCommentary(DOT_COMMENTARY[random.nextInt(DOT_COMMENTARY.length)]);
    }

    private void applyRuns(Ball ball, Innings innings, String striker, String bowler,
                            int runs, boolean isFour, boolean isSix) {
        ball.setRuns(runs);
        ball.setFour(isFour);
        ball.setSix(isSix);

        innings.setTotalRuns(innings.getTotalRuns() + runs);

        BattingStats bs = innings.getBattingStats().get(striker);
        bs.setRuns(bs.getRuns() + runs);
        bs.setBalls(bs.getBalls() + 1);
        if (isFour) bs.setFours(bs.getFours() + 1);
        if (isSix) bs.setSixes(bs.getSixes() + 1);

        innings.getBowlingStats().get(bowler).addRuns(runs);

        if (isFour) {
            ball.setCommentary(FOUR_COMMENTARY[random.nextInt(FOUR_COMMENTARY.length)]);
        } else if (isSix) {
            ball.setCommentary(SIX_COMMENTARY[random.nextInt(SIX_COMMENTARY.length)]);
        } else if (runs == 1) {
            ball.setCommentary(SINGLE_COMMENTARY[random.nextInt(SINGLE_COMMENTARY.length)]);
        } else if (runs == 2) {
            ball.setCommentary(TWO_COMMENTARY[random.nextInt(TWO_COMMENTARY.length)]);
        } else {
            ball.setCommentary(THREE_COMMENTARY[random.nextInt(THREE_COMMENTARY.length)]);
        }
    }

    // ─── Over Management ─────────────────────────────────────────────────────

    private void addToCurrentOver(Innings innings, Ball ball) {
        List<Over> overs = innings.getOvers();
        if (overs.isEmpty()) return;
        Over curr = overs.get(overs.size() - 1);
        curr.getBalls().add(ball);
        if (!ball.isWide() && !ball.isNoBall()) {
            curr.setRunsInOver(curr.getRunsInOver() + ball.getRuns());
            if (ball.isWicket()) curr.setWicketsInOver(curr.getWicketsInOver() + 1);
        } else {
            curr.setRunsInOver(curr.getRunsInOver() + 1);
        }
    }

    private void endOver(Innings innings, int maxOvers) {
        // Check for maiden
        List<Over> overs = innings.getOvers();
        if (!overs.isEmpty()) {
            Over last = overs.get(overs.size() - 1);
            if (last.isMaiden()) {
                innings.getBowlingStats().get(last.getBowler()).addMaiden();
            }
        }

        // Rotate strike at end of over
        rotateStrike(innings);

        // Advance to next bowler
        innings.setBowlerRotationIdx(innings.getBowlerRotationIdx() + 1);

        // Start new over only if more overs remain
        int oversCompleted = innings.getLegalBalls() / 6;
        if (oversCompleted < maxOvers) {
            Over next = new Over(oversCompleted, innings.getCurrentBowler());
            innings.getOvers().add(next);
        }
    }

    // ─── Innings Lifecycle ────────────────────────────────────────────────────

    private void checkInningsEnd(Innings innings, Match match) {
        if (innings.isCompleted()) return;

        boolean allOut  = innings.getWickets() >= 10;
        boolean allOver = innings.getLegalBalls() >= match.getMaxOvers() * 6;

        if (allOut || allOver) {
            innings.setCompleted(true);
        }

        // 2nd innings: target chased
        if (!innings.isCompleted() && match.getCurrentInnings() == 2
                && innings.getTarget() > 0
                && innings.getTotalRuns() >= innings.getTarget()) {
            innings.setCompleted(true);
            int wktsLeft = 10 - innings.getWickets();
            match.setStatus("COMPLETED");
            match.setResult(innings.getBattingTeam() + " won by " + wktsLeft
                    + " wicket" + (wktsLeft == 1 ? "" : "s"));
        }

        // 1st innings complete → launch 2nd innings
        if (innings.isCompleted() && match.getCurrentInnings() == 1
                && match.getInnings2() == null) {
            startInnings2(match);
        }

        // 2nd innings ended (all out / all overs) — compute winner by runs
        if (innings.isCompleted() && match.getCurrentInnings() == 2
                && "LIVE".equals(match.getStatus())) {
            int scored = innings.getTotalRuns();
            int needed = innings.getTarget();
            match.setStatus("COMPLETED");
            if (scored == needed - 1) {
                match.setResult("Match Tied!");
            } else {
                int deficit = (needed - 1) - scored;
                match.setResult(innings.getBowlingTeam() + " won by " + deficit
                        + " run" + (deficit == 1 ? "" : "s"));
            }
        }
    }

    private void startInnings2(Match match) {
        Innings inn1 = match.getInnings1();
        String newBatTeam = inn1.getBowlingTeam();
        String newBowlTeam = inn1.getBattingTeam();

        List<String> newBatOrder = newBatTeam.equals(match.getTeam1())
                ? match.getTeam1Players() : match.getTeam2Players();
        List<String> newBowlPlayers = newBowlTeam.equals(match.getTeam1())
                ? match.getTeam1Players() : match.getTeam2Players();

        Innings inn2 = createInnings(2, newBatTeam, newBowlTeam, newBatOrder, newBowlPlayers);
        inn2.setTarget(inn1.getTotalRuns() + 1);

        match.setInnings2(inn2);
        match.setCurrentInnings(2);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Resolves which innings is currently active, initializing inn2 if needed. */
    private Innings resolveActiveInnings(Match match) {
        if (match.getCurrentInnings() == 2) {
            return match.getInnings2() != null && !match.getInnings2().isCompleted()
                    ? match.getInnings2() : null;
        }
        Innings inn1 = match.getInnings1();
        if (inn1 == null || inn1.isCompleted()) {
            if (match.getInnings2() == null) startInnings2(match);
            return match.getInnings2() != null && !match.getInnings2().isCompleted()
                    ? match.getInnings2() : null;
        }
        return inn1;
    }

    private void rotateStrike(Innings innings) {
        int temp = innings.getStrikerIdx();
        innings.setStrikerIdx(innings.getNonStrikerIdx());
        innings.setNonStrikerIdx(temp);
    }

    private String generateWicketCommentary(String striker, String bowler, String type) {
        return switch (type) {
            case "Bowled"       -> "BOWLED! " + bowler + " crashes through the defense of " + striker + "! The stumps are rattled! ";
            case "Caught"       -> "CAUGHT! " + striker + " holes out in the deep off " + bowler + "! ";
            case "Caught Behind"-> "CAUGHT BEHIND! " + striker + " edges " + bowler + " straight to the keeper! ";
            case "LBW"          -> "LBW! " + bowler + " raps " + striker + " on the pad! The finger goes up! ";
            case "Stumped"      -> "STUMPED! " + striker + " is out of the crease, lightning glove work! ";
            case "Run Out"      -> "RUN OUT! " + striker + " is well short of his ground! Great fielding! ";
            default             -> "OUT! " + striker + " is dismissed " + type + " off " + bowler + "! ";
        };
    }
}
