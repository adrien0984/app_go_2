import axios, { AxiosInstance } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Config } from '../utils/config';
import { CreateGameResponse, LoginRequest, TokenResponse } from '../types';

const api: AxiosInstance = axios.create({
  baseURL: Config.API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to add token to requests
api.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const authService = {
  async login(credentials: LoginRequest): Promise<TokenResponse> {
    const response = await api.post<TokenResponse>('/auth/login', credentials);
    return response.data;
  },

  async refresh(refreshToken: string): Promise<TokenResponse> {
    const response = await api.post<TokenResponse>('/auth/refresh', {
      refreshToken,
    });
    return response.data;
  },

  async logout(): Promise<void> {
    await Promise.all([
      AsyncStorage.removeItem('access_token'),
      AsyncStorage.removeItem('refresh_token'),
      AsyncStorage.removeItem('user'),
    ]);
  },
};

export const health = {
  async check() {
    const response = await api.get('/health');
    return response.data;
  },
};

export const gamesService = {
  async createGame(boardSize: number = 19): Promise<CreateGameResponse> {
    const response = await api.post<CreateGameResponse>('/games', { boardSize });
    return response.data;
  },
};

export default api;
