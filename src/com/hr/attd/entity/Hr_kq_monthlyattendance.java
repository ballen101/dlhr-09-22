package com.hr.attd.entity;

import java.sql.Types;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.cjpa.util.CLinkFieldInfo;
import com.corsair.cjpa.util.LinkFieldItem;
import com.corsair.server.cjpa.CJPA;
import com.hr.attd.ctr.CtrHr_kq_monthlyattendance;

@CEntity(controller =CtrHr_kq_monthlyattendance.class)
public class Hr_kq_monthlyattendance extends CJPA{
	
	@CFieldinfo(fieldname ="maid",iskey=true,notnull=true,precision=20,scale=0,caption="月考勤数主表id",datetype=Types.INTEGER)
	public CField maid;  //月考勤报批ID
	@CFieldinfo(fieldname ="macode",notnull=true,codeid=147,precision=16,scale=0,caption="v",datetype=Types.VARCHAR)
	public CField macode;  //月考勤数主表编码
	@CFieldinfo(fieldname ="orgid",notnull=true,precision=10,scale=0,caption="部门ID",datetype=Types.INTEGER)
	public CField orgid;  //部门ID
	@CFieldinfo(fieldname ="orgcode",notnull=true,precision=16,scale=0,caption="部门编码",datetype=Types.VARCHAR)
	public CField orgcode;  //部门编码
	@CFieldinfo(fieldname ="orgname",notnull=true,precision=128,scale=0,caption="部门名称",datetype=Types.VARCHAR)
	public CField orgname;  //部门名称
	@CFieldinfo(fieldname ="submitdate",precision=10,scale=0,caption="提报月份",datetype=Types.DATE)
	public CField submitdate;  //提报月份
	@CFieldinfo(fieldname ="remark",precision=512,scale=0,caption="备注",datetype=Types.VARCHAR)
	public CField remark;  //备注
	@CFieldinfo(fieldname ="wfid",precision=20,scale=0,caption="wfid",datetype=Types.INTEGER)
	public CField wfid;  //wfid
	@CFieldinfo(fieldname ="attid",precision=20,scale=0,caption="attid",datetype=Types.INTEGER)
	public CField attid;  //attid
	@CFieldinfo(fieldname ="stat",notnull=true,precision=2,scale=0,caption="流程状态",datetype=Types.INTEGER)
	public CField stat;  //流程状态
	@CFieldinfo(fieldname ="entid",notnull=true,precision=5,scale=0,caption="entid",datetype=Types.INTEGER)
	public CField entid;  //entid
	@CFieldinfo(fieldname ="idpath",notnull=true,precision=256,scale=0,caption="idpath",datetype=Types.VARCHAR)
	public CField idpath;  //idpath
	@CFieldinfo(fieldname ="creator",notnull=true,precision=32,scale=0,caption="创建人",datetype=Types.VARCHAR)
	public CField creator;  //创建人
	@CFieldinfo(fieldname ="createtime",notnull=true,precision=19,scale=0,caption="创建时间",datetype=Types.TIMESTAMP)
	public CField createtime;  //创建时间
	@CFieldinfo(fieldname ="updator",precision=32,scale=0,caption="更新人",datetype=Types.VARCHAR)
	public CField updator;  //更新人
	@CFieldinfo(fieldname ="updatetime",precision=19,scale=0,caption="更新时间",datetype=Types.TIMESTAMP)
	public CField updatetime;  //更新时间
	@CFieldinfo(fieldname ="usable",notnull=true,precision=1,scale=0,caption="有效",defvalue="1",datetype=Types.INTEGER)
	public CField usable;  //有效
	@CFieldinfo(fieldname ="orghrlev",precision=1,scale=0,caption="orghrlev",datetype=Types.INTEGER)
	public CField orghrlev;  //orghrlev
	public String SqlWhere; //查询附加条件
	public int MaxCount; //查询最大数量
	
	//自关联数据定义
	@CLinkFieldInfo(jpaclass = Hr_kq_monthlyattendance_line.class, linkFields = { @LinkFieldItem(lfield = "maid", mfield = "maid") })
	public CJPALineData<Hr_kq_monthlyattendance_line> Hr_kq_monthlyattendance_lines;
	
	public Hr_kq_monthlyattendance() throws Exception {
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
