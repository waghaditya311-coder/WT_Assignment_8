/**
 * CricketLive — Frontend Application Logic
 * Handles match selection, real-time simulation, scorecard rendering, and SSE.
 */

const API = '';  // same origin
let currentMatchId = null;
let autoTimer = null;
let autoSpeed = 2000;
let sseSource = null;

// ── Bootstrap ────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadAllMatches();
    connectSSE();
});

// ── SSE Connection ────────────────────────────────────────────────────────────
function connectSSE() {
    sseSource = new EventSource('/api/live/stream');
    sseSource.addEventListener('update', (e) => {
        const match = JSON.parse(e.data);
        if (match.id === currentMatchId) renderMatch(match);
        refreshMatchCard(match);
    });
    sseSource.onerror = () => {
        document.getElementById('sse-badge').style.opacity = '0.3';
    };
    sseSource.onopen = () => {
        document.getElementById('sse-badge').style.opacity = '1';
    };
}

// ── Match List ────────────────────────────────────────────────────────────────
async function loadAllMatches() {
    try {
        const res = await fetch('/api/matches');
        const matches = await res.json();
        renderMatchList(matches);
        updateTicker(matches);
        // Auto-select first LIVE match
        const live = matches.find(m => m.status === 'LIVE');
        if (live) selectMatch(live.id);
    } catch (e) {
        console.error('Failed to load matches', e);
    }
}

function renderMatchList(matches) {
    const el = document.getElementById('match-list');
    el.innerHTML = matches.map(m => matchCardHTML(m)).join('');
}

function refreshMatchCard(match) {
    const card = document.querySelector(`[data-match="${match.id}"]`);
    if (card) card.outerHTML = matchCardHTML(match);
}

function matchCardHTML(m) {
    const statusClass = m.status.toLowerCase();
    const liveInnings = m.currentInnings === 2 && m.innings2 ? m.innings2 : m.innings1;
    const scoreStr = liveInnings
        ? `${liveInnings.totalRuns}/${liveInnings.wickets} (${liveInnings.oversString} Ov)`
        : 'Yet to bat';
    const badgeTxt = m.status === 'LIVE' ? '● LIVE' : m.status;
    return `
      <div class="match-card ${m.id === currentMatchId ? 'active' : ''}"
           data-match="${m.id}" onclick="selectMatch('${m.id}')">
        <div class="mc-header">
          <span class="mc-type">${m.matchType} · ${m.matchDate}</span>
          <span class="mc-badge ${statusClass}">${badgeTxt}</span>
        </div>
        <div class="mc-teams">${m.team1Flag} ${m.team1Short} vs ${m.team2Flag} ${m.team2Short}</div>
        <div class="mc-score">${m.status === 'UPCOMING' ? 'Match not started' : (m.status === 'COMPLETED' && m.result ? m.result : scoreStr)}</div>
        <div class="mc-venue">📍 ${m.venue}</div>
      </div>`;
}

function updateTicker(matches) {
    const live = matches.filter(m => m.status === 'LIVE');
    if (!live.length) { document.getElementById('live-ticker').textContent = 'No live matches'; return; }
    const parts = live.map(m => {
        const inn = m.currentInnings === 2 && m.innings2 ? m.innings2 : m.innings1;
        const sc = inn ? `${inn.totalRuns}/${inn.wickets} (${inn.oversString})` : '';
        return `${m.team1Short} vs ${m.team2Short} — ${sc}`;
    });
    document.getElementById('live-ticker').textContent = parts.join('  |  ');
}

// ── Match Selection ───────────────────────────────────────────────────────────
async function selectMatch(id) {
    currentMatchId = id;
    // Update active card
    document.querySelectorAll('.match-card').forEach(c => {
        c.classList.toggle('active', c.dataset.match === id);
    });
    try {
        const res = await fetch(`/api/matches/${id}`);
        const match = await res.json();
        renderMatch(match);
    } catch (e) { console.error(e); }
}

function renderMatch(match) {
    renderHeader(match);
    renderControls(match);
    renderLiveTab(match);
    renderScorecard(match);
    renderCommentary(match.id);
    document.getElementById('tabs').style.display = 'flex';
}

// ── Match Header ──────────────────────────────────────────────────────────────
function renderHeader(match) {
    const inn1 = match.innings1;
    const inn2 = match.innings2;

    const team1Batting = inn1 && inn1.battingTeam === match.team1;
    const inn1Score = inn1 ? `${inn1.totalRuns}/${inn1.wickets}<br><small>${inn1.oversString} Ov · RR ${inn1.runRate}</small>` : '';
    const inn2Score = inn2 ? `${inn2.totalRuns}/${inn2.wickets}<br><small>${inn2.oversString} Ov · RR ${inn2.runRate}</small>` : '';

    const t1Score = team1Batting ? inn1Score : (inn2 ? inn2Score : inn1Score || '');
    const t2Score = team1Batting ? (inn2 ? inn2Score : '<span class="not-yet">Yet to bat</span>') : inn1Score;

    const statusHTML = match.status === 'LIVE'
        ? `<div class="status-badge live"><span class="dot"></span>LIVE</div>`
        : match.status === 'COMPLETED'
        ? `<div class="status-badge completed">✓ COMPLETED</div>`
        : `<div class="status-badge upcoming">🕐 UPCOMING</div>`;

    const resultHTML = match.result
        ? `<div class="result-banner">🏆 ${match.result}</div>` : '';

    // 2nd innings target info
    let targetHTML = '';
    if (inn2 && inn2.target > 0 && match.status === 'LIVE') {
        const needed = inn2.target - inn2.totalRuns;
        const ballsLeft = (match.maxOvers * 6) - inn2.legalBalls;
        const rrr = ballsLeft > 0 ? (needed * 6 / ballsLeft).toFixed(2) : '—';
        targetHTML = `<div style="margin-top:.5rem;font-size:.78rem;color:var(--gold)">
          Target: <strong>${inn2.target}</strong> &nbsp;|&nbsp;
          Need: <strong>${Math.max(0, needed)}</strong> off <strong>${ballsLeft}</strong> balls &nbsp;|&nbsp;
          RRR: <strong>${rrr}</strong>
        </div>`;
    }

    document.getElementById('match-header').innerHTML = `
      <div class="score-header">
        <div class="team-block">
          <span class="team-flag">${match.team1Flag}</span>
          <div class="team-short">${match.team1Short}</div>
          <div class="team-name">${match.team1}</div>
          <div class="team-score ${t1Score ? 'batting-now' : ''}">${t1Score || '—'}</div>
        </div>
        <div class="vs-block">
          <div class="vs-text">VS</div>
          ${statusHTML}
          <div style="font-size:.65rem;color:var(--text-muted);margin-top:.4rem">${match.matchType}</div>
        </div>
        <div class="team-block">
          <span class="team-flag">${match.team2Flag}</span>
          <div class="team-short">${match.team2Short}</div>
          <div class="team-name">${match.team2}</div>
          <div class="team-score ${t2Score ? 'batting-now' : ''}">${t2Score || '—'}</div>
        </div>
      </div>
      <div class="header-meta">
        <span>📍 ${match.venue}</span>
        <span>📅 ${match.matchDate}</span>
        <span>🏆 ${match.seriesName}</span>
        <span>🎲 ${match.tossWinner} won toss, elected to ${match.tossDecision}</span>
      </div>
      ${targetHTML}
      ${resultHTML}`;
}

// ── Controls ──────────────────────────────────────────────────────────────────
function renderControls(match) {
    const ctrlPanel = document.getElementById('controls-panel');
    const startPanel = document.getElementById('start-panel');
    if (match.status === 'LIVE') {
        ctrlPanel.style.display = 'block';
        startPanel.style.display = 'none';
    } else if (match.status === 'UPCOMING') {
        ctrlPanel.style.display = 'none';
        startPanel.style.display = 'block';
    } else {
        ctrlPanel.style.display = 'none';
        startPanel.style.display = 'none';
    }
}

// ── Live Score Tab ────────────────────────────────────────────────────────────
function renderLiveTab(match) {
    const innings = match.currentInnings === 2 && match.innings2 ? match.innings2 : match.innings1;
    if (!innings) return;

    // Batsmen
    const bStats = innings.battingStats || {};
    const striker    = innings.striker;
    const nonStriker = innings.nonStriker;
    renderBatsmanCard('card-striker', striker, bStats[striker], true);
    renderBatsmanCard('card-nonstriker', nonStriker, bStats[nonStriker], false);

    // Bowler
    const bowler = innings.currentBowler;
    const bwStats = innings.bowlingStats || {};
    renderBowlerCard(bowler, bwStats[bowler]);

    // This Over
    renderCurrentOver(innings);

    // Info strip
    const extras = innings.wides + innings.noBalls;
    document.getElementById('match-info-strip').innerHTML = `
      <div class="info-item">Run Rate: <strong>${innings.runRate}</strong></div>
      <div class="info-item">Extras: <strong>${extras}</strong> (Wd ${innings.wides}, Nb ${innings.noBalls})</div>
      <div class="info-item">Overs: <strong>${innings.oversString}</strong>/${match.maxOvers}</div>
      ${innings.target ? `<div class="info-item">Target: <strong>${innings.target}</strong></div>` : ''}`;
}

function renderBatsmanCard(elId, name, stats, isStriker) {
    const el = document.getElementById(elId);
    if (!name || !stats) { el.innerHTML = ''; return; }
    el.className = `batsman-card ${isStriker ? 'striker' : ''}`;
    el.innerHTML = `
      <div class="bc-role ${isStriker ? 'on-strike' : 'non-strike'}">${isStriker ? '🏏 On Strike' : 'Non Striker'}</div>
      <div class="bc-name">${name}</div>
      <div class="bc-stats">
        <div class="bc-stat">
          <div class="bc-stat-val big">${stats.runs}</div>
          <div class="bc-stat-lbl">Runs</div>
        </div>
        <div class="bc-stat">
          <div class="bc-stat-val">${stats.balls}</div>
          <div class="bc-stat-lbl">Balls</div>
        </div>
        <div class="bc-stat">
          <div class="bc-stat-val">${stats.fours}</div>
          <div class="bc-stat-lbl">4s</div>
        </div>
        <div class="bc-stat">
          <div class="bc-stat-val">${stats.sixes}</div>
          <div class="bc-stat-lbl">6s</div>
        </div>
      </div>
      <div class="bc-sr">SR: <strong>${stats.strikeRate.toFixed(1)}</strong></div>`;
}

function renderBowlerCard(name, stats) {
    const el = document.getElementById('bowler-row');
    if (!name || !stats) { el.innerHTML = ''; return; }
    el.innerHTML = `
      <div class="bowler-card">
        <div class="bw-name">🎳 ${name}</div>
        <div class="bw-stats">
          <div class="bw-stat"><div class="bw-stat-val">${stats.overs}</div><div class="bw-stat-lbl">Overs</div></div>
          <div class="bw-stat"><div class="bw-stat-val">${stats.wickets}</div><div class="bw-stat-lbl">Wkts</div></div>
          <div class="bw-stat"><div class="bw-stat-val">${stats.runsConceded}</div><div class="bw-stat-lbl">Runs</div></div>
          <div class="bw-stat"><div class="bw-stat-val">${stats.economy.toFixed(2)}</div><div class="bw-stat-lbl">Econ</div></div>
          <div class="bw-stat"><div class="bw-stat-val">${stats.maidens}</div><div class="bw-stat-lbl">Mdns</div></div>
        </div>
      </div>`;
}

function renderCurrentOver(innings) {
    const overs = innings.overs || [];
    const currentOver = overs.length > 0 ? overs[overs.length - 1] : null;
    const balls = currentOver ? currentOver.balls : [];
    const legalBalls = balls.filter(b => !b.wide && !b.noBall);

    document.getElementById('over-label').textContent =
        `Over ${(innings.legalBalls / 6) | 0} — ${currentOver ? currentOver.bowler : ''}`;

    // Show 6 slots, fill with actual balls
    let html = '';
    for (let i = 0; i < 6; i++) {
        const b = legalBalls[i];
        if (b) html += `<div class="ball-dot ${b.ballClass}" title="${b.commentary}">${b.label}</div>`;
        else   html += `<div class="ball-dot empty"></div>`;
    }
    // Show extras separately
    balls.filter(b => b.wide || b.noBall).forEach(b => {
        html += `<div class="ball-dot extra" title="${b.commentary}">${b.label}</div>`;
    });
    document.getElementById('over-balls').innerHTML = html;
}

// ── Scorecard ────────────────────────────────────────────────────────────────
function renderScorecard(match) {
    const inn = match.currentInnings === 2 && match.innings2 ? match.innings2
              : match.innings1;
    if (!inn) return;

    // Batting table
    const bStats = inn.battingStats || {};
    const battingBody = document.getElementById('batting-body');
    battingBody.innerHTML = inn.battingOrder.map(name => {
        const s = bStats[name] || {};
        const isStriker = name === inn.striker || name === inn.nonStriker;
        const isCurrent = !s.out && isStriker;
        return `<tr class="${isCurrent ? 'batting-now' : ''}">
          <td>${name}</td>
          <td class="dismissal">${s.status || 'not out'}</td>
          <td class="runs-big">${s.runs ?? 0}</td>
          <td>${s.balls ?? 0}</td>
          <td>${s.fours ?? 0}</td>
          <td>${s.sixes ?? 0}</td>
          <td>${(s.strikeRate ?? 0).toFixed(1)}</td>
        </tr>`;
    }).join('');

    // Bowling table
    const wStats = inn.bowlingStats || {};
    const bowlingBody = document.getElementById('bowling-body');
    bowlingBody.innerHTML = Object.values(wStats).map(s => `
      <tr>
        <td>${s.playerName}</td>
        <td>${s.overs}</td>
        <td>${s.maidens}</td>
        <td>${s.runsConceded}</td>
        <td class="wicket-col">${s.wickets}</td>
        <td>${s.economy.toFixed(2)}</td>
        <td>${s.wides}</td>
        <td>${s.noBalls}</td>
      </tr>`).join('');

    // If innings 2 exists, render it too
    const inn2El = document.getElementById('innings2-scorecard');
    if (match.innings2 && match.innings1 && match.innings2 !== inn) {
        inn2El.innerHTML = renderInningsTable(match.innings2);
    } else if (match.innings1 && match.innings2 && inn === match.innings2) {
        inn2El.innerHTML = renderInningsTable(match.innings1);
    } else {
        inn2El.innerHTML = '';
    }
}

function renderInningsTable(inn) {
    const bStats = inn.battingStats || {};
    const wStats = inn.bowlingStats || {};
    return `
      <div style="margin-top:2rem">
        <div class="innings-header-row">
          <span>${inn.battingTeam} — Innings ${inn.inningsNumber}</span>
          <strong>${inn.totalRuns}/${inn.wickets} (${inn.oversString} Ov)</strong>
        </div>
        <div class="table-wrap">
          <table class="score-table">
            <thead><tr><th>Batsman</th><th>Status</th><th>R</th><th>B</th><th>4s</th><th>6s</th><th>SR</th></tr></thead>
            <tbody>${inn.battingOrder.map(name => {
              const s = bStats[name] || {};
              return `<tr><td>${name}</td><td class="dismissal">${s.status||'not out'}</td>
                <td class="runs-big">${s.runs??0}</td><td>${s.balls??0}</td>
                <td>${s.fours??0}</td><td>${s.sixes??0}</td><td>${(s.strikeRate??0).toFixed(1)}</td></tr>`;
            }).join('')}</tbody>
          </table>
        </div>
        <div style="margin-top:1rem">
        <div class="table-wrap">
          <table class="score-table">
            <thead><tr><th>Bowler</th><th>O</th><th>M</th><th>R</th><th>W</th><th>Econ</th></tr></thead>
            <tbody>${Object.values(wStats).map(s =>
              `<tr><td>${s.playerName}</td><td>${s.overs}</td><td>${s.maidens}</td>
               <td>${s.runsConceded}</td><td class="wicket-col">${s.wickets}</td>
               <td>${s.economy.toFixed(2)}</td></tr>`).join('')}</tbody>
          </table>
        </div></div>
      </div>`;
}

// ── Commentary ────────────────────────────────────────────────────────────────
async function renderCommentary(matchId) {
    try {
        const res = await fetch(`/api/matches/${matchId}/commentary?limit=30`);
        const balls = await res.json();
        const feed = document.getElementById('commentary-feed');
        if (!balls.length) {
            feed.innerHTML = '<p class="commentary-empty">No balls simulated yet.</p>';
            return;
        }
        feed.innerHTML = balls.map(b => {
            const cls  = b.wicket ? 'wicket' : b.six ? 'six' : b.four ? 'four' : (b.wide || b.noBall) ? 'extra' : '';
            const over = b.wide || b.noBall ? `${b.overNumber}.${b.ballNumber}e` : `${b.overNumber}.${b.ballNumber}`;
            return `
              <div class="commentary-item ${cls}">
                <div class="ci-ball ${b.ballClass}">${b.label}</div>
                <div class="ci-info">
                  <div class="ci-over">Over ${over}</div>
                  <div class="ci-text">${b.commentary}</div>
                  <div class="ci-players">${b.bowler} → ${b.batsman}</div>
                </div>
              </div>`;
        }).join('');
    } catch (e) { console.error(e); }
}

// ── Simulation Actions ────────────────────────────────────────────────────────
async function doSimulateBall() {
    if (!currentMatchId) return showToast('Select a live match first', 'red');
    try {
        const res = await fetch(`/api/matches/${currentMatchId}/simulate`, { method: 'POST' });
        const data = await res.json();
        if (!res.ok) return showToast(data.error || 'Cannot simulate', 'red');

        const ball = data.ball;
        const toastMsg = ball.wicket ? `🏏 WICKET! ${ball.batsman}`
                       : ball.six    ? `🚀 SIX by ${ball.batsman}!`
                       : ball.four   ? `💥 FOUR by ${ball.batsman}!`
                       : `${ball.runs} run${ball.runs !== 1 ? 's' : ''}`;
        const toastType = ball.wicket ? 'red' : ball.six ? 'gold' : ball.four ? '' : '';
        showToast(toastMsg, toastType);

        await selectMatch(currentMatchId);
    } catch (e) { console.error(e); }
}

async function doSimulateOver() {
    if (!currentMatchId) return showToast('Select a live match first', 'red');
    try {
        const res = await fetch(`/api/matches/${currentMatchId}/over`, { method: 'POST' });
        const data = await res.json();
        showToast(`Over complete — ${data.ballsDelivered} balls`, 'green');
        await selectMatch(currentMatchId);
    } catch (e) { console.error(e); }
}

async function doStartMatch() {
    if (!currentMatchId) return;
    try {
        const res = await fetch(`/api/matches/${currentMatchId}/start`, { method: 'POST' });
        const data = await res.json();
        if (!res.ok) return showToast(data.error || 'Cannot start', 'red');
        showToast('🏏 Match started!', 'green');
        await selectMatch(currentMatchId);
        await loadAllMatches();
    } catch (e) { console.error(e); }
}

function toggleAuto() {
    const btn  = document.getElementById('btn-auto');
    const icon = document.getElementById('auto-icon');
    const lbl  = document.getElementById('auto-label');

    if (autoTimer) {
        clearInterval(autoTimer);
        autoTimer = null;
        btn.classList.remove('active');
        icon.textContent = '🔴';
        lbl.textContent  = 'Auto Simulate';
        showToast('Auto simulate stopped', '');
    } else {
        btn.classList.add('active');
        icon.textContent = '⏸';
        lbl.textContent  = 'Stop Auto';
        showToast('Auto simulate started!', 'green');
        autoTimer = setInterval(doSimulateBall, autoSpeed);
    }
}

function updateAutoSpeed() {
    autoSpeed = parseInt(document.getElementById('speed-select').value);
    if (autoTimer) {
        clearInterval(autoTimer);
        autoTimer = setInterval(doSimulateBall, autoSpeed);
    }
}

// ── Tab Switching ─────────────────────────────────────────────────────────────
function switchTab(tab) {
    document.querySelectorAll('.tab').forEach(t => t.classList.toggle('active', t.dataset.tab === tab));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.toggle('active', c.id === `tab-${tab}`));
    if (tab === 'commentary' && currentMatchId) renderCommentary(currentMatchId);
}

// ── Toast ─────────────────────────────────────────────────────────────────────
let toastTimer;
function showToast(msg, type = '') {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.className = `toast show ${type}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => t.classList.remove('show'), 2500);
}
