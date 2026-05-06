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
    // Trust stored user data — backend will reject invalid tokens on API calls
    const user = JSON.parse(localStorage.getItem('medibook_user') || 'null');
    if (user) {
      dispatch({ type: 'AUTH_SUCCESS', payload: { user, token } });
    } else {
      dispatch({ type: 'AUTH_LOADED' });
    }
  }, []);

  const login = async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    const user = {
      id: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
    };
    localStorage.setItem('medibook_token', response.token);
    localStorage.setItem('medibook_user', JSON.stringify(user));
    dispatch({ type: 'AUTH_SUCCESS', payload: { user, token: response.token } });
    return user;
  };

  const register = async (data) => {
    const response = await api.post('/auth/register', data);
    const user = {
      id: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
    };
    localStorage.setItem('medibook_token', response.token);
    localStorage.setItem('medibook_user', JSON.stringify(user));
    dispatch({ type: 'AUTH_SUCCESS', payload: { user, token: response.token } });
    return user;
  };

  const logout = () => {
    localStorage.removeItem('medibook_token');
    localStorage.removeItem('medibook_user');
    dispatch({ type: 'LOGOUT' });
  };

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
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
