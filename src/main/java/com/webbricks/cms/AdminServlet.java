package com.webbricks.cms;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.webbricks.exception.WBException;

public class AdminServlet extends HttpServlet {

	public static final String ADMIN_URI_PREFIX = "admin-uri-prefix";
	public static final String ADMIN_RESOURCE_FOLDER = "admin";
	public static final String ADMIN_CONFIG_FOLDER = "config";
	public static final String ADMIN_CONFIG_RESOURCES = "META-INF/config/resourceswhitelist.properties";
	public static final String ADMIN_CONFIG_AJAX = "ajaxwhitelist.properties";
	
	
	private ResourceRequestProcessor resourceRequestProcessor = null;
	private AjaxRequestProcessor ajaxRequestProcssor = null;
	private RequestProcessorFactory processorFactory = null;
	private WBServletUtility servletUtility = null;
	
	private String adminURIPart = "";
	
	public AdminServlet()
	{
		processorFactory = new DefaultRequestProcessorFactory();
		servletUtility = new WBServletUtility();
	}
	
	public void doAjax(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException
    {
		try
		{
			String reqUri = getAdminRelativeUri(req);
			reqUri = decorateAdminRelativeUri(reqUri);
			if (ajaxRequestProcssor.isAjaxRequest(req, reqUri))
			{
				ajaxRequestProcssor.process(req, resp, reqUri);
			} else
			{
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (WBException e)
		{
			throw new ServletException("WBGae - cound not complete request", e); 
		}
		
    }
	public String getAdminRelativeUri(HttpServletRequest req) throws ServletException
	{
		String reqUri = req.getRequestURI();
		if (reqUri.startsWith(adminURIPart))
		{
			reqUri = reqUri.substring(adminURIPart.length());
		} else
		{
			throw new ServletException("Cannot handle requests for uri that start with something different than " + adminURIPart);
		}
		return reqUri;
	}
	
	public String decorateAdminRelativeUri(String uri)
	{
		if (uri.endsWith("/"))
		{
			return uri + "index.html";
		} else
		if (uri.length() == 0)
		{
				return "/index.html";
		}
		return uri;
	}
	
	public void init() throws ServletException
    {
		if ((adminURIPart == null) || (adminURIPart.length() == 0))
		{
			try
			{
				
			String strTemp =  servletUtility.getInitParameter(ADMIN_URI_PREFIX, this);
			
			if (null != strTemp && strTemp.length()>0)
			{
				adminURIPart = strTemp;
			} else
			{
				throw new ServletException("There is no admin-prefix-uri parameter for the admin servlet configuration.");
			}
			} catch (Exception e)
			{
				throw new ServletException(e);
			}
		}
		try
		{
			resourceRequestProcessor = processorFactory.createResourceRequestProcessor();
			resourceRequestProcessor.initialize(ADMIN_RESOURCE_FOLDER, ADMIN_CONFIG_RESOURCES);
	
			ajaxRequestProcssor = processorFactory.createAjaxRequestProcessor();
			ajaxRequestProcssor.initialize(ADMIN_CONFIG_FOLDER, ADMIN_CONFIG_AJAX);
			ajaxRequestProcssor.setAdminUriPart(adminURIPart);
		} catch (WBException e)
		{
			throw new ServletException("WB Servlet initialization", e);
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
     throws ServletException,
            java.io.IOException
            {
				try
				{
					String reqUri = getAdminRelativeUri(req);
					reqUri = decorateAdminRelativeUri(reqUri);
					if (resourceRequestProcessor.isResourceRequest(reqUri))
					{
						resourceRequestProcessor.process(req, resp, reqUri);
						return;
					} else
					if (ajaxRequestProcssor.isAjaxRequest(req, reqUri))
					{
						ajaxRequestProcssor.process(req, resp, reqUri);
					} else
					{
						resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					}
				} catch (WBException e)
				{
					// to do - go to error handling
				}
			}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException,
           java.io.IOException
           {
				doAjax(req, resp);
           }

	public void doPut(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException,
           java.io.IOException
           {
				doAjax(req, resp);
           }

	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException,
           java.io.IOException
           {
				doAjax(req, resp);
           }

	public ResourceRequestProcessor getResourceRequestProcessor() {
		return resourceRequestProcessor;
	}

	public void setResourceRequestProcessor(
			ResourceRequestProcessor resourceRequestProcessor) {
		this.resourceRequestProcessor = resourceRequestProcessor;
	}

	public String getAdminURIPart() {
		return adminURIPart;
	}

	public void setAdminURIPart(String adminURIPart) {
		this.adminURIPart = adminURIPart;
	}

	public AjaxRequestProcessor getAjaxRequestProcssor() {
		return ajaxRequestProcssor;
	}

	public void setAjaxRequestProcssor(AjaxRequestProcessor ajaxRequestProcssor) {
		this.ajaxRequestProcssor = ajaxRequestProcssor;
	}

	public RequestProcessorFactory getProcessorFactory() {
		return processorFactory;
	}

	public void setProcessorFactory(RequestProcessorFactory processorFactory) {
		this.processorFactory = processorFactory;
	}

	public WBServletUtility getServletUtility() {
		return servletUtility;
	}

	public void setServletUtility(WBServletUtility servletUtility) {
		this.servletUtility = servletUtility;
	}

	
	
	
	
}
