package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-数据对接同步日志 Entity
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_SYNC_LOG", alias = "a", label = "同步日志", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "sync_time", attrName = "syncTime", label = "同步时间"),
		@Column(name = "api_name", attrName = "apiName", label = "接口名称"),
		@Column(name = "sync_status", attrName = "syncStatus", label = "结果"),
		@Column(name = "record_count", attrName = "recordCount", label = "条数"),
		@Column(name = "error_count", attrName = "errorCount", label = "异常条数"),
		@Column(name = "remark_text", attrName = "remarkText", label = "说明"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sync_time DESC, a.id DESC"
)
public class FillSyncLog extends DataEntity<FillSyncLog> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillSyncLog() {
	}

	public FillSyncLog(String id) {
		super(id);
	}

	/** 同步时间 */
	protected Date syncTime;
	/** 接口名称 */
	protected String apiName;
	/** 结果：成功 / 失败 */
	protected String syncStatus;
	/** 条数 */
	protected Long recordCount;
	/** 异常条数 */
	protected Long errorCount;
	/** 说明 */
	protected String remarkText;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(String syncStatus) {
		this.syncStatus = syncStatus;
	}

	public Long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
	}

	public Long getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(Long errorCount) {
		this.errorCount = errorCount;
	}

	public String getRemarkText() {
		return remarkText;
	}

	public void setRemarkText(String remarkText) {
		this.remarkText = remarkText;
	}

}
