package com.hr.attd.co;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.dbpool.util.CJSON;
import com.corsair.dbpool.util.JSONParm;
import com.corsair.dbpool.util.Logsw;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.base.ConstsSw;
import com.corsair.server.generic.Shw_physic_file;
import com.corsair.server.generic.Shworg;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CExcelField;
import com.corsair.server.util.CExcelUtil;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CorUtil;
import com.corsair.server.util.DictionaryTemp;
import com.corsair.server.util.UpLoadFileEx;
//import com.hr.attd.ctr.CtrHr_kq_monthlyattendance;
import com.hr.attd.ctr.HrkqUtil;
import com.hr.attd.entity.Hr_kq_makeup_monthsubmit_line;
import com.hr.attd.entity.Hr_kq_monthlyattendance_line;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_leavejob;
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
		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(ym)));// 去除时分秒
		Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
		Shworg org = new Shworg();
		org.findByID(orgid);
		if (org.isEmpty())
			throw new Exception("id为【" + orgid + "】的机构不存在");

		String sqlstr = "select a.er_id,a.employee_code,a.employee_name,a.ospid,a.ospcode,a.sp_name,a.orgid,a.orgcode,a.orgname,a.lv_id,a.lv_num,a.hiredday,a.ljdate"+
				" from hr_month_employee a INNER JOIN hr_employee  b on a.employee_code=b.employee_code WHERE a.yearmonth='"+ym+"'  and  a.idpath like '"+org.idpath.getValue()+"%' and  a.er_id in (select er_id from hrkq_workschmonthlist where yearmonth='"+ym+"')"+
				" union"+
				" select er_id,employee_code,employee_name,ospid,ospcode,sp_name,orgid,orgcode,orgname,lv_id,lv_num,hiredday,ljdate"+
				" from hr_employee where idpath like '"+org.idpath.getValue()+"%'  AND DATE_FORMAT(kqdate_end,'%Y-%m')='"+ym+"' and  er_id in (select er_id from hrkq_workschmonthlist where yearmonth='"+ym+"')";
		String[] ignParms = { "orgcode"};

		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
//		for (int i = 0; i < emps.size(); i++) {
//			JSONObject jo = emps.getJSONObject(i);
//			rptlvinfo(jo,bgdate, eddate);// 离职类型
//		}
		JSONObject rows=new JSONObject();
		rows.put("rows", calcKq1(ym,emps,null));
		return rows.toString();

	}

//	//	查询离职类别
//	private static void rptlvinfo(JSONObject jo,Date bgdate, Date eddate) throws Exception {
//		//String sqlstr = "SELECT * FROM hr_leavejob WHERE stat=9 AND  er_id=" + jo.getString("er_id");
//		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
//		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
//		String sqlstr="SELECT ljtype2,ljtype1,ljreason FROM hr_leavejob l,hr_employee h WHERE stat=9 and l.er_id='"+jo.getString("er_id")+"' and l.er_id=h.er_id and h.kqdate_end is NOT NULL and h.ljdate>='"+bs+"' and h.ljdate<='"+es+"'";
//		Hr_leavejob lv = new Hr_leavejob();
//		lv.findBySQL(sqlstr);
//		if (!lv.isEmpty()) {
//			jo.put("ljtype2", lv.ljtype2.getValue());
//			jo.put("ljtype1", lv.ljtype1.getValue());
//			jo.put("ljreason", lv.ljreason.getValue());
//		}else{
//			jo.put("ljtype2","");
//			jo.put("ljtype1","");
//			jo.put("ljreason", "");
//		}
//	}
	/*
	 * 特殊月考勤提报明细查询
	 */
	@ACOAction(eventname = "rpt_hrkq_monthlyattendance", Authentication = true, notes = "获取月考勤提报明细")
	public String rpt_hrkq_monthsubmit() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String parms = urlparms.get("parms");
		List<JSONParm> jps = CJSON.getParms(parms);
		List<JSONParm> orgnameparam = CorUtil.getJSONParms(jps, "orgname");
		List<JSONParm> submitdateparam = CorUtil.getJSONParms(jps, "submitdate");
		if (orgnameparam.size() <= 0)
			throw new Exception("机构不能为空");
		String orgname = orgnameparam.get(0).getParmvalue();
		if(orgname.equals(""))throw new Exception("机构不能为空");
		if (submitdateparam.size() <= 0)
			throw new Exception("提报年月不能为空");
		String ym = submitdateparam.get(0).getParmvalue();
		if(ym.equals(""))throw new Exception("提报年月不能为空");
		Shworg org = new Shworg();
		String sqlstr = "select * from shworg where code='" + orgname + "'";
		org.findBySQL(sqlstr);
		String[] notnull = {};
		sqlstr = "SELECT l.*,h.submitdate,h.orgname as horgname from Hr_kq_monthsubmit_line l inner join Hr_kq_monthsubmit h on l.mkq_id=h.mkq_id where h.idpath like '"
				+ org.idpath.getValue() + "%' and submitdate='"+ym+"-01' and stat='9'";
		String[] ignParms = { "orgname","submitdate" };// 忽略的查询条件
		return new CReport(HRUtil.getReadPool(), sqlstr, null, notnull).findReport(ignParms);
	}
	/**
	 * 数据导入
	 * @return
	 * @throws Exception
	 */
	@ACOAction(eventname = "impchgsubmit_listexcel", Authentication = true, ispublic = false, notes = "导入特殊月考勤提报明细Excel")
	public String impchgsubmit_listexcel() throws Exception {
		if (!CSContext.isMultipartContent())
			throw new Exception("没有文件");
		String submitdate = CorUtil.hashMap2Str(CSContext.getParms(), "submitdate", "需要参数submitdate");
		CJPALineData<Shw_physic_file> pfs = UpLoadFileEx.doupload(false);
		if (pfs.size() > 0) {
			Shw_physic_file p = (Shw_physic_file) pfs.get(0);
			String rst = parserExcelFile_chgsubmitlist(p,submitdate);
			for (CJPABase pfb : pfs) {
				Shw_physic_file pf = (Shw_physic_file) pfb;
				UpLoadFileEx.delAttFile(pf.pfid.getValue());
			}
			return rst;
		} else {
			return "[]";
		}

	}
	private String parserExcelFile_chgsubmitlist(Shw_physic_file pf,String submitdate) throws Exception {
		String fs = System.getProperty("file.separator");
		String fullname = ConstsSw.geAppParmStr("UDFilePath") + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
		File file = new File(fullname);
		if (!file.exists()) {
			fullname = ConstsSw._root_filepath + "attifiles" + fs + pf.ppath.getValue() + fs + pf.pfname.getValue();
			file = new File(fullname);
			if (!file.exists())
				throw new Exception("文件" + fullname + "不存在!");
		}

		Workbook workbook = WorkbookFactory.create(file);
		int sn = workbook.getNumberOfSheets();
		if (sn <= 0)
			throw new Exception("excel<" + fullname + ">没有sheet");
		Sheet aSheet = workbook.getSheetAt(0);// 获得一个sheet
		return parserExcelSheet_chgsubmitlist(aSheet,submitdate);
	}

	private JSONArray calcKq1(String ym,JSONArray emps,List<Map<String, String>>values) throws Exception{
		DictionaryTemp dictemp = new DictionaryTemp();// 数据字典缓存
		JSONArray datalist=new JSONArray();
		for (int i = 0; i < emps.size(); i++) {
			System.out.print("emps="+emps.size());
			JSONObject jo = emps.getJSONObject(i);
			JSONObject bean= new JSONObject();
			bean.put("er_id", jo.get("er_id"));
			bean.put("employee_code", jo.get("employee_code"));
			bean.put("employee_name", jo.get("employee_name"));
			bean.put("idpath", jo.get("idpath"));
			bean.put("orgid", jo.get("orgid"));
			bean.put("orgcode", jo.get("orgcode"));
			bean.put("orgname", jo.get("orgname"));
			bean.put("sp_name", jo.get("sp_name"));
			bean.put("lv_num", jo.get("lv_num"));
			bean.put("hiredday", jo.get("hiredday"));
			bean.put("ljdate", jo.get("ljdate"));
			
//			bean.put("ljtype", jo.get("ljtype1"));
			HashMap<String, String> parms = CSContext.getParms();
			String orgid = CorUtil.hashMap2Str(parms, "orgid", "orgid参数不能为空");
			String ym1 = CorUtil.hashMap2Str(parms, "submitdate", "submitdate参数不能为空");
			Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(ym1)));// 去除时分秒
			Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
//			rptlvinfo(jo,bgdate, eddate);
//			String sqlstr="SELECT ljtype2,ljtype1,ljreason FROM hr_leavejob l,hr_employee h WHERE stat=9 and l.er_id='"+jo.getString("er_id")+"' and l.er_id=h.er_id and h.kqdate_end is NOT NULL and h.ljdate>='"+bgdate+"' and h.ljdate<='"+eddate+"'";
			String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
			String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
			String sqlstr="SELECT ljtype2,ljtype1,ljreason FROM hr_leavejob l,hr_employee h WHERE stat=9 and l.er_id='"+jo.getString("er_id")+"' and l.er_id=h.er_id and h.kqdate_end is NOT NULL and h.ljdate>='"+bs+"' and h.ljdate<='"+es+"'" ;
			Hr_leavejob lv = new Hr_leavejob();
			lv.findBySQL(sqlstr);
			
			if (!lv.isEmpty()) {
//				bean.put("ljtype1", jo.get("ljtype1"));
				if(!lv.ljreason.getValue().isEmpty()){
					bean.put("ljtype", lv.ljreason.getValue());
				}
				if(!lv.ljtype2.getValue().isEmpty()){
					bean.put("ljtype", lv.ljtype2.getValue());
				}
				if(!lv.ljtype1.getValue().isEmpty()){
					bean.put("ljtype", lv.ljtype1.getValue());
				}
							
			}else{
				bean.put("ljtype","");
			}
			datalist.add(bean);
			//查询
		}
		//重排
		JSONArray datalistOrderBy=new JSONArray();
		if(values!=null){
			for (Map<String, String> v : values) {
				for (int i = 0; i < datalist.size(); i++) {
					JSONObject jo = datalist.getJSONObject(i);
					if(jo.get("employee_code").equals(v.get("employee_code"))){
						datalistOrderBy.add(jo);
					}
				}
			}
		}else{
			datalistOrderBy=datalist;
		}
		return datalistOrderBy;
	}
	private String parserExcelSheet_chgsubmitlist(Sheet aSheet,String submitdate) throws Exception {
		if (aSheet.getLastRowNum() == 0) {
			return "[]";
		}
		List<CExcelField> efds = initExcelFields_chgsubmitlist();
		efds = CExcelUtil.parserExcelSheetFields(aSheet, efds, 0);// 解析title 并检查必须存在的列
		List<Map<String, String>> values = CExcelUtil.getExcelValues(aSheet, efds, 0);
		HashMap<String, String> parms = CSContext.getParms();
		String orgid = CorUtil.hashMap2Str(parms, "orgid", "orgid参数不能为空");
		String ym = CorUtil.hashMap2Str(parms, "submitdate", "submitdate参数不能为空");
		Shworg org = new Shworg();
		org.findByID(orgid);
		if (org.isEmpty())
			throw new Exception("id为【" + orgid + "】的机构不存在");
		Hr_employee emp = new Hr_employee();
		Shworg emporg = new Shworg();
		List<String> empIdsList=new ArrayList<String>();
		for (Map<String, String> v : values) {
			String employee_code = v.get("employee_code");
			if ((employee_code == null) || (employee_code.isEmpty()))
				throw new Exception("明细行上的工号不能为空");
			emp.clear();
			emp.findBySQL("SELECT * FROM `hr_employee` WHERE employee_code='" + employee_code + "'");
			if (emp.isEmpty())
				throw new Exception("工号【" + employee_code + "】不存在人事资料");
			emporg.clear();
			emporg.findByID(emp.orgid.getValue());
			if (emporg.isEmpty())
				throw new Exception("没找到员工【" + emp.employee_name.getValue() + "】所属的ID为【" + emp.orgid.getValue() + "】的机构");
			empIdsList.add(emp.er_id.getValue());
		}
		String empIdsString="";
		if(empIdsList!=null && empIdsList.size()>0){
			empIdsString=" and a.er_id in ("+HRUtil.tranInSql(empIdsList)+")";
		}
		String sqlstr = "select a.er_id,a.employee_code,a.employee_name,a.orgname,a.sp_name,a.lv_id,a.lv_num,a.hiredday,a.ljdate"+
				" from hr_month_employee a INNER JOIN hr_employee  b on a.employee_code=b.employee_code WHERE a.yearmonth='"+ym+"'  and  a.idpath like '"+org.idpath.getValue()+"%' "+empIdsString+" and  a.er_id in (select er_id from hrkq_workschmonthlist where yearmonth='"+ym+"')"+
				" union"+
				" select er_id,employee_code,employee_name,orgname,sp_name,lv_id,lv_num,hiredday,ljdate"+
				" from hr_employee where idpath like '"+org.idpath.getValue()+"%'  AND DATE_FORMAT(kqdate_end,'%Y-%m')='"+ym+"' "+empIdsString.replace("a.", "")+" and  er_id in (select er_id from hrkq_workschmonthlist where yearmonth='"+ym+"')";
		String[] ignParms = { "orgcode"};
		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
		CJPALineData<Hr_kq_monthlyattendance_line> tsls = new CJPALineData<Hr_kq_monthlyattendance_line>(Hr_kq_monthlyattendance_line.class);
		for (Map<String, String> v : values) {
			String employee_code = v.get("employee_code");
			if ((employee_code == null) || (employee_code.isEmpty()))
				throw new Exception("明细行上的工号不能为空");
			for (int i = 0; i < emps.size(); i++) {
				JSONObject jo = emps.getJSONObject(i);
				if(employee_code.equals(jo.getString("employee_code"))){
					Hr_kq_monthlyattendance_line kqmsl = new Hr_kq_monthlyattendance_line();
					kqmsl.er_id.setValue(jo.getString("er_id"));
					kqmsl.employee_code.setValue(jo.getString("employee_code"));
					kqmsl.employee_name.setValue(jo.getString("employee_name"));

					kqmsl.sp_name.setValue(jo.getString("sp_name"));

					kqmsl.orgname.setValue(jo.getString("orgname"));
					kqmsl.lv_id.setValue(jo.getString("lv_id"));
					kqmsl.lv_num.setValue(jo.getString("lv_num"));
					kqmsl.hiredday.setValue(jo.getString("hiredday"));
					Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(ym)));// 去除时分秒
					Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
//					rptlvinfo(jo,bgdate, eddate);// 离职类型
					if(jo.has("ljdate")){
						kqmsl.ljdate.setValue(jo.getString("ljdate"));
						String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
						String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
						String sqlstr1="SELECT ljtype2,ljtype1,ljreason FROM hr_leavejob l,hr_employee h WHERE stat=9 and l.er_id='"+jo.getString("er_id")+"' and l.er_id=h.er_id and h.kqdate_end is NOT NULL and h.ljdate>='"+bs+"' and h.ljdate<='"+es+"'" ;
						Hr_leavejob lv = new Hr_leavejob();
						lv.findBySQL(sqlstr1);
						
						if (!lv.isEmpty()) {
							if(!lv.ljreason.getValue().isEmpty()){

								kqmsl.ljtype.setValue(lv.ljreason.getValue());
							}
							if(!lv.ljtype2.getValue().isEmpty()){

								kqmsl.ljtype.setValue(lv.ljtype2.getValue());
							}
							if(!lv.ljtype1.getValue().isEmpty()){

								kqmsl.ljtype.setValue(lv.ljtype1.getValue());
							}
										
						}else{
							jo.put("ljtype","");
						}
					}
					
					kqmsl.ycmq.setValue(v.get("ycmq"));
					kqmsl.sjcq.setValue(v.get("sjcq"));
					kqmsl.psjb.setValue(v.get("psjb"));			
					kqmsl.xxrjb.setValue(v.get("xxrjb"));
					kqmsl.fdjrjb.setValue(v.get("fdjrjb"));

					kqmsl.kgts.setValue(v.get("kgts"));
					kqmsl.cdztcs.setValue(v.get("cdztcs"));
					kqmsl.cqcs.setValue(v.get("cqcs"));
					kqmsl.bjts.setValue(v.get("bjts"));
					kqmsl.qjts.setValue(v.get("qjts"));
					kqmsl.yxts.setValue(v.get("yxts"));
					kqmsl.gzxsz.setValue(v.get("gzxsz"));					
					kqmsl.jxfs.setValue(v.get("jxfs"));
					kqmsl.tsts1.setValue(v.get("tsts1"));
					kqmsl.tsts2.setValue(v.get("tsts2"));
					kqmsl.remark.setValue(v.get("remark"));				
					tsls.add(kqmsl);

				}
			}

		}
		//运算记录
		return tsls.tojson();

	}
	private List<CExcelField> initExcelFields_chgsubmitlist() {
		List<CExcelField> efields = new ArrayList<CExcelField>();
		efields.add(new CExcelField("工号", "employee_code", true));
		efields.add(new CExcelField("姓名", "employee_name", true));
		efields.add(new CExcelField("应出满勤", "ycmq", true));
		efields.add(new CExcelField("实际出勤", "sjcq", true));
		efields.add(new CExcelField("平时加班", "psjb", true));
		efields.add(new CExcelField("休息日加班", "xxrjb", true));
		efields.add(new CExcelField("法定假日加班", "fdjrjb", true));
		efields.add(new CExcelField("旷工天数", "kgts", true));
		efields.add(new CExcelField("迟到早退次数", "cdztcs", true));
		efields.add(new CExcelField("超签次数", "cqcs", true));

		efields.add(new CExcelField("病假天数", "bjts", true));
		efields.add(new CExcelField("请假天数", "qjts", true));
		efields.add(new CExcelField("法定假日", "yxts", true));

		efields.add(new CExcelField("夜班天数", "ybts", true));
		efields.add(new CExcelField("计薪方式", "jxfs", true));
		efields.add(new CExcelField("特殊天数1", "tsts1", true));


		efields.add(new CExcelField("特殊天数2", "tsts2", true));
		efields.add(new CExcelField("备注", "remark", false));

		return efields;
	}


}
