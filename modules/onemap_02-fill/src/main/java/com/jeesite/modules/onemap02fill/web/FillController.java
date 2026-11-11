package com.jeesite.modules.onemap02fill.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.onemap02fill.entity.FillSyncLog;
import com.jeesite.modules.onemap02fill.service.FillService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 市政基础设施项目填报系统开放接口
 * <p>
 * 【免登录】路径不在 adminPath（/a）之下，命中 shiro 过滤器链 {@code /** = anon}：
 * <pre>
 *   GET  http://127.0.0.1:5182/js/api/fill/project/list
 *   POST http://127.0.0.1:5182/js/api/fill/node/fb/save
 * </pre>
 * 返回结构沿用框架约定：{@code {"result":"true","message":"操作成功","data":...}}
 * 角色（owner 建设单位 / gov 住更局）由前端传入，仅影响操作人与待办口径。
 *
 * @author wuchangyue
 * @version 2026-11-11
 */
@Controller
@RequestMapping("/api/fill")
public class FillController extends BaseController {

	private final FillService fillService;

	public FillController(FillService fillService) {
		this.fillService = fillService;
	}

	// ==================== 项目 ====================

	/**
	 * 项目列表
	 */
	@GetMapping(value = "project/list")
	@ResponseBody
	public String projectList() {
		return execute("查询项目列表", () -> fillService.projectList());
	}

	/**
	 * 项目详情（基本信息 + 修改留痕 + 节点统计）
	 */
	@GetMapping(value = "project/get")
	@ResponseBody
	public String projectGet(@RequestParam String id) {
		return execute("查询项目详情", () -> wrap(fillService.projectGet(id)));
	}

	/**
	 * 保存项目基本信息（修改留痕）
	 */
	@PostMapping(value = "project/save")
	@ResponseBody
	public String projectSave(@RequestBody Map<String, Object> form) {
		return execute("保存项目基本信息", () -> {
			fillService.projectSave(str(form.get("projectId")), form, role(form));
			return true;
		});
	}

	// ==================== 节点 ====================

	/**
	 * 节点列表
	 */
	@GetMapping(value = "node/list")
	@ResponseBody
	public String nodeList(@RequestParam(required = false) String projectId,
			@RequestParam(required = false) String stage) {
		return execute("查询节点列表", () -> fillService.nodeList(projectId, stage));
	}

	/**
	 * 新增 / 编辑节点
	 */
	@PostMapping(value = "node/save")
	@ResponseBody
	public String nodeSave(@RequestBody Map<String, Object> form) {
		return execute("保存节点", () -> fillService.nodeSave(form, role(form)));
	}

	/**
	 * 删除节点
	 */
	@PostMapping(value = "node/delete")
	@ResponseBody
	public String nodeDelete(@RequestBody Map<String, Object> form) {
		return execute("删除节点", () -> {
			fillService.nodeDelete(str(form.get("id")));
			return true;
		});
	}

	/**
	 * 节点上移 / 下移
	 */
	@PostMapping(value = "node/move")
	@ResponseBody
	public String nodeMove(@RequestBody Map<String, Object> form) {
		return execute("调整节点顺序", () -> {
			int direction = Integer.parseInt(String.valueOf(form.getOrDefault("direction", "1")));
			fillService.nodeMove(str(form.get("id")), direction);
			return true;
		});
	}

	/**
	 * 保存节点期内反馈
	 */
	@PostMapping(value = "node/fb/save")
	@ResponseBody
	public String nodeFbSave(@RequestBody Map<String, Object> form) {
		return execute("提交节点反馈", () -> {
			fillService.nodeFbSave(form, role(form));
			return true;
		});
	}

	/**
	 * 修正前期审批环节
	 */
	@PostMapping(value = "pre/save")
	@ResponseBody
	public String preSave(@RequestBody Map<String, Object> form) {
		return execute("修正前期审批信息", () -> {
			fillService.preSave(form, role(form));
			return true;
		});
	}

	// ==================== 协调事项 ====================

	/**
	 * 协调事项列表
	 */
	@GetMapping(value = "issue/list")
	@ResponseBody
	public String issueList(@RequestParam(required = false) String projectId) {
		return execute("查询协调事项", () -> fillService.issueList(projectId));
	}

	/**
	 * 提交协调事项
	 */
	@PostMapping(value = "issue/save")
	@ResponseBody
	public String issueSave(@RequestBody Map<String, Object> form) {
		return execute("提交协调事项", () -> fillService.issueSave(form, role(form)));
	}

	/**
	 * 住更局回复协调事项
	 */
	@PostMapping(value = "issue/reply")
	@ResponseBody
	public String issueReply(@RequestBody Map<String, Object> form) {
		return execute("回复协调事项", () -> {
			fillService.issueReply(form, role(form));
			return true;
		});
	}

	/**
	 * 建设单位补充材料
	 */
	@PostMapping(value = "issue/supplement")
	@ResponseBody
	public String issueSupplement(@RequestBody Map<String, Object> form) {
		return execute("上传补充材料", () -> {
			fillService.issueSupplement(form, role(form));
			return true;
		});
	}

	/**
	 * 住更局办结协调事项
	 */
	@PostMapping(value = "issue/close")
	@ResponseBody
	public String issueClose(@RequestBody Map<String, Object> form) {
		return execute("办结协调事项", () -> {
			fillService.issueClose(str(form.get("id")), role(form));
			return true;
		});
	}

	// ==================== 督办台账 ====================

	/**
	 * 督办列表
	 */
	@GetMapping(value = "sup/list")
	@ResponseBody
	public String supList(@RequestParam(required = false) String projectId) {
		return execute("查询督办台账", () -> fillService.supList(projectId));
	}

	/**
	 * 发出督办
	 */
	@PostMapping(value = "sup/save")
	@ResponseBody
	public String supSave(@RequestBody Map<String, Object> form) {
		return execute("发出督办", () -> fillService.supSave(form, role(form)));
	}

	/**
	 * 回复督办
	 */
	@PostMapping(value = "sup/reply")
	@ResponseBody
	public String supReply(@RequestBody Map<String, Object> form) {
		return execute("回复督办", () -> {
			fillService.supReply(form, role(form));
			return true;
		});
	}

	/**
	 * 审核闭环
	 */
	@PostMapping(value = "sup/close")
	@ResponseBody
	public String supClose(@RequestBody Map<String, Object> form) {
		return execute("督办闭环", () -> {
			fillService.supClose(str(form.get("id")), role(form));
			return true;
		});
	}

	// ==================== 档案与附件 ====================

	/**
	 * 自主归档列表
	 */
	@GetMapping(value = "archive/list")
	@ResponseBody
	public String archiveList(@RequestParam String projectId) {
		return execute("查询档案", () -> fillService.archiveList(projectId));
	}

	/**
	 * 上传归档
	 */
	@PostMapping(value = "archive/save")
	@ResponseBody
	public String archiveSave(@RequestBody Map<String, Object> form) {
		return execute("归档", () -> fillService.archiveSave(form, role(form)));
	}

	/**
	 * 删除自主归档
	 */
	@PostMapping(value = "archive/delete")
	@ResponseBody
	public String archiveDelete(@RequestBody Map<String, Object> form) {
		return execute("删除档案", () -> {
			fillService.archiveDelete(str(form.get("id")));
			return true;
		});
	}

	/**
	 * 项目全部附件
	 */
	@GetMapping(value = "file/list")
	@ResponseBody
	public String fileList(@RequestParam(required = false) String projectId) {
		return execute("查询附件", () -> fillService.fileList(projectId));
	}

	/**
	 * 上传附件（落盘，返回元数据）
	 */
	@PostMapping(value = "file/upload")
	@ResponseBody
	public String fileUpload(@RequestParam("file") MultipartFile file) {
		return execute("上传附件", () -> wrap(fillService.fileUpload(file)));
	}

	/**
	 * 下载 / 预览附件
	 */
	@GetMapping(value = "file/download/{id}")
	public void fileDownload(@PathVariable String id, HttpServletResponse response) {
		fillService.fileDownload(id, response);
	}

	// ==================== 工作台与数据对接 ====================

	/**
	 * 工作台看板
	 */
	@GetMapping(value = "dashboard")
	@ResponseBody
	public String dashboard(@RequestParam(defaultValue = "owner") String role) {
		return execute("查询工作台", () -> wrap(fillService.dashboard(role)));
	}

	/**
	 * 同步日志
	 */
	@GetMapping(value = "sync/log")
	@ResponseBody
	public String syncLog() {
		return execute("查询同步日志", () -> fillService.syncLogList());
	}

	/**
	 * 立即同步
	 */
	@PostMapping(value = "sync/do")
	@ResponseBody
	public String syncDo() {
		return execute("同步", () -> wrap(fillService.syncDo()));
	}

	// ==================== 通用 ====================

	/**
	 * 包装 Map 返回：框架的 renderResult 会展开 Map，这里统一包一层 data，
	 * 与 List 返回口径一致。
	 */
	private Map<String, Object> wrap(Map<String, Object> map) {
		Map<String, Object> wrap = new java.util.LinkedHashMap<>();
		wrap.put("data", map);
		return wrap;
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
			logger.error("[填报系统] " + action + "失败", e);
			String message = root.getMessage() == null
				? root.getClass().getSimpleName() : root.getMessage().replaceAll("\\s+", " ").trim();
			return renderResult(Global.FALSE, action + "失败：" + message);
		}
	}

	private String role(Map<String, Object> form) {
		Object role = form.get("role");
		return role == null ? "owner" : String.valueOf(role);
	}

	private String str(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

}
