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
 * 填报-督办台账 Entity
 * <p>
 * 市住更局在项目生命周期内发出的督办事项，被督单位需在要求时限内回复。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_SUP", alias = "a", label = "督办台账", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "被督项目ID"),
		@Column(name = "sup_no", attrName = "supNo", label = "督办单号"),
		@Column(name = "title", attrName = "title", label = "督办事项", queryType = QueryType.LIKE),
		@Column(name = "sup_type", attrName = "supType", label = "督办类型"),
		@Column(name = "sup_level", attrName = "supLevel", label = "督办层级"),
		@Column(name = "content_text", attrName = "contentText", label = "督办内容"),
		@Column(name = "sup_date", attrName = "supDate", label = "发出时间"),
		@Column(name = "due_date", attrName = "dueDate", label = "要求完成时间"),
		@Column(name = "sup_status", attrName = "supStatus", label = "状态"),
		@Column(name = "sup_by", attrName = "supBy", label = "发出人"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sup_date DESC, a.id DESC"
)
public class FillSup extends DataEntity<FillSup> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillSup() {
	}

	public FillSup(String id) {
		super(id);
	}

	/** 被督项目ID */
	protected String projectId;
	/** 督办单号 */
	protected String supNo;
	/** 督办事项 */
	protected String title;
	/** 督办类型 */
	protected String supType;
	/** 督办层级：一般 / 重点 */
	protected String supLevel;
	/** 督办内容 */
	protected String contentText;
	/** 发出时间 */
	protected Date supDate;
	/** 要求完成时间 */
	protected Date dueDate;
	/** 状态：待回复 / 已回复 / 已闭环 / 已超期 */
	protected String supStatus;
	/** 发出人 */
	protected String supBy;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getSupNo() {
		return supNo;
	}

	public void setSupNo(String supNo) {
		this.supNo = supNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSupType() {
		return supType;
	}

	public void setSupType(String supType) {
		this.supType = supType;
	}

	public String getSupLevel() {
		return supLevel;
	}

	public void setSupLevel(String supLevel) {
		this.supLevel = supLevel;
	}

	public String getContentText() {
		return contentText;
	}

	public void setContentText(String contentText) {
		this.contentText = contentText;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getSupDate() {
		return supDate;
	}

	public void setSupDate(Date supDate) {
		this.supDate = supDate;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getSupStatus() {
		return supStatus;
	}

	public void setSupStatus(String supStatus) {
		this.supStatus = supStatus;
	}

	public String getSupBy() {
		return supBy;
	}

	public void setSupBy(String supBy) {
		this.supBy = supBy;
	}

}
