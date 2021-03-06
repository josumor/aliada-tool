// ALIADA - Automatic publication under Linked Data paradigm
//          of library and museum data
//
// Component: aliada-user-interface
// Responsible: ALIADA Consortium

package eu.aliada.gui.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

import eu.aliada.gui.log.MessageCatalog;
import eu.aliada.gui.rdbms.DBConnectionManager;
import eu.aliada.shared.log.Log;

/**@author xabi
 * @version $Revision: 1.1 $, $Date: 2015/03/23 15:20:54 $
 * @since 1.0 */
public class InstitutionAction extends ActionSupport {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String organisationName;
    private File organisationLogo;
    private String organisationCatalogUrl; 
    
    private final Log logger = new Log(InstitutionAction.class);
    private ResourceBundle defaults = ResourceBundle.getBundle("defaultValues", getLocale());
    
    /** Show the institution.
     * @return String */
    public String showInstitution() {
    	
    	ServletActionContext.getRequest().getSession().setAttribute("action", defaults.getString("lang.showInstitution"));
    	
    	String userName = (String) ServletActionContext.getRequest().getSession().getAttribute("logedUser");
    	
        try {
            Connection connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM aliada.organisation o INNER JOIN aliada.user u ON o.organisationId = "
            		+ "u.organisationId WHERE u.user_name='" + userName + "';");
            if (rs.next() && rs.getString("org_name") != null) {
                setOrganisationName(rs.getString("org_name"));
                setOrganisationCatalogUrl(rs.getString("org_catalog_url"));
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION, e);
            return ERROR;
        }
        return SUCCESS;
    }
    
	/** Edit the institution.
     * @return String */
    public String editInstitution() {
    	
    	ServletActionContext.getRequest().getSession().setAttribute("action", defaults.getString("lang.default"));
    	
    	String user = (String) ServletActionContext.getRequest().getSession().getAttribute("logedUser");
        Connection connection;
        FileInputStream fis = null;
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
        	if (this.organisationCatalogUrl.trim().isEmpty()) {
        		ResultSet rs = statement.executeQuery("select org_catalog_url from aliada.organisation o "
        				+ "INNER JOIN aliada.user u on o.organisationId=u.organisationId where u.user_name='" + user + "';");
        		if (rs.next()) {
        			setOrganisationCatalogUrl(rs.getString("org_catalog_url"));
        		}
        		statement.close();
        		connection.close();
        		addActionError(getText("catalogue.required"));
        		return ERROR;
        	}
        	PreparedStatement preparedStatement;
            if (this.organisationLogo != null) {
            	preparedStatement = connection.prepareStatement("UPDATE aliada.organisation  SET org_logo = ?, org_catalog_url =? WHERE org_name = ?");
                fis = new FileInputStream(this.organisationLogo);
                preparedStatement.setBinaryStream(1, fis, (int) this.organisationLogo.length());
                preparedStatement.setString(2, this.organisationCatalogUrl);
                preparedStatement.setString(3, this.organisationName);
            } else {
                preparedStatement = connection.prepareStatement("UPDATE aliada.organisation  SET org_catalog_url =? WHERE org_name = ?");
                preparedStatement.setString(1, this.organisationCatalogUrl);
                preparedStatement.setString(2, this.organisationName);
            }
            preparedStatement.executeUpdate();
            addActionMessage(getText("institution.changed"));
            preparedStatement.close();
            connection.close();
            return SUCCESS;
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION, e);
            return ERROR;
        } catch (FileNotFoundException e) {
            logger.error(MessageCatalog._00013_FILE_NOT_FOUND_EXCEPTION, e);
            return ERROR;
        }
    }
    /** File Upload incorrect.
     * @return String */
    public String fileUploadFormat() {
    	clearErrorsAndMessages();
    	addActionError(getText("institution.error"));
    	return showInstitution();
    }
    
    /** @return Returns the organisationName. */
    public String getOrganisationName() {
        return organisationName;
    }
    /** @param organisationName The organisationName to set. */
    public void setOrganisationName(final String organisationName) {
        this.organisationName = organisationName;
    }
    /** @return Returns the organisationLogo. */
    public File getOrganisationLogo() {
        return organisationLogo;
    }
    /** @param organisationLogo The organisationLogo to set. */
    public void setOrganisationLogo(final File organisationLogo) {
        this.organisationLogo = organisationLogo;
    }
    /** @return Returns the organisationCatalogUrl. */
    public String getOrganisationCatalogUrl() {
        return organisationCatalogUrl;
    }
    /** @param organisationCatalogUrl The organisationCatalogUrl to set. */
    public void setOrganisationCatalogUrl(final String organisationCatalogUrl) {
        this.organisationCatalogUrl = organisationCatalogUrl;
    }

}
