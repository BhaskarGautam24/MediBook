import { createContext, useContext, useReducer, useEffect } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

// Initial state
const initialState = {
  user: JSON.parse(localStorage.getItem('medibook_user') || 'null'),
  token: localStorage.getItem('medibook_token') || null,
  isAuthenticated: !!localStorage.getItem('medibook_token'),
  isLoading: true,
};

// Reducer
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
      return {
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
      };
    case 'UPDATE_USER':
      return { ...state, user: { ...state.user, ...action.payload } };
    default:
      return state;
  }
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // On mount — validate stored token
  useEffect(() => {
    const validateStoredToken = async () => {
      const token = localStorage.getItem('medibook_token');
      if (!token) {
        dispatch({ type: 'AUTH_LOADED' });
        return;
      }
      try {
        const user = await authService.validateToken();
        dispatch({
          type: 'AUTH_SUCCESS',
          payload: { user, token },
        });
      } catch {
        // Token invalid — clear storage
        localStorage.removeItem('medibook_token');
        localStorage.removeItem('medibook_user');
        dispatch({ type: 'LOGOUT' });
      }
    };
    validateStoredToken();
  }, []);

  // Login
  const login = async (credentials) => {
    const response = await authService.login(credentials);
    const user = {
      userId: response.userId,
      fullName: response.fullName,
      email: response.email,
      role: response.role,
      profilePicUrl: response.profilePicUrl,
    };
    localStorage.setItem('medibook_token', response.token);
    localStorage.setItem('medibook_user', JSON.stringify(user));
    dispatch({ type: 'AUTH_SUCCESS', payload: { user, token: response.token } });
    return user;
  };

  // Register
  const register = async (data) => {
    const response = await authService.register(data);
    const user = {
      userId: response.userId,
      fullName: response.fullName,
      email: response.email,
      role: response.role,
      profilePicUrl: response.profilePicUrl,
    };
    localStorage.setItem('medibook_token', response.token);
    localStorage.setItem('medibook_user', JSON.stringify(user));
    dispatch({ type: 'AUTH_SUCCESS', payload: { user, token: response.token } });
    return user;
  };

  // Logout
  const logout = () => {
    localStorage.removeItem('medibook_token');
    localStorage.removeItem('medibook_user');
    dispatch({ type: 'LOGOUT' });
  };

  // Update user in context
  const updateUser = (userData) => {
    const updatedUser = { ...state.user, ...userData };
    localStorage.setItem('medibook_user', JSON.stringify(updatedUser));
    dispatch({ type: 'UPDATE_USER', payload: userData });
  };

  return (
    <AuthContext.Provider value={{
      ...state,
      login,
      register,
      logout,
      updateUser,
    }}>
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
