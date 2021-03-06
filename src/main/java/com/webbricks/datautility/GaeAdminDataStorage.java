package com.webbricks.datautility;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.webbricks.datautility.AdminDataStorage.AdminQueryOperator;
import com.webbricks.datautility.AdminDataStorageListener.AdminDataStorageOperation;
import com.webbricks.exception.WBIOException;
import com.webbricks.template.WBFreeMarkerTemplateEngine;
import com.google.appengine.api.datastore.*;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;

public class GaeAdminDataStorage implements AdminDataStorage {
	public static final int MAX_FETCH_SIZE = 1000000;
	private static final Logger log = Logger.getLogger(GaeAdminDataStorage.class.getName());

	private GaeDataStoreUtility gaeDataStoreUtility;
	private Vector<AdminDataStorageListener> storageListeners;
	
	public GaeAdminDataStorage()
	{
		gaeDataStoreUtility = new GaeDataStoreUtility();
		storageListeners = new Vector();
	}
		
	public GaeDataStoreUtility getGaeDataStoreUtility() {
		return gaeDataStoreUtility;
	}

	public void setGaeDataStoreUtility(GaeDataStoreUtility gaeDataStoreUtility) {
		this.gaeDataStoreUtility = gaeDataStoreUtility;
	}

	public void delete(String recordid, Class dataClass)
	{
			DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
			Key key = gaeDataStoreUtility.getGaeDataFactory().createKey(dataClass.getName(), recordid);
			datastoreService.delete(key);
			//gae bug on high replication datastore
			notifyOperation(null, AdminDataStorageOperation.DELETE);
			try
			{
				datastoreService.get(key);
			} catch (Exception e)
			{
				// no nothing
			}
			notifyOperation(null, AdminDataStorageOperation.DELETE);

	}
	
	public void delete(Long recordid, Class dataClass)
	{
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		Transaction tx = datastoreService.beginTransaction();		
		Key key = gaeDataStoreUtility.getGaeDataFactory().createKey(dataClass.getName(), recordid);
		datastoreService.delete(key);
		tx.commit();	
		//gae bug on high replication datastore
		try
		{
			datastoreService.get(key);
		} catch (Exception e)
		{
			// no nothing
		}
		notifyOperation(null, AdminDataStorageOperation.DELETE);

	}
	
	public<T> List<T> getAllRecords(Class dataClass) throws WBIOException
	{
		log.log(Level.INFO, "GaeAdminDataStorage:getAllRecords " + dataClass.getName());
		
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		Query query = gaeDataStoreUtility.getGaeDataFactory().createQuery(dataClass.getName());
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		List<Entity> fetchEntities = preparedQuery.asList(withLimit(GaeAdminDataStorage.MAX_FETCH_SIZE));		
		List returnObjects = new ArrayList();
		for(Entity entity: fetchEntities)
		{
			Object obj = gaeDataStoreUtility.objectFromEntity(entity, dataClass);
			returnObjects.add(obj);
		}		
		return returnObjects;
	}

	public<T> T add(T t) throws WBIOException
	{
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		Key key = null;
		Transaction tx = datastoreService.beginTransaction();		
		try
		{
			Entity entity = gaeDataStoreUtility.getEmptyEntityWithKey(t);
			entity = gaeDataStoreUtility.entityFromObject(entity, t);
			key = datastoreService.put(entity);
			gaeDataStoreUtility.populateObjectWithKey(t, key);
			tx.commit();
		} catch (Exception e)
		{
			if (tx.isActive())
			{
				tx.rollback();
			}
			throw new WBIOException(e.getMessage(), e);
		}
		
		//gae bug on high replication datastore
		try
		{
			datastoreService.get(key);
		} catch (Exception e)
		{
			// no nothing
		}
		
		notifyOperation(t, AdminDataStorageOperation.CREATE);

		return t;	
	}
	
	public<T> T get(Long dataid, Class dataClass) throws WBIOException
	{
		log.log(Level.INFO, "GaeAdminDataStorage:get " + dataClass.getName());
		
		Object object = null;
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		Transaction tx = datastoreService.beginTransaction();		
		try
		{
			Key key = gaeDataStoreUtility.getGaeDataFactory().createKey(dataClass.getName(), dataid);	
			Entity entity = datastoreService.get(key);
			object = gaeDataStoreUtility.objectFromEntity(entity, dataClass);
			gaeDataStoreUtility.populateObjectWithKey(object, key);
			tx.commit();

		}
		catch (EntityNotFoundException e)
		{
			if (tx.isActive())
			{
				tx.rollback();
			}
			return null;
		}		
		catch (Exception e)
		{
			if (tx.isActive())
			{
				tx.rollback();
			}
			throw new WBIOException(e.getMessage(), e);
		}
		return (T)object;	
	}
	
	public<T> T get(String dataid, Class dataClass) throws WBIOException
	{
		log.log(Level.INFO, "GaeAdminDataStorage:get " + dataClass.getName());
		
		Object object = null;
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		Transaction tx = datastoreService.beginTransaction();		
		try
		{
			Key key = gaeDataStoreUtility.getGaeDataFactory().createKey(dataClass.getName(), dataid);	
			Entity entity = datastoreService.get(key);
			object = gaeDataStoreUtility.objectFromEntity(entity, dataClass);
			gaeDataStoreUtility.populateObjectWithKey(object, key);
			tx.commit();

		} 
		catch (EntityNotFoundException e)
		{
			if (tx.isActive())
			{
				tx.rollback();
			}
			return null;
		}
		catch (Exception e)
		{
			if (tx.isActive())
			{
				tx.rollback();
			}
			throw new WBIOException(e.getMessage(), e);
		}
		return (T)object;	
	}
	
	public<T> T update(T t) throws WBIOException
	{
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		Key key = null;
		Transaction tx = datastoreService.beginTransaction();		
		try
		{
			Entity entity = gaeDataStoreUtility.getEmptyEntityWithKey(t);
			entity = gaeDataStoreUtility.entityFromObject(entity, t);
			key = datastoreService.put(entity);
			gaeDataStoreUtility.populateObjectWithKey(t, key);
			tx.commit();
		} catch (Exception e)
		{
			if (tx.isActive())
			{
				tx.rollback();
			}
			throw new WBIOException(e.getMessage(), e);
		}
		//gae bug on high replication datastore
		try
		{
			datastoreService.get(key);
		} catch (Exception e)
		{
			// no nothing
		}
		notifyOperation(t, AdminDataStorageOperation.UPDATE);

		return t;	
	}
	public Query.FilterOperator adminOperatorToGaeOperator(AdminQueryOperator operator)
	{
		if (operator == AdminDataStorage.AdminQueryOperator.EQUAL)
				return Query.FilterOperator.EQUAL;
		else if (operator == AdminDataStorage.AdminQueryOperator.NOT_EQUAL)
				return Query.FilterOperator.NOT_EQUAL;
		else if (operator == AdminDataStorage.AdminQueryOperator.LESS_THAN)
				return Query.FilterOperator.LESS_THAN;
		else if (operator == AdminDataStorage.AdminQueryOperator.GREATER_THAN)
				return Query.FilterOperator.GREATER_THAN;
		else if (operator == AdminDataStorage.AdminQueryOperator.LESS_THAN_OR_EQUAL)
				return Query.FilterOperator.LESS_THAN_OR_EQUAL;
		else 
			return Query.FilterOperator.GREATER_THAN_OR_EQUAL;
		
	}
	
	public<T> List<T> query(Class dataClass, String propertyName, AdminQueryOperator operator, Object value) throws WBIOException
	{
		log.log(Level.INFO, "GaeAdminDataStorage:query " + dataClass.getName());
		
		List<T> result = new ArrayList<T>();
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		try
		{
			String className = dataClass.getName();
			Query query = gaeDataStoreUtility.getGaeDataFactory().createQuery(className);
			query.addFilter(propertyName, adminOperatorToGaeOperator(operator), value);
			PreparedQuery preparedQuery = datastoreService.prepare(query);
			List<Entity> entities = preparedQuery.asList(withLimit(MAX_FETCH_SIZE));
			for(Entity entity: entities)
			{
				T t = (T)gaeDataStoreUtility.objectFromEntity(entity, dataClass);
				result.add(t);
			}
		} catch (Exception e)
		{
			throw new WBIOException(e.getMessage(), e);
		}
		return result;	
	}

	public<T> List<T> queryEx(Class dataClass, Set<String> propertyNames, Map<String, AdminQueryOperator> operators, Map<String, Object> values) throws WBIOException
	{
		log.log(Level.INFO, "GaeAdminDataStorage:queryEx " + dataClass.getName());
		
		List<T> result = new ArrayList<T>();
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		try
		{
			String className = dataClass.getName();
			Query query = gaeDataStoreUtility.getGaeDataFactory().createQuery(className);
			for(String propertyName: propertyNames)
			{
				query.addFilter(propertyName, adminOperatorToGaeOperator(operators.get(propertyName)), values.get(propertyName));
			}
			PreparedQuery preparedQuery = datastoreService.prepare(query);
			List<Entity> entities = preparedQuery.asList(withLimit(MAX_FETCH_SIZE));
			for(Entity entity: entities)
			{
				T t = (T)gaeDataStoreUtility.objectFromEntity(entity, dataClass);
				result.add(t);
			}
		} catch (Exception e)
		{
			throw new WBIOException(e.getMessage(), e);
		}
		return result;	
	}

	protected<T> void notifyOperation(T t, AdminDataStorageListener.AdminDataStorageOperation operation)
	{
		synchronized (storageListeners)
		{
			for(int i=0; i< storageListeners.size(); i++)
			{
				storageListeners.get(i).notify(t, operation);
			}
		}		
	}
	public void addStorageListener(AdminDataStorageListener listener)
	{
			storageListeners.add(listener);
	}
	
	public void removeStorageListener(AdminDataStorageListener listener)
	{
		synchronized (storageListeners)
		{
			for(int i=0; i< storageListeners.size(); i++)
			{
				if (storageListeners.get(i) == listener)
				{
					storageListeners.remove(i);
					return;
				}
			}
		}
	}
	
	public String getUploadUrl(String returnUrl)
	{
		return gaeDataStoreUtility.getUploadUrl(returnUrl);
	}
	
	public void deleteAllRecords(Class dataClass)
	{

	}
	
	public Long getUniqueId()
	{
		log.log(Level.INFO, "GaeAdminDataStorage:getUniqueId ");
		DatastoreService datastoreService = gaeDataStoreUtility.getGaeDataFactory().createDatastoreService();
		KeyRange range = datastoreService.allocateIds("java.lang.String", 1);
		return range.getStart().getId();
	}
}
