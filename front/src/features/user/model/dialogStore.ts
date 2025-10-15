import { create } from 'zustand';

type DialogView = 'login' | 'signup' | null;

interface DialogState {
  view: DialogView;
  openDialog: (view: DialogView) => void;
  closeDialog: () => void;
}

export const useDialogStore = create<DialogState>((set) => ({
  view: null,
  openDialog: (view) => set({ view }),
  closeDialog: () => set({ view: null }),
}));