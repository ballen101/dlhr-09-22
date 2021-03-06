package com.hr.perm.ctr;

import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONObject;

import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.util.Logsw;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.wordflow.Shwwf;
import com.corsair.server.wordflow.Shwwfproc;
import com.hr.attd.ctr.CtrHrkq_workschmonth;
import com.hr.base.entity.Hr_employeestat;
import com.hr.base.entity.Hr_orgposition;
import com.hr.insurance.co.COHr_ins_insurance;
import com.hr.insurance.entity.Hr_ins_buyinsurance;
import com.hr.insurance.entity.Hr_ins_insurancetype;
import com.hr.insurance.entity.Hr_ins_prebuyins;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_entry;
import com.hr.perm.entity.Hr_entry_try;
import com.hr.perm.entity.Hr_systemparms;
import com.hr.salary.co.Cosacommon;
import com.hr.salary.ctr.CtrSalaryCommon;
import com.hr.util.HTQuotaUtil;

public class CtrHr_entry extends JPAController {

	@Override
	public void BeforeWFStartSave(CJPA jpa, Shwwf wf, CDBConnection con, boolean isFilished) throws Exception {
		// TODO Auto-generated method stub
		Hr_entry et = (Hr_entry) jpa;
		Hr_employee emp = new Hr_employee();
		emp.findByID4Update(con, et.er_id.getValue(), false);
		if (emp.isEmpty()) {
			throw new Exception("ID为【" + et.er_id.getValue() + "】的人事档案不存在");
		}
		wf.subject.setValue(((Hr_entry) jpa).entry_code.getValue());
		checkcansubmit(con, emp);
		if (isFilished)
			doEntry(jpa, con);
	}

	private void checkcansubmit(CDBConnection con, Hr_employee emp) throws NumberFormatException, Exception {
		String id_number = emp.id_number.getValue();
		// String sqlstr = "SELECT * FROM hr_employee WHERE id_number ='" + id_number + " and er_id<>" + emp.er_id + " and empstatid in(1,2,3,4,5,6,7,8)";
		// if (Integer.valueOf(con.openSql2List(sqlstr).get(0).get("ct")) != 0) {
		// throw new Exception("员工资料在【在职列表】中，不允许重新提交入职");
		// }

		Hr_employee temp = CtrHrEmployeeUtil.getIDNumberIsFrmal(id_number);
		if ((!temp.isEmpty()) && (temp.empstatid.getAsInt() != 6) && (temp.er_id.getAsInt() != emp.er_id.getAsInt()))
			throw new Exception("员工资料在【在职列表】中，不允许重新提交入职");

		if (CtrHrEmployeeUtil.isInBlackList(id_number))
			throw new Exception("身份证为【" + id_number + "】的员工标记为【黑名单】");

	}

	@Override
	public void OnWfSubmit(CJPA jpa, CDBConnection con, Shwwf wf, Shwwfproc proc,
			Shwwfproc arg3, boolean arg4) throws Exception {
		// TODO Auto-generated method stub
		if (arg4) {
			doEntry(jpa, con);
			prebuyinsurances(jpa, con);
		}
	}

	@Override
	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf, boolean isFilished) throws Exception {
		// TODO Auto-generated method stub
		checkquota(jpa, con);
		if (isFilished) {
			doEntry(jpa, con);
			prebuyinsurances(jpa, con);
			
		}
	}

	private void doEntry(CJPA jpa, CDBConnection con) throws Exception {
		Logsw.dblog("执行了doEntry");
		Hr_entry entry = (Hr_entry) jpa;
		Hr_employee emp = new Hr_employee();
		emp.findByID4Update(con, entry.er_id.getValue(), false);
		if (emp.isEmpty()) {
			throw new Exception("ID为【" + entry.er_id.getValue() + "】的人事档案不存在");
		}
		// System.out.println("doEntry222222222222222222222");
		if (entry.quota_over.getAsInt() == 1) {// 超编
			// System.out.println("doEntry3333333333333333333333333");
			if (entry.quota_over_rst.isEmpty())
				throw new Exception("超编的需要评审超编处理方式");
			int quota_over_rst = entry.quota_over_rst.getAsInt(); // 1 允许增加编制入职 2 超编入职 3不允许入职
			if (quota_over_rst == 1) {
				// 增加编制
				Hr_orgposition hop = new Hr_orgposition();
				hop.findByID4Update(con, emp.ospid.getValue(), false);
				CtrHr_quota_release.dochgquota(con, hop, 1, entry.entry_id.getValue(), "0", entry.entry_code.getValue(), "6");
				setEmploestat(entry, emp, con);// 入职
			} else if (quota_over_rst == 2) {
				setEmploestat(entry, emp, con);// 入职
			} else if (quota_over_rst == 3) {
				// 不处理资料
			}
		} else
			setEmploestat(entry, emp, con);// 入职

		CtrSalaryCommon.newSalaryChangeLog(con, entry);

	}

	private void setEmploestat(Hr_entry entry, Hr_employee emp, CDBConnection con) throws Exception {
		// System.out.println("doEntry444444444444444444444444444444");
		Hr_employeestat estat = new Hr_employeestat();
		estat.findBySQL("select * from hr_employeestat where statvalue=" + emp.empstatid.getValue());
		if (emp.empstatid.getAsInt() != 6) {
			throw new Exception("状态为【" + estat.language1.getValue() + "】的人事档案不允许做入职审批操作");
		}
		int entrytype = entry.entrytype.getAsInt();// 类型 新招 重聘 毕业生

		if ((entrytype == 1) || (entrytype == 2)) {
			if (entry.probation.getAsInt() == 0) {// 无试用期
				emp.empstatid.setAsInt(4);// 正式员工
				entry.ispromotioned.setAsInt(1);
			} else {
				emp.empstatid.setAsInt(2);// 试用期
				createEmtrytryInfo(entry, emp, con);// 创建试用期信息列表
			}
		} else if (entrytype == 3) {
			if (entry.probation.getAsInt() == 0) {// 无试用期
				emp.empstatid.setAsInt(4);// 正式员工
				entry.ispromotioned.setAsInt(1);
			} else {
				emp.empstatid.setAsInt(1);// 实习期
			}
		} else
			throw new Exception("入职类型错误");
		emp.save(con, false);
		CtrHrkq_workschmonth.putEmDdtWeekPB(con, emp, emp.hiredday.getAsDatetime());// 生成默认排班
	}

	private void createEmtrytryInfo(Hr_entry entry, Hr_employee emp, CDBConnection con) throws Exception {
		Hr_entry_try et = new Hr_entry_try();
		et.entry_id.setValue(entry.entry_id.getValue()); // 入职单ID
		et.entry_code.setValue(entry.entry_code.getValue()); // 入职表单编码
		et.er_id.setValue(emp.er_id.getValue()); // 人事档案ID
		et.employee_code.setValue(emp.employee_code.getValue()); // 工号
		et.employee_name.setValue(emp.employee_name.getValue()); // 姓名
		et.id_number.setValue(emp.id_number.getValue());// 身份证号码
		et.degree.setValue(emp.degree.getValue()); // 学历
		et.orgid.setValue(emp.orgid.getValue()); // 部门ID
		et.orgcode.setValue(emp.orgcode.getValue()); // 部门编码
		et.orgname.setValue(emp.orgname.getValue()); // 部门名称
		et.ospid.setValue(emp.ospid.getValue()); // 职位ID
		et.ospcode.setValue(emp.ospcode.getValue()); // 职位编码
		et.sp_name.setValue(emp.sp_name.getValue()); // 职位名称
		et.hwc_namezl.setValue(emp.hwc_namezl.getValue()); // 职类
		et.hg_name.setValue(emp.hg_name.getValue()); // 职等名称
		et.lv_num.setValue(emp.lv_num.getValue()); // 职级
		et.entrydate.setValue(emp.hiredday.getValue()); // 入职日期
		et.probation.setValue(entry.probation.getValue()); // 试用期
		et.promotionday.setValue(entry.promotionday.getValue()); // 转正日期 第一次约定的试用期
		et.pay_way.setValue(entry.pay_way.getValue()); // 计薪方式
		et.promotiondaytrue.setValue(null); // 实际转正日期 根据转正评审结果确定 如延长试用期则为空
		et.wfresult.setValue(null); // 评审结果 1 同意转正 2 延长试用 3 试用不合格
		et.delayprobation.setValue(null); // 延长试用期
		et.delaypromotionday.setValue(null); // 延期待转正时间 根据延长试用期自动计算，多次延期累计计算
		et.delaypromotiondaytrue.setValue(null); // 延期实际转正时间
		et.delaywfresult.setValue(null); // 评审结果 1 同意转正 2 延长试用 3 试用不合格
		et.delaytimes.setValue("0"); // 延期次数
		et.trystat.setValue("1"); // 试用期人事状态 试用期中、试用过期、试用延期、己转正、试用不合格
		et.remark.setValue(null); // 备注
		et.idpath.setValue(emp.idpath.getValue()); // idpath
		et.entid.setValue(emp.entid.getValue()); // entid
		et.creator.setValue("SYSTEM"); // 创建人
		et.createtime.setAsDatetime(new Date()); // 创建时间
		et.save(con);
	}

	private void checkquota(CJPA jpa, CDBConnection con) throws Exception {
		Hr_entry entry = (Hr_entry) jpa;
		// 入职单提交时候 检查编制
		Hr_employee emp = new Hr_employee();
		emp.findByID4Update(con, entry.er_id.getValue(), false);
		if (emp.isEmpty()) {
			throw new Exception("ID为【" + entry.er_id.getValue() + "】的人事档案不存在");
		}

		Hr_orgposition op = new Hr_orgposition();
		op.findByID(con, emp.ospid.getValue());
		if (op.isEmpty())
			throw new Exception("ID为【" + emp.ospid.getValue() + "】的机构职位不存在");

		String sqlstr = "SELECT * FROM hr_systemparms WHERE usable=1 AND parmcode='BATCH_QUATA_CLASS' AND parmvalue='" + op.hwc_idzl.getValue() + "'";
		Hr_systemparms hs = new Hr_systemparms(sqlstr);
		if (hs.isEmpty()) {// 单独控制
			if (HTQuotaUtil.checkOrgPositionQuota(con, emp.ospid.getValue(), 1))
				entry.quota_over.setAsInt(2);
			else
				entry.quota_over.setAsInt(1);
		} else {// 批量控制
			if (HTQuotaUtil.checkOrgClassQuota(con, emp.orgid.getValue(), op.hwc_idzl.getValue(), 1))
				entry.quota_over.setAsInt(2);
			else
				entry.quota_over.setAsInt(1);
		}
		entry.save(con);
	}

	private void dobuyinsurance(CJPA jpa, CDBConnection con) throws Exception {
		Hr_entry entry = (Hr_entry) jpa;
		Hr_employee emp = new Hr_employee();
		emp.findByID4Update(con, entry.er_id.getValue(), false);
		if (emp.isEmpty()) {
			throw new Exception("ID为【" + entry.er_id.getValue() + "】的人事档案不存在");
		}
		if (emp.empstatid.getAsInt() >= 12) {
			throw new Exception("工号为【" + entry.employee_code.getValue() + "】的人员状态不是在职或其他可购保的状态");
		}
		if (emp.lv_num.getAsFloat() <= 6.2) {
			Hr_ins_buyinsurance bi = new Hr_ins_buyinsurance();
			Hr_ins_insurancetype it = new Hr_ins_insurancetype();
			String buydate = Systemdate.getStrDateyyyy_mm_dd(new Date());
			it.findBySQL("SELECT * FROM hr_ins_insurancetype WHERE ins_type=1 AND '" + buydate + "'>=buydate ORDER BY buydate DESC ");
			if (it.isEmpty()) {
				throw new Exception("购保时间为【" + buydate + "】时无可用的险种");
			}

			bi.insurance_number.setValue(emp.employee_code.getValue());
			bi.buydday.setValue(buydate);
			bi.insit_id.setValue(it.insit_id.getValue());
			bi.insit_code.setValue(it.insit_code.getValue());
			bi.insname.setValue(it.insname.getValue());
			bi.ins_type.setAsInt(1);
			bi.payment.setValue(it.payment.getValue());
			bi.tselfpay.setValue(it.selfpay.getValue());
			bi.tcompay.setValue(it.compay.getValue());

			bi.er_id.setValue(emp.er_id.getValue());
			bi.employee_code.setValue(emp.employee_code.getValue());
			bi.employee_name.setValue(emp.employee_name.getValue());
			bi.ospid.setValue(emp.ospid.getValue());
			bi.ospcode.setValue(emp.ospcode.getValue());
			bi.sp_name.setValue(emp.sp_name.getValue());
			bi.lv_id.setValue(emp.lv_id.getValue());
			bi.lv_num.setValue(emp.lv_num.getValue());
			bi.hiredday.setValue(emp.hiredday.getValue());
			bi.orgid.setValue(emp.orgid.getValue());
			bi.orgcode.setValue(emp.orgcode.getValue());
			bi.orgname.setValue(emp.orgname.getValue());
			bi.degree.setValue(emp.degree.getValue());
			bi.sex.setValue(emp.sex.getValue());
			bi.telphone.setValue(emp.telphone.getValue());
			bi.nativeplace.setValue(emp.nativeplace.getValue());
			bi.registertype.setValue(emp.registertype.getValue());
			bi.pay_type.setValue(emp.pay_way.getValue());
			bi.id_number.setValue(emp.id_number.getValue());
			bi.sign_org.setValue(emp.sign_org.getValue());
			bi.sign_date.setValue(emp.sign_date.getValue());
			bi.expired_date.setValue(emp.expired_date.getValue());
			bi.birthday.setValue(emp.birthday.getValue());
			int age = getAgeByBirth(emp.birthday.getValue());
			bi.age.setAsInt(age);
			bi.sptype.setValue(emp.emnature.getValue());
			bi.isnew.setAsInt(1);
			bi.reg_type.setAsInt(1);
			String nowtime = "";
			Calendar hday = Calendar.getInstance();
			Date hd = Systemdate.getDateByStr(emp.hiredday.getValue());
			hday.setTime(hd);
			int inday = hday.get(Calendar.DATE);
			if (inday < 21) {
				nowtime = Systemdate.getStrDateByFmt(new Date(), "yyyy-MM-dd");
			} else {
				Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateByFmt(new Date(), "yyyy-MM-dd"));// 去除时分秒
				Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
				nowtime = Systemdate.getStrDateyyyy_mm_dd(eddate);
			}
			bi.buydday.setValue(nowtime);
			bi.ins_type.setValue(it.ins_type.getValue());
			bi.selfratio.setValue(it.selfratio.getValue());
			bi.selfpay.setValue(it.selfpay.getValue());
			bi.comratio.setValue(it.comratio.getValue());
			bi.compay.setValue(it.compay.getValue());
			bi.insurancebase.setValue(it.insurancebase.getValue());
			bi.idpath.setValue(emp.idpath.getValue());
			bi.remark.setValue("入职单【" + entry.entry_code.getValue() + "】自动生成购保");
			bi.save(con);
		}

	}

	private static int getAgeByBirth(String birthday) {
		int age = 0;
		try {
			Calendar now = Calendar.getInstance();
			now.setTime(new Date());// 当前时间
			Calendar birth = Calendar.getInstance();
			Date bd = Systemdate.getDateByStr(birthday);
			birth.setTime(bd);
			if (birth.after(now)) {// 如果传入的时间，在当前时间的后面，返回0岁
				age = 0;
			} else {
				age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
				if (now.get(Calendar.DAY_OF_YEAR) > birth.get(Calendar.DAY_OF_YEAR)) {
					age += 1;
				}
			}
			return age;
		} catch (Exception e) {// 兼容性更强,异常后返回数据
			return 0;
		}
	}

	/**
	 * 购保单生成
	 * 
	 * @param jpa
	 * @param con
	 * @throws Exception
	 */
	private void prebuyinsurances(CJPA jpa, CDBConnection con) throws Exception {
		Hr_entry entry = (Hr_entry) jpa;
		Hr_employee emp = new Hr_employee();
		emp.findByID4Update(con, entry.er_id.getValue(), false);
		if (emp.isEmpty()) {
			throw new Exception("ID为【" + entry.er_id.getValue() + "】的人事档案不存在");
		}

		Hr_ins_buyinsurance oldbi = new Hr_ins_buyinsurance();
		oldbi.findBySQL("SELECT * FROM `hr_ins_buyinsurance` WHERE stat=9  AND '" + entry.entrydate.getValue() + "'>=buydday AND '" + entry.entrydate.getValue() + "'<=LAST_DAY(buydday) AND er_id=" + emp.er_id.getValue());
		if (!oldbi.isEmpty()) {
			return;
		}
		boolean islocal = false;
		boolean levcheck = false;
		if (emp.registertype.isEmpty())
			throw new Exception("工号【" + emp.employee_code.getValue() + "】未维护户籍类型，购保单生成失败");
		if ((emp.registertype.getAsInt() == 1) || (emp.registertype.getAsInt() == 2)) {// 户籍类型是本地的
			islocal = true;
		}
		if (emp.lv_num.getAsFloatDefault(0) == 0)
			throw new Exception("工号【" + emp.employee_code.getValue() + "】生成购保单需要职级");
		if (emp.lv_num.getAsFloat() <= 6.3) {// 职级<=6.3的
			levcheck = true;
		}
		if (islocal || levcheck) {
			Hr_ins_prebuyins pbi = new Hr_ins_prebuyins();
			pbi.er_id.setValue(emp.er_id.getValue());
			pbi.employee_code.setValue(emp.employee_code.getValue());
			pbi.employee_name.setValue(emp.employee_name.getValue());
			pbi.ospid.setValue(emp.ospid.getValue());
			pbi.ospcode.setValue(emp.ospcode.getValue());
			pbi.sp_name.setValue(emp.sp_name.getValue());
			pbi.lv_id.setValue(emp.lv_id.getValue());
			pbi.lv_num.setValue(emp.lv_num.getValue());
			pbi.hiredday.setValue(emp.hiredday.getValue());
			pbi.orgid.setValue(emp.orgid.getValue());
			pbi.orgcode.setValue(emp.orgcode.getValue());
			pbi.orgname.setValue(emp.orgname.getValue());
			pbi.registertype.setValue(emp.registertype.getValue());
			pbi.sex.setValue(emp.sex.getValue());
			pbi.birthday.setValue(emp.birthday.getValue());
			pbi.idpath.setValue(emp.idpath.getValue());
			pbi.degree.setValue(emp.degree.getValue());
			pbi.nativeplace.setValue(emp.nativeplace.getValue());
			pbi.id_number.setValue(emp.id_number.getValue());
			pbi.registeraddress.setValue(emp.registeraddress.getValue());
			pbi.telphone.setValue(emp.telphone.getValue());
			pbi.transorg.setValue(emp.transorg.getValue());
			pbi.dispunit.setValue(emp.dispunit.getValue());
			int age = COHr_ins_insurance.getAgeByBirth(emp.birthday.getValue());
			pbi.age.setAsInt(age);
			pbi.tranfcmpdate.setValue(entry.entrydate.getValue());
			pbi.isbuyins.setAsInt(2);
			pbi.sourceid.setValue(entry.entry_id.getValue());
			pbi.sourcecode.setValue(entry.entry_code.getValue());
			pbi.prebuytype.setAsInt(1);
			Calendar hday = Calendar.getInstance();
			Date hd = Systemdate.getDateByStr(entry.entrydate.getValue());
			hday.setTime(hd);
			String nowtime = "";
			// Date nowdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd());
			int inday = hday.get(Calendar.DATE);
			if (inday <= 25) {
				Date firstdate = Systemdate.getFirstAndLastOfMonth(hd).date1;
				nowtime = Systemdate.getStrDateyyyy_mm_dd(firstdate);
			} else {
				Date bgdate = Systemdate.getFirstAndLastOfMonth(hd).date2;// 取最后一天的日期
				Date eddate = Systemdate.dateDayAdd(bgdate, 1);// 加一天获取下月一号的日期
				nowtime = Systemdate.getStrDateyyyy_mm_dd(eddate);
			}
			pbi.dobuyinsdate.setValue(nowtime);
			pbi.save(con);
		}
	}

	
}
