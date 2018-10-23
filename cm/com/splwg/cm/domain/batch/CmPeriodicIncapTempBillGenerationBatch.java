package com.splwg.cm.domain.batch;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.cm.domain.common.entities.CmPeriodicBillEntity;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.AutomaticProcessingLookup;
import com.splwg.tax.domain.admin.assetType.AssetType_Id;
import com.splwg.tax.domain.admin.filingCalendar.FilingCalendar_Id;
import com.splwg.tax.domain.admin.valuationType.ValuationType_Id;
import com.splwg.tax.domain.admin.valueDetailType.ValueDetailType_Id;
import com.splwg.tax.domain.asset.asset.Asset;
import com.splwg.tax.domain.asset.asset.Asset_DTO;
import com.splwg.tax.domain.asset.asset.Asset_Id;
import com.splwg.tax.domain.asset.valuation.Valuation;
import com.splwg.tax.domain.asset.valuation.ValuationDetail;
import com.splwg.tax.domain.asset.valuation.ValuationDetail_DTO;
import com.splwg.tax.domain.asset.valuation.ValuationDetail_Id;
import com.splwg.tax.domain.asset.valuation.Valuation_DTO;
import com.splwg.tax.domain.asset.valuation.Valuation_Id;
import com.splwg.tax.domain.processFlow.ProcessFlowCharacteristic;
import com.splwg.tax.domain.processFlow.ProcessFlowCharacteristic_DTO;
import com.splwg.tax.domain.processFlow.ProcessFlowCharacteristic_Id;
import com.splwg.tax.domain.processFlow.ProcessFlow_Id;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_DTO;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_Id;

/**
 * @author Papa
 *
@BatchJob (modules = {},
 *      softParameters = { @BatchJobSoftParameter (name = billStatut, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = billType, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = assetBusObjCd, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = assetType, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = valuationBusObjCd, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = valuationDetailType, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = filingCalender, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = valuationType, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = billTypeBusObjCd, required = true, type = string)
 *            , @BatchJobSoftParameter  (name = numberOfDays, required = true, type = string)})
 *            
 */
public class CmPeriodicIncapTempBillGenerationBatch extends CmPeriodicIncapTempBillGenerationBatch_Gen {

private final static Logger logger = LoggerFactory.getLogger(CmPeriodicIncapTempBillGenerationBatch.class);
	
	private List<CmPeriodicBillEntity> getAllElements() {
		List<CmPeriodicBillEntity> listeElements = new ArrayList<CmPeriodicBillEntity>();
		CmPeriodicBillEntity cmPeriodicBillEntity = null;
		String billType = this.getParameters().getBillType();
		String billStatut=this.getParameters().getBillStatut();
		String query = "SELECT TB.TAX_BILL_ID, TB.CRE_DTTM, PCC.PROC_FLOW_ID FROM C1_TAX_BILL TB, CI_PROC_FLOW_CHAR PCC WHERE TB.TAX_BILL_TYPE_CD = '" + billType + "'"
				+ " AND TB.BO_STATUS_CD = '"+billStatut+"' AND PCC.CHAR_TYPE_CD = 'CM-INCAP'"
				+ " AND TB.TAX_BILL_ID = PCC.CHAR_VAL_FK1";
		PreparedStatement preparedStatement = createPreparedStatement(query, "SELECT");
		QueryIterator<SQLResultRow> resultIterator = null;
		try {
			resultIterator = preparedStatement.iterate();
			while (resultIterator.hasNext()) {
				SQLResultRow result = (SQLResultRow) resultIterator.next();
				cmPeriodicBillEntity = new CmPeriodicBillEntity();
				cmPeriodicBillEntity.setTaxBillId(result.getString("TAX_BILL_ID"));
				cmPeriodicBillEntity.setProcessFlowId(result.getString("PROC_FLOW_ID"));
				cmPeriodicBillEntity.setBillStartDate(result.getDate("CRE_DTTM"));
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
		System.out.println(getAllElements().size()); 
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

	public Class<CmPeriodicIncapTempBillGenerationBatchWorker> getThreadWorkerClass() {
		return CmPeriodicIncapTempBillGenerationBatchWorker.class;
	}

	public static class CmPeriodicIncapTempBillGenerationBatchWorker
			extends CmPeriodicIncapTempBillGenerationBatchWorker_Gen {

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
			BusinessObjectInstance boInstance = null;
			boInstance=BusinessObjectInstance.create("CM-IncaptemporairePfwTransBO");
			boInstance.set("processFlowId", element.getProcessFlowId());
			boInstance=BusinessObjectDispatcher.read(boInstance); 
			COTSInstanceNode group = boInstance.getGroupFromPath("champs");
			BigDecimal nbreJoursDeRepos=group.getNumber("nbreJoursRepos");
			List<BigDecimal> listevalues=null;
			FilingCalendar_Id filingCalender = new FilingCalendar_Id(this.getParameters().getFilingCalender());
			Date endDate = getEndDateAndTaxYear(filingCalender);
			Date dateAT=group.getDate("dateAT");
			System.out.println(nbreJoursDeRepos);
			System.out.println(dateAT);
			int nbreJoursFirstPayment=diffDates(dateAT, element.getBillStartDate());
			TaxBill_Id taxBillId = new TaxBill_Id(element.getTaxBillId());
			logger.info("Bill Id: " + element.getTaxBillId());
		    int z=nbreJoursDeRepos.intValue()-nbreJoursFirstPayment;
		    int nbrejours=Integer.parseInt(this.getParameters().getNumberOfDays());
		    if(z>=nbrejours){
		    	String newDate=ajoutDays(element.getBillStartDate(), nbrejours); 
		    	if(newDate.equals(getSystemDateTime().getDate().toString())){
		    		Asset_Id assetId = getAssetId();
		    		listevalues=new ArrayList<>();
					listevalues.add(group.getNumber("salaireDernierMois"));
					listevalues.add(group.getNumber("nbreJoursTrav")); 
					listevalues.add(group.getNumber("salaireJournalier")); 
					listevalues.add(group.getNumber("nbreHeurSalJourn")); 
					listevalues.add(new BigDecimal(0));
					listevalues.add(new BigDecimal(nbrejours)); 
		    		
		    		String[] valuationDetailTypeArray = this.getParameters().getValuationDetailType().split(",");
					Valuation_Id valuationId = createValuation(assetId, endDate, taxBillId); 
					int sequence=1;
					for(int i=0; i<valuationDetailTypeArray.length; i++){
						ValuationDetail_DTO valuationDetailDTO = createDTO(ValuationDetail.class);
						valuationDetailDTO.setDetailValue(listevalues.get(i)); 
						valuationDetailDTO.setCurrencyId(new Currency_Id("XOF"));
						//String valuationDetailTypeValue = this.getValuationDetailType();
						valuationDetailDTO.setValueDetailTypeId(new ValueDetailType_Id(valuationDetailTypeArray[i]));
						valuationDetailDTO.setId(new ValuationDetail_Id(valuationId, BigInteger.valueOf(sequence)));
						valuationDetailDTO.newEntity();
						System.out.println("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
						logger.info("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
						logger.info("sequence: " + sequence);
						sequence++;  
					}
		    		
		    		generateBill(taxBillId);
					z-=nbrejours;
		    	}
		    	
		    	
		    }
		    else if(z>0){
		    	String newDate=ajoutDays(element.getBillStartDate(), z); 
		    	if(newDate.equals(getSystemDateTime().getDate().toString())){
		    		Asset_Id assetId = getAssetId();
		    		listevalues=new ArrayList<>();
					listevalues.add(group.getNumber("salaireDernierMois"));
					listevalues.add(group.getNumber("nbreJoursTrav")); 
					listevalues.add(group.getNumber("salaireJournalier")); 
					listevalues.add(group.getNumber("nbreHeurSalJourn")); 
					listevalues.add(new BigDecimal(0));
					listevalues.add(new BigDecimal(nbrejours)); 
		    		String[] valuationDetailTypeArray = this.getParameters().getValuationDetailType().split(",");
					Valuation_Id valuationId = createValuation(assetId, endDate, taxBillId); 
					int sequence=1;
					for(int i=0; i<valuationDetailTypeArray.length; i++){
						ValuationDetail_DTO valuationDetailDTO = createDTO(ValuationDetail.class);
						valuationDetailDTO.setDetailValue(listevalues.get(i)); 
						valuationDetailDTO.setCurrencyId(new Currency_Id("XOF"));
						//String valuationDetailTypeValue = this.getValuationDetailType();
						valuationDetailDTO.setValueDetailTypeId(new ValueDetailType_Id(valuationDetailTypeArray[i]));
						valuationDetailDTO.setId(new ValuationDetail_Id(valuationId, BigInteger.valueOf(sequence)));
						valuationDetailDTO.newEntity();
						System.out.println("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
						logger.info("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
						logger.info("sequence: " + sequence);
						sequence++;  
					}
		    		
		    		generateBill(taxBillId);
		    	}
		    }
			
			return true;
		}
		
		/**
		 * @param assetId
		 * @return
		 */
		private Valuation_Id createValuation(Asset_Id assetId, Date endDate,TaxBill_Id taxBillId) {
			
			Valuation_DTO valuationDTO = createDTO(Valuation.class);
			valuationDTO.setValuationTypeId(new ValuationType_Id(this.getParameters().getValuationType()));
			logger.info("Valuation type: " + this.getParameters().getValuationType());
			valuationDTO.setAssetId(assetId);
			valuationDTO.setValuationDate(endDate);
			valuationDTO.setFilingPeriodId(taxBillId.getEntity().getFilingPeriod().getId()); 
			// valuationDTO.setBusinessObjectId(new
			// BusinessObject_Id("C1-Valuation"));
			valuationDTO.setBusinessObjectId(new BusinessObject_Id(this.getParameters().getValuationBusObjCd()));
			valuationDTO.setStatus("ACTIVE");
			valuationDTO.setTaxYear(BigInteger.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR)));
			valuationDTO.newEntity();
			System.out.println(valuationDTO.getEntity().getId().toString());
			return valuationDTO.getEntity().getId();
		}
		/**
		 * @param filingCalender
		 * @return
		 */
		private Date getEndDateAndTaxYear(FilingCalendar_Id filingCalender) {

			String filingCal = filingCalender.getIdValue();
			PreparedStatement preparedStatement = createPreparedStatement("SELECT * FROM CI_FILING_CAL_PERIOD where FILING_CAL_CD= \'"+filingCal+"\'","SELECT");
			preparedStatement.setAutoclose(false);
			Map<String, Date> yearMap = new HashMap<String, Date>();
			Date endDate = null;
			QueryIterator<SQLResultRow> result  = null;

			try {
				 result = preparedStatement.iterate();
				while (result.hasNext()) {
					SQLResultRow lookUpValue = result.next();
					yearMap.put(lookUpValue.getString("TAX_YR"), lookUpValue.getDate("END_DT"));
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				preparedStatement.close();
				preparedStatement = null;
				result.close();
			}
			for(Map.Entry<String, Date> yearMap1 : yearMap.entrySet()) {
				if(yearMap1.getKey().equalsIgnoreCase(String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR)))) {//Check this
					endDate = (Date)yearMap1.getValue();
				}
			}
			
			return endDate;
		}
		
		private Asset_Id getAssetId() {
			
			Asset_DTO asset = createDTO(Asset.class);
			// asset.setAssetTypeId(new AssetType_Id("PRENATAL"));
			String assetType = this.getParameters().getAssetType();
			asset.setAssetTypeId(new AssetType_Id(assetType));
			asset.setStatus("ACTIVE");
			// asset.setBusinessObjectId(new
			// BusinessObject_Id("C1-RealPropertyAsset"));
			asset.setBusinessObjectId(new BusinessObject_Id(this.getParameters().getAssetBusObjCd()));
			asset.setCreDt(currentDate);
			asset.newEntity();
			
			return asset.getEntity().getId();
		}
		
		private String ajoutDays(Date startDate,int nuberOfDays){
			String dt=startDate.getYear()+"-"+startDate.getMonth()+"-"+startDate.getDay();
			try {
				//String dt = "2008-12-31";  // Start date
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
				Calendar c = Calendar.getInstance();
				c.setTime(sdf.parse(dt));
				c.add(Calendar.DATE, nuberOfDays);  // number of days to add
				dt = sdf.format(c.getTime()); 
				System.out.println(dt+ "#############" +getSystemDateTime().getDate()); 
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return dt;
			
		}
		
		private int diffDates(Date date1, Date date2){
			 
	        Calendar c1=Calendar.getInstance();
	        c1.set(date1.getYear(),date1.getMonth(), date1.getDay() );
	        Calendar c2=Calendar.getInstance();
	        c2.set(date2.getYear(),date2.getMonth(), date2.getDay());
	 
	        java.util.Date d1=c1.getTime();
	        java.util.Date d2=c2.getTime();
	 
	        long diff=d2.getTime()-d1.getTime();
	        int noofdays=(int)(diff/(1000*24*60*60));
	        System.out.println(noofdays);
	        return noofdays;
	   
	}
		
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
			billDTO.setBusinessObjectId(new BusinessObject_Id(this.getParameters().getBillTypeBusObjCd()));
			billDTO.setServiceAgreementId(taxBill.getServiceAgreement().getId());
			billDTO.setTaxYear(BigInteger.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR)));
			logger.info("billMonitorDate:: " + billMonitorDate);
			billDTO.setMonitorControlDate(billMonitorDate);
			billDTO.setAutomaticProcessing(AutomaticProcessingLookup.constants.AUTO_PROCESS);
			billDTO.newEntity();

			try {
				BusinessObject businessObject = new BusinessObject_Id(this.getParameters().getBillTypeBusObjCd()).getEntity();
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
