package com.hr.publishedco;

import java.util.List;
import java.util.Date;
//以上二项新增

import java.util.HashMap;

import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CjpaUtil;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.CDBPool;
import com.corsair.dbpool.DBPools;
import com.corsair.dbpool.util.CJSON;
import com.corsair.dbpool.util.JSONParm;
import com.corsair.dbpool.util.Logsw;
import com.corsair.dbpool.util.PraperedSql;
import com.corsair.dbpool.util.PraperedValue;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.base.Login;
import com.corsair.server.csession.CSession;
import com.corsair.server.genco.COCSCommon;
import com.corsair.server.generic.Shworg;
import com.corsair.server.generic.Shworguser;
import com.corsair.server.generic.Shwuser;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CorUtil;
import com.corsair.server.util.DictionaryTemp;
import com.hr.attd.entity.Hrkq_business_trip;
import com.hr.attd.entity.Hrkq_holidayapp;
import com.hr.attd.entity.Hrkq_holidayapp_cancel;
import com.hr.attd.entity.Hrkq_holidaytype;
import com.hr.attd.entity.Hrkq_overtime;
import com.hr.attd.entity.Hrkq_overtime_hour;
import com.hr.attd.entity.Hrkq_overtime_line;
import com.hr.attd.entity.Hrkq_resign;
import com.hr.attd.entity.Hrkq_wkoff;
import com.hr.attd.entity.Hrkq_wkoffsourse;
import com.hr.base.entity.Hr_orgposition;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_employee_linked;
import com.hr.perm.entity.Hr_entry;
import com.hr.salary.co.Cosacommon;
import com.hr.util.HRUtil;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;
import com.hr.attd.ctr.HrkqUtil;
import com.hr.attd.entity.Hrkq_bckqrst;//以下二项新增

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@ACO(coname = "hr.published1")
public class COPublished1 {
	@ACOAction(eventname = "hrkq_holidayapp_new", Authentication = false, notes = "创建请假表单并自动提交生效")
	public String hrkq_holidayapp_new() throws Exception {
		Hrkq_holidayapp happ = new Hrkq_holidayapp();// 创建请假表单实体
		happ.fromjson(CSContext.getPostdata());// 从前端JSON获取数据
		if (happ.employee_code.isEmpty()) {
			new Exception("请假表单字段【employee_code】不能为空");
		}
		if (happ.hdays.isEmpty()) {
			new Exception("请假表单字段【hdays】不能为空");
		}
		if (happ.timebg.isEmpty()) {
			new Exception("请假表单字段【timebg】不能为空");
		}
		if (happ.timeed.isEmpty()) {
			new Exception("请假表单字段【timeed】不能为空");
		}
		if (happ.htid.isEmpty()) {
			new Exception("请假表单字段【htid】不能为空");
		}
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + happ.employee_code.getValue() + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + happ.er_id.getValue() + "】的人事资料不存在");
		Hrkq_holidaytype ht = new Hrkq_holidaytype();// 创建假期类型实体
		ht.findByID(happ.htid.getValue());
		if (ht.isEmpty())
			new Exception("ID为【" + happ.htid.getValue() + "】的假期类型不存在");

		happ.er_id.setValue(emp.er_id.getValue()); // 人事ID
		happ.employee_code.setValue(emp.employee_code.getValue()); // 工号
		happ.employee_name.setValue(emp.employee_name.getValue()); // 姓名
		happ.orgid.setValue(emp.orgid.getValue()); // 部门ID
		happ.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
		happ.orgname.setValue(emp.orgname.getValue()); // 部门
		happ.ospid.setValue(emp.ospid.getValue()); // 职位ID
		happ.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
		happ.sp_name.setValue(emp.sp_name.getValue()); // 职位
		happ.lv_num.setValue(emp.lv_num.getValue()); // 职级
		// happ.hdays.setValue(value); // 请假天数
		happ.hdaystrue.setValue(happ.hdays.getValue()); // 实际天数 如果没销假 则==销假天数
		// happ.timebg.setValue(value); // 请假时间开始 yyyy-MM-dd hh:mm
		// happ.timeed.setValue(value); // 请假时间截止 yyyy-MM-dd hh:mm
		happ.timeedtrue.setValue(happ.timeed.getValue()); // 实际截止时间 如果有销假 按销假时间 否则按申请截止时间 yyyy-MM-dd hh:mm
		happ.htid.setValue(ht.htid.getValue()); // 类型ID
		happ.htname.setValue(ht.htname.getValue()); // 假期类型
		// happ.htreason.setValue(value); // 事由
		happ.htconfirm.setValue("1"); // 假期确认 //符合规则 违反规则 空
		happ.viodeal.setValue(null); // 违规处理 //事假 旷工 空
		happ.timebk.setValue(null); // 销假时间 yyyy-MM-dd hh:mm
		happ.btconfirm.setValue(null); // 销假确认
		happ.iswfagent.setValue("1"); // 启用流程代理
		happ.stat.setValue("1"); // 表单状态
		happ.idpath.setValue(emp.idpath.getValue()); // idpath
		happ.entid.setValue("1"); // entid
		happ.creator.setValue("inteface"); // 创建人
		happ.createtime.setAsDatetime(new Date()); // 创建时间
		happ.orghrlev.setValue("0");
		happ.emplev.setValue("0");
		happ.creator.setValue("inteface");
		happ.entid.setValue("1");


		CDBConnection con = happ.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			// System.out.println("happ json:" + happ.tojson());
			happ.save(con);// 保存请假表单
			// happ.wfcreate(null, con);// 提交表单（无流程）
			Shwuser admin = Login.getAdminUser();
			happ.wfcreate(null, con, "1", "1", null);// 当前Session无登录验证，用任意管理员账号 提交表单（无流程）
			con.submit();// 提交数据库事务

			// String rxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><xml><haid>" + happ.haid.getValue() + "</haid><hacode>" + happ.hacode.getValue() +
			// "</hacode></xml>";
			// /return rxml;
			// return happ.toxml();
			return happ.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}

	@ACOAction(eventname = "testfindhapp_cancel", Authentication = false, notes = "创建销假表单并自动提交生效")
	public String testfindhapp_cancel() throws Exception {
		Hrkq_holidayapp_cancel happ_cancel = new Hrkq_holidayapp_cancel();// 创建销假表单实体
		happ_cancel.findByID("2733");
		return happ_cancel.tojson();
	}

	@ACOAction(eventname = "hrkq_holidayapp_cancel_new", Authentication = false, notes = "创建销假表单并自动提交生效")
	public String hrkq_holidayapp_cancel_new() throws Exception {
		Hrkq_holidayapp_cancel happ_cancel = new Hrkq_holidayapp_cancel();// 创建销假表单实体
		happ_cancel.fromjson(CSContext.getPostdata());// 从前端JSON获取数据

		if (happ_cancel.hacode.isEmpty())
			new Exception("销假表单字段【hacode】不能为空");

		if (happ_cancel.canceltime.isEmpty()) {
			new Exception("销假表单字段【canceltime】不能为空");
		}
		if (happ_cancel.cancelreason.isEmpty()) {
			new Exception("销假表单字段【cancelreason】不能为空");
		}
		if (happ_cancel.hdaystrue.isEmpty()) {
			new Exception("销假表单字段【hdaystrue】不能为空");
		}

		Hrkq_holidayapp happ = new Hrkq_holidayapp();// 创建请假表单实体
		String sqlstr = "select * from hrkq_holidayapp where hacode='" + happ_cancel.hacode.getValue() + "'";
		happ.findBySQL(sqlstr);// 查询请假表单实体
		if (happ.isEmpty())
			new Exception("CODE为【" + happ_cancel.hacode.getValue() + "】的请假表单不存在");

		happ_cancel.haid.setValue(happ.haid.getValue()); // 请假表单ID
		// happ_cancel.hacode.setValue(value); // 请假表单CODE
		// happ_cancel.canceltime.setValue(value); // 销假时间
		// happ_cancel.cancelreason.setValue(value); // 销假事由

		happ_cancel.er_id.setValue(happ.er_id.getValue()); // 人事ID
		happ_cancel.employee_code.setValue(happ.employee_code.getValue()); // 工号
		happ_cancel.employee_name.setValue(happ.employee_name.getValue()); // 姓名
		happ_cancel.orgid.setValue(happ.orgid.getValue()); // 部门ID
		happ_cancel.orgcode.setValue(happ.orgcode.getValue()); // 部门编码
		happ_cancel.orgname.setValue(happ.orgname.getValue()); // 部门
		happ_cancel.ospid.setValue(happ.ospid.getValue()); // 职位ID
		happ_cancel.ospcode.setValue(happ.ospcode.getValue()); // 职位编码
		happ_cancel.sp_name.setValue(happ.sp_name.getValue()); // 职位
		happ_cancel.lv_num.setValue(happ.lv_num.getValue()); // 职级
		happ_cancel.hdays.setValue(happ.hdays.getValue()); // 请假天数
		// happ_cancel.hdaystrue.setValue(value); // 实际天数 如果没销假 则==销假天数hdaystrue
		happ_cancel.timebg.setValue(happ.timebg.getValue()); // 请假时间开始 yyyy-MM-dd hh:mm
		happ_cancel.timeed.setValue(happ.timeed.getValue()); // 请假时间截止 yyyy-MM-dd hh:mm
		happ_cancel.orghrlev.setValue(happ.orghrlev.getValue()); // 机构人事层级
		happ_cancel.emplev.setValue(happ.emplev.getValue()); // 人事层级
		// happ_cancel.remark.setValue(value); // 备注

		happ_cancel.stat.setValue("1"); // 表单状态
		happ_cancel.idpath.setValue(happ.idpath.getValue()); // idpath
		happ_cancel.entid.setValue("1"); // entid
		happ_cancel.creator.setValue("inteface"); // 创建人
		happ_cancel.createtime.setAsDatetime(new Date()); // 创建时间

		CDBConnection con = happ_cancel.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			// System.out.println("happ_cancel json:" + happ_cancel.tojson());
			happ_cancel.save(con);// 保存销假表单
			// happ_cancel.wfcreate(null, con);// 提交表单（无流程）
			Shwuser admin = Login.getAdminUser();
			happ_cancel.wfcreate(null, con, "1", "1", null);// 当前Session无登录验证，用任意管理员账号 提交表单（无流程）
			con.submit();// 提交数据库事务

			// String rxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><xml><hacid>" + happ_cancel.hacid.getValue() + "</hacid><haccode>" +
			// //happ_cancel.haccode.getValue() +
			// "</haccode></xml>";
			// /return rxml;
			// return happ_cancel.toxml();
			return happ_cancel.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}

	@ACOAction(eventname = "findempsuper_test", Authentication = false, notes = "查找人员上级名单")
	public String findempsuper_test() throws Exception {

		HashMap<String, String> urlparms = CSContext.get_pjdataparms();

		String stremployee_code = "270884";

		String strtype = "中心负责人";

		String sqlstr = "SELECT * FROM hrms_emp_super ";
		sqlstr = sqlstr + " where employee_code='" + stremployee_code + "' and type='" + strtype + "'";
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}

	@ACOAction(eventname = "findempsuper", Authentication = false, notes = "查找人员上级名单")
	public String findempsuper() throws Exception {
		// HashMap<String, String> parms = CSContext.getParms();
		// String eparms = parms.get("parms");

		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String stremployee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");
		String strtype = CorUtil.hashMap2Str(urlparms, "type", "需要参数type");

		String sqlstr = "SELECT * FROM hrms_emp_super ";
		sqlstr = sqlstr + " where employee_code='" + stremployee_code + "' and type='" + strtype + "'";
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}

	@ACOAction(eventname = "testbusinesstrip", Authentication = false, notes = "创建出差单并自动提交生效")
	public String testbusinesstrip() throws Exception {
		Hrkq_business_trip happ_btrip = new Hrkq_business_trip();// 创建出差单实体
		happ_btrip.findByID("32");
		return happ_btrip.tojson();
	}

	@ACOAction(eventname = "Hrkq_business_trip_new", Authentication = false, notes = "创建出差单并自动提交生效")
	public String Hrkq_business_trip_new() throws Exception {
		Hrkq_business_trip happ_btrip = new Hrkq_business_trip();// 创建出差单实体
		happ_btrip.fromjson(CSContext.getPostdata());// 从前端JSON获取数据

		if (happ_btrip.employee_code.isEmpty()) {
			new Exception("出差单字段【employee_code】不能为空");
		}
		if (happ_btrip.tripdays.isEmpty()) {
			new Exception("出差单字段【tripdays】不能为空");
		}
		if (happ_btrip.begin_date.isEmpty()) {
			new Exception("出差单字段【begin_date】不能为空");
		}
		if (happ_btrip.end_date.isEmpty()) {
			new Exception("出差单字段【end_date】不能为空");
		}
		if (happ_btrip.trip_type.isEmpty()) {
			new Exception("出差单字段【trip_type】不能为空");
		}
		if (happ_btrip.destination.isEmpty()) {
			new Exception("出差单字段【destination】不能为空");
		}
		if (happ_btrip.tripreason.isEmpty()) {
			new Exception("出差单字段【tripreason】不能为空");
		}
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + happ_btrip.employee_code.getValue() + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + happ_btrip.er_id.getValue() + "】的人事资料不存在");

		if ((happ_btrip.trip_type.getAsIntDefault(0) != 1) && (happ_btrip.trip_type.getAsIntDefault(0) != 2))
			new Exception("ID为【" + happ_btrip.trip_type + "】的出差类型不存在");

		happ_btrip.er_id.setValue(emp.er_id.getValue()); // 人事ID
		happ_btrip.employee_code.setValue(emp.employee_code.getValue()); // 工号
		happ_btrip.employee_name.setValue(emp.employee_name.getValue()); // 姓名
		happ_btrip.orgid.setValue(emp.orgid.getValue()); // 部门ID
		happ_btrip.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
		happ_btrip.orgname.setValue(emp.orgname.getValue()); // 部门
		happ_btrip.ospid.setValue(emp.ospid.getValue()); // 职位ID
		happ_btrip.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
		happ_btrip.sp_name.setValue(emp.sp_name.getValue()); // 职位
		happ_btrip.lv_num.setValue(emp.lv_num.getValue()); // 职级

		happ_btrip.trip_type.setValue(happ_btrip.trip_type.getValue()); // 出差类型
		happ_btrip.tripreason.setValue(happ_btrip.tripreason.getValue()); // 事由
		happ_btrip.destination.setValue(happ_btrip.destination.getValue()); // 目的地

		happ_btrip.tripdays.setValue(happ_btrip.tripdays.getValue()); // 实际天数

		happ_btrip.begin_date.setValue(happ_btrip.begin_date.getValue()); // 实际开始时间
		happ_btrip.end_date.setValue(happ_btrip.end_date.getValue()); // 实际截止时间

		happ_btrip.iswfagent.setValue("1"); // 启用流程代理
		happ_btrip.stat.setValue("1"); // 表单状态
		happ_btrip.idpath.setValue(emp.idpath.getValue()); // idpath
		happ_btrip.creator.setValue("inteface"); // 创建人
		happ_btrip.createtime.setAsDatetime(new Date()); // 创建时间
		happ_btrip.orghrlev.setValue("0");
		happ_btrip.emplev.setValue("0");
		happ_btrip.entid.setValue("1");

		CDBConnection con = happ_btrip.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			// System.out.println("happ json:" + happ.tojson());
			happ_btrip.save(con);// 保存出差表单
			// happ.wfcreate(null, con);// 提交表单（无流程）
			Shwuser admin = Login.getAdminUser();
			happ_btrip.wfcreate(null, con, "1", "1", null);// 当前Session无登录验证，用任意管理员账号 提交表单（无流程）
			con.submit();// 提交数据库事务

			// String rxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><xml><haid>" + happ_btrip.haid.getValue() + "</haid><hacode>" + happ.hacode.getValue() +
			// "</hacode></xml>";
			// /return rxml;
			// return happ.toxml();
			return happ_btrip.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}

	@ACOAction(eventname = "testovertime", Authentication = false, notes = "创建加班单并自动提交生效")
	public String testovertime() throws Exception {
		Hrkq_overtime happ_overtime = new Hrkq_overtime();// 创建加班单实体
		happ_overtime.findByID("23");
		return happ_overtime.tojson();
	}

	@ACOAction(eventname = "hrkq_overtime_new", Authentication = false, notes = "创建加班单并自动提交生效")
	public String hrkq_overtime_new() throws Exception {
		Hrkq_overtime happ_overtime = new Hrkq_overtime();// 创建加班单实体
		happ_overtime.fromjson(CSContext.getPostdata());// 从前端JSON获取数据

		// System.out.println("hrkq_overtime_new json:" + happ_overtime.tojson());

		if (happ_overtime.orgcode.isEmpty()) {
			new Exception("加班单字段【orgcode】不能为空");
		}

		if (happ_overtime.over_type.isEmpty()) {
			new Exception("加班单字段【overtype】不能为空");
		}

		if (happ_overtime.dealtype.isEmpty()) {
			new Exception("加班单字段【dealtype】不能为空");
		}
		if (happ_overtime.otrate.isEmpty()) {
			new Exception("加班单字段【otrate】不能为空");
		}
		if (happ_overtime.otreason.isEmpty()) {
			new Exception("加班单字段【otreason】不能为空");
		}
		if (happ_overtime.isoffjob.isEmpty()) {
			new Exception("加班单字段【isoffjob】不能为空");
		}
		if ((happ_overtime.over_type.getAsIntDefault(0) != 1) && (happ_overtime.over_type.getAsIntDefault(0) != 2))
			new Exception("ID为【" + happ_overtime.over_type + "】的加班类型不存在");

		Shworg borg = new Shworg();// 创建组织实体
		String sqlstr = "select * from shworg where code='" + happ_overtime.orgcode.getValue() + "'";
		borg.findBySQL(sqlstr);// 查询组织档案
		if (borg.isEmpty())
			new Exception("ID为【" + happ_overtime.orgcode.getValue() + "】的组织资料不存在");

		happ_overtime.orgid.setValue(borg.orgid.getValue()); // 组织ID
		happ_overtime.orgcode.setValue(happ_overtime.orgcode.getValue()); // 组织编号
		happ_overtime.orgname.setValue(borg.extorgname.getValue()); // 组织名称
		happ_overtime.idpath.setValue(borg.idpath.getValue()); // 组织ID结构

		// happ_overtime.over_type.setValue(happ_overtime.over_type.getValue());
		// happ_overtime.dealtype.setValue(happ_overtime.dealtype.getValue());
		// happ_overtime.otrate.setValue(happ_overtime.otrate.getValue());
		// happ_overtime.otreason.setValue(happ_overtime.otreason.getValue());
		// happ_overtime.isoffjob.setValue(happ_overtime.isoffjob.getValue());

		Hr_employee emp = new Hr_employee();// 创建人事档案实体

		CJPALineData<Hrkq_overtime_line> otlist = happ_overtime.hrkq_overtime_lines;
		for (CJPABase sl1 : otlist) {
			Hrkq_overtime_line otl = (Hrkq_overtime_line) sl1;
			if (otl.employee_code.isEmpty()) {
				new Exception("加班单字段【employee_code】不能为空");
			}
			if (otl.allothours.isEmpty()) {
				new Exception("加班单字段【allothours】不能为空");
			}
			sqlstr = "select * from hr_employee where employee_code='" + otl.employee_code.getValue() + "'";
			emp.findBySQL(sqlstr);// 查询人事档案
			if (emp.isEmpty())
				new Exception("ID为【" + happ_overtime.er_id.getValue() + "】的人事资料不存在");
			otl.er_id.setValue(emp.er_id.getValue()); // 人事ID
			otl.employee_code.setValue(emp.employee_code.getValue()); // 工号
			otl.employee_name.setValue(emp.employee_name.getValue()); // 姓名
			otl.orgid.setValue(emp.orgid.getValue()); // 部门ID
			otl.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
			otl.orgname.setValue(emp.orgname.getValue()); // 部门
			otl.ospid.setValue(emp.ospid.getValue()); // 职位ID
			otl.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
			otl.sp_name.setValue(emp.sp_name.getValue()); // 职位
			otl.lv_num.setValue(emp.lv_num.getValue()); // 职级
			// otl.allothours.setValue(otl.allothours.getValue()); // 实际天数

			CJPALineData<Hrkq_overtime_hour> othlist = otl.hrkq_overtime_hours;
			for (CJPABase sl2 : othlist) {
				Hrkq_overtime_hour oth = (Hrkq_overtime_hour) sl2;
				if (oth.begin_date.isEmpty()) {
					new Exception("加班单字段【begin_date】不能为空");
				}
				if (oth.end_date.isEmpty()) {
					new Exception("加班单字段【end_dae】不能为空");
				}
				if (oth.needchedksb.isEmpty()) {
					new Exception("加班单字段【needchedksb】不能为空");
				}
				if (oth.needchedkxb.isEmpty()) {
					new Exception("加班单字段【needchedkxb】不能为空");
				}

				oth.er_id.setValue(emp.er_id.getValue());
				oth.employee_code.setValue(emp.employee_code.getValue());
				oth.employee_name.setValue(emp.employee_name.getValue());

				// oth.begin_date.setValue(happ_overtime.begin_date.getValue()); // 申请加班开始时间
				// oth.end_date.setValue(happ_overtime.end_date.getValue()); // 申请加班结束时间
				// oth.needchedksb.setValue(happ_overtime.needchedksb.getValue()); // 上班需要打卡
				// oth.needchedkxb.setValue(happ_overtime.needchedkxb.getValue()); // 下班需要打卡
				// otl.hrkq_overtime_hours.add(oth);
			}
			// happ_overtime.hrkq_overtime_lines.add(otl);
		}

		// otls=otlist.toarray();
		// happ_overtime.iswfagent.setValue("1"); // 启用流程代理
		happ_overtime.stat.setValue("1"); // 表单状态
		happ_overtime.entid.setValue("1"); // entid
		happ_overtime.creator.setValue("inteface"); // 创建人
		happ_overtime.createtime.setAsDatetime(new Date()); // 创建时间
		happ_overtime.orghrlev.setValue("0");
		happ_overtime.emplev.setValue("0");

		CDBConnection con = happ_overtime.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			// System.out.println("happ json:" + happ.tojson());
			happ_overtime.save(con);// 保存加班表单
			// happ.wfcreate(null, con);// 提交表单（无流程）
			Shwuser admin = Login.getAdminUser();
			happ_overtime.wfcreate(null, con, "1", "1", null);// 当前Session无登录验证，用任意管理员账号 提交表单（无流程）
			con.submit();// 提交数据库事务

			// String rxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><xml><haid>" + happ_overtime.haid.getValue() + "</haid><hacode>" +
			// happ.hacode.getValue() +
			// "</hacode></xml>";
			// /return rxml;
			// return happ.toxml();
			return happ_overtime.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}

	@ACOAction(eventname = "testfindwkoff", Authentication = false, notes = "创建调休表单并自动提交生效")
	public String testfindwkoff() throws Exception {
		Hrkq_wkoff happwkoff = new Hrkq_wkoff();// 创建调休表单实体
		happwkoff.findByID("2733");
		return happwkoff.tojson();
	}

	@ACOAction(eventname = "Hrkq_wkoff_new", Authentication = false, notes = "创建调休表单并自动提交生效")
	public String Hrkq_wkoff_new() throws Exception {
		Hrkq_wkoff happwkoff = new Hrkq_wkoff();// 创建调休表单实体
		// Hrkq_wkoffsourse happwkoffss = new Hrkq_wkoffsourse();// 创建调休表单体
		happwkoff.fromjson(CSContext.getPostdata());// 从前端JSON获取数据
		if (happwkoff.employee_code.isEmpty()) {
			new Exception("调休表单字段【employee_code】不能为空");
		}
		if (happwkoff.begin_date.isEmpty()) {
			new Exception("调休表单字段【begin_date】不能为空");
		}
		if (happwkoff.end_date.isEmpty()) {
			new Exception("调休表单字段【end_date】不能为空");
		}
		if (happwkoff.wodays.isEmpty()) {
			new Exception("调休表单字段【wodays】不能为空");
		}
		if (happwkoff.reason.isEmpty()) {
			new Exception("调休表单字段【reason】不能为空");
		}
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + happwkoff.employee_code.getValue() + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + happwkoff.employee_code.getValue() + "】的人事资料不存在");

		happwkoff.er_id.setValue(emp.er_id.getValue()); // 人事ID
		happwkoff.employee_code.setValue(emp.employee_code.getValue()); // 工号
		happwkoff.employee_name.setValue(emp.employee_name.getValue()); // 姓名
		happwkoff.orgid.setValue(emp.orgid.getValue()); // 部门ID
		happwkoff.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
		happwkoff.orgname.setValue(emp.orgname.getValue()); // 部门
		// happwkoff.ospid.setValue(emp.ospid.getValue()); // 职位ID
		// happwkoff.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
		happwkoff.sp_name.setValue(emp.sp_name.getValue()); // 职位
		happwkoff.lv_num.setValue(emp.lv_num.getValue()); // 职级

		happwkoff.wodays.setValue(happwkoff.wodays.getValue()); // 实际天数
		happwkoff.begin_date.setValue(happwkoff.begin_date.getValue()); // 调休时间开始
		happwkoff.end_date.setValue(happwkoff.end_date.getValue()); // 调休时间截止
		happwkoff.reason.setValue(happwkoff.reason.getValue()); // 事由

		CJPALineData<Hrkq_wkoffsourse> wss = happwkoff.hrkq_wkoffsourses;
		for (CJPABase jpa1 : wss) {
			Hrkq_wkoffsourse ws = (Hrkq_wkoffsourse) jpa1;
			ws.lbid.setValue(ws.lbid.getValue());
			ws.lbname.setValue(ws.lbname.getValue());
			ws.stype.setValue(ws.stype.getValue());
			ws.sccode.setValue(ws.sccode.getValue());
			ws.alllbtime.setValue(ws.alllbtime.getValue());
			ws.usedlbtime.setValue(ws.usedlbtime.getValue());
			ws.valdate.setValue(ws.valdate.getValue());
			ws.note.setValue(ws.note.getValue());

			ws.wotime.setValue(ws.wotime.getValue());

			// happwkoff.Hrkq_wkoffsourse.add(ws);
		}
		// happwkoff.iswfagent.setValue("1"); // 启用流程代理
		happwkoff.stat.setValue("1"); // 表单状态
		happwkoff.idpath.setValue(emp.idpath.getValue()); // idpath
		happwkoff.entid.setValue("1"); // entid
		happwkoff.creator.setValue("inteface");// 创建人
		happwkoff.createtime.setAsDatetime(new Date()); // 创建时间
		happwkoff.orghrlev.setValue("0");
		happwkoff.emplev.setValue("0");

		CDBConnection con = happwkoff.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			// System.out.println("happwkoff json:" + happwkoff.tojson());
			happwkoff.save(con);// 保存调休表单
			// happwkoff.wfcreate(null, con);// 提交表单（无流程）
			Shwuser admin = Login.getAdminUser();
			happwkoff.wfcreate(null, con, "1", "1", null);// 当前Session无登录验证，用任意管理员账号 提交表单（无流程）
			con.submit();// 提交数据库事务

			// String rxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><xml><haid>" + happ.haid.getValue() + "</haid><hacode>" + happ.hacode.getValue() +
			// "</hacode></xml>";
			// /return rxml;
			// return happ.toxml();
			return happwkoff.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}

	@ACOAction(eventname = "testfindresign", Authentication = false, notes = "创建补签表单并自动提交生效")
	public String testfindresign() throws Exception {
		Hrkq_resign happresign = new Hrkq_resign();// 创建补签表单实体
		happresign.findByID("2733");
		return happresign.tojson();
	}

	@ACOAction(eventname = "hrkq_resign_new", Authentication = false, notes = "创建补签表单并自动提交生效")
	public String hrkq_resign_new() throws Exception {
		Hrkq_resign happresign = new Hrkq_resign();// 创建补签表单实体
		happresign.fromjson(CSContext.getPostdata());// 从前端JSON获取数据
		if (happresign.employee_code.isEmpty()) {
			new Exception("补签表单字段【employee_code】不能为空");
		}
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + happresign.employee_code.getValue() + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("工号为【" + happresign.employee_code.getValue() + "】的人事资料不存在");
		happresign.er_id.setValue(emp.er_id.getValue()); // 人事ID
		happresign.employee_code.setValue(emp.employee_code.getValue()); // 工号
		happresign.employee_name.setValue(emp.employee_name.getValue()); // 姓名
		happresign.orgid.setValue(emp.orgid.getValue()); // 部门ID
		happresign.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
		happresign.orgname.setValue(emp.orgname.getValue()); // 部门
		happresign.ospid.setValue(emp.ospid.getValue()); // 职位ID
		happresign.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
		happresign.sp_name.setValue(emp.sp_name.getValue()); // 职位
		happresign.lv_num.setValue(emp.lv_num.getValue()); // 职级
		happresign.stat.setValue("1"); // 表单状态
		happresign.idpath.setValue(emp.idpath.getValue()); // idpath
		happresign.entid.setValue("1"); // entid
		happresign.creator.setValue("inteface");// 创建人
		happresign.createtime.setAsDatetime(new Date()); // 创建时间
		happresign.orghrlev.setValue("0");
		happresign.emplev.setValue("0");
		CDBConnection con = happresign.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			happresign.save(con);// 保存补签表单
			Shwuser admin = Login.getAdminUser();
			happresign.wfcreate(null, con, "1", "1", null);// 当前Session无登录验证，用任意管理员账号 提交表单（无流程）
			con.submit();// 提交数据库事务
			return happresign.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}

	@ACOAction(eventname = "testchgemail", Authentication = false, notes = "修改邮件")
	public String testchgemail() throws Exception {
		Hr_employee emp = new Hr_employee();// 人事实体
		emp.findByID("2733");
		return emp.tojson();
	}

	@ACOAction(eventname = "hr_chgemail_new", Authentication = false, notes = "修改邮件")
	public String hr_chgemail_new() throws Exception {
		JSONObject jsonObject = JSONObject.fromObject(CSContext.getPostdata());// 从前端JSON获取数据
		String employee_code = jsonObject.getString("employee_code");
		String email = jsonObject.getString("email");
		if (employee_code.isEmpty()) {
			new Exception("人事档案字段【employee_code】不能为空");
		}
		if (email.isEmpty()) {
			new Exception("人事档案字段【email】不能为空");
		}
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		emp.email.setValue(email);
		emp.save();
		return emp.toString();
	}

	@ACOAction(eventname = "testqrykqresult", Authentication = false, notes = "查询考勤结果")
	public String testqrykqresult() throws Exception {
		Hr_employee emp = new Hr_employee();// 人事实体
		emp.findByID("2733");
		return emp.tojson();
	}

	// @ACOAction(eventname = "hr_qrykqresult_new", Authentication = false, notes = "查询考勤结果")
	// public String qrykqresult_new() throws Exception {
	// JSONObject jsonObject = JSONObject.fromObject(CSContext.getPostdata());// 从前端JSON获取数据
	// String employee_code = jsonObject.getString("employee_code");
	// String bgdate = jsonObject.getString("bgdate");
	// String eddate = jsonObject.getString("eddate");
	// if (employee_code.isEmpty()) {
	// new Exception("字段【employee_code】不能为空");
	// }
	// if (bgdate.isEmpty()) {
	// new Exception("字段【bgdate】不能为空");
	// }
	// if (eddate.isEmpty()) {
	// new Exception("字段【eddate】不能为空");
	// }
	// Hr_employee emp = new Hr_employee();// 创建人事档案实体
	// String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
	// emp.findBySQL(sqlstr);// 查询人事档案
	// if (emp.isEmpty())
	// new Exception("ID为【" + employee_code + "】的人事资料不存在");
	// String sql = "SELECT a.bckqid,a.er_id,a.kqdate,a.scdname,a.sclid,a.bcno,a.frtime,a.totime,a.frsktime,a.tosktime,";
	// sql = sql + "frst,trst,a.mtslate,a.mtslearly,lrst,a.extcode,a.exttype,b.employee_code,dpt.orgname,b.lv_num ZhiJi,dpt.attribute1";
	// sql = sql + " FROM hrkq_bckqrst a";
	// sql = sql + " INNER JOIN hr_employee b ON a.er_id=b.er_id";
	// sql = sql + " LEFT JOIN shworg dpt ON dpt.code=b.orgcode";
	// sql = sql + " WHERE a.lrst IN(12) and employee_code='" + employee_code + "' and kqdate>='" + bgdate + "' and kqdate<='" + eddate + "'";
	// return emp.pool.opensql2json(sqlstr);
	// }

	@ACOAction(eventname = "testqryresign", Authentication = false, notes = "查询考勤补签")
	public String testqryresign() throws Exception {
		Hr_employee emp = new Hr_employee();// 人事实体
		emp.findByID("2733");
		return emp.tojson();
	}

	@ACOAction(eventname = "hr_qrykqresign_new", Authentication = false, notes = "查询考勤补签")
	public String hr_qrykqresign_new() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");
		String bgdate = CorUtil.hashMap2Str(urlparms, "bgdate", "需要参数bgdate");
		String eddate = CorUtil.hashMap2Str(urlparms, "eddate", "需要参数eddate");
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		String sql = "SELECT * from v_csr_hrkq_resign  WHERE employee_code='" + employee_code + "' and kqdate>='" + bgdate + "' and kqdate<='" + eddate + "'";
		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sql);
	}

	@ACOAction(eventname = "hr_qrykqresult_new", Authentication = false, notes = "查询考勤结果")
	public String hr_qrykqresult_new() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");
		String bgdate = CorUtil.hashMap2Str(urlparms, "bgdate", "需要参数bgdate");
		String eddate = CorUtil.hashMap2Str(urlparms, "eddate", "需要参数eddate");
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		String sql = "SELECT * from view_hrkq_bckqrst WHERE employee_code='" + employee_code + "' and kqdate>='" + bgdate + "' and kqdate<='" + eddate + "'";
		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sql);
	}

	@ACOAction(eventname = "testQrymonthdetail", Authentication = false, notes = "考勤月度明细测试")
	public String testQrymonthdetail() throws Exception {
		Hr_employee emp = new Hr_employee();// 人事实体
		emp.findByID("2733");
		return emp.tojson();
	}

	@ACOAction(eventname = "hr_monthdetail_new", Authentication = false, notes = "考勤月度明细")
	public String hr_monthdetail_new() throws Exception {
		// 定义变量
		String er_id, zjbss;
		float sjcq, gj, hj, snj, gsj, fdjq;

		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");
		String bgdate = CorUtil.hashMap2Str(urlparms, "bgdate", "需要参数bgdate");
		String eddate = CorUtil.hashMap2Str(urlparms, "eddate", "需要参数eddate");
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		// JSONObject jo=new JSONObject();
		// jo.put("employee_code",employee_code);

		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		er_id = emp.er_id.getValue();
		// System.out.print(employee_code);
		// 计算实际出勤
		sqlstr = "SELECT ";
		sqlstr = sqlstr + "  IFNULL(SUM(CASE  WHEN r.lrst NOT IN(9,10,12,0,6) THEN sl.dayratio ELSE 0 END )/100,0) sjcq ,";
		sqlstr = sqlstr + "  IFNULL(SUM(CASE  WHEN r.lrst IN (9, 10, 12) THEN sl.dayratio ELSE 0 END )/100,0) kg ,";
		sqlstr = sqlstr + "  IFNULL(SUM(CASE  WHEN r.lrst =2 THEN 1 ELSE 0 END ),0) cd ,";
		sqlstr = sqlstr + "  IFNULL(SUM(CASE  WHEN r.lrst =3 THEN 1 ELSE 0 END ),0) zt ";
		sqlstr = sqlstr + " FROM  hrkq_bckqrst r,hrkq_sched_line sl  ";
		sqlstr = sqlstr + " WHERE r.sclid=sl.sclid ";
		sqlstr = sqlstr + " and r.kqdate>='" + bgdate + "' and r.kqdate<='" + eddate;
		sqlstr = sqlstr + "' and r.er_id  ='" + er_id + "'";
		List<HashMap<String, String>> lm = (new Hrkq_bckqrst()).pool.openSql2List(sqlstr);
		HashMap<String, String> m = lm.get(0);
		sjcq = Float.valueOf(m.get("sjcq"));
		sjcq = sjcq + sjcq % (0.5f);

		String ym = Systemdate.getStrDateByFmt(Systemdate.getDateByStr(bgdate), "yyyy-MM");
		sqlstr = "SELECT  "
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =2 or h.viodeal=1 THEN l.lhdaystrue ELSE 0 END ),0) sj," // 事假或违规为事假
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =3 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) gj,"// 且没违规
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =4 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) hj, "
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =5 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) cj,"
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =7 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) snj,"
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =8 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) bj,"
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =9 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) gsj"
				+ " FROM  hrkq_holidayapp h,hrkq_holidayapp_month l,hrkq_holidaytype t "
				+ " WHERE h.stat = 9 AND h.htid=t.htid "
				+ " AND h.haid=l.haid AND l.yearmonth='" + ym + "' AND er_id='" + er_id + "'";

		List<HashMap<String, String>> lm0 = (new Hrkq_bckqrst()).pool.openSql2List(sqlstr);
		System.out.println(lm0.size());
		gj = Float.valueOf(lm0.get(0).get("gj"));
		hj = Float.valueOf(lm0.get(0).get("hj"));
		snj = Float.valueOf(lm0.get(0).get("snj"));
		gsj = Float.valueOf(lm0.get(0).get("gsj"));
		// fdjq=Float.valueOf(lm0.get(0).get("gsj"));
		System.out.println(gj);
		sqlstr = "SELECT IFNULL(COUNT(*),0)  ct  FROM hrkq_ohyear WHERE ohdate>='" + bgdate + "' AND ohdate<='"
				+ eddate + "' AND iswork=2";

		fdjq = Float.valueOf(emp.pool.openSql2List(sqlstr).get(0).get("ct"));
		fdjq = fdjq + fdjq % (0.5f);

		sjcq = (float) (sjcq + gj + hj + snj + gsj + fdjq);
		// 计算加班时数
		sqlstr = "SELECT  IFNULL(SUM(CASE  WHEN (over_type = 1 AND otltype IN(1,2))  THEN othours ELSE 0 END ),0) prjbss,"
				+ " IFNULL(SUM(CASE  WHEN (over_type = 2 AND otltype IN(1,2))  THEN othours ELSE 0 END ),0) zmjbss,"
				+ " IFNULL(SUM(CASE  WHEN (over_type = 3 AND otltype IN(1,2))  THEN othours ELSE 0 END ),0) fdjbss,"
				+ " IFNULL(SUM(CASE  WHEN otltype IN(3,4,5) THEN othours ELSE 0 END ),0) zlbjbss,"
				+ " IFNULL(SUM(othours),0) zjbss "
				+ " FROM hrkq_overtime_list  WHERE ((bgtime >='"
				+ bgdate + "' AND bgtime<='" + eddate + "') OR(edtime >='" + bgdate
				+ "' AND edtime<='" + eddate + "')) and dealtype=2 AND er_id ='" + er_id + "'";// 只显示计算加班费的

		List<HashMap<String, String>> lm1 = (new Hrkq_overtime()).pool.openSql2List(sqlstr);
		HashMap<String, String> m1 = lm1.get(0);
		System.out.println(lm1.size());
		zjbss = m1.get("zjbss");
		// 返回工号、姓名、实际出勤、加班时数
		sqlstr = "select employee_code,employee_name,'" + sjcq + "','" + zjbss + "' from hr_employee where employee_code='" + employee_code + "'";

		JSONObject jo = new JSONObject();
		jo.put("employee_code", employee_code);
		jo.put("sjcq", sjcq);
		jo.put("zjbss", zjbss);
		return jo.toString();
		// return emp.pool.opensql2json(sqlstr);
		// return new CReport(sqlstr, null).findReport();
		// return DBPools.defaultPool().openrowsql2json(sqlstr);

	}

	// 正式地址http://192.168.117.132:8001/dlhr/hr/published1/hr_chgagent_new.co
	@ACOAction(eventname = "hr_chgagent_new", Authentication = false, notes = "修改流程代理人")
	public String hr_chgagent_new() throws Exception {
		// 定义变量
		String employee_code = null, employee_id = null;
		String agent_code = null, agent_name, agent_id, starttime, endtime, OperateType, Strerror;

		JSONObject rtnjsobject = new JSONObject();

		// Date starttime,endtime;
		// int OperateType;

		// 从前端JSON获取数据

		JSONObject jsonObject = JSONObject.fromObject(CSContext.getPostdata());// 从前端JSON获取数据

		// 变量赋值
		employee_code = jsonObject.getString("SourceCode");// 被代理人工号
		agent_code = jsonObject.getString("AgentCode");// 代理人工号
		starttime = jsonObject.getString("StartTime");// 代理开始时间
		endtime = jsonObject.getString("EndTime");// 代理结束时间
		OperateType = jsonObject.getString("OperateType");// 操作类型：1新增 3作废

		// 控制传入的工号不能为空==null不可少加上以下三者之一isempty();equals("")；length()
		if (employee_code == null || employee_code.equals("")) {
			new Exception("被代理人字段【employee_code】不能为空");
			Strerror = "被代理人字段【employee_code】不能为空";
			rtnjsobject.put("err", Strerror);
			return rtnjsobject.toString();
			// return "{\"err\":\"被代理人字段【employee_code】不能为空\"}";
		}

		if (agent_code == null || agent_code.length() == 0) {
			new Exception("代理人字段【agent_code】不能为空");
			Strerror = "代理人字段【agent_code】不能为空";
			rtnjsobject.put("err", Strerror);
			return rtnjsobject.toString();
		}

		// 通过传入的工号进一步查询用户表中的用户ID和名称，并给变量赋值
		Shwuser user = new Shwuser();// 创建用户实体,作为获取数据库连接的对象

		String sqlstr = "select * from shwuser where username='" + agent_code + "'";
		user.findBySQL(sqlstr);// 查询代理人
		if (user.isEmpty()) {
			new Exception("ID为【" + user.username.getValue() + "】的代理人用户不存在");

			Strerror = "ID为【" + user.username.getValue() + "】的代理人用户不存在";
			rtnjsobject.put("err", Strerror);
			return rtnjsobject.toString();
		}

		agent_id = user.userid.getValue();
		agent_name = user.displayname.getValue();

		sqlstr = "select * from shwuser where username='" + employee_code + "'";
		user.findBySQL(sqlstr);// 查询被代理人
		if (user.isEmpty()) {
			new Exception("ID为【" + user.username.getValue() + "】的被代理人用户不存在");
			Strerror = "ID为【" + user.username.getValue() + "】的被代理人用户不存在";
			rtnjsobject.put("err", Strerror);
			return rtnjsobject.toString();
		}
		employee_id = user.userid.getValue();

		String sqlqry = " SELECT DISTINCT shu.userid,sht.wftempid,sht.wftempname";
		sqlqry = sqlqry + " FROM shwuser shu";// 用户表
		sqlqry = sqlqry + " INNER JOIN shwpositionuser shpu ON shpu.userid=shu.userid";// 岗位中包含的用户
		sqlqry = sqlqry + " INNER JOIN shwposition shp ON shpu.positionid=shp.positionid";// 流程岗位中定义的岗位
		sqlqry = sqlqry + " INNER JOIN shwwftempprocuser shwu ON shwu.displayname=shp.positiondesc";// 审批流程节点中定义的岗位
		sqlqry = sqlqry + " INNER JOIN shwwftemp sht ON sht.wftempid=shwu.wftempid";// 审批流程模板(stat=1为生效的)
		sqlqry = sqlqry + " where sht.stat=1 AND shu.username='" + employee_code + "'";
		// Shwuser_wf_agent useragent = new shwuser_wf_agent();// 创建流程模板实体
		// String sqlstr = "select * from shwuser_wf_agent where userid='" + employee_id + "'";
		// useragent.findBySQL(sqlstr);// 查询流程模板代理人

		String sqlins = "", sqldel = "", sqlupdate = "", sqlupdatea = "";

		// 新增流程代理
		if (OperateType.equals("1")) {
			// 删除旧流程代理
			sqldel = "delete from  shwuser_wf_agent where userid='" + employee_id + "'";
			// 根据审批流程模板及节点、流程岗位、用户增加新流程代理

			sqlins = "INSERT INTO shwuser_wf_agent(wfagentid,userid,wftempid,wftempname,auserid,ausername,adisplayname)";
			sqlins = sqlins + " SELECT DISTINCT (SELECT MAX(wfagentid)+1 FROM  shwuser_wf_agent) wfagentid,shu.userid,sht.wftempid,sht.wftempname,'" + agent_id + "','" + agent_code + "','" + agent_name + "'";
			sqlins = sqlins + " FROM shwuser shu";// 用户表
			sqlins = sqlins + " INNER JOIN shwpositionuser shpu ON shpu.userid=shu.userid";// 岗位中包含的用户
			sqlins = sqlins + " INNER JOIN shwposition shp ON shpu.positionid=shp.positionid";// 流程岗位中定义的岗位
			sqlins = sqlins + " INNER JOIN shwwftempprocuser shwu ON shwu.displayname=shp.positiondesc";// 审批流程节点中定义的岗位
			sqlins = sqlins + " INNER JOIN shwwftemp sht ON sht.wftempid=shwu.wftempid";// 审批流程模板(stat=1为生效的)
			sqlins = sqlins + " where sht.stat=1 AND shu.username='" + employee_code + "' ORDER BY sht.wftempid";
			// 更新代理模板中的代理人
			sqlupdatea = "UPDATE shwuser_wf_agent SET auserid='" + agent_id + "',ausername='" + agent_code + "',adisplayname='" + agent_name + "' WHERE userid='" + employee_id + "'";
			// 修改是否代理，代理时间
			sqlupdate = "update shwuser  set goout=2,gooutstarttime='" + starttime + "',gooutendtime='" + endtime + "' WHERE username='" + employee_code + "'";

		}
		;

		// 删除流程代理
		if (OperateType.equals("3")) {
			sqldel = "delete from  shwuser_wf_agent where userid='" + employee_id + "'";
			sqlins = "";
			sqlupdate = "update shwuser  set goout=1,gooutstarttime='" + starttime + "',gooutendtime='" + endtime + "' WHERE username='" + employee_code +

					"'";
		}

		CDBConnection con = user.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			con.execsql(sqlupdatea);
			con.execsql(sqlupdate);
			con.submit();// 提交数据库事务

			con.close();// 关闭通道
			return user.tojson();
		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}
	}
	//新增4个查询服务2019.4.25

	@ACOAction(eventname = "hr_qryempdd", Authentication = false, notes = "人员异动:入职，调动")
	public String hr_qryempdd() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");

		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		String sql = "SELECT "
				+ " v.employee_code,v.employee_name,v.Ym,v.cdate,v.TYPE,v.orgname,v.sp_name,v.lv_num,v.ifpt,v.note"
				+ " FROM"
				+ " (SELECT  IFNULL(ht.employee_code,hr.employee_code) employee_code,IFNULL(ht.employee_name,hr.employee_name)" + " employee_name,"
				+ " DATE_FORMAT(IFNULL(ht.hiredday,hr.hiredday),'%Y-%m') Ym,"
				+ " IFNULL(ht.hiredday,hr.hiredday) cdate,'入职'  TYPE,"
				+ " IFNULL(ht.odorgname,hr.orgname) orgname,IFNULL(ht.odsp_name,hr.sp_name) sp_name,IFNULL(ht.odlv_num,hr.lv_num) lv_num,"
				+ " CASE WHEN IFNULL(pt.ptjacode,'')='' THEN '无' ELSE '有' END ifpt,"
				+ " IFNULL(ht.tranfreason,'') note"
				+ " FROM hr_employee hr"
				+ " LEFT JOIN hr_employee_transfer ht ON hr.employee_code=ht.employee_code"
				+ " LEFT JOIN hr_empptjob_app pt ON hr.employee_code=pt.employee_code"
				+ " WHERE ht.employee_code='" + employee_code + "'" + " ORDER BY ht.tranfcmpdate"
				+ " LIMIT 0,1) v"

				+ " UNION"
				+ " SELECT  ht.employee_code,ht.employee_name,DATE_FORMAT(ht.tranfcmpdate,'%Y-%m') Ym,ht.tranfcmpdate cdate,"
				+ " CASE WHEN ht.tranftype1=1 THEN '晋升' WHEN ht.tranftype1=2 THEN '降职'  ELSE '平级'  END AS TYPE,"
				+ " ht.neworgname orgname,ht.newsp_name sp_name,ht.newlv_num lv_num,CASE WHEN IFNULL(pt.ptjacode,'')='' THEN '无' " + " ELSE '有' END ifpt,"
				+ " ht.tranfreason note"
				+ " FROM hr_employee_transfer ht "
				+ " LEFT JOIN hr_empptjob_app pt ON ht.employee_code=pt.employee_code"
				+ " WHERE ht.employee_code='" + employee_code + "'"
				+ " ORDER BY cdate";

		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sql);

	}

	@ACOAction(eventname = "hr_qryempjl", Authentication = false, notes = "正激历")
	public String hr_qryempjl() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");

		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		String sql = "SELECT "
				+ " hr.`rewardtime`,hr.`rwnote`,hr.`rwamount`,hr.`rwfile`,hr.rwnature,hr.* FROM hr_employee_reward hr"
				+ " INNER JOIN hr_employee he ON hr.`er_id`=he.`er_id`"
				+ " WHERE hr.rwnature=1 AND"
				+ " he.employee_code='" + employee_code + "'"
				+ " ORDER BY hr.rewardtime";
		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sql);

	}

	@ACOAction(eventname = "hr_qryempcf", Authentication = false, notes = "负激历")
	public String hr_qryempcf() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");

		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr = "select * from hr_employee where employee_code='" + employee_code + "'";
		emp.findBySQL(sqlstr);// 查询人事档案
		if (emp.isEmpty())
			new Exception("ID为【" + employee_code + "】的人事资料不存在");
		String sql = "SELECT "
				+ " hr.`rewardtime`,hr.`rwnote`,hr.`rwamount`,hr.`rwfile`,hr.rwnature,hr.* FROM hr_employee_reward hr"
				+ " INNER JOIN hr_employee he ON hr.`er_id`=he.`er_id`"
				+ " WHERE hr.rwnature=2 AND"
				+ " he.employee_code='" + employee_code + "'"
				+ " ORDER BY hr.rewardtime";

		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sql);
	}


	@ACOAction(eventname = "hr_qryemptrans", Authentication = false, notes = "教育经历,厂外培训，厂内培训")
	public String hr_qryemptrans() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "employee_code", "需要参数employee_code");
		String sql = "SELECT "
				+ "  hl.begintime,hl.schoolname,hl.major,hl.degree certname,hl.remark,hl.endtime,hl.certnum,hl.gcernum ,1 type FROM hr_employee_leanexp hl "
				+ " INNER JOIN hr_employee he ON hl.er_id=he.er_id WHERE he.`employee_code`='" + employee_code + "'"
				+ " UNION "
				+ " SELECT ht.begintime,ht.schoolname,ht.major,ht.certname,ht.remark,ht.endtime,ht.certnum,ht.certname gcernum,2 type  FROM hr_employee_trainexp ht "
				+ " INNER JOIN hr_employee he ON ht.er_id=he.er_id WHERE he.`employee_code`='" + employee_code + "'"
				+ " UNION "
				+ " SELECT htw.begintime,htw.schoolname,htw.twktitle major,htw.certname,htw.remark,htw.endtime,htw.certnum,htw.certname gcernum,3 type FROM hr_employee_trainwk htw "
				+ " INNER JOIN hr_employee he ON htw.er_id=he.er_id WHERE he.`employee_code`='" + employee_code + "'"
				+ " ORDER BY begintime ";

		//CDBPool pool = DBPools.poolByName("dlhrmycatread");
		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sql);
	}


	@ACOAction(eventname = "findleavlblctowx", Authentication = false, notes = "浏览可休假列表供考勤通查询用")
	public String findleavlblctowx() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "code", "需要参数code");
		//String[] notnull = {};
		//JSONArray dws=new JSONArray();
		String sqlstr = "select * from (SELECT employee_code,alllbtime,usedlbtime,valdate"+
				" FROM( SELECT b.*, IF(b.valdate>curdate(),2,1) isexpire,IF(b.usedlbtime<b.alllbtime,2,1) usup,IF((b.valdate>curdate()) AND (b.usedlbtime<b.alllbtime),1,2) canuses "+
				" FROM hrkq_leave_blance b,hr_employee e where e.er_id=b.er_id ) tb where canuses=1 )"+ 
				" tb where 1=1  and `employee_code` = '"+employee_code+"' order by  valdate desc;";
		CDBPool pool = HRUtil.getReadPool();
		return pool.opensql2json(sqlstr);

	}
	@ACOAction(eventname = "hr_get_dict", Authentication = false, notes = "字典项")
	public String hr_get_dict() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String searchTime=urlparms.get("searchtime");
		String sqlstr="";
		if(searchTime==null ||searchTime.isEmpty()){
			sqlstr="select dictcode,dictname,pid,dictvalue,createtime,updatetime from shwdict where  usable=1";
		}else{
			sqlstr="select dictcode,dictname,pid,dictvalue,createtime,updatetime from shwdict where  usable=1 and  (createtime>='"+searchTime+"' or updatetime>='"+searchTime+"')";
		}

		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}
	@ACOAction(eventname = "hr_get_orgposition", Authentication = false, notes = "机构职位表")
	public String hr_get_orgposition() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String searchTime=urlparms.get("searchtime");
		String sqlstr="";
		if(searchTime==null){
			sqlstr="SELECT orgid,orgcode,orgname,hwc_namezl,lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name,iskey,hwc_namezq,hwc_namezz,createtime,updatetime FROM hr_orgposition where usable=1";	
		}else{
			sqlstr="SELECT orgid,orgcode,orgname,hwc_namezl,lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name,iskey,hwc_namezq,hwc_namezz,createtime,updatetime FROM hr_orgposition where usable=1 and  (createtime>='"+searchTime+"' or updatetime>='"+searchTime+"')";

		}
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}

	@ACOAction(eventname = "findEmployeeByCode", Authentication = false, notes = "根据工号获取员工档案")
	public String findEmployeeByCode() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String employee_code = CorUtil.hashMap2Str(urlparms, "code", "需要参数code");
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		String sqlstr="select * from hr_employee where employee_code='"+employee_code+"'";
		emp.findBySQL(sqlstr,false);// 查询人事档案
		if (emp.isEmpty())
			new Exception("工号为【" + emp.employee_code.getValue() + "】的人事资料不存在");
		return emp.tojson();
	}


	@ACOAction(eventname = "hr_add_employee", Authentication = false, notes = "增加人员档案")
	public String hr_add_employee() throws Exception {
		Hr_employee emp = new Hr_employee();// 创建人事档案实体
		emp.fromjson(CSContext.getPostdata());// 从前端JSON获取数据
		Hr_orgposition osp = new Hr_orgposition();
		DictionaryTemp dictemp = new DictionaryTemp();// 数据字典缓存
		if (emp.employee_code.isEmpty()) {
			new Exception("入职档案字段【employee_code】不能为空");
		}

		if (emp.employee_name.isEmpty()) {
			new Exception("入职档案字段【employee_name】不能为空");
		}
		if (emp.degree.isEmpty()) {
			new Exception("入职档案字段【degree】不能为空");
		}
		if (emp.degreetype.isEmpty()) {
			new Exception("入职档案字段【degreetype】不能为空");
		}
		if (emp.degreecheck.isEmpty()) {
			new Exception("入职档案字段【degreecheck】不能为空");
		}
		if (emp.empstatid.isEmpty()) {
			new Exception("入职档案字段【empstatid】不能为空");
		}
		if (emp.sex.isEmpty()) {
			new Exception("入职档案字段【sex】不能为空");
		}
		if (emp.birthday.isEmpty()) {
			new Exception("入职档案字段【birthday】不能为空");
		}
		if (emp.id_number.isEmpty()) {
			new Exception("入职档案字段【id_number】不能为空");
		}
		if (emp.registeraddress.isEmpty()) {
			new Exception("入职档案字段【registeraddress】不能为空");
		}
		if (emp.sign_org.isEmpty()) {
			new Exception("入职档案字段【sign_org】不能为空");
		}
		if (emp.sign_date.isEmpty()) {
			new Exception("入职档案字段【sign_date】不能为空");
		}
		if (emp.expired_date.isEmpty()) {
			new Exception("入职档案字段【expired_date】不能为空");
		}
		if (emp.ospcode.isEmpty()) {
			new Exception("入职档案字段【ospcode】不能为空");
		}
		if (emp.entrysourcr.isEmpty()) {
			new Exception("入职档案字段【entrysourcr】不能为空");
		}
		if (emp.dispunit.isEmpty()) {
			new Exception("入职档案字段【dispunit】不能为空");
		}
		if (emp.dispeextime.isEmpty()) {
			new Exception("入职档案字段【dispeextime】不能为空");
		}
		if (emp.transorg.isEmpty()) {
			new Exception("入职档案字段【transorg】不能为空");
		}
		if (emp.transextime.isEmpty()) {
			new Exception("入职档案字段【transextime】不能为空");
		}
		if (emp.advisercode.isEmpty()) {
			new Exception("入职档案字段【advisercode】不能为空");
		}
		if (emp.advisername.isEmpty()) {
			new Exception("入职档案字段【advisername】不能为空");
		}
		if (emp.adviserphone.isEmpty()) {
			new Exception("入职档案字段【adviserphone】不能为空");
		}
		if (emp.juridical.isEmpty()) {
			new Exception("入职档案字段【juridical】不能为空");
		}
		if (emp.atdtype.isEmpty()) {
			new Exception("入职档案字段【atdtype】不能为空");
		}
		if (emp.noclock.isEmpty()) {
			new Exception("入职档案字段【noclock】不能为空");
		}
		if (emp.married.isEmpty()) {
			new Exception("入职档案字段【married】不能为空");
		}
		if (emp.registertype.isEmpty()) {
			new Exception("入职档案字段【registertype】不能为空");
		}
		if(emp.ospcode.isEmpty()){
			new Exception("入职档案字段【ospcode】不能为空");
		}
		String sqlstr = "select * from hr_employee where employee_code='" + emp.employee_code + "'";
		Hr_employee findEmp=new Hr_employee();
		findEmp.findBySQL(sqlstr);// 查询人事档案
		if (!findEmp.isEmpty())
			new Exception("工号为【" + emp.employee_code.getValue() + "】的人事资料已存在");
		sqlstr = "SELECT * FROM hr_orgposition WHERE ospcode='" + emp.ospcode.getValue() + "' ";
		osp.clear();
		osp.findBySQL(sqlstr, false);
		if (osp.isEmpty())
			throw new Exception("工号【" +  emp.employee_code.getValue()  + "】的机构职位编码【" + emp.ospcode.getValue() + "】不存在");
		if ("正式".equals(emp.empstatid.getValue()))
			emp.empstatid.setValue("4");
		else
			throw new Exception("【" + emp.employee_code.getValue() + "】只能导入人事状态为【正式、离职】的人事资料");
		emp.pldcp.setValue(dictemp.getVbCE("495", emp.pldcp.getValue(), true,"工号【" + emp.employee_code.getValue() + "】政治面貌【" + emp.pldcp.getValue() + "】不存在"));
		emp.bloodtype.setValue(dictemp.getVbCE("697", emp.bloodtype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】血型【" + emp.bloodtype.getValue() + "】不存在"));
		emp.nation.setValue(dictemp.getVbCE("797", emp.nation.getValue(), false,"工号【" + emp.employee_code.getValue() + "】民族【" + emp.nation.getValue() + "】不存在"));
		emp.married.setValue(dictemp.getVbCE("714", emp.married.getValue(), false,"工号【" + emp.employee_code.getValue() + "】婚姻状况【" + emp.married.getValue() + "】不存在"));
		emp.registertype.setValue(dictemp.getVbCE("702", emp.registertype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】户籍类型【" + emp.registertype.getValue() + "】不存在"));
		emp.entrysourcr.setValue(dictemp.getVbCE("741", emp.entrysourcr.getValue(), true,"工号【" + emp.employee_code.getValue() + "】人员来源【" + emp.entrysourcr.getValue() + "】不存在"));
		emp.idtype.setValue(dictemp.getVbCE("1469", emp.idtype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】证件类型【" + emp.idtype.getValue() + "】不存在"));
		//emp.entrysourcr.setValue(dictemp.getValueByCation("741", v.get("entrysourcr")));//
		emp.dispunit.setValue(dictemp.getVbCE("1322", emp.dispunit.getValue(), false,"工号【" + emp.employee_code.getValue() + "】的派遣机构【" + emp.dispunit.getValue() + "】不存在"));
		emp.sex.setValue(dictemp.getVbCE("81", emp.sex.getValue(), false,"工号【" + emp.employee_code.getValue() + "】性别【" + emp.sex.getValue() + "】不存在"));
		emp.degree.setValue(dictemp.getVbCE("84", emp.degree.getValue(), false,"工号【" + emp.employee_code.getValue() + "】学历【" + emp.degree.getValue() + "】不存在"));
		emp.degreetype.setValue(dictemp.getVbCE("1495", emp.degreetype.getValue(), false,"工号【" + emp.employee_code.getValue() + "】学历类型【" + emp.degreetype.getValue() + "】不存在"));
		emp.degreecheck.setValue(dictemp.getVbCE("1507", emp.degreecheck.getValue(), false,"工号【" + emp.employee_code.getValue() + "】学历验证【" + emp.degreecheck.getValue() + "】不存在"));
		emp.eovertype.setValue(dictemp.getVbCE("1394", emp.eovertype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】加班类别【" + emp.eovertype.getValue() + "】不存在"));
		emp.atdtype.setValue(dictemp.getVbCE("1399", emp.atdtype.getValue(), false,"工号【" + emp.employee_code.getValue() + "】出勤类别【" + emp.atdtype.getValue() + "】不存在"));
		emp.noclock.setValue(dictemp.getVbCE("5", emp.noclock.getValue(), false,"工号【" + emp.employee_code.getValue() + "】免打考勤卡【" + emp.noclock.getValue() + "】不存在"));
		emp.needdrom.setValue(dictemp.getVbCE("5", emp.needdrom.getValue(), false,"工号【" + emp.employee_code.getValue() + "】入住宿舍【" + emp.needdrom.getValue() + "】不存在"));
		emp.pay_way.setValue(dictemp.getVbCE("901", emp.pay_way.getValue(), true,"工号【" + emp.employee_code.getValue() + "】计薪方式【" + emp.pay_way.getValue() + "】不存在"));
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
		emp.ospcode.setValue(osp.ospcode.getValue()); // 职位编码
		emp.sp_name.setValue(osp.sp_name.getValue()); // 职位名称
		emp.iskey.setValue(osp.iskey.getValue()); // 关键岗位
		emp.hwc_namezq.setValue(osp.hwc_namezq.getValue()); // 职群
		emp.hwc_namezz.setValue(osp.hwc_namezz.getValue()); // 职种
		emp.idpath.setValue(osp.idpath.getValue());
		if(osp.isoffjob.getValue().equals("1")){
			emp.emnature.setValue("脱产");
		}else {
			emp.emnature.setValue("非脱产");
		}
		if (emp.kqdate_start.isEmpty() && (!emp.hiredday.isEmpty())) {
			emp.kqdate_start.setAsDatetime(emp.hiredday.getAsDatetime());
		}
		if (emp.kqdate_end.isEmpty() && (!emp.ljdate.isEmpty())) {
			emp.kqdate_end.setAsDatetime(emp.ljdate.getAsDatetime());
		}
		int age = Integer.valueOf(HrkqUtil.getParmValue("EMPLOYEE_AGE_LMITE"));// 年龄小于*岁不允许入职
		// 0
		// 不控制
		System.out.println("age:" + age);
		if (emp.idtype.getAsIntDefault(1) == 1) {
			int l = emp.id_number.getValue().length();
			if ((l != 18) && (l != 20))
				throw new Exception("身份证必须为18或20位");
		}
		if (age > 0) {
			float ag = HRUtil.getAgeByBirth(emp.birthday.getAsDatetime());
			System.out.println("ag:" + ag);
			if (ag < age) {
				throw new Exception("年龄小于【" + age + "】周岁");
			}
		}
		if (!"M类".equalsIgnoreCase(emp.hwc_namezl.getValue())) {
			emp.mlev.setValue(null);
		}
		if (!emp.employee_code.isEmpty() && (emp.employee_code.getValue().length() != 6)) {
			throw new Exception("工号必须为6位");
		}
		CDBConnection con = emp.pool.getCon(this); // 获取数据库连接
		con.startTrans(); // 开始事务
		try {
			emp.save(con);
			con.submit();// 提交数据库事务

			if(!emp.er_id.isEmpty()){
				return "Y";
			}else{
				return "N";
			}

		} catch (Exception e) {
			con.rollback();// 回滚事务
			throw e;
		} finally {
			con.close();
		}

	}

	@ACOAction(eventname = "hr_add_entry", Authentication = false, notes = "增加职工")
	public String hr_add_entry() throws Exception {
		Hr_employee_linked emp = new Hr_employee_linked();
		Hr_orgposition osp = new Hr_orgposition();
		DictionaryTemp dictemp = new DictionaryTemp();// 数据字典缓存
		Hr_entry enty = new Hr_entry();
		String psd = CSContext.getPostdata();
		JSONObject oj = JSONObject.fromObject(psd);
		Object oerid = oj.get("er_id");
		String[] disFlds = null;
		if ((oerid != null) && (!oerid.toString().isEmpty())) {
			emp.findByID(oerid.toString());
			if (emp.isEmpty()) {
				throw new Exception("没有找到ID为【" + oerid.toString() + "】的员工档案 ");
			}
			disFlds = new String[] { "idpath", "creator", "createtime", "updator", "updatetime" };
		} else {
			disFlds = new String[] {};
		}
		String[] flds = emp.getFieldNames(disFlds);
		emp.fromjson(psd, flds);
		enty.fromjson(psd);
		Logsw.dblog("政治面貌【" + emp.pldcp.getValue() + "】");
		emp.pldcp.setValue(dictemp.getVbCE("495", emp.pldcp.getValue(), true,"工号【" + emp.employee_code.getValue() + "】政治面貌【" + emp.pldcp.getValue() + "】不存在"));
		enty.entrytype.setValue(dictemp.getVbCE("693", enty.entrytype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】入职类型【" + enty.entrytype.getValue() + "】不存在"));
		emp.bloodtype.setValue(dictemp.getVbCE("697", emp.bloodtype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】血型【" + emp.bloodtype.getValue() + "】不存在"));
		emp.nation.setValue(dictemp.getVbCE("797", emp.nation.getValue(), true,"工号【" + emp.employee_code.getValue() + "】民族【" + emp.nation.getValue() + "】不存在"));
		emp.married.setValue(dictemp.getVbCE("714", emp.married.getValue(), true,"工号【" + emp.employee_code.getValue() + "】婚姻状况【" + emp.married.getValue() + "】不存在"));
		emp.registertype.setValue(dictemp.getVbCE("702", emp.registertype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】户籍类型【" + emp.registertype.getValue() + "】不存在"));
		emp.entrysourcr.setValue(dictemp.getVbCE("741", emp.entrysourcr.getValue(), true,"工号【" + emp.employee_code.getValue() + "】人员来源【" + emp.entrysourcr.getValue() + "】不存在"));
		emp.idtype.setValue(dictemp.getVbCE("1469", emp.idtype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】证件类型【" + emp.idtype.getValue() + "】不存在"));
		emp.dispunit.setValue(dictemp.getVbCE("1322", emp.dispunit.getValue(), true,"工号【" + emp.employee_code.getValue() + "】的派遣机构【" + emp.dispunit.getValue() + "】不存在"));
		emp.sex.setValue(dictemp.getVbCE("81", emp.sex.getValue(), true,"工号【" + emp.employee_code.getValue() + "】性别【" + emp.sex.getValue() + "】不存在"));
		emp.degree.setValue(dictemp.getVbCE("84", emp.degree.getValue(), true,"工号【" + emp.employee_code.getValue() + "】学历【" + emp.degree.getValue() + "】不存在"));
		emp.degreetype.setValue(dictemp.getVbCE("1495", emp.degreetype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】学历类型【" + emp.degreetype.getValue() + "】不存在"));
		emp.degreecheck.setValue(dictemp.getVbCE("1507", emp.degreecheck.getValue(), true,"工号【" + emp.employee_code.getValue() + "】学历验证【" + emp.degreecheck.getValue() + "】不存在"));
		emp.eovertype.setValue(dictemp.getVbCE("1394", emp.eovertype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】加班类别【" + emp.eovertype.getValue() + "】不存在"));
		emp.atdtype.setValue(dictemp.getVbCE("1399", emp.atdtype.getValue(), true,"工号【" + emp.employee_code.getValue() + "】出勤类别【" + emp.atdtype.getValue() + "】不存在"));
		emp.noclock.setValue(dictemp.getVbCE("5", emp.noclock.getValue(), true,"工号【" + emp.employee_code.getValue() + "】免打考勤卡【" + emp.noclock.getValue() + "】不存在"));
		emp.needdrom.setValue(dictemp.getVbCE("5", emp.needdrom.getValue(), true,"工号【" + emp.employee_code.getValue() + "】入住宿舍【" + emp.needdrom.getValue() + "】不存在"));
		emp.pay_way.setValue(dictemp.getVbCE("901", emp.pay_way.getValue(), true,"工号【" + emp.employee_code.getValue() + "】计薪方式【" + emp.pay_way.getValue() + "】不存在"));
		emp.empstatid.setAsInt(6);// 待入职
		String sqlstr = "SELECT IFNULL(COUNT(*),0) ct FROM `hr_balcklist` where id_number='" + emp.id_number.getValue()
		+ "'";
		if (Integer.valueOf(emp.pool.openSql2List(sqlstr).get(0).get("ct")) != 0)
			throw new Exception("身份证号码【" + emp.id_number.getValue() + "】在黑名单");
		String sqlstr2 = "select * from hr_employee where employee_code='" + emp.employee_code + "'";
		Hr_employee findEmp=new Hr_employee();
		findEmp.findBySQL(sqlstr2);// 查询人事档案
		if (!findEmp.isEmpty())
			new Exception("工号为【" + emp.employee_code.getValue() + "】的人事资料已存在");
		sqlstr2 = "SELECT * FROM hr_orgposition WHERE ospcode='" + emp.ospcode.getValue() + "' ";
		osp.clear();
		osp.findBySQL(sqlstr2, false);
		if (osp.isEmpty())
			throw new Exception("工号【" +  emp.employee_code.getValue()  + "】的机构职位编码【" + emp.ospcode.getValue() + "】不存在");
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
		emp.ospcode.setValue(osp.ospcode.getValue()); // 职位编码
		emp.sp_name.setValue(osp.sp_name.getValue()); // 职位名称
		emp.iskey.setValue(osp.iskey.getValue()); // 关键岗位
		emp.hwc_namezq.setValue(osp.hwc_namezq.getValue()); // 职群
		emp.hwc_namezz.setValue(osp.hwc_namezz.getValue()); // 职种
		emp.idpath.setValue(osp.idpath.getValue());
		enty.idpath.setValue(osp.idpath.getValue());
		if(osp.isoffjob.getValue().equals("1")){
			emp.emnature.setValue("脱产");
		}else {
			emp.emnature.setValue("非脱产");
		}
		if (emp.kqdate_start.isEmpty() && (!emp.hiredday.isEmpty())) {
			emp.kqdate_start.setAsDatetime(emp.hiredday.getAsDatetime());
		}
		if (emp.kqdate_end.isEmpty() && (!emp.ljdate.isEmpty())) {
			emp.kqdate_end.setAsDatetime(emp.ljdate.getAsDatetime());
		}
		int age = Integer.valueOf(HrkqUtil.getParmValue("EMPLOYEE_AGE_LMITE"));// 年龄小于*岁不允许入职
		// 0
		// 不控制
		System.out.println("age:" + age);
		if (emp.idtype.getAsIntDefault(1) == 1) {
			int l = emp.id_number.getValue().length();
			if ((l != 18) && (l != 20))
				throw new Exception("身份证必须为18或20位");
		}
		if (age > 0) {
			float ag = HRUtil.getAgeByBirth(emp.birthday.getAsDatetime());
			System.out.println("ag:" + ag);
			if (ag < age) {
				throw new Exception("年龄小于【" + age + "】周岁");
			}
		}
		if (!"M类".equalsIgnoreCase(emp.hwc_namezl.getValue())) {
			emp.mlev.setValue(null);
		}
		if (!emp.employee_code.isEmpty() && (emp.employee_code.getValue().length() != 6)) {
			throw new Exception("工号必须为6位");
		}

		int hrlev = HRUtil.getOrgHrLev(osp.orgid.getValue());

		enty.orghrlev.setValue(hrlev);
		emp.note.setValue(enty.remark.getValue());
		CDBConnection con = emp.pool.getCon(this);
		con.startTrans();
		try {
			emp.save(con);
			enty.er_id.setValue(emp.er_id.getValue());
			enty.entrysourcr.setValue(emp.entrysourcr.getValue());
			enty.employee_code.setValue(emp.employee_code.getValue());
			enty.er_id.setValue(emp.er_id.getValue());
			enty.orgid.setValue(emp.orgid.getValue());
			enty.lv_num.setAsFloat(emp.lv_num.getAsFloat());
			enty.ospid.setValue(osp.ospid.getValue());
			enty.save(con, false);
			con.submit();
			if(!emp.er_id.isEmpty()){
				return "Y";
			}else{
				return "N";
			}
		} catch (Exception e) {
			con.rollback();
			throw e;
		} finally {
			con.close();
		}

	}

	@ACOAction(eventname = "hr_get_blacklist", Authentication = false, notes = "获取黑名单")
	public String hr_get_blacklist() throws Exception {
		String sqlstr="";
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String searchTime=urlparms.get("searchtime");
		String start=urlparms.get("start");
		String take=urlparms.get("take");
		if(searchTime==null){
			sqlstr="SELECT  e.employee_code,e.employee_name,e.id_number,e.sex,e.hiredday,e.ljdate,e.orgname,e.telphone,e.sp_name,e.lv_num,e.nation,e.registeraddress,l.ljtype2,l.ljtype1,l.ljreason"+
					" FROM hr_employee e LEFT JOIN hr_leavejob l ON e.er_id=l.er_id WHERE l.`stat`=9 AND l.iscanced<>1  and e.empstatid='13' limit "+start+","+take+"";
		}else{
			sqlstr="SELECT  e.employee_code,e.employee_name,e.id_number,e.sex,e.hiredday,e.ljdate,e.orgname,e.telphone,e.sp_name,e.lv_num,e.nation,e.registeraddress,l.ljtype2,l.ljtype1,l.ljreason"+
					" FROM hr_employee e LEFT JOIN hr_leavejob l ON e.er_id=l.er_id WHERE l.`stat`=9 AND l.iscanced<>1  and e.empstatid='13' and (l.createtime>='"+searchTime+"' or l.updatetime>='"+searchTime+"') limit "+start+","+take+"";
		}
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}

	@ACOAction(eventname = "hr_get_standposition", Authentication = false, notes = "标准职位库")
	public String hr_get_standposition() throws Exception {
		String sqlstr="";
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String searchTime=urlparms.get("searchtime");
		if(searchTime==null){
			sqlstr="select hg_name,lv_num,sp_name,hwc_namezl,hwc_namezq,hwc_namezz,gtitle,isoffjob,updatetime from Hr_standposition where usable=1";
		}else{
			sqlstr="select hg_name,lv_num,sp_name,hwc_namezl,hwc_namezq,hwc_namezz,gtitle,isoffjob,updatetime from Hr_standposition where usable=1 and  (createtime>='"+searchTime+"' or updatetime>='"+searchTime+"')";

		}
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}

	@ACOAction(eventname = "hr_get_emptransfer", Authentication = false, notes = "调动记录")
	public String hr_get_emptransfer() throws Exception {
		String sqlstr="";
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		//String searchTime=urlparms.get("searchtime");
		String employee_code=urlparms.get("employee_code");
		//String start=urlparms.get("start");
		//String take=urlparms.get("take");
		sqlstr="select emptranfcode,employee_code,employee_name,odorgname,odsp_name,odlv_num,neworgname,newlv_num,newsp_name,tranftype1,tranfcmpdate,updatetime from `hr_employee_transfer` where stat='9' and employee_code='"+employee_code+"'"+
				" UNION ALL SELECT b.emptranfbcode,bl.employee_code,bl.employee_name,bl.odorgname,bl.odsp_name,bl.odlv_num,bl.neworgname,bl.newlv_num,bl.newsp_name,'批量调动',b.tranfcmpdate,b.updatetime"+
				" FROM  hr_emptransferbatch b,hr_emptransferbatch_line bl WHERE b.emptranfb_id=bl.emptranfb_id AND b.stat=9 and employee_code='"+employee_code+"'";
		//		if(searchTime!=null){
		//			sqlstr="select emptranfcode,employee_code,employee_name,odorgname,odsp_name,odlv_num,neworgname,newlv_num,newsp_name,tranftype1,tranfcmpdate,updatetime from `hr_employee_transfer` where stat='9'"+
		//					" and  updatetime>='"+searchTime+"'"+
		//					" UNION ALL SELECT b.emptranfbcode,bl.employee_code,bl.employee_name,bl.odorgname,bl.odsp_name,bl.odlv_num,bl.neworgname,bl.newlv_num,bl.newsp_name,'批量调动',b.tranfcmpdate,b.updatetime"+
		//					" FROM  hr_emptransferbatch b,hr_emptransferbatch_line bl WHERE b.emptranfb_id=bl.emptranfb_id AND b.stat=9  and  updatetime>='"+searchTime+"' limit "+start+","+take+"";
		//		}
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}
	/**
	 * 获取人员信息
	 * @return
	 * @throws Exception
	 */
	@ACOAction(eventname = "hr_get_employee", Authentication = false, notes = "人员信息")
	public String hr_get_employee()throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String begintime=urlparms.get("begintime");
		String endtime=urlparms.get("endtime");
		String sqlstr="SELECT employee_code,employee_name,RIGHT(id_number,6) id_number,lv_num,telphone,orgcode,b.orgname,b.extorgname,b.attribute1 as orgcode2,a.createtime"+
				" from hr_employee a INNER JOIN shworg b on a.orgid =b.orgid"+
				" where a.createtime BETWEEN '"+begintime+"' and '"+endtime+"'";
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();
	}
	@ACOAction(eventname = "hr_get_finish_quotacode", Authentication = false, notes = "获取已经使用的工资额度编码")
	public String hr_get_finish_quotacode()throws Exception {
		String sqlstr="";
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String searchTime=urlparms.get("searchtime");
		if(searchTime==null){
			sqlstr="SELECT * from (SELECT employee_code,salary_quotacode,createtime FROM hr_employee_transfer WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0'"+
					" UNION SELECT employee_code,salary_quotacode,createtime FROM hr_entry_prob WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0'"+
					" UNION SELECT employee_code,salary_quotacode,createtime FROM hr_transfer_prob WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0'"+
					" UNION SELECT employee_code,salary_quotacode,createtime FROM Hr_salary_specchgsa WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0')tb"+
					" where createtime>'2020-07-01' order by createtime";
		}else{
			sqlstr="SELECT * from (SELECT employee_code,salary_quotacode,createtime FROM hr_employee_transfer WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0'"+
					" UNION SELECT employee_code,salary_quotacode,createtime FROM hr_entry_prob WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0'"+
					" UNION SELECT employee_code,salary_quotacode,createtime FROM hr_transfer_prob WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0'"+
					" UNION SELECT employee_code,salary_quotacode,createtime FROM Hr_salary_specchgsa WHERE stat=9   and salary_quotacode is not NULL and salary_quotacode<>'0')tb"+
					" where createtime>'"+searchTime+"' order by createtime";
		}
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();

	}

	@ACOAction(eventname = "hr_createTrip", Authentication = false, notes = "移动端创建出差单")
	public String hr_createTrip() throws Exception{
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		//HashMap<String, String> pparms = CSContext.parPostDataParms();
		Hrkq_business_trip happ_btrip = new Hrkq_business_trip();// 创建出差单实体
		//初始化参数
		String entid=null;
		String userid=null;
		String userids = CorUtil.hashMap2Str(urlparms, "userids");
		String[] ckuserids = ((userids != null) && (userids.length() > 0)) ? userids.split(",") : null;
		Shwuser user = new Shwuser();
		happ_btrip.fromjson(CSContext.getPostdata());// 从前端JSON获取数据
		String sqlstr = "SELECT * FROM shwuser WHERE username=?";
		PraperedSql psql = new PraperedSql();
		psql.setSqlstr(sqlstr);
		psql.getParms().add(new PraperedValue(java.sql.Types.VARCHAR, happ_btrip.employee_code.getValue().trim()));// UserName.trim().toUpperCase()
		user.findByPSQL(psql);
		if (user.isEmpty())
			throw new Exception("用户名不存在!");
		if (user.actived.getAsIntDefault(0) != 1)
			throw new Exception("用户已禁用!");
		userid=user.getid();
		CJPALineData<Shworguser> ous = new CJPALineData<Shworguser>(Shworguser.class);
		ous.setPool(user.pool);
		String sqlstrfinduser = "select * from shworguser where userid=" + user.userid.getValue();
		ous.findDataBySQL(sqlstrfinduser, true, true);
		for (CJPABase oub : ous) {
			Shworguser ou = (Shworguser) oub;
			if (ou.isdefault.getAsIntDefault(0) == 1) {
				Shworg org = new Shworg();
				org.findByID(ou.orgid.getValue());
				entid= org.entid.getValue();
				break;
			}
		}
		
		int hrlev = HRUtil.getOrgHrLev(happ_btrip.orgid.getValue());
		//初始化参数结束
		if(happ_btrip.bta_id.getValue()==null || happ_btrip.bta_id.getValue().isEmpty())
		{
			if (happ_btrip.employee_code.isEmpty()) {
				new Exception("出差单字段【employee_code】不能为空");
			}
			if (happ_btrip.tripdays.isEmpty()) {
				new Exception("出差单字段【tripdays】不能为空");
			}
			if (happ_btrip.begin_date.isEmpty()) {
				new Exception("出差单字段【begin_date】不能为空");
			}
			if (happ_btrip.end_date.isEmpty()) {
				new Exception("出差单字段【end_date】不能为空");
			}
			if (happ_btrip.trip_type.isEmpty()) {
				new Exception("出差单字段【trip_type】不能为空");
			}
			if (happ_btrip.destination.isEmpty()) {
				new Exception("出差单字段【destination】不能为空");
			}
			if (happ_btrip.tripreason.isEmpty()) {
				new Exception("出差单字段【tripreason】不能为空");
			}
			Hr_employee emp = new Hr_employee();// 创建人事档案实体
			String sqlstr2 = "select * from hr_employee where employee_code='" + happ_btrip.employee_code.getValue() + "'";
			emp.findBySQL(sqlstr2);// 查询人事档案
			if (emp.isEmpty())
				new Exception("ID为【" + happ_btrip.er_id.getValue() + "】的人事资料不存在");
			if ((happ_btrip.trip_type.getAsIntDefault(0) != 1) && (happ_btrip.trip_type.getAsIntDefault(0) != 2))
				new Exception("ID为【" + happ_btrip.trip_type + "】的出差类型不存在");

			happ_btrip.er_id.setValue(emp.er_id.getValue()); // 人事ID
			happ_btrip.employee_code.setValue(emp.employee_code.getValue()); // 工号
			happ_btrip.employee_name.setValue(emp.employee_name.getValue()); // 姓名
			happ_btrip.orgid.setValue(emp.orgid.getValue()); // 部门ID
			happ_btrip.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
			happ_btrip.orgname.setValue(emp.orgname.getValue()); // 部门
			happ_btrip.ospid.setValue(emp.ospid.getValue()); // 职位ID
			happ_btrip.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
			happ_btrip.sp_name.setValue(emp.sp_name.getValue()); // 职位
			happ_btrip.lv_num.setValue(emp.lv_num.getValue()); // 职级
			happ_btrip.trip_type.setValue(happ_btrip.trip_type.getValue()); // 出差类型
			happ_btrip.tripreason.setValue(happ_btrip.tripreason.getValue()); // 事由
			happ_btrip.destination.setValue(happ_btrip.destination.getValue()); // 目的地

			happ_btrip.tripdays.setValue(happ_btrip.tripdays.getValue()); // 实际天数

			happ_btrip.begin_date.setValue(happ_btrip.begin_date.getValue()); // 实际开始时间
			happ_btrip.end_date.setValue(happ_btrip.end_date.getValue()); // 实际截止时间

			happ_btrip.iswfagent.setValue("1"); // 启用流程代理
			happ_btrip.stat.setValue("1"); // 表单状态
			happ_btrip.idpath.setValue(emp.idpath.getValue()); // idpath
			happ_btrip.creator.setValue(emp.employee_code.getValue()); // 创建人
			happ_btrip.createtime.setAsDatetime(new Date()); // 创建时间
			
			happ_btrip.orghrlev.setValue(HRUtil.getOrgHrLev(happ_btrip.orgid.getValue()));
			//happ_btrip.emplev.setValue("0");
			happ_btrip.entid.setValue("1");//制单状态

			CDBConnection con = happ_btrip.pool.getCon(this); // 获取数据库连接
			con.startTrans(); // 开始事务
			try {
				happ_btrip.save(con);// 保存出差表单
				con.submit();// 提交数据库事务
				//return happ_btrip.tojson();
			} catch (Exception e) {
				con.rollback();// 回滚事务
				throw e;
			} finally {
				con.close();
			}
		}
		try{
			return COCSCommon.createWFForMobile("com.hr.attd.entity.Hrkq_business_trip", happ_btrip.bta_id.getValue(), userid, entid,ckuserids);
		}catch (Exception e){
			return "{\"jpaid\":"+happ_btrip.getid()+"}";
		}
		
	
	}
	
	
	@ACOAction(eventname = "hr_get_iccard", Authentication = false, notes = "获取IC卡数据")
	public String hr_get_iccard()throws Exception {
		String sqlstr="";
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String start=urlparms.get("start");
		String end=urlparms.get("end");
		if(start!=null && end!=null){
			sqlstr="select employee_code, card_stat,card_sn,card_number,updatetime from  hr_ykt_card order where updatetime>='"+start+"' and updatetime<='"+end+"' by updatetime";
		}else{
			new Exception("开始和结束日期不能为空！");
		}
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport();

	}

}
