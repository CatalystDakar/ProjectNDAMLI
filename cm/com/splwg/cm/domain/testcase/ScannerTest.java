package com.splwg.cm.domain.testcase;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.splwg.cm.domain.batch.CmEmployerRegConstant;

public class ScannerTest {

	public static void main(String[] args) throws IOException {

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
