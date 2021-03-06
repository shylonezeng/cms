package com.webbricks.controllers;

import java.util.Calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.webbricks.cache.DefaultWBCacheFactory;
import com.webbricks.cache.WBCacheFactory;
import com.webbricks.cache.WBImageCache;
import com.webbricks.cmsdata.WBImage;
import com.webbricks.cmsdata.WBWebPage;
import com.webbricks.cmsdata.WBWebPageModule;
import com.webbricks.datautility.AdminDataStorage;
import com.webbricks.datautility.AdminDataStorageListener;
import com.webbricks.datautility.GaeAdminDataStorage;
import com.webbricks.datautility.WBBlobHandler;
import com.webbricks.datautility.WBGaeBlobHandler;
import com.webbricks.datautility.WBJSONToFromObjectConverter;
import com.webbricks.datautility.AdminDataStorageListener.AdminDataStorageOperation;
import com.webbricks.exception.WBException;
import com.webbricks.exception.WBIOException;
import com.webbricks.utility.HttpServletToolbox;

public class WBImageController extends WBController implements AdminDataStorageListener<WBImage> {

	private static final String UPLOAD_RETURN_URL = "/wbimageupload"; 
	private HttpServletToolbox httpServletToolbox;
	private WBJSONToFromObjectConverter jsonObjectConverter;
	private AdminDataStorage adminStorage;
	private WBBlobHandler blobHandler;
	private WBImageValidator validator;
	private WBImageCache imageCache;
	
	public WBImageController()
	{
		httpServletToolbox = new HttpServletToolbox();
		jsonObjectConverter = new WBJSONToFromObjectConverter();
		adminStorage = new GaeAdminDataStorage();
		blobHandler = new WBGaeBlobHandler();
		validator = new WBImageValidator();
		
		WBCacheFactory wbCacheFactory = new DefaultWBCacheFactory();
		imageCache = wbCacheFactory.createWBImageCacheInstance();
		
		adminStorage.addStorageListener(this);
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

	public void serveImage(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		int size = 0;
		String blobKey = request.getParameter("blobKey");
		String sizeStr = request.getParameter("size");
		if (sizeStr != null && sizeStr.length() > 0)
		{
			try
			{
				size = Integer.valueOf(sizeStr);
			} catch (NumberFormatException e)
			{
				size = 0;
			}			
		}
		String url = blobHandler.serveBlobUrl(blobKey, size);
		response.addHeader("Location", url);
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		
	}
	public void serveImageUrl(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		int size = 0;
		String blobKey = request.getParameter("blobKey");
		String sizeStr = request.getParameter("size");
		if (sizeStr != null && sizeStr.length() > 0)
		{
			try
			{
				size = Integer.valueOf(sizeStr);
			} catch (NumberFormatException e)
			{
				size = 0;
			}			
		}
		String url = blobHandler.serveBlobUrl(blobKey, size);
		try
		{	
			JSONObject obj = new JSONObject();
			obj.put("url", url);
			httpServletToolbox.writeBodyResponseAsJson(response, obj.toString(), null);		
		} catch (Exception e)
		{
			throw new WBIOException(e.getMessage());
		}
		
	}
	
	public void uploadSubmit(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			WBImage image = null;
			String oldBlobKey = null;
			String imgKey = request.getParameter("key");
			if (imgKey != null && imgKey.length()> 0)
			{
				try {
				Long key = Long.valueOf(imgKey);
				image = adminStorage.get(key, WBImage.class);
				
				// store old blob key 
				oldBlobKey = image.getBlobKey();
				
				} catch (NumberFormatException e)
				{
					throw new WBIOException("cannot get Long from key " + imgKey);
				}				
			} else
			{
				image = new WBImage();
				image.setExternalKey(adminStorage.getUniqueId());
				if (request.getParameter("name") != null)
				{
					image.setName(request.getParameter("name"));
				}
				if (request.getParameter("enabled") != null)
				{
					image.setEnabled(request.getParameter("enabled").equals("1") ? 1 : 0);
				}				
			}
			
			String blobKey = blobHandler.storeBlob(request);
			if (blobKey != null)
			{
				image.setBlobKey(blobKey);
				image.setLastModified(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());				
				WBImage storedImage = adminStorage.add(image);
				String referer = request.getHeader("Referer");
				
				if (oldBlobKey != null)
				{
					blobHandler.deleteBlob(oldBlobKey);
				}
				
				if (referer!= null)
				{
					response.addHeader("Location", referer);
					response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				}
			}
		} catch (Exception e)
		{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public void get(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Long key = Long.valueOf((String)request.getAttribute("key"));
			WBImage wbimage = adminStorage.get(key, WBImage.class);
			String jsonReturn = jsonObjectConverter.JSONStringFromObject(wbimage, null);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonReturn, null);
			
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, "", errors);			
		}		
	}
	
	public void getAll(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			List<WBImage> images = null;
			images = adminStorage.getAllRecords(WBImage.class);
			
			String jsonReturn = jsonObjectConverter.JSONStringFromListObjects(images);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonReturn, null);
			
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, "", errors);			
		}
	}

	public void delete(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Long key = Long.valueOf((String)request.getAttribute("key"));
			WBImage tempImage = adminStorage.get(key, WBImage.class);
			if (tempImage.getBlobKey() != null)
			{
				blobHandler.deleteBlob(tempImage.getBlobKey());
			}
			
			adminStorage.delete(key, WBImage.class);
			
			WBImage param = new WBImage();
			param.setKey(key);
			String jsonReturn = jsonObjectConverter.JSONStringFromObject(param, null);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonReturn, null);
			
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_DELETE_RECORD);
			httpServletToolbox.writeBodyResponseAsJson(response, "", errors);			
		}		
	}

	public void notify (WBImage t, AdminDataStorageOperation o)
	{
		try
		{
			imageCache.Refresh();
		} catch (WBIOException e)
		{
			// TBD
		}
	}

	public void update(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Long key = Long.valueOf((String)request.getAttribute("key"));
			String jsonRequest = httpServletToolbox.getBodyText(request);
			WBImage wbimage = (WBImage)jsonObjectConverter.objectFromJSONString(jsonRequest, WBImage.class);
			wbimage.setKey(key);
			Map<String, String> errors = validator.validateUpdate(wbimage);
			
			if (errors.size()>0)
			{
				httpServletToolbox.writeBodyResponseAsJson(response, "", errors);
				return;
			}
			WBImage existingImage = adminStorage.get(key, WBImage.class);
			existingImage.setLastModified(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
			existingImage.setName(wbimage.getName());
			WBImage newImage = adminStorage.update(existingImage);
			
			String jsonReturn = jsonObjectConverter.JSONStringFromObject(newImage, null);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonReturn.toString(), errors);
	
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_UPDATE_RECORD);
			httpServletToolbox.writeBodyResponseAsJson(response, "", errors);			
		}				
	}


}
