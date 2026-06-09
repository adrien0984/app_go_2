import React, { createContext, useContext, useEffect, useRef, useState, ReactNode } from 'react';
import { SubscriptionContextType, WebSocketEvent, SubscriptionOptions } from '../types';
import { useAuth } from './AuthContext';
import websocketService from '../services/websocket';

const SubscriptionContext = createContext<SubscriptionContextType | undefined>(undefined);

interface SubscriptionCallback {
  callback: (event: WebSocketEvent) => void;
}

interface SubscriptionProviderProps {
  children: ReactNode;
}

export const SubscriptionProvider: React.FC<SubscriptionProviderProps> = ({ children }) => {
  const { token, user, isSignout } = useAuth();
  const [isConnected, setIsConnected] = useState(false);
  const [isReconnecting, setIsReconnecting] = useState(false);
  const subscriptionCallbacksRef = useRef<Map<string, SubscriptionCallback>>(new Map());
  const initializeRef = useRef(false);

  // Initialize WebSocket when token is available
  useEffect(() => {
    if (!token || !user || isSignout) {
      // Disconnect if not authenticated
      if (websocketService.isConnected()) {
        websocketService.disconnect();
        setIsConnected(false);
      }
      return;
    }

    // Only initialize once
    if (initializeRef.current) {
      return;
    }
    initializeRef.current = true;

    const initializeWebSocket = async () => {
      try {
        websocketService.setToken(token);
        websocketService.setUserId(user.id);

        // Setup reconnect handler
        websocketService.onReconnect((connected) => {
          setIsConnected(connected);
          setIsReconnecting(!connected && websocketService.isReconnecting());
        });

        await websocketService.connect();
        setIsConnected(true);
        setIsReconnecting(false);
      } catch (error) {
        console.error('Failed to initialize WebSocket:', error);
        setIsConnected(false);
        setIsReconnecting(true);
      }
    };

    initializeWebSocket();

    return () => {
      // Don't disconnect on unmount - let the service manage the connection
      // Only disconnect on complete auth logout
    };
  }, [token, user, isSignout]);

  // Disconnect when signing out
  useEffect(() => {
    if (isSignout) {
      websocketService.disconnect();
      subscriptionCallbacksRef.current.clear();
      setIsConnected(false);
      initializeRef.current = false;
    }
  }, [isSignout]);

  const subscribe = (
    options: SubscriptionOptions,
    callback: (event: WebSocketEvent) => void
  ): string => {
    const subscriptionId = websocketService.subscribe(options.eventType, callback, options.userId);

    // Store callback reference for cleanup
    subscriptionCallbacksRef.current.set(subscriptionId, { callback });

    return subscriptionId;
  };

  const unsubscribe = (subscriptionId: string): void => {
    websocketService.unsubscribe(subscriptionId);
    subscriptionCallbacksRef.current.delete(subscriptionId);
  };

  const send = (message: any): void => {
    websocketService.send(message);
  };

  const contextValue: SubscriptionContextType = {
    isConnected,
    isReconnecting,
    subscribe,
    unsubscribe,
    send,
  };

  return (
    <SubscriptionContext.Provider value={contextValue}>
      {children}
    </SubscriptionContext.Provider>
  );
};

export const useSubscription = (): SubscriptionContextType => {
  const context = useContext(SubscriptionContext);
  if (!context) {
    throw new Error('useSubscription must be used within a SubscriptionProvider');
  }
  return context;
};
