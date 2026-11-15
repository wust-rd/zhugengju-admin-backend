package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 市政安全一张图-审批与手续共享 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_APPROVAL", alias = "a", label = "市政安全审批手续", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "item", attrName = "item", label = "审批事项"),
		@Column(name = "doc_no", attrName = "docNo", label = "文号/编号"),
		@Column(name = "approval_date", attrName = "approvalDate", label = "批复日期"),
		@Column(name = "share_status", attrName = "shareStatus", label = "共享状态"),
		@Column(name = "source", attrName = "source", label = "数据来源"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sort_no")
public class OmApproval extends DataEntity<OmApproval> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmApproval() {
	}

	public OmApproval(String id) {
		super(id);
	}

	protected String projectId;
	protected String item;
	protected String docNo;
	protected String approvalDate;
	protected String shareStatus;
	protected String source;
	protected Integer sortNo;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getItem() { return item; }
	public void setItem(String item) { this.item = item; }
	public String getDocNo() { return docNo; }
	public void setDocNo(String docNo) { this.docNo = docNo; }
	public String getApprovalDate() { return approvalDate; }
	public void setApprovalDate(String approvalDate) { this.approvalDate = approvalDate; }
	public String getShareStatus() { return shareStatus; }
	public void setShareStatus(String shareStatus) { this.shareStatus = shareStatus; }
	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; }
	public Integer getSortNo() { return sortNo; }
	public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}
