package com.jeesite.modules.onemap04fill.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.onemap04fill.entity.OmFile;
import com.jeesite.modules.onemap04fill.service.OmFillService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 市政工程安全一张图填报系统开放接口
 * <p>
 * 【免登录】路径不在 adminPath（/a）之下，命中 shiro 过滤器链 {@code /** = anon}：
 * <pre>
 *   GET  http://127.0.0.1:5182/js/api/safetyfill/project/list
 *   POST http://127.0.0.1:5182/js/api/safetyfill/score/save
 * </pre>
 * 返回结构沿用框架约定：{@code {"result":"true","message":"操作成功","data":...}}
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
@Controller
@RequestMapping("/api/safetyfill")
public class OmFillController extends BaseController {

	private final OmFillService omFillService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public OmFillController(OmFillService omFillService) {
		this.omFillService = omFillService;
	}

	// ==================== 字典 ====================

	/** 字典与评分口径 */
	@GetMapping(value = "meta")
	@ResponseBody
	public String meta() {
		return execute("查询字典", () -> wrap(omFillService.meta()));
	}

	// ==================== 项目台账 ====================

	/** 项目列表 */
	@GetMapping(value = "project/list")
	@ResponseBody
	public String projectList(@RequestParam(required = false) String district,
			@RequestParam(required = false) String cat,
			@RequestParam(required = false) String buildStatus,
			@RequestParam(required = false) String riskLevel,
			@RequestParam(required = false) String kw,
			@RequestParam(required = false, defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "5") int pageSize) {
		return execute("查询项目列表", () -> wrap(omFillService.projectList(district, cat, buildStatus, riskLevel, kw, page, pageSize)));
	}

	/** 项目详情 */
	@GetMapping(value = "project/get")
	@ResponseBody
	public String projectGet(@RequestParam String id) {
		return execute("查询项目详情", () -> wrap(omFillService.projectGet(id)));
	}

	/** 台账统计 */
	@GetMapping(value = "project/stats")
	@ResponseBody
	public String stats() {
		return execute("查询台账统计", () -> wrap(omFillService.stats()));
	}

	/** 新增项目（手工建档） */
	@PostMapping(value = "project/create")
	@ResponseBody
	public String projectCreate(@RequestBody Map<String, Object> form) {
		return execute("新建项目", () -> wrap(omFillService.projectCreate(form, operator(form))));
	}

	/** 保存项目基本信息 */
	@PostMapping(value = "project/save")
	@ResponseBody
	public String projectSave(@RequestBody Map<String, Object> form) {
		return execute("保存项目信息", () -> wrap(omFillService.projectSave(str(form.get("projectId")), form, operator(form))));
	}

	// ==================== 危大 / 超危大 ====================

	/** 新增 / 编辑危大工程 */
	@PostMapping(value = "hazard/save")
	@ResponseBody
	public String hazardSave(@RequestBody Map<String, Object> form) {
		return execute("保存危大工程", () -> omFillService.hazardSave(form, operator(form)));
	}

	/** 删除危大工程 */
	@PostMapping(value = "hazard/delete")
	@ResponseBody
	public String hazardDelete(@RequestBody Map<String, Object> form) {
		return execute("删除危大工程", () -> {
			omFillService.hazardDelete(str(form.get("id")), operator(form));
			return true;
		});
	}

	// ==================== 基坑 ====================

	/** 全量保存基坑清单 */
	@PostMapping(value = "pit/save")
	@ResponseBody
	public String pitSave(@RequestBody Map<String, Object> form) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) form.get("list");
		return execute("保存基坑清单", () -> omFillService.pitSave(str(form.get("projectId")), list, operator(form)));
	}

	// ==================== 隐患 ====================

	/** 登记隐患 */
	@PostMapping(value = "risk/save")
	@ResponseBody
	public String riskSave(@RequestBody Map<String, Object> form) {
		return execute("登记隐患", () -> omFillService.riskSave(form, operator(form)));
	}

	/** 保存隐患整改 */
	@PostMapping(value = "risk/edit")
	@ResponseBody
	public String riskEdit(@RequestBody Map<String, Object> form) {
		return execute("保存隐患整改", () -> omFillService.riskEdit(str(form.get("id")), form, operator(form)));
	}

	/** 隐患闭环 */
	@PostMapping(value = "risk/close")
	@ResponseBody
	public String riskClose(@RequestBody Map<String, Object> form) {
		return execute("隐患闭环", () -> omFillService.riskClose(str(form.get("id")), operator(form)));
	}

	// ==================== 评分 ====================

	/** 评分页数据 */
	@GetMapping(value = "score/get")
	@ResponseBody
	public String scoreGet(@RequestParam String projectId) {
		return execute("查询评分", () -> wrap(omFillService.scoreGet(projectId)));
	}

	/** 保存评分 */
	@PostMapping(value = "score/save")
	@ResponseBody
	public String scoreSave(@RequestBody Map<String, Object> form) {
		return execute("保存评分", () -> {
			JsonNode review;
			try {
				review = objectMapper.valueToTree(form.get("review"));
			} catch (Exception e) {
				throw new IllegalArgumentException("评分数据格式错误");
			}
			return wrap(omFillService.scoreSave(str(form.get("projectId")), review, operator(form)));
		});
	}

	/** 按台账重算勾选（不落库） */
	@PostMapping(value = "score/reset")
	@ResponseBody
	public String scoreReset(@RequestBody Map<String, Object> form) {
		return execute("重算评分", () -> wrap(omFillService.scoreReset(str(form.get("projectId")))));
	}

	// ==================== 操作日志 ====================

	/** 操作日志 */
	@GetMapping(value = "log/list")
	@ResponseBody
	public String logList(@RequestParam(required = false) String projectId) {
		return execute("查询操作日志", () -> omFillService.logList(projectId));
	}

	// ==================== 附件 ====================

	/** 上传附件 */
	@PostMapping(value = "file/upload")
	@ResponseBody
	public String fileUpload(@RequestParam String projectId,
			@RequestParam(required = false) String bizType,
			@RequestParam(required = false) String bizKey,
			@RequestParam(required = false) String attType,
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) String operator) {
		return execute("上传附件", () -> omFillService.fileUpload(projectId, bizType, bizKey, attType, file, operator));
	}

	/** 下载附件 */
	@GetMapping(value = "file/download")
	@ResponseBody
	public ResponseEntity<FileSystemResource> fileDownload(@RequestParam String id) throws Exception {
		OmFile f = omFillService.fileGet(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		Path path = omFillService.filePath(f);
		String name = URLEncoder.encode(f.getFileName() == null ? "file" : f.getFileName(), StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename*=UTF-8''" + name)
				.body(new FileSystemResource(path.toFile()));
	}

	/** 删除附件 */
	@PostMapping(value = "file/delete")
	@ResponseBody
	public String fileDelete(@RequestBody Map<String, Object> form) {
		return execute("删除附件", () -> {
			omFillService.fileDelete(str(form.get("id")), operator(form));
			return true;
		});
	}

	// ==================== 辅助 ====================

	private Map<String, Object> wrap(Map<String, Object> map) {
		Map<String, Object> wrap = new LinkedHashMap<>();
		wrap.put("data", map);
		return wrap;
	}

	private String operator(Map<String, Object> form) {
		Object v = form.get("operator");
		return v == null || StringUtils.isBlank(v.toString()) ? "张工" : v.toString().trim();
	}

	private String str(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	/**
	 * 统一执行与异常转换
	 */
	private String execute(String action, Supplier<Object> task) {
		try {
			return renderResult(Global.TRUE, action + "成功", task.get());
		} catch (IllegalArgumentException e) {
			return renderResult(Global.FALSE, e.getMessage());
		} catch (Exception e) {
			Throwable root = e;
			while (root.getCause() != null && root.getCause() != root) {
				root = root.getCause();
			}
			logger.error("[市政安全填报] " + action + "失败", e);
			String message = root.getMessage() == null
					? root.getClass().getSimpleName() : root.getMessage().replaceAll("\\s+", " ").trim();
			return renderResult(Global.FALSE, action + "失败：" + message);
		}
	}

}
