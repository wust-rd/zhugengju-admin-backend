package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 市政安全一张图-项目台账 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_PROJECT", alias = "a", label = "市政安全项目", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_code", attrName = "projectCode", label = "项目统一编码"),
		@Column(name = "project_name", attrName = "projectName", label = "项目名称", queryType = QueryType.LIKE),
		@Column(name = "cat", attrName = "cat", label = "工程类别"),
		@Column(name = "district", attrName = "district", label = "所属区域"),
		@Column(name = "owner", attrName = "owner", label = "建设单位"),
		@Column(name = "address", attrName = "address", label = "项目地址"),
		@Column(name = "cost", attrName = "cost", label = "工程造价（万元）"),
		@Column(name = "scale", attrName = "scale", label = "建设规模"),
		@Column(name = "scale_unit", attrName = "scaleUnit", label = "规模单位"),
		@Column(name = "days", attrName = "days", label = "工期总日历天数"),
		@Column(name = "start_date", attrName = "startDate", label = "开工日期"),
		@Column(name = "end_date", attrName = "endDate", label = "计划竣工日期"),
		@Column(name = "build_status", attrName = "buildStatus", label = "施工状态"),
		@Column(name = "supervise", attrName = "supervise", label = "监管责任单位"),
		@Column(name = "lng", attrName = "lng", label = "经度"),
		@Column(name = "lat", attrName = "lat", label = "纬度"),
		@Column(name = "crs", attrName = "crs", label = "坐标系"),
		@Column(name = "spatial", attrName = "spatial", label = "空间位置信息"),
		@Column(name = "sync_at", attrName = "syncAt", label = "最近同步时间"),
		@Column(name = "risk_level", attrName = "riskLevel", label = "风险等级"),
		@Column(name = "score_total", attrName = "scoreTotal", label = "本月安全风险分值"),
		@Column(name = "inspect_score", attrName = "inspectScore", label = "安全检查分数"),
		@Column(name = "side_count", attrName = "sideCount", label = "旁站记录数"),
		@Column(name = "accept_count", attrName = "acceptCount", label = "验收记录数"),
		@Column(name = "completeness", attrName = "completeness", label = "填报完整度"),
		@Column(name = "review_json", attrName = "reviewJson", label = "评分勾选结果"),
		@Column(name = "review_src", attrName = "reviewSrc", label = "分值来源"),
		@Column(name = "review_by", attrName = "reviewBy", label = "评分人"),
		@Column(name = "review_time", attrName = "reviewTime", label = "评分时间"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.project_code")
public class OmProject extends DataEntity<OmProject> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmProject() {
	}

	public OmProject(String id) {
		super(id);
	}

	protected String projectCode;
	protected String projectName;
	protected String cat;
	protected String district;
	protected String owner;
	protected String address;
	protected BigDecimal cost;
	protected String scale;
	protected String scaleUnit;
	protected Integer days;
	protected String startDate;
	protected String endDate;
	protected String buildStatus;
	protected String supervise;
	protected String lng;
	protected String lat;
	protected String crs;
	protected String spatial;
	protected String syncAt;
	protected String riskLevel;
	protected Integer scoreTotal;
	protected Integer inspectScore;
	protected Integer sideCount;
	protected Integer acceptCount;
	protected Integer completeness;
	protected String reviewJson;
	protected String reviewSrc;
	protected String reviewBy;
	protected String reviewTime;

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

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public String getScaleUnit() {
		return scaleUnit;
	}

	public void setScaleUnit(String scaleUnit) {
		this.scaleUnit = scaleUnit;
	}

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public String getSupervise() {
		return supervise;
	}

	public void setSupervise(String supervise) {
		this.supervise = supervise;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getSpatial() {
		return spatial;
	}

	public void setSpatial(String spatial) {
		this.spatial = spatial;
	}

	public String getSyncAt() {
		return syncAt;
	}

	public void setSyncAt(String syncAt) {
		this.syncAt = syncAt;
	}

	public String getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(String riskLevel) {
		this.riskLevel = riskLevel;
	}

	public Integer getScoreTotal() {
		return scoreTotal;
	}

	public void setScoreTotal(Integer scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	public Integer getInspectScore() {
		return inspectScore;
	}

	public void setInspectScore(Integer inspectScore) {
		this.inspectScore = inspectScore;
	}

	public Integer getSideCount() {
		return sideCount;
	}

	public void setSideCount(Integer sideCount) {
		this.sideCount = sideCount;
	}

	public Integer getAcceptCount() {
		return acceptCount;
	}

	public void setAcceptCount(Integer acceptCount) {
		this.acceptCount = acceptCount;
	}

	public Integer getCompleteness() {
		return completeness;
	}

	public void setCompleteness(Integer completeness) {
		this.completeness = completeness;
	}

	public String getReviewJson() {
		return reviewJson;
	}

	public void setReviewJson(String reviewJson) {
		this.reviewJson = reviewJson;
	}

	public String getReviewSrc() {
		return reviewSrc;
	}

	public void setReviewSrc(String reviewSrc) {
		this.reviewSrc = reviewSrc;
	}

	public String getReviewBy() {
		return reviewBy;
	}

	public void setReviewBy(String reviewBy) {
		this.reviewBy = reviewBy;
	}

	public String getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

}
