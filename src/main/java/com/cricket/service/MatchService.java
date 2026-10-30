package com.cricket.service;

import com.cricket.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service layer managing all cricket matches.
 * Pre-loads sample match data on startup and exposes simulation and query operations.
 */
@Service
public class MatchService {

    @Autowired
    private SimulationService simulationService;

    // In-memory store: matchId → Match
    private final Map<String, Match> matchStore = new LinkedHashMap<>();

    // ─── Initialization ───────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        // Match 1: India vs Australia — T20 World Cup (LIVE, partially simulated)
        Match m1 = buildIndiaVsAustralia();
        matchStore.put(m1.getId(), m1);
        prewarm(m1, 54);  // ~9 overs already done

        // Match 2: England vs Pakistan — ODI (UPCOMING)
        Match m2 = buildEnglandVsPakistan();
        matchStore.put(m2.getId(), m2);

        // Match 3: South Africa vs New Zealand — T20 (COMPLETED)
        Match m3 = buildSAvsNZ();
        matchStore.put(m3.getId(), m3);
        prewarm(m3, 280);  // Simulate full match

        System.out.println("✅ Sample match data loaded: " + matchStore.size() + " matches.");
    }

    /** Simulate `n` balls to pre-populate a match with data. */
    private void prewarm(Match match, int balls) {
        for (int i = 0; i < balls; i++) {
            if ("COMPLETED".equals(match.getStatus())) break;
            simulationService.simulateBall(match);
        }
    }

    // ─── Match Factory Methods ────────────────────────────────────────────────

    private Match buildIndiaVsAustralia() {
        Match m = new Match();
        m.setId("match-001");
        m.setTeam1("India");          m.setTeam1Short("IND"); m.setTeam1Flag("🇮🇳");
        m.setTeam2("Australia");      m.setTeam2Short("AUS"); m.setTeam2Flag("🇦🇺");
        m.setVenue("Wankhede Stadium, Mumbai");
        m.setMatchType("T20");
        m.setStatus("LIVE");
        m.setMatchDate("30 Oct 2026");
        m.setMaxOvers(20);
        m.setSeriesName("ICC T20 World Cup 2026 — Final");
        m.setTossWinner("India");
        m.setTossDecision("BAT");

        m.setTeam1Players(Arrays.asList(
            "Rohit Sharma", "Shubman Gill", "Virat Kohli",
            "Suryakumar Yadav", "Hardik Pandya", "KL Rahul",
            "Ravindra Jadeja", "Axar Patel", "Jasprit Bumrah",
            "Mohammed Shami", "Arshdeep Singh"
        ));

        m.setTeam2Players(Arrays.asList(
            "David Warner", "Travis Head", "Steve Smith",
            "Marnus Labuschagne", "Glenn Maxwell", "Tim David",
            "Matthew Wade", "Pat Cummins", "Mitchell Starc",
            "Adam Zampa", "Josh Hazlewood"
        ));

        m.setCurrentInnings(1);
        Innings inn1 = simulationService.createInnings(
            1, "India", "Australia", m.getTeam1Players(), m.getTeam2Players());
        m.setInnings1(inn1);
        return m;
    }

    private Match buildEnglandVsPakistan() {
        Match m = new Match();
        m.setId("match-002");
        m.setTeam1("England");        m.setTeam1Short("ENG"); m.setTeam1Flag("🏴󠁧󠁢󠁥󠁮󠁧󠁿");
        m.setTeam2("Pakistan");       m.setTeam2Short("PAK"); m.setTeam2Flag("🇵🇰");
        m.setVenue("Lord's Cricket Ground, London");
        m.setMatchType("ODI");
        m.setStatus("UPCOMING");
        m.setMatchDate("01 Nov 2026");
        m.setMaxOvers(50);
        m.setSeriesName("ICC Champions Trophy 2026 — Semi-Final");
        m.setTossWinner("England");
        m.setTossDecision("BOWL");

        m.setTeam1Players(Arrays.asList(
            "Zak Crawley", "Ben Duckett", "Ollie Pope",
            "Joe Root", "Harry Brook", "Ben Stokes",
            "Jos Buttler", "Chris Woakes", "Mark Wood",
            "Jofra Archer", "Stuart Broad"
        ));

        m.setTeam2Players(Arrays.asList(
            "Mohammad Rizwan", "Saim Ayub", "Babar Azam",
            "Shan Masood", "Fakhar Zaman", "Shadab Khan",
            "Mohammad Nawaz", "Naseem Shah", "Haris Rauf",
            "Shaheen Afridi", "Mohammad Wasim"
        ));

        m.setCurrentInnings(0);
        return m;
    }

    private Match buildSAvsNZ() {
        Match m = new Match();
        m.setId("match-003");
        m.setTeam1("South Africa");   m.setTeam1Short("SA");  m.setTeam1Flag("🇿🇦");
        m.setTeam2("New Zealand");    m.setTeam2Short("NZ");  m.setTeam2Flag("🇳🇿");
        m.setVenue("Newlands, Cape Town");
        m.setMatchType("T20");
        m.setStatus("LIVE");
        m.setMatchDate("30 Oct 2026");
        m.setMaxOvers(20);
        m.setSeriesName("SA vs NZ T20 Series — Match 3");
        m.setTossWinner("South Africa");
        m.setTossDecision("BOWL");

        List<String> saPlayers = Arrays.asList(
            "Reeza Hendricks", "Quinton de Kock", "Temba Bavuma",
            "Rassie van der Dussen", "David Miller", "Heinrich Klaasen",
            "Marco Jansen", "Keshav Maharaj", "Kagiso Rabada",
            "Lungi Ngidi", "Anrich Nortje"
        );
        List<String> nzPlayers = Arrays.asList(
            "Devon Conway", "Finn Allen", "Kane Williamson",
            "Daryl Mitchell", "Glenn Phillips", "James Neesham",
            "Mitchell Santner", "Tim Southee", "Trent Boult",
            "Lockie Ferguson", "Kyle Jamieson"
        );

        m.setTeam1Players(saPlayers);
        m.setTeam2Players(nzPlayers);
        m.setCurrentInnings(1);

        // NZ bats first (SA won toss and chose to bowl)
        Innings inn1 = simulationService.createInnings(
            1, "New Zealand", "South Africa", nzPlayers, saPlayers);
        m.setInnings1(inn1);
        return m;
    }

    // ─── Public Operations ────────────────────────────────────────────────────

    public List<Match> getAllMatches() {
        return new ArrayList<>(matchStore.values());
    }

    public Match getMatch(String id) {
        return matchStore.get(id);
    }

    /** Simulate a single ball for the given match. */
    public Ball simulateBall(String matchId) {
        Match m = matchStore.get(matchId);
        if (m == null || "UPCOMING".equals(m.getStatus())) return null;
        return simulationService.simulateBall(m);
    }

    /** Simulate a full over (up to 6 legal balls + extras). */
    public List<Ball> simulateOver(String matchId) {
        Match m = matchStore.get(matchId);
        if (m == null || "UPCOMING".equals(m.getStatus())) return Collections.emptyList();

        List<Ball> delivered = new ArrayList<>();
        int legalBefore = getCurrentLegalBalls(m);
        int targetLegal  = ((legalBefore / 6) + 1) * 6; // next multiple of 6

        while (true) {
            if ("COMPLETED".equals(m.getStatus())) break;
            Innings inn = m.getCurrentInnings() == 2 ? m.getInnings2() : m.getInnings1();
            if (inn == null || inn.isCompleted()) break;
            if (inn.getLegalBalls() >= targetLegal) break;

            Ball b = simulationService.simulateBall(m);
            if (b != null) delivered.add(b);
            else break;
        }
        return delivered;
    }

    /**
     * Returns the most recent `limit` balls from the current innings,
     * ordered newest-first for the commentary feed.
     */
    public List<Ball> getRecentBalls(String matchId, int limit) {
        Match m = matchStore.get(matchId);
        if (m == null) return Collections.emptyList();

        Innings inn = m.getCurrentInnings() == 2 && m.getInnings2() != null
                ? m.getInnings2() : m.getInnings1();
        if (inn == null) return Collections.emptyList();

        List<Ball> all = inn.getAllBalls();
        int from = Math.max(0, all.size() - limit);
        List<Ball> recent = new ArrayList<>(all.subList(from, all.size()));
        Collections.reverse(recent);
        return recent;
    }

    /** Starts a LIVE match (transitions from UPCOMING → LIVE). */
    public boolean startMatch(String matchId) {
        Match m = matchStore.get(matchId);
        if (m == null || !"UPCOMING".equals(m.getStatus())) return false;

        m.setStatus("LIVE");
        m.setCurrentInnings(1);

        List<String> batters = m.getTeam1Players();
        List<String> bowlers = m.getTeam2Players();
        if ("BOWL".equals(m.getTossDecision())) { // Team1 chose to bowl
            batters = m.getTeam2Players();
            bowlers = m.getTeam1Players();
        }

        String battingTeam  = batters == m.getTeam1Players() ? m.getTeam1() : m.getTeam2();
        String bowlingTeam  = batters == m.getTeam1Players() ? m.getTeam2() : m.getTeam1();

        Innings inn1 = simulationService.createInnings(1, battingTeam, bowlingTeam, batters, bowlers);
        m.setInnings1(inn1);
        return true;
    }

    // ─── Internal Helper ──────────────────────────────────────────────────────

    private int getCurrentLegalBalls(Match m) {
        Innings inn = m.getCurrentInnings() == 2 && m.getInnings2() != null
                ? m.getInnings2() : m.getInnings1();
        return inn != null ? inn.getLegalBalls() : 0;
    }
}
