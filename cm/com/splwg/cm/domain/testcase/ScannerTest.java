package com.splwg.cm.domain.testcase;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.cm.domain.batch.CmEmployerRegConstant;

public class ScannerTest {

	public static void main(String[] args) throws IOException {
		
		/*String money = "21206";
		Float.valueOf(money);
		*/
		String dateInString = "7-Jun-2013";
		try {
			com.splwg.base.api.datatypes.Date date = com.splwg.base.api.datatypes.Date.
					fromString(dateInString, new DateFormat("dd-MMM-yyyy"));
			System.out.println("date:: "+ date);
		} catch (DateFormatParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> myList = new ArrayList<String>();
		myList.add("123");
		myList.add("324");
		myList.add("2132");
		myList.add("12331");
		
		int money = 100;
		int value = 0;
		
		for (int i = 0; i < myList.size(); i++) {
		
			if(i == myList.size()-1) {
				System.out.println(myList.get(i));
			} else {
				System.out.println(myList.get(i));
			}
		}
		
		int a = 100;
		int div = Math.floorDiv(300, 100);
		System.out.println("Answer: " + Math.multiplyExact(div, 100));
		System.out.println("After Round of:: " + Math.round(Float.valueOf("100")/Float.valueOf("300")*Float.valueOf("100")));

		File file = null;
		file = new File("D:\\PSRM\\Denash\\Registration\\458343240EMPLR.csv");
		Scanner scanner = new Scanner(file);
		List<Object> al = new ArrayList<Object>();
		int count = 0;
		while (scanner.hasNextLine()) {
			
			//System.out.println("***Reading From File****Line Number****" + scanner.nextLine());
			String csvValues = scanner.nextLine();
			System.out.println("***Reading From File****Line Number**** " +csvValues);
			if(count >= 1) {
				String[] terms = csvValues.split(",");
				for(int i = 0; i<terms.length; i++) {
					if(i == 15 || i == 30) {
						terms[i] = URLEncoder.encode(terms[i], CmEmployerRegConstant.UTF);
					}
					al.add(terms[i]);	
				}
			}
			count++;
		}
		
		System.out.println("ArrayList: " + al);
	}
}
