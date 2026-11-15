package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 市政安全一张图-危大/超危大工程 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_HAZARD", alias = "a", label = "市政安全危大工程", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "name", attrName = "name", label = "危大工程名称"),
		@Column(name = "cat", attrName = "cat", label = "工程类别"),
		@Column(name = "level", attrName = "level", label = "级别"),
		@Column(name = "stage", attrName = "stage", label = "当前所属阶段"),
		@Column(name = "plan", attrName = "plan", label = "专项方案状态"),
		@Column(name = "expert", attrName = "expert", label = "专家论证结论"),
		@Column(name = "start_date", attrName = "startDate", label = "计划开工日期"),
		@Column(name = "end_date", attrName = "endDate", label = "计划完工日期"),
		@Column(name = "supervisor", attrName = "supervisor", label = "现场监管人员"),
		@Column(name = "phone", attrName = "phone", label = "联系电话"),
		@Column(name = "part", attrName = "part", label = "所属工程/工区"),
		@Column(name = "space", attrName = "space", label = "是否含有限空间作业"),
		@Column(name = "mon", attrName = "mon", label = "是否需第三方监测"),
		@Column(name = "side", attrName = "side", label = "旁站方式"),
		@Column(name = "memo", attrName = "memo", label = "类别判定说明"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sort_no")
public class OmHazard extends DataEntity<OmHazard> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmHazard() {
	}

	public OmHazard(String id) {
		super(id);
	}

	protected String projectId;
	protected String name;
	protected String cat;
	protected String level;
	protected String stage;
	protected String plan;
	protected String expert;
	protected String startDate;
	protected String endDate;
	protected String supervisor;
	protected String phone;
	protected String part;
	protected String space;
	protected String mon;
	protected String side;
	protected String memo;
	protected Integer sortNo;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getCat() { return cat; }
	public void setCat(String cat) { this.cat = cat; }
	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }
	public String getStage() { return stage; }
	public void setStage(String stage) { this.stage = stage; }
	public String getPlan() { return plan; }
	public void setPlan(String plan) { this.plan = plan; }
	public String getExpert() { return expert; }
	public void setExpert(String expert) { this.expert = expert; }
	public String getStartDate() { return startDate; }
	public void setStartDate(String startDate) { this.startDate = startDate; }
	public String getEndDate() { return endDate; }
	public void setEndDate(String endDate) { this.endDate = endDate; }
	public String getSupervisor() { return supervisor; }
	public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getPart() { return part; }
	public void setPart(String part) { this.part = part; }
	public String getSpace() { return space; }
	public void setSpace(String space) { this.space = space; }
	public String getMon() { return mon; }
	public void setMon(String mon) { this.mon = mon; }
	public String getSide() { return side; }
	public void setSide(String side) { this.side = side; }
	public String getMemo() { return memo; }
	public void setMemo(String memo) { this.memo = memo; }
	public Integer getSortNo() { return sortNo; }
	public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}
