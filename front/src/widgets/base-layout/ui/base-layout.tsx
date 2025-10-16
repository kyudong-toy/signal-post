import { useMediaQuery } from "@shared/lib/useMediaQuery";
import { Outlet } from "react-router-dom";
import { useState } from "react";
import DesktopNavbar from "@/widgets/desktop-navbar/ui/desktop-navbar.tsx";
import PanelManager from "@/widgets/pannel-manager/ui/panel-manager.tsx";
import MobileNavbar from "@/widgets/mobile-navbar/ui/mobile-navbar.tsx";
import MobileHeader from "@/widgets/mobile-header/ui/mobile-header.tsx";


const BaseLayout = () => {
  const isDesktop = useMediaQuery('(min-width: 1024px)');
  const [activePanel, setActivePanel] = useState<string | null>(null);

  const handlePanelToggle = (panelName: string) => {
    setActivePanel(prev => {
      return prev === panelName ? null : panelName;
    });
  };

  if (isDesktop) {
    return (
      <div className="flex h-screen bg-gray-100 dark:bg-neutral-900">
        <DesktopNavbar onPanelToggle={handlePanelToggle} />
        <PanelManager activePanel={activePanel} />

        <main className="flex-1 relative px-5 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    )
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-neutral-900">
      <MobileHeader />

      <motion.main
        layout
        className="flex-1 overflow-y-auto p-4"
      >
        <Outlet />
      </motion.main>

      <MobileNavbar />
    </div>
  )
}

export default BaseLayout;