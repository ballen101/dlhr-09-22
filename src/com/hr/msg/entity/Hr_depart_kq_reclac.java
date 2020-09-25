package com.hr.msg.entity;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.server.cjpa.CJPA;
import java.sql.Types;
@CEntity()
public class Hr_depart_kq_reclac extends CJPA{
//  id
	@CFieldinfo(fieldname = "id", iskey = true, notnull = true, caption = "ID", datetype = Types.INTEGER)
	public CField id;
//	考勤变更code
	@CFieldinfo(fieldname = "dcode", codeid = 200, notnull = true, caption = "考勤变更编码", datetype = Types.VARCHAR)
	public CField dcode;
//	部门名称
	@CFieldinfo(fieldname = "orgname", caption = "部门名称", datetype = Types.VARCHAR)
	public CField orgname;
//	部门编号
	@CFieldinfo(fieldname="orgcode",caption="部门编号",datetype=Types.VARCHAR)
	public CField orgcode;
//	ipath
	@CFieldinfo(fieldname="idpath",caption="idpath",notnull = true,datetype = Types.VARCHAR)
	public CField idpath;
//	状态
	@CFieldinfo(fieldname="status",caption="状态",datetype=Types.INTEGER)
	public CField status;
//	创建人
	@CFieldinfo(fieldname="creator",caption="创建人",datetype=Types.VARCHAR)
	public CField creator;
// 创建时间
	@CFieldinfo(fieldname="createtime",caption="创建时间",datetype=Types.TIMESTAMP)
	public CField createtime;
//	更改人
	@CFieldinfo(fieldname="updator",caption="更改人",datetype=Types.VARCHAR)
	public CField updator;
//	更改时间
	@CFieldinfo(fieldname="updatetime",caption="更改时间",datetype=Types.TIMESTAMP)
	public CField updatetime;
	
	public Hr_depart_kq_reclac() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean InitObject() {// 类初始化调用的方法
		super.InitObject();
		return true;
	}

	@Override
	public boolean FinalObject() { // 类释放前调用的方法
		super.FinalObject();
		return true;
	}
	
}
