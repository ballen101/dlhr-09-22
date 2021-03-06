package com.hr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.corsair.dbpool.util.Systemdate;

public class DateUtil {
	/*
	 * 把MM/dd/yyyy转成yyyy/MM/dd
	 */
	public static String DateParseYYYYMMDD(String d1) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Date date = format.parse(d1);//有异常要捕获
		format = new SimpleDateFormat("yyyy-MM-dd");
		return  format.format(date);
	}
	
	/**
	 * 获取当前月的第一天  
	 * @return
	 */
	public static  String getFirstDayOfMonth(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
		String firstday;  
		// 获取当前月的第一天  
		Calendar cale = Calendar.getInstance();  
		cale.add(Calendar.MONTH, 0);  
		cale.set(Calendar.DAY_OF_MONTH, 1);  
		firstday = format.format(cale.getTime());
		return firstday;
	}


	/**
	 * 获取当前月的第一天  
	 * @return
	 */
	public static  String getFirstDayOfMonth(String yerDateString){
		Date yerDates =Systemdate.getDateByyyyy_mm_dd(yerDateString);
		Calendar cal = Calendar.getInstance();
		cal.setTime(yerDates);
		int year=cal.get(Calendar.YEAR);
		int month=cal.get(Calendar.MONTH) + 1;
		String firstday=year+"-"+month+"-1";  

		return firstday;
	}
	/**
	 * 获取当前月的最后一天  
	 * @return
	 */
	public static  String getLastDayOfMonth(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
		String  lastday;  
		Calendar cale = Calendar.getInstance();  
		cale.add(Calendar.MONTH, 1);  
		cale.set(Calendar.DAY_OF_MONTH, 0);  
		lastday = format.format(cale.getTime());
		return lastday;
	}

	/**
	 * 获取当前日期
	 * @return
	 */
	public static  String getNowDate(){
		String  now;  
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
		Date d = new Date();
		now = format.format(d);
		return now;
	}
	/**
	 * 获取昨天日期
	 * @return
	 */
	public static  String getYerDate(){
		String yer;  
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE,-1);
		Date d=cal.getTime();
		yer=format.format(d);
		return yer;
	}

	
	public  static long getBetweenDays(Date dateStart,Date dateEnd) throws ParseException{
		// 获取日期
		Date date1 =Systemdate.getDateYYYYMMDD(dateStart);
		Date date2 = Systemdate.getDateYYYYMMDD(dateEnd);
		// 获取相差的天数
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date1);
		long timeInMillis1 = calendar.getTimeInMillis();
		calendar.setTime(date2);
		long timeInMillis2 = calendar.getTimeInMillis();

		long betweenDays =  (timeInMillis2 - timeInMillis1) / (1000L*3600L*24L);
		return betweenDays;
	}


	public static void main(String[] args) {
		
	

	}

}
