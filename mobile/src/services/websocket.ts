import { Config } from '../utils/config';

export interface WebSocketOptions {
  token?: string;
  userId?: string;
  maxReconnectAttempts?: number;
  maxReconnectDelay?: number;
}

interface Subscription {
  id: string;
  eventType: string;
  userId?: string;
  callback: (event: any) => void;
}

class WebSocketService {
  private ws: WebSocket | null = null;
  private url: string;
  private token: string | null = null;
  private userId: string | null = null;
  private subscriptions: Map<string, Subscription> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private maxReconnectDelay = 5000;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private messageQueue: string[] = [];
  private isIntentionallyClosed = false;
  private reconnectCallbacks: Set<(connected: boolean) => void> = new Set();

  constructor() {
    this.url = `${Config.WS_URL}/ws`;
  }

  public setToken(token: string): void {
    this.token = token;
  }

  public setUserId(userId: string): void {
    this.userId = userId;
  }

  public onReconnect(callback: (connected: boolean) => void): void {
    this.reconnectCallbacks.add(callback);
  }

  public connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        resolve();
        return;
      }

      this.isIntentionallyClosed = false;

      try {
        const wsUrl = this.token
          ? `${this.url}?token=${encodeURIComponent(this.token)}`
          : this.url;

        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
          console.log('[WebSocket] Connected');
          this.reconnectAttempts = 0;
          this.notifyReconnectCallbacks(true);
          this.flushMessageQueue();
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            this.handleMessage(message);
          } catch (error) {
            console.error('[WebSocket] Failed to parse message:', error);
          }
        };

        this.ws.onerror = (error) => {
          console.error('[WebSocket] Error:', error);
          reject(error);
        };

        this.ws.onclose = () => {
          console.log('[WebSocket] Closed');
          this.notifyReconnectCallbacks(false);
          if (!this.isIntentionallyClosed) {
            this.scheduleReconnect();
          }
        };
      } catch (error) {
        console.error('[WebSocket] Failed to create connection:', error);
        reject(error);
      }
    });
  }

  public disconnect(): void {
    this.isIntentionallyClosed = true;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.subscriptions.clear();
    this.messageQueue = [];
  }

  public subscribe(
    eventType: string,
    callback: (event: any) => void,
    userId?: string
  ): string {
    const subscriptionId = `${eventType}_${Date.now()}_${Math.random()}`;
    const subscription: Subscription = {
      id: subscriptionId,
      eventType,
      userId: userId || this.userId || undefined,
      callback,
    };

    this.subscriptions.set(subscriptionId, subscription);

    // Send subscription message if connected
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.sendSubscription(eventType, userId);
    }

    return subscriptionId;
  }

  public unsubscribe(subscriptionId: string): void {
    const subscription = this.subscriptions.get(subscriptionId);
    if (subscription) {
      this.subscriptions.delete(subscriptionId);

      // Check if this event type has other subscribers
      const hasOtherSubscribers = Array.from(this.subscriptions.values()).some(
        (sub) => sub.eventType === subscription.eventType
      );

      if (!hasOtherSubscribers && this.ws?.readyState === WebSocket.OPEN) {
        this.sendUnsubscription(subscription.eventType, subscription.userId);
      }
    }
  }

  public send(message: any): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      this.messageQueue.push(JSON.stringify(message));
    }
  }

  public isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  public isReconnecting(): boolean {
    return this.reconnectAttempts > 0 && this.reconnectAttempts < this.maxReconnectAttempts;
  }

  private handleMessage(message: any): void {
    const { type, data } = message;

    // Broadcast to relevant subscribers
    this.subscriptions.forEach((subscription) => {
      if (subscription.eventType === type) {
        try {
          subscription.callback({ type, data });
        } catch (error) {
          console.error('[WebSocket] Subscription callback error:', error);
        }
      }
    });
  }

  private scheduleReconnect(): void {
    if (this.isIntentionallyClosed || this.reconnectAttempts >= this.maxReconnectAttempts) {
      return;
    }

    this.reconnectAttempts++;
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts - 1), this.maxReconnectDelay);

    console.log(`[WebSocket] Scheduling reconnect in ${delay}ms (attempt ${this.reconnectAttempts})`);

    this.reconnectTimer = setTimeout(() => {
      this.connect().catch((error) => {
        console.error('[WebSocket] Reconnection failed:', error);
        this.scheduleReconnect();
      });
    }, delay);
  }

  private sendSubscription(eventType: string, userId?: string): void {
    const message = {
      type: 'subscribe',
      eventType,
      ...(userId && { userId }),
    };
    this.send(message);
  }

  private sendUnsubscription(eventType: string, userId?: string): void {
    const message = {
      type: 'unsubscribe',
      eventType,
      ...(userId && { userId }),
    };
    this.send(message);
  }

  private flushMessageQueue(): void {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift();
      if (message && this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(message);
      }
    }
  }

  private notifyReconnectCallbacks(connected: boolean): void {
    this.reconnectCallbacks.forEach((callback) => {
      try {
        callback(connected);
      } catch (error) {
        console.error('[WebSocket] Reconnect callback error:', error);
      }
    });
  }
}

export default new WebSocketService();
