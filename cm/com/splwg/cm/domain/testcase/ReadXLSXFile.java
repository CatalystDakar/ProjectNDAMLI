package com.splwg.cm.domain.testcase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Iterator;
 
//apache poi imports
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
public class ReadXLSXFile {
 
	public void givenUsingJDK7Nio2_whenMovingFile_thenCorrect() throws IOException {
		String fineName = "";
	    //Path fileToMovePath = Files.createFile(Paths.get("D:\\PSRM\\Bala\\cnt1790655.ppt"));
		Path fileToMovePath = Paths.get("D:\\Batcch\\Immatriculation.xlsx");
	    Path targetPath = Paths.get("D:\\PSRM\\");
	 
	    Files.move(fileToMovePath, targetPath.resolve(fileToMovePath.getFileName()));
	}
	
	
	public void moveFileToProcessedFolder(String fileName, String parameter) {
		Path fileToMovePath = Paths.get(fileName);
		Path targetPath = Paths.get(parameter);
		try {
			Files.move(fileToMovePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
			exception.getCause();
		}
	}
	
	
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
    	DecimalFormat df = new DecimalFormat("0000"); 
    	String tin = "";
    	System.out.println(tin.toUpperCase());
    	String decimal = df.format(9); // 0009 String a = df.format(99); // 0099 String b = df.format(999);
    	System.out.println(decimal);
    	
    	String source = "D:\\Batcch\\Immatriculation.xlsx";
    	String dest = "D:\\PSRM\\Immatriculation.xlsx";
    	ReadXLSXFile readXLSXFile = new ReadXLSXFile();
    	readXLSXFile.moveFileToProcessedFolder(source, dest);
 
    }
 
}
