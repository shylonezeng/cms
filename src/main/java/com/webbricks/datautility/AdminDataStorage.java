package com.webbricks.datautility;

import com.google.appengine.api.datastore.PreparedQuery;
import com.webbricks.exception.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdminDataStorage {

	enum AdminQueryOperator{
		LESS_THAN,
		GREATER_THAN,
		EQUAL,
		NOT_EQUAL,
		LESS_THAN_OR_EQUAL,
		GREATER_THAN_OR_EQUAL		
	}
	public void delete(String recordid, Class dataClass) throws WBIOException;
	
	public void delete(Long recordid, Class dataClass) throws WBIOException;
	
	public<T> List<T> getAllRecords(Class dataClass) throws WBIOException;

	public<T> T add(T t) throws WBIOException;
	
	public<T> T get(Long dataid, Class dataClass) throws WBIOException;
	
	public<T> T get(String dataid, Class dataClass) throws WBIOException;
	
	public<T> T update(T data) throws WBIOException;
	
	public<T> List<T> query(Class dataClass, String property, AdminQueryOperator operator, Object parameter) throws WBIOException;
	
	public<T> List<T> queryEx(Class dataClass, Set<String> propertyNames, Map<String, AdminQueryOperator> operators, Map<String, Object> values) throws WBIOException;
	
	public void addStorageListener(AdminDataStorageListener listener);
	
	public void removeStorageListener(AdminDataStorageListener listener);
	
	public String getUploadUrl(String returnUrl);
	
	public void deleteAllRecords(Class dataClass);
	
	public Long getUniqueId();
}
