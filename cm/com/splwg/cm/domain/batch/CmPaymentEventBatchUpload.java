package com.splwg.cm.domain.batch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author Balaganesh M
 *
@BatchJob (modules = {} , softParameters = { @BatchJobSoftParameter (name = obligationType, required = true, type = string)
 *            , @BatchJobSoftParameter (name = typeOfVentilation, required = true, type = string)
 *            , @BatchJobSoftParameter (name = typeOfBatchSettlement, required = true, type = string)
 *            , @BatchJobSoftParameter (name = accountId, required = true, type = string)
 *            , @BatchJobSoftParameter (name = typeEmployerAccount, required = true, type = string)})
 */
public class CmPaymentEventBatchUpload extends CmPaymentEventBatchUpload_Gen {
	
	private final static Logger log = LoggerFactory.getLogger(CmPaymentEventBatchUpload.class);

	public JobWork getJobWork() {

		ThreadWorkUnit unit = new ThreadWorkUnit();
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		unit.addSupplementalData("obligationType", this.getParameters().getObligationType());
		listOfThreadWorkUnit.add(unit);
		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;
	}

	public Class<CmPaymentEventBatchUploadWorker> getThreadWorkerClass() {
		return CmPaymentEventBatchUploadWorker.class;
	}
	@SuppressWarnings("deprecation")
	public static class CmPaymentEventBatchUploadWorker extends CmPaymentEventBatchUploadWorker_Gen {
		private String externalSrcId = null;
		
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new CommitEveryUnitStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			PreparedStatement psPreparedStatement = null;
			QueryIterator<SQLResultRow> result = null;
			Date dat = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(dat);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int date = cal.get(Calendar.DATE);
			com.splwg.base.api.datatypes.Date dtime = new com.splwg.base.api.datatypes.Date(year, month, date);
			try {
				startChanges();
				psPreparedStatement = createPreparedStatement("SELECT * FROM CI_PEVT_DTL_ST WHERE PEVT_STG_ST_FLG = 'C1UP'");
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow payDetailRow = result.next();
					boolean validationFlag = checkFieldValidation(payDetailRow);
						if(!validationFlag){
							String extRefId = payDetailRow.getString("EXT_REFERENCE_ID"); 
							log.info("External Reference Id: " + extRefId);
							String dstRuleValue = null;
							String personId = null;
							String personName = null;
							String accntId = null;
							boolean valExtRefIdFlag = validateExternalRefId(extRefId);
							if(valExtRefIdFlag){
								String[] extItems = extRefId.split("/");
								dstRuleValue = extItems[2];
								personId = getPerIdPer(extItems[1]);
								if(isBlankOrNull(personId)){
									personId = getPersonIdAcctId(this.getParameters().getAccountId());
								}
								personName = getPersonNamePerId(personId);
							} else{
								personId = getPersonIdAcctId(this.getParameters().getAccountId());
								personName = getPersonNamePerId(personId);
								//dsts_rule_value Raphael working
							}
							accntId = getAccountId(personId);
							if(isBlankOrNull(accntId)){
								accntId = this.getParameters().getAccountId();
							}
							if(isBlankOrNull(dstRuleValue)){
								dstRuleValue = " ";
							}
							if(isBlankOrNull(personId)){
								personId = " ";
							}
							if(isBlankOrNull(personName)){
								personName = " ";
							}
							log.info("Account Id :" + accntId);
							log.info("Person Id :" + personId);
							log.info("Person Name :" + personName);
							log.info("DST Rule Value :" + dstRuleValue);
								try {
									startChanges();
									psPreparedStatement = createPreparedStatement("UPDATE CI_PEVT_DTL_ST SET EXT_SOURCE_ID=:EXT_SOURCE_ID,DST_RULE_CD=:DST_RULE_CD, DST_RULE_VALUE=:DST_RULE_VALUE, "
													+ " CURRENCY_CD='XOF', ACCOUNTING_DT=:ACCOUNTING_DT, TENDER_TYPE_CD =:TENDER_TYPE_CD, CUST_ID=:CUST_ID,NAME1=:NAME1,ACCT_ID =:ACCT_ID, "
													+ " PEVT_STG_ST_FLG =:PEVT_STG_ST_FLG WHERE EXT_TRANSMIT_ID = :EXT_TRANSMIT_ID");
		
									psPreparedStatement.bindString("EXT_SOURCE_ID", externalSrcId,null);
									psPreparedStatement.bindString("DST_RULE_CD", this.getParameters().getTypeOfVentilation(),null);
									psPreparedStatement.bindString("DST_RULE_VALUE", dstRuleValue, null);
									psPreparedStatement.bindDate("ACCOUNTING_DT", dtime);
									psPreparedStatement.bindString("TENDER_TYPE_CD",this.getParameters().getTypeOfBatchSettlement(), null);
									psPreparedStatement.bindString("CUST_ID", personId, null);
									psPreparedStatement.bindString("NAME1", personName, null);
									psPreparedStatement.bindString("ACCT_ID",accntId,null);
									psPreparedStatement.bindString("PEVT_STG_ST_FLG", "10", null);
									psPreparedStatement.bindString("EXT_TRANSMIT_ID",payDetailRow.getString("EXT_TRANSMIT_ID"),null);
									int updateCount = psPreparedStatement.executeUpdate();
									System.out.println("updateCount:: " + updateCount);
	
								} catch (Exception exception) {
									log.error("Exception in Updating CI_PEVT_DTL_ST table: " + exception);
								} finally {
									saveChanges();
									psPreparedStatement.close();
									psPreparedStatement = null;
								}
								try {
									startChanges();
									psPreparedStatement = createPreparedStatement("UPDATE CI_PEVT_DTL_ST_CHAR SET EXT_SOURCE_ID=:EXT_SOURCE_ID WHERE EXT_TRANSMIT_ID = :EXT_TRANSMIT_ID");
		
									psPreparedStatement.bindString("EXT_SOURCE_ID", externalSrcId,null);
									psPreparedStatement.bindString("EXT_TRANSMIT_ID", payDetailRow.getString("EXT_TRANSMIT_ID"),null);
									int updateCount = psPreparedStatement.executeUpdate();
									System.out.println("updateCount:: " + updateCount);
		
								} catch (Exception exception) {
									log.error("Exception in Updating CI_PEVT_DTL_ST_CHAR table: " + exception);
								} finally {
									saveChanges();
									psPreparedStatement.close();
									psPreparedStatement = null;
								}
								try {
									startChanges();
									psPreparedStatement = createPreparedStatement("UPDATE CM_PEVT_DTL_UP_ST SET EXT_SOURCE_ID=:EXT_SOURCE_ID,PEVT_STG_ST_UP_FLG = 'Chargé' WHERE EXT_TRANSMIT_ID = :EXT_TRANSMIT_ID");
		
									psPreparedStatement.bindString("EXT_SOURCE_ID", externalSrcId,null);
									psPreparedStatement.bindString("EXT_TRANSMIT_ID", payDetailRow.getString("EXT_TRANSMIT_ID"),null);
									int updateCount = psPreparedStatement.executeUpdate();
									System.out.println("updateCount:: " + updateCount);
		
								} catch (Exception exception) {
									log.error("Exception in Updating CM_PEVT_DTL_UP_ST table: " + exception);
								} finally {
									saveChanges();
									psPreparedStatement.close();
									psPreparedStatement = null;
								}
							} 
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					} finally {
						saveChanges();
						result.close();
						psPreparedStatement = null;
					}
					return true;
		}
		
		private String getAccountId(String personId) {

			PreparedStatement psPreparedStatement = null;
			String accntId = null;
			String accountType = this.getParameters().getTypeEmployerAccount();
			String oblType = this.getParameters().getObligationType();
			log.info("Batch Parameter Account Type : " + accountType);
			log.info("Batch Parameter Obligation Type : " + oblType);
				String accntIdArr[] = accountType.split(",");
				String oblTypeArr[] = oblType.split(",");
				accountType = "'" + StringUtils.join(accntIdArr,"','") + "'";
				oblType = "'" + StringUtils.join(oblTypeArr,"','") + "'";
				psPreparedStatement = createPreparedStatement("select acct.ACCT_ID from ci_per per, ci_acct_per acctper, ci_acct acct, ci_sa sa"
											+" where per.per_id=acctper.per_id "
											+" and acctper.acct_id=acct.acct_id "
											+" and acct.acct_id=sa.acct_id "
											+" and per.per_id = \'"+personId+"\' "
											+" and acct.cust_cl_cd in ("+accountType+") "
											+" and sa.sa_type_cd in ("+oblType+") "
											+" and sa.sa_status_flg = '40' ");
					QueryIterator<SQLResultRow> result = null;
					try {
							startChanges();
							result = psPreparedStatement.iterate();
							while (result.hasNext()) {
							SQLResultRow lookUpValue = result.next();
							accntId = lookUpValue.getString("ACCT_ID");
							}
					} catch (Exception excep) {
						log.error("Exception in getting  getAccountId : " + excep);
					} finally {
						saveChanges();
						psPreparedStatement.close();
						psPreparedStatement = null;
					}
				return accntId;
		}
		
		private String getPersonNamePerId(String personId) {

			PreparedStatement psPreparedStatement = null;
			String personName = null;

			psPreparedStatement = createPreparedStatement("select ENTITY_NAME from CI_PER_NAME where PER_ID =\'"+personId+"\'");
			QueryIterator<SQLResultRow> result = null;

			try {
				startChanges();
				//psPreparedStatement.bindString("PER_ID", personId, null);
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					personName = lookUpValue.getString("ENTITY_NAME");
				}
			} catch (Exception excep) {
				log.error("Exception in getting  getPersonName : " + excep);
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

			psPreparedStatement = createPreparedStatement("select PER_ID from CI_ACCT_PER where ACCT_ID = \'"+accountId+"\'");
			QueryIterator<SQLResultRow> result = null;

			try {
				startChanges();
				//psPreparedStatement.bindString("ACCT_ID", accountId, null);
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					personID = lookUpValue.getString("PER_ID");
				}
			} catch (Exception excep) {
				log.error("Exception in getting  getPersonIdAcctId : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return personID;
		}

		private String getPerIdPer(String perId) {

			PreparedStatement psPreparedStatement = null;
			String perIdPer = null;

			psPreparedStatement = createPreparedStatement("select PER_ID from CI_PER where PER_ID = \'"+perId+"\'");
			QueryIterator<SQLResultRow> result = null;

			try {
				startChanges();
				//psPreparedStatement.bindString("PER_ID", perId, null);
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					perIdPer = lookUpValue.getString("PER_ID");
				}
			} catch (Exception excep) {
				log.error("Exception in getting  getPerIdPer : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return perIdPer;
		}

		private String getExternalSourceIDST(String extTransmitId) {

			PreparedStatement psPreparedStatement = null;
			String extSourceId = null;
			psPreparedStatement = createPreparedStatement(" select EXT_SOURCE_ID from ci_tndr_srce where tndr_srce_type_flg ='VIRP' and BANK_CD IN (select bank_cd from ci_bank_account where ACCOUNT_NBR IN " 
					+ " (select ADHOC_CHAR_VAL from CI_PEVT_DTL_ST_CHAR where CHAR_TYPE_CD = 'CM-ANBR' and EXT_TRANSMIT_ID = \'"+extTransmitId+"\'))");
			
			QueryIterator<SQLResultRow> result = null;
			try {
				startChanges();
				//psPreparedStatement.bindString("EXT_TRANSMIT_ID", extTransmitId, null);
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					extSourceId = lookUpValue.getString("EXT_SOURCE_ID");
				}
			} catch (Exception excep) {
				log.error("Exception in getting  getAccoutDetails : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return extSourceId;
		}
		
		private boolean validateExternalRefId(String extTransmitId) {
			boolean flag = false;
			if(extTransmitId.startsWith("TD")){
				flag = true;
			}
			return flag;
		}
		
		private boolean checkFieldValidation(SQLResultRow payDetailRow) {
			
			String currencyCode = payDetailRow.getString("CURRENCY_CD");
			String extTransmitId  = payDetailRow.getString("EXT_TRANSMIT_ID");
			String tenderAmt  = payDetailRow.getString("TENDER_AMT");
			String checkNbr  = payDetailRow.getString("CHECK_NBR");
			String micrId  = payDetailRow.getString("MICR_ID");
			String extReferenceId  = payDetailRow.getString("EXT_REFERENCE_ID");
			String matchTypeCd  = payDetailRow.getString("MATCH_TYPE_CD"); 
			String matchValue = payDetailRow.getString("MATCH_VAL"); 
			String tenderCtrlId = payDetailRow.getString("TNDR_CTL_ID");
			String payEventId = payDetailRow.getString("PAY_EVENT_ID");
			String pevtProcessId =  payDetailRow.getString("PEVT_PROCESS_ID");
			String paySourceCd = payDetailRow.getString("APAY_SRC_CD"); 
			String extAccntId = payDetailRow.getString("EXT_ACCT_ID");
			String expiryDate = payDetailRow.getString("EXPIRE_DT");
			String entityName = payDetailRow.getString("ENTITY_NAME");
			boolean errorFlag = false;
			String msgCategoryNbr = null;
			String msgNumber = null;
			String messageText = null;
			externalSrcId = null;
			
			if(isBlankOrNull(extTransmitId)){
				 msgCategoryNbr = "9002";
				 msgNumber = "101";
				 messageText = getMessageText(msgCategoryNbr,msgNumber);
				// messageText = "External Transmit Id is empty";
				 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
			} else{
				 externalSrcId = getExternalSourceIDST(extTransmitId);
				 if(isBlankOrNull(externalSrcId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "102";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "External Source Id is empty,There is no data linked to batch settlement,bank code char and rules.";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				 } else if(null==currencyCode || !currencyCode.equalsIgnoreCase("XOF")){
					 msgCategoryNbr = "9002";
					 msgNumber = "103";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					// messageText = "Currency is invalid or empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(isBlankOrNull(tenderAmt)){
					 msgCategoryNbr = "9002";
					 msgNumber = "104";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					//messageText = "Tender Amount is invalid or empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(checkNbr)){
					 msgCategoryNbr = "9002";
					 msgNumber = "105";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Check Number is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(micrId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "106";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "MicrId is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				}  else if(isBlankOrNull(extReferenceId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "107";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "External Reference Id is invalid or empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(matchTypeCd)){
					 msgCategoryNbr = "9002";
					 msgNumber = "108";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Match Type is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(matchValue)){
					 msgCategoryNbr = "9002";
					 msgNumber = "109";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Match Value is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(tenderCtrlId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "110";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Tender Control Id is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(payEventId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "111";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Payment Event Id is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(pevtProcessId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "112";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Payment process Id is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(paySourceCd)){
					 msgCategoryNbr = "9002";
					 msgNumber = "113";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "Payment Source is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(extAccntId)){
					 msgCategoryNbr = "9002";
					 msgNumber = "114";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					 //messageText = "External Account ID is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(expiryDate)){
					 msgCategoryNbr = "9002";
					 msgNumber = "115";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					//messageText = "Expiry Date is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} else if(!isBlankOrNull(entityName)){
					 msgCategoryNbr = "9002";
					 msgNumber = "116";
					 messageText = getMessageText(msgCategoryNbr,msgNumber);
					//messageText = "Entity Name is not empty";
					 errorFlag = updateErrorMessage(payDetailRow,msgCategoryNbr,msgNumber,messageText,externalSrcId);
				} 
			}
			return errorFlag;
		}

		private String getMessageText(String msgCategoryNbr, String msgNumber) {

			PreparedStatement psPreparedStatement = null;
			String messageText = null;

			psPreparedStatement = createPreparedStatement("select MESSAGE_TEXT from CI_MSG_L where MESSAGE_CAT_NBR = \'"+msgCategoryNbr+"\' and MESSAGE_NBR = \'"+msgNumber+"\' and LANGUAGE_CD = 'FRA'");
			QueryIterator<SQLResultRow> result = null;

			try {
				startChanges();
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					messageText = lookUpValue.getString("MESSAGE_TEXT");
				}
			} catch (Exception excep) {
				log.error("Exception in getting GetMessageText: " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return messageText;
		
		}

		private boolean updateErrorMessage(SQLResultRow payDetailRow,String msgCatNbr,String msgNbr, String msgTest, String externalSrcId) {

			PreparedStatement psPreparedStatement = null;
			String extTrasmitId = payDetailRow.getString("EXT_TRANSMIT_ID");
			boolean updateErrorFlag = false;
			if(isBlankOrNull(externalSrcId)){
				externalSrcId = " ";
			}
			if(!isBlankOrNull(extTrasmitId)){
				psPreparedStatement = createPreparedStatement("UPDATE CM_PEVT_DTL_UP_ST SET EXT_SOURCE_ID=:EXT_SOURCE_ID,PEVT_STG_ST_UP_FLG = 'Error',MESSAGE_CAT_NBR =:MESSAGE_CAT_NBR, "
						+ "MESSAGE_NBR =:MESSAGE_NBR, MESSAGE_TEXT=:MESSAGE_TEXT WHERE EXT_TRANSMIT_ID =:EXT_TRANSMIT_ID");
			} else{
				psPreparedStatement = createPreparedStatement("UPDATE CM_PEVT_DTL_UP_ST SET EXT_SOURCE_ID=:EXT_SOURCE_ID,PEVT_STG_ST_UP_FLG = 'Error',MESSAGE_CAT_NBR =:MESSAGE_CAT_NBR, "
						+ "MESSAGE_NBR =:MESSAGE_NBR, MESSAGE_TEXT=:MESSAGE_TEXT WHERE PEVT_DTL_SEQ =:PEVT_DTL_SEQ");
			}
			try {
					startChanges();
					psPreparedStatement.bindString("EXT_SOURCE_ID", externalSrcId, null);
					psPreparedStatement.bindString("MESSAGE_CAT_NBR", msgCatNbr, null);
					psPreparedStatement.bindString("MESSAGE_NBR", msgNbr, null);	
					psPreparedStatement.bindString("MESSAGE_TEXT", msgTest, null);
				if(!isBlankOrNull(extTrasmitId)){
					psPreparedStatement.bindString("EXT_TRANSMIT_ID", extTrasmitId, null);
				} else{
					psPreparedStatement.bindString("PEVT_DTL_SEQ", payDetailRow.getString("PEVT_DTL_SEQ"), null);
				}
				
				int result = psPreparedStatement.executeUpdate();
				System.out.println("Update Error Message CM_PEVT_DTL_UP_ST Result Number of rows" +result);
				log.info("Update Error message" +result);
				updateErrorFlag = true;
				
			} catch (Exception excep) {
				log.error("Exception in updating updateErrorMessage : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
			return updateErrorFlag;
		}
	}

}

