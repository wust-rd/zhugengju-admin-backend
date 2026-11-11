package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-自主归档档案 Entity
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_ARCHIVE", alias = "a", label = "自主归档档案", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "归属项目ID"),
		@Column(name = "cat", attrName = "cat", label = "档案类别"),
		@Column(name = "title", attrName = "title", label = "档案名称 / 说明"),
		@Column(name = "note", attrName = "note", label = "备注"),
		@Column(name = "archive_date", attrName = "archiveDate", label = "归档时间"),
		@Column(name = "archive_by", attrName = "archiveBy", label = "归档人"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.archive_date DESC, a.id DESC"
)
public class FillArchive extends DataEntity<FillArchive> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillArchive() {
	}

	public FillArchive(String id) {
		super(id);
	}

	/** 归属项目ID */
	protected String projectId;
	/** 档案类别 */
	protected String cat;
	/** 档案名称 / 说明 */
	protected String title;
	/** 备注 */
	protected String note;
	/** 归档时间 */
	protected Date archiveDate;
	/** 归档人 */
	protected String archiveBy;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getArchiveDate() {
		return archiveDate;
	}

	public void setArchiveDate(Date archiveDate) {
		this.archiveDate = archiveDate;
	}

	public String getArchiveBy() {
		return archiveBy;
	}

	public void setArchiveBy(String archiveBy) {
		this.archiveBy = archiveBy;
	}

}
