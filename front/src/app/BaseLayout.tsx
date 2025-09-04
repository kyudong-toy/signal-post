import {Outlet} from "react-router-dom";
import {Header} from "../widgets/Header.tsx";

export const BaseLayout = () => {
  return (
    <div>
      <Header />
      <main>
        <Outlet />
      </main>
    </div>
  )
}