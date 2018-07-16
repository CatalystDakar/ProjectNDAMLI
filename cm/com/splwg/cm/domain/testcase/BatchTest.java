package com.splwg.cm.domain.testcase;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.apache.poi.ss.usermodel.CellType;

public class BatchTest {
		
		public void readExcel(String filePath,String fileName,String sheetName) throws IOException{
		    
		    File file =    new File(filePath+"\\"+fileName);
		    FileInputStream inputStream = new FileInputStream(file);
		    Workbook workbook = null;
		    String fileExtensionName = fileName.substring(fileName.indexOf("."));
		    if(fileExtensionName.equals(".xlsx")){
		    	workbook = new XSSFWorkbook(inputStream);

		    }
		    else if(fileExtensionName.equals(".xls")){
		    	workbook = new HSSFWorkbook(inputStream);

		    }
		    final String MM_DD_YYYY = "MM/dd/yyyy";
		   Sheet sheet = workbook.getSheet(sheetName);
		  		   int rowCount = sheet.getLastRowNum()-sheet.getFirstRowNum();
		    for (int i = 1; i < rowCount+1; i++) {
		        Row row = sheet.getRow(i);
		        for (int j = 0; j < row.getLastCellNum(); j++) {
		       Cell cell = row.getCell(j);
		       System.out.println(cell);
		       
		       System.out.println(HSSFDateUtil.isCellDateFormatted(cell));
		       SimpleDateFormat DtFormat = new SimpleDateFormat("dd/MM/yyyy");
		       System.out.println(DtFormat.format(cell.getDateCellValue()));
		       System.out.println(cell.getCellStyle().getDataFormatString());
		       
		       
		       Object o = row.getCell(j).toString();
		        System.out.print(o.toString()+"\n ");
		        }
		       // System.out.println();

		    }
		   
		    }

		    public static void main(String p[]) throws IOException, Exception{
		    	BatchTest objExcelFile = new BatchTest();
		    	
		    	BigDecimal screenAm = new BigDecimal("10000");
		    	BigDecimal obl1 = new BigDecimal("5000");
		    	BigDecimal obl2 = new BigDecimal("4000");
		    	BigDecimal obl3 = new BigDecimal("300");
		    	
		    	
		    	BigDecimal total = obl1.add(obl2).add(obl3);
		    	System.out.println(screenAm.multiply(obl1.divide(total)));
		    	System.out.println(screenAm.multiply(obl2.divide(total)));
		    	System.out.println(screenAm.multiply(obl3.divide(total)));
		    	
		    	System.out.println(total);

		    	/*String screenAm = "15000";
		    	String obl1 = "5000";
		    	String obl2 = "10000";
		    	String obl3 = "3000";
		    	String total = "18000";*/
		    	//15000*(5000/18000)
		    	String date_s = "Mon Mar 12 00:00:00 UTC 2018";
	             //SimpleDateFormat dt = new SimpleDateFormat("dd/mm/yyyy");
	             DateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss 'UTC' YYYY");
	             java.util.Date date = inputFormat.parse(date_s);
	               
	             DateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	             //dt1.setTimeZone(TimeZone.getTimeZone("UTC"));
	             String parsedDate = dt1.format(date);
	              System.out.println(dt1.format(date));
		    	
		    	
		    	/*//Thu Jan 04 00:00:00 GMT 2018 //Sun Mar 11 00:00:00 UTC 2018
		    	String appDate = "Thu Jan 04 00:00:00 UTC 2018";
		    	//String input = "Thu Jun 06 2015 00:00:00 GMT+0530 (India Standard Time)";
		        //DateFormat inputFormat = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss 'GMT'z", Locale.ENGLISH);
		    	DateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss 'UTC' yyyy");
		        Date date = inputFormat.parse(appDate);

		        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		        String output = outputFormat.format(date);
		        System.out.println(output);*/
		    	
		    String filePath ="D:\\Batcch\\";
		   // objExcelFile.readExcel(filePath,"Immatriculation.xlsx","sheet1");

		    }

	}


