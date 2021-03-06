package com.hr.perm.entity;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.server.cjpa.CJPA;
import java.sql.Types;

@CEntity()
public class Hr_transfer_try extends CJPA {
	@CFieldinfo(fieldname = "transfertry_id", iskey = true, notnull = true, caption = "ID", datetype = Types.INTEGER)
	public CField transfertry_id; // ID
	@CFieldinfo(fieldname = "emptranf_id", notnull = true, caption = "调动单ID", datetype = Types.INTEGER)
	public CField emptranf_id; // 调动单ID
	@CFieldinfo(fieldname = "emptranfcode", notnull = true, caption = "调用编码", datetype = Types.VARCHAR)
	public CField emptranfcode; // 调用编码
	@CFieldinfo(fieldname = "er_id", notnull = true, caption = "人事档案ID", datetype = Types.INTEGER)
	public CField er_id; // 人事档案ID
	@CFieldinfo(fieldname = "employee_code", notnull = true, caption = "工号", datetype = Types.VARCHAR)
	public CField employee_code; // 工号
	@CFieldinfo(fieldname = "employee_name", notnull = true, caption = "姓名", datetype = Types.VARCHAR)
	public CField employee_name; // 姓名
	@CFieldinfo(fieldname = "degree", caption = "学历", datetype = Types.INTEGER)
	public CField degree; // 学历
	@CFieldinfo(fieldname = "id_number", notnull = true, caption = "身份证号", defvalue = "0", datetype = Types.VARCHAR)
	public CField id_number; // 身份证号
	@CFieldinfo(fieldname = "orgid", notnull = true, caption = "部门ID", datetype = Types.INTEGER)
	public CField orgid; // 部门ID
	@CFieldinfo(fieldname = "orgcode", notnull = true, caption = "部门编码", datetype = Types.VARCHAR)
	public CField orgcode; // 部门编码
	@CFieldinfo(fieldname = "orgname", notnull = true, caption = "部门名称", datetype = Types.VARCHAR)
	public CField orgname; // 部门名称
	@CFieldinfo(fieldname = "ospid", notnull = true, caption = "职位ID", datetype = Types.INTEGER)
	public CField ospid; // 职位ID
	@CFieldinfo(fieldname = "ospcode", notnull = true, caption = "职位编码", datetype = Types.VARCHAR)
	public CField ospcode; // 职位编码
	@CFieldinfo(fieldname = "sp_name", notnull = true, caption = "职位名称", datetype = Types.VARCHAR)
	public CField sp_name; // 职位名称
	@CFieldinfo(fieldname = "hwc_namezl", caption = "职类", datetype = Types.VARCHAR)
	public CField hwc_namezl; // 职类
	@CFieldinfo(fieldname = "hg_name", caption = "职等名称", datetype = Types.VARCHAR)
	public CField hg_name; // 职等名称
	@CFieldinfo(fieldname = "lv_num", caption = "职级", datetype = Types.DECIMAL)
	public CField lv_num; // 职级
	@CFieldinfo(fieldname = "entrydate", notnull = true, caption = "入职日期", datetype = Types.TIMESTAMP)
	public CField entrydate; // 入职日期
	@CFieldinfo(fieldname = "tranfcmpdate", caption = "调动生效时间", datetype = Types.TIMESTAMP)
	public CField tranfcmpdate; // 调动生效时间
	@CFieldinfo(fieldname = "probation", caption = "考察期", datetype = Types.INTEGER)
	public CField probation; // 考察期
	@CFieldinfo(fieldname = "probationdate", notnull = true, caption = "转正日期 第一次约定的考察到期", datetype = Types.TIMESTAMP)
	public CField probationdate; // 转正日期 第一次约定的考察到期
	@CFieldinfo(fieldname = "probationdatetrue", caption = "实际转正日期 根据转正评审结果确定 如延长考察期则为空", datetype = Types.TIMESTAMP)
	public CField probationdatetrue; // 实际转正日期 根据转正评审结果确定 如延长考察期则为空
	@CFieldinfo(fieldname = "wfresult", caption = "评审结果  1 同意转正 2 延长试用 3 考察不合格", datetype = Types.INTEGER)
	public CField wfresult; // 评审结果 1 同意转正 2 延长试用 3 考察不合格
	@CFieldinfo(fieldname = "delayprobation", caption = "延长考察期", datetype = Types.INTEGER)
	public CField delayprobation; // 延长考察期
	@CFieldinfo(fieldname = "delaypromotionday", caption = "延期待转正时间 根据延长考察期自动计算，多次延期累计计算", datetype = Types.TIMESTAMP)
	public CField delaypromotionday; // 延期待转正时间 根据延长考察期自动计算，多次延期累计计算
	@CFieldinfo(fieldname = "delaypromotiondaytrue", caption = "延期实际转正时间", datetype = Types.TIMESTAMP)
	public CField delaypromotiondaytrue; // 延期实际转正时间
	@CFieldinfo(fieldname = "delaywfresult", caption = "评审结果  1 同意转正 2 延长考察 3 考察不合格", datetype = Types.INTEGER)
	public CField delaywfresult; // 评审结果 1 同意转正 2 延长考察 3 考察不合格
	@CFieldinfo(fieldname = "delaytimes", caption = "延期次数", datetype = Types.INTEGER)
	public CField delaytimes; // 延期次数
	@CFieldinfo(fieldname = "trystat", notnull = true, caption = "考察期人事状态 考察期中、考察过期、考察延期、己转正、考察不合格", datetype = Types.INTEGER)
	public CField trystat; // 考察期人事状态 考察期中、考察过期、考察延期、己转正、考察不合格
	@CFieldinfo(fieldname = "remark", caption = "备注", datetype = Types.VARCHAR)
	public CField remark; // 备注
	@CFieldinfo(fieldname = "wfid", caption = "wfid", datetype = Types.INTEGER)
	public CField wfid; // wfid
	@CFieldinfo(fieldname = "attid", caption = "attid", datetype = Types.INTEGER)
	public CField attid; // attid
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
	public CField attribute1; // 备用字段1 //记录自动转正信息
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

	public Hr_transfer_try() throws Exception {
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
