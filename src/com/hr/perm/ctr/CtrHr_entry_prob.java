package com.hr.perm.ctr;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.corsair.cjpa.CJPABase;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.util.Logsw;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.wordflow.Shwwf;
import com.corsair.server.wordflow.Shwwfproc;
import com.hr.base.entity.Hr_employeestat;
import com.hr.base.entity.Hr_orgposition;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_entry;
import com.hr.perm.entity.Hr_entry_prob;
import com.hr.perm.entity.Hr_entry_try;
import com.hr.perm.entity.Hr_transfer_prob;
import com.hr.salary.co.Cosacommon;
import com.hr.salary.ctr.CtrSalaryCommon;
import com.hr.salary.entity.Hr_salary_chgbill;
import com.hr.salary.entity.Hr_salary_orgminstandard;
import com.hr.salary.entity.Hr_salary_positionwage;
import com.hr.salary.entity.Hr_salary_structure;
import com.hr.util.HRUtil;

import net.sf.json.JSONObject;

public class CtrHr_entry_prob extends JPAController {

	/**
	 * 保存前
	 * 
	 * @param jpa里面有值
	 * ，还没检测数据完整性，没生成ID CODE 设置默认值
	 * @param con
	 * @param selfLink
	 * @throws Exception
	 */
	@Override
	public void BeforeSave(CJPABase jpa, CDBConnection con, boolean selfLink) throws Exception {
		Hr_entry_prob ep = (Hr_entry_prob) jpa;
		Hr_orgposition osp = new Hr_orgposition();
		osp.findByID(ep.ospid.getValue(), false);
		if (!osp.isEmpty()) {
			if ((osp.isoffjob.getAsIntDefault(0) == 1) && (ep.pbtsarylev.getAsFloatDefault(0) > 0))
				if (ep.salary_quotacode.isEmpty())
					throw new Exception("工资额度编号不允许为空");
		}
		if ((((ep.newposition_salary.getAsFloatDefault(0) > ep.oldposition_salary.getAsFloatDefault(0)) || (ep.newtech_allowance.getAsFloatDefault(0) > ep.oldtech_allowance.getAsFloatDefault(0))) && ep.pbtsarylev.getAsFloatDefault(0) == 0)||
				(((ep.newposition_salary.getAsFloatDefault(0)>ep.oldposition_salary.getAsFloatDefault(0)))
				&&ep.overf_salary.getAsFloatDefault(0)==0)) {
			resetsalaryinfo(ep);
			
			//throw new Exception("薪资有调整但调整额度为0，请检查单据或清理浏览器缓存后再制单。（清理浏览器缓存方法：同时按 ctr+shift+delete 即可弹出删除缓存对话框。）");
		}
		
		Date pbtdate=Systemdate.getDateByyyyy_mm_dd(ep.pbtdate.getValue());
		Calendar ca = Calendar.getInstance();
		ca.setTime(pbtdate);
		int pbmonth=ca.get(Calendar.MONTH);
		int pbyear=ca.get(Calendar.YEAR);
		Calendar now = Calendar.getInstance();
		int nowmonth=now.get(Calendar.MONTH);
		int nowyear=now.get(Calendar.YEAR);
		
		if(pbyear>nowyear){
			ep.salarydate.setValue(ep.pbtdate.getValue());
		}else if(pbyear<nowyear){
			ep.salarydate.setValue(Systemdate.getStrDateyyyy_mm_01());
		}
		else if(pbmonth>=nowmonth){
			ep.salarydate.setValue(ep.pbtdate.getValue());
		}else if(pbmonth<nowmonth){
			ep.salarydate.setValue(Systemdate.getStrDateyyyy_mm_01());
		}
		Logsw.dblog("实际调薪日期："+ep.salarydate.getValue());
	}
	
private void resetsalaryinfo(Hr_entry_prob scs) throws Exception{
		
		float np=scs.newposition_salary.getAsFloatDefault(0);
		float op=scs.oldposition_salary.getAsFloatDefault(0);
		if((np>op)&scs.pbtsarylev.getAsFloatDefault(0)==0){
			Hr_salary_structure  ss=new Hr_salary_structure();
			ss.findByID(scs.newstru_id.getValue());
			if(!ss.isEmpty()){
				if(ss.strutype.getAsInt()==1){
					float minstand=0;
					String sqlstr="SELECT * FROM `hr_salary_orgminstandard` WHERE stat=9 AND usable=1 AND INSTR('"+scs.idpath.getValue()+"',idpath)=1  ORDER BY idpath DESC  ";
					Hr_salary_orgminstandard oms=new Hr_salary_orgminstandard();
					oms.findBySQL(sqlstr);
					if(oms.isEmpty()){
						minstand=0;
					}else{
						minstand=oms.minstandard.getAsFloatDefault(0);
					}
					float bw=(np*ss.basewage.getAsFloatDefault(0))/100;
					float bow=(np*ss.otwage.getAsFloatDefault(0))/100;
					float sw=(np*ss.skillwage.getAsFloatDefault(0))/100;
					float pw=(np*ss.meritwage.getAsFloatDefault(0))/100;
					if(minstand>bw){
						if((bw+bow)>minstand){
							bow=bw+bow-minstand;
							bw=minstand;
						}else if((bw+bow+sw)>minstand){
							sw=bw+bow+sw-minstand;
							bow=0;
							bw=minstand;
						}else if((bw+bow+sw+pw)>minstand){
							pw=bw+bow+sw+pw-minstand;
							sw=0;
							bow=0;
							bw=minstand;
						}else{
							bw=np;
							pw=0;
							sw=0;
							bow=0;
						}
					}
					scs.newbase_salary.setAsFloat(bw);
					scs.newtech_salary.setAsFloat(sw);
					scs.newachi_salary.setAsFloat(pw);
					scs.newotwage.setAsFloat(bow);
					scs.chgbase_salary.setAsFloat(bw-scs.oldbase_salary.getAsFloatDefault(0));
					scs.chgtech_salary.setAsFloat(sw-scs.oldtech_salary.getAsFloatDefault(0));
					scs.chgachi_salary.setAsFloat(pw-scs.oldachi_salary.getAsFloatDefault(0));
					scs.chgotwage.setAsFloat(bow-scs.oldotwage.getAsFloatDefault(0));
				}else{
					scs.chgbase_salary.setAsFloat(scs.newbase_salary.getAsFloatDefault(0)-scs.oldbase_salary.getAsFloatDefault(0));
					scs.chgtech_salary.setAsFloat(scs.newtech_salary.getAsFloatDefault(0)-scs.oldtech_salary.getAsFloatDefault(0));
					scs.chgachi_salary.setAsFloat(scs.newachi_salary.getAsFloatDefault(0)-scs.oldachi_salary.getAsFloatDefault(0));
					scs.chgotwage.setAsFloat(scs.newotwage.getAsFloatDefault(0)-scs.oldotwage.getAsFloatDefault(0));
				}
				scs.chgposition_salary.setAsFloat(np-scs.oldposition_salary.getAsFloatDefault(0));
				scs.chgtech_allowance.setAsFloat(scs.newtech_allowance.getAsFloatDefault(0)-scs.oldtech_allowance.getAsFloatDefault(0));
				scs.pbtsarylev.setAsFloat(np+scs.newtech_allowance.getAsFloatDefault(0)-scs.oldposition_salary.getAsFloatDefault(0)-scs.oldtech_allowance.getAsFloatDefault(0));
			}
		}
		if(np>0){
			Hr_salary_positionwage spw=new Hr_salary_positionwage();
			spw.findBySQL("SELECT pw.* FROM `hr_salary_positionwage` pw,`hr_orgposition` op WHERE pw.stat=9 AND pw.usable=1 AND pw.ospid=op.sp_id AND op.ospid="+scs.ospid.getValue());
			if(spw.isEmpty()){
				scs.overf_salary.setValue(0);
				scs.overf_salary_chgtech.setValue(0);
			}else{
				if(np>spw.psl5.getAsFloatDefault(0)){
					float spwage=spw.psl5.getAsFloatDefault(0);
					float chgspw=np-spwage;
					scs.overf_salary.setAsFloat(chgspw);
					float chgper=(chgspw/spwage)*100;
					scs.overf_salary_chgtech.setAsFloat(chgper);
				}
			}
		}
		
	}

	/**
	 * 通用保存后，提交事务前触发
	 * 
	 * @param con
	 * @param jpa
	 * @return 不为空的返回值，将作为查询结果返回给前台
	 * @throws Exception
	 */
	@Override
	public String AfterCCoSave(CDBConnection con, CJPA jpa) throws Exception {
		Hr_entry_prob ep = (Hr_entry_prob) jpa;
		HashMap<String, String> pparms = CSContext.parPostDataParms();
		String jpadata = pparms.get("jpadata");
		if ((jpadata == null) || jpadata.isEmpty())
			throw new Exception("需要jpadata参数");
		JSONObject jo = JSONObject.fromObject(jpadata);
		Logsw.dblog("流程AfterCCoSave:"+ep.salarydate.getValue());
		Cosacommon.save_salary_chgblill(con, ep, jo);
		return null;
	}

	@Override
	public void BeforeWFStartSave(CJPA jpa, Shwwf wf, CDBConnection con, boolean isFilished) throws Exception {
		Hr_entry_prob ep = (Hr_entry_prob) jpa;
		wf.subject.setValue(ep.pbtcode.getValue());
		if ((ep.shw_attachs == null) || (ep.shw_attachs.size() == 0)) {
			throw new Exception("需要上传附件");
		}
	}

	@Override
	public void OnWfSubmit(CJPA jpa, CDBConnection con, Shwwf wf, Shwwfproc proc,
			Shwwfproc arg3, boolean arg4) throws Exception {
		// TODO Auto-generated method stub
		if (arg4)
			doepend(jpa, con);
		Hr_entry_prob prob=(Hr_entry_prob) jpa;
		CtrSalaryCommon.updateWageState(prob.salary_quotacode.getValue(),prob.employee_code.getValue());
		
	}

	@Override
	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf, boolean isFilished) throws Exception {
		// TODO Auto-generated method stub

		String sqlstr = "SELECT * FROM hr_salary_chgbill WHERE scatype=3 AND stype=5 AND sid=" + ((Hr_entry_prob) jpa).pbtid.getValue();
		Hr_salary_chgbill cb = new Hr_salary_chgbill();
		cb.findBySQL(con, sqlstr, false);
		if (cb.isEmpty())
			throw new Exception("薪资结构不能为空");

		HRUtil.issalary_quotacode_used(1, ((Hr_entry_prob) jpa).salary_quotacode.getValue(), ((Hr_entry_prob) jpa).pbtid.getValue());
		if (isFilished){
			doepend(jpa, con);
		}
		
	}
	/*
	 * 更新调薪生效月份
	 */
	private void updateSalarydate(CJPA jpa, CDBConnection con) throws Exception {
		Hr_entry_prob tp = (Hr_entry_prob) jpa;
		Date pbtdate=Systemdate.getDateByyyyy_mm_dd(tp.pbtdate.getValue());
		Calendar ca = Calendar.getInstance();
		ca.setTime(pbtdate);
		int pbmonth=ca.get(Calendar.MONTH);
		Calendar now = Calendar.getInstance();
		int nowmonth=now.get(Calendar.MONTH);
		if(pbmonth>=nowmonth){
			tp.salarydate.setValue(tp.pbtdate.getValue());
		}else if(pbmonth<nowmonth){
			tp.salarydate.setValue(Systemdate.getStrDateyyyy_mm_dd());
		}
		tp.save();
	}

	private void doepend(CJPA jpa, CDBConnection con) throws Exception {
		Hr_entry_prob ep = (Hr_entry_prob) jpa;
		Hr_employee emp = new Hr_employee();
		Hr_employeestat estat = new Hr_employeestat();
		Hr_entry entry = new Hr_entry();
		Hr_entry_try et = new Hr_entry_try();

		emp.findByID4Update(con, ep.er_id.getValue(), false);
		if (emp.isEmpty())
			throw new Exception("没有找到id为【" + ep.er_id.getValue() + "】的员工资料");

		estat.findBySQL("select * from hr_employeestat where statvalue=" + emp.empstatid.getValue());

		entry.findByID4Update(con, ep.sourceid.getValue(), false);
		et.findBySQL4Update(con, "select * from hr_entry_try where er_id=" + ep.er_id.getValue(), false);
		int olddelaytimes = et.delaytimes.getAsIntDefault(0);

		int pbttype1 = ep.pbttype1.getAsInt();// 转正类型 实习转正 入职转正 调动转正
		if (pbttype1 == 1) {
			if (estat.issxenprob.getAsInt() != 1) {
				throw new Exception("状态为【" + estat.language1.getValue() + "】的员工【" + emp.employee_name.getValue() + "】不允许【实习生转正操作】");
			}
		} else if (pbttype1 == 2) {
			if (estat.isptemprob.getAsInt() != 1) {
				throw new Exception("状态为【" + estat.language1.getValue() + "】的员工【" + emp.employee_name.getValue() + "】不允许【转正操作】");
			}
		} else if (pbttype1 == 3) {
			if (estat.isjxprob.getAsInt() != 1) {
				throw new Exception("状态为【" + estat.language1.getValue() + "】的员工【" + emp.employee_name.getValue() + "】不允许【考察期转正操作】");
			}
		} else
			throw new Exception("【" + emp.employee_name.getValue() + "】转正类型错误【" + pbttype1 + "】");
		if (ep.wfresult.isEmpty())
			throw new Exception("需要审批意见");
		int wfresult = ep.wfresult.getAsInt(); // 1 同意转正 2 延长试用 3 试用不合格
		if (wfresult == 1) {// 1 同意转正
			emp.empstatid.setAsInt(4);// 正式
			emp.promotionday.setAsDatetime(ep.pbtdate.getAsDatetime());
			entry.ispromotioned.setAsInt(1);// 已经转正
			if (!et.isEmpty()) {
				if (et.delaytimes.getAsIntDefault(0) > 0) {
					et.delaypromotiondaytrue.setAsDatetime(ep.pbtdate.getAsDatetime());// 延长过试用期转正时间
				} else {
					et.promotiondaytrue.setAsDatetime(ep.pbtdate.getAsDatetime());// 没延长过试用期转正时间
				}
				et.trystat.setAsInt(4);// 1试用期中、2试用过期、3试用延期、4己转正、5试用不合格
			}
			CtrSalaryCommon.newSalaryChangeLog(con, ep);
		} else if (wfresult == 2) {// 2 延长试用
			entry.promotionday.setValue(ep.wfpbtdate.getValue());
			if (!et.isEmpty()) {
				et.delaypromotionday.setValue(ep.wfpbtdate.getValue());
				et.delaytimes.setAsInt(et.delaytimes.getAsIntDefault(0) + 1);
				et.trystat.setAsInt(3);// 1试用期中、2试用过期、3试用延期、4己转正、5试用不合格
				et.delayprobation.setAsInt(et.delayprobation.getAsIntDefault(0) + ep.wfpbprobation.getAsIntDefault(0));
			}
		} else if (wfresult == 3) {// 3 试用不合格
			// 是否创建离职表单 并提交流程
			if (!et.isEmpty()) {
				et.trystat.setAsInt(5);// 1试用期中、2试用过期、3试用延期、4己转正、5试用不合格
			}
		} else
			throw new Exception("评审结果错误");

		if (!et.isEmpty()) {
			if (olddelaytimes > 0) {
				et.delaywfresult.setAsInt(wfresult);// 延长过试用期 审批结果
			} else {
				et.wfresult.setAsInt(wfresult);// 没延长过试用期 审批结果
			}
		}
		et.save(con, false);
		emp.save(con, false);
		entry.save(con, false);
	}

}
