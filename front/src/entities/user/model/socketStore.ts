import { create } from 'zustand';
import { Client } from '@stomp/stompjs';

interface WebSocketState {
  isConnected: boolean;
  stompClient: Client | null;
  subscriptions: Map<string, any>;

  connect: (token: string) => void;
  disconnect: () => void;
  subscribe: (destination: string, callback: (message: any) => void) => string;
  unsubscribe: (subscriptionId: string) => void;
  sendMessage: (destination: string, body: any) => void;
  reconnect: () => void;
}

const baseWsURL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080/ws';

export const useWebSocketStore = create<WebSocketState>((set, get) => ({
  isConnected: false,
  stompClient: null,
  subscriptions: new Map(),

  connect: (token) => {
    const prevClient = get().stompClient;
    if (prevClient?.active) {
      prevClient.deactivate();
    }

    const client = new Client({
      brokerURL: baseWsURL,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => console.log('STOMP debug:', str),
    });

    client.onConnect = () => {
      console.log('STOMP connected');
      set({ isConnected: true, stompClient: client });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      set({ isConnected: false });
    };

    client.onWebSocketClose = () => {
      console.log('WebSocket closed, reconnecting...');
      set({ isConnected: false });
    };

    client.onWebSocketError = (error) => {
      console.error('WebSocket error:', error);
      set({ isConnected: false });
    };

    client.activate();
    set({ stompClient: client });
  },

  disconnect: () => {
    const { stompClient, subscriptions } = get();

    // 모든 구독 해제
    subscriptions.forEach((subscription) => {
      if (subscription && typeof subscription.unsubscribe === 'function') {
        subscription.unsubscribe();
      }
    });

    if (stompClient?.active) {
      stompClient.deactivate();
    }

    set({
      isConnected: false,
      stompClient: null,
      subscriptions: new Map()
    });
  },

  subscribe: (destination, callback) => {
    const { stompClient, subscriptions } = get();

    if (!stompClient?.active) {
      console.warn('STOMP client not active for subscription:', destination);
      return '';
    }

    const subscription = stompClient.subscribe(destination, (message) => {
      callback(JSON.parse(message.body));
    });

    const subscriptionId = `${destination}-${Date.now()}`;
    subscriptions.set(subscriptionId, subscription);

    set({ subscriptions: new Map(subscriptions) });
    return subscriptionId;
  },

  unsubscribe: (subscriptionId) => {
    const { subscriptions } = get();
    const subscription = subscriptions.get(subscriptionId);

    if (subscription && typeof subscription.unsubscribe === 'function') {
      subscription.unsubscribe();
      subscriptions.delete(subscriptionId);
      set({ subscriptions: new Map(subscriptions) });
    }
  },

  sendMessage: (destination, body) => {
    const client = get().stompClient;

    if (!client?.active) {
      console.error('STOMP client not active for sending:', destination);
      return;
    }

    client.publish({
      destination,
      body: JSON.stringify(body),
    });
  },

  reconnect: () => {
    const { stompClient } = get();
    if (stompClient && !stompClient.active) {
      stompClient.activate();
    }
  },
}));