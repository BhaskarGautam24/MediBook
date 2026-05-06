const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

function getToken() {
  return localStorage.getItem('medibook_token');
}

async function request(method, path, data = null) {
  const token = getToken();

  const headers = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };

  const config = {
    method,
    headers,
    ...(data && { body: JSON.stringify(data) }),
  };

  const response = await fetch(`${API_BASE}${path}`, config);

  if (response.status === 401) {
    localStorage.removeItem('medibook_token');
    localStorage.removeItem('medibook_user');
    window.location.href = '/login';
    throw new Error('Session expired. Please login again.');
  }

  // Handle 204 No Content
  if (response.status === 204) return null;

  const responseData = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = responseData?.message || responseData?.error || `Request failed (${response.status})`;
    throw new Error(errorMsg);
  }

  return responseData;
}

const api = {
  get: (path) => request('GET', path),
  post: (path, data) => request('POST', path, data),
  put: (path, data) => request('PUT', path, data),
  delete: (path) => request('DELETE', path),
};

export default api;
