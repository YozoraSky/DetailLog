package com.ctbcbank.detaillog.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "detaillog")
@PropertySource(value = { "classpath:application.properties" })
public class DetailLogProperties {
	private String logPath;
	private String insertStatusSql;
	private String updateStatusSql;
	private String selectStatusSql;
	private String deleteDetailLog;
	private String selectCountSql;
	private String key;
	
	public String getLogPath() {
		return logPath;
	}
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	public String getInsertStatusSql() {
		return insertStatusSql;
	}
	public void setInsertStatusSql(String insertStatusSql) {
		this.insertStatusSql = insertStatusSql;
	}
	public String getUpdateStatusSql() {
		return updateStatusSql;
	}
	public void setUpdateStatusSql(String updateStatusSql) {
		this.updateStatusSql = updateStatusSql;
	}
	public String getSelectStatusSql() {
		return selectStatusSql;
	}
	public void setSelectStatusSql(String selectStatusSql) {
		this.selectStatusSql = selectStatusSql;
	}
	public String getDeleteDetailLog() {
		return deleteDetailLog;
	}
	public void setDeleteDetailLog(String deleteDetailLog) {
		this.deleteDetailLog = deleteDetailLog;
	}
	public String getSelectCountSql() {
		return selectCountSql;
	}
	public void setSelectCountSql(String selectCountSql) {
		this.selectCountSql = selectCountSql;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}
