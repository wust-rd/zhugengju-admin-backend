package com.jeesite.modules.onemap02fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import java.io.Serial;

/**
 * 填报-项目基本信息 Entity
 * <p>
 * 28 个首批示范项目，接口同步口径，回显后可修正，修正留痕见 FILL_CHG。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_PROJECT", alias = "a", label = "填报项目", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_code", attrName = "projectCode", label = "项目编号"),
		@Column(name = "project_name", attrName = "projectName", label = "项目名称", queryType = QueryType.LIKE),
		@Column(name = "project_type", attrName = "projectType", label = "项目类别"),
		@Column(name = "district", attrName = "district", label = "所属区 / 片区"),
		@Column(name = "project_stage", attrName = "projectStage", label = "项目阶段"),
		@Column(name = "owner_unit", attrName = "ownerUnit", label = "业主单位"),
		@Column(name = "build_unit", attrName = "buildUnit", label = "建设单位"),
		@Column(name = "master_name", attrName = "masterName", label = "项目负责人"),
		@Column(name = "master_tel", attrName = "masterTel", label = "联系电话"),
		@Column(name = "invest_total", attrName = "investTotal", label = "总投资（亿元）"),
		@Column(name = "invest_done", attrName = "investDone", label = "已完成投资（亿元）"),
		@Column(name = "year_plan", attrName = "yearPlan", label = "年度计划投资（亿元）"),
		@Column(name = "fund_source", attrName = "fundSource", label = "资金来源"),
		@Column(name = "progress_pct", attrName = "progressPct", label = "形象进度（%）"),
		@Column(name = "plan_start", attrName = "planStart", label = "计划开工时间 yyyy-MM"),
		@Column(name = "plan_end", attrName = "planEnd", label = "计划竣工时间 yyyy-MM"),
		@Column(name = "act_start", attrName = "actStart", label = "实际开工时间 yyyy-MM"),
		@Column(name = "geom_type", attrName = "geomType", label = "空间几何类型"),
		@Column(name = "longitude", attrName = "longitude", label = "经度"),
		@Column(name = "latitude", attrName = "latitude", label = "纬度"),
		@Column(name = "center_lnglat", attrName = "centerLnglat", label = "中心坐标"),
		@Column(name = "scale_desc", attrName = "scaleDesc", label = "建设规模"),
		@Column(name = "place", attrName = "place", label = "建设地点"),
		@Column(name = "scope_desc", attrName = "scopeDesc", label = "建设规模及内容"),
		@Column(name = "contractor", attrName = "contractor", label = "施工单位"),
		@Column(name = "supervisor", attrName = "supervisor", label = "监理单位"),
		@Column(name = "safety_org", attrName = "safetyOrg", label = "质量安全监督机构"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.project_code"
)
public class FillProject extends DataEntity<FillProject> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillProject() {
	}

	public FillProject(String id) {
		super(id);
	}

	/** 项目编号 */
	protected String projectCode;
	/** 项目名称 */
	protected String projectName;
	/** 项目类别 */
	protected String projectType;
	/** 所属区 / 片区 */
	protected String district;
	/** 项目阶段：pre / dev / ops */
	protected String projectStage;
	/** 业主单位 */
	protected String ownerUnit;
	/** 建设单位 */
	protected String buildUnit;
	/** 项目负责人 */
	protected String masterName;
	/** 联系电话 */
	protected String masterTel;
	/** 总投资（亿元） */
	protected Double investTotal;
	/** 已完成投资（亿元） */
	protected Double investDone;
	/** 年度计划投资（亿元） */
	protected Double yearPlan;
	/** 资金来源 */
	protected String fundSource;
	/** 形象进度（%） */
	protected Double progressPct;
	/** 计划开工时间 yyyy-MM */
	protected String planStart;
	/** 计划竣工时间 yyyy-MM */
	protected String planEnd;
	/** 实际开工时间 yyyy-MM */
	protected String actStart;
	/** 空间几何类型 */
	protected String geomType;
	/** 经度 */
	protected String longitude;
	/** 纬度 */
	protected String latitude;
	/** 中心坐标（纬度, 经度） */
	protected String centerLnglat;
	/** 建设规模 */
	protected String scaleDesc;
	/** 建设地点 */
	protected String place;
	/** 建设规模及内容 */
	protected String scopeDesc;
	/** 施工单位 */
	protected String contractor;
	/** 监理单位 */
	protected String supervisor;
	/** 质量安全监督机构 */
	protected String safetyOrg;

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getProjectStage() {
		return projectStage;
	}

	public void setProjectStage(String projectStage) {
		this.projectStage = projectStage;
	}

	public String getOwnerUnit() {
		return ownerUnit;
	}

	public void setOwnerUnit(String ownerUnit) {
		this.ownerUnit = ownerUnit;
	}

	public String getBuildUnit() {
		return buildUnit;
	}

	public void setBuildUnit(String buildUnit) {
		this.buildUnit = buildUnit;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public String getMasterTel() {
		return masterTel;
	}

	public void setMasterTel(String masterTel) {
		this.masterTel = masterTel;
	}

	public Double getInvestTotal() {
		return investTotal;
	}

	public void setInvestTotal(Double investTotal) {
		this.investTotal = investTotal;
	}

	public Double getInvestDone() {
		return investDone;
	}

	public void setInvestDone(Double investDone) {
		this.investDone = investDone;
	}

	public Double getYearPlan() {
		return yearPlan;
	}

	public void setYearPlan(Double yearPlan) {
		this.yearPlan = yearPlan;
	}

	public String getFundSource() {
		return fundSource;
	}

	public void setFundSource(String fundSource) {
		this.fundSource = fundSource;
	}

	public Double getProgressPct() {
		return progressPct;
	}

	public void setProgressPct(Double progressPct) {
		this.progressPct = progressPct;
	}

	public String getPlanStart() {
		return planStart;
	}

	public void setPlanStart(String planStart) {
		this.planStart = planStart;
	}

	public String getPlanEnd() {
		return planEnd;
	}

	public void setPlanEnd(String planEnd) {
		this.planEnd = planEnd;
	}

	public String getActStart() {
		return actStart;
	}

	public void setActStart(String actStart) {
		this.actStart = actStart;
	}

	public String getGeomType() {
		return geomType;
	}

	public void setGeomType(String geomType) {
		this.geomType = geomType;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getCenterLnglat() {
		return centerLnglat;
	}

	public void setCenterLnglat(String centerLnglat) {
		this.centerLnglat = centerLnglat;
	}

	public String getScaleDesc() {
		return scaleDesc;
	}

	public void setScaleDesc(String scaleDesc) {
		this.scaleDesc = scaleDesc;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getScopeDesc() {
		return scopeDesc;
	}

	public void setScopeDesc(String scopeDesc) {
		this.scopeDesc = scopeDesc;
	}

	public String getContractor() {
		return contractor;
	}

	public void setContractor(String contractor) {
		this.contractor = contractor;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public String getSafetyOrg() {
		return safetyOrg;
	}

	public void setSafetyOrg(String safetyOrg) {
		this.safetyOrg = safetyOrg;
	}

}
