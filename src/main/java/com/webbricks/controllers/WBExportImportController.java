package com.webbricks.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.webbricks.cmsdata.WBProject;
import com.webbricks.datautility.AdminDataStorage;
import com.webbricks.datautility.DataStoreImporterExporter;
import com.webbricks.datautility.GaeAdminDataStorage;
import com.webbricks.datautility.WBBlobHandler;
import com.webbricks.datautility.WBGaeBlobHandler;
import com.webbricks.exception.WBException;
import com.webbricks.exception.WBIOException;
import com.webbricks.utility.HttpServletToolbox;

public class WBExportImportController extends WBController {
	private static final String UPLOAD_RETURN_URL = "/wbimportupload";
	
	DataStoreImporterExporter importerExporter;
	AdminDataStorage adminStorage;
	WBBlobHandler blobHandler;
	HttpServletToolbox httpServletToolbox;
	
	public WBExportImportController()
	{
		importerExporter = new DataStoreImporterExporter();
		adminStorage = new GaeAdminDataStorage();
		httpServletToolbox = new HttpServletToolbox();
		blobHandler = new WBGaeBlobHandler();

	}

	public void upload(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{	
			JSONObject obj = new JSONObject();
			obj.put("url", blobHandler.getUploadUrl(getAdminUriPart() + UPLOAD_RETURN_URL));
			httpServletToolbox.writeBodyResponseAsJson(response, obj.toString(), null);		
		} catch (Exception e)
		{
			throw new WBIOException(e.getMessage());
		}
	}

	public void exportContent(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		
		try
		{
			response.setContentType("application/zip");
			importerExporter.exportToZip(adminStorage, response.getOutputStream());
			
		} catch (IOException e)
		{
			throw new WBException(e.getMessage());
		}
	}
			
	public void importUpload(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		String blobKey = blobHandler.storeBlob(request);
		InputStream is = blobHandler.getBlobData(blobKey);
		importerExporter.importfromZip(adminStorage, is);
		
		String referer = request.getHeader("Referer");
		if (blobKey != null)
		{
			blobHandler.deleteBlob(blobKey);
		}
		if (referer!= null)
		{
			response.addHeader("Location", referer);
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		}

	}
	
}
