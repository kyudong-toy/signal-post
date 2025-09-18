import type { ReactNode } from 'react';
import { useEffect} from 'react';
import { useNavigate } from 'react-router-dom';
import { useMediaQuery } from '@/shared/lib/useMediaQuery';

interface MobileOnlyRouteProps {
  children: ReactNode;
}

const MobileRouteCover = ({ children }: MobileOnlyRouteProps) => {
  const isDesktop = useMediaQuery('(min-width: 1024px)');
  const navigate = useNavigate();

  useEffect(() => {
    if (isDesktop) {
      console.log("데스크탑 뷰에서는 접근할 수 없는 페이지입니다.");
      navigate('/', { replace: true });
    }
  }, [isDesktop, navigate]);

  if (isDesktop) {
    return null;
  }

  return <>{children}</>;
};

export default MobileRouteCover;