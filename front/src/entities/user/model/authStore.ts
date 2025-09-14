import { create } from 'zustand';
import { persist } from "zustand/middleware";

interface AuthState {
  accessToken: string | null;
  user: {
    id: number;
    username: string;
  } | null;
  isAuthenticated: boolean;

  setAccessToken: (token: string) => void;
  setUser: (user: AuthState['user']) => void;
  clearAuth: () => void;
  isTokenValid: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      isAuthenticated: false,

      setAccessToken: (token) => {
        set({
          accessToken: token,
          isAuthenticated: true
        });
      },

      setUser: (user) => {
        set({ user });
      },

      clearAuth: () => {
        set({
          accessToken: null,
          user: null,
          isAuthenticated: false
        });
      },

      isTokenValid: () => {
        const token = get().accessToken;
        if (!token) {
          return false;
        }

        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          return payload.exp * 1000 > Date.now();
        } catch {
          return false;
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated
      }),
    }
  )
);