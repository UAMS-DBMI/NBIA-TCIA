//To Test: 
//curl -X POST -d "project=testPROJ&siteName=site&seriesId=1.1&seriesId=2.2&seriesId=3.3&newQcStatus=1" -H "Authorization:Bearer ec6c2120-63ec-4a49-9643-19a7f47de9fc" -k "http://localhost:8080/nbia-api/services/submitQCVisibility"

package gov.nih.nci.nbia.restAPI;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MultivaluedMap;

import gov.nih.nci.ncia.criteria.*;
import gov.nih.nci.nbia.query.DICOMQuery;
import gov.nih.nci.nbia.dynamicsearch.DynamicSearchCriteria;
import gov.nih.nci.nbia.dynamicsearch.Operator;
import gov.nih.nci.nbia.dynamicsearch.QueryHandler;
import gov.nih.nci.nbia.lookup.StudyNumberMap;
import gov.nih.nci.nbia.qctool.VisibilityStatus;
import gov.nih.nci.nbia.search.PatientSearcher;
import gov.nih.nci.nbia.searchresult.PatientSearchResult;
import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.security.*;
import gov.nih.nci.nbia.util.SiteData;
import gov.nih.nci.nbia.restUtil.AuthorizationUtil;
import gov.nih.nci.nbia.restUtil.JSONUtil;
import gov.nih.nci.nbia.restUtil.QAUserUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import gov.nih.nci.nbia.dao.QcStatusDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import gov.nih.nci.nbia.security.NCIASecurityManager;

@Path("/submitSiteForSeries")
public class SetSiteForSeries extends getData{
	public final static String TEXT_CSV = "text/csv";

	@Context private HttpServletRequest httpRequest;
	/**
	 * This method set 
	 *
	 * @return String - set of species names
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)

	public Response constructResponse(@FormParam("seriesId") List<String> seriesIdList,
			@FormParam("site") String site) {
		


		try {	
			
			   Authentication authentication = SecurityContextHolder.getContext()
						.getAuthentication();
				String user = (String) authentication.getPrincipal();
                if (!QAUserUtil.isUserQA(user)) {
                	System.out.println("Not QA User!!!!");
				    NCIASecurityManager sm = (NCIASecurityManager)SpringApplicationContext.getBean("nciaSecurityManager");
				   if (!sm.hasQaRole(user)) {
				  	  return Response.status(401)
							.entity("Insufficiant Privileges").build();
				   } else {
					   QAUserUtil.setUserQA(user);
				   }
                }
				QcStatusDAO qDao = (QcStatusDAO)SpringApplicationContext.getBean("qcStatusDAO");
				qDao.setSiteForSeries(seriesIdList, site);
			    return Response.ok("ok").type("application/text")
						.build();
		    } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
				return Response.status(500)
						.entity("Server was not able to process your request").build();
	}
	private Date getDate(String date) {
		Date returnValue=null;
		
		if (date==null)
		{
			return null;
		}
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		try {
		returnValue=format.parse(date);
		} catch (Exception e) {
			format = new SimpleDateFormat("MM/dd/yyyy");
			try {
				returnValue=format.parse(date);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
		}
		return returnValue;
	}
}
