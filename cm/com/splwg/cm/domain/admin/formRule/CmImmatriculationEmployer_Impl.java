package com.splwg.cm.domain.admin.formRule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.cm.domain.common.businessComponent.CmXLSXReaderComponent;
import com.splwg.cm.domain.common.constant.CmEmployerRegConstant;
import com.splwg.cm.domain.common.constant.CmEmployerRegHelper;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formType.FormType;
import com.splwg.tax.domain.admin.formType.FormType_Id;

/**
 * @author Denash Kumar M
 *
 * @AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = formType, required = true, type = string)
 * 					, @AlgorithmSoftParameter (name = filePath, required = true, type = string)
 *                  , @AlgorithmSoftParameter (name = successFilePath, required = true, type = string)
 *                  , @AlgorithmSoftParameter (name = errorFilePath, required = true, type = string)})
 */
public class CmImmatriculationEmployer_Impl extends CmImmatriculationEmployer_Gen
		implements BusinessObjectExitStatusAlgorithmSpot {

	private final static Logger log = LoggerFactory.getLogger(CmImmatriculationEmployer_Impl.class);
	private BusinessObjectInstanceKey boKey;
	private BusinessObjectInstance boInstance;
	String fileName = null;
	private final static CmEmployerRegHelper customHelper = new CmEmployerRegHelper();
	private final static CmEmployerRegConstant cmConstants = new CmEmployerRegConstant();
	final static HashMap<String, String> regionMap = new HashMap<String, String>();
	final static HashMap<String, String> deptMap = new HashMap<String, String>();
	final static HashMap<String, String> arrondMap = new HashMap<String, String>();
	final static HashMap<String, String> communeMap = new HashMap<String, String>();
	final static HashMap<String, String> qartierMap = new HashMap<String, String>();
	final static HashMap<String, String> agenceMap = new HashMap<String, String>();
	final static HashMap<String, String> zoneMap = new HashMap<String, String>();
	final static HashMap<String, String> sectorMap = new HashMap<String, String>();
	final static HashMap<String, String> sectorActMap = new HashMap<String, String>();
	final static HashMap<String, String> actPrinceMap = new HashMap<String, String>();
	final static HashMap<String, String> atRateMap = new HashMap<String, String>();

	// Fields for Constants

	public static final String AS_CURRENT = "asCurrent";
	Calendar calInstacne = Calendar.getInstance();
	Date statusUploadDate = new Date();
	String nineaNumber = null;
	String actualHeader = null;
	String processFlowId = null;
	
	private CmXLSXReaderComponent cmXLSXReader = CmXLSXReaderComponent.Factory.newInstance();
	//private static CmConstant cmConstant = new CmConstant();
	XSSFSheet spreadsheet;
	private int cellId = 0;
	PreparedStatement psPreparedStatement = null;
	StringBuilder stringBuilder = null;

	@Override
	public void invoke() {

		System.out.println("######################## Demarrage executeWorkUnit ############################");
		
		System.out.println("I am In Invoke method " + this.boKey);
		log.info("I am In Invoke method BO intance Key " + this.boKey);
		this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		log.info("I am In Invoke method BO intance " + this.boInstance);
	    COTSFieldDataAndMD<?> cots = this.boInstance.getFieldAndMDForPath("employerDetails/ninea");
	    COTSFieldDataAndMD<?> cotsNode = this.boInstance.getFieldAndMDForPath("processFlowId");
	    processFlowId = cotsNode.getValue().toString();
	    System.out.println("processFlowId:: " + processFlowId);
	    log.info("processFlowId:: " + processFlowId);
		String nineaNumber = cots.getValue().toString();
		//String nineaNumber = "458343243";
		System.out.println("Ninea: " + nineaNumber);// 90909090990EMPLR.xlsx/99009099909099EMPLE.xlsx
		log.info("Ninea: " + nineaNumber);
		fileName = this.getFilePath()+nineaNumber + "EMPLR" + ".xlsx";
		this.nineaNumber = nineaNumber;
		//boolean fileExist = verifyExistFileInFolder(this.getFilePath() + fileName);	
		
		boolean foundNinea = false, checkErrorInExcel = false, processed = false,processStgFlag =false;
		Cell cell;
		List<Object> listesValues = new ArrayList<Object>();
		log.info("*****Starting Execute Work Unit");
		log.info("*****executeWorkUnit : " + fileName);
		cmXLSXReader.openXLSXFile(fileName);
		Set<String> headerConstants = getHeaderConstants();
		spreadsheet = cmXLSXReader.openSpreadsheet(0, null);
		int rowCount = spreadsheet.getLastRowNum() - spreadsheet.getFirstRowNum();
		System.out.println("rowCount:: " + rowCount);
		
		Iterator<Row> rowIterator = spreadsheet.iterator();
		int cellCount = spreadsheet.getRow(0).getLastCellNum();
		log.info("CellCount:: " + cellCount);
		while (rowIterator.hasNext()) {
			XSSFRow row = (XSSFRow) rowIterator.next();
			if (row.getRowNum() >= 2) {
				break;
			}
			cellId = 1;
			String establishmentDate = null, immatriculationDate = null, premierEmployeeDate = null, deliveryDate = null;
			Boolean checkValidationFlag = false;
			if (row.getRowNum() == 0) {
				continue;
			}
			log.info("#############----ENTERTING INTO ROW-----#############:" + row.getRowNum());

			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext() && !foundNinea) {
				try {
					while (cellId <= cellCount && !checkErrorInExcel) {
						cell = cellIterator.next();
						String headerName = URLEncoder.encode(
								cell.getSheet().getRow(0).getCell(cellId - 1).getRichStringCellValue().toString().trim(),
								CmEmployerRegConstant.UTF);
						String actualHeader = cell.getSheet().getRow(0).getCell(cellId - 1).getRichStringCellValue()
								.toString().trim();
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							if (headerName != null && headerName
									.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.EMAIL_EMPLOYER, CmEmployerRegConstant.UTF))) {
								checkValidationFlag = customHelper.validateEmail(cell.getStringCellValue().trim());
								if (checkValidationFlag != null && !checkValidationFlag) {// Email validation
									// Error Skip the row
									checkErrorInExcel = true;
									createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.EMPLOYER_EMAIL_INVALID, fileName);
									log.info("Given Ninea Number: " + nineaNumber + " having Incorrect Email Id: "
											+ cell.getStringCellValue().trim());
									break;
								} else {
									checkValidationFlag = customHelper.validateEmailExist(cell.getStringCellValue().trim());
									if (checkValidationFlag != null && checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.EMPLOYER_EMAIL_EXIST, fileName);
										log.info("Given Email ID:--> " + cell.getStringCellValue().trim()
												+ " already Exists");
										break;
									}
								}
								listesValues.add(cell.getStringCellValue());
							} else if (headerName != null
									&& headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.EMAIL, CmEmployerRegConstant.UTF))) {
									checkValidationFlag = customHelper.validateEmail(cell.getStringCellValue().trim());
								if (checkValidationFlag != null && !checkValidationFlag) {// Email validation
									// Error Skip the row
									checkErrorInExcel = true;
									createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.EMAIL_INVALID, fileName);
									log.info("Given Ninea Number: " + nineaNumber + " having Incorrect Email Id: "
											+ cell.getStringCellValue().trim());
									break;
								}
								listesValues.add(cell.getStringCellValue().trim());
							} else if (headerName != null && headerName.equalsIgnoreCase(
									URLEncoder.encode(CmEmployerRegConstant.TRADE_REG_NUM, CmEmployerRegConstant.UTF))) {
								if (customHelper.validateCommercialRegister(cell.getStringCellValue().trim())) {// Validation trade register number
									checkValidationFlag = customHelper.validateTRNExist(cell.getStringCellValue().trim());
									if (checkValidationFlag != null && checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.TRN_EXIST, fileName);
										log.info("Given Trade Registration Number--> " + cell.getStringCellValue().trim()
												+ " already Exists");
										break;
									}
								} else {
									checkErrorInExcel = true;
									createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.TRN_INVALID, fileName);
									log.info("Given Trade Registration Number:--> " + cell.getStringCellValue().trim()
											+ " is Invalid ");
									break;
								}
								listesValues.add(cell.getStringCellValue());
							} else if (headerName != null && headerName.equalsIgnoreCase(
									URLEncoder.encode(CmEmployerRegConstant.TAX_IDENTIFY_NUM, CmEmployerRegConstant.UTF))) {
									checkValidationFlag = customHelper.validateTaxIdenficationNumber(cell.getStringCellValue().trim().toUpperCase());
								if (checkValidationFlag != null && !checkValidationFlag) {// TIN validation
									// Error Skip the row
									checkErrorInExcel = true;
									createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.TIN_INVALID, fileName);
									log.info("Given Ninea Number having Invalid TIN Number:" + nineaNumber);
									break;
								}
								listesValues.add(cell.getStringCellValue().trim().toUpperCase());
								break;
							} else if (headerName != null && (headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.LAST_NAME, CmEmployerRegConstant.UTF))
									|| headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.FIRST_NAME, CmEmployerRegConstant.UTF)))) {
									checkValidationFlag = customHelper.validateAlphabetsOnly(cell.getStringCellValue().trim());
								if (checkValidationFlag != null && !checkValidationFlag) { // Alphabets only
									// Error Skip the row
									checkErrorInExcel = true;
									createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.NAME_INVALID, fileName);
									log.info("Given " + actualHeader
											+ "is having special characters or number for the given ninea number:"
											+ nineaNumber);
									break;
								}
								listesValues.add(cell.getStringCellValue().trim());
								break;
							} else if (headerName != null && headerName.equalsIgnoreCase(CmEmployerRegConstant.NINET)) {
								checkErrorInExcel = true;
								createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.NINET_INVALID, fileName);
								log.info("Given " + headerName
										+ " having special characters or alphabets for the given ninea number:"
										+ nineaNumber);
								//listesValues.add(cell.getStringCellValue().trim());
								break;
							} else if (headerName != null && headerName.equalsIgnoreCase(CmEmployerRegConstant.NINEA)) {
								checkErrorInExcel = true;
								createToDo(cell.getStringCellValue().trim(), nineaNumber, CmEmployerRegConstant.NINEA_INVALID, fileName);
								log.info("Given " + headerName
										+ " having special characters or alphabets for the given ninea number:"
										+ nineaNumber);
								//listesValues.add(cell.getStringCellValue().trim());
								break;
							} else if (headerName != null && (headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.LEGAL_REP_NIN, CmEmployerRegConstant.UTF))
									|| headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.EMPLOYEE_NIN, CmEmployerRegConstant.UTF)))) {
								checkErrorInExcel = true;
								createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.NIN_INVALID, fileName);
								log.info("Given " + headerName
										+ " having special characters or alphabets for the given ninea number:"
										+ nineaNumber);
								//listesValues.add(cell.getStringCellValue().trim());
								break;
							} else if (headerName != null) {
								String cellValue = cell.getStringCellValue().trim();
								if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.REGION, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(regionMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(regionMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.DÉPARTEMENT, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(deptMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(deptMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.ARONDISSEMENT, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(arrondMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(arrondMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.COMMUNE, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(communeMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(communeMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.QUARTIER, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(qartierMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(qartierMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.AGENCE_CSS, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(agenceMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(agenceMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.AGENCE_IPRES, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(agenceMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(agenceMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_CSS, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(zoneMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(zoneMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_IPRES, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(zoneMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(zoneMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_CSS, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(sectorMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(sectorMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_IPRES, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(sectorMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(sectorMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.SECTEUR_ACTIVITIES, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(sectorActMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(sectorActMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.ACTIVATE_PRINCIPAL, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(actPrinceMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(actPrinceMap.get(cellValue));
									}
									break;
								} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.TAUX_AT, CmEmployerRegConstant.UTF))) {
									if(isBlankOrNull(atRateMap.get(cellValue))) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, "330", fileName);
									} else {
										listesValues.add(atRateMap.get(cellValue));
									}
									break;
								} else {
									listesValues.add(cell.getStringCellValue().trim());break;
								}
							} 
							
							System.out.println(cell.getStringCellValue().trim());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {// Date Validation
								// Immatriculation Date
								if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
										URLEncoder.encode(CmEmployerRegConstant.IMMATRICULATION_DATE, CmEmployerRegConstant.UTF))) {
									immatriculationDate = cell.getDateCellValue().toString().trim();
									checkValidationFlag = customHelper.compareDateWithSysDate(immatriculationDate, "lessEqual"); // validating with current date.
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
										log.info("Given->" + actualHeader + " Date greater than System Date-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
								}
								// Establishment Date
								if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
										URLEncoder.encode(CmEmployerRegConstant.ESTABLISHMENT_DATE, CmEmployerRegConstant.UTF))) {
									establishmentDate = cell.getDateCellValue().toString().trim();
									checkValidationFlag = customHelper.compareDateWithSysDate(establishmentDate,"lessEqual"); // validating with current date.
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
										log.info("Given->" + actualHeader + " Date greater than System Date-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
										checkValidationFlag = customHelper.checkDateSunOrSat(establishmentDate); // validating data is on sat or sun
									if (checkValidationFlag != null && checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_SAT_SUN_VALID, fileName);
										log.info("Given->" + actualHeader
												+ " Date should not be on Saturday or Sunday-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
									 checkValidationFlag = customHelper.compareTwoDates(establishmentDate,immatriculationDate, "greatEqual"); //  validate two dates
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_EST_GREAT_IMM, fileName);
										log.info("Given->" + actualHeader
												+ " Date lesser than Date de numéro de registre du commerce-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
								}

								// Premier Employee Date
								if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
										URLEncoder.encode(CmEmployerRegConstant.PREMIER_EMP_DATE, CmEmployerRegConstant.UTF))) {
									premierEmployeeDate = cell.getDateCellValue().toString().trim();
									checkValidationFlag = customHelper.compareDateWithSysDate(premierEmployeeDate, "lessEqual"); // validating with current date
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
										log.info("Given->" + actualHeader + " Date greater than System Date- "
												+ cell.getDateCellValue() + ": " + nineaNumber);
										break;
									}
									checkValidationFlag = customHelper.checkDateSunOrSat(premierEmployeeDate); // validating data is on sat or sun
									if (checkValidationFlag != null && checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_SAT_SUN_VALID, fileName);
										log.info("Given->" + actualHeader
												+ " Date should not be on Saturday or Sunday-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
									checkValidationFlag = customHelper.compareTwoDates(premierEmployeeDate, establishmentDate, "greatEqual"); // validate two dates
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_EMP_GREAT_EST, fileName);
										log.info("Given->" + actualHeader
												+ " Date lesser than Date de l'inspection du travail-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
									checkValidationFlag = customHelper.compareTwoDates(premierEmployeeDate, immatriculationDate, "greatEqual"); // validate two dates
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_EMP_GREAT_IMM, fileName);
										log.info("Given->" + actualHeader
												+ " Date lesser than Date de numéro de registre du commerce-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
								}
								
								if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
										URLEncoder.encode(CmEmployerRegConstant.DATE_DE_DELIVRANCE, CmEmployerRegConstant.UTF))) {
									deliveryDate = cell.getDateCellValue().toString();
									checkValidationFlag = customHelper.compareDateWithSysDate(immatriculationDate, "lessEqual"); // validating with current date.
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
										log.info("Given->" + actualHeader + " Date greater than System Date-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
								}
								if (!isBlankOrNull(headerName) &&( headerName.equalsIgnoreCase(
										URLEncoder.encode(CmEmployerRegConstant.DATE_IDENTIFICATION_FISCALE, CmEmployerRegConstant.UTF)) || headerName.equalsIgnoreCase(
												URLEncoder.encode(CmEmployerRegConstant.DATE_DE_NAISSANCE, CmEmployerRegConstant.UTF)) || headerName.equalsIgnoreCase(
														URLEncoder.encode(CmEmployerRegConstant.DATE_DE_CREATION, CmEmployerRegConstant.UTF)))) {
									checkValidationFlag = customHelper.compareDateWithSysDate(cell.getDateCellValue().toString(), "lessEqual"); // validating with current date.
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
										log.info("Given->" + actualHeader + " Date greater than System Date-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
								}
								if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
										URLEncoder.encode(CmEmployerRegConstant.DATE_DE_EXPIRATION, CmEmployerRegConstant.UTF))) {
									checkValidationFlag = customHelper.compareTwoDates(cell.getDateCellValue().toString(), deliveryDate, "great"); // validate two dates
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.DATE_DEL_GREAT_EXP, fileName);
										log.info("Given->" + actualHeader
												+ " Date lesser than Date de délivrance-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									}
								}
								
								String convertedDate = customHelper.convertDateFormat(cell.getDateCellValue().toString().trim());

								if (isBlankOrNull(convertedDate) || convertedDate.equalsIgnoreCase(CmEmployerRegConstant.INVALID_DATE_STRING)) {
									checkErrorInExcel = true;
									createToDo(cell.getDateCellValue().toString(), nineaNumber, CmEmployerRegConstant.INVALID_DATE, fileName);
									log.info("Given Ninea Number having invalid Date Format-"
											+ cell.getDateCellValue() + ":" + nineaNumber);
									break;
								} else {
									listesValues.add(convertedDate);
								}
								System.out.println(convertedDate);
							} else {
								if (headerName != null && headerName.equalsIgnoreCase(CmEmployerRegConstant.NINEA)
										&& cell.getColumnIndex() == 5) {// Ninea Validation
									Double nineaNum = cell.getNumericCellValue();
									DecimalFormat df = new DecimalFormat("#");
									nineaNumber = df.format(nineaNum);
									if (nineaNum.toString().length() == 7) {// Adding zero based on functional testing feedback from khawla - 09April
										nineaNumber = CmEmployerRegConstant.NINEA_PREFIX + nineaNumber;
									}
									if (customHelper.validateNineaNumber(nineaNumber)) {
										checkValidationFlag = customHelper.validateNineaExist(nineaNumber);
										if (checkValidationFlag != null && checkValidationFlag) {
											checkErrorInExcel = true;
											createToDo("", nineaNumber, CmEmployerRegConstant.NINEA_EXIST, fileName);
											log.info("Given Ninea Number already Exists: " + nineaNumber);
											break;
										}
									} else {
										checkErrorInExcel = true;
										createToDo("", nineaNumber, CmEmployerRegConstant.NINEA_INVALID, fileName);
										log.info("Given Ninea Number is Invalid: " + cell.getNumericCellValue());
										break;
									}
								} else if (headerName != null && headerName.equalsIgnoreCase(CmEmployerRegConstant.NINET)) {
									checkValidationFlag = customHelper.validateNinetNumber(cell.getNumericCellValue());
									if (checkValidationFlag != null && !checkValidationFlag) {// NINET validation
										// Error Skip the row
										checkErrorInExcel = true;
										createToDo(cell.getStringCellValue(), nineaNumber, CmEmployerRegConstant.NINET_INVALID, fileName);
										log.info("Given Ninea Number having Invalid NINET:" + nineaNumber);
										break;
									}
								} else if (headerName != null && (headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.TELEPHONE, CmEmployerRegConstant.UTF))
										|| headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.PHONE, CmEmployerRegConstant.UTF)) || headerName
												.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.MOBILE_NUM, CmEmployerRegConstant.UTF)))) { // PhoneNum Validation
									checkValidationFlag = customHelper
											.validatePhoneNumber(String.valueOf(cell.getNumericCellValue()));
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.TELEPHONE_INVALID, fileName);
										log.info("Given Ninea Number having invalid PhoneNumber- " + actualHeader
												+ ":" + cell.getNumericCellValue() + ":" + nineaNumber);
										break;
									}
								} else if (headerName != null && (headerName.equalsIgnoreCase(CmEmployerRegConstant.LAST_NAME)
										|| headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.FIRST_NAME, CmEmployerRegConstant.UTF)))) {
									// Error Skip the row
									checkErrorInExcel = true;
									createToDo(cell.getStringCellValue(), nineaNumber, CmEmployerRegConstant.NAME_LETTER_CHECK, fileName);
									log.info("Given " + headerName
											+ " should be alphabets for the given ninea number:" + nineaNumber);
									break;
								} else if (headerName != null && (headerName
										.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.LEGAL_REP_NIN, CmEmployerRegConstant.UTF))
										|| headerName.equalsIgnoreCase(
												URLEncoder.encode(CmEmployerRegConstant.EMPLOYEE_NIN, CmEmployerRegConstant.UTF)))) {
									Double ninNum = cell.getNumericCellValue();
									DecimalFormat df = new DecimalFormat("#");
									String ninNumber = df.format(ninNum);
									checkValidationFlag = customHelper.validateNinNumber(ninNumber);
									if (checkValidationFlag != null && !checkValidationFlag) {
										checkErrorInExcel = true;
										createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.NIN_INVALID, fileName);
										log.info("Given Ninea Number having invalid Nin Number- " + actualHeader + ":"
												+ cell.getNumericCellValue() + ":" + nineaNumber);
										break;
									}
								} else if(headerName != null) {
									String cellValue = "";
									double d = cell.getNumericCellValue();
									cellValue = new Long(new Double(d).longValue()).toString();
									if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.ACTIVATE_PRINCIPAL, CmEmployerRegConstant.UTF))) {
										if(isBlankOrNull(actPrinceMap.get(cellValue))) {
											checkErrorInExcel = true;
											createToDo(actualHeader, nineaNumber, "330", fileName);
										} else {
											listesValues.add(actPrinceMap.get(cellValue));
										}
										break;
									} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.TAUX_AT, CmEmployerRegConstant.UTF))) {
										if(isBlankOrNull(atRateMap.get(cellValue))) {
											checkErrorInExcel = true;
											createToDo(actualHeader, nineaNumber, "330", fileName);
										} else {
											listesValues.add(atRateMap.get(cellValue));
										}
										break;
									}  else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.SECTEUR_ACTIVITIES, CmEmployerRegConstant.UTF))) {
										if(isBlankOrNull(sectorActMap.get(cellValue))) {
											checkErrorInExcel = true;
											createToDo(actualHeader, nineaNumber, "330", fileName);
										} else {
											listesValues.add(sectorActMap.get(cellValue));
										}
										break;
									}  else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_CSS, CmEmployerRegConstant.UTF))) {
										if(isBlankOrNull(sectorMap.get(cellValue))) {
											checkErrorInExcel = true;
											createToDo(actualHeader, nineaNumber, "330", fileName);
										} else {
											listesValues.add(sectorMap.get(cellValue));
										}
										break;
									} else if(headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_IPRES, CmEmployerRegConstant.UTF))) {
										if(isBlankOrNull(sectorMap.get(cellValue))) {
											checkErrorInExcel = true;
											createToDo(actualHeader, nineaNumber, "330", fileName);
										} else {
											listesValues.add(sectorMap.get(cellValue));
										}
										break;
									}
								}
								listesValues.add((long) cell.getNumericCellValue());break;
							}
							
							System.out.println((long) cell.getNumericCellValue());
							break;
						case Cell.CELL_TYPE_BLANK:
							if (headerConstants.contains(headerName)){
								checkErrorInExcel = true;
								createToDo(actualHeader, nineaNumber, CmEmployerRegConstant.EMPTY, fileName);
								log.info(actualHeader+ " is Empty:"+ nineaNumber);
								break;
							} 
							System.out.println("Blank:");
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							listesValues.add(cell.getBooleanCellValue());
							System.out.println(cell.getBooleanCellValue());
							break;
						default:
							listesValues.add("");
							System.out.println("Blank:");
							break;
						}
						cellId++;
					}
					
				} catch (UnsupportedEncodingException ex) {
					log.info("*****Unsupported Encoding**** " + ex);
				}
				foundNinea = true;
				if (checkErrorInExcel) {
					checkErrorInExcel = false;
					break;
				}
				
				try {
					processed = formCreator(listesValues);
					System.out.println("*****Bo Creation Status**** " + processed);
					log.info("*****Bo Creation Status**** " + processed);
				} catch (Exception exception) {
					processed = false;
					processStgFlag = false;
					System.out.println("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
							+ this.nineaNumber);
					log.info("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
							+ this.nineaNumber);
					exception.printStackTrace();
				}
			}
			foundNinea = false;
		}
		if (processed && processStgFlag) {
			customHelper.moveFileToProcessedFolder(this.getFilePath()+fileName, this.getSuccessFilePath());
		} else {
			customHelper.moveFileToFailuireFolder(this.getFilePath()+fileName, this.getErrorFilePath());
		}
		
		System.out.println("######################## Terminer executeWorkUnit ############################");
	}

	private boolean verifyExistFileInFolder(String fileName) {
		Path path = Paths.get(fileName);
		boolean isExits = false;
		if (Files.exists(path)) {
			isExits = true;
		}
		return isExits;
	}

	/**
	 * Method to read CSV and validate and finally create the from and post it.
	 * 
	 * @param regFileName
	 */
	private String readCSVAndPostForm(String regFileName) {

		File file = null;
		file = new File(regFileName);
		List<String> headerValues = new ArrayList<String>();
		List<String> listValues = new ArrayList<String>();
		List<Object> formCreatorList = new ArrayList<Object>();
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
			String csvValues = scanner.nextLine();
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
		System.out.println("***Read value from File****headerValues**** " + headerValues);
		log.info("***Read value from File****headerValues**** " + headerValues);
		System.out.println("***Read value from File****listValues**** " + listValues);
		log.info("***Read value from File****listValues**** " + listValues);

		Iterator<String> headerIterator = headerValues.iterator();
		Iterator<String> listIterator = listValues.iterator();
		Boolean checkValidationFlag = false;
		String headerName = null;
		boolean checkErrorInCSV = false, processed = false;
		String immatriculationDate = null;
		String establishmentDate = null;
		String premierEmployeeDate = null;
		String deliveryDate = null;
		log.info("Before While:: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
				"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
		System.out.println("Before While:: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
				"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
		Set<String> headerConstants = getHeaderConstants();
		while (headerIterator.hasNext() && listIterator.hasNext()) {
			String header = headerIterator.next();
			actualHeader = header;
			String value = listIterator.next();
			log.info("CSV Value: " + value);
			try {
				headerName = URLEncoder.encode(header.trim(), CmEmployerRegConstant.UTF);
				if (headerName != null && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.EMAIL_EMPLOYER, CmEmployerRegConstant.UTF))) {
					checkValidationFlag = customHelper.validateEmail(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						log.info("Given Ninea Number: " + nineaNumber + " having Incorrect Email Id: " + value);
						checkErrorInCSV = true;
						createToDo(value, nineaNumber, CmEmployerRegConstant.EMPLOYER_EMAIL_INVALID, fileName);
						break;
					} else {
						checkValidationFlag = customHelper.validateEmailExist(value);
						if (checkValidationFlag != null && checkValidationFlag) {
							checkErrorInCSV = true;
							createToDo(value, nineaNumber, CmEmployerRegConstant.EMPLOYER_EMAIL_EXIST, fileName);
							log.info("Given Email ID:--> " + value + " already Exists");
							break;

						}
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}
				} else if (headerName != null && headerName
						.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.EMAIL, CmEmployerRegConstant.UTF))) {
					checkValidationFlag = customHelper.validateEmail(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(value, nineaNumber, CmEmployerRegConstant.EMAIL_INVALID, fileName);
						log.info("Given Ninea Number: " + nineaNumber + " having Incorrect Email Id: " + value);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);

					}
				} else if (headerName != null && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.TRADE_REG_NUM, CmEmployerRegConstant.UTF))) {
					if (customHelper.validateCommercialRegister(value)) {
						checkValidationFlag = customHelper.validateTRNExist(value);
						if (checkValidationFlag != null && checkValidationFlag) {
							checkErrorInCSV = true;
							createToDo(value, nineaNumber, CmEmployerRegConstant.TRN_EXIST, fileName);
							log.info("Given Trade Registration Number--> " + value + " already Exists");
							break;

						}
					} else {
						checkErrorInCSV = true;
						createToDo(value, nineaNumber, CmEmployerRegConstant.TRN_INVALID, fileName);
						log.info("Given Trade Registration Number:--> " + value + " is Invalid ");
						break;

					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}
				} else if (headerName != null && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.TAX_IDENTIFY_NUM, CmEmployerRegConstant.UTF))) {
					checkValidationFlag = customHelper.validateTaxIdenficationNumber(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(value, nineaNumber, CmEmployerRegConstant.TIN_INVALID, fileName);
						log.info("Given Ninea Number having Invalid TIN Number: " + nineaNumber);
						break;

					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}

				} else if (headerName != null
						&& (headerName
								.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.LAST_NAME,
										CmEmployerRegConstant.UTF))
						|| headerName.equalsIgnoreCase(
								URLEncoder.encode(CmEmployerRegConstant.FIRST_NAME, CmEmployerRegConstant.UTF)))) {
					checkValidationFlag = customHelper.validateAlphabetsOnly(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.NAME_INVALID, fileName);
						log.info("Given " + header
								+ "is having special characters or number for the given ninea number:" + nineaNumber);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}

				} else if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.IMMATRICULATION_DATE, CmEmployerRegConstant.UTF))) {
				
					log.info("Inside IMMATRICULATION_DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside IMMATRICULATION_DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					
					immatriculationDate = value;
					log.info("Inside IMMATRICULATION_DATE :: after immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside IMMATRICULATION_DATE :: after immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					checkValidationFlag = customHelper.compareDateWithSysDate(value, "lessEqual"); 
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
						log.info("Given->" + header + " Date greater than System Date- " + value + ":" + nineaNumber);
						break;
					}
					log.info("Inside IMMATRICULATION_DATE after value :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside IMMATRICULATION_DATE after value  :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					if (!checkErrorInCSV) {
						formCreatorList.add(customHelper.convertDateFormat(value));
					}
				} else if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.ESTABLISHMENT_DATE, CmEmployerRegConstant.UTF))) {
					
					log.info("Inside ESTABLISHMENT_DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside ESTABLISHMENT_DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					establishmentDate = value;
					log.info("Inside ESTABLISHMENT_DATE after:: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside ESTABLISHMENT_DATE after:: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					
					checkValidationFlag = customHelper.compareDateWithSysDate(value, "lessEqual"); 
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
						log.info("Given->" + header + " Date greater than System Date- " + value + ":" + nineaNumber);
						break;
					}
					
					checkValidationFlag = customHelper.checkDateSunOrSat(value); 
					if (checkValidationFlag != null && checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_SAT_SUN_VALID, fileName);
						log.info("Given->" + header + " Date should not be on Saturday or Sunday- " + value + ":"
								+ nineaNumber);
						break;
					}
					checkValidationFlag = customHelper.compareTwoDates(establishmentDate, immatriculationDate, "greatEqual"); // validate two dates
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_EST_GREAT_IMM, fileName);
						log.info("Given->" + header + " Date lesser than Date de numéro de registre du commerce- "
								+ value + ":" + nineaNumber);
						break;
					}
					log.info("Inside ESTABLISHMENT_DATE final value :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside ESTABLISHMENT_DATE after value :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					if (!checkErrorInCSV) {
						formCreatorList.add(customHelper.convertDateFormat(value));
					}
				} else if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.PREMIER_EMP_DATE, CmEmployerRegConstant.UTF))) {
					log.info("Inside PREMIER_EMP_DATE  :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside PREMIER_EMP_DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					premierEmployeeDate = value;
					checkValidationFlag = customHelper.compareDateWithSysDate(premierEmployeeDate, "lessEqual"); 
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
						log.info("Given->" + header + " Date greater than System Date- " + value + ": " + nineaNumber);
						break;
					}
					checkValidationFlag = customHelper.checkDateSunOrSat(premierEmployeeDate); 
					if (checkValidationFlag != null && checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_SAT_SUN_VALID, fileName);
						log.info("Given->" + header + " Date should not be on Saturday or Sunday- " + value + ":"
								+ nineaNumber);
						break;
					}
					checkValidationFlag = customHelper.compareTwoDates(premierEmployeeDate, establishmentDate,
							"greatEqual"); // validate two dates
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_EMP_GREAT_EST, fileName);
						log.info("Given->" + header + " Date lesser than Date de l'inspection du travail- " + value
								+ ":" + nineaNumber);
						break;
					}
					checkValidationFlag = customHelper.compareTwoDates(premierEmployeeDate, immatriculationDate,
							"greatEqual"); // validate two dates
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_EMP_GREAT_IMM, fileName);
						log.info("Given->" + header + " Date lesser than Date de numéro de registre du commerce- "
								+ value + ":" + nineaNumber);
						break;
					}
					log.info("Inside PREMIER_EMP_DATE after value :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside PREMIER_EMP_DATE after value:: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					if (!checkErrorInCSV) {
						formCreatorList.add(customHelper.convertDateFormat(value));
					}
				} else if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.DATE_DE_DELIVRANCE, CmEmployerRegConstant.UTF))) {
					log.info("Inside DATE_DE_DELIVRANCE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside DATE_DE_DELIVRANCE :: immatriculationDate " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					deliveryDate = value;
					checkValidationFlag = customHelper.compareDateWithSysDate(immatriculationDate, "lessEqual"); 
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
						log.info("Given->" + header + " Date greater than System Date- " + value + ":" + nineaNumber);
						break;
					}
					log.info("Inside DATE_DE_DELIVRANCE after value :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside DATE_DE_DELIVRANCE after value  :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					if (!checkErrorInCSV) {
						formCreatorList.add(customHelper.convertDateFormat(value));
					}
				} else if (!isBlankOrNull(headerName) && (headerName
						.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.DATE_IDENTIFICATION_FISCALE,
								CmEmployerRegConstant.UTF))
						|| headerName.equalsIgnoreCase(
								URLEncoder.encode(CmEmployerRegConstant.DATE_DE_NAISSANCE, CmEmployerRegConstant.UTF))
						|| headerName.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.DATE_DE_CREATION,
								CmEmployerRegConstant.UTF)))) {
					checkValidationFlag = customHelper.compareDateWithSysDate(value, "lessEqual"); 
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_LESSEQUAL_TODAY_VALID, fileName);
						log.info("Given->" + header + " Date greater than System Date- " + value + ":" + nineaNumber);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(customHelper.convertDateFormat(value));
					}
				} else if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.DATE_DE_EXPIRATION, CmEmployerRegConstant.UTF))) {
					checkValidationFlag = customHelper.compareTwoDates(value, deliveryDate, "great"); // validate
					log.info("Inside DATE_DE_EXPIRATION :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
					System.out.println("Inside DATE_DE_EXPIRATION:: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
							"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate + "value::"+ deliveryDate);
					
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.DATE_DEL_GREAT_EXP, fileName);
						log.info("Given->" + header + " Date lesser than Date de délivrance- " + value + ":"
								+ nineaNumber);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(customHelper.convertDateFormat(value));
					}
				} 
				else if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.TAX_ID_DATE, CmEmployerRegConstant.UTF))) {
					formCreatorList.add(customHelper.convertDateFormat(value));
				}
				else if (headerName != null && headerName.equalsIgnoreCase(CmEmployerRegConstant.NINEA)) {// Ninea
																											// Validation
					if (value.length() == 7) {// Adding zero based on functional
												// testing feedback from khawla
												// - 09April
						value = CmEmployerRegConstant.NINEA_PREFIX + value;
					}
					if (customHelper.validateNineaNumber(value)) {
						checkValidationFlag = customHelper.validateNineaExist(value);
						if (checkValidationFlag != null && checkValidationFlag) {
							checkErrorInCSV = true;
							createToDo("", nineaNumber, CmEmployerRegConstant.NINEA_EXIST, fileName);
							log.info("Given Ninea Number already Exists: " + nineaNumber);
							break;
						}
					} else {
						checkErrorInCSV = true;
						createToDo("", nineaNumber, CmEmployerRegConstant.NINEA_INVALID, fileName);
						log.info("Given Ninea Number is Invalid: " + value);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}
				} else if (headerName != null && headerName.equalsIgnoreCase(CmEmployerRegConstant.NINET)) {
					if (!isBlankOrNull(value)) {
						checkValidationFlag = customHelper.validateNinetNumber(value);
					}
					if (checkValidationFlag != null && !checkValidationFlag) {// NINET
																				// validation
						// Error Skip the row
						checkErrorInCSV = true;
						createToDo(value, nineaNumber, CmEmployerRegConstant.NINET_INVALID, fileName);
						log.info("Given Ninea Number having Invalid NINET:" + nineaNumber);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}
				} else
					if (headerName != null
							&& (headerName
									.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.TELEPHONE,
											CmEmployerRegConstant.UTF))
							|| headerName.equalsIgnoreCase(
									URLEncoder.encode(CmEmployerRegConstant.PHONE, CmEmployerRegConstant.UTF))
							|| headerName.equalsIgnoreCase(
									URLEncoder.encode(CmEmployerRegConstant.MOBILE_NUM, CmEmployerRegConstant.UTF)))) { // PhoneNum
																														// Validation
					checkValidationFlag = customHelper.validatePhoneNumber(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.TELEPHONE_INVALID, fileName);
						log.info("Given Ninea Number having invalid PhoneNumber- " + header + ":" + value + ":"
								+ nineaNumber);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}
				} else if (headerName != null
						&& (headerName
								.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.LEGAL_REP_NIN,
										CmEmployerRegConstant.UTF))
						|| headerName.equalsIgnoreCase(
								URLEncoder.encode(CmEmployerRegConstant.EMPLOYEE_NIN, CmEmployerRegConstant.UTF)))) {
					checkValidationFlag = customHelper.validateNinNumber(value);
					if (checkValidationFlag != null && !checkValidationFlag) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, CmEmployerRegConstant.NIN_INVALID, fileName);
						log.info("Given Ninea Number having invalid Nin Number- " + header + ":" + value + ":"
								+ nineaNumber);
						break;
					}
					if (!checkErrorInCSV) {
						formCreatorList.add(value);
					}
				} else if (headerName
						.equalsIgnoreCase(URLEncoder.encode(CmEmployerRegConstant.REGION, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(regionMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(regionMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.DÉPARTEMENT, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(deptMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(deptMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.ARONDISSEMENT, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(arrondMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(arrondMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.COMMUNE, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(communeMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(communeMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.QUARTIER, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(qartierMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(qartierMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.AGENCE_CSS, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(agenceMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(agenceMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.AGENCE_IPRES, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(agenceMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(agenceMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_CSS, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(zoneMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(zoneMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_IPRES, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(zoneMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(zoneMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_CSS, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(sectorMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(sectorMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_IPRES, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(sectorMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(sectorMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.SECTEUR_ACTIVITIES, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(sectorActMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(sectorActMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.ACTIVATE_PRINCIPAL, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(actPrinceMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(header, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(actPrinceMap.get(value));
					}
				} else if (headerName.equalsIgnoreCase(
						URLEncoder.encode(CmEmployerRegConstant.TAUX_AT, CmEmployerRegConstant.UTF))) {
					if (isBlankOrNull(atRateMap.get(value))) {
						checkErrorInCSV = true;
						createToDo(value, nineaNumber, "330", fileName);
						break;
					} else {
						formCreatorList.add(atRateMap.get(value));
					}
				} else if (headerConstants.contains(headerName) && isBlankOrNull(value)) {
					checkErrorInCSV = true;
					createToDo(header, nineaNumber, CmEmployerRegConstant.EMPTY, fileName);
					log.info(header + " is Empty:" + nineaNumber);
					break;
				} else {
					formCreatorList.add(value);
				}
			} catch (Exception exception) {
				log.info("Error in CSV File: " + headerName);
				log.info("Error in CSV File: " + headerName);
				exception.printStackTrace();
			}
		}
		System.out.println("Final List formCreatorList::- " + formCreatorList);
		log.info("Final List formCreatorList::- " + formCreatorList);
		
		log.info("Final DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
				"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate);
		System.out.println("Final DATE :: immatriculationDate:: " + immatriculationDate +"establishmentDate:: "+establishmentDate+
				"premierEmployeeDate:: "+premierEmployeeDate+ "deliveryDate::"+ deliveryDate + "value::"+ deliveryDate);
		
		if(!checkErrorInCSV) {
			try {
				processed = formCreator(formCreatorList);
				System.out.println("*****Bo Creation Status**** " + processed);
				log.info("*****Bo Creation Status**** " + processed);
			} catch (Exception exception) {
				processed = false;
				System.out.println("*****Issue in Processing file***** " + fileName + "NineaNumber:: " + this.nineaNumber);
				log.info("*****Issue in Processing file***** " + fileName + "NineaNumber:: " + this.nineaNumber);
				exception.printStackTrace();
			}
		} else {
			System.out.println("*****Error in CSV File***** " + fileName +"::"+actualHeader+"::"+ formCreatorList);
			log.info("*****Error in CSV File***** " + fileName +"::"+actualHeader+"::"+ formCreatorList);
			return actualHeader;
		}
		
		if (processed) {
			//customHelper.moveFileToProcessedFolder(this.getFilePath()+fileName, this.getSuccessFilePath());
			return "Success";
		} else {
			//customHelper.moveFileToFailuireFolder(this.getFilePath()+fileName, this.getErrorFilePath());
			return "Failure";
		}
		
	}

	/**
	 * Method Form Creator
	 * 
	 * @param fileName2
	 * @param listesValues
	 * @return
	 */
	private boolean formCreator(List<Object> listesValues) {
		log.info("I am Inside Bo Creator:: " + listesValues.size());
		System.out.println("I am Inside Bo Creator:: " + listesValues.size());
		BusinessObjectInstance boInstance = null;

		boInstance = createFormBOInstance(this.getFormType(), "T-REG-" + getSystemDateTime().toString());

		COTSInstanceNode employerQuery = boInstance.getGroup("employerQuery");
		COTSInstanceNode mainRegistrationForm = boInstance.getGroup("mainRegistrationForm");
		COTSInstanceNode legalRepresentativeForm = boInstance.getGroup("legalRepresentativeForm");
		COTSInstanceNode documentForm = boInstance.getGroup("documents");

		int count = 0;
		while (count == 0) {

			COTSFieldDataAndMD<?> regType = employerQuery.getFieldAndMDForPath("regType/asCurrent");
			regType.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> employerType = employerQuery.getFieldAndMDForPath("employerType/asCurrent");
			employerType.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> estType = employerQuery.getFieldAndMDForPath("estType/asCurrent");
			estType.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> employerName = employerQuery.getFieldAndMDForPath("employerName/asCurrent");
			employerName.setXMLValue(listesValues.get(count).toString());
			count++;// Moved from second section

			COTSFieldDataAndMD<?> hqId = employerQuery.getFieldAndMDForPath("hqId/asCurrent");
			hqId.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> nineaNumber = employerQuery.getFieldAndMDForPath("nineaNumber/asCurrent");
			nineaNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> ninetNumber = employerQuery.getFieldAndMDForPath("ninetNumber/asCurrent");
			ninetNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> companyOriginId = employerQuery.getFieldAndMDForPath("companyOriginId/asCurrent");
			companyOriginId.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> legalStatus = employerQuery.getFieldAndMDForPath("legalStatus/asCurrent");
			legalStatus.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> taxId = employerQuery.getFieldAndMDForPath("taxId/asCurrent");
			taxId.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> taxIdDate = employerQuery.getFieldAndMDForPath("taxIdDate/asCurrent");
			taxIdDate.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> tradeRegisterNumber = employerQuery
					.getFieldAndMDForPath("tradeRegisterNumber/asCurrent");
			tradeRegisterNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> tradeRegisterDate = employerQuery.getFieldAndMDForPath("tradeRegisterDate/asCurrent");
			tradeRegisterDate.setXMLValue(listesValues.get(count).toString());
			count++;
			// --------------------------*************------------------------------------------------------------------------------//

			// ******Main Registration Form BO
			// Creation*********************************//
			COTSFieldDataAndMD<?> dateOfInspection = mainRegistrationForm
					.getFieldAndMDForPath("dateOfInspection/asCurrent");
			dateOfInspection.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> dateOfFirstHire = mainRegistrationForm
					.getFieldAndMDForPath("dateOfFirstHire/asCurrent");
			dateOfFirstHire.setXMLValue(listesValues.get(count).toString());
			count++;

			/*
			 * COTSFieldDataAndMD<?> shortName =
			 * mainRegistrationForm.getFieldAndMDForPath("shortName/asCurrent");
			 * shortName.setXMLValue(listesValues.get(count).toString());
			 * count++;
			 */

			COTSFieldDataAndMD<?> businessSector = mainRegistrationForm
					.getFieldAndMDForPath("businessSector/asCurrent");
			businessSector.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> mainLineOfBusiness = mainRegistrationForm
					.getFieldAndMDForPath("mainLineOfBusiness/asCurrent");
			mainLineOfBusiness.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> atRate = mainRegistrationForm.getFieldAndMDForPath("atRate/asCurrent");
			atRate.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> noOfWorkersInGenScheme = mainRegistrationForm
					.getFieldAndMDForPath("noOfWorkersInGenScheme/asCurrent");
			noOfWorkersInGenScheme.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> noOfWorkersInBasicScheme = mainRegistrationForm
					.getFieldAndMDForPath("noOfWorkersInBasicScheme/asCurrent");
			noOfWorkersInBasicScheme.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> region = mainRegistrationForm.getFieldAndMDForPath("region/asCurrent");
			region.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> department = mainRegistrationForm.getFieldAndMDForPath("department/asCurrent");
			department.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> arondissement = mainRegistrationForm.getFieldAndMDForPath("arondissement/asCurrent");
			arondissement.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> commune = mainRegistrationForm.getFieldAndMDForPath("commune/asCurrent");
			commune.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> qartier = mainRegistrationForm.getFieldAndMDForPath("qartier/asCurrent");
			qartier.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> address = mainRegistrationForm.getFieldAndMDForPath("address/asCurrent");
			address.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> postboxNumber = mainRegistrationForm.getFieldAndMDForPath("postboxNo/asCurrent");
			postboxNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> telephone = mainRegistrationForm.getFieldAndMDForPath("telephone/asCurrent");
			telephone.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> email = mainRegistrationForm.getFieldAndMDForPath("email/asCurrent");
			email.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> website = mainRegistrationForm.getFieldAndMDForPath("website/asCurrent");
			website.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> zoneCss = mainRegistrationForm.getFieldAndMDForPath("zoneCss/asCurrent");
			zoneCss.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> zoneIpres = mainRegistrationForm.getFieldAndMDForPath("zoneIpres/asCurrent");
			zoneIpres.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> sectorCss = mainRegistrationForm.getFieldAndMDForPath("sectorCss/asCurrent");
			sectorCss.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> sectorIpres = mainRegistrationForm.getFieldAndMDForPath("sectorIpres/asCurrent");
			sectorIpres.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> agencyCss = mainRegistrationForm.getFieldAndMDForPath("agencyCss/asCurrent");
			agencyCss.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> agencyIpres = mainRegistrationForm.getFieldAndMDForPath("agencyIpres/asCurrent");
			agencyIpres.setXMLValue(listesValues.get(count).toString());
			count++;

			// ------------------------------LegalRepresentativeForm BO
			// Creation----------------------------------------------------//

			COTSFieldDataAndMD<?> legalRepPerson = legalRepresentativeForm
					.getFieldAndMDForPath("legalRepPerson/asCurrent");
			legalRepPerson.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> lastName = legalRepresentativeForm.getFieldAndMDForPath("lastName/asCurrent");
			lastName.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> firstName = legalRepresentativeForm.getFieldAndMDForPath("firstName/asCurrent");
			firstName.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> birthDate = legalRepresentativeForm.getFieldAndMDForPath("birthDate/asCurrent");
			birthDate.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> nationality = legalRepresentativeForm.getFieldAndMDForPath("nationality/asCurrent");
			nationality.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> nin = legalRepresentativeForm.getFieldAndMDForPath("nin/asCurrent");
			nin.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> placeOfBirth = legalRepresentativeForm.getFieldAndMDForPath("placeOfBirth/asCurrent");
			placeOfBirth.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> cityOfBirth = legalRepresentativeForm.getFieldAndMDForPath("cityOfBirth/asCurrent");
			cityOfBirth.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> typeOfIdentity = legalRepresentativeForm
					.getFieldAndMDForPath("typeOfIdentity/asCurrent");
			typeOfIdentity.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> identityIdNumber = legalRepresentativeForm
					.getFieldAndMDForPath("identityIdNumber/asCurrent");
			identityIdNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> ninCedeo = legalRepresentativeForm.getFieldAndMDForPath("ninCedeo/asCurrent");
			ninCedeo.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> dateOfIssue = legalRepresentativeForm.getFieldAndMDForPath("issuedDate/asCurrent");
			dateOfIssue.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> expirationDate = legalRepresentativeForm.getFieldAndMDForPath("expiryDate/asCurrent");
			expirationDate.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> legalRegion = legalRepresentativeForm.getFieldAndMDForPath("region/asCurrent");
			legalRegion.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> legalDepartment = legalRepresentativeForm
					.getFieldAndMDForPath("department/asCurrent");
			legalDepartment.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> arondissementLegal = legalRepresentativeForm
					.getFieldAndMDForPath("arondissement/asCurrent");
			arondissementLegal.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> communeLegal = legalRepresentativeForm.getFieldAndMDForPath("commune/asCurrent");
			communeLegal.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> legalqartier = legalRepresentativeForm.getFieldAndMDForPath("qartier/asCurrent");
			legalqartier.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> legaladdress = legalRepresentativeForm.getFieldAndMDForPath("address/asCurrent");
			legaladdress.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> landLineNumber = legalRepresentativeForm
					.getFieldAndMDForPath("landLineNumber/asCurrent");
			landLineNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> mobileNumber = legalRepresentativeForm.getFieldAndMDForPath("mobileNumber/asCurrent");
			mobileNumber.setXMLValue(listesValues.get(count).toString());
			count++;

			COTSFieldDataAndMD<?> legalRepresentativeEmail = legalRepresentativeForm
					.getFieldAndMDForPath("email/asCurrent");
			legalRepresentativeEmail.setXMLValue(listesValues.get(count).toString());
			count++;

			// --------------------------*************------------------------------------------------------------------------------//
			
			int increment = 1;
			
			Map<String,String> docMap = getDcoumentList(this.nineaNumber);
			for(Map.Entry<String, String> docMapList : docMap.entrySet()){

				String docName = "documentName";
				String docUrl = "documentUrl";
				docName = docName+increment;
				docUrl = docUrl+increment;
				COTSFieldDataAndMD<?> documentName = documentForm.getFieldAndMDForPath(docName+"/asCurrent");
				documentName.setXMLValue(docMapList.getKey());
				COTSFieldDataAndMD<?> documentUrl = documentForm.getFieldAndMDForPath(docUrl+"/asCurrent");
				documentUrl.setXMLValue(docMapList.getValue());
				increment++;
			}

		}

		if (boInstance != null) {
			boInstance = validateAndPostForm(boInstance);
		}
		return true;
	}
	
	/**
	 * This method used to get the list document related with Ninea from GED
	 * 
	 * @param nineaNumber
	 * @return
	 */
	private Map<String, String> getDcoumentList(String nineaNumber) {

		log.info("#### getDcoumentList for : " + nineaNumber);
		PreparedStatement psPreparedStatement = null;
		Map<String, String> docMap = new HashMap<String, String>();
		QueryIterator<SQLResultRow> resultIterator = null;
		try {
			psPreparedStatement = createPreparedStatement("SELECT * FROM CM_INT_GED where NINEANUMBER =" + nineaNumber,
					"select");
			psPreparedStatement.setAutoclose(false);
			resultIterator = psPreparedStatement.iterate();
			while (resultIterator.hasNext()) {
				SQLResultRow lookUpValue = resultIterator.next();
				docMap.put(lookUpValue.getString("DOCNAME"), lookUpValue.getString("DOCURL"));
				log.info("#DOCNAME" + lookUpValue.getString("DOCNAME") +"and URL:" + lookUpValue.getString("DOCURL"));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
			resultIterator.close();
		}
		return docMap;
	}

	/**
	 * Method to create Form BO Instance
	 * 
	 * @param formTypeString
	 * @param documentLocator
	 * @return
	 */
	private BusinessObjectInstance createFormBOInstance(String formTypeString, String documentLocator) {

		FormType formType = new FormType_Id(formTypeString).getEntity();
		String formTypeBo = formType.getRelatedTransactionBOId().getTrimmedValue();

		log.info("#### Creating BO for " + formType);
		System.out.println("#### Creating BO for " + formType);

		BusinessObjectInstance boInstance = BusinessObjectInstance.create(formTypeBo);

		log.info("#### Form Type BO MD Schema: " + boInstance.getSchemaMD());
		System.out.println("#### Form Type BO MD Schema: " + boInstance.getSchemaMD());

		boInstance.set("bo", formTypeBo);
		boInstance.set("formType", formType.getId().getTrimmedValue());
		boInstance.set("receiveDate", getSystemDateTime().getDate());
		boInstance.set("documentLocator", documentLocator);

		return boInstance;

	}

	/**
	 * Method to validate and post the form
	 * 
	 * @param boInstance
	 * @return
	 */
	private BusinessObjectInstance validateAndPostForm(BusinessObjectInstance boInstance) {

		log.info("#### BO Instance Schema before ADD: " + boInstance.getDocument().asXML());
		System.out.println("#### BO Instance Schema before ADD: " + boInstance.getDocument().asXML());
		boInstance = BusinessObjectDispatcher.add(boInstance);
		log.info("#### BO Instance Schema after ADD: " + boInstance.getDocument().asXML());

		boInstance.set("boStatus", "VALIDATE");
		boInstance = BusinessObjectDispatcher.update(boInstance);
		log.info("#### BO Instance Schema after VALIDATE: " + boInstance.getDocument().asXML());

		boInstance.set("boStatus", "READYFORPOST");
		boInstance = BusinessObjectDispatcher.update(boInstance);
		log.info("#### BO Instance Schema after READYFORPOST: " + boInstance.getDocument().asXML());

		boInstance.set("boStatus", "POSTED");
		boInstance = BusinessObjectDispatcher.update(boInstance);
		log.info("#### BO Instance Schema after POSTED: " + boInstance.getDocument().asXML());
		System.out.println("#### BO Instance Schema after POSTED: " + boInstance.getDocument().asXML());

		COTSFieldDataAndMD<?> cotsRegId = this.boInstance.getFieldAndMDForPath("informationFormulaire/formId");
		cotsRegId.setXMLValue(boInstance.getFieldAndMDForPath("registrationFormId").getXMLValue());

		String personId = executeBSAndRetrievePerson();

		if (!isNull(personId)) {
			COTSFieldDataAndMD<?> cotsPerId = this.boInstance.getFieldAndMDForPath("employerDetails/personId");
			cotsPerId.setXMLValue(personId);// personId

			COTSFieldDataAndMD<?> cotsPersonId = this.boInstance.getFieldAndMDForPath("personId");
			cotsPersonId.setXMLValue(personId);
		} else {
			log.info("Person Id is null for the Id nmuber" + nineaNumber);
		}

		BusinessObjectDispatcher.update(this.boInstance);

		return boInstance;
	}

	private String executeBSAndRetrievePerson() {

		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-PersonSearchByIdTypeNumber");
		bsInstance.set("idType", "SCI");
		bsInstance.set("idNumber", nineaNumber);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);

		// Getting the list of results
		COTSInstanceList list = bsInstance.getList("results");

		// If list IS NOT empty
		if (!list.isEmpty()) {

			// Get the first result
			COTSInstanceListNode firstRow = list.iterator().next();

			// Return the person entity
			System.out.println("personId" + firstRow.getString( "personId"));
			log.info("personId" + firstRow.getString( "personId"));
			return firstRow.getString("personId");

		}

		return null;
	}

	/**
	 * Method to get the LookUpValues
	 */
	private void processLookup() {
		Map<String, String> getLookUpValuesMap;
		// trimString = trimString.trim().replaceAll("\\s{2,}", " ");
		HashMap<String, String> lookUpConstantmap = cmConstants.getLookUpConstanst();
		for (Map.Entry<String, String> entry : lookUpConstantmap.entrySet()) {
			if (entry.getValue().equalsIgnoreCase("CMREGION_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("REGION", entry.getValue());
				regionMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMDEPARTEMENT_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("DEPARTEMENT", entry.getValue());
				deptMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMARRONDISSEMENT_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ARRONDISSEMENT", entry.getValue());
				arrondMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMCOMMUNE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("COMMUNE", entry.getValue());
				communeMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMQUARTIER_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("QUARTIER", entry.getValue());
				qartierMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMAGENCE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("AGENCE", entry.getValue());
				agenceMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMZONE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ZONE", entry.getValue());
				zoneMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMSECTEUR_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("SECTEUR", entry.getValue());
				sectorMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMSECTEURACTIVITES_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("SECTEURACTIVITES", entry.getValue());
				sectorActMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMACTIVITESPRINCIPAL_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ACTIVITESPRINCIPAL", entry.getValue());
				actPrinceMap.putAll(getLookUpValuesMap);
			} else if (entry.getValue().equalsIgnoreCase("CMATRATE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ATRATE", entry.getValue());
				atRateMap.putAll(getLookUpValuesMap);
			}
		}
	}

	/**
	 * Method to get the getter constants
	 * 
	 * @return
	 */
	private Set<String> getHeaderConstants() {
		Set<String> headerConstanstSet = null;
		try {
			headerConstanstSet = new HashSet<String>(
					Arrays.asList(URLEncoder.encode(CmEmployerRegConstant.TYPE_D_EMPLOYEUR, CmEmployerRegConstant.UTF),
							URLEncoder.encode(CmEmployerRegConstant.TYPE_D_EST, CmEmployerRegConstant.UTF),
							URLEncoder.encode(CmEmployerRegConstant.RAISON_SOCIALE, CmEmployerRegConstant.UTF),
							URLEncoder.encode(CmEmployerRegConstant.NINEA, CmEmployerRegConstant.UTF),
							URLEncoder.encode(CmEmployerRegConstant.NINET, CmEmployerRegConstant.UTF),
							URLEncoder.encode(CmEmployerRegConstant.FORME_JURIDIQUE, CmEmployerRegConstant.UTF),
							URLEncoder.encode(CmEmployerRegConstant.DATE_IDENTIFICATION_FISCALE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.NUMERO_REGISTER_DE_COMMERCE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DATE_IMM_REGISTER_DE_COMMERCE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DATE_OUVERTURE_EST, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DATE_EMBAUCHE_PREMIER_SALARY, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.SECTEUR_ACTIVITIES, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.ACTIVATE_PRINCIPAL, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.TAUX_AT, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.NOMBRE_TRAVAIL_REGIME_GENERAL, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.NOMBRE_TRAVAIL_REGIME_CADRE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.REGION, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DÉPARTEMENT, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.ARONDISSEMENT, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.COMMUNE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.QUARTIER, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.ADDRESS, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.TELEPHONE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.EMAIL, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_CSS, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_IPRES, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_CSS, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_IPRES, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.AGENCE_CSS, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.AGENCE_IPRES, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.LAST_NAME, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.FIRST_NAME, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DATE_DE_NAISSANCE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.NATIONALITE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.LEGAL_REP_NIN, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.EMPLOYEE_NIN, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.PAYS_DE_NAISSANCE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DATE_DE_DELIVRANCE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.DATE_DE_EXPIRATION, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.MOBILE_NUM, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.TYPE_PIECE_IDENTITE, CmEmployerRegConstant.UTF),
					URLEncoder.encode(CmEmployerRegConstant.NUMERO_PIECE_IDENTITE, CmEmployerRegConstant.UTF)));
		} catch (UnsupportedEncodingException e) {
			log.error("*****Issue in Processing file***** " + e);
		}
		return headerConstanstSet;

	}

	/**
	 * Method to create To Do
	 * 
	 * @param messageParam
	 * @param nineaNumber
	 * @param messageNumber
	 * @param fileName
	 */
	private void createToDo(String messageParam, String nineaNumber, String messageNumber, String fileName) {
		log.info("Creating to Do:: messageParam:: " + messageParam+"messageNumber:: "+ messageNumber + "processFlowId " + processFlowId);
		
		startChanges();
		
		BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
		Role_Id toDoRoleId = new Role_Id("CM-REGTODO");
		Role toDoRole = toDoRoleId.getEntity();
		businessServiceInstance.getFieldAndMDForPath("sendTo").setXMLValue("SNDR");
		businessServiceInstance.getFieldAndMDForPath("subject").setXMLValue("Batch Update from PSRM");
		businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue("CM-REGTO");
		businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
		businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue(processFlowId);
		businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue("90007");
		businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue(messageNumber);
		businessServiceInstance.getFieldAndMDForPath("messageParm1").setXMLValue(messageParam);
		businessServiceInstance.getFieldAndMDForPath("messageParm2").setXMLValue(nineaNumber);
		businessServiceInstance.getFieldAndMDForPath("messageParm3").setXMLValue(fileName);
		businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue(processFlowId);

		BusinessServiceDispatcher.execute(businessServiceInstance);
		saveChanges();
		// getSession().commit();
	}

	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		// TODO Auto-generated method stub
		this.boKey = arg0;
	}
}
