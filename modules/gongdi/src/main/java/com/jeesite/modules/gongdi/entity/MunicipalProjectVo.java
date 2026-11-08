package com.jeesite.modules.gongdi.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 市政工程项目基本信息
 * <p>
 * 一条对应清单中的一行：清单里有项目ID的取登记库信息，没有的原样用清单值。
 * 库中字段为空时回退清单值，保证 106 行都有可展示内容。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MunicipalProjectVo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 数据来源：db 取登记库，excel 取清单
	 */
	public static final String SOURCE_DB = "db";

	/**
	 * 数据来源：清单（登记库无此项目ID）
	 */
	public static final String SOURCE_EXCEL = "excel";

	// ========== 清单信息 ==========

	/**
	 * 清单序号
	 */
	private Integer seq;

	/**
	 * 项目ID，清单无则为空
	 */
	private Long projectId;

	/**
	 * 数据来源：db / excel
	 */
	private String dataSource;

	/**
	 * 行业分类
	 */
	private String industry;

	/**
	 * 区域（清单口径）
	 */
	private String district;

	// ========== 项目基本信息 ==========

	/**
	 * 项目名称（清单）
	 */
	private String nameExcel;

	/**
	 * 项目名称（登记库），无ID取库时为空
	 */
	private String nameDb;

	/**
	 * 总投资（清单），万元
	 */
	private String investExcel;

	/**
	 * 总投资（登记库合同金额），万元，已去千分位；无ID取库时为空
	 */
	private String investDb;

	/**
	 * 登记库中的行政区
	 */
	private String dbArea;

	/**
	 * 经度
	 */
	private String longitude;

	/**
	 * 纬度
	 */
	private String latitude;

	/**
	 * 计划开工时间
	 */
	private java.util.Date plannedStartTime;

	/**
	 * 计划竣工时间
	 */
	private java.util.Date plannedCompletionTime;

	/**
	 * 是否重点项目（1 是 0 否）
	 */
	private Integer keyProjects;

	/**
	 * 是否须报建（1 是 0 否）
	 */
	private Integer reportRequired;

	/**
	 * 项目地点
	 */
	private String address;

	/**
	 * 面积、长度
	 */
	private String constructionScale;

	/**
	 * 结构（层数、类型）
	 */
	private String structure;

	/**
	 * 联系人姓名
	 */
	private String contactName;

	/**
	 * 联系电话
	 */
	private String contactPhone;

	/**
	 * 原始「联系人联系方式」值，反序列化用，拆分后不对外输出
	 */
	@JsonProperty(value = "contact", access = JsonProperty.Access.WRITE_ONLY)
	private String rawContact;

	/**
	 * 形象进度
	 */
	private String progress;

	// ========== 责任主体 ==========

	/**
	 * 建设单位
	 */
	private String buildUnit;

	/**
	 * 施工单位
	 */
	private String constructUnit;

	/**
	 * 监理单位
	 */
	private String superviseUnit;

	// ========== 监管信息 ==========

	/**
	 * 在建 / 中止监督 / 终止监督 / 不在监（收集表监督口径）
	 */
	private String buildStatus;

	/**
	 * 施工许可证编码
	 */
	private String permitNumber;

	/**
	 * 管辖范围，取自登记库
	 */
	private String jurisdiction;

	/**
	 * 主要风险点
	 */
	private String riskPoint;

	// ========== 安全风险分级（收集表） ==========

	/**
	 * 工程类别（维度一），选项原文：隧道盾构类 / 桥梁高架类 / 常规市政 / 维修零星
	 */
	private String engineeringCategory;

	/**
	 * 危大基础信息（维度二），选项原文：存在超危大工程 / 仅危大 / 均不存在
	 */
	private String dangerBasicInfo;

	/**
	 * 危大工程数量
	 */
	private String dangerCount;

	/**
	 * 超危大工程数量
	 */
	private String superDangerCount;

	/**
	 * 危大工程名称
	 */
	private String dangerNames;

	/**
	 * 专项方案编审及专家论证：已组织 / 未组织
	 */
	private String specialPlanReview;

	/**
	 * 现场旁站与过程管控：已落实且与方案一致 / 未落实
	 */
	private String siteControlStatus;

	/**
	 * 分部分项工程验收后进入下一道工序：已组织 / 未组织
	 */
	private String acceptanceStatus;

	/**
	 * 是否有基坑工程：是 / 否
	 */
	private String hasPitWork;

	/**
	 * 现有基坑数量
	 */
	private String pitCount;

	/**
	 * 基坑深度（m）
	 */
	private String pitDepth;

	/**
	 * 支护形式
	 */
	private String pitSupportType;

	/**
	 * 是否邻近轨道/建筑物/承压水软土：是 / 否
	 */
	private String nearSensitiveArea;

	/**
	 * 监测是否在控：在控 / 超限未处置 / 监测频率不足
	 */
	private String monitoringStatus;

	/**
	 * 预警处置情况说明
	 */
	private String warningHandling;

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	/**
	 * 项目名称（清单）
	 */
	public String getNameExcel() {
		return nameExcel;
	}

	public void setNameExcel(String nameExcel) {
		this.nameExcel = nameExcel;
	}

	/**
	 * 项目名称（登记库）
	 */
	public String getNameDb() {
		return nameDb;
	}

	public void setNameDb(String nameDb) {
		this.nameDb = nameDb;
	}

	/**
	 * 总投资（清单，万元）
	 */
	public String getInvestExcel() {
		return investExcel;
	}

	public void setInvestExcel(String investExcel) {
		this.investExcel = investExcel;
	}

	/**
	 * 总投资（登记库合同金额，万元）
	 */
	public String getInvestDb() {
		return investDb;
	}

	public void setInvestDb(String investDb) {
		this.investDb = investDb;
	}

	public String getDbArea() {
		return dbArea;
	}

	public void setDbArea(String dbArea) {
		this.dbArea = dbArea;
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

	public java.util.Date getPlannedStartTime() {
		return plannedStartTime;
	}

	public void setPlannedStartTime(java.util.Date plannedStartTime) {
		this.plannedStartTime = plannedStartTime;
	}

	public java.util.Date getPlannedCompletionTime() {
		return plannedCompletionTime;
	}

	public void setPlannedCompletionTime(java.util.Date plannedCompletionTime) {
		this.plannedCompletionTime = plannedCompletionTime;
	}

	public Integer getKeyProjects() {
		return keyProjects;
	}

	public void setKeyProjects(Integer keyProjects) {
		this.keyProjects = keyProjects;
	}

	public Integer getReportRequired() {
		return reportRequired;
	}

	public void setReportRequired(Integer reportRequired) {
		this.reportRequired = reportRequired;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getConstructionScale() {
		return constructionScale;
	}

	public void setConstructionScale(String constructionScale) {
		this.constructionScale = constructionScale;
	}

	public String getStructure() {
		return structure;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	/**
	 * 原始联系人值，仅供反序列化与拆分使用
	 */
	public String getRawContact() {
		return rawContact;
	}

	public void setRawContact(String rawContact) {
		this.rawContact = rawContact;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getBuildUnit() {
		return buildUnit;
	}

	public void setBuildUnit(String buildUnit) {
		this.buildUnit = buildUnit;
	}

	public String getConstructUnit() {
		return constructUnit;
	}

	public void setConstructUnit(String constructUnit) {
		this.constructUnit = constructUnit;
	}

	public String getSuperviseUnit() {
		return superviseUnit;
	}

	public void setSuperviseUnit(String superviseUnit) {
		this.superviseUnit = superviseUnit;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public String getPermitNumber() {
		return permitNumber;
	}

	public void setPermitNumber(String permitNumber) {
		this.permitNumber = permitNumber;
	}

	public String getJurisdiction() {
		return jurisdiction;
	}

	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}

	public String getRiskPoint() {
		return riskPoint;
	}

	public void setRiskPoint(String riskPoint) {
		this.riskPoint = riskPoint;
	}

	public String getEngineeringCategory() {
		return engineeringCategory;
	}

	public void setEngineeringCategory(String engineeringCategory) {
		this.engineeringCategory = engineeringCategory;
	}

	public String getDangerBasicInfo() {
		return dangerBasicInfo;
	}

	public void setDangerBasicInfo(String dangerBasicInfo) {
		this.dangerBasicInfo = dangerBasicInfo;
	}

	public String getDangerCount() {
		return dangerCount;
	}

	public void setDangerCount(String dangerCount) {
		this.dangerCount = dangerCount;
	}

	public String getSuperDangerCount() {
		return superDangerCount;
	}

	public void setSuperDangerCount(String superDangerCount) {
		this.superDangerCount = superDangerCount;
	}

	public String getDangerNames() {
		return dangerNames;
	}

	public void setDangerNames(String dangerNames) {
		this.dangerNames = dangerNames;
	}

	public String getSpecialPlanReview() {
		return specialPlanReview;
	}

	public void setSpecialPlanReview(String specialPlanReview) {
		this.specialPlanReview = specialPlanReview;
	}

	public String getSiteControlStatus() {
		return siteControlStatus;
	}

	public void setSiteControlStatus(String siteControlStatus) {
		this.siteControlStatus = siteControlStatus;
	}

	public String getAcceptanceStatus() {
		return acceptanceStatus;
	}

	public void setAcceptanceStatus(String acceptanceStatus) {
		this.acceptanceStatus = acceptanceStatus;
	}

	public String getHasPitWork() {
		return hasPitWork;
	}

	public void setHasPitWork(String hasPitWork) {
		this.hasPitWork = hasPitWork;
	}

	public String getPitCount() {
		return pitCount;
	}

	public void setPitCount(String pitCount) {
		this.pitCount = pitCount;
	}

	public String getPitDepth() {
		return pitDepth;
	}

	public void setPitDepth(String pitDepth) {
		this.pitDepth = pitDepth;
	}

	public String getPitSupportType() {
		return pitSupportType;
	}

	public void setPitSupportType(String pitSupportType) {
		this.pitSupportType = pitSupportType;
	}

	public String getNearSensitiveArea() {
		return nearSensitiveArea;
	}

	public void setNearSensitiveArea(String nearSensitiveArea) {
		this.nearSensitiveArea = nearSensitiveArea;
	}

	public String getMonitoringStatus() {
		return monitoringStatus;
	}

	public void setMonitoringStatus(String monitoringStatus) {
		this.monitoringStatus = monitoringStatus;
	}

	public String getWarningHandling() {
		return warningHandling;
	}

	public void setWarningHandling(String warningHandling) {
		this.warningHandling = warningHandling;
	}

}
