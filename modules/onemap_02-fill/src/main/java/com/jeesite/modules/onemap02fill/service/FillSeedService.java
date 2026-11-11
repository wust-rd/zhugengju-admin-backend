package com.jeesite.modules.onemap02fill.service;

import com.jeesite.modules.onemap02fill.dao.FillArchiveDao;
import com.jeesite.modules.onemap02fill.dao.FillChgDao;
import com.jeesite.modules.onemap02fill.dao.FillIssueDao;
import com.jeesite.modules.onemap02fill.dao.FillIssueReplyDao;
import com.jeesite.modules.onemap02fill.dao.FillNodeDao;
import com.jeesite.modules.onemap02fill.dao.FillNodeFbDao;
import com.jeesite.modules.onemap02fill.dao.FillProjectDao;
import com.jeesite.modules.onemap02fill.dao.FillSupDao;
import com.jeesite.modules.onemap02fill.dao.FillSupReplyDao;
import com.jeesite.modules.onemap02fill.dao.FillSyncLogDao;
import com.jeesite.modules.onemap02fill.entity.FillChg;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 填报系统种子数据
 * <p>
 * 首次启动且 FILL_PROJECT 为空时，按产品原型口径写入 28 个首批示范项目，
 * 以及对应的前期审批 / 建设实施 / 运维管理节点、协调事项、督办台账、
 * 修改留痕与同步日志。已有数据时不重复执行。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Service
public class FillSeedService implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(FillSeedService.class);

	/** 28 个首批示范项目：id / 名称 / 类别 / 阶段 / 所属区 / 建设单位 / 纬度 / 经度 / 规模 / 总投资 / 已完成 / 进度% / 开工 / 竣工 */
	private static final String[][] PROJECTS = {
		{"SZ001", "轨道交通12号线", "五个中心", "dev", "跨江环线", "武汉地铁集团", "30.546", "114.286", "59.9 公里", "486.0", "96.0", "64", "2020-12", "2027-12"},
		{"SZ002", "两湖隧道东湖段", "五个中心", "dev", "武昌区", "市城投集团", "30.548", "114.365", "6.4 公里", "78.5", "16.0", "58", "2023-06", "2027-06"},
		{"SZ003", "两湖隧道南湖段", "五个中心", "dev", "洪山区", "市城投集团", "30.505", "114.362", "5.2 公里", "62.4", "12.0", "41", "2023-06", "2027-12"},
		{"SZ004", "武汉关泵站及配套市政工程", "五个中心", "pre", "江汉区", "武汉城建集团", "30.5798", "114.2928", "2 座泵站", "12.8", "3.6", "22", "2026-08", "2028-06"},
		{"SZ005", "江汉十桥及两岸接线工程", "五个中心", "dev", "硚口区", "武汉城发集团", "30.586", "114.178", "2.5 公里", "24.6", "5.8", "55", "2024-03", "2026-12"},
		{"SZ006", "多福路跨汉江桥工程", "五个中心", "dev", "硚口区", "市城投集团", "30.562", "114.253", "2.2 公里", "19.2", "1.2", "2", "2026-10", "2029-06"},
		{"SZ007", "和平大道（工人村路—四环线）工程", "五个中心", "dev", "青山区", "武汉城建集团", "30.629", "114.445", "7.4 公里", "36.5", "8.4", "47", "2025-03", "2028-06"},
		{"SZ008", "右岸大道南段（临江大道）工程（江安路—八坦路）", "五个中心", "dev", "武昌区", "市城投集团", "30.511", "114.294", "4.8 公里", "28.4", "6.2", "52", "2024-09", "2027-03"},
		{"SZ009", "右岸大道南段工程（张之洞路—江安路）", "五个中心", "dev", "武昌区", "武汉城建集团", "30.536", "114.306", "3.6 公里", "21.8", "4.6", "61", "2024-06", "2026-12"},
		{"SZ010", "汉蔡骨干通道（解放大道—常青路）", "五个中心", "pre", "江汉区", "市城投集团", "30.603", "114.249", "6.8 公里", "42.0", "5.0", "12", "2027-03", "2030-06"},
		{"SZ011", "武汉站、汉口站功能提升项目", "五个中心", "ops", "洪山区", "市城投集团、江汉区", "30.61", "114.423", "2 座站区", "16.4", "1.8", "100", "2023-04", "2025-12"},
		{"SZ012", "二七过江通道（工农兵路—解放大道段/503工程）", "畅通工程", "dev", "江岸区", "市城投集团", "30.603", "114.317", "5.6 公里", "88.6", "18.6", "44", "2022-12", "2028-12"},
		{"SZ013", "芦湾湖路工程（珞狮南路—南湖大道）", "畅通工程", "pre", "洪山区", "市城投集团", "30.492", "114.344", "3.2 公里", "9.8", "2.4", "16", "2026-11", "2029-03"},
		{"SZ014", "建设大道延长线", "畅通工程", "dev", "硚口区", "市城投集团", "30.591", "114.216", "4.6 公里", "26.2", "6.0", "38", "2025-06", "2028-03"},
		{"SZ015", "南泥湾大道（三环线—汉西三路）改造工程", "畅通工程", "dev", "硚口区", "武汉城发集团", "30.584", "114.202", "5.4 公里", "18.6", "4.4", "51", "2024-10", "2027-06"},
		{"SZ016", "额头湾立交改造工程", "畅通工程", "dev", "硚口区", "武汉城发集团", "30.5845", "114.187", "1 座立交", "6.8", "1.6", "63", "2025-05", "2027-05"},
		{"SZ017", "琴川大道东延线及配套绿化工程", "畅通工程", "dev", "汉阳区", "武汉城发集团", "30.543", "114.168", "6.2 公里", "14.2", "3.2", "35", "2025-09", "2028-09"},
		{"SZ018", "新湾四路", "畅通工程", "pre", "江汉区", "东西湖区政府 / 江汉区政府", "30.634", "114.253", "2.4 公里", "7.6", "1.8", "20", "2026-09", "2028-12"},
		{"SZ019", "青化路（21号公路—外环匝道）拓宽工程", "青山北湖片区", "dev", "青山区", "市城投集团", "30.641", "114.448", "5.8 公里", "22.4", "5.2", "42", "2025-04", "2028-04"},
		{"SZ020", "青化路（青江大道—吴土公路）道路排水工程", "青山北湖片区", "dev", "青山区", "市城投集团", "30.653", "114.468", "6.4 公里", "8.9", "2.2", "36", "2025-08", "2028-02"},
		{"SZ021", "北湖路（焦沙中路—北湖污水处理厂）道路排水工程", "青山北湖片区", "dev", "青山区", "市城投集团", "30.599", "114.488", "4.2 公里", "6.4", "1.2", "78", "2024-04", "2026-12"},
		{"SZ022", "八吉府大街南段（青化路—武鄂高速）工程", "青山北湖片区", "dev", "青山区", "建设中心", "30.619", "114.478", "5.2 公里", "12.6", "3.0", "28", "2025-12", "2028-12"},
		{"SZ023", "轨道交通11号线四期工程", "其他路桥隧", "dev", "武昌区", "武汉地铁集团", "30.52", "114.276", "14.2 公里", "168.5", "34.0", "33", "2025-03", "2029-12"},
		{"SZ024", "轨道交通新港线一期工程", "其他路桥隧", "dev", "青山区", "武汉地铁集团", "30.66", "114.51", "21.6 公里", "142.0", "30.0", "57", "2021-12", "2026-12"},
		{"SZ025", "轨道交通10号线一期（原新港线西延线）工程", "其他路桥隧", "pre", "江岸区", "武汉地铁集团", "30.606", "114.345", "16.8 公里", "196.0", "12.0", "6", "2027-06", "2031-12"},
		{"SZ026", "轨道交通3号线二期工程", "其他路桥隧", "dev", "蔡甸区", "武汉地铁集团", "30.5", "114.118", "8.4 公里", "98.6", "22.0", "39", "2023-12", "2027-12"},
		{"SZ027", "轨道交通6号线三期工程", "其他路桥隧", "pre", "蔡甸区", "武汉地铁集团", "30.514", "114.139", "6.6 公里", "56.4", "9.0", "8", "2027-03", "2031-06"},
		{"SZ028", "汉口滨江国际商务区基础设施PPP项目", "其他路桥隧", "ops", "江岸区", "武汉城建集团", "30.608", "114.326", "3.6 平方公里", "64.8", "1.2", "100", "2019-06", "2025-06"},
	};

	/** 前期审批计划月份偏移 */
	private static final int[] PRE_OFFSETS = {-26, -22, -18, -14, -11, -8};

	private final FillProjectDao fillProjectDao;
	private final FillNodeDao fillNodeDao;
	private final FillNodeFbDao fillNodeFbDao;
	private final FillIssueDao fillIssueDao;
	private final FillIssueReplyDao fillIssueReplyDao;
	private final FillSupDao fillSupDao;
	private final FillSupReplyDao fillSupReplyDao;
	private final FillChgDao fillChgDao;
	private final FillSyncLogDao fillSyncLogDao;

	public FillSeedService(FillProjectDao fillProjectDao, FillNodeDao fillNodeDao, FillNodeFbDao fillNodeFbDao,
			FillIssueDao fillIssueDao, FillIssueReplyDao fillIssueReplyDao, FillSupDao fillSupDao,
			FillSupReplyDao fillSupReplyDao, FillChgDao fillChgDao, FillSyncLogDao fillSyncLogDao) {
		this.fillProjectDao = fillProjectDao;
		this.fillNodeDao = fillNodeDao;
		this.fillNodeFbDao = fillNodeFbDao;
		this.fillIssueDao = fillIssueDao;
		this.fillIssueReplyDao = fillIssueReplyDao;
		this.fillSupDao = fillSupDao;
		this.fillSupReplyDao = fillSupReplyDao;
		this.fillChgDao = fillChgDao;
		this.fillSyncLogDao = fillSyncLogDao;
	}

	@Override
	public void run(ApplicationArguments args) {
		long count = fillProjectDao.findCount(new FillProject());
		if (count > 0) {
			logger.info("[填报系统] 已有 {} 个项目，跳过种子数据", count);
			return;
		}
		logger.info("[填报系统] 开始写入种子数据");
		try {
			seed();
			logger.info("[填报系统] 种子数据写入完成，共 {} 个项目", PROJECTS.length);
		} catch (Exception e) {
			logger.error("[填报系统] 种子数据写入失败", e);
		}
	}

	@Transactional
	protected void seed() {
		LocalDate today = FillUtils.today();
		for (String[] row : PROJECTS) {
			FillProject p = toProject(row);
			fillProjectDao.insert(p);
			seedNodes(p);
			seedIssues(p, today);
			seedSups(p, today);
			seedChgs(p);
		}
		seedSyncLogs();
	}

	/** 行转项目实体 */
	private FillProject toProject(String[] r) {
		FillProject p = new FillProject();
		p.setId(r[0]);
		String year = r[12].substring(0, 4);
		p.setProjectCode("WH-SZ-" + year + "-" + r[0].substring(2));
		p.setProjectName(r[1]);
		p.setProjectType(r[2]);
		p.setProjectStage(r[3]);
		p.setDistrict(r[4]);
		p.setOwnerUnit(r[5]);
		p.setBuildUnit(r[5] + " 建设事业总部");
		p.setMasterName(FillConstants.OWNER_NAME);
		p.setMasterTel("027-8572 3168");
		p.setInvestTotal(Double.valueOf(r[9]));
		p.setInvestDone(Double.valueOf(r[10]));
		p.setYearPlan(Double.valueOf(String.format("%.1f", Double.parseDouble(r[9]) * 0.17)));
		p.setFundSource("市级财政资金 + 政府专项债");
		p.setProgressPct(Double.valueOf(r[11]));
		p.setPlanStart(r[12]);
		p.setPlanEnd(r[13]);
		p.setActStart(FillConstants.STAGE_PRE.equals(r[3]) ? null : r[12]);
		p.setGeomType(r[8].contains("公里") ? "线状" : "点状");
		p.setLatitude(r[6]);
		p.setLongitude(r[7]);
		p.setCenterLnglat(String.format("%.4f, %.4f", Double.parseDouble(r[6]), Double.parseDouble(r[7])));
		p.setScaleDesc(r[8]);
		p.setPlace(r[4] + " · 项目红线范围内");
		p.setScopeDesc(r[8] + "；" + ("其他路桥隧".equals(r[2])
			? "轨道交通土建、站场及附属工程"
			: "道路（隧道 / 轨道）主体工程，含排水管网、照明、绿化及交通安全设施"));
		p.setContractor("中铁十一局集团有限公司");
		p.setSupervisor("武汉飞虹建设监理有限公司");
		p.setSafetyOrg("武汉市市政工程质量安全监督站");
		p.setUpdateBy("system");
		p.setIsNewRecord(true);
		return p;
	}

	/** 前期审批 + 建设实施 + 运维管理节点 */
	private void seedNodes(FillProject p) {
		int sidx = FillConstants.stageIndex(p.getProjectStage());
		int seed = FillUtils.seedOf(p.getId());
		int doneCount = p.getProgressPct() >= 20 ? 3 : p.getProgressPct() >= 12 ? 2 : 1;

		for (int i = 0; i < FillConstants.PRE_TPL.size(); i++) {
			String[] t = FillConstants.PRE_TPL.get(i);
			String planM = FillUtils.mAdd(p.getPlanStart(), PRE_OFFSETS[i]);
			LocalDate planDate = FillUtils.parseDay(FillUtils.mLast(planM));
			String status = "todo";
			LocalDate actDate = null;
			String doc = null;
			if (sidx > 0 || i < doneCount) {
				status = "done";
				actDate = planDate.minusDays((seed + i * 3) % 8 + 1);
				doc = preDocNo(i, planM);
			} else if (i == doneCount) {
				status = p.getProgressPct() >= 12 ? "late" : "doing";
			}
			insertNode(p.getId(), FillConstants.STAGE_PRE, i + 1, t[0], t[1], t[2],
				planDate, actDate, status, doc, null, null, "api");
		}

		seedCustom(p, FillConstants.STAGE_DEV, sidx, seed);
		seedCustom(p, FillConstants.STAGE_OPS, sidx, seed);
	}

	/** 建设 / 运维自定义节点（与原型 buildCustom 同口径） */
	private void seedCustom(FillProject p, String stage, int sidx, int seed) {
		boolean dev = FillConstants.STAGE_DEV.equals(stage);
		List<String[]> lib = dev ? FillConstants.DEV_LIB : FillConstants.OPS_LIB;
		int need = dev ? 1 : 2;
		String from = dev ? p.getPlanStart() : p.getPlanEnd();
		String to = dev ? p.getPlanEnd() : FillUtils.mAdd(p.getPlanEnd(), 24);
		int months = Math.max(FillUtils.mDiff(from, to), 4);
		int cnt = Math.min(8, Math.max(4, (int) Math.round(months / 9.0) + 2));
		List<String[]> mid = lib.subList(1, lib.size() - 1);
		List<String[]> picked = new ArrayList<>();
		for (int i = 0; picked.size() < cnt - 2 && i < 40; i++) {
			String[] c = mid.get((seed + i * (dev ? 1 : 3)) % mid.size());
			if (!picked.contains(c)) {
				picked.add(c);
			}
		}
		picked.sort(Comparator.comparingInt(mid::indexOf));
		List<String[]> chosen = new ArrayList<>();
		chosen.add(lib.get(0));
		chosen.addAll(picked);
		chosen.add(lib.get(lib.size() - 1));
		int total = Math.max(months, chosen.size());

		LocalDate today = FillUtils.today();
		List<Integer> expired = new ArrayList<>();
		List<FillNode> nodes = new ArrayList<>();
		List<FillNodeFb> fbs = new ArrayList<>();
		List<int[]> pending = new ArrayList<>();

		for (int i = 0; i < chosen.size(); i++) {
			String[] t = chosen.get(i);
			int startIdx = (int) Math.floor(i * total / (double) chosen.size());
			int endIdx = (int) Math.floor((i + 1) * total / (double) chosen.size()) - 1;
			endIdx = Math.max(endIdx, startIdx);
			LocalDate st0 = FillUtils.parseDay(FillUtils.mFirst(FillUtils.mAdd(from, startIdx)));
			LocalDate en = FillUtils.parseDay(FillUtils.mLast(FillUtils.mAdd(from, endIdx)));
			FillNode node = new FillNode();
			node.setProjectId(p.getId());
			node.setStage(stage);
			node.setSortNo(i + 1);
			node.setNodeName(t[0]);
			node.setShortName(t[1]);
			node.setPlanContent(t[2]);
			node.setStartDate(FillUtils.toDate(st0));
			node.setEndDate(FillUtils.toDate(en));
			node.setSource("fill");
			node.setUpdateBy("system");
			node.setIsNewRecord(true);
			nodes.add(node);

			if (sidx >= need) {
				boolean done;
				if (sidx > need) {
					done = true;
				} else if (today.isAfter(en)) {
					done = true;
					expired.add(i);
				} else if (!today.isBefore(st0)) {
					done = false;
				} else {
					continue;
				}
				// 反馈依赖节点ID，先记录待生成项，节点入库后统一补
				pending.add(new int[]{i, done ? 1 : 0});
			}
		}

		for (FillNode node : nodes) {
			fillNodeDao.insert(node);
		}

		// 超期节点：取最后一个已到期且未反馈的节点，符合“最近一期漏报”的实际情形
		if (sidx == need && !expired.isEmpty()) {
			int li = expired.get(expired.size() - 1);
			pending.removeIf(item -> item[0] == li);
		}

		for (int[] item : pending) {
			int i = item[0];
			boolean done = item[1] == 1;
			String[] t = chosen.get(i);
			fbs.add(makeFb(nodes.get(i), t, i, done, seed, today));
		}
		for (FillNodeFb fb : fbs) {
			fillNodeFbDao.insert(fb);
		}
	}

	/** 节点反馈内容（与原型 makeFb 同口径） */
	private FillNodeFb makeFb(FillNode node, String[] t, int i, boolean done, int seed, LocalDate today) {
		long span = Math.max(FillUtils.dDiff(FillUtils.fmtDay(node.getStartDate()), FillUtils.fmtDay(node.getEndDate())), 3);
		int offset = (int) Math.min(span - 1, 2 + ((seed + i * 7) % Math.max(span - 4, 3)));
		LocalDate date = FillUtils.toLocalDate(node.getStartDate()).plusDays(offset);
		if (date.isAfter(today)) {
			date = today;
		}
		double pct = done ? 100 : Math.min(92, 42 + ((seed + i * 11) % 46));
		String what = t[2].replaceFirst("^完成", "");
		String txt = done
			? "本节点计划内容已完成：" + what + "。经监理复核，质量、安全与文明施工检查未发现问题，现场影像与验收资料已同步归档。"
			: "本节点正在实施，" + what + "已完成约 " + (int) pct + "%；现场投入作业人员 " + (18 + (seed + i) % 26)
				+ " 人、机械 " + (3 + (seed + i) % 7) + " 台；计划 " + FillUtils.fmtDay(node.getEndDate())
				+ " 前完成，暂无需协调的制约因素。";
		FillNodeFb fb = new FillNodeFb();
		fb.setNodeId(node.getId());
		fb.setFbDate(FillUtils.toDate(date));
		fb.setDoneFlag(done ? "1" : "0");
		fb.setPct(pct);
		fb.setFbText(txt);
		fb.setFbBy(FillConstants.OWNER_NAME);
		fb.setFbTime(java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
		fb.setUpdateBy("system");
		fb.setIsNewRecord(true);
		return fb;
	}

	/** 前期审批批复文号 */
	private String preDocNo(int i, String ym) {
		String y = ym.substring(0, 4);
		return switch (i) {
			case 0 -> "武政办〔" + y + "〕18 号";
			case 1 -> "武自然资规〔" + y + "〕" + (36 + i * 11) + " 号";
			case 2 -> "武发改审批〔" + y + "〕" + (58 + i * 7) + " 号";
			case 3 -> "武城建初设〔" + y + "〕" + (12 + i * 5) + " 号";
			case 4 -> "武施审〔" + y + "〕" + (226 + i * 9) + " 号";
			case 5 -> "中标通知〔" + y + "〕" + (77 + i * 3) + " 号";
			default -> "武审批〔" + y + "〕" + (20 + i) + " 号";
		};
	}

	private void insertNode(String projectId, String stage, int sortNo, String name, String shortName,
			String planContent, LocalDate planDate, LocalDate actDate, String status, String docNo,
			LocalDate startDate, LocalDate endDate, String source) {
		FillNode n = new FillNode();
		n.setProjectId(projectId);
		n.setStage(stage);
		n.setSortNo(sortNo);
		n.setNodeName(name);
		n.setShortName(shortName);
		n.setPlanContent(planContent);
		n.setPlanDate(planDate == null ? null : java.util.Date.from(planDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
		n.setActDate(actDate == null ? null : java.util.Date.from(actDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
		n.setNodeStatus(status);
		n.setDocNo(docNo);
		n.setStartDate(startDate == null ? null : java.util.Date.from(startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
		n.setEndDate(endDate == null ? null : java.util.Date.from(endDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
		n.setSource(source);
		n.setUpdateBy("system");
		n.setIsNewRecord(true);
		fillNodeDao.insert(n);
	}

	/** 协调事项种子 */
	private void seedIssues(FillProject p, LocalDate today) {
		int seed = FillUtils.seedOf(p.getId());
		int n = "SZ001".equals(p.getId()) ? 12 : (3 + seed % 3);
		List<String[]> pool = FillConstants.ISSUE_POOL;
		for (int i = 0; i < n; i++) {
			String[] t = pool.get((seed + i * 5) % pool.size());
			int days = 8 + ((seed + i * 11) % 70);
			LocalDate date = today.minusDays(days);
			String status;
			if (i == 0) {
				status = "待受理";
			} else if (i == 1) {
				status = "协调中";
			} else if (i == 2 && seed % 2 == 0) {
				status = "已超期";
			} else {
				status = (i % 3 == 0) ? "协调中" : "已办结";
			}
			LocalDate expect = date.plusDays(15 + (seed + i) % 10);
			FillIssue x = new FillIssue();
			x.setProjectId(p.getId());
			x.setIssueNo("XTS" + p.getId().substring(2) + "-" + String.format("%02d", i + 1));
			x.setTitle(t[0]);
			x.setIssueType(t[1]);
			x.setUrgent(t[2]);
			x.setDept(t[3]);
			x.setDescText(t[4]);
			x.setIssueDate(java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
			x.setExpectDate(java.util.Date.from(expect.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
			x.setIssueStatus(status);
			x.setIssueBy(FillConstants.OWNER_NAME);
			x.setSource("fill");
			x.setUpdateBy("system");
			x.setIsNewRecord(true);
			fillIssueDao.insert(x);

			if (!"待受理".equals(status)) {
				insertReply(x.getId(), "gov", FillConstants.GOV_NAME, "市住更局 市政处", date.plusDays(1),
					"已受理。已安排相关权属单位于本周内现场踏勘，请建设单位同步准备管线资料与施工组织安排。");
			}
			if ("协调中".equals(status)) {
				insertReply(x.getId(), "gov", FillConstants.GOV_NAME, "市住更局 市政处", date.plusDays(4),
					"已组织专题协调会，形成三点意见：一是由权属单位 5 个工作日内出具书面意见；二是建设单位调整施工顺序，先行施工不受影响段落；三是迁改费用按原批复口径分担。");
			}
			if ("已办结".equals(status)) {
				insertReply(x.getId(), "gov", FillConstants.GOV_NAME, "市住更局 市政处", date.plusDays(4),
					"已协调完成，权属单位出具书面确认意见，相关审批手续已办结。");
			}
			if ("已超期".equals(status)) {
				insertReply(x.getId(), "gov", FillConstants.GOV_NAME, "市住更局 市政处", date.plusDays(6),
					"事项涉及跨区权属单位，协调周期较长，已提请市政府专题会研究，预计下周出具意见。");
			}
		}
	}

	private void insertReply(String issueId, String role, String who, String dept, LocalDate date, String text) {
		FillIssueReply r = new FillIssueReply();
		r.setIssueId(issueId);
		r.setReplyRole(role);
		r.setReplyWho(who);
		r.setReplyDept(dept);
		r.setReplyDate(java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
		r.setReplyText(text);
		r.setUpdateBy("system");
		r.setIsNewRecord(true);
		fillIssueReplyDao.insert(r);
	}

	/** 督办台账种子 */
	private void seedSups(FillProject p, LocalDate today) {
		int seed = FillUtils.seedOf(p.getId());
		int n = "SZ001".equals(p.getId()) ? 9 : (2 + seed % 3);
		List<String[]> pool = FillConstants.SUP_POOL;
		for (int i = 0; i < n; i++) {
			String[] t = pool.get((seed + i * 3) % pool.size());
			LocalDate date = today.minusDays(6 + i * 9 + (seed % 7));
			LocalDate due = date.plusDays(7 + (i % 3) * 4);
			String status;
			if (today.isAfter(due)) {
				status = (i % 3 == 0) ? "已超期" : "已回复";
			} else {
				status = (i % 2 == 0) ? "待回复" : "已回复";
			}
			FillSup x = new FillSup();
			x.setProjectId(p.getId());
			x.setSupNo("督办〔2026〕" + String.format("%03d", 37 + i * 7 + (seed % 9)) + " 号");
			x.setTitle(t[0]);
			x.setSupType(t[1]);
			x.setSupLevel(t[2]);
			x.setContentText(t[3]);
			x.setSupDate(java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
			x.setDueDate(java.util.Date.from(due.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
			x.setSupStatus(status);
			x.setSupBy(FillConstants.GOV_NAME);
			x.setUpdateBy("system");
			x.setIsNewRecord(true);
			fillSupDao.insert(x);

			if ("已回复".equals(status) || "已闭环".equals(status)) {
				int span = (int) Math.min(6, FillUtils.dDiff(FillUtils.fmtDay(x.getSupDate()), FillUtils.fmtDay(x.getDueDate())));
				LocalDate replyDate = date.plusDays(Math.max(span, 0));
				FillSupReply r = new FillSupReply();
				r.setSupId(x.getId());
				r.setReplyDate(java.util.Date.from(replyDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
				r.setReplyBy(FillConstants.OWNER_NAME);
				r.setReplyText("已按要求完成整改，明确责任人与完成时限，相关问题已逐项销号；整改情况经监理复核，现场照片与台账资料已上传，请审核。");
				r.setUpdateBy("system");
				r.setIsNewRecord(true);
				fillSupReplyDao.insert(r);
				if (i % 4 == 3) {
					x.setSupStatus("已闭环");
					fillSupDao.update(x);
				}
			}
		}
	}

	/** 修改留痕种子 */
	private void seedChgs(FillProject p) {
		Object[][] rows = {
			{"项目负责人", FillConstants.OWNER_NAME, FillConstants.OWNER_NAME, "2026-11-08 16:22", "核对联系方式"},
			{"年度计划投资", "82.0 亿元", "84.5 亿元", "2026-10-30 09:14", "按市发改委下达计划调整"},
		};
		for (Object[] r : rows) {
			FillChg c = new FillChg();
			c.setProjectId(p.getId());
			c.setFieldLabel(String.valueOf(r[0]));
			c.setOldValue(String.valueOf(r[1]));
			c.setNewValue(String.valueOf(r[2]));
			c.setChgTime(java.util.Date.from(
				java.time.LocalDateTime.parse(String.valueOf(r[3]).replace(" ", "T")).atZone(java.time.ZoneId.systemDefault()).toInstant()));
			c.setChgBy(FillConstants.OWNER_NAME);
			c.setNote(String.valueOf(r[4]));
			c.setUpdateBy("system");
			c.setIsNewRecord(true);
			fillChgDao.insert(c);
		}
	}

	/** 同步日志种子 */
	private void seedSyncLogs() {
		Object[][] rows = {
			{"2026-11-11 08:30", "项目基本信息", "成功", 28, 0, "增量同步完成，无字段冲突"},
			{"2026-11-11 08:30", "前期审批进度", "成功", 168, 0, "增量同步完成，无字段冲突"},
			{"2026-11-11 08:30", "投资与形象进度", "成功", 28, 0, "增量同步完成，无字段冲突"},
			{"2026-11-10 08:30", "项目基本信息", "成功", 28, 0, "增量同步完成，无字段冲突"},
			{"2026-11-10 08:30", "前期审批进度", "成功", 165, 0, "增量同步完成，无字段冲突"},
			{"2026-11-10 08:30", "投资与形象进度", "成功", 28, 0, "增量同步完成，无字段冲突"},
			{"2026-11-09 08:30", "项目基本信息", "成功", 28, 0, "增量同步完成，无字段冲突"},
			{"2026-11-09 08:30", "前期审批进度", "失败", 0, 3, "审批系统超时，已自动重试 3 次，下次同步将补传"},
		};
		for (Object[] r : rows) {
			FillSyncLog log = new FillSyncLog();
			log.setSyncTime(java.util.Date.from(
				java.time.LocalDateTime.parse(String.valueOf(r[0]).replace(" ", "T")).atZone(java.time.ZoneId.systemDefault()).toInstant()));
			log.setApiName(String.valueOf(r[1]));
			log.setSyncStatus(String.valueOf(r[2]));
			log.setRecordCount(Long.parseLong(String.valueOf(r[3])));
			log.setErrorCount(Long.parseLong(String.valueOf(r[4])));
			log.setRemarkText(String.valueOf(r[5]));
			log.setUpdateBy("system");
			log.setIsNewRecord(true);
			fillSyncLogDao.insert(log);
		}
	}

}
