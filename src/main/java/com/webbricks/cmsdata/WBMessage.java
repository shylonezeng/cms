package com.webbricks.cmsdata;

import java.io.Serializable;
import java.util.Date;

import com.webbricks.datautility.AdminFieldKey;
import com.webbricks.datautility.AdminFieldStore;
import com.webbricks.datautility.AdminFieldTextStore;

public class WBMessage implements Serializable {

	@AdminFieldKey
	private Long key;

	@AdminFieldStore
	private String name;

	@AdminFieldStore
	private Long externalKey;

	@AdminFieldTextStore
	private String value;

	@AdminFieldStore
	private String lcid;

	@AdminFieldStore
	private Integer isTranslated;

	@AdminFieldStore
	private Date lastModified;
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getExternalKey() {
		return externalKey;
	}

	public void setExternalKey(Long externalKey) {
		this.externalKey = externalKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLcid() {
		return lcid;
	}

	public void setLcid(String lcid) {
		this.lcid = lcid;
	}

	public Integer getIsTranslated() {
		return isTranslated;
	}

	public void setIsTranslated(Integer isTranslated) {
		this.isTranslated = isTranslated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	
}
