package com.hr.salary.entity;

import com.corsair.cjpa.CField;
import com.corsair.cjpa.CJPALineData;
import com.corsair.cjpa.util.CEntity;
import com.corsair.cjpa.util.CFieldinfo;
import com.corsair.server.cjpa.CJPA;
import java.sql.Types;

@CEntity(caption = "调薪表单（跟业务表单）", tablename = "Hr_salary_chgbill")
public class Hr_salary_chgbill extends CJPA {
	@CFieldinfo(fieldname = "cbid", iskey = true, notnull = true, precision = 10, scale = 0, caption = "ID", datetype = Types.INTEGER)
	public CField cbid; // ID
	@CFieldinfo(fieldname = "er_id", notnull = true, precision = 10, scale = 0, caption = "人事ID", datetype = Types.INTEGER)
	public CField er_id; // 人事ID
	@CFieldinfo(fieldname = "scatype", notnull = true, precision = 2, scale = 0, caption = "入职定薪、调薪核薪、转正调薪、年度调薪、特殊调薪", datetype = Types.INTEGER)
	public CField scatype; // 入职定薪、调薪核薪、转正调薪、年度调薪、特殊调薪
	@CFieldinfo(fieldname = "stype", notnull = true, precision = 2, scale = 0, caption = "来源类别 历史数据、实习登记、入职表单、调动表单、入职转正、调动转正....", datetype = Types.INTEGER)
	public CField stype; // 来源类别 历史数据、实习登记、入职表单、调动表单、入职转正、调动转正....
	@CFieldinfo(fieldname = "sid", notnull = true, precision = 10, scale = 0, caption = "来源ID", datetype = Types.INTEGER)
	public CField sid; // 来源ID
	@CFieldinfo(fieldname = "scode", notnull = true, precision = 15, scale = 0, caption = "来源单号", datetype = Types.VARCHAR)
	public CField scode; // 来源单号
	@CFieldinfo(fieldname = "oldstru_id", precision = 10, scale = 0, caption = "调薪前工资结构ID", datetype = Types.INTEGER)
	public CField oldstru_id; // 调薪前工资结构ID
	@CFieldinfo(fieldname = "oldstru_name", precision = 64, scale = 0, caption = "调薪前工资结构名", datetype = Types.VARCHAR)
	public CField oldstru_name; // 调薪前工资结构名
	@CFieldinfo(fieldname = "oldchecklev", precision = 2, scale = 0, caption = "调薪前绩效考核层级", datetype = Types.INTEGER)
	public CField oldchecklev; // 调薪前绩效考核层级
	@CFieldinfo(fieldname = "oldattendtype", precision = 32, scale = 0, caption = "调薪前出勤类别", datetype = Types.VARCHAR)
	public CField oldattendtype; // 调薪前出勤类别
	@CFieldinfo(fieldname = "oldcalsalarytype", precision = 32, scale = 0, caption = "调薪前计薪方式", datetype = Types.VARCHAR)
	public CField oldcalsalarytype; // 调薪前计薪方式
	@CFieldinfo(fieldname = "oldposition_salary", precision = 10, scale = 2, caption = "调薪前职位工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldposition_salary; // 调薪前职位工资
	@CFieldinfo(fieldname = "oldbase_salary", precision = 10, scale = 2, caption = "调薪前基本工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldbase_salary; // 调薪前基本工资
	@CFieldinfo(fieldname = "oldtech_salary", precision = 10, scale = 2, caption = "调薪前技能工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldtech_salary; // 调薪前技能工资
	@CFieldinfo(fieldname = "oldachi_salary", precision = 10, scale = 2, caption = "调薪前绩效工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldachi_salary; // 调薪前绩效工资
	@CFieldinfo(fieldname = "oldotwage", precision = 10, scale = 2, caption = "调薪前固定加班工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldotwage; // 调薪前固定加班工资
	@CFieldinfo(fieldname = "oldtech_allowance", precision = 10, scale = 2, caption = "调薪前技术津贴", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldtech_allowance; // 调薪前技术津贴
	@CFieldinfo(fieldname = "oldparttimesubs", precision = 10, scale = 2, caption = "调薪前兼职津贴", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldparttimesubs; // 调薪前兼职津贴
	@CFieldinfo(fieldname = "oldpostsubs", precision = 10, scale = 2, caption = "调薪前岗位津贴", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldpostsubs; // 调薪前岗位津贴
	@CFieldinfo(fieldname = "oldovt_salary", precision = 10, scale = 2, caption = "调薪前加班工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldovt_salary; // 调薪前加班工资
	@CFieldinfo(fieldname = "oldavg_salary", precision = 10, scale = 2, caption = "调薪前平均工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField oldavg_salary; // 调薪前平均工资
	@CFieldinfo(fieldname = "newstru_id", precision = 10, scale = 0, caption = "调薪后工资结构ID", datetype = Types.INTEGER)
	public CField newstru_id; // 调薪后工资结构ID
	@CFieldinfo(fieldname = "newstru_name", precision = 64, scale = 0, caption = "调薪后工资结构名", datetype = Types.VARCHAR)
	public CField newstru_name; // 调薪后工资结构名
	@CFieldinfo(fieldname = "newchecklev", precision = 2, scale = 0, caption = "调薪后绩效考核层级", datetype = Types.INTEGER)
	public CField newchecklev; // 调薪后绩效考核层级
	@CFieldinfo(fieldname = "newattendtype", precision = 32, scale = 0, caption = "调薪后出勤类别", defvalue = "0", datetype = Types.VARCHAR)
	public CField newattendtype; // 调薪后出勤类别
	@CFieldinfo(fieldname = "newcalsalarytype", precision = 32, scale = 0, caption = "调薪后计薪方式", defvalue = "0", datetype = Types.VARCHAR)
	public CField newcalsalarytype; // 调薪后计薪方式
	@CFieldinfo(fieldname = "newposition_salary", precision = 10, scale = 2, caption = "调薪后职位工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newposition_salary; // 调薪后职位工资
	@CFieldinfo(fieldname = "newbase_salary", precision = 10, scale = 2, caption = "调薪后基本工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newbase_salary; // 调薪后基本工资
	@CFieldinfo(fieldname = "newtech_salary", precision = 10, scale = 2, caption = "调薪后技能工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newtech_salary; // 调薪后技能工资
	@CFieldinfo(fieldname = "newachi_salary", precision = 10, scale = 2, caption = "调薪后绩效工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newachi_salary; // 调薪后绩效工资
	@CFieldinfo(fieldname = "newotwage", precision = 10, scale = 2, caption = "调薪后固定加班工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newotwage; // 调薪后固定加班工资
	@CFieldinfo(fieldname = "newtech_allowance", precision = 10, scale = 2, caption = "调薪后技术津贴", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newtech_allowance; // 调薪后技术津贴
	@CFieldinfo(fieldname = "newparttimesubs", precision = 10, scale = 2, caption = "调薪后兼职津贴", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newparttimesubs; // 调薪后兼职津贴
	@CFieldinfo(fieldname = "newpostsubs", precision = 10, scale = 2, caption = "调薪后岗位津贴", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newpostsubs; // 调薪后岗位津贴
	@CFieldinfo(fieldname = "newovt_salary", precision = 10, scale = 2, caption = "调薪后加班工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newovt_salary; // 调薪后加班工资
	@CFieldinfo(fieldname = "newavg_salary", precision = 10, scale = 2, caption = "调薪后平均工资", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField newavg_salary; // 调薪后平均工资
	@CFieldinfo(fieldname = "sacrage", precision = 10, scale = 2, caption = "调薪幅度", defvalue = "0.00", datetype = Types.DECIMAL)
	public CField sacrage; // 调薪幅度
	@CFieldinfo(fieldname = "chgdate", notnull = false, precision = 10, scale = 0, caption = "调薪日期", datetype = Types.DATE)
	public CField chgdate; // 调薪日期
	@CFieldinfo(fieldname = "chgreason", precision = 128, scale = 0, caption = "调薪原因", datetype = Types.VARCHAR)
	public CField chgreason; // 调薪原因
	@CFieldinfo(fieldname = "remark", precision = 128, scale = 0, caption = "备注", datetype = Types.VARCHAR)
	public CField remark; // 备注
	@CFieldinfo(fieldname = "createtime", notnull = true, precision = 19, scale = 0, caption = "创建时间", datetype = Types.TIMESTAMP)
	public CField createtime; // 创建时间
	@CFieldinfo(fieldname = "creator", precision = 32, scale = 0, caption = "创建人", datetype = Types.VARCHAR)
	public CField creator; // 创建人
	public String SqlWhere; // 查询附加条件
	public int MaxCount; // 查询最大数量

	// 自关联数据定义

	public Hr_salary_chgbill() throws Exception {
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
