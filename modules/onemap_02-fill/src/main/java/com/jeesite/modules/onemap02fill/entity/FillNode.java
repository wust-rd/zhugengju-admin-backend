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
 * 填报-节点 Entity
 * <p>
 * 前期审批 6 环节由接口同步（stage=pre，SOURCE=api），建设实施 / 运维管理由
 * 建设单位自定义（stage=dev/ops，SOURCE=fill）。节点顺序即一张图节点带展示顺序。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Table(name = "FILL_NODE", alias = "a", label = "填报节点", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "stage", attrName = "stage", label = "阶段"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(name = "node_name", attrName = "nodeName", label = "节点名称", queryType = QueryType.LIKE),
		@Column(name = "short_name", attrName = "shortName", label = "节点简称"),
		@Column(name = "plan_content", attrName = "planContent", label = "计划完成内容"),
		@Column(name = "plan_date", attrName = "planDate", label = "计划完成时间"),
		@Column(name = "act_date", attrName = "actDate", label = "实际完成时间"),
		@Column(name = "node_status", attrName = "nodeStatus", label = "办理状态"),
		@Column(name = "doc_no", attrName = "docNo", label = "批复 / 文号"),
		@Column(name = "start_date", attrName = "startDate", label = "开始时间"),
		@Column(name = "end_date", attrName = "endDate", label = "结束时间"),
		@Column(name = "source", attrName = "source", label = "数据来源"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.project_id, a.stage, a.sort_no"
)
public class FillNode extends DataEntity<FillNode> {

	@Serial
	private static final long serialVersionUID = 1L;

	public FillNode() {
	}

	public FillNode(String id) {
		super(id);
	}

	/** 项目ID */
	protected String projectId;
	/** 阶段：pre / dev / ops */
	protected String stage;
	/** 排序号 */
	protected Integer sortNo;
	/** 节点名称 */
	protected String nodeName;
	/** 节点简称 */
	protected String shortName;
	/** 计划完成内容 */
	protected String planContent;
	/** 计划完成时间（前期审批） */
	protected Date planDate;
	/** 实际完成时间（前期审批） */
	protected Date actDate;
	/** 办理状态：done / doing / late / todo */
	protected String nodeStatus;
	/** 批复 / 文号 */
	protected String docNo;
	/** 开始时间（建设 / 运维） */
	protected Date startDate;
	/** 结束时间（建设 / 运维） */
	protected Date endDate;
	/** 数据来源：api / fill */
	protected String source;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public Integer getSortNo() {
		return sortNo;
	}

	public void setSortNo(Integer sortNo) {
		this.sortNo = sortNo;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getPlanContent() {
		return planContent;
	}

	public void setPlanContent(String planContent) {
		this.planContent = planContent;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getPlanDate() {
		return planDate;
	}

	public void setPlanDate(Date planDate) {
		this.planDate = planDate;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getActDate() {
		return actDate;
	}

	public void setActDate(Date actDate) {
		this.actDate = actDate;
	}

	public String getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(String nodeStatus) {
		this.nodeStatus = nodeStatus;
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
