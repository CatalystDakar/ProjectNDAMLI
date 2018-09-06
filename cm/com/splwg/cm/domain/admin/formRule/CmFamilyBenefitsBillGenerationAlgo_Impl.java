package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
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
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_DTO;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_Id;

/**
 * @author Denash Kumar M
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = benefitType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = assetType, required = true, type = string)
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
 *            , @AlgorithmSoftParameter (name = postNatalFactorId, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = prenatalFactorId, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = calculationControlId, required = true, type = string)})
 */
public class CmFamilyBenefitsBillGenerationAlgo_Impl extends CmFamilyBenefitsBillGenerationAlgo_Gen
		implements BusinessObjectExitStatusAlgorithmSpot {

	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger( CmFamilyBenefitsBillGenerationAlgo_Impl.class );
	private BusinessObjectInstance boInstance;
	private BusinessObjectInstanceKey boKey;
	String processFlowId = null;
	String benefitType = null;
	String ninNumber = null;
	String personId = null;
	String accountId = null;
	private Date effectiveDatePre;
	private Date effectiveDatePost;
	FilingPeriod_Id filingPeriod = FilingPeriod_Id.NULL;
	GregorianCalendar gregorianCalendar = new GregorianCalendar(); 
	Date currentDate = new Date(gregorianCalendar.get(GregorianCalendar.YEAR),
			gregorianCalendar.get(GregorianCalendar.MONTH), gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
	Date billMonitorDate = new Date(gregorianCalendar.get(GregorianCalendar.YEAR),
			gregorianCalendar.get(GregorianCalendar.MONTH)+1, gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
	
	@Override
	public void invoke() {
	
		this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		LOGGER.info("I am In Invoke method BO intance " + this.boInstance);
	    COTSFieldDataAndMD<?> processFlowNode = this.boInstance.getFieldAndMDForPath("processFlowId");
	    processFlowId = processFlowNode.getValue().toString();
	    LOGGER.info("ProcessFlowId :: " + processFlowId);
	    System.out.println("ProcessFlowId :: " + processFlowId);
	    
	    COTSFieldDataAndMD<?> benefitTypeNode = this.boInstance.getFieldAndMDForPath("benefitDetails/benefitType");
	    benefitType = benefitTypeNode.getValue().toString();
	    
	    LOGGER.info("benefitType :: " + benefitType);
	    System.out.println("benefitType :: " + benefitType);
	    
	    COTSFieldDataAndMD<?> ninNumberNode = this.boInstance.getFieldAndMDForPath("nin/NIN");
	    ninNumber = ninNumberNode.getValue().toString();
	    
	    LOGGER.info("ninNumber :: " + ninNumber);
	    System.out.println("ninNumber :: " + ninNumber);
	    COTSFieldDataAndMD<?> cotsRegId = this.boInstance.getFieldAndMDForPath("billDetail/billId");
		
	/*	benefitType = "POST";
		ninNumber = "97534689";*/
	    
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

				if ("PRE".equalsIgnoreCase(benefitType.trim())) {
					Map<String, Bool> preNatalMap = getPrenatalDocMap();
					LOGGER.info("preNatalMap :: " + preNatalMap);
					System.out.println("preNatalMap :: " + preNatalMap);
					int sequence = Integer.parseInt(getSequence("PRENATAL VALUATION"));
					for (Map.Entry<String, Bool> preNata : preNatalMap.entrySet()) {
						if (!isNull(preNata.getValue()) && preNata.getValue().isTrue()) {
							LOGGER.info("Sequence:: " + sequence);
							String factorId = this.getPrenatalFactorId();
							effectiveDatePre = getEffectiveDate("CM-PRENATAL");
							String factorCharValue = null;
							if ("DOCUMENT1".equalsIgnoreCase(preNata.getKey().trim())) {
								factorCharValue = "DOCUMENT1";
							} else if ("DOCUMENT2".equalsIgnoreCase(preNata.getKey().trim())) {
								factorCharValue = "DOCUMENT2";
							} else if ("DOCUMENT3".equalsIgnoreCase(preNata.getKey().trim())) {
								factorCharValue = "DOCUMENT3";
							}
							BigDecimal detailValue = getFactorValue(factorId, String.valueOf(effectiveDatePre),
									factorCharValue);
							
							LOGGER.info("Factor Value:: " + detailValue);
							System.out.println("Factor Value:: " + detailValue);

							ValuationDetail_DTO valuationDetailDTO = createDTO(ValuationDetail.class);
							valuationDetailDTO.setDetailValue(detailValue);
							valuationDetailDTO.setCurrencyId(new Currency_Id(currency.getId().getIdValue()));
							String[] valuationDetailTypeArray = this.getValuationDetailType().split(",");
							String valuationDetailTypeValue = "";
							if("PRE".equalsIgnoreCase(benefitType)) {
								valuationDetailTypeValue =  valuationDetailTypeArray[0];
							} else {
								valuationDetailTypeValue =  valuationDetailTypeArray[1];
							}
							valuationDetailDTO.setValueDetailTypeId(new ValueDetailType_Id(valuationDetailTypeValue));
							valuationDetailDTO.setId(new ValuationDetail_Id(valuationDetailId, BigInteger.valueOf(sequence)));
							valuationDetailDTO.newEntity();
							System.out.println("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
							LOGGER.info("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
							sequence++;

						}
					}

				} else if ("POST".equalsIgnoreCase(benefitType.trim())) {
					Map<String, Bool> postNatalMap = getPostnatalDocMap();
					LOGGER.info("postNatalMap :: " + postNatalMap);
					System.out.println("postNatalMap :: " + postNatalMap);
					int sequence = Integer.parseInt(getSequence("POSTNATAL VALUATION"));
					for (Map.Entry<String, Bool> postNatal : postNatalMap.entrySet()) {
						if (!isNull(postNatal.getValue()) && postNatal.getValue().isTrue()) {
							String factorId = this.getPostNatalFactorId();
							effectiveDatePost = getEffectiveDate("CM-POSTNATAL");
							String factorCharValue = null;
							if ("DOCUMENT4".equalsIgnoreCase(postNatal.getKey().trim())) {
								factorCharValue = "DOCUMENT4";
							} else if ("DOCUMENT5".equalsIgnoreCase(postNatal.getKey().trim())) {
								factorCharValue = "DOCUMENT5";
							} else if ("DOCUMENT6".equalsIgnoreCase(postNatal.getKey().trim())) {
								factorCharValue = "DOCUMENT6";
							} else if ("DOCUMENT7".equalsIgnoreCase(postNatal.getKey().trim())) {
								factorCharValue = "DOCUMENT7";
							} else if ("DOCUMENT8".equalsIgnoreCase(postNatal.getKey().trim())) {
								factorCharValue = "DOCUMENT8";
							}
							BigDecimal detailValue = getFactorValue(factorId, String.valueOf(effectiveDatePost),
									factorCharValue);
							LOGGER.info("Factor Value:: " + detailValue);
							ValuationDetail_DTO valuationDetailDTO = createDTO(ValuationDetail.class);
							valuationDetailDTO.setDetailValue(detailValue);
							valuationDetailDTO.setCurrencyId(new Currency_Id(currency.getId().getIdValue()));
							String[] valuationDetailTypeArray = this.getValuationDetailType().split(",");
							String valuationDetailTypeValue = "";
							if("PRE".equalsIgnoreCase(benefitType)) {
								valuationDetailTypeValue =  valuationDetailTypeArray[0];
							} else {
								valuationDetailTypeValue =  valuationDetailTypeArray[1];
							}
							valuationDetailDTO.setValueDetailTypeId(new ValueDetailType_Id(valuationDetailTypeValue));
							valuationDetailDTO.setId(new ValuationDetail_Id(valuationDetailId, BigInteger.valueOf(sequence)));
							valuationDetailDTO.newEntity();
							System.out.println("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
							LOGGER.info("valuationDetailId:: " + valuationDetailDTO.getEntity().getId().toString());
							sequence++;

						}
					}
				}
				String obligationId = findOrCreateObligation(taxRoleId);

				TaxBill_Id taxbBillId = generateBill(taxRoleId, obligationId);
				cotsRegId.setXMLValue(taxbBillId.getEntity().getId().getIdValue());
				BusinessObjectDispatcher.update(this.boInstance);//updating the BO
			} else {
				 LOGGER.info("Account is not found for the Person ID:: " + personId);
				 addWarning(CmMessageRepository90000.MSG_6005(personId));
			}

		} else {
	    	 LOGGER.info("PersonId is not found for the NIN:: " + ninNumber);
	    	 addWarning(CmMessageRepository90000.MSG_6004(ninNumber));
	    }
	}

	/**
	 * @param factorType
	 * @return
	 */
	private Date getEffectiveDate(String factorType) {

		PreparedStatement preparedStatement = createPreparedStatement("SELECT EFFDT FROM C1_FACTOR_VALUE where FACTOR_CD= \'"+factorType+"\' order by EFFDT DESC","SELECT");
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
		return endDate;
	}

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
		
		LOGGER.info("Enters inside generateBill : taxRoleId: " + taxRoleId +" obligationId::" + obligationId);
		TaxBill_DTO billDTO = createDTO(TaxBill.class);
		billDTO.setTaxRoleId(new TaxRole_Id(taxRoleId));
	
		String[] billTypeArray = this.getBillType().split(",");
		String billTypeValue = null;
		
		String[] calcControlArray = this.getCalculationControlId().split(",");//POSTNATAL CALC,PRENATAL CALC, FAMILY C
		String calcControlValue = null;
		Map<String, String> calcMap = new HashMap<String, String>();
		for (int i = 0; i < calcControlArray.length; i++) {
			if(calcControlArray[i].contains("PRENATAL")) {
				calcMap.put("PRENATAL", calcControlArray[i]);
			} else if(calcControlArray[i].contains("POSTNATAL")) {
				calcMap.put("POSTNATAL", calcControlArray[i]);
			}
		}
		if("PRE".equalsIgnoreCase(benefitType)) {
			billTypeValue =  billTypeArray[0];
			//calcControlValue = calcControlArray[0];
			calcControlValue = calcMap.get("PRENATAL");
			billDTO.setTaxBillTypeId(new TaxBillType_Id(billTypeValue));
			billDTO.setCalculationControlVersionId(new CalculationControlVersion_Id(new CalculationControl_Id(calcControlValue), effectiveDatePre));
		} else {
			billTypeValue =  billTypeArray[1];
			//calcControlValue = calcControlArray[1];
			calcControlValue = calcMap.get("POSTNATAL");
			billDTO.setTaxBillTypeId(new TaxBillType_Id(billTypeValue));
			billDTO.setCalculationControlVersionId(new CalculationControlVersion_Id(new CalculationControl_Id(calcControlValue), effectiveDatePost));
		}
		
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
		String[] assetTypeArray = this.getAssetType().split(",");
		String assetType = "";
		if("PRE".equalsIgnoreCase(benefitType)) {
			 assetType =  assetTypeArray[0];
		} else {
			assetType =  assetTypeArray[1];
		}
		asset.setAssetTypeId(new AssetType_Id(assetType));
		asset.setStatus("ACTIVE");
		asset.setBusinessObjectId(new BusinessObject_Id(this.getAssetBusObjCd()));
		asset.setCreDt(currentDate);
		asset.newEntity();
		
		return asset.getEntity().getId();
	}
	
	/**
	 * @return
	 */
	private Map<String, Bool> getPostnatalDocMap() {
		Bool docValue4 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document4").getValue();
		Bool docValue5 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document5").getValue();
		Bool docValue6 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document6").getValue();
		Bool docValue7 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document7").getValue();
		Bool docValue8 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document8").getValue();

		Map<String, Bool> postNatalMap = new LinkedHashMap<String, Bool>();
		postNatalMap.put("document4", docValue4);
		postNatalMap.put("document5", docValue5);
		postNatalMap.put("document6", docValue6);
		postNatalMap.put("document7", docValue7);
		postNatalMap.put("document8", docValue8);
		return postNatalMap;
	}

	/**
	 * @return
	 */
	private Map<String, Bool> getPrenatalDocMap() {
		LOGGER.info("***Inside getPrenatalDocMap");
		Bool docValue1 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document1").getValue();
		Bool docValue2 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document2").getValue();
		Bool docValue3 = (Bool) this.boInstance.getFieldAndMDForPath("docDetails/document3").getValue();
		Map<String, Bool> preNatalMap = new LinkedHashMap<String, Bool>();
		preNatalMap.put("document1", docValue1);
		preNatalMap.put("document2", docValue2);
		preNatalMap.put("document3", docValue3);
		LOGGER.info("***Exits getPrenatalDocMap");
		return preNatalMap;
		
	}

	/**
	 * @param factorId
	 * @param effectiveDate
	 * @return
	 */
	private BigDecimal getFactorValue(String factorId, String effectiveDate, String factorCharValue) {

		LOGGER.info("Enters getFactorValue : " + "factorId:: " + factorId +"effectiveDate:: " + effectiveDate +"factorCharValue:: " + factorCharValue);
		System.out.println("Enters getFactorValue : " + "factorId:: " + factorId +"effectiveDate:: " + effectiveDate +"factorCharValue:: " + factorCharValue);
		PreparedStatement preparedStatement = createPreparedStatement(
				"SELECT FACTOR_VAL FROM C1_FACTOR_VALUE where FACTOR_CHAR_VAL=\'"+factorCharValue+"\' and FACTOR_CD=\'"+factorId+"\' and TO_CHAR(EFFDT,'YYYY-MM-DD') <=\'"+effectiveDate+"\' order by EFFDT DESC",
				"SELECT");
		/*preparedStatement.bindString("factorCharValue", factorCharValue, null);
		preparedStatement.bindString("factor", factorId, null);
		preparedStatement.bindString("effectiveDate", effectiveDate, null);*/
		preparedStatement.setAutoclose(false);
		BigDecimal factorValue = BigDecimal.ZERO;
		QueryIterator<SQLResultRow> result = null;

		try {
			result = preparedStatement.iterate();
			while (result.hasNext()) {
				SQLResultRow lookUpValue = result.next();
				System.out.println(lookUpValue.getString("FACTOR_VAL"));
				factorValue = BigDecimal.valueOf(Double.valueOf(lookUpValue.getString("FACTOR_VAL")));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			preparedStatement.close();
			preparedStatement = null;
			result.close();
		}
		LOGGER.info("Exits getFactorValue:: " + factorValue);
		return factorValue;
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
				"select TAX_ROLE_ID from CI_TAX_ROLE where SVC_TYPE_CD = \'"+serviceType+"\' and ACCT_ID = \'"+accId+"\'",
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
			String serviceTypeValue = "";
			if("PRE".equalsIgnoreCase(benefitType)) {
				serviceTypeValue =  serviceTypeArray[0];
			} else {
				serviceTypeValue =  serviceTypeArray[1];
			}
		    taxRole.setServiceTypeId(new ServiceType_Id(serviceTypeValue));
		    taxRole.setStartDate(currentDate);//current date passed
		    //taxRole.setBusinessObjectId(new BusinessObject_Id("C1-TaxRoleAsset"));
		    taxRole.setBusinessObjectId(new BusinessObject_Id(this.getTaxRoleBusObjCd()));
		    taxRole.newEntity();
		    taxRoleId = taxRole.getEntity().getId().getIdValue();
		    System.out.println(taxRole.getEntity().getId());
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
			String obligationTypeValue = "";
		    if("PRE".equalsIgnoreCase(benefitType)) {
				obligationTypeValue =  obligationTypeArray[0];
			} else {
				obligationTypeValue =  obligationTypeArray[1];
			}
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
