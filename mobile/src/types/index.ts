export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  accessTokenExpiresInSeconds: number;
  refreshTokenExpiresInSeconds: number;
}

export interface CreateGameResponse {
  gameId: string;
}

export interface AuthContextType {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isLoading: boolean;
  isSignout: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshAuthToken: () => Promise<void>;
}

export interface User {
  id: string;
  username: string;
}

// WebSocket Event Types
export interface MovePlayedEvent {
  type: 'move.played';
  data: {
    id: string;
    timestamp: string;
    // Add additional fields as needed from server
  };
}

export type WebSocketEvent = MovePlayedEvent;

export interface WebSocketMessage<T = any> {
  type: string;
  data: T;
  timestamp?: string;
}

export interface SubscriptionOptions {
  eventType: string;
  userId?: string;
}

export interface SubscriptionContextType {
  isConnected: boolean;
  isReconnecting: boolean;
  subscribe: (options: SubscriptionOptions, callback: (event: WebSocketEvent) => void) => string;
  unsubscribe: (subscriptionId: string) => void;
  send: (message: WebSocketMessage) => void;
}
