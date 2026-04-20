const API_BASE_URLS = {
  auth: import.meta.env.VITE_AUTH_URL || 'http://localhost:8081',
  provider: import.meta.env.VITE_PROVIDER_URL || 'http://localhost:8082',
  schedule: import.meta.env.VITE_SCHEDULE_URL || 'http://localhost:8083',
  appointment: import.meta.env.VITE_APPOINTMENT_URL || 'http://localhost:8084',
  payment: import.meta.env.VITE_PAYMENT_URL || 'http://localhost:8085',
  review: import.meta.env.VITE_REVIEW_URL || 'http://localhost:8086',
  notification: import.meta.env.VITE_NOTIFICATION_URL || 'http://localhost:8087',
  record: import.meta.env.VITE_RECORD_URL || 'http://localhost:8088',
};

/**
 * Creates an axios-like fetch wrapper for a given service.
 * Automatically attaches JWT token and handles errors.
 */
const createServiceApi = (serviceName) => {
  const baseURL = API_BASE_URLS[serviceName];

  const request = async (method, path, data = null, options = {}) => {
    const token = localStorage.getItem('medibook_token');

    const headers = {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    const config = {
      method,
      headers,
      ...(data && { body: JSON.stringify(data) }),
    };

    const response = await fetch(`${baseURL}${path}`, config);

    // Handle 401 — redirect to login
    if (response.status === 401) {
      localStorage.removeItem('medibook_token');
      localStorage.removeItem('medibook_user');
      window.location.href = '/login';
      throw new Error('Session expired. Please login again.');
    }

    const responseData = await response.json().catch(() => null);

    if (!response.ok) {
      const errorMsg = responseData?.message || responseData?.error || `Request failed with status ${response.status}`;
      throw new Error(errorMsg);
    }

    return responseData;
  };

  return {
    get: (path, options) => request('GET', path, null, options),
    post: (path, data, options) => request('POST', path, data, options),
    put: (path, data, options) => request('PUT', path, data, options),
    delete: (path, options) => request('DELETE', path, null, options),
  };
};

// Export pre-configured API instances for each service
export const authApi = createServiceApi('auth');
export const providerApi = createServiceApi('provider');
export const scheduleApi = createServiceApi('schedule');
export const appointmentApi = createServiceApi('appointment');
export const paymentApi = createServiceApi('payment');
export const reviewApi = createServiceApi('review');
export const notificationApi = createServiceApi('notification');
export const recordApi = createServiceApi('record');

export default API_BASE_URLS;
