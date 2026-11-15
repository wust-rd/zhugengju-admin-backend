package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 市政安全一张图-基坑工程 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_PIT", alias = "a", label = "市政安全基坑", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "name", attrName = "name", label = "基坑名称"),
		@Column(name = "belong", attrName = "belong", label = "所属工区"),
		@Column(name = "depth", attrName = "depth", label = "开挖深度"),
		@Column(name = "risk_level", attrName = "riskLevel", label = "基坑风险等级"),
		@Column(name = "mon_level", attrName = "monLevel", label = "监测等级"),
		@Column(name = "stage", attrName = "stage", label = "施工阶段"),
		@Column(name = "plan", attrName = "plan", label = "专项方案状态"),
		@Column(name = "unit", attrName = "unit", label = "监测单位"),
		@Column(name = "qual", attrName = "qual", label = "监测单位资质"),
		@Column(name = "leader", attrName = "leader", label = "监测项目负责人"),
		@Column(name = "argued", attrName = "argued", label = "是否已论证"),
		@Column(name = "accept", attrName = "accept", label = "是否验收"),
		@Column(name = "monitor", attrName = "monitor", label = "是否已监测"),
		@Column(name = "status", attrName = "status", label = "状态"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sort_no")
public class OmPit extends DataEntity<OmPit> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmPit() {
	}

	public OmPit(String id) {
		super(id);
	}

	protected String projectId;
	protected String name;
	protected String belong;
	protected BigDecimal depth;
	protected String riskLevel;
	protected String monLevel;
	protected String stage;
	protected String plan;
	protected String unit;
	protected String qual;
	protected String leader;
	protected String argued;
	protected String accept;
	protected String monitor;
	protected String status;
	protected Integer sortNo;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getBelong() { return belong; }
	public void setBelong(String belong) { this.belong = belong; }
	public BigDecimal getDepth() { return depth; }
	public void setDepth(BigDecimal depth) { this.depth = depth; }
	public String getRiskLevel() { return riskLevel; }
	public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
	public String getMonLevel() { return monLevel; }
	public void setMonLevel(String monLevel) { this.monLevel = monLevel; }
	public String getStage() { return stage; }
	public void setStage(String stage) { this.stage = stage; }
	public String getPlan() { return plan; }
	public void setPlan(String plan) { this.plan = plan; }
	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	public String getQual() { return qual; }
	public void setQual(String qual) { this.qual = qual; }
	public String getLeader() { return leader; }
	public void setLeader(String leader) { this.leader = leader; }
	public String getArgued() { return argued; }
	public void setArgued(String argued) { this.argued = argued; }
	public String getAccept() { return accept; }
	public void setAccept(String accept) { this.accept = accept; }
	public String getMonitor() { return monitor; }
	public void setMonitor(String monitor) { this.monitor = monitor; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Integer getSortNo() { return sortNo; }
	public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}
