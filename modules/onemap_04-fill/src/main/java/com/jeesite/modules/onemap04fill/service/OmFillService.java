package com.jeesite.modules.onemap04fill.service;

import com.jeesite.common.config.Global;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.onemap04fill.dao.OmApprovalDao;
import com.jeesite.modules.onemap04fill.dao.OmFileDao;
import com.jeesite.modules.onemap04fill.dao.OmHazardDao;
import com.jeesite.modules.onemap04fill.dao.OmLogDao;
import com.jeesite.modules.onemap04fill.dao.OmPartyDao;
import com.jeesite.modules.onemap04fill.dao.OmPitDao;
import com.jeesite.modules.onemap04fill.dao.OmProjectDao;
import com.jeesite.modules.onemap04fill.dao.OmRiskDao;
import com.jeesite.modules.onemap04fill.entity.OmApproval;
import com.jeesite.modules.onemap04fill.entity.OmFile;
import com.jeesite.modules.onemap04fill.entity.OmHazard;
import com.jeesite.modules.onemap04fill.entity.OmLog;
import com.jeesite.modules.onemap04fill.entity.OmParty;
import com.jeesite.modules.onemap04fill.entity.OmPit;
import com.jeesite.modules.onemap04fill.entity.OmProject;
import com.jeesite.modules.onemap04fill.entity.OmRisk;
import com.jeesite.modules.onemap04fill.util.OmMeta;
import com.jeesite.modules.onemap04fill.util.OmScoreEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 市政安全一张图填报系统 - 业务服务
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Service
@Transactional(readOnly = true)
public class OmFillService {

	private final OmProjectDao omProjectDao;
	private final OmPartyDao omPartyDao;
	private final OmApprovalDao omApprovalDao;
	private final OmHazardDao omHazardDao;
	private final OmPitDao omPitDao;
	private final OmRiskDao omRiskDao;
	private final OmFileDao omFileDao;
	private final OmLogDao omLogDao;

	public OmFillService(OmProjectDao omProjectDao, OmPartyDao omPartyDao, OmApprovalDao omApprovalDao,
			OmHazardDao omHazardDao, OmPitDao omPitDao, OmRiskDao omRiskDao, OmFileDao omFileDao, OmLogDao omLogDao) {
		this.omProjectDao = omProjectDao;
		this.omPartyDao = omPartyDao;
		this.omApprovalDao = omApprovalDao;
		this.omHazardDao = omHazardDao;
		this.omPitDao = omPitDao;
		this.omRiskDao = omRiskDao;
		this.omFileDao = omFileDao;
		this.omLogDao = omLogDao;
	}

	/** 字典与评分口径 */
	public Map<String, Object> meta() {
		return OmMeta.meta();
	}

	// ==================== 项目台账 ====================

	/** 项目列表（内存过滤 + 分页），返回统计与分值 */
	public Map<String, Object> projectList(String district, String cat, String buildStatus, String riskLevel,
			String kw, int page, int pageSize) {
		List<OmProject> all = omProjectDao.findList(new OmProject()).stream()
				.sorted(Comparator.comparing(OmProject::getProjectCode, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
		List<Map<String, Object>> rows = new ArrayList<>();
		for (OmProject p : all) {
			Map<String, Object> m = projectView(p);
			if (StringUtils.isNotBlank(district) && !district.equals(m.get("district"))) {
				continue;
			}
			if (StringUtils.isNotBlank(cat) && !cat.equals(m.get("cat"))) {
				continue;
			}
			if (StringUtils.isNotBlank(buildStatus) && !buildStatus.equals(m.get("buildStatus"))) {
				continue;
			}
			if (StringUtils.isNotBlank(riskLevel) && !riskLevel.equals(String.valueOf(m.get("riskLevel")))) {
				continue;
			}
			if (StringUtils.isNotBlank(kw)) {
				String hay = str(m.get("projectName")) + str(m.get("projectCode")) + str(m.get("owner"));
				if (!hay.contains(kw.trim())) {
					continue;
				}
			}
			rows.add(m);
		}
		int total = rows.size();
		int size = pageSize <= 0 ? 5 : pageSize;
		int from = Math.max(0, (Math.max(page, 1) - 1) * size);
		List<Map<String, Object>> pageRows = rows.stream().skip(from).limit(size).collect(Collectors.toList());
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("total", total);
		result.put("page", page);
		result.put("pageSize", size);
		result.put("rows", pageRows);
		return result;
	}

	/** 列表行视图：台账字段 + 动态分值 + 完整度 */
	private Map<String, Object> projectView(OmProject p) {
		List<OmHazard> hazards = hazardList(p.getId());
		List<OmPit> pits = pitList(p.getId());
		Map<String, Object> score = effectiveScore(p, hazards, pits);
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", p.getId());
		m.put("projectCode", p.getProjectCode());
		m.put("projectName", p.getProjectName());
		m.put("cat", p.getCat());
		m.put("district", p.getDistrict());
		m.put("owner", p.getOwner());
		m.put("buildStatus", p.getBuildStatus());
		m.put("syncAt", p.getSyncAt());
		m.put("hazardTotal", hazards.size());
		m.put("hazardSup", hazards.stream().filter(h -> "超危大".equals(h.getLevel())).count());
		m.put("pitTotal", pits.size());
		m.put("score", score);
		m.put("scoreTotal", score.get("total"));
		m.put("riskLevel", score.get("lv"));
		m.put("completeness", completeness(p, hazards, pits));
		return m;
	}

	/** 有效分值：已提交评分用人工勾选结果，否则按台账自动推算 */
	private Map<String, Object> effectiveScore(OmProject p, List<OmHazard> hazards, List<OmPit> pits) {
		ObjectNode review = (ObjectNode) OmScoreEngine.toJsonNode(p.getReviewJson());
		if (review != null && review.isObject() && review.has("haz")
				&& !"empty".equals(review.path("src").asText())) {
			return OmScoreEngine.score(review);
		}
		return OmScoreEngine.score(OmScoreEngine.autoSelections(p, hazards, pits));
	}

	/** 填报完整度（仅统计本系统可填报字段，外部系统数据留空不计入） */
	private int completeness(OmProject p, List<OmHazard> hazards, List<OmPit> pits) {
		List<Boolean> c = List.of(
				StringUtils.isNotBlank(p.getProjectCode()),
				StringUtils.isNotBlank(p.getProjectName()),
				p.getCost() != null,
				StringUtils.isNotBlank(p.getAddress()),
				StringUtils.isNotBlank(p.getOwner()),
				partyList(p.getId()).stream().anyMatch(x -> "施工单位".equals(x.getPartyType())
						&& StringUtils.isNotBlank(x.getName())),
				StringUtils.isNotBlank(p.getLng()),
				StringUtils.isNotBlank(p.getLat()),
				!hazards.isEmpty(),
				!pits.isEmpty(),
				approvalList(p.getId()).stream().filter(a -> "已共享".equals(a.getShareStatus())).count() >= 5,
				!riskList(p.getId()).isEmpty());
		return (int) Math.round(c.stream().filter(Boolean::booleanValue).count() * 100.0 / c.size());
	}

	/** 项目详情 */
	public Map<String, Object> projectGet(String id) {
		OmProject p = omProjectDao.get(new OmProject(id));
		if (p == null) {
			return null;
		}
		List<OmHazard> hazards = hazardList(id);
		List<OmPit> pits = pitList(id);
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("base", baseView(p));
		m.put("parties", partyList(id));
		m.put("approvals", approvalList(id));
		m.put("hazards", hazards);
		m.put("pits", pits);
		m.put("risks", riskList(id));
		m.put("files", fileList(id));
		m.put("score", effectiveScore(p, hazards, pits));
		m.put("review", OmScoreEngine.toMap(OmScoreEngine.toJsonNode(p.getReviewJson())));
		m.put("completeness", completeness(p, hazards, pits));
		return m;
	}

	private Map<String, Object> baseView(OmProject p) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", p.getId());
		m.put("projectCode", p.getProjectCode());
		m.put("projectName", p.getProjectName());
		m.put("cat", p.getCat());
		m.put("district", p.getDistrict());
		m.put("owner", p.getOwner());
		m.put("address", p.getAddress());
		m.put("cost", p.getCost());
		m.put("scale", p.getScale());
		m.put("scaleUnit", p.getScaleUnit());
		m.put("days", p.getDays());
		m.put("startDate", p.getStartDate());
		m.put("endDate", p.getEndDate());
		m.put("buildStatus", p.getBuildStatus());
		m.put("supervise", p.getSupervise());
		m.put("lng", p.getLng());
		m.put("lat", p.getLat());
		m.put("crs", p.getCrs());
		m.put("spatial", p.getSpatial());
		m.put("syncAt", p.getSyncAt());
		m.put("inspectScore", p.getInspectScore());
		m.put("reviewSrc", p.getReviewSrc());
		m.put("reviewBy", p.getReviewBy());
		m.put("reviewTime", p.getReviewTime());
		return m;
	}

	/** 新增项目（手工建档） */
	@Transactional
	public Map<String, Object> projectCreate(Map<String, Object> form, String operator) {
		OmProject p = new OmProject();
		applyBase(p, form);
		if (StringUtils.isBlank(p.getProjectCode())) {
			throw new IllegalArgumentException("项目统一编码不能为空");
		}
		OmProject exist = new OmProject();
		exist.setProjectCode(p.getProjectCode());
		if (!omProjectDao.findList(exist).isEmpty()) {
			throw new IllegalArgumentException("该项目编码已在台账中，请勿重复建档");
		}
		if (StringUtils.isBlank(p.getBuildStatus())) {
			p.setBuildStatus("未开工");
		}
		if (StringUtils.isBlank(p.getSyncAt())) {
			p.setSyncAt(now());
		}
		p.setInspectScore(intVal(form.get("inspectScore"), 0));
		p.setRiskLevel("4");
		p.setScoreTotal(0);
		p.setCompleteness(0);
		p.setStatus("0");
		omProjectDao.insert(p);
		saveParties(p.getId(), form, true);
		if (partyList(p.getId()).isEmpty()) {
			saveDefaultParties(p.getId(), p.getOwner());
		}
		saveApprovals(p.getId(), form, true);
		addLog(p.getId(), "填报", operator, "新建项目台账", "项目统一编码 " + p.getProjectCode());
		return projectGet(p.getId());
	}

	/** 保存项目基本信息（含五方责任主体与审批手续），接口字段修改记留痕 */
	@Transactional
	public Map<String, Object> projectSave(String projectId, Map<String, Object> form, String operator) {
		OmProject p = omProjectDao.get(new OmProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在");
		}
		OmProject before = omProjectDao.get(new OmProject(projectId));
		Map<String, String> snap = baseSnapshot(before);
		applyBase(p, form);
		omProjectDao.update(p);
		saveParties(p.getId(), form, false);
		saveApprovals(p.getId(), form, false);
		List<String> changed = diffBase(snap, p);
		if (!changed.isEmpty()) {
			addLog(p.getId(), "填报", operator, "保存项目基本信息", "变更字段：" + String.join("、", changed));
		}
		return projectGet(p.getId());
	}

	private void applyBase(OmProject p, Map<String, Object> form) {
		if (form == null) {
			return;
		}
		p.setProjectCode(str(form.get("projectCode"), p.getProjectCode()));
		p.setProjectName(str(form.get("projectName"), p.getProjectName()));
		p.setCat(str(form.get("cat"), p.getCat()));
		p.setDistrict(str(form.get("district"), p.getDistrict()));
		p.setOwner(str(form.get("owner"), p.getOwner()));
		p.setAddress(str(form.get("address"), p.getAddress()));
		p.setCost(decimal(form.get("cost"), p.getCost()));
		p.setScale(str(form.get("scale"), p.getScale()));
		p.setScaleUnit(str(form.get("scaleUnit"), p.getScaleUnit()));
		p.setDays(intVal(form.get("days"), p.getDays()));
		p.setStartDate(str(form.get("startDate"), p.getStartDate()));
		p.setEndDate(str(form.get("endDate"), p.getEndDate()));
		p.setBuildStatus(str(form.get("buildStatus"), p.getBuildStatus()));
		p.setSupervise(str(form.get("supervise"), p.getSupervise()));
		p.setLng(str(form.get("lng"), p.getLng()));
		p.setLat(str(form.get("lat"), p.getLat()));
		p.setCrs(str(form.get("crs"), p.getCrs()));
		p.setSpatial(str(form.get("spatial"), p.getSpatial()));
		if (form.get("inspectScore") != null) {
			p.setInspectScore(intVal(form.get("inspectScore"), p.getInspectScore()));
		}
	}

	/** 修改前快照（MyBatis 一级缓存会让两次 get 返回同一实例，故先取字段值） */
	private Map<String, String> baseSnapshot(OmProject p) {
		Map<String, String> m = new LinkedHashMap<>();
		m.put("项目名称", str(p.getProjectName()));
		m.put("工程类别", str(p.getCat()));
		m.put("所属区域", str(p.getDistrict()));
		m.put("建设单位", str(p.getOwner()));
		m.put("项目地址", str(p.getAddress()));
		m.put("工程造价", str(p.getCost()));
		m.put("施工状态", str(p.getBuildStatus()));
		m.put("经度", str(p.getLng()));
		m.put("纬度", str(p.getLat()));
		m.put("安全检查分数", str(p.getInspectScore()));
		return m;
	}

	private List<String> diffBase(Map<String, String> snap, OmProject after) {
		List<String> list = new ArrayList<>();
		compare(list, "项目名称", snap.get("项目名称"), str(after.getProjectName()));
		compare(list, "工程类别", snap.get("工程类别"), str(after.getCat()));
		compare(list, "所属区域", snap.get("所属区域"), str(after.getDistrict()));
		compare(list, "建设单位", snap.get("建设单位"), str(after.getOwner()));
		compare(list, "项目地址", snap.get("项目地址"), str(after.getAddress()));
		compare(list, "工程造价", snap.get("工程造价"), str(after.getCost()));
		compare(list, "施工状态", snap.get("施工状态"), str(after.getBuildStatus()));
		compare(list, "经度", snap.get("经度"), str(after.getLng()));
		compare(list, "纬度", snap.get("纬度"), str(after.getLat()));
		compare(list, "安全检查分数", snap.get("安全检查分数"), str(after.getInspectScore()));
		return list;
	}

	private void compare(List<String> list, String name, String a, String b) {
		if (StringUtils.equals(StringUtils.trim(a), StringUtils.trim(b))) {
			return;
		}
		list.add(name);
	}

	// ==================== 参建五方 / 审批手续 ====================

	private void saveParties(String projectId, Map<String, Object> form, boolean create) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> parties = form == null ? null : (List<Map<String, Object>>) form.get("parties");
		if (parties == null) {
			return;
		}
		OmParty where = new OmParty();
		where.setProjectId(projectId);
		List<OmParty> old = omPartyDao.findList(where);
		for (OmParty p : old) {
			omPartyDao.delete(p);
		}
		int i = 0;
		for (Map<String, Object> m : parties) {
			OmParty p = new OmParty();
			p.setProjectId(projectId);
			p.setPartyType(str(m.get("partyType")));
			p.setName(str(m.get("name")));
			p.setLeader(str(m.get("leader")));
			p.setCert(str(m.get("cert")));
			p.setPhone(str(m.get("phone")));
			p.setSortNo(i++);
			omPartyDao.insert(p);
		}
	}

	/** 新建项目未传五方时，按原型口径预置五方责任主体（待平台同步） */
	private void saveDefaultParties(String projectId, String owner) {
		List<String> types = List.of("建设单位", "设计单位", "施工单位", "监理单位", "勘察单位");
		int i = 0;
		for (String type : types) {
			OmParty p = new OmParty();
			p.setProjectId(projectId);
			p.setPartyType(type);
			p.setName("建设单位".equals(type) ? StringUtils.defaultIfBlank(owner, "待同步") : "待平台同步");
			p.setLeader("待同步");
			p.setCert("待同步");
			p.setPhone("待同步");
			p.setSortNo(i++);
			omPartyDao.insert(p);
		}
	}

	private void saveApprovals(String projectId, Map<String, Object> form, boolean create) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> approvals = form == null ? null : (List<Map<String, Object>>) form.get("approvals");
		if (approvals == null) {
			if (create) {
				// 新建项目时预置 9 类审批事项（待共享）
				List<String> items = List.of("立项批复", "规划许可", "施工图审查", "招投标", "施工许可",
						"报建", "阶段验收", "竣工验收", "备案");
				int i = 0;
				for (String item : items) {
					OmApproval a = new OmApproval();
					a.setProjectId(projectId);
					a.setItem(item);
					a.setDocNo("待同步");
					a.setApprovalDate("—");
					a.setShareStatus("待共享");
					a.setSource("市政基础设施应用场景平台");
					a.setSortNo(i++);
					omApprovalDao.insert(a);
				}
			}
			return;
		}
		OmApproval where = new OmApproval();
		where.setProjectId(projectId);
		List<OmApproval> old = omApprovalDao.findList(where);
		for (OmApproval a : old) {
			omApprovalDao.delete(a);
		}
		int i = 0;
		for (Map<String, Object> m : approvals) {
			OmApproval a = new OmApproval();
			a.setProjectId(projectId);
			a.setItem(str(m.get("item")));
			a.setDocNo(str(m.get("docNo")));
			a.setApprovalDate(str(m.get("approvalDate")));
			a.setShareStatus(str(m.get("shareStatus")));
			a.setSource(str(m.get("source")));
			a.setSortNo(i++);
			omApprovalDao.insert(a);
		}
	}

	private List<OmParty> partyList(String projectId) {
		OmParty where = new OmParty();
		where.setProjectId(projectId);
		return omPartyDao.findList(where);
	}

	private List<OmApproval> approvalList(String projectId) {
		OmApproval where = new OmApproval();
		where.setProjectId(projectId);
		return omApprovalDao.findList(where);
	}

	// ==================== 危大 / 超危大工程 ====================

	public List<OmHazard> hazardList(String projectId) {
		OmHazard where = new OmHazard();
		where.setProjectId(projectId);
		return omHazardDao.findList(where);
	}

	@Transactional
	public OmHazard hazardSave(Map<String, Object> form, String operator) {
		OmHazard h = StringUtils.isNotBlank(str(form.get("id"))) ? omHazardDao.get(new OmHazard(str(form.get("id")))) : new OmHazard();
		if (h == null) {
			h = new OmHazard();
		}
		h.setProjectId(str(form.get("projectId")));
		h.setName(str(form.get("name")));
		h.setCat(str(form.get("cat")));
		h.setLevel(str(form.get("level")));
		h.setStage(str(form.get("stage")));
		h.setPlan(str(form.get("plan")));
		h.setExpert(str(form.get("expert")));
		h.setStartDate(str(form.get("startDate")));
		h.setEndDate(str(form.get("endDate")));
		h.setSupervisor(str(form.get("supervisor")));
		h.setPhone(str(form.get("phone")));
		h.setPart(str(form.get("part")));
		h.setSpace(str(form.get("space")));
		h.setMon(str(form.get("mon")));
		h.setSide(str(form.get("side")));
		h.setMemo(str(form.get("memo")));
		if (h.getId() == null) {
			omHazardDao.insert(h);
			addLog(h.getProjectId(), "填报", operator, "新增危大工程", h.getLevel() + " · " + h.getName());
		} else {
			omHazardDao.update(h);
			addLog(h.getProjectId(), "填报", operator, "编辑危大工程", h.getLevel() + " · " + h.getName());
		}
		return h;
	}

	@Transactional
	public void hazardDelete(String id, String operator) {
		OmHazard h = omHazardDao.get(new OmHazard(id));
		if (h == null) {
			return;
		}
		omHazardDao.delete(h);
		addLog(h.getProjectId(), "填报", operator, "删除危大工程", h.getLevel() + " · " + h.getName());
	}

	// ==================== 基坑工程 ====================

	public List<OmPit> pitList(String projectId) {
		OmPit where = new OmPit();
		where.setProjectId(projectId);
		return omPitDao.findList(where);
	}

	/** 全量保存基坑清单（前端逐座编辑后提交） */
	@Transactional
	public List<OmPit> pitSave(String projectId, List<Map<String, Object>> list, String operator) {
		OmPit where = new OmPit();
		where.setProjectId(projectId);
		List<OmPit> old = omPitDao.findList(where);
		for (OmPit p : old) {
			omPitDao.delete(p);
		}
		List<OmPit> result = new ArrayList<>();
		int i = 0;
		for (Map<String, Object> m : list == null ? List.<Map<String, Object>>of() : list) {
			OmPit p = new OmPit();
			p.setProjectId(projectId);
			p.setName(str(m.get("name")));
			p.setBelong(str(m.get("belong")));
			p.setDepth(decimal(m.get("depth"), null));
			p.setRiskLevel(str(m.get("riskLevel")));
			p.setMonLevel(str(m.get("monLevel")));
			p.setStage(str(m.get("stage")));
			p.setPlan(str(m.get("plan")));
			p.setUnit(str(m.get("unit")));
			p.setQual(str(m.get("qual")));
			p.setLeader(str(m.get("leader")));
			p.setArgued(boolStr(m.get("argued")));
			p.setAccept(boolStr(m.get("accept")));
			p.setMonitor(boolStr(m.get("monitor")));
			p.setStatus(str(m.get("status")));
			p.setSortNo(i++);
			omPitDao.insert(p);
			result.add(p);
		}
		addLog(projectId, "填报", operator, "保存基坑工程清单", "共 " + result.size() + " 座");
		return result;
	}

	// ==================== 日常隐患 ====================

	public List<OmRisk> riskList(String projectId) {
		OmRisk where = new OmRisk();
		where.setProjectId(projectId);
		return omRiskDao.findList(where);
	}

	@Transactional
	public OmRisk riskSave(Map<String, Object> form, String operator) {
		OmRisk r = new OmRisk();
		r.setProjectId(str(form.get("projectId")));
		r.setRiskDate(str(form.get("riskDate")));
		r.setRiskType(str(form.get("riskType")));
		r.setPart(str(form.get("part")));
		r.setLevel(str(form.get("level")));
		r.setDueDate(str(form.get("dueDate")));
		r.setStatus(str(form.get("status")));
		r.setOwner(str(form.get("owner")));
		r.setRiskDesc(str(form.get("riskDesc")));
		omRiskDao.insert(r);
		addLog(r.getProjectId(), "填报", operator, "登记安全隐患", r.getRiskType() + " · " + r.getPart());
		return r;
	}

	@Transactional
	public OmRisk riskEdit(String id, Map<String, Object> form, String operator) {
		OmRisk r = omRiskDao.get(new OmRisk(id));
		if (r == null) {
			throw new IllegalArgumentException("隐患不存在");
		}
		r.setPart(str(form.get("part"), r.getPart()));
		r.setOwner(str(form.get("owner"), r.getOwner()));
		r.setRiskDesc(str(form.get("riskDesc"), r.getRiskDesc()));
		r.setStatus(str(form.get("status"), r.getStatus()));
		omRiskDao.update(r);
		addLog(r.getProjectId(), "填报", operator, "保存隐患整改记录", r.getRiskType() + " → " + r.getStatus());
		return r;
	}

	@Transactional
	public OmRisk riskClose(String id, String operator) {
		OmRisk r = omRiskDao.get(new OmRisk(id));
		if (r == null) {
			throw new IllegalArgumentException("隐患不存在");
		}
		r.setStatus("已整改");
		omRiskDao.update(r);
		addLog(r.getProjectId(), "填报", operator, "隐患闭环", r.getRiskType() + "（" + r.getPart() + "）");
		return r;
	}

	// ==================== 评分 ====================

	/** 评分页数据：勾选结果 + 计算得分 */
	public Map<String, Object> scoreGet(String projectId) {
		OmProject p = omProjectDao.get(new OmProject(projectId));
		if (p == null) {
			return null;
		}
		List<OmHazard> hazards = hazardList(projectId);
		List<OmPit> pits = pitList(projectId);
		ObjectNode review = (ObjectNode) OmScoreEngine.toJsonNode(p.getReviewJson());
		if (review == null || !review.isObject() || !review.has("haz")) {
			review = OmScoreEngine.autoSelections(p, hazards, pits);
		}
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("projectId", projectId);
		m.put("cat", p.getCat());
		m.put("review", OmScoreEngine.toMap(review));
		m.put("score", OmScoreEngine.score(review));
		m.put("hazards", hazards);
		m.put("pits", pits);
		return m;
	}

	/** 保存评分：后端按勾选结果重新计分并回写项目 */
	@Transactional
	public Map<String, Object> scoreSave(String projectId, JsonNode reviewNode, String operator) {
		ObjectNode review = (ObjectNode) reviewNode;
		OmProject p = omProjectDao.get(new OmProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在");
		}
		review.put("src", "manual");
		review.put("by", operator);
		review.put("time", now());
		Map<String, Object> score = OmScoreEngine.score(review);
		p.setReviewJson(OmScoreEngine.toJson(review));
		p.setReviewSrc("manual");
		p.setReviewBy(operator);
		p.setReviewTime(now());
		p.setScoreTotal((Integer) score.get("total"));
		p.setRiskLevel(String.valueOf(score.get("lv")));
		p.setCompleteness(completeness(p, hazardList(projectId), pitList(projectId)));
		omProjectDao.update(p);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> atts = (List<Map<String, Object>>) score.get("atts");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> pitAtts = (List<Map<String, Object>>) score.get("pitAtts");
		addLog(projectId, "评分", operator, "提交本月风险评分 " + score.get("total") + " 分（" + riskName(score.get("lv")) + "）",
				"四维得分：工程类别 " + score.get("d1") + " + 危大/超危大 " + score.get("d2")
						+ " + 基坑工程 " + score.get("d3") + " + 隐患排查 " + score.get("d4") + " = " + score.get("total")
						+ "；专项方案/论证附件 " + (atts.size() - ((List<?>) score.get("missing")).size()) + "/" + atts.size()
						+ " 套齐全；基坑附件 " + (pitAtts.size() - ((List<?>) score.get("pitMissing")).size()) + "/"
						+ pitAtts.size() + " 座齐全");
		return scoreGet(projectId);
	}

	/** 按台账重算勾选（不落库，仅返回预览） */
	public Map<String, Object> scoreReset(String projectId) {
		OmProject p = omProjectDao.get(new OmProject(projectId));
		if (p == null) {
			return null;
		}
		JsonNode review = OmScoreEngine.autoSelections(p, hazardList(projectId), pitList(projectId));
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("review", OmScoreEngine.toMap(review));
		m.put("score", OmScoreEngine.score(review));
		return m;
	}

	// ==================== 操作日志 ====================

	public List<OmLog> logList(String projectId) {
		OmLog where = new OmLog();
		if (StringUtils.isNotBlank(projectId)) {
			where.setProjectId(projectId);
		}
		return omLogDao.findList(where).stream()
				.sorted(Comparator.comparing(OmLog::getLogTime, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());
	}

	@Transactional
	public void addLog(String projectId, String cat, String operator, String action, String detail) {
		OmLog log = new OmLog();
		log.setProjectId(projectId);
		log.setLogCat(cat);
		log.setLogBy(operator);
		log.setAction(action);
		log.setDetail(detail);
		log.setLogTime(now());
		omLogDao.insert(log);
	}

	// ==================== 附件 ====================

	public List<OmFile> fileList(String projectId) {
		OmFile where = new OmFile();
		if (StringUtils.isNotBlank(projectId)) {
			where.setProjectId(projectId);
		}
		return omFileDao.findList(where);
	}

	/** 附件落盘并登记 */
	@Transactional
	public OmFile fileUpload(String projectId, String bizType, String bizKey, String attType, MultipartFile file, String operator) {
		String uploadDir = Global.getProperty("om4.upload-dir", System.getProperty("user.home") + "/jeesite-om4-upload");
		String month = new SimpleDateFormat("yyyyMM").format(new Date());
		File dir = new File(uploadDir, "om4/" + month);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("附件目录创建失败：" + dir);
		}
		String ext = "";
		String name = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
		int idx = name.lastIndexOf('.');
		if (idx > -1) {
			ext = name.substring(idx);
		}
		String path = "om4/" + month + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
		try {
			Files.copy(file.getInputStream(), Paths.get(uploadDir, path));
		} catch (IOException e) {
			throw new IllegalStateException("附件写入失败", e);
		}
		OmFile f = new OmFile();
		f.setProjectId(projectId);
		f.setBizType(bizType);
		f.setBizKey(bizKey);
		f.setAttType(attType);
		f.setFileName(name);
		f.setFilePath(path);
		f.setFileSize(file.getSize());
		omFileDao.insert(f);
		return f;
	}

	public OmFile fileGet(String id) {
		return omFileDao.get(new OmFile(id));
	}

	public Path filePath(OmFile f) {
		String uploadDir = Global.getProperty("om4.upload-dir", System.getProperty("user.home") + "/jeesite-om4-upload");
		return Paths.get(uploadDir, f.getFilePath());
	}

	@Transactional
	public void fileDelete(String id, String operator) {
		OmFile f = omFileDao.get(new OmFile(id));
		if (f == null) {
			return;
		}
		omFileDao.delete(f);
		try {
			Files.deleteIfExists(filePath(f));
		} catch (IOException ignored) {
		}
	}

	// ==================== 统计 ====================

	public Map<String, Object> stats() {
		List<OmProject> all = omProjectDao.findList(new OmProject());
		List<Map<String, Object>> views = all.stream().map(this::projectView).collect(Collectors.toList());
		int allCount = views.size();
		long building = views.stream().filter(v -> "在建".equals(v.get("buildStatus"))).count();
		long hazardSum = views.stream().mapToLong(v -> ((Number) v.get("hazardTotal")).longValue()).sum();
		long hazardSup = views.stream().mapToLong(v -> ((Number) v.get("hazardSup")).longValue()).sum();
		long red = views.stream().filter(v -> "1".equals(String.valueOf(v.get("riskLevel")))).count();
		int avgDone = views.isEmpty() ? 0 : (int) Math.round(views.stream().mapToInt(v -> (Integer) v.get("completeness")).average().orElse(0));
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("projectCount", allCount);
		m.put("buildingCount", building);
		m.put("hazardTotal", hazardSum);
		m.put("hazardSup", hazardSup);
		m.put("redCount", red);
		m.put("avgCompleteness", avgDone);
		m.put("needFixCount", views.stream().filter(v -> (Integer) v.get("completeness") < 100).count());
		m.put("districts", districtDist(views));
		m.put("cats", catDist(views));
		return m;
	}

	private List<Map<String, Object>> districtDist(List<Map<String, Object>> views) {
		Map<String, List<Map<String, Object>>> by = views.stream().collect(Collectors.groupingBy(v -> str(v.get("district"))));
		int max = by.values().stream().mapToInt(List::size).max().orElse(0);
		return by.entrySet().stream().map(e -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("name", e.getKey());
			m.put("count", e.getValue().size());
			m.put("max", max);
			Map<String, Long> lv = e.getValue().stream().collect(Collectors.groupingBy(
					v -> String.valueOf(v.get("riskLevel")), Collectors.counting()));
			m.put("levels", lv);
			return m;
		}).collect(Collectors.toList());
	}

	private List<Map<String, Object>> catDist(List<Map<String, Object>> views) {
		Map<String, Long> by = views.stream().collect(Collectors.groupingBy(v -> str(v.get("cat")), Collectors.counting()));
		long max = by.values().stream().mapToLong(Long::longValue).max().orElse(0);
		return by.entrySet().stream().map(e -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("name", e.getKey());
			m.put("count", e.getValue());
			m.put("max", max);
			return m;
		}).collect(Collectors.toList());
	}

	// ==================== 工具 ====================

	private static String now() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	}

	private static String riskName(Object lv) {
		return OmMeta.RISK_NAME.getOrDefault(Integer.valueOf(String.valueOf(lv)), "四级风险");
	}

	private static String str(Object v) {
		return v == null ? null : StringUtils.trim(v.toString());
	}

	private static String str(Object v, String def) {
		String s = str(v);
		return StringUtils.isBlank(s) ? def : s;
	}

	private static String str(Object v, Object def) {
		return str(v, def == null ? null : str(def));
	}

	private static String boolStr(Object v) {
		return (v instanceof Boolean && (Boolean) v) || "1".equals(str(v)) || "true".equalsIgnoreCase(str(v)) ? "1" : "0";
	}

	private static BigDecimal decimal(Object v, BigDecimal def) {
		String s = str(v);
		if (StringUtils.isBlank(s)) {
			return def;
		}
		try {
			return new BigDecimal(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static Integer intVal(Object v, Integer def) {
		String s = str(v);
		if (StringUtils.isBlank(s)) {
			return def == null ? 0 : def;
		}
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {
			return def == null ? 0 : def;
		}
	}

}
