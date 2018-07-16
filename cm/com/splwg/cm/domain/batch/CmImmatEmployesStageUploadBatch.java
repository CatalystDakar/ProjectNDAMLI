package com.splwg.cm.domain.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.cm.domain.common.businessComponent.CmXLSXReaderComponent;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formType.FormType;
import com.splwg.tax.domain.admin.formType.FormType_Id;

/**
 * @author BEYE
 *
 * @BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = errorFilePathToMove, required = true, type = string)
 *           , @BatchJobSoftParameter (name = formType, required = true, type =string) , @BatchJobSoftParameter (name = filePaths, required =
 *           true, type = string) , @BatchJobSoftParameter (name = pathToMove,required = true, type = string)})
 */
public class CmImmatEmployesStageUploadBatch extends CmImmatEmployesStageUploadBatch_Gen {

	private final static Logger log = LoggerFactory.getLogger(CmImmatEmployesStageUploadBatch.class);

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
        // TODO Auto-generated method stub
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

    public Class<CmImmatEmployesStageUploadBatchWorker> getThreadWorkerClass() {
        return CmImmatEmployesStageUploadBatchWorker.class;
    }

    public static class CmImmatEmployesStageUploadBatchWorker extends CmImmatEmployesStageUploadBatchWorker_Gen {
        private CmXLSXReaderComponent cmXLSXReader = CmXLSXReaderComponent.Factory.newInstance();
        private static CmHelper customHelper = new CmHelper();
        XSSFSheet spreadsheet;
        private int cellId = 0;
        PreparedStatement psPreparedStatement = null;
        private static final String C1_FORM_UPLOAD_STAGING = "C1-FormUploadStaging";
        //private static final String C1_EXT_FORM_SUBM_ID = "C1_EXT_FORM_SUBM_ID";
        private static final String CM_REGSTGULPD = "CM-STG-IMMAT-SAL";
        private static final String EXTERNALFRMTYPE = "EXTERNALFRMTYPE";
        private static final String C1_FORM_PYMNT_FLG = "C1_FORM_PYMNT_FLG";
        private static final String C1_FORM_PYMNT_AMT = "C1_FORM_PYMNT_AMT";
        private static final String C1_FORM_UPLD_STG_TYP_CD = "C1_FORM_UPLD_STG_TYP_CD";
        private static final String EXT_FORM_TYPE = "EXT_FORM_TYPE";
        private static final String C1_FORM_YEAR = "C1_FORM_YEAR";
        private static final String C1_EXT_FORM_SUBM_SEQ = "C1_EXT_FORM_SUBM_SEQ";
        private static final String C1_FORM_UPLD_STG_ID = "C1_FORM_UPLD_STG_ID";
        private static final String C1_STANDARD_FORM_BATCH_HEADER = "C1-StandardFormBatchHeader";
        private static final String ONLINE_ENTRY = "ONLINE-ENTRY";
        private static final String PENDING = "PENDING";
        private static final String STATUS_UPD_DTTM = "STATUS_UPD_DTTM";
        private static final String CRE_DTTM = "CRE_DTTM";
        private static final String TOT_PAY_AMT = "TOT_PAY_AMT";
        private static final String TNDR_CTL_ID = "TNDR_CTL_ID";
        private static final String TOT_FORMS_CNT = "TOT_FORMS_CNT";
        private static final String VERSION = "VERSION";
        private static final String C1_FORM_SRCE_CD = "C1_FORM_SRCE_CD";
        private static final String BO_DATA_AREA = "BO_DATA_AREA";
        private static final String BO_STATUS_CD = "BO_STATUS_CD";
        private static final String BUS_OBJ_CD = "BUS_OBJ_CD";
        private static final String EXT_FORM_BATCH_ID = "EXT_FORM_BATCH_ID";
        private static final String FORM_BATCH_HDR_ID = "FORM_BATCH_HDR_ID";
        public static final String AS_CURRENT = "asCurrent";
        public static final String TAX_FORM_ID = "TAX_FORM_ID";
        public static final String REG_FORM_ID = "REG_FORM_ID";
        
        public ThreadExecutionStrategy createExecutionStrategy() {
            // TODO Auto-generated method stub
            return new CommitEveryUnitStrategy(this);
        }

        public boolean executeWorkUnit(ThreadWorkUnit listOfUnit) throws ThreadAbortedException, RunAbortedException {
            System.out.println("######################## Demarrage executeWorkUnit ############################");
            boolean foundNinea = false, checkErrorInExcel = false, processed = false, processStgFlag = false;
            Cell cell;
            List<Object> listesValues = new ArrayList<Object>();  
            log.info("*****Starting Execute Work Unit");
            String fileName = listOfUnit.getSupplementallData("fileName").toString(); 
            log.info("*****executeWorkUnit : " + fileName);
            cmXLSXReader.openXLSXFile(fileName);
            spreadsheet = cmXLSXReader.openSpreadsheet(0, null);
            int rowCount = spreadsheet.getLastRowNum() - spreadsheet.getFirstRowNum();
            System.out.println("rowCount:: " + rowCount);

            Iterator<Row> rowIterator = spreadsheet.iterator();
            int cellCount = spreadsheet.getRow(0).getLastCellNum();
            System.out.println("CellCount: " +cellCount);
            log.info("CellCount:: " + cellCount);
            while (rowIterator.hasNext()) {
                XSSFRow row = (XSSFRow) rowIterator.next();
                String nineaNumber = null;
                String ninNumber=null;
                cellId = 1;
                String dateDebut = null, dateFin = null, deliveryDate = null;
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
                                    cell.getSheet().getRow(0).getCell(cellId - 1).getRichStringCellValue().toString(),
                                    CmConstant.UTF);
                            String actualHeader = cell.getSheet().getRow(0).getCell(cellId - 1).getRichStringCellValue()
                                    .toString();
                            switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                if (headerName != null
                                        && (headerName.equalsIgnoreCase(URLEncoder.encode(CmConstant.NOM_EMPLOYE))
                                                || headerName.equalsIgnoreCase(
                                                        URLEncoder.encode(CmConstant.PRENOM_EMPLOYE, CmConstant.UTF))
                                                || headerName.equalsIgnoreCase(
                                                        URLEncoder.encode(CmConstant.NOM_DU_PERE, CmConstant.UTF))
                                                || headerName.equalsIgnoreCase(
                                                        URLEncoder.encode(CmConstant.PRENOM_DU_PERE, CmConstant.UTF))
                                                || headerName.equalsIgnoreCase(
                                                        URLEncoder.encode(CmConstant.NOM_DE_LA_MERE, CmConstant.UTF))
                                                || headerName.equalsIgnoreCase(URLEncoder
                                                        .encode(CmConstant.PRENOM_DE_LA_MERE, CmConstant.UTF)))) {
                                    checkValidationFlag = customHelper.validateAlphabetsOnly(cell.getStringCellValue());
                                    if (checkValidationFlag != null && !checkValidationFlag) { // Alphabets
                                                                                               // only
                                        // Error Skip the row
                                        checkErrorInExcel = true;
                                        // createToDo(actualHeader, nineaNumber,
                                        // CmConstant.NAME_INVALID, fileName);
                                        log.info("Given " + actualHeader
                                                + "is having special characters or number for the given ninea number:"
                                                + nineaNumber);
                                        break;
                                    }
                                } else if (headerName != null && headerName.equalsIgnoreCase(CmConstant.NINEA)) {
                                    checkErrorInExcel = true;
                                    // createToDo(cell.getStringCellValue(),
                                    // nineaNumber, CmConstant.NINEA_INVALID,
                                    // fileName);
                                    log.info("Given " + headerName
                                            + " having special characters or alphabets for the given ninea number:"
                                            + nineaNumber);
                                    break;
                                } else if (headerName != null && headerName.equalsIgnoreCase(CmConstant.NIN)) {
                                    checkErrorInExcel = true;
                                    // createToDo(cell.getStringCellValue(),
                                    // nineaNumber, CmConstant.NINEA_INVALID,
                                    // fileName);
                                    log.info("Given " + headerName
                                            + " having special characters or alphabets for the given nin number:"
                                            + nineaNumber);
                                    break;
                                }
                                listesValues.add(cell.getStringCellValue());
                                System.out.println(cell.getStringCellValue());
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {// Date
                                                                         // Validation
                                    // Date de debut
                                    if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
                                            URLEncoder.encode(CmConstant.DATE_DEBUT_CONTRAT, CmConstant.UTF))) {
                                        dateDebut = cell.getDateCellValue().toString();
                                    }
                                    // Establishment Date
                                    if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
                                            URLEncoder.encode(CmConstant.DATE_FIN_CONTRAT, CmConstant.UTF))) {
                                        dateFin = cell.getDateCellValue().toString();
                                        checkValidationFlag = customHelper.compareTwoDates(dateFin, dateDebut,
                                                "greatEqual"); // validate two
                                                               // dates
                                        if (checkValidationFlag != null && !checkValidationFlag) {
                                            checkErrorInExcel = true;
                                            // createToDo(actualHeader,
                                            // nineaNumber,
                                            // CmConstant.DATE_EST_GREAT_IMM,
                                            // fileName);
                                            log.info("Given->" + actualHeader
                                                    + " Date lesser than Date de numéro de registre du commerce-"
                                                    + cell.getDateCellValue() + ":" + nineaNumber);
                                            break;
                                        }
                                    }

                                    if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
                                            URLEncoder.encode(CmConstant.DATE_DE_DELIVRANCE, CmConstant.UTF))) {
                                        deliveryDate = cell.getDateCellValue().toString();
                                    }

                                    if (!isBlankOrNull(headerName) && headerName.equalsIgnoreCase(
                                            URLEncoder.encode(CmConstant.DATE_DE_EXPIRATION, CmConstant.UTF))) {
                                        checkValidationFlag = customHelper.compareTwoDates(
                                                cell.getDateCellValue().toString(), deliveryDate, "great"); // validate
                                                                                                            // two
                                                                                                            // dates
                                        if (checkValidationFlag != null && !checkValidationFlag) {
                                            checkErrorInExcel = true;
                                            // createToDo(actualHeader,
                                            // nineaNumber,
                                            // CmConstant.DATE_DEL_GREAT_EXP,
                                            // fileName);
                                            log.info("Given->" + actualHeader + " Date lesser than Date de délivrance-"
                                                    + cell.getDateCellValue() + ":" + nineaNumber);
                                            break;
                                        }
                                    }

                                    String convertedDate = customHelper
                                            .convertDateFormat(cell.getDateCellValue().toString());

                                    if (isBlankOrNull(convertedDate)
                                            || convertedDate.equalsIgnoreCase(CmConstant.INVALID_DATE_STRING)) {
                                        checkErrorInExcel = true;
                                        // createToDo(cell.getDateCellValue().toString(),
                                        // nineaNumber, CmConstant.INVALID_DATE,
                                        // fileName);
                                        log.info("Given Ninea Number having invalid Date Format-"
                                                + cell.getDateCellValue() + ":" + nineaNumber);
                                        break;
                                    } else {
                                        listesValues.add(convertedDate);
                                    }
                                    System.out.println(convertedDate);
                                } else {
                                    if (headerName != null && headerName.equalsIgnoreCase(CmConstant.NINEA)
                                            && cell.getColumnIndex() == 0) {// Ninea
                                                                            // Validation
                                        Double nineaNum = cell.getNumericCellValue();
                                        DecimalFormat df = new DecimalFormat("#");
                                        nineaNumber = df.format(nineaNum);
                                        if (nineaNum.toString().length() == 7) {// Adding
                                                                                // zero
                                                                                // based
                                                                                // on
                                                                                // functional
                                                                                // testing
                                                                                // feedback
                                                                                // from
                                                                                // khawla
                                                                                // -
                                                                                // 09April
                                            nineaNumber = CmConstant.NINEA_PREFIX + nineaNumber;
                                        }
                                        if (customHelper.validateNineaNumber(nineaNumber)) {
                                            checkValidationFlag = customHelper.validateNineaExist(nineaNumber);
                                            if (checkValidationFlag != null && checkValidationFlag) {
                                                checkErrorInExcel = true;
                                                // createToDo("", nineaNumber,
                                                // CmConstant.NINEA_EXIST,
                                                // fileName);
                                                log.info("Given Ninea Number already Exists: " + nineaNumber);
                                                break;
                                            }
                                        } else {
                                            checkErrorInExcel = true;
                                            // createToDo("", nineaNumber,
                                            // CmConstant.NINEA_INVALID,
                                            // fileName);
                                            log.info("Given Ninea Number is Invalid: " + cell.getNumericCellValue());
                                            break;
                                        }
                                    }
                                    else if (headerName != null && (headerName.equalsIgnoreCase(URLEncoder.encode(CmConstant.NIN, CmConstant.UTF)))) { // PhoneNum Validation
                                        Double ninNum= cell.getNumericCellValue();
                                        DecimalFormat df = new DecimalFormat("#");
                                        ninNumber = df.format(ninNum);
                                        checkValidationFlag = customHelper
                                                .validateNinNumber(ninNumber);
                                        if (checkValidationFlag != null && !checkValidationFlag) {
                                            checkErrorInExcel = true;
                                            //createToDo(actualHeader, nineaNumber, CmConstant.TELEPHONE_INVALID, fileName);
                                            log.info("Given Ninea Number having invalid PhoneNumber- " + actualHeader
                                                    + ":" + cell.getNumericCellValue() + ":" + nineaNumber);
                                            break;
                                        }
                                    }
                                   else if (headerName != null
                                            && (headerName.equalsIgnoreCase(URLEncoder.encode(CmConstant.NOM_EMPLOYE))
                                                    || headerName.equalsIgnoreCase(
                                                            URLEncoder.encode(CmConstant.PRENOM_EMPLOYE,
                                                                    CmConstant.UTF))
                                                    || headerName
                                                            .equalsIgnoreCase(URLEncoder.encode(CmConstant.NOM_DU_PERE,
                                                                    CmConstant.UTF))
                                                    || headerName.equalsIgnoreCase(
                                                            URLEncoder.encode(CmConstant.PRENOM_DU_PERE,
                                                                    CmConstant.UTF))
                                                    || headerName.equalsIgnoreCase(
                                                            URLEncoder.encode(CmConstant.NOM_DE_LA_MERE,
                                                                    CmConstant.UTF))
                                                    || headerName.equalsIgnoreCase(URLEncoder
                                                            .encode(CmConstant.PRENOM_DE_LA_MERE, CmConstant.UTF)))) {
                                        // Error Skip the row
                                        checkErrorInExcel = true;
                                        // createToDo(cell.getStringCellValue(),
                                        // nineaNumber,
                                        // CmConstant.NAME_LETTER_CHECK,
                                        // fileName);
                                        log.info("Given " + headerName
                                                + " should be alphabets for the given ninea number:" + nineaNumber);
                                        break;
                                    }
                                    listesValues.add((long) cell.getNumericCellValue());
                                    System.out.println((long) cell.getNumericCellValue());
                                }
                                break;
                            case Cell.CELL_TYPE_BLANK:
                                /*
                                 * if (headerConstants.contains(headerName)){
                                 * checkErrorInExcel = true;
                                 * //createToDo(actualHeader, nineaNumber,
                                 * CmConstant.EMPTY, fileName);
                                 * log.info(actualHeader+ " is Empty:"+
                                 * nineaNumber); break; }
                                 */
                                System.out.println("Blank:");
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                listesValues.add(cell.getBooleanCellValue());
                                System.out.println(cell.getBooleanCellValue());
                                break;
                            default:
                                listesValues.add(" ");
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
                    	System.out.println("Nombre elements: " +listesValues.size()); 
                        //String headerXML = createFormBatchHeaderXML();
                        // nineaNumber = "123456789";
                        processed = saveFormBatchHeaderXML(nineaNumber);
                       // String stgUploadXML = createFormStageUploadXML(xmlList, listesValues);
                        processStgFlag = saveFormUploadStageXML(listesValues, nineaNumber);
                        System.out.println("*****Bo Creation Status**** " + processed);
                        log.info("*****Bo Creation Status**** " + processed);
                    } catch (Exception exception) {
                        processed = false;
                        System.out.println("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
                                + listesValues.get(3));
                        log.info("*****Issue in Processing file***** " + fileName + "NineaNumber:: "
                                + listesValues.get(3));
                        exception.printStackTrace();
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

            System.out.println("######################## Terminer executeWorkUnit ############################");
            return true;
        }


            private List<String> createTagList() {

                List<String> li = new ArrayList<String>();

                li.add("ninea");        
                li.add("nomEmploye");
                li.add("prenomEmploye");
                li.add("sexe");
                li.add("etatCivil");
                li.add("dateDeNaissance");
                li.add("numIdentificationCSS");
                li.add("numIdentificationIPRES");
                li.add("nomPere");
                li.add("prenomPere");
                li.add("nomMere");
                li.add("prenomMere");
                li.add("nationalite");
                li.add("nin");
                li.add("paysDeNaissance");
                li.add("regionDeNaissance");
                li.add("departementDeNaissance");
                li.add("arrondDeNaissance");
                li.add("comNaiss");
                li.add("quartierDeNaissance");
                li.add("typePieceIdentite");
                li.add("numPieceIdentite");
                li.add("delivreLe");
                li.add("lieuDelivrance");
                li.add("expireLe");
                li.add("pays");
                li.add("region");
                li.add("departement");
                li.add("arrondissement");
                li.add("commune");
                li.add("quartier");
                li.add("adresse");
                li.add("boitePostale");
                li.add("typeMouvement");
                li.add("natureContrat");
                li.add("dateDebut");
                li.add("dateFinContrat");
                li.add("profession");
                li.add("emploi");
                li.add("employeCadre");
                li.add("conventionApplicable");
                li.add("salaireContractuel");
                li.add("tpsDeTravail");
                li.add("employeATMP");
                li.add("categorie");

                return li;

            }

            @SuppressWarnings("deprecation")
            private boolean saveFormBatchHeaderXML(String nineaNumber) {
                
                log.info("saveFormBatchHeaderXML Enters");
                FormType formType = new FormType_Id(this.getParameters().getFormType()).getEntity();
                String formTypeId = formType.getId().getTrimmedValue();
                String headerXml = "<suspenseIssueList/><validFormTypes><formType>"+formTypeId+"</formType></validFormTypes>";
                log.info("Header XML : " + headerXml);
                Date statusUploadDate = new Date();
                Calendar calender = Calendar.getInstance();
                calender.setTime( statusUploadDate);
                SimpleDateFormat simpledateformat = new SimpleDateFormat("dd/MM/yyyy");
                String formHdrId = nineaNumber + statusUploadDate.getDate();
                String extFormBatch = nineaNumber + statusUploadDate.getDate()+ statusUploadDate.getDay();
                //com.splwg.base.api.datatypes.Date dtime = new com.splwg.base.api.datatypes.Date(year,month,date);
                boolean saveFlag = false;
                simpledateformat.format( statusUploadDate);         
                //startChanges();         
                psPreparedStatement = createPreparedStatement(CmConstant.HEADER_SQL_QUERY);
                psPreparedStatement.setAutoclose(false);
                psPreparedStatement.bindBigInteger(FORM_BATCH_HDR_ID, BigInteger.valueOf(Long.parseLong(formHdrId))); 
                psPreparedStatement.bindString(EXT_FORM_BATCH_ID, extFormBatch, null);
                psPreparedStatement.bindString(BUS_OBJ_CD, C1_STANDARD_FORM_BATCH_HEADER, null); 
                psPreparedStatement.bindString(BO_STATUS_CD, PENDING, null);
                psPreparedStatement.bindDate(STATUS_UPD_DTTM, getSystemDateTime().getDate());
                psPreparedStatement.bindDate(CRE_DTTM, getSystemDateTime().getDate());
                psPreparedStatement.bindBigInteger(TOT_PAY_AMT, BigInteger.valueOf(0));
                psPreparedStatement.bindBigInteger(TOT_FORMS_CNT, BigInteger.valueOf(1));
                psPreparedStatement.bindBigInteger(VERSION, BigInteger.valueOf(5));
                psPreparedStatement.bindString(C1_FORM_SRCE_CD, ONLINE_ENTRY, null);
                psPreparedStatement.bindString(BO_DATA_AREA, headerXml, null);  
                
                try {
                    int result = psPreparedStatement.executeUpdate();
                    log.info("Data Insert Count : " + result);
                    saveFlag = true;
                    //saveChanges();
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
            
            @SuppressWarnings("deprecation")
            private boolean saveFormUploadStageXML(List listesValues, String nineaNumber) {
                log.info("saveFormUploadStageXML Enters");
                //startChanges(); 
                List<String> xmlList = createTagList();
                FormType formType = new FormType_Id(this.getParameters().getFormType()).getEntity();
                String formTypeId = formType.getId().getTrimmedValue();
                StringBuilder uploadXMl = new StringBuilder();
                Date statusUploadDate = new Date();
                Calendar calender = Calendar.getInstance();
                calender.setTime( statusUploadDate);
                int year = calender.get(Calendar.YEAR);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (int i = 0; i < xmlList.size(); i++) {
                    uploadXMl.append("<"+xmlList.get(i)+">"+listesValues.get(i)+"</"+xmlList.get(i)+">");
                }
                String stgUploadXML = "<suspenseIssueList/><formData><customForm><formType>" +formTypeId+"</formType><dateReceived>"+simpleDateFormat.format(statusUploadDate)+"</dateReceived>"+uploadXMl.toString()+"</customForm></formData>";
                
                log.info("Header XML : " + stgUploadXML);
                
                SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
                String formHdrId2 = nineaNumber + statusUploadDate.getDate()+statusUploadDate.getDay();
                System.out.println("formSTGID" +formHdrId2); 
                String formHdrId = nineaNumber + statusUploadDate.getDate()+statusUploadDate.getDay();
                String extFormBatch = nineaNumber + statusUploadDate.getDate();
                boolean saveFlag = false;
            
                psPreparedStatement = createPreparedStatement(CmConstant.STAGING_SQL_QUERY);
                psPreparedStatement.setAutoclose(false);
                psPreparedStatement.bindBigInteger(C1_FORM_UPLD_STG_ID, BigInteger.valueOf(Long.parseLong(formHdrId)));
                psPreparedStatement.bindBigInteger(FORM_BATCH_HDR_ID, BigInteger.valueOf(Long.parseLong(extFormBatch)));
                //psPreparedStatement.bindString(C1_EXT_FORM_SUBM_ID, " ", null);
                psPreparedStatement.bindBigInteger(C1_EXT_FORM_SUBM_SEQ, BigInteger.valueOf(0));
                psPreparedStatement.bindString(C1_FORM_SRCE_CD,ONLINE_ENTRY, null);
                psPreparedStatement.bindString(EXT_FORM_TYPE, EXTERNALFRMTYPE, null);
                psPreparedStatement.bindString(C1_FORM_YEAR, String.valueOf(year) ,null);
                psPreparedStatement.bindString(C1_FORM_UPLD_STG_TYP_CD, CM_REGSTGULPD, null);
//                psPreparedStatement.bindString(TAX_FORM_ID, " ", null);
//                psPreparedStatement.bindString(REG_FORM_ID, " ", null);
                psPreparedStatement.bindString(BUS_OBJ_CD, C1_FORM_UPLOAD_STAGING, null);
                psPreparedStatement.bindString(BO_STATUS_CD, PENDING, null);
                psPreparedStatement.bindString(C1_FORM_PYMNT_FLG, "C1NP", null);
                psPreparedStatement.bindBigInteger(C1_FORM_PYMNT_AMT, BigInteger.valueOf(0));
                psPreparedStatement.bindBigInteger(VERSION, BigInteger.valueOf(5));
                psPreparedStatement.bindDate(STATUS_UPD_DTTM,getSystemDateTime().getDate());
                psPreparedStatement.bindDate(CRE_DTTM, getSystemDateTime().getDate());
                psPreparedStatement.bindString(BO_DATA_AREA, stgUploadXML,null);
                
            
                try {
                    int result = psPreparedStatement.executeUpdate();
                    log.info("Data Insert Count : " + result);
                    saveFlag = true;
                    //saveChanges();
                } catch (Exception exception) {
                    log.info("Exceeption while Inserting records: " + exception.getMessage());
                    exception.printStackTrace();
                } finally {
                    psPreparedStatement.close();
                    psPreparedStatement = null;
                }
                return saveFlag;
            }

 

        }

}
