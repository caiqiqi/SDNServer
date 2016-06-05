package caiqiqi.sdnserver;


import java.util.Date;
import java.text.SimpleDateFormat;

public class CurrentTimeFormat {
	
	
	public static String getTimeInFormat(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//设置日期格式
		String timeInFormat = df.format(new Date()); 
		//返回一个特定格式的时间字符串
		return timeInFormat;
	}
}
