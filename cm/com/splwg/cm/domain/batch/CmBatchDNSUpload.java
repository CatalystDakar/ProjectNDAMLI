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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * @author Balaganesh M
 *
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = errorFilePathToMove, required = true, type = string)
 *            , @BatchJobSoftParameter (name = pathToMove, required = true, type = string)
 *            , @BatchJobSoftParameter (name = formType, required = true, type = string)
 *            , @BatchJobSoftParameter (name = filePaths, required = true, type = string)})
 */
public class CmBatchDNSUpload extends CmBatchDNSUpload_Gen {
	

	private final static Logger log = LoggerFactory.getLogger(CmBatchDNSUpload.class);

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

	public JobWork getJobWork() {
		log.info("***** Start JobWorker***");
		System.out.println("######################## Start JobWorker ############################");
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();

		File[] files = this.getNewTextFiles();

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

	public Class<CmBatchDNSUploadWorker> getThreadWorkerClass() {
		return CmBatchDNSUploadWorker.class;
	}

	public static class CmBatchDNSUploadWorker extends CmBatchDNSUploadWorker_Gen {
		

		private static final String C1_FORM_UPLOAD_STAGING = "C1-FormUploadStaging";
		private static final String CM_REGSTGULPD = "CM_DNSFORMSTGULPD";
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
		private static CmHelper customHelper = new CmHelper();
		StringBuilder uploadXML = null;
		// private static CmConstant cmConstant = new CmConstant();
		XSSFSheet spreadsheet;
		private int cellId = 0;
		PreparedStatement psPreparedStatement = null;
		StringBuilder stringBuilder = null;
		Calendar calInstacne = Calendar.getInstance();
		Date statusUploadDate = new Date();

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new CommitEveryUnitStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit listOfUnit) throws ThreadAbortedException, RunAbortedException {
			
			System.out.println("######################## Demarrage executeWorkUnit ############################");
			boolean foundNinea = false, checkErrorInExcel = false, processed = false, processStgFlag = false, checkXmlExecute =false;
			Cell cell;
			String formHeaderId = null,nineaNumber = null,typeIdentifiant = null;
			List<Object> employerObject = new ArrayList<Object>();
			List<Object> syntheseObject = new ArrayList<Object>();
			List<String> employerXmlList = createEmployerTagList();
			List<String> syntheseXmlList = createSyntheseTagList();
			List<String> employeeXmlList = createEmployeeTagList();
			uploadXML = new StringBuilder();
			Set<Integer> rowNumber = skipHeaderRowNumber();
			Set<String> headerConstants = getHeaderConstants();
			log.info("*****Starting Execute Work Unit");
			String fileName = listOfUnit.getSupplementallData("fileName").toString();
			log.info("*****executeWorkUnit : " + fileName);
			cmXLSXReader.openXLSXFile(fileName);
			spreadsheet = cmXLSXReader.openSpreadsheet(0, null);
			int rowCount = spreadsheet.getLastRowNum() - spreadsheet.getFirstRowNum();
			System.out.println("rowCount:: " + rowCount);
			Iterator<Row> rowIterator = spreadsheet.iterator();
			while (rowIterator.hasNext()) {
				XSSFRow row = (XSSFRow) rowIterator.next();
				List<Object> employeeObject = new ArrayList<Object>();
				cellId = 1;
				int cellCount = spreadsheet.getRow(row.getRowNum()).getLastCellNum();
				log.info("CellCount:: " + cellCount);
				if (rowNumber.contains(row.getRowNum())) {
					continue;
				}
				if (row.getRowNum() > 7) {
					long millis = System.currentTimeMillis();
					formHeaderId = String.valueOf(millis).substring(1,13);
				}
				log.info("#############----ENTERTING INTO ROW-----#############:" + row.getRowNum());
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext() && !foundNinea) {
					try {
						while (cellId <= cellCount && !checkErrorInExcel) {
							String headerName = null, actualHeader = null;
							cell = cellIterator.next();
							if (row.getRowNum() == 2) {
								headerName = URLEncoder.encode(cell.getSheet().getRow(1).getCell(cellId - 1)
										.getRichStringCellValue().toString().trim(), CmDNSConstant.UTF);
								actualHeader = cell.getSheet().getRow(1).getCell(cellId - 1).getRichStringCellValue()
										.toString().trim();
							} else if (row.getRowNum() == 5) {
								headerName = URLEncoder.encode(cell.getSheet().getRow(4).getCell(cellId - 1)
										.getRichStringCellValue().toString().trim(), CmDNSConstant.UTF);
								actualHeader = cell.getSheet().getRow(4).getCell(cellId - 1).getRichStringCellValue()
										.toString().trim();
							} else if (row.getRowNum() > 7) {
								headerName = URLEncoder.encode(cell.getSheet().getRow(7).getCell(cellId - 1)
										.getRichStringCellValue().toString().trim(), CmDNSConstant.UTF);
								actualHeader = cell.getSheet().getRow(7).getCell(cellId - 1).getRichStringCellValue()
										.toString().trim();
							}
							switch (cell.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								if (row.getRowNum() == 2)
								{	
									if(!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
											URLEncoder.encode(CmDNSConstant.MAISON_MERE, CmConstant.UTF))){
										if(cell.getStringCellValue().equalsIgnoreCase("Oui")){
											employerObject.add(true);
										}else{
											employerObject.add(false);
										}
										break;
									}
									if(!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
											URLEncoder.encode(CmDNSConstant.TYPE_IDENTIFIANT, CmConstant.UTF))){
										typeIdentifiant = cell.getStringCellValue();
									}
									employerObject.add(cell.getStringCellValue());
								} else if (row.getRowNum() == 5) {
									syntheseObject.add(cell.getStringCellValue());
								} else if (row.getRowNum() > 7){
									employeeObject.add(cell.getStringCellValue());
								}	
								System.out.println(cell.getStringCellValue());
								break;
							case Cell.CELL_TYPE_NUMERIC:
								if (DateUtil.isCellDateFormatted(cell)) {
									String convertedDate = customHelper
											.convertDateFormat(cell.getDateCellValue().toString().trim());

									if (isBlankOrNull(convertedDate) || convertedDate
											.equalsIgnoreCase(CmDNSConstant.INVALID_DATE_STRING)) {
										checkErrorInExcel = true;
										createToDo(cell.getDateCellValue().toString(), nineaNumber,
												CmDNSConstant.INVALID_DATE, fileName);
										log.info("Given Ninea Number having invalid Date Format-"
												+ cell.getDateCellValue() + ":" + nineaNumber);
										break;
									} else {
										if (row.getRowNum() == 2)
											employerObject.add(convertedDate);
										else if (row.getRowNum() == 5) 
											syntheseObject.add(convertedDate);
										else if (row.getRowNum() > 7)
											employeeObject.add(convertedDate);
									}
									System.out.println(convertedDate);
								} else {
									if (!isBlankOrNull(headerName)
											&& (typeIdentifiant.equalsIgnoreCase(CmDNSConstant.SCI) && headerName.equalsIgnoreCase(
													URLEncoder.encode(CmDNSConstant.NUMERO_IDENTIFANT, CmConstant.UTF)))) {
										Double nineaNum = cell.getNumericCellValue();
										DecimalFormat df = new DecimalFormat("#");
										nineaNumber = df.format(nineaNum);
										if (nineaNum.toString().length() == 7) {
											nineaNumber = CmDNSConstant.NINEA_PREFIX + nineaNumber;
										}
										if (!customHelper.validateNineaNumber(nineaNumber)) {
											checkErrorInExcel = true;
											createToDo("", nineaNumber, CmDNSConstant.NINEA_INVALID, fileName);
											log.info("Given Ninea Number is Invalid: " + cell.getNumericCellValue());
											break;
										}
									}
									if (row.getRowNum() == 2){
										employerObject.add((long) cell.getNumericCellValue());
									} else if (row.getRowNum() == 5) {
										syntheseObject.add((long) cell.getNumericCellValue());
									} else if (row.getRowNum() > 7) {
										employeeObject.add((long) cell.getNumericCellValue());
									}
									System.out.println((long) cell.getNumericCellValue());
								}
								break;
							case Cell.CELL_TYPE_BLANK:
								 if (headerConstants.contains(headerName)){
									 checkErrorInExcel = true;
									 createToDo(actualHeader, nineaNumber,CmDNSConstant.EMPTY, fileName);
									 log.info(actualHeader+ " is Empty:"+
									 nineaNumber); 
									 break; 
								 }
								 if (row.getRowNum() == 5) {
									 syntheseObject.add("");
								 } else if(row.getRowNum()>7){
									 employeeObject.add("");
								 } 
								System.out.println("Blank:");
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								System.out.println("BOOLEAN");
								System.out.println(cell.getBooleanCellValue());
								break;
							default:
								employeeObject.add("");
								System.out.println("Blank:");
								break;
							}
							cellId++;
						}
					} catch (UnsupportedEncodingException ex) {
						log.info("*****Unsupported Encoding**** " + ex);
					}
					if (row.getRowNum() > 7) {
						foundNinea = true;
						if (checkErrorInExcel) {
							checkErrorInExcel = false;
							break;
						}
						if(!checkXmlExecute){
							checkXmlExecute = true;
							for (int i = 0; i < employerXmlList.size(); i++) {
								uploadXML.append("<" + employerXmlList.get(i) + ">" + employerObject.get(i) + "</" + employerXmlList.get(i) + ">");
							}
							for (int i = 0; i < syntheseXmlList.size(); i++) {
								uploadXML.append("<" + syntheseXmlList.get(i) + ">" + syntheseObject.get(i) + "</" + syntheseXmlList.get(i) + ">");
							}
							uploadXML.append("<informationsDesSalaries> <informationsDesSalariesList>");
							for (int i = 0; i < employeeXmlList.size(); i++) {
								uploadXML.append("<" + employeeXmlList.get(i) + ">" + employeeObject.get(i) + "</" + employeeXmlList.get(i) + ">");
							}
							uploadXML.append("</informationsDesSalariesList>");
						} else{
							uploadXML.append("<informationsDesSalariesList>");
							for (int i = 0; i < employeeXmlList.size(); i++) {
								uploadXML.append("<" + employeeXmlList.get(i) + ">" + employeeObject.get(i) + "</" + employeeXmlList.get(i) + ">");
							}
							uploadXML.append("</informationsDesSalariesList>");
						}
					}
				}
				foundNinea = false;
			}
			/*
			 * if (processed && processStgFlag) {
			 * customHelper.moveFileToProcessedFolder(fileName,
			 * this.getParameters().getPathToMove()); } else {
			 * customHelper.moveFileToFailuireFolder(fileName,
			 * this.getParameters().getErrorFilePathToMove()); }
			 */
			try {
				processed = saveFormBatchHeaderXML(formHeaderId,nineaNumber);
				processStgFlag = saveFormUploadStageXML(employerObject,syntheseObject,nineaNumber,formHeaderId);
				if(processed && processStgFlag ){
					customHelper.moveFileToProcessedFolder(fileName, this.getParameters().getPathToMove());
				} else {
					customHelper.moveFileToFailuireFolder(fileName,this.getParameters().getErrorFilePathToMove());
				}
				System.out.println("*****Save Batch Header Table**** " + processed);
				log.info("*****Save Batch Staging Table**** " + processed);
			} catch (Exception exception) {
				processed = false;
				processStgFlag = false;
				System.out.println("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
						+ employerObject.get(1));
				log.info("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
						+ employerObject.get(1));
			}
			System.out.println("######################## Terminer executeWorkUnit ############################");
			return true;
		}

		private boolean saveFormUploadStageXML(List<Object> employerObject,List<Object> syntheseObject,
				String nineaNumber, String formHeaderId) {
			
			log.info("saveFormUploadStageXML Enters");
			startChanges();
			FormType formType = new FormType_Id(this.getParameters().getFormType()).getEntity();
			String formTypeId = formType.getId().getTrimmedValue();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
			String stgUploadXML = "<suspenseIssueList/><formData><taxForm><formType>" + formTypeId
					+ "</formType><dateReceived>" + simpleDateFormat.format(statusUploadDate) + "</dateReceived>"
					+ uploadXML.toString() + "</informationsDesSalaries></taxForm></formData>";

			log.info("Header XML : " + stgUploadXML);

			SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
			long millis = System.currentTimeMillis();
			String extFormBatch  = String.valueOf(millis).substring(1,13);
			//String formHdrId = getHeaderId(String.valueOf(employeeObject.get(0)));
			//String extFormBatch = nineaNumber + statusUploadDate.getDay()+statusUploadDate.getSeconds();
			boolean saveFlag = false;

			psPreparedStatement = createPreparedStatement(CmDNSConstant.STAGING_SQL_QUERY);
			psPreparedStatement.setAutoclose(false);
			psPreparedStatement.bindString(C1_FORM_UPLD_STG_ID, extFormBatch, null);
			psPreparedStatement.bindString(FORM_BATCH_HDR_ID, formHeaderId, null);
			psPreparedStatement.bindString(C1_EXT_FORM_SUBM_SEQ, "0", null);
			psPreparedStatement.bindString(C1_FORM_SRCE_CD, "ONLINE-ENTRY", null);
			psPreparedStatement.bindString(EXT_FORM_TYPE, EXTERNALFRMTYPE, null);
			psPreparedStatement.bindString(C1_FORM_YEAR, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),null);
			psPreparedStatement.bindString(C1_FORM_UPLD_STG_TYP_CD, CM_REGSTGULPD, null);
			psPreparedStatement.bindString(BUS_OBJ_CD, C1_FORM_UPLOAD_STAGING, null);
			psPreparedStatement.bindString(BO_STATUS_CD, PENDING, null);
			psPreparedStatement.bindString(C1_FORM_PYMNT_FLG, "C1NP", null);
			psPreparedStatement.bindString(C1_FORM_PYMNT_AMT, "0", null);
			psPreparedStatement.bindString(VERSION, "10", null);
			psPreparedStatement.bindString(STATUS_UPD_DTTM, sf.format(statusUploadDate), null);
			psPreparedStatement.bindString(CRE_DTTM, sf.format(statusUploadDate), null);
			psPreparedStatement.bindString(BO_DATA_AREA, stgUploadXML, null);

			try {
				int result = psPreparedStatement.executeUpdate();
				log.info("Data Insert Count : " + result);
				saveFlag = true;
				saveChanges();
			} catch (Exception exception) {
				saveFlag = false;
				log.info("Exceeption while Inserting records: " + exception.getMessage());
				exception.printStackTrace();
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return saveFlag;
		}

		private List<String> createSyntheseTagList() {
			
			List<String> syntheseList = new ArrayList<String>();

			syntheseList.add("totalSalaries");
			syntheseList.add("totalSalIpres");
			syntheseList.add("totalSalCSS");
			syntheseList.add("totalSalVerses");
			syntheseList.add("tauxCotisationPF");
			syntheseList.add("tauxCotisationAtMp");
			syntheseList.add("tauxCotisationRetraite");
			syntheseList.add("tauxCotisationRetraiteRcc");
			syntheseList.add("plafondPF");
			syntheseList.add("plafondATMP");
			syntheseList.add("plafondCRRG");
			syntheseList.add("cotisationRGCC");
			syntheseList.add("montantPF");
			syntheseList.add("montantATMP");
			syntheseList.add("montantRRG");
			syntheseList.add("montantRCC");
			
			return syntheseList;
		}

		private List<String> createEmployerTagList() {
			
			List<String> employerNameList = new ArrayList<String>();

			employerNameList.add("typeIdentifiant");
			employerNameList.add("idNumber");
			employerNameList.add("raisonSociale");
			employerNameList.add("adresse");
			employerNameList.add("dateDebutCotisation");
			employerNameList.add("dateFinCotisation");
			employerNameList.add("activitePrincipale");
			
			return employerNameList;
		}

		private List<String> createEmployeeTagList() {
			List<String> employeeNameList = new ArrayList<String>();

			employeeNameList.add("nom");
			employeeNameList.add("prenom");
			employeeNameList.add("typePieceIdentite");
			employeeNameList.add("numeroPieceIdentite");
			employeeNameList.add("regime");
			employeeNameList.add("dateEffetRegime");
			employeeNameList.add("totalPlafondIpres");
			employeeNameList.add("totalPlafondCSS");
			employeeNameList.add("salaireBrut");
			employeeNameList.add("typeContrat");
			employeeNameList.add("dateEntree");
			employeeNameList.add("dateSortie");
			employeeNameList.add("motifSortie");
			employeeNameList.add("nombreJours");
			employeeNameList.add("nombreHeures");
			employeeNameList.add("tempsTravail");
			employeeNameList.add("trancheTravail");
			employeeNameList.add("smig");
			employeeNameList.add("tauxCssCpf");
			employeeNameList.add("tauxCssCatmp");
			employeeNameList.add("tauxIpresCrrg");
			employeeNameList.add("tauxIpresCrcc");
			employeeNameList.add("plafondCssCpf");
			employeeNameList.add("plafondCssAtMp");
			employeeNameList.add("plafondIpresCrrg");
			employeeNameList.add("plafondIpresCrcc");
			employeeNameList.add("montantCssCpf");
			employeeNameList.add("montantCssAtMp");
			employeeNameList.add("montantIpresCrrg");
			employeeNameList.add("montantIpresCrcc");

			return employeeNameList;
		}

		private boolean saveFormBatchHeaderXML(String formHeaderId,String nineaNumber) {
			PreparedStatement psPreparedStatement = null;
			StringBuilder stringBuilder = null;
			Date dt = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int date = cal.get(Calendar.DATE);
			int minute =  cal.get(Calendar.MINUTE);
			int hour =  cal.get(Calendar.HOUR);
			int seconds =  cal.get(Calendar.SECOND);
			int milliseconds =  cal.get(Calendar.MILLISECOND);
			SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
			//String formHdrId = String.valueOf(employeeObject.get(0)) + dt.getDate();
			String extFormBatch = null;
			extFormBatch = nineaNumber + "_" + date + month + year + "_" + hour + ":" + minute + ":"
					+ seconds+":"+milliseconds;
			String tenderCtrlId =  " ";// nineaNumber + dt.getDay();
			String headerXML = createFormBatchHeaderXML();
			com.splwg.base.api.datatypes.Date dtime = new com.splwg.base.api.datatypes.Date(year, month, date);
			stringBuilder = new StringBuilder();
			boolean saveFlag = false;
			sf.format(dt);

			startChanges();

			stringBuilder.append(
					"insert into CI_FORM_BATCH_HDR (FORM_BATCH_HDR_ID,EXT_FORM_BATCH_ID,BUS_OBJ_CD,BO_STATUS_CD,STATUS_UPD_DTTM,CRE_DTTM,TOT_PAY_AMT,TOT_FORMS_CNT,VERSION,C1_FORM_SRCE_CD,TNDR_CTL_ID,BO_DATA_AREA)");
			stringBuilder.append(" values ");
			stringBuilder.append("(:FORM_BATCH_HDR_ID,:EXT_FORM_BATCH_ID,:BUS_OBJ_CD,:BO_STATUS_CD,:STATUS_UPD_DTTM,:CRE_DTTM,:TOT_PAY_AMT,:TOT_FORMS_CNT,:VERSION,:C1_FORM_SRCE_CD,:TNDR_CTL_ID,:BO_DATA_AREA)");
			psPreparedStatement = createPreparedStatement(stringBuilder.toString());
			psPreparedStatement.setAutoclose(false);
			psPreparedStatement.bindBigInteger("FORM_BATCH_HDR_ID", BigInteger.valueOf(Long.parseLong(formHeaderId)));
			psPreparedStatement.bindString("EXT_FORM_BATCH_ID", extFormBatch, null);
			psPreparedStatement.bindString("BUS_OBJ_CD", "C1-StandardFormBatchHeader", null);
			psPreparedStatement.bindString("BO_STATUS_CD", "PENDING", null);
			psPreparedStatement.bindDate("STATUS_UPD_DTTM", dtime);
			psPreparedStatement.bindDate("CRE_DTTM", dtime);
			psPreparedStatement.bindBigInteger("TOT_PAY_AMT", BigInteger.valueOf(0));
			psPreparedStatement.bindBigInteger("TOT_FORMS_CNT", BigInteger.valueOf(1));
			psPreparedStatement.bindBigInteger("VERSION", BigInteger.valueOf(5));
			psPreparedStatement.bindString("C1_FORM_SRCE_CD", "ONLINE-ENTRY", null);
			psPreparedStatement.bindString("TNDR_CTL_ID", tenderCtrlId, null);
			psPreparedStatement.bindString("BO_DATA_AREA", headerXML, null);

			try {
				int result = psPreparedStatement.executeUpdate();
				saveFlag = true;
				saveChanges();
			} catch (Exception exception) {
				System.out.println("Unable to get Lookup value for the Description:: " + exception.getMessage());
				exception.printStackTrace();
				saveFlag = false;
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}

			return saveFlag;
		}

		private String createFormBatchHeaderXML() {
			
			FormType formType = new FormType_Id(this.getParameters().getFormType()).getEntity();
			String formTypeId = formType.getId().getTrimmedValue();
			StringBuilder sb = new StringBuilder();

			try {
				sb.append("<suspenseIssueList/>");
				sb.append("<validFormTypes><formType>");
				sb.append(formTypeId);
				sb.append("</formType></validFormTypes>");

			} catch (Exception exception) {
				log.info("Exception in createFormBatchHeaderXML :" + exception);
			}

			return sb.toString();
		}

		private Set<String> getHeaderConstants() {
			Set<String> headerConstanstSet = null;
			try {
				headerConstanstSet = new HashSet<String>(Arrays
						.asList(URLEncoder.encode(CmDNSConstant.RAISON_SOCIALE, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.ADDRESS, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.NINEA, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.TYPE_IDENTIFIANT, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.NUMERO_IDENTIFANT, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.IDENTIFIANT_IMMATRICULATION,CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.ACTIVITE_PRINCIPALE, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.MAISON_MERE, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.DATE_DEBUT_PERIOD, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.DATE_DEBUT_PERIOD, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.DATE_FIN_PERIOD, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.NOM, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.PRENOM, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.REGIME, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.TOTAL_SALARIE_COTISATION, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.SALARIE_REEL, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.TYPE_DE_CONTRACT, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.DATE_ENTREE, CmDNSConstant.UTF),
								URLEncoder.encode(CmDNSConstant.TEMPS_SUR_PERIODE, CmDNSConstant.UTF)));
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
		
		private Set<Integer> skipHeaderRowNumber() {
			Set<Integer> rowNumber = new HashSet<Integer>();
			rowNumber.add(0);
			rowNumber.add(1);
			rowNumber.add(3);
			rowNumber.add(4);
			rowNumber.add(6);
			rowNumber.add(7);
			return rowNumber;
		}
		
	}

}
