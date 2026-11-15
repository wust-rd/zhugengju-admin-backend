package com.jeesite.modules.onemap04fill.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 市政安全一张图-参建五方责任主体 Entity
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Table(name = "OM4_PARTY", alias = "a", label = "市政安全参建单位", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目ID"),
		@Column(name = "party_type", attrName = "partyType", label = "单位类型"),
		@Column(name = "name", attrName = "name", label = "企业名称"),
		@Column(name = "leader", attrName = "leader", label = "项目负责人"),
		@Column(name = "cert", attrName = "cert", label = "执业证书编号"),
		@Column(name = "phone", attrName = "phone", label = "联系方式"),
		@Column(name = "sort_no", attrName = "sortNo", label = "排序号"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.sort_no")
public class OmParty extends DataEntity<OmParty> {

	@Serial
	private static final long serialVersionUID = 1L;

	public OmParty() {
	}

	public OmParty(String id) {
		super(id);
	}

	protected String projectId;
	protected String partyType;
	protected String name;
	protected String leader;
	protected String cert;
	protected String phone;
	protected Integer sortNo;

	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getPartyType() { return partyType; }
	public void setPartyType(String partyType) { this.partyType = partyType; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getLeader() { return leader; }
	public void setLeader(String leader) { this.leader = leader; }
	public String getCert() { return cert; }
	public void setCert(String cert) { this.cert = cert; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public Integer getSortNo() { return sortNo; }
	public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

}
