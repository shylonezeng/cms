package com.webbricks.controllers;

import java.util.HashMap;
import java.util.Map;

import com.webbricks.cmsdata.WBParameter;

public class WBParameterValidator {
	public Map<String, String> validateCreate(WBParameter wbParameter)
	{
		Map<String, String> errors = new HashMap<String, String>();
		String name = wbParameter.getName();
		if (name==null || name.length()==0)
		{
			errors.put("name", WBErrors.WBPARAMETER_EMPTY_NAME);
		}
		
		return errors;
	}

	public Map<String, String> validateUpdate(WBParameter wbParameter)
	{
		Map<String, String> errors = new HashMap<String, String>();
		String name = wbParameter.getName();
		if (name==null || name.length()==0)
		{
			errors.put("name", WBErrors.WBPARAMETER_EMPTY_NAME);
		}

		return errors;
	}

}
