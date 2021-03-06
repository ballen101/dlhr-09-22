package com.hr.attd.ctr;

import java.util.Date;

import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.CJPABase.CJPAStat;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.util.Logsw;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.wordflow.Shwwf;
import com.corsair.server.wordflow.Shwwfproc;
import com.hr.attd.entity.Hrkq_overtime;
import com.hr.attd.entity.Hrkq_resign;
import com.hr.attd.entity.Hrkq_resignline;
import com.hr.attd.entity.Hrkq_sched_line;
import com.hr.attd.entity.Hrkq_swcdlst;
import com.hr.card.entity.Hr_ykt_card;
import com.hr.perm.entity.Hr_employee;
import com.hr.util.DateUtil;
import com.hr.util.HRUtil;

public class CtrHrkq_resign extends JPAController {

	@Override
	public void BeforeWFStartSave(CJPA jpa, Shwwf wf, CDBConnection con, boolean isFilished) throws Exception {
		// TODO Auto-generated method stub
		wf.subject.setValue(((Hrkq_resign) jpa).rescode.getValue());
		if (isFilished) {
			doinsertSWCardInfo(jpa, con);
		}
	}

	@Override
	public void BeforeWFStart(CJPA jpa, String wftempid, CDBConnection con) throws Exception {
		// 检查当月已经提交的因私签批数量
		checkysretimes(jpa, con);
		checkValidDate(jpa);
	}

	@Override
	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf, boolean isFilished)
			throws Exception {
		// TODO Auto-generated method stub
		checkresigned(jpa);// 不能用这个con
		
		if (isFilished) {
			doinsertSWCardInfo(jpa, con);
		}
	}

	@Override
	public String OnCCoVoid(CDBConnection con, CJPA jpa) throws Exception {
		dodelSWCardInfo(jpa, con);
		return null;
	}

	private void checkresigned(CJPA jpa) throws NumberFormatException, Exception {
		Hrkq_resign r = (Hrkq_resign) jpa;
		CJPALineData<Hrkq_resignline> ls = r.hrkq_resignlines;
		for (CJPABase j : ls) {
			Hrkq_resignline l = (Hrkq_resignline) j;
			String sqlstr = "SELECT IFNULL(COUNT(*),0) ct FROM `hrkq_resign` h,  `hrkq_resignline`  l"
					+ " WHERE h.resid =l.resid AND l.isreg=1"
					+ " AND l.`kqdate`='" + l.kqdate.getValue() + "' AND h.`er_id`=" + r.er_id.getValue() + " AND l.`sclid`=" + l.sclid.getValue()
					+ " AND h.`stat`>1 AND h.`stat`<10";
			if (Integer.valueOf(r.pool.openSql2List(sqlstr).get(0).get("ct")) != 0)
				throw new Exception("考勤日期【" + Systemdate.getStrDateyyyy_mm_dd(l.kqdate.getAsDatetime()) + "】班次号【" + l.bcno.getValue() + "】存在已经提交的补签记录");
		}
	}

	@Override
	public void OnWfSubmit(CJPA jpa, CDBConnection con, Shwwf wf, Shwwfproc proc,
			Shwwfproc nxtproc, boolean isFilished) throws Exception {
		// TODO Auto-generated method stub
		if (isFilished) {
			doinsertSWCardInfo(jpa, con);
		}
	}

	@Override
	public String OnCCoSave(CDBConnection con, CJPA jpa) throws Exception {
		Hrkq_resign r = (Hrkq_resign) jpa;
		if (r.stat.getAsIntDefault(0) <= 1) {// 制单状态保存 重新计算次数 保存之前 没有默认值 所有 为0
			checkresignlines(con, r);

			Date resdate = r.resdate.getAsDatetime();
			int rgedtimes = getResignTimesMonth(r.resid.getValue(), r.res_type.getAsIntDefault(0), r.er_id.getValue(), resdate);// 本月因私已签卡次数
			int curtimes = getCurTimes(r);
			// System.out.println("curtimes:" + curtimes);
			r.res_times.setAsInt(rgedtimes + curtimes);
		}
		return null;
	}

	private String parkqdt(Date kqdate, String time) {
		// System.out.println("time:" + time);
		Date rdate = kqdate;
		String rtime = time;
		String fs = rtime.substring(0, 1).toUpperCase();
		if (fs.equalsIgnoreCase("Y")) {// 前一天
			rdate = Systemdate.dateDayAdd(rdate, -1);
			rtime = rtime.substring(1, rtime.length());
		} else if (fs.equalsIgnoreCase("T")) {// 后一天
			rdate = Systemdate.dateDayAdd(rdate, 1);
			rtime = rtime.substring(1, rtime.length());
		}
		// rtime = rtime.substring(1, rtime.length() - 1);
		String rst = Systemdate.getStrDateyyyy_mm_dd(rdate) + " " + rtime;
		// System.out.println("parkqdt:" + rst);
		return rst;
	}

	private void checkresignlines(CDBConnection con, Hrkq_resign r) throws Exception {
		Hrkq_sched_line sl = new Hrkq_sched_line();

		CJPALineData<Hrkq_resignline> ls = r.hrkq_resignlines;
		for (CJPABase j : ls) {
			Hrkq_resignline l = (Hrkq_resignline) j;
			if (l.isreg.getAsIntDefault(0) == 1) {
				Date kqdate = l.kqdate.getAsDatetime();
				String sdt = Systemdate.getStrDateyyyy_mm_dd(kqdate);
				sl.clear();
				sl.findByID(l.sclid.getValue());
				if (sl.isEmpty()) {
					throw new Exception("ID为【" + l.sclid.getValue() + "】的班次不存在");
				}
				Date dtbg, dted;
				if (l.sxb.getAsInt() == 1) {
					dtbg = Systemdate.getDateByStr(parkqdt(kqdate, sl.frvtimebg.getValue()));
					dted = Systemdate.getDateByStr(parkqdt(kqdate, sl.frvtimeed.getValue()));
				} else {
					dtbg = Systemdate.getDateByStr(parkqdt(kqdate, sl.tovtimebg.getValue()));
					dted = Systemdate.getDateByStr(parkqdt(kqdate, sl.tovtimeed.getValue()));
				}
				String sqlstr = "select count(*) ct from hrkq_swcdlst where empno='" + r.employee_code.getValue()
						+ "' and skdate>='" + Systemdate.getStrDate(dtbg) + "' and skdate<='" + Systemdate.getStrDate(dted) + "'";
				if (Integer.valueOf(con.openSql2List(sqlstr).get(0).get("ct")) > 0)
					throw new Exception("工号【" + r.employee_code.getValue() + "】在【" + sdt + "】班次【" + sl.bcno.getValue() + "】已经打卡，不允许补签");
			}
		}
	}

	private int getCurTimes(Hrkq_resign r) {
		CJPALineData<Hrkq_resignline> ls = r.hrkq_resignlines;
		int curtimes = 0;// 本次签卡次数
		for (CJPABase j : ls) {
			Hrkq_resignline l = (Hrkq_resignline) j;
			if ((l.isreg.getAsIntDefault(0) == 1) && (l.getJpaStat() != CJPAStat.RSDEL))
				curtimes++;
		}
		return curtimes;

	}

	private void checkysretimes(CJPA jpa, CDBConnection con) throws Exception {
		Hrkq_resign r = (Hrkq_resign) jpa;
		if (r.res_type.getAsIntDefault(0) == 1)// 因公不管
			return;
		int curtimes = getCurTimes(r);
		if (curtimes == 0)
			throw new Exception("所有班次没有签卡，不予提交");
		Hr_employee emp = new Hr_employee();
		emp.findByID(r.er_id.getValue(), false);
		if (emp.isEmpty())
			throw new Exception("没有找到ID为【" + r.er_id.getValue() + "】的在职员工资料");
		// int resigntimes = emp.resigntimes.getAsIntDefault(3);// 允许签卡次数
		Date resdate = r.resdate.getAsDatetime();
		int rgedtimes = getResignTimesMonth(null, 2, r.er_id.getValue(), resdate);// 本月因私已签卡次数
		// if ((rgedtimes + curtimes) > resigntimes)
		// throw new Exception("本月累计已提交签卡【" + rgedtimes + "】+本次签卡【" + curtimes + "】>月度允许因私签卡次数【" + resigntimes + "】");
		r.res_times.setAsInt(rgedtimes + curtimes);// 更新补签次数
		r.save(con);
	}

	// 获取当月已经提交补签次数 res_type 1 因公 2因私
	public static int getResignTimesMonth(String resid, int res_type, String er_id, Date resdate) throws Exception {
		String sdb = Systemdate.getStrDateByFmt(resdate, "yyyy-MM-01");
		String sde = Systemdate.getStrDateByFmt(Systemdate.dateMonthAdd(resdate, 1), "yyyy-MM-01");
		String sqlstr = "SELECT COUNT(*) ct FROM hrkq_resign m,hrkq_resignline l"
				+ " WHERE m.resid=l.resid and m.res_type=" + res_type + " AND stat>1 AND stat<=9 AND m.er_id=" + er_id
				+ "   AND m.resdate>='" + sdb + "' AND m.resdate<'" + sde + "' AND l.isreg=1 and l.ri_times=1";
		if ((resid != null) && (!resid.isEmpty()))
			sqlstr = sqlstr + " and m.resid<>" + resid;
		return Integer.valueOf((new Hrkq_resign()).pool.openSql2List(sqlstr).get(0).get("ct"));
	}

	private void doinsertSWCardInfo(CJPA jpa, CDBConnection con) throws Exception {
		Hrkq_resign r = (Hrkq_resign) jpa;
		Hrkq_swcdlst sw = new Hrkq_swcdlst();
		String sqlstr = "SELECT * FROM hr_employee WHERE er_id=" + r.er_id.getValue();
		Hr_employee emp = new Hr_employee();
		emp.findBySQL(sqlstr, false);
		if (emp.isEmpty())
			throw new Exception("没有找到ID为【" + r.er_id.getValue() + "】的在职员工资料");
		String card_number = null;
		if (emp.card_number.isEmpty()) {
			sqlstr = "SELECT * FROM `hr_ykt_card` WHERE er_id=" + emp.er_id.getValue()
					+ " ORDER BY createtime DESC ";
			Hr_ykt_card yc = new Hr_ykt_card();
			yc.findBySQL(sqlstr);
			if (yc.isEmpty())
				throw new Exception("员工【" + r.employee_name.getValue() + "】无卡号");
			else
				card_number = yc.card_number.getValue();

		} else
			card_number = emp.card_number.getValue();

		for (CJPABase jpal : r.hrkq_resignlines) {
			Hrkq_resignline rl = (Hrkq_resignline) jpal;
			String daystr = Systemdate.getStrDateByFmt(rl.kqdate.getAsDatetime(), "yyyy-MM-dd");
			if (rl.isreg.getAsInt() == 1) {
				// String timestr = daystr + " " + rl.rgtime.getValue();
				Date skdate = gettimebybltime(daystr, rl.rgtime.getValue());
				sw.clear();
				sw.card_number.setValue(card_number); // 卡号
				sw.empno.setValue(emp.employee_code.getValue());
				sw.skdate.setAsDatetime(skdate); // yyyy-MM-dd hh:mm:ss
				sw.sktype.setAsInt(2); // 1 刷卡 2 签卡
				sw.synid.setAsInt(0);
				sw.readerno.setValue(rl.reslid.getValue());// 补签的时候 是补签明细表
				sw.save(con);// 貌似不支持事务处理
			}
		}
		// 自动重新计算 当月考勤
	}

	/**
	 * 根据班次时间 返回日期，考虑跨天解析
	 * 
	 * @param daystr
	 * @param timestr
	 * @return
	 */
	private Date gettimebybltime(String daystr, String timestr) {
		int tp = 0;
		String tstr = timestr;
		if (timestr.substring(0, 1).equalsIgnoreCase("Y")) {// 前一天
			tp = -1;
			tstr = timestr.substring(1, timestr.length());
		}
		if (timestr.substring(0, 1).equalsIgnoreCase("T")) {// 后一天
			tp = 1;
			tstr = timestr.substring(1, timestr.length());
		}
		String dtstr = daystr + " " + tstr;
		// System.out.println("dtstr:" + dtstr);
		Date rst = Systemdate.getDateByStr(daystr + " " + tstr);
		rst = Systemdate.dateDayAdd(rst, tp);
		return rst;
	}

	/**
	 * 删除补签信息
	 * 
	 * @param jpa
	 * @param con
	 * @throws Exception
	 */
	private void dodelSWCardInfo(CJPA jpa, CDBConnection con) throws Exception {
		Hrkq_resign r = (Hrkq_resign) jpa;
		for (CJPABase jpal : r.hrkq_resignlines) {
			Hrkq_resignline rl = (Hrkq_resignline) jpal;
			if (rl.isreg.getAsInt() == 1) {
				String sqlstr = "DELETE FROM hrkq_swcdlst WHERE sktype=2 AND readerno=" + rl.reslid.getValue();
				// System.out.println(sqlstr);
				con.execsql(sqlstr, true);
			}
		}
	}
	
	private void checkValidDate(CJPA jpa) throws Exception{
		if (HRUtil.hasRoles("58"))// 单据维护员 和 管理员
			return;
		Hrkq_resign r = (Hrkq_resign) jpa;
		CJPALineData<Hrkq_resignline> ls = r.hrkq_resignlines;
		for (CJPABase j : ls) {
			Hrkq_resignline l = (Hrkq_resignline) j;
		
			if (l.isreg.getAsIntDefault(0) == 1) {
				Date kqdate = l.kqdate.getAsDatetime();
				//String sdt = Systemdate.getStrDateyyyy_mm_dd(kqdate);
				long advanceDays = Long.valueOf(HrkqUtil.getParmValue("KQ_RESIGN_BSDAY"));
			
				//long advanceDays=2;
				if(advanceDays>0){	
					long betweenDays= DateUtil.getBetweenDays(kqdate, new Date());
					System.out.print("advanceDays="+advanceDays+"betweenDays="+betweenDays);
					Logsw.dblog("advanceDays="+advanceDays+"betweenDays="+betweenDays);
					if(betweenDays>advanceDays){
						throw new Exception("不能提交【"+advanceDays+"】天前的表单");
					}
				}
			}
		}
	}
}
