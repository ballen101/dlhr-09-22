package com.hr.attd.co;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.corsair.cjpa.CJPALineData;
import com.corsair.server.base.CSContext;
import com.corsair.server.generic.Shworg;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CorUtil;
import com.hr.attd.entity.Hr_kq_monthlyattendance_line;
import com.hr.util.HRUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//特殊月考勤数据明细主表
@ACO(coname = "web.hrkq.monthlyattendance")
public class CO_Hr_kq_monthlyattendance {
	@ACOAction(eventname = "getSubmitKqMonth", Authentication = true, ispublic = true, notes = "获取指定机构的月考勤记录")
	public String getSubmitKqMonth() throws Exception{
		HashMap<String, String> parms = CSContext.getParms();
		String orgid = CorUtil.hashMap2Str(parms, "orgid", "orgid参数不能为空");
		String ym = CorUtil.hashMap2Str(parms, "submitdate", "submitdate参数不能为空");
		Shworg org = new Shworg();
		org.findByID(orgid);
		if (org.isEmpty())
			throw new Exception("id为【" + orgid + "】的机构不存在");
		String	sqlstr = "select '1' as type, er_id,er_code,employee_code,employee_name,orgid,orgcode,orgname,"+
				"lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name,hiredday,ljdate"+
				" from hr_month_employee WHERE yearmonth='"+ym+"'  and  idpath like '"+org.idpath.getValue()+"%'"+
				" union"+
				" select '2' as type, er_id,er_code,employee_code,employee_name,orgid,orgcode,orgname,"+
				"lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name,hiredday,ljdate"+
				" from hr_employee where idpath like '"+org.idpath.getValue()+"%' order by type";
		String[] ignParms = { "orgcode" };
		JSONObject rst =new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
		List<String>erIds=new ArrayList<String>();
		CJPALineData<Hr_kq_monthlyattendance_line> tsls=new CJPALineData<Hr_kq_monthlyattendance_line>(Hr_kq_monthlyattendance_line.class);
		for (int i = 0; i < emps.size(); i++) {
			JSONObject jo = emps.getJSONObject(i);
			Hr_kq_monthlyattendance_line kqmal=new Hr_kq_monthlyattendance_line();
			boolean flag=false;
			if("1".equals(jo.getString("type"))){
				flag=true;
			}else if("2".equals(jo.getString("type")) &&  !erIds.contains(jo.getString("er_id"))){
				flag=true;
			}
			if(flag){
				kqmal.er_id.setValue(jo.getString("er_id"));
				kqmal.employee_code.setValue(jo.getString("employee_code"));
				kqmal.employee_name.setValue(jo.getString("employee_name"));
				kqmal.ospid.setValue(jo.getString("ospid"));
				kqmal.ospcode.setValue(jo.getString("ospcode"));
				kqmal.sp_name.setValue(jo.getString("sp_name"));
				kqmal.orgid.setValue(jo.getString("orgid"));
				kqmal.orgcode.setValue(jo.getString("orgcode"));
				kqmal.orgname.setValue(jo.getString("orgname"));
				kqmal.lv_id.setValue(jo.getString("lv_id"));
				kqmal.lv_num.setValue(jo.getString("lv_num"));
				kqmal.hiredday.setValue(jo.getString("hiredday"));		
				if(jo.containsValue("ljdate")){
					kqmal.ljdate.setValue(jo.getString("ljdate"));
				}
				erIds.add(jo.getString("er_id"));
				tsls.add(kqmal);
			}
		}
		return tsls.tojson();
	}
}
