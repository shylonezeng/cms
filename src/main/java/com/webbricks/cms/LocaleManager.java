package com.webbricks.cms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.webbricks.exception.WBFileNotFoundException;
import com.webbricks.exception.WBIOException;
import com.webbricks.exception.WBReadConfigException;

import java.util.Locale;

public class LocaleManager {
	protected static final String LANGUAGES_CONFIG_FILE = "META-INF/config/langs.csv"; 
	protected Map<String, Locale> langToLocales;
	protected Map<String, Locale> langAndCountriesToLocales;
	public static LocaleManager getInstance() {
		LocaleManager localeManager = new LocaleManager();
		try {
		localeManager.loadLocalesfromFile(LANGUAGES_CONFIG_FILE);
		} catch (WBIOException e)
		{
			return null;
		}
		return localeManager;
		
	}
	public Map<String, Locale> getSupportedLanguages()
	{
		Map<String, Locale> retValue = new HashMap<String, Locale>();
		retValue.putAll(langToLocales);
		return retValue;
	}
	
	public Map<String, Locale> getSupportedLanguagesAndCountries()
	{
		Map<String, Locale> retValue = new HashMap<String, Locale>();
		retValue.putAll(langAndCountriesToLocales);
		return retValue;
	}
	
	
	public void loadLocalesfromFile(String filePath) throws WBIOException
	{
		langToLocales = new HashMap<String, Locale>();
		langAndCountriesToLocales = new HashMap<String, Locale>();

		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(filePath);
			if (null == is)
			{
				throw new WBFileNotFoundException("Could not locate:" + filePath);
			}
			BufferedReader breader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = breader.readLine()) != null)
			{
				if (line.indexOf('*') == -1)
				{
					continue;
				}
				StringTokenizer stk = new StringTokenizer(line, "*");
				if (stk.hasMoreTokens())
				{
					String display = stk.nextToken().trim();
					if (stk.hasMoreTokens())
					{
						String language = stk.nextToken().trim();
						String country = "";
						if (stk.hasMoreElements())
						{
							country = stk.nextToken().trim();
						}
						if (display.length() >0 && language.length()> 0 && country.length() == 0)
						{
							String key = language.toLowerCase();
							langToLocales.put(key, new Locale(language.toLowerCase()));
							langAndCountriesToLocales.put(key, new Locale(language.toLowerCase()));
						} else if (display.length() >0 && language.length()> 0 && country.length() > 0)
						{
							String key = language.toLowerCase() + "_" + country.toUpperCase();
							langAndCountriesToLocales.put(key, new Locale(language.toLowerCase(), country.toUpperCase()));
						}					
					}
				}
			}
			breader.close();
	        is.close();

		} catch (IOException e)
		{
			throw new WBReadConfigException("Coult not read config file:" + filePath, e);
		}
	
	}
}
