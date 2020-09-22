package com.hr.perm.co;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CJPASqlUtil;
import com.corsair.cjpa.util.CjpaUtil;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.DBPools;
import com.corsair.dbpool.util.CJSON;
import com.corsair.dbpool.util.JSONParm;
import com.corsair.server.base.CSContext;
import com.corsair.server.base.ConstsSw;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.generic.Shw_physic_file;
import com.corsair.server.generic.Shwuser;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CExcelField;
import com.corsair.server.util.CExcelUtil;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CorUtil;
import com.corsair.server.util.DictionaryTemp;
import com.corsair.server.util.UpLoadFileEx;
import com.hr.base.entity.Hr_orgposition;
import com.hr.inface.entity.WageSysTohr;
import com.hr.perm.ctr.CtrHr_employee_transfer;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_employee_transfer;
import com.hr.salary.ctr.CtrSalaryCommon;
import com.hr.util.HRUtil;

@ACO(coname = "web.hr.employeetransfer")
public class COHr_employee_transfer {

	@ACOAction(eventname = "findOrgPostions", Authentication = true, ispublic = false, notes = "查询机构职位")
	public String findOrgPostions() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String sqlwhere = parms.get("sqlwhere");
		sqlwhere = (sqlwhere == null) ? "" : sqlwhere;
		String disidpwstr = parms.get("disidpw");
		boolean disidpw = (disidpwstr == null) ? false : Boolean.valueOf(disidpwstr);
		String sqlstr = "select * from hr_orgposition where usable=1 ";
		if (!sqlwhere.isEmpty())
			sqlstr = sqlstr + " and " + sqlwhere;
		if (disidpw) {
			return (new CReport(sqlstr, null)).findReport(null, null);
		} else {
			return (new CReport(sqlstr, null)).findReport();
		}
	}

	@ACOAction(eventname = "findTransferlist", Authentication = true, ispublic = false, notes = "查询调动单")
	public String findTransferlist() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String eparms = parms.get("parms");
		List<JSONParm> jps = CJSON.getParms(eparms);
		Hr_employee_transfer ht = new Hr_employee_transfer();
		String where = CjpaUtil.buildFindSqlByJsonParms(new Hr_employee_transfer(), jps);
		String idpw = CSContext.getIdpathwhere().replace("idpath", "shworg.idpath");
		String sqlstr = "SELECT * FROM hr_employee_transfer WHERE 1=1 ";
		where = where + " and (EXISTS (SELECT 1 FROM shworg WHERE hr_employee_transfer.neworgid=shworg.orgid " + idpw + ") "
				+ "or EXISTS (SELECT 1 FROM shworg WHERE hr_employee_transfer.odorgid=shworg.orgid " + idpw + ")"
				+ ") ";
		if (!HRUtil.hasRoles("71")) {// 薪酬管理员
			sqlstr = sqlstr + " and employee_code='" + CSContext.getUserName() + "' ";
		}
		sqlstr = sqlstr + where;

		String[] ignParms = {};
		return new CReport(sqlstr, "createtime desc", null).findReport(ignParms, null);
		// JSONArray ems = ht.pool.opensql2json_O(sqlstr);
		// // for (int i = 0; i < ems.size(); i++) {
		// // JSONObject em = ems.getJSONObject(i);
		// // em.put("extorgname",
		// // COShwUser.getOrgNamepath(em.getString("orgid")));
		// // }
		// return ems.toString();
	}

	@ACOAction(eventname = "isneedsarryqcode", Authentication = true, ispublic = false, notes = "是否需要工资额度必填")
	public String isneedsarryqcode() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String odospid = CorUtil.hashMap2Str(parms, "odospid", "需要参数odospid");
		String newospid = CorUtil.hashMap2Str(parms, "newospid", "需要参数newospid");
		int tranftype3 = Integer.valueOf(CorUtil.hashMap2Str(parms, "tranftype3", "需要参数tranftype3"));// 内部
		int bt = CtrHr_employee_transfer.isneedsarryqcode(odospid, newospid, tranftype3);
		JSONObject rst = new JSONObject();
		rst.put("salary_qcnotnull", bt);
		return rst.toString();
	}

	@ACOAction(eventname = "findTransfer", Authentication = true, ispublic = false, notes = "替换通用查询")
	public String findTransfer() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String jpaclass = CorUtil.hashMap2Str(urlparms, "jpaclass", "需要参数jpaclass");
		String type = CorUtil.hashMap2Str(urlparms, "type", "需要参数type");

		String disidpath = urlparms.get("disidpath");
		boolean disi = (disidpath != null) ? Boolean.valueOf(disidpath) : false;
		disi = disi || (ConstsSw.getSysParmIntDefault("ALLCLIENTCHGIDPATH", 2) == 2);
		String sqlwhere = urlparms.get("sqlwhere");
		String selflines = urlparms.get("selfline");
		boolean selfline = (selflines != null) ? Boolean.valueOf(selflines) : true;
		if ("list".equalsIgnoreCase(type) || "tree".equalsIgnoreCase(type)) {
			selfline = false;
			String parms = urlparms.get("parms");
			String edittps = CorUtil.hashMap2Str(urlparms, "edittps", "需要参数edittps");
			String activeprocname = urlparms.get("activeprocname");

			HashMap<String, String> edts = CJSON.Json2HashMap(edittps);

			String smax = urlparms.get("max");
			String order = urlparms.get("order");

			String spage = urlparms.get("page");
			String srows = urlparms.get("rows");
			boolean needpage = false;// 是否需要分页
			int page = 0, rows = 0;
			if (spage == null) {
				if (smax == null) {
					needpage = false;
				} else {
					needpage = true;
					page = 1;
					rows = Integer.valueOf(smax);
				}
			} else {
				needpage = true;
				page = Integer.valueOf(spage);
				rows = (srows == null) ? 300 : Integer.valueOf(srows);
			}

			CJPALineData<CJPABase> jpas = CjpaUtil.newJPALinedatas(jpaclass);
			CJPA jpa = (CJPA) CjpaUtil.newJPAObjcet(jpaclass);

			List<JSONParm> jps = CJSON.getParms(parms);
			String where = CjpaUtil.buildFindSqlByJsonParms(jpa, jps);
			String idpw = CSContext.getIdpathwhere().replace("idpath", "shworg.idpath");
			if ((sqlwhere != null) && (sqlwhere.length() > 0))
				where = where + " and " + sqlwhere + " ";
			where = where + "  and ((EXISTS (SELECT 1 FROM shworg WHERE hr_employee_transfer.odorgid=shworg.orgid " + idpw + ")) "
					+ " OR "
					+ " (EXISTS (SELECT 1 FROM shworg WHERE hr_employee_transfer.neworgid=shworg.orgid " + idpw + "))) ";

			if (jpa.getPublicControllerBase() != null) {
				String w = ((JPAController) jpa.getPublicControllerBase()).OnCCoFindBuildWhere(jpa, urlparms);
				if (w != null)
					where = where + " " + w;
			}

			if (jpa.getController() != null) {
				String w = ((JPAController) jpa.getController()).OnCCoFindBuildWhere(jpa, urlparms);
				if (w != null)
					where = where + " " + w;
			}

			// edittps:{"isedit":true,"issubmit":true,"isview":true,"isupdate":false,"isfind":true}
			if (jpa.cfieldbycfieldname("stat") != null) {
				String sqlstat = "";
				if (Obj2Bl(edts.get("isedit")))
					sqlstat = sqlstat + " (stat=1) or";
				if (Obj2Bl(edts.get("issubmit"))) {
					sqlstat = sqlstat + " (stat>1 and stat<9) or";
					// 去掉 在流程中、当前节点为数据保护节点 且 当前 登录用户不在 当前节点
				}
				if (Obj2Bl(edts.get("isview"))) // 查询所有单据 包含作废的
					sqlstat = sqlstat + " ( 1=1 ) or";
				if (Obj2Bl(edts.get("isupdate")) || Obj2Bl(edts.get("isfind")))
					sqlstat = sqlstat + " (stat=9) or";
				if (sqlstat.length() > 0) {
					sqlstat = sqlstat.substring(1, sqlstat.length() - 2);
					where = where + " and (" + sqlstat + ")";
				}
			}
			if ((activeprocname != null) && (!activeprocname.isEmpty())) {
				String idfd = jpa.getIDField().getFieldname();
				String ew = "SELECT " + idfd + " FROM " + jpa.tablename + " t,shwwf wf,shwwfproc wfp"
						+ " WHERE t.stat>1 AND t.stat<9 AND t.wfid=wf.wfid AND wf.wfid=wfp.wfid "
						+ "  AND wfp.stat=2 AND wfp.procname='" + activeprocname + "'";
				ew = " and " + idfd + " in (" + ew + ")";
				where = where + ew;
			}
			String sqltr = null;

			String textfield = urlparms.get("textfield");
//			String pidfd = null;
//			if ("tree".equalsIgnoreCase(type))
//				pidfd = CorUtil.hashMap2Str(urlparms, "parentid", "需要参数parentid");
//
//			if (("tree".equalsIgnoreCase(type)) && (textfield != null) && (textfield.length() > 0)) {
//				String idfd = jpa.getIDField().getFieldname();
//				sqltr = "select " + idfd + " as id," + textfield +
//						" as text," + idfd + "," + textfield + "," + pidfd
//						+ ",a.* from " + jpa.tablename + " a where 1=1 " + where;
//			} else
			sqltr = "select * from " + jpa.tablename + " where 1=1 " + where;
			sqltr = sqltr + " order by tranfcmpdate desc,appdate desc";
//			if ((order != null) && (!order.isEmpty())) {
//				sqltr = sqltr + " order by " + order;
//			} else
//				sqltr = sqltr + " order by " + jpa.getIDFieldName() + " desc ";
			if (jpa.getPublicControllerBase() != null) {
				String rst = ((JPAController) jpa.getPublicControllerBase()).OnCCoFindList((Class<CJPA>) jpa.getClass(), jps, edts, disi, selfline);
				if (rst != null)
					return rst;
			}
			if (jpa.getController() != null) {
				String rst = ((JPAController) jpa.getController()).OnCCoFindList((Class<CJPA>) jpa.getClass(), jps, edts, disi, selfline);
				if (rst != null)
					return rst;
			}
			if ("list".equalsIgnoreCase(type)) {
				String scols = urlparms.get("cols");
				if (scols != null) {
					String[] ignParms = new String[] {};
					new CReport(sqltr, null).export2excel(ignParms, scols, null);
					return null;
				} else {
					if (!needpage) {
						JSONArray js = jpa.pool.opensql2json_O(sqltr);
						// JSONArray js = DBPools.poolByName("dlhrmycatread").opensql2json_O(sqltr);
						if (jpa.getController() != null) {
							String rst = ((JPAController) jpa.getController()).AfterCOFindList((Class<CJPA>) jpa.getClass(), js, 0, 0);
							if (rst != null)
								return rst;
						}
						return js.toString();
					} else {
						// JSONObject jo = DBPools.poolByName("dlhrmycatread").opensql2json_O(sqltr, page, rows);
						JSONObject jo = jpa.pool.opensql2json_O(sqltr, page, rows);
						if (jpa.getController() != null) {
							String rst = ((JPAController) jpa.getController()).AfterCOFindList((Class<CJPA>) jpa.getClass(), jo, page, rows);
							if (rst != null)
								return rst;
						}
						return jo.toString();
					}
				}
			}
//			if ("tree".equalsIgnoreCase(type)) {
//				return jpa.pool.opensql2jsontree(sqltr, jpa.getIDField().getFieldname(), pidfd, false);
//			}
		}
		// //by id
		if ("byid".equalsIgnoreCase(type)) {
			String id = CorUtil.hashMap2Str(urlparms, "id", "需要参数id");
			CJPA jpa = (CJPA) CjpaUtil.newJPAObjcet(jpaclass);
			if (jpa.getPublicControllerBase() != null) {
				String rst = ((JPAController) jpa.getPublicControllerBase()).OnCCoFindByID((Class<CJPA>) jpa.getClass(), id);
				if (rst != null)
					return rst;
			}
			if (jpa.getController() != null) {
				String rst = ((JPAController) jpa.getController()).OnCCoFindByID((Class<CJPA>) jpa.getClass(), id);
				if (rst != null)
					return rst;
			}

			CField idfd = jpa.getIDField();
			if (idfd == null) {
				throw new Exception("根据ID查询JPA<" + jpa.getClass().getSimpleName() + ">数据没发现ID字段");
			}
			String sqlfdname = CJPASqlUtil.getSqlField(jpa.pool.getDbtype(), idfd.getFieldname());
			String sqlvalue = CJPASqlUtil.getSqlValue(jpa.pool.getDbtype(), idfd.getFieldtype(), id);
			String sqlstr = "select * from " + jpa.tablename + " where " + sqlfdname + "=" + sqlvalue;
			// if ((chgi) && (userIsInside())) {
			// sqlstr = sqlstr + " and idpath like '1,%'";
			// } else if (dfi && (jpa.cfield("idpath") != null))
			// sqlstr = sqlstr + CSContext.getIdpathwhere();
			jpa.findBySQL(sqlstr, selfline);
			if (jpa.isEmpty())
				return "{}";
			else
				return jpa.tojson();
		}
		return "";
	}

	private static boolean Obj2Bl(Object o) {
		if (o == null)
			return false;
		return Boolean.valueOf(o.toString());
	}

	public static boolean hasaccrole37() throws Exception {
		String userid = CSContext.getUserID();
		Shwuser user = new Shwuser();
		user.findByID(userid, false);
		if (user.isEmpty())
			throw new Exception("获取当前登录用户错误!");
		if (user.usertype.getAsIntDefault(0) != 1) {// 不是管理员
			String sqlstr = "SELECT IFNULL(COUNT(*),0) ct FROM shwroleuser where roleid=37 AND userid=" + userid; // 调动维护人员
			return (Integer.valueOf(DBPools.defaultPool().openSql2List(sqlstr).get(0).get("ct")) > 0);
		} else
			return true;
	}

	@ACOAction(eventname = "impexcel", Authentication = true, ispublic = false, notes = "导入Excel")
	public String impexcel() throws Exception {
		// 权限检查
		if (!hasaccrole37())
			throw new Exception("当前登录用户无此权限");

		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String batchno = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");// 批次号
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		int rst = 0;
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			rst = parserExcelFile(p, batchno);
			for (CJPABase pfb : pfs) {
				Shw_physic_file pf = (Shw_physic_file) pfb;
				UpLoadFileEx.delAttFile(pf.pfid.getValue());
			}
		}
		JSONObject jo = new JSONObject();
		jo.put("rst", rst);
		jo.put("batchno", batchno);
		return jo.toString();
	}

	private int parserExcelFile(Shw_physic_file pf, String batchno) throws Exception {
		String fs = System.getProperty("file.separator");
		String fullname = ConstsSw.geAppParmStr("UDFilePath") + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
		File file = new File(fullname);
		if (!file.exists()) {
			fullname = ConstsSw._root_filepath + "attifiles" + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
			file = new File(fullname);
			if (!file.exists())
				throw new Exception("文件" + fullname + "不存在!");
		}

		// Workbook workbook = CExcelUtil.isExcel2003(fullname) ? new HSSFWorkbook(new FileInputStream(fullname))
		// : new XSSFWorkbook(new FileInputStream(fullname));

		Workbook workbook = WorkbookFactory.create(file);

		int sn = workbook.getNumberOfSheets();
		if (sn <= 0)
			throw new Exception("excel<" + fullname + ">没有sheet");
		Sheet aSheet = workbook.getSheetAt(0);// 获得一个sheet
		return parserExcelSheet(aSheet, batchno);
	}

	private int parserExcelSheet(Sheet aSheet, String batchno) throws Exception {
		if (aSheet.getLastRowNum() == 0) {
			return 0;
		}
		List<CExcelField> efds = initExcelFields();
		efds = CExcelUtil.parserExcelSheetFields(aSheet, efds, 0);// 解析title 并检查必须存在的列
		List<Map<String, String>> values = CExcelUtil.getExcelValues(aSheet, efds, 0);
		Hr_employee emp = new Hr_employee();
		Hr_orgposition osp = new Hr_orgposition();
		Hr_employee_transfer tr = new Hr_employee_transfer();
		CDBConnection con = emp.pool.getCon(this);
		DictionaryTemp dictemp = new DictionaryTemp();// 数据字典缓存
		con.startTrans();
		int rst = 0;
		try {
			for (Map<String, String> v : values) {
				String employee_code = v.get("employee_code");
				if ((employee_code == null) || (employee_code.isEmpty()))
					continue;
				rst++;
				emp.clear();
				emp.findBySQL("SELECT * FROM hr_employee WHERE employee_code='" + employee_code + "'");
				if (emp.isEmpty())
					throw new Exception("工号【" + employee_code + "】不存在人事资料");
				osp.clear();
				osp.findBySQL("SELECT * FROM hr_orgposition WHERE ospcode='" + v.get("newospcode") + "'", false);
				if (osp.isEmpty())
					throw new Exception("工号【" + employee_code + "】的机构职位编码【" + v.get("newospcode") + "】不存在");
				if (osp.usable.getAsIntDefault(0) == 2)
					throw new Exception("工号【" + employee_code + "】的机构职位编码【" + v.get("newospcode") + "】不可用");

				tr.clear();

				tr.er_id.setValue(emp.er_id.getValue()); // 人事ID
				tr.employee_code.setValue(emp.employee_code.getValue()); // 工号
				tr.id_number.setValue(emp.id_number.getValue()); // 身份证号
				tr.employee_name.setValue(emp.employee_name.getValue()); // 姓名
				tr.mnemonic_code.setValue(emp.mnemonic_code.getValue()); // 助记码
				tr.email.setValue(emp.email.getValue()); // 邮箱/微信
				tr.empstatid.setValue(emp.empstatid.getValue()); // 人员状态
				tr.telphone.setValue(emp.telphone.getValue()); // 电话
				tr.tranfcmpdate.setValue(v.get("tranfcmpdate")); // 调动时间
				tr.hiredday.setValue(emp.hiredday.getValue()); // 聘用日期
				tr.degree.setValue(emp.degree.getValue()); // 学历
				tr.tranflev.setValue(dictemp.getVbCE("1215", v.get("tranflev"), false, "工号【" + employee_code + "】调动层级【" + v.get("tranflev") + "】不存在")); // 调动层级
				tr.tranftype1.setValue(dictemp.getVbCE("719", v.get("tranftype1"), false, "工号【" + employee_code + "】调动类别【" + v.get("tranftype1") + "】不存在")); // 调动类别
																																								// 1晋升调动
																																								// 2降职调动
																																								// 3同职种平级调动
																																								// 4跨职种平级调动
				tr.tranftype2.setValue(dictemp.getVbCE("724", v.get("tranftype2"), false, "工号【" + employee_code + "】调动性质【" + v.get("tranftype2") + "】不存在")); // 调动性质
																																								// 1公司安排
																																								// 2个人申请
				// 3梦职场调动
				String tranftype3 = dictemp.getVbCE("729", v.get("tranftype3"), false, "工号【" + employee_code + "】调动范围【" + v.get("tranftype3") + "】不存在"); // 4内部招聘
				// System.out.println("tranftype3:" + tranftype3);
				tr.tranftype3.setValue(tranftype3); // 调动范围// 1内部调用// 2// 跨单位// 3// 跨模块/制造群
				// System.out.println(tr.toString());

				tr.tranfreason.setValue(v.get("tranfreason")); // 调动原因
				tr.probation.setValue(v.get("probation")); // 考察期
				tr.probationdate.setValue(v.get("probationdate")); // 考察到期日期
				tr.ispromotioned.setValue("2"); // 已转正
				tr.odorgid.setValue(emp.orgid.getValue()); // 调动前部门ID
				tr.odorgcode.setValue(emp.orgcode.getValue()); // 调动前部门编码
				tr.odorgname.setValue(emp.orgname.getValue()); // 调动前部门名称
				tr.odorghrlev.setValue("0"); // 调调动前部门人事层级
				tr.odlv_id.setValue(emp.lv_id.getValue()); // 调动前职级ID
				tr.odlv_num.setValue(emp.lv_num.getValue()); // 调动前职级
				tr.odhg_id.setValue(emp.hg_id.getValue()); // 调动前职等ID
				tr.odhg_code.setValue(emp.hg_code.getValue()); // 调动前职等编码
				tr.odhg_name.setValue(emp.hg_name.getValue()); // 调动前职等名称
				tr.odospid.setValue(emp.ospid.getValue()); // 调动前职位ID
				tr.odospcode.setValue(emp.ospcode.getValue()); // 调动前职位编码
				tr.odsp_name.setValue(emp.sp_name.getValue()); // 调动前职位名称
				tr.oldemnature.setValue(emp.emnature.getValue());// 调动前职位性质
				tr.odattendtype.setValue(emp.atdtype.getValue()); // 调动前出勤类别
				tr.oldcalsalarytype.setValue(emp.pay_way.getValue()); // 调动前计薪方式
				tr.oldhwc_namezl.setValue(emp.hwc_namezl.getValue()); // 调动前职类
				tr.oldposition_salary.setValue("0"); // 调动前职位工资
				tr.oldbase_salary.setValue("0"); // 调动前基本工资
				tr.oldtech_salary.setValue("0"); // 调动前技能工资
				tr.oldachi_salary.setValue("0"); // 调动前技能工资
				tr.oldtech_allowance.setValue("0"); // 调动前技术津贴
				tr.oldavg_salary.setValue("0"); // 调动前平均工资
				tr.neworgid.setValue(osp.orgid.getValue()); // 调动后部门ID
				tr.neworgcode.setValue(osp.orgcode.getValue()); // 调动后部门编码
				tr.neworgname.setValue(osp.orgname.getValue()); // 调动后部门名称
				tr.neworghrlev.setValue("0"); // 调动后部门人事层级
				tr.newlv_id.setValue(osp.lv_id.getValue()); // 调动后职级ID
				tr.newlv_num.setValue(osp.lv_num.getValue()); // 调动后职级
				tr.newhg_id.setValue(osp.hg_id.getValue()); // 调动后职等ID
				tr.newhg_code.setValue(osp.hg_code.getValue()); // 调动后职等编码
				tr.newhg_name.setValue(osp.hg_name.getValue()); // 调动后职等名称
				tr.newospid.setValue(osp.ospid.getValue()); // 调动后职位ID
				tr.newospcode.setValue(osp.ospcode.getValue()); // 调动后职位编码
				tr.newsp_name.setValue(osp.sp_name.getValue()); // 调动后职位名称
				String newemnature = (osp.isoffjob.getAsIntDefault(0) == 1) ? "脱产" : "非脱产";
				tr.newemnature.setValue(newemnature);// 调动后职位性质
				tr.newattendtype.setValue(emp.atdtype.getValue()); // 调动后出勤类别
				tr.newcalsalarytype.setValue(emp.pay_way.getValue()); // 调动后计薪方式
				tr.newhwc_namezl.setValue(osp.hwc_namezl.getValue()); // 调动后职类
				tr.newposition_salary.setValue("0"); // 调动前职位工资
				tr.newbase_salary.setValue("0"); // 调动后基本工资
				tr.newtech_salary.setValue("0"); // 调动后技能工资
				tr.newachi_salary.setValue("0"); // 调动后技能工资
				tr.newtech_allowance.setValue("0"); // 调动后技术津贴
				tr.newavg_salary.setValue("0"); // 调动后平均工资
				tr.salary_quotacode.setValue("0"); // 可用工资额度证明编号
				tr.salary_quota_stand.setValue("0"); // 标准工资额度
				tr.salary_quota_canuse.setValue("0"); // 可用工资额度
				tr.salary_quota_used.setValue("0"); // 己用工资额度
				tr.salary_quota_blance.setValue("0"); // 可用工资余额
				tr.salary_quota_appy.setValue("0"); // 申请工资额度
				tr.salary_quota_ovsp.setValue("0"); // 超额度审批
				tr.salarydate.setValue(null); // 核薪生效日期
				tr.istp.setValue(dictemp.getVbCE("5", v.get("istp"), false, "工号【" + employee_code + "】是否词汇【" + v.get("istp") + "】不存在")); // 是否特批
				tr.tranamt.setValue(v.get("tranamt")); // 调拨金额

				tr.remark.setValue(v.get("remark")); // 备注
				tr.quota_over.setValue("2"); // 是否超编
				tr.quota_over_rst.setValue("2"); // 超编审批结果 1 允许增加编制调动 ps是否自动生成编制调整单 2 超编调动 3 不允许调动
				tr.isdreamposition.setValue("2"); // 是否梦职场调入
				tr.isdreamemploye.setValue("2"); // 是否梦职场储备员工
				tr.tranftype4.setValue(dictemp.getVbCE("1189", v.get("tranftype4"), false, "工号【" + employee_code + "】调动类型【" + v.get("tranftype4") + "】不存在")); // 调动类型
																																								// 1M类调动
																																								// 2 高技调动
																																								//3梦职场调动
																																								// 4其他调动
				tr.isexistsrl.setValue("2"); // 关联关系 有关联关系 无关联关系
				tr.rlmgms.setValue("1"); // 管控措施 不需管控 终止调动 申请豁免
				tr.ismangerl.setValue("2"); // 是否构成需要管控的管理关系类别
				tr.isapprlhm.setValue(null); // 是否申请关联关系豁免
				tr.isapprlhmsp.setValue(null); // 关联关系豁免申请是否得到审批
				tr.quotastand.setValue(osp.quota.getValue()); // 标准配置人数
				tr.quotasj.setValue("0"); // 实际配置人数
				tr.quotacq.setValue("0"); // 超缺编人数(正数表示超编、负数表示缺编)
				tr.isallowdrmin.setValue(null); // 是否同意特批调动至梦职场职位
				tr.idpath.setValue(emp.idpath.getValue()); // idpath
				tr.attribute1.setAsInt(2);// 批量导入的单据
				tr.save(con);
				tr.wfcreate(null, con);
				// throw new Exception("1111");
			}
			con.submit();
			return rst;
		} catch (Exception e) {
			con.rollback();
			throw e;
		} finally {
			con.close();
		}
	}

	private List<CExcelField> initExcelFields() {
		List<CExcelField> efields = new ArrayList<CExcelField>();
		efields.add(new CExcelField("工号", "employee_code", true));
		efields.add(new CExcelField("调动范围", "tranftype3", true));
		efields.add(new CExcelField("调动层级", "tranflev", true));
		efields.add(new CExcelField("调动性质", "tranftype2", true));
		efields.add(new CExcelField("调动类型", "tranftype4", true));
		efields.add(new CExcelField("调动类别", "tranftype1", true));
		efields.add(new CExcelField("考察期", "probation", true));
		efields.add(new CExcelField("调动生效日期", "tranfcmpdate", true));
		efields.add(new CExcelField("待转正日期", "probationdate", true));
		efields.add(new CExcelField("调动后机构职位编码", "newospcode", true));
		efields.add(new CExcelField("是否特批", "istp", true));
		efields.add(new CExcelField("调拨金额", "tranamt", true));
		efields.add(new CExcelField("调动原因", "tranfreason", false));
		efields.add(new CExcelField("备注", "remark", false));
		return efields;
	}

	@ACOAction(eventname = "imphisexcel", Authentication = true, ispublic = false, notes = "历史导入Excel")
	public String imphisexcel() throws Exception {
		// 权限检查
		if (!hasaccrole37())
			throw new Exception("当前登录用户无此权限");

		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String batchno = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");// 批次号
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		int rst = 0;
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			rst = parserHisExcelFile(p, batchno);
			for (CJPABase pfb : pfs) {
				Shw_physic_file pf = (Shw_physic_file) pfb;
				UpLoadFileEx.delAttFile(pf.pfid.getValue());
			}
		}
		JSONObject jo = new JSONObject();
		jo.put("rst", rst);
		jo.put("batchno", batchno);
		return jo.toString();
	}

	private int parserHisExcelFile(Shw_physic_file pf, String batchno) throws Exception {
		String fs = System.getProperty("file.separator");
		String fullname = ConstsSw.geAppParmStr("UDFilePath") + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
		File file = new File(fullname);
		if (!file.exists()) {
			fullname = ConstsSw._root_filepath + "attifiles" + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
			file = new File(fullname);
			if (!file.exists())
				throw new Exception("文件" + fullname + "不存在!");
		}
		// Workbook workbook = CExcelUtil.isExcel2003(fullname) ? new HSSFWorkbook(new FileInputStream(fullname))
		// : new XSSFWorkbook(new FileInputStream(fullname));

		Workbook workbook = WorkbookFactory.create(file);

		int sn = workbook.getNumberOfSheets();
		if (sn <= 0)
			throw new Exception("excel<" + fullname + ">没有sheet");
		Sheet aSheet = workbook.getSheetAt(0);// 获得一个sheet
		return parserHisExcelSheet(aSheet, batchno);
	}

	private int parserHisExcelSheet(Sheet aSheet, String batchno) throws Exception {
		if (aSheet.getLastRowNum() == 0) {
			return 0;
		}
		List<CExcelField> efds = initHisExcelFields();
		efds = CExcelUtil.parserExcelSheetFields(aSheet, efds, 0);// 解析title 并检查必须存在的列
		List<Map<String, String>> values = CExcelUtil.getExcelValues(aSheet, efds, 0);
		Hr_employee emp = new Hr_employee();
		Hr_orgposition oldosp = new Hr_orgposition();
		Hr_orgposition newosp = new Hr_orgposition();
		Hr_employee_transfer tr = new Hr_employee_transfer();
		CDBConnection con = emp.pool.getCon(this);
		DictionaryTemp dictemp = new DictionaryTemp();// 数据字典缓存
		con.startTrans();
		int rst = 0;
		try {
			for (Map<String, String> v : values) {
				String employee_code = v.get("employee_code");
				if ((employee_code == null) || (employee_code.isEmpty()))
					continue;
				rst++;
				emp.clear();
				emp.findBySQL("SELECT * FROM hr_employee WHERE employee_code='" + employee_code + "'");
				if (emp.isEmpty())
					throw new Exception("工号【" + employee_code + "】不存在人事资料");

				oldosp.clear();
				oldosp.findBySQL("SELECT * FROM hr_orgposition WHERE ospcode='" + v.get("odospcode") + "'", false);
				if (oldosp.isEmpty())
					throw new Exception("工号【" + employee_code + "】的调动前机构职位编码【" + v.get("odospcode") + "】不存在");

				newosp.clear();
				newosp.findBySQL("SELECT * FROM hr_orgposition WHERE ospcode='" + v.get("newospcode") + "'", false);
				if (newosp.isEmpty())
					throw new Exception("工号【" + employee_code + "】的调动后机构职位编码【" + v.get("newospcode") + "】不存在");
				// if (newosp.usable.getAsIntDefault(0) == 2) 19.3.21 历史记录 职位不可用也没关系
				// throw new Exception("工号【" + employee_code + "】的调动后机构职位编码【" + v.get("newospcode") + "】不可用");

				tr.clear();
				tr.er_id.setValue(emp.er_id.getValue()); // 人事ID
				tr.employee_code.setValue(emp.employee_code.getValue()); // 工号
				tr.id_number.setValue(emp.id_number.getValue()); // 身份证号
				tr.employee_name.setValue(emp.employee_name.getValue()); // 姓名
				tr.mnemonic_code.setValue(emp.mnemonic_code.getValue()); // 助记码
				tr.email.setValue(emp.email.getValue()); // 邮箱/微信
				tr.empstatid.setValue(emp.empstatid.getValue()); // 人员状态
				tr.telphone.setValue(emp.telphone.getValue()); // 电话
				tr.tranfcmpdate.setValue(v.get("tranfcmpdate")); // 调动时间
				tr.hiredday.setValue(emp.hiredday.getValue()); // 聘用日期
				tr.degree.setValue(emp.degree.getValue()); // 学历
				tr.tranflev.setValue(dictemp.getVbCE("1215", v.get("tranflev"), true, "工号【" + employee_code + "】调动层级【" + v.get("tranflev") + "】不存在")); // 调动层级
				tr.tranftype1.setValue(dictemp.getVbCE("719", v.get("tranftype1"), true, "工号【" + employee_code + "】调动类别【" + v.get("tranftype1") + "】不存在")); // 调动类别
																																							// 1晋升调动
																																							// 2降职调动
																																							// 3同职种平级调动
																																							// 4跨职种平级调动
				tr.tranftype2.setValue(dictemp.getVbCE("724", v.get("tranftype2"), true, "工号【" + employee_code + "】调动性质【" + v.get("tranftype2") + "】不存在")); // 调动性质
																																							// 1公司安排
																																							// 2个人申请
																																							// 3梦职场调动
																																							// 4内部招聘
				tr.tranftype3.setValue(dictemp.getVbCE("729", v.get("tranftype3"), true, "工号【" + employee_code + "】调动范围【" + v.get("tranftype3") + "】不存在")); // 调动范围
																																							// 1内部调用
																																							// 2
																																							// 跨单位
																																							// 3
																																							// 跨模块/制造群
				tr.tranfreason.setValue(v.get("tranfreason")); // 调动原因
				tr.probation.setValue(v.get("probation")); // 考察期
				tr.probationdate.setValue(v.get("probationdate")); // 考察到期日期
				tr.ispromotioned.setValue("1"); // 已转正
				tr.odorgid.setValue(oldosp.orgid.getValue()); // 调动前部门ID
				tr.odorgcode.setValue(oldosp.orgcode.getValue()); // 调动前部门编码
				tr.odorgname.setValue(oldosp.orgname.getValue()); // 调动前部门名称
				tr.odorghrlev.setValue("0"); // 调调动前部门人事层级
				tr.odlv_id.setValue(oldosp.lv_id.getValue()); // 调动前职级ID
				tr.odlv_num.setValue(oldosp.lv_num.getValue()); // 调动前职级
				tr.odhg_id.setValue(oldosp.hg_id.getValue()); // 调动前职等ID
				tr.odhg_code.setValue(oldosp.hg_code.getValue()); // 调动前职等编码
				tr.odhg_name.setValue(oldosp.hg_name.getValue()); // 调动前职等名称
				tr.odospid.setValue(oldosp.ospid.getValue()); // 调动前职位ID
				tr.odospcode.setValue(oldosp.ospcode.getValue()); // 调动前职位编码
				tr.odsp_name.setValue(oldosp.sp_name.getValue()); // 调动前职位名称
				tr.oldemnature.setValue(emp.emnature.getValue());// 调动前职位性质
				tr.odattendtype.setValue(emp.atdtype.getValue()); // 调动前出勤类别
				tr.oldcalsalarytype.setValue("0"); // 调动前计薪方式
				tr.oldhwc_namezl.setValue(oldosp.hwc_namezl.getValue()); // 调动前职类
				tr.oldposition_salary.setValue("0"); // 调动前职位工资
				tr.oldbase_salary.setValue("0"); // 调动前基本工资
				tr.oldtech_salary.setValue("0"); // 调动前技能工资
				tr.oldachi_salary.setValue("0"); // 调动前技能工资
				tr.oldtech_allowance.setValue("0"); // 调动前技术津贴
				tr.oldavg_salary.setValue("0"); // 调动前平均工资
				tr.neworgid.setValue(newosp.orgid.getValue()); // 调动后部门ID
				tr.neworgcode.setValue(newosp.orgcode.getValue()); // 调动后部门编码
				tr.neworgname.setValue(newosp.orgname.getValue()); // 调动后部门名称
				tr.neworghrlev.setValue("0"); // 调动后部门人事层级
				tr.newlv_id.setValue(newosp.lv_id.getValue()); // 调动后职级ID
				tr.newlv_num.setValue(newosp.lv_num.getValue()); // 调动后职级
				tr.newhg_id.setValue(newosp.hg_id.getValue()); // 调动后职等ID
				tr.newhg_code.setValue(newosp.hg_code.getValue()); // 调动后职等编码
				tr.newhg_name.setValue(newosp.hg_name.getValue()); // 调动后职等名称
				tr.newospid.setValue(newosp.ospid.getValue()); // 调动后职位ID
				tr.newospcode.setValue(newosp.ospcode.getValue()); // 调动后职位编码
				tr.newsp_name.setValue(newosp.sp_name.getValue()); // 调动后职位名称
				String newemnature = (newosp.isoffjob.getAsIntDefault(0) == 1) ? "脱产" : "非脱产";
				tr.newemnature.setValue(newemnature);// 调动后职位性质
				tr.newattendtype.setValue("0"); // 19.3.21 取消 调动后出勤类别 dictemp.getVbCE("1399", v.get("newattendtype"), true, "工号【" + employee_code + "】调动后出勤类别【" + v.get("newattendtype") + "】不存在")
				tr.newcalsalarytype.setValue("0"); // 调动后计薪方式
				tr.newhwc_namezl.setValue(newosp.hwc_idzl.getValue()); // 调动后职类
				tr.newposition_salary.setValue("0"); // 调动前职位工资
				tr.newbase_salary.setValue("0"); // 调动后基本工资
				tr.newtech_salary.setValue("0"); // 调动后技能工资
				tr.newachi_salary.setValue("0"); // 调动后技能工资
				tr.newtech_allowance.setValue("0"); // 调动后技术津贴
				tr.newavg_salary.setValue("0"); // 调动后平均工资
				tr.salary_quotacode.setValue("0"); // 可用工资额度证明编号
				tr.salary_quota_stand.setValue("0"); // 标准工资额度
				tr.salary_quota_canuse.setValue("0"); // 可用工资额度
				tr.salary_quota_used.setValue("0"); // 己用工资额度
				tr.salary_quota_blance.setValue("0"); // 可用工资余额
				tr.salary_quota_appy.setValue("0"); // 申请工资额度
				tr.salary_quota_ovsp.setValue("0"); // 超额度审批
				tr.salarydate.setValue(null); // 核薪生效日期
				tr.istp.setValue(dictemp.getVbCE("5", v.get("istp"), true, "工号【" + employee_code + "】是否词汇【" + v.get("istp") + "】不存在")); // 是否特批
				tr.tranamt.setValue(v.get("tranamt")); // 调拨金额

				tr.remark.setValue(v.get("remark")); // 备注
				tr.quota_over.setValue("2"); // 是否超编
				tr.quota_over_rst.setValue("2"); // 超编审批结果 1 允许增加编制调动 ps是否自动生成编制调整单 2 超编调动 3 不允许调动
				tr.isdreamposition.setValue("2"); // 是否梦职场调入
				tr.isdreamemploye.setValue("2"); // 是否梦职场储备员工
				tr.tranftype4.setValue(dictemp.getVbCE("1189", v.get("tranftype4"), true, "工号【" + employee_code + "】调动类型【" + v.get("tranftype4") + "】不存在")); // 调动类型
																																								// 4其他调动
				tr.isexistsrl.setValue("2"); // 关联关系 有关联关系 无关联关系
				tr.rlmgms.setValue("1"); // 管控措施 不需管控 终止调动 申请豁免
				tr.ismangerl.setValue("2"); // 是否构成需要管控的管理关系类别
				tr.isapprlhm.setValue(null); // 是否申请关联关系豁免
				tr.isapprlhmsp.setValue(null); // 关联关系豁免申请是否得到审批
				tr.quotastand.setValue("0"); // 标准配置人数
				tr.quotasj.setValue("0"); // 实际配置人数
				tr.quotacq.setValue("0"); // 超缺编人数(正数表示超编、负数表示缺编)
				tr.isallowdrmin.setValue(null); // 是否同意特批调动至梦职场职位
				tr.idpath.setValue(emp.idpath.getValue()); // idpath
				tr.stat.setAsInt(9);
				tr.attribute3.setValue(batchno);
				tr.save(con);
			}
			con.submit();
			return rst;
		} catch (Exception e) {
			con.rollback();
			throw e;
		} finally {
			con.close();
		}
	}

	private List<CExcelField> initHisExcelFields() {
		List<CExcelField> efields = new ArrayList<CExcelField>();
		efields.add(new CExcelField("工号", "employee_code", true));
		efields.add(new CExcelField("调动范围", "tranftype3", true));
		efields.add(new CExcelField("调动层级", "tranflev", true));
		efields.add(new CExcelField("调动性质", "tranftype2", true));
		efields.add(new CExcelField("调动类型", "tranftype4", true));
		efields.add(new CExcelField("调动类别", "tranftype1", true));
		efields.add(new CExcelField("考察期", "probation", true));
		efields.add(new CExcelField("调动生效日期", "tranfcmpdate", true));
		efields.add(new CExcelField("待转正日期", "probationdate", true));
		efields.add(new CExcelField("调动前机构职位编码", "odospcode", true));
		efields.add(new CExcelField("调动后机构职位编码", "newospcode", true));
		efields.add(new CExcelField("调动后出勤类别", "newattendtype", false));
		efields.add(new CExcelField("是否特批", "istp", true));
		efields.add(new CExcelField("调拨金额", "tranamt", true));
		efields.add(new CExcelField("调动原因", "tranfreason", false));
		efields.add(new CExcelField("备注", "remark", false));
		return efields;
	}

	@ACOAction(eventname = "findWageSysTohr", Authentication = true, ispublic = false, notes = "工资额度查询")
	public String findWageSysTohr() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String salary_quotacode = CorUtil.hashMap2Str(parms, "salary_quotacode", "需要参数salary_quotacode");
		//String type = CorUtil.hashMap2Str(parms, "type", "需要参数type");
		String employee_code = CorUtil.hashMap2Str(parms, "employee_code", "需要参数employee_code");
//		WageSysTohr wst = new WageSysTohr();
//		String sqlstr = "select * from dbo.wageSysTohr where DocNo='" + salary_quotacode + "' and Status='完成'";
//		wst.findBySQL(sqlstr);
//		if (wst.isEmpty())
//			throw new Exception("编码为【" + salary_quotacode + "】的额度不存在");
//		System.out.println("wst:" + wst.toString());
//		return wst.tojson();
		return CtrSalaryCommon.getWageSys(salary_quotacode,employee_code).toString();
	}
}
