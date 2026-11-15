package com.jeesite.modules.onemap04fill.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jeesite.modules.onemap04fill.entity.OmHazard;
import com.jeesite.modules.onemap04fill.entity.OmPit;
import com.jeesite.modules.onemap04fill.entity.OmProject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 市政安全一张图 - 风险评分引擎
 * <p>
 * 规则与产品原型 v2 的 scoreFromSel / autoSelections 完全一致，前后端口径统一。
 * 评分结果持久化在 OM4_PROJECT.REVIEW_JSON（勾选结果）与 SCORE_TOTAL / RISK_LEVEL（结果）。
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
public final class OmScoreEngine {

	private OmScoreEngine() {
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final Pattern PIT_CAT = Pattern.compile("基坑|土方|支护|降水");
	private static final Pattern FORMWORK_CAT = Pattern.compile("模板|滑模|爬模|飞模|隧道模|支撑体系");
	private static final Pattern LIFT_CAT = Pattern.compile("起重|吊装|塔式|塔机|吊车|龙门吊");
	private static final Pattern SCAFFOLD_CAT = Pattern.compile("脚手架|吊篮|卸料平台|操作平台");
	private static final Pattern DEMOLISH_CAT = Pattern.compile("拆除");
	private static final Pattern MINE_CAT = Pattern.compile("暗挖|盾构|顶管|矿山|隧道|洞室");

	/** 分值 → 等级：≤40 一级，≤60 二级，≤80 三级，其余四级 */
	public static int gradeOf(int t) {
		return t <= 40 ? 1 : t <= 60 ? 2 : t <= 80 ? 3 : 4;
	}

	public static int catScore(String cat) {
		Integer v = OmMeta.CAT_SCORE.get(cat);
		return v == null ? 15 : v;
	}

	/** 由台账危大工程名称推断所属大类，基坑工程返回 -1（不计入第二项） */
	public static int hazCatIdx(OmHazard h) {
		String t = (h.getCat() == null ? "" : h.getCat()) + " " + (h.getName() == null ? "" : h.getName());
		if (PIT_CAT.matcher(t).find()) {
			return -1;
		}
		if (FORMWORK_CAT.matcher(t).find()) {
			return 0;
		}
		if (LIFT_CAT.matcher(t).find()) {
			return 1;
		}
		if (SCAFFOLD_CAT.matcher(t).find()) {
			return 2;
		}
		if (DEMOLISH_CAT.matcher(t).find()) {
			return 3;
		}
		if (MINE_CAT.matcher(t).find()) {
			return 4;
		}
		return 5;
	}

	// ==================== 勾选结构 ====================

	public static ObjectNode emptySelections(String cat) {
		ObjectNode sel = MAPPER.createObjectNode();
		sel.put("catV", catScore(cat));
		sel.set("haz", emptyHaz());
		sel.putArray("hazDed");
		sel.set("pit", emptyPit());
		sel.putArray("pitDed");
		sel.put("inspect", 0);
		sel.put("src", "empty");
		sel.put("by", "—");
		sel.put("time", "—");
		return sel;
	}

	private static ObjectNode emptyHaz() {
		ObjectNode haz = MAPPER.createObjectNode();
		haz.set("nod", emptyLevel());
		haz.set("sup", emptyLevel());
		haz.put("none", false);
		haz.putObject("att");
		haz.putObject("qty");
		return haz;
	}

	private static ObjectNode emptyLevel() {
		ObjectNode o = MAPPER.createObjectNode();
		o.put("on", false);
		o.putArray("items");
		return o;
	}

	private static ObjectNode emptyPit() {
		ObjectNode pit = MAPPER.createObjectNode();
		pit.set("sup", emptyLevel());
		pit.set("nod", emptyLevel());
		pit.put("none", false);
		return pit;
	}

	/** 依据台账数据自动推算一组勾选值（「按台账重算」） */
	public static ObjectNode autoSelections(OmProject p, List<OmHazard> hazards, List<OmPit> pits) {
		ObjectNode sel = emptySelections(p.getCat());
		List<Integer> ded = new ArrayList<>();
		boolean planBad = hazards.stream().anyMatch(h -> !"已审批".equals(h.getPlan()) && !"已交底".equals(h.getPlan()))
				|| hazards.stream().anyMatch(h -> "超危大".equals(h.getLevel())
						&& ("未论证".equals(h.getExpert()) || "不需要".equals(h.getExpert())));
		if (planBad) {
			ded.add(15);
		}
		boolean sideBad = !hazards.isEmpty()
				&& ((p.getSideCount() == null || p.getSideCount() == 0)
						|| hazards.stream().anyMatch(h -> h.getSupervisor() == null || "—".equals(h.getSupervisor())));
		if (sideBad) {
			ded.add(5);
		}
		boolean accBad = hazards.stream().anyMatch(h -> "实施中".equals(h.getStage()) || "完工".equals(h.getStage()))
				&& (p.getAcceptCount() == null || p.getAcceptCount() <= 0);
		if (accBad) {
			ded.add(10);
		}
		ArrayNode hazDed = sel.putArray("hazDed");
		ded.forEach(hazDed::add);

		ObjectNode haz = sel.putObject("haz");
		haz.set("nod", emptyLevel());
		haz.set("sup", emptyLevel());
		haz.putObject("att");
		haz.putObject("qty");
		List<OmHazard> hz = hazards.stream().filter(h -> hazCatIdx(h) >= 0).toList();
		boolean supOn = hz.stream().anyMatch(h -> "超危大".equals(h.getLevel()));
		boolean nodOn = hz.stream().anyMatch(h -> !"超危大".equals(h.getLevel()));
		((ObjectNode) haz.get("nod")).put("on", nodOn);
		((ObjectNode) haz.get("sup")).put("on", supOn);
		haz.put("none", !(supOn || nodOn));

		sel.set("pit", pitFromLedger(pits));
		sel.put("inspect", p.getInspectScore() == null ? 0 : p.getInspectScore());
		sel.put("src", "auto");
		sel.put("by", "系统");
		sel.put("time", "—");
		return sel;
	}

	/** 台账基坑 → 评分页勾选（方案已审批的预置为归档附件） */
	public static ObjectNode pitFromLedger(List<OmPit> pits) {
		ObjectNode pit = emptyPit();
		List<OmPit> led = pits.stream().filter(x -> x.getDepth() != null && x.getDepth().doubleValue() >= 3).toList();
		for (OmPit x : led) {
			boolean sup = x.getDepth().doubleValue() >= 5;
			String lv = sup ? "sup" : "nod";
			ObjectNode level = (ObjectNode) pit.get(lv);
			level.put("on", true);
			ObjectNode it = MAPPER.createObjectNode();
			it.put("name", x.getName() == null ? "" : x.getName());
			it.put("depth", x.getDepth() == null ? 0 : x.getDepth().doubleValue());
			boolean filed = "已审批".equals(x.getPlan());
			String name = x.getName() == null ? "" : x.getName();
			it.put("plan", filed ? "台账归档-" + name + "-专项施工方案.pdf" : "");
			it.put("argue", sup && filed ? "台账归档-" + name + "-专家论证意见.pdf" : "");
			it.put("mont", "1".equals(x.getMonitor()) ? "台账归档-" + name + "-基坑监测数据.pdf" : "");
			((ArrayNode) level.get("items")).add(it);
		}
		if (led.isEmpty()) {
			pit.put("none", true);
		}
		return pit;
	}

	// ==================== 计分 ====================

	/** 由一组勾选值计算四维得分 */
	public static Map<String, Object> score(JsonNode sel) {
		int d1 = catV(sel);
		int base = hazBaseOf(sel);
		ObjectNode haz = (ObjectNode) (sel.path("haz").isObject() ? sel.path("haz") : emptyHaz());

		List<Map<String, Object>> dedList = new ArrayList<>();
		for (JsonNode n : sel.path("hazDed")) {
			int v = n.asInt();
			OmMeta.HAZ_DED_OPTS.stream().filter(o -> (int) o.get("v") == v).findFirst()
					.ifPresent(o -> dedList.add(new LinkedHashMap<>(Map.of("t", o.get("t"), "v", v))));
			if (dedList.stream().noneMatch(x -> (int) x.get("v") == v)) {
				dedList.add(new LinkedHashMap<>(Map.of("t", "其它扣分项", "v", v)));
			}
		}
		dedList.sort((a, b) -> (int) b.get("v") - (int) a.get("v"));

		List<Map<String, Object>> atts = attList(sel);
		List<Map<String, Object>> missing = atts.stream().filter(a -> !(boolean) a.get("ok")).toList();
		boolean autoLock15 = base != 35 && !missing.isEmpty();
		if (autoLock15) {
			String tip = "专项施工方案 / 专家论证附件未上传完整（缺 " + missing.size() + " 项）";
			boolean found = false;
			for (Map<String, Object> x : dedList) {
				if ((int) x.get("v") == 15) {
					x.put("t", x.get("t") + "（附件未传齐，自动判定）");
					x.put("auto", true);
					found = true;
					break;
				}
			}
			if (!found) {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("t", tip);
				m.put("v", 15);
				m.put("auto", true);
				dedList.add(0, m);
			}
			dedList.sort((a, b) -> (int) b.get("v") - (int) a.get("v"));
		}
		boolean attAllOk = !atts.isEmpty() && missing.isEmpty();
		if (attAllOk) {
			dedList.removeIf(x -> (int) x.get("v") == 15);
		}
		int dedSum = base == 35 ? 0 : dedList.stream().mapToInt(x -> (int) x.get("v")).sum();
		int rawD2 = base - dedSum;
		int d2 = Math.max(0, rawD2);

		int pitBase = pitBaseOfSel(sel);
		List<Integer> pitDed = new ArrayList<>();
		sel.path("pitDed").forEach(n -> pitDed.add(n.asInt()));
		pitDed.sort(Comparator.reverseOrder());
		List<Map<String, Object>> pitAtts = attListPit(sel);
		List<Map<String, Object>> pitMissing = pitAtts.stream().filter(a -> !(boolean) a.get("ok")).toList();
		boolean pitAttAllOk = !pitAtts.isEmpty() && pitMissing.isEmpty();
		if (!pitMissing.isEmpty() && !pitDed.contains(12)) {
			pitDed.add(12);
		}
		if (pitAttAllOk && sel.path("_auto12").asBoolean(false)) {
			pitDed.remove(Integer.valueOf(12));
		}
		int pitDedSum = pitDed.stream().mapToInt(Integer::intValue).sum();
		int d3 = Math.max(0, pitBase - pitDedSum);

		int ins = sel.path("inspect").asInt(0);
		int d4 = Math.max(0, Math.min(25, (int) Math.round(ins * 0.25)));
		int total = Math.max(0, Math.min(100, d1 + d2 + d3 + d4));

		boolean both = haz.path("nod").path("on").asBoolean() && haz.path("sup").path("on").asBoolean();
		String baseNote;
		if (haz.path("none").asBoolean()) {
			baseNote = "非危大工程 → 不扣分，得满分 35 分";
		} else if (both) {
			baseNote = "同时存在危大与超危大工程，按超危大计 → 基础档 25 分";
		} else if (haz.path("sup").path("on").asBoolean()) {
			baseNote = "存在超危大工程 → 基础档 25 分";
		} else if (haz.path("nod").path("on").asBoolean()) {
			baseNote = "仅存在危大工程 → 基础档 30 分";
		} else {
			baseNote = "尚未勾选级别 → 暂按满分 35 分计";
		}
		int cntNod = haz.path("nod").path("on").asBoolean() ? haz.path("nod").path("items").size() : 0;
		int cntSup = haz.path("sup").path("on").asBoolean() ? haz.path("sup").path("items").size() : 0;
		String hazNote;
		if (haz.path("none").asBoolean()) {
			hazNote = "已确认为非危大工程，不涉及危大 / 超危大类型";
		} else if (cntNod + cntSup > 0) {
			StringBuilder sb = new StringBuilder("已选类型：");
			if (cntNod > 0) {
				sb.append("危大 ").append(cntNod).append(" 项");
			}
			if (cntNod > 0 && cntSup > 0) {
				sb.append("、");
			}
			if (cntSup > 0) {
				sb.append("超危大 ").append(cntSup).append(" 项");
			}
			if (!atts.isEmpty()) {
				sb.append("；专项方案/论证附件已传齐 ").append(atts.size() - missing.size()).append(" / ").append(atts.size())
						.append(" 套");
				if (!missing.isEmpty()) {
					sb.append("（缺 ").append(missing.size()).append(" 套）");
				}
			}
			hazNote = sb.toString();
		} else if (base == 35) {
			hazNote = "本项目未涉及危大 / 超危大工程";
		} else {
			hazNote = "已勾选级别，但尚未细分具体类型";
		}
		String pitBaseNote = OmMeta.PIT_BASE_OPTS.stream().filter(o -> (int) o.get("v") == pitBase).findFirst()
				.map(o -> (String) o.get("t")).orElse(OmMeta.PIT_BASE_OPTS.get(2).get("t").toString());
		String pitDedText = pitDed.isEmpty() ? "无扣分项"
				: "扣分：" + pitDed.stream().map(v -> {
					String t = OmMeta.PIT_DED_OPTS.stream().filter(o -> (int) o.get("v") == v).findFirst()
							.map(o -> (String) o.get("t")).orElse("其它情形");
					return "－" + v + " 分（" + t + "）";
				}).reduce((a, b) -> a + "；" + b).orElse("");

		Map<String, Object> r = new LinkedHashMap<>();
		r.put("d1", d1);
		r.put("d2", d2);
		r.put("d3", d3);
		r.put("d4", d4);
		r.put("total", total);
		r.put("lv", gradeOf(total));
		r.put("base", base);
		r.put("rawD2", rawD2);
		r.put("dedSum", dedSum);
		r.put("baseNote", baseNote);
		r.put("hazNote", hazNote);
		r.put("pitBase", pitBase);
		r.put("pitDed", pitDed);
		r.put("pitDedSum", pitDedSum);
		r.put("pitBaseNote", pitBaseNote);
		r.put("pitDedText", pitDedText);
		r.put("d3note", "基础档 " + pitBase + " 分（" + pitBaseNote + "）－ 扣分合计 " + pitDedSum + " 分 = " + d3 + " 分；" + pitDedText);
		r.put("d4note", "安监智慧监管系统安全检查分数 " + ins + " × 0.25 = " + String.format("%.2f", ins * 0.25) + " → 四舍五入 " + d4 + " 分");
		r.put("ins", ins);
		r.put("catNote", OmMeta.CAT_NOTE.getOrDefault(d1, OmMeta.CAT_NOTE.get(15)));
		r.put("atts", atts);
		r.put("missing", missing);
		r.put("attAllOk", attAllOk);
		r.put("autoLock15", autoLock15);
		r.put("pitAtts", pitAtts);
		r.put("pitMissing", pitMissing);
		r.put("pitAttAllOk", pitAttAllOk);
		r.put("cnt", Map.of("nod", cntNod, "sup", cntSup, "total", cntNod + cntSup));
		r.put("src", sel.path("src").asText("empty"));
		r.put("manual", "manual".equals(sel.path("src").asText()));
		return r;
	}

	private static int catV(JsonNode sel) {
		int v = sel.path("catV").asInt(15);
		return v > 0 ? v : 15;
	}

	private static int hazBaseOf(JsonNode sel) {
		JsonNode h = sel.path("haz");
		if (h.path("none").asBoolean()) {
			return 35;
		}
		if (h.path("sup").path("on").asBoolean()) {
			return 25;
		}
		if (h.path("nod").path("on").asBoolean()) {
			return 30;
		}
		return 35;
	}

	private static int pitBaseOfSel(JsonNode sel) {
		JsonNode p = sel.path("pit");
		if (p.path("none").asBoolean()) {
			return 20;
		}
		if (p.path("sup").path("on").asBoolean()) {
			return 16;
		}
		if (p.path("nod").path("on").asBoolean()) {
			return 18;
		}
		return 20;
	}

	private static int hazQty(JsonNode sel, String key) {
		JsonNode v = sel.path("haz").path("qty").path(key);
		int n = (int) v.asDouble(0);
		if (!v.isNumber() || n < 1) {
			return 1;
		}
		return Math.min(99, n);
	}

	/** 危大/超危大逐项附件清单 */
	public static List<Map<String, Object>> attList(JsonNode sel) {
		List<Map<String, Object>> out = new ArrayList<>();
		JsonNode h = sel.path("haz");
		for (String lv : List.of("nod", "sup")) {
			JsonNode o = h.path(lv);
			if (!o.path("on").asBoolean()) {
				continue;
			}
			for (JsonNode k : o.path("items")) {
				String raw = k.asText();
				String key = lv + ":" + raw;
				int qty = hazQty(sel, key);
				List<String> req = OmMeta.ATT_REQ.get(lv);
				Map<String, String> info = hazItemInfo(raw, "sup".equals(lv));
				for (int i = 0; i < qty; i++) {
					String ak = key + "#" + i;
					JsonNode a = h.path("att").path(ak);
					List<String> miss = new ArrayList<>();
					for (String t : req) {
						if (a.path(t).asText().isEmpty()) {
							miss.add(OmMeta.ATT_LABEL.get(t));
						}
					}
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("key", key);
					m.put("attKey", ak);
					m.put("idx", i);
					m.put("of", qty);
					m.put("lv", lv);
					m.put("raw", raw);
					m.put("cat", info.get("cat"));
					m.put("sub", info.get("sub"));
					m.put("text", info.get("text"));
					m.put("plan", a.path("plan").asText(""));
					m.put("argue", a.path("argue").asText(""));
					m.put("req", req);
					m.put("miss", miss);
					m.put("ok", miss.isEmpty());
					out.add(m);
				}
			}
		}
		return out;
	}

	/** 基坑逐座附件清单 */
	public static List<Map<String, Object>> attListPit(JsonNode sel) {
		List<Map<String, Object>> out = new ArrayList<>();
		JsonNode p = sel.path("pit");
		for (String lv : List.of("sup", "nod")) {
			JsonNode o = p.path(lv);
			if (!o.path("on").asBoolean()) {
				continue;
			}
			JsonNode list = o.path("items");
			boolean both = p.path("sup").path("on").asBoolean() && p.path("nod").path("on").asBoolean();
			for (int i = 0; i < list.size(); i++) {
				JsonNode it = list.get(i);
				boolean deep = "sup".equals(lv);
				boolean needArgue = deep;
				boolean needMont = deep;
				List<String> miss = new ArrayList<>();
				if (it.path("plan").asText().isEmpty()) {
					miss.add("专项施工方案");
				}
				if (needArgue && it.path("argue").asText().isEmpty()) {
					miss.add("专家论证意见");
				}
				if (needMont && it.path("mont").asText().isEmpty()) {
					miss.add("基坑监测数据");
				}
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("lv", lv);
				m.put("key", lv + ":" + i);
				m.put("idx", i);
				m.put("name", it.path("name").asText(""));
				m.put("depth", it.path("depth").asDouble(0));
				m.put("plan", it.path("plan").asText(""));
				m.put("argue", it.path("argue").asText(""));
				m.put("mont", it.path("mont").asText(""));
				m.put("deep", deep);
				m.put("needArgue", needArgue);
				m.put("needMont", needMont);
				m.put("both", both);
				m.put("req", deep ? List.of("plan", "argue", "mont") : List.of("plan"));
				m.put("miss", miss);
				m.put("ok", miss.isEmpty());
				out.add(m);
			}
		}
		return out;
	}

	/** 'ci-si' → 类别名、子项名 */
	public static Map<String, String> hazItemInfo(String k, boolean sup) {
		String[] a = k.split("-");
		int ci = a.length > 0 ? Integer.parseInt(a[0]) : 0;
		int si = a.length > 1 ? Integer.parseInt(a[1]) : 0;
		List<Map<String, Object>> t = sup ? OmMeta.supTree() : OmMeta.hazTree();
		Map<String, Object> c = t.get(Math.min(ci, t.size() - 1));
		String cat = (String) c.get("n");
		List<String> subs = (List<String>) c.get("subs");
		String sub = si < subs.size() ? subs.get(si) : "";
		Map<String, String> m = new LinkedHashMap<>();
		m.put("ci", String.valueOf(ci));
		m.put("si", String.valueOf(si));
		m.put("cat", cat);
		m.put("sub", sub);
		m.put("text", cat + " · " + sub);
		return m;
	}

	/** 计算 JSON 序列化 */
	public static String toJson(Object o) {
		try {
			return MAPPER.writeValueAsString(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** JsonNode → 普通 Map（避免框架把 JsonNode 当 Bean 序列化） */
	public static Map<String, Object> toMap(JsonNode node) {
		return MAPPER.convertValue(node, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
		});
	}

	public static JsonNode toJsonNode(String json) {
		try {
			return json == null || json.isBlank() ? MAPPER.createObjectNode() : MAPPER.readTree(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** 兼容旧结构：把 pitQty/pitDepths/pitAtt 迁移为逐座结构 */
	public static JsonNode migratePit(JsonNode sel, JsonNode pitItems) {
		if (sel.has("pit")) {
			return sel;
		}
		if (sel.has("pitQty")) {
			ObjectNode pit = emptyPit();
			int n = Math.max(0, Math.min(20, sel.path("pitQty").asInt(0)));
			for (int i = 0; i < n; i++) {
				double d = sel.path("pitDepths").path(i).asDouble(0);
				String lv = d >= 5 ? "sup" : "nod";
				JsonNode a = sel.path("pitAtt").path(i);
				ObjectNode it = MAPPER.createObjectNode();
				it.put("name", pitItems.path(i).path("name").asText(""));
				it.put("depth", d);
				it.put("plan", a.path("plan").asText(""));
				it.put("argue", a.path("argue").asText(""));
				it.put("mont", a.path("mont").asText(""));
				ObjectNode lvNode = (ObjectNode) pit.get(lv);
				lvNode.put("on", true);
				((ArrayNode) lvNode.get("items")).add(it);
			}
			if (!pit.path("sup").path("on").asBoolean() && !pit.path("nod").path("on").asBoolean()) {
				pit.put("none", true);
			}
			return pit;
		}
		return pitFromLedger(CollectionsPits.of(pitItems));
	}

	/** 兼容层：把 JsonNode 形式的台账基坑转成 OmPit 列表 */
	private static final class CollectionsPits {
		private static List<OmPit> of(JsonNode arr) {
			List<OmPit> list = new ArrayList<>();
			if (arr == null || !arr.isArray()) {
				return list;
			}
			Iterator<JsonNode> it = arr.elements();
			while (it.hasNext()) {
				JsonNode n = it.next();
				OmPit p = new OmPit();
				p.setName(n.path("name").asText(""));
				p.setDepth(BigDecimal.valueOf(n.path("depth").asDouble(0)));
				p.setPlan(n.path("plan").asText(""));
				p.setMonitor(n.path("monitor").asBoolean(false) ? "1" : "0");
				list.add(p);
			}
			return list;
		}
	}

}
