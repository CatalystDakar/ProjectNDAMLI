package com.splwg.cm.domain.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * @author Divakar
 *	
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = maxErrors, type = string)})
 */
public class CmImportReleasePaymentBatch extends CmImportReleasePaymentBatch_Gen {

	private static final Logger logger = LoggerFactory.getLogger(CmImportReleasePaymentBatch_Gen.class);

	public JobWork getJobWork() {
		ThreadWorkUnit unit = new ThreadWorkUnit();
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		unit.addSupplementalData("maxErrors", this.getParameters().getMaxErrors());
		listOfThreadWorkUnit.add(unit);

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;
	}
 
	public Class < CmImportReleasePaymentBatchWorker > getThreadWorkerClass() {
		return CmImportReleasePaymentBatchWorker.class;
	}

	public static class CmImportReleasePaymentBatchWorker extends CmImportReleasePaymentBatchWorker_Gen {

		private String externalSrcId1 = null, finalTenderId = null, acctNbr = null, tenderAmount = null, testPayTenderId = null, bankCd = null, custId = null, personName = null, checkNumber = null, extTransmitId = null;
		public int count = 0;
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date date = new Date();
		String accDate = dateFormat.format(date);
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new CommitEveryUnitStrategy(this);
		}

		@SuppressWarnings("deprecation")
		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			PreparedStatement psPreparedStatement = null;
			QueryIterator<SQLResultRow> result = null;
			try {
				startChanges();
				psPreparedStatement = createPreparedStatement(" SELECT * FROM CM_CHEQ_REJET_ST where LOWER(CHEQ_RJT_ST_FLG) = LOWER('Imported') or LOWER(CHEQ_RJT_ST_FLG) = LOWER('IMPORTÉ') ");
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow payDetailRow = result.next();
					checkNumber = payDetailRow.getString("CHECK_NBR");
					tenderAmount = payDetailRow.getString("TENDER_AMT");
					extTransmitId = payDetailRow.getString("EXT_TRANSMIT_ID");
					acctNbr = payDetailRow.getString("ACCOUNT_NBR");
					if(acctNbr !=null && !acctNbr.isEmpty())
					{
					if (checkNumber != null && !checkNumber.isEmpty()) {
						CmImportReleaseDTO cmImportReleaseDTO = getDeatilsFromTenderSource();
						String getAcctId = cmImportReleaseDTO.getAcctId();
						String getPayEventId = cmImportReleaseDTO.getPayEventId();
						String tndCtrId = cmImportReleaseDTO.getTenderCtrlId();
						String tndCiPayid = cmImportReleaseDTO.getTenderIdCiPay();
						if ((isBlankOrNull(getAcctId)) || (isBlankOrNull(getPayEventId)) || (isBlankOrNull(tndCtrId))|| (isBlankOrNull(tndCiPayid))) {
							if (count == 0) {
								PreparedStatement psPreparedStatementCiChecknull = null;
								psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='53', MESSAGE_TEXT = 'Le numéro de chèque '\'" + checkNumber + "\'' n existe pas dans les lots de règlements avec le même montant - Une vérification manuelle est nécessaire' where CHECK_NBR = \'" + checkNumber + "\' ");
								int updateCount = psPreparedStatementCiChecknull.executeUpdate();
								logger.info("updateCount:Number of Rows with Checknumber from table CM_CHEQ_REJET_ST  is not in CI_PAY_TNDR Table: " + updateCount);
							} else if (count > 1) {
								PreparedStatement psPreparedStatementCiChecknull = null;
								
										bankCd=getBankCd(acctNbr);	
										if(isNullOrBlank(bankCd))
										{
											
										    	SQLResultRow resultBanCd = null;
										    	PreparedStatement psPreparedStatementCiBankCdCheck = null;
										    	//PreparedStatement msgRetrival = null;
										    	//String msgTextTest = null;
										    	//msgRetrival = createPreparedStatement(" select MESSAGE_TEXT from ci_msg_l where message_cat_nbr = '9002' and message_nbr = '118' and language_cd = 'FRA' ");
										    	//result=	msgRetrival.firstRow();
										    	
													
													//msgTextTest = result.getString("MESSAGE_TEXT");							
													
													
										    	
										    	psPreparedStatementCiBankCdCheck = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='118' , MESSAGE_TEXT = (select message_text from ci_msg_l where message_cat_nbr ='9002' and message_nbr ='118' and language_cd ='FRA') where CHECK_NBR = \'" + checkNumber + "\' ");
															
												 // psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='118' , MESSAGE_TEXT = '\'"+msgTextTest+"\' where CHECK_NBR = \'" + checkNumber + "\' ");
												int updateCount = psPreparedStatementCiBankCdCheck.executeUpdate();
												logger.info("updateCount:: " + updateCount);
												//msgRetrival.close();

										    }
										else {
										
								psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='54', MESSAGE_TEXT = 'Le numéro de chèque '\'" + checkNumber + "\''  de la banque  '\'" + bankCd + "\''   est présent sur plusieurs événements de paiements' where CHECK_NBR = \'" + checkNumber + "\' ");
								int updateCount = psPreparedStatementCiChecknull.executeUpdate();
								logger.info("updateCount:Number of Rows with Checknumber from table in CI_PAY_TNDR has more than one Entry or result: " + updateCount);
										}
							}

						} else if (count == 1) {
							updateCmCheckReg(cmImportReleaseDTO);
						}
					} else {
						psPreparedStatement = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='54' , MESSAGE_TEXT = 'Le numéro de chèque est vide pour l ID de transmission externe  '\'" + extTransmitId + "\''  ' where EXT_TRANSMIT_ID = \'" + extTransmitId + "\' ");
						int updateCount = psPreparedStatement.executeUpdate();
						logger.info("updateCount:Checknumber in CM_CHEQ_REJET_ST is empty: " + updateCount);
					}
				}
					else
					{
						psPreparedStatement = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='54' , MESSAGE_TEXT = 'No Account Number found' where EXT_TRANSMIT_ID = \'" + extTransmitId + "\' ");
						int updateCount = psPreparedStatement.executeUpdate();
						logger.info("updateCount:Checknumber in CM_CHEQ_REJET_ST is empty: " + updateCount);
					}
					
					
			} 
			}catch (Exception exception) {
				logger.error("Exception in Updating CM_CHEQ_REJET_ST table: ");
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
			QueryIterator<SQLResultRow> result = null;

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
		
		
		private String getBankCd(String acctNbr) {

			PreparedStatement psPreparedStatement = null;
			String bankCd = null;
			int count2 = 0;
			psPreparedStatement = createPreparedStatement("select DISTINCT BANK_CD from ci_bank_account where account_nbr  =:ACCOUNT_NBR");
			QueryIterator<SQLResultRow> result = null;

			try {
				startChanges();
				psPreparedStatement.bindString("ACCOUNT_NBR", acctNbr, null);
				result = psPreparedStatement.iterate();
				
				while (result.hasNext()) {
					
					SQLResultRow lookUpValue = result.next();
					bankCd = lookUpValue.getString("BANK_CD");
					count2++;
					logger.info(count); 
					if(count2>1)
					{
						logger.error("Back_Cd return two Bank Cd");
					}
					
				}
			} catch (Exception excep) {
				logger.error("Exception in getting  bankCd : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
				if(count2>1)
				{logger.error("Back_Cd return two Bank Cd");}
			}
			if(count2==1)
			{ 
				bankCd=bankCd.trim();
			return bankCd;
			}
			else return null;
		}
		
		
			
		

		private String getPersonIdAcctId(String accountId) {

			PreparedStatement psPreparedStatement = null;
			String personID = null;
			psPreparedStatement = createPreparedStatement("select PER_ID from CI_ACCT_PER where ACCT_ID = :ACCT_ID");
			QueryIterator<SQLResultRow> result = null;

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
					+ " PAY_EVENT_ID=:PAY_EVENT_ID, BANK_CD=:BANK_CD, PAY_TENDER_ID=:PAY_TENDER_ID, MESSAGE_CAT_NBR=null, MESSAGE_NBR=null ,MESSAGE_TEXT=null WHERE CHECK_NBR =:CHECK_NBR");
			try {
				    bankCd = getBankCd(acctNbr);
				    if(!isEmptyOrNull(bankCd))
				    {
				    externalSrcId1 = getExternalSourceIDST(extTransmitId);
				   if (!isEmptyOrNull(externalSrcId1)) {
					psPreparedStatementCmPayUpdate.bindString("ACCOUNTING_DT", accDate, null);
					psPreparedStatementCmPayUpdate.bindString("PROCESS_DTTM", accDate, null);
					psPreparedStatementCmPayUpdate.bindString("CHECK_NBR", checkNumber, null);
					psPreparedStatementCmPayUpdate.bindString("EXT_SOURCE_ID", externalSrcId1, null);
					custId = getPersonIdAcctId(cmImportReleaseDTO.getAcctId());
					personName = getPersonNamePerId(custId);
					psPreparedStatementCmPayUpdate.bindString("BANK_CD", bankCd, null);
					psPreparedStatementCmPayUpdate.bindString("CUST_ID", custId, null);
					psPreparedStatementCmPayUpdate.bindString("NAME1", personName, null);
					psPreparedStatementCmPayUpdate.bindString("TNDR_CTL_ID", cmImportReleaseDTO.getTenderCtrlId(),null);
					psPreparedStatementCmPayUpdate.bindString("PAYOR_ACCT_ID", cmImportReleaseDTO.getAcctId(), null);
					psPreparedStatementCmPayUpdate.bindString("PAY_EVENT_ID", cmImportReleaseDTO.getPayEventId(), null);
					psPreparedStatementCmPayUpdate.bindString("PAY_TENDER_ID", cmImportReleaseDTO.getTenderIdCiPay(),null);
					int updateCount = psPreparedStatementCmPayUpdate.executeUpdate();
					logger.info("updateCount:: " + updateCount);

				} else {

					PreparedStatement psPreparedStatementCiChecknull = null;
					psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='51' , MESSAGE_TEXT = 'Le '\'" + extTransmitId + "\'' ext_source_id du EXT_TRANSMIT_ID  ne peut-être retrouvé - Il est nécessaire de faire une vérification manuelle' where CHECK_NBR = \'" + checkNumber + "\' ");
					int updateCount = psPreparedStatementCiChecknull.executeUpdate();
					logger.info("updateCount:: " + updateCount);

				}
			} 
				    else{
				    	SQLResultRow result = null;
				    	PreparedStatement psPreparedStatementCiChecknull = null;
				    	//PreparedStatement msgRetrival = null;
				    	//String msgTextTest = null;
				    	//msgRetrival = createPreparedStatement(" select MESSAGE_TEXT from ci_msg_l where message_cat_nbr = '9002' and message_nbr = '118' and language_cd = 'FRA' ");
				    	//result=	msgRetrival.firstRow();
				    	
							
							//msgTextTest = result.getString("MESSAGE_TEXT");							
							
							
				    	
				    			  psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='118' , MESSAGE_TEXT = (select message_text from ci_msg_l where message_cat_nbr ='9002' and message_nbr ='118' and language_cd ='FRA') where CHECK_NBR = \'" + checkNumber + "\' ");
									
						 // psPreparedStatementCiChecknull = createPreparedStatement(" UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG = 'Error', MESSAGE_CAT_NBR ='9002' , MESSAGE_NBR ='118' , MESSAGE_TEXT = '\'"+msgTextTest+"\' where CHECK_NBR = \'" + checkNumber + "\' ");
						int updateCount = psPreparedStatementCiChecknull.executeUpdate();
						logger.info("updateCount:: " + updateCount);
						//msgRetrival.close();

				    }
			}

			catch (Exception exception) {
				logger.error("Exception in Updating CM_CHEQ_REJET table: Field value is null");
			} finally {
				saveChanges();
				psPreparedStatementCmPayUpdate.close();
			}

		}

		@SuppressWarnings("null")
		public CmImportReleaseDTO getDeatilsFromTenderSource() {
			CmImportReleaseDTO importDto = new CmImportReleaseDTO();
			QueryIterator<SQLResultRow> resultCiPayCount = null;
			QueryIterator<SQLResultRow> resultCiPayMore = null;			
			List<SQLResultRow> resultCiPay = null;
			PreparedStatement psPreparedStatementCiPay = null;
			PreparedStatement psPreparedStatementCiCount = null;
			PreparedStatement psPreparedStatementCiFinalPay = null;
			List<SQLResultRow> resultCiPayFinal = null;
			int countl = 0;
			try {

                
				psPreparedStatementCiPay = createPreparedStatement(" SELECT * FROM ci_pay_tndr where CHECK_NBR= \'" + checkNumber + "\' and  TENDER_AMT = \'" + tenderAmount + "\'  ");
				psPreparedStatementCiCount = createPreparedStatement(" SELECT * FROM ci_pay_tndr where CHECK_NBR= \'" + checkNumber + "\'and  TENDER_AMT = \'" + tenderAmount + "\' ");
				resultCiPayCount = psPreparedStatementCiCount.iterate();
				if (resultCiPayCount.hasNext()) {
					while (resultCiPayCount.hasNext()) {
						countl++;
						resultCiPayCount.next();
					}
				}
			//	psPreparedStatementCiCount.close();
				count = countl;
				if (countl == 1) {
					resultCiPay = psPreparedStatementCiPay.list();
					SQLResultRow payDetailRowCiPay = resultCiPay.get(0);
					importDto.setTenderIdCiPay(payDetailRowCiPay.getString("PAY_TENDER_ID"));
					importDto.setTenderCtrlId(payDetailRowCiPay.getString("TNDR_CTL_ID"));
					importDto.setPayEventId(payDetailRowCiPay.getString("PAY_EVENT_ID"));
					testPayTenderId = payDetailRowCiPay.getString("PAY_TENDER_ID");
					importDto.setAcctId(payDetailRowCiPay.getString("PAYOR_ACCT_ID"));
					importDto.setTenderAmt(payDetailRowCiPay.getString("TENDER_AMT"));
				}
				else if (countl > 1)
				{
					
					String bankKeyForNext = null;
					String bankValueForNext = null;
					/*resultCiPayMore = psPreparedStatementCiPay.iterate();
					List <String> payTenderIdlist = null ;
						while (resultCiPayMore.hasNext()) {
					         
							SQLResultRow resultCiPayMoreList = resultCiPayMore.next();
					         String payTenderId = resultCiPayMoreList.getString("PAY_TENDER_ID");							
							  payTenderIdlist.add(payTenderId);
							
						}
					     */         
					
					Map<String, String> bankNameCount = new HashMap<String, String>();
					Map<String, String> bankCdCount = new HashMap<String, String>();
					PreparedStatement PreparedStatementBankCd = null;
					QueryIterator<SQLResultRow> resultBankCd = null;		
					PreparedStatementBankCd = createPreparedStatement(" select b.srch_char_val as BANKNAME , count (b.srch_char_val) as BANKCOUNT  from ci_pay_tndr a, ci_pay_tndr_char b where a.pay_tender_id =b.pay_tender_id and b.char_type_cd ='CM-BANQT' and a.check_nbr = \'" + checkNumber + "\' and TENDER_AMT = \'" + tenderAmount + "\' and tender_type_cd ='CHEC' and tndr_status_flg ='25'group by b.SRCH_CHAR_VAL ");
					resultBankCd = PreparedStatementBankCd.iterate();
					if (resultBankCd.hasNext())
						
					{	
					while (resultBankCd.hasNext()) 
					{
						SQLResultRow resultBankCdList = resultBankCd.next();
						String bankName = resultBankCdList.getString("BANKNAME");
						String bankCount = resultBankCdList.getString("BANKCOUNT");
						if (!isBlankOrNull(bankName))
						{
						bankNameCount.put(bankName, bankCount);
						}
						
				}
					resultBankCd.close();
					boolean countCheck = true ;
					for(Map.Entry<String,String> bankKey : bankNameCount.entrySet())
					{
						
					if(bankKey.getValue().equals("1"))
					{
					    bankKeyForNext = bankKey.getValue();
						bankValueForNext = bankKey.getKey();	
						
							
					}
					else
					{
						
					count=2;
					countCheck=false;
					break;
					
					}
					
					
					
					}
				if(countCheck)
					
				{
					bankCd = getBankCd(acctNbr);
					
				/* need to put New Query TO get map value as Paytender id and bank mame, then need to compare it with Bank CD from the Cehcq Rejet Stat*/
				
					PreparedStatement PreparedStatementBankCdCustomTable = null;
					QueryIterator<SQLResultRow> resultBankCdCustomtTable = null;		
					PreparedStatementBankCdCustomTable = createPreparedStatement(" select distinct bank_cd,Pay_TENDER_ID from  ci_pay_tndr a,  ci_tndr_ctl b, ci_tndr_srce c where a.tndr_ctl_id=b.tndr_ctl_id and b.tndr_source_cd=c.TNDR_SOURCE_CD and a.pay_tender_id in (select  b. pay_tender_id  from ci_pay_tndr a, ci_pay_tndr_char b where a.pay_tender_id =b.pay_tender_id and b.char_type_cd ='CM-BANQT' and a.check_nbr =\'" + checkNumber + "\' and TENDER_AMT = \'" + tenderAmount + "\' and tender_type_cd ='CHEC' and tndr_status_flg ='25') ");
					resultBankCdCustomtTable = PreparedStatementBankCdCustomTable.iterate();
					if (resultBankCdCustomtTable.hasNext())
						
					{	
					     while (resultBankCdCustomtTable.hasNext()) 
					     {
						SQLResultRow resultBankCdList = resultBankCdCustomtTable.next();
						String bankName = resultBankCdList.getString("BANK_CD");
						bankName=bankName.trim();
						String payTenderNumberCd = resultBankCdList.getString("PAY_TENDER_ID");
				
						if (!isBlankOrNull(payTenderNumberCd))
						{
							bankCdCount.put(payTenderNumberCd,bankName );
						}
					
						
					     }
				
					     
					     
					     for(Map.Entry<String,String> TenderKey : bankCdCount.entrySet())
							{
								
							if(TenderKey.getValue().contentEquals(bankCd))
							{
							    bankKeyForNext = TenderKey.getValue();
							    finalTenderId = TenderKey.getKey();	
								break;
									
							}
							
					     
				                 }
                if(!isBlankOrNull(finalTenderId))
                {
                	
                	psPreparedStatementCiFinalPay = createPreparedStatement(" SELECT * FROM ci_pay_tndr where PAY_TENDER_ID = \'" + finalTenderId + "\' " , "select");
                	resultCiPayFinal = psPreparedStatementCiFinalPay.list();
                	
                	SQLResultRow payDetailRowCiPay = resultCiPayFinal.get(0);
                	testPayTenderId = payDetailRowCiPay.getString("PAY_TENDER_ID");// for Geting External Id
					importDto.setTenderIdCiPay(payDetailRowCiPay.getString("PAY_TENDER_ID"));
					importDto.setTenderCtrlId(payDetailRowCiPay.getString("TNDR_CTL_ID"));
					importDto.setPayEventId(payDetailRowCiPay.getString("PAY_EVENT_ID"));
					importDto.setAcctId(payDetailRowCiPay.getString("PAYOR_ACCT_ID"));
					importDto.setTenderAmt(payDetailRowCiPay.getString("TENDER_AMT"));
                	count=1;
                	
                }
				
				}
					}
				
					}
				}
			} catch (Exception e) {
				logger.error("Exception in Updating CM_CHEQ_REJET_ST table: Values from CmImportReleaseDTO is null");
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
			QueryIterator<SQLResultRow> result = null;
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