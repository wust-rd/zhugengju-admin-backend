package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-督办回复 Entity
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_SUP_REPLY", alias = "a", label = "督办回复", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "sup_id", attrName = "supId", label = "督办ID"),
		@Column(name = "reply_date", attrName = "replyDate", label = "回复时间"),
		@Column(name = "reply_by", attrName = "replyBy", label = "回复人"),
		@Column(name = "reply_text", attrName = "replyText", label = "整改回复内容"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.reply_date"
)
public class FillSupReply extends DataEntity<FillSupReply> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillSupReply() {
	}

	public FillSupReply(String id) {
		super(id);
	}

	/** 督办ID */
	protected String supId;
	/** 回复时间 */
	protected Date replyDate;
	/** 回复人 */
	protected String replyBy;
	/** 整改回复内容 */
	protected String replyText;

	public String getSupId() {
		return supId;
	}

	public void setSupId(String supId) {
		this.supId = supId;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getReplyDate() {
		return replyDate;
	}

	public void setReplyDate(Date replyDate) {
		this.replyDate = replyDate;
	}

	public String getReplyBy() {
		return replyBy;
	}

	public void setReplyBy(String replyBy) {
		this.replyBy = replyBy;
	}

	public String getReplyText() {
		return replyText;
	}

	public void setReplyText(String replyText) {
		this.replyText = replyText;
	}

}
