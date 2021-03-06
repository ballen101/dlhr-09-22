package com.hr.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

import com.corsair.cjpa.CJPALineData;
import com.corsair.dbpool.CDBConnection;
import com.corsair.dbpool.CDBPool;
import com.corsair.dbpool.DBPoolParms;
import com.corsair.dbpool.util.Systemdate;
import com.corsair.server.base.CSContext;
import com.corsair.server.generic.Shw_attach;
import com.corsair.server.generic.Shw_attach_line;
import com.corsair.server.generic.Shw_physic_file;
import com.corsair.server.retention.ACO;
import com.corsair.server.retention.ACOAction;
import com.corsair.server.util.CorUtil;
import com.hr.attd.ctr.CtrHrkq_leave_blance;
import com.hr.attd.entity.Hrkq_swcdlst;
import com.hr.perm.entity.Hr_employee_cretl;
import com.hr.util.hrmail.DLHRMailCenterWS;
import com.hr.util.hrmail.EipMail;
import com.hr.util.hrmail.Hr_emailsend_log;
import com.hr.util.hrmail.SOAPUtil;

@ACO(coname = "web.hr.test")
public class Test {
	// need time:115

	public static void main(String[] args) throws Exception {
		
	String aa=	CtrHrkq_leave_blance.getworkymString(Systemdate.getDateByStr("2020-06-02"), new Date());
	     
	System.out.print(aa);
	}

	private static CDBPool getpool() {
		DBPoolParms pm = new DBPoolParms();
		pm.dirver = "com.mysql.jdbc.Driver";
		pm.url = "jdbc:mysql://127.0.0.1:13306/dlhr?characterEncoding=utf-8&amp;autoReconnect=true&amp;useSSL=true";
		pm.schema = "dlhr";
		pm.user = "root";
		pm.password = "q1w2e3";
		pm.minsession = 5;
		pm.maxsession = 10;
		pm.isdefault = true;
		CDBPool pool = new CDBPool(pm);
		return pool;
	}

	public static void test() {
		try {
			Date bgdt = new Date();
			for (int i = 0; i < 1000; i++) {
				Hrkq_swcdlst sw = new Hrkq_swcdlst();
				sw.empno.setValue("00000");// 工号
				sw.card_number.setValue("11111"); // 卡号
				sw.skdate.setAsDatetime(new Date()); // yyyy-MM-dd hh:mm:ss
				sw.sktype.setAsInt(1);// 1 刷卡 2 签卡
				sw.synid.setValue("1"); // 旧ID 同步条件
				sw.save();
			}
			Date eddt = new Date();
			System.out.println("need time:" + ((eddt.getTime() - bgdt.getTime()) / 1000));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 单个获取ID need time:50
	// 批量获取ID 15 5000 38

	public static void test2() {
		try {
			Date bgdt = new Date();
			CJPALineData<Hrkq_swcdlst> sws = new CJPALineData<Hrkq_swcdlst>(Hrkq_swcdlst.class);
			for (int i = 0; i < 5000; i++) {
				Hrkq_swcdlst sw = new Hrkq_swcdlst();
				sw.empno.setValue("00000");// 工号
				sw.card_number.setValue("11111"); // 卡号
				sw.skdate.setAsDatetime(new Date()); // yyyy-MM-dd hh:mm:ss
				sw.sktype.setAsInt(1);// 1 刷卡 2 签卡
				sw.synid.setValue("1"); // 旧ID 同步条件
				sws.add(sw);
			}
			sws.saveBatchSimple();
			Date eddt = new Date();
			System.out.println("need time:" + ((eddt.getTime() - bgdt.getTime()) / 1000));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * tp 1 发送 2 更新审批人 3 删除
	 * 
	 * @return
	 * @throws Exception
	 */
	@ACOAction(eventname = "testmail", Authentication = false, notes = "test")
	public String testmail() throws Exception {
		HashMap<String, String> parms = CSContext.get_pjdataparms();
		String aynemid = CorUtil.hashMap2Str(parms, "aynemid", "需要参数【aynemid】");
		int tp = Integer.valueOf(CorUtil.hashMap2Str(parms, "tp", "需要参数【tp】"));
		Hr_emailsend_log ml = new Hr_emailsend_log();
		String sqlstr = "SELECT * FROM hr_emailsend_log WHERE aynemid='" + aynemid + "'";
		System.out.println(sqlstr);
		ml.findBySQL(sqlstr);
		if (ml.isEmpty())
			throw new Exception("消息不存在");
		if (tp == 1) {
			DLHRMailCenterWS.sendmailex(ml);
		} else if (tp == 2) {
			String approvalman = CorUtil.hashMap2Str(parms, "approvalman");
			String approvaldate = CorUtil.hashMap2Str(parms, "approvaldate");
			DLHRMailCenterWS.updatemail(aynemid, approvalman, approvaldate);
		} else if (tp == 3) {
			// DLHRMailCenterWS.delMail(aynemid);
		} else
			throw new Exception("tp错误");
		ml.findBySQL(sqlstr);
		return ml.tojson();
	}
}
