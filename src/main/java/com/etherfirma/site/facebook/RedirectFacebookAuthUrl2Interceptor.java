package com.etherfirma.site.facebook;

import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.apache.log4j.*;

import com.etherfirma.util.settings.*;
import com.tessera.dispatch.*;
import com.tessera.intercept.*;
import com.weaselworks.util.*;

/**
 * 
 * @author crawford
 *
 */

public class RedirectFacebookAuthUrl2Interceptor 
	extends InterceptorSupport
{
	private static final Logger logger = Logger.getLogger(RedirectFacebookAuthUrl2Interceptor.class);
	
	public
	RedirectFacebookAuthUrl2Interceptor(Map<String,String> props) 
	{
		super(props);
		return; 
	}
	
	@Override
	public 
	Alteration intercept (HttpServletRequest req, HttpServletResponse res, DispatchContext dc) 
		throws Exception
	{
//		OAuthFacebookRedirect2Interceptor.logRequest (logger, req); 		
		
		// Read the settings and get the base redirect url 
		
		final Settings settings = SettingsUtil.getSettings (req);
		final String permissions = settings.lookup ("facebook.permissions", String.class, true); 
		final String appId = settings.lookup ("facebook.appId", String.class, true); 
		String oauthUrl = req.getScheme () + "://" + settings.lookup ("facebook.oauthRedirect", String.class, true);
			
		// Extract the GET parameters from the request
		
		final String queryString = req.getQueryString (); 
		boolean first = true; 
		
		if (! StringUtil.isEmpty (queryString)) { 
			final List<String> keys = new ArrayList<String> (); 
			for (final String pair : queryString.split ("&")) { 
				keys.add (pair.split ("=")[0]); 
			}
			Collections.sort (keys); 
			for (final String key : keys) {
				if (first) { 
					oauthUrl += '?'; 
					first = false ;
				} else { 
					oauthUrl += '&'; 
				}
				oauthUrl += key + '=' + URLEncoder.encode (req.getParameter (key), "UTF-8"); 
			}
		}
		
		// Put together the URL to redirect the end-user to 
        String url = "https://www.facebook.com/dialog/oauth?" + 
		    "client_id=" + appId + "&" + 
		    "redirect_uri=" + URLEncoder.encode (oauthUrl, "UTF-8") + "&" +
		    "scope=" + permissions;
				
		// Output a page that will redirect them to the specified page. 
		
		final StringBuffer html = new StringBuffer();
		html.append("<script type=\"text/javascript\">");
		html.append("var frame = self;");
		html.append("while (frame != parent)");
		html.append("  frame = parent;");
		html.append("frame.location = \"" + url + "\";");
		html.append("</script>");
		
		res.setContentType ("text/html");
		res.setContentLength (html.length());
		res.getOutputStream().write(html.toString().getBytes());
		
		return ABORT;
	}
}

// EOF
