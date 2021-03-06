package com.webbricks.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.webbricks.cms.LocaleManager;
import com.webbricks.cmsdata.WBArticle;
import com.webbricks.cmsdata.WBProject;
import com.webbricks.datautility.AdminDataStorage;
import com.webbricks.datautility.GaeAdminDataStorage;
import com.webbricks.datautility.WBJSONToFromObjectConverter;
import com.webbricks.exception.WBException;
import com.webbricks.exception.WBIOException;
import com.webbricks.utility.HttpServletToolbox;

public class WBLanguagesController extends WBController {

	private LocaleManager localeManager;
	private WBJSONToFromObjectConverter jsonObjectConverter;
	private HttpServletToolbox httpServletToolbox;
	private AdminDataStorage adminStorage;
	private ArrayList<String> sortedLanguages;
	private Map<String, Locale> allLocales;

	private WBProject getProject() throws WBIOException
	{
		WBProject project = adminStorage.get(WBProject.PROJECT_KEY, WBProject.class);
		if (null == project)
		{
			project = new WBProject();
			project.setKey("wbprojectid");
			project.setDefaultLanguage("en");
			project.setSupportedLanguages("en");
			project = adminStorage.add(project);
		}
		return project;
	}
	
	public WBLanguagesController()
	{
		httpServletToolbox = new HttpServletToolbox();
		jsonObjectConverter = new WBJSONToFromObjectConverter();
		adminStorage = new GaeAdminDataStorage();
		localeManager = LocaleManager.getInstance();
		sortedLanguages = new ArrayList<String>();
		
		allLocales = localeManager.getSupportedLanguagesAndCountries();
		Set<String> keyset = allLocales.keySet();
		sortedLanguages.addAll(keyset);
		Collections.sort(sortedLanguages);
		
	}

	public void getAllLanguages(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		JSONArray result = new JSONArray();		
		try
		{
			for(String str: sortedLanguages)
			{
				JSONObject item = new JSONObject();
				item.put("lcid", str);
				item.put("name", allLocales.get(str).getDisplayName());
				result.put(item);
			}
			httpServletToolbox.writeBodyResponseAsJson(response, result.toString(), null);	
		} catch (Exception e)
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("WBErrors.WB_CANT_GET_RECORDS", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, "{}", errors);			
		}
		
	}
	
	public void getSupportedLanguages(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		WBProject project = getProject();
		String supportedLanguages = project.getSupportedLanguages();
		StringTokenizer stk = new StringTokenizer(supportedLanguages, ",");
		Set<String> supportedlanguages = new HashSet<String>();
		while (stk.hasMoreElements())
		{
			String language = stk.nextToken();
			if (allLocales.containsKey(language))
			{
				supportedlanguages.add(language);
			}			
		}

		JSONArray result = new JSONArray();		
		try
		{
			for(String str: sortedLanguages)
			{
				if (supportedlanguages.contains(str))
				{
					JSONObject item = new JSONObject();
					item.put("lcid", str);
					item.put("name", allLocales.get(str).getDisplayName());
					if (str.compareTo(project.getDefaultLanguage()) == 0)
					{
						item.put("default", "true");
					} else
					{
						item.put("default", "false");
					}
					result.put(item);
				}
			}
			httpServletToolbox.writeBodyResponseAsJson(response, result.toString(), null);	
		} catch (Exception e)
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("WBErrors.WB_CANT_GET_RECORDS", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, "{}", errors);			
		}
	}
	
	public void setSupportedLanguages(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			String requestBody = httpServletToolbox.getBodyText(request);
			JSONArray array = new JSONArray(requestBody);
			Set<String> inputLanguages = new HashSet<String>();
			String defaultLanguage = "";
			Map<String, String> errors = new HashMap<String, String>();					
			for(int i=0; i< array.length(); i++)
			{
				JSONObject aJson = array.getJSONObject(i);
				String aLanguage = aJson.getString("lcid");
				if (inputLanguages.contains(aLanguage))
				{
					errors.put(WBErrors.WB_TWO_IDENTICAL_LANGUAGES, WBErrors.WB_TWO_IDENTICAL_LANGUAGES);
				}
				if (!allLocales.containsKey(aLanguage))
				{
					errors.put(WBErrors.WB_INVALID_LANGUAGE, WBErrors.WB_INVALID_LANGUAGE);
				}
				
				String def = aJson.getString("default");
				if (def.equals("true"))
				{
					if (defaultLanguage.length()>0)
					{
						errors.put(WBErrors.WB_TWO_DEFAULT_LANGUAGES, WBErrors.WB_TWO_DEFAULT_LANGUAGES);
					}
					defaultLanguage = aLanguage;
				} 	
				inputLanguages.add(aLanguage);
			}
			if (defaultLanguage.length() == 0)
			{
				errors.put(WBErrors.WB_NO_DEFAULT_LANGUAGES, WBErrors.WB_NO_DEFAULT_LANGUAGES);				
			}
			if (errors.size() > 0)
			{
				httpServletToolbox.writeBodyResponseAsJson(response, "", errors);						
				return;
			}
			String selections = "";
			for(String str: inputLanguages)
			{
				if (selections.length()>0)
				{
					selections += ",";
				}
				selections += str;
			}
			
			WBProject project = getProject();
			project.setDefaultLanguage(defaultLanguage);
			project.setSupportedLanguages(selections);
			project.setLastModified(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());			
			adminStorage.update(project);
			httpServletToolbox.writeBodyResponseAsJson(response, "{}", errors);
			
		} catch (Exception e)
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put(WBErrors.WB_CANT_UPDATE_RECORD, WBErrors.WB_CANT_UPDATE_RECORD);
			httpServletToolbox.writeBodyResponseAsJson(response, "{}", errors);						
		}
		
	}
	
	
}
