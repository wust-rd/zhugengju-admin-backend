package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-附件 Entity
 * <p>
 * 节点、反馈、协调、督办、档案的附件统一归集，按 bizType + bizId 关联业务。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_FILE", alias = "a", label = "填报附件", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "归属项目ID"),
		@Column(name = "biz_type", attrName = "bizType", label = "业务类型"),
		@Column(name = "biz_id", attrName = "bizId", label = "业务主键"),
		@Column(name = "file_name", attrName = "fileName", label = "文件名称"),
		@Column(name = "file_ext", attrName = "fileExt", label = "扩展名"),
		@Column(name = "file_size", attrName = "fileSize", label = "文件大小（字节）"),
		@Column(name = "file_kind", attrName = "fileKind", label = "文件种类"),
		@Column(name = "file_path", attrName = "filePath", label = "磁盘相对路径"),
		@Column(name = "file_url", attrName = "fileUrl", label = "下载地址"),
		@Column(name = "upload_by", attrName = "uploadBy", label = "上传人"),
		@Column(name = "upload_date", attrName = "uploadDate", label = "上传时间"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.upload_date DESC, a.id DESC"
)
public class FillFile extends DataEntity<FillFile> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillFile() {
	}

	public FillFile(String id) {
		super(id);
	}

	/** 归属项目ID */
	protected String projectId;
	/** 业务类型：pre / node / node_fb / issue / issue_reply / sup / sup_reply / archive */
	protected String bizType;
	/** 业务主键 */
	protected String bizId;
	/** 文件名称 */
	protected String fileName;
	/** 扩展名 */
	protected String fileExt;
	/** 文件大小（字节） */
	protected Long fileSize;
	/** 文件种类：img / video / pdf / doc / xls / dwg / zip */
	protected String fileKind;
	/** 磁盘相对路径 */
	protected String filePath;
	/** 下载地址 */
	protected String fileUrl;
	/** 上传人 */
	protected String uploadBy;
	/** 上传时间 */
	protected Date uploadDate;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getBizId() {
		return bizId;
	}

	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileKind() {
		return fileKind;
	}

	public void setFileKind(String fileKind) {
		this.fileKind = fileKind;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getUploadBy() {
		return uploadBy;
	}

	public void setUploadBy(String uploadBy) {
		this.uploadBy = uploadBy;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

}
