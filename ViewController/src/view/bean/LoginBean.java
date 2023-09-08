package view.bean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;

import oracle.adf.view.rich.component.rich.input.RichInputText;

import view.common.MyADFUtil;

public class LoginBean
{
  private RichInputText usernameInputText;
  private RichInputText passwordInputText;

  public LoginBean()
  {
  }

  public void setUsernameInputText(RichInputText usernameInputText)
  {
    this.usernameInputText = usernameInputText;
  }

  public RichInputText getUsernameInputText()
  {
    return usernameInputText;
  }

  public void setPasswordInputText(RichInputText passwordInputText)
  {
    this.passwordInputText = passwordInputText;
  }

  public RichInputText getPasswordInputText()
  {
    return passwordInputText;
  }

  public String loginAction()
  {
    String username = usernameInputText.getValue().toString();
    String password = passwordInputText.getValue().toString();
    
    System.out.println("username: " + username + " - password: " + password);
    
    PreparedStatement stat = null;
    ResultSet rs = null;
    Connection conn = null;
    try
    {
      String sql = "select * from login_details where Username='" + username + "' and password='" + password + "'";
      conn = MyADFUtil.getConnection();
      stat = conn.prepareStatement(sql);
      rs = stat.executeQuery();
      while(rs.next())
      {
        MyADFUtil.putInSessionScope("username", username);
          String role = rs.getString(3);
          System.out.println("username: " + username + " - password: " + password+ " Role -"+ role);
        MyADFUtil.putInSessionScope("Role", rs.getString(3));
        return "dashboard";
      }
    }
    catch (Exception e)
    {
      // TODO: Add catch code
      e.printStackTrace();
    }
    MyADFUtil.showErrorMessage("Invalid username or password");
    return null;
  }

  public String logoutAction()
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
      if (session != null)
      {
        session.invalidate();
      }
    }
    catch (Exception e)
    {
      System.err.println("error in logout -- " + e);
    }
    return "index";
  }
}
