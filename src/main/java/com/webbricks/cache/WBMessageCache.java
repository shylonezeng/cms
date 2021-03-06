package com.webbricks.cache;

import java.util.Locale;
import java.util.Map;

import com.webbricks.cmsdata.WBMessage;
import com.webbricks.exception.WBIOException;

public interface WBMessageCache extends WBRefreshableCache {
	public Map<String, String> getAllMessages(Locale locale) throws WBIOException;
	public Map<String, String> getAllMessages(String lcid) throws WBIOException;
	public Long getFingerPrint(Locale locale);
}
