package com.hr.attd.ctr;

import java.util.Date;

import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.CJPABase.CJPAStat;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.wordflow.Shwwf;
import com.corsair.server.wordflow.Shwwfproc;
import com.hr.attd.entity.Hrkq_holidayapp;
import com.hr.attd.entity.Hrkq_holidayapp_month;
import com.hr.attd.entity.Hrkq_holidaytype;
import com.hr.perm.entity.Hr_employee;
import com.hr.util.HRUtil;

public class CtrHrkq_holidayapp extends JPAController {
	@Override
	public void BeforeWFStartSave(CJPA jpa, Shwwf wf, CDBConnection con, boolean isFilished) throws Exception {
		// TODO Auto-generated method stub
		Hrkq_holidayapp hp = (Hrkq_holidayapp) jpa;
		Date bgtime = hp.timebg.getAsDatetime();
		Date edtime = hp.timeedtrue.getAsDatetime();
		HrkqUtil.checkAllOverlapDatetime(5, hp.haid.getValue(), hp.er_id.getValue(), hp.employee_name.getValue(), bgtime, edtime);

		String sbjet = hp.hacode.getValue();
		// String sbjet = + "(" + hp.employee_name.getValue()
		// + "请假时间:" + Systemdate.getStrDateByFmt(hp.timebg.getAsDatetime(), "yyyy-MM-dd HH:mm") + "到"
		// + Systemdate.getStrDateByFmt(hp.timeed.getAsDatetime(), "yyyy-MM-dd HH:mm") + ")";
		wf.subject.setValue(sbjet);
	}

	@Override
	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf, boolean isFilished) throws Exception {
		Hrkq_holidayapp hp = (Hrkq_holidayapp) jpa;
		Date bgtime = hp.timebg.getAsDatetime();
		Date edtime = hp.timeedtrue.getAsDatetime();
		if(!hp.creator.getValue().equals("inteface")){
			HrkqUtil.checkValidDate(bgtime);
		}
		HrkqUtil.checkAllOverlapDatetime(5, hp.haid.getValue(), hp.er_id.getValue(), hp.employee_name.getValue(), bgtime, edtime);
		checkMonth(jpa);
		if (isFilished) {
			doWFAgent(jpa, con);
			setholidaytype(jpa, con);
		}
	}

	@Override
	public void OnWfSubmit(CJPA jpa, CDBConnection con, Shwwf wf, Shwwfproc proc,
			Shwwfproc nxtproc, boolean isFilished) throws Exception {
		Hrkq_holidayapp hp = (Hrkq_holidayapp) jpa;
		Date bgtime = hp.timebg.getAsDatetime();
		Date edtime = hp.timeedtrue.getAsDatetime();
		if (isFilished) {
			HrkqUtil.checkAllOverlapDatetime(5, hp.haid.getValue(), hp.er_id.getValue(), hp.employee_name.getValue(), bgtime, edtime);
			doWFAgent(jpa, con);
			setholidaytype(jpa, con);
		}
	}

	private void checkMonth(CJPA jpa) throws Exception {
		Hrkq_holidayapp app = (Hrkq_holidayapp) jpa;
		Hrkq_holidaytype ht = new Hrkq_holidaytype();
		ht.findByID(app.htid.getValue());
		if (ht.isEmpty()) {
			return;
		}
		float daymmax = ht.daymmax.getAsFloatDefault(0);
		if (daymmax == 0)
			return;

		CJPALineData<Hrkq_holidayapp_month> ms = app.hrkq_holidayapp_months;
		for (CJPABase j : ms) {
			Hrkq_holidayapp_month m = (Hrkq_holidayapp_month) j;
			String ym = m.yearmonth.getValue();
			float lhdays = m.lhdays.getAsFloatDefault(0);
			String sqlstr = "SELECT IFNULL(SUM(m.lhdaystrue),0) dt FROM hrkq_holidayapp a, hrkq_holidayapp_month m "
					+ "WHERE a.stat>1 AND a.stat<=9 AND a.haid=m.haid AND er_id=" + app.er_id.getValue() + " AND m.yearmonth='" + ym + "' AND a.haid<>" + app.haid.getValue();
			float dt = Float.valueOf(app.pool.openSql2List(sqlstr).get(0).get("dt"));
			if ((lhdays + dt) > daymmax) {
				throw new Exception("【" + ht.htname.getValue() + "】天数【" + lhdays + "+" + dt + "】大于月度【" + ym + "】允许天数【" + daymmax + "】");
			}
		}

	}

	/**
	 * 如果违反规则，确认为事假，修改假期类型ID；
	 * 如果没违反规则，处理类型 设置为null；
	 * 
	 * @param jpa
	 * @param con
	 * @throws Exception
	 */
	private void setholidaytype(CJPA jpa, CDBConnection con) throws Exception {
		Hrkq_holidayapp app = (Hrkq_holidayapp) jpa;
		if (app.bhtype.getAsIntDefault(0) == 4) {
			Hr_employee e = new Hr_employee();
			e.findByID(app.er_id.getValue(), false);
			if (e.isEmpty())
				throw new Exception("人事ID【" + app.er_id.getValue() + "】不存在");
			e.married.setValue(2);
			e.save(con);
		}

		if (app.htconfirm.getAsIntDefault(0) == 2) {// 违反规则
			int viodeal = app.viodeal.getAsIntDefault(0);
			if (viodeal == 1) {// 事假
				app.attribute1.setValue("假期确认事假，原假期类型ID为" + app.htid.getValue());
				app.htid.setAsInt(1);// 设置为事假
				app.save(con);
			}
		} else
			app.viodeal.setValue(null);
	}

	private void doWFAgent(CJPA jpa, CDBConnection con) {
		Hrkq_holidayapp app = (Hrkq_holidayapp) jpa;
		if (app.iswfagent.getAsIntDefault(0) == 1) {
			HRUtil.setWFAgend(con, app.employee_code.getValue(), app.timebg.getAsDatetime(), app.timeed.getAsDatetime());
		}
	}

	@Override
	public void BeforeSave(CJPABase jpa, CDBConnection con, boolean selfLink) throws Exception {
		Hrkq_holidayapp hp = (Hrkq_holidayapp) jpa;
		if (hp.getJpaStat() == CJPAStat.RSDEL)// 标记为删除的不检查
			return;
		if (hp.timeedtrue.isEmpty())
			hp.timeedtrue.setValue(hp.timeed.getValue());
		if (hp.hdaystrue.isEmpty())
			hp.hdaystrue.setValue(hp.hdays.getValue());
		if (selfLink) {
			CJPALineData<Hrkq_holidayapp_month> ls = hp.hrkq_holidayapp_months;
			float sd = 0;
			for (CJPABase j : ls) {
				Hrkq_holidayapp_month l = (Hrkq_holidayapp_month) j;
				if (l.getJpaStat() != CJPAStat.RSDEL) {// 标记为删除的不处理
					float d = l.lhdays.getAsFloatDefault(0);
					if ((d * 10 % 5) != 0)
						throw new Exception("分解到月度错误，【" + l.yearmonth.getValue() + "】最小单位为0.5天");
					l.lhdays.setAsFloat(d);
					sd += d;
					if (l.lhdaystrue.isEmpty())
						l.lhdaystrue.setValue(l.lhdays.getValue());
				}
			}
			if (hp.hdays.getAsFloat() != sd) {
				throw new Exception("请假时间为【" + hp.hdays.getAsFloat() + "】天，分解到月度时间合计为【" + sd + "】，分解错误");
			}
		}
	}
}
