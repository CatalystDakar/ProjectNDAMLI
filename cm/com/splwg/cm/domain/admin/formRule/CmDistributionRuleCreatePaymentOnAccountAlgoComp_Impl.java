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
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.currency.Currency;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.cm.domain.customMessages.CmMessageRepository90002;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.PaymentStatusLookup;
import com.splwg.tax.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.tax.domain.admin.distributionRule.DistributionRule;
import com.splwg.tax.domain.admin.distributionRule.DistributionRuleCreatePaymentAlgorithmSpot;
import com.splwg.tax.domain.admin.matchType.MatchType;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
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
	private Money amount;
	private String characteristicValueFk1;
	private BigInteger sequence;
	private Payment_Id paymentId;
	PreparedStatement psPreparedStatement = null;
	private CmDistributionRuleOblMoneyDTO cmObligationMoneyDTO = new CmDistributionRuleOblMoneyDTO();
	private CMDistributionRulePeriodOblListMoneyListDTO cmPeriodObligationMoneyDTO = new CMDistributionRulePeriodOblListMoneyListDTO();
	Money epfAmount = Money.ZERO;
	Money erAmount = Money.ZERO;
	Money atmpAmount = Money.ZERO;
	Currency currency = null;
	Map<ServiceAgreement, Money> obligationMoneyMap = new HashMap<>();
	
	String obligationContributionArr[] = null;
	String obligationOverPaymentArr[] = null;
	String adjustmentTypeContributionArr[] = null;
	String adjustmentTypePenalityArr[] = null; 
	String adjustmentTypeMajorationArr[] = null; 
	String adjustmentTyepeOverpaymentArr[] = null; 
	
	@Override
	public void invoke() {

		logger.info("characteristicFK: " + this.characteristicValueFk1);
		System.out.println("characteristicFK: " + this.characteristicValueFk1);
		currency = new Account_Id(this.characteristicValueFk1).getEntity().getCurrency();

		logger.info("Amount: " + this.amount);
		System.out.println("Amount: " + this.amount);

		logger.info("Sequence: " + this.sequence);
		System.out.println("Sequence: " + this.sequence);

		logger.info("paymentEvent: " + this.paymentEvent);
		System.out.println("paymentEvent: " + this.paymentEvent);// 12321324

		LinkedHashMap<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO> debtOblMap = getDebtObligation(
				this.characteristicValueFk1);// 1000

		logger.info("debtOblMap: " + debtOblMap.size());
		System.out.println("debtOblMap: " + debtOblMap.size());
		logger.info("debtOblMap: " + debtOblMap);
		System.out.println("debtOblMap: " + debtOblMap);
		ServiceAgreement debtObligation = null;
		Money debtMoneyforSingleSA = Money.ZERO;
		Money totalDebtAmountToBePaid = Money.ZERO;
		Money actualMoneyValue = this.amount;
		String periodValue = null;
			if (!debtOblMap.isEmpty()) {
				for (Map.Entry<CmDistributionRuleOblMoneyDTO, CMDistributionRulePeriodOblListMoneyListDTO> debtMapObj : debtOblMap
						.entrySet()) {
					cmObligationMoneyDTO = debtMapObj.getKey();
					cmPeriodObligationMoneyDTO = debtMapObj.getValue();
					Map<String, Money> moneyMapkey = cmObligationMoneyDTO.getMapOblMoney();
					Map<String, HashMap<List<String>, List<Money>>> moneyMap = cmPeriodObligationMoneyDTO.getPeriodOblMoney();
					if (!moneyMapkey.isEmpty()) {
						for (Map.Entry<String, Money> moneyMapObj : moneyMapkey.entrySet()) {
							Money moneyMapList = moneyMapObj.getValue();
							totalDebtAmountToBePaid = moneyMapList.add(totalDebtAmountToBePaid);
						}
						logger.info("Sum of Obligation Amount:: " + totalDebtAmountToBePaid);
						System.out.println("Sum of Obligation Amount:: " + totalDebtAmountToBePaid);
							
						if (!totalDebtAmountToBePaid.isZero() && !this.amount.isZero() && this.amount.isGreaterThanOrEqual(totalDebtAmountToBePaid)) {
							logger.info("###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##");
							System.out.println("###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##");
							Map<String, Money> moneyMapValue = cmObligationMoneyDTO.getMapOblMoney();
							Money overPayAmount = Money.ZERO;
							for (Map.Entry<String, Money> moneyEntry : moneyMapValue.entrySet()) {
								ServiceAgreement_Id sa_id = new ServiceAgreement_Id(moneyEntry.getKey());
								logger.info("ServiceAgreement_Id: " + sa_id);
								System.out.println("ServiceAgreement_Id, : " + sa_id);
								debtObligation = (ServiceAgreement) sa_id.getEntity();
								logger.info("ServiceAgreement: " + debtObligation);
								System.out.println("ServiceAgreement: " + debtObligation);
								debtMoneyforSingleSA = moneyEntry.getValue();
								int payAmount = Math.round(debtMoneyforSingleSA.getAmount().floatValue());
								debtMoneyforSingleSA = new Money(String.valueOf(payAmount), currency.getId());
								logger.info("DebtMoney: " + debtMoneyforSingleSA);
								logger.info("Amount before the payment creation:: " + this.amount);
								System.out.println("Amount before the payment creation:: " + this.amount);
								obligationMoneyMap.put(debtObligation, debtMoneyforSingleSA);
							}
							overPayAmount = this.amount.subtract(totalDebtAmountToBePaid);
							if (!overPayAmount.isZero() && overPayAmount.isPositive()) {
								logger.info("********Creating OverPayment and the Amount :: " + overPayAmount);
								for (Map.Entry<String, Money> moneyMapObj : moneyMapkey.entrySet()) {
									String monthObligation = moneyMapObj.getKey();
									ServiceAgreement_Id saId = new ServiceAgreement_Id(monthObligation);
									String saType = saId.getEntity().getServiceAgreementType().getId().getSaType().trim();
									if("O-EPF".equalsIgnoreCase(saType)) {
										epfAmount = moneyMapObj.getValue().add(epfAmount);
									} else if("O-EATMP".equalsIgnoreCase(saType)) {
										atmpAmount = moneyMapObj.getValue().add(atmpAmount);
									} else if("O-ER".equalsIgnoreCase(saType)) {
										erAmount = moneyMapObj.getValue().add(erAmount);
									}
								}
								int prorateMoney = 0;
								Money additionMoney = Money.ZERO;
								Money moneyToBePaid = Money.ZERO;//new Money(String.valueOf(prorateMoney), currency.getId());
								Money finalOblgMoney = Money.ZERO;
								for (int i = 0; i < obligationOverPaymentArr.length; i++) {
									if("E-AVPF".equalsIgnoreCase(obligationOverPaymentArr[i])) {
										prorateMoney = Math.round(epfAmount.getAmount().floatValue()
												/ totalDebtAmountToBePaid.getAmount().floatValue()
												* overPayAmount.getAmount().floatValue());
									} else if("E-AVATMP".equalsIgnoreCase(obligationOverPaymentArr[i])) {
										prorateMoney = Math.round(atmpAmount.getAmount().floatValue()
												/ totalDebtAmountToBePaid.getAmount().floatValue()
												* overPayAmount.getAmount().floatValue());
									} else if("E-AVCR".equalsIgnoreCase(obligationOverPaymentArr[i])) {
										prorateMoney = Math.round(erAmount.getAmount().floatValue()
												/ totalDebtAmountToBePaid.getAmount().floatValue()
												* overPayAmount.getAmount().floatValue());
									}
									//Proration and Round off Logic
									if(i == obligationOverPaymentArr.length-1) {
										moneyToBePaid = overPayAmount.subtract(additionMoney);
									} else {
										finalOblgMoney = new Money(String.valueOf(prorateMoney), currency.getId()).add(finalOblgMoney);
										if(overPayAmount.subtract(finalOblgMoney).isEqualTo(new Money("1", currency.getId()))) {
											moneyToBePaid = new Money(String.valueOf(prorateMoney - 1), currency.getId());
										} else {
											moneyToBePaid = new Money(String.valueOf(prorateMoney), currency.getId());
										}
									}
									additionMoney = additionMoney.add(moneyToBePaid);								
									
									String division = getDivisionByObligationType(obligationOverPaymentArr[i]);					
									String obligationId = createObligation(this.characteristicValueFk1, division , obligationOverPaymentArr[i]);
									logger.info("OverPayment Created against the Obligation ID" + obligationId
											+ "for account ID: " + this.characteristicValueFk1);
									ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligationId);
									logger.info("ServiceAgreement_Id: " + sa_id);
									System.out.println("ServiceAgreement_Id, : " + sa_id);
									debtObligation = (ServiceAgreement) sa_id.getEntity();
									logger.info("ServiceAgreement: " + debtObligation);
									System.out.println("ServiceAgreement: " + debtObligation);
									logger.info("OverPayAmount Amount " + overPayAmount +"OverPayAmount Amount per SA: " + moneyToBePaid);
									System.out.println("OverPayAmount Amount " + overPayAmount +"OverPayAmount Amount per SA: " + moneyToBePaid);
									obligationMoneyMap.put(debtObligation, moneyToBePaid);
								}
							}
							this.createFrozenPayment(obligationMoneyMap);
						} else {
							if (getPartialPayment().trim().equalsIgnoreCase("O") && !this.amount.isZero()) {
								for (Entry<String, HashMap<List<String>, List<Money>>> moneyMapObj : moneyMap.entrySet()) {
									periodValue = moneyMapObj.getKey();
									actualMoneyValue = this.amount;
									HashMap<List<String>, List<Money>> finalMoneyMap = moneyMapObj.getValue();
									Money monthObligationMoney = Money.ZERO;
									Money finalOblMoney = Money.ZERO;
									Money additionMoney = Money.ZERO;
									for (Map.Entry<List<String>, List<Money>> moneyEntry : finalMoneyMap.entrySet()) {
										List<String> obligIdList = moneyEntry.getKey();
										if (!isNull(moneyEntry) && moneyEntry.getValue().size() >= 1) {
											List<Money> moneyList = moneyEntry.getValue();
											for (int i = 0; i < moneyList.size(); i++) {
												monthObligationMoney = moneyList.get(i).add(monthObligationMoney);
											}
											if (!monthObligationMoney.isZero() && this.amount.isLessThanOrEqual(monthObligationMoney)) {
												logger.info("###Creating payment for same month obligations####");
												System.out.println("###Creating payment for same month obligations####");
												for (int i = 0; i < moneyList.size(); i++) {
													Money obligationMoney = moneyList.get(i);
													logger.info("obligation Money: " + obligationMoney);
													logger.info("Screen Amount: " + this.amount);
													logger.info("Month Obligation Money: " + monthObligationMoney);
													logger.info("Actual Money Value: " + actualMoneyValue);
													String oblStr = obligIdList.get(i);
													int prorateMoney = 0;
													if(i == moneyList.size()-1) {
														debtMoneyforSingleSA = actualMoneyValue.subtract(additionMoney);
													} else {
														prorateMoney = Math.round(actualMoneyValue.getAmount().floatValue()
																/ monthObligationMoney.getAmount().floatValue()
																* obligationMoney.getAmount().floatValue());
														finalOblMoney = new Money(String.valueOf(prorateMoney), currency.getId()).add(finalOblMoney);
														if(actualMoneyValue.subtract(finalOblMoney).isEqualTo(new Money("1", currency.getId()))) {
															debtMoneyforSingleSA = new Money(String.valueOf(prorateMoney - 1), currency.getId());
														} else {
															debtMoneyforSingleSA = new Money(String.valueOf(prorateMoney), currency.getId());
														}
													}
													additionMoney = additionMoney.add(debtMoneyforSingleSA);
													ServiceAgreement_Id sa_id = new ServiceAgreement_Id(oblStr);
													logger.info("ServiceAgreement_Id: " + sa_id);
													System.out.println("ServiceAgreement_Id: " + sa_id);
													debtObligation = (ServiceAgreement) sa_id.getEntity();
													logger.info("ServiceAgreement: " + debtObligation);
													System.out.println("ServiceAgreement: " + debtObligation);
													logger.info("prorateMoney: " + debtMoneyforSingleSA);
													logger.info("Amount before the payment creation:: " + this.amount);
													System.out.println("Amount before the payment creation:: " + this.amount);
													obligationMoneyMap.put(debtObligation, debtMoneyforSingleSA);
												}
												if (!this.amount.isZero() && this.amount.isPositive()) {
													this.createFrozenPayment(obligationMoneyMap);
												}
											} else {
												logger.info("###Creating payment for sequence month obligations####");
												System.out.println("###Creating payment for sequence month obligations####");
												for (Map.Entry<List<String>, List<Money>> moneyEntryy : finalMoneyMap
														.entrySet()) {
													List<String> obligIdListt = moneyEntryy.getKey();
													List<Money> moneyListt = moneyEntry.getValue();
													for (int i = 0; i < obligIdListt.size(); i++) {
														ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligIdListt.get(i));
														logger.info("ServiceAgreement_Id: " + sa_id);
														System.out.println("ServiceAgreement_Id: " + sa_id);
														debtObligation = (ServiceAgreement) sa_id.getEntity();
														logger.info("ServiceAgreement: " + debtObligation);
														System.out.println("ServiceAgreement: " + debtObligation);
														debtMoneyforSingleSA = moneyListt.get(i);
														int payAmount = Math.round(debtMoneyforSingleSA.getAmount().floatValue());
														debtMoneyforSingleSA = new Money(String.valueOf(payAmount), currency.getId());
														System.out.println("DebtMoney: " + debtMoneyforSingleSA);
														logger.info("DebtMoney:" + debtMoneyforSingleSA);
														logger.info("Amount before the payment creation :: " + this.amount);
														System.out.println("Amount before the payment creation:: " + this.amount);
														obligationMoneyMap.put(debtObligation, debtMoneyforSingleSA);
													}
													if (!this.amount.isZero() && this.amount.isPositive()) {
														this.createFrozenPayment(obligationMoneyMap);
													}
												}
											}
											
										}
									}
								}
							} else {
								addError(CmMessageRepository90002.MSG_300());//if the partial payment is not allowed
							}
						}
					} else {
						logger.info("Money Map is empty");
					}
				}
			} else if(!this.amount.isZero() && this.amount.isPositive()) {//2000
				logger.info("There is no obligation to pay: Creating OverPayment- the Amount is:: "	+ this.amount);
				Money debtMoneyforOverPaySA = Money.ZERO;
				int prorateMoney = Math.round(this.amount.getAmount().floatValue() / obligationOverPaymentArr.length);
					for (int i = 0; i < obligationOverPaymentArr.length; i++) {
					String division = getDivisionByObligationType(obligationOverPaymentArr[i]);					
					String obligationId = createObligation(this.characteristicValueFk1, division , obligationOverPaymentArr[i]);
					logger.info("OverPayment Created against the Obligation ID" + obligationId + "for account ID: " + this.characteristicValueFk1);
					ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligationId);
					logger.info("ServiceAgreement_Id: " + sa_id);
					System.out.println("ServiceAgreement_Id, : " + sa_id);
					debtObligation = (ServiceAgreement) sa_id.getEntity();
					logger.info("ServiceAgreement: " + debtObligation);
					System.out.println("ServiceAgreement: " + debtObligation);
					if(i == obligationOverPaymentArr.length-1) {
						int length = obligationOverPaymentArr.length-1;
						prorateMoney = prorateMoney * length;
						Money finalMoney = new Money(String.valueOf(prorateMoney), currency.getId());
						debtMoneyforOverPaySA = this.amount.subtract(finalMoney);
					} else {
						debtMoneyforOverPaySA = new Money(String.valueOf(prorateMoney), currency.getId());
						
					}
					logger.info("Amount before the payment creation:: " + obligationMoneyMap);
					obligationMoneyMap.put(debtObligation, debtMoneyforOverPaySA);
				}
				
				this.createFrozenPayment(obligationMoneyMap);
			}
		
		
	}

	/**
	 * @param saType
	 * @return
	 */
	private String getDivisionByObligationType(String saType) {
		PreparedStatement preparedStatement = createPreparedStatement(
				"SELECT CIS_DIVISION FROM CI_SA_TYPE where SA_TYPE_CD= \'" + saType + "\'",	"SELECT");
		preparedStatement.setAutoclose(false);
		String division = null;

		try {
			SQLResultRow sql = preparedStatement.firstRow();
			division = sql.getString("CIS_DIVISION");
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			preparedStatement.close();
			preparedStatement = null;
		}
		return division;

	}

	/**
	 * Method to get All Account Details From AccountId
	 * 
	 * @param accountId
	 * @return
	 */
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
	 * Method to create and Frozen the Payment
	 * 
	 * @param obligation
	 * @param money
	 */
	private void createFrozenPayment(Map<ServiceAgreement, Money> obligationMoneyMap) {
		
		Money moneyToSubtract = Money.ZERO;
		Payment_DTO paymentDTO = (Payment_DTO) this.createDTO(Payment.class);
		paymentDTO.setAccountId(new Account_Id(this.characteristicValueFk1));
		paymentDTO.setPaymentAmount(this.amount);
		paymentDTO.setCurrencyId(currency.getId());
		paymentDTO.setSequence(this.sequence);
		paymentDTO.setPaymentEventId(this.paymentEvent.getId()); 
        paymentDTO.setPaymentStatus(PaymentStatusLookup.constants.FREEZABLE);
		Payment payment = paymentDTO.newEntity();
        
		logger.info("paymentId: " +  payment.getId());
		System.out.println("paymentId: " +  payment.getId());
	
		PaymentSegment_DTO paymentSegmentDTO = (PaymentSegment_DTO) this.createDTO(PaymentSegment.class);
		for (Entry<ServiceAgreement, Money> obliMoneymap : obligationMoneyMap.entrySet()){
			paymentSegmentDTO.setServiceAgreementId(obliMoneymap.getKey().getId());
			paymentSegmentDTO.setCurrencyId(currency.getId());
			paymentSegmentDTO.setPaySegmentAmount(obliMoneymap.getValue());
			paymentSegmentDTO.setPaymentId(payment.getId());
			paymentSegmentDTO.newEntity();
			moneyToSubtract = moneyToSubtract.add(obliMoneymap.getValue());
			System.out.println("PaySegment Id:: " + paymentSegmentDTO.getEntity().getId());
			logger.info("PaySegment Id:: " + paymentSegmentDTO.getEntity().getId());
		}
		if (payment.getPaymentStatus().isFreezable()) {
			payment.freeze();
		}
		this.amount = this.amount.subtract(moneyToSubtract);;
		if (this.notNull(payment)) {
			this.paymentId = payment.getId();
		}
		
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
	 * Method to execute Business Service And Create Obligation
	 * 
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
			 obligationContributionArr = obligationContribution.split(","); 
			 obligationOverPaymentArr = obligationOverPayment.split(","); 
			 adjustmentTypeContributionArr = adjustmentTypeContribution.split(","); 
			 adjustmentTypePenalityArr = adjustmentTypePenality.split(","); 
			 adjustmentTypeMajorationArr = adjustmentTypeMajoration.split(","); 
			 adjustmentTyepeOverpaymentArr = adjustmentTyepeOverpayment.split(","); 
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
							
		  /* psPreparedStatement = createPreparedStatement(" SELECT CAP.ACCT_ID, CS.SA_ID, CS.SA_TYPE_CD, CS.SA_STATUS_FLG , "
	    				+" CADJ.ADJ_TYPE_CD, CADJ.ADJ_ID, CADJ.ADJ_AMT, CS.START_DT , CADJ.CRE_DT FROM CI_ACCT_PER CAP, "
	    				+" CI_ACCT CA ,CI_SA CS,CI_ADJ CADJ WHERE CAP.PER_ID IN (SELECT PER_ID from CI_ACCT_PER where ACCT_ID = :accId)"
	    				+" AND CAP.ACCT_ID=CA.ACCT_ID AND CA.CUST_CL_CD IN(:accountType) AND CAP.ACCT_ID = CS.ACCT_ID  "
	    				+" AND CS.SA_TYPE_CD in(:obligationContribution, :obligationOverPayment) AND CS.SA_ID = CADJ.SA_ID AND CADJ.ADJ_TYPE_CD IN  "
	    				+" (:adjustmentTypeContribution, :adjustmentTypePenality, :adjustmentTypeMajoration, :adjustmentTyepeOverpayment) AND CS.SA_STATUS_FLG=40 ORDER BY CS.START_DT", "SELECT");*/	
			
			
	       psPreparedStatement = createPreparedStatement(" SELECT CAP.ACCT_ID, CS.SA_ID, CS.SA_TYPE_CD, CS.SA_STATUS_FLG , "
	    				+" CADJ.ADJ_TYPE_CD, CADJ.ADJ_ID, CADJ.ADJ_AMT, CS.START_DT , CADJ.CRE_DT FROM CI_ACCT_PER CAP, "
	    				+" CI_ACCT CA ,CI_SA CS,CI_ADJ CADJ WHERE CAP.PER_ID IN (SELECT PER_ID from CI_ACCT_PER where ACCT_ID = \'"+accId+"\') "
	    				+" AND CAP.ACCT_ID=CA.ACCT_ID AND CA.CUST_CL_CD IN("+accountType+") AND CAP.ACCT_ID = CS.ACCT_ID  "
	    				+" AND CS.SA_TYPE_CD in("+obligationContribution+") AND CS.SA_ID = CADJ.SA_ID AND CADJ.ADJ_TYPE_CD IN  "
	    				+" ("+adjustmentTypeContribution+","+adjustmentTypePenality+","+adjustmentTypeMajoration+","+adjustmentTyepeOverpayment+") AND CS.SA_STATUS_FLG=40 ORDER BY CS.START_DT", "SELECT");
	    
			psPreparedStatement.setAutoclose(false);
			QueryIterator<SQLResultRow> resultIterator = null;
			try {
				/*psPreparedStatement.bindString("accId", accId, null);
				psPreparedStatement.bindString("accountType", accountType, null);
				psPreparedStatement.bindString("obligationContribution", obligationContribution, null);
				psPreparedStatement.bindString("obligationOverPayment", obligationOverPayment, null);
				psPreparedStatement.bindString("adjustmentTypeContribution", adjustmentTypeContribution, null);
				psPreparedStatement.bindString("adjustmentTypePenality", adjustmentTypePenality, null);
				psPreparedStatement.bindString("adjustmentTypeMajoration", adjustmentTypeMajoration, null);
				psPreparedStatement.bindString("adjustmentTyepeOverpayment", adjustmentTyepeOverpayment, null);*/
				
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
								/*if(lookUpValue.getString("SA_TYPE_CD").trim().equalsIgnoreCase("O-EPF")) {
									Money oblAmount = new Money(oblResult.getString("Total"), new Currency_Id("XOF"));
									epfAmount = epfAmount.add(oblAmount);
								} else 	if(lookUpValue.getString("SA_TYPE_CD").trim().equalsIgnoreCase("O-ER")) {
									Money oblAmount = new Money(oblResult.getString("Total"), new Currency_Id("XOF"));
									erAmount = erAmount.add(oblAmount);
								} else if(lookUpValue.getString("SA_TYPE_CD").trim().equalsIgnoreCase("O-EATMP")) {
									Money oblAmount = new Money(oblResult.getString("Total"), new Currency_Id("XOF"));
									atmpAmount = atmpAmount.add(oblAmount);
								}*/
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
				if (!debtOblMap.isEmpty() && !periodMap.isEmpty()) {
					debtPriorityMap.put(cmObligationMoneyDTO, cmPeriodObligationMoneyDTO);
				}
				
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
