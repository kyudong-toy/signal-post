import {Link as RouterLink, type LinkProps} from "react-router-dom";

type Props = LinkProps;

export const Link = ({children, ...props}: Props) => {
  return <RouterLink {...props}>{children}</RouterLink>;
};