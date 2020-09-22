package com.hr.perm.entity;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.server.cjpa.CJPA;
import com.hr.perm.ctr.CtrHr_black_add;

import java.sql.Types;

@CEntity(controller = CtrHr_black_add.class)
public class Hr_black_add extends CJPA {
	@CFieldinfo(fieldname = "baid", iskey = true, notnull = true, caption = "加封ID", datetype = Types.INTEGER)
	public CField baid; // 解封ID
	@CFieldinfo(fieldname = "bacode", notnull = true, codeid = 66, caption = "加封编码", datetype = Types.VARCHAR)
	public CField bacode; // 解封编码
	@CFieldinfo(fieldname = "addappdate", notnull = true, caption = "加封申请日期", datetype = Types.TIMESTAMP)
	public CField addappdate; // 加封申请日期
	@CFieldinfo(fieldname = "adddate", notnull = true, caption = "加封生效日期", datetype = Types.TIMESTAMP)
	public CField adddate; // 加封生效日期
	@CFieldinfo(fieldname="addtype",caption="加封类型",datetype=Types.INTEGER)
	public CField addtype;  //加封类型
	@CFieldinfo(fieldname="addtype1",caption="加封类别",datetype=Types.INTEGER)
	public CField addtype1;  //加封类别
	@CFieldinfo(fieldname="addnum",caption="加封次数",datetype=Types.INTEGER)
	public CField addnum;  //加封次数
	@CFieldinfo(fieldname = "blackreason", caption = "加入黑名单原因", datetype = Types.VARCHAR)
	public CField blackreason; // 加入黑名单原因
	@CFieldinfo(fieldname = "allwf", notnull = true, caption = "是否启动流程", datetype = Types.INTEGER)
	public CField allwf; // 是否启动流程，用来控制需要加入黑名单离职表单审批通过后自动加黑
	@CFieldinfo(fieldname="billtype",caption="表单方式1 单个 2 批量",defvalue="1",datetype=Types.INTEGER)
	public CField billtype;  //表单方式1 单个 2 批量
	@CFieldinfo(fieldname = "ljid", notnull = true, caption = "离职ID", datetype = Types.INTEGER)
	public CField ljid; // 离职ID
	@CFieldinfo(fieldname = "ljcode", notnull = true, caption = "离职编码", datetype = Types.VARCHAR)
	public CField ljcode; // 离职编码
	@CFieldinfo(fieldname = "ljreason", caption = "离职原因", datetype = Types.VARCHAR)
	public CField ljreason; // 离职原因
	@CFieldinfo(fieldname = "orgid", notnull = true, caption = "部门ID", datetype = Types.INTEGER)
	public CField orgid; // 部门ID
	@CFieldinfo(fieldname = "orgcode", notnull = true, caption = "部门编码", datetype = Types.VARCHAR)
	public CField orgcode; // 部门编码
	@CFieldinfo(fieldname = "orgname", notnull = true, caption = "部门名称", datetype = Types.VARCHAR)
	public CField orgname; // 部门名称
	@CFieldinfo(fieldname = "er_id", notnull = true, caption = "档案ID", datetype = Types.INTEGER)
	public CField er_id; // 档案ID
	@CFieldinfo(fieldname = "er_code", caption = "档案编码", datetype = Types.VARCHAR)
	public CField er_code; // 档案编码
	@CFieldinfo(fieldname = "employee_code", notnull = true, caption = "工号", datetype = Types.VARCHAR)
	public CField employee_code; // 工号
	@CFieldinfo(fieldname = "sex", notnull = true, caption = "性别", datetype = Types.INTEGER)
	public CField sex; // 性别
	@CFieldinfo(fieldname = "id_number", notnull = true, caption = "身份证号", datetype = Types.VARCHAR)
	public CField id_number; // 身份证号
	@CFieldinfo(fieldname = "employee_name", notnull = true, caption = "姓名", datetype = Types.VARCHAR)
	public CField employee_name; // 姓名
	@CFieldinfo(fieldname = "degree", caption = "学历", datetype = Types.INTEGER)
	public CField degree; // 学历
	@CFieldinfo(fieldname = "hiredday", notnull = true, caption = "入职日期", datetype = Types.TIMESTAMP)
	public CField hiredday; // 入职日期
	@CFieldinfo(fieldname = "ljdate", notnull = true, caption = "离职日期", datetype = Types.TIMESTAMP)
	public CField ljdate; // 离职日期
	@CFieldinfo(fieldname = "ljtype1", notnull = true, caption = "离职方式 自离 辞职 等", datetype = Types.INTEGER)
	public CField ljtype1; // 离职方式 自离 辞职 等
	@CFieldinfo(fieldname = "lv_id", notnull = true, caption = "职级ID", datetype = Types.INTEGER)
	public CField lv_id; // 职级ID
	@CFieldinfo(fieldname = "lv_num", notnull = true, caption = "职级", datetype = Types.DECIMAL)
	public CField lv_num; // 职级
	@CFieldinfo(fieldname = "hg_id", notnull = true, caption = "职等ID", datetype = Types.INTEGER)
	public CField hg_id; // 职等ID
	@CFieldinfo(fieldname = "hg_code", notnull = true, caption = "职等编码", datetype = Types.VARCHAR)
	public CField hg_code; // 职等编码
	@CFieldinfo(fieldname = "hg_name", notnull = true, caption = "职等名称", datetype = Types.VARCHAR)
	public CField hg_name; // 职等名称
	@CFieldinfo(fieldname = "ospid", notnull = true, caption = "职位ID", datetype = Types.INTEGER)
	public CField ospid; // 职位ID
	@CFieldinfo(fieldname = "ospcode", notnull = true, caption = "职位编码", datetype = Types.VARCHAR)
	public CField ospcode; // 职位编码
	@CFieldinfo(fieldname = "sp_name", notnull = true, caption = "职位名称", datetype = Types.VARCHAR)
	public CField sp_name; // 职位名称
	@CFieldinfo(fieldname="lvlev",caption="离职层级",datetype=Types.INTEGER)
	public CField lvlev;  //离职层级
	@CFieldinfo(fieldname="orghrlev",caption="人事机构层级",datetype=Types.INTEGER)
	public CField orghrlev;  //人事机构层级
	@CFieldinfo(fieldname = "remark", caption = "备注", datetype = Types.VARCHAR)
	public CField remark; // 备注
	@CFieldinfo(fieldname = "wfid", caption = "wfid", datetype = Types.INTEGER)
	public CField wfid; // wfid
	@CFieldinfo(fieldname = "attid", caption = "attid", datetype = Types.INTEGER)
	public CField attid; // attid
	@CFieldinfo(fieldname = "stat", notnull = true, caption = "表单状态", datetype = Types.INTEGER)
	public CField stat; // 表单状态
	@CFieldinfo(fieldname = "idpath", notnull = true, caption = "idpath", datetype = Types.VARCHAR)
	public CField idpath; // idpath
	@CFieldinfo(fieldname = "entid", notnull = true, caption = "entid", datetype = Types.INTEGER)
	public CField entid; // entid
	@CFieldinfo(fieldname = "creator", notnull = true, caption = "制单人", datetype = Types.VARCHAR)
	public CField creator; // 制单人
	@CFieldinfo(fieldname = "createtime", notnull = true, caption = "制单时间", datetype = Types.TIMESTAMP)
	public CField createtime; // 制单时间
	@CFieldinfo(fieldname = "updator", caption = "更新人", datetype = Types.VARCHAR)
	public CField updator; // 更新人
	@CFieldinfo(fieldname = "updatetime", caption = "更新时间", datetype = Types.TIMESTAMP)
	public CField updatetime; // 更新时间
	@CFieldinfo(fieldname = "attribute1", caption = "备用字段1", datetype = Types.VARCHAR)
	public CField attribute1; // 备用字段1
	@CFieldinfo(fieldname = "attribute2", caption = "备用字段2", datetype = Types.VARCHAR)
	public CField attribute2; // 备用字段2
	@CFieldinfo(fieldname = "attribute3", caption = "备用字段3", datetype = Types.VARCHAR)
	public CField attribute3; // 备用字段3
	@CFieldinfo(fieldname = "attribute4", caption = "备用字段4", datetype = Types.VARCHAR)
	public CField attribute4; // 备用字段4
	@CFieldinfo(fieldname = "attribute5", caption = "备用字段5", datetype = Types.VARCHAR)
	public CField attribute5; // 备用字段5
	public String SqlWhere; // 查询附加条件
	public int MaxCount; // 查询最大数量

	// 自关联数据定义

	public Hr_black_add() throws Exception {
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
