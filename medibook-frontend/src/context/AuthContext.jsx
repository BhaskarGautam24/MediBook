import { createContext, useContext, useReducer, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

const initialState = {
  user: JSON.parse(localStorage.getItem('medibook_user') || 'null'),
  token: localStorage.getItem('medibook_token') || null,
  isAuthenticated: !!localStorage.getItem('medibook_token'),
  isLoading: true,
};

function authReducer(state, action) {
  switch (action.type) {
    case 'AUTH_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        isAuthenticated: true,
        isLoading: false,
      };
    case 'UPDATE_USER':
      return {
        ...state,
        user: { ...state.user, ...action.payload },
      };
    case 'AUTH_LOADED':
      return { ...state, isLoading: false };
    case 'LOGOUT':
      return { user: null, token: null, isAuthenticated: false, isLoading: false };
    default:
      return state;
  }
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  useEffect(() => {
    const token = localStorage.getItem('medibook_token');
    if (!token) {
      dispatch({ type: 'AUTH_LOADED' });
      return;
    }
    const user = JSON.parse(localStorage.getItem('medibook_user') || 'null');
    if (user) {
      dispatch({ type: 'AUTH_SUCCESS', payload: { user, token } });
    } else {
      dispatch({ type: 'AUTH_LOADED' });
    }
  }, []);

  /** Store auth data from login/register/OAuth response */
  const storeAuth = (response) => {
    const user = {
      id: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
      profilePicture: response.profilePicture || '',
      emailVerified: response.emailVerified,
    };
    localStorage.setItem('medibook_token', response.token);
    if (response.refreshToken) {
      localStorage.setItem('medibook_refresh_token', response.refreshToken);
    }
    localStorage.setItem('medibook_user', JSON.stringify(user));
    dispatch({ type: 'AUTH_SUCCESS', payload: { user, token: response.token } });
    return user;
  };

  const login = async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    return storeAuth(response);
  };

  const register = async (data) => {
    const response = await api.post('/auth/register', data);
    return storeAuth(response);
  };

  /** Called from OAuthCallback page after Google redirect */
  const loginWithOAuthData = (params) => {
    const user = {
      id: parseInt(params.userId),
      name: params.name,
      email: params.email,
      role: params.role,
      profilePicture: params.profilePicture || '',
      emailVerified: true,
    };
    localStorage.setItem('medibook_token', params.token);
    if (params.refreshToken) {
      localStorage.setItem('medibook_refresh_token', params.refreshToken);
    }
    localStorage.setItem('medibook_user', JSON.stringify(user));
    dispatch({ type: 'AUTH_SUCCESS', payload: { user, token: params.token } });
    return user;
  };

  /** Update user data in context (e.g., after profile update) */
  const updateUser = (updates) => {
    const currentUser = JSON.parse(localStorage.getItem('medibook_user') || '{}');
    const updatedUser = { ...currentUser, ...updates };
    localStorage.setItem('medibook_user', JSON.stringify(updatedUser));
    dispatch({ type: 'UPDATE_USER', payload: updates });
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('medibook_refresh_token');
    try {
      if (refreshToken) {
        await api.post('/auth/logout', { refreshToken });
      }
    } catch {
      // Ignore logout API errors
    }
    localStorage.removeItem('medibook_token');
    localStorage.removeItem('medibook_refresh_token');
    localStorage.removeItem('medibook_user');
    dispatch({ type: 'LOGOUT' });
  };

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout, loginWithOAuthData, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}

export default AuthContext;
