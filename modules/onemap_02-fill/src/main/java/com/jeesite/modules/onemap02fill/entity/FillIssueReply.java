package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-协调事项回复 Entity
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_ISSUE_REPLY", alias = "a", label = "协调事项回复", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "issue_id", attrName = "issueId", label = "协调事项ID"),
		@Column(name = "reply_role", attrName = "replyRole", label = "回复角色"),
		@Column(name = "reply_who", attrName = "replyWho", label = "回复人"),
		@Column(name = "reply_dept", attrName = "replyDept", label = "回复单位"),
		@Column(name = "reply_date", attrName = "replyDate", label = "回复时间"),
		@Column(name = "reply_text", attrName = "replyText", label = "回复内容"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.reply_date, a.id"
)
public class FillIssueReply extends DataEntity<FillIssueReply> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillIssueReply() {
	}

	public FillIssueReply(String id) {
		super(id);
	}

	/** 协调事项ID */
	protected String issueId;
	/** 回复角色：gov / owner */
	protected String replyRole;
	/** 回复人 */
	protected String replyWho;
	/** 回复单位 */
	protected String replyDept;
	/** 回复时间 */
	protected Date replyDate;
	/** 回复内容 */
	protected String replyText;

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	public String getReplyRole() {
		return replyRole;
	}

	public void setReplyRole(String replyRole) {
		this.replyRole = replyRole;
	}

	public String getReplyWho() {
		return replyWho;
	}

	public void setReplyWho(String replyWho) {
		this.replyWho = replyWho;
	}

	public String getReplyDept() {
		return replyDept;
	}

	public void setReplyDept(String replyDept) {
		this.replyDept = replyDept;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getReplyDate() {
		return replyDate;
	}

	public void setReplyDate(Date replyDate) {
		this.replyDate = replyDate;
	}

	public String getReplyText() {
		return replyText;
	}

	public void setReplyText(String replyText) {
		this.replyText = replyText;
	}

}
