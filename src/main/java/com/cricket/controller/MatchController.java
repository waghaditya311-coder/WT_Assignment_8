package com.cricket.controller;

import com.cricket.model.Ball;
import com.cricket.model.Match;
import com.cricket.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * REST controller exposing all cricket match APIs and Server-Sent Events stream.
 *
 * Endpoints:
 *   GET  /api/matches               – List all matches
 *   GET  /api/matches/{id}          – Match details (full)
 *   GET  /api/matches/{id}/summary  – Lightweight score summary
 *   POST /api/matches/{id}/simulate – Simulate next ball
 *   POST /api/matches/{id}/over     – Simulate a full over
 *   POST /api/matches/{id}/start    – Start an upcoming match
 *   GET  /api/matches/{id}/commentary – Recent ball commentary
 *   GET  /api/live/stream           – SSE stream (real-time push)
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MatchController {

    @Autowired
    private MatchService matchService;

    /** Thread-safe list of active SSE emitters (one per connected client) */
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    // ─── Match Queries ────────────────────────────────────────────────────────

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<Match> getMatch(@PathVariable String id) {
        Match m = matchService.getMatch(id);
        return m != null ? ResponseEntity.ok(m) : ResponseEntity.notFound().build();
    }

    @GetMapping("/matches/{id}/summary")
    public ResponseEntity<Map<String, Object>> getMatchSummary(@PathVariable String id) {
        Match m = matchService.getMatch(id);
        if (m == null) return ResponseEntity.notFound().build();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", m.getId());
        summary.put("matchType", m.getMatchType());
        summary.put("status", m.getStatus());
        summary.put("seriesName", m.getSeriesName());
        summary.put("venue", m.getVenue());
        summary.put("matchDate", m.getMatchDate());
        summary.put("team1", m.getTeam1());
        summary.put("team2", m.getTeam2());
        summary.put("team1Short", m.getTeam1Short());
        summary.put("team2Short", m.getTeam2Short());
        summary.put("team1Flag", m.getTeam1Flag());
        summary.put("team2Flag", m.getTeam2Flag());
        summary.put("result", m.getResult());
        summary.put("toss", m.getTossWinner() + " won the toss and elected to " + m.getTossDecision());

        if (m.getInnings1() != null) {
            var i = m.getInnings1();
            summary.put("innings1", Map.of(
                "team", i.getBattingTeam(),
                "score", i.getTotalRuns() + "/" + i.getWickets(),
                "overs", i.getOversString(),
                "runRate", i.getRunRate()
            ));
        }
        if (m.getInnings2() != null) {
            var i = m.getInnings2();
            int needed  = i.getTarget() - i.getTotalRuns();
            int ballsLeft = (m.getMaxOvers() * 6) - i.getLegalBalls();
            double rrr = ballsLeft > 0 ? Math.round(needed * 6.0 / ballsLeft * 100.0) / 100.0 : 0;
            summary.put("innings2", Map.of(
                "team", i.getBattingTeam(),
                "score", i.getTotalRuns() + "/" + i.getWickets(),
                "overs", i.getOversString(),
                "runRate", i.getRunRate(),
                "target", i.getTarget(),
                "runsNeeded", Math.max(0, needed),
                "requiredRunRate", rrr
            ));
        }

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/matches/{id}/commentary")
    public ResponseEntity<List<Ball>> getCommentary(
            @PathVariable String id,
            @RequestParam(defaultValue = "25") int limit) {
        return ResponseEntity.ok(matchService.getRecentBalls(id, limit));
    }

    // ─── Simulation Actions ───────────────────────────────────────────────────

    @PostMapping("/matches/{id}/simulate")
    public ResponseEntity<Map<String, Object>> simulateBall(@PathVariable String id) {
        Ball ball = matchService.simulateBall(id);
        if (ball == null) return ResponseEntity.badRequest().body(
            Map.of("error", "Match is not live or has ended."));

        Match updated = matchService.getMatch(id);
        broadcastToSse(updated);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ball", ball);
        resp.put("match", updated.getScoreSummary());
        resp.put("status", updated.getStatus());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/matches/{id}/over")
    public ResponseEntity<Map<String, Object>> simulateOver(@PathVariable String id) {
        List<Ball> balls = matchService.simulateOver(id);
        Match updated = matchService.getMatch(id);
        broadcastToSse(updated);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("balls", balls);
        resp.put("ballsDelivered", balls.size());
        resp.put("match", updated.getScoreSummary());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/matches/{id}/start")
    public ResponseEntity<Map<String, String>> startMatch(@PathVariable String id) {
        boolean started = matchService.startMatch(id);
        if (!started) return ResponseEntity.badRequest().body(
            Map.of("error", "Match cannot be started (not UPCOMING or not found)."));
        return ResponseEntity.ok(Map.of("message", "Match started!", "id", id));
    }

    // ─── Server-Sent Events (Real-time Push) ──────────────────────────────────

    /**
     * Clients connect here to receive real-time score updates as JSON events.
     * Each simulate call broadcasts an 'update' event to all connected clients.
     */
    @GetMapping(value = "/live/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter liveStream() {
        SseEmitter emitter = new SseEmitter(0L);  // No timeout

        sseEmitters.add(emitter);

        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> sseEmitters.remove(emitter));
        emitter.onError(e -> sseEmitters.remove(emitter));

        // Send current state immediately on connect
        try {
            emitter.send(SseEmitter.event()
                .name("init")
                .data(matchService.getAllMatches()));
        } catch (IOException e) {
            sseEmitters.remove(emitter);
        }

        return emitter;
    }

    /** Pushes a match update to all active SSE subscribers. */
    private void broadcastToSse(Match match) {
        if (match == null) return;
        List<SseEmitter> stale = new ArrayList<>();
        for (SseEmitter emitter : sseEmitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("update")
                    .data(match));
            } catch (IOException e) {
                stale.add(emitter);
            }
        }
        sseEmitters.removeAll(stale);
    }
}
