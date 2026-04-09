// API URL: sempre localhost:4000 em dev, /api em prod
const API_URL = window.location.hostname === 'localhost'
  ? 'http://localhost:4000/api'
  : '/api';

async function request(path, options = {}) {
  const token = localStorage.getItem('loveall_token');
  const fullPath = `${API_URL}${path}`;

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
    ...options,
  };

  let res;
  try {
    res = await fetch(fullPath, config);
  } catch (err) {
    throw new Error(`Falha de conexão: ${err.message}. Tente conectar à rede: ${fullPath}`);
  }

  let data;
  const contentType = res.headers.get('content-type');

  try {
    if (contentType && contentType.includes('application/json')) {
      data = await res.json();
    } else {
      const text = await res.text();
      data = { error: `Resposta inválida: ${text.substring(0, 100)}` };
    }
  } catch (err) {
    throw new Error(`Erro ao fazer parsing: ${err.message}`);
  }

  if (!res.ok) {
    throw new Error(data.error || `Erro ${res.status} na requisição`);
  }

  return data;
}

export const api = {
  // Auth
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  getMe: () => request('/auth/me'),

  // Profiles
  updateProfile: (data) => request('/profiles/me', { method: 'PUT', body: JSON.stringify(data) }),
  discover: () => request('/profiles/discover'),
  getProfile: (id) => request(`/profiles/${id}`),

  // Likes (ilimitados!)
  like: (to_user, is_super_like = false) => request('/likes/like', { method: 'POST', body: JSON.stringify({ to_user, is_super_like }) }),
  dislike: (to_user) => request('/likes/dislike', { method: 'POST', body: JSON.stringify({ to_user }) }),
  whoLikedMe: () => request('/likes/who-liked-me'),

  // Matches
  getMatches: () => request('/matches'),
  unmatch: (matchId) => request(`/matches/${matchId}`, { method: 'DELETE' }),

  // Messages
  getMessages: (matchId, page = 1) => request(`/messages/${matchId}?page=${page}`),
  sendMessage: (matchId, content) => request(`/messages/${matchId}`, { method: 'POST', body: JSON.stringify({ content }) }),

  // Safety
  report: (data) => request('/safety/report', { method: 'POST', body: JSON.stringify(data) }),
  block: (blocked_id) => request('/safety/block', { method: 'POST', body: JSON.stringify({ blocked_id }) }),
  unblock: (id) => request(`/safety/block/${id}`, { method: 'DELETE' }),
  getBlocked: () => request('/safety/blocked'),
  getTips: () => request('/safety/tips'),

  // Calls (premium)
  checkCall: (data) => request('/calls/check', { method: 'POST', body: JSON.stringify(data) }),
  logCall: (data) => request('/calls/log', { method: 'POST', body: JSON.stringify(data) }),
  subscribe: (plan) => request('/calls/subscribe', { method: 'POST', body: JSON.stringify({ plan }) }),
  getPaymentStatus: (transactionId) => request(`/calls/payment-status/${transactionId}`),
  getPlans: () => request('/calls/plans'),
  getMyPlan: () => request('/calls/my-plan'),
};
