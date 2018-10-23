package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.currency.Currency;
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
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.tax.domain.payment.payment.Payment;
import com.splwg.tax.domain.payment.payment.PaymentSegment;
import com.splwg.tax.domain.payment.payment.PaymentSegment_DTO;
import com.splwg.tax.domain.payment.payment.Payment_DTO;
import com.splwg.tax.domain.payment.payment.Payment_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent_Id;
import com.splwg.tax.domain.taxBilling.taxBill.TaxBill_Id;
import com.splwg.tax.domain.payment.payment.CreateDistributeFreezePayment;
import com.splwg.tax.domain.payment.payment.CreateDistributeFreezePayment.Factory;


/**
 * @author Deepak P
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = billType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = accountType, required = true, type = string)})
 */
public class CmDistributionRuleCreatePaymentOnTaxBillAlgo_Impl extends CmDistributionRuleCreatePaymentOnTaxBillAlgo_Gen
		implements DistributionRuleCreatePaymentAlgorithmSpot {
	private static final Logger logger = LoggerFactory
			.getLogger(CmDistributionRuleCreatePaymentOnTaxBillAlgo_Impl.class);
	
	private String characteristicValueFk1;
	private Money amount;
	Currency currency = null;
	Account_Id accId = null;             
	private BigInteger sequence;
	private PaymentEvent paymentEvent;
	private Payment_Id paymentId;
	Map<ServiceAgreement, Money> billOblMap = new HashMap<>();
	String billTypeArr[] = null;
	String accountId = null;
	Boolean statusCheck = false;
	
	@Override
	public void invoke() {
		logger.info("characteristicFK: " + this.characteristicValueFk1);
		System.out.println("characteristicFK: " + this.characteristicValueFk1);
		TaxBill_Id tb = new TaxBill_Id(this.characteristicValueFk1);
		accountId = tb.getEntity().getServiceAgreement().getAccount().getId().getIdValue();
		System.out.println("Account ID: "+tb.getEntity().getServiceAgreement().getAccount().getId().getIdValue());
		currency = new Account_Id(String.valueOf(tb.getEntity().getServiceAgreement().getAccount().getId().getIdValue())).getEntity().getCurrency();
	 
		logger.info("Amount: " + this.amount);
		System.out.println("Amount: " + this.amount);

		logger.info("Sequence: " + this.sequence);
		System.out.println("Sequence: " + this.sequence);

		logger.info("paymentEvent: " + this.paymentEvent);
		System.out.println("paymentEvent: " + this.paymentEvent);
		
		if(this.amount.isNegative())
		{
		Map<ServiceAgreement, Money> getBillOblMap = getUnidentifiedObliations(this.characteristicValueFk1);
		if(!getBillOblMap.isEmpty()){
		Set<ServiceAgreement> oblKey = getBillOblMap.keySet();
		ServiceAgreement sa = oblKey.iterator().next();
		createFrozenPayment(sa);
		}else{
			addError(StandardMessages.fieldInvalid("L'obligation de facture fiscale est vide."));
		}
		}else{
			addError(CmMessageRepository90002.MSG_351());	
		
		}
	}    
	
	private Map<ServiceAgreement, Money> getUnidentifiedObliations(String taxBillId) {
		 
		//taxBillId = "731363606240";
		String billType = getBillType();
		billTypeArr = billType.split(",");
		billType = "'" + StringUtils.join(billTypeArr,"','") + "'";
		//String billType = "'" +"INCAP-TEMP-BILL"+ "'";
		//String status = "COMPLETED";
		PreparedStatement psPreparedStatement = null;
		
		
		/*psPreparedStatement = createPreparedStatement("select OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG,"
				+ "OBL.START_DT,(select sum(ft.cur_amt) from ci_ft ft where ft.sa_id=OBL.sa_id) as CUR_AMT from CI_SA OBL "
				+ "where OBL.SA_ID in(select SA_ID from c1_tax_bill where tax_bill_id=\'"+taxBillId+"\' and TAX_BILL_TYPE_CD "
				+ "in(\'"+billType+"\') and bo_status_cd=\'"+status+"\') "
				+ "and OBL.SA_STATUS_FLG=40 ORDER BY OBL.START_DT","select");*/
		
		psPreparedStatement = createPreparedStatement("select OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG, "
				+ "OBL.START_DT, tbill.BO_STATUS_CD,(select sum(ft.cur_amt) from ci_ft ft where ft.sa_id=OBL.sa_id) as CUR_AMT "
				+ "from CI_SA OBL, c1_tax_bill tbill where tbill.sa_id = OBL.sa_id and tbill.tax_bill_id=\'"+taxBillId+"\' "
				+ "and tbill.TAX_BILL_TYPE_CD in("+billType+")","select");
	
		psPreparedStatement.setAutoclose(false);
		QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
		
		while (result.hasNext()) {
			System.out.println("I am In");
			SQLResultRow lookUpValue = result.next();
			System.out.println(lookUpValue.getString("SA_ID"));
			System.out.println(lookUpValue.getString("CUR_AMT"));
			System.out.println(lookUpValue.getString("SA_STATUS_FLG"));
			System.out.println(lookUpValue.getString("BO_STATUS_CD"));
			if(!"40".equalsIgnoreCase(lookUpValue.getString("SA_STATUS_FLG").trim()) || !"COMPLETED".equalsIgnoreCase(lookUpValue.getString("BO_STATUS_CD").trim()))
			{
				addError(CmMessageRepository90002.MSG_350());	
			}
			ServiceAgreement_Id billOblId = new ServiceAgreement_Id(lookUpValue.getString("SA_ID"));
			ServiceAgreement billObl = (ServiceAgreement) billOblId.getEntity();
			Money billAmount = new Money(lookUpValue.getString("CUR_AMT"), currency.getId());
			billOblMap.put(billObl, billAmount);
			
			/*if(!saIdList.contains(lookUpValue.getString("SA_ID"))){
				saIdList.add(lookUpValue.getString("SA_ID"));
			}*/
			
	    	
		}
		return billOblMap;
		 }
	
	
	private void createFrozenPayment(ServiceAgreement obligation) {
		Payment_DTO paymentDTO = (Payment_DTO) this.createDTO(Payment.class);
		paymentDTO.setAccountId(obligation.getAccount().getId());
		paymentDTO.setPaymentAmount(this.amount);
		paymentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());
		paymentDTO.setSequence(this.sequence);
		paymentDTO.setPaymentEventId(this.paymentEvent.getId());
		paymentDTO.setPaymentStatus(PaymentStatusLookup.constants.FREEZABLE);
		PaymentSegment_DTO paymentSegmentDTO = (PaymentSegment_DTO) this.createDTO(PaymentSegment.class);
		paymentSegmentDTO.setServiceAgreementId(obligation.getId());
		paymentSegmentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());
		paymentSegmentDTO.setPaySegmentAmount(this.amount);
		Adjustment_Id linkedAdjustmentId = paymentSegmentDTO.getAdjustmentId();
		CreateDistributeFreezePayment createDistributeFreezePayment = Factory.newInstance();
		Payment payment = createDistributeFreezePayment.process(paymentDTO, paymentSegmentDTO, (Date) null, (Date) null,
				(GeneralLedgerDistributionCode) null, linkedAdjustmentId);
		if (this.notNull(payment)) {
			this.paymentId = payment.getId();
		}

	}
	
	/*private void createFrozenPayment(Map<ServiceAgreement, Money> obligationMoneyMap) {
		
		Set<ServiceAgreement> oblKey = obligationMoneyMap.keySet();
		//Account_Id accId = oblKey.iterator().next().getAccount().getId();
		Money moneyToSubtract = Money.ZERO;
		Payment_DTO paymentDTO = (Payment_DTO) this.createDTO(Payment.class);
		paymentDTO.setAccountId(new Account_Id(accountId));
		System.out.println("Account Id for payment: " +  accountId);
		//paymentDTO.setAccountId(new Account_Id(String.valueOf(obligationId)));
		paymentDTO.setPaymentAmount(this.amount);
		paymentDTO.setCurrencyId(currency.getId());
		paymentDTO.setSequence(this.sequence);
		//paymentDTO.setPaymentEventId(new PaymentEvent_Id("245693748074"));
		paymentDTO.setPaymentEventId(this.paymentEvent.getId()); 
	    paymentDTO.setPaymentStatus(PaymentStatusLookup.constants.FREEZABLE);
		Payment payment = paymentDTO.newEntity();
	    
		logger.info("paymentId: " +  payment.getId());
		System.out.println("paymentId: " +  payment.getId());
		
	    PaymentEvent paymentEvent = (PaymentEvent) paymentDTO.getPaymentEventId().getEntity();
		Payment payment = paymentEvent.createPayment(paymentDTO);

		PaymentSegment_DTO paymentSegmentDTO = (PaymentSegment_DTO) this.createDTO(PaymentSegment.class);
		for (Entry<ServiceAgreement, Money> obliMoneyma : obligationMoneyMap.entrySet()){
			paymentSegmentDTO.setServiceAgreementId(obliMoneyma.getKey().getId());
			paymentSegmentDTO.setCurrencyId(currency.getId());
			paymentSegmentDTO.setPaySegmentAmount(obliMoneyma.getValue().negate());
			paymentSegmentDTO.setPaymentId(payment.getId());
			paymentSegmentDTO.newEntity();
			moneyToSubtract = moneyToSubtract.add(obliMoneyma.getValue());
			System.out.println("PaySegment Id:: " + paymentSegmentDTO.getEntity().getId());
			logger.info("PaySegment Id:: " + paymentSegmentDTO.getEntity().getId());
		}
		if (payment.getPaymentStatus().isFreezable()) {
			payment.freeze();
		}
		this.amount = this.amount.subtract(moneyToSubtract);
		if (this.notNull(payment)) {
			this.paymentId = payment.getId();
		}
		

	}*/

	@Override
	public Payment_Id getPaymentId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAdhocCharacteristicValue(String arg0) {
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
	public void setDistributionRule(DistributionRule arg0) {
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
	public void setPaymentEvent(PaymentEvent arg0) {
		// TODO Auto-generated method stub
		paymentEvent = arg0;
	}

	@Override
	public void setSequence(BigInteger arg0) {
		// TODO Auto-generated method stub
		sequence = arg0;
	}

	@Override
	public void setTenderAccount(Account arg0) {
		// TODO Auto-generated method stub

	}

}
