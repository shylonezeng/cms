package com.webbricks.cms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webbricks.cache.DefaultWBCacheFactory;
import com.webbricks.cache.WBCacheFactory;
import com.webbricks.cache.WBCacheInstances;
import com.webbricks.cache.WBParameterCache;
import com.webbricks.cache.WBProjectCache;
import com.webbricks.cache.WBUriCache;
import com.webbricks.cache.WBWebPageCache;
import com.webbricks.cmsdata.WBParameter;
import com.webbricks.cmsdata.WBProject;
import com.webbricks.cmsdata.WBWebPage;
import com.webbricks.exception.WBException;
import com.webbricks.exception.WBIOException;
import com.webbricks.exception.WBLocaleException;
import com.webbricks.exception.WBSetKeyException;
import com.webbricks.template.WBFreeMarkerModuleDirective;

public class PublicContentServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(PublicContentServlet.class.getName());
	
	private WBServletUtility servletUtility = null;
	
	// this is the common uri part that will be common to all requests served by this CMS
	// corresponds to uri-prefix init parameter.
	
	private String uriCommonPrefix = ""; 
	
	private URLMatcher urlMatcher;
	private PageContentBuilder pageContentBuilder;
	private WBCacheInstances cacheInstances;
	
	public PublicContentServlet()
	{
		setServletUtility(new WBServletUtility());
	}
	
	public void init(ServletConfig config) throws ServletException
    {
		super.init(config);
		String initUriPrefix = servletUtility.getInitParameter("uri-prefix", this);
		if (initUriPrefix.length() > 0)
		{
			if (initUriPrefix.endsWith("/"))
			{
				initUriPrefix = initUriPrefix.substring(0, initUriPrefix.length()-1);
			}
			uriCommonPrefix = initUriPrefix;
		}
		
		try
		{
			WBCacheFactory wbCacheFactory = new DefaultWBCacheFactory();
			this.cacheInstances = new WBCacheInstances(wbCacheFactory.createWBUriCacheInstance(), 
					wbCacheFactory.createWBWebPageCacheInstance(), 
					wbCacheFactory.createWBWebPageModuleCacheInstance(), 
					wbCacheFactory.createWBParameterCacheInstance(),
					wbCacheFactory.createWBImageCacheInstance(),
					wbCacheFactory.createWBArticleCacheInstance(),
					wbCacheFactory.createWBMessageCacheInstance(),
					wbCacheFactory.createWBProjectCacheInstance());

			Set<String> allUris = cacheInstances.getWBUriCache().getAllUris();
			this.urlMatcher = new URLMatcher();
			this.urlMatcher.initialize(allUris, cacheInstances.getWBUriCache().getCacheFingerPrint());
						
			pageContentBuilder = new PageContentBuilder(cacheInstances);
			pageContentBuilder.initialize();
			
		} catch (Exception e)
		{
			log.log(Level.SEVERE, "ERROR: ", e);
			throw new ServletException(e);
		}
    }
	
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
    	throws ServletException,
    	java.io.IOException
    {
		String uri = req.getRequestURI();
		if (uriCommonPrefix.length()>0 && uri.startsWith(uriCommonPrefix))
		{
			uri = uri.substring(uriCommonPrefix.length());
		}
		//reinitialize the matchurlToPattern if needed
		if (cacheInstances.getWBUriCache().getCacheFingerPrint().compareTo(urlMatcher.getFingerPrint())!= 0)
		{
			try
			{
				Set<String> allUris = cacheInstances.getWBUriCache().getAllUris();
				urlMatcher.initialize(allUris, cacheInstances.getWBUriCache().getCacheFingerPrint());
			} catch (WBIOException e)
			{
				log.log(Level.SEVERE, "Could not reinitialize the URL matcher ", e);
				// do not fail as some urls may still work
			}
		}
		URLMatcherResult urlMatcherResult = urlMatcher.matchUrlToPattern(uri);
		if (urlMatcherResult == null)
		{
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else
		{
			try
			{
				WBWebPage webPage = pageContentBuilder.findWebPage(urlMatcherResult);
				if (webPage == null)
				{
					resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				WBProject wbProject = cacheInstances.getProjectCache().getProject();
				String content = pageContentBuilder.buildPageContent(req, urlMatcherResult, webPage, wbProject);
				uri = uri.toLowerCase();
				if (uri.endsWith(".js"))
				{
					resp.setContentType("application/javascript");
					resp.setCharacterEncoding("UTF-8");
				} else
				if (uri.endsWith(".css")){
					resp.setContentType("text/css");
					resp.setCharacterEncoding("UTF-8");
				} else if (uri.endsWith(".zip")){
					resp.setContentType("application/zip");
				}else
				{
					resp.setCharacterEncoding("UTF-8");
					resp.setContentType("text/html");
				}
				ServletOutputStream os = resp.getOutputStream();
				os.write(content.getBytes("UTF-8"));
				os.flush();
			} 
			catch (WBLocaleException e)
			{
				log.log(Level.SEVERE, "ERROR: ", e);
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;				
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "ERROR: ", e);
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}
    }
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
     throws ServletException,
            java.io.IOException
            {
			handleRequest(req, resp);
			}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException,
           java.io.IOException
           {
			handleRequest(req, resp);
           }

	public void doPut(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException,
           java.io.IOException
           {
			handleRequest(req, resp);
           }

	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException,
           java.io.IOException
           {
			handleRequest(req, resp);
           }

	public WBServletUtility getServletUtility() {
		return servletUtility;
	}

	public void setServletUtility(WBServletUtility servletUtility) {
		this.servletUtility = servletUtility;
	}
	
	

}
