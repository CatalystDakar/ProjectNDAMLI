package com.splwg.cm.domain.batch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.SingleTransactionStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id; 
import com.splwg.cm.domain.common.entities.CmCheqRejetST;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.tax.domain.admin.paymentCancelReason.PaymentCancelReason_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentTender;
import com.splwg.tax.domain.payment.paymentEvent.PaymentTender_Id;

/**
 * @author Papa
 *
@BatchJob (modules = {},
 *      softParameters = { @BatchJobSoftParameter (name = statut, required = true, type = string)
 *            , @BatchJobSoftParameter (name = obligationTypePF, required = true, type = string)
 *            , @BatchJobSoftParameter (name = obligationTypeATMP, required = true, type = string)
 *            , @BatchJobSoftParameter (name = obligationTypePV, required = true, type = string)
 *            , @BatchJobSoftParameter (name = adjustmentTypePF, required = true, type = string)
 *            , @BatchJobSoftParameter (name = adjustmentTypeATMP, required = true, type = string)
 *            , @BatchJobSoftParameter (name = adjustmentTypePV, required = true, type = string)
 *            , @BatchJobSoftParameter (name = toDoType, required = true, type = string)})
 */
public class CmAnnulationChequeBatch extends CmAnnulationChequeBatch_Gen {

	private final static Logger log = LoggerFactory.getLogger(CmAnnulationChequeBatch.class);

	private List<CmCheqRejetST> getAllElements() {
		// List<PaymentEvent> listePayEvnts = new ArrayList<>();
		List<CmCheqRejetST> listeElements = new ArrayList<CmCheqRejetST>();
		CmCheqRejetST cheqRejetST = null;
		// PaymentEvent payEvt;
		String query = "SELECT * FROM CM_CHEQ_REJET_ST WHERE CHEQ_RJT_ST_FLG=:statut ";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("statut", this.getParameters().getStatut(), null);

		QueryIterator<SQLResultRow> resultIterator = preparedStatement.iterate();
		while (resultIterator.hasNext()) {
			SQLResultRow result = (SQLResultRow) resultIterator.next();
			cheqRejetST = new CmCheqRejetST();
			cheqRejetST.setExttransId(result.getString("EXT_TRANSMIT_ID"));
			cheqRejetST.setFileId(result.getString("FILE_ID"));
			cheqRejetST.setFileName(result.getString("FILE_NAME"));
			cheqRejetST.setFileDate(result.getDate("FILE_DATE"));
			cheqRejetST.setTransDate(result.getDate("TRANS_DATE"));
			cheqRejetST.setProcessDate(result.getDate("PROCESS_DTTM"));
			cheqRejetST.setExtSourceId(result.getString("EXT_SOURCE_ID"));
			cheqRejetST.setCheqRjtDtlSeq(result.getInteger("CHEQ_RJT_DTL_SEQ"));
			cheqRejetST.setCheqRjtStFlg(result.getString("CHEQ_RJT_ST_FLG"));
			cheqRejetST.setCancelReason(result.getString("CANCEL_REASON"));
			cheqRejetST.setTenderTypeCD(result.getString("TENDER_TYPE_CD"));
			cheqRejetST.setCheckNbr(result.getString("CHECK_NBR"));
			cheqRejetST.setCurrentCD(result.getString("CURRENCY_CD"));
			cheqRejetST.setTenderAmt(result.getMoney("TENDER_AMT"));
			cheqRejetST.setFraisRjtSW(result.getString("FRAIS_REJET_SW"));
			cheqRejetST.setFraisRsn(result.getString("FRAIS_RSN"));
			cheqRejetST.setFraisRjtMT(result.getMoney("FRAIS_REJET_MT"));
			cheqRejetST.setAccountingDate(result.getDate("ACCOUNTING_DT"));
			cheqRejetST.setMicroId(result.getString("MICR_ID"));
			cheqRejetST.setCustId(result.getString("CUST_ID"));
			cheqRejetST.setName1(result.getString("NAME1"));
			cheqRejetST.setExtReferenceId(result.getString("EXT_REFERENCE_ID"));
			cheqRejetST.setTenderCtrlId(result.getString("TNDR_CTL_ID"));
			cheqRejetST.setAcctId(result.getString("ACCT_ID"));
			cheqRejetST.setPayEventId(result.getString("PAY_EVENT_ID"));
			cheqRejetST.setPayTenderId(result.getString("PAY_TENDER_ID"));
			cheqRejetST.setMsgCatNumber(result.getInteger("MESSAGE_CAT_NBR"));
			cheqRejetST.setMsgNumber(result.getInteger("MESSAGE_NBR"));
			cheqRejetST.setMesText(result.getString("MESSAGE_TEXT"));
			listeElements.add(cheqRejetST);

		}
		return listeElements;
	}

	public JobWork getJobWork() {
		System.out.println("######################## Demarrage JobWorker ############################");
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit unit = null;
		if (getAllElements() != null) {
			for (CmCheqRejetST element : getAllElements()) {
				if (element != null) {
					unit = new ThreadWorkUnit();
					// A unit must be created for every file in the path, this
					// will
					// represent a row to be processed.
					// String fileName =
					// this.getParameters().getFilePaths()+file.getName();
					// unit.addSupplementalData("elementIndex", element);
					unit.setPrimaryId(element);
					// unit.addSupplementalData("fileName", file.getName());
					listOfThreadWorkUnit.add(unit);
				}
			}
		}

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminer JobWorker ############################");
		return jobWork;
	}

	public Class<CmAnnulationChequeBatchWorker> getThreadWorkerClass() {
		return CmAnnulationChequeBatchWorker.class;
	}

	public static class CmAnnulationChequeBatchWorker extends CmAnnulationChequeBatchWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new SingleTransactionStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			// TODO Auto-generated method stub
			boolean ok = false;
			// System.out.println("ID EVENT: " +
			// unit.getPrimaryId().toString());
			// CmCheqRejetST element=(CmCheqRejetST)
			// unit.getSupplementallData("elementIndex");
			CmCheqRejetST element = (CmCheqRejetST) unit.getPrimaryId();
			log.info("xxxxxxxxx: " + element);
			log.info("YYYYYYY" + element.getPayEventId());
			// System.out.println("ELEMENT: " +element);
			// System.out.println("EVENT ID: " +element.getPayEventId());
			PaymentTender_Id payTnderId = new PaymentTender_Id(element.getPayTenderId());
			PaymentTender payTender = payTnderId.getEntity();
			//PaymentCancelReason_Id paymentCancelReason = new PaymentCancelReason_Id(element.getCancelReason());
			PaymentCancelReason_Id paymentCancelReason = new PaymentCancelReason_Id("ACCT");
			System.out.println("ACCT_ID " +element.getAcctId()); 
			String statutTender = null;
			String typeTender = null;
			// for (PaymentTender payTender :
			// payEvtId.getEntity().getPaymentTenders()) {
			statutTender = payTender.getTenderStatus().getLookupValue().getValueName();
			typeTender = payTender.getTenderType().getId().getIdValue();
			System.out.println(payTender.getCheckNumber().trim()+" " +element.getCheckNbr()); 
			String checkNumberFromFrontEnd=payTender.getCheckNumber().trim(); 
			String checkNumberFromBDD=element.getCheckNbr();
			if (!statutTender.equals("canceled") && typeTender.equals("CHEC") && checkNumberFromFrontEnd.equals(checkNumberFromBDD)) {
				payTender.cancel(paymentCancelReason.getEntity());
				ok = true;
				Money montantFraisRjt=element.getFraisRjtMT();
				String obligationId=null;
				if (montantFraisRjt!=null && montantFraisRjt.getAmount().intValue()>0) {
					int montantByObligation=montantFraisRjt.getAmount().intValue()/3;
					Money montant=new Money(new BigDecimal(montantByObligation)); 
					
                    	obligationId = createObligation(element.getAcctId(),getDivisionByObligationType(getParameters().getObligationTypePF().trim()),getParameters().getObligationTypePF().trim());
    					createAjustement(getParameters().getAdjustmentTypePF(), obligationId, montant);
    					
    					obligationId = createObligation(element.getAcctId(),getDivisionByObligationType(getParameters().getObligationTypeATMP().trim()),getParameters().getObligationTypeATMP().trim());
    					createAjustement(getParameters().getAdjustmentTypeATMP(), obligationId, montant);
    					
    					obligationId = createObligation(element.getAcctId(),getDivisionByObligationType(getParameters().getObligationTypePV().trim()),getParameters().getObligationTypePV().trim());
    					createAjustement(getParameters().getAdjustmentTypePV(), obligationId, montant);
					
				}

				 log.info("TENDER TYPE: "
				 +payTender.getTenderType().getId().getIdValue());
				 log.info("ID TENDER: " +payTender.getId().getIdValue());
				 System.out.println("TENDER TYPE : "
				 +payTender.getTenderType().getId().getIdValue());
				 System.out.println("ID TENDER : "
				 +payTender.getId().getIdValue());
				 System.out.println(payTender.getTenderStatus().getLookupValue().getValueName());
				 System.out.println("CHEK NBR " +payTender.getCheckNumber()); 
			}
			if (ok) {
				updateStatut("ANNULATION_OK", payTnderId.getIdValue());
			}
			if (!statutTender.equals("canceled") && typeTender.equals("CHEC") && checkNumberFromFrontEnd.equals(checkNumberFromBDD) && !ok) {
				updateStatut("ANNULATION_KO", payTnderId.getIdValue());
				// envoyer une TODO
				createToDo(element.getPayEventId(), element.getMsgNumber(), element.getMsgCatNumber(),
						payTender.getId().getIdValue(), element.getFraisRjtMT(), element.getExttransId(),
						element.getExtReferenceId(), element.getFraisRjtMT(), element.getAccountingDate());
			}
			return true;
		}

		private void updateStatut(String statut, String payTenderId) {
			String query = "UPDATE CM_CHEQ_REJET_ST SET CHEQ_RJT_ST_FLG=:statut WHERE PAY_TENDER_ID=:payTenderId";
			PreparedStatement preparedStatement = createPreparedStatement(query);
			preparedStatement.bindString("statut", statut, null);
			preparedStatement.bindString("payTenderId", payTenderId, null);
			preparedStatement.executeUpdate();
		}

		private void createToDo(String eventId, BigInteger msgNumber, BigInteger msgCat, String numeroLot,
				Money mntRejete, String extTransId, String extReferenceId, Money fraisRejetMnt,
				Date dateBatchCM_IMPAI) {
			BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
			Role_Id toDoRoleId = new Role_Id("TD-CHRJTRO");
			Role toDoRole = toDoRoleId.getEntity();
			businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue(getParameters().getToDoType().trim());  //TD-CHREJ
			businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
			businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue(eventId);
			businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue(msgCat.toString());
			businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue(msgNumber.toString());
			businessServiceInstance.getFieldAndMDForPath("messageParm1").setXMLValue(numeroLot);
			businessServiceInstance.getFieldAndMDForPath("messageParm2").setXMLValue(mntRejete.toString());
			businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue(extTransId);
			businessServiceInstance.getFieldAndMDForPath("sortKey2").setXMLValue(extReferenceId);
			businessServiceInstance.getFieldAndMDForPath("sortKey3").setXMLValue(fraisRejetMnt.toString());
			businessServiceInstance.getFieldAndMDForPath("sortKey4").setXMLValue(dateBatchCM_IMPAI.toString());

			BusinessServiceDispatcher.execute(businessServiceInstance);
		}

//		public String createObligation(String accountId, String division, String obligationType) {
//
//			// Business Service Instance
//			BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-FindCreateObligation");
//
//			// Populate BS parameters if available
//			if (null != accountId && null != division && null != obligationType) {
//				COTSInstanceNode group = bsInstance.getGroupFromPath("input");
//				group.set("accountId", accountId);
//				group.set("division", division);
//				group.set("obligationType", obligationType); 
//			}
//			bsInstance = BusinessServiceDispatcher.execute(bsInstance);
//			String obligationId = null;
//			System.out.println(getSystemDateTime().getDate());
//			// log.info(bsInstance.getDocument().asXML());
//			// Getting the list of results
//			COTSInstanceNode group = bsInstance.getGroupFromPath("output"); 
//
//			// If list IS NOT empty
//			if (group != null) {
//				obligationId = group.getString("obligationId");
//			}
//			return obligationId;
//
//		}
		
		private String createObligation(String accountId, String division, String obligationType){
			BusinessObjectInstance obligationInstance = BusinessObjectInstance.create("C1-FilingPeriodObligation");
			obligationInstance.set("accountId", accountId);  
			obligationInstance.set("obligationStatus", ServiceAgreementStatusLookup.constants.PENDING_START);
			obligationInstance.set("startDate", getSystemDateTime().getDate());
			obligationInstance.set("division", division);
			obligationInstance.set("obligationType",obligationType); 
			obligationInstance = BusinessObjectDispatcher.add(obligationInstance);
			String obligationId = obligationInstance.getString("obligationId");
			System.out.println("obligationId " +obligationId); 
			return obligationId;
		}
		
		private String getDivisionByObligationType(String obligationType) {
			// TODO Auto-generated method stub
			String resultat = null;
			String query = "SELECT CIS_DIVISION FROM CI_SA_TYPE WHERE SA_TYPE_CD='" + obligationType + "'";
			PreparedStatement preparedStatement = createPreparedStatement(query);
			// preparedStatement.bindString("obligationType", obligationTypes,
			// null);
			SQLResultRow sqlResultRow = preparedStatement.firstRow();

			if (sqlResultRow != null) {
				resultat = sqlResultRow.getString("CIS_DIVISION");
				System.out.println("RESULTAT SQL= " + resultat);
			}
			return resultat;
		}
		
		public void createAjustement(String adjustType, String obligationId, Money adjustmentAmount) {

			// Business Service Instance
			BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-CancelCreateAdjustments");
			// Money money=new Money("8750");

			// Populate BS parameters if available
			if (null != adjustType && null != obligationId && null != adjustmentAmount) {
				COTSInstanceNode group = bsInstance.getGroupFromPath("input");
				COTSInstanceListNode firstRow = group.getList("newAdjustments").newChild();
				// COTSInstanceListNode firstRow = list.iterator().next();
				firstRow.set("adjustmentType", adjustType);// ASMT-WO
				firstRow.set("obligationId", obligationId); // createObligation("9045036816",
															// "DOR","E-TPERCU")
				firstRow.set("adjustmentAmount", adjustmentAmount);
				System.out.println(getSystemDateTime().getDate());
				firstRow.set("adjustmentDate", getSystemDateTime().getDate());
			}

			// Execute BS and return the Ninea if exists
			bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		}

	}

}
