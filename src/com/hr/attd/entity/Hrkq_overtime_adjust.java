package com.hr.attd.entity;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.cjpa.util.CLinkFieldInfo;
import com.corsair.cjpa.util.LinkFieldItem;
import com.corsair.server.cjpa.CJPA;
import com.hr.attd.ctr.CtrHrkq_overtime_adjust;

import java.sql.Types;

@CEntity(controller = CtrHrkq_overtime_adjust.class)
public class Hrkq_overtime_adjust extends CJPA {
	@CFieldinfo(fieldname = "otad_id", iskey = true, notnull = true, caption = "加班调整ID", datetype = Types.INTEGER)
	public CField otad_id; // 加班调整ID
	@CFieldinfo(fieldname = "otad_code", notnull = true, codeid = 84, caption = "加班调整编码", datetype = Types.VARCHAR)
	public CField otad_code; // 加班调整编码
	@CFieldinfo(fieldname = "ot_id", notnull = true, caption = "加班申请ID", datetype = Types.INTEGER)
	public CField ot_id; // 加班申请ID
	@CFieldinfo(fieldname = "ot_code", notnull = true, caption = "加班申请编码", datetype = Types.VARCHAR)
	public CField ot_code; // 加班申请编码
	@CFieldinfo(fieldname = "er_id", caption = "申请人档案ID", datetype = Types.INTEGER)
	public CField er_id; // 申请人档案ID
	@CFieldinfo(fieldname = "employee_code", caption = "申请人工号", datetype = Types.VARCHAR)
	public CField employee_code; // 申请人工号
	@CFieldinfo(fieldname = "employee_name", caption = "申请人姓名", datetype = Types.VARCHAR)
	public CField employee_name; // 申请人姓名
	@CFieldinfo(fieldname = "orgid", notnull = true, caption = "部门ID", datetype = Types.INTEGER)
	public CField orgid; // 部门ID
	@CFieldinfo(fieldname = "orgcode", notnull = true, caption = "部门编码", datetype = Types.VARCHAR)
	public CField orgcode; // 部门编码
	@CFieldinfo(fieldname = "orgname", notnull = true, caption = "部门名称", datetype = Types.VARCHAR)
	public CField orgname; // 部门名称
	@CFieldinfo(fieldname = "deductoff", caption = "扣休息时数", datetype = Types.DECIMAL)
	public CField deductoff; // 扣休息时数
	@CFieldinfo(fieldname = "over_type", caption = "加班类型", datetype = Types.INTEGER)
	public CField over_type; // 加班类型
	@CFieldinfo(fieldname = "check_type", caption = "加班打卡", datetype = Types.INTEGER)
	public CField check_type; // 加班打卡
	@CFieldinfo(fieldname = "begin_date", caption = "申请加班开始时间", datetype = Types.TIMESTAMP)
	public CField begin_date; // 申请加班开始时间
	@CFieldinfo(fieldname = "end_date", caption = "申请加班结束时间", datetype = Types.TIMESTAMP)
	public CField end_date; // 申请加班结束时间
	@CFieldinfo(fieldname = "alterbegin_date", caption = "加班调整开始时间", datetype = Types.TIMESTAMP)
	public CField alterbegin_date; // 加班调整开始时间
	@CFieldinfo(fieldname = "alterend_date", caption = "加班调整结束时间", datetype = Types.TIMESTAMP)
	public CField alterend_date; // 加班调整结束时间
	@CFieldinfo(fieldname = "dealtype", caption = "加班处理", datetype = Types.INTEGER)
	public CField dealtype; // 加班处理
	@CFieldinfo(fieldname = "otrate", caption = "加班倍率", datetype = Types.DECIMAL)
	public CField otrate; // 加班倍率
	@CFieldinfo(fieldname = "otreason", caption = "加班事由", datetype = Types.VARCHAR)
	public CField otreason; // 加班事由
	@CFieldinfo(fieldname = "isoffday", caption = "是否已调休", datetype = Types.INTEGER)
	public CField isoffday; // 是否已调休
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
	@CFieldinfo(fieldname = "creator", notnull = true, caption = "创建人", datetype = Types.VARCHAR)
	public CField creator; // 创建人
	@CFieldinfo(fieldname = "createtime", notnull = true, caption = "创建时间", datetype = Types.TIMESTAMP)
	public CField createtime; // 创建时间
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
	@CFieldinfo(fieldname = "orghrlev", caption = "机构人事层级", datetype = Types.INTEGER)
	public CField orghrlev; // 机构人事层级
	@CFieldinfo(fieldname = "emplev", caption = "人事层级", datetype = Types.INTEGER)
	public CField emplev; // 人事层级
	@CFieldinfo(fieldname = "lv_num", caption = "lv_num", datetype = Types.DECIMAL)
	public CField lv_num; // lv_num
	@CFieldinfo(fieldname = "sp_name", caption = "sp_name", datetype = Types.VARCHAR)
	public CField sp_name; // sp_name
	public String SqlWhere; // 查询附加条件
	public int MaxCount; // 查询最大数量

	// 自关联数据定义
	@CLinkFieldInfo(jpaclass = Hrkq_overtime_adjust_line.class, linkFields = { @LinkFieldItem(lfield = "otad_id", mfield = "otad_id") })
	public CJPALineData<Hrkq_overtime_adjust_line> hrkq_overtime_adjust_lines;

	public Hrkq_overtime_adjust() throws Exception {
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
