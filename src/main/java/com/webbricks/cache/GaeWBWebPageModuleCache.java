package com.webbricks.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.webbricks.cmsdata.WBWebPageModule;
import com.webbricks.datautility.AdminDataStorage;
import com.webbricks.datautility.GaeAdminDataStorage;
import com.webbricks.exception.WBIOException;

public class GaeWBWebPageModuleCache implements WBWebPageModuleCache, WBRefreshableCache {
	private static final Logger log = Logger.getLogger(GaeWBWebPageModuleCache.class.getName());
	
	private MemcacheService memcache = null;
	private static final String memcacheNamespace = "cacheWBWebPageModule";
	private static final String memcacheMapKey = "keyToWBWebPageModule";
	private static final String memcacheMapNames = "nameToWBWebPageModule";
	private AdminDataStorage adminDataStorage = null;
	
	public GaeWBWebPageModuleCache() {
		memcache = MemcacheServiceFactory.getMemcacheService(memcacheNamespace);
		adminDataStorage = new GaeAdminDataStorage();
	}
	
	public void Refresh() throws WBIOException
	{
		RefreshInternal(null, null);
	}

	private void RefreshInternal(Map<Long, WBWebPageModule> keyMap, Map<String, WBWebPageModule> pageNamesMap) throws WBIOException
	{
		synchronized (this) {
			log.log(Level.INFO, "GaeWBWebPageModuleCache:RefreshInternal");
			List<WBWebPageModule> wbWebPageModules = adminDataStorage.getAllRecords(WBWebPageModule.class);
			if (keyMap == null)
			{
				keyMap = new HashMap<Long, WBWebPageModule>();
			}
			if (pageNamesMap == null)
			{
				pageNamesMap = new HashMap<String, WBWebPageModule>();
			}
			for (WBWebPageModule webPageModule : wbWebPageModules)
			{
				keyMap.put(webPageModule.getExternalKey(), webPageModule);
				pageNamesMap.put(webPageModule.getName(), webPageModule);
			}
			memcache.put(memcacheMapKey, keyMap);
			memcache.put(memcacheMapNames, pageNamesMap);
		}
	}

	public synchronized WBWebPageModule get(Long externalKey) throws WBIOException
	{
		HashMap<Long, WBWebPageModule> mapkeys = (HashMap<Long, WBWebPageModule>) memcache.get(memcacheMapKey);
		if (mapkeys != null && mapkeys.containsKey(externalKey))
		{
			return (WBWebPageModule) mapkeys.get(externalKey);
		}
		Map<Long, WBWebPageModule> refreshData = new HashMap<Long, WBWebPageModule>(); 
		RefreshInternal(refreshData, null);
		if (refreshData.containsKey(externalKey))
		{
			return mapkeys.get(externalKey);
		}		
		return null;

	}
	
	public WBWebPageModule get(String pageModuleName) throws WBIOException
	{
		HashMap<String, WBWebPageModule> mapNames = (HashMap<String, WBWebPageModule>) memcache.get(memcacheMapNames);
		if (mapNames != null && mapNames.containsKey(pageModuleName))
		{
			return (WBWebPageModule) mapNames.get(pageModuleName);
		}
		Map<String, WBWebPageModule> refreshData = new HashMap<String, WBWebPageModule>(); 
		RefreshInternal(null, refreshData);
		if (refreshData.containsKey(pageModuleName))
		{
			return refreshData.get(pageModuleName);
		}		
		return null;
	}
}