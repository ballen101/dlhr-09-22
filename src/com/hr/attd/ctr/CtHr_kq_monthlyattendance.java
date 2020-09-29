package com.hr.attd.ctr;

import java.util.Date;
import java.util.List;

import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.ConstsSw;
import com.corsair.server.generic.Shworg;
import com.corsair.server.util.CReport;
import com.hr.perm.entity.Hr_leavejob;
import com.hr.util.HRUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CtHr_kq_monthlyattendance {
	/**
	 * 
	 * @param orgcode 部门编码
	 * @param dqdate  特殊考勤月
	 * @param empIds  要查询的员工
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getkqorgmonthrptdetail(String orgcode,String dqdate,List<String> empIdsList) throws Exception {
		boolean edk = ConstsSw.getSysParmIntDefault("ENTRYDAYKQ", 1) == 1;// 入职当天计算考勤 1是 2否
		boolean lvk = ConstsSw.getSysParmIntDefault("LEAVDAYKQ", 1) == 1;// 离职当天计算考勤 1是 2否
		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(dqdate)));// 去除时分秒
		Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
		String yearmonth = Systemdate.getStrDateByFmt(Systemdate.getDateByStr(dqdate), "yyyy-MM");
		String sqlstr = "select * from shworg where code='" + orgcode + "'";
		Shworg org = new Shworg();
		org.findBySQL(sqlstr);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");
		String empIdsString="";
		if(empIdsList!=null && empIdsList.size()>0){
			empIdsString=" and a.er_id in ("+HRUtil.tranInSql(empIdsList)+")";
		}
		sqlstr = "select a.er_id,a.employee_code,a.employee_name,a.ospid,a.ospcode,a.sp_name,a.orgid,a.orgcode,a.orgname,a.lv_id,a.lv_num,a.hiredday,a.ljdate"+
				" from hr_month_employee a INNER JOIN hr_employee  b on a.employee_code=b.employee_code WHERE a.yearmonth='"+yearmonth+"'  and  a.idpath like '"+org.idpath.getValue()+"%' "+empIdsString+" and  a.er_id in (select er_id from hrkq_workschmonthlist where yearmonth='"+yearmonth+"')"+
				" union"+
				" select er_id,employee_code,employee_name,ospid,ospcode,sp_name,orgid,orgcode,orgname,lv_id,lv_num,hiredday,ljdate"+
				" from hr_employee where idpath like '"+org.idpath.getValue()+"%'  AND DATE_FORMAT(kqdate_end,'%Y-%m')='"+yearmonth+"' "+empIdsString.replace("a.", "")+" and  er_id in (select er_id from hrkq_workschmonthlist where yearmonth='"+yearmonth+"')";
		String[] ignParms = { "orgcode"};
		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
		for (int i = 0; i < emps.size(); i++) {
			JSONObject jo = emps.getJSONObject(i);
			if(empIdsList!=null && empIdsList.size()>0){
				//筛选有效数据
				if(!empIdsList.contains(jo.getString("er_id")))continue;
			}
			rptlvinfo(jo,bgdate, eddate);// 离职类型
		}
		return rst;	
	}
	private static void rptlvinfo(JSONObject jo,Date bgdate, Date eddate) throws Exception {
		//String sqlstr = "SELECT * FROM hr_leavejob WHERE stat=9 AND  er_id=" + jo.getString("er_id");
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr="SELECT ljtype2,ljtype1,ljreason FROM hr_leavejob l,hr_employee h WHERE stat=9 and l.er_id='"+jo.getString("er_id")+"' and l.er_id=h.er_id and h.kqdate_end is NOT NULL and h.ljdate>='"+bs+"' and h.ljdate<='"+es+"'";
		Hr_leavejob lv = new Hr_leavejob();
		lv.findBySQL(sqlstr);
		if (!lv.isEmpty()) {
			jo.put("ljtype2", lv.ljtype2.getValue());
			jo.put("ljtype1", lv.ljtype1.getValue());
			jo.put("ljreason", lv.ljreason.getValue());
		}else{
			jo.put("ljtype2","");
			jo.put("ljtype1","");
			jo.put("ljreason", "");
		}
	}
	
}
