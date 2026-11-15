package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 市政安全一张图-日常安全隐患 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_RISK", alias = "a", label = "市政安全隐患", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "risk_date", attrName = "riskDate", label = "发现日期"),
		@Column(name = "risk_type", attrName = "riskType", label = "隐患类型"),
		@Column(name = "part", attrName = "part", label = "隐患部位"),
		@Column(name = "level", attrName = "level", label = "隐患等级"),
		@Column(name = "due_date", attrName = "dueDate", label = "整改期限"),
		@Column(name = "status", attrName = "status", label = "状态"),
		@Column(name = "owner", attrName = "owner", label = "整改责任人"),
		@Column(name = "risk_desc", attrName = "riskDesc", label = "隐患描述"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sort_no")
public class OmRisk extends DataEntity<OmRisk> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmRisk() {
	}

	public OmRisk(String id) {
		super(id);
	}

	protected String projectId;
	protected String riskDate;
	protected String riskType;
	protected String part;
	protected String level;
	protected String dueDate;
	protected String status;
	protected String owner;
	protected String riskDesc;
	protected Integer sortNo;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getRiskDate() { return riskDate; }
	public void setRiskDate(String riskDate) { this.riskDate = riskDate; }
	public String getRiskType() { return riskType; }
	public void setRiskType(String riskType) { this.riskType = riskType; }
	public String getPart() { return part; }
	public void setPart(String part) { this.part = part; }
	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }
	public String getDueDate() { return dueDate; }
	public void setDueDate(String dueDate) { this.dueDate = dueDate; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getOwner() { return owner; }
	public void setOwner(String owner) { this.owner = owner; }
	public String getRiskDesc() { return riskDesc; }
	public void setRiskDesc(String riskDesc) { this.riskDesc = riskDesc; }
	public Integer getSortNo() { return sortNo; }
	public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}
