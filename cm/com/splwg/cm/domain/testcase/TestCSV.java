package com.splwg.cm.domain.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.splwg.cm.domain.common.constant.CmEmployerRegConstant;
import com.splwg.cm.domain.common.constant.CmEmployerRegHelper;

public class TestCSV {
	
	private final static CmEmployerRegHelper customHelper = new CmEmployerRegHelper();
	private static Boolean checkValidationFlag = false;
	
	public void start() {
		
	}

	
	public static void moveFileToFailuireFolder(String fileName, String parameter) {
		Path fileToMovePath = Paths.get(fileName);
		Path targetPath = Paths.get(parameter);
		try {
			Files.move(fileToMovePath, targetPath.resolve(fileToMovePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}
	
	public static void move(String fileName, String parameter) {
		File file = new File(fileName);
	    
	    // renaming the file and moving it to a new location
	    if(file.renameTo(new File(parameter)))
	    {
	        // if file copied successfully then delete the original file
	        file.delete();
	        System.out.println("File moved successfully");
	    }
	    else
	    {
	        System.out.println("Failed to move the file");
	    }
	}
	

	public static void main(String[] args) {
		TestCSV testCSV = new TestCSV();
		
		testCSV.move("D:\\PSRM\\Denash\\Registration\\254189351EMPLR.csv", "D:\\PSRM\\Denash\\Failure\\");
		//testCSV.moveFileToFailuireFolder("D:\\PSRM\\Denash\\Registration\\254189351EMPLR.csv", "D:\\PSRM\\Denash\\Failure\\");
		
		String trimString = "hello    there  i am   here ";
		trimString = trimString.trim().replaceAll("\\s{2,}", " ");
		System.out.println("trimString::" + trimString +"trimString");

		String regFileName = "D:\\PSRM\\Denash\\Registration\\909090909EMPLR.csv";
		File file = null;
		file = new File(regFileName);
		List<String> headerValues = new ArrayList<String>();
		List<String> listValues = new ArrayList<String>();
		List<String> formCreatorList = new ArrayList<String>();
		String[] headerArray;
		String[] valueArray;
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("File Not Found ");
		}

		int count = 0;

		while (scanner.hasNextLine()) {

			// System.out.println("***Reading From File****Line Number****" +
			// scanner.nextLine());
			String csvValues = scanner.nextLine();

			// System.out.println("***Reading From File****Line Number**** " +
			// csvValues);

			if (count == 0) {
				headerArray = csvValues.split(",");
				for (int i = 0; i < headerArray.length; i++) {
					headerValues.add(headerArray[i]);
				}
			} else if (count == 1) {
				valueArray = csvValues.split(",");
				for (int i = 0; i < valueArray.length; i++) {
					valueArray[i] = valueArray[i].replace("é", "e");
					valueArray[i] = valueArray[i].replace("è", "e");
					valueArray[i] = valueArray[i].replace("à", "a");
					listValues.add(valueArray[i]);
				}

			}
			count++;
		}
		System.out.println("***Reading From File****headerValues**** " + headerValues);
		System.out.println("***Reading From File****listValues**** " + listValues);

		Iterator<String> headerIterator = headerValues.iterator();
		Iterator<String> listIterator = listValues.iterator();
		while (headerIterator.hasNext() && listIterator.hasNext()) {
			String header = headerIterator.next();
			String value = listIterator.next();
			try {
				String headerName = URLEncoder.encode(header.trim(), CmEmployerRegConstant.UTF);
				if (headerName != null && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.EMAIL_EMPLOYER, CmEmployerRegConstant.UTF))) {
					checkValidationFlag = customHelper.validateEmail(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						System.out.println("Given Ninea Number:  having Incorrect Email Id: " + value);
					} else {
						formCreatorList.add(value);
					}
					//checkErrorInCSV = true;
					//createToDo(csvList.get(validationIndex + ((header.length - 1) * rowCount)).toString().trim(), nineaNumber, CmEmployerRegConstant.EMPLOYER_EMAIL_INVALID, fileName);
					

				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	
		}
	}
}
