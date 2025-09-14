import { useEffect } from 'react';
import { useAuthStore } from "../model/authStore.ts";
import { useWebSocketStore } from "../model/socketStore.ts";
import type { UserLoginRes } from "../model/types.ts";

export const useAuth = () => {
  const auth = useAuthStore();
  const websocket = useWebSocketStore();

  // 토큰이 변경되면 웹소켓 연결
  useEffect(() => {
    if (auth.accessToken && auth.isAuthenticated) {
      websocket.connect(auth.accessToken);
    } else {
      websocket.disconnect();
    }
  }, [auth.accessToken, auth.isAuthenticated]);

  // 토큰 유효성 검사
  useEffect(() => {
    if (auth.accessToken && !auth.isTokenValid()) {
      auth.clearAuth();
    }
  }, []);

  const setAuth = (data: UserLoginRes) => {
    auth.setAccessToken(data.token);
    const user = {
      id: data.id,
      username: data.username
    };
    auth.setUser(user);
  };

  const clearAuth = () => {
    websocket.disconnect();
    auth.clearAuth();
  };

  return {
    ...auth,
    setAuth,
    clearAuth,
    isConnected: websocket.isConnected,
  };
};