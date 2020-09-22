package com.hr.perm.co;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.CJPABase.CJPAStat;
import com.corsair.cjpa.util.CjpaUtil;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.DBPools;
import com.corsair.dbpool.util.CJSON;
import com.corsair.dbpool.util.JSONParm;
import com.corsair.dbpool.util.Logsw;
import com.corsair.dbpool.util.PraperedSql;
import com.corsair.dbpool.util.PraperedValue;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.base.ConstsSw;
import com.corsair.server.genco.COShwUser;
import com.corsair.server.generic.Shw_attach;
import com.corsair.server.generic.Shw_attach_line;
import com.corsair.server.generic.Shw_physic_file;
import com.corsair.server.generic.Shworg;
import com.corsair.server.generic.Shwuser;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CExcelField;
import com.corsair.server.util.CExcelUtil;
import com.corsair.server.util.CFileUtiil;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CSearchForm;
import com.corsair.server.util.CorUtil;
import com.corsair.server.util.DictionaryTemp;
import com.corsair.server.util.UpLoadFileEx;
import com.hr.attd.ctr.CtrHrkq_leave_blance;
import com.hr.attd.ctr.HrkqUtil;
import com.hr.base.ctr.CtrHr_systemparms;
import com.hr.base.entity.Hr_orgposition;
import com.hr.perm.ctr.CtrHrEmployeeUtil;
import com.hr.perm.ctr.CtrHrPerm;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_employee_cretl;
import com.hr.perm.entity.Hr_employee_family;
import com.hr.perm.entity.Hr_employee_leanexp;
import com.hr.perm.entity.Hr_employee_linked;
import com.hr.perm.entity.Hr_employee_nreward;
import com.hr.perm.entity.Hr_employee_relation;
import com.hr.perm.entity.Hr_employee_reward;
import com.hr.perm.entity.Hr_employee_trainexp;
import com.hr.perm.entity.Hr_employee_work;
import com.hr.perm.entity.Hr_leavejob;
import com.hr.util.HRUtil;
import com.hr.util.TimerTaskHRZM;

@ACO(coname = "web.hr.employee")
public class COHr_employee {

	@ACOAction(eventname = "findEmployeeByid", Authentication = true, notes = "根据员工ID查找员工资料,权限范围内")
	public String findEmployeeByid() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		int tp = Integer.valueOf(CorUtil.hashMap2Str(parms, "tp", "需要参数tp"));// tp:1
		// 扩展
		// 2
		// 基础资料
		String serid = CorUtil.hashMap2Str(parms, "er_id");
		if (serid == null)
			serid = CorUtil.hashMap2Str(parms, "id");
		if (serid == null)
			throw new Exception("需要参数er_id");
		int er_id = Integer.valueOf(serid);

		String sqlstr = "SELECT *,TIMESTAMPDIFF(YEAR, birthday, CURDATE()) age FROM hr_employee WHERE er_id=? "
				+ CSContext.getIdpathwhere();
		PraperedSql psql = new PraperedSql();
		psql.setSqlstr(sqlstr);
		psql.getParms().add(new PraperedValue(1, er_id));

		JSONArray jrmps = DBPools.defaultPool().opensql2json_O(psql);
		if (jrmps.size() == 0)
			throw new Exception("权限范围内没找到该用户资料！");
		int age = jrmps.getJSONObject(0).getInt("age");
		if (tp == 1) {
			Hr_employee_linked el = new Hr_employee_linked();
			el.findByPSQL(psql);
			if (el.isEmpty())
				throw new Exception("权限范围内没找到该用户资料！");
			JSONObject rst = el.toJsonObj();
			rst.put("age", age);
			return rst.toString();
		} else if (tp == 2) {
			Hr_employee e = new Hr_employee();
			e.findByPSQL(psql);
			if (e.isEmpty())
				throw new Exception("权限范围内没找到该用户资料！");
			JSONObject rst = e.toJsonObj();
			rst.put("age", age);
			return rst.toString();
		} else
			throw new Exception("tp参数错误");
	}

	@ACOAction(eventname = "findEmployeeinfo4Edit", Authentication = true, notes = "根据员工ID查找员工资料,权限范围内")
	public String findEmployeeinfo4Edit() throws Exception {
		// String sqlstr = "SELECT
		// e.*,l.ljtype2,l.ljtype1,l.ljreason,TIMESTAMPDIFF(YEAR, e.birthday,
		// CURDATE()) age "
		// + "FROM hr_employee e LEFT JOIN hr_leavejob l ON e.er_id=l.er_id AND
		// l.`stat`=9 and l.iscanced<>1 ";

		String sqlstr = "SELECT * FROM "
				+ "(SELECT e.*,l.ljtype2,l.ljtype1,l.ljreason,TIMESTAMPDIFF(YEAR, e.birthday, CURDATE()) age "
				+ " FROM hr_employee e LEFT JOIN hr_leavejob l ON e.er_id=l.er_id AND l.`stat`=9 AND l.iscanced<>1) tb "
				+ "WHERE 1=1 ";
		if (!HRUtil.hasRoles("2")) {// 不是招募中心人员 只能看没有离职的 或 离职类型不是招募中心自理的
			sqlstr = sqlstr + " AND ((ljtype1 is NULL) OR (ljtype1<>8)) ";
		}

		JSONObject rst = new CReport(sqlstr, " createtime desc ", null).findReport2JSON_O();
		JSONArray rows = rst.getJSONArray("rows");
		for (int i = 0; i < rows.size(); i++) {
			JSONObject row = rows.getJSONObject(i);
			String erid = row.getString("er_id");
			Date hiredday = Systemdate.getDateByStr(row.getString("hiredday"));
			// row.put("workym", CtrHrkq_leave_blance.getworkym(hiredday, new
			// Date()));// 工龄

			String yearmonth = CtrHrkq_leave_blance.getworkymString(hiredday, new Date());
			if(yearmonth.equals("0")){
				row.put("workym",  "0年0月");// 工龄
			}else{
				String nian = yearmonth.substring(0, yearmonth.indexOf("."));
				String yue = yearmonth.substring(yearmonth.indexOf(".") + 1);
				row.put("workym", nian + "年" + yue + "月");// 工龄
			}

		}
		// 判斷前端是否有返回參數，無則是查詢，有則是導出
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		String scols = parms.get("cols");
		if (scols == null) {
			return rst.toString();
		} else {
			(new CReport()).export2excel(rows, scols);
			return null;
		}
	}

	@ACOAction(eventname = "findEmpnameByCode", Authentication = true, notes = "根据工号查姓名")
	public String findEmpnameByCode() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String employee_code = CorUtil.hashMap2Str(parms, "employee_code", "需要参数employee_code");
		String sqlstr = "select employee_name from hr_employee where employee_code='" + employee_code + "'";
		Hr_employee emp = new Hr_employee();
		List<HashMap<String, String>> ns = emp.pool.openSql2List(sqlstr);
		if (ns.size() == 0)
			throw new Exception("工号为【" + employee_code + "】的人事资料不存在");
		JSONObject rst = new JSONObject();
		rst.put("employee_name", ns.get(0).get("employee_name"));
		return rst.toString();
	}

	@ACOAction(eventname = "findOrgOptionByLoginUser", Authentication = true, notes = "获取权限范围内机构岗位非批量")
	public String findOrgOptionByLoginUser() throws Exception {
		HashMap<String, String> urlparms = CSContext.getParms();
		String parms = urlparms.get("parms");
		String sqlstr = "select * from hr_orgposition where usable=1 ";
		String order = " ORDER BY orgid ASC ";

		String smax = urlparms.get("max");
		int max = (smax == null) ? 300 : Integer.valueOf(smax);

		List<JSONParm> jps = CJSON.getParms(parms);
		Hr_orgposition hsp = new Hr_orgposition();
		String where = CjpaUtil.buildFindSqlByJsonParms(hsp, jps);
		sqlstr = sqlstr + CSContext.getIdpathwhere() + where + order + " limit 0," + max;

		return hsp.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "getepsts", Authentication = true, ispublic = true, notes = "获取状态")
	public String getEmpkoyeeStats() throws Exception {
		String sqlstr = "select * from hr_employeestat where usable=1 order by statvalue";
		return DBPools.defaultPool().opensql2json(sqlstr);
	}

	@ACOAction(eventname = "findEmployeeTrancer", Authentication = true, ispublic = false, notes = "查询权限范围内允许调动的员工资料")
	public String findEmployeeTrancer() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		int tp = Integer.valueOf(CorUtil.hashMap2Str(parms, "tp", "需要参数tp"));// 1单调
		// 2批调
		// 3混合
		String eparms = parms.get("parms");
		List<JSONParm> jps = CJSON.getParms(eparms);
		// if (jps.size() == 0)
		// throw new Exception("需要查询参数");
		Hr_employee he = new Hr_employee();
		String where = CjpaUtil.buildFindSqlByJsonParms(he, jps);
		// System.out.println("where:" + where);
		String sqlstr = "select * from hr_employee where usable=1 and empstatid in(SELECT statvalue FROM hr_employeestat WHERE allowtransfer=1) ";
		if (tp == 1) {// OO类允许单独调动 主要是升职调动时候

		} else if (tp == 2) {
			String orgid = CorUtil.hashMap2Str(parms, "orgid", "需要参数orgid");
			Shworg org = new Shworg();
			org.findByID(orgid);
			if (org.isEmpty())
				throw new Exception("ID为【" + orgid + "】的机构不存在");
			sqlstr = sqlstr + " and ospid IN (SELECT ospid FROM hr_orgposition WHERE hwc_idzl IN("
					+ "SELECT parmvalue FROM hr_systemparms WHERE usable=1 AND parmcode='BATCH_QUATA_CLASS')) "
					+ " and orgid in(select orgid from shworg where idpath like '" + org.idpath.getValue() + "%')";
		} else if (tp == 3) {// 混合调动
			String orgid = CorUtil.hashMap2Str(parms, "orgid", "需要参数orgid");
			Shworg org = new Shworg();
			org.findByID(orgid);
			if (org.isEmpty())
				throw new Exception("ID为【" + orgid + "】的机构不存在");
			sqlstr = sqlstr + " and orgid=" + org.orgid.getValue();
		} else
			throw new Exception("tp参数允许值为1,2,3");

		String smax = parms.get("max");
		int max = (smax == null) ? 300 : Integer.valueOf(smax);
		if (!HRUtil.hasRoles("71")) {// 薪酬管理员
			sqlstr = sqlstr + " and employee_code='" + CSContext.getUserName() + "' ";
		}
		sqlstr = sqlstr + CSContext.getIdpathwhere() + where + " limit 0," + max;
		return he.pool.opensql2json(sqlstr);

		// JSONArray ems = he.pool.opensql2json_O(sqlstr);
		// for (int i = 0; i < ems.size(); i++) {
		// JSONObject em = ems.getJSONObject(i);
		// em.put("extorgname",
		// COShwUser.getOrgNamepath(em.getString("orgid")));
		// }
		// return ems.toString();
	}

	@ACOAction(eventname = "findEmployeeByIDCard", Authentication = true, ispublic = false, notes = "入职按身份证查找重入")
	public String findEmployeeByIDCard() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String id_number = CorUtil.hashMap2Str(parms, "id_number", "id_number参数不能为空");
		// 黑名单
		// if (CtrHrEmployeeUtil.isInBlackList(id_number))
		// throw new Exception("身份证为【" + id_number + "】的员工标记为【黑名单】");
		// 有在职的
		Hr_employee he = CtrHrEmployeeUtil.getIDNumberIsFrmal(id_number);
		if (!he.isEmpty()) {
			throw new Exception("员工资料在职，不允许重新入职");
		}
		// 查找最后离职
		String sqlstr = "SELECT * FROM hr_employee WHERE empstatid=12 AND id_number ='" + id_number
				+ "' ORDER BY hiredday DESC";
		he.findBySQL(sqlstr);
		if (he.isEmpty())
			throw new Exception("只有【离职】状态的员工允许重新入职");

		return he.tojson();
	}

	@ACOAction(eventname = "findEmployeebackByIDCard", Authentication = true, ispublic = false, notes = "查找黑名单")
	public String findEmployeebackByIDCard() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String id_number = CorUtil.hashMap2Str(parms, "id_number", "id_number参数不能为空");
		// 黑名单
		if (CtrHrEmployeeUtil.isInBlackList(id_number))
			throw new Exception("身份证为【" + id_number + "】的员工标记为【黑名单】，请先解封");

		Hr_employee he = CtrHrEmployeeUtil.getIDNumberIsFrmal(id_number);
		if (!he.isEmpty()) {
			throw new Exception("员工资料在职，不允许重新入职");
		}

		JSONObject rst = new JSONObject();
		rst.put("rst", "OK");
		return rst.toString();
	}

	@ACOAction(eventname = "getUserInfo", Authentication = true, ispublic = false, notes = "根据用户名获取用户信息")
	public String getUserInfo() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String employee_code = CorUtil.hashMap2Str(parms, "employee_code", "employee_code参数不能为空");

		String sqlstr = "select * from shwuser where username='" + employee_code + "'";
		Shwuser user = new Shwuser();
		user.findBySQL(sqlstr, false);
		if (user.isEmpty())
			throw new Exception("没有登录名为【" + employee_code + "】的系统用户");
		String[] fields = { "userid", "username", "displayname" };
		return user.tojsonsimple(fields);
	}

	/**
	 * includelv 包含离职的
	 * 
	 * @return
	 * @throws Exception
	 */
	@ACOAction(eventname = "findEmoloyeeList", Authentication = true, ispublic = false, notes = "根据登录权限查询员工资料")
	public String findEmoloyeeList() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String eparms = parms.get("parms");
		String spcetype = parms.get("spcetype");
		List<JSONParm> jps = CJSON.getParms(eparms);

		Hr_employee he = new Hr_employee();
		String where = CjpaUtil.buildFindSqlByJsonParms(he, jps);

		String orgid = parms.get("orgid");
		if ((orgid != null) && (!orgid.isEmpty())) {
			Shworg org = new Shworg();
			org.findByID(orgid, false);
			if (org.isEmpty())
				throw new Exception("没发现ID为【" + orgid + "】的机构");
			where = where + " and idpath like '" + org.idpath.getValue() + "%'";
		}

		if ((spcetype != null) && (Integer.valueOf(spcetype) == 1)) {
			where = where + " and employee_code='" + CSContext.getUserName() + "'";
		}

		String strincludelv = parms.get("includelv");
		boolean includelv = (strincludelv == null) ? false : Boolean.valueOf(strincludelv);
		String llvdate = parms.get("llvdate");// 最晚离职日期

		String smax = parms.get("max");
		int max = (smax == null) ? 300 : Integer.valueOf(smax);
		String sqlstr = "select * from hr_employee where usable=1";
		if ((llvdate != null) && (!llvdate.isEmpty())) {
			sqlstr = sqlstr + " and ( empstatid<=10 || ljdate<'" + llvdate + "') ";
		} else {
			if (!includelv)
				sqlstr = sqlstr + " and empstatid<=10 ";
		}
		sqlstr = sqlstr + CSContext.getIdpathwhere() + where + " limit 0," + max;
		return he.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "findEmoloyeeListAll", Authentication = true, ispublic = false, notes = "查询所有员工资料")
	public String findEmoloyeeListAll() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String eparms = parms.get("parms");
		List<JSONParm> jps = CJSON.getParms(eparms);

		Hr_employee he = new Hr_employee();
		String where = CjpaUtil.buildFindSqlByJsonParms(he, jps);

		String orgid = parms.get("orgid");
		if ((orgid != null) && (!orgid.isEmpty())) {
			Shworg org = new Shworg();
			org.findByID(orgid, false);
			if (org.isEmpty())
				throw new Exception("没发现ID为【" + orgid + "】的机构");
			where = where + " and idpath like '" + org.idpath.getValue() + "%'";
		}
		String smax = parms.get("max");
		int max = (smax == null) ? 300 : Integer.valueOf(smax);
		String sqlstr = "select * from hr_employee where usable=1 and empstatid<=10 " + where + " limit 0," + max;
		return he.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "findStateEmoloyeeList", Authentication = true, ispublic = false, notes = "查询特定类型员工资料")
	public String findStateEmoloyeeList() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String empstatid = CorUtil.hashMap2Str(parms, "empstatid", "需要参数empstatid");
		String sqlstr = "select * from hr_employee where usable=1 and empstatid=" + empstatid;
		return CSearchForm.doExec2JSON(sqlstr);
	}

	@ACOAction(eventname = "findEmoloyee4BlackDel", Authentication = true, ispublic = false, notes = "查询特定类型员工资料")
	public String findEmoloyee4BlackDel() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String empstatid = CorUtil.hashMap2Str(parms, "empstatid", "需要参数empstatid");
		String sqlstr = "select * from hr_employee where usable=1 and empstatid=" + empstatid;
		return new CReport(sqlstr, null).findReport2JSON_O(null, true, "").toString();
	}

	@ACOAction(eventname = "findEmpkoyeeByid", Authentication = true, ispublic = false, notes = "查询员工详细资料")
	public String findEmpkoyeeByid() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");

		Hr_employee_linked eml = new Hr_employee_linked();
		eml.findByID(er_id);

		if (eml.isEmpty())
			throw new Exception("没有发现id为【" + er_id + "】的员工资料");

		return eml.tojson();
	}

	// @ACOAction(eventname = "findEmpkoyeeLevedByid", Authentication = true,
	// ispublic = false, notes = "查询离职员工详细资料")
	// public String findEmpkoyeeLevedByid() throws Exception {
	// HashMap<String, String> parms = CSContext.getParms();
	// String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
	//
	// Hr_employee_linked eml = new Hr_employee_linked();
	// String sqlstr = "select * from hr_employee_leved where er_id=" + er_id;
	// eml.findBySQL(sqlstr);
	// if (eml.isEmpty())
	// throw new Exception("没有发现id为【" + er_id + "】的 离职员工资料");
	// return eml.tojson();
	// }

	@ACOAction(eventname = "removeEmoloyePic", Authentication = true, notes = "")
	public String removeEmoloyePic() throws Exception {
		String avatar_id1 = CorUtil.hashMap2Str(CSContext.getParms(), "avatar_id1");
		String avatar_id2 = CorUtil.hashMap2Str(CSContext.getParms(), "avatar_id2");
		String er_id = CorUtil.hashMap2Str(CSContext.getParms(), "er_id");
		if ((avatar_id1 == null) && (avatar_id2 == null)) {
			throw new Exception("avatar_id1 avatar_id2参数不能都为空");
		}
		Hr_employee he = new Hr_employee();
		if (er_id != null) {
			he.findByID(er_id, false);
			if (he.isEmpty())
				throw new Exception("没有找到ID为【" + er_id + "】的员工资料");
		}

		if (avatar_id1 != null) {
			UpLoadFileEx.delAttFile(avatar_id1);
			if (!he.isEmpty()) {
				he.avatar_id1.setValue(null);
				he.save();
			}

		}
		if (avatar_id2 != null) {
			UpLoadFileEx.delAttFile(avatar_id2);
			if (!he.isEmpty()) {
				he.avatar_id2.setValue(null);
				he.save();
			}
		}
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "uploadEmployeePic", Authentication = true, notes = "")
	public String uploadEmployeePic() throws Exception {
		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String er_id = CorUtil.hashMap2Str(CSContext.getParms(), "er_id", "需要参数er_id");
		Hr_employee he = new Hr_employee();
		he.findByID(er_id, false);
		if (he.isEmpty())
			throw new Exception("没有找到ID为【" + er_id + "】的员工资料");
		if (!he.avatar_id2.isEmpty()) {
			UpLoadFileEx.delAttFile(he.avatar_id2.getValue());
		}
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			if (!UpLoadFileEx.isImage(p)) {
				UpLoadFileEx.delAttFile(he.avatar_id2.getValue());
				throw new Exception("不是图片格式！");
			}
			if (p.filesize.getAsFloat() > (1024 * 5)) {
				UpLoadFileEx.delAttFile(he.avatar_id2.getValue());
				throw new Exception("图片不能超过5M！");
			}
			// BufferedImage Image = ImageIO.read(new File(name));
			he.avatar_id2.setValue(p.pfid.getValue());
			he.save(false);
			// 添加图片引用
			return "{\"avatar_id\":\"" + p.pfid.getValue() + "\"}";
		} else
			throw new Exception("没有图片文件");
	}

	@ACOAction(eventname = "getEmployeeFamilay", Authentication = true, notes = "获取员工家庭关系")
	public String getEmployeeFamilay() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_family hef = new Hr_employee_family();
		String sqlstr = "select * from hr_employee_family where er_id=" + er_id + " order by empf_id";
		return hef.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeFamilay", Authentication = true, notes = "获取员工家庭关系")
	public String delEmployeeFamilay() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String empf_id = CorUtil.hashMap2Str(parms, "empf_id", "需要参数empf_id");
		Hr_employee_family hef = new Hr_employee_family();
		hef.findByID(empf_id);
		if (hef.isEmpty())
			throw new Exception("没有找到ID为【" + empf_id + "】的家庭关系资料");
		hef.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeFamilay", Authentication = true, notes = "获取员工家庭关系")
	public String saveEmployeeFamilay() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_family hef = new Hr_employee_family();
		hef.fromjson(jsondata);
		if (isnew) {
			hef.setJpaStat(CJPAStat.RSINSERT);
		} else {
			hef.setJpaStat(CJPAStat.RSUPDATED);
		}
		hef.save();
		return hef.tojson();
	}

	@ACOAction(eventname = "getEmployeeLeanExp", Authentication = true, notes = "获取员工教育经历")
	public String getEmployeeLeanExp() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_leanexp hele = new Hr_employee_leanexp();
		String sqlstr = "select * from hr_employee_leanexp where er_id=" + er_id + " order by emple_id";
		return hele.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeLeanExp", Authentication = true, notes = "删除员工教育经历")
	public String delEmployeeLeanExp() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String emple_id = CorUtil.hashMap2Str(parms, "emple_id", "需要参数emple_id");
		Hr_employee_leanexp hele = new Hr_employee_leanexp();
		hele.findByID(emple_id);
		if (hele.isEmpty())
			throw new Exception("没有找到ID为【" + emple_id + "】的教育经历资料");
		hele.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeLeanExp", Authentication = true, notes = "保存员工教育经历")
	public String saveEmployeeLeanExp() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_leanexp hele = new Hr_employee_leanexp();
		hele.fromjson(jsondata);
		if (isnew) {
			hele.setJpaStat(CJPAStat.RSINSERT);
		} else {
			hele.setJpaStat(CJPAStat.RSUPDATED);
		}
		hele.save();
		return hele.tojson();
	}

	@ACOAction(eventname = "getEmployeeWork", Authentication = true, notes = "获取员工工作经历")
	public String getEmployeeWork() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_work hew = new Hr_employee_work();
		String sqlstr = "select * from hr_employee_work where er_id=" + er_id + " order by empl_id";
		return hew.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeWork", Authentication = true, notes = "删除员工工作经历")
	public String delEmployeeWork() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String empl_id = CorUtil.hashMap2Str(parms, "empl_id", "需要参数empl_id");
		Hr_employee_work hew = new Hr_employee_work();
		hew.findByID(empl_id);
		if (hew.isEmpty())
			throw new Exception("没有找到ID为【" + empl_id + "】的工作经历资料");
		hew.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeWork", Authentication = true, notes = "保存员工工作经历")
	public String saveEmployeeWork() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_work hew = new Hr_employee_work();
		hew.fromjson(jsondata);
		if (isnew) {
			hew.setJpaStat(CJPAStat.RSINSERT);
		} else {
			hew.setJpaStat(CJPAStat.RSUPDATED);
		}
		hew.save();
		return hew.tojson();
	}

	@ACOAction(eventname = "getEmployeeTrainExp", Authentication = true, notes = "获取员工培训经历")
	public String getEmployeeTrainExp() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_trainexp hete = new Hr_employee_trainexp();
		String sqlstr = "select * from hr_employee_trainexp where er_id=" + er_id + " order by tranexp_id";
		return hete.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeTrainExp", Authentication = true, notes = "删除员工培训经历")
	public String delEmployeeTrainExp() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String tranexp_id = CorUtil.hashMap2Str(parms, "tranexp_id", "需要参数tranexp_id");
		Hr_employee_trainexp hete = new Hr_employee_trainexp();
		hete.findByID(tranexp_id);
		if (hete.isEmpty())
			throw new Exception("没有找到ID为【" + tranexp_id + "】的培训经历资料");
		hete.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeTrainExp", Authentication = true, notes = "保存员工培训经历")
	public String saveEmployeeTrainExp() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_trainexp hete = new Hr_employee_trainexp();
		hete.fromjson(jsondata);
		if (isnew) {
			hete.setJpaStat(CJPAStat.RSINSERT);
		} else {
			hete.setJpaStat(CJPAStat.RSUPDATED);
		}
		hete.save();
		return hete.tojson();
	}

	@ACOAction(eventname = "getEmployeeRelation", Authentication = true, notes = "获取员工关联关系")
	public String getEmployeeRelation() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_relation her = new Hr_employee_relation();
		String sqlstr = "select * from hr_employee_relation where er_id=" + er_id + " order by empr_id";
		return her.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeRelation", Authentication = true, notes = "删除员工关联关系")
	public String delEmployeeRelation() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String empr_id = CorUtil.hashMap2Str(parms, "empr_id", "需要参数empr_id");
		Hr_employee_relation her = new Hr_employee_relation();
		her.findByID(empr_id);
		if (her.isEmpty())
			throw new Exception("没有找到ID为【" + empr_id + "】的关联关系资料");
		her.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeRelation", Authentication = true, notes = "保存员工关联关系")
	public String saveEmployeeRelation() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_relation her = new Hr_employee_relation();
		her.fromjson(jsondata);
		if (isnew) {
			her.setJpaStat(CJPAStat.RSINSERT);
		} else {
			her.setJpaStat(CJPAStat.RSUPDATED);
		}
		her.save();
		return her.tojson();
	}

	@ACOAction(eventname = "getEmployeeCretl", Authentication = true, notes = "获取员工证书资料")
	public String getEmployeeCretl() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_cretl hec = new Hr_employee_cretl();
		String sqlstr = "select * from hr_employee_cretl where er_id=" + er_id + " order by cert_id";
		return hec.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeCretl", Authentication = true, notes = "删除员工证书资料")
	public String delEmployeeCretl() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String cert_id = CorUtil.hashMap2Str(parms, "cert_id", "需要参数cert_id");
		Hr_employee_cretl hec = new Hr_employee_cretl();
		hec.findByID(cert_id);
		if (hec.isEmpty())
			throw new Exception("没有找到ID为【" + cert_id + "】的证书资料");
		hec.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeCretl", Authentication = true, notes = "保存员工证书资料")
	public String saveEmployeeCretl() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_cretl hec = new Hr_employee_cretl();
		hec.fromjson(jsondata);
		if (isnew) {
			hec.setJpaStat(CJPAStat.RSINSERT);
		} else {
			hec.setJpaStat(CJPAStat.RSUPDATED);
		}
		hec.save();
		return hec.tojson();
	}

	@ACOAction(eventname = "uploadEmployeeCretlPic", Authentication = true, notes = "上传证件图片")
	public String uploadEmployeeCretlPic() throws Exception {
		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String cert_id = CorUtil.hashMap2Str(CSContext.getParms(), "cert_id", "需要参数cert_id");
		Hr_employee_cretl hec = new Hr_employee_cretl();
		hec.findByID(cert_id, false);
		if (hec.isEmpty())
			throw new Exception("没有找到ID为【" + cert_id + "】的证件资料");
		if (!hec.picture_id.isEmpty()) {
			UpLoadFileEx.delAttFile(hec.picture_id.getValue());
		}
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			hec.picture_id.setValue(p.pfid.getValue());
			hec.save(false);
			// 添加图片引用
			return "{\"picture_id\":\"" + p.pfid.getValue() + "\"}";
		} else
			throw new Exception("没有图片文件");
	}

	@ACOAction(eventname = "getEmployeeReward", Authentication = true, notes = "获取员工激励记录")
	public String getEmployeeReward() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_reward her = new Hr_employee_reward();
		String sqlstr = "select * from hr_employee_reward where er_id=" + er_id + " order by emprw_id";
		return her.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeReward", Authentication = true, notes = "删除员工激励记录")
	public String delEmployeeReward() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String emprw_id = CorUtil.hashMap2Str(parms, "emprw_id", "需要参数emprw_id");
		Hr_employee_reward her = new Hr_employee_reward();
		her.findByID(emprw_id);
		if (her.isEmpty())
			throw new Exception("没有找到ID为【" + emprw_id + "】的激励记录");
		her.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeReward", Authentication = true, notes = "保存员工激励记录")
	public String saveEmployeeReward() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_reward her = new Hr_employee_reward();
		her.fromjson(jsondata);
		if (isnew) {
			her.setJpaStat(CJPAStat.RSINSERT);
		} else {
			her.setJpaStat(CJPAStat.RSUPDATED);
		}
		her.save();
		return her.tojson();
	}

	@ACOAction(eventname = "getEmployeeNReward", Authentication = true, notes = "获取员工负激励记录")
	public String getEmployeeNReward() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		Hr_employee_nreward henr = new Hr_employee_nreward();
		String sqlstr = "select * from hr_employee_nreward where er_id=" + er_id + " order by nemprw_id";
		return henr.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "delEmployeeNReward", Authentication = true, notes = "删除员工负激励记录")
	public String delEmployeeNReward() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String nemprw_id = CorUtil.hashMap2Str(parms, "nemprw_id", "需要参数nemprw_id");
		Hr_employee_nreward henr = new Hr_employee_nreward();
		henr.findByID(nemprw_id);
		if (henr.isEmpty())
			throw new Exception("没有找到ID为【" + nemprw_id + "】的负激励记录");
		henr.delete();
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "saveEmployeeNReward", Authentication = true, notes = "保存员工负激励记录")
	public String saveEmployeeNReward() throws Exception {
		JSONObject rootNode = JSONObject.fromObject(CSContext.getPostdata());
		boolean isnew = rootNode.getBoolean("isnew");
		String jsondata = rootNode.getString("jsondata");
		Hr_employee_nreward henr = new Hr_employee_nreward();
		henr.fromjson(jsondata);
		if (isnew) {
			henr.setJpaStat(CJPAStat.RSINSERT);
		} else {
			henr.setJpaStat(CJPAStat.RSUPDATED);
		}
		henr.save();
		return henr.tojson();
	}

	@ACOAction(eventname = "findLVEmoloyeeList", Authentication = true, ispublic = false, notes = "根据登录权限查询可离职员工资料")
	public String findLVEmoloyeeList() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String eparms = parms.get("parms");
		String orgid = parms.get("orgid");
		List<JSONParm> jps = CJSON.getParms(eparms);
		// if (jps.size() == 0)
		// throw new Exception("需要查询参数");
		Hr_employee he = new Hr_employee();
		int mengtag = Integer.valueOf(CorUtil.hashMap2Str(parms, "mengtag", "需要参数mengtag"));
		String sdfdname = null;
		if (mengtag == 1)
			sdfdname = "allowlv";
		else if (mengtag == 2)
			sdfdname = "allowsxlv";
		else
			throw new Exception("参数【mengtag】只允许为1、2");
		String where = CjpaUtil.buildFindSqlByJsonParms(he, jps);

		String smax = parms.get("max");
		int max = (smax == null) ? 300 : Integer.valueOf(smax);

		String sqlstr = "SELECT * FROM hr_employee WHERE 1=1 ";
		if (orgid != null) {
			Shworg org = new Shworg();
			org.findByID(orgid);
			if (org.isEmpty())
				throw new Exception("ID为【" + orgid + "】的机构不存在!");
			sqlstr = sqlstr
					+ "AND EXISTS (SELECT 1 FROM shworg WHERE shworg.usable=1 AND hr_employee.orgid=shworg.orgid AND shworg.idpath LIKE '"
					+ org.idpath.getValue() + "%')";
			// sqlstr = sqlstr + " and orgid in (select orgid from shworg where
			// idpath like
			// '" + org.idpath.getValue() + "%')";
		}
		// sqlstr = sqlstr + " and empstatid IN( " + " SELECT statvalue FROM
		// hr_employeestat WHERE " + sdfdname + "=1 " + ") ";
		sqlstr = sqlstr
				+ "AND EXISTS (SELECT 1 FROM hr_employeestat WHERE hr_employee.empstatid=hr_employeestat.statvalue AND hr_employeestat."+sdfdname+"=1)";
		sqlstr = sqlstr + " AND usable=1 " + CSContext.getIdpathwhere() + where + " limit 0," + max;
		return he.pool.opensql2json(sqlstr);
	}

	@ACOAction(eventname = "findOrgHrlev", Authentication = true, ispublic = true, notes = "获取机构人事级别")
	public String findOrgHrlev() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String orgid = CorUtil.hashMap2Str(parms, "orgid", "orgid参数不能为空");
		int hrlev = HRUtil.getOrgHrLev(orgid);
		return "{\"hrlev\":\"" + hrlev + "\"}";
	}

	@ACOAction(eventname = "reSetOPNameIDPath", Authentication = true, ispublic = true, notes = "设置机构职位信息")
	public String reSetOPNameIDPath() throws Exception {
		CJPALineData<Hr_orgposition> ops = new CJPALineData<Hr_orgposition>(Hr_orgposition.class);
		String sqlstr = "select * from Hr_orgposition ";
		ops.findDataBySQL(sqlstr);
		Shworg o = new Shworg();
		for (CJPABase jpa : ops) {
			Hr_orgposition e = (Hr_orgposition) jpa;
			o.findByID(e.orgid.getValue());
			if (!o.isEmpty()) {
				e.orgname.setValue(COShwUser.getOrgNamepath(o.orgid.getValue()));
				e.orgcode.setValue(o.code.getValue());
				e.idpath.setValue(o.idpath.getValue());
				e.save();
			}
		}
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "reSetEmpOrgNameIDPath", Authentication = true, ispublic = true, notes = "设置人事机构信息")
	public String reSetEmpOrgNameIDPath() throws Exception {
		CJPALineData<Hr_employee> emps = new CJPALineData<Hr_employee>(Hr_employee.class);
		String sqlstr = "select * from hr_employee ";
		emps.findDataBySQL(sqlstr);
		Shworg o = new Shworg();
		for (CJPABase jpa : emps) {
			Hr_employee e = (Hr_employee) jpa;
			o.findByID(e.orgid.getValue());
			if (!o.isEmpty()) {
				e.orgname.setValue(COShwUser.getOrgNamepath(o.orgid.getValue()));
				e.orgcode.setValue(o.code.getValue());
				e.idpath.setValue(o.idpath.getValue());
				e.save();
			}
		}
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "reSetEmpOSPInfo", Authentication = true, ispublic = true, notes = "设置人事职位信息")
	public String reSetEmpOSPInfo() throws Exception {
		CJPALineData<Hr_employee> emps = new CJPALineData<Hr_employee>(Hr_employee.class);
		String sqlstr = "select * from hr_employee ";
		emps.findDataBySQL(sqlstr);
		Hr_orgposition osp = new Hr_orgposition();
		Shworg o = new Shworg();

		for (CJPABase jpa : emps) {
			Hr_employee e = (Hr_employee) jpa;
			osp.findByID(e.ospid.getValue());
			o.findByID(e.orgid.getValue());
			if (!osp.isEmpty()) {
				e.lv_id.setValue(osp.lv_id.getValue());
				e.lv_num.setValue(osp.lv_num.getValue());
				e.hg_id.setValue(osp.hg_id.getValue());
				e.hg_code.setValue(osp.hg_code.getValue());
				e.hg_name.setValue(osp.hg_name.getValue());
				e.ospcode.setValue(osp.ospcode.getValue());
				e.sp_name.setValue(osp.sp_name.getValue());
				e.iskey.setValue(osp.iskey.getValue());
				e.hwc_namezq.setValue(osp.hwc_namezq.getValue());
				e.hwc_namezz.setValue(osp.hwc_namezz.getValue());
				e.save();
			}
		}
		return "{\"result\":\"OK\"}";
	}

	@ACOAction(eventname = "getEmployee_cret_attach", Authentication = true, ispublic = false, notes = "getEmployee_cret_attach")
	public String getEmployee_cret_attach() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String id = CorUtil.hashMap2Str(parms, "id", "id参数不能为空");
		Hr_employee_cretl ec = new Hr_employee_cretl();
		ec.findByID(id);
		if (ec.isEmpty())
			throw new Exception("ID为【" + id + "】的证件不存在");
		Shw_attach att = new Shw_attach();
		att.findByID(ec.attid.getValue());
		return att.tojson();
	}

	/**
	 * 文件名
	 * 工号+证件名.*
	 * 
	 * @return
	 * @throws Exception
	 */
	@ACOAction(eventname = "impattrs", Authentication = true, ispublic = false, notes = "批量导入附件")
	public String impattrs() throws Exception {
		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String batchno = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");// 批次号
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		if (!HRUtil.hasRoles("87")) {// 没有权限，直接删除上传的文件
			dellallfile(pfs);
			throw new Exception("需要证件管理员权限");
		}
		int rst = 0;
		Hr_employee emp = new Hr_employee();
		CDBConnection con = DBPools.defaultPool().getCon(this);
		con.startTrans();
		try {
			for (CJPABase pfb : pfs) {
				Shw_physic_file pf = (Shw_physic_file) pfb;
				String displayfname = pf.displayfname.getValue();
				Integer spidx = displayfname.indexOf("+");
				String empcode = displayfname.substring(0, spidx);
				String dname = displayfname.substring(spidx + 1, displayfname.length());
				emp.clear();
				String sqlstr = "SELECT * FROM hr_employee WHERE employee_code='" + empcode + "'";
				emp.findBySQL(con, sqlstr, true);
				if (emp.isEmpty())
					throw new Exception("工号【" + empcode + "】不存在");
				CJPALineData<Shw_attach> shw_attachs = emp.shw_attachs;
				Shw_attach att = null;
				if (shw_attachs.size() == 0) {
					att = new Shw_attach();
					shw_attachs.add(att);
				} else {
					att = (Shw_attach) shw_attachs.get(0);
				}
				Shw_attach_line al = new Shw_attach_line();
				al.pfid.setValue(pf.pfid.getValue()); // 物理文件ID
				al.fdrid.setValue(0); // 文件夹ID 单据的附件 本属性为0
				al.displayfname.setValue(dname); // 显示的名称
				al.extname.setValue(pf.extname.getValue()); // 扩展文件名
				al.filesize.setValue(pf.filesize.getValue()); // 文件大小
				al.filevision.setValue(pf.filevision.getValue()); // 文件版本
				al.filecreator.setValue(pf.creator.getValue()); // 所有者
				al.filecreate_time.setAsDatetime(new Date()); // 创建时间
				att.shw_attach_lines.add(al);
				emp.save(con);
				rst++;
				// UpLoadFileEx.delAttFile(pf.pfid.getValue());
			}
			con.submit();
		} catch (Exception e) {
			con.rollback();
			dellallfile(pfs);
			throw e;
		} finally {
			con.close();
		}
		JSONObject jo = new JSONObject();
		jo.put("rst", rst);
		jo.put("batchno", batchno);
		return jo.toString();
	}

	private void dellallfile(CJPALineData<Shw_physic_file> pfs) throws Exception {
		for (CJPABase pfb : pfs) {
			Shw_physic_file pf = (Shw_physic_file) pfb;
			UpLoadFileEx.delAttFile(pf.pfid.getValue());
		}
	}

	@ACOAction(eventname = "expcerts", Authentication = true, ispublic = false, notes = "批量导出附件")
	public String expcerts() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String erids = CorUtil.hashMap2Str(parms, "erids", "需要参数erids");
		String sqlstr = "SELECT p.*,al.displayfname fname,e.employee_code FROM hr_employee e,shw_attach_line al,shw_physic_file p " +
				"WHERE e.attid=al.attid AND al.pfid=p.pfid  " +
				"AND e.er_id IN(" + erids + ")";
		List<JSONObject> files = new ArrayList<JSONObject>();

		JSONArray pfs1 = DBPools.defaultPool().opensql2json_O(sqlstr);
		for (int i = 0; i < pfs1.size(); i++) {
			files.add(pfs1.getJSONObject(i));
		}
		sqlstr = "SELECT p.*,al.displayfname fname,e.employee_code FROM hr_employee e,hr_employee_cretl c,shw_attach_line al,shw_physic_file p "
				+ " WHERE e.er_id=c.er_id AND al.attid=c.attid AND p.pfid=al.pfid"
				+ " AND e.er_id IN(" + erids + ")";
		JSONArray pfs2 = DBPools.defaultPool().opensql2json_O(sqlstr);
		for (int i = 0; i < pfs2.size(); i++) {
			files.add(pfs2.getJSONObject(i));
		}

		if (files.size() == 0)
			throw new Exception("没有附件");
		createTempFile(files);
		return null;
	}

	private void createTempFile(List<JSONObject> files) throws Exception {
		String un = (CSContext.getUserNameEx() == null) ? "SYSTEM" : CSContext.getUserNameEx();
		String fsep = System.getProperty("file.separator");
		String rootfstr = ConstsSw.geAppParmStr("UDFilePath") + "temp" + fsep + un
				+ "_" + Systemdate.getStrDateByFmt(new Date(), "hhmmss") + fsep;
		File file = new File(rootfstr);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}
		for (int i = 0; i < files.size(); i++) {
			JSONObject pf = files.get(i);
			String fullname = ConstsSw.geAppParmStr("UDFilePath") + fsep + pf.getString("ppath") + fsep + pf.getString("pfname");
			if (!(new File(fullname)).exists()) {
				fullname = ConstsSw._root_filepath + "attifiles" + fsep + pf.getString("ppath") + fsep + pf.getString("pfname");
				if (!(new File(fullname)).exists())
					throw new Exception("文件" + fullname + "不存在!");
			}
			String fname = pf.getString("fname");
			fname = fname.replace("\\", fsep);
			fname = fname.replace("/", fsep);
			String[] sz = fname.split(fsep);
			fname = sz[sz.length - 1];
			String strtem = pf.getString("employee_code") + "-" + fname;
			String nfname = rootfstr + fsep + strtem;
			file = new File(nfname);
			if ((file.exists()) && (file.isFile())) {
				if (!file.delete())
					throw new Exception("删除文件错误【" + nfname + "】");
			}
			RandomAccessFile outfile = new RandomAccessFile(nfname, "rw");
			forTransfer(new RandomAccessFile(fullname, "r"), outfile);
		}
		String rarfname = ConstsSw.geAppParmStr("UDFilePath") + "temp" + fsep + "下载文件.zip";
		file = new File(rarfname);
		if ((file.exists()) && (file.isFile())) {
			if (!file.delete())
				throw new Exception("删除文件错误【" + rarfname + "】");
		}

		CFileUtiil.zipFold(rarfname, rootfstr);
		HttpServletResponse rsp = CSContext.getResponse();
		rsp.setContentType(getContentType(rarfname));
		rsp.setHeader("content-disposition", "attachment; filename=" + new String(("下载文件.zip").getBytes("GB2312"), "ISO_8859_1"));
		OutputStream os = rsp.getOutputStream();
		FileInputStream fis = new FileInputStream(rarfname);
		try {
			byte[] b = new byte[1024];
			int i = 0;
			while ((i = fis.read(b)) > 0) {
				os.write(b, 0, i);
			}
			fis.close();
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			fis.close();
			os.flush();
			os.close();
		}
		File f = new File(rootfstr);
		CFileUtiil.delete(f);
		f = new File(rarfname);
		CFileUtiil.delete(f);
	}

	private static String getContentType(String pathToFile) {
		Path path = Paths.get(pathToFile);
		try {
			return Files.probeContentType(path);
		} catch (IOException e) {
			return "application/text";
		}

	}

	private static void forTransfer(RandomAccessFile f1, RandomAccessFile f2) throws Exception {
		final int len = 20971520;
		int curPosition = 0;
		FileChannel inC = f1.getChannel();
		FileChannel outC = f2.getChannel();
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				break;
			}
			if ((inC.size() - inC.position()) < len)
				curPosition = (int) (inC.size() - inC.position());
			else
				curPosition = len;
			inC.transferTo(inC.position(), curPosition, outC);
			inC.position(inC.position() + curPosition);
		}
	}

	@ACOAction(eventname = "impexcel", Authentication = true, ispublic = false, notes = "入职导入Excel")
	public String impexcel() throws Exception {
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

		// Workbook workbook = CExcelUtil.isExcel2003(fullname) ? new
		// HSSFWorkbook(new FileInputStream(fullname))
		// : new XSSFWorkbook(new FileInputStream(fullname));
		Workbook workbook = WorkbookFactory.create(file);

		int sn = workbook.getNumberOfSheets();
		if (sn <= 0)
			throw new Exception("excel<" + fullname + ">没有sheet");
		Sheet aSheet = workbook.getSheetAt(0);// 获得一个sheet

		return parserExcelSheet(aSheet, batchno);
	}

	private int parserExcelSheet(Sheet aSheet, String batchno) throws Exception {
		String idpathwhere = CSContext.getIdpathwhere();
		String entid = CSContext.getCurEntID();
		if (aSheet.getLastRowNum() == 0) {
			return 0;
		}
		List<CExcelField> efds = initExcelFields();
		efds = CExcelUtil.parserExcelSheetFields(aSheet, efds, 1);// 解析title
		// 并检查必须存在的列
		List<Map<String, String>> values = CExcelUtil.getExcelValues(aSheet, efds, 1);
		Hr_employee emp = new Hr_employee();
		Hr_leavejob lv = new Hr_leavejob();
		Hr_orgposition osp = new Hr_orgposition();
		CDBConnection con = emp.pool.getCon(this);
		DictionaryTemp dictemp = new DictionaryTemp();// 数据字典缓存
		con.startTrans();
		int rst = 0;
		try {
			int ct = values.size();
			int curidx = 0;
			for (Map<String, String> v : values) {
				Logsw.debug("人事资料导入【" + (curidx++) + "/" + ct + "】");
				String employee_code = v.get("employee_code");
				if ((employee_code == null) || (employee_code.isEmpty()))
					continue;
				rst++;
				emp.clear();
				emp.findBySQL("SELECT * FROM hr_employee WHERE employee_code='" + employee_code + "'");
				boolean undup = (Integer.valueOf(HrkqUtil.getParmValueErr("IMPORT_EMP_DUP")) == 1);// 批量导入人事资料重复时处理规则
				// 1
				// 禁止重复
				// 2
				// 重复更新
				if (undup) {
					if (!emp.isEmpty())
						throw new Exception("工号【" + employee_code + "】的人事资料已经存在");
				}

				String sqlstr = "select * from hr_leavejob where employee_code = '" + employee_code + "'";
				lv.findBySQL(sqlstr, false);
				if (!lv.isEmpty())
					throw new Exception("已经存在员工【" + employee_code + "】的离职表单，无法重新导入");
				lv.clear();

				String ospcode = v.get("ospcode");
				if ((ospcode == null) || (ospcode.isEmpty()))
					throw new Exception("【" + v.get("employee_name") + "】机构职位编码不能为空");

				sqlstr = "SELECT * FROM hr_orgposition WHERE ospcode='" + ospcode + "' ";
				// if (!CSContext.isAdminNoErr())
				sqlstr = sqlstr + " " + idpathwhere;

				osp.clear();
				osp.findBySQL(sqlstr, false);
				if (osp.isEmpty())
					throw new Exception("工号【" + employee_code + "】的机构职位编码【" + ospcode + "】不存在或权限范围内不存在");

				String id_number = v.get("id_number");
				if ((id_number == null) || (id_number.isEmpty()))
					throw new Exception("【" + v.get("employee_name") + "】身份证不能为空");
				if (undup)// 1 禁止重复 2 重复更新
					emp.clear();

				boolean lvjob = false;
				if ("离职".equalsIgnoreCase(v.get("empstatid"))) {
					lvjob = true;
					emp.empstatid.setValue("4");
				} else if ("正式".equals(v.get("empstatid")))
					emp.empstatid.setValue("4");
				else
					throw new Exception("【" + v.get("employee_name") + "】只能导入人事状态为【正式、离职】的人事资料");

				emp.card_number.setValue(v.get("card_number"));
				emp.employee_code.setValue(employee_code);
				emp.employee_name.setValue(v.get("employee_name"));
				emp.usedname.setValue(v.get("usedname"));
				emp.english_name.setValue(v.get("english_name"));
		
				
				emp.pldcp.setValue(dictemp.getVbCE("495", v.get("pldcp"), true,
						"工号【" + employee_code + "】政治面貌【" + v.get("pldcp") + "】不存在"));
				emp.bwday.setValue(v.get("bwday"));
				emp.health.setValue(v.get("health"));
				emp.bloodtype.setValue(dictemp.getVbCE("697", v.get("bloodtype"), true,
						"工号【" + employee_code + "】血型【" + v.get("bloodtype") + "】不存在"));
				emp.medicalhistory.setValue(v.get("medicalhistory"));
				emp.nation.setValue(dictemp.getVbCE("797", v.get("nation"), false,
						"工号【" + employee_code + "】民族【" + v.get("nation") + "】不存在"));
				emp.nationality.setValue(v.get("nationality"));
				emp.talentstype.setValue(v.get("talentstype"));
				emp.married.setValue(dictemp.getVbCE("714", v.get("married"), false,
						"工号【" + employee_code + "】婚姻状况【" + v.get("married") + "】不存在"));
				emp.email.setValue(v.get("email"));
				emp.nativeplace.setValue(v.get("nativeplace"));
				emp.address.setValue(v.get("address"));
				emp.registertype.setValue(dictemp.getVbCE("702", v.get("registertype"), true,
						"工号【" + employee_code + "】户籍类型【" + v.get("registertype") + "】不存在"));
				emp.registeraddress.setValue(v.get("registeraddress"));
				emp.sscurty_addr.setValue(v.get("sscurty_addr"));
				emp.sscurty_startdate.setValue(v.get("sscurty_startdate"));
				emp.shoesize.setValue(v.get("shoesize"));
				emp.pants_code.setValue(v.get("pants_code"));
				emp.coat_code.setValue(v.get("coat_code"));
				emp.pay_way.setValue(v.get("pay_way"));
				emp.schedtype.setValue(v.get("schedtype"));
				emp.speciality.setValue(v.get("speciality"));
				emp.entrysourcr.setValue(dictemp.getValueByCation("741", v.get("entrysourcr")));//
				emp.advisercode.setValue(v.get("advisercode"));
				emp.advisername.setValue(v.get("advisername"));
				emp.adviserphone.setValue(v.get("adviserphone"));
				emp.juridical.setValue(v.get("juridical"));
				emp.transorg.setValue(v.get("transorg"));
				emp.transextime.setValue(v.get("transextime"));
				emp.dispunit.setValue(dictemp.getVbCE("1322", v.get("dispunit"), false,
						"工号【" + employee_code + "】的派遣机构【" + v.get("dispunit") + "】不存在"));
				emp.dispeextime.setValue(v.get("dispeextime"));
				emp.note.setValue(v.get("note"));
				emp.sex.setValue(dictemp.getVbCE("81", v.get("sex"), false,
						"工号【" + employee_code + "】性别【" + v.get("sex") + "】不存在"));
				emp.degree.setValue(dictemp.getVbCE("84", v.get("degree"), false,
						"工号【" + employee_code + "】学历【" + v.get("degree") + "】不存在"));
				emp.degreetype.setValue(dictemp.getVbCE("1495", v.get("degreetype"), false,
						"工号【" + employee_code + "】学历类型【" + v.get("degreetype") + "】不存在"));
				emp.degreecheck.setValue(dictemp.getVbCE("1507", v.get("degreecheck"), false,
						"工号【" + employee_code + "】学历验证【" + v.get("degreecheck") + "】不存在"));
				emp.introducer.setValue(v.get("introducer"));
				emp.guarantor.setValue(v.get("guarantor"));
				emp.eovertype.setValue(dictemp.getVbCE("1394", v.get("eovertype"), true,
						"工号【" + employee_code + "】加班类别【" + v.get("eovertype") + "】不存在"));
				emp.major.setValue(v.get("major"));
				emp.birthday.setValue(v.get("birthday"));
				emp.id_number.setValue(v.get("id_number"));
				emp.registeraddress.setValue(v.get("registeraddress"));
				emp.sign_org.setValue(v.get("sign_org"));
				emp.sign_date.setValue(v.get("sign_date"));
				emp.expired_date.setValue(v.get("expired_date"));
				emp.hiredday.setValue(v.get("hiredday"));
				emp.telphone.setValue(v.get("telphone"));
				emp.urgencycontact.setValue(v.get("urgencycontact"));
				emp.cellphone.setValue(v.get("cellphone"));
				emp.dorm_bed.setValue(v.get("dorm_bed"));
				emp.orgid.setValue(osp.orgid.getValue()); // 部门ID
				emp.orgcode.setValue(osp.orgcode.getValue()); // 部门编码
				emp.orgname.setValue(osp.orgname.getValue()); // 部门名称
				emp.hwc_namezl.setValue(osp.hwc_namezl.getValue()); // 职类
				emp.lv_id.setValue(osp.lv_id.getValue()); // 职级ID
				emp.lv_num.setValue(osp.lv_num.getValue()); // 职级
				emp.hg_id.setValue(osp.hg_id.getValue()); // 职等ID
				emp.hg_code.setValue(osp.hg_code.getValue()); // 职等编码
				emp.hg_name.setValue(osp.hg_name.getValue()); // 职等名称
				emp.ospid.setValue(osp.ospid.getValue()); // 职位ID
				if(osp.isoffjob.getValue().equals("1")){
					emp.emnature.setValue("脱产");
				}else {
					emp.emnature.setValue("非脱产");
				}
				emp.ospcode.setValue(osp.ospcode.getValue()); // 职位编码
				emp.sp_name.setValue(osp.sp_name.getValue()); // 职位名称
				emp.iskey.setValue(osp.iskey.getValue()); // 关键岗位
				emp.hwc_namezq.setValue(osp.hwc_namezq.getValue()); // 职群
				emp.hwc_namezz.setValue(osp.hwc_namezz.getValue()); // 职种
				emp.rectcode.setValue(v.get("rectcode")); // 招聘人工号
				emp.rectname.setValue(v.get("rectname")); // 招聘人
				// System.out.println("atdtype:" + v.get("atdtype"));
				emp.atdtype.setValue(dictemp.getVbCE("1399", v.get("atdtype"), false,
						"工号【" + employee_code + "】出勤类别【" + v.get("atdtype") + "】不存在"));
				emp.noclock.setValue(dictemp.getVbCE("5", v.get("noclock"), false,
						"工号【" + employee_code + "】免打考勤卡【" + v.get("noclock") + "】不存在"));
				emp.usable.setAsInt(1); // 有效
				emp.idpath.setValue(osp.idpath.getValue());
				emp.property2.setValue("批量导入，批号:" + batchno);
				emp.entid.setValue(entid);
				emp.save(con, false);

				if (lvjob) {// 离职表单
					String[] asfds = { "orgid", "orgcode", "orgname", "er_id", "er_code", "employee_code", "sex",
							"id_number", "employee_name", "degree", "birthday", "registeraddress", "hiredday", "lv_id",
							"lv_num", "hg_id", "hg_code", "hg_name", "ospid", "ospcode", "sp_name", "hwc_namezl",
					"idpath" };
					lv.assignfieldOnlyValue(emp, asfds);
					lv.ljtype.setAsInt(2); // 实习生或员工离职 1 实习生 2 员工
					lv.ljappdate.setValue(v.get("ljappdate")); // 申请离职日期
					lv.ljdate.setValue(v.get("ljappdate")); // 离职日期
					lv.worktime.setValue(v.get("worktime")); // 司龄
					lv.ljtype1.setValue(dictemp.getVbCE("782", v.get("ljtype1"), false,
							"工号【" + employee_code + "】离职类别【" + v.get("ljtype1") + "】不存在")); // 离职类别
					lv.ljtype2.setValue(dictemp.getVbCE("1045", v.get("ljtype2"), false,
							"工号【" + employee_code + "】离职类型【" + v.get("ljtype2") + "】不存在")); // 离职类型
					lv.ljreason.setValue(dictemp.getVbCE("1049", v.get("ljreason"), false,
							"工号【" + employee_code + "】离职原因【" + v.get("ljreason") + "】不存在")); // 离职原因
					lv.iscpst.setValue(dictemp.getValueByCation("5", v.get("iscpst"), "2")); // 是否补偿
					lv.cpstarm.setValue(v.get("cpstarm")); // 补偿金额
					lv.iscpt.setValue(dictemp.getValueByCation("5", v.get("iscpt"), "2")); // 是否投诉
					lv.isabrt.setValue(dictemp.getValueByCation("5", v.get("isabrt"), "2")); // 是否仲裁
					lv.islawsuit.setValue(dictemp.getValueByCation("5", v.get("islawsuit"), "2")); // 是否诉讼
					lv.isblacklist.setValue(dictemp.getValueByCation("5", v.get("isblacklist"), "2")); // 是否加入黑名单
					boolean addb = (lv.isblacklist.getAsInt() == 1);
					// System.out.println("工号【" + employee_code + "】加封类型【" +
					// v.get("addtype") + "】");
					// System.out.println("工号【" + employee_code + "】加封类别【" +
					// v.get("addtype1") + "】");
					lv.addtype.setValue(dictemp.getVbCE("1071", v.get("addtype"), !addb,
							"工号【" + employee_code + "】加封类型【" + v.get("addtype") + "】不存在")); // 加封类型
					lv.addtype1.setValue(dictemp.getVbCE("1074", v.get("addtype1"), !addb,
							"工号【" + employee_code + "】加封类别【" + v.get("addtype1") + "】不存在")); // 加封类别
					lv.blackreason.setValue(v.get("blackreason")); // 加入黑名单原因
					lv.iscanced.setValue("2"); // 已撤销
					lv.pempstatid.setValue(emp.empstatid.getValue()); // 离职前状态
					lv.remark.setValue("自动导入离职表单"); // 备注
					lv.attribute2.setValue("批量导入，批号:" + batchno);
					lv.entid.setValue(entid);
					lv.save(con);
					lv.wfcreate(null, con);// 提交离职表单
				}
			}
			// throw new Exception("不给通过");
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
		efields.add(new CExcelField("卡号", "card_number", false));
		efields.add(new CExcelField("工号", "employee_code", true));
		efields.add(new CExcelField("姓名", "employee_name", true));
		efields.add(new CExcelField("学历类型", "degreetype", true));
		efields.add(new CExcelField("学历验证", "degreecheck", true));
		efields.add(new CExcelField("介绍人工号", "guarantor", false));
		efields.add(new CExcelField("介绍人", "introducer", false));
		efields.add(new CExcelField("加班类别", "eovertype", false));
		efields.add(new CExcelField("曾用名", "usedname", false));
		efields.add(new CExcelField("英文名", "english_name", false));
		efields.add(new CExcelField("人事状态", "empstatid", true));
		efields.add(new CExcelField("政治面貌", "pldcp", false));
		efields.add(new CExcelField("参加工作时间", "bwday", false));
		efields.add(new CExcelField("健康情况", "health", false));
		efields.add(new CExcelField("血型", "bloodtype", false));
		efields.add(new CExcelField("过往病史", "medicalhistory", false));
		efields.add(new CExcelField("民族", "nation", false));
		efields.add(new CExcelField("国籍", "nationality", false));
		efields.add(new CExcelField("人才类型", "talentstype", false));
		efields.add(new CExcelField("婚姻状况", "married", false));
		efields.add(new CExcelField("邮箱", "email", false));
		efields.add(new CExcelField("籍贯", "nativeplace", false));
		efields.add(new CExcelField("现住址", "address", false));
		efields.add(new CExcelField("户籍类型", "registertype", false));
		efields.add(new CExcelField("户籍住址", "registeraddress", false));
		efields.add(new CExcelField("社保购买地", "sscurty_addr", false));
		efields.add(new CExcelField("社保开始时间", "sscurty_startdate", false));
		efields.add(new CExcelField("鞋码", "shoesize", false));
		efields.add(new CExcelField("裤码", "pants_code", false));
		efields.add(new CExcelField("上衣码", "coat_code", false));
		efields.add(new CExcelField("计薪方式", "pay_way", false));
		efields.add(new CExcelField("默认班制", "schedtype", false));
		efields.add(new CExcelField("招聘人工号", "rectcode", false));
		efields.add(new CExcelField("招聘人", "rectname", false));
		efields.add(new CExcelField("兴趣特长", "speciality", false));
		efields.add(new CExcelField("备注", "note", false));

		efields.add(new CExcelField("性别", "sex", true));
		efields.add(new CExcelField("学历", "degree", false));
		efields.add(new CExcelField("专业", "major", false));
		efields.add(new CExcelField("出生日期", "birthday", true));
		efields.add(new CExcelField("身份证/护照号码", "id_number", true));
		// efields.add(new CExcelField("身份证地址", "registeraddress", true));
		efields.add(new CExcelField("发证机关", "sign_org", true));
		efields.add(new CExcelField("签发日期", "sign_date", true));
		efields.add(new CExcelField("签发到期", "expired_date", true));
		efields.add(new CExcelField("入职日期", "hiredday", true));
		efields.add(new CExcelField("机构职位编码", "ospcode", true));
		efields.add(new CExcelField("联系电话", "telphone", false));
		efields.add(new CExcelField("紧急联系人", "urgencycontact", false));
		efields.add(new CExcelField("紧急联系人电话", "cellphone", false));
		efields.add(new CExcelField("宿舍床位", "dorm_bed", false));

		efields.add(new CExcelField("离职日期", "ljappdate", false));
		efields.add(new CExcelField("离职类型", "ljtype2", false));
		efields.add(new CExcelField("司龄", "worktime", false));
		efields.add(new CExcelField("离职类别", "ljtype1", false));
		efields.add(new CExcelField("离职原因", "ljreason", false));
		efields.add(new CExcelField("是否补偿", "iscpst", false));
		efields.add(new CExcelField("补偿金额", "cpstarm", false));
		efields.add(new CExcelField("劳动投诉", "iscpt", false));
		efields.add(new CExcelField("申请仲裁", "isabrt", false));
		efields.add(new CExcelField("申请诉讼", "islawsuit", false));
		efields.add(new CExcelField("加入黑名单", "isblacklist", false));
		efields.add(new CExcelField("加封类型", "addtype", false));
		efields.add(new CExcelField("加封类别", "addtype1", false));
		efields.add(new CExcelField("加封原因", "blackreason", false));
		efields.add(new CExcelField("人员来源", "entrysourcr", true));
		efields.add(new CExcelField("派遣机构", "dispunit", true));
		efields.add(new CExcelField("派遣期限", "dispeextime", true));
		efields.add(new CExcelField("输送机构", "transorg", true));
		efields.add(new CExcelField("输送期限", "transextime", true));
		efields.add(new CExcelField("指导老师工号", "advisercode", true));
		efields.add(new CExcelField("指导老师姓名", "advisername", true));
		efields.add(new CExcelField("指导老师电话", "adviserphone", true));
		efields.add(new CExcelField("法人单位", "juridical", true));
		efields.add(new CExcelField("出勤类别", "atdtype", true));
		efields.add(new CExcelField("免考勤打卡", "noclock", true));
		return efields;
	}

	@ACOAction(eventname = "getrewardlist", Authentication = true, ispublic = true, notes = "获取奖惩信息")
	public String getrewardlist() throws Exception {
		String sqlstr = "SELECT e.employee_code,e.employee_name,e.orgname,e.sp_name,e.hg_name,e.lv_num,e.idpath,r.* "
				+ " FROM  hr_employee e,hr_employee_reward r " + " WHERE e.er_id=r.er_id ";
		if (CtrHr_systemparms.getIntValue("ALLFJL") != 1)// 不允许负激励
			sqlstr = sqlstr + " and rwnature=1 ";
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String type = CorUtil.hashMap2Str(urlparms, "type", "需要参数type");
		if ("byid".equalsIgnoreCase(type)) {
			String id = CorUtil.hashMap2Str(urlparms, "id", "需要参数id");
			sqlstr = sqlstr + " and emprw_id=" + id;
		}
		return new CReport(sqlstr, "rewardtime desc ", null).findReport();
	}

	@ACOAction(eventname = "imprewardlistexcel", Authentication = true, ispublic = false, notes = "奖惩导入Excel")
	public String imprewardlistexcel() throws Exception {
		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String batchno = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");// 批次号
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		int rst = 0;
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			rst = parserExcelFile_rwd(p, batchno);
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

	private int parserExcelFile_rwd(Shw_physic_file pf, String batchno) throws Exception {
		String fs = System.getProperty("file.separator");
		String fullname = ConstsSw.geAppParmStr("UDFilePath") + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
		File file = new File(fullname);
		if (!file.exists()) {
			fullname = ConstsSw._root_filepath + "attifiles" + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
			file = new File(fullname);
			if (!file.exists())
				throw new Exception("文件" + fullname + "不存在!");
		}

		// Workbook workbook = CExcelUtil.isExcel2003(fullname) ? new
		// HSSFWorkbook(new FileInputStream(fullname))
		// : new XSSFWorkbook(new FileInputStream(fullname));

		Workbook workbook = WorkbookFactory.create(file);

		int sn = workbook.getNumberOfSheets();
		if (sn <= 0)
			throw new Exception("excel<" + fullname + ">没有sheet");
		Sheet aSheet = workbook.getSheetAt(0);// 获得一个sheet
		return parserExcelSheet_rwd(aSheet, batchno);
	}

	private int parserExcelSheet_rwd(Sheet aSheet, String batchno) throws Exception {
		if (aSheet.getLastRowNum() == 0) {
			return 0;
		}

		String idpathwhere = CSContext.getIdpathwhere();

		List<CExcelField> efds = initExcelFields_rwd();
		efds = CExcelUtil.parserExcelSheetFields(aSheet, efds, 0);// 解析title
		// 并检查必须存在的列
		List<Map<String, String>> values = CExcelUtil.getExcelValues(aSheet, efds, 0);
		Hr_employee emp = new Hr_employee();
		Hr_employee_reward er = new Hr_employee_reward();
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
				String sqlstr = "SELECT * FROM hr_employee WHERE employee_code='" + employee_code + "' " + idpathwhere;
				emp.findBySQL(sqlstr);
				if (emp.isEmpty())
					throw new Exception("工号【" + employee_code + "】不存在或不在权限范围内");
				er.clear();
				er.er_id.setValue(emp.er_id.getValue());
				er.rewardtime.setValue(v.get("rewardtime"));
				er.rreason.setValue(v.get("rreason")); // 激励事由
				er.rwtype.setValue(dictemp.getVbCE("870", v.get("rwtype"), false,
						"工号【" + employee_code + "】激励类型【" + v.get("rwtype") + "】不存在")); // 激励类型
				er.rwnature.setValue(dictemp.getVbCE("711", v.get("rwnature"), false,
						"工号【" + employee_code + "】激励性质【" + v.get("rwnature") + "】不存在"));// 激励性质
				er.rwscore.setValue(v.get("rwscore")); // 激励绩效系数
				er.rwfile.setValue(v.get("rwfile")); // 激励支持文件
				er.rwnote.setValue(v.get("rwnote")); // 激励情况描述
				er.rwfilenum.setValue(v.get("rwfilenum")); // 激励文件字号
				er.remark.setValue(v.get("remark")); // 备注

				int rwnature = er.rwnature.getAsInt(0);
				float rwscore = er.rwscore.getAsFloat(0);
				if (rwnature == 1 && rwscore < 0) {
					throw new Exception("正激励绩效系数不能为负数");
				}
				if (rwnature == 2 && rwscore > 0) {
					throw new Exception("负激励绩效系数不能为正数");
				}
				float lv_num = emp.lv_num.getAsFloat(0);
				float rwamount = ((lv_num < 4) ? 2000 : 500) * rwscore;
				er.rwamount.setValue(rwamount); // 激励金额//v.get("rwamount")

				er.save(con);
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

	private List<CExcelField> initExcelFields_rwd() {
		List<CExcelField> efields = new ArrayList<CExcelField>();
		efields.add(new CExcelField("工号", "employee_code", true));
		efields.add(new CExcelField("激励性质", "rwnature", true));
		efields.add(new CExcelField("激励类型", "rwtype", true));
		efields.add(new CExcelField("激励金额", "rwamount", false));
		efields.add(new CExcelField("激励绩效系数", "rwscore", false));
		efields.add(new CExcelField("激励情况描述", "rwnote", false));
		efields.add(new CExcelField("激励支持文件", "rwfile", false));
		efields.add(new CExcelField("激励文件字号", "rwfilenum", false));
		efields.add(new CExcelField("激励生效日期", "rewardtime", false));
		efields.add(new CExcelField("备注", "remark", false));
		return efields;
	}

	@ACOAction(eventname = "impphotoexcel", Authentication = true, ispublic = false, notes = "相片同步Excel")
	public String impphotoexcel() throws Exception {
		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String batchno = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");// 批次号
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		int rst = 0;
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			rst = parserExcelFile_photo(p, batchno);
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

	private int parserExcelFile_photo(Shw_physic_file pf, String batchno) throws Exception {
		String fs = System.getProperty("file.separator");
		String fullname = ConstsSw.geAppParmStr("UDFilePath") + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
		File file = new File(fullname);
		if (!file.exists()) {
			fullname = ConstsSw._root_filepath + "attifiles" + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
			file = new File(fullname);
			if (!file.exists())
				throw new Exception("文件" + fullname + "不存在!");
		}

		// Workbook workbook = CExcelUtil.isExcel2003(fullname) ? new
		// HSSFWorkbook(new FileInputStream(fullname))
		// : new XSSFWorkbook(new FileInputStream(fullname));

		Workbook workbook = WorkbookFactory.create(file);

		int sn = workbook.getNumberOfSheets();
		if (sn <= 0)
			throw new Exception("excel<" + fullname + ">没有sheet");
		Sheet aSheet = workbook.getSheetAt(0);// 获得一个sheet
		return parserExcelSheet_photo(aSheet, batchno);
	}

	private int parserExcelSheet_photo(Sheet aSheet, String batchno) throws Exception {
		if (aSheet.getLastRowNum() == 0) {
			return 0;
		}
		List<CExcelField> efds = initExcelFields_photo();
		efds = CExcelUtil.parserExcelSheetFields(aSheet, efds, 1);// 解析title
		// 并检查必须存在的列
		List<Map<String, String>> values = CExcelUtil.getExcelValues(aSheet, efds, 1);
		int rst = 0;
		try {
			for (Map<String, String> v : values) {
				String employee_code = v.get("employee_code");
				if ((employee_code == null) || (employee_code.isEmpty()))
					continue;
				if (TimerTaskHRZM.syncempphoto(employee_code))
					rst++;
			}
			return rst;
		} catch (Exception e) {
			throw e;
		}
	}

	private List<CExcelField> initExcelFields_photo() {
		List<CExcelField> efields = new ArrayList<CExcelField>();
		efields.add(new CExcelField("工号", "employee_code", true));
		return efields;
	}

	// @ACOAction(eventname = "find", Authentication = true, ispublic = false,
	// notes = "人事资料查询")
	// public String find() {
	// String sqlstr = "";
	//
	// }
	@ACOAction(eventname = "putmonthemployee", Authentication = true, ispublic = true, notes = "人事信息月结")
	public String putmonthemployee() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		String ym = CorUtil.hashMap2Str(parms, "ym", "需要参数ym");// yyyy-mm 年月
		JSONObject rst = new JSONObject();
		rst.put("result", CtrHrPerm.putmonthemployee(ym));
		return rst.toString();
	}

	@ACOAction(eventname = "findEmployeeAdvtech", Authentication = true, ispublic = true, notes = "高技人事信息")
	public String findEmployeeAdvtech() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String type = CorUtil.hashMap2Str(urlparms, "type", "需要参数type");
		String sqlwhere = urlparms.get("sqlwhere");
		if ("list".equalsIgnoreCase(type)) {
			String sqlstr = "SELECT e.* FROM hr_employee e,hr_orgposition op"
					+ " WHERE e.ospid=op.ospid AND op.isadvtech=1 AND e.empstatid>0 AND e.empstatid<10";
			return new CReport(sqlstr, null).findReport();
		}
		if ("byid".equalsIgnoreCase(type)) {
			String id = CorUtil.hashMap2Str(urlparms, "id", "需要参数id");
			Hr_employee emp = new Hr_employee();
			emp.findByID(id);
			return emp.toString();
		}
		return null;
	}

	@ACOAction(eventname = "findHrClass", Authentication = true, ispublic = true, notes = "职类列表")
	public String findHrClass() throws Exception {
		String sqlstr = "select * from hr_wclass where type_id=1 and usable=1";
		return DBPools.defaultPool().opensql2json(sqlstr);
	}

}
