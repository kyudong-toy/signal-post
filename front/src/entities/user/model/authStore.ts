import {create} from 'zustand';
import {Client} from '@stomp/stompjs';

interface AuthState {
  accessToken: string | null;
  isStompConnected: boolean;
  stompClient: Client | null;
  setAccessToken: (token: string) => void;
  setStompConnected: (connected: boolean) => void;
  clearAuth: () => void;
  subscribe: (destination: string, callback: (message: any) => void) => void;
  unsubscribe: (destination: string) => void;
  sendMessage: (destination: string, body: any) => void;
}

const baseWsURL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080/ws';

export const useAuthStore = create<AuthState>((set, get) => ({
  accessToken: null,
  isStompConnected: false,
  stompClient: null,
  setAccessToken: (token) => {
    const prevClient = get().stompClient;
    if (prevClient) {
      prevClient.deactivate();
    }

    const client = new Client({
      brokerURL: baseWsURL,
      connectHeaders: {Authorization: `Bearer ${token}`},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => console.log('STOMP debug:', str),
    });

    client.onConnect = () => {
      console.log('STOMP connected');
      set({isStompConnected: true, stompClient: client});
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      set({isStompConnected: false});
    };

    client.onWebSocketClose = () => {
      console.log('WebSocket closed, reconnecting...');
      set({isStompConnected: false});
    };

    client.onWebSocketError = (error) => {
      console.error('WebSocket error:', error);
      set({isStompConnected: false});
    };

    client.activate();
    set({ accessToken: token, stompClient: client });
  },

  setStompConnected: (connected) => set({ isStompConnected: connected }),

  clearAuth: () => {
    const client = get().stompClient;
    if (client) {
      client.deactivate();
    }
    set({ accessToken: null, isStompConnected: false, stompClient: null });
  },

  subscribe: (destination, callback) => {
    const client = get().stompClient;
    if (client?.active) {
      return client.subscribe(destination, (message) => {
        callback(JSON.parse(message.body));
      });
    } else {
      console.warn('STOMP client not active for subscription:', destination);
      return null;
    }
  },

  unsubscribe: (destination) => {
    const client = get().stompClient;
    if (client?.active) {
      client.unsubscribe(destination);
    }
  },

  sendMessage: (destination, body) => {
    const client = get().stompClient;
    if (client?.active) {
      client.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.error('STOMP client not active for sending:', destination);
    }
  },
}));