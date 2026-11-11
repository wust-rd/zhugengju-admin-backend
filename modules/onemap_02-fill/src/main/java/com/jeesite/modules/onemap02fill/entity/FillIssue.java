package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-协调事项 Entity
 * <p>
 * 项目建设期内需市住更局协调解决的事项，一事一报；住更局回复全过程留痕。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_ISSUE", alias = "a", label = "协调事项", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "issue_no", attrName = "issueNo", label = "编号"),
		@Column(name = "title", attrName = "title", label = "事项标题", queryType = QueryType.LIKE),
		@Column(name = "issue_type", attrName = "issueType", label = "事项类型"),
		@Column(name = "urgent", attrName = "urgent", label = "紧急程度"),
		@Column(name = "dept", attrName = "dept", label = "涉及单位"),
		@Column(name = "desc_text", attrName = "descText", label = "事项描述"),
		@Column(name = "issue_date", attrName = "issueDate", label = "提交时间"),
		@Column(name = "expect_date", attrName = "expectDate", label = "期望完成时间"),
		@Column(name = "issue_status", attrName = "issueStatus", label = "状态"),
		@Column(name = "issue_by", attrName = "issueBy", label = "提交人"),
		@Column(name = "source", attrName = "source", label = "数据来源"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.issue_date DESC, a.id DESC"
)
public class FillIssue extends DataEntity<FillIssue> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillIssue() {
	}

	public FillIssue(String id) {
		super(id);
	}

	/** 项目ID */
	protected String projectId;
	/** 编号 */
	protected String issueNo;
	/** 事项标题 */
	protected String title;
	/** 事项类型 */
	protected String issueType;
	/** 紧急程度：一般 / 紧急 */
	protected String urgent;
	/** 涉及单位 */
	protected String dept;
	/** 事项描述 */
	protected String descText;
	/** 提交时间 */
	protected Date issueDate;
	/** 期望完成时间 */
	protected Date expectDate;
	/** 状态：待受理 / 协调中 / 已办结 / 已超期 */
	protected String issueStatus;
	/** 提交人 */
	protected String issueBy;
	/** 数据来源：fill */
	protected String source;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getIssueNo() {
		return issueNo;
	}

	public void setIssueNo(String issueNo) {
		this.issueNo = issueNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	public String getUrgent() {
		return urgent;
	}

	public void setUrgent(String urgent) {
		this.urgent = urgent;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getDescText() {
		return descText;
	}

	public void setDescText(String descText) {
		this.descText = descText;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getExpectDate() {
		return expectDate;
	}

	public void setExpectDate(Date expectDate) {
		this.expectDate = expectDate;
	}

	public String getIssueStatus() {
		return issueStatus;
	}

	public void setIssueStatus(String issueStatus) {
		this.issueStatus = issueStatus;
	}

	public String getIssueBy() {
		return issueBy;
	}

	public void setIssueBy(String issueBy) {
		this.issueBy = issueBy;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
