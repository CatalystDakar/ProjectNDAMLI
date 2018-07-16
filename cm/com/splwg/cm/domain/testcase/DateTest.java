package com.splwg.cm.domain.testcase;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

public class DateTest {
	
	public static Date funx(String S) {

	    String DateStr = S;
	    Date d = null;
	    try {
	        d = new SimpleDateFormat("yyyy-MM-dd").parse(DateStr);
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    java.sql.Date d1 = new java.sql.Date(d.getTime());

	    return d1;
	}

	public static void main(String[] args) throws IOException {
		
		String dateString1 = "2018-05-01-00.00.00";
		String oblId1 = "4062146591, 4062146591, 4062146591";
		String dateString2 = "2018-05-01-00.00.00";
		String oblId2 = "5698052439, 4062146591, 4062146591";
		String dateString3 = "2018-05-01-00.00.00";
		String oblId3 = "4062146736";
		
		if(dateString1.contains(",") || oblId1.contains(",") || oblId2.contains(",")) {
			 String dateString1Arry[] = dateString1.split(",");
			 String oblId1Arry[] = oblId1.split(","); 
			 String oblId2Arry[] = oblId2.split(","); 
			 
			 String join = "'" + StringUtils.join(dateString1Arry,"','") + "'";
			 System.out.println(join);
			 String join1 = "'" + StringUtils.join(oblId1Arry,"','") + "'";
			 System.out.println(join1);
			 String join2 = "'" + StringUtils.join(oblId2Arry,"','") + "'";
			 System.out.println(join2);
		}
		
		Map<String, String> ham = new TreeMap<String, String>();
		ham.put(dateString1, oblId1);
		ham.put(dateString2, oblId1);
		ham.put(dateString3, oblId1);
		
	}
}
