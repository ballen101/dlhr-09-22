package com.hr.attd.ctr;

import java.util.HashMap;
import java.util.List;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPABase;
import com.corsair.cjpa.CJPALineData;
import com.corsair.dbpool.CDBConnection;
import com.corsair.server.cjpa.CJPA;
import com.corsair.server.cjpa.JPAController;
import com.corsair.server.wordflow.Shwwf;
import com.hr.attd.entity.Hr_kq_monthlyattendance;
import com.hr.attd.entity.Hr_kq_monthlyattendance_line;
import com.hr.attd.entity.Hr_kq_monthsubmit;
import com.hr.attd.entity.Hr_kq_monthsubmit_line;

public class CtrHr_kq_monthlyattendance extends JPAController {
	@Override
	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf, boolean isFilished) throws Exception {
		//检查当月是否有提报
		Hr_kq_monthlyattendance h = (Hr_kq_monthlyattendance) jpa;
		CJPALineData<Hr_kq_monthlyattendance_line> ls = h.Hr_kq_monthlyattendance_lines;		
//		String name = ls.get(0).cfield("employee_name").getValue();
		String sql="SELECT * FROM Hr_kq_monthsubmit h,  Hr_kq_monthsubmit_line  l WHERE h.mkq_id =l.mkq_id  AND h.submitdate='" + h.submitdate.getValue() + "'  AND h.stat>1 AND h.stat<10";
		String sql1="SELECT * FROM Hr_kq_monthlyattendance h,  Hr_kq_monthlyattendance_line  l WHERE h.maid =l.maid AND h.submitdate='" + h.submitdate.getValue() + "'  AND h.stat>1 AND h.stat<10";
		List<HashMap<String, String>> sl=h.pool.openSql2List(sql);
		sl.addAll(h.pool.openSql2List(sql1));		
//		String lsnameString=sl.get(0).get("employee_name");
		for(int i=0;i<ls.size();i++){
			for(int j=0;j<sl.size();j++){
				if(sl.get(j).get("employee_name").equals(ls.get(i).cfield("employee_name").getValue())){
					throw new Exception(ls.get(i).cfield("employee_code").getValue()+ls.get(i).cfield("employee_name").getValue()+(h.submitdate.getAsDatetime().getMonth()+1)+"月已存在考勤提报数据，请勿重复提报");
				}
			}
		}
//		sl.get(0).get("employee_name");
		//		for (CJPABase j : ls) {
//			Hr_kq_monthlyattendance_line l = (Hr_kq_monthlyattendance_line) j;
//			String sqlstr = "SELECT COUNT(*) ct FROM `Hr_kq_monthsubmit` h,  `Hr_kq_monthsubmit_line`  l"
//					+ " WHERE h.mkq_id =l.mkq_id"
//					+ " AND h.`submitdate`='" + h.submitdate.getValue() + "' AND l.`er_id`=" + l.er_id.getValue()
//					+ " AND h.`stat`>1 AND h.`stat`<10";
//			if (!h.pool.openSql2List(sqlstr).get(0).get("ct").equals("0"))
//				//373919冼健发05月已存在考勤提报数据，请勿重复提报
//				
//				//提报日期【" + Systemdate.getStrDateyyyy_mm_dd(h.submitdate.getAsDatetime()) + "】【工号"+l.employee_code.getValue()+"】已经存在提保记录"
//				throw new Exception(l.employee_code.getValue()+l.employee_name.getValue()+(h.submitdate.getAsDatetime().getMonth()+1)+"月已存在考勤提报数据，请勿重复提报");
//		}
	}
//	public void AfterWFStart(CJPA jpa, CDBConnection con, Shwwf wf, boolean isFilished) throws Exception {
//		//检查当月是否有提报
//		Hr_kq_monthlyattendance h = (Hr_kq_monthlyattendance) jpa;
//		CJPALineData<Hr_kq_monthlyattendance_line> ls = h.Hr_kq_monthlyattendance_lines;
//		for (CJPABase j : ls) {
//			Hr_kq_monthlyattendance_line l = (Hr_kq_monthlyattendance_line) j;
//			String sqlstr = "SELECT IFNULL(COUNT(*),0) ct FROM `Hr_kq_monthlyattendance` h,  `Hr_kq_monthlyattendance_line`  l"
//					+ " WHERE h.maid =l.maid"
//					+ " AND h.`submitdate`='" + h.submitdate.getValue() + "' AND l.`er_id`=" + l.er_id.getValue()
//					+ " AND h.`stat`>1 AND h.`stat`<10";
//			if (Integer.valueOf(h.pool.openSql2List(sqlstr).get(0).get("ct")) != 0)
//				//373919冼健发05月已存在考勤提报数据，请勿重复提报
//				
//				//提报日期【" + Systemdate.getStrDateyyyy_mm_dd(h.submitdate.getAsDatetime()) + "】【工号"+l.employee_code.getValue()+"】已经存在提保记录"
//				throw new Exception(l.employee_code.getValue()+l.employee_name.getValue()+(h.submitdate.getAsDatetime().getMonth()+1)+"月已存在考勤提报数据，请勿重复提报");
//		}
//	}

}
