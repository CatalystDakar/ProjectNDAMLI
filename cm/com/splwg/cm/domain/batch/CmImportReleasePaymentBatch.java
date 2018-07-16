package com.splwg.cm.domain.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

import oracle.xml.xquery.update.compiler.Goto;

/**
 * @author Divakar
 *	
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = maxErrors, type = string)})
 */
public class CmImportReleasePaymentBatch extends CmImportReleasePaymentBatch_Gen {

    private static final Logger logger = LoggerFactory.getLogger(CmImportReleasePaymentBatch.class);
    public static final CmImportReleaseDTO CmImportReleaseDTO = null;


    public JobWork getJobWork() {
        ThreadWorkUnit unit = new ThreadWorkUnit();
        List < ThreadWorkUnit > listOfThreadWorkUnit = new ArrayList < ThreadWorkUnit > ();

        unit.addSupplementalData("maxErrors", this.getParameters().getMaxErrors());
        //unit.addSupplementalData("typeOfVentilation", this.getParameters().getTypeOfVentilation());
        //unit.addSupplementalData("typeOfBatchSettlement", this.getParameters().getTypeOfBatchSettlement());
        //unit.addSupplementalData("accountId", this.getParameters().getAccountId());
        //unit.addSupplementalData("typeEmployerAccount", this.getParameters().getTypeEmployerAccount());

        listOfThreadWorkUnit.add(unit);

        JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
        System.out.println("######################## Terminate JobWorker ############################");
        return jobWork;
    }

    public Class < CmImportReleasePaymentBatchWorker > getThreadWorkerClass() {
        return CmImportReleasePaymentBatchWorker.class;
    }

    public static class CmImportReleasePaymentBatchWorker extends CmImportReleasePaymentBatchWorker_Gen {

        private String externalSrcId1;
        public String checkNumber;
        public String extTransmitId;
        public String personName;
        public String custId;
        public int count=0;
        public String testPayTenderId;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String accDate = dateFormat.format(date);
        public ThreadExecutionStrategy createExecutionStrategy() {
            // TODO Auto-generated method stub
            return new CommitEveryUnitStrategy(this);
        }

        @SuppressWarnings({
            "deprecation",
            "null"
        })
        public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
            // TODO Auto-generated method stub
            String msgCategoryNbr = null;
            String msgNumber = null;
            String messageText = null;
            String personId = null;
            String personName = null;
            String accntId = null;
            externalSrcId1 = null;
            PreparedStatement psPreparedStatement = null;
            QueryIterator < SQLResultRow > result = null;
            try {
                startChanges();
                psPreparedStatement = createPreparedStatement(" SELECT * FROM CM_CHEQ_REJET_ST where LOWER(CHEQ_RJT_ST_FLG) = LOWER('Imported') or LOWER(CHEQ_RJT_ST_FLG) = LOWER('IMPORTÉ') ");
           
                result = psPreparedStatement.iterate();

                while (result.hasNext()) {
                    SQLResultRow payDetailRow = result.next();
                    
                     String tenderTypeCd = payDetailRow.getString("TENDER_TYPE_CD");
                    checkNumber = payDetailRow.getString("CHECK_NBR");
                    extTransmitId = payDetailRow.getString("EXT_TRANSMIT_ID");
                    //String checkSeqNum = payDetailRow.getString("CHEQ_RJT_DTL_SEQ");
                    
                    
                    if(checkNumber != null && !checkNumber.isEmpty()) {

                        CmImportReleaseDTO cto = getDeatilsFromTenderSource();
                        
                       String getAcctId = cto.getAcctId();
                       String getPayEventId = cto.getPayEventId();
                       String tndCtrId=cto.getTenderCtrlId();
                       String tndCiPayid=cto.getTenderIdCiPay();
                       
                        if((isBlankOrNull(getAcctId)) ||(isBlankOrNull (getPayEventId)) || (isBlankOrNull (tndCtrId)) || (isBlankOrNull(tndCiPayid)))
                        { if(count==0) {
                        	PreparedStatement psPreparedStatementCiChecknull = null;
                        	psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='53', MESSAGE_TEXT = 'Le numéro de chèque '\'" + checkNumber + "\'' n existe pas dans les lots de règlements – Une vérification manuelle est nécessaire' where CHECK_NBR = \'" + checkNumber + "\' ");
                        	int updateCount = psPreparedStatementCiChecknull.executeUpdate();
                            System.out.println("updateCount:: " + updateCount);
                        }else if(count>1) {
                        	PreparedStatement psPreparedStatementCiChecknull = null;
                        	psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='54', MESSAGE_TEXT = 'Le numéro de chèque '\'" + checkNumber + "\'' est présent sur plusieurs événements de paiements. Une vérification manuelle est nécessaire' where CHECK_NBR = \'" + checkNumber + "\' ");
                           int updateCount = psPreparedStatementCiChecknull.executeUpdate();
                        System.out.println("updateCount:: " + updateCount);
                      
                    }
                      
                        } else 
                            if(count==1){
                    
                        	updateCmCheckReg(cto); 
                    
                }

                    }
                    else {

                        psPreparedStatement = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='54' , MESSAGE_TEXT = 'Le numéro de chèque est vide pour l ID de transmission externe  '\'" + extTransmitId + "\''  ' where EXT_TRANSMIT_ID = \'" + extTransmitId + "\' ");
                        int updateCount =  psPreparedStatement.executeUpdate();
                        System.out.println("updateCount:: " + updateCount);
                    }
                }
               

            } catch (Exception exception) {
                logger.error("Exception in Updating CM_PEVT_DTL_UP_ST table: ");
            } finally {
                saveChanges();
                result.close();
                
                psPreparedStatement.close();
                psPreparedStatement = null;
            }
            return true;
        }




        private String getPersonNamePerId(String personId) {

            PreparedStatement psPreparedStatement = null;
            String personName = null;

            psPreparedStatement = createPreparedStatement("select ENTITY_NAME from CI_PER_NAME where PER_ID =:PER_ID");
            QueryIterator < SQLResultRow > result = null;

            try {
                startChanges();
                psPreparedStatement.bindString("PER_ID", personId, null);
                result = psPreparedStatement.iterate();
                while (result.hasNext()) {
                    SQLResultRow lookUpValue = result.next();
                    personName = lookUpValue.getString("ENTITY_NAME");
                }
            } catch (Exception excep) {
                logger.error("Exception in getting  getPersonName : " + excep);
            } finally {
                saveChanges();
                psPreparedStatement.close();
                psPreparedStatement = null;
            }
            return personName;
        }

        private String getPersonIdAcctId(String accountId) {

            PreparedStatement psPreparedStatement = null;
            String personID = null;

            psPreparedStatement = createPreparedStatement("select PER_ID from CI_ACCT_PER where ACCT_ID = :ACCT_ID");
            QueryIterator < SQLResultRow > result = null;

            try {
                startChanges();
                psPreparedStatement.bindString("ACCT_ID", accountId, null);
                result = psPreparedStatement.iterate();
                while (result.hasNext()) {
                    SQLResultRow lookUpValue = result.next();
                    personID = lookUpValue.getString("PER_ID");
                }
            } catch (Exception excep) {
                logger.error("Exception in getting  getPersonIdAcctId : " + excep);
            } finally {
                saveChanges();
                psPreparedStatement.close();
                psPreparedStatement = null;
            }
            return personID;

        }

        public void updateCmCheckReg(CmImportReleaseDTO cmImportReleaseDTO) {
        	 PreparedStatement psPreparedStatementCmPayUpdate =  psPreparedStatementCmPayUpdate = createPreparedStatement("UPDATE CM_CHEQ_REJET_ST SET  EXT_SOURCE_ID=:EXT_SOURCE_ID, PROCESS_DTTM=:PROCESS_DTTM, ACCOUNTING_DT=:ACCOUNTING_DT, CUST_ID=:CUST_ID,NAME1=:NAME1, CHEQ_RJT_ST_FLG='CHARGE',TNDR_CTL_ID =:TNDR_CTL_ID, ACCT_ID =:PAYOR_ACCT_ID, " 
                     + " PAY_EVENT_ID=:PAY_EVENT_ID , PAY_TENDER_ID=:PAY_TENDER_ID, MESSAGE_CAT_NBR=null, MESSAGE_NBR=null ,MESSAGE_TEXT=null WHERE CHECK_NBR =:CHECK_NBR");
             
            try{             
        	
            	externalSrcId1 = getExternalSourceIDST(extTransmitId);
                if(!isEmptyOrNull(externalSrcId1)) 
                {
                	            psPreparedStatementCmPayUpdate.bindString("ACCOUNTING_DT", accDate, null);
                                  psPreparedStatementCmPayUpdate.bindString("PROCESS_DTTM", accDate, null);
                                  psPreparedStatementCmPayUpdate.bindString("CHECK_NBR", checkNumber, null);
                                  psPreparedStatementCmPayUpdate.bindString("EXT_SOURCE_ID", externalSrcId1, null);
                                  custId = getPersonIdAcctId(cmImportReleaseDTO.getAcctId());
                                  personName = getPersonNamePerId(custId);
                                  psPreparedStatementCmPayUpdate.bindString("CUST_ID", custId, null);
                                  psPreparedStatementCmPayUpdate.bindString("NAME1", personName, null);
                                  psPreparedStatementCmPayUpdate.bindString("TNDR_CTL_ID", cmImportReleaseDTO.getTenderCtrlId(), null);
                                  psPreparedStatementCmPayUpdate.bindString("PAYOR_ACCT_ID", cmImportReleaseDTO.getAcctId(), null);
                                  psPreparedStatementCmPayUpdate.bindString("PAY_EVENT_ID", cmImportReleaseDTO.getPayEventId(), null);
                                  psPreparedStatementCmPayUpdate.bindString("PAY_TENDER_ID", cmImportReleaseDTO.getTenderIdCiPay(), null);
                                  int updateCount = psPreparedStatementCmPayUpdate.executeUpdate();
                                  System.out.println("updateCount:: " + updateCount);


                }else{
                
                	PreparedStatement psPreparedStatementCiChecknull = null;
                	psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='51' , MESSAGE_TEXT = 'Le '\'" + extTransmitId + "\'' ext_source_id du EXT_TRANSMIT_ID  ne peut-être retrouvé - Il est nécessaire de faire une vérification manuelle' where CHECK_NBR = \'" + checkNumber + "\' ");
                    int updateCount = psPreparedStatementCiChecknull.executeUpdate();                                                                                                                                                                                                                                                              
                    System.out.println("updateCount:: " + updateCount);
                    
           
                }
            }catch (Exception exception) {
                logger.error("Exception in Updating CM_PEVT_DTL_UP_ST table: Field value is null");
            } finally {
            	saveChanges();
            	psPreparedStatementCmPayUpdate.close();
            }
            
        }


        public CmImportReleaseDTO getDeatilsFromTenderSource() {
        	 CmImportReleaseDTO importDto = new CmImportReleaseDTO();
        	 QueryIterator < SQLResultRow > resultCiPayCount = null;
             List<SQLResultRow> resultCiPay = null;
        	 PreparedStatement psPreparedStatementCiPay = null;
        	 PreparedStatement psPreparedStatementCiCount = null;
        	 int countl=0;
try{
           
            //psPreparedStatementCiPay =  createPreparedStatement("SELECT * FROM ci_pay_tndr where CHECK_NBR =:CHECK_NBR ","select");
            // psPreparedStatementCiPay.bindString("CHECK_NBR",checkNumber,null);
            psPreparedStatementCiPay = createPreparedStatement(" SELECT * FROM ci_pay_tndr where CHECK_NBR= \'" + checkNumber + "\' ");
            psPreparedStatementCiCount =  createPreparedStatement(" SELECT * FROM ci_pay_tndr where CHECK_NBR= \'" + checkNumber + "\'  ");
            
            
            
            resultCiPayCount = psPreparedStatementCiCount.iterate();	
           
            if(resultCiPayCount.hasNext()){
             while(resultCiPayCount.hasNext()) {
            	 countl++;
                resultCiPayCount.next();
                     }
                }
            psPreparedStatementCiCount.close();
            
            count=countl;
            if(countl==1)   
            { 
             	resultCiPay = psPreparedStatementCiPay.list();
                SQLResultRow payDetailRowCiPay = resultCiPay.get(0);
                importDto.setTenderIdCiPay(payDetailRowCiPay.getString("PAY_TENDER_ID"));
                importDto.setTenderCtrlId(payDetailRowCiPay.getString("TNDR_CTL_ID"));
                importDto.setPayEventId(payDetailRowCiPay.getString("PAY_EVENT_ID"));
                 testPayTenderId = payDetailRowCiPay.getString("PAY_TENDER_ID");
                System.out.println(testPayTenderId);
                importDto.setAcctId(payDetailRowCiPay.getString("PAYOR_ACCT_ID"));
                importDto.setTenderAmt(payDetailRowCiPay.getString("TENDER_AMT"));
             //String chkRjtSeq = payDetailRowCiPay.getString("CHEQ_RJT_DTL_SEQ");
            //String custId = payDetailRowCiPay.getString("CUST_ID");
            //String name1 = payDetailRowCiPay.getString("NAME1");
            
            }
           
}
        catch(Exception e)
               { 
        	logger.error("Exception in Updating CM_PEVT_DTL_UP_ST table: Check number in Ci_FT table is null");
        	} finally {
                saveChanges();
                
                
                psPreparedStatementCiPay.close();
                }
             return importDto;
             
            }
        String getExternalSourceIDST(String extTransmitId) {

            PreparedStatement psPreparedStatement = null;
            String extSourceId = null;
            psPreparedStatement = createPreparedStatement("select ext_source_id from ci_tndr_srce where tndr_source_cd in(select tndr_source_cd from ci_tndr_ctl where tndr_ctl_id in(select tndr_ctl_id from ci_pay_tndr where pay_tender_id = \'" + testPayTenderId + "\' ))");
            QueryIterator < SQLResultRow > result = null;
            try {
                startChanges();
                result = psPreparedStatement.iterate();
                while (result.hasNext()) {
                    SQLResultRow lookUpValue = result.next();
                    extSourceId = lookUpValue.getString("EXT_SOURCE_ID");
                }
            } catch (Exception excep) {
                logger.error("Exception in getting  getAccoutDetails : " + excep);
            } finally {
                saveChanges();
                psPreparedStatement.close();
                psPreparedStatement = null;
            }
            return extSourceId;
        }
    }


}