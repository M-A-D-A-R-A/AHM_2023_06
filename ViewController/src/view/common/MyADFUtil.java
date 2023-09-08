package view.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;

import oracle.jbo.ApplicationModule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import javax.naming.InitialContext;

import javax.servlet.http.HttpSession;

import javax.sql.DataSource;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.UploadedFile;

public class MyADFUtil {
    private static Object resolveExpression(String expression) {
        FacesContext ctx = getFacesContext();
        ELContext elContext = ctx.getELContext();
        ValueExpression ve = ctx.getApplication()
                                .getExpressionFactory()
                                .createValueExpression(elContext, expression, Object.class);
        return ve.getValue(elContext);
    }

    private static Object invokeMethod(String expr, Class returnType, Class[] argTypes, Object[] args) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory = fc.getApplication().getExpressionFactory();
        MethodExpression methodExpr = elFactory.createMethodExpression(elctx, expr, returnType, argTypes);
        return methodExpr.invoke(elctx, args);
    }

    private static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    private static BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    private static ApplicationModule getDefaultApplicationModule() {
        return (ApplicationModule) resolveExpression("#{data.AppModuleDataControl.dataProvider}");
    }

    public static Connection getConnection() {
        try {
            InitialContext initialContext = new InitialContext();
            DataSource ds = (DataSource) initialContext.lookup("jdbc/LoanDS");
            Connection conn = ds.getConnection();
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DBTransaction getDefaultDBTransaction() {
        return (DBTransaction) getDefaultApplicationModule().getTransaction();
    }

    public static DCIteratorBinding getIterator(String iteratorName) {
        DCIteratorBinding iter = (DCIteratorBinding) getBindings().get(iteratorName);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + iteratorName + "' not found");
        }
        return iter;
    }

    public static void executeOperation(String operationName) {
        OperationBinding operationBinding = getBindings().getOperationBinding(operationName);
        operationBinding.execute();
    }

    public static void setAttributeInIterator(String iteratorName, String attributeName, Object value) {
        if (getIterator(iteratorName).getCurrentRow() != null) {
            getIterator(iteratorName).getCurrentRow().setAttribute(attributeName, value);
        }
    }

    public static Object getAttributeFromIterator(String iteratorName, String attributeName) {
        if (getIterator(iteratorName).getCurrentRow() != null) {
            return getIterator(iteratorName).getCurrentRow().getAttribute(attributeName);
        }
        return null;
    }

    public static void putInSessionScope(String key, Object object) {
        try {
            FacesContext ctx = getFacesContext();
            HttpSession session = (HttpSession) ctx.getExternalContext().getSession(true);
            session.setAttribute(key, object);
        } catch (Exception e) {
            System.err.println("storeOnSession -- " + e);
        }
    }

    public static Object getFromSessionScope(String key) {
        try {
            FacesContext ctx = getFacesContext();
            Map sessionState = ctx.getExternalContext().getSessionMap();
            return sessionState.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void putInRequestScope(String name, Object value) {
        getFacesContext().getExternalContext()
                         .getRequestMap()
                         .put(name, value);
    }

    public static Object getFromRequestScope(String name) {
        return getFacesContext().getExternalContext()
                                .getRequestMap()
                                .get(name);
    }

    public static void navigateToPage(String outcome) {
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getApplication()
          .getNavigationHandler()
          .handleNavigation(fc, null, outcome);
    }

    public static void showErrorMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, null, null);
        fm.setDetail(message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    public static void showSuccessfulMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, null, null);
        fm.setDetail(message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    public static void showWarnMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, null, null);
        fm.setDetail(message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    public static void uploadFile(ValueChangeEvent valueChangeEvent, String fileLocation, String fileName) {
        UploadedFile file = (UploadedFile) valueChangeEvent.getNewValue();
        if (fileLocation == null) {
            fileLocation = "c:/";
        }
        InputStream in;
        FileOutputStream out;
        boolean exists = (new File(fileLocation)).exists();
        if (!exists) {
            (new File(fileLocation)).mkdirs();
        }
        if (file != null && file.getLength() > 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message =
                new FacesMessage("File Uploaded  " + file.getFilename() + " (" + file.getLength() + " bytes)");
            context.addMessage(valueChangeEvent.getComponent().getClientId(context), message);
            try {
                out = new FileOutputStream(fileLocation + "" + fileName);
                in = file.getInputStream();
                for (int bytes = 0; bytes < file.getLength(); bytes++) {
                    out.write(in.read());
                }
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String filename = file != null ? file.getFilename() : null;
            String byteLength = file != null ? "" + file.getLength() : "0";
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message =
                new FacesMessage(FacesMessage.SEVERITY_WARN, " " + " " + filename + " (" + byteLength + " bytes)",
                                 null);
            context.addMessage(valueChangeEvent.getComponent().getClientId(context), message);
        }
    }

    public static void downloadFile(java.io.OutputStream outputStream, String fileName) throws IOException {
        try {
            File file = new File(fileName);
            byte[] b = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            outputStream.write(b);
            outputStream.flush();
        } catch (Exception e) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, null, null);
            fm.setDetail("No file found");
            FacesContext.getCurrentInstance().addMessage(null, fm);
        }
    }

    public static String getSqlDescription(String sql) {
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = getDefaultDBTransaction().createPreparedStatement(sql, 1);
            rs = stat.executeQuery();
            while (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /** function take dml sql statement and execute this statement then return number of records affects during the execution */
    public static int executeDML(String sql) {
        PreparedStatement stat = null;
        try {
            stat = getDefaultDBTransaction().createPreparedStatement(sql, 1);
            int result = stat.executeUpdate();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static void makeTableSelectedRowCurrentRow(String exp, SelectionEvent selectionEvent) {
        invokeMethod(exp, null, new Class[] { SelectionEvent.class }, new Object[] { selectionEvent });
    }
}
