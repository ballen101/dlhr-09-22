package com.hr.attd.co;

import java.io.File;
import java.util.ArrayList;
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
import com.corsair.server.util.UpLoadFileEx;
import com.hr.attd.ctr.HrkqUtil;
import com.hr.attd.entity.Hr_kq_makeup_monthsubmit_line;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_month_employee;
import com.hr.util.HRUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * 补发出勤数据提报
 */
@ACO(coname = "web.hrkq.makeupmonthsubmit")
public class CO_Hr_kq_makeup_monthsubmit {
	@ACOAction(eventname = "getSubmitKqMonth", Authentication = true, ispublic = true, notes = "获取指定机构的月考勤记录")
	public String getSubmitKqMonth() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String orgid = CorUtil.hashMap2Str(parms, "orgid", "orgid参数不能为空");
		String ym = CorUtil.hashMap2Str(parms, "submitdate", "submitdate参数不能为空");
		Shworg org = new Shworg();
		org.findByID(orgid);
		if (org.isEmpty())
			throw new Exception("id为【" + orgid + "】的机构不存在");

		String	sqlstr = "select '1' as type, er_id,er_code,employee_code,employee_name,orgid,orgcode,orgname,"+
				"lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name"+
				" from hr_month_employee WHERE yearmonth='"+ym+"'  and  idpath like '"+org.idpath.getValue()+"%'"+
				" union"+
				" select '2' as type, er_id,er_code,employee_code,employee_name,orgid,orgcode,orgname,"+
				"lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name"+
				" from hr_employee where idpath like '"+org.idpath.getValue()+"%' order by type";
		String[] ignParms = { "orgcode" };
		JSONObject rst =new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
		List<String>erIds=new ArrayList<String>();
		CJPALineData<Hr_kq_makeup_monthsubmit_line> tsls = new CJPALineData<Hr_kq_makeup_monthsubmit_line>(Hr_kq_makeup_monthsubmit_line.class);
		for (int i = 0; i < emps.size(); i++) {
			JSONObject jo = emps.getJSONObject(i);
			Hr_kq_makeup_monthsubmit_line kqmsl = new Hr_kq_makeup_monthsubmit_line();
			boolean flag=false;
			if("1".equals(jo.getString("type"))){
				flag=true;
			}else if("2".equals(jo.getString("type")) &&  !erIds.contains(jo.getString("er_id"))){
				flag=true;
			}
			if(flag){
				kqmsl.er_id.setValue(jo.getString("er_id"));
				kqmsl.employee_code.setValue(jo.getString("employee_code"));
				kqmsl.employee_name.setValue(jo.getString("employee_name"));
				kqmsl.ospid.setValue(jo.getString("ospid"));
				kqmsl.ospcode.setValue(jo.getString("ospcode"));
				kqmsl.sp_name.setValue(jo.getString("sp_name"));
				kqmsl.orgid.setValue(jo.getString("orgid"));
				kqmsl.orgcode.setValue(jo.getString("orgcode"));
				kqmsl.orgname.setValue(jo.getString("orgname"));
				kqmsl.lv_id.setValue(jo.getString("lv_id"));
				kqmsl.lv_num.setValue(jo.getString("lv_num"));
				erIds.add(jo.getString("er_id"));
				tsls.add(kqmsl);
			}
		}
		return tsls.tojson();
	}
	/**
	 * 补发出勤明细
	 * @return
	 * @throws Exception
	 */
	@ACOAction(eventname = "rpt_hrkq_makeup_monthsubmit", Authentication = true, notes = "获取月考勤提报明细")
	public String rpt_hrkq_makeup_monthsubmit() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String parms = urlparms.get("parms");
		List<JSONParm> jps = CJSON.getParms(parms);
		List<JSONParm> orgnameparam = CorUtil.getJSONParms(jps, "orgname");
		List<JSONParm> submitdateparam = CorUtil.getJSONParms(jps, "submitdate");
		if (orgnameparam.size() <= 0)
			throw new Exception("机构不能为空");
		String orgname = orgnameparam.get(0).getParmvalue();
		if(orgname.equals("")){
			throw new Exception("机构不能为空");
		}	
		if (submitdateparam.size() <= 0)
			throw new Exception("提报年月不能为空");
		String ym = submitdateparam.get(0).getParmvalue();
		if(ym.equals(""))
			throw new Exception("提报年月不能为空");
		Shworg org = new Shworg();
		String sqlstr = "select * from shworg where code='" + orgname + "'";
		org.findBySQL(sqlstr);
		String[] notnull = {};
		sqlstr = "SELECT l.*,h.submitdate,h.orgname as horgname from Hr_kq_makeup_monthsubmit_line l inner join Hr_kq_makeup_monthsubmit h on l.mmkq_id=h.mmkq_id where h.idpath like '"
				+ org.idpath.getValue() + "%' and   submitdate='"+ym+"-01' and stat='9'";
		String[] ignParms = { "orgname","submitdate" };// 忽略的查询条件
		return new CReport(HRUtil.getReadPool(), sqlstr, null, notnull).findReport(ignParms);
	}

	@ACOAction(eventname = "impchgsubmit_listexcel", Authentication = true, ispublic = false, notes = "导入月考勤提报明细Excel")
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
		String	sqlstr = "select '1' as type, er_id,er_code,employee_code,employee_name,orgid,orgcode,orgname,"+
				"lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name"+
				" from hr_month_employee WHERE yearmonth='"+ym+"'  and  idpath like '"+org.idpath.getValue()+"%'"+
				" union"+
				" select '2' as type, er_id,er_code,employee_code,employee_name,orgid,orgcode,orgname,"+
				"lv_id,lv_num,hg_id,hg_code,hg_name,ospid,ospcode,sp_name"+
				" from hr_employee where idpath like '"+org.idpath.getValue()+"%' order by type";
		String[] ignParms = { "orgcode"};
		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
		List<String>erIds=new ArrayList<String>();
		CJPALineData<Hr_kq_makeup_monthsubmit_line> tsls = new CJPALineData<Hr_kq_makeup_monthsubmit_line>(Hr_kq_makeup_monthsubmit_line.class);
		for (Map<String, String> v : values) {
			String employee_code = v.get("employee_code");
			if ((employee_code == null) || (employee_code.isEmpty()))
				throw new Exception("明细行上的工号不能为空");
			for (int i = 0; i < emps.size(); i++) {
				JSONObject jo = emps.getJSONObject(i);
				if(employee_code.equals(jo.getString("employee_code"))){
					//有效数据添加
					boolean flag=false;
					if("1".equals(jo.getString("type"))){
						flag=true;
					}else if("2".equals(jo.getString("type")) &&  !erIds.contains(jo.getString("er_id"))){
						flag=true;
					}
					if(flag){
						Hr_kq_makeup_monthsubmit_line kqmsl = new Hr_kq_makeup_monthsubmit_line();
						kqmsl.er_id.setValue(jo.getString("er_id"));
						kqmsl.employee_code.setValue(jo.getString("employee_code"));
						kqmsl.employee_name.setValue(jo.getString("employee_name"));
						kqmsl.ospid.setValue(jo.getString("ospid"));
						kqmsl.ospcode.setValue(jo.getString("ospcode"));
						kqmsl.sp_name.setValue(jo.getString("sp_name"));
						//kqmsl.idpath.setValue(jo.getString("idpath"));
						kqmsl.orgid.setValue(jo.getString("orgid"));
						kqmsl.orgcode.setValue(jo.getString("orgcode"));
						kqmsl.orgname.setValue(jo.getString("orgname"));
						kqmsl.lv_id.setValue(jo.getString("lv_id"));
						kqmsl.lv_num.setValue(jo.getString("lv_num"));
						kqmsl.bcqts.setValue(v.get("bcqts"));
						kqmsl.khcqts.setValue(v.get("khcqts"));
						kqmsl.bpsjb.setValue(v.get("bpsjb"));
						kqmsl.bxxsjjb.setValue(v.get("bxxsjjb"));
						kqmsl.yxj.setValue(v.get("yxj"));
						kqmsl.yxcj.setValue(v.get("yxcj"));
						kqmsl.bj.setValue(v.get("bj"));
						kqmsl.fdjrjb.setValue(v.get("fdjrjb"));
						kqmsl.bztcs.setValue(v.get("bztcs"));
						kqmsl.bhkcdcqcs.setValue(v.get("bhkcdcqcs"));
						kqmsl.bfkcdkgts.setValue(v.get("bfkcdkgts"));
						kqmsl.bfjbf.setValue(v.get("bfjbf"));
						kqmsl.kccqts.setValue(v.get("kccqts"));
						kqmsl.kccqcs.setValue(v.get("kccqcs"));
						kqmsl.kcpsjbss.setValue(v.get("kcpsjbss"));
						kqmsl.kcxxsjjb.setValue(v.get("kcxxsjjb"));
						kqmsl.bffwfl.setValue(v.get("bffwfl"));
						kqmsl.fdyxjts.setValue(v.get("fdyxjts"));
						kqmsl.remark.setValue(v.get("remark"));
						tsls.add(kqmsl);
					}
				}
			}

			//emp.clear();
			//emp.findBySQL("SELECT * FROM `hr_employee` WHERE employee_code='" + employee_code + "'");
			//if (emp.isEmpty())
			//	throw new Exception("工号【" + employee_code + "】不存在人事资料");
			//emporg.clear();
			//emporg.findByID(emp.orgid.getValue());
			//if (emporg.isEmpty())
			//	throw new Exception("没找到员工【" + emp.employee_name.getValue() + "】所属的ID为【" + emp.orgid.getValue() + "】的机构");
			//if(!allEmpIds.contains(emp.er_id.getValue()))continue;

		}
		//运算记录
		return tsls.tojson();

	}

	private List<CExcelField> initExcelFields_chgsubmitlist() {
		List<CExcelField> efields = new ArrayList<CExcelField>();
		efields.add(new CExcelField("工号", "employee_code", true));
		efields.add(new CExcelField("姓名", "employee_name", true));
		efields.add(new CExcelField("补出勤天数", "bcqts", true));
		efields.add(new CExcelField("扣回出勤天数", "khcqts", true));
		efields.add(new CExcelField("补平时加班（H）", "bpsjb", true));
		efields.add(new CExcelField("补休息时间加班（H）", "bxxsjjb", true));
		efields.add(new CExcelField("有薪假", "yxj", true));
		efields.add(new CExcelField("有薪产假", "yxcj", true));
		efields.add(new CExcelField("病假", "bj", true));
		efields.add(new CExcelField("法定假日加班（H）", "fdjrjb", true));
		efields.add(new CExcelField("补早迟次数", "bztcs", true));
		efields.add(new CExcelField("补回扣除的超签次数", "bhkcdcqcs", true));
		efields.add(new CExcelField("补发扣除的旷工天数", "bfkcdkgts", true));
		efields.add(new CExcelField("补发加班费（元）", "bfjbf", true));
		efields.add(new CExcelField("扣除出勤天数", "kccqts", true));
		efields.add(new CExcelField("扣除超签次数", "kccqcs", true));
		efields.add(new CExcelField("扣除平时加班时数（H）", "kcpsjbss", true));
		efields.add(new CExcelField("扣除休息时间加班（H）", "kcxxsjjb", true));
		efields.add(new CExcelField("补发法务福利（元）", "bffwfl", true));
		efields.add(new CExcelField("法定有薪假天数", "fdyxjts", true));
		efields.add(new CExcelField("备注", "remark", false));
		return efields;
	}

	/**
	 * 查询某月份在职员工
	 * @param idpath 部门IDpath
	 * @param ym 月份
	 * @return 在职员工ID
	 * @throws Exception
	 */
	//	public static List<String> findEmployeeByZz(String idpath,String ym) throws Exception{
	//		List<String>erIdList=new ArrayList<>();
	//		String strSql="select er_id from hr_month_employee  where  yearmonth='"+ym+"'  and  idpath like '"+idpath+"%'"+
	//				" UNION select er_id from hr_employee where idpath like '"+idpath+"%'  AND DATE_FORMAT(kqdate_end,'%Y-%m')='"+ym+"'";
	//		List<HashMap<String, String>> List = HRUtil.getReadPool().openSql2List(strSql);
	//		for(HashMap<String, String>emp : List){
	//			erIdList.add(emp.get("er_id"));
	//		}
	//		return erIdList;
	//	}

}
