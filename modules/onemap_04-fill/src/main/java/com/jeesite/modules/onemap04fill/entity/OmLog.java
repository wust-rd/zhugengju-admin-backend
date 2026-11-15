package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 市政安全一张图-操作日志 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_LOG", alias = "a", label = "市政安全操作日志", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "log_cat", attrName = "logCat", label = "日志分类"),
		@Column(name = "log_by", attrName = "logBy", label = "操作人"),
		@Column(name = "action", attrName = "action", label = "操作内容"),
		@Column(name = "detail", attrName = "detail", label = "明细"),
		@Column(name = "log_time", attrName = "logTime", label = "操作时间"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.create_date DESC")
public class OmLog extends DataEntity<OmLog> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmLog() {
	}

	public OmLog(String id) {
		super(id);
	}

	protected String projectId;
	protected String logCat;
	protected String logBy;
	protected String action;
	protected String detail;
	protected String logTime;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getLogCat() { return logCat; }
	public void setLogCat(String logCat) { this.logCat = logCat; }
	public String getLogBy() { return logBy; }
	public void setLogBy(String logBy) { this.logBy = logBy; }
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
	public String getDetail() { return detail; }
	public void setDetail(String detail) { this.detail = detail; }
	public String getLogTime() { return logTime; }
	public void setLogTime(String logTime) { this.logTime = logTime; }

}
