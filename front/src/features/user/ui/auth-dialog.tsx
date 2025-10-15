import { useDialogStore } from '../model/dialogStore';
import { useMediaQuery } from '@/shared/lib/useMediaQuery';
import { LoginDialogForm } from './login-dialog-form.tsx';
import { SignupDialogForm } from './signup-dialog-form.tsx';
import {Dialog, DialogContent} from "@shared/ui/dialog.tsx";
import {useEffect, useState} from "react";

export const AuthDialog = () => {
  const { view, openDialog, closeDialog } = useDialogStore();
  const isDesktop = useMediaQuery('(min-width: 1024px)');
  const [renderedView, setRenderedView] = useState(view);

  useEffect(() => {
    if (view !== null) {
      setRenderedView(view);
    }
  }, [view]);

  if (!isDesktop) {
    return null;
  }

  const handleLoginSuccess = () => {
    closeDialog();
  };

  const handleSignupSuccess = () => {
    openDialog('login');
  };

  return (
    <Dialog open={ !!view } onOpenChange={() => closeDialog()}>
      <DialogContent className="sm:max-w-md">
        {renderedView === 'login' && (
          <LoginDialogForm
            onSwitchToSignup={() => openDialog('signup')}
            onLoginSuccess={ handleLoginSuccess }
          />
        )}
        {renderedView === 'signup' && (
          <SignupDialogForm
            onSwitchToLogin={() => openDialog('login')}
            onSignupSuccess={ handleSignupSuccess }
          />
        )}
      </DialogContent>
    </Dialog>
  );
};