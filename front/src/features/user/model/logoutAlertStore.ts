import { create } from 'zustand';

type LogoutAlertView = 'logout' | null;

interface LogoutAlertState {
  view: LogoutAlertView;
  openAlert: (view: LogoutAlertView) => void;
  closeAlert: () => void;
}

export const useLogoutAlertStore = create<LogoutAlertState>((set) => ({
  view: null,
  openAlert: (view) => set({ view }),
  closeAlert: () => set({ view: null }),
}));