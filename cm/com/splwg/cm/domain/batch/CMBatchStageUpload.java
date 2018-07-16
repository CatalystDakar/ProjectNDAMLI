package com.splwg.cm.domain.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.cm.domain.common.businessComponent.CmXLSXReaderComponent;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formType.FormType;
import com.splwg.tax.domain.admin.formType.FormType_Id;

/**
 * @author Denash.M
 *
 * @BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = errorFilePathToMove, required = true, type = string)
 *           , @BatchJobSoftParameter (name = formType, required = true, type =string) , @BatchJobSoftParameter (name = filePaths, required =
 *           true, type = string) , @BatchJobSoftParameter (name = pathToMove,required = true, type = string)})
 */
public class CMBatchStageUpload extends CMBatchStageUpload_Gen {

	private final static Logger log = LoggerFactory.getLogger(CMBatchStageUpload.class);
	
	@Override
	public void validateSoftParameters(boolean isNewRun) {
		System.out.println("File path: " + this.getParameters().getFilePaths());
		System.out.println("Form Type: " + this.getParameters().getFormType());
	}
	
	private File[] getNewTextFiles() {
		File dir = new File(this.getParameters().getFilePaths());
		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xlsx");
			}
		});
	}
	
	private final static CmEmployerRegHelper customHelper = new CmEmployerRegHelper();
	private final static CmEmployerRegConstant cmConstants = new CmEmployerRegConstant();
	final static HashMap<String, String> regionMap = new HashMap<String, String>();
	final static HashMap<String, String> deptMap =  new HashMap<String, String>();
	final static HashMap<String, String> arrondMap =  new HashMap<String, String>();
	final static HashMap<String, String> communeMap =  new HashMap<String, String>();
	final static HashMap<String, String> qartierMap = new HashMap<String, String>();
	final static HashMap<String, String> agenceMap =  new HashMap<String, String>();
	final static HashMap<String, String> zoneMap =  new HashMap<String, String>();
	final static HashMap<String, String> sectorMap =  new HashMap<String, String>();
	final static HashMap<String, String> sectorActMap =  new HashMap<String, String>();
	final static HashMap<String, String> actPrinceMap =  new HashMap<String, String>();
	final static HashMap<String, String> atRateMap =  new HashMap<String, String>();
	
	private void processLookup() {
		Map<String, String> getLookUpValuesMap;
		
		HashMap<String, String> lookUpConstantmap = cmConstants.getLookUpConstanst();
		for (Map.Entry<String, String> entry : lookUpConstantmap.entrySet()) {
			if(entry.getValue().equalsIgnoreCase("CMREGION_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("REGION", entry.getValue());
				regionMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMDEPARTEMENT_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("DEPARTEMENT", entry.getValue());
				deptMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMARRONDISSEMENT_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ARRONDISSEMENT", entry.getValue());
				arrondMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMCOMMUNE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("COMMUNE", entry.getValue());
				communeMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMQUARTIER_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("QUARTIER", entry.getValue());
				qartierMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMAGENCE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("AGENCE", entry.getValue());
				agenceMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMZONE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ZONE", entry.getValue());
				zoneMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMSECTEUR_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("SECTEUR", entry.getValue());
				sectorMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMSECTEURACTIVITES_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("SECTEURACTIVITES", entry.getValue());
				sectorActMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMACTIVITESPRINCIPAL_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ACTIVITESPRINCIPAL", entry.getValue());
				actPrinceMap.putAll(getLookUpValuesMap);
			} else if(entry.getValue().equalsIgnoreCase("CMATRATE_L")) {
				getLookUpValuesMap = customHelper.getLookUpValues("ATRATE", entry.getValue());
				atRateMap.putAll(getLookUpValuesMap);
			} 
		}
	}
	
	public JobWork getJobWork() {
		log.info("***** Start JobWorker***");
		System.out.println("######################## Start JobWorker ############################");
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();

		File[] files = this.getNewTextFiles();
		this.processLookup();

		for (File file : files) {
			if (file.isFile()) {
				ThreadWorkUnit unit = new ThreadWorkUnit();
				unit.addSupplementalData("fileName", this.getParameters().getFilePaths() + file.getName());
				listOfThreadWorkUnit.add(unit);
				log.info("***** getJobWork ::::: " + this.getParameters().getFilePaths() + file.getName());
			}
		}

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;
	}

	public Class<CMBatchStageUploadWorker> getThreadWorkerClass() {
		return CMBatchStageUploadWorker.class;
	}

	public static class CMBatchStageUploadWorker extends CMBatchStageUploadWorker_Gen {

		private static final String C1_FORM_UPLOAD_STAGING = "C1-FormUploadStaging";
		private static final String CM_REGSTGULPD = "CM-REGFORMSTGULPD";
		private static final String EXTERNALFRMTYPE = "EXTERNALFRMTYPE";
		private static final String C1_FORM_PYMNT_FLG = "C1_FORM_PYMNT_FLG";
		private static final String C1_FORM_PYMNT_AMT = "C1_FORM_PYMNT_AMT";
		private static final String C1_FORM_UPLD_STG_TYP_CD = "C1_FORM_UPLD_STG_TYP_CD";
		private static final String EXT_FORM_TYPE = "EXT_FORM_TYPE";
		private static final String C1_FORM_YEAR = "C1_FORM_YEAR";
		private static final String C1_EXT_FORM_SUBM_SEQ = "C1_EXT_FORM_SUBM_SEQ";
		private static final String C1_FORM_UPLD_STG_ID = "C1_FORM_UPLD_STG_ID";
		private static final String C1_STANDARD_FORM_BATCH_HEADER = "C1-StandardFormBatchHeader";
		private static final String LAD = "LAD";
		private static final String PENDING = "PENDING";
		private static final String STATUS_UPD_DTTM = "STATUS_UPD_DTTM";
		private static final String CRE_DTTM = "CRE_DTTM";
		private static final String TOT_PAY_AMT = "TOT_PAY_AMT";
		private static final String TOT_FORMS_CNT = "TOT_FORMS_CNT";
		private static final String VERSION = "VERSION";
		private static final String C1_FORM_SRCE_CD = "C1_FORM_SRCE_CD";
		private static final String BO_DATA_AREA = "BO_DATA_AREA";
		private static final String BO_STATUS_CD = "BO_STATUS_CD";
		private static final String BUS_OBJ_CD = "BUS_OBJ_CD";
		private static final String EXT_FORM_BATCH_ID = "EXT_FORM_BATCH_ID";
		private static final String FORM_BATCH_HDR_ID = "FORM_BATCH_HDR_ID";
		public static final String AS_CURRENT = "asCurrent";
		private CmXLSXReaderComponent cmXLSXReader = CmXLSXReaderComponent.Factory.newInstance();
		private static CmEmployerRegHelper customHelper = new CmEmployerRegHelper();
		//private static CmConstant cmConstant = new CmConstant();
		XSSFSheet spreadsheet;
		private int cellId = 0;
		PreparedStatement psPreparedStatement = null;
		StringBuilder stringBuilder = null;
		Calendar calInstacne = Calendar.getInstance();
		Date statusUploadDate = new Date();
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new CommitEveryUnitStrategy(this);
		}
		
		@Override
		public void initializeThreadWork(boolean initializationPreviouslySuccessful)
				throws ThreadAbortedException, RunAbortedException {

			log.info("*****initializeThreadWork");
		}

		public boolean executeWorkUnit(ThreadWorkUnit listOfUnit) throws ThreadAbortedException, RunAbortedException {
			
			System.out.println("######################## Demarrage executeWorkUnit ############################");
			boolean foundNinea = false, checkErrorInExcel = false, processed = false,processStgFlag =false;
			Cell cell;
			List<Object> listesValues = new ArrayList<Object>();
			log.info("*****Starting Execute Work Unit");
			String fileName = listOfUnit.getSupplementallData("fileName").toString();
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
				String nineaNumber = null;
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
												.validatePhoneNumber(cell.getNumericCellValue());
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
						processed = saveFormBatchHeaderXML(nineaNumber);
						processStgFlag = saveFormUploadStageXML(listesValues, nineaNumber);
						System.out.println("*****Bo Creation Status**** " + processed);
						log.info("*****Bo Creation Status**** " + processed);
					} catch (Exception exception) {
						processed = false;
						processStgFlag = false;
						System.out.println("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
								+ listesValues.get(3));
						log.info("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
								+ listesValues.get(3));
						exception.printStackTrace();
					}
				}
				foundNinea = false;
			}
			if (processed && processStgFlag) {
				customHelper.moveFileToProcessedFolder(fileName, this.getParameters().getPathToMove());
			} else {
				customHelper.moveFileToFailuireFolder(fileName, this.getParameters().getErrorFilePathToMove());
			}
			
			System.out.println("######################## Terminer executeWorkUnit ############################");
			return true;
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
						Arrays.asList(URLEncoder.encode(CmEmployerRegConstant.TYPE_D_EMPLOYEUR, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.TYPE_D_EST, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.RAISON_SOCIALE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.NINEA, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.NINET, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.FORME_JURIDIQUE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DATE_DE_CREATION, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.DATE_IDENTIFICATION_FISCALE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.NUMERO_REGISTER_DE_COMMERCE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DATE_IMM_REGISTER_DE_COMMERCE, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.DATE_OUVERTURE_EST, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DATE_EMBAUCHE_PREMIER_SALARY, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.SECTEUR_ACTIVITIES, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.ACTIVATE_PRINCIPAL, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.TAUX_AT, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.NOMBRE_TRAVAIL_REGIME_GENERAL, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.NOMBRE_TRAVAIL_REGIME_CADRE, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.REGION, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DÉPARTEMENT, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.ARONDISSEMENT, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.COMMUNE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.QUARTIER, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.ADDRESS, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.TELEPHONE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.EMAIL, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_CSS, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.ZONE_GEOGRAPHIQUE_IPRES, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_CSS, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.SECTOR_GEOGRAPHIC_IPRES, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.AGENCE_CSS, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.AGENCE_IPRES, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.LEGAL_REPRESENTANT, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.LAST_NAME, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.FIRST_NAME, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DATE_DE_NAISSANCE, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.NATIONALITE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.LEGAL_REP_NIN, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.EMPLOYEE_NIN, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.PAYS_DE_NAISSANCE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DATE_DE_DELIVRANCE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.DATE_DE_EXPIRATION, CmEmployerRegConstant.UTF),
								URLEncoder.encode(CmEmployerRegConstant.MOBILE_NUM, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.TYPE_PIECE_IDENTITE, CmEmployerRegConstant.UTF),URLEncoder.encode(CmEmployerRegConstant.NUMERO_PIECE_IDENTITE, CmEmployerRegConstant.UTF)
					));
			} catch (UnsupportedEncodingException e) {
				log.error("*****Issue in Processing file***** "+e);
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
			startChanges();
			// BusinessService_Id businessServiceId=new
			// BusinessService_Id("F1-AddToDoEntry");
			BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
			Role_Id toDoRoleId = new Role_Id("CM-REGTODO");
			Role toDoRole = toDoRoleId.getEntity();
			businessServiceInstance.getFieldAndMDForPath("sendTo").setXMLValue("SNDR");
			businessServiceInstance.getFieldAndMDForPath("subject").setXMLValue("Batch Update from PSRM");
			businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue("CM-REGTO");
			businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
			businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue("CM-REGFORMSTGULPD");
			businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue("90007");
			businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue(messageNumber);
			businessServiceInstance.getFieldAndMDForPath("messageParm1").setXMLValue(messageParam);
			businessServiceInstance.getFieldAndMDForPath("messageParm2").setXMLValue(nineaNumber);
			businessServiceInstance.getFieldAndMDForPath("messageParm3").setXMLValue(fileName);
			businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue("CM-REGFORMSTGULPD");

			BusinessServiceDispatcher.execute(businessServiceInstance);
			saveChanges();
			// getSession().commit();
		}
		
		@SuppressWarnings("deprecation")
		private boolean saveFormUploadStageXML(List<Object> listesValues, String nineaNumber) {
			log.info("saveFormUploadStageXML Enters");
			startChanges();	
			List<String> xmlList = createTagList();
			FormType formType = new FormType_Id(this.getParameters().getFormType()).getEntity();
			String formTypeId = formType.getId().getTrimmedValue();
			StringBuilder uploadXMl = new StringBuilder();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 0; i < xmlList.size(); i++) {
				uploadXMl.append("<"+xmlList.get(i)+">"+listesValues.get(i)+"</"+xmlList.get(i)+">");
			}
			String stgUploadXML = "<suspenseIssueList/><formData><regForm><formType>" +formTypeId+"</formType><dateReceived>"+simpleDateFormat.format(statusUploadDate)+"</dateReceived>"+uploadXMl.toString()+"</regForm></formData>";
			
			log.info("Header XML : " + stgUploadXML);
			
			SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
			String formHdrId = getHeaderId(nineaNumber);
			String extFormBatch = nineaNumber + statusUploadDate.getMonth();
			boolean saveFlag = false;
		
			psPreparedStatement = createPreparedStatement(CmEmployerRegConstant.STAGING_SQL_QUERY);
			psPreparedStatement.setAutoclose(false);
			psPreparedStatement.bindString(C1_FORM_UPLD_STG_ID, extFormBatch, null);
			psPreparedStatement.bindString(FORM_BATCH_HDR_ID, formHdrId, null);
			psPreparedStatement.bindString(C1_EXT_FORM_SUBM_SEQ, "0", null);
			psPreparedStatement.bindString(C1_FORM_SRCE_CD,LAD, null);
			psPreparedStatement.bindString(EXT_FORM_TYPE, EXTERNALFRMTYPE, null);
			psPreparedStatement.bindString(C1_FORM_YEAR, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) ,null);
			psPreparedStatement.bindString(C1_FORM_UPLD_STG_TYP_CD, CM_REGSTGULPD, null);
			psPreparedStatement.bindString(BUS_OBJ_CD, C1_FORM_UPLOAD_STAGING, null);
			psPreparedStatement.bindString(BO_STATUS_CD, PENDING, null);
			psPreparedStatement.bindString(C1_FORM_PYMNT_FLG, "C1NP", null);
			psPreparedStatement.bindString(C1_FORM_PYMNT_AMT, "0", null);
			psPreparedStatement.bindString(VERSION, "10" ,null);
			psPreparedStatement.bindString(STATUS_UPD_DTTM, sf.format(statusUploadDate), null);
			psPreparedStatement.bindString(CRE_DTTM, sf.format(statusUploadDate), null);
			psPreparedStatement.bindString(BO_DATA_AREA, stgUploadXML, null);
		
			try {
				int result = psPreparedStatement.executeUpdate();
				log.info("Data Insert Count : " + result);
				saveFlag = true;
				saveChanges();
			} catch (Exception exception) {
				log.info("Exceeption while Inserting records: " + exception.getMessage());
				exception.printStackTrace();
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return saveFlag;
		}

		private String getHeaderId(String nineaNumber) {
			Calendar calender = Calendar.getInstance();
			String formHdrId = nineaNumber+calender.get(Calendar.DATE);
			return formHdrId;
		}
		@SuppressWarnings("deprecation")
		private boolean saveFormBatchHeaderXML(String nineaNumber) {
			
			log.info("saveFormBatchHeaderXML Enters");
			FormType formType = new FormType_Id(this.getParameters().getFormType()).getEntity();
			String formTypeId = formType.getId().getTrimmedValue();
			String headerXml = "<suspenseIssueList/><validFormTypes><formType>"+formTypeId+"</formType></validFormTypes>";
			log.info("Header XML : " + headerXml);
			String formHdrId = getHeaderId(nineaNumber);
			//SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
			String extFormBatch = nineaNumber + "IMMAT" + statusUploadDate.getDate();
			com.splwg.base.api.datatypes.Date dtime = new com.splwg.base.api.datatypes.Date(
					calInstacne.get(Calendar.YEAR), (calInstacne.get(Calendar.MONTH)+1), calInstacne.get(Calendar.DATE));
			boolean saveFlag = false;
			startChanges();			
			psPreparedStatement = createPreparedStatement(CmEmployerRegConstant.HEADER_SQL_QUERY);
			psPreparedStatement.setAutoclose(false);
			psPreparedStatement.bindString(FORM_BATCH_HDR_ID, formHdrId, null);
			psPreparedStatement.bindString(EXT_FORM_BATCH_ID, extFormBatch, null);
			psPreparedStatement.bindString(BUS_OBJ_CD, C1_STANDARD_FORM_BATCH_HEADER, null);
			psPreparedStatement.bindString(BO_STATUS_CD, PENDING, null);
			psPreparedStatement.bindDate(STATUS_UPD_DTTM, dtime);//getSystemDateTime().getDate()
			psPreparedStatement.bindDate(CRE_DTTM, dtime);
			psPreparedStatement.bindBigInteger(TOT_PAY_AMT, BigInteger.valueOf(0));
			psPreparedStatement.bindBigInteger(TOT_FORMS_CNT, BigInteger.valueOf(1));
			psPreparedStatement.bindBigInteger(VERSION, BigInteger.valueOf(5));
			psPreparedStatement.bindString(C1_FORM_SRCE_CD, LAD, null);
			psPreparedStatement.bindString(BO_DATA_AREA, headerXml, null);
			
			try {
				int result = psPreparedStatement.executeUpdate();
				log.info("Data Insert Count : " + result);
				saveFlag = true;
				saveChanges();
			} catch (Exception exception) {
				log.info("Exceeption while Inserting records: " + exception.getMessage());
				exception.printStackTrace();
				saveFlag = false;
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			
			return saveFlag;
		}
		
		private List<String> createTagList() {

			List<String> lineNameList = new ArrayList<String>();

			lineNameList.add("regType");
			lineNameList.add("employerType");
			lineNameList.add("estType");
			lineNameList.add("employerName");
			lineNameList.add("hqId");
			lineNameList.add("nineaNumber");
			lineNameList.add("ninetNumber");
			lineNameList.add("companyOriginId");
			lineNameList.add("legalStatus");
			lineNameList.add("startDate");
			lineNameList.add("taxId");
			lineNameList.add("taxIdDate");
			lineNameList.add("tradeRegisterNumber");
			lineNameList.add("tradeRegisterDate");
			lineNameList.add("dateOfInspection");
			lineNameList.add("dateOfFirstHire");
			lineNameList.add("shortName");
			lineNameList.add("businessSector");
			lineNameList.add("mainLineOfBusiness");
			lineNameList.add("atRate");
			lineNameList.add("noOfWorkersInGenScheme");
			lineNameList.add("noOfWorkersInBasicScheme");
			lineNameList.add("region");
			lineNameList.add("department");
			lineNameList.add("arondissement");
			lineNameList.add("commune");
			lineNameList.add("qartier");
			lineNameList.add("address");
			lineNameList.add("postboxNo");
			lineNameList.add("telephone");
			lineNameList.add("email");
			lineNameList.add("website");
			lineNameList.add("zoneCss");
			lineNameList.add("zoneIpres");
			lineNameList.add("sectorCss");
			lineNameList.add("sectorIpres");
			lineNameList.add("agencyCss");
			lineNameList.add("agencyIpres");
			lineNameList.add("legalRepPerson");
			lineNameList.add("lastName");
			lineNameList.add("firstName");
			lineNameList.add("birthDate");
			lineNameList.add("nationality");
			lineNameList.add("nin");
			lineNameList.add("placeOfBirth");
			lineNameList.add("cityOfBirth");
			lineNameList.add("typeOfIdentity");
			lineNameList.add("identityIdNumber");
			lineNameList.add("ninCedeo");
			lineNameList.add("issuedDate");
			lineNameList.add("expiryDate");
			lineNameList.add("region");
			lineNameList.add("department");
			lineNameList.add("arondissement");
			lineNameList.add("commune");
			lineNameList.add("qartier");
			lineNameList.add("address");
			lineNameList.add("landLineNumber");
			lineNameList.add("mobileNumber");
			lineNameList.add("email");

			return lineNameList;

		}

	}
	
}
