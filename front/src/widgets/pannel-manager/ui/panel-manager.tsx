import { NotificationPanel } from "@/widgets/notification/ui/notification-panel.tsx";

interface PanelManagerProps {
  activePanel: string | null;
}

const PanelManager = ({ activePanel }: PanelManagerProps) => {
  if (!activePanel) {
    return null;
  }

  return (
    <div className="dark:bg-black border-r bg-white z-20 animate-in slide-in-from-left duration-300">
      { activePanel === 'notification' && <NotificationPanel /> }
      {/*{ activePanel === 'messages' && <ChatPanel /> }*/}
    </div>
  );
};

export default PanelManager;