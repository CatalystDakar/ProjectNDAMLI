package com.splwg.cm.domain.batch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.cm.domain.customMessages.CmMessageRepository1001;
import com.splwg.cm.domain.customMessages.CmMessageRepository90002;
import com.splwg.cm.domain.customMessages.CmMessageRepository90002.Messages;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author Balaganesh M
 *
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = tenderSourceExclusion, required = true, type = string)
 *            , @BatchJobSoftParameter (name = tenderSourceType, required = true, type = string)})
 */
public class CmExportBankChequeBatch extends CmExportBankChequeBatch_Gen {

	private final static Logger log = LoggerFactory.getLogger(CmExportBankChequeBatch.class);
	
	@Override
	public void validateSoftParameters(boolean isNewRun) {
		System.out.println("Tender Source Type: " + this.getParameters().getTenderSourceType());
		System.out.println("Tender Source Exclusion" + this.getParameters().getTenderSourceExclusion());
		System.out.println("Batch Number" + this.getBatchNumber()); 
	}
	
	public JobWork getJobWork() {

		ThreadWorkUnit unit = new ThreadWorkUnit();
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		unit.addSupplementalData("tenderSourceType", this.getParameters().getTenderSourceType());
		listOfThreadWorkUnit.add(unit);
		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;
	}

	public Class<CmExportBankChequeBatchWorker> getThreadWorkerClass() {
		return CmExportBankChequeBatchWorker.class;
	}

	public static class CmExportBankChequeBatchWorker extends CmExportBankChequeBatchWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			return new CommitEveryUnitStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			
			PreparedStatement psPreparedStatement = null;
			QueryIterator<SQLResultRow> result = null;
			CmExportBankCheckDTO cmExportBankCheckDTO = null;
			boolean returnFlag = false; 
			java.util.Date dt = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int date = cal.get(Calendar.DATE);
			com.splwg.base.api.datatypes.Date dtime = new com.splwg.base.api.datatypes.Date(year, month, date);
			startChanges();
			List<String> payTenderIdList = getPayTenderIdList();
			if(!isNull(payTenderIdList)){
				for(String payTendID : payTenderIdList){
					String query = getQueryForExistExport(this.getParameters().getTenderSourceType(),
							this.getParameters().getTenderSourceExclusion(),payTendID);
					psPreparedStatement = createPreparedStatement(query);
					try {
						result = psPreparedStatement.iterate();
						while (result.hasNext()) {
							SQLResultRow resultValues = result.next();
							cmExportBankCheckDTO = new CmExportBankCheckDTO();
							
							cmExportBankCheckDTO.setCreDttm(dtime);
							cmExportBankCheckDTO.setBatchNbr(this.getBatchNumber().toString());
							cmExportBankCheckDTO.setDepCtrlId(resultValues.getString("DEP_CTL_ID"));
							cmExportBankCheckDTO.setTndrSrceCd(resultValues.getString("TNDR_SOURCE_CD"));
							cmExportBankCheckDTO.setBanqDescr(resultValues.getString("DESCR"));
							cmExportBankCheckDTO.setAccntNbr(resultValues.getString("ACCOUNT_NBR"));
							cmExportBankCheckDTO.setBankCd(resultValues.getString("BANK_CD"));
							cmExportBankCheckDTO.setBankAcctKey(resultValues.getString("BANK_ACCT_KEY"));
							cmExportBankCheckDTO.setBalanceDttm(resultValues.getDate("BALANCED_DTTM"));
							cmExportBankCheckDTO.setTndrCtrlId(resultValues.getString("TNDR_CTL_ID"));
							cmExportBankCheckDTO.setPayEvntId(resultValues.getString("PAY_EVENT_ID"));
							cmExportBankCheckDTO.setPayTndrId(resultValues.getString("PAY_TENDER_ID"));
							cmExportBankCheckDTO.setPayorAccntId(resultValues.getString("PAYOR_ACCT_ID"));
							cmExportBankCheckDTO.setPayDate(resultValues.getDate("PAY_DT"));
							cmExportBankCheckDTO.setTenderAmount(resultValues.getString("TENDER_AMT"));
							cmExportBankCheckDTO.setCheckNbr(resultValues.getString("CHECK_NBR"));
							cmExportBankCheckDTO.setCurrencyCd(resultValues.getString("CURRENCY_CD"));
							cmExportBankCheckDTO.setBanqCheq(resultValues.getString("BANK_CHEQ"));
							cmExportBankCheckDTO.setPayId(resultValues.getString("PAY_ID"));

							saveExportBankCheq(cmExportBankCheckDTO);
						}
					} catch (Exception exp) {
						System.out.println("Exception in extracting data in Existing Query" + exp.getMessage());
						log.info("Exception in extracting data in Existing Query" + exp.getMessage());
					} finally{
						psPreparedStatement.close();
						result.close();
						psPreparedStatement = null;
					}
				}
			}
			
			returnFlag = executeQueryForExport();
			
			return returnFlag;
		}

		/**
		 * @return returnFlag
		 */
		private boolean executeQueryForExport() {
			
			PreparedStatement psPreparedStatement = null;
			QueryIterator<SQLResultRow> result = null;
			CmExportBankCheckDTO cmExportBankCheckDTO = null;
			java.util.Date dt = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int date = cal.get(Calendar.DATE);
			boolean returnFlag = false; 
			int count = 0;
			com.splwg.base.api.datatypes.Date dtime = new com.splwg.base.api.datatypes.Date(year, month, date);
			startChanges();
			
			String query = getQueryForExport(this.getParameters().getTenderSourceType(),
					this.getParameters().getTenderSourceExclusion());
			psPreparedStatement = createPreparedStatement(query);
			try {
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow resultValues = result.next();
					cmExportBankCheckDTO = new CmExportBankCheckDTO();
					
					cmExportBankCheckDTO.setCreDttm(dtime);
					cmExportBankCheckDTO.setBatchNbr(this.getBatchNumber().toString());
					cmExportBankCheckDTO.setDepCtrlId(resultValues.getString("DEP_CTL_ID"));
					cmExportBankCheckDTO.setTndrSrceCd(resultValues.getString("TNDR_SOURCE_CD"));
					cmExportBankCheckDTO.setBanqDescr(resultValues.getString("DESCR"));
					cmExportBankCheckDTO.setAccntNbr(resultValues.getString("ACCOUNT_NBR"));
					cmExportBankCheckDTO.setBankCd(resultValues.getString("BANK_CD"));
					cmExportBankCheckDTO.setBankAcctKey(resultValues.getString("BANK_ACCT_KEY"));
					cmExportBankCheckDTO.setBalanceDttm(resultValues.getDate("BALANCED_DTTM"));
					cmExportBankCheckDTO.setTndrCtrlId(resultValues.getString("TNDR_CTL_ID"));
					cmExportBankCheckDTO.setPayEvntId(resultValues.getString("PAY_EVENT_ID"));
					cmExportBankCheckDTO.setPayTndrId(resultValues.getString("PAY_TENDER_ID"));
					cmExportBankCheckDTO.setPayorAccntId(resultValues.getString("PAYOR_ACCT_ID"));
					cmExportBankCheckDTO.setPayDate(resultValues.getDate("PAY_DT"));
					cmExportBankCheckDTO.setTenderAmount(resultValues.getString("TENDER_AMT"));
					cmExportBankCheckDTO.setCheckNbr(resultValues.getString("CHECK_NBR"));
					cmExportBankCheckDTO.setCurrencyCd(resultValues.getString("CURRENCY_CD"));
					cmExportBankCheckDTO.setBanqCheq(resultValues.getString("BANK_CHEQ"));
					//cmExportBankCheckDTO.setRsCheq(resultValues.getString("RS_PAYOR"));
					cmExportBankCheckDTO.setPayId(resultValues.getString("PAY_ID"));
					//cmExportBankCheckDTO.setPaySegId(resultValues.getString("PAY_SEG_ID"));
					//cmExportBankCheckDTO.setFtId(resultValues.getString("FT_ID"));
					//cmExportBankCheckDTO.setCgDebit(resultValues.getString("GL_ACCT_DEBIT"));
					//cmExportBankCheckDTO.setCgCredit(resultValues.getString("GL_ACCT_CREDIT"));
					//cmExportBankCheckDTO.setCgDebtAmnt(resultValues.getString("DEBIT_AMOUNT"));
					//cmExportBankCheckDTO.setCgCreditAmnt(resultValues.getString("CREDIT_AMOUNT"));

					returnFlag = saveExportBankCheq(cmExportBankCheckDTO);
					count++;
				}
			} catch (Exception exp) {
				System.out.println("Exception in extracting data in Query" + exp.getMessage());
				log.info("Exception in extracting data in Query" + exp.getMessage());
			} finally{
				psPreparedStatement.close();
				result.close();
				psPreparedStatement = null;
				if(count == 0){
					returnFlag = true;
					log.info("There is no data retrieved from export query" );
				}
			}
			return returnFlag;
			
		}
		
		/**
		 * @return payTenderList
		 */
		private List<String> getPayTenderIdList() {
			PreparedStatement psPreparedStatement = null;
			List<String> payTenderList = new ArrayList<String>();
			psPreparedStatement = createPreparedStatement("SELECT PAY_TENDER_ID from cm_rbanq_cheq_exp where STATUT_FLG = '30' OR STATUT_FLG = '10'");
			QueryIterator<SQLResultRow> result = null;
			try {
				startChanges();
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					if(!isBlankOrNull(lookUpValue.getString("PAY_TENDER_ID"))) {
						payTenderList.add(lookUpValue.getString("PAY_TENDER_ID"));
					}
				}
			} catch (Exception excep) {
				log.error("Exception in getting  getRsCheq : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				result.close();
				psPreparedStatement = null;
			}
		
			return payTenderList;
		}

		/**
		 * @param cmExportBankCheckDTO
		 * @return
		 */
		private boolean saveExportBankCheq(CmExportBankCheckDTO cmExportBankCheckDTO) {
			PreparedStatement psPreparedStatement = null;
			StringBuilder stringBuilder = null;
			boolean saveFlag = false;
			startChanges();
			String statutFlag = "20";
			String msgCatNbr = null;
			String msgNbr = null;
			String msgText = null;

			if (!isNull(cmExportBankCheckDTO)) {
				stringBuilder = new StringBuilder();
				String version = checkCheqQueryInsertOrUpdate(cmExportBankCheckDTO.getPayId());
				if(isNullOrBlank(version)){
					stringBuilder = insertQueryToBankCheq();
				} else {
					stringBuilder = updateQueryToBankCheq();
				}
				
				if(isNull(cmExportBankCheckDTO.getCreDttm())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_401().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_401);
					msgText = CmMessageRepository90002.MSG_401().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getBatchNbr())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_402().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_402);
					msgText = CmMessageRepository90002.MSG_402().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getDepCtrlId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_403().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_403);
					msgText = CmMessageRepository90002.MSG_403().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getTndrSrceCd())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_404().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_404);
					msgText = CmMessageRepository90002.MSG_404().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getBanqDescr())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_405().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_405);
					msgText = CmMessageRepository90002.MSG_405().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getAccntNbr())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_406().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_406);
					msgText = CmMessageRepository90002.MSG_406().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getBankCd())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_407().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_407);
					msgText = CmMessageRepository90002.MSG_407().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getBankAcctKey())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_408().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_408);
					msgText = CmMessageRepository90002.MSG_408().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getBalanceDttm())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_409().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_409);
					msgText = CmMessageRepository90002.MSG_409().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getTndrCtrlId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_410().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_410);
					msgText = CmMessageRepository90002.MSG_410().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getPayEvntId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_411().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_411);
					msgText = CmMessageRepository90002.MSG_411().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getPayTndrId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_412().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_412);
					msgText = CmMessageRepository90002.MSG_412().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getPayorAccntId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_413().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_413);
					msgText = CmMessageRepository90002.MSG_413().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getPayDate())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_414().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_414);
					msgText = CmMessageRepository90002.MSG_414().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getTenderAmount())){ 
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_415().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_415);
					msgText = CmMessageRepository90002.MSG_415().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getCurrencyCd())){ 
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_301().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_301);
					msgText = CmMessageRepository90002.MSG_301().getMessageText();
				}  else if (isNull(cmExportBankCheckDTO.getPayId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_417().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_417);
					msgText = CmMessageRepository90002.MSG_417().getMessageText();
				} /*else if (isNull(cmExportBankCheckDTO.getPaySegId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_418().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_418);
					msgText = CmMessageRepository90002.MSG_418().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getFtId())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_419().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_419);
					msgText = CmMessageRepository90002.MSG_419().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getCgDebit())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_421().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_421);
					msgText = CmMessageRepository90002.MSG_421().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getCgCredit())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_422().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_422);
					msgText = CmMessageRepository90002.MSG_422().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getCgDebtAmnt())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_423().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_423);
					msgText = CmMessageRepository90002.MSG_423().getMessageText();
				} else if (isNull(cmExportBankCheckDTO.getCgCreditAmnt())){
					statutFlag = "30";
					msgCatNbr = CmMessageRepository90002.MSG_424().getCategory().toString();
					msgNbr = String.valueOf(CmMessageRepository90002.Messages.MSG_424);
					msgText = CmMessageRepository90002.MSG_424().getMessageText();
				} */else if(isNull(cmExportBankCheckDTO.getCheckNbr())) {
					statutFlag = "30";
					createToDo("425");
				}
				psPreparedStatement = createPreparedStatement(stringBuilder.toString());
				psPreparedStatement.setAutoclose(false);
				psPreparedStatement.bindDate("CRE_DTTM", cmExportBankCheckDTO.getCreDttm());
				psPreparedStatement.bindString("BATCH_NBR", cmExportBankCheckDTO.getBatchNbr(), null);
				psPreparedStatement.bindString("DEP_CTL_ID", cmExportBankCheckDTO.getDepCtrlId(), null);
				psPreparedStatement.bindString("TNDR_SOURCE_CD", cmExportBankCheckDTO.getTndrSrceCd(),null);
				psPreparedStatement.bindString("BANQ_DESCR", cmExportBankCheckDTO.getBanqDescr(),null);
				psPreparedStatement.bindString("ACCOUNT_NBR", cmExportBankCheckDTO.getAccntNbr(),null);
				psPreparedStatement.bindString("BANK_CD", cmExportBankCheckDTO.getBankCd(),null);
				psPreparedStatement.bindString("BANK_ACCOUNT_KEY", cmExportBankCheckDTO.getBankAcctKey(),null);
				psPreparedStatement.bindDate("BALANCE_DTTM", cmExportBankCheckDTO.getBalanceDttm());
				psPreparedStatement.bindString("TNDR_CTL_ID", cmExportBankCheckDTO.getTndrCtrlId(), null);
				psPreparedStatement.bindString("PAY_EVENT_ID", cmExportBankCheckDTO.getPayEvntId(), null);
				psPreparedStatement.bindString("PAY_TENDER_ID", cmExportBankCheckDTO.getPayTndrId(), null);
				psPreparedStatement.bindString("PAYOR_ACCT_ID", cmExportBankCheckDTO.getPayorAccntId(), null);
				psPreparedStatement.bindDate("PAY_DT", cmExportBankCheckDTO.getPayDate());
				psPreparedStatement.bindBigInteger("TENDER_AMT", isNull(cmExportBankCheckDTO.getTenderAmount())? BigInteger.valueOf(0):BigInteger.valueOf(Long.parseLong(cmExportBankCheckDTO.getTenderAmount().replaceAll("\\.0*$", ""))));
				psPreparedStatement.bindString("CHECK_NBR", cmExportBankCheckDTO.getCheckNbr(), null);
				psPreparedStatement.bindString("CURRENCY_CD", cmExportBankCheckDTO.getCurrencyCd(), null);
				psPreparedStatement.bindString("BANQ_CHEQ", cmExportBankCheckDTO.getBanqCheq(), null);
				//psPreparedStatement.bindString("RS_CHEQ", cmExportBankCheckDTO.getRsCheq(), null);
				psPreparedStatement.bindString("PAY_ID", cmExportBankCheckDTO.getPayId(), null);
				//psPreparedStatement.bindString("PAY_SEG_ID", cmExportBankCheckDTO.getPaySegId(), null);
				//psPreparedStatement.bindString("FT_ID", cmExportBankCheckDTO.getFtId(), null);
				//psPreparedStatement.bindString("CG_DEBIT", cmExportBankCheckDTO.getCgDebit(), null);
				//psPreparedStatement.bindString("CG_CREDIT", cmExportBankCheckDTO.getCgCredit(), null);
				//psPreparedStatement.bindBigInteger("CG_DEBIT_AMT", isNull(cmExportBankCheckDTO.getCgDebtAmnt())? BigInteger.valueOf(0):BigInteger.valueOf(Long.parseLong(cmExportBankCheckDTO.getCgDebtAmnt().replaceAll("\\.0*$", ""))));
				//psPreparedStatement.bindBigInteger("CG_CREDIT_AMT", isNull(cmExportBankCheckDTO.getCgCreditAmnt())? BigInteger.valueOf(0):BigInteger.valueOf(Long.parseLong(cmExportBankCheckDTO.getCgCreditAmnt().replaceAll("\\.0*$", ""))));
				psPreparedStatement.bindString("STATUT_FLG", statutFlag, null);
				psPreparedStatement.bindString("MESSAGE_CAT_NBR", msgCatNbr, null);
				psPreparedStatement.bindString("MESSAGE_NBR", msgNbr, null);
				psPreparedStatement.bindString("MESSAGE_TXT", msgText, null);
				if(isNullOrBlank(version)){
					psPreparedStatement.bindString("VERSION", "1", null);
				} else {
					int incrementVersion = Integer.parseInt(version.trim());
					incrementVersion = incrementVersion+1;
					psPreparedStatement.bindString("VERSION", String.valueOf(incrementVersion), null);
					psPreparedStatement.bindString("PAY_ID", cmExportBankCheckDTO.getPayId(), null);
				}
				
				try {
					int result = psPreparedStatement.executeUpdate();
					saveFlag = true;
					String rsCheq = getRsCheq(cmExportBankCheckDTO.getPayTndrId());
					updateRsCheq(cmExportBankCheckDTO.getPayId(),rsCheq);
					if(statutFlag.equals("20")){
						updateStatusDeptCtrl(cmExportBankCheckDTO.getDepCtrlId());
					}
					saveChanges();
				} catch (Exception exception) {
					System.out.println("Exception in saveExportBankCheq" + exception.getMessage());
					log.info("Exception in saveExportBankCheq" + exception.getMessage());
					saveFlag = false;
				} finally {
					psPreparedStatement.close();
					psPreparedStatement = null;
				}
			}
			return saveFlag;
		}
		
		private String getRsCheq(String payTndrId) {
			
			PreparedStatement psPreparedStatement = null;
			String rsCheq = null;
			psPreparedStatement = createPreparedStatement("select SRCH_CHAR_VAL from ci_pay_tndr_char where pay_tender_id = \'"+payTndrId+"\' and char_type_cd = 'CHEQ-RS'");
			QueryIterator<SQLResultRow> result = null;
			try {
				startChanges();
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					rsCheq = lookUpValue.getString("SRCH_CHAR_VAL");
				}
			} catch (Exception excep) {
				log.error("Exception in getting  getRsCheq : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				result.close();
				psPreparedStatement = null;
			}
			return rsCheq;
		}

		/**
		 * @param payId
		 */
		private void updateRsCheq(String payId,String rsCheq) {
			PreparedStatement psPreparedStatement = null;
			try {
				startChanges();
				if(!isNull(payId)){
					psPreparedStatement = createPreparedStatement("UPDATE cm_rbanq_cheq_exp SET RS_CHEQ = :RS_CHEQ WHERE PAY_ID = :PAY_ID");
					psPreparedStatement.bindString("RS_CHEQ", rsCheq,null);
					psPreparedStatement.bindString("PAY_ID", payId,null);
					int updateCount = psPreparedStatement.executeUpdate();
					System.out.println("updateCount:: " + updateCount);
					log.info("updateCount:: " + updateCount);
				} else {
					log.info("Pay id is empty:: ");
				}
			} catch (Exception exception) {
				System.out.println("Exception in Updating updateRsCheq: " + exception);
				log.error("Exception in Updating updateRsCheq: " + exception);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
		}

		/**
		 * Method to create To Do
		 * 
		 * @param messageParam
		 * @param nineaNumber
		 * @param messageNumber
		 * @param fileName
		 */
		private void createToDo(String messageNumber) {
			startChanges();
			BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
			Role_Id toDoRoleId = new Role_Id("CM-EXCTR");
			Role toDoRole = toDoRoleId.getEntity();
			businessServiceInstance.getFieldAndMDForPath("sendTo").setXMLValue("SNDR");
			businessServiceInstance.getFieldAndMDForPath("subject").setXMLValue("Export Bank Cheque");
			businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue("CM-EXCTD");
			businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
			businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue("BATCH_JOB_ID");
			businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue("90002");
			businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue(messageNumber);
			businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue("BATCH_JOB_ID");

			BusinessServiceDispatcher.execute(businessServiceInstance);
			saveChanges();
		}

		/**
		 * @param paySegId
		 * @return
		 */
		private String checkCheqQueryInsertOrUpdate(String payId) {
			PreparedStatement psPreparedStatement = null;
			String version = null;
			psPreparedStatement = createPreparedStatement("select VERSION FROM CM_RBANQ_CHEQ_EXP WHERE PAY_ID = \'"+payId+"\'");
			QueryIterator<SQLResultRow> result = null;
			try {
				startChanges();
				result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					version = lookUpValue.getString("VERSION");
				}
			} catch (Exception excep) {
				log.error("Exception in getting  checkInsertOrUpdate : " + excep);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				result.close();
				psPreparedStatement = null;
			}
			return version;
		}

		/**
		 * @param depCtrlId
		 */
		private void updateStatusDeptCtrl(String depCtrlId) {
			PreparedStatement psPreparedStatement = null;
			try {
				startChanges();
				psPreparedStatement = createPreparedStatement("UPDATE CI_DEP_CTL SET DEP_CTL_STATUS_FLG = '40' WHERE DEP_CTL_ID = :DEP_CTL_ID");
				psPreparedStatement.bindString("DEP_CTL_ID", depCtrlId,null);
				int updateCount = psPreparedStatement.executeUpdate();
				System.out.println("updateCount:: " + updateCount);
				log.info("updateCount:: " + updateCount);

			} catch (Exception exception) {
				System.out.println("Exception in Updating updateStatusDeptCtrl CI_DEP_CTL table: " + exception);
				log.error("Exception in Updating updateStatusDeptCtrl CI_DEP_CTL table: " + exception);
			} finally {
				saveChanges();
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
		}

		/**
		 * @return
		 */
		private StringBuilder updateQueryToBankCheq() {
			
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("UPDATE CM_RBANQ_CHEQ_EXP SET CRE_DTTM = :CRE_DTTM ");
			stringBuilder.append(",BATCH_NBR = :BATCH_NBR");
			stringBuilder.append(",DEP_CTL_ID = :DEP_CTL_ID");
			stringBuilder.append(",TNDR_SOURCE_CD = :TNDR_SOURCE_CD");
			stringBuilder.append(",BANQ_DESCR = :BANQ_DESCR");
			stringBuilder.append(",ACCOUNT_NBR = :ACCOUNT_NBR");
			stringBuilder.append(",BANK_CD = :BANK_CD");
			stringBuilder.append(",BANK_ACCOUNT_KEY =:BANK_ACCOUNT_KEY");
			stringBuilder.append(",BALANCE_DTTM = :BALANCE_DTTM");
			stringBuilder.append(",TNDR_CTL_ID = :TNDR_CTL_ID");
			stringBuilder.append(",PAY_EVENT_ID = :PAY_EVENT_ID");
			stringBuilder.append(",PAY_TENDER_ID = :PAY_TENDER_ID");
			stringBuilder.append(",PAYOR_ACCT_ID = :PAYOR_ACCT_ID");
			stringBuilder.append(",PAY_DT = :PAY_DT");
			stringBuilder.append(",TENDER_AMT = :TENDER_AMT");
			stringBuilder.append(",CHECK_NBR = :CHECK_NBR");
			stringBuilder.append(",CURRENCY_CD = :CURRENCY_CD");
			stringBuilder.append(",BANQ_CHEQ = :BANQ_CHEQ");
			//stringBuilder.append(",RS_CHEQ = :RS_CHEQ");
			stringBuilder.append(",PAY_ID = :PAY_ID");
			//stringBuilder.append(",PAY_SEG_ID = :PAY_SEG_ID");
			//stringBuilder.append(",FT_ID = :FT_ID");
			//stringBuilder.append(",CG_DEBIT = :CG_DEBIT");
			//stringBuilder.append(",CG_CREDIT = :CG_CREDIT");
			//stringBuilder.append(",CG_DEBIT_AMT = :CG_DEBIT_AMT");
			//stringBuilder.append(",CG_CREDIT_AMT = :CG_CREDIT_AMT");
			stringBuilder.append(",STATUT_FLG = :STATUT_FLG");
			stringBuilder.append(",MESSAGE_CAT_NBR = :MESSAGE_CAT_NBR");
			stringBuilder.append(",MESSAGE_NBR = :MESSAGE_NBR");
			stringBuilder.append(",MESSAGE_TXT  = :MESSAGE_TXT");
			stringBuilder.append(",VERSION  =:VERSION");
			stringBuilder.append(" WHERE PAY_ID = :PAY_ID");
					
			return stringBuilder;
		}
		
		/**
		 * @return
		 */
		private StringBuilder insertQueryToBankCheq() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(
					"INSERT INTO CM_RBANQ_CHEQ_EXP (ID_EXP_RBAN,CRE_DTTM,BATCH_NBR,DEP_CTL_ID,TNDR_SOURCE_CD,BANQ_DESCR,ACCOUNT_NBR,BANK_CD,BANK_ACCOUNT_KEY,");
			stringBuilder.append(
					"BALANCE_DTTM,TNDR_CTL_ID,PAY_EVENT_ID,PAY_TENDER_ID,PAYOR_ACCT_ID,PAY_DT,TENDER_AMT,CHECK_NBR,CURRENCY_CD,BANQ_CHEQ,PAY_ID");//,RS_CHEQ,PAY_SEG_ID,FT_ID,
			//stringBuilder.append("CG_DEBIT,CG_CREDIT,CG_DEBIT_AMT,CG_CREDIT_AMT");
			stringBuilder.append(",STATUT_FLG ");
			stringBuilder.append(",MESSAGE_CAT_NBR ");
			stringBuilder.append(",MESSAGE_NBR ");
			stringBuilder.append(",MESSAGE_TXT ");
			 stringBuilder.append(",VERSION )");
			stringBuilder.append(" values ( ");
			stringBuilder.append(
					"CM_BANK_CHQ_EXPORT.nextval,:CRE_DTTM,:BATCH_NBR,:DEP_CTL_ID,:TNDR_SOURCE_CD,:BANQ_DESCR,:ACCOUNT_NBR,:BANK_CD,:BANK_ACCOUNT_KEY,");
			stringBuilder.append(
					":BALANCE_DTTM,:TNDR_CTL_ID,:PAY_EVENT_ID,:PAY_TENDER_ID,:PAYOR_ACCT_ID,:PAY_DT,:TENDER_AMT,:CHECK_NBR,:CURRENCY_CD,:BANQ_CHEQ,:PAY_ID");//,:RS_CHEQ,:PAY_SEG_ID,:FT_ID,
			//stringBuilder.append(":CG_DEBIT,:CG_CREDIT,:CG_DEBIT_AMT,:CG_CREDIT_AMT");
			stringBuilder.append(",:STATUT_FLG ");
			stringBuilder.append(",:MESSAGE_CAT_NBR ");
			stringBuilder.append(",:MESSAGE_NBR ");
			stringBuilder.append(",:MESSAGE_TXT ");
			stringBuilder.append(",:VERSION )");
			return stringBuilder;
		}
		
		private String getQueryForExistExport (String tenderSourceType, String tenderSourceExclusion,String payTendID) {
			
			String EXPORT_EXIST_DATA_QUERY = ""
					+ "                  select distinct "
					+ "					 dept.DEP_CTL_ID, "
					+ "					 tndr_srce.TNDR_SOURCE_CD, "
					+ "					 tndr_srce.BANK_CD, "
					+ "					 tndr_srce.BANK_ACCT_KEY, "
					+ "					 dept.BALANCED_DTTM, "
					+ "					 bank_acct.ACCOUNT_NBR, "
					+ "					 bank_acct_l.DESCR, "
					+ "					 tndr.TNDR_CTL_ID, "
					+ "					 tndr_char.SRCH_CHAR_VAL AS BANK_CHEQ, "
					//+ "				 -- tndr_char_rs.SRCH_CHAR_VAL AS RS_PAYOR,   "
					+ "					 tndr.PAY_EVENT_ID, "
					+ "					 tndr.PAY_TENDER_ID, "
					+ "					 tndr.PAYOR_ACCT_ID, "
					+ "					 pay_event.PAY_DT, "
					+ "					 tndr.TENDER_AMT, "
					+ "					 tndr.CHECK_NBR, "
					+ "					 tndr.CURRENCY_CD, "
					+ "					 pay.PAY_ID "
					//+ "			     --payseg.PAY_SEG_ID,   "
					//+ "				 --ft.FT_ID,   "
					//+ "				 --ft_gl_debit.GL_ACCT as GL_ACCT_DEBIT,   "
					//+ "                --ft_gl_credit.GL_ACCT as GL_ACCT_CREDIT,   "
					//+ "				 --ft_gl_debit.AMOUNT as DEBIT_AMOUNT,   "
					//+ "				 --ft_gl_credit.AMOUNT as CREDIT_AMOUNT   "
					+ "					 from "
					+ "					 ci_dep_ctl dept, "
					+ "					 ci_tndr_ctl ctrl, "
					+ "					 ci_pay_tndr tndr, "
					+ "					 ci_pay_tndr_char tndr_char, "
					+ "					 ci_pay_tndr_char tndr_char_rs, "
					+ "					 ci_pay_event pay_event, "
					+ "					 ci_pay pay, "
					+ "					 ci_pay_seg payseg, "
					+ "					 ci_ft ft, "
					+ "					 ci_ft_gl ft_gl_debit, "
					+ "					 ci_ft_gl ft_gl_credit, "
					+ "					 ci_tndr_srce tndr_srce, "
					+ "					 ci_bank_account bank_acct, "
					+ "					 ci_bank_account_l bank_acct_l "
					+ "					 where "
					+ "					 dept.DEP_CTL_ID = ctrl.DEP_CTL_ID "
					+ "					 AND ctrl.TNDR_CTL_ID =  tndr.TNDR_CTL_ID "
					+ "					 AND pay_event.PAY_EVENT_ID = tndr.PAY_EVENT_ID "
					+ "					 AND pay_event.PAY_EVENT_ID = pay.PAY_EVENT_ID "
					+ "					 AND pay.PAY_ID = payseg.PAY_ID "
					+ "					 AND payseg.SA_ID = ft.SA_ID "
					+ "					 AND payseg.PAY_SEG_ID = ft.SIBLING_ID "
					+ "					 AND ft.FT_ID = ft_gl_debit.FT_ID "
					+ "					 AND ft_gl_debit.FT_ID = ft_gl_credit.FT_ID "
					+ "					 AND ctrl.TNDR_SOURCE_CD = tndr_srce.TNDR_SOURCE_CD "
					+ "					 AND tndr_srce.BANK_ACCT_KEY =  bank_acct.BANK_ACCT_KEY "
					+ "					 AND bank_acct.BANK_ACCT_KEY = bank_acct_l.BANK_ACCT_KEY "
					+ "					 AND tndr.PAY_TENDER_ID = tndr_char.PAY_TENDER_ID "
					+ "					 AND tndr.PAY_TENDER_ID = tndr_char_rs.PAY_TENDER_ID "
					+ "					 AND dept.TNDR_SRCE_TYPE_FLG = \'"+tenderSourceType+"\' "
					+ "					 AND dept.DEP_CTL_STATUS_FLG = '40' "
					+ "					 AND ctrl.TNDR_SOURCE_CD <> \'"+tenderSourceExclusion+"\' "
					+ "					 AND ctrl.TNDR_CTL_ST_FLG = '30' "
					+ "					 AND tndr.TENDER_TYPE_CD = 'CHEC' "
					+ "					 AND tndr.TNDR_STATUS_FLG = '25' "
					+ "					 AND pay.PAY_STATUS_FLG = '50' "
					+ "					 AND ft.FT_TYPE_FLG in ('PS') "
					+ "					 AND ft_gl_debit.GL_SEQ_NBR = '1' "
					+ "					 AND ft_gl_credit.GL_SEQ_NBR = '2' "
					+ "					 AND ft.GL_DISTRIB_STATUS = 'D' "
					+ "					 AND bank_acct_l.LANGUAGE_CD ='FRA' "
					+ "					 AND tndr_char.CHAR_TYPE_CD = 'CM-BANQT' "
					+ "           AND tndr.pay_tender_id = \'"+payTendID+"\'";
			
			return EXPORT_EXIST_DATA_QUERY;
			
			
		}
		
		/**
		 * @param tenderSourceType
		 * @param tenderSourceExclusion
		 * @return
		 */
		private String getQueryForExport(String tenderSourceType, String tenderSourceExclusion) {

			String EXPORT_DATA_QUERY = ""
					+ "                  select distinct "
					+ "					 dept.DEP_CTL_ID, "
					+ "					 tndr_srce.TNDR_SOURCE_CD, "
					+ "					 tndr_srce.BANK_CD, "
					+ "					 tndr_srce.BANK_ACCT_KEY, "
					+ "					 dept.BALANCED_DTTM, "
					+ "					 bank_acct.ACCOUNT_NBR, "
					+ "					 bank_acct_l.DESCR, "
					+ "					 tndr.TNDR_CTL_ID, "
					+ "					 tndr_char.SRCH_CHAR_VAL AS BANK_CHEQ, "
					//+ "			     tndr_char_rs.SRCH_CHAR_VAL AS RS_PAYOR,   "
					+ "					 tndr.PAY_EVENT_ID, "
					+ "					 tndr.PAY_TENDER_ID, "
					+ "					 tndr.PAYOR_ACCT_ID, "
					+ "					 pay_event.PAY_DT, "
					+ "					 tndr.TENDER_AMT, "
					+ "					 tndr.CHECK_NBR, "
					+ "					 tndr.CURRENCY_CD, "
					+ "					 pay.PAY_ID "
					//+ "				 payseg.PAY_SEG_ID,   "
					//+ "				 ft.FT_ID,   "
					//+ "				 ft_gl_debit.GL_ACCT as GL_ACCT_DEBIT,   "
					//+ "                ft_gl_credit.GL_ACCT as GL_ACCT_CREDIT,   "
					//+ "				 ft_gl_debit.AMOUNT as DEBIT_AMOUNT,   "
					//+ "				 ft_gl_credit.AMOUNT as CREDIT_AMOUNT   "
					+ "					 from "
					+ "					 ci_dep_ctl dept, "
					+ "					 ci_tndr_ctl ctrl, "
					+ "					 ci_pay_tndr tndr, "
					+ "					 ci_pay_tndr_char tndr_char, "
					//+ "				 ci_pay_tndr_char tndr_char_rs,   "
					+ "					 ci_pay_event pay_event, "
					+ "					 ci_pay pay, "
					+ "					 ci_pay_seg payseg, "
					+ "					 ci_ft ft, "
					+ "					 ci_ft_gl ft_gl_debit, "
					+ "					 ci_ft_gl ft_gl_credit, "
					+ "					 ci_tndr_srce tndr_srce, "
					+ "					 ci_bank_account bank_acct, "
					+ "					 ci_bank_account_l bank_acct_l "
					+ "					 where "
					+ "					 dept.DEP_CTL_ID = ctrl.DEP_CTL_ID "
					+ "					 AND ctrl.TNDR_CTL_ID =  tndr.TNDR_CTL_ID "
					+ "					 AND pay_event.PAY_EVENT_ID = tndr.PAY_EVENT_ID "
					+ "					 AND pay_event.PAY_EVENT_ID = pay.PAY_EVENT_ID "
					+ "					 AND pay.PAY_ID = payseg.PAY_ID "
					+ "					 AND payseg.SA_ID = ft.SA_ID "
					+ "					 AND payseg.PAY_SEG_ID = ft.SIBLING_ID "
					+ "					 AND ft.FT_ID = ft_gl_debit.FT_ID "
					+ "					 AND ft_gl_debit.FT_ID = ft_gl_credit.FT_ID "
					+ "					 AND ctrl.TNDR_SOURCE_CD = tndr_srce.TNDR_SOURCE_CD "
					+ "					 AND tndr_srce.BANK_ACCT_KEY =  bank_acct.BANK_ACCT_KEY "
					+ "					 AND bank_acct.BANK_ACCT_KEY = bank_acct_l.BANK_ACCT_KEY "
					+ "					 AND tndr.PAY_TENDER_ID = tndr_char.PAY_TENDER_ID "
				   //+ "			     AND tndr.PAY_TENDER_ID = tndr_char_rs.PAY_TENDER_ID   "
					+ "					 AND dept.TNDR_SRCE_TYPE_FLG = \'"+tenderSourceType+"\' "
					+ "					 AND dept.DEP_CTL_STATUS_FLG = '30' "
					+ "					 AND ctrl.TNDR_SOURCE_CD <> \'"+tenderSourceExclusion+"\' "
					+ "					 AND ctrl.TNDR_CTL_ST_FLG = '30' "
					+ "					 AND tndr.TENDER_TYPE_CD = 'CHEC' "
					+ "					 AND tndr.TNDR_STATUS_FLG = '25' "
					+ "					 AND pay.PAY_STATUS_FLG = '50' "
					+ "					 AND ft.FT_TYPE_FLG in ('PS') "
					+ "					 AND ft_gl_debit.GL_SEQ_NBR = '1' "
					+ "					 AND ft_gl_credit.GL_SEQ_NBR = '2' "
					+ "					 AND ft.GL_DISTRIB_STATUS = 'D' "
					+ "					 AND bank_acct_l.LANGUAGE_CD = 'FRA' "
					+ "					 AND tndr_char.CHAR_TYPE_CD = 'CM-BANQT'";
					//+ "					 --AND tndr_char_rs.CHAR_TYPE_CD = 'CHEQ-RS'";

			return EXPORT_DATA_QUERY;
		}
	}
}
