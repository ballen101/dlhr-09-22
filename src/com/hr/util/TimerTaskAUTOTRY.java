package com.hr.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import com.corsair.dbpool.DBPools;
import com.corsair.dbpool.util.Systemdate;
import com.hr.attd.ctr.HrkqUtil;
import com.hr.perm.ctr.CtrHrPerm;
import com.hr.perm.ctr.CtrHr_empptjob_app;

public class TimerTaskAUTOTRY extends TimerTask {
	@Override
	public void run() {
		try {
			DBPools.defaultPool().execsql("UPDATE hr_entry_try SET trystat=2 WHERE trystat =1 AND  promotionday<CURDATE()");// 试用自动过期
			DBPools.defaultPool().execsql("UPDATE hr_transfer_try SET trystat=2 WHERE trystat =1 AND  probationdate<CURDATE()");// 考察自动过期
			DBPools.defaultPool().execsql("UPDATE hr_recruit_transport SET `recruit_transport_stat` =2 WHERE cooperate_end_date <CURDATE()");// 输送机构自动失效
			DBPools.defaultPool()
			.execsql("DELETE FROM hr_interfaceempaddr  WHERE  employee_code IN (SELECT e.employee_code FROM hr_employee e WHERE e.empstatid>10 )");// 自动干掉联系人

			try {
				CtrHr_empptjob_app.scanOutTimeApp();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		

			HrkqUtil.backAndMoveKQSWCT();// 将3个月前前的打卡记录同步到历史表
		} catch (Exception e) {
			System.out.println("自动处理试用过期考察过期等错误");
			e.printStackTrace();
		}
	}

}
