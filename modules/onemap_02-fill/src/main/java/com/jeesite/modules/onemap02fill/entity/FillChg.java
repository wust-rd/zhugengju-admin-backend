package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-修改留痕 Entity
 * <p>
 * 基本信息与前期审批接口字段修正后留痕备查，修正值作为一张图展示依据。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_CHG", alias = "a", label = "修改留痕", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "field_label", attrName = "fieldLabel", label = "字段名称"),
		@Column(name = "old_value", attrName = "oldValue", label = "原值"),
		@Column(name = "new_value", attrName = "newValue", label = "新值"),
		@Column(name = "chg_time", attrName = "chgTime", label = "修改时间"),
		@Column(name = "chg_by", attrName = "chgBy", label = "操作人"),
		@Column(name = "note", attrName = "note", label = "备注"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.chg_time DESC, a.id DESC"
)
public class FillChg extends DataEntity<FillChg> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillChg() {
	}

	public FillChg(String id) {
		super(id);
	}

	/** 项目ID */
	protected String projectId;
	/** 字段名称 */
	protected String fieldLabel;
	/** 原值 */
	protected String oldValue;
	/** 新值 */
	protected String newValue;
	/** 修改时间 */
	protected Date chgTime;
	/** 操作人 */
	protected String chgBy;
	/** 备注 */
	protected String note;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getChgTime() {
		return chgTime;
	}

	public void setChgTime(Date chgTime) {
		this.chgTime = chgTime;
	}

	public String getChgBy() {
		return chgBy;
	}

	public void setChgBy(String chgBy) {
		this.chgBy = chgBy;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
