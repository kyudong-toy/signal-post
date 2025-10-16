import { create } from 'zustand';

type LoginDialogView = 'login' | 'signup' | null;

interface LoginDialogState {
  view: LoginDialogView;
  openDialog: (view: LoginDialogView) => void;
  closeDialog: () => void;
}

export const useLoginDialogStore = create<LoginDialogState>((set) => ({
  view: null,
  openDialog: (view) => set({ view }),
  closeDialog: () => set({ view: null }),
}));