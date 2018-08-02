package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
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
 * @author Deepak P
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = ObligationContributionType, type = string)
 *            , @AlgorithmSoftParameter (name = PartialPayment, type = string)
 *            , @AlgorithmSoftParameter (name = AdjustmentOverpaymentType, type = string)
 *            , @AlgorithmSoftParameter (name = ObligationOverpaymentType, type = string)
 *            , @AlgorithmSoftParameter (name = CharacteristicType, type = string)
 *            , @AlgorithmSoftParameter (name = AdjustmentTypeInterest, type = string)
 *            , @AlgorithmSoftParameter (name = AdjustmentTypePenalty, type = string)
 *            , @AlgorithmSoftParameter (name = AdjustmentTypeContribution, type = string)
 *            , @AlgorithmSoftParameter (name = FormType, type = string)})
 */
public class CmDistributionRuleCreatePaymentOnDnsIdAlgo_Impl extends CmDistributionRuleCreatePaymentOnDnsIdAlgo_Gen
		implements DistributionRuleCreatePaymentAlgorithmSpot {
	private static final Logger logger = LoggerFactory.getLogger(CmDistributionRuleCreatePaymentOnDnsIdAlgo_Impl.class);

	private PaymentEvent paymentEvent;
	private DistributionRule distributionRule;
	private Money amount;
	private String characteristicValueFk1;
	private String adhocCharacteristicValue;
	private BigInteger sequence;
	private Payment_Id paymentId;
	PreparedStatement psPreparedStatement = null;
	public String accId = null;
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

		
		System.out.println("paymentEvent: " + this.paymentEvent);

		LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> debtOblMap = getDebtObligation(
				this.characteristicValueFk1);

		logger.info("debtOblMap: " + debtOblMap.size());
		System.out.println("debtOblMap: " + debtOblMap.size());
		debtOblMap.containsKey("");
		ServiceAgreement debtObligation = null;
		Money debtMoney = Money.ZERO;
		Money moneyValue = Money.ZERO;
		Money actualMoneyValue = this.amount;
		String periodValue = null;
		

		if (!debtOblMap.isEmpty()) {
			for (Map.Entry<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> debtMapObj : debtOblMap
					.entrySet()) {
				HashMap<String, Money> moneyMapkey = debtMapObj.getKey();
				HashMap<String, HashMap<List<String>, List<Money>>> moneyMap = debtMapObj.getValue();
				for (Map.Entry<String, Money> moneyMapObj : moneyMapkey.entrySet()) {
					Money moneyMapList = moneyMapObj.getValue();
					moneyValue = moneyMapList.add(moneyValue);
				}
				logger.info("Sum of Obligation Amount:: " + moneyValue);
				System.out.println("Sum of Obligation Amount:: " + moneyValue);

				if (!moneyValue.isZero() && this.amount.isGreaterThan(moneyValue)) {
					logger.info(
							"###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##");
					System.out.println(
							"###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##");
					HashMap<String, Money> moneyMapValue = debtMapObj.getKey();
					for (Map.Entry<String, Money> moneyEntry : moneyMapValue.entrySet()) {
						ServiceAgreement_Id sa_id = new ServiceAgreement_Id(moneyEntry.getKey());
						logger.info("ServiceAgreement_Id: " + sa_id);
						System.out.println("ServiceAgreement_Id, : " + sa_id);
						debtObligation = (ServiceAgreement) sa_id.getEntity();
						logger.info("ServiceAgreement: " + debtObligation);
						System.out.println("ServiceAgreement: " + debtObligation);
						debtMoney = moneyEntry.getValue();
						logger.info("DebtMoney: " + debtMoney);
						logger.info("Amount before the payment creation:: " + this.amount);
						System.out.println("Amount before the payment creation:: " + this.amount);
						if (!this.amount.isZero() && this.amount.isPositive()) {
							this.createFrozenPayment(debtObligation, debtMoney);
						}
					}

					if (!this.amount.isZero() && this.amount.isPositive()) {
						logger.info("********Creating OverPayment and the Amount is*******" + this.amount);						
						Map<String, String> accountDetailsMap = getAllAccountDetailsFromTaxId(this.characteristicValueFk1);
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
								
								String obligationId = createObligation(accntId,"DOR", getObligationOverpaymentType());
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
						}
					}

				} else {
					if(getPartialPayment().trim().equalsIgnoreCase("O")) {
						for (Entry<String, HashMap<List<String>, List<Money>>> moneyMapObj : moneyMap.entrySet()) {
							periodValue = moneyMapObj.getKey();
							actualMoneyValue = this.amount;
							HashMap<List<String>, List<Money>> finalMoneyMap = moneyMapObj.getValue();
							Money monthObligationMoney = Money.ZERO;
							for (Map.Entry<List<String>, List<Money>> moneyEntry : finalMoneyMap.entrySet()) {
								List<String> obligIdList = moneyEntry.getKey();
								if (!isNull(moneyEntry) && moneyEntry.getValue().size() >= 1) {
									List<Money> moneyList = moneyEntry.getValue();
									for (int i = 0; i < moneyList.size(); i++) {
										monthObligationMoney = moneyList.get(i).add(monthObligationMoney);
									}
									if (!monthObligationMoney.isZero()
											&& this.amount.isLessThanOrEqual(monthObligationMoney)) {
										logger.info("###Creating payment for same month obligations####");
										System.out.println("###Creating payment for same month obligations####");
										for (int i = 0; i < moneyList.size(); i++) {
											Money obligationMoney = moneyList.get(i);

											logger.info("obligation Money: " + obligationMoney);
											logger.info("Screen Amount: " + this.amount);
											logger.info("Month Obligation Money: " + monthObligationMoney);
											logger.info("Actual Money Value: " + actualMoneyValue);
											String oblStr = obligIdList.get(i);
											int prorateMoney = Math.round(actualMoneyValue.getAmount().floatValue()
													/ monthObligationMoney.getAmount().floatValue()
													* obligationMoney.getAmount().floatValue());
											debtMoney = new Money(String.valueOf(prorateMoney),new Currency_Id("XOF"));
											ServiceAgreement_Id sa_id = new ServiceAgreement_Id(oblStr);
											logger.info("ServiceAgreement_Id: " + sa_id);
											System.out.println("ServiceAgreement_Id: " + sa_id);
											debtObligation = (ServiceAgreement) sa_id.getEntity();
											logger.info("ServiceAgreement: " + debtObligation);
											System.out.println("ServiceAgreement: " + debtObligation);
											logger.info("prorateMoney: " + debtMoney);
											logger.info("Amount before the payment creation:: " + this.amount);
											System.out.println("Amount before the payment creation:: " + this.amount);
											String oblLast = obligIdList.get(obligIdList.size() - 1);
											logger.info("Same month last obligation: " + oblLast);
											if (oblLast.equalsIgnoreCase(oblStr)) {
												debtMoney = this.amount;
												logger.info("Same month last obligation money: " + debtMoney);
											}
											if (!this.amount.isZero() && this.amount.isPositive()) {
												this.createFrozenPayment(debtObligation, debtMoney);
												logger.info("Amount after the payment creation:: " + this.amount);

											}
										}
									} else {
										logger.info("###Creating payment for sequence month obligations####");
										System.out.println("###Creating payment for sequence month obligations####");
										for (Map.Entry<List<String>, List<Money>> moneyEntryy : finalMoneyMap
												.entrySet()) {
											List<String> obligIdListt = moneyEntryy.getKey();
											List<Money> moneyListt = moneyEntry.getValue();
											for (int i = 0; i < obligIdListt.size(); i++) {
												ServiceAgreement_Id sa_id = new ServiceAgreement_Id(
														obligIdListt.get(i));
												logger.info("ServiceAgreement_Id: " + sa_id);
												System.out.println("ServiceAgreement_Id: " + sa_id);
												debtObligation = (ServiceAgreement) sa_id.getEntity();
												logger.info("ServiceAgreement: " + debtObligation);
												System.out.println("ServiceAgreement: " + debtObligation);
												debtMoney = moneyListt.get(i);
												System.out.println("DebtMoney: " + debtMoney);
												logger.info("DebtMoney:" + debtMoney);
												logger.info("Amount before the payment creation :: " + this.amount);
												System.out
														.println("Amount before the payment creation:: " + this.amount);
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
			}
		} else {
			
			logger.info("There is no oblogation to pay");
			addError(StandardMessages.fieldInvalid("There is no oblogation to pay"));
			/*if(!this.amount.isZero() && this.amount.isPositive()){
			logger.info("********Creating OverPayment Adjustment*******");
			
			List<String> accountDetailList = getAllAccountDetailsFromAccountId(this.characteristicValueFk1);
			if(!accountDetailList.isEmpty()){
				BigDecimal split = new BigDecimal(accountDetailList.size());
				BigDecimal splitMoneyDec  = this.amount.getAmount().divide(split);
				Money splitMoney = new Money(splitMoneyDec);
				for(String accntId : accountDetailList){
					String obligationId = createObligation(accntId,"DOR","E-TPERCU");
					logger.info("OverPayment Created against the Obligation ID"+ obligationId +"for account ID:"+accntId);
					
					ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligationId);
					logger.info("ServiceAgreement_Id: " + sa_id);
					System.out.println("ServiceAgreement_Id, : " + sa_id);
					debtObligation = (ServiceAgreement) sa_id.getEntity();
					logger.info("ServiceAgreement: " + debtObligation);
					System.out.println("ServiceAgreement: " + debtObligation);
					//Money splitMoney = new Money(splitMoneyDec);
					logger.info("DebtMoney: " + splitMoneyDec);
					logger.info("DebtMoney: " + splitMoney);
					logger.info("Amount before the payment creation:: " + this.amount);
					System.out.println("Amount before the payment creation:: " + this.amount);
					if (!splitMoney.isZero() && splitMoney.isPositive()) {
						this.createFrozenPayment(debtObligation, splitMoney);
					}
				}
				
			
			}
		}*/
		}
	}

	private Map<String, String> getAllAccountDetailsFromTaxId(String adhocCharacteristicValue) {

		PreparedStatement accntPreparedStatement = null;
		QueryIterator<SQLResultRow> accntResultIterator = null;
		Map<String, String> accountDetailsMap = null;
		String accountId = null;

	    accntPreparedStatement = createPreparedStatement("select acc.ACCT_ID,acct.CUST_CL_CD from ci_acct_per acc,"
				+ "ci_acct acct,ci_tax_form tax where acc.acct_id = acct.acct_id and acc.per_id=tax.per_id "
				+ "and tax.tax_form_id=\'"+this.characteristicValueFk1+"\'","select");
		accntPreparedStatement.setAutoclose(false);
		try {                          
			startChanges();
			accountDetailsMap = new HashMap<String, String>();
			accntResultIterator = accntPreparedStatement.iterate();
			while (accntResultIterator.hasNext()) {
				SQLResultRow lookUpValue = accntResultIterator.next();
				accountId = lookUpValue.getString("ACCT_ID");
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
		if (this.amount.isLessThanOrEqual(money)) { // money = 4000,
													// amount-screen = 5000
			paymentDTO.setPaymentAmount(this.amount);
			paymentSegmentDTO.setPaySegmentAmount(this.amount);
			this.amount = this.amount.subtract(money);
		} else if (this.amount.isGreaterThan(money)) {
			this.amount = this.amount.subtract(money);
			paymentDTO.setPaymentAmount(money);
			paymentSegmentDTO.setPaySegmentAmount(money);
		}
		paymentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());
		paymentDTO.setSequence(this.sequence);
		paymentDTO.setPaymentEventId(this.paymentEvent.getId());// new
																// PaymentEvent_Id("245693748074")
		// paymentDTO.setPaymentEventId(new
		// PaymentEvent_Id("245693748074"));//new
		// PaymentEvent_Id("245693748074")
		paymentDTO.setPaymentStatus(PaymentStatusLookup.constants.FREEZABLE);
		paymentSegmentDTO.setServiceAgreementId(obligation.getId());
		paymentSegmentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());
		Adjustment_Id linkedAdjustmentId = paymentSegmentDTO.getAdjustmentId();
		CreateDistributeFreezePayment createDistributeFreezePayment = Factory.newInstance();
		Payment payment = createDistributeFreezePayment.process(paymentDTO, paymentSegmentDTO, (Date) null, (Date) null,
				(GeneralLedgerDistributionCode) null, linkedAdjustmentId);
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
		logger.info("obligationId " + obligationId);
		System.out.println("obligationId " + obligationId);
		return obligationId;

	}

	@SuppressWarnings("deprecation")
	private LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> getDebtObligation(
			String characteristicValueFk1) {

		PreparedStatement psPreparedStatement = null;

		String oblType = getObligationContributionType();
		String adjType = getAdjustmentTypeContribution();


		String period = null;
		HashMap<String, Money> debtOblMap = new HashMap<String, Money>();
		HashMap<String, HashMap<List<String>, List<Money>>> periodMap = new HashMap<String, HashMap<List<String>, List<Money>>>();
		LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> debtPriorityMap = new LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>>();

		psPreparedStatement = createPreparedStatement("select distinct OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG,"
				+ "ADJ.ADJ_TYPE_CD,ADJ.ADJ_ID,ADJ.ADJ_AMT,OBL.START_DT,ADJ.CRE_DT from CI_SA OBL,CI_ADJ ADJ,ci_ft FT "
				+ "where ADJ.SA_ID=OBL.SA_ID and FT.SA_ID=OBL.SA_ID and OBL.SA_ID in(select sa.sa_id from ci_sa sa,"
				+ "ci_sa_char sach,ci_tax_form tax where tax.tax_form_id = sach.CHAR_VAL_FK1 and sach.sa_id = sa.sa_id "
				+ "and tax.tax_form_id =\'"+this.characteristicValueFk1+"\') and ADJ.ADJ_TYPE_CD IN(\'"+adjType+"\') "
				+ "and OBL.SA_TYPE_CD in(\'"+oblType+"\') and OBL.SA_STATUS_FLG=40 ORDER BY OBL.START_DT","select");

		psPreparedStatement.setAutoclose(false);
		try {
			
			QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
			List<Money> moneyList = new ArrayList<Money>();
			List<String> oblgList = new ArrayList<String>();
			List<String> saIdList = new ArrayList<String>();
			HashMap<List<String>, List<Money>> oblMoneyMap = new HashMap<List<String>, List<Money>>();
			while (result.hasNext()) {
				System.out.println("I am In");
				SQLResultRow lookUpValue = result.next();
				System.out.println(lookUpValue.getString("SA_ID"));
				if (!saIdList.contains(lookUpValue.getString("SA_ID"))) {
					saIdList.add(lookUpValue.getString("SA_ID"));
					try {
						psPreparedStatement = createPreparedStatement(
								"SELECT SUM(CUR_AMT) AS \"Total\" from CI_FT where SA_ID = "
										+ lookUpValue.getString("SA_ID"),
								"select");
						psPreparedStatement.setAutoclose(false);
						QueryIterator<SQLResultRow> oblResultIterator = psPreparedStatement.iterate();
						while (oblResultIterator.hasNext()) {
							System.out.println("I am In");
							SQLResultRow oblResult = oblResultIterator.next();
							System.out.println(lookUpValue.getString("SA_ID"));
							if (oblResult.getString("Total") != null
									&& Integer.parseInt(oblResult.getString("Total")) > 0) {
								debtOblMap.put(lookUpValue.getString("SA_ID"), new Money(oblResult.getString("Total")));
								
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

								if (null == period || lookUpValue.getString("START_DT").equalsIgnoreCase(period)) {
									period = lookUpValue.getString("START_DT");
									moneyList.add(new Money(oblResult.getString("Total")));
									oblgList.add(lookUpValue.getString("SA_ID"));
									oblMoneyMap = new HashMap<List<String>, List<Money>>();
									oblMoneyMap.put(oblgList, moneyList);
									periodMap.put(period, oblMoneyMap);
								} else if (!lookUpValue.getString("START_DT").equalsIgnoreCase(period)) {
									moneyList = new ArrayList<Money>();
									oblgList = new ArrayList<String>();
									oblMoneyMap = new HashMap<List<String>, List<Money>>();
									moneyList.add(new Money(oblResult.getString("Total")));
									oblgList.add(lookUpValue.getString("SA_ID"));
									oblMoneyMap.put(oblgList, moneyList);
									periodMap.put(lookUpValue.getString("START_DT"), oblMoneyMap);
									period = lookUpValue.getString("START_DT");
								}
							}
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}

			}

			if (!debtOblMap.isEmpty() && !periodMap.isEmpty()) {
				debtPriorityMap.put(debtOblMap, periodMap);
			} else {
				addError(StandardMessages.fieldInvalid(
						"The DNS ID mentioned for payment is not linked with ID-DNS characteristic type"));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}
		// }
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
		adhocCharacteristicValue = arg0;

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
	}}