package com.hr.salary.ctr;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONObject;

import com.corsair.cjpa.CJPABase;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.wordflow.Shwwf;
import com.corsair.server.wordflow.Shwwfproc;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_entry_prob;
import com.hr.perm.entity.Hr_transfer_prob;
import com.hr.salary.co.Cosacommon;
import com.hr.salary.entity.Hr_salary_chgbill;
import com.hr.salary.entity.Hr_salary_orgminstandard;
import com.hr.salary.entity.Hr_salary_positionwage;
import com.hr.salary.entity.Hr_salary_specchgsa;
import com.hr.salary.entity.Hr_salary_structure;
import com.hr.util.HRUtil;

public class CtrHr_salary_specchgsa extends JPAController{

	@Override
	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf,
			boolean isFilished) throws Exception {
		// TODO Auto-generated method stub
		if(isFilished){
			Hr_salary_specchgsa scs=(Hr_salary_specchgsa)jpa;
			CtrSalaryCommon.newSalaryChangeLog(con,scs);
		}
			
	}


	
	@Override
	public void OnWfSubmit(CJPA jpa, CDBConnection con, Shwwf wf,
			Shwwfproc proc, Shwwfproc nxtproc, boolean isFilished)
			throws Exception {
		// TODO Auto-generated method stub
		if(isFilished){
			Hr_salary_specchgsa scs=(Hr_salary_specchgsa)jpa;
			CtrSalaryCommon.newSalaryChangeLog(con,scs);
			CtrSalaryCommon.updateWageState(scs.salary_quotacode.getValue(),scs.employee_code.getValue());
			
		}
	}

	@Override
	public String AfterCCoSave(CDBConnection con, CJPA jpa) throws Exception {
		// TODO Auto-generated method stub
		Hr_salary_specchgsa scs=(Hr_salary_specchgsa)jpa;
		HashMap<String, String> pparms = CSContext.parPostDataParms();
		String jpadata = pparms.get("jpadata");
		if ((jpadata == null) || jpadata.isEmpty())
			throw new Exception("需要jpadata参数");
		JSONObject jo = JSONObject.fromObject(jpadata);
		Cosacommon.save_salary_chgblill(con, scs, jo);
		return null;
	}


	@Override
	public void BeforeWFStart(CJPA jpa, String wftempid, CDBConnection con)
			throws Exception {
		// TODO Auto-generated method stub
		Hr_salary_specchgsa scs=(Hr_salary_specchgsa)jpa;
		Hr_employee emp = new Hr_employee();
		Hr_salary_chgbill cb = new Hr_salary_chgbill();
		emp.findByID(scs.er_id.getValue());
		cb.findBySQL("SELECT * FROM `hr_salary_chgbill` WHERE stype=7 AND er_id="+emp.er_id.getValue()+" AND sid="+scs.scsid.getValue());
		float np=cb.newposition_salary.getAsFloatDefault(0);
		float op=cb.oldposition_salary.getAsFloatDefault(0);
		float chgpos=np-op;
		if(emp.emnature.getValue().equals("脱产")&&chgpos>0){
			if(scs.salary_quotacode.isEmpty()){
				throw new Exception("脱产人员调薪额度不为0时，额度编号不能为空");
			}
		}
		HRUtil.issalary_quotacode_used(4, scs.salary_quotacode.getValue(), scs.scsid.getValue());
	}

	@Override
	public void BeforeSave(CJPABase jpa, CDBConnection con, boolean selfLink)
			throws Exception {
		Hr_salary_specchgsa scs=(Hr_salary_specchgsa)jpa;
		if((((scs.newposition_salary.getAsFloatDefault(0)>scs.oldposition_salary.getAsFloatDefault(0))
				||(scs.newtech_allowance.getAsFloatDefault(0)>scs.oldtech_allowance.getAsFloatDefault(0)))
				&&scs.pbtsarylev.getAsFloatDefault(0)==0)||
				(((scs.newposition_salary.getAsFloatDefault(0)>scs.oldposition_salary.getAsFloatDefault(0)))
						&&scs.overf_salary.getAsFloatDefault(0)==0)||((scs.newbase_salary.getAsFloatDefault(0)+
								scs.newtech_salary.getAsFloatDefault(0)+scs.newachi_salary.getAsFloatDefault(0)+
								scs.newotwage.getAsFloatDefault(0)-scs.newposition_salary.getAsFloatDefault(0))!=0)){
			resetsalaryinfo(scs);
			//throw new Exception("薪资有调整但调整额度为0，请检查单据或清理浏览器缓存后再制单。（清理浏览器缓存方法：同时按 ctr+shift+delete 即可弹出删除缓存对话框。）");
		}
		Hr_employee emp = new Hr_employee();
		emp.findByID(scs.er_id.getValue());
		//String sacra=jo.getString("sacrage");
		System.out.println("----------保存前");
		float sac=scs.pbtsarylev.getAsFloatDefault(0);
		if(emp.emnature.getValue().equals("脱产")&&sac>0){
			if(scs.salary_quotacode.isEmpty()){
				throw new Exception("脱产人员调薪额度大于0,额度编号不能为空！");
			}
		}
		if(!scs.salary_quotacode.isEmpty()){
			if(scs.salary_quota_appy.getAsFloatDefault(0)==0){
				throw new Exception("申请额度为0，请核查！");
			}
		}
		scs.newcalsalarytype.setValue(emp.pay_way.getValue());
		scs.salarydate.setValue(Systemdate.getStrDateyyyy_mm_01());
	}
	
	private void resetsalaryinfo(Hr_salary_specchgsa scs) throws Exception{
		
		float np=scs.newposition_salary.getAsFloatDefault(0);
		float op=scs.oldposition_salary.getAsFloatDefault(0);
		if(((np>op)&scs.pbtsarylev.getAsFloatDefault(0)==0)||((scs.newbase_salary.getAsFloatDefault(0)+
				scs.newtech_salary.getAsFloatDefault(0)+scs.newachi_salary.getAsFloatDefault(0)+
				scs.newotwage.getAsFloatDefault(0)-np)!=0)){
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
					float newnp=scs.newbase_salary.getAsFloatDefault(0)+scs.newtech_salary.getAsFloatDefault(0)+scs.newachi_salary.getAsFloatDefault(0)+scs.newotwage.getAsFloatDefault(0);
					scs.newposition_salary.setAsFloat(newnp);
				}
				scs.chgposition_salary.setAsFloat(np-scs.oldposition_salary.getAsFloatDefault(0));
				scs.chgtech_allowance.setAsFloat(scs.newtech_allowance.getAsFloatDefault(0)-scs.oldtech_allowance.getAsFloatDefault(0));
				scs.pbtsarylev.setAsFloat(np+scs.newtech_allowance.getAsFloatDefault(0)-scs.oldposition_salary.getAsFloatDefault(0)-scs.oldtech_allowance.getAsFloatDefault(0));
				scs.sacrage.setAsFloat(np+scs.newtech_allowance.getAsFloatDefault(0)-scs.oldposition_salary.getAsFloatDefault(0)-scs.oldtech_allowance.getAsFloatDefault(0));
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

}
