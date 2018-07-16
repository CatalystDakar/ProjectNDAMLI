package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.PaymentStatusLookup;
import com.splwg.tax.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.tax.domain.admin.distributionRule.DistributionRule;
import com.splwg.tax.domain.admin.distributionRule.DistributionRuleCreatePaymentAlgorithmSpot;
import com.splwg.tax.domain.admin.distributionRule.MessageRepository;
import com.splwg.tax.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCode;
import com.splwg.tax.domain.admin.matchType.MatchType;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
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
@AlgorithmComponent ()
 */
public class CmDistributionRuleCreatePaymentOnAccountAlgComp_Impl extends
		CmDistributionRuleCreatePaymentOnAccountAlgComp_Gen implements DistributionRuleCreatePaymentAlgorithmSpot {
	
	private static final Logger logger = LoggerFactory
			.getLogger(CmDistributionRuleCreatePaymentOnAccountAlgComp_Impl.class);
	private PaymentEvent paymentEvent;
	private DistributionRule distributionRule;
	private Money amount;
	private String characteristicValueFk1;
	private BigInteger sequence;
	private Payment_Id paymentId;
	PreparedStatement psPreparedStatement = null;
	public String accId = null;
	@Override
	public void invoke() {
		//this.validateParameters();

		logger.info("characteristicFK: " + accId);
		System.out.println("characteristicFK: " + accId);
		
		logger.info("characteristicFK: " + this.characteristicValueFk1);
		System.out.println("characteristicFK: " + this.characteristicValueFk1);

		logger.info("Amount: " + this.amount);
		System.out.println("Amount: " + this.amount);		

		logger.info("Sequence: " + this.sequence);
		System.out.println("Sequence: " + this.sequence);		

		logger.info("paymentEvent: " + this.paymentEvent);
		System.out.println("paymentEvent: " + this.paymentEvent);//12321324
		
		List<String> accList = getAccountList(this.characteristicValueFk1);
		List<String> saList = getObligationList(accList);
		Map<String, Money> debtOblMap = getDebtObligation(saList);//1000
		
		logger.info("debtOblMap: " + debtOblMap.size());
		System.out.println("debtOblMap: " + debtOblMap.size());
		
		if (!debtOblMap.isEmpty()) {
			for (Map.Entry<String, Money> entry : debtOblMap.entrySet()) {
				ServiceAgreement_Id id = new ServiceAgreement_Id(entry.getKey());
				logger.info("ServiceAgreement_Id: " + id);
				System.out.println("ServiceAgreement_Id: " + id);
				ServiceAgreement debtObligation = (ServiceAgreement) id.getEntity();
				logger.info("ServiceAgreement: " + debtObligation);
				System.out.println("ServiceAgreement: " + debtObligation);
				logger.info("Amount before the payment creation:: " + this.amount);
				System.out.println("Amount before the payment creation:: " + this.amount);
				if (this.amount.isPositive()) {
					this.createFrozenPayment(debtObligation, entry.getValue());
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private Map<String, Money> getDebtObligation(List<String> saList) {

		PreparedStatement psPreparedStatement = null;

		Iterator its = saList.iterator();
		String sa_id = "";
		Map<String, Money> debtOblMap = new HashMap<String, Money>();
		while (its.hasNext()) {
			sa_id = (String) its.next();
			psPreparedStatement = createPreparedStatement("SELECT SUM(CUR_AMT) AS \"Total\" FROM CI_FT WHERE SA_ID = " + sa_id);
			psPreparedStatement.setAutoclose(false);
			try {
				QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					System.out.println("I am In");
					SQLResultRow lookUpValue = result.next();
					System.out.println(lookUpValue.getString("Total"));
					if (lookUpValue.getString("Total") != null
							&& Integer.parseInt(lookUpValue.getString("Total")) > 0) {
						debtOblMap.put(sa_id, new Money(lookUpValue.getString("Total")));
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
		}
		return debtOblMap;
	}

	private List<String> getObligationList(List<String> accList) {

		PreparedStatement psPreparedStatement = null;

		Iterator its = accList.iterator();
		String acc_id = "";
		List<String> saList = new ArrayList<String>();
		while (its.hasNext()) {
			acc_id = (String) its.next();
			psPreparedStatement = createPreparedStatement("select SA_ID from CI_SA where ACCT_ID = " + acc_id);
			psPreparedStatement.setAutoclose(false);
			try {
				QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
				while (result.hasNext()) {
					System.out.println("I am In");
					SQLResultRow lookUpValue = result.next();
					System.out.println(lookUpValue.getString("SA_ID"));
					saList.add(lookUpValue.getString("SA_ID"));
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
		}
		return saList;
	}

	@SuppressWarnings("deprecation")
	private List<String> getAccountList(String accId) {

		psPreparedStatement = createPreparedStatement("select ACCT_ID from CI_ACCT_PER where PER_ID ="
				+ " (select PER_ID from CI_ACCT_PER where ACCT_ID =" + accId+")");
		psPreparedStatement.setAutoclose(false);
		List<String> accList = new ArrayList<String>();

		try {
			QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
			while (result.hasNext()) {
				System.out.println("I am In");
				SQLResultRow lookUpValue = result.next();
				System.out.println(lookUpValue.getString("ACCT_ID"));
				accList.add(lookUpValue.getString("ACCT_ID"));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}
		return accList;
	}

	private void createFrozenPayment(ServiceAgreement obligation, Money money) {

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
		paymentDTO.setPaymentEventId(this.paymentEvent.getId());
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
	
	private void validateParameters() {
		if (this.isBlankOrNull(this.characteristicValueFk1)) {
			this.addError(StandardMessages.fieldMissing("ACCOUNT_ID_LBL"));
		}

		Account_Id id = new Account_Id(this.characteristicValueFk1);
		Account tenderObligation =  id.getEntity();
		if (this.isNull(tenderObligation)) {
			this.addError(StandardMessages.idInvalid(id));
		} else {
			/*if (tenderObligation.getStatus().isCanceled()) {
				this.addError(
						MessageRepository.saStatusInvalid(tenderObligation.getStatus().getLookupValue().getValueName(),
								tenderObligation.getId().getIdValue()));
			}*/

			if (this.isNull(this.sequence) || this.sequence.intValue() < 0) {
				this.addError(MessageRepository.sequenceNumberMustNotBeANegativeNumber());
			}

			if (this.isNull(this.paymentEvent)) {
				this.addError(MessageRepository.isRequired("Payment Event ID"));
			}

			if (this.isNull(this.distributionRule)) {
				this.addError(MessageRepository.isRequired("Distribution Rule"));
			}
		}

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
