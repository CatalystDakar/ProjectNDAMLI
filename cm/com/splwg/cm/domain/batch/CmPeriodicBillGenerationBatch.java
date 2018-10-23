package com.splwg.cm.domain.batch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.SingleTransactionStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.cm.domain.common.entities.CmPeriodicBillEntity;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.AutomaticProcessingLookup;
import com.splwg.tax.domain.processFlow.ProcessFlowCharacteristic;
import com.splwg.tax.domain.processFlow.ProcessFlowCharacteristic_DTO;
import com.splwg.tax.domain.processFlow.ProcessFlowCharacteristic_Id;
import com.splwg.tax.domain.processFlow.ProcessFlow_Id;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_DTO;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_Id;

/**
 * @author Denash Kumar M
 *
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = billType, required = true, type = string)
 *            , @BatchJobSoftParameter (name = numberOfDays, required = true, type = string)
 *            , @BatchJobSoftParameter (name = numberOfBills, required = true, type = string)})
 */
public class CmPeriodicBillGenerationBatch extends CmPeriodicBillGenerationBatch_Gen {
	
	private final static Logger logger = LoggerFactory.getLogger(CmPeriodicBillGenerationBatch.class);
	
	private List<CmPeriodicBillEntity> getAllElements() {
		List<CmPeriodicBillEntity> listeElements = new ArrayList<CmPeriodicBillEntity>();
		CmPeriodicBillEntity cmPeriodicBillEntity = null;
		String numberOfDays = this.getParameters().getNumberOfDays();
		String billType = this.getParameters().getBillType();
		String query = "SELECT TB.TAX_BILL_ID, PROC_FLOW_ID FROM C1_TAX_BILL TB, CI_PROC_FLOW_CHAR PCC WHERE TB.TAX_BILL_TYPE_CD = \'" + billType + "\'"
				+ " AND TB.BO_STATUS_CD = 'COMPLETED' AND SYSDATE - TB.CRE_DTTM >= \'" + numberOfDays + "\' AND PCC.CHAR_TYPE_CD = 'CM-BILID'"
				+ " AND TB.TAX_BILL_ID = PCC.CHAR_VAL_FK1 AND NOT EXISTS"
				+ " (SELECT PCFC.PROC_FLOW_ID FROM CI_PROC_FLOW_CHAR PCFC WHERE"
				+ " PCFC.PROC_FLOW_ID = PCC.PROC_FLOW_ID AND PCFC.CHAR_TYPE_CD='CM-STAT' AND PCFC.CHAR_VAL='COMPLETED')";
		PreparedStatement preparedStatement = createPreparedStatement(query, "SELECT");
		QueryIterator<SQLResultRow> resultIterator = null;
		try {
			resultIterator = preparedStatement.iterate();
			while (resultIterator.hasNext()) {
				SQLResultRow result = (SQLResultRow) resultIterator.next();
				cmPeriodicBillEntity = new CmPeriodicBillEntity();
				cmPeriodicBillEntity.setTaxBillId(result.getString("TAX_BILL_ID"));
				cmPeriodicBillEntity.setProcessFlowId(result.getString("PROC_FLOW_ID"));
				listeElements.add(cmPeriodicBillEntity);
			}
		} catch(Exception exception) {
			logger.error(exception);
		} finally {
			preparedStatement.close();
			preparedStatement = null;
			resultIterator.close();
			resultIterator = null;
		}
		
		return listeElements;
	}
		
	public JobWork getJobWork() {

		System.out.println("######################## Demarrage JobWorker ############################");
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit unit = null;
		if (getAllElements() != null) {
			for (CmPeriodicBillEntity element : getAllElements()) {
				if (element != null) {
					unit = new ThreadWorkUnit();
					unit.setPrimaryId(element);
					listOfThreadWorkUnit.add(unit);
				}
			}
		}

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminer JobWorker ############################");
		return jobWork;
	
	}

	public Class<CmPeriodicBillGenerationBatchWorker> getThreadWorkerClass() {
		return CmPeriodicBillGenerationBatchWorker.class;
	}

	public static class CmPeriodicBillGenerationBatchWorker extends CmPeriodicBillGenerationBatchWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new SingleTransactionStrategy(this);
		}
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		Date currentDate = new Date(gregorianCalendar.get(GregorianCalendar.YEAR),
				gregorianCalendar.get(GregorianCalendar.MONTH), gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
		Date billMonitorDate = new Date(gregorianCalendar.get(GregorianCalendar.YEAR),
				gregorianCalendar.get(GregorianCalendar.MONTH)+1, gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));

		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			
			CmPeriodicBillEntity element = (CmPeriodicBillEntity) unit.getPrimaryId();
			logger.info("Bill Id: " + element.getTaxBillId());
			TaxBill_Id taxBillId = new TaxBill_Id(element.getTaxBillId());
			
			TaxBill_Id taxbBillId =  generateBill(taxBillId);
			
			getBillCompletionStatusAndUpdate(element.getProcessFlowId(), taxbBillId);
			
			return true;
		}
		
		/**
		 * Method to get Bill Completion Status And Update based on bill id and processFlow id
		 * 
		 * @param processFlowId
		 * @param taxbBillId
		 */
		private void getBillCompletionStatusAndUpdate(String processFlowId, TaxBill_Id taxbBillId) {
			// TODO Auto-generated method stub
			String query = "select count(*) BILL_STATUS from CI_PROC_FLOW_CHAR where CHAR_VAL = 'INPROGRESS'"
					+ " AND CHAR_TYPE_CD = 'CM-STAT' AND PROC_FLOW_ID = \'" + processFlowId + "\'";
			PreparedStatement preparedStatement = createPreparedStatement(query, "SELECT");

			try {
				SQLResultRow sqlResultRow = preparedStatement.firstRow();
				BigInteger count = sqlResultRow.getInteger("BILL_STATUS");
				ProcessFlowCharacteristic_DTO processFlowChar_DTO = createDTO(ProcessFlowCharacteristic.class);
				ProcessFlowCharacteristic_DTO processFlowCharacteristic_DTO = createDTO(ProcessFlowCharacteristic.class);
				processFlowCharacteristic_DTO.setCharacteristicValueForeignKey1(taxbBillId.getIdValue());
				
				logger.info("Bill Completion status count:: "+count+ " for the process flowId: "+processFlowId);
		
				if(count.intValue() == Integer.parseInt(this.getParameters().getNumberOfBills())) {
					ProcessFlowCharacteristic_Id pp1 = new ProcessFlowCharacteristic_Id(new ProcessFlow_Id(processFlowId),
							new CharacteristicType_Id("CM-STAT"), count.add(BigInteger.ONE));
					ProcessFlowCharacteristic_Id pp = new ProcessFlowCharacteristic_Id(new ProcessFlow_Id(processFlowId),
							new CharacteristicType_Id("CM-BILID"), count.add(BigInteger.ONE));
					processFlowCharacteristic_DTO.setId(pp);
					processFlowChar_DTO.setId(pp1);
					processFlowChar_DTO.setCharacteristicValue("COMPLETED");
					processFlowCharacteristic_DTO.newEntity();
					processFlowChar_DTO.newEntity();
					
				} else if(count.intValue() > 0){
					ProcessFlowCharacteristic_Id pp1 = new ProcessFlowCharacteristic_Id(new ProcessFlow_Id(processFlowId),
							new CharacteristicType_Id("CM-STAT"), count.add(BigInteger.ONE));
					ProcessFlowCharacteristic_Id pp = new ProcessFlowCharacteristic_Id(new ProcessFlow_Id(processFlowId),
							new CharacteristicType_Id("CM-BILID"), count.add(BigInteger.ONE));
					processFlowCharacteristic_DTO.setId(pp);
					processFlowChar_DTO.setId(pp1);
					processFlowChar_DTO.setCharacteristicValue("INPROGRESS");
					processFlowCharacteristic_DTO.newEntity();
					processFlowChar_DTO.newEntity();
				}
				
			} catch(Exception exception) {
				logger.error(exception);
			} finally {
				preparedStatement.close();
				preparedStatement = null;
			}
			
		}

		/*private void updateBillStatus(String processFlowId) {
			// TODO Auto-generated method stub
			String query = "UPDATE CI_PROC_FLOW_CHAR SET CHAR_VAL=:status WHERE PROC_FLOW_ID=:processFlowId and CHAR_TYPE_CD = 'CM-STAT'";
			PreparedStatement preparedStatement = createPreparedStatement(query, "UPDATE");
			preparedStatement.bindString("processFlowId", processFlowId, null);
			preparedStatement.bindString("status", "COMPLETED", null);
			preparedStatement.executeUpdate();
		}*/

		/**
		 * @param taxRoleId
		 * @param obligationId
		 * @return 
		 */
		private TaxBill_Id generateBill(TaxBill_Id taxBillId) {
			
			logger.info("Enters inside generateBill : taxRoleId: " 
			+ taxBillId.getEntity().getTaxRole().getId().getIdValue() +" obligationId:: " 
					+ taxBillId.getEntity().getServiceAgreement().getId().getIdValue());
			
			TaxBill taxBill = taxBillId.getEntity();
			
			TaxBill_DTO billDTO = createDTO(TaxBill.class);
			billDTO.setTaxRoleId(taxBillId.getEntity().getTaxRole().getId());
		
			billDTO.setTaxBillTypeId(taxBill.getTaxBillType().getId());
			billDTO.setFilingPeriodId(taxBill.getFilingPeriod().getId());
			billDTO.setCalculationControlVersionId(taxBill.getCalculationControlVersionId());
			logger.info("currentDate:: " + currentDate);
			billDTO.setTaxBillStartDate(currentDate);// current date
			billDTO.setTaxBillEndDate(taxBill.getTaxBillEndDate());// current date
			billDTO.setBusinessObjectId(new BusinessObject_Id("CM-TaxBillAndPrint"));
			billDTO.setServiceAgreementId(taxBill.getServiceAgreement().getId());
			billDTO.setTaxYear(BigInteger.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR)));
			logger.info("billMonitorDate:: " + billMonitorDate);
			billDTO.setMonitorControlDate(billMonitorDate);
			billDTO.setAutomaticProcessing(AutomaticProcessingLookup.constants.AUTO_PROCESS);
			billDTO.newEntity();

			try {
				BusinessObject businessObject = new BusinessObject_Id("CM-TaxBillAndPrint").getEntity();
				BusinessObjectInstance boi = BusinessObjectInstance.create(businessObject);		
				boi.set("taxBillId", billDTO.getEntity().getId().getIdValue());//boStatus		
				BusinessObjectInstance dispatchedBoi = BusinessObjectDispatcher.read(boi);
				System.out.println("BO_Schema:: " + dispatchedBoi.getDocument().asXML());
				
				dispatchedBoi.set("boStatus", "GENERATED");
				dispatchedBoi = BusinessObjectDispatcher.update(dispatchedBoi);//calcControl
			    System.out.println("#### BO Instance Schema after GENERATED: " +dispatchedBoi.getDocument().asXML());
				
			    dispatchedBoi.set("boStatus", "COMPLETED");
			    dispatchedBoi = BusinessObjectDispatcher.update(dispatchedBoi);
			    System.out.println("#### BO Instance Schema after COMPLETED: " +dispatchedBoi.getDocument().asXML());
			    logger.info("#### BO Instance Schema after COMPLETED: " +dispatchedBoi.getDocument().asXML());
			    logger.info("Bill generated Successfully:: " + billDTO.getEntity().getId());
			} catch (Exception exception) {
				logger.info("Exception in Bill generation and completion:: " + exception.getMessage());
				logger.info("Exception in Bill generation and completion:: " + exception);
			}
			System.out.println("Exits generateBill : taxRoleId: " + billDTO.getEntity().getId());
	   	    logger.info("Exits generateBill : taxRoleId: " + billDTO.getEntity().getId());
	   	 
	   	return billDTO.getEntity().getId();
		}
		
	}

}
