import React, { createContext, useContext, useEffect, useReducer, ReactNode } from 'react';
import { AuthContextType, User } from '../types';
import { authService } from '../services/api';
import { StorageService } from '../services/storage';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthState {
  isLoading: boolean;
  isSignout: boolean;
  user: User | null;
  token: string | null;
  refreshToken: string | null;
}

type AuthAction =
  | { type: 'RESTORE_TOKEN'; payload: { token: string | null; refreshToken: string | null; user: User | null } }
  | { type: 'SIGN_IN'; payload: { token: string; refreshToken: string; user: User } }
  | { type: 'SIGN_OUT' }
  | { type: 'SET_LOADING'; payload: boolean };

const initialState: AuthState = {
  isLoading: true,
  isSignout: false,
  user: null,
  token: null,
  refreshToken: null,
};

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'RESTORE_TOKEN':
      return {
        ...state,
        isLoading: false,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        user: action.payload.user,
      };
    case 'SIGN_IN':
      return {
        ...state,
        isSignout: false,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        user: action.payload.user,
      };
    case 'SIGN_OUT':
      return {
        ...state,
        isSignout: true,
        token: null,
        refreshToken: null,
        user: null,
      };
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    default:
      return state;
  }
}

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Bootstrap async data when app loads
  useEffect(() => {
    const bootstrapAsync = async () => {
      try {
        const token = await StorageService.getAccessToken();
        const refreshToken = await StorageService.getRefreshToken();
        const user = await StorageService.getUser();

        dispatch({
          type: 'RESTORE_TOKEN',
          payload: { token, refreshToken, user },
        });
      } catch (e) {
        console.error('Failed to restore token:', e);
        dispatch({
          type: 'RESTORE_TOKEN',
          payload: { token: null, refreshToken: null, user: null },
        });
      }
    };

    bootstrapAsync();
  }, []);

  const authContext: AuthContextType = {
    user: state.user,
    token: state.token,
    refreshToken: state.refreshToken,
    isLoading: state.isLoading,
    isSignout: state.isSignout,

    login: async (username: string, password: string) => {
      try {
        dispatch({ type: 'SET_LOADING', payload: true });
        const response = await authService.login({ username, password });

        await StorageService.saveTokens(response.accessToken, response.refreshToken);

        const user: User = {
          id: username, // API doesn't return user ID, using username as fallback
          username,
        };
        await StorageService.saveUser(user);

        dispatch({
          type: 'SIGN_IN',
          payload: {
            token: response.accessToken,
            refreshToken: response.refreshToken,
            user,
          },
        });
      } catch (error) {
        console.error('Login failed:', error);
        throw error;
      } finally {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    },

    logout: async () => {
      try {
        dispatch({ type: 'SET_LOADING', payload: true });
        await StorageService.clearAll();
        dispatch({ type: 'SIGN_OUT' });
      } catch (error) {
        console.error('Logout failed:', error);
        throw error;
      } finally {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    },

    refreshAuthToken: async () => {
      try {
        if (!state.refreshToken) {
          throw new Error('No refresh token available');
        }

        const response = await authService.refresh(state.refreshToken);
        await StorageService.saveTokens(response.accessToken, response.refreshToken);

        dispatch({
          type: 'SIGN_IN',
          payload: {
            token: response.accessToken,
            refreshToken: response.refreshToken,
            user: state.user!,
          },
        });
      } catch (error) {
        console.error('Token refresh failed:', error);
        await StorageService.clearAll();
        dispatch({ type: 'SIGN_OUT' });
        throw error;
      }
    },
  };

  return <AuthContext.Provider value={authContext}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
