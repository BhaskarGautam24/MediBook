import { authApi } from './api';

/**
 * Auth service — handles all authentication API calls.
 */
const authService = {
  register: (data) => authApi.post('/api/v1/auth/register', data),

  login: (data) => authApi.post('/api/v1/auth/login', data),

  validateToken: () => authApi.get('/api/v1/auth/validate'),

  getProfile: (userId) => authApi.get(`/api/v1/auth/profile/${userId}`),

  updateProfile: (userId, data) => authApi.put(`/api/v1/auth/profile/${userId}`, data),

  changePassword: (userId, data) => authApi.put(`/api/v1/auth/password/${userId}`, data),

  deactivateAccount: (userId) => authApi.put(`/api/v1/auth/deactivate/${userId}`),

  getAllUsers: () => authApi.get('/api/v1/auth/users'),

  getUsersByRole: (role) => authApi.get(`/api/v1/auth/users/role/${role}`),
};

export default authService;
