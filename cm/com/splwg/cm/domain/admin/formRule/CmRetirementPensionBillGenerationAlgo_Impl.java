package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.currency.Currency;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.AutomaticProcessingLookup;
import com.splwg.tax.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.tax.domain.admin.assetType.AssetType_Id;
import com.splwg.tax.domain.admin.calculationControl.CalculationControlVersion_Id;
import com.splwg.tax.domain.admin.calculationControl.CalculationControl_Id;
import com.splwg.tax.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.tax.domain.admin.filingCalendar.FilingCalendar_Id;
import com.splwg.tax.domain.admin.filingCalendar.FilingPeriod_Id;
import com.splwg.tax.domain.admin.serviceAgreementType.ServiceAgreementType_Id;
import com.splwg.tax.domain.admin.serviceType.ServiceType_Id;
import com.splwg.tax.domain.admin.taxBillType.TaxBillType_Id;
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
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_DTO;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.tax.domain.customerinfo.taxRole.TaxRole;
import com.splwg.tax.domain.customerinfo.taxRole.TaxRole_DTO;
import com.splwg.tax.domain.customerinfo.taxRole.TaxRole_Id;
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
@AlgorithmComponent (softParameters = {@AlgorithmSoftParameter (name = assetType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = assetBusObjCd, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = serviceType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = taxRoleBusObjCd, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = filingCalender, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = valuationType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = valuationBusObjCd, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = billTypeBusObjCd, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = billType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = obligationType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = valuationDetailType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = calculationControlId, required = true, type = string)})
 */
public class CmRetirementPensionBillGenerationAlgo_Impl extends CmRetirementPensionBillGenerationAlgo_Gen
		implements BusinessObjectExitStatusAlgorithmSpot {

	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger( CmRetirementPensionBillGenerationAlgo_Impl.class );
	private BusinessObjectInstance boInstance;
	private BusinessObjectInstanceKey boKey;
	String processFlowId = null;
	String pensionCategory = null;
	String ninNumber = null;
	String personId = null;
	String accountId = null;
	String docName = null;
	BigDecimal detailValue = null;
	private Date effectiveDateAllocationFam;
	FilingPeriod_Id filingPeriod = FilingPeriod_Id.NULL;
	GregorianCalendar gregorianCalendar = new GregorianCalendar(); 
	Date currentDate = new Date(gregorianCalendar.get(GregorianCalendar.YEAR),
			gregorianCalendar.get(GregorianCalendar.MONTH), gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
	Date billMonitorDate = new Date(gregorianCalendar.get(GregorianCalendar.YEAR),
			gregorianCalendar.get(GregorianCalendar.MONTH)+1, gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
	
	@Override
	public void invoke() {
	
		this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		LOGGER.info("***CmRetirementPensionBillGenerationAlgo_Impl***" + this.boInstance);
	    COTSFieldDataAndMD<?> processFlowNode = this.boInstance.getFieldAndMDForPath("processFlowId");
	    processFlowId = processFlowNode.getValue().toString();
	    
	    LOGGER.info("ProcessFlowId :: " + processFlowId);
	    System.out.println("ProcessFlowId :: " + processFlowId);
	    
	    COTSFieldDataAndMD<?> pensionCategoryNode = this.boInstance.getFieldAndMDForPath("pension/pensionCategory");
	    pensionCategory = pensionCategoryNode.getValue().toString();
	    
	    LOGGER.info("pensionCategory :: " + pensionCategory);
	    System.out.println("pensionCategory :: " + pensionCategory);
	    
	    COTSFieldDataAndMD<?> ninNumberNode = this.boInstance.getFieldAndMDForPath("nin/nin");
	    ninNumber = ninNumberNode.getValue().toString();
	    
	    COTSFieldDataAndMD<?> cotsRegId = this.boInstance.getFieldAndMDForPath("billDetail/billId");
	    
	    COTSFieldDataAndMD<?> monthlyPensionNode = this.boInstance.getFieldAndMDForPath("monthlyPension");
	    COTSFieldDataAndMD<?> singlePaymentNode = this.boInstance.getFieldAndMDForPath("singlePayment");
	    if(!isBlankOrNull(monthlyPensionNode.getValue().toString())) {
	    	detailValue =  new BigDecimal(monthlyPensionNode.getValue().toString());
	    } else if(!isBlankOrNull(monthlyPensionNode.getValue().toString())) {
	    	  detailValue = new BigDecimal(singlePaymentNode.getValue().toString());
	    }
	    
	    LOGGER.info("pensionCategory :: " + pensionCategory);
	    System.out.println("pensionCategory :: " + pensionCategory);
	    
	    ProcessFlowCharacteristic_DTO processFlowCharacteristic_DTO = createDTO(ProcessFlowCharacteristic.class);
	    LOGGER.info("ninNumber :: " + ninNumber);
	    System.out.println("ninNumber :: " + ninNumber);
		
		//ninNumber = "1458655545888";
	    
	    personId = getPersonByNin(ninNumber);
	    
	    LOGGER.info("personId :: " + personId);
	    System.out.println("personId :: " + personId);
		if (!isBlankOrNull(personId)) {	
			accountId = getAccountsByIdPerson(personId);
			LOGGER.info("accountId :: " + accountId);
			System.out.println("accountId :: " + accountId);
			if(!isBlankOrNull(accountId)) {
				Currency currency = new Account_Id(accountId).getEntity().getCurrency();

				LOGGER.info("currency :: " + currency.getId().getIdValue());
				System.out.println("currency :: " + currency.getId().getIdValue());

				FilingCalendar_Id filingCalender = new FilingCalendar_Id(this.getFilingCalender());

				Date endDate = getEndDateAndTaxYear(filingCalender);
				filingPeriod = new FilingPeriod_Id(filingCalender, endDate);

				Asset_Id assetId = getAssetId();
				String taxRoleId = findOrCreateTaxRole(accountId, this.getServiceType(), assetId);

				Valuation_Id valuationDetailId = createValuation(assetId, endDate);
				
				ValuationDetail_DTO valuationDetailDTO = null;
				//int sequence = Integer.parseInt(getSequence("RETIREMENTPENSIONVALUEDETAIL"));
				String[] valuationDetailTypeArray = this.getValuationDetailType().split(",");
				Map<String, String> valuationMap = getLinkedMap(valuationDetailTypeArray);
				String valuationDetailTypeValue = null;
						
				LOGGER.info("Factor Value:: " + detailValue);
				System.out.println("Factor Value:: " + detailValue);
				valuationDetailDTO = createDTO(ValuationDetail.class);
				if(detailValue != null) {
					detailValue = detailValue.setScale(0, BigDecimal.ROUND_UP);
				}
				valuationDetailDTO.setDetailValue(detailValue.negate());
				valuationDetailDTO.setCurrencyId(new Currency_Id(currency.getId().getIdValue()));				
				valuationDetailTypeValue = valuationMap.get("RETIRE");
				LOGGER.info("valuationDetailTypeValue:: " + valuationDetailTypeValue);
				valuationDetailDTO.setValueDetailTypeId(new ValueDetailType_Id(valuationDetailTypeValue));
				valuationDetailDTO.setId(new ValuationDetail_Id(valuationDetailId, BigInteger.valueOf(1)));
				valuationDetailDTO.newEntity();
				System.out.println("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
				LOGGER.info("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());

				String obligationId = findOrCreateObligation(taxRoleId);

				TaxBill_Id taxbBillId = generateBill(taxRoleId, obligationId);
				cotsRegId.setXMLValue(taxbBillId.getEntity().getId().getIdValue());
				BusinessObjectDispatcher.update(this.boInstance);
				ProcessFlowCharacteristic_Id pp = new ProcessFlowCharacteristic_Id(new ProcessFlow_Id(processFlowId),
						new CharacteristicType_Id("CM-RTPBL"), BigInteger.ONE);
				processFlowCharacteristic_DTO.setId(pp);
				processFlowCharacteristic_DTO.setCharacteristicValueForeignKey1(taxbBillId.getIdValue());
				processFlowCharacteristic_DTO.newEntity();
				
				ProcessFlowCharacteristic_DTO processFlowChar_DTO = createDTO(ProcessFlowCharacteristic.class);
				ProcessFlowCharacteristic_Id pp1 = new ProcessFlowCharacteristic_Id(new ProcessFlow_Id(processFlowId),
						new CharacteristicType_Id("CM-RTPST"), BigInteger.ONE);
				processFlowChar_DTO.setId(pp1);
				processFlowChar_DTO.setCharacteristicValue("INPROGRESS");
				processFlowChar_DTO.newEntity();
				
			} else {
				 LOGGER.info("Account is not found for the Person ID:: " + personId);
				 addWarning(CmMessageRepository90000.MSG_6005(personId));
			}

		} else {
	    	 LOGGER.info("PersonId is not found for the NIN:: " + ninNumber);
	    	 addWarning(CmMessageRepository90000.MSG_6004(ninNumber));
	    }
	}

	private Map<String, String> getLinkedMap(String[] valueArray) {
		// TODO Auto-generated method stub
		Map<String, String> calcMap = new HashMap<String, String>();
		for (int i = 0; i < valueArray.length; i++) {
			if(valueArray[i].contains("FAM")) {
				calcMap.put("FAMILY", valueArray[i]);
			} else if(valueArray[i].contains("PRE")) {
				calcMap.put("PRE", valueArray[i]);
			} else if (valueArray[i].contains("POST")) {
				calcMap.put("POST", valueArray[i]);
			} else {
				calcMap.put("RETIRE", valueArray[i]);
			}
		}
		return calcMap;
	}
	
	/**
	 * @param factorType
	 * @return
	 */
	private Date getEffectiveDate(String calcControl) {

		LOGGER.info("Enters getEffectiveDate Cal Control:: " + calcControl);
		PreparedStatement preparedStatement = createPreparedStatement("SELECT EFFDT FROM C1_CALC_CTRL_VER where CALC_CTRL_CD= \'"+calcControl+"\' order by EFFDT DESC","SELECT");
		preparedStatement.setAutoclose(false);
		Date endDate = null;

		try {
			SQLResultRow sql = preparedStatement.firstRow();
			endDate = sql.getDate("EFFDT");
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			preparedStatement.close();
			preparedStatement = null;
		}
		LOGGER.info("Exits getEffectiveDate" + endDate);
		return endDate;
	}

	/**
	 * @param valuationType
	 * @return
	 */
	private String getSequence(String valuationType) {
		String listeSequences = null;
		QueryIterator<SQLResultRow> queryIterator = null;
		String query = "select max(seqno) as SEQNO from C1_VALTN_DTL where VAL_DTL_TYPE_CD = \'"+valuationType+"\'";
		PreparedStatement preparedStatement = createPreparedStatement(query, "SELECT");
		try {
			queryIterator = preparedStatement.iterate();
			while (queryIterator.hasNext()) {
				SQLResultRow result = (SQLResultRow) queryIterator.next();
				listeSequences = result.getString("SEQNO");
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		} finally {
			preparedStatement.close();
			preparedStatement = null;
			queryIterator.close();
		}
		return listeSequences;
   }

	/**
	 * @param taxRoleId
	 * @param obligationId
	 * @return 
	 */
	private TaxBill_Id generateBill(String taxRoleId, String obligationId) {
		
		LOGGER.info("Enters inside generateBill : taxRoleId: " + taxRoleId +" obligationId:: " + obligationId);
		TaxBill_DTO billDTO = createDTO(TaxBill.class);
		billDTO.setTaxRoleId(new TaxRole_Id(taxRoleId));
	
		String[] billTypeArray = this.getBillType().split(",");
		String[] calcControlArray = this.getCalculationControlId().split(",");
		String calcControlValue = null;
		Map<String, String> calcControlMap = getLinkedMap(calcControlArray);
		Map<String, String> billTypeMap = getLinkedMap(billTypeArray);
		calcControlValue = calcControlMap.get("RETIRE");
		LOGGER.info("calcControlValue:: " + calcControlValue +" billTypeMap:: " + billTypeMap.get("RETIRE"));
		billDTO.setTaxBillTypeId(new TaxBillType_Id(billTypeMap.get("RETIRE")));
		effectiveDateAllocationFam = getEffectiveDate("RETIREMENT PENSION CALC CONTRL");
		billDTO.setCalculationControlVersionId(new CalculationControlVersion_Id(new CalculationControl_Id(calcControlValue), effectiveDateAllocationFam));
		
		billDTO.setFilingPeriodId(filingPeriod);
		LOGGER.info("currentDate:: " + currentDate);
		billDTO.setTaxBillStartDate(currentDate);// current date
		billDTO.setTaxBillEndDate(filingPeriod.getEndDate());// current date
		billDTO.setBusinessObjectId(new BusinessObject_Id(this.getBillTypeBusObjCd()));
		billDTO.setServiceAgreementId(new ServiceAgreement_Id(obligationId));
		billDTO.setTaxYear(BigInteger.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR)));
		LOGGER.info("billMonitorDate:: " + billMonitorDate);
		billDTO.setMonitorControlDate(billMonitorDate);
		billDTO.setAutomaticProcessing(AutomaticProcessingLookup.constants.AUTO_PROCESS);
		billDTO.newEntity();
    	System.out.println("Exits generateBill : taxRoleId: " + billDTO.getEntity().getId());
   	    LOGGER.info("Exits generateBill : taxRoleId: " + billDTO.getEntity().getId());
   	    
		
   	 try {
			BusinessObject businessObject = new BusinessObject_Id("CM-TaxBillAndPrint").getEntity();
			BusinessObjectInstance boi = BusinessObjectInstance.create(businessObject);		
			boi.set("taxBillId", billDTO.getEntity().getId().getIdValue());//boStatus		
			BusinessObjectInstance dispatchedBoi = BusinessObjectDispatcher.read(boi);
			System.out.println("BO_Schema" +   dispatchedBoi.getDocument().asXML());
			
			dispatchedBoi.set("boStatus", "GENERATED");
			dispatchedBoi = BusinessObjectDispatcher.update(dispatchedBoi);//calcControl
		    System.out.println("#### BO Instance Schema after GENERATED: " +dispatchedBoi.getDocument().asXML());
			
		    dispatchedBoi.set("boStatus", "COMPLETED");
		    dispatchedBoi = BusinessObjectDispatcher.update(dispatchedBoi);
		    System.out.println("#### BO Instance Schema after COMPLETED: " +dispatchedBoi.getDocument().asXML());
		} catch (Exception exception) {
			LOGGER.info("Exception in Bill generation and competion" + exception.getMessage());
			LOGGER.info("Exception in Bill generation and competion" + exception);
		}
   	return billDTO.getEntity().getId();
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

	/**
	 * @param assetId
	 * @return
	 */
	private Valuation_Id createValuation(Asset_Id assetId, Date endDate) {
		
		Valuation_DTO valuationDTO = createDTO(Valuation.class);
		valuationDTO.setValuationTypeId(new ValuationType_Id(this.getValuationType()));
		valuationDTO.setAssetId(assetId);
		valuationDTO.setValuationDate(currentDate);
		valuationDTO.setFilingPeriodId(filingPeriod);
		valuationDTO.setBusinessObjectId(new BusinessObject_Id(this.getValuationBusObjCd()));
		valuationDTO.setStatus("ACTIVE");
		valuationDTO.setTaxYear(BigInteger.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR)));
		valuationDTO.newEntity();
		System.out.println(valuationDTO.getEntity().getId().toString());
		return valuationDTO.getEntity().getId();
	}

	/**
	 * @return
	 */
	private Asset_Id getAssetId() {
		
		Asset_DTO asset = createDTO(Asset.class);
		// asset.setAssetTypeId(new AssetType_Id("PRENATAL"));
		String[] assetTypeArray = this.getAssetType().split(",");
		String assetType = null;
		Map<String, String> assetTypeMap = getLinkedMap(assetTypeArray);
		assetType =  assetTypeMap.get("RETIRE");
		LOGGER.info("assetType:: " + assetType);
		asset.setAssetTypeId(new AssetType_Id(assetType));
		asset.setStatus("ACTIVE");
		asset.setBusinessObjectId(new BusinessObject_Id(this.getAssetBusObjCd()));
		asset.setCreDt(currentDate);
		//asset.setEndDate(value);
		asset.newEntity();
		 LOGGER.info("assetId:: " + asset.getEntity().getId());
		return asset.getEntity().getId();
	}
	

	/**
	 * @param accId
	 * @param serviceType
	 * @param assetId
	 * @return
	 */
	private String findOrCreateTaxRole(String accId, String serviceType, Asset_Id assetId) {

		PreparedStatement psPreparedStatement = null;
		psPreparedStatement = createPreparedStatement(
				"SELECT TAX_ROLE_ID FROM CI_TAX_ROLE WHERE SVC_TYPE_CD = \'"+serviceType+"\' AND ACCT_ID = \'"+accId+"\' AND ASSET_ID= \'"+assetId+"\'",
				"SELECT");
		psPreparedStatement.setAutoclose(false);
		String taxRoleId = null;
		QueryIterator<SQLResultRow> result = null;

		try {
			result = psPreparedStatement.iterate();
			while (result.hasNext()) {
				SQLResultRow lookUpValue = result.next();
				System.out.println(lookUpValue.getString("TAX_ROLE_ID"));
				taxRoleId = lookUpValue.getString("TAX_ROLE_ID");
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
			result.close();
		}
		
		if(isBlankOrNull(taxRoleId)) {
	    	TaxRole_DTO taxRole = createDTO(TaxRole.class);
		    taxRole.setAccountId(new Account_Id(accountId));
		    taxRole.setAssetId(assetId);
		    String[] serviceTypeArray = this.getServiceType().split(",");
		    Map<String, String> serviceTypeMap = getLinkedMap(serviceTypeArray);
			String serviceTypeValue = "";
			serviceTypeValue =  serviceTypeMap.get("RETIRE");
		    taxRole.setServiceTypeId(new ServiceType_Id(serviceTypeValue));
		    taxRole.setStartDate(currentDate);//current date passed
		    taxRole.setBusinessObjectId(new BusinessObject_Id(this.getTaxRoleBusObjCd()));
		    taxRole.newEntity();
		    taxRoleId = taxRole.getEntity().getId().getIdValue();
		    LOGGER.info("taxRoleId:: " + taxRole.getEntity().getId());
	    }
		
		return taxRoleId;
	}
	
	//Find the obligation linked to this acc of tax type and tax role id and which is Active//C1-TXRLOBLC//C1-OBLTAXR
	/**
	 * @param taxRoleId
	 * @return
	 */
	private String findOrCreateObligation(String taxRoleId) {

		LOGGER.info("Enters Inside findOrCreateObligation:: "+ taxRoleId);
		PreparedStatement psPreparedStatement = null;
		psPreparedStatement = createPreparedStatement("select SA_ID from CI_SA where SA_STATUS_FLG NOT IN ('60','70') and TAX_ROLE_ID= \'"+taxRoleId+"\'",
				"SELECT");
		psPreparedStatement.setAutoclose(false);
		String obligationId = null;
		QueryIterator<SQLResultRow> result = null;

		try {
			result = psPreparedStatement.iterate();
			while (result.hasNext()) {
				SQLResultRow lookUpValue = result.next();
				System.out.println(lookUpValue.getString("SA_ID"));
				obligationId = lookUpValue.getString("SA_ID");
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
			result.close();
		}
		
		if(isBlankOrNull(obligationId)) {		    
		    ServiceAgreement_DTO obligation = createDTO(ServiceAgreement.class);
		    obligation.setAccountId(new Account_Id(accountId));
		    obligation.setTaxRoleId(new TaxRole_Id(taxRoleId));
		    //String division = getDivisionByTaxRoleId(taxRoleId);
		    //obligation.setServiceAgreementTypeId(new ServiceAgreementType_Id(new CisDivision_Id(division), "PRENATAL"));
		    String[] obligationTypeArray = this.getObligationType().split(",");
		    Map<String, String> obligationTypeMap = getLinkedMap(obligationTypeArray);
			String obligationTypeValue = "";
			obligationTypeValue =  obligationTypeMap.get("RETIRE");
		    obligation.setServiceAgreementTypeId(new ServiceAgreementType_Id(new CisDivision_Id("CSS"), obligationTypeValue));
		    obligation.setFilingPeriodId(filingPeriod);
		    obligation.setStartDate(filingPeriod.getEndDate());//current date check
		    obligation.setEndDate(filingPeriod.getEndDate());//remove this
		    obligation.setStatus(ServiceAgreementStatusLookup.constants.PENDING_START);
		    obligation.newEntity();
		    
		    System.out.println(obligation.getEntity().getId());
		    obligationId = obligation.getEntity().getId().getIdValue();
		  }
		LOGGER.info("Exits Inside findOrCreateObligation:: "+ obligationId);
		return obligationId;
	}

	private String getPersonByNin(String idNumber) {
		
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-PersonSearchByIdTypeNumber");
		String personId = null;
		bsInstance.set("idType", "NIN");
		bsInstance.set("idNumber", idNumber);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);

		// Getting the list of results
		COTSInstanceList list = bsInstance.getList("results");

		// If list IS NOT empty
		if (!list.isEmpty()) {

			// Get the first result
			COTSInstanceListNode firstRow = list.iterator().next();

			// Return the person entity
			System.out.println(firstRow.getString("personId"));
			personId = firstRow.getString("personId");

		}
		return personId;

	}
	

	public String getDivisionByTaxRoleId(String taxRoleId) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-TXRLDIVS");//8629186835

		bsInstance.set("taxRoleId", taxRoleId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		COTSInstanceList list = bsInstance.getList("results");
		String resultat = null;
		if (!list.isEmpty()) {
			COTSInstanceListNode nextElt = list.iterator().next();
			if (nextElt != null) {
				System.out.println("Division: " + nextElt.getString("division"));
				System.out.println("Description: " + nextElt.getString("description"));
				resultat = nextElt.getString("division");
			}

		}
		return resultat;
	}

	/**
	 * @param personId
	 * @return
	 */
	private String getAccountsByIdPerson(String personId) {
		String accountId = null;
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GetPersonAccounts");

		bsInstance.set("personId", personId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);

		Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			System.out.println("AccountId: " + nextElt.getNumber("accountId"));
			System.out.println("AccountInfo: " + nextElt.getString("accountInfo"));
			accountId = nextElt.getXMLString("accountId");

		}
		return accountId;
	}
	
	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		// TODO Auto-generated method stub
		this.boKey = arg0;
	}

}
