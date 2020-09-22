package com.hr.attd.co;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CjpaUtil;
import com.corsair.dbpool.CDBPool;
import com.corsair.dbpool.DBPools;
import com.corsair.dbpool.CDBConnection.DBType;
import com.corsair.dbpool.util.CJSON;
import com.corsair.dbpool.util.JSONParm;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.base.ConstsSw;
import com.corsair.server.generic.Shworg;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CReport;
import com.corsair.server.util.CorUtil;
import com.hr.attd.ctr.CacalKQData;
import com.hr.attd.ctr.HrkqUtil;
import com.hr.attd.entity.Hrkq_bckqrst;
import com.hr.attd.entity.Hrkq_business_trip;
import com.hr.attd.entity.Hrkq_holidayapp;
import com.hr.attd.entity.Hrkq_leave_blance;
import com.hr.attd.entity.Hrkq_ondutyline;
import com.hr.attd.entity.Hrkq_overtime;
import com.hr.attd.entity.Hrkq_overtime_list;
import com.hr.attd.entity.Hrkq_resign;
import com.hr.attd.entity.Hrkq_sched;
import com.hr.attd.entity.Hrkq_sched_line;
import com.hr.attd.entity.Hrkq_special_holday;
import com.hr.attd.entity.Hrkq_workschmonthlist;
import com.hr.perm.entity.Hr_employee;
import com.hr.perm.entity.Hr_leavejob;
import com.hr.util.HRUtil;

@ACO(coname = "web.hrkq.rpt2")
public class COreport2 {

	@ACOAction(eventname = "findleavlblc", Authentication = true, notes = "浏览可休假列表")
	public String getleavlblc() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		String[] notnull = {};
		String sqlstr = "SELECT * FROM( "
				+ "SELECT e.hwc_namezl,e.sp_name,b.*, IF(b.valdate>curdate(),2,1) isexpire,IF(b.usedlbtime<b.alllbtime,2,1) usup,IF((b.valdate>curdate()) AND (b.usedlbtime<b.alllbtime),1,2) canuses "
				+ "FROM hrkq_leave_blance b,hr_employee e where e.er_id=b.er_id " + CSContext.getIdpathwhere().replace("idpath", "b.idpath") + " ) tb ";
		Hrkq_overtime_list ol = new Hrkq_overtime_list();
		Hrkq_business_trip bt = new Hrkq_business_trip();
		Hrkq_ondutyline dl = new Hrkq_ondutyline();
		Hrkq_special_holday sh = new Hrkq_special_holday();

		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, " valdate desc ", notnull).findReport2JSON_O();
		JSONArray rows = rst.getJSONArray("rows");
		for (int i = 0; i < rows.size(); i++) {
			JSONObject row = rows.getJSONObject(i);
			int stype = row.getInt("stype");// 源类型 1 年假 2 加班 3 值班 4出差 5特殊
			int sid = row.getInt("sid");
			if ((stype == 2) && (sid != 0)) {
				ol.clear();
				ol.findByID(String.valueOf(sid));
				if (!ol.isEmpty()) {
					row.put("bgtime", ol.bgtime.getValue());
					row.put("edtime", ol.edtime.getValue());
				}
			}

			if ((stype == 3) && (sid != 0)) {
				dl.clear();
				dl.findByID(String.valueOf(sid));
				if (!dl.isEmpty()) {
					row.put("bgtime", dl.begin_date.getValue());
					row.put("edtime", dl.end_date.getValue());
				}
			}

			if ((stype == 4) && (sid != 0)) {
				bt.clear();
				bt.findByID(String.valueOf(sid));
				if (!bt.isEmpty()) {
					row.put("bgtime", bt.begin_date.getValue());
					row.put("edtime", bt.end_date.getValue());
				}
			}

			if ((stype == 5) && (sid != 0)) {
				sh.clear();
				sqlstr = "SELECT h.* FROM hrkq_special_holday h ,hrkq_special_holdayline l " + " WHERE h.sphid=l.sphid AND l.sphlid=" + sid;
				sh.findBySQL(sqlstr);
				if (!sh.isEmpty()) {
					row.put("bgtime", sh.sphdate.getValue());
				}
			}
		}

		String scols = urlparms.get("cols");
		if (scols == null) {
			return rst.toString();
		} else {
			(new CReport()).export2excel(rows, scols);
			return null;
		}

	}
	
	@ACOAction(eventname = "getkqorgdayrpt", Authentication = true, notes = "机构考勤日报")
	public String getkqorgdayrpt() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(urlparms.get("parms"));
		JSONParm jporgcode = CjpaUtil.getParm(jps, "orgcode");
		if (jporgcode == null)
			throw new Exception("需要参数【orgcode】");
		JSONParm jpdqdate = CjpaUtil.getParm(jps, "dqdate");
		if (jpdqdate == null)
			throw new Exception("需要参数【dqdate】");
		String orgcode = jporgcode.getParmvalue();
		String dqdate = jpdqdate.getParmvalue();

		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(dqdate)));// 去除时分秒
		Date eddate = Systemdate.dateDayAdd(bgdate, 1);// 加一天

		Hr_employee he = new Hr_employee();
		String sqlstr = "select * from shworg where code='" + orgcode + "'";
		Shworg org = new Shworg();
		org.findBySQL(sqlstr);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");
		JSONArray dws = HRUtil.getOrgsByPid(org.orgid.getValue());
		dws.add(0, org.toJsonObj());
		for (int i = 0; i < dws.size(); i++) {
			JSONObject dw = dws.getJSONObject(i);
			boolean includechld = true;// boolean includechld = (i != 0);
			dw.put("numzz", getNeedKQEmp(he, dw, includechld));
			dw.put("numrz", getintoNumEmp(he, dw, includechld, bgdate, eddate));
			dw.put("numlz", getLeaveNumEmp(he, dw, includechld, bgdate, eddate));
			dw.put("numdr", getTrintoEmpNum(he, dw, includechld, bgdate, eddate));
			dw.put("numdc", getTroutEmpNum(he, dw, includechld, bgdate, eddate));
			dw.put("numsj", getHolidayEmpNum(he, dw, "2", includechld, bgdate, eddate).empnum);// 事假人数
			dw.put("numgj", getHolidayEmpNum(he, dw, "3", includechld, bgdate, eddate).empnum);// 公假人数
			dw.put("numhj", getHolidayEmpNum(he, dw, "4", includechld, bgdate, eddate).empnum);// 婚假人数
			dw.put("numcj", getHolidayEmpNum(he, dw, "5", includechld, bgdate, eddate).empnum);// 产假人数
			dw.put("numsnj", getHolidayEmpNum(he, dw, "7", includechld, bgdate, eddate).empnum);// 丧假人数
			dw.put("numbj", getHolidayEmpNum(he, dw, "8", includechld, bgdate, eddate).empnum);// 病假人数
			dw.put("numgsj", getHolidayEmpNum(he, dw, "9", includechld, bgdate, eddate).empnum);// 工伤人数
			dw.put("numgkgkg", getKQRstEmpNum(he, dw, 1, includechld, bgdate, eddate).empnum);// 旷工（旷工）
			dw.put("numgkgcd", getKQRstEmpNum(he, dw, 2, includechld, bgdate, eddate).empnum);// 旷工（迟到）
			dw.put("numgkgzt", getKQRstEmpNum(he, dw, 3, includechld, bgdate, eddate).empnum);// 旷工（早退）
			dw.put("numgcd", getKQRstEmpNum(he, dw, 4, includechld, bgdate, eddate).empnum);// 迟到人数
			dw.put("numgzt", getKQRstEmpNum(he, dw, 5, includechld, bgdate, eddate).empnum);// 早退人数
			dw.put("numjb", getOvertimeEmpnum(he, dw, includechld, bgdate, eddate, 0).empnum);// 加班人数
		}
		String scols = urlparms.get("cols");
		if (scols == null) {
			return dws.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	}

	@ACOAction(eventname = "getkqorgmonthrpt", Authentication = true, notes = "机构考勤月报")
	public String getkqorgmonthrpt() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(urlparms.get("parms"));
		JSONParm jporgcode = CjpaUtil.getParm(jps, "orgcode");
		if (jporgcode == null)
			throw new Exception("需要参数【orgcode】");
		JSONParm jpdqdate = CjpaUtil.getParm(jps, "dqdate");
		if (jpdqdate == null)
			throw new Exception("需要参数【dqdate】");
		String orgcode = jporgcode.getParmvalue();
		String dqdate = jpdqdate.getParmvalue();
		String ym = Systemdate.getStrDateByFmt(Systemdate.getDateByStr(dqdate), "yyyy-MM");
		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(dqdate)));// 去除时分秒
		Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
		Hr_employee he = new Hr_employee();
		String sqlstr = "select * from shworg where code='" + orgcode + "'";
		Shworg org = new Shworg();
		org.findBySQL(sqlstr);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");
		JSONArray dws = HRUtil.getOrgsByPid(org.orgid.getValue());
		dws.add(0, org.toJsonObj());
		NumHour hr;
		for (int i = 0; i < dws.size(); i++) {
			JSONObject dw = dws.getJSONObject(i);
			boolean includechld = true;// boolean includechld = (i != 0);
			dw.put("numzz", getNeedKQEmp(he, dw, includechld));
			dw.put("numrz", getintoNumEmp(he, dw, includechld, bgdate, eddate));
			dw.put("numlz", getLeaveNumEmp(he, dw, includechld, bgdate, eddate));
			dw.put("numdr", getTrintoEmpNum(he, dw, includechld, bgdate, eddate));
			dw.put("numdc", getTroutEmpNum(he, dw, includechld, bgdate, eddate));
			hr = getHolidayEmpNumMonth(he, dw, "2", includechld, ym);
			dw.put("numsj", hr.empnum);// 事假人数
			dw.put("numsjsc", hr.hours);// 事假时长
			hr = getHolidayEmpNumMonth(he, dw, "3", includechld, ym);
			dw.put("numgj", hr.empnum);// 公假人数
			dw.put("numgjsc", hr.hours);// 公假时长
			hr = getHolidayEmpNumMonth(he, dw, "4", includechld, ym);
			dw.put("numhj", hr.empnum);// 婚假人数
			dw.put("numhjsc", hr.hours);// 婚假人数
			hr = getHolidayEmpNumMonth(he, dw, "5", includechld, ym);
			dw.put("numcj", hr.empnum);// 产假
			dw.put("numcjsc", hr.hours);// 产假
			hr = getHolidayEmpNumMonth(he, dw, "7", includechld, ym);
			dw.put("numsnj", hr.empnum);// 丧假
			dw.put("numsnjsc", hr.hours);// 丧假
			hr = getHolidayEmpNumMonth(he, dw, "8", includechld, ym);
			dw.put("numbj", hr.empnum);// 病假
			dw.put("numbjsc", hr.hours);// 病假
			hr = getHolidayEmpNumMonth(he, dw, "9", includechld, ym);
			dw.put("numgsj", hr.empnum);// 工伤
			dw.put("numgsjsc", hr.hours);// 工伤

			hr = getKQRstEmpNum(he, dw, 1, includechld, bgdate, eddate);// 旷工（旷工）
			dw.put("numgkgkg", hr.empnum);
			dw.put("numgkgkgsc", hr.hours);
			dw.put("numgkgcd", getKQRstEmpNum(he, dw, 2, includechld, bgdate, eddate).empnum);// 旷工（迟到）
			dw.put("numgkgzt", getKQRstEmpNum(he, dw, 3, includechld, bgdate, eddate).empnum);// 旷工（早退）
			dw.put("numgcd", getKQRstEmpNum(he, dw, 4, includechld, bgdate, eddate).empnum);// 迟到人数
			dw.put("numgzt", getKQRstEmpNum(he, dw, 5, includechld, bgdate, eddate).empnum);// 早退人数
			hr = getOvertimeEmpnum(he, dw, includechld, bgdate, eddate, 1);// 平日
			dw.put("numjbpr", hr.empnum);
			dw.put("numjbprsc", hr.hours);
			hr = getOvertimeEmpnum(he, dw, includechld, bgdate, eddate, 2);// 休息日
			dw.put("numjbxx", hr.empnum);
			dw.put("numjbxxsc", hr.hours);
			hr = getOvertimeEmpnum(he, dw, includechld, bgdate, eddate, 3);// 法定
			dw.put("numjbfd", hr.empnum);
			dw.put("numjbfdsc", hr.hours);
			hr = getOvertimeEmpnum(he, dw, includechld, bgdate, eddate, 0);// 法定
			dw.put("numjb", hr.empnum);
			dw.put("numjbsc", hr.hours);
		}
		String scols = urlparms.get("cols");
		if (scols == null) {
			return dws.toString();
		} else {
			(new CReport()).export2excel(dws, scols);
			return null;
		}
	}

	// 在职人数
	private int getNeedKQEmp(Hr_employee he, JSONObject dw, boolean includechld) throws Exception {
		String sqlstr = "select count(*) ct from hr_employee where empstatid in(select  statvalue from hr_employeestat where isstaffefficiency=1)";
		if (includechld)
			sqlstr = sqlstr + " AND idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND orgid=" + dw.getString("orgid");
		return Integer.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("ct"));
	}

	// 期间入职人数
	private int getintoNumEmp(Hr_employee he, JSONObject dw, boolean includechld, Date bgdate, Date eddate) throws NumberFormatException, Exception {
		String sqlstr = "SELECT   COUNT(*) ct FROM hr_employee WHERE hiredday>='" + Systemdate.getStrDateyyyy_mm_dd(bgdate) + "' AND  hiredday<'"
				+ Systemdate.getStrDateyyyy_mm_dd(eddate) + "' ";
		if (includechld)
			sqlstr = sqlstr + " AND idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND orgid=" + dw.getString("orgid");
		return Integer.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("ct"));
	}

	// 期间离职人数
	private int getLeaveNumEmp(Hr_employee he, JSONObject dw, boolean includechld, Date bgdate, Date eddate) throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "SELECT COUNT(*) ct FROM hr_employee e, " + " (SELECT DISTINCT er_id FROM("
				+ " SELECT hr_leavejob.er_id FROM hr_leavejob WHERE stat=9 AND ljdate>='" + bs + "' AND ljdate<'" + es + "'" + " UNION all "
				+ " SELECT l.er_id FROM hr_leavejobbatch h,hr_leavejobbatchline l " + " WHERE h.stat=9 AND h.ljbid=l.ljbid AND  l.ljdate>='" + bs
				+ "' AND l.ljdate<'" + es + "') tb) tb1" + " WHERE e.er_id=tb1.er_id ";
		if (includechld)
			sqlstr = sqlstr + " AND e.idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND e.orgid=" + dw.getString("orgid");
		return Integer.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("ct"));
	}

	// 期间调入人数
	private int getTrintoEmpNum(Hr_employee he, JSONObject dw, boolean includechld, Date bgdate, Date eddate) throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "select count(*) ct from shworg o," + " (select distinct * from "
				+ " (select er_id,neworgid orgid from hr_employee_transfer where stat=9 and tranfcmpdate>='" + bs + "' and tranfcmpdate<'" + es + "'"
				+ " union all " + " select l.er_id,h.neworgid orgid  from hr_emptransferbatch h,hr_emptransferbatch_line l"
				+ " where h.emptranfb_id=l.emptranfb_id and h.stat=9 and h.tranfcmpdate>='" + bs + "' and h.tranfcmpdate<'" + es + "') tb) tb2"
				+ " where o.orgid=tb2.orgid";
		if (includechld)
			sqlstr = sqlstr + " AND o.idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND o.orgid=" + dw.getString("orgid");
		return Integer.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("ct"));
	}

	// 期间调出人数
	private int getTroutEmpNum(Hr_employee he, JSONObject dw, boolean includechld, Date bgdate, Date eddate) throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "select count(*) ct from shworg o," + " (select distinct * from "
				+ " (select er_id,odorgid orgid from hr_employee_transfer where stat=9 and tranfcmpdate>='" + bs + "' and tranfcmpdate<'" + es + "'"
				+ " union all " + " select l.er_id,h.olorgid orgid  from hr_emptransferbatch h,hr_emptransferbatch_line l"
				+ " where h.emptranfb_id=l.emptranfb_id and h.stat=9 and h.tranfcmpdate>='" + bs + "' and h.tranfcmpdate<'" + es + "') tb) tb2"
				+ " where o.orgid=tb2.orgid";
		if (includechld)
			sqlstr = sqlstr + " AND o.idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND o.orgid=" + dw.getString("orgid");
		return Integer.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("ct"));
	}

	// 期间请假人数 时长
	private class NumHour {
		public int empnum;
		public float hours;

		public NumHour(int empnum, float hours) {
			this.empnum = empnum;
			this.hours = hours;
		}
	}

	/**
	 * 获取月度请假人数及时常
	 * 
	 * @param he
	 * @param dw 机构 或 人事资料 ；机构： 查机构合计，人事资料：查一个人的
	 * @param bhtype
	 * @param includechld
	 * @param ym
	 * @return
	 * @throws Exception
	 */
	private NumHour getHolidayEmpNumMonth(Hr_employee he, JSONObject dw, String bhtype, boolean includechld, String ym) throws Exception {
		String sqlstr = "SELECT h.*,l.lhdays,l.lhdaystrue FROM  hrkq_holidayapp h,hrkq_holidayapp_month l "
				+ " WHERE h.stat = 9 AND (h.htconfirm<>2 OR h.htconfirm IS NULL) AND h.htid IN " + "   (SELECT htid FROM hrkq_holidaytype  WHERE bhtype = " + bhtype + ") "
				+ "   AND h.haid=l.haid AND l.yearmonth='" + ym + "'";
		if (dw.has("er_id")) {
			sqlstr = sqlstr + " AND h.er_id=" + dw.getString("er_id");
		} else {
			if (includechld)
				sqlstr = sqlstr + " AND h.idpath LIKE '" + dw.getString("idpath") + "%'";
			else
				sqlstr = sqlstr + " AND h.orgid=" + dw.getString("orgid");
		}
		JSONArray apps = HRUtil.getReadPool().opensql2json_O(sqlstr);
		int hours = 0;
		for (int i = 0; i < apps.size(); i++) {
			JSONObject app = apps.getJSONObject(i);
			hours += (app.getDouble("lhdaystrue") * 8);
		}
		return new NumHour(apps.size(), hours);
	}

	private NumHour getHolidayEmpNum(Hr_employee he, JSONObject dw, String bhtype, boolean includechld, Date bgdate, Date eddate) throws Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "SELECT * FROM hrkq_holidayapp WHERE (htconfirm<>2 OR htconfirm IS NULL) and stat=9 "
				+ "and htid in(select htid from hrkq_holidaytype where bhtype=" + bhtype
				+ ") and (((timebg<='" + bs + "')and(timeedtrue>='" + bs + "'))or((timebg>='" + bs + "')and(timebg<='" + es + "')))";
		if (includechld)
			sqlstr = sqlstr + " AND idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND orgid=" + dw.getString("orgid");
		JSONArray apps = HRUtil.getReadPool().opensql2json_O(sqlstr);

		long bgt = bgdate.getTime();
		long edt = eddate.getTime();
		float hours = 0;
		for (int i = 0; i < apps.size(); i++) {
			JSONObject app = apps.getJSONObject(i);
			Date timebg = Systemdate.getDateByStr(app.get("timebg").toString());
			Date timeedtrue = Systemdate.getDateByStr(app.get("timeedtrue").toString());
			long ft = timebg.getTime();
			long tt = timeedtrue.getTime();
			Date bt = null, et = null;
			if ((ft <= bgt) && (tt > bgt) && (tt <= edt)) {
				bt = bgdate;
				et = timeedtrue;
			} else if ((ft <= bgt) && (tt >= edt)) {
				bt = bgdate;
				et = eddate;
			} else if ((ft >= bgt) && (ft <= edt) && (tt >= edt)) {
				bt = timebg;
				et = eddate;
			} else if ((ft >= bgt) && (ft <= edt) && (tt <= edt)) {
				bt = timebg;
				et = timeedtrue;
			}
			if ((bt == null) || (et == null))
				continue;
			hours = hours + CacalKQData.calcDateDiffHH(bt, et);
		}

		return new NumHour(apps.size(), hours);
	}

	// 期间考勤结果 人次 rsttp
	// 1:旷工（旷工） 上下班都没打卡 或 上班旷工（迟到） 且 下班旷工（早退）
	// 2:旷工（迟到） 上班 旷工（迟到），下班不是 旷工（早退），不是无打卡
	// 3:旷工（早退） 下班旷工（早退），上班不是无打卡 不是旷工（迟到）
	// 4:迟到 上班迟到 下班无所谓
	// 5:早退 下班早退 上班无所谓
	private NumHour getKQRstEmpNum(Hr_employee he, JSONObject dw, int rsttp, boolean includechld, Date bgdate, Date eddate)
			throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "select COUNT(DISTINCT er_id) empnum,IFNULL(SUM(mtslate + mtslearly),0) minute from hrkq_bckqrst where kqdate>='" + bs
				+ "' and kqdate<'" + es + "' and EXISTS(" + "select 1 from hr_employee where hrkq_bckqrst.er_id=hr_employee.er_id ";
		if (includechld)
			sqlstr = sqlstr + " AND hr_employee.idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND hr_employee.orgid=" + dw.getString("orgid");
		sqlstr = sqlstr + ")";

		if (rsttp == 1) {// 旷工
			sqlstr = sqlstr + " and lrst =12 ";
		} else if (rsttp == 2) {// 旷工（迟到）
			sqlstr = sqlstr + "  and lrst =9 ";
		} else if (rsttp == 3) {// 旷工（早退）
			sqlstr = sqlstr + " and lrst=10 ";
		} else if (rsttp == 4) {// 迟到
			sqlstr = sqlstr + " and lrst in(2,11) ";
		} else if (rsttp == 5) {// 早退
			sqlstr = sqlstr + " and lrst in(3,11) ";
		} else
			throw new Exception("参数错误");

		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);

		if (lm.size() == 0)
			return new NumHour(0, 0);
		return new NumHour(Integer.valueOf(lm.get(0).get("empnum")), Float.valueOf(lm.get(0).get("minute")) / 60);
	}

	// 加班人数 时长 0 所有 1平日 2休息日 3法定假 4特殊
	private NumHour getOvertimeEmpnum(Hr_employee he, JSONObject dw, boolean includechld, Date bgdate, Date eddate, int tp)
			throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);

		String sqlstr = "select count(*) empnum,ifnull(sum(othours),0) othours from(SELECT DISTINCT * FROM hrkq_overtime_list WHERE ((bgtime >='" + bs
				+ "' AND bgtime<'" + es + "') OR(edtime >='" + bs + "' AND edtime<'" + es + "'))  ";
		if (tp != 0) {
			sqlstr = sqlstr + " and otltype=1 "
					+ "AND EXISTS(SELECT 1 FROM  hrkq_overtime WHERE  hrkq_overtime.stat=9 AND  hrkq_overtime.over_type=" + tp + " AND  hrkq_overtime.ot_id=hrkq_overtime_list.ot_id) ";
		}

		sqlstr = sqlstr + "AND EXISTS(SELECT 1 FROM hr_employee WHERE hrkq_overtime_list.er_id=hr_employee.er_id ";
		if (includechld)
			sqlstr = sqlstr + " AND hr_employee.idpath LIKE '" + dw.getString("idpath") + "%'";
		else
			sqlstr = sqlstr + " AND hr_employee.orgid=" + dw.getString("orgid");
		sqlstr = sqlstr + ")";
		sqlstr = sqlstr + ") tb";
		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);

		if (lm.size() == 0)
			return new NumHour(0, 0);
		return new NumHour(Integer.valueOf(lm.get(0).get("empnum")), Float.valueOf(lm.get(0).get("othours")));
	}

	@ACOAction(eventname = "leave_detailinfo", Authentication = true, notes = "调休明细")
	public String leave_detailinfo() throws Exception {
		String[] notnull = {};
		String sqlstr = "SELECT w.wocode,w.employee_code,w.employee_name,w.sp_name,w.hg_name,w.lv_num,w.orgname,"
				+ " w.begin_date,w.end_date,w.reason,w.remark,w.stat,ws.stype,ws.lbname,ws.alllbtime,ws.wotime," + " ws.sccode,w.idpath"
				+ " FROM hrkq_wkoff w, hrkq_wkoffsourse ws" + " WHERE w.woid=ws.woid AND w.stat<=9 and ws.wotime>0 ";
		return new CReport(HRUtil.getReadPool(), sqlstr, " begin_date desc ", notnull).findReport();
	}

	@ACOAction(eventname = "kqcqrpt", Authentication = true, notes = "考勤超签报表")
	public String kqcqrpt() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();

		String ps = parms.get("parms");
		if (ps == null) {
			throw new Exception("需要参数kqdate");
		}
		List<JSONParm> jps = CJSON.getParms(ps);
		JSONParm jp = getParmByName(jps, "kqdate");
		if (jp == null) {
			throw new Exception("需要参数kqdate");
		}
		Date kqdate = Systemdate.getDateByStr(jp.getParmvalue());

		jp = getParmByName(jps, "orgcode1");
		if (jp == null) {
			throw new Exception("需要参数orgcode1");
		}
		String orgcode = jp.getParmvalue();
		Shworg org = new Shworg();
		org.findBySQL("select * from shworg where code='" + orgcode + "'", false);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");

		String bgtimestr = Systemdate.getStrDateByFmt(kqdate, "yyyy-MM-01 00:00:00");
		String edtimestr = Systemdate.getStrDateByFmt(Systemdate.dateMonthAdd(kqdate, 1), "yyyy-MM-01 00:00:00");

		delParmByName(jps, "kqdate");
		delParmByName(jps, "orgcode1");

		String kqdatestr = Systemdate.getStrDateByFmt(kqdate, "yyyy-MM");

		String sqlstr = "SELECT '" + kqdatestr + "' kqmonth, tb.*,IFNULL(m.resigntimes,3) mxrstime FROM "
				+ " (SELECT e.orgid,e.orgcode,e.orgname,r.res_type,e.idpath,r.er_id,r.employee_code,r.employee_name,e.sp_name,e.lv_num,COUNT(*) rstime "
				+ " FROM hrkq_resign r,hrkq_resignline rl,hr_employee e "
				+ " WHERE r.stat=9 AND rl.isreg=1 and rl.ri_times=1 AND r.resid=rl.resid AND r.er_id=e.er_id " + " and r.resdate>='" + bgtimestr
				+ "' and r.resdate<'" + edtimestr + "'" + " and e.idpath like '" + org.idpath.getValue() + "%' " + " GROUP BY e.orgid,r.er_id,r.res_type)tb "
				+ " LEFT JOIN (select * from hrkq_resigntimeparm where usable=1) m ON tb.er_id=m.er_id";
		String[] notnull = {};
		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, notnull).findReport2JSON_O();
		// rst.put("total", size);
		// rst.put("rows", rows);
		JSONArray rows = rst.getJSONArray("rows");
		for (int i = 0; i < rows.size(); i++) {
			JSONObject row = rows.getJSONObject(i);
			if (row.getInt("res_type") == 1) {// 因公补签不计补签
				row.put("cqrstime", 0);
				row.put("mxrstime", 0);
			} else {
				int cbrstime = row.getInt("rstime") - row.getInt("mxrstime");
				cbrstime = (cbrstime < 0) ? 0 : cbrstime;
				row.put("cqrstime", cbrstime);
			}
		}

		String scols = parms.get("cols");
		if (scols == null) {
			return rows.toString();
		} else {
			(new CReport()).export2excel(rows, scols);
			return null;
		}
	}

	private JSONParm getParmByName(List<JSONParm> jps, String pname) {
		for (JSONParm jp : jps) {
			if (jp.getParmname().equals(pname))
				return jp;
		}
		return null;
	}

	private void delParmByName(List<JSONParm> jps, String pname) {
		for (JSONParm jp : jps) {
			if (jp.getParmname().equals(pname)) {
				jps.remove(jp);
				return;
			}
		}
	}

	@ACOAction(eventname = "perworkoff", Authentication = true, notes = "调休一览表")
	public String perworkoff() throws Exception {
		HashMap<String, String> parms = CSContext.getParms();
		String er_id = CorUtil.hashMap2Str(parms, "er_id", "需要参数er_id");
		String sqlstr = String.format(sqlstr_1, er_id);
		CDBPool pool = HRUtil.getReadPool();

		JSONObject pwo = new JSONObject();

		JSONArray rst = pool.opensql2json_O(sqlstr);
		Hrkq_overtime_list ol = new Hrkq_overtime_list();
		Hrkq_business_trip trip = new Hrkq_business_trip();
		Hrkq_special_holday sh = new Hrkq_special_holday();
		for (int i = 0; i < rst.size(); i++) {
			JSONObject jo = rst.getJSONObject(i);
			int stype = Integer.valueOf(jo.getString("stype"));// 源类型 1 年假 2 加班
			// 3 值班 4出差 5特殊
			String sid = jo.getString("sid");
			if (stype == 2) {
				ol.findByID(sid, false);
				if (!ol.isEmpty()) {
					jo.put("bgtime", ol.bgtime.getValue());
					jo.put("edtime", ol.edtime.getValue());
				}
			}
			if (stype == 3) {
				List<HashMap<String, String>> dt = pool.openSql2List("SELECT begin_date bgtime, end_date edtime FROM hrkq_ondutyline WHERE odlid=" + sid);
				if (dt.size() > 0) {
					jo.put("bgtime", dt.get(0).get("bgtime"));
					jo.put("edtime", dt.get(0).get("edtime"));
				}
			}

			if (stype == 4) {
				trip.findByID(sid, false);
				if (!trip.isEmpty()) {
					jo.put("bgtime", trip.begin_date.getValue());
					jo.put("edtime", trip.end_date.getValue());
				}
			}

			if (stype == 5) {
				sh.findByID(sid, false);
				if (!sh.isEmpty()) {
					jo.put("bgtime", sh.sphdate.getValue());
					jo.put("edtime", sh.sphdate.getValue());
				}
			}

			float alllbtime = Float.valueOf(jo.getString("alllbtime"));
			alllbtime = (float) Math.round((alllbtime / 8) * 100) / 100;
			jo.put("alllbtime", alllbtime);
			float freetime = Float.valueOf(jo.getString("freetime"));
			// System.out.println("freetime：" + freetime);
			freetime = (float) Math.round((freetime / 8) * 100) / 100;
			jo.put("freetime", freetime);
		}
		pwo.put("ktx", rst);

		sqlstr = String.format(sqlstr_3, er_id);
		rst = pool.opensql2json_O(sqlstr);
		for (int i = 0; i < rst.size(); i++) {
			JSONObject jo = rst.getJSONObject(i);
			int stype = Integer.valueOf(jo.getString("stype"));// 源类型 1 年假 2 加班
			// 3 值班 4出差 5特殊
			String sid = jo.getString("sid");
			if (stype == 2) {
				ol.findByID(sid, false);
				if (!ol.isEmpty()) {
					jo.put("bgtime", ol.bgtime.getValue());
					jo.put("edtime", ol.edtime.getValue());
				}
			}
			if (stype == 3) {
				List<HashMap<String, String>> dt = pool.openSql2List("SELECT MIN(begin_date) bgtime FROM hrkq_ondutyline WHERE od_id=" + sid);
				if (dt.size() > 0)
					jo.put("bgtime", dt.get(0).get("bgtime"));
				dt = pool.openSql2List("SELECT MAX(end_date) edtime FROM hrkq_ondutyline WHERE od_id=" + sid);
				if (dt.size() > 0)
					jo.put("edtime", dt.get(0).get("bgtime"));
			}

			if (stype == 4) {
				trip.findByID(sid, false);
				if (!trip.isEmpty()) {
					jo.put("bgtime", trip.begin_date.getValue());
					jo.put("edtime", trip.end_date.getValue());
				}
			}

			if (stype == 5) {
				sh.findByID(sid, false);
				if (!sh.isEmpty()) {
					jo.put("bgtime", sh.sphdate.getValue());
					jo.put("edtime", sh.sphdate.getValue());
				}
			}

			float alllbtime = Float.valueOf(jo.getString("alllbtime"));
			alllbtime = (float) Math.round((alllbtime / 8) * 100) / 100;
			jo.put("alllbtime", alllbtime);
			float freetime = Float.valueOf(jo.getString("freetime"));
			// System.out.println("freetime：" + freetime);
			freetime = (float) Math.round((freetime / 8) * 100) / 100;
			jo.put("freetime", freetime);
		}
		pwo.put("gq", rst);

		sqlstr = String.format(sqlstr_2, er_id);
		rst = pool.opensql2json_O(sqlstr);
		for (int i = 0; i < rst.size(); i++) {
			JSONObject jo = rst.getJSONObject(i);
			int stype = Integer.valueOf(jo.getString("stype"));// 源类型 1 年假 2 加班
			// 3 值班 4出差 5特殊
			String sid = jo.getString("sid");
			if (stype == 2) {
				ol.findByID(sid, false);
				if (!ol.isEmpty()) {
					jo.put("bgtime", ol.bgtime.getValue());
					jo.put("edtime", ol.edtime.getValue());
				}
			}
			if (stype == 3) {
				jo.put("bgtime", pool.openSql2List("SELECT MIN(begin_date) bgtime FROM hrkq_ondutyline WHERE od_id=" + sid).get(0).get("bgtime"));
				jo.put("edtime", pool.openSql2List("SELECT MAX(end_date) edtime FROM hrkq_ondutyline WHERE od_id=" + sid).get(0).get("bgtime"));
			}

			if (stype == 4) {
				trip.findByID(sid, false);
				if (!trip.isEmpty()) {
					jo.put("bgtime", trip.begin_date.getValue());
					jo.put("edtime", trip.end_date.getValue());
				}
			}
			// float wotime = Float.valueOf(jo.getString("valdate") + " " +
			// jo.getString("wotime"));
			float wotime = jo.getInt("wotime");
			wotime = (float) Math.round((wotime / 8) * 100) / 100;
			jo.put("wotime", wotime);
		}

		String scols = parms.get("cols");
		if (scols == null) {
			pwo.put("ytx", rst);
			return pwo.toString();
		} else {
			(new CReport()).export2excel(rst, scols);
			return null;
		}
	}

	private String sqlstr_1 = "SELECT er_id,stype,sid,sccode,hg_name,lv_num,orgname,SUM(alllbtime) alllbtime,SUM(alllbtime-usedlbtime) freetime,valdate,note "
			+ " FROM hrkq_leave_blance " + " WHERE er_id=%s AND alllbtime > usedlbtime and valdate >=  CURDATE()" + " GROUP BY stype,sid,valdate"
			+ " order by lbid desc  ";

	private String sqlstr_3 = "SELECT er_id,stype,sid,sccode,hg_name,lv_num,orgname,SUM(alllbtime) alllbtime,SUM(alllbtime-usedlbtime) freetime,valdate,note "
			+ " FROM hrkq_leave_blance " + " WHERE er_id=%s AND alllbtime > usedlbtime and valdate <  CURDATE()" + " GROUP BY stype,sid,valdate"
			+ " order by lbid desc  ";

	private String sqlstr_2 = "SELECT wf.er_id,lb.stype,lb.sid,lb.sccode,lb.hg_name,lb.lv_num,wf.orgname,wfs.wotime,wf.wocode,wf.begin_date,wf.end_date,lb.note "
			+ " FROM  hrkq_wkoff wf,hrkq_wkoffsourse wfs,hrkq_leave_blance lb "
			+ " WHERE wf.stat=9 and wf.woid=wfs.woid AND wfs.lbid=lb.lbid AND wf.er_id=%s AND wotime>0 order by wf.begin_date desc ";

	@ACOAction(eventname = "longholidayrpt", Authentication = true, notes = "长假报表")
	public String longholidayrpt() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();

		String ps = parms.get("parms");
		if (ps == null) {
			throw new Exception("缺少参数");
		}
		List<JSONParm> jps = CJSON.getParms(ps);

		JSONParm jp = getParmByName(jps, "hdmonth");
		if (jp == null) {
			throw new Exception("需要参数hdmonth");
		}
		Date kqdate = Systemdate.getDateByStr(jp.getParmvalue());

		jp = getParmByName(jps, "orgcode");
		if (jp == null) {
			throw new Exception("需要参数orgcode");
		}
		String orgcode = jp.getParmvalue();
		Shworg org = new Shworg();
		org.findBySQL("select * from shworg where code='" + orgcode + "'", false);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");

		String cj = HrkqUtil.getParmValueErr("LONG_HOLDAY_MINDAYS");// 超过X天，记为长假
		String sd = HrkqUtil.getParmValueErr("LONG_HOLDAY_MAXMDAY").trim();// 未在当月X日前销假停发当月工资
		if (sd.length() == 1)
			sd = "0" + sd;

		String bg = Systemdate.getStrDateByFmt(Systemdate.dateMonthAdd(kqdate, -1), "yyyy-MM-01");
		String ed = Systemdate.getStrDateByFmt(Systemdate.dateMonthAdd(Systemdate.getDateByStr(bg), 1), "yyyy-MM-01");
		kqdate = Systemdate.dateMonthAdd(kqdate, 1);// 检索下一个月的请假单
		String bd = Systemdate.getStrDateByFmt(kqdate, "yyyy-MM-01");
		String md = Systemdate.getStrDateByFmt(kqdate, "yyyy-MM-" + sd);
//		String sqlstr = "SELECT *,IF(timebk IS NULL,2,1) istimebk FROM hrkq_holidayapp WHERE stat=9 ";// and
//		sqlstr = sqlstr + " AND  (viodeal<>2 OR viodeal IS NULL) ";// 没有无效
//		sqlstr = sqlstr + " AND (timebk IS NULL OR timebk>'" + md + "') ";
//
//		sqlstr = sqlstr + " AND hdays>" + cj + " AND ((timeed>'" + bg + "' AND timeed<'" + ed + "') OR (timeed>='" + ed + "') AND timebg<'" + bg + "')"
//				+ " AND idpath LIKE '" + org.idpath.getValue() + "%'";

		String sqlstr = "SELECT *,IF(timebk IS NULL,2,1) istimebk FROM hrkq_holidayapp WHERE stat=9";
		sqlstr = sqlstr + " AND  (viodeal<>2 OR viodeal IS NULL) ";// 没有无效
		sqlstr = sqlstr + " AND hdays>" + cj + " AND idpath LIKE '" + org.idpath.getValue() + "%'";
		sqlstr = sqlstr + " AND timebg<='" + md + "' AND timeed>'" + bd + "' AND (timebk is null or timebk>'" + md + "')";

		return new CReport(HRUtil.getReadPool(), sqlstr, null, new String[] {}).findReport(new String[] { "hdmonth", "orgcode" });
	}

	@ACOAction(eventname = "longholidaybarpt", Authentication = true, notes = "长假补发薪报表")
	public String longholidaybarpt() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();

		String ps = parms.get("parms");
		if (ps == null) {
			throw new Exception("缺少参数");
		}
		List<JSONParm> jps = CJSON.getParms(ps);

		JSONParm jp = getParmByName(jps, "hdmonth");
		if (jp == null) {
			throw new Exception("需要参数hdmonth");
		}
		Date kqdate = Systemdate.getDateByStr(jp.getParmvalue());
		String hapym = Systemdate.getStrDateByFmt(kqdate, "yyyy-MM");

		jp = getParmByName(jps, "orgcode");
		if (jp == null) {
			throw new Exception("需要参数orgcode");
		}
		String orgcode = jp.getParmvalue();
		Shworg org = new Shworg();
		org.findBySQL("select * from shworg where code='" + orgcode + "'", false);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");
		String sqlstr = "SELECT tb.*,ha.hdays,ha.htname,ha.timebk FROM(" +
				"SELECT b.hapym,b.hacid,b.haccode,b.haid,b.hacode,b.er_id,b.employee_code,b.employee_name, " +
				"b.orgid,b.orgcode,b.orgname,b.ospid,b.ospcode,b.sp_name,b.lv_num,b.hdaystrue,b.timebg,b.timeed, " +
				"GROUP_CONCAT(habpym) habpym " +
				"FROM `hrkq_holidayapp_backpay` b " +
				"WHERE b.hapym='" + hapym + "' "
				+ " AND b.idpath LIKE '" + org.idpath.getValue() + "%'"
				+ "GROUP BY haid) tb,hrkq_holidayapp ha WHERE tb.haid=ha.haid ";
		return new CReport(HRUtil.getReadPool(), sqlstr, null, new String[] {}).findReport(new String[] { "hdmonth", "orgcode" });
	}

	@ACOAction(eventname = "getkqorgmonthrptdetail", Authentication = true, notes = "机构考勤月度明细表")
	public String getkqorgmonthrptdetail() throws Exception {
		// List<Hrkq_sched> scs = new ArrayList<Hrkq_sched>();// 用来做班制缓存
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(urlparms.get("parms"));
		JSONParm jporgcode = CjpaUtil.getParm(jps, "orgcode");
		if (jporgcode == null)
			throw new Exception("需要参数【orgcode】");
		JSONParm jpdqdate = CjpaUtil.getParm(jps, "dqdate");
		if (jpdqdate == null)
			throw new Exception("需要参数【dqdate】");

		boolean edk = ConstsSw.getSysParmIntDefault("ENTRYDAYKQ", 1) == 1;// 入职当天计算考勤 1是 2否
		boolean lvk = ConstsSw.getSysParmIntDefault("LEAVDAYKQ", 1) == 1;// 离职当天计算考勤 1是 2否

		String orgcode = jporgcode.getParmvalue();
		String dqdate = jpdqdate.getParmvalue();
		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(dqdate)));// 去除时分秒
		Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
		String yearmonth = Systemdate.getStrDateByFmt(Systemdate.getDateByStr(dqdate), "yyyy-MM");
		String sqlstr = "select * from shworg where code='" + orgcode + "'";
		Shworg org = new Shworg();
		org.findBySQL(sqlstr);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");
		sqlstr = "SELECT * FROM hr_employee WHERE empstatid>0 and (empstatid NOT IN(6,11,12,13) OR(empstatid IN(11,12,13) AND kqdate_end>='" + yearmonth
				+ "-01'))" + " and idpath like '" + org.idpath.getValue() + "%' and kqdate_start<'" + Systemdate.getStrDateByFmt(eddate, "yyyy-MM-dd") + "'";
		String[] ignParms = { "orgcode" };
		String scols = urlparms.get("cols");
		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms);
		JSONArray emps = rst.getJSONArray("rows");
		// System.out.println("1111111111111111111111111111:" + emps.size());
		for (int i = 0; i < emps.size(); i++) {
			JSONObject jo = emps.getJSONObject(i);
			// if ("OO".equalsIgnoreCase(jo.getString("hwc_namezl")))
			// jo.put("zwxz", "非脱产");
			// else
			// jo.put("zwxz", "脱产");
			jo.put("zwxz", jo.has("emnature") ? jo.getString("emnature") : "");

			mq(jo, yearmonth, bgdate, eddate);// 满勤 及 法定假
			getHolidayEmpHour(jo, yearmonth);// 假期
			getOvertimeEmpHours(jo, bgdate, eddate);// 加班
			getKGCDZTDays(jo, bgdate, eddate, edk, lvk);// 旷工天数 迟到 早退次数
			// 实际出勤(不包含请假)；
			// 离职前、入职后、当前时间前
			rptresigntime(jo, bgdate, eddate);// 补签
			if(jo.get("kqdate_end")!=null){
				rptlvinfo(jo);// 离职类型
			}
		
			jo.put("nightworkdays", getNightDays(jo, bgdate, eddate, false));// 夜班上班天数
			float sjcq = (float) jo.getDouble("sjcq");
			sjcq = (float) (sjcq + jo.getDouble("gj") + jo.getDouble("hj")
					+ /* jo.getDouble("cj") + */jo.getDouble("snj") + jo.getDouble("gsj") + jo.getDouble("fdjq"));
			// 180114 客户要求实际出勤 加上法定假期
			// System.out.println("JSON:" + jo.toString());
			// float sjcq = (float) (jo.getDouble("ycmq") - jo.getDouble("sj") -
			// jo.getDouble("bj") - jo.getDouble("kgts"));
			if (sjcq < 0)
				sjcq = 0;
			float ycmq = (float) jo.getDouble("ycmq"); // 如果实际出勤大于应出满勤 ，则修改为应出满勤
			sjcq = (sjcq > ycmq) ? ycmq : sjcq;
			jo.put("sjcq", sjcq);// 实际出勤
		}
		/*
		 * for (int i = emps.size() - 1; i >= 0; i--) {// 要求删除未排版的（应出满勤为0）的记录 JSONObject jo = emps.getJSONObject(i); Double d = jo.getDouble("ycmq"); if (d == 0) { emps.remove(i); } }
		 */
		// System.out.println("JSON:" + rst.toString());
		if (scols == null) {
			return rst.toString();
		} else {
			(new CReport()).export2excel(rst.getJSONArray("rows"), scols);
			return null;
		}

	}

	/**
	 * @param jo
	 * @param all true：所有 false ：上班天数
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static float getNightDays(JSONObject jo, Date bgdate, Date eddate, Boolean all) throws NumberFormatException, Exception {
		String er_id = jo.getString("er_id");
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
//		String sqlstr = "SELECT IFNULL(SUM(s.allworktime) /8,0) days FROM hrkq_bckqrst r, hrkq_sched s,hrkq_sched_line sl "
//				+ "WHERE r.sclid=sl.sclid AND sl.scid=s.scid AND s.sctype=2 AND er_id=" + er_id + " and kqdate>='" + bs + "' and kqdate<='" + es + "'";
//		if (!all)
//			sqlstr = sqlstr + " AND r.lrst IN(1,2,3) ";

		String sqlstr = "select ifnull(sum(dd),0) days from "
				+ "(select  CASE  WHEN d>1 THEN 1 WHEN d>0.5 and d<1 THEN 0.5 ELSE 0 END dd from "
				+ "(SELECT  sum(s.`allworktime`) /7 d FROM "
				+ "  hrkq_bckqrst r,  hrkq_sched s,  hrkq_sched_line sl "
				+ "WHERE r.sclid = sl.sclid  AND sl.scid = s.scid  AND s.sctype = 2 "
				+ "  AND er_id = " + er_id
				+ "  and kqdate >= '" + bs + "' "
				+ "  and kqdate < '" + es + "' ";
		if (!all)
			sqlstr = sqlstr + " AND r.lrst IN(1,2,3) ";
		sqlstr = sqlstr + "  group by kqdate) tb) tb2";

		float days = Float.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("days").toString());
		// days = days - (days % (0.5f));// 比如数据库为16.875 应该计16.5
		return days;
	}

	/**
	 * @param ym 年月 yyyy-mm
	 * @param er_id 人事ID
	 * @return JSONObject sjcq:实际出勤;ycmq:应出满勤
	 * @throws Exception
	 */
	public static JSONObject getYCMQSJCQ(String ym, String er_id) throws Exception {
		boolean edk = ConstsSw.getSysParmIntDefault("ENTRYDAYKQ", 1) == 1;// 入职当天计算考勤
		// 1是
		// 2否
		boolean lvk = ConstsSw.getSysParmIntDefault("LEAVDAYKQ", 1) == 1;// 离职当天计算考勤
		// 1是
		// 2否

		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(ym)));// 去除时分秒
		Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
		String sqlstr = "SELECT * FROM hr_employee WHERE (empstatid NOT IN(6,11,12,13) OR(empstatid IN(11,12,13) AND kqdate_end>='" + ym + "-01'))"
				+ " and er_id=" + er_id + " and kqdate_start<'" + Systemdate.getStrDateByFmt(eddate, "yyyy-MM-dd") + "'";
		JSONArray emps = HRUtil.getReadPool().opensql2json_O(sqlstr);
		if (emps.size() <= 0)
			throw new Exception("没有合适的人事资料");
		JSONObject jo = emps.getJSONObject(0);
		mq(jo, ym, bgdate, eddate);// 满勤 及 法定假
		getHolidayEmpHour(jo, ym);// 假期
		// getOvertimeEmpHours(jo, bgdate, eddate);// 加班
		getKGCDZTDays(jo, bgdate, eddate, edk, lvk);// 旷工天数 迟到 早退次数 实际出勤(不包含请假)；
		// 离职前、入职后、当前时间前
		// rptresigntime(jo, bgdate, eddate);// 补签
		float sjcq = (float) jo.getDouble("sjcq");
		sjcq = (float) (sjcq + jo.getDouble("gj") + jo.getDouble("hj")
				+ /* jo.getDouble("cj") + */jo.getDouble("snj") + jo.getDouble("gsj") + jo.getDouble("fdjq"));
		// 180114 客户要求实际出勤 加上法定假期
		// System.out.println("JSON:" + jo.toString());
		// float sjcq = (float) (jo.getDouble("ycmq") - jo.getDouble("sj") -
		// jo.getDouble("bj") - jo.getDouble("kgts"));
		if (sjcq < 0)
			sjcq = 0;
		float ycmq = (float) jo.getDouble("ycmq"); // 如果实际出勤大于应出满勤 ，则修改为应出满勤
		sjcq = (sjcq > ycmq) ? ycmq : sjcq;
		jo.put("sjcq", sjcq);// 实际出勤
		return jo;
	}

	private Hrkq_sched getsched(List<Hrkq_sched> scs, String scid) throws Exception {
		for (Hrkq_sched sc : scs) {
			if (scid.equalsIgnoreCase(sc.scid.getValue()))
				return sc;
		}
		Hrkq_sched sc = new Hrkq_sched();
		sc.findBySQL("select * from hrkq_sched where scid=" + scid, false);
		if (sc.isEmpty())
			throw new Exception("ID为【" + scid + "】的班次不存在");
		scs.add(sc);
		return sc;
	}

	/**
	 * 获取满勤天数
	 * 
	 * @param jo
	 * @throws Exception
	 */
	private static void mq(JSONObject jo, String yearmonth, Date bgdate, Date eddate) throws Exception {
		Hrkq_workschmonthlist wml = new Hrkq_workschmonthlist();
		Object okqdate_end = jo.get("kqdate_end"); // ljdate-->kqdate_end
		String kqdate_end = ((okqdate_end == null) || (okqdate_end.toString().isEmpty())) ? null
				: Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(okqdate_end.toString()));

		Object okqdate_start = jo.get("kqdate_start"); // hiredday-->kqdate_start
		String kqdate_start = ((okqdate_start == null) || (okqdate_start.toString().isEmpty())) ? null
				: Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(okqdate_start.toString()));

		String er_id = jo.getString("er_id");
		String sqltem = "";
		for (int i = 1; i <= 31; i++) {
			String d = "00" + i;
			d = d.substring(d.length() - 2, d.length());
			sqltem = sqltem + "SELECT '" + yearmonth + "-" + d + "' d,scid" + i + " scid FROM hrkq_workschmonthlist WHERE er_id=" + er_id + " AND yearmonth='"
					+ yearmonth + "' UNION ";
		}
		if (!sqltem.isEmpty())
			sqltem = sqltem.substring(0, sqltem.length() - 6);
		String sqlstr = "SELECT IFNULL(SUM(l.dayratio),0) days FROM hrkq_sched_line l,(" + sqltem + ") tb WHERE l.scid=tb.scid ";
		// if (ljdate != null) 应出满勤与离职无关
		// sqlstr = sqlstr + " and d<='" + ljdate + "'";

		float days = Float.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("days")) / 100;
		days = days + days % (0.5f);

		sqlstr = "SELECT IFNULL(COUNT(*),0)  ct  FROM hrkq_ohyear WHERE ohdate>='" + Systemdate.getStrDateyyyy_mm_dd(bgdate) + "' AND ohdate<'"
				+ Systemdate.getStrDateyyyy_mm_dd(eddate) + "' AND iswork=2";
		if (kqdate_end != null)
			sqlstr = sqlstr + " and ohdate<='" + kqdate_end + "'";
		if (kqdate_start != null)
			sqlstr = sqlstr + " and ohdate>='" + kqdate_start + "'";

		float fddays = Float.valueOf(HRUtil.getReadPool().openSql2List(sqlstr).get(0).get("ct"));
		fddays = fddays + fddays % (0.5f);
		jo.put("ycmq", days + fddays);// 应出满勤
		jo.put("fdjq", fddays);
	}

	/**
	 * 请假时长 天
	 * 
	 * @param he
	 * @param dw
	 * @param includechld
	 * @param bgdate
	 * @param eddate
	 * @return
	 * @throws Exception
	 */
	private static void getHolidayEmpHour(JSONObject jo, String ym) throws Exception {
		String sqlstr = "SELECT  " + " IFNULL(SUM(CASE  WHEN t.bhtype =2 or h.viodeal=1 THEN l.lhdaystrue ELSE 0 END ),0) sj," // 事假或违规为事假
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =3 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) gj,"// 且没违规
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =4 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) hj, "
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =5 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) cj,"
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =7 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) snj,"
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =8 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) bj,"
				+ " IFNULL(SUM(CASE  WHEN t.bhtype =9 AND (htconfirm<>2 OR htconfirm IS NULL) THEN l.lhdaystrue ELSE 0 END ),0) gsj"
				+ " FROM  hrkq_holidayapp h,hrkq_holidayapp_month l,hrkq_holidaytype t " + " WHERE h.stat = 9 AND h.htid=t.htid "
				+ " AND h.haid=l.haid AND l.yearmonth='" + ym + "' AND er_id=" + jo.getString("er_id");
		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);
		jo.put("sj", Float.valueOf(lm.get(0).get("sj")));
		jo.put("gj", Float.valueOf(lm.get(0).get("gj")));
		jo.put("hj", Float.valueOf(lm.get(0).get("hj")));
		jo.put("cj", Float.valueOf(lm.get(0).get("cj")));
		jo.put("snj", Float.valueOf(lm.get(0).get("snj")));
		jo.put("bj", Float.valueOf(lm.get(0).get("bj")));
		jo.put("gsj", Float.valueOf(lm.get(0).get("gsj")));
	}

	private void getOvertimeEmpHours(JSONObject jo, Date bgdate, Date eddate) throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "SELECT  IFNULL(SUM(CASE  WHEN (over_type = 1 AND otltype IN(1,2))  THEN othours ELSE 0 END ),0) prjbss,"
				+ " IFNULL(SUM(CASE  WHEN (over_type = 2 AND otltype IN(1,2))  THEN othours ELSE 0 END ),0) zmjbss,"
				+ " IFNULL(SUM(CASE  WHEN (over_type = 3 AND otltype IN(1,2))  THEN othours ELSE 0 END ),0) fdjbss,"
				+ " IFNULL(SUM(CASE  WHEN otltype IN(3,4,5) THEN othours ELSE 0 END ),0) zlbjbss," + " IFNULL(SUM(othours),0) zjbss "
				+ " FROM hrkq_overtime_list  WHERE ((bgtime >='" + bs + "' AND bgtime<'" + es + "') OR(edtime >='" + bs + "' AND edtime<'" + es
				+ "')) and dealtype=2 AND er_id =" + jo.getString("er_id");// 只显示计算加班费的
		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);
		HashMap<String, String> m = lm.get(0);
		jo.put("prjbss", m.get("prjbss"));
		jo.put("zmjbss", m.get("zmjbss"));
		jo.put("fdjbss", m.get("fdjbss"));
		jo.put("zlbjbss", m.get("zlbjbss"));
		jo.put("zjbss", m.get("zjbss"));
	}

	/**
	 * @return 旷工天数,迟到次数,早退次数 离职前、当前日期之前
	 * @throws Exception
	 */
	private static void getKGCDZTDays(JSONObject jo, Date bgdate, Date eddate, boolean edk, boolean lvk) throws Exception {
		Object okqdate_end = jo.get("kqdate_end"); // ljdate-->kqdate_end
		String kqdate_end = ((okqdate_end == null) || (okqdate_end.toString().isEmpty())) ? null
				: Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(okqdate_end.toString()));

		Object okqdate_start = jo.get("kqdate_start"); // hiredday-->kqdate_start
		String kqdate_start = ((okqdate_start == null) || (okqdate_start.toString().isEmpty())) ? null
				: Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(okqdate_start.toString()));

		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		// String exs=Systemdate.getStrDateyyyy_mm_dd(new Date);
		String sqlstr = "SELECT " + "  IFNULL(SUM(CASE  WHEN r.lrst NOT IN(9,10,12,0,6) THEN sl.dayratio ELSE 0 END )/100,0) sjcq ,"
				+ "  IFNULL(SUM(CASE  WHEN r.lrst IN (9, 10, 12) THEN sl.dayratio ELSE 0 END )/100,0) kg ,"
				+ "  IFNULL(SUM(CASE  WHEN r.lrst =2 THEN 1 ELSE 0 END ),0) cd ," + "  IFNULL(SUM(CASE  WHEN r.lrst =3 THEN 1 ELSE 0 END ),0) zt "
				+ "FROM  hrkq_bckqrst r,hrkq_sched_line sl  " + " WHERE r.sclid=sl.sclid " + " and r.kqdate>='" + bs + "' and r.kqdate<'" + es
				+ "' and r.er_id  =" + jo.getString("er_id") + " and r.kqdate<'" + Systemdate.getStrDateyyyy_mm_dd() + "' ";
		if (kqdate_end != null) {
			if (lvk) {
				sqlstr = sqlstr + " and r.kqdate<='" + kqdate_end + "' ";
			} else
				sqlstr = sqlstr + " and r.kqdate<'" + kqdate_end + "' ";
		}

		if (kqdate_start != null) {
			if (edk)
				sqlstr = sqlstr + " and r.kqdate>='" + kqdate_start + "' ";
			else
				sqlstr = sqlstr + " and r.kqdate>'" + kqdate_start + "' ";
		}

		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);
		HashMap<String, String> m = lm.get(0);
		float kg = Float.valueOf(m.get("kg"));
		kg = kg + kg % (0.5f);
		jo.put("kgts", kg);

		float sjcq = Float.valueOf(m.get("sjcq"));
		sjcq = sjcq + sjcq % (0.5f);
		jo.put("sjcq", sjcq);

		jo.put("cdcs", m.get("cd"));
		jo.put("ztcs", m.get("zt"));
	}

	/**
	 * 实际出勤天数 不含请假
	 * 
	 * @param jo
	 * @param bgdate
	 * @param eddate
	 * @return
	 * @throws Exception
	 */
	private float getSJCQDaysex(JSONObject jo, Date bgdate, Date eddate) throws Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		//
		String sqlstr = "SELECT ifnull(SUM(l.dayratio)/100,0) days  " + " FROM hrkq_bckqrst r,hrkq_sched_line l " + " WHERE r.er_id=" + jo.getString("er_id")
				+ " AND lrst NOT IN(9,10,12,0,6) AND r.sclid=l.sclid " + " and r.kqdate>='" + bs + "' and r.kqdate<'" + es + "'";
		// System.out.println("getSJCQDays:" + sqlstr);
		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);
		float rst = Float.valueOf(lm.get(0).get("days"));
		rst = rst + rst % (0.5f);
		return rst;
	}

	// 期间考勤结果 次数
	// 1:旷工（旷工） 上下班都没打卡
	// 2:旷工（迟到） 上班未打卡 下班有打卡
	// 3:旷工（早退） 上班打卡 下班未打卡
	// 4:迟到 上班迟到 下班正常
	// 5:早退 下班早退 上班正常
	private int getKQTimes(JSONObject jo, int rsttp, Date bgdate, Date eddate) throws NumberFormatException, Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "select ifnull(count(*),0) ct from hrkq_bckqrst where kqdate>='" + bs + "' and kqdate<'" + es + "' and er_id  ="
				+ jo.getString("er_id");
		if (rsttp == 1) {// 旷工（旷工）
			sqlstr = sqlstr + " and lrst =12 ";
		} else if (rsttp == 2) {// 旷工（迟到）
			sqlstr = sqlstr + " and lrst =9 ";
		} else if (rsttp == 3) {// :旷工（早退）
			sqlstr = sqlstr + " and lrst=10 ";
		} else if (rsttp == 4) {// 4:迟到
			sqlstr = sqlstr + " and lrst in(2,11) ";
		} else if (rsttp == 5) {// 5:早退
			sqlstr = sqlstr + " and lrst in(3,11) ";
		} else
			throw new Exception("参数错误");

		List<HashMap<String, String>> lm = HRUtil.getReadPool().openSql2List(sqlstr);

		return Integer.valueOf(lm.get(0).get("ct"));
	}

	/**
	 * 超签次数
	 * 
	 * @param jo
	 * @param bgdate
	 * @param eddate
	 * @throws Exception
	 */
	private void rptresigntime(JSONObject jo, Date bgdate, Date eddate) throws Exception {
		String bs = Systemdate.getStrDateyyyy_mm_dd(bgdate);
		String es = Systemdate.getStrDateyyyy_mm_dd(eddate);
		String sqlstr = "SELECT  tb.*,IFNULL(m.resigntimes,3) mxrstime FROM " + " (SELECT r.er_id,COUNT(*) rstime " + " FROM hrkq_resign r,hrkq_resignline rl "
				+ " WHERE r.stat=9 AND rl.isreg=1 and rl.ri_times=1 AND r.resid=rl.resid " + " and r.resdate>='" + bs + "' and r.resdate<'" + es + "'"
				+ " and r.er_id  =" + jo.getString("er_id") + " and r.res_type=2 " + " GROUP BY r.orgid,r.er_id)tb "
				+ " LEFT JOIN hrkq_resigntimeparm m ON tb.er_id=m.er_id";
		JSONArray rs = HRUtil.getReadPool().opensql2json_O(sqlstr);
		if (rs.size() > 0) {
			JSONObject r = rs.getJSONObject(0);
			int cbrstime = r.getInt("rstime") - r.getInt("mxrstime");
			cbrstime = (cbrstime < 0) ? 0 : cbrstime;
			jo.put("cqcs", cbrstime);
		} else
			jo.put("cqcs", 0);
	}

	private void rptlvinfo(JSONObject jo) throws Exception {
		String sqlstr = "SELECT * FROM hr_leavejob WHERE stat=9 AND  er_id=" + jo.getString("er_id");
		Hr_leavejob lv = new Hr_leavejob();
		lv.findBySQL(sqlstr);
		if (!lv.isEmpty()) {
			jo.put("ljtype2", lv.ljtype2.getValue());
			jo.put("ljtype1", lv.ljtype1.getValue());
			jo.put("ljreason", lv.ljreason.getValue());
		}
	}

	@ACOAction(eventname = "kqholday_type_rpt", Authentication = true, notes = "休假统计报表")
	public String kqholday_type_rpt() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(urlparms.get("parms"));
		JSONParm jporgcode = CjpaUtil.getParm(jps, "orgcode");
		if (jporgcode == null)
			throw new Exception("需要参数【orgcode】");
		JSONParm jpdqdate = CjpaUtil.getParm(jps, "dqdate");
		if (jpdqdate == null)
			throw new Exception("需要参数【dqdate】");
		String orgcode = jporgcode.getParmvalue();
		String dqdate = jpdqdate.getParmvalue();
		String ym = Systemdate.getStrDateByFmt(Systemdate.getDateByStr(dqdate), "yyyy-MM");
		Date bgdate = Systemdate.getDateByStr(Systemdate.getStrDateyyyy_mm_dd(Systemdate.getDateByStr(dqdate)));// 去除时分秒
		Date eddate = Systemdate.dateMonthAdd(bgdate, 1);// 加一月
		Hr_employee he = new Hr_employee();
		String sqlstr = "select * from shworg where code='" + orgcode + "'";
		Shworg org = new Shworg();
		org.findBySQL(sqlstr);
		if (org.isEmpty())
			throw new Exception("编码为【" + orgcode + "】的机构不存在");
		sqlstr = "SELECT * FROM hr_employee WHERE (empstatid<10 OR ljdate>'" + Systemdate.getStrDateyyyy_mm_dd(eddate) + "' ) AND  idpath LIKE '"
				+ org.idpath.getValue() + "%'";

		String[] ignParms = { "orgcode", "dqdate" };
		JSONObject rst = new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport2JSON_O(ignParms, true, null);
		JSONArray emps = rst.getJSONArray("rows");// =
		// he.pool.opensql2json_O(sqlstr);

		for (int i = 0; i < emps.size(); i++) {
			JSONObject emp = emps.getJSONObject(i);
			NumHour hr = getHolidayEmpNumMonth(he, emp, "2", false, ym);
			emp.put("numsjsc", getDays(hr.hours));// 事假时长
			hr = getHolidayEmpNumMonth(he, emp, "3", false, ym);
			emp.put("numgjsc", getDays(hr.hours));// 公假时长
			hr = getHolidayEmpNumMonth(he, emp, "4", false, ym);
			emp.put("numhjsc", getDays(hr.hours));// 婚假人数
			hr = getHolidayEmpNumMonth(he, emp, "5", false, ym);
			emp.put("numcjsc", getDays(hr.hours));// 产假
			hr = getHolidayEmpNumMonth(he, emp, "7", false, ym);
			emp.put("numsnjsc", getDays(hr.hours));// 丧假
			hr = getHolidayEmpNumMonth(he, emp, "8", false, ym);
			emp.put("numbjsc", getDays(hr.hours));// 病假
			hr = getHolidayEmpNumMonth(he, emp, "9", false, ym);
			emp.put("numgsjsc", getDays(hr.hours));// 工伤
		}

		String scols = urlparms.get("cols");
		if (scols == null) {
			return rst.toString();
		} else {
			(new CReport()).export2excel(rst.getJSONArray("rows"), scols);
			return null;
		}
	}

	private float getDays(float hours) {
		float a = (hours / 8);
		return (float) (a - a % 0.5);
	}

	@ACOAction(eventname = "rpt_hrkq_queqing", Authentication = true, notes = "缺勤统计")
	public String rpt_hrkq_queqing() throws Exception {
		HashMap<String, String> urlparms = CSContext.get_pjdataparms();
		List<JSONParm> jps = CJSON.getParms(urlparms.get("parms"));
		JSONParm jpbt = CjpaUtil.getParm(jps, "bt");
		if (jpbt == null)
			throw new Exception("需要参数【bt】");
		JSONParm jpet = CjpaUtil.getParm(jps, "et");
		if (jpet == null)
			throw new Exception("需要参数【et】");
		String sqlwhere = CjpaUtil.buildFindSqlByJsonParm(DBType.mysql, Types.DATE, jpbt) + CjpaUtil.buildFindSqlByJsonParm(DBType.mysql, Types.DATE, jpet);

		JSONParm jpemployee_code = CjpaUtil.getParm(jps, "employee_code");
		if (jpemployee_code != null) {
			sqlwhere += CjpaUtil.buildFindSqlByJsonParm(DBType.mysql, Types.VARCHAR, jpemployee_code);
		}
		String sqlstr = "SELECT * FROM " +
				"(SELECT t.employee_code,t.employee_name,t.orgname,t.sp_name,t.lv_num, '请假' billtype,t.hacode billcode,t.timebg bt,t.timeedtrue et,t.hdays days,t.stat,t.htreason reason FROM hrkq_holidayapp t WHERE t.stat>1 AND t.stat<10 " +
				sqlwhere.replace("`bt`", "t.timebg").replace("`et`", "t.timeedtrue") +
				"UNION " +
				"SELECT t.employee_code,t.employee_name,t.orgname,t.sp_name,t.lv_num, '出差' billtype,t.bta_code billcode,t.begin_date bt,t.end_date et,t.tripdays days,t.stat,t.tripreason reason FROM hrkq_business_trip t WHERE t.stat>1 AND t.stat<10 "
				+
				sqlwhere.replace("`bt`", "t.begin_date").replace("`et`", "t.end_date") +
				"UNION " +
				"SELECT t.employee_code,t.employee_name,t.orgname,t.sp_name,t.lv_num, '调休' billtype,t.wocode billcode,t.begin_date bt,t.end_date et,t.wodays days,t.stat,t.reason FROM hrkq_wkoff t WHERE t.stat>1 AND t.stat<10" +
				sqlwhere.replace("`bt`", "t.begin_date").replace("`et`", "t.end_date") + ") tb WHERE 1=1  ";
		String[] ignParms = { "bt", "et", "employee_code" };
		return new CReport(HRUtil.getReadPool(), sqlstr, null, null).findReport(ignParms);
	}
}
