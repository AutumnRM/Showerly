// Showerly 后端：每分钟轮询校方接口，写入 D1，并对内提供查询 API。
// 校方响应字段需在抓包后按实际结构调整 normalize()。

export default {
  async scheduled(event, env) {
    try {
      await recordSample(env);
    } catch (e) {
      console.error('scheduled failed:', e);
    }
  },

  async fetch(request, env) {
    const url = new URL(request.url);
    const cors = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': '*'
    };

    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: cors });
    }
    if (request.method !== 'GET') {
      return json({ ok: false, error: 'method not allowed' }, cors, 405);
    }

    try {
      if (url.pathname === '/' || url.pathname === '/api') {
        return json(
          { ok: true, service: 'showerly', endpoints: ['/api/current', '/api/history', '/api/status'] },
          cors
        );
      }

      if (url.pathname === '/api/current') {
        const row = await env.DB.prepare('SELECT * FROM samples ORDER BY ts DESC LIMIT 1').first();
        return json({ ok: true, data: row ?? null }, cors);
      }

      if (url.pathname === '/api/history') {
        const limit = clampInt(url.searchParams.get('limit'), 1, 1440, 60);
        const res = await env.DB
          .prepare('SELECT ts, total, capacity, status FROM samples ORDER BY ts DESC LIMIT ?')
          .bind(limit)
          .all();
        return json({ ok: true, data: res.results }, cors);
      }

      if (url.pathname === '/api/status') {
        const row = await env.DB
          .prepare('SELECT ts, total, capacity, status FROM samples ORDER BY ts DESC LIMIT 1')
          .first();
        return json({ ok: true, alive: true, latest: row ?? null }, cors);
      }

      return json({ ok: false, error: 'not found' }, cors, 404);
    } catch (e) {
      return json({ ok: false, error: String(e) }, cors, 500);
    }
  }
};

async function recordSample(env) {
  const src = (env.SCHOOL_API_URL || '').trim();
  let payload;
  if (!src) {
    payload = demo();
  } else {
    const res = await fetch(src, { headers: buildAuthHeaders(env) });
    if (!res.ok) throw new Error('school api status ' + res.status);
    payload = await res.json();
  }

  const p = normalize(payload);
  const ts = Math.floor(Date.now() / 1000);
  await env.DB.prepare(
    'INSERT INTO samples (ts, total, capacity, status, raw) VALUES (?, ?, ?, ?, ?)'
  )
    .bind(ts, p.total, p.capacity, p.status, JSON.stringify(payload))
    .run();
}

function buildAuthHeaders(env) {
  const name = env.SCHOOL_HEADER_NAME || 'Authorization';
  const headers = { Accept: 'application/json' };
  if (env.SCHOOL_TOKEN) headers[name] = env.SCHOOL_TOKEN;
  return headers;
}

function normalize(payload) {
  const d = payload?.data ?? payload;
  const total = num(
    d?.total ?? d?.count ?? d?.current ?? d?.used ?? payload?.total ?? payload?.count ?? 0
  );
  const capacity = num(d?.capacity ?? d?.totalBays ?? payload?.capacity ?? 0);
  let status = 'unknown';
  if (capacity > 0) {
    const r = total / capacity;
    status = r >= 0.9 ? 'full' : r >= 0.6 ? 'busy' : r > 0 ? 'normal' : 'empty';
  }
  return { total, capacity, status };
}

function demo() {
  const total = 55 + Math.floor(Math.random() * 20);
  return { data: { total, capacity: 80, status: 'busy' } };
}

function num(v) {
  const n = Number(v);
  return Number.isFinite(n) ? Math.round(n) : 0;
}

function clampInt(v, min, max, def) {
  const n = parseInt(v, 10);
  return Number.isFinite(n) ? Math.max(min, Math.min(max, n)) : def;
}

function json(obj, headers, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { 'Content-Type': 'application/json; charset=utf-8', ...headers }
  });
}
