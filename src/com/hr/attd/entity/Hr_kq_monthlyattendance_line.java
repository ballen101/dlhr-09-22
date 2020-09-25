package com.hr.attd.entity;

import java.sql.Types;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.server.cjpa.CJPA;

@CEntity(caption="特殊月考勤提报明细",tablename="Hr_kq_monthlyattendance_line")
public class Hr_kq_monthlyattendance_line extends CJPA {

//	月考勤id
	@CFieldinfo(fieldname ="malid",iskey=true,notnull=true,precision=11,scale=0,caption="malid",datetype=Types.INTEGER)
	public CField malid;
	@CFieldinfo(fieldname ="maid",notnull=true,precision=11,scale=0,caption="maid",datetype=Types.INTEGER)
	public CField maid;
	@CFieldinfo(fieldname ="er_id",precision=10,scale=0,caption="员工档案ID",datetype=Types.INTEGER)
	public CField er_id;  //员工档案ID
	@CFieldinfo(fieldname ="employee_code",precision=16,scale=0,caption="工号",datetype=Types.VARCHAR)
	public CField employee_code;  //工号
	@CFieldinfo(fieldname ="employee_name",precision=64,scale=0,caption="姓名",datetype=Types.VARCHAR)
	public CField employee_name;  //姓名
	@CFieldinfo(fieldname ="orgid",precision=10,scale=0,caption="部门ID",datetype=Types.INTEGER)
	public CField orgid;  //部门ID
	@CFieldinfo(fieldname ="orgcode",precision=16,scale=0,caption="部门编码",datetype=Types.VARCHAR)
	public CField orgcode;  //部门编码
	@CFieldinfo(fieldname ="orgname",precision=128,scale=0,caption="部门名称",datetype=Types.VARCHAR)
	public CField orgname;  //部门名称
	@CFieldinfo(fieldname ="idpath",precision=256,scale=0,caption="idpath",datetype=Types.VARCHAR)
	public CField idpath;  //idpath
	@CFieldinfo(fieldname ="ospid",precision=20,scale=0,caption="职位ID",datetype=Types.INTEGER)
	public CField ospid;  //职位ID
	@CFieldinfo(fieldname ="ospcode",precision=16,scale=0,caption="职位编码",datetype=Types.VARCHAR)
	public CField ospcode;  //职位编码
	@CFieldinfo(fieldname ="sp_name",precision=128,scale=0,caption="标准职位名称",datetype=Types.VARCHAR)
	public CField sp_name;  //标准职位名称
	@CFieldinfo(fieldname ="lv_id",precision=10,scale=0,caption="职级ID",datetype=Types.INTEGER)
	public CField lv_id;  //职级ID
	@CFieldinfo(fieldname ="lv_num",precision=4,scale=1,caption="职级",datetype=Types.DECIMAL)
	public CField lv_num;  //职级
	@CFieldinfo(fieldname ="hiredday",precision=19,scale=0,caption="入职日期",datetype=Types.TIMESTAMP)
	public CField hiredday;  //入职日期
	@CFieldinfo(fieldname ="ljdate",precision=19,scale=0,caption="ljdate" ,datetype=Types.TIMESTAMP)
	public CField ljdate;  //ljdate
	@CFieldinfo(fieldname ="ljtype",precision=255,scale=0,caption="ljtype",datetype=Types.VARCHAR)
	public CField ljtype;  //ljtype
	@CFieldinfo(fieldname ="ycmq",notnull=true,precision=11,scale=1,caption="应出满勤",defvalue="0",datetype=Types.DECIMAL)
	public CField ycmq;  //应出满勤
	@CFieldinfo(fieldname ="sjcq",notnull=true,precision=11,scale=1,caption="实际出勤",defvalue="0",datetype=Types.DECIMAL)
	public CField sjcq;  //实际出勤
	@CFieldinfo(fieldname ="psjb",notnull=true,precision=11,scale=1,caption="平时加班",defvalue="0",datetype=Types.DECIMAL)
	public CField psjb;  //平时加班
	@CFieldinfo(fieldname ="xxrjb",notnull=true,precision=11,scale=1,caption="休息日加班",defvalue="0",datetype=Types.DECIMAL)
	public CField xxrjb;  //休息日加班
	@CFieldinfo(fieldname ="fdjrjb",notnull=true,precision=11,scale=1,caption="法定假日加班",defvalue="0",datetype=Types.DECIMAL)
	public CField fdjrjb;  //法定假日加班
	@CFieldinfo(fieldname ="kgts",notnull=true,precision=11,scale=1,caption="旷工天数",defvalue="0",datetype=Types.DECIMAL)
	public CField kgts;  //旷工天数
	@CFieldinfo(fieldname ="cdztcs",notnull=true,precision=11,scale=1,defvalue="0",caption="迟到早退次数",datetype=Types.INTEGER)
	public CField cdztcs;  //迟到早退次数
	@CFieldinfo(fieldname ="cqcs",notnull=true,precision=11,scale=0,caption="超签次数",defvalue="0",datetype=Types.INTEGER)
	public CField cqcs;  //超签次数
	@CFieldinfo(fieldname ="bjts",notnull=true,precision=11,scale=1,caption="病假天数",defvalue="0",datetype=Types.DECIMAL)
	public CField bjts;  //病假天数
	@CFieldinfo(fieldname ="qjts",notnull=true,precision=11,scale=1,caption="请假天数",defvalue="0",datetype=Types.DECIMAL)
	public CField qjts;  //请假天数
	@CFieldinfo(fieldname ="yxts",notnull=true,precision=11,scale=1,caption="有薪天数",defvalue="0",datetype=Types.DECIMAL)
	public CField yxts;  //有薪天数
	@CFieldinfo(fieldname ="ybts",notnull=true,precision=11,scale=1,caption="夜班天数",defvalue="0",datetype=Types.DECIMAL)
	public CField ybts;  //夜班天数
	@CFieldinfo(fieldname ="gzxsz",precision=11,scale=0,caption="工作小时制",defvalue="0",datetype=Types.INTEGER)
	public CField gzxsz;  //工作小时制
	@CFieldinfo(fieldname ="jxfs",notnull=true,precision=255,scale=0,caption="计薪方式",defvalue="0",datetype=Types.VARCHAR)
	public CField jxfs;  //计薪方式
	@CFieldinfo(fieldname ="tsts1",precision=11,scale=1,caption="特殊天数1",defvalue="0",datetype=Types.DECIMAL)
	public CField tsts1;  //特殊天数1
	@CFieldinfo(fieldname ="tsts2",precision=11,scale=1,caption="特殊天数2",defvalue="0",datetype=Types.DECIMAL)
	public CField tsts2;  //特殊天数2
	@CFieldinfo(fieldname ="remark",precision=255,scale=0,caption="备注",datetype=Types.VARCHAR)
	public CField remark;  //备注
	public String SqlWhere; //查询附加条件
	public int MaxCount; //查询最大数量
	
	 //自关联数据定义
	public Hr_kq_monthlyattendance_line() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean InitObject() {//类初始化调用的方法
		super.InitObject();
		return true;
	} 

	@Override
	public boolean FinalObject() { //类释放前调用的方法
		super.FinalObject(); 
		return true; 
	} 

}
