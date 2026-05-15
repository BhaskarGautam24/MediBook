const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

function getToken() {
  return localStorage.getItem('medibook_token');
}

function getRefreshToken() {
  return localStorage.getItem('medibook_refresh_token');
}

/** Clear all auth data and redirect to login. */
function clearAuthAndRedirect() {
  localStorage.removeItem('medibook_token');
  localStorage.removeItem('medibook_refresh_token');
  localStorage.removeItem('medibook_user');
  window.location.href = '/login';
}

/** Try to refresh the access token using the refresh token. */
async function tryRefreshToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  try {
    const response = await fetch(`${API_BASE}/auth/refresh-token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) return false;

    const data = await response.json();
    localStorage.setItem('medibook_token', data.token);
    if (data.refreshToken) {
      localStorage.setItem('medibook_refresh_token', data.refreshToken);
    }
    const user = {
      id: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
      profilePicture: data.profilePicture || '',
      emailVerified: data.emailVerified,
    };
    localStorage.setItem('medibook_user', JSON.stringify(user));
    return true;
  } catch {
    return false;
  }
}

/** Handle 401 response — try refresh, if failed clear auth. */
async function handleUnauthorized() {
  const refreshed = await tryRefreshToken();
  if (!refreshed) {
    clearAuthAndRedirect();
    throw new Error('Session expired. Please login again.');
  }
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

  let response = await fetch(`${API_BASE}${path}`, config);

  // If 401, try refreshing the token and retry once
  if (response.status === 401) {
    await handleUnauthorized();
    config.headers.Authorization = `Bearer ${getToken()}`;
    response = await fetch(`${API_BASE}${path}`, config);
  }

  if (response.status === 204) return null;

  const responseData = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = responseData?.message || responseData?.error || `Request failed (${response.status})`;
    throw new Error(errorMsg);
  }

  return responseData;
}

/** Upload a file using multipart/form-data. */
async function uploadFile(path, file, fieldName = 'file') {
  const token = getToken();
  const formData = new FormData();
  formData.append(fieldName, file);

  const headers = token ? { Authorization: `Bearer ${token}` } : {};

  let response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers,
    body: formData,
  });

  // If 401, try refreshing
  if (response.status === 401) {
    await handleUnauthorized();
    response = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${getToken()}` },
      body: formData,
    });
  }

  const responseData = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = responseData?.message || `Upload failed (${response.status})`;
    throw new Error(errorMsg);
  }

  return responseData;
}

const api = {
  get: (path) => request('GET', path),
  post: (path, data) => request('POST', path, data),
  put: (path, data) => request('PUT', path, data),
  delete: (path) => request('DELETE', path),
  upload: (path, file, fieldName) => uploadFile(path, file, fieldName),
};

export default api;
