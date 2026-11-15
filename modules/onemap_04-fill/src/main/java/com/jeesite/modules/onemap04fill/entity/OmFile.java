package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 市政安全一张图-附件 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_FILE", alias = "a", label = "市政安全附件", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "biz_type", attrName = "bizType", label = "业务类型"),
		@Column(name = "biz_key", attrName = "bizKey", label = "业务键"),
		@Column(name = "att_type", attrName = "attType", label = "附件类型"),
		@Column(name = "file_name", attrName = "fileName", label = "文件名称"),
		@Column(name = "file_path", attrName = "filePath", label = "落盘路径"),
		@Column(name = "file_size", attrName = "fileSize", label = "文件大小"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sort_no")
public class OmFile extends DataEntity<OmFile> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmFile() {
	}

	public OmFile(String id) {
		super(id);
	}

	protected String projectId;
	protected String bizType;
	protected String bizKey;
	protected String attType;
	protected String fileName;
	protected String filePath;
	protected Long fileSize;
	protected Integer sortNo;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getBizType() { return bizType; }
	public void setBizType(String bizType) { this.bizType = bizType; }
	public String getBizKey() { return bizKey; }
	public void setBizKey(String bizKey) { this.bizKey = bizKey; }
	public String getAttType() { return attType; }
	public void setAttType(String attType) { this.attType = attType; }
	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }
	public String getFilePath() { return filePath; }
	public void setFilePath(String filePath) { this.filePath = filePath; }
	public Long getFileSize() { return fileSize; }
	public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
	public Integer getSortNo() { return sortNo; }
	public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}
