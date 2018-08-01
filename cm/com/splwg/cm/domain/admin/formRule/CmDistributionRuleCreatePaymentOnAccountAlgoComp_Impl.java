package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.cm.domain.customMessages.CmMessageRepository90002;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.PaymentStatusLookup;
import com.splwg.tax.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.tax.domain.admin.distributionRule.DistributionRule;
import com.splwg.tax.domain.admin.distributionRule.DistributionRuleCreatePaymentAlgorithmSpot;
import com.splwg.tax.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCode;
import com.splwg.tax.domain.admin.matchType.MatchType;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.tax.domain.payment.payment.CreateDistributeFreezePayment;
import com.splwg.tax.domain.payment.payment.CreateDistributeFreezePayment.Factory;
import com.splwg.tax.domain.payment.payment.Payment;
import com.splwg.tax.domain.payment.payment.PaymentSegment;
import com.splwg.tax.domain.payment.payment.PaymentSegment_DTO;
import com.splwg.tax.domain.payment.payment.Payment_DTO;
import com.splwg.tax.domain.payment.payment.Payment_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent;

/**
 * @author Denash Kumar M
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = accountType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = partialPayment, type = string)
 *            , @AlgorithmSoftParameter (name = obligationType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = obligationOverPayment, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeContribution, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypePenality, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeMajoration, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTyepeOverpayment, required = true, type = string)})
 */
public class CmDistributionRuleCreatePaymentOnAccountAlgoComp_Impl extends
		CmDistributionRuleCreatePaymentOnAccountAlgoComp_Gen implements DistributionRuleCreatePaymentAlgorithmSpot {	
	private static final Logger logger = LoggerFactory
			.getLogger(CmDistributionRuleCreatePaymentOnAccountAlgoComp_Impl.class);

	private PaymentEvent paymentEvent;
	private DistributionRule distributionRule;
	private Money amount;
	private String characteristicValueFk1;
	private BigInteger sequence;
	private Payment_Id paymentId;
	PreparedStatement psPreparedStatement = null;
	private String accId = null;
	private CmDistributionRuleOblMoneyDTO cmObligationMoneyDTO = new CmDistributionRuleOblMoneyDTO();
	private CMDistributionRulePeriodOblListMoneyListDTO cmPeriodObligationMoneyDTO = new CMDistributionRulePeriodOblListMoneyListDTO();
	Money epfAmount = Money.ZERO;
	Money erAmount = Money.ZERO;
	Money atmpAmount = Money.ZERO;
	
	@Override
	public void invoke() {
		
		logger.info("characteristicFK: " + this.characteristicValueFk1);
		System.out.println("characteristicFK: " + this.characteristicValueFk1);

		logger.info("Amount: " + this.amount);
		System.out.println("Amount: " + this.amount);		

		logger.info("Sequence: " + this.sequence);
		System.out.println("Sequence: " + this.sequence);		

		logger.info("paymentEvent: " + this.paymentEvent);
		System.out.println("paymentEvent: " + this.paymentEvent);//12321324
		
		LinkedHashMap<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO> debtOblMap = getDebtObligation(
				this.characteristicValueFk1);// 1000
		
		logger.info("debtOblMap: " + debtOblMap.size());
		System.out.println("debtOblMap: " + debtOblMap.size());
		logger.info("debtOblMap: " + debtOblMap);
		System.out.println("debtOblMap: " + debtOblMap);
		ServiceAgreement debtObligation = null;
		Money debtMoney = Money.ZERO;
		Money moneyValue = Money.ZERO;
		Money actualMoneyValue = this.amount;
		String periodValue = null;
		if(!debtOblMap.isEmpty()) {
			for(Map.Entry<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO> debtMapObj : debtOblMap.entrySet()){
				cmObligationMoneyDTO = debtMapObj.getKey();
				cmPeriodObligationMoneyDTO = debtMapObj.getValue();
				Map<String,Money> moneyMapkey = cmObligationMoneyDTO.getMapOblMoney();
				Map<String, HashMap<List<String>,List<Money>>> moneyMap = cmPeriodObligationMoneyDTO.getPeriodOblMoney();
				if(!moneyMapkey.isEmpty()) {
				
				for(Map.Entry<String, Money> moneyMapObj : moneyMapkey.entrySet() ){
					Money moneyMapList = moneyMapObj.getValue();
					moneyValue = moneyMapList.add(moneyValue);
				}
				
				logger.info("Sum of Obligation Amount:: " + moneyValue);
				System.out.println("Sum of Obligation Amount:: " + moneyValue);
				
				if(!moneyValue.isZero() && this.amount.isGreaterThan(moneyValue)) {
					logger.info("###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##" );
					System.out.println("###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##" );
					Map<String,Money> moneyMapValue = cmObligationMoneyDTO.getMapOblMoney();
					for (Map.Entry<String, Money> moneyEntry : moneyMapValue.entrySet()) {
						ServiceAgreement_Id sa_id = new ServiceAgreement_Id(moneyEntry.getKey());
						logger.info("ServiceAgreement_Id: " + sa_id);
						System.out.println("ServiceAgreement_Id, : " + sa_id);
						debtObligation = (ServiceAgreement) sa_id.getEntity();
						logger.info("ServiceAgreement: " + debtObligation);
						System.out.println("ServiceAgreement: " + debtObligation);
						debtMoney = moneyEntry.getValue();
						int payAmount = Math.round(debtMoney.getAmount().floatValue());
						debtMoney = new Money(String.valueOf(payAmount), new Currency_Id("XOF"));
						logger.info("DebtMoney: " + debtMoney);
						logger.info("Amount before the payment creation:: " + this.amount);
						System.out.println("Amount before the payment creation:: " + this.amount);
						if (!this.amount.isZero() && this.amount.isPositive()) {
							this.createFrozenPayment(debtObligation, debtMoney);
						}
			        }
					
					if(!this.amount.isZero() && this.amount.isPositive()) {
						logger.info("********Creating OverPayment and the Amount is*******" + this.amount);						
						Map<String, String> accountDetailsMap = getAllAccountDetailsFromAccountId(this.characteristicValueFk1);
						Money overPayAmount = this.amount;
						Money splitMoney = null;
						if(!accountDetailsMap.isEmpty()) {
							String accntId = null;
							int prorateMoney = 0;
							Money roundOff = Money.ZERO;
							int count = 1;
							for(Map.Entry<String, String> accntIdMap : accountDetailsMap.entrySet()) {
								if("OLDAGE".equalsIgnoreCase(accntIdMap.getValue())) {
									accntId = accntIdMap.getKey();
									if (count == accountDetailsMap.size()) {
										splitMoney = roundOff;
									} else {
										prorateMoney = Math.round(erAmount.getAmount().floatValue() / moneyValue.getAmount().floatValue() * overPayAmount.getAmount().floatValue());
										splitMoney = new Money(String.valueOf(prorateMoney), new Currency_Id("XOF"));
									}
									logger.info("OverPayment amount for Account OLDAGE:: " + prorateMoney);
								} else if("ATMP".equalsIgnoreCase(accntIdMap.getValue())) {
									accntId = accntIdMap.getKey();
									if (count == accountDetailsMap.size()) {
										splitMoney = roundOff;
									} else {
										prorateMoney = Math.round(atmpAmount.getAmount().floatValue() / moneyValue.getAmount().floatValue() * overPayAmount.getAmount().floatValue());
										splitMoney = new Money(String.valueOf(prorateMoney), new Currency_Id("XOF"));
									}
									logger.info("OverPayment amount for Account ATMP:: " + prorateMoney);
								} else if("PF".equalsIgnoreCase(accntIdMap.getValue())) {
									accntId = accntIdMap.getKey();
									if (count == accountDetailsMap.size()) {
										splitMoney = roundOff;
									} else {
										prorateMoney = Math.round(epfAmount.getAmount().floatValue() / moneyValue.getAmount().floatValue() * overPayAmount.getAmount().floatValue());
										splitMoney = new Money(String.valueOf(prorateMoney), new Currency_Id("XOF"));
									}
									logger.info("OverPayment amount for Account PF:: " + prorateMoney);
								}
								
								String obligationId = createObligation(accntId,"DOR", getObligationOverPayment());
								logger.info("OverPayment Created against the Obligation ID"+ obligationId +"for account ID:"+accntId);
								
								ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligationId);
								logger.info("ServiceAgreement_Id: " + sa_id);
								System.out.println("ServiceAgreement_Id, : " + sa_id);
								debtObligation = (ServiceAgreement) sa_id.getEntity();
								logger.info("ServiceAgreement: " + debtObligation);
								System.out.println("ServiceAgreement: " + debtObligation);
								logger.info("DebtMoney: " + splitMoney);
								logger.info("Amount before the payment creation:: " + this.amount);
								System.out.println("Amount before the payment creation:: " + this.amount);
								if (!splitMoney.isZero() && splitMoney.isPositive()) {
									this.createFrozenPayment(debtObligation, splitMoney);
									roundOff = overPayAmount.subtract(splitMoney);
									logger.info("RoundOff Amount after the payment creation:: " + roundOff);
									System.out.println("RoundOff Amount after the payment creation:: " + roundOff);
									count ++;
								}
							}
							//String adjustmentId = createAdjustment(obligationId,  getAdjustmentType7(), this.amount, "OVERPAY", getSystemDateTime().getDate());
							//logger.info("OverPayment Created against the Adjustment ID: " + adjustmentId);
						}
					}
						
				} else {
					if(getPartialPayment().trim().equalsIgnoreCase("O")) {
						for(Entry<String, HashMap<List<String>, List<Money>>> moneyMapObj : moneyMap.entrySet()) {
							  periodValue = moneyMapObj.getKey(); 
							  actualMoneyValue = this.amount;
							  HashMap<List<String>,List<Money>>  finalMoneyMap = moneyMapObj.getValue();
							  Money monthObligationMoney = Money.ZERO;
							  for(Map.Entry<List<String>, List<Money>> moneyEntry : finalMoneyMap.entrySet()) {
								  List<String> obligIdList = moneyEntry.getKey();
								  if(!isNull(moneyEntry) && moneyEntry.getValue().size()>=1) { 
									  List<Money> moneyList = moneyEntry.getValue();
									 	for(int i=0;i<moneyList.size();i++){
									 		monthObligationMoney = moneyList.get(i).add(monthObligationMoney);
									 	}
									 	if(!monthObligationMoney.isZero() && this.amount.isLessThanOrEqual(monthObligationMoney)) {
									 		logger.info("###Creating payment for same month obligations####" );
											System.out.println("###Creating payment for same month obligations####" );
									 		for(int i=0;i<moneyList.size();i++){
										 		Money obligationMoney = moneyList.get(i);
										 		logger.info("obligation Money: " + obligationMoney);
										 		logger.info("Screen Amount: " + this.amount);
										 		logger.info("Month Obligation Money: " + monthObligationMoney);
										 		logger.info("Actual Money Value: " + actualMoneyValue);
										 		String oblStr = obligIdList.get(i);
												int prorateMoney = Math.round(actualMoneyValue.getAmount().floatValue()/monthObligationMoney.getAmount().floatValue()*obligationMoney.getAmount().floatValue());
										 		debtMoney = new Money(String.valueOf(prorateMoney), new Currency_Id("XOF"));
										 		ServiceAgreement_Id sa_id = new ServiceAgreement_Id(oblStr);
												logger.info("ServiceAgreement_Id: " + sa_id);
												System.out.println("ServiceAgreement_Id: " + sa_id);
												debtObligation = (ServiceAgreement) sa_id.getEntity();
												logger.info("ServiceAgreement: " + debtObligation);
												System.out.println("ServiceAgreement: " + debtObligation);
												logger.info("prorateMoney: " + debtMoney);
												logger.info("Amount before the payment creation:: " + this.amount);
												System.out.println("Amount before the payment creation:: " + this.amount);
												if (!this.amount.isZero() && this.amount.isPositive()) {
													this.createFrozenPayment(debtObligation, debtMoney);
												}
										 	}
									 	} else { 
									 		logger.info("###Creating payment for sequence month obligations####" );
											System.out.println("###Creating payment for sequence month obligations####" );
									 		for (Map.Entry<List<String>,List<Money>> moneyEntryy : finalMoneyMap.entrySet()) {
									 			List<String> obligIdListt = moneyEntryy.getKey();
									 			List<Money> moneyListt = moneyEntry.getValue();
									 			for(int i=0;i<obligIdListt.size();i++){
									 				ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligIdListt.get(i));
													logger.info("ServiceAgreement_Id: " + sa_id);
													System.out.println("ServiceAgreement_Id: " + sa_id);
													debtObligation = (ServiceAgreement) sa_id.getEntity();
													logger.info("ServiceAgreement: " + debtObligation);
													System.out.println("ServiceAgreement: " + debtObligation);
													debtMoney = moneyListt.get(i);
													int payAmount = Math.round(debtMoney.getAmount().floatValue());
													debtMoney = new Money(String.valueOf(payAmount), new Currency_Id("XOF"));
													System.out.println("DebtMoney: " + debtMoney);
													logger.info("DebtMoney:" + debtMoney);
													logger.info("Amount before the payment creation :: " + this.amount);
													System.out.println("Amount before the payment creation:: " + this.amount);
													if (!this.amount.isZero() && this.amount.isPositive()) {
														this.createFrozenPayment(debtObligation, debtMoney);
													}
											}
										}
									}
								}
							}
						}
					} else {
						addError(CmMessageRepository90002.MSG_300());
					}
					
				}
			} else{
				logger.info("There is no obligation to pay,Money Map Key value is empty.");
			}
		}
	} else {
			logger.info("There is no obligation to pay, getDebtObligation is empty");
	}
}

	private Map<String, String> getAllAccountDetailsFromAccountId(String accountId) {
		
		PreparedStatement accntPreparedStatement = null;
		QueryIterator<SQLResultRow> accntResultIterator = null;
		Map<String, String> accountDetailsMap = null;
		//accntPreparedStatement = createPreparedStatement("select ACCT_ID from CI_ACCT_PER where PER_ID in ( select PER_ID from CI_ACCT_PER where ACCT_ID = \'"+characteristicValueFk12+"\' ) ", "select");
		
		accntPreparedStatement = createPreparedStatement("SELECT ACCT.ACCT_ID, ACCT.CUST_CL_CD FROM CI_ACCT_PER ACCPER, CI_ACCT ACCT"
				+ " WHERE ACCT.ACCT_ID=ACCPER.ACCT_ID AND ACCPER.PER_ID IN"
				+ " ( SELECT PER_ID FROM CI_ACCT_PER WHERE ACCT_ID = \'"+accountId+"\' )", "SELECT");
		accntPreparedStatement.setAutoclose(false);
		try {
			startChanges();
			accountDetailsMap = new HashMap<String, String>();
			accntResultIterator = accntPreparedStatement.iterate();
			while (accntResultIterator.hasNext()) {
				SQLResultRow lookUpValue = accntResultIterator.next();
				accountDetailsMap.put(lookUpValue.getString("ACCT_ID").trim(), lookUpValue.getString("CUST_CL_CD").trim());
			}
		} catch (Exception excep) {
			logger.error("Exception in getting  getAllAccountDetailsFromAccountId : " + excep);
		} finally {
			saveChanges();
			accntPreparedStatement.close();
			accntResultIterator.close();
			accntPreparedStatement = null;
		}
		return accountDetailsMap;
	}

	/**
	 * @param obligation
	 * @param money
	 */
	private void createFrozenPayment(ServiceAgreement obligation, Money money) {

		
		logger.info("***createFrozenPayment***: ");
		logger.info("Money: " + money);
		System.out.println("Money: " + money);
		
		logger.info("Money String: " + String.valueOf(money));
		System.out.println("Money String: " + String.valueOf(money));
		
		Payment_DTO paymentDTO = (Payment_DTO) this.createDTO(Payment.class);
		paymentDTO.setAccountId(obligation.getAccount().getId());
		PaymentSegment_DTO paymentSegmentDTO = (PaymentSegment_DTO) this.createDTO(PaymentSegment.class);
		if(this.amount.isLessThanOrEqual(money)) { //money = 4000, amount-screen = 5000
			paymentDTO.setPaymentAmount(this.amount);
			paymentSegmentDTO.setPaySegmentAmount(this.amount);
			this.amount = this.amount.subtract(money);
		} else if(this.amount.isGreaterThan(money)){
			this.amount = this.amount.subtract(money);
			paymentDTO.setPaymentAmount(money);
			paymentSegmentDTO.setPaySegmentAmount(money);
		}
		paymentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());
		paymentDTO.setSequence(this.sequence);
		paymentDTO.setPaymentEventId(this.paymentEvent.getId()); //new PaymentEvent_Id("245660788074")
		paymentDTO.setPaymentStatus(PaymentStatusLookup.constants.FREEZABLE);
		paymentSegmentDTO.setServiceAgreementId(obligation.getId());
		paymentSegmentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());	
		//MatchEvent_Id match = paymentSegmentDTO.getAdjustmentId().getEntity().getRelatedFinancialTransaction().getMatchEventId();
		//paymentSegmentDTO.setMatchEventId(match);
		Adjustment_Id linkedAdjustmentId = paymentSegmentDTO.getAdjustmentId();
		CreateDistributeFreezePayment createDistributeFreezePayment = Factory.newInstance();
		Payment payment = createDistributeFreezePayment.process(paymentDTO, paymentSegmentDTO, (Date) null, (Date) null,
				(GeneralLedgerDistributionCode) null, linkedAdjustmentId);
		if (this.notNull(payment)) {
			this.paymentId = payment.getId();
		}
		logger.info("paymentId: " +  payment.getId());
		System.out.println("paymentId: " +  payment.getId());

	}
	
	public String createObligation(String accountId, String division, String obligationType) {

		  // Business Service Instance
		  BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-FindCreateObligation");

		  // Populate BS parameters if available
		  if (null != accountId && null != division && null != obligationType) {
		   COTSInstanceNode group = bsInstance.getGroupFromPath("input");
		   group.set("accountId", accountId);
		   group.set("division", division);
		   group.set("obligationType", obligationType);
		  }

		  return executeBSAndCreateObligation(bsInstance);

		 }
	
	/**
	 * @param bsInstance
	 * @return
	 */
	private String executeBSAndCreateObligation(BusinessServiceInstance bsInstance) {
		  // TODO Auto-generated method stub
		  bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		  String obligationId = null;
		  System.out.println(getSystemDateTime().getDate());
		  // Getting the list of results
		  COTSInstanceNode group = bsInstance.getGroupFromPath("output");

		  // If list IS NOT empty
		  if (group != null) {
		   obligationId = group.getString("obligationId");
		  }
		  logger.info("obligationId " +obligationId); 
		  System.out.println("obligationId " +obligationId); 
		  return obligationId;

		 }
	
/*	*//**
	 * @param obligationId
	 * @param adjustmentType
	 * @param adjustmentAmount
	 * @param debtCat
	 * @param date
	 * @return
	 *//*
	private String createAdjustment(String obligationId, String adjustmentType, Money adjustmentAmount,String debtCat,
			com.splwg.base.api.datatypes.Date date) { 
		
		    BusinessServiceInstance businessServiceInstanc = BusinessServiceInstance.create("CM-AdjustmentAddFreeze");
			COTSInstanceNode cotsGroup = businessServiceInstanc.getGroupFromPath("input");
			cotsGroup.set("serviceAgreement", obligationId);
			cotsGroup.set("adjustmentType", adjustmentType);
			cotsGroup.set("adjustmentAmount", adjustmentAmount);
			cotsGroup.set("debtCategory", debtCat); 	
			cotsGroup.set("adjustmentDate", date);

		  return executeBSAndCreateAdjustment(businessServiceInstanc);
		  
	}
	
	*//**
	 * @param bsInstance
	 * @return
	 *//*
	private String executeBSAndCreateAdjustment(BusinessServiceInstance bsInstance) {
		  // TODO Auto-generated method stub
		  bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		  String adjustmentId = null;
		  // Getting the list of results
		  COTSInstanceNode group = bsInstance.getGroupFromPath("output");

		  if (group != null) {
			  adjustmentId = group.getString("adjustment");
		  }
		  logger.info("adjustmentId " +adjustmentId); 
		  System.out.println("adjustmentId " +adjustmentId); 
		  return adjustmentId;

		 }
*/
	
	@SuppressWarnings("deprecation")
	private LinkedHashMap<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO> getDebtObligation(String accId) {
		
		PreparedStatement psPreparedStatement = null;
		/*Date date = new Date(1950, 06, 01);//01/06/1950
		AlgorithmVersion algVersion = new AlgorithmVersion_Id(new Algorithm_Id("CM-PAYACCALG"), date).getEntity();
		List<String> softParametersList =  getRawParametersForValidating(algVersion, new Algorithm_Id("CM-PAYACCALG"));*/
		
		String accountType = getAccountType();
		String obligationContribution = getObligationType();
		String obligationOverPayment = getObligationOverPayment();
		String adjustmentTypeContribution = getAdjustmentTypeContribution();
		String adjustmentTypePenality = getAdjustmentTypePenality();
		String adjustmentTypeMajoration = getAdjustmentTypeMajoration();
		String adjustmentTyepeOverpayment = getAdjustmentTyepeOverpayment();
		
		 if(accountType.contains(",") || obligationContribution.contains(",") || obligationOverPayment.contains(",")
				 ||  adjustmentTypeContribution.contains(",") ||
				 adjustmentTypePenality.contains(",") || adjustmentTypeMajoration.contains(",") || adjustmentTyepeOverpayment.contains(",")){
			 String accntIdArr[] = accountType.split(",");
			 String obligationContributionArr[] = obligationContribution.split(","); 
			 String obligationOverPaymentArr[] = obligationOverPayment.split(","); 
			 String adjustmentTypeContributionArr[] = adjustmentTypeContribution.split(","); 
			 String adjustmentTypePenalityArr[] = adjustmentTypePenality.split(","); 
			 String adjustmentTypeMajorationArr[] = adjustmentTypeMajoration.split(","); 
			 String adjustmentTyepeOverpaymentArr[] = adjustmentTyepeOverpayment.split(","); 
			 accountType = "'" + StringUtils.join(accntIdArr,"','") + "'";
			 obligationContribution = "'" + StringUtils.join(obligationContributionArr,"','") + "'";
			 obligationOverPayment = "'" + StringUtils.join(obligationOverPaymentArr,"','") + "'";
			 adjustmentTypeContribution = "'" + StringUtils.join(adjustmentTypeContributionArr,"','") + "'";
			 adjustmentTypePenality = "'" + StringUtils.join(adjustmentTypePenalityArr,"','") + "'";
			 adjustmentTypeMajoration = "'" + StringUtils.join(adjustmentTypeMajorationArr,"','") + "'";
			 adjustmentTyepeOverpayment = "'" + StringUtils.join(adjustmentTyepeOverpaymentArr,"','") + "'";
		 }
			   
			    
		String period = null;
		HashMap<String, Money> debtOblMap = new HashMap<String, Money>();
		HashMap<String, HashMap<List<String>,List<Money>>> periodMap = new HashMap<String, HashMap<List<String>,List<Money>>>();
	    LinkedHashMap<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO> debtPriorityMap = new LinkedHashMap<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO>();
/*			psPreparedStatement = createPreparedStatement("SELECT CAP.ACCT_ID, CS.SA_ID, CS.SA_TYPE_CD, CS.SA_STATUS_FLG ,"
					+ "CADJ.ADJ_TYPE_CD, CADJ.ADJ_ID, CADJ.ADJ_AMT, CS.START_DT , CADJ.CRE_DT FROM CI_ACCT_PER CAP,"
					+ " CI_ACCT CA ,CI_SA CS,CI_ADJ CADJ WHERE CAP.PER_ID IN (SELECT PER_ID from CI_ACCT_PER where ACCT_ID = :accId)"
							+ " AND CAP.ACCT_ID=CA.ACCT_ID AND CA.CUST_CL_CD IN(:acc1,:acc2,:acc3) AND CAP.ACCT_ID = CS.ACCT_ID "
							+ " AND CS.SA_TYPE_CD in(:oblType1,:oblType2,:oblType3) AND CS.SA_ID = CADJ.SA_ID AND CADJ.ADJ_TYPE_CD IN "
							+ " (:adjType1,:adjType2,:adjType3,:adjType4,:adjType5,:adjType6,:adjType7) AND CS.SA_STATUS_FLG=40 ORDER BY CS.START_DT");
							*/
		
	    psPreparedStatement = createPreparedStatement(" SELECT CAP.ACCT_ID, CS.SA_ID, CS.SA_TYPE_CD, CS.SA_STATUS_FLG , "
	    				+" CADJ.ADJ_TYPE_CD, CADJ.ADJ_ID, CADJ.ADJ_AMT, CS.START_DT , CADJ.CRE_DT FROM CI_ACCT_PER CAP, "
	    				+" CI_ACCT CA ,CI_SA CS,CI_ADJ CADJ WHERE CAP.PER_ID IN (SELECT PER_ID from CI_ACCT_PER where ACCT_ID = \'"+accId+"\') "
	    				+" AND CAP.ACCT_ID=CA.ACCT_ID AND CA.CUST_CL_CD IN("+accountType+") AND CAP.ACCT_ID = CS.ACCT_ID  "
	    				+" AND CS.SA_TYPE_CD in("+obligationContribution+","+obligationOverPayment+") AND CS.SA_ID = CADJ.SA_ID AND CADJ.ADJ_TYPE_CD IN  "
	    				+" ("+adjustmentTypeContribution+","+adjustmentTypePenality+","+adjustmentTypeMajoration+","+adjustmentTyepeOverpayment+") AND CS.SA_STATUS_FLG=40 ORDER BY CS.START_DT");
	    
			psPreparedStatement.setAutoclose(false);
			QueryIterator<SQLResultRow> resultIterator = null;
			try {
		/*		psPreparedStatement.bindString("accId", accId, null);
				psPreparedStatement.bindString("acc1", acc1, null);
				psPreparedStatement.bindString("acc2", acc2, null);
				psPreparedStatement.bindString("acc3", acc3, null);
				psPreparedStatement.bindString("oblType1", oblType1, null);
				psPreparedStatement.bindString("oblType2", oblType2, null);
				psPreparedStatement.bindString("oblType3", oblType3, null);
				psPreparedStatement.bindString("adjType1", adjType1, null);
				psPreparedStatement.bindString("adjType2", adjType2, null);
				psPreparedStatement.bindString("adjType3", adjType3, null);
				psPreparedStatement.bindString("adjType4", adjType4, null);
				psPreparedStatement.bindString("adjType5", adjType5, null);
				psPreparedStatement.bindString("adjType6", adjType6, null);
				psPreparedStatement.bindString("adjType7", adjType7, null);*/
				
				resultIterator = psPreparedStatement.iterate();
				QueryIterator<SQLResultRow> oblResultIterator = null;			
				List<Money> moneyList = new ArrayList<Money>();
				List<String> oblgList = new ArrayList<String>();
				List<String> saIdList = new  ArrayList<String>();
				HashMap<List<String>, List<Money>> oblMoneyMap = new HashMap<List<String>, List<Money>>();
				while (resultIterator.hasNext()) {
					logger.info("getDebtObligation query Inside: ");
					SQLResultRow lookUpValue = resultIterator.next();
					System.out.println(lookUpValue.getString("SA_ID"));
					if(!saIdList.contains(lookUpValue.getString("SA_ID"))){
						saIdList.add(lookUpValue.getString("SA_ID"));
						
					try {
						psPreparedStatement = createPreparedStatement("SELECT SUM(CUR_AMT) AS \"Total\" from CI_FT where SA_ID = "+ lookUpValue.getString("SA_ID"), "select");
						psPreparedStatement.setAutoclose(false);
						oblResultIterator = psPreparedStatement.iterate();
						
						while (oblResultIterator.hasNext()) {
							logger.info("getDebtObligation total query inside: ");
							SQLResultRow oblResult = oblResultIterator.next();
							System.out.println(lookUpValue.getString("SA_ID"));
							if (oblResult.getString("Total") != null && Integer.parseInt(oblResult.getString("Total")) > 0) {
								debtOblMap.put(lookUpValue.getString("SA_ID"), new Money(oblResult.getString("Total"), new Currency_Id("XOF")));
								if(lookUpValue.getString("SA_TYPE_CD").trim().equalsIgnoreCase("O-EPF")) {
									Money oblAmount = new Money(oblResult.getString("Total"), new Currency_Id("XOF"));
									epfAmount = epfAmount.add(oblAmount);
								} else 	if(lookUpValue.getString("SA_TYPE_CD").trim().equalsIgnoreCase("O-ER")) {
									Money oblAmount = new Money(oblResult.getString("Total"), new Currency_Id("XOF"));
									erAmount = erAmount.add(oblAmount);
								} else if(lookUpValue.getString("SA_TYPE_CD").trim().equalsIgnoreCase("O-EATMP")) {
									Money oblAmount = new Money(oblResult.getString("Total"), new Currency_Id("XOF"));
									atmpAmount = atmpAmount.add(oblAmount);
								}
								logger.info("debtOblMap Total:: " + debtOblMap);
								if(null == period || (null != period && lookUpValue.getString("START_DT").equalsIgnoreCase(period))){
									period = lookUpValue.getString("START_DT");
									moneyList.add(new Money(oblResult.getString("Total"), new Currency_Id("XOF")));
									oblgList.add(lookUpValue.getString("SA_ID"));
									oblMoneyMap = new HashMap<List<String>,List<Money>>();
									oblMoneyMap.put(oblgList, moneyList);
									periodMap.put(period, oblMoneyMap);
								} else if(!lookUpValue.getString("START_DT").equalsIgnoreCase(period)) {
									moneyList = new ArrayList<Money>();
									oblgList = new ArrayList<String>();
									oblMoneyMap = new HashMap<List<String>,List<Money>>();
									moneyList.add(new Money(oblResult.getString("Total"), new Currency_Id("XOF")));
									oblgList.add(lookUpValue.getString("SA_ID"));
									oblMoneyMap.put(oblgList, moneyList);
									periodMap.put(lookUpValue.getString("START_DT"), oblMoneyMap);
									period = lookUpValue.getString("START_DT");
								}
							}
							
							logger.info("moneyList:: " + moneyList);
							logger.info("oblgList:: " + oblgList);
							logger.info("oblMoneyMap:: " + oblMoneyMap);
							logger.info("periodMap:: " + periodMap);
						}
						
					} catch (Exception exception) {
						logger.info("getDebtObligation ERROR::"+exception);
					} finally {
						psPreparedStatement.close();
						oblResultIterator.close();
					}
				}
			}
				cmPeriodObligationMoneyDTO.setPeriodOblMoney(periodMap);
				cmObligationMoneyDTO.setMapOblMoney(debtOblMap);
				debtPriorityMap.put(cmObligationMoneyDTO, cmPeriodObligationMoneyDTO);
		} catch (Exception exception) {
			logger.info("getDebtObligation error::"+exception);
			} finally {
				psPreparedStatement.close();
				resultIterator.close();
				psPreparedStatement = null;
			}
		//}
			logger.info("getDebtObligation:: epfAmount:: " + epfAmount);//21240
			logger.info("getDebtObligation:: erAmount:: " + erAmount);//361700
			logger.info("getDebtObligation:: atmpAmount:: " + atmpAmount);//9160
		return debtPriorityMap;
	}
	
	@Override
	public void setPaymentEvent(PaymentEvent arg0) {
		// TODO Auto-generated method stub
		paymentEvent = arg0;
	}

	@Override
	public void setDistributionRule(DistributionRule arg0) {
		// TODO Auto-generated method stub
		distributionRule = arg0;

	}

	@Override
	public void setAmount(Money arg0) {
		// TODO Auto-generated method stub
		amount = arg0;

	}

	@Override
	public void setCharacteristicType(CharacteristicType arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacteristicValue(String arg0) {
		// TODO Auto-generated method stub


	}

	@Override
	public void setAdhocCharacteristicValue(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacteristicValueFk1(String arg0) {
		// TODO Auto-generated method stub
		accId = arg0;
		characteristicValueFk1 = arg0;

	}

	@Override
	public void setCharacteristicValueFk2(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacteristicValueFk3(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacteristicValueFk4(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacteristicValueFk5(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTenderAccount(Account arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMatchType(MatchType arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMatchValue(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSequence(BigInteger arg0) {
		// TODO Auto-generated method stub
		sequence = arg0;
		
	}

	@Override
	public Payment_Id getPaymentId() {
		// TODO Auto-generated method stub
		return null;
	}

}
