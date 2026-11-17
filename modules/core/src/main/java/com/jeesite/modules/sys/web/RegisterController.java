/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.sys.web;

import com.jeesite.common.codec.DesUtils;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.entity.TreeEntity;
import com.jeesite.common.idgen.IdGen;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.sys.dao.CompanyDao;
import com.jeesite.modules.sys.dao.OfficeDao;
import com.jeesite.modules.sys.entity.Company;
import com.jeesite.modules.sys.entity.EmpUser;
import com.jeesite.modules.sys.entity.Employee;
import com.jeesite.modules.sys.entity.Office;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.CompanyService;
import com.jeesite.modules.sys.service.EmpUserService;
import com.jeesite.modules.sys.service.OfficeService;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 自助注册 Controller
 * <p>注册字段：公司名、手机号（等同于登录账号）、联系人姓名、密码。
 * 注册成功后账号可直接登录，并默认授予「普通员工」角色。
 *
 * @author wuchangyue
 * @version 2026-11-17
 */
@Controller
@Tag(name = "Register - 自助注册")
@RequestMapping(value = "/register")
@ConditionalOnProperty(name = { "user.enabled", "web.core.enabled" }, havingValue = "true", matchIfMissing = true)
public class RegisterController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

	/** 新注册用户默认授予的角色编码 */
	private static final String DEFAULT_ROLE_CODE = "user";

	/** 新公司默认创建的机构名称 */
	private static final String DEFAULT_OFFICE_NAME = "综合部";

	/** 默认租户（未启用多租户模型） */
	private static final String DEFAULT_CORP_CODE = "0";
	private static final String DEFAULT_CORP_NAME = "JeeSite";

	private final UserService userService;
	private final EmpUserService empUserService;
	private final CompanyService companyService;
	private final OfficeService officeService;
	private final CompanyDao companyDao;
	private final OfficeDao officeDao;

	public RegisterController(UserService userService, EmpUserService empUserService,
			CompanyService companyService, OfficeService officeService,
			CompanyDao companyDao, OfficeDao officeDao) {
		this.userService = userService;
		this.empUserService = empUserService;
		this.companyService = companyService;
		this.officeService = officeService;
		this.companyDao = companyDao;
		this.officeDao = officeDao;
	}

	/**
	 * 自助注册账号
	 */
	@PostMapping(value = "account")
	@ResponseBody
	@Operation(summary = "自助注册账号（公司名 + 手机号 + 联系人 + 密码）")
	public String register(User user, HttpServletRequest request) {
		try {
			return doRegister(user);
		} catch (Exception e) {
			logger.error("自助注册失败", e);
			return renderResult(Global.FALSE, text("注册失败：{0}", e.getMessage()));
		}
	}

	private String doRegister(User user) {
		// 手机号等同于登录账号
		String loginCode = StringUtils.trim(user.getMobile());
		String userName = StringUtils.trim(user.getUserName());
		String corpName = StringUtils.trim(user.getCorpName_());
		if (StringUtils.isBlank(corpName)) {
			return renderResult(Global.FALSE, text("请填写公司名称！"));
		}
		if (StringUtils.isBlank(loginCode)) {
			return renderResult(Global.FALSE, text("请填写手机号码！"));
		}
		if (StringUtils.isBlank(userName)) {
			return renderResult(Global.FALSE, text("请填写联系人姓名！"));
		}
		if (StringUtils.isBlank(user.getPassword())) {
			return renderResult(Global.FALSE, text("请设置登录密码！"));
		}
		if (UserUtils.getByLoginCode(loginCode, null) != null) {
			return renderResult(Global.FALSE, text("该手机号码已注册，请直接登录！"));
		}
		// 密码解密（前端 Base64 提交）
		String secretKey = Global.getProperty("shiro.loginSubmit.secretKey");
		String password = user.getPassword();
		if (StringUtils.isNotBlank(secretKey)) {
			password = DesUtils.decode(password, secretKey);
		}

		// 公司：按名称查找，不存在则创建
		Company company = findCompanyByName(corpName);
		if (company == null) {
			company = createCompany(corpName);
		}
		// 机构：该公司下默认「综合部」，不存在则创建
		Office office = findOfficeByCompany(company.getCompanyCode());
		if (office == null) {
			office = createOffice(company);
		}

		// 用户与员工
		EmpUser u = new EmpUser();
		u.setIsNewRecord(true);
		u.setLoginCode(loginCode);
		u.setUserName(userName);
		u.setMobile(loginCode);
		u.setPassword(password);
		u.setUserType(EmpUser.USER_TYPE_EMPLOYEE);
		u.setMgrType(EmpUser.MGR_TYPE_NOT_ADMIN);
		u.setStatus(DataEntity.STATUS_NORMAL);
		// 未启用多租户模型，用户归属默认租户；公司名挂在员工所属公司及机构上
		u.setCorpCode(CorpUtils.DEFAULT_CORP_CODE);
		u.setCorpName(CorpUtils.DEFAULT_CORP_NAME);
		Employee employee = u.getEmployee();
		employee.setEmpName(userName);
		employee.setCompany(company);
		employee.setOffice(office);
		u.setUserRoleString(DEFAULT_ROLE_CODE);

		empUserService.save(u);
		// 保存角色关系需要管理员身份，否则会被数据权限过滤
		u.currentUser(new User(User.SUPER_ADMIN_CODES[0]));
		userService.saveAuth(u);

		return renderResult(Global.TRUE, text("注册成功，请使用手机号码登录！"));
	}

	/** 按公司名称查找 */
	private Company findCompanyByName(String corpName) {
		Company where = new Company();
		where.setCompanyName(corpName);
		List<Company> list = companyDao.findList(where);
		return list.isEmpty() ? null : list.get(0);
	}

	/** 创建默认公司 */
	private Company createCompany(String corpName) {
		Company company = new Company();
		company.setIsNewRecord(true);
		company.setCompanyCode(IdGen.nextId());
		company.setViewCode(IdGen.randomShortString());
		company.setCompanyName(corpName);
		company.setFullName(corpName);
		company.setParentCode(TreeEntity.ROOT_CODE);
		company.setTreeSort(TreeEntity.DEFAULT_TREE_SORT);
		company.setStatus(DataEntity.STATUS_NORMAL);
		company.setCorpCode(DEFAULT_CORP_CODE);
		company.setCorpName(DEFAULT_CORP_NAME);
		companyService.save(company);
		return company;
	}

	/** 按公司查找默认机构 */
	private Office findOfficeByCompany(String companyCode) {
		Office where = new Office();
		where.setCompanyCode(companyCode);
		List<Office> list = officeDao.findList(where);
		for (Office office : list) {
			if (DEFAULT_OFFICE_NAME.equals(office.getOfficeName())) {
				return office;
			}
		}
		return null;
	}

	/** 在公司下创建默认机构 */
	private Office createOffice(Company company) {
		Office office = new Office();
		office.setIsNewRecord(true);
		office.setOfficeCode(IdGen.nextId());
		office.setViewCode(IdGen.randomShortString());
		office.setOfficeName(DEFAULT_OFFICE_NAME);
		office.setFullName(company.getCompanyName() + DEFAULT_OFFICE_NAME);
		office.setOfficeType("3"); // 3：部门
		office.setParentCode(TreeEntity.ROOT_CODE);
		office.setTreeSort(TreeEntity.DEFAULT_TREE_SORT);
		office.setStatus(DataEntity.STATUS_NORMAL);
		office.setCorpCode(company.getCorpCode());
		office.setCorpName(company.getCorpName());
		office.setCompanyCode(company.getCompanyCode());
		officeService.save(office);
		return office;
	}

}
