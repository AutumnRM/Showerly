// Showerly 后端：每分钟轮询校方接口，写入 D1，并对内提供查询 API。
// 校方接口（2026-09-01 抓包确认）：
//   GET https://cloudman.jinghaojian.net/bathroom?campusId=4&uid=...
//   Authorization: Bearer <JWT>
//   code: "200", msg: "成功", data: [{ id, name, sex, maxLoad, useCount,
//                                     bookingDeviceCnt, availableBookingDeviceCnt }]

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

// 模拟校方返回，便于本地/无 token 时试用。
function demo() {
  return {
    code: '200',
    msg: '成功',
    data: [
      { id: 31, name: '博硕2楼男', sex: 0, maxLoad: 62, useCount: 12, bookingDeviceCnt: 0, availableBookingDeviceCnt: 0 },
      { id: 46, name: '东区第一浴室-1层', sex: 0, maxLoad: 20, useCount: 14, bookingDeviceCnt: 0, availableBookingDeviceCnt: 0 }
    ]
  };
}

// 组装校方请求头。SCHOOL_TOKEN 为 JWT，可带或不带 "Bearer " 前缀。
function buildAuthHeaders(env) {
  const token = env.SCHOOL_TOKEN || '';
  const auth = token.includes(' ') ? token : (token ? 'Bearer ' + token : '');
  const ts = String(Date.now());
  const reqid = ts + '-' + (crypto.randomUUID ? crypto.randomUUID() : '00000000-0000-4000-8000-000000000000');
  return {
    'accept': 'application/json',
    'accept-language': 'zh-CN,zh;q=0.8',
    'user-agent': 'okhttp-okgo/jeasonlzy',
    'authorization': auth,
    'timestamp': ts,
    'requestid': reqid,
    'os': 'android',
    'versionno': '120',
    'accept-encoding': 'gzip'
  };
  // 校方另带 sign / encrypt 头，但经 PC 端裸测：仅带 authorization 等即可 200，可忽略（2026-09-01 验证）。
}

// 把校方整包响应归一化成汇总 + 每个浴室明细。
function normalize(payload) {
  const list = Array.isArray(payload?.data) ? payload.data : [];
  const bathrooms = list.map((b) => {
    const maxLoad = num(b?.maxLoad);
    const useCount = num(b?.useCount);
    return {
      id: b?.id ?? null,
      name: b?.name ?? null,
      sex: num(b?.sex),
      maxLoad,
      useCount,
      bookingDeviceCnt: num(b?.bookingDeviceCnt),
      availableBookingDeviceCnt: num(b?.availableBookingDeviceCnt),
      vacant: Math.max(0, maxLoad - useCount)
    };
  });
  const total = bathrooms.reduce((s, b) => s + b.useCount, 0);
  // capacity 用 max(maxLoad, useCount) 累加，避免个别浴室 useCount>maxLoad 时出现负的空位。
  const capacity = bathrooms.reduce((s, b) => s + Math.max(b.maxLoad, b.useCount), 0);

  let status = 'unknown';
  if (capacity > 0) {
    const r = total / capacity;
    status = r >= 0.9 ? 'full' : r >= 0.6 ? 'busy' : r > 0 ? 'normal' : 'empty';
  }
  return { total, capacity, status, bathrooms };
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

