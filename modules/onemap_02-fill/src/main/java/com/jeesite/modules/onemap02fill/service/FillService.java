package com.jeesite.modules.onemap02fill.service;

import com.jeesite.common.config.Global;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.onemap02fill.dao.FillArchiveDao;
import com.jeesite.modules.onemap02fill.dao.FillChgDao;
import com.jeesite.modules.onemap02fill.dao.FillFileDao;
import com.jeesite.modules.onemap02fill.dao.FillIssueDao;
import com.jeesite.modules.onemap02fill.dao.FillIssueReplyDao;
import com.jeesite.modules.onemap02fill.dao.FillNodeDao;
import com.jeesite.modules.onemap02fill.dao.FillNodeFbDao;
import com.jeesite.modules.onemap02fill.dao.FillProjectDao;
import com.jeesite.modules.onemap02fill.dao.FillSupDao;
import com.jeesite.modules.onemap02fill.dao.FillSupReplyDao;
import com.jeesite.modules.onemap02fill.dao.FillSyncLogDao;
import com.jeesite.modules.onemap02fill.entity.FillArchive;
import com.jeesite.modules.onemap02fill.entity.FillChg;
import com.jeesite.modules.onemap02fill.entity.FillFile;
import com.jeesite.modules.onemap02fill.entity.FillIssue;
import com.jeesite.modules.onemap02fill.entity.FillIssueReply;
import com.jeesite.modules.onemap02fill.entity.FillNode;
import com.jeesite.modules.onemap02fill.entity.FillNodeFb;
import com.jeesite.modules.onemap02fill.entity.FillProject;
import com.jeesite.modules.onemap02fill.entity.FillSup;
import com.jeesite.modules.onemap02fill.entity.FillSupReply;
import com.jeesite.modules.onemap02fill.entity.FillSyncLog;
import com.jeesite.modules.onemap02fill.util.FillConstants;
import com.jeesite.modules.onemap02fill.util.FillUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 填报系统核心服务
 * <p>
 * 覆盖项目基本信息、节点与反馈、前期审批修正、协调事项、督办台账、档案与附件、
 * 工作台看板与数据对接日志。状态口径与产品原型一致。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Service
public class FillService {

	private static final Logger logger = LoggerFactory.getLogger(FillService.class);

	/** 上传根目录 */
	@Value("${fill.upload-dir:${user.home}/jeesite-fill-upload}")
	private String uploadDir;

	private final FillProjectDao fillProjectDao;
	private final FillNodeDao fillNodeDao;
	private final FillNodeFbDao fillNodeFbDao;
	private final FillIssueDao fillIssueDao;
	private final FillIssueReplyDao fillIssueReplyDao;
	private final FillSupDao fillSupDao;
	private final FillSupReplyDao fillSupReplyDao;
	private final FillArchiveDao fillArchiveDao;
	private final FillFileDao fillFileDao;
	private final FillChgDao fillChgDao;
	private final FillSyncLogDao fillSyncLogDao;

	public FillService(FillProjectDao fillProjectDao, FillNodeDao fillNodeDao, FillNodeFbDao fillNodeFbDao,
			FillIssueDao fillIssueDao, FillIssueReplyDao fillIssueReplyDao, FillSupDao fillSupDao,
			FillSupReplyDao fillSupReplyDao, FillArchiveDao fillArchiveDao, FillFileDao fillFileDao,
			FillChgDao fillChgDao, FillSyncLogDao fillSyncLogDao) {
		this.fillProjectDao = fillProjectDao;
		this.fillNodeDao = fillNodeDao;
		this.fillNodeFbDao = fillNodeFbDao;
		this.fillIssueDao = fillIssueDao;
		this.fillIssueReplyDao = fillIssueReplyDao;
		this.fillSupDao = fillSupDao;
		this.fillSupReplyDao = fillSupReplyDao;
		this.fillArchiveDao = fillArchiveDao;
		this.fillFileDao = fillFileDao;
		this.fillChgDao = fillChgDao;
		this.fillSyncLogDao = fillSyncLogDao;
	}

	// ==================== 项目 ====================

	/**
	 * 项目列表（含节点完成数、超期数、填报完整度、附件数）
	 */
	public List<Map<String, Object>> projectList() {

		List<FillProject> projects = fillProjectDao.findList(new FillProject());
		List<FillNode> allNodes = fillNodeDao.findList(new FillNode());
		List<FillNodeFb> allFbs = fillNodeFbDao.findList(new FillNodeFb());
		List<FillFile> allFiles = fillFileDao.findList(new FillFile());

		Map<String, List<FillNode>> nodesByProject = allNodes.stream()
			.collect(Collectors.groupingBy(FillNode::getProjectId));
		Map<String, FillNodeFb> fbByNode = allFbs.stream()
			.collect(Collectors.toMap(FillNodeFb::getNodeId, fb -> fb, (a, b) -> a));
		Map<String, Long> filesByProject = allFiles.stream()
			.collect(Collectors.groupingBy(FillFile::getProjectId, Collectors.counting()));

		List<Map<String, Object>> list = new ArrayList<>();
		for (FillProject p : projects) {
			Map<String, Object> row = projectMap(p);
			List<FillNode> nodes = nodesByProject.getOrDefault(p.getId(), new ArrayList<>());
			List<FillNode> arr = nodes.stream()
				.filter(n -> StringUtils.equals(n.getStage(), p.getProjectStage()))
				.collect(Collectors.toList());
			int done = 0;
			int late = 0;
			for (FillNode n : arr) {
				String st = nodeStatus(n, fbByNode.get(n.getId()), FillUtils.today());
				if ("ok".equals(st) || "done".equals(st)) {
					done++;
				}
				if ("late".equals(st)) {
					late++;
				}
			}
			row.put("nodeDone", done);
			row.put("nodeTotal", arr.size());
			row.put("nodeLate", late);
			row.put("completeness", completeness(p, arr, fbByNode));
			row.put("fileCount", filesByProject.getOrDefault(p.getId(), 0L));
			list.add(row);
		}
		list.sort(Comparator.comparing(m -> String.valueOf(m.get("projectCode"))));
		return list;
	}

	/**
	 * 项目详情：基本信息 + 修改留痕 + 节点统计
	 */
	public Map<String, Object> projectGet(String projectId) {

		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null) {
			return null;
		}
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("project", projectMap(p));

		FillChg chg = new FillChg();
		chg.setProjectId(projectId);
		List<Map<String, Object>> chgs = fillChgDao.findList(chg).stream()
			.map(this::chgMap).collect(Collectors.toList());
		data.put("chgs", chgs);

		List<FillNode> nodes = nodesOfProject(projectId);
		Map<String, Object> stats = new LinkedHashMap<>();
		for (String stage : List.of(FillConstants.STAGE_PRE, FillConstants.STAGE_DEV, FillConstants.STAGE_OPS)) {
			List<FillNode> arr = nodes.stream().filter(n -> stage.equals(n.getStage())).collect(Collectors.toList());
			int done = 0;
			int late = 0;
			for (FillNode n : arr) {
				FillNodeFb fb = fbOfNode(n.getId());
				String st = FillConstants.STAGE_PRE.equals(stage) ? preStatus(n) : nodeStatus(n, fb, FillUtils.today());
				if ("ok".equals(st) || "done".equals(st)) {
					done++;
				}
				if ("late".equals(st)) {
					late++;
				}
			}
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("total", arr.size());
			item.put("done", done);
			item.put("late", late);
			stats.put(stage, item);
		}
		data.put("nodeStats", stats);
		data.put("completeness", completeness(p, currentStageNodes(p, nodes), fbByNodeMap()));
		return data;
	}

	/**
	 * 保存项目基本信息（仅允许白名单字段，修改留痕）
	 */
	@Transactional
	public void projectSave(String projectId, Map<String, Object> form, String role) {

		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在：" + projectId);
		}
		String operator = operator(role);
		List<Map<String, Object>> changes = new ArrayList<>();
		for (Map.Entry<String, Object> e : form.entrySet()) {
			String key = e.getKey();
			String label = FIELD_LABELS.get(key);
			if (label == null) {
				continue;
			}
			String newValue = e.getValue() == null ? "" : String.valueOf(e.getValue()).trim();
			String oldValue = StringUtils.defaultIfBlank(getFieldValue(p, key), "");
			if (StringUtils.equals(oldValue, newValue)) {
				continue;
			}
			setFieldValue(p, key, newValue);
			Map<String, Object> chg = new LinkedHashMap<>();
			chg.put("fieldLabel", label);
			chg.put("oldValue", oldValue.isEmpty() ? "（空）" : oldValue);
			chg.put("newValue", newValue.isEmpty() ? "（空）" : newValue);
			changes.add(chg);
		}
		if (!changes.isEmpty()) {
			p.setUpdateBy(operator);
			p.sqlMap().updateNullValue();
			fillProjectDao.update(p);
			saveChanges(projectId, changes, operator);
		}
	}

	/** 基本信息字段标签 */
	private static final Map<String, String> FIELD_LABELS = new HashMap<>();
	static {
		FIELD_LABELS.put("projectName", "项目名称");
		FIELD_LABELS.put("projectCode", "项目代码");
		FIELD_LABELS.put("projectType", "项目类别");
		FIELD_LABELS.put("district", "所属区 / 片区");
		FIELD_LABELS.put("projectStage", "项目阶段");
		FIELD_LABELS.put("place", "建设地点");
		FIELD_LABELS.put("scopeDesc", "建设规模及内容");
		FIELD_LABELS.put("investTotal", "总投资（亿元）");
		FIELD_LABELS.put("investDone", "已完成投资（亿元）");
		FIELD_LABELS.put("yearPlan", "年度计划投资（亿元）");
		FIELD_LABELS.put("fundSource", "资金来源");
		FIELD_LABELS.put("progressPct", "形象进度（%）");
		FIELD_LABELS.put("planStart", "计划开工时间");
		FIELD_LABELS.put("planEnd", "计划竣工时间");
		FIELD_LABELS.put("actStart", "实际开工时间");
		FIELD_LABELS.put("masterName", "项目负责人");
		FIELD_LABELS.put("masterTel", "联系电话");
		FIELD_LABELS.put("ownerUnit", "业主单位");
		FIELD_LABELS.put("buildUnit", "建设单位");
		FIELD_LABELS.put("contractor", "施工单位");
		FIELD_LABELS.put("supervisor", "监理单位");
		FIELD_LABELS.put("safetyOrg", "质量安全监督机构");
		FIELD_LABELS.put("geomType", "空间几何类型");
		FIELD_LABELS.put("centerLnglat", "中心坐标（纬度, 经度）");
		FIELD_LABELS.put("scaleDesc", "建设规模");
	}

	private String getFieldValue(FillProject p, String key) {
		return switch (key) {
			case "projectName" -> p.getProjectName();
			case "projectCode" -> p.getProjectCode();
			case "projectType" -> p.getProjectType();
			case "district" -> p.getDistrict();
			case "projectStage" -> p.getProjectStage();
			case "place" -> p.getPlace();
			case "scopeDesc" -> p.getScopeDesc();
			case "investTotal" -> p.getInvestTotal() == null ? null : String.valueOf(p.getInvestTotal());
			case "investDone" -> p.getInvestDone() == null ? null : String.valueOf(p.getInvestDone());
			case "yearPlan" -> p.getYearPlan() == null ? null : String.valueOf(p.getYearPlan());
			case "fundSource" -> p.getFundSource();
			case "progressPct" -> p.getProgressPct() == null ? null : String.valueOf(p.getProgressPct());
			case "planStart" -> p.getPlanStart();
			case "planEnd" -> p.getPlanEnd();
			case "actStart" -> p.getActStart();
			case "masterName" -> p.getMasterName();
			case "masterTel" -> p.getMasterTel();
			case "ownerUnit" -> p.getOwnerUnit();
			case "buildUnit" -> p.getBuildUnit();
			case "contractor" -> p.getContractor();
			case "supervisor" -> p.getSupervisor();
			case "safetyOrg" -> p.getSafetyOrg();
			case "geomType" -> p.getGeomType();
			case "centerLnglat" -> p.getCenterLnglat();
			case "scaleDesc" -> p.getScaleDesc();
			default -> null;
		};
	}

	private void setFieldValue(FillProject p, String key, String value) {
		switch (key) {
			case "projectName" -> p.setProjectName(value);
			case "projectCode" -> p.setProjectCode(value);
			case "projectType" -> p.setProjectType(value);
			case "district" -> p.setDistrict(value);
			case "projectStage" -> p.setProjectStage(value);
			case "place" -> p.setPlace(value);
			case "scopeDesc" -> p.setScopeDesc(value);
			case "investTotal" -> p.setInvestTotal(parseDouble(value));
			case "investDone" -> p.setInvestDone(parseDouble(value));
			case "yearPlan" -> p.setYearPlan(parseDouble(value));
			case "fundSource" -> p.setFundSource(value);
			case "progressPct" -> p.setProgressPct(parseDouble(value));
			case "planStart" -> p.setPlanStart(value);
			case "planEnd" -> p.setPlanEnd(value);
			case "actStart" -> p.setActStart(value);
			case "masterName" -> p.setMasterName(value);
			case "masterTel" -> p.setMasterTel(value);
			case "ownerUnit" -> p.setOwnerUnit(value);
			case "buildUnit" -> p.setBuildUnit(value);
			case "contractor" -> p.setContractor(value);
			case "supervisor" -> p.setSupervisor(value);
			case "safetyOrg" -> p.setSafetyOrg(value);
			case "geomType" -> p.setGeomType(value);
			case "centerLnglat" -> p.setCenterLnglat(value);
			case "scaleDesc" -> p.setScaleDesc(value);
			default -> {
			}
		}
	}

	private Double parseDouble(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void saveChanges(String projectId, List<Map<String, Object>> changes, String operator) {
		String now = FillUtils.nowText();
		for (Map<String, Object> c : changes) {
			FillChg chg = new FillChg();
			chg.setProjectId(projectId);
			chg.setFieldLabel(String.valueOf(c.get("fieldLabel")));
			chg.setOldValue(String.valueOf(c.get("oldValue")));
			chg.setNewValue(String.valueOf(c.get("newValue")));
			chg.setChgTime(new Date());
			chg.setChgBy(operator);
			chg.setNote(now);
			chg.setUpdateBy(operator);
			chg.setIsNewRecord(true);
			fillChgDao.insert(chg);
		}
	}

	// ==================== 节点 ====================

	/**
	 * 节点列表（含反馈、附件与计算状态）
	 */
	public List<Map<String, Object>> nodeList(String projectId, String stage) {

		List<FillNode> nodes = nodesOfProject(projectId);
		if (StringUtils.isNotBlank(stage)) {
			nodes = nodes.stream().filter(n -> stage.equals(n.getStage())).collect(Collectors.toList());
		}
		Map<String, FillNodeFb> fbMap = fbByNodeMap();
		Map<String, List<FillFile>> fileMap = filesOfProject(projectId);
		List<Map<String, Object>> list = new ArrayList<>();
		for (FillNode n : nodes) {
			FillNodeFb fb = fbMap.get(n.getId());
			List<FillFile> files = new ArrayList<>();
			List<FillFile> preFiles = new ArrayList<>();
			List<FillFile> fbFiles = new ArrayList<>();
			if (fileMap.containsKey("pre:" + n.getId())) {
				preFiles.addAll(fileMap.get("pre:" + n.getId()));
				files.addAll(preFiles);
			}
			if (fileMap.containsKey("node:" + n.getId())) {
				files.addAll(fileMap.get("node:" + n.getId()));
			}
			if (fb != null && fileMap.containsKey("node_fb:" + n.getId())) {
				fbFiles.addAll(fileMap.get("node_fb:" + n.getId()));
				files.addAll(fbFiles);
			}
			list.add(nodeMap(n, fb, files, preFiles, fbFiles));
		}
		return list;
	}

	/**
	 * 新增 / 编辑建设或运维节点
	 */
	@Transactional
	public String nodeSave(Map<String, Object> form, String role) {

		String id = str(form.get("id"));
		String projectId = str(form.get("projectId"));
		String stage = str(form.get("stage"));
		if (StringUtils.isBlank(projectId) || StringUtils.isBlank(stage)) {
			throw new IllegalArgumentException("项目与阶段不能为空");
		}
		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在：" + projectId);
		}
		String nodeName = str(form.get("nodeName"));
		String startDate = str(form.get("startDate"));
		String endDate = str(form.get("endDate"));
		String planContent = str(form.get("planContent"));
		if (StringUtils.isBlank(nodeName)) {
			throw new IllegalArgumentException("请填写节点名称");
		}
		if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
			throw new IllegalArgumentException("请填写节点开始与结束时间");
		}
		if (FillUtils.parseDay(startDate).isAfter(FillUtils.parseDay(endDate))) {
			throw new IllegalArgumentException("开始时间不能晚于结束时间");
		}
		if (StringUtils.isBlank(planContent)) {
			throw new IllegalArgumentException("请填写计划完成内容");
		}
		String operator = operator(role);
		FillNode node;
		if (StringUtils.isNotBlank(id)) {
			node = fillNodeDao.get(new FillNode(id));
			if (node == null) {
				throw new IllegalArgumentException("节点不存在：" + id);
			}
		} else {
			node = new FillNode();
			node.setProjectId(projectId);
			node.setStage(stage);
			node.setSource("fill");
			node.setSortNo(nextSortNo(projectId, stage));
			node.setUpdateBy(operator);
			node.setIsNewRecord(true);
		}
		node.setNodeName(nodeName);
		node.setShortName(StringUtils.defaultIfBlank(str(form.get("shortName")), nodeName.substring(0, Math.min(8, nodeName.length()))));
		node.setPlanContent(planContent);
		node.setStartDate(FillUtils.toDate(FillUtils.parseDay(startDate)));
		node.setEndDate(FillUtils.toDate(FillUtils.parseDay(endDate)));
		if (node.getIsNewRecord()) {
			fillNodeDao.insert(node);
		} else {
			node.setUpdateBy(operator);
			node.sqlMap().updateNullValue();
			fillNodeDao.update(node);
		}
		return node.getId();
	}

	/**
	 * 删除节点（含反馈与附件记录）
	 */
	@Transactional
	public void nodeDelete(String nodeId) {

		FillNodeFb fb = fbOfNode(nodeId);
		if (fb != null) {
			deleteFiles("node_fb", fb.getId());
			fillNodeFbDao.delete(fb);
		}
		deleteFiles("node", nodeId);
		deleteFiles("pre", nodeId);
		fillNodeDao.delete(new FillNode(nodeId));
	}

	/**
	 * 节点上移 / 下移
	 */
	@Transactional
	public void nodeMove(String nodeId, int direction) {

		FillNode node = fillNodeDao.get(new FillNode(nodeId));
		if (node == null) {
			throw new IllegalArgumentException("节点不存在：" + nodeId);
		}
		List<FillNode> nodes = fillNodeDao.findList(new FillNode()).stream()
			.filter(n -> StringUtils.equals(n.getProjectId(), node.getProjectId())
				&& StringUtils.equals(n.getStage(), node.getStage()))
			.sorted(Comparator.comparingInt(n -> n.getSortNo() == null ? 0 : n.getSortNo()))
			.collect(Collectors.toList());
		int idx = -1;
		for (int i = 0; i < nodes.size(); i++) {
			if (StringUtils.equals(nodes.get(i).getId(), nodeId)) {
				idx = i;
				break;
			}
		}
		int target = idx + direction;
		if (idx < 0 || target < 0 || target >= nodes.size()) {
			return;
		}
		FillNode other = nodes.get(target);
		int tmp = node.getSortNo() == null ? 0 : node.getSortNo();
		node.setSortNo(other.getSortNo() == null ? 0 : other.getSortNo());
		other.setSortNo(tmp);
		fillNodeDao.update(node);
		fillNodeDao.update(other);
	}

	/**
	 * 保存节点期内反馈（每节点最新一条）
	 */
	@Transactional
	public void nodeFbSave(Map<String, Object> form, String role) {

		String nodeId = str(form.get("nodeId"));
		FillNode node = fillNodeDao.get(new FillNode(nodeId));
		if (node == null) {
			throw new IllegalArgumentException("节点不存在：" + nodeId);
		}
		String fbDate = str(form.get("fbDate"));
		String fbText = str(form.get("fbText"));
		if (StringUtils.isBlank(fbDate)) {
			throw new IllegalArgumentException("请选择反馈时间");
		}
		if (StringUtils.isBlank(fbText)) {
			throw new IllegalArgumentException("请填写完成情况说明");
		}
		boolean done = "1".equals(str(form.get("doneFlag"))) || "true".equals(str(form.get("doneFlag")));
		double pct = done ? 100 : parseDouble(str(form.get("pct")));
		pct = Math.max(0, Math.min(100, pct));
		String operator = operator(role);

		FillNodeFb fb = fbOfNode(nodeId);
		if (fb == null) {
			fb = new FillNodeFb();
			fb.setNodeId(nodeId);
			fb.setUpdateBy(operator);
			fb.setIsNewRecord(true);
		} else {
			fb.setUpdateBy(operator);
		}
		fb.setFbDate(FillUtils.toDate(FillUtils.parseDay(fbDate)));
		fb.setDoneFlag(done ? "1" : "0");
		fb.setPct(pct);
		fb.setFbText(fbText);
		fb.setFbBy(operator);
		fb.setFbTime(new Date());
		if (fb.getIsNewRecord()) {
			fillNodeFbDao.insert(fb);
		} else {
			fillNodeFbDao.update(fb);
		}
		syncFiles("node_fb", fb.getId(), node.getProjectId(), operator, list(form.get("files")));
		// 形象进度联动：节点反馈完成时按节点权重刷新项目形象进度
		updateProjectProgress(node.getProjectId());
	}

	/**
	 * 修正前期审批环节
	 */
	@Transactional
	public void preSave(Map<String, Object> form, String role) {

		String nodeId = str(form.get("nodeId"));
		FillNode node = fillNodeDao.get(new FillNode(nodeId));
		if (node == null) {
			throw new IllegalArgumentException("节点不存在：" + nodeId);
		}
		String planDate = str(form.get("planDate"));
		String actDate = str(form.get("actDate"));
		String status = str(form.get("nodeStatus"));
		String docNo = str(form.get("docNo"));
		if (StringUtils.isBlank(planDate)) {
			throw new IllegalArgumentException("请填写计划完成时间");
		}
		if ("done".equals(status) && StringUtils.isBlank(actDate)) {
			throw new IllegalArgumentException("办理状态为已完成时，需填写实际完成时间");
		}
		String old = String.valueOf(node.getPlanDate()) + "|" + String.valueOf(node.getActDate()) + "|" + node.getDocNo();
		node.setPlanDate(FillUtils.toDate(FillUtils.parseDay(planDate)));
		node.setActDate(FillUtils.toDate(FillUtils.parseDay(actDate)));
		node.setNodeStatus(status);
		node.setDocNo(docNo);
		node.setUpdateBy(operator(role));
		fillNodeDao.update(node);

		syncFiles("pre", nodeId, node.getProjectId(), operator(role), list(form.get("files")));

		String now = String.valueOf(node.getPlanDate()) + "|" + String.valueOf(node.getActDate()) + "|" + node.getDocNo();
		if (!StringUtils.equals(old, now)) {
			List<Map<String, Object>> changes = new ArrayList<>();
			Map<String, Object> c = new LinkedHashMap<>();
			c.put("fieldLabel", "前期 · " + StringUtils.defaultIfBlank(node.getShortName(), node.getNodeName()));
			c.put("oldValue", old.replace("|", " / "));
			c.put("newValue", now.replace("|", " / "));
			changes.add(c);
			saveChanges(node.getProjectId(), changes, operator(role));
		}
	}

	/** 按节点反馈刷新项目形象进度（简单加权：已完成节点占比） */
	private void updateProjectProgress(String projectId) {

		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null || !FillConstants.STAGE_DEV.equals(p.getProjectStage())) {
			return;
		}
		List<FillNode> nodes = nodesOfProject(projectId).stream()
			.filter(n -> FillConstants.STAGE_DEV.equals(n.getStage())).collect(Collectors.toList());
		if (nodes.isEmpty()) {
			return;
		}
		Map<String, FillNodeFb> fbMap = fbByNodeMap();
		int done = 0;
		double pctSum = 0;
		for (FillNode n : nodes) {
			FillNodeFb fb = fbMap.get(n.getId());
			if (fb != null) {
				pctSum += fb.getPct() == null ? 0 : fb.getPct();
				if ("1".equals(fb.getDoneFlag())) {
					done++;
				}
			}
		}
		double progress = Math.min(100, Math.round(pctSum / nodes.size()));
		if (progress > (p.getProgressPct() == null ? 0 : p.getProgressPct())) {
			p.setProgressPct(progress);
			p.setUpdateBy("system");
			fillProjectDao.update(p);
		}
	}

	// ==================== 协调事项 ====================

	/**
	 * 协调事项列表（含回复与附件）
	 */
	public List<Map<String, Object>> issueList(String projectId) {

		FillIssue where = new FillIssue();
		if (StringUtils.isNotBlank(projectId)) {
			where.setProjectId(projectId);
		}
		List<FillIssue> issues = fillIssueDao.findList(where);
		Map<String, List<FillIssueReply>> replies = fillIssueReplyDao.findList(new FillIssueReply()).stream()
			.collect(Collectors.groupingBy(FillIssueReply::getIssueId));
		Map<String, List<FillFile>> files = fillFileDao.findList(new FillFile()).stream()
			.collect(Collectors.groupingBy(FillFile::getBizId));
		List<Map<String, Object>> list = new ArrayList<>();
		for (FillIssue x : issues) {
			Map<String, Object> row = issueMap(x);
			List<Map<String, Object>> replyRows = new ArrayList<>();
			for (FillIssueReply r : replies.getOrDefault(x.getId(), new ArrayList<>())) {
				Map<String, Object> rm = issueReplyMap(r);
				rm.put("files", fileMaps(files.getOrDefault(r.getId(), new ArrayList<>())));
				replyRows.add(rm);
			}
			row.put("replies", replyRows);
			row.put("files", fileMaps(files.getOrDefault(x.getId(), new ArrayList<>())));
			list.add(row);
		}
		return list;
	}

	/**
	 * 提交协调事项
	 */
	@Transactional
	public String issueSave(Map<String, Object> form, String role) {

		String projectId = str(form.get("projectId"));
		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在：" + projectId);
		}
		String title = str(form.get("title"));
		String descText = str(form.get("descText"));
		String dept = str(form.get("dept"));
		if (StringUtils.isBlank(title)) {
			throw new IllegalArgumentException("请填写事项标题");
		}
		if (StringUtils.isBlank(descText)) {
			throw new IllegalArgumentException("请填写事项描述");
		}
		if (StringUtils.isBlank(dept)) {
			throw new IllegalArgumentException("请填写涉及单位");
		}
		String operator = operator(role);
		String issueNo = "XTS" + StringUtils.substring(projectId, 2) + "-"
			+ String.format("%02d", countIssues(projectId) + 1);
		FillIssue x = new FillIssue();
		x.setProjectId(projectId);
		x.setIssueNo(issueNo);
		x.setTitle(title);
		x.setIssueType(str(form.get("issueType")));
		x.setUrgent(StringUtils.defaultIfBlank(str(form.get("urgent")), "一般"));
		x.setDept(dept);
		x.setDescText(descText);
		LocalDate issueDate = FillUtils.parseDay(str(form.get("issueDate")));
		x.setIssueDate(FillUtils.toDate(issueDate == null ? FillUtils.today() : issueDate));
		x.setExpectDate(FillUtils.toDate(FillUtils.parseDay(str(form.get("expectDate")))));
		x.setIssueStatus("待受理");
		x.setIssueBy(operator);
		x.setSource("fill");
		x.setUpdateBy(operator);
		x.setIsNewRecord(true);
		fillIssueDao.insert(x);
		attachFiles("issue", x.getId(), projectId, operator, list(form.get("files")));
		return x.getId();
	}

	/**
	 * 住更局回复协调事项
	 */
	@Transactional
	public void issueReply(Map<String, Object> form, String role) {

		String issueId = str(form.get("issueId"));
		FillIssue x = fillIssueDao.get(new FillIssue(issueId));
		if (x == null) {
			throw new IllegalArgumentException("协调事项不存在：" + issueId);
		}
		String text = str(form.get("replyText"));
		if (StringUtils.isBlank(text)) {
			throw new IllegalArgumentException("请填写回复内容");
		}
		String operator = operator(role);
		FillIssueReply r = new FillIssueReply();
		r.setIssueId(issueId);
		r.setReplyRole("gov");
		r.setReplyWho(operator);
		r.setReplyDept("市住更局 市政处");
		r.setReplyDate(FillUtils.toDate(FillUtils.today()));
		r.setReplyText(text);
		r.setUpdateBy(operator);
		r.setIsNewRecord(true);
		fillIssueReplyDao.insert(r);
		attachFiles("issue_reply", r.getId(), x.getProjectId(), operator, list(form.get("files")));

		if (StringUtils.equalsAny(x.getIssueStatus(), "待受理", "已超期")) {
			x.setIssueStatus("协调中");
			x.setUpdateBy(operator);
			fillIssueDao.update(x);
		}
	}

	/**
	 * 建设单位补充材料
	 */
	@Transactional
	public void issueSupplement(Map<String, Object> form, String role) {

		String issueId = str(form.get("issueId"));
		FillIssue x = fillIssueDao.get(new FillIssue(issueId));
		if (x == null) {
			throw new IllegalArgumentException("协调事项不存在：" + issueId);
		}
		List<Map<String, Object>> files = list(form.get("files"));
		if (files.isEmpty()) {
			throw new IllegalArgumentException("请先选择要上传的材料");
		}
		String operator = operator(role);
		FillIssueReply r = new FillIssueReply();
		r.setIssueId(issueId);
		r.setReplyRole("owner");
		r.setReplyWho(operator);
		r.setReplyDept("建设单位");
		r.setReplyDate(FillUtils.toDate(FillUtils.today()));
		r.setReplyText("补充提交材料，详见附件。");
		r.setUpdateBy(operator);
		r.setIsNewRecord(true);
		fillIssueReplyDao.insert(r);
		attachFiles("issue_reply", r.getId(), x.getProjectId(), operator, files);
	}

	/**
	 * 住更局办结协调事项
	 */
	@Transactional
	public void issueClose(String issueId, String role) {

		FillIssue x = fillIssueDao.get(new FillIssue(issueId));
		if (x == null) {
			throw new IllegalArgumentException("协调事项不存在：" + issueId);
		}
		x.setIssueStatus("已办结");
		x.setUpdateBy(operator(role));
		fillIssueDao.update(x);
	}

	// ==================== 督办台账 ====================

	/**
	 * 督办列表（含回复与附件）
	 */
	public List<Map<String, Object>> supList(String projectId) {

		FillSup where = new FillSup();
		if (StringUtils.isNotBlank(projectId)) {
			where.setProjectId(projectId);
		}
		List<FillSup> sups = fillSupDao.findList(where);
		Map<String, FillSupReply> replies = fillSupReplyDao.findList(new FillSupReply()).stream()
			.collect(Collectors.toMap(FillSupReply::getSupId, r -> r, (a, b) -> a));
		Map<String, List<FillFile>> files = fillFileDao.findList(new FillFile()).stream()
			.collect(Collectors.groupingBy(FillFile::getBizId));
		List<Map<String, Object>> list = new ArrayList<>();
		for (FillSup x : sups) {
			Map<String, Object> row = supMap(x);
			FillSupReply r = replies.get(x.getId());
			Map<String, Object> replyMap = null;
			if (r != null) {
				replyMap = supReplyMap(r);
				replyMap.put("files", fileMaps(files.getOrDefault(r.getId(), new ArrayList<>())));
			}
			row.put("reply", replyMap);
			row.put("files", fileMaps(files.getOrDefault(x.getId(), new ArrayList<>())));
			list.add(row);
		}
		return list;
	}

	/**
	 * 住更局发出督办
	 */
	@Transactional
	public String supSave(Map<String, Object> form, String role) {

		String projectId = str(form.get("projectId"));
		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在：" + projectId);
		}
		String title = str(form.get("title"));
		String content = str(form.get("contentText"));
		if (StringUtils.isBlank(title)) {
			throw new IllegalArgumentException("请填写督办事项");
		}
		if (StringUtils.isBlank(content)) {
			throw new IllegalArgumentException("请填写督办内容");
		}
		String operator = operator(role);
		String supNo = "督办〔2026〕" + String.format("%03d", countSups(projectId) + 90) + " 号";
		FillSup x = new FillSup();
		x.setProjectId(projectId);
		x.setSupNo(supNo);
		x.setTitle(title);
		x.setSupType(str(form.get("supType")));
		x.setSupLevel(StringUtils.defaultIfBlank(str(form.get("supLevel")), "一般"));
		x.setContentText(content);
		x.setSupDate(FillUtils.toDate(FillUtils.today()));
		x.setDueDate(FillUtils.toDate(FillUtils.parseDay(str(form.get("dueDate")))));
		x.setSupStatus("待回复");
		x.setSupBy(operator);
		x.setUpdateBy(operator);
		x.setIsNewRecord(true);
		fillSupDao.insert(x);
		attachFiles("sup", x.getId(), projectId, operator, list(form.get("files")));
		return x.getId();
	}

	/**
	 * 建设单位回复督办
	 */
	@Transactional
	public void supReply(Map<String, Object> form, String role) {

		String supId = str(form.get("supId"));
		FillSup x = fillSupDao.get(new FillSup(supId));
		if (x == null) {
			throw new IllegalArgumentException("督办不存在：" + supId);
		}
		String text = str(form.get("replyText"));
		if (StringUtils.isBlank(text)) {
			throw new IllegalArgumentException("请填写整改回复内容");
		}
		String operator = operator(role);
		FillSupReply where = new FillSupReply();
		where.setSupId(supId);
		List<FillSupReply> exists = fillSupReplyDao.findList(where);
		FillSupReply r;
		if (exists.isEmpty()) {
			r = new FillSupReply();
			r.setSupId(supId);
			r.setUpdateBy(operator);
			r.setIsNewRecord(true);
		} else {
			// 已有回复则覆盖更新，避免重复回复产生多条记录
			r = exists.get(0);
			deleteFiles("sup_reply", r.getId());
			r.setUpdateBy(operator);
		}
		r.setReplyDate(FillUtils.toDate(FillUtils.today()));
		r.setReplyBy(operator);
		r.setReplyText(text);
		if (r.getIsNewRecord()) {
			fillSupReplyDao.insert(r);
		} else {
			fillSupReplyDao.update(r);
		}
		attachFiles("sup_reply", r.getId(), x.getProjectId(), operator, list(form.get("files")));

		x.setSupStatus("已回复");
		x.setUpdateBy(operator);
		fillSupDao.update(x);
	}

	/**
	 * 住更局审核闭环
	 */
	@Transactional
	public void supClose(String supId, String role) {

		FillSup x = fillSupDao.get(new FillSup(supId));
		if (x == null) {
			throw new IllegalArgumentException("督办不存在：" + supId);
		}
		x.setSupStatus("已闭环");
		x.setUpdateBy(operator(role));
		fillSupDao.update(x);
	}

	// ==================== 档案与附件 ====================

	/**
	 * 自主归档列表
	 */
	public List<Map<String, Object>> archiveList(String projectId) {

		FillArchive where = new FillArchive();
		where.setProjectId(projectId);
		List<FillArchive> archives = fillArchiveDao.findList(where);
		Map<String, List<FillFile>> files = fillFileDao.findList(new FillFile()).stream()
			.filter(f -> "archive".equals(f.getBizType()))
			.collect(Collectors.groupingBy(FillFile::getBizId));
		List<Map<String, Object>> list = new ArrayList<>();
		for (FillArchive a : archives) {
			Map<String, Object> row = archiveMap(a);
			row.put("files", fileMaps(files.getOrDefault(a.getId(), new ArrayList<>())));
			list.add(row);
		}
		return list;
	}

	/**
	 * 上传归档（一次可多个文件，统一归入同一档案类别）
	 */
	@Transactional
	public List<String> archiveSave(Map<String, Object> form, String role) {

		String projectId = str(form.get("projectId"));
		FillProject p = fillProjectDao.get(new FillProject(projectId));
		if (p == null) {
			throw new IllegalArgumentException("项目不存在：" + projectId);
		}
		List<Map<String, Object>> files = list(form.get("files"));
		if (files.isEmpty()) {
			throw new IllegalArgumentException("请先选择要归档的文件");
		}
		String operator = operator(role);
		String cat = str(form.get("cat"));
		String title = str(form.get("title"));
		String note = str(form.get("note"));
		List<String> ids = new ArrayList<>();
		for (Map<String, Object> f : files) {
			FillArchive a = new FillArchive();
			a.setProjectId(projectId);
			a.setCat(cat);
			a.setTitle(StringUtils.defaultIfBlank(title, str(f.get("name"))));
			a.setNote(note);
			a.setArchiveDate(FillUtils.toDate(FillUtils.today()));
			a.setArchiveBy(operator);
			a.setUpdateBy(operator);
			a.setIsNewRecord(true);
			fillArchiveDao.insert(a);
			attachFiles("archive", a.getId(), projectId, operator, List.of(f));
			ids.add(a.getId());
		}
		return ids;
	}

	/**
	 * 删除自主归档（含附件记录）
	 */
	@Transactional
	public void archiveDelete(String archiveId) {

		deleteFiles("archive", archiveId);
		fillArchiveDao.delete(new FillArchive(archiveId));
	}

	/**
	 * 项目全部附件（含归属信息）
	 */
	public List<Map<String, Object>> fileList(String projectId) {

		FillFile where = new FillFile();
		where.setProjectId(projectId);
		List<FillFile> files = fillFileDao.findList(where);
		Map<String, String> nodeNames = nodesOfProject(projectId).stream()
			.collect(Collectors.toMap(FillNode::getId, FillNode::getNodeName, (a, b) -> a));
		Map<String, String> issueTitles = fillIssueDao.findList(new FillIssue()).stream()
			.collect(Collectors.toMap(FillIssue::getId, FillIssue::getTitle, (a, b) -> a));
		Map<String, String> supTitles = fillSupDao.findList(new FillSup()).stream()
			.collect(Collectors.toMap(FillSup::getId, FillSup::getTitle, (a, b) -> a));
		Map<String, String> archiveTitles = fillArchiveDao.findList(new FillArchive()).stream()
			.collect(Collectors.toMap(FillArchive::getId, FillArchive::getTitle, (a, b) -> a));

		List<Map<String, Object>> list = new ArrayList<>();
		for (FillFile f : files) {
			Map<String, Object> row = fileMap(f);
			row.put("belongType", belongType(f.getBizType()));
			row.put("belongName", belongName(f, nodeNames, issueTitles, supTitles, archiveTitles));
			list.add(row);
		}
		list.sort((a, b) -> String.valueOf(b.get("uploadDate")).compareTo(String.valueOf(a.get("uploadDate"))));
		return list;
	}

	/**
	 * 上传文件：落盘到 upload-dir，返回元数据（不建业务记录）
	 */
	public Map<String, Object> fileUpload(MultipartFile file) {

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("请选择要上传的文件");
		}
		String original = StringUtils.defaultIfBlank(file.getOriginalFilename(), "file");
		String ext = StringUtils.substringAfterLast(original, ".");
		if (StringUtils.isBlank(ext)) {
			ext = "bin";
		}
		ext = ext.toLowerCase();
		String id = UUID.randomUUID().toString().replace("-", "");
		String relative = "fill/" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
			+ "/" + id + "." + ext;
		Path target = Paths.get(uploadDir, relative);
		try {
			Files.createDirectories(target.getParent());
			file.transferTo(target.toFile());
		} catch (IOException e) {
			throw new IllegalStateException("文件保存失败：" + e.getMessage(), e);
		}
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("id", id);
		meta.put("name", original);
		meta.put("ext", ext);
		meta.put("size", file.getSize());
		meta.put("kind", FillUtils.kindOf(ext));
		meta.put("url", fileUrl(id));
		return meta;
	}

	/**
	 * 下载 / 预览附件
	 */
	public void fileDownload(String id, HttpServletResponse response) {

		FillFile f = fillFileDao.get(new FillFile(id));
		Path path = null;
		if (f != null && StringUtils.isNotBlank(f.getFilePath())) {
			path = Paths.get(uploadDir, f.getFilePath());
		}
		// 上传后未落库的临时文件：按 id 在上传目录中回查，保证预览可用
		if (path == null || !Files.exists(path)) {
			path = findFileById(id);
		}
		if (path == null || !Files.exists(path)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String fileName = f == null ? path.getFileName().toString() : f.getFileName();
		String kind = f == null ? FillUtils.kindOf(StringUtils.substringAfterLast(fileName, ".")) : f.getFileKind();
		response.setContentType(contentType(kind));
		String encoded = URLEncoder.encode(StringUtils.defaultIfBlank(fileName, "file"), StandardCharsets.UTF_8)
			.replace("+", "%20");
		response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encoded);
		try (OutputStream out = response.getOutputStream()) {
			Files.copy(path, out);
			out.flush();
		} catch (IOException e) {
			logger.warn("[填报] 附件下载失败：{}", e.getMessage());
		}
	}

	/**
	 * 在上传目录中按文件ID回查（文件名格式：{id}.{ext}）
	 */
	private Path findFileById(String id) {
		Path root = Paths.get(uploadDir, "fill");
		if (!Files.exists(root)) {
			return null;
		}
		try (java.util.stream.Stream<Path> stream = Files.walk(root)) {
			return stream.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().startsWith(id + "."))
				.findFirst().orElse(null);
		} catch (IOException e) {
			logger.warn("[填报] 附件回查失败：{}", e.getMessage());
			return null;
		}
	}

	private String contentType(String kind) {
		return switch (kind == null ? "" : kind) {
			case "img" -> "image/jpeg";
			case "video" -> "video/mp4";
			case "pdf" -> "application/pdf";
			case "xls" -> "application/vnd.ms-excel";
			case "doc" -> "application/msword";
			default -> "application/octet-stream";
		};
	}

	// ==================== 工作台 ====================

	/**
	 * 工作台看板（按角色返回待办口径）
	 */
	public Map<String, Object> dashboard(String role) {

		boolean owner = FillConstants.ROLE_OWNER.equals(role);
		List<FillProject> projects = fillProjectDao.findList(new FillProject());
		List<FillNode> allNodes = fillNodeDao.findList(new FillNode());
		List<FillNodeFb> allFbs = fillNodeFbDao.findList(new FillNodeFb());
		List<FillSup> allSups = fillSupDao.findList(new FillSup());
		List<FillIssue> allIssues = fillIssueDao.findList(new FillIssue());
		Map<String, FillNodeFb> fbByNode = allFbs.stream()
			.collect(Collectors.toMap(FillNodeFb::getNodeId, fb -> fb, (a, b) -> a));
		Map<String, FillProject> projectMap = projects.stream()
			.collect(Collectors.toMap(FillProject::getId, p -> p));

		LocalDate today = FillUtils.today();
		int pre = 0;
		int dev = 0;
		int ops = 0;
		for (FillProject p : projects) {
			if (FillConstants.STAGE_PRE.equals(p.getProjectStage())) {
				pre++;
			} else if (FillConstants.STAGE_OPS.equals(p.getProjectStage())) {
				ops++;
			} else {
				dev++;
			}
		}

		List<Map<String, Object>> todoNodes = new ArrayList<>();
		List<Map<String, Object>> todoSups = new ArrayList<>();
		List<Map<String, Object>> todoIssues = new ArrayList<>();
		List<Map<String, Object>> todo = new ArrayList<>();

		for (FillNode n : allNodes) {
			if (!FillConstants.STAGE_DEV.equals(n.getStage()) && !FillConstants.STAGE_OPS.equals(n.getStage())) {
				continue;
			}
			FillNodeFb fb = fbByNode.get(n.getId());
			String st = nodeStatus(n, fb, today);
			if ("late".equals(st) || ("run".equals(st) && fb == null)) {
				FillProject p = projectMap.get(n.getProjectId());
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("projectId", n.getProjectId());
				item.put("projectName", p == null ? "" : p.getProjectName());
				item.put("nodeId", n.getId());
				item.put("title", n.getNodeName());
				item.put("sub", "节点 " + FillUtils.fmtDay(n.getStartDate()) + " ~ " + FillUtils.fmtDay(n.getEndDate())
					+ " · " + ("late".equals(st) ? "已超期未反馈" : "期内待反馈"));
				item.put("st", "late".equals(st) ? "danger" : "warn");
				item.put("stTxt", "late".equals(st) ? (owner ? "超期待补报" : "超期未反馈") : "待填报");
				todoNodes.add(item);
			}
		}
		todoNodes.sort(Comparator.comparing(m -> "danger".equals(m.get("st")) ? 0 : 1));

		for (FillSup s : allSups) {
			String st = effectiveSupStatus(s, today);
			if ("待回复".equals(st) || "已超期".equals(st)) {
				FillProject p = projectMap.get(s.getProjectId());
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("projectId", s.getProjectId());
				item.put("projectName", p == null ? "" : p.getProjectName());
				item.put("supId", s.getId());
				item.put("title", s.getTitle());
				item.put("sub", s.getSupNo() + " · 要求 " + FillUtils.fmtDay(s.getDueDate()) + " 前回复");
				item.put("st", "已超期".equals(st) ? "danger" : "warn");
				item.put("stTxt", st);
				todoSups.add(item);
			}
		}

		for (FillIssue x : allIssues) {
			String st = effectiveIssueStatus(x, today);
			boolean hit = owner ? "协调中".equals(st) : "待受理".equals(st);
			if (hit) {
				FillProject p = projectMap.get(x.getProjectId());
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("projectId", x.getProjectId());
				item.put("projectName", p == null ? "" : p.getProjectName());
				item.put("issueId", x.getId());
				item.put("title", x.getTitle());
				item.put("sub", x.getIssueNo() + " · " + FillUtils.fmtDay(x.getIssueDate()) + " 提交 · " + st
					+ (owner ? "，等待住更局回复" : "，待受理"));
				item.put("st", "info");
				item.put("stTxt", st);
				todoIssues.add(item);
			}
		}

		if (owner) {
			todo.addAll(todoNodes);
			todo.addAll(todoSups);
			todo.addAll(todoIssues);
		} else {
			// 住更局待办仅纳入超期未反馈节点（与原型 viewDashboard 口径一致）
			todo.addAll(todoIssues);
			todo.addAll(todoSups);
			todoNodes.stream().filter(m -> "danger".equals(m.get("st"))).forEach(todo::add);
		}
		todo.sort(Comparator.comparing(m -> sev(m.get("st"))));

		List<Map<String, Object>> focus = new ArrayList<>();
		for (String id : List.of("SZ001", "SZ004", "SZ011", "SZ024", "SZ021")) {
			FillProject p = projectMap.get(id);
			if (p == null) {
				continue;
			}
			Map<String, Object> row = projectMap(p);
			List<FillNode> arr = allNodes.stream()
				.filter(n -> id.equals(n.getProjectId()) && StringUtils.equals(n.getStage(), p.getProjectStage()))
				.collect(Collectors.toList());
			int done = 0;
			int late = 0;
			for (FillNode n : arr) {
				String st = FillConstants.STAGE_PRE.equals(p.getProjectStage())
					? preStatus(n) : nodeStatus(n, fbByNode.get(n.getId()), today);
				if ("ok".equals(st) || "done".equals(st)) {
					done++;
				}
				if ("late".equals(st)) {
					late++;
				}
			}
			row.put("nodeDone", done);
			row.put("nodeTotal", arr.size());
			row.put("nodeLate", late);
			row.put("completeness", completeness(p, arr, fbByNode));
			focus.add(row);
		}

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("role", role);
		data.put("owner", owner);
		data.put("projectCount", projects.size());
		data.put("stageCounts", Map.of("pre", pre, "dev", dev, "ops", ops));
		data.put("todoNodes", todoNodes);
		data.put("todoSups", todoSups);
		data.put("todoIssues", todoIssues);
		data.put("todo", todo);
		data.put("lateNodeCount", todoNodes.stream().filter(m -> "danger".equals(m.get("st"))).count());
		// 累计协调事项（全市项目），与原型「累计 N 件」口径一致
		data.put("issueTotal", allIssues.size());
		// 待办角标：建设单位 = 节点待填报 + 督办待回复；住更局 = 督办待跟踪 + 协调待受理 + 超期节点
		data.put("workCount", owner
			? todoNodes.size() + todoSups.size()
			: todoSups.size() + todoIssues.size()
				+ todoNodes.stream().filter(m -> "danger".equals(m.get("st"))).count());
		data.put("focus", focus);
		data.put("syncStatus", syncStatus());
		return data;
	}

	private int sev(Object st) {
		return switch (String.valueOf(st)) {
			case "danger" -> 0;
			case "warn" -> 1;
			default -> 2;
		};
	}

	// ==================== 数据对接 ====================

	/**
	 * 同步日志（最近 30 条）
	 */
	public List<FillSyncLog> syncLogList() {
		List<FillSyncLog> list = fillSyncLogDao.findList(new FillSyncLog());
		return list.stream().limit(30).collect(Collectors.toList());
	}

	/**
	 * 立即同步（演示：写入同步日志并返回统计）
	 */
	@Transactional
	public Map<String, Object> syncDo() {

		String now = FillUtils.nowText();
		int projectCount = fillProjectDao.findList(new FillProject()).size();
		int nodeCount = fillNodeDao.findList(new FillNode()).stream()
			.filter(n -> FillConstants.STAGE_PRE.equals(n.getStage())).collect(Collectors.toList()).size();
		insertSyncLog(now, "项目基本信息", "成功", projectCount, 0, "增量同步完成，无字段冲突");
		insertSyncLog(now, "前期审批进度", "成功", nodeCount, 0, "增量同步完成，无字段冲突");
		insertSyncLog(now, "投资与形象进度", "成功", projectCount, 0, "增量同步完成，无字段冲突");
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("projectCount", projectCount);
		data.put("approvalCount", nodeCount);
		data.put("investCount", projectCount);
		data.put("logs", syncLogList());
		return data;
	}

	private void insertSyncLog(String time, String apiName, String status, long count, long error, String remark) {
		FillSyncLog log = new FillSyncLog();
		log.setSyncTime(new Date());
		log.setApiName(apiName);
		log.setSyncStatus(status);
		log.setRecordCount(count);
		log.setErrorCount(error);
		log.setRemarkText(remark);
		log.setUpdateBy("system");
		log.setIsNewRecord(true);
		fillSyncLogDao.insert(log);
	}

	private List<Map<String, Object>> syncStatus() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (String api : List.of("项目基本信息", "前期审批进度", "投资与形象进度")) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("api", api);
			item.put("time", FillUtils.nowText().substring(11));
			item.put("status", "成功");
			list.add(item);
		}
		return list;
	}

	// ==================== 状态与完整度 ====================

	/**
	 * 建设 / 运维节点计算状态：ok 已反馈 / run 进行中 / late 超期未反馈 / todo 未开始
	 */
	public String nodeStatus(FillNode n, FillNodeFb fb, LocalDate today) {
		if (fb != null) {
			return "1".equals(fb.getDoneFlag()) ? "ok" : "run";
		}
		if (n.getEndDate() != null && today.isAfter(FillUtils.toLocalDate(n.getEndDate()))) {
			return "late";
		}
		if (n.getStartDate() != null && !today.isBefore(FillUtils.toLocalDate(n.getStartDate()))) {
			return "run";
		}
		return "todo";
	}

	/**
	 * 前期审批计算状态
	 */
	public String preStatus(FillNode n) {
		String stored = StringUtils.trim(n.getNodeStatus());
		if (StringUtils.equalsAny(stored, "done", "late", "doing")) {
			return stored;
		}
		if (n.getPlanDate() != null && FillUtils.today().isAfter(FillUtils.toLocalDate(n.getPlanDate()))) {
			return "late";
		}
		return "todo";
	}

	/**
	 * 协调事项有效状态（超期自动标记）
	 */
	public String effectiveIssueStatus(FillIssue x, LocalDate today) {
		String st = x.getIssueStatus();
		if (StringUtils.equalsAny(st, "待受理", "协调中") && x.getExpectDate() != null && today.isAfter(FillUtils.toLocalDate(x.getExpectDate()))) {
			return "已超期";
		}
		return st;
	}

	/**
	 * 督办有效状态（超期自动标记）
	 */
	public String effectiveSupStatus(FillSup x, LocalDate today) {
		String st = x.getSupStatus();
		if ("待回复".equals(st) && x.getDueDate() != null && today.isAfter(FillUtils.toLocalDate(x.getDueDate()))) {
			return "已超期";
		}
		return st;
	}

	/**
	 * 填报完整度：基本信息必填项 50% + 节点期内反馈 50%
	 */
	private int completeness(FillProject p, List<FillNode> arr, Map<String, FillNodeFb> fbMap) {

		String[] basicFields = {"projectName", "projectCode", "investTotal", "planStart", "planEnd",
			"ownerUnit", "buildUnit", "masterName", "masterTel", "scopeDesc", "centerLnglat"};
		int need = basicFields.length;
		int ok = 0;
		for (String f : basicFields) {
			if (StringUtils.isNotBlank(getFieldValue(p, f))) {
				ok++;
			}
		}
		double basic = need == 0 ? 1 : (double) ok / need;

		int fed;
		if (FillConstants.STAGE_PRE.equals(p.getProjectStage())) {
			fed = (int) arr.stream().filter(n -> "done".equals(preStatus(n))).count();
		} else {
			fed = (int) arr.stream().filter(n -> fbMap.containsKey(n.getId())).count();
		}
		double nodeP = arr.isEmpty() ? 1 : (double) fed / arr.size();
		return (int) Math.round(basic * 50 + nodeP * 50);
	}

	// ==================== 数据装配 ====================

	private List<FillNode> nodesOfProject(String projectId) {
		return fillNodeDao.findList(new FillNode()).stream()
			.filter(n -> StringUtils.equals(n.getProjectId(), projectId))
			.sorted(Comparator.comparingInt(n -> n.getSortNo() == null ? 0 : n.getSortNo()))
			.collect(Collectors.toList());
	}

	private List<FillNode> currentStageNodes(FillProject p, List<FillNode> nodes) {
		return nodes.stream().filter(n -> StringUtils.equals(n.getStage(), p.getProjectStage()))
			.collect(Collectors.toList());
	}

	private Map<String, FillNodeFb> fbByNodeMap() {
		return fillNodeFbDao.findList(new FillNodeFb()).stream()
			.collect(Collectors.toMap(FillNodeFb::getNodeId, fb -> fb, (a, b) -> a));
	}

	private FillNodeFb fbOfNode(String nodeId) {
		FillNodeFb where = new FillNodeFb();
		where.setNodeId(nodeId);
		List<FillNodeFb> list = fillNodeFbDao.findList(where);
		return list.isEmpty() ? null : list.get(0);
	}

	private Map<String, List<FillFile>> filesOfProject(String projectId) {
		Map<String, List<FillFile>> map = new HashMap<>();
		FillFile where = new FillFile();
		where.setProjectId(projectId);
		for (FillFile f : fillFileDao.findList(where)) {
			map.computeIfAbsent(f.getBizType() + ":" + f.getBizId(), k -> new ArrayList<>()).add(f);
		}
		return map;
	}

	private int nextSortNo(String projectId, String stage) {
		return fillNodeDao.findList(new FillNode()).stream()
			.filter(n -> StringUtils.equals(n.getProjectId(), projectId) && StringUtils.equals(n.getStage(), stage))
			.mapToInt(n -> n.getSortNo() == null ? 0 : n.getSortNo()).max().orElse(0) + 1;
	}

	private long countIssues(String projectId) {
		return fillIssueDao.findList(new FillIssue()).stream()
			.filter(x -> StringUtils.equals(x.getProjectId(), projectId)).count();
	}

	private long countSups(String projectId) {
		return fillSupDao.findList(new FillSup()).stream()
			.filter(x -> StringUtils.equals(x.getProjectId(), projectId)).count();
	}

	/**
	 * 批量创建 / 更新附件记录（按文件 id 幂等：已存在则更新归属与元数据）
	 */
	private void attachFiles(String bizType, String bizId, String projectId, String operator, List<Map<String, Object>> files) {
		if (files == null || files.isEmpty()) {
			return;
		}
		for (Map<String, Object> f : files) {
			String id = str(f.get("id"));
			if (StringUtils.isBlank(id)) {
				continue;
			}
			FillFile e = fillFileDao.get(new FillFile(id));
			boolean isNew = e == null;
			if (isNew) {
				e = new FillFile();
				e.setId(id);
			}
			e.setProjectId(projectId);
			e.setBizType(bizType);
			e.setBizId(bizId);
			e.setFileName(str(f.get("name")));
			String ext = str(f.get("ext"));
			e.setFileExt(ext);
			e.setFileSize(f.get("size") == null ? 0L : Long.parseLong(String.valueOf(f.get("size"))));
			e.setFileKind(StringUtils.defaultIfBlank(str(f.get("kind")), FillUtils.kindOf(ext)));
			if (isNew) {
				e.setFilePath("fill/" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
					+ "/" + id + "." + StringUtils.defaultIfBlank(ext, "bin"));
			}
			e.setFileUrl(fileUrl(id));
			e.setUploadBy(operator);
			e.setUploadDate(FillUtils.toDate(FillUtils.today()));
			e.setUpdateBy(operator);
			if (isNew) {
				e.setIsNewRecord(true);
				fillFileDao.insert(e);
			} else {
				fillFileDao.update(e);
			}
		}
	}

	/**
	 * 全量同步附件：保留本次提交的文件（按 id），删除其余旧附件
	 * <p>用于「编辑反馈 / 修正前期审批」场景，避免未重新上传的原附件被误删。
	 */
	private void syncFiles(String bizType, String bizId, String projectId, String operator, List<Map<String, Object>> files) {
		FillFile where = new FillFile();
		where.setBizType(bizType);
		where.setBizId(bizId);
		List<FillFile> existing = fillFileDao.findList(where);
		Set<String> keep = new HashSet<>();
		if (files != null) {
			for (Map<String, Object> f : files) {
				String id = str(f.get("id"));
				if (StringUtils.isNotBlank(id)) {
					keep.add(id);
				}
			}
		}
		for (FillFile e : existing) {
			if (!keep.contains(e.getId())) {
				fillFileDao.delete(e);
			}
		}
		attachFiles(bizType, bizId, projectId, operator, files);
	}

	private void deleteFiles(String bizType, String bizId) {
		FillFile where = new FillFile();
		where.setBizType(bizType);
		where.setBizId(bizId);
		for (FillFile f : fillFileDao.findList(where)) {
			fillFileDao.delete(f);
		}
	}

	private String fileUrl(String id) {
		return Global.getCtxPath() + "/api/fill/file/download/" + id;
	}

	// ==================== Map 装配 ====================

	private Map<String, Object> projectMap(FillProject p) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", p.getId());
		m.put("projectCode", p.getProjectCode());
		m.put("projectName", p.getProjectName());
		m.put("projectType", p.getProjectType());
		m.put("district", p.getDistrict());
		m.put("projectStage", StringUtils.trim(p.getProjectStage()));
		m.put("ownerUnit", p.getOwnerUnit());
		m.put("buildUnit", p.getBuildUnit());
		m.put("masterName", p.getMasterName());
		m.put("masterTel", p.getMasterTel());
		m.put("investTotal", p.getInvestTotal());
		m.put("investDone", p.getInvestDone());
		m.put("yearPlan", p.getYearPlan());
		m.put("fundSource", p.getFundSource());
		m.put("progressPct", p.getProgressPct());
		m.put("planStart", p.getPlanStart());
		m.put("planEnd", p.getPlanEnd());
		m.put("actStart", p.getActStart());
		m.put("geomType", p.getGeomType());
		m.put("longitude", p.getLongitude());
		m.put("latitude", p.getLatitude());
		m.put("centerLnglat", p.getCenterLnglat());
		m.put("scaleDesc", p.getScaleDesc());
		m.put("place", p.getPlace());
		m.put("scopeDesc", p.getScopeDesc());
		m.put("contractor", p.getContractor());
		m.put("supervisor", p.getSupervisor());
		m.put("safetyOrg", p.getSafetyOrg());
		return m;
	}

	private Map<String, Object> nodeMap(FillNode n, FillNodeFb fb, List<FillFile> files,
			List<FillFile> preFiles, List<FillFile> fbFiles) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", n.getId());
		m.put("projectId", n.getProjectId());
		m.put("stage", StringUtils.trim(n.getStage()));
		m.put("sortNo", n.getSortNo());
		m.put("nodeName", n.getNodeName());
		m.put("shortName", n.getShortName());
		m.put("planContent", n.getPlanContent());
		m.put("planDate", FillUtils.fmtDay(n.getPlanDate()));
		m.put("actDate", FillUtils.fmtDay(n.getActDate()));
		m.put("nodeStatus", StringUtils.trim(n.getNodeStatus()));
		m.put("docNo", n.getDocNo());
		m.put("startDate", FillUtils.fmtDay(n.getStartDate()));
		m.put("endDate", FillUtils.fmtDay(n.getEndDate()));
		m.put("source", StringUtils.trim(n.getSource()));
		String st = FillConstants.STAGE_PRE.equals(n.getStage()) ? preStatus(n) : nodeStatus(n, fb, FillUtils.today());
		m.put("status", st);
		m.put("fb", fb == null ? null : nodeFbMap(fb, fbFiles));
		m.put("files", fileMaps(files));
		m.put("preFiles", fileMaps(preFiles));
		m.put("fbFiles", fileMaps(fbFiles));
		return m;
	}

	private Map<String, Object> nodeFbMap(FillNodeFb fb, List<FillFile> fbFiles) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", fb.getId());
		m.put("nodeId", fb.getNodeId());
		m.put("fbDate", FillUtils.fmtDay(fb.getFbDate()));
		m.put("doneFlag", fb.getDoneFlag());
		m.put("pct", fb.getPct());
		m.put("fbText", fb.getFbText());
		m.put("fbBy", fb.getFbBy());
		m.put("fbTime", fb.getFbTime() == null ? null : FillUtils.fmtDateTime(fb.getFbTime()));
		m.put("files", fileMaps(fbFiles));
		return m;
	}

	private Map<String, Object> issueMap(FillIssue x) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", x.getId());
		m.put("projectId", x.getProjectId());
		m.put("issueNo", x.getIssueNo());
		m.put("title", x.getTitle());
		m.put("issueType", x.getIssueType());
		m.put("urgent", x.getUrgent());
		m.put("dept", x.getDept());
		m.put("descText", x.getDescText());
		m.put("issueDate", FillUtils.fmtDay(x.getIssueDate()));
		m.put("expectDate", FillUtils.fmtDay(x.getExpectDate()));
		m.put("issueStatus", StringUtils.trim(x.getIssueStatus()));
		m.put("status", effectiveIssueStatus(x, FillUtils.today()));
		m.put("issueBy", x.getIssueBy());
		m.put("source", x.getSource());
		return m;
	}

	private Map<String, Object> issueReplyMap(FillIssueReply r) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", r.getId());
		m.put("issueId", r.getIssueId());
		m.put("replyRole", StringUtils.trim(r.getReplyRole()));
		m.put("replyWho", r.getReplyWho());
		m.put("replyDept", r.getReplyDept());
		m.put("replyDate", FillUtils.fmtDay(r.getReplyDate()));
		m.put("replyText", r.getReplyText());
		return m;
	}

	private Map<String, Object> supMap(FillSup x) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", x.getId());
		m.put("projectId", x.getProjectId());
		m.put("supNo", x.getSupNo());
		m.put("title", x.getTitle());
		m.put("supType", x.getSupType());
		m.put("supLevel", x.getSupLevel());
		m.put("contentText", x.getContentText());
		m.put("supDate", FillUtils.fmtDay(x.getSupDate()));
		m.put("dueDate", FillUtils.fmtDay(x.getDueDate()));
		m.put("supStatus", StringUtils.trim(x.getSupStatus()));
		m.put("status", effectiveSupStatus(x, FillUtils.today()));
		m.put("supBy", x.getSupBy());
		return m;
	}

	private Map<String, Object> supReplyMap(FillSupReply r) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", r.getId());
		m.put("supId", r.getSupId());
		m.put("replyDate", FillUtils.fmtDay(r.getReplyDate()));
		m.put("replyBy", r.getReplyBy());
		m.put("replyText", r.getReplyText());
		return m;
	}

	private Map<String, Object> archiveMap(FillArchive a) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", a.getId());
		m.put("projectId", a.getProjectId());
		m.put("cat", a.getCat());
		m.put("title", a.getTitle());
		m.put("note", a.getNote());
		m.put("archiveDate", FillUtils.fmtDay(a.getArchiveDate()));
		m.put("archiveBy", a.getArchiveBy());
		return m;
	}

	private Map<String, Object> fileMap(FillFile f) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", f.getId());
		m.put("projectId", f.getProjectId());
		m.put("bizType", f.getBizType());
		m.put("bizId", f.getBizId());
		m.put("fileName", f.getFileName());
		m.put("fileExt", f.getFileExt());
		m.put("fileSize", f.getFileSize());
		m.put("fileKind", f.getFileKind());
		m.put("fileUrl", f.getFileUrl());
		m.put("uploadBy", f.getUploadBy());
		m.put("uploadDate", FillUtils.fmtDay(f.getUploadDate()));
		return m;
	}

	private List<Map<String, Object>> fileMaps(List<FillFile> files) {
		return files.stream().map(this::fileMap).collect(Collectors.toList());
	}

	private Map<String, Object> chgMap(FillChg c) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", c.getId());
		m.put("projectId", c.getProjectId());
		m.put("fieldLabel", c.getFieldLabel());
		m.put("oldValue", c.getOldValue());
		m.put("newValue", c.getNewValue());
		m.put("chgTime", c.getChgTime() == null ? null : FillUtils.fmtDateTime(c.getChgTime()));
		m.put("chgBy", c.getChgBy());
		m.put("note", c.getNote());
		return m;
	}

	private String belongType(String bizType) {
		return switch (bizType == null ? "" : bizType) {
			case "pre" -> "节点附件";
			case "node" -> "节点附件";
			case "node_fb" -> "节点反馈";
			case "issue" -> "协调事项";
			case "issue_reply" -> "协调回复";
			case "sup" -> "督办通知";
			case "sup_reply" -> "督办回复";
			case "archive" -> "档案上传";
			default -> "附件";
		};
	}

	private String belongName(FillFile f, Map<String, String> nodeNames, Map<String, String> issueTitles,
			Map<String, String> supTitles, Map<String, String> archiveTitles) {
		return switch (f.getBizType() == null ? "" : f.getBizType()) {
			case "pre", "node", "node_fb" -> nodeNames.getOrDefault(f.getBizId(), "");
			case "issue" -> issueTitles.getOrDefault(f.getBizId(), "");
			case "issue_reply" -> {
				FillIssueReply r = fillIssueReplyDao.get(new FillIssueReply(f.getBizId()));
				yield r == null ? "" : issueTitles.getOrDefault(r.getIssueId(), "");
			}
			case "sup" -> supTitles.getOrDefault(f.getBizId(), "");
			case "sup_reply" -> {
				FillSupReply r = fillSupReplyDao.get(new FillSupReply(f.getBizId()));
				yield r == null ? "" : supTitles.getOrDefault(r.getSupId(), "");
			}
			case "archive" -> archiveTitles.getOrDefault(f.getBizId(), "");
			default -> "";
		};
	}

	// ==================== 通用 ====================

	private String operator(String role) {
		return FillConstants.ROLE_GOV.equals(role) ? FillConstants.GOV_NAME : FillConstants.OWNER_NAME;
	}

	private String str(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	private List<Map<String, Object>> list(Object value) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (value instanceof List<?> raw) {
			for (Object o : raw) {
				if (o instanceof Map<?, ?> m) {
					Map<String, Object> item = new LinkedHashMap<>();
					for (Map.Entry<?, ?> e : m.entrySet()) {
						item.put(String.valueOf(e.getKey()), e.getValue());
					}
					result.add(item);
				}
			}
		}
		return result;
	}

}
