package com.jeesite.modules.onemap02fill.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 填报-节点期内反馈 Entity
 * <p>
 * 每个节点保留最新一条反馈；反馈内容与影像直接展示在一张图节点详情中。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_NODE_FB", alias = "a", label = "节点反馈", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "node_id", attrName = "nodeId", label = "节点ID"),
		@Column(name = "fb_date", attrName = "fbDate", label = "反馈时间"),
		@Column(name = "done_flag", attrName = "doneFlag", label = "完成状态"),
		@Column(name = "pct", attrName = "pct", label = "完成百分比"),
		@Column(name = "fb_text", attrName = "fbText", label = "完成情况说明"),
		@Column(name = "fb_by", attrName = "fbBy", label = "反馈人"),
		@Column(name = "fb_time", attrName = "fbTime", label = "反馈提交时间"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.fb_date"
)
public class FillNodeFb extends DataEntity<FillNodeFb> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillNodeFb() {
	}

	public FillNodeFb(String id) {
		super(id);
	}

	/** 节点ID */
	protected String nodeId;
	/** 反馈时间 */
	protected Date fbDate;
	/** 完成状态：0 阶段性进展 / 1 已完成 */
	protected String doneFlag;
	/** 完成百分比 */
	protected Double pct;
	/** 完成情况说明 */
	protected String fbText;
	/** 反馈人 */
	protected String fbBy;
	/** 反馈提交时间 */
	protected Date fbTime;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getFbDate() {
		return fbDate;
	}

	public void setFbDate(Date fbDate) {
		this.fbDate = fbDate;
	}

	public String getDoneFlag() {
		return doneFlag;
	}

	public void setDoneFlag(String doneFlag) {
		this.doneFlag = doneFlag;
	}

	public Double getPct() {
		return pct;
	}

	public void setPct(Double pct) {
		this.pct = pct;
	}

	public String getFbText() {
		return fbText;
	}

	public void setFbText(String fbText) {
		this.fbText = fbText;
	}

	public String getFbBy() {
		return fbBy;
	}

	public void setFbBy(String fbBy) {
		this.fbBy = fbBy;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getFbTime() {
		return fbTime;
	}

	public void setFbTime(Date fbTime) {
		this.fbTime = fbTime;
	}

}
