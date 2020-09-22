package com.hr.salary.co;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.corsair.cjpa.util.CjpaUtil;
import com.corsair.dbpool.util.CJSON;
import com.corsair.dbpool.util.JSONParm;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.generic.Shworg;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CorUtil;
import com.hr.base.entity.Hr_orgposition;
import com.hr.base.entity.Hr_standposition;
import com.hr.util.HRUtil;

@ACO(coname = "web.hr.sareport")
public class COHr_salary_analysis {
	 @ACOAction(eventname = "getAVGSalarys", Authentication = true, notes = "查询某职位的平均工资")
	    public String getAVGSalarys() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = HRUtil.getOrgsByOrgid(orgid, false, yms);
		Shworg org = new Shworg();
		org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");
		 Hr_standposition sp=new Hr_standposition();
		 sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }

		 JSONArray savg = new JSONArray();
		 for(String ym:yearmonths){
			 for(int i=0;i<dws.size();i++){
				 JSONObject dw = dws.getJSONObject(i);// 机构 年月
				 String sqlstr="SELECT  '"+ym+"' AS months,'"+sp.sp_name.getValue()+"' as spname,IFNULL(ROUND(AVG(poswage)),0) AS avgpos,IFNULL(SUM(poswage),0) AS ttpos,COUNT(*) AS nums "+
				 "FROM `hr_salary_list` cl WHERE cl.yearmonth='"+ym+"' AND cl.lv_num<5 and cl.wagetype=1  AND cl.ospid IN "+
				"(SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id)"+
				 " AND idpath LIKE '"+dw.getString("idpath")+"%'";
				 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
				 JSONObject tempob=tempar.getJSONObject(0);
				 dw.putAll(tempob);
			 }
			 savg.addAll(dws);
		 }
		JSONObject jo=new JSONObject();
		jo.put("rows", savg);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(savg, scols);
			return null;
		}
	 }
	 
	 @ACOAction(eventname = "getSalaryTrendAnalysis", Authentication = true, notes = "查询月薪人员薪资水平趋势分析")
	 public String getSalaryTrendAnalysis() throws Exception {
		 HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		//String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		String spname = CorUtil.getJSONParmValue(jps, "spname");
		String iswzjq_str = CorUtil.getJSONParmValue(jps, "findtype");
		int findtype = (iswzjq_str == null) ? 1 : Integer.valueOf(iswzjq_str);
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = getTrendOrgsByOrgid(orgid, yearmonths,findtype,spname);
		Shworg org = new Shworg();
		org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");
	/*	Hr_standposition sp=new Hr_standposition();
		sp.findByID(spid);
		if(sp.isEmpty()){
	   	  throw new Exception("id为【"+spid+"】的职位不存在！");
		}*/
		JSONArray savg = new JSONArray();
		 //for(String ym:yearmonths){
				for(int i = dws.size() - 1; i >= 0; i--){
					JSONObject dw = dws.getJSONObject(i);// 机构 年月
					dw.put("months", dw.getString("yearmonth"));
					//dw.put("spname", sp.sp_name.getValue());
					Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(dw.getString("yearmonth"))));// 去除时分秒
					Date ftdate=Systemdate.dateMonthAdd(bgdate, 1);
					String ymbg=Systemdate.getStrDateByFmt(bgdate, "yyyy-MM");
					String ymed=Systemdate.getStrDateByFmt(ftdate, "yyyy-MM");
					String sqlstr="SELECT poswage FROM `hr_salary_list` WHERE ospid IN (";
					if(findtype==1||findtype==2){
						sqlstr=sqlstr+"SELECT ospid FROM hr_orgposition WHERE sp_id = "+dw.getString("sp_id");
					}
					if(findtype==3){
						sqlstr=sqlstr+"SELECT op.ospid FROM `hr_standposition` sp,`hr_orgposition` op WHERE op.sp_id=sp.sp_id AND  sp.sp_name LIKE '%"+spname+"%' AND op.idpath LIKE '"+org.idpath.getValue()+"%'";
					}
					sqlstr=sqlstr+") AND wagetype=1 AND "+
						" idpath like '"+org.idpath.getValue()+"%' and"+
						" yearmonth>='"+ymbg+"' AND yearmonth<'"+ymed+"' ORDER BY poswage";
					JSONArray res= org.pool.opensql2json_O(sqlstr);
					if(res.size()==0){
						dws.remove(i);
					}
					double[] datas=new double[res.size()];
					if(res.size()>0){
						for(int j=0;j<res.size();j++){
							JSONObject row = res.getJSONObject(j);
							Double pw=Double.valueOf(row.getString("poswage"));
							datas[j]=pw;
						}
					}
					double pw10=COHr_salary.getPercentile(datas,0.1);
					double pw25=COHr_salary.getPercentile(datas,0.25);
					double pw50=COHr_salary.getPercentile(datas,0.5);
					double pw75=COHr_salary.getPercentile(datas,0.75);
					double pw90=COHr_salary.getPercentile(datas,0.9);
					 BigDecimal bpw10 = new BigDecimal(pw10);
					 BigDecimal bpw25 = new BigDecimal(pw25);
					 BigDecimal bpw50 = new BigDecimal(pw50);
					 BigDecimal bpw75 = new BigDecimal(pw75);
					 BigDecimal bpw90 = new BigDecimal(pw90);
					 int ipw10 = bpw10.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();  
					 int ipw25 = bpw25.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
					 int ipw50 = bpw50.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
					 int ipw75 = bpw75.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
					 int ipw90 = bpw90.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
					sqlstr="SELECT IFNULL(ROUND(AVG(poswage)),0) AS avgpos,IFNULL(COUNT(*),0) AS nums,IFNULL(ROUND(MAX(poswage)),0) as maxpos,IFNULL(ROUND(MIN(poswage)),0) as minpos,IFNULL('"+dw.getString("sp_name")+
						"','') AS pos FROM `hr_salary_list` WHERE ospid IN (";
					if(findtype==1||findtype==2){
						sqlstr=sqlstr+"SELECT ospid FROM hr_orgposition WHERE sp_id = "+dw.getString("sp_id");
					}
					if(findtype==3){
						sqlstr=sqlstr+"SELECT op.ospid FROM `hr_standposition` sp,`hr_orgposition` op WHERE op.sp_id=sp.sp_id AND  sp.sp_name LIKE '%"+spname+"%' AND op.idpath LIKE '"+org.idpath.getValue()+"%'";
					}
					sqlstr=sqlstr+") AND wagetype=1 "+" and idpath like '"+org.idpath.getValue()+"%' "+" AND yearmonth>='"+ymbg+"' AND yearmonth<'"+ymed+"'";
					res= org.pool.opensql2json_O(sqlstr);
					if(res.size()>0){
						dw.put("avgpos", res.getJSONObject(0).getString("avgpos"));
						dw.put("maxpos", res.getJSONObject(0).getString("maxpos"));
						dw.put("minpos", res.getJSONObject(0).getString("minpos"));
						dw.put("nums", res.getJSONObject(0).getString("nums"));
					}else{
						dw.put("avgpos", 0);
						dw.put("maxpos", 0);
						dw.put("minpos", 0);
						dw.put("nums",0);
					}
					dw.put("pw10", ipw10);
					dw.put("pw25", ipw25);
					dw.put("pw50", ipw50);
					dw.put("pw75", ipw75);
					dw.put("pw90", ipw90);	
					}
		//	}
		 savg.addAll(dws);
		 if(savg.size()==0){
			 JSONObject nulljson=new JSONObject();
				nulljson.put("orgname", org.orgname.getValue());
				nulljson.put("sp_name", spname);
				savg.add(nulljson);
		 }
		JSONObject jo=new JSONObject();
		jo.put("rows", savg);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(savg, scols);
			return null;
		}
	 }
	 
	 
	 @ACOAction(eventname = "getSalaryOrgsAnalysis", Authentication = true, notes = "查询月薪人员薪资各单位间对比分析")
	 public String getSalaryOrgsAnalysis() throws Exception {
		 HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		//String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		String spname = CorUtil.getJSONParmValue(jps, "spname");
		String iswzjq_str = CorUtil.getJSONParmValue(jps, "findtype");
		int findtype = (iswzjq_str == null) ? 1 : Integer.valueOf(iswzjq_str);
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = getTrendOrgsByOrgid(orgid, yms,findtype,spname);
		
		Shworg org = new Shworg();
		/*org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");*/
	/*	Hr_standposition sp=new Hr_standposition();
		sp.findByID(spid);
		if(sp.isEmpty()){
	   	  throw new Exception("id为【"+spid+"】的职位不存在！");
		}*/
		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(yearmonth_begin)));// 去除时分秒
		Date eddate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(yearmonth_end)));// 去除时分秒
		int comparedate=bgdate.compareTo(eddate);
		String analysisyms="";
		if(comparedate==0){
			analysisyms=yearmonth_begin;
		}else if(comparedate<0){
			analysisyms=yearmonth_begin+"--"+yearmonth_end;
		}
		Date ftdate=Systemdate.dateMonthAdd(eddate, 1);
		String ymbg=Systemdate.getStrDateByFmt(bgdate, "yyyy-MM");
		String ymed=Systemdate.getStrDateByFmt(ftdate, "yyyy-MM");
		
		JSONArray savg = new JSONArray();
		for(int i = dws.size() - 1; i >= 0; i--){
			JSONObject dw = dws.getJSONObject(i);// 机构 年月
			dw.put("months", analysisyms);
			//dw.put("spname", sp.sp_name.getValue());
			org.clear();
			org.findByID(dw.getString("orgid"));
			if(org.isEmpty()){
				throw new Exception("机构列表里ID为【" + dw.getString("orgid") + "】的机构不存在");
			}
			double fpw10=0;
			double fpw25=0;
			double fpw50=0;
			double fpw75=0;
			double fpw90=0;
			double temp10=0;
			double temp25=0;
			double temp50=0;
			double temp75=0;
			double temp90=0;
			double ymnums=0;
			for(String ym:yearmonths){
				Date fromdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(ym)));// 去除时分秒
				Date todate=Systemdate.dateMonthAdd(fromdate, 1);
				String frstr=Systemdate.getStrDateByFmt(fromdate, "yyyy-MM");
				String tostr=Systemdate.getStrDateByFmt(todate, "yyyy-MM");
				String sqlstr="SELECT poswage FROM `hr_salary_list` WHERE ospid IN (";
				if(findtype==1||findtype==2){
					sqlstr=sqlstr+"SELECT ospid FROM hr_orgposition WHERE sp_id = "+dw.getString("sp_id");
				}
				if(findtype==3){
					sqlstr=sqlstr+"SELECT op.ospid FROM `hr_standposition` sp,`hr_orgposition` op WHERE op.sp_id=sp.sp_id AND  sp.sp_name LIKE '%"+spname+"%' AND op.idpath LIKE '"+org.idpath.getValue()+"%'";
				}
				sqlstr=sqlstr+") AND wagetype=1 AND "+
						" idpath like '"+org.idpath.getValue()+"%' and "+
						" yearmonth>='"+frstr+"' AND yearmonth<'"+tostr+"' ORDER BY poswage";
					JSONArray res= org.pool.opensql2json_O(sqlstr);
					double[] datas=new double[res.size()];
					if(res.size()>0){
						for(int j=0;j<res.size();j++){
							JSONObject row = res.getJSONObject(j);
							Double pw=Double.valueOf(row.getString("poswage"));
							datas[j]=pw;
						}
						double pw10=COHr_salary.getPercentile(datas,0.1);
						double pw25=COHr_salary.getPercentile(datas,0.25);
						double pw50=COHr_salary.getPercentile(datas,0.5);
						double pw75=COHr_salary.getPercentile(datas,0.75);
						double pw90=COHr_salary.getPercentile(datas,0.9);
						temp10=temp10+pw10;
						temp25=temp25+pw25;
						temp50=temp50+pw50;
						temp75=temp75+pw75;
						temp90=temp90+pw90;
						
						ymnums=ymnums+1;
						fpw10=temp10/ymnums;
						fpw25=temp25/ymnums;
						fpw50=temp50/ymnums;
						fpw75=temp75/ymnums;
						fpw90=temp90/ymnums;
					}
			}
			
			String sqlstr="SELECT IFNULL(ROUND(AVG(avgpos)),0) AS avgpos,IFNULL(ROUND(AVG(nums)),0) AS nums,"+
			"IFNULL(ROUND(AVG(maxpos)),0) AS maxpos,IFNULL(ROUND(AVG(minpos)),0) AS minpos,IFNULL(ROUND(AVG(minpos)),0) AS minpos,IFNULL('"+dw.getString("sp_name")+
				"','') AS pos FROM (SELECT IFNULL(AVG(poswage),0) AS avgpos,IFNULL(COUNT(DISTINCT er_id),0) AS nums,"+
				"IFNULL(MAX(poswage),0) as maxpos,IFNULL(MIN(poswage),0) as minpos,IFNULL('"+dw.getString("sp_name")+
				"','') AS pos FROM `hr_salary_list` WHERE ospid IN (";
			if(findtype==1||findtype==2){
				sqlstr=sqlstr+"SELECT ospid FROM hr_orgposition WHERE sp_id = "+dw.getString("sp_id");
			}
			if(findtype==3){
				sqlstr=sqlstr+"SELECT op.ospid FROM `hr_standposition` sp,`hr_orgposition` op WHERE op.sp_id=sp.sp_id AND  sp.sp_name LIKE '%"+spname+"%' AND op.idpath LIKE '"+org.idpath.getValue()+"%'";
			}
				sqlstr=sqlstr+") AND wagetype=1 AND  idpath like '"+org.idpath.getValue()+"%' and  yearmonth>='"+ymbg+"' AND yearmonth<'"+ymed+"' GROUP BY yearmonth) ss";
			JSONArray analysis= org.pool.opensql2json_O(sqlstr);
			if(Integer.parseInt(analysis.getJSONObject(0).getString("nums"))==0){
				dws.remove(i);
			}
			if(analysis.size()>0){
				dw.put("avgpos", analysis.getJSONObject(0).getString("avgpos"));
				dw.put("maxpos", analysis.getJSONObject(0).getString("maxpos"));
				dw.put("minpos", analysis.getJSONObject(0).getString("minpos"));
				dw.put("nums", analysis.getJSONObject(0).getString("nums"));
			}else{
				dw.put("avgpos", 0);
				dw.put("maxpos", 0);
				dw.put("minpos", 0);
				dw.put("nums",0);
			}
			 BigDecimal bpw10 = new BigDecimal(fpw10);
			 BigDecimal bpw25 = new BigDecimal(fpw25);
			 BigDecimal bpw50 = new BigDecimal(fpw50);
			 BigDecimal bpw75 = new BigDecimal(fpw75);
			 BigDecimal bpw90 = new BigDecimal(fpw90);
			 int ipw10 = bpw10.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();  
			 int ipw25 = bpw25.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
			 int ipw50 = bpw50.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
			 int ipw75 = bpw75.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
			 int ipw90 = bpw90.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); 
			dw.put("pw10", ipw10);
			dw.put("pw25", ipw25);
			dw.put("pw50", ipw50);
			dw.put("pw75", ipw75);
			dw.put("pw90", ipw90);			
			}
		savg.addAll(dws);
		if(savg.size()==0){
			JSONObject nulljson=new JSONObject();
			nulljson.put("months", analysisyms);
			nulljson.put("sp_name", spname);
			savg.add(nulljson);
		}
		JSONObject jo=new JSONObject();
		jo.put("rows", savg);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(savg, scols);
			return null;
		}
	 }
	 
	 
	 @ACOAction(eventname = "getOrgAVGSalarys", Authentication = true, notes = "查询各单位某职位的平均工资")
	    public String getOrgAVGSalarys() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		//String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		//List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = HRUtil.getOrgsByOrgid(orgid, true, yms);
		Shworg org = new Shworg();
		org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");
		 Hr_standposition sp=new Hr_standposition();
		 sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }

		 //JSONArray savg = new JSONArray();
		 for(int i=0;i<dws.size();i++){
			 JSONObject dw = dws.getJSONObject(i);// 机构 年月
			 String sqlstr="SELECT  '"+dw.getString("yearmonth")+"' AS months,'"+sp.sp_name.getValue()+"' as spname,IFNULL(ROUND(AVG(poswage)),0) AS avgpos,IFNULL(SUM(poswage),0) AS ttpos,COUNT(*) AS nums "+
			 "FROM `hr_salary_list` cl WHERE cl.yearmonth='"+dw.getString("yearmonth")+"' AND cl.lv_num<5 and cl.wagetype=1  AND cl.ospid IN "+
			"(SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id)"+
			 " AND idpath LIKE '"+dw.getString("idpath")+"%'";
			 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
			 JSONObject tempob=tempar.getJSONObject(0);
			 dw.putAll(tempob);
		 }
		 //savg.addAll(dws);
		JSONObject jo=new JSONObject();
		jo.put("rows", dws);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	 }
	 
	 private List<String> buildYeatMonths(String yearmonth_begin, String yearmonth_end) throws Exception {
			Date bgdate = Systemdate.getDateByStr(yearmonth_begin);
			Date eddate = Systemdate.getDateByStr(yearmonth_end);
			if (eddate.getTime() < bgdate.getTime())
			    throw new Exception("开始月度大于截止月度");
			List<String> rst = new ArrayList<String>();
			Date tempDate = bgdate;
			while (tempDate.getTime() <= eddate.getTime()) {
			    System.out.println("yearmonths:" + Systemdate.getStrDateByFmt(tempDate, "yyyy-MM"));
			    rst.add(Systemdate.getStrDateByFmt(tempDate, "yyyy-MM"));
			    tempDate = Systemdate.dateMonthAdd(tempDate, 1);
			}
			if (rst.size() > 12)
			    throw new Exception("不能超过12个月");
			return rst;
		    }
	 
	 @ACOAction(eventname = "getSalaryAnalysis", Authentication = true, notes = "查询某职位的入职定薪分析/入职转正调薪分析/调动转正调薪分析/特殊调薪分析")
	    public String getSalaryAnalysis() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid");
		String spid = CorUtil.getJSONParmValue(jps, "spid");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		int scatype =Integer.parseInt(CorUtil.getJSONParmValue(jps, "scatype", "需要参数【scatype】"));
		int stype = Integer.parseInt(CorUtil.getJSONParmValue(jps, "stype", "需要参数【stype】"));
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		if(orgid=="0"){
			orgid="1";
		}
		JSONArray dws = getTrendOrgsByOrgid(orgid,yearmonths,0,"");//HRUtil.getOrgsByOrgid(orgid, includechild, yearmonths);
		JSONArray result=doAnalysisSalary(dws,yearmonths,spid,orgid,false,scatype,stype);
		JSONObject jo=new JSONObject();
		jo.put("rows", result);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(result, scols);
			return null;
		}
	 }
	 
	 private JSONArray doAnalysisSalary(JSONArray dws, List<String> yms, String spid,String orgid, boolean includchd,int scatype,int stype) throws Exception{
		 JSONArray rst=new JSONArray();
		 Hr_standposition sp=new Hr_standposition();
		 if(spid!="0"){
			 sp.findByID(spid);
			 if(sp.isEmpty()){
				 throw new Exception("id为【"+spid+"】的职位不存在！");
			 }
		 }
			 for(int i=0;i<dws.size();i++){
				 JSONObject dw = dws.getJSONObject(i);// 机构 年月
				 String yearmonths =  dw.getString("yearmonth") + "-01";
				 Date month=Systemdate.getDateByStr(yearmonths);
				 Date nextmonth=Systemdate.dateMonthAdd(month, 1);
				 String nm=Systemdate.getStrDateByFmt(nextmonth, "yyyy-MM-dd");
				 String sqlstr="";
				 if(scatype==1){
					 if(spid=="0"){
						 sqlstr="SELECT '' as sp_name,COUNT(*) AS nums ,"+
								 "ROUND(SUM(newposition_salary+newtech_allowance+newparttimesubs+newpostsubs)) AS ttwages,"+
								"ROUND(AVG(newposition_salary+newtech_allowance+newparttimesubs+newpostsubs)) AS avgwage FROM "+
								 "(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
								"WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=1  "+
								"AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt ";
					 }else{
						 sqlstr="SELECT '"+sp.sp_name.getValue()+"' as sp_name,COUNT(*) AS nums ,"+
								 "ROUND(SUM(newposition_salary+newtech_allowance+newparttimesubs+newpostsubs)) AS ttwages,"+
								"ROUND(AVG(newposition_salary+newtech_allowance+newparttimesubs+newpostsubs)) AS avgwage FROM "+
								 "(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
								"WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=1 AND e.ospid IN "+
								 "(SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id) "+
								"AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt ";
					 }
				 }
				 if(scatype==3){
					 if(stype==5){
						 if(spid=="0"){
							 sqlstr="SELECT '' as sp_name,COUNT(*) AS nums ,IFNULL(ROUND(AVG(oldposition_salary)),0) AS oldavgs,"+
									 "IFNULL(ROUND(SUM(sacrage)),0) AS ttchgwages,IFNULL(ROUND(SUM(sacrage)/COUNT(*)),0) AS avgchgs,"+
									"CONCAT(IFNULL(ROUND(((SUM(sacrage)/COUNT(*))/AVG(oldposition_salary))*100),0),'%') AS avgchgrank,"+
									 "IFNULL(ROUND(AVG(newposition_salary)),0) AS avgwage FROM "+
									"(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
									 "WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=3 AND sc.stype=5   "+
									 "AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt";
						 }else{
							 sqlstr="SELECT '"+sp.sp_name.getValue()+"' as sp_name,COUNT(*) AS nums ,IFNULL(ROUND(AVG(oldposition_salary)),0) AS oldavgs,"+
									 "IFNULL(ROUND(SUM(sacrage)),0) AS ttchgwages,IFNULL(ROUND(SUM(sacrage)/COUNT(*)),0) AS avgchgs,"+
									"CONCAT(IFNULL(ROUND(((SUM(sacrage)/COUNT(*))/AVG(oldposition_salary))*100),0),'%') AS avgchgrank,"+
									 "IFNULL(ROUND(AVG(newposition_salary)),0) AS avgwage FROM "+
									"(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
									 "WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=3 AND sc.stype=5 AND e.ospid IN "+
									"(SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id) "+
									 "AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt";
						 }
					 }
					 if(stype==6){
						 if(spid=="0"){
							 sqlstr="SELECT '' as sp_name,COUNT(*) AS nums ,IFNULL(ROUND(AVG(oldposition_salary)),0) AS oldavgs,"+
									 "IFNULL(ROUND(SUM(sacrage)),0) AS ttchgwages,IFNULL(ROUND(SUM(sacrage)/COUNT(*)),0) AS avgchgs,"+
									"CONCAT(IFNULL(ROUND(((SUM(sacrage)/COUNT(*))/AVG(oldposition_salary))*100),0),'%') AS avgchgrank,"+
									 "IFNULL(ROUND(AVG(newposition_salary)),0) AS avgwage FROM "+
									"(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
									 "WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=3 AND sc.stype=6  "+
									 "AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt";
						 }else{
							 sqlstr="SELECT '"+sp.sp_name.getValue()+"' as sp_name,COUNT(*) AS nums ,IFNULL(ROUND(AVG(oldposition_salary)),0) AS oldavgs,"+
									 "IFNULL(ROUND(SUM(sacrage)),0) AS ttchgwages,IFNULL(ROUND(SUM(sacrage)/COUNT(*)),0) AS avgchgs,"+
									"CONCAT(IFNULL(ROUND(((SUM(sacrage)/COUNT(*))/AVG(oldposition_salary))*100),0),'%') AS avgchgrank,"+
									 "IFNULL(ROUND(AVG(newposition_salary)),0) AS avgwage FROM "+
									"(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
									 "WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=3 AND sc.stype=6 AND e.ospid IN "+
									"(SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id) "+
									 "AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt";
						 }
					 }
				 }
				 if(scatype==5){
					 if(spid=="0"){
						 sqlstr="SELECT '' as sp_name,COUNT(*) AS nums ,IFNULL(ROUND(AVG(oldposition_salary)),0) AS oldavgs,"+
								 "IFNULL(ROUND(SUM(sacrage)),0) AS ttchgwages,IFNULL(ROUND(SUM(sacrage)/COUNT(*)),0) AS avgchgs,"+
								"CONCAT(IFNULL(ROUND(((SUM(sacrage)/COUNT(*))/AVG(oldposition_salary))*100),0),'%') AS avgchgrank,"+
								 "IFNULL(ROUND(AVG(newposition_salary)),0) AS avgwage FROM "+
								"(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
								 "WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=5 AND (sc.stype=7 or sc.stype=9)  "+
								 "AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt";
					 }else{
						 sqlstr="SELECT '"+sp.sp_name.getValue()+"' as sp_name,COUNT(*) AS nums ,IFNULL(ROUND(AVG(oldposition_salary)),0) AS oldavgs,"+
								 "IFNULL(ROUND(SUM(sacrage)),0) AS ttchgwages,IFNULL(ROUND(SUM(sacrage)/COUNT(*)),0) AS avgchgs,"+
								"CONCAT(IFNULL(ROUND(((SUM(sacrage)/COUNT(*))/AVG(oldposition_salary))*100),0),'%') AS avgchgrank,"+
								 "IFNULL(ROUND(AVG(newposition_salary)),0) AS avgwage FROM "+
								"(SELECT e.orgid,e.orgname,e.idpath,e.employee_name,e.ospid,e.sp_name,sc.* FROM `hr_salary_chglg` sc,`hr_employee` e "+
								 "WHERE sc.er_id=e.er_id AND sc.useable=1 AND sc.scatype=5 AND (sc.stype=7 or sc.stype=9) AND e.ospid IN "+
								"(SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id) "+
								 "AND sc.chgdate>='"+yearmonths+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%') tt";
					 }
					 
				 }
				 
				 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
				 JSONObject tempob=tempar.getJSONObject(0);
				 dw.putAll(tempob);
			 }
			 rst.addAll(dws);

		 return rst;
	 }
	 
	 
	 @ACOAction(eventname = "getSubAnalysis", Authentication = true, notes = "获得津贴分析")
	    public String getSubAnalysis() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = getTrendOrgsByOrgid(orgid, yms,0,"");
		Shworg org = new Shworg();
	/*	org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");*/
		JSONArray ttsubs = new JSONArray();
		 for(String ym:yearmonths){
			 JSONArray orgsubs = new JSONArray();
			 for(int i=0;i<dws.size();i++){
				 JSONObject dw = dws.getJSONObject(i);// 机构 年月
				 String nowmon =  ym + "-01";
				 Date month=Systemdate.getDateByStr(nowmon);
				 Date nextmonth=Systemdate.dateMonthAdd(month, 1);
				 String nm=Systemdate.getStrDateByFmt(nextmonth, "yyyy-MM-dd");
				 
				 String sqlstr="SELECT '"+dw.getString("orgname")+"' AS orgname,'"+ym+"' AS months,(CASE stype WHEN 8 THEN 1  WHEN 10 THEN 2  WHEN 11 THEN 4 END )  AS stype,"+
				 "ROUND(SUM(sacrage)) AS subpays,COUNT(*) AS nums FROM `hr_salary_chglg` sc,`hr_employee` e "+
				"WHERE sc.er_id=e.er_id AND useable=1 AND (sc.stype=8 OR sc.stype=10 OR sc.stype=11) AND "+
				 " sc.chgdate>='"+nowmon+"' AND sc.chgdate<'"+nm+"' AND e.idpath LIKE '"+dw.getString("idpath")+"%' GROUP BY stype ";
				 JSONArray tempsubs=org.pool.opensql2json_O(sqlstr);
				 
				 sqlstr="SELECT '"+dw.getString("orgname")+"' AS orgname,'"+ym+"' AS months,'3' AS stype,ROUND(SUM(shouldpay)) AS subpays,COUNT(*) AS nums "+
				 "FROM `hr_salary_hotsub` hs,`hr_salary_hotsub_line` hsl WHERE hsl.hs_id=hs.hs_id AND hs.stat=9 AND "+
				"hs.applydate>='"+ym+"' AND hs.applydate<'"+nm+"' AND hsl.idpath LIKE '"+dw.getString("idpath")+"%'";
				 JSONArray temphs=org.pool.opensql2json_O(sqlstr);
				 JSONObject hotsub=temphs.getJSONObject(0);
				 tempsubs.add(hotsub);
				 orgsubs.addAll(tempsubs);
			 }
			 ttsubs.addAll(orgsubs);
		 }
			JSONObject jo=new JSONObject();
			jo.put("rows", ttsubs);
			String scols = parms.get("cols");
			if (scols == null) {
				return jo.toString();
			} else {
				(new CReport()).export2excel(ttsubs, scols);
				return null;
			}
	 }
	 
	 @ACOAction(eventname = "getAVGPerHoursSalarys", Authentication = true, notes = "查询6-8级某职位的平均工资与平均小时工资")
	    public String getAVGPerHoursSalarys() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		JSONArray dws = HRUtil.getOrgsByOrgid(orgid, false, yearmonths);
		Shworg org = new Shworg();
		org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");
		 Hr_standposition sp=new Hr_standposition();
		 sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }

		// JSONArray savg = new JSONArray();
		// for(String ym:yearmonths){
			 for(int i=0;i<dws.size();i++){
				 JSONObject dw = dws.getJSONObject(i);// 机构 年月
				 String sqlstr="SELECT ROUND(AVG(paywsubs)) AS avgpws,ROUND(AVG(IFNULL(paywsubs/tworkhours,0))) AS avgperhour,"+
				 "tworkhours,COUNT(*) AS nums,IFNULL('"+sp.sp_name.getValue()+"','') AS pos FROM `hr_salary_list` "+
				"WHERE wagetype=2 AND  isfullattend=1 AND ospid IN (SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp "+
				 "WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id) AND yearmonth='"+dw.getString("yearmonth")+
				 "' AND lv_num>=6 AND idpath LIKE '"+dw.getString("idpath")+"%'";
				 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
				 JSONObject tempob=tempar.getJSONObject(0);
				 dw.putAll(tempob);
			 }
			// savg.addAll(dws);
		// }
		JSONObject jo=new JSONObject();
		jo.put("rows", dws);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	 }
	 
	 @ACOAction(eventname = "getNotMonthSalarysTrend", Authentication = true, notes = "查询非月薪人员薪资水平时间趋势")
	    public String getNotMonthSalarysTrend() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		//String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		String spname = CorUtil.getJSONParmValue(jps, "spname", "需要参数【spname】");
		String iswzjq_str = CorUtil.getJSONParmValue(jps, "findtype", "需要参数【findtype】");
		int findtype = (iswzjq_str == null) ? 1 : Integer.valueOf(iswzjq_str);
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		JSONArray dws = getTrendOrgsByOrgid(orgid,  yearmonths,findtype,spname);
		Shworg org = new Shworg();
		org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");
		Hr_standposition sp=new Hr_standposition();
		/* sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }*/

		// JSONArray savg = new JSONArray();
		// for(String ym:yearmonths){
			 for(int i=0;i<dws.size();i++){
				 JSONObject dw = dws.getJSONObject(i);// 机构 年月
				 String sqlstr="SELECT IFNULL(ROUND(AVG(paywsubs)),0) AS avgpws,IFNULL(ROUND(MIN(paywsubs)),0) AS minpws,"+
				 "IFNULL(ROUND(MAX(paywsubs)),0) AS maxpws,IFNULL(ROUND(AVG(paynosubs)),0) AS avgpns,IFNULL(ROUND(MIN(paynosubs)),0) AS minpns,"+
				"IFNULL(ROUND(MAX(paynosubs)),0) AS maxpns,IFNULL(ROUND(AVG(IFNULL(paywsubs/tworkhours,0)),1),0) AS avgperhour,"+
				 "IFNULL(ROUND(MAX(IFNULL(paywsubs/tworkhours,0)),1),0) AS maxperhour,IFNULL(ROUND(MIN(IFNULL(paywsubs/tworkhours,0)),1),0) AS minperhour,"+
				"IFNULL(ROUND(AVG(IFNULL(paynosubs/tworkhours,0)),1),0) AS avgnsperhour,IFNULL(ROUND(MAX(IFNULL(paynosubs/tworkhours,0)),1),0) AS maxnsperhour,"+
				 "IFNULL(ROUND(MIN(IFNULL(paynosubs/tworkhours,0)),1),0) AS minnsperhour,"+
				 "IFNULL(tworkhours,0) AS tworkhours,COUNT(*) AS nums,IFNULL('"+dw.getString("sp_name")+"','') AS pos FROM `hr_salary_list` "+
				"WHERE wagetype=2 AND  isfullattend=1 AND ospid IN (";
				 if(findtype==1||findtype==2){
						sqlstr=sqlstr+"SELECT ospid FROM hr_orgposition WHERE sp_id = "+dw.getString("sp_id");
					}
					if(findtype==3){
						sqlstr=sqlstr+"SELECT op.ospid FROM `hr_standposition` sp,`hr_orgposition` op WHERE op.sp_id=sp.sp_id AND  sp.sp_name LIKE '%"
					+spname+"%' AND op.idpath LIKE '"+dw.getString("idpath")+"%'";
					}
				 sqlstr=sqlstr+") AND yearmonth='"+dw.getString("yearmonth")+
				 "'  AND idpath LIKE '"+dw.getString("idpath")+"%'";
				 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
				 JSONObject tempob=tempar.getJSONObject(0);
				 dw.putAll(tempob);
			 }
			// savg.addAll(dws);
		// }
		JSONObject jo=new JSONObject();
		jo.put("rows", dws);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	 }
	 
	 @ACOAction(eventname = "getNotMonthSalarysOrgsAnalysis", Authentication = true, notes = "查询非月薪人员薪资各单位对比分析")
	    public String getNotMonthSalarysOrgsAnalysis() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		//String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		//List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		String spname = CorUtil.getJSONParmValue(jps, "spname", "需要参数【spname】");
		String iswzjq_str = CorUtil.getJSONParmValue(jps, "findtype", "需要参数【findtype】");
		int findtype = (iswzjq_str == null) ? 1 : Integer.valueOf(iswzjq_str);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = getTrendOrgsByOrgid(orgid, yms,findtype,spname);
		Shworg org = new Shworg();
		/*org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");*/
		 Hr_standposition sp=new Hr_standposition();
		 /* sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }*/
		 Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(yearmonth_begin)));// 去除时分秒
			Date eddate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(yearmonth_end)));// 去除时分秒
			int comparedate=bgdate.compareTo(eddate);
			String analysisyms="";
			if(comparedate==0){
				analysisyms=yearmonth_begin;
			}else if(comparedate<0){
				analysisyms=yearmonth_begin+"--"+yearmonth_end;
			}
			Date ftdate=Systemdate.dateMonthAdd(eddate, 1);
			String ymbg=Systemdate.getStrDateByFmt(bgdate, "yyyy-MM");
			String ymed=Systemdate.getStrDateByFmt(ftdate, "yyyy-MM");
		// JSONArray savg = new JSONArray();
		// for(String ym:yearmonths){
			 for(int i=0;i<dws.size();i++){
				 JSONObject dw = dws.getJSONObject(i);// 机构 年月
				 String sqlstr="SELECT IFNULL(ROUND(AVG(avgpws),0),0) AS avgpws,IFNULL(ROUND(AVG(minpws),0),0) AS minpws,IFNULL(ROUND(AVG(maxpws),0),0) AS maxpws,"+
				 "IFNULL(ROUND(AVG(avgpns),0),0) AS avgpns,IFNULL(ROUND(AVG(minpns),0),0) AS minpns,IFNULL(ROUND(AVG(maxpns),0),0) AS maxpns,"+
				"IFNULL(ROUND(AVG(avgperhour),1),0) AS avgperhour,IFNULL(ROUND(AVG(maxperhour),1),0) AS maxperhour,IFNULL(ROUND(AVG(minperhour),1),0) AS minperhour,"+
				 "IFNULL(ROUND(AVG(avgnsperhour),1),0) AS avgnsperhour,IFNULL(ROUND(AVG(maxnsperhour),1),0) AS maxnsperhour,IFNULL(ROUND(AVG(minnsperhour),1),0) AS minnsperhour,"+
				"IFNULL(pos,'') AS pos,'"+analysisyms+"' as months FROM "+
				"(SELECT IFNULL(ROUND(AVG(paywsubs)),0) AS avgpws,IFNULL(ROUND(MIN(paywsubs)),0) AS minpws,"+
				 "IFNULL(ROUND(MAX(paywsubs)),0) AS maxpws,IFNULL(ROUND(AVG(paynosubs)),0) AS avgpns,IFNULL(ROUND(MIN(paynosubs)),0) AS minpns,"+
				"IFNULL(ROUND(MAX(paynosubs)),0) AS maxpns,IFNULL(ROUND(AVG(IFNULL(paywsubs/tworkhours,0)),1),0) AS avgperhour,"+
				 "IFNULL(ROUND(MAX(IFNULL(paywsubs/tworkhours,0)),1),0) AS maxperhour,IFNULL(ROUND(MIN(IFNULL(paywsubs/tworkhours,0)),1),0) AS minperhour,"+
				"IFNULL(ROUND(AVG(IFNULL(paynosubs/tworkhours,0)),1),0) AS avgnsperhour,IFNULL(ROUND(MAX(IFNULL(paynosubs/tworkhours,0)),1),0) AS maxnsperhour,"+
				 "IFNULL(ROUND(MIN(IFNULL(paynosubs/tworkhours,0)),1),0) AS minnsperhour,"+
				 "IFNULL(tworkhours,0) AS tworkhours,COUNT(*) AS nums,IFNULL('"+dw.getString("sp_name")+"','') AS pos FROM `hr_salary_list` "+
				"WHERE wagetype=2 AND  isfullattend=1 AND ospid IN (";
				 if(findtype==1||findtype==2){
						sqlstr=sqlstr+"SELECT ospid FROM hr_orgposition WHERE sp_id = "+dw.getString("sp_id");
					}
					if(findtype==3){
						sqlstr=sqlstr+"SELECT op.ospid FROM `hr_standposition` sp,`hr_orgposition` op WHERE op.sp_id=sp.sp_id AND  sp.sp_name LIKE '%"
					+spname+"%' AND op.idpath LIKE '"+dw.getString("idpath")+"%'";
					}
				 sqlstr=sqlstr+ ") AND yearmonth>='"+ymbg+"' AND yearmonth<'"+ymed+
				 "'  AND idpath LIKE '"+dw.getString("idpath")+"%' GROUP BY yearmonth) nmss ";
				 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
				 JSONObject tempob=tempar.getJSONObject(0);
				 dw.putAll(tempob);
			 }
			// savg.addAll(dws);
		// }
		JSONObject jo=new JSONObject();
		jo.put("rows", dws);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	 }
	 
	 
	 @ACOAction(eventname = "getAVGOrgPerHoursSalarys", Authentication = true, notes = "查询6-8级某职位的各单位间的平均工资与平均小时工资")
	    public String getAVGOrgPerHoursSalarys() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = HRUtil.getOrgsByOrgid(orgid, true, yms);
		Shworg org = new Shworg();
		org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");
		 Hr_standposition sp=new Hr_standposition();
		 sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }
		 for(int i=0;i<dws.size();i++){
			 JSONObject dw = dws.getJSONObject(i);// 机构 年月
			 String sqlstr="SELECT ROUND(AVG(paywsubs)) AS avgpws,ROUND(AVG(IFNULL(paywsubs/tworkhours,0))) AS avgperhour,"+
			 "tworkhours,COUNT(*) AS nums,IFNULL('"+sp.sp_name.getValue()+"','') AS pos FROM `hr_salary_list` "+
			"WHERE wagetype=2 AND  isfullattend=1 AND ospid IN (SELECT op.ospid FROM `hr_orgposition` op,`hr_standposition` sp "+
			 "WHERE sp.sp_id="+spid+" AND op.sp_id=sp.sp_id) AND yearmonth='"+dw.getString("yearmonth")+
			 "' AND lv_num>=6 AND idpath LIKE '"+dw.getString("idpath")+"%'";
			 JSONArray tempar=sp.pool.opensql2json_O(sqlstr);
			 JSONObject tempob=tempar.getJSONObject(0);
			 dw.putAll(tempob);
		 }
		JSONObject jo=new JSONObject();
		jo.put("rows", dws);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	 }
	 
	 @ACOAction(eventname = "getYearRaiseAnalysis", Authentication = true, notes = "查询年度调薪分析")
	    public String getYearRaiseAnalysis() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(parms.get("parms"));
		String orgid = CorUtil.getJSONParmValue(jps, "orgid", "需要参数【orgid】");
		//String spid = CorUtil.getJSONParmValue(jps, "spid", "需要参数【spid】");
		String yearmonth_begin = CorUtil.getJSONParmValue(jps, "yearmonth_begin", "需要参数【yearmonth_begin】");
		String yearmonth_end = CorUtil.getJSONParmValue(jps, "yearmonth_end", "需要参数【yearmonth_end】");
		//boolean includechild = Boolean.valueOf(CorUtil.getJSONParmValue(jps, "includechild", "需要参数【includechild】"));
		List<String> yearmonths = buildYeatMonths(yearmonth_begin, yearmonth_end);
		List<String> yms=new ArrayList<String>();
		yms.add(yearmonth_begin);
		JSONArray dws = getTrendOrgsByOrgid(orgid, yearmonths,0,"");
		Shworg org = new Shworg();
	/*	org.findByID(orgid, false);
		if (org.isEmpty())
			throw new Exception("ID为【" + orgid + "】的机构不存在");*/
		/* Hr_standposition sp=new Hr_standposition();
		 sp.findByID(spid);
		 if(sp.isEmpty()){
			 throw new Exception("id为【"+spid+"】的职位不存在！");
		 }*/
		 for(int i=0;i<dws.size();i++){
			 JSONObject dw = dws.getJSONObject(i);// 机构 年月
			 String nowmon =  dw.getString("yearmonth") + "-01";
			 Date month=Systemdate.getDateByStr(nowmon);
			 Date nextmonth=Systemdate.dateMonthAdd(month, 1);
			 String nm=Systemdate.getStrDateByFmt(nextmonth, "yyyy-MM-dd");
			 String sqlstr="SELECT IFNULL(ROUND(SUM(DISTINCT salary_quota_canuse)),0) AS qtcu ,COUNT(*) AS nums ,IFNULL(ROUND(SUM(oldposition_salary+oldtech_allowance)),0) AS oldwage,"+
			 "IFNULL(ROUND(SUM(pbtsarylev)),0) AS chgsa,IFNULL(ROUND(SUM(pbtsarylev))-ROUND(SUM(salary_quota_canuse)),0) AS overquta,CONCAT(IFNULL(ROUND(SUM(pbtsarylev)/SUM(oldposition_salary+oldtech_allowance)*100),0),'%') AS chagper "+
			"FROM (SELECT yrl.*,yr.salary_quota_stand,yr.salary_quota_canuse,yr.salarydate FROM `hr_salary_yearraise` yr,`hr_salary_yearraise_line` yrl "+
			 "WHERE yr.stat=9 AND yrl.yrid=yr.yrid AND salarydate>='"+nowmon+"' AND salarydate<'"+nm+"' AND yrl.idpath LIKE '"+dw.getString("idpath")+"%') tt";
			 JSONArray tempar=org.pool.opensql2json_O(sqlstr);
			 JSONObject tempob=tempar.getJSONObject(0);
			 if((tempob.size()>0)&&(!tempob.isEmpty())){
				 sqlstr="SELECT COUNT(*) ct FROM `hr_employee` WHERE idpath LIKE '1,%' AND empstatid=4 AND pay_way='月薪'";
				 float empnums=Float.parseFloat(org.pool.openSql2List(sqlstr).get(0).get("ct"));
				 float chagwages=Float.parseFloat(tempob.getString("chgsa"));
				 float avgchag=chagwages/empnums;
				 BigDecimal b = new BigDecimal(avgchag);  
				 avgchag = b.setScale(0,BigDecimal.ROUND_HALF_UP).floatValue();
				 tempob.put("avgchag", avgchag);
			 }else{
				 tempob.put("avgchag", 0);
			 }
			 dw.putAll(tempob);
		 }
		JSONObject jo=new JSONObject();
		jo.put("rows", dws);
		String scols = parms.get("cols");
		if (scols == null) {
			return jo.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	 }
	 
	 public static JSONArray getTrendOrgsByOrgid(String orgid, List<String> yearmonths,int findtype,String spname) throws Exception {
			Shworg org = new Shworg();
		/*	org.findByID(orgid, false);
			if (org.isEmpty())
				throw new Exception("ID为【" + orgid + "】的机构不存在");*/
			JSONArray orgs = new JSONArray();
			String sqlstr = "SELECT * FROM shworg WHERE  usable=1 " + CSContext.getIdpathwhere()
					+ " AND orgid in (" + orgid + ") ORDER BY orgid ";
			orgs = org.pool.opensql2json_O(sqlstr);
			Hr_orgposition op=new Hr_orgposition();
			JSONArray rst = new JSONArray();
			JSONArray newrst = new JSONArray();
			for (String yearmonth : yearmonths) {
				for (int i = 0; i < orgs.size(); i++) {
					JSONObject neworgs=orgs.getJSONObject(i);
					if(findtype!=0){
						JSONArray sps = new JSONArray();
						if(findtype==1){
							sqlstr="SELECT * FROM hr_standposition  WHERE sp_id IN (SELECT DISTINCT sp_id FROM hr_orgposition WHERE sp_name='"+spname+"' AND idpath LIKE '"+orgs.getJSONObject(i).getString("idpath")+"%') ";
						}
						if(findtype==2){
							sqlstr="SELECT * FROM hr_standposition  WHERE sp_id IN (SELECT DISTINCT sp_id FROM hr_orgposition WHERE sp_name like '%"+spname+"%' AND idpath LIKE '"+orgs.getJSONObject(i).getString("idpath")+"%') ";
						}
						if(findtype==3){
							sqlstr="SELECT * FROM hr_standposition  WHERE sp_id IN (SELECT DISTINCT sp_id FROM hr_orgposition WHERE sp_name like '%"+spname+"%' AND idpath LIKE '"+orgs.getJSONObject(i).getString("idpath")+"%') limit 0,1 ";
						}
						sps=op.pool.opensql2json_O(sqlstr);
						for(int j=0;j<sps.size();j++){
							neworgs.put("sp_id", sps.getJSONObject(j).get("sp_id"));
							if(findtype==3){
								neworgs.put("sp_name", spname);
							}else{
								neworgs.put("sp_name", sps.getJSONObject(j).get("sp_name"));
							}
							neworgs.put("spname", spname);
							neworgs.put("yearmonth",yearmonth);
							newrst.add(neworgs);
							//System.out.println("============="+sps.getJSONObject(j).get("sp_name"));
						}
					}else{
						neworgs.put("yearmonth",yearmonth);
						newrst.add(neworgs);
					}
					
					//orgs.getJSONObject(i).put("yearmonth", yearmonth);
				}
				//rst.addAll(orgs);
			}
			rst.addAll(newrst);
			//System.out.println(rst);
			return rst;
		}
}
