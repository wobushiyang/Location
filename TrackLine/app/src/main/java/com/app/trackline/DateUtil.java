package com.app.trackline;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转换的工具类
 */
public class DateUtil {

    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String toDate(Date date){
        return sdf.format(date);
    }

}

