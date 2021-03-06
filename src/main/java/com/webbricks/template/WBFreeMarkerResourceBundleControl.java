package com.webbricks.template;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.webbricks.cache.WBMessageCache;

public class WBFreeMarkerResourceBundleControl extends ResourceBundle.Control {

	protected WBMessageCache messageCache;
	
	WBFreeMarkerResourceBundleControl(WBMessageCache messageCache)
	{
		this.messageCache = messageCache;
	}
	
    public List<String> getFormats(String baseName) {
        if (baseName == null)
            throw new NullPointerException();
        return FORMAT_PROPERTIES;
    }

	public ResourceBundle newBundle(String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload) throws IllegalAccessException, InstantiationException, IOException 
    {
		return new WBResourceBundle(messageCache, locale);
	}
	
	public long getTimeToLive(String baseName,
            Locale locale)
	{
		return 0;
	}
	public boolean needsReload(String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            ResourceBundle bundle,
            long loadTime)
	{
		WBResourceBundle wbresource = (WBResourceBundle) bundle;
		if (!wbresource.getFingerPrint().equals(messageCache.getFingerPrint(locale)))
		{
			return true;
		}
		return false;
	}
}
