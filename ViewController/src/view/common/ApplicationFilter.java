package view.common;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ApplicationFilter implements Filter
{
  private FilterConfig _filterConfig = null;

  public void init(FilterConfig filterConfig)
    throws ServletException
  {
    _filterConfig = filterConfig;
  }

  public void destroy()
  {
    _filterConfig = null;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    String uri = req.getRequestURI();

    if (!uri.toLowerCase().endsWith("index.jspx") && !uri.toLowerCase().endsWith("login.jsf"))
    {
      HttpSession session = req.getSession(false);
      if (session == null)
      {
        res.sendRedirect(req.getContextPath() + "/faces/index.jspx");
        return;
      }
      else
      {
        String user = (String) session.getAttribute("username");
        if (user == null)
        {
          res.sendRedirect(req.getContextPath() + "/faces/index.jspx");
          return;
        }
      }
    }
    chain.doFilter(request, response);
  }
}
