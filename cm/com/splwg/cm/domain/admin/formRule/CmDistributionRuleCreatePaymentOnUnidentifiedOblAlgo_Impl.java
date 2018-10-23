package com.splwg.cm.domain.admin.formRule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.currency.Currency;
import com.splwg.cm.domain.customMessages.CmMessageRepository90002;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.PaymentStatusLookup;
import com.splwg.tax.domain.admin.customerClass.CustomerClass;
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
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent_Id;

/**
 * @author Deepak P
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = accountType, type = string)
 *            , @AlgorithmSoftParameter (name = unidentifiedObligationType, type = string)})
 */
public class CmDistributionRuleCreatePaymentOnUnidentifiedOblAlgo_Impl extends
		CmDistributionRuleCreatePaymentOnUnidentifiedOblAlgo_Gen implements DistributionRuleCreatePaymentAlgorithmSpot {
	private static final Logger logger = LoggerFactory
		.getLogger(CmDistributionRuleCreatePaymentOnUnidentifiedOblAlgo_Impl.class);
		
private String characteristicValueFk1;
private Money amount;
Currency currency = null;
private BigInteger sequence;
private PaymentEvent paymentEvent;
private Payment_Id paymentId;
Map<ServiceAgreement, Money> obligationMoneyMap = new HashMap<>();

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
	System.out.println("paymentEvent: " + this.paymentEvent);
	
	CustomerClass accType = new Account_Id(this.characteristicValueFk1).getEntity().getCustomerClass();
	//accType.getId().getIdValue();
	System.out.println("Account Type: " + accType.getId().getIdValue());
	if(getAccountType().equalsIgnoreCase(accType.getId().getIdValue())){
	String getUnidentifiedObliation = getUnidentifiedObliations(this.characteristicValueFk1);
	ServiceAgreement_Id undefinedSaId = new ServiceAgreement_Id(getUnidentifiedObliation);
	ServiceAgreement undefinedSa = (ServiceAgreement) undefinedSaId.getEntity();
	obligationMoneyMap.put(undefinedSa,this.amount);
	createFrozenPayment(obligationMoneyMap);
	}else{
		addError(CmMessageRepository90002.MSG_117());
	}

}

private String getUnidentifiedObliations(String accountId) {
	 
	String undefinedObl = null;
	String undefinedOblType = getUnidentifiedObligationType();
	//String  undefinedOblType = "PAI-SUSP";
	PreparedStatement psPreparedStatement = null;
	psPreparedStatement = createPreparedStatement("select distinct OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG,"
	 		+ "OBL.START_DT from CI_SA OBL where "
	 		+ "OBL.SA_ID in(SELECT TNDR.SA_ID FROM CI_TNDR_SRCE TNDR,"
	 		+ "CI_PEVT_DTL_ST PEVT WHERE TNDR.EXT_SOURCE_ID=PEVT.EXT_SOURCE_ID AND PEVT.DST_RULE_VALUE=\'"+accountId+"\') "
	 	    + "and OBL.SA_TYPE_CD in(\'"+undefinedOblType+"\') and OBL.SA_STATUS_FLG=20 ORDER BY OBL.START_DT","select");
	
	psPreparedStatement.setAutoclose(false);
	QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
	List<String> saIdList = new  ArrayList<String>();
	while (result.hasNext()) {
		System.out.println("I am In");
		SQLResultRow lookUpValue = result.next();
		undefinedObl = lookUpValue.getString("SA_ID");
		System.out.println(lookUpValue.getString("SA_ID"));
		/*if(!saIdList.contains(lookUpValue.getString("SA_ID"))){
			saIdList.add(lookUpValue.getString("SA_ID"));
		}*/
		
    	
	}
	return undefinedObl;
	 }

private void createFrozenPayment(Map<ServiceAgreement, Money> obligationMoneyMap) {
	
	Set<ServiceAgreement> oblKey = obligationMoneyMap.keySet();
	Account_Id obligationId = oblKey.iterator().next().getAccount().getId();
	Money moneyToSubtract = Money.ZERO;
	Payment_DTO paymentDTO = (Payment_DTO) this.createDTO(Payment.class);
	paymentDTO.setAccountId(new Account_Id(String.valueOf(obligationId.getIdValue())));
	System.out.println("Account Id for payment: " +  String.valueOf(obligationId.getIdValue()));
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
	
    /*PaymentEvent paymentEvent = (PaymentEvent) paymentDTO.getPaymentEventId().getEntity();
	Payment payment = paymentEvent.createPayment(paymentDTO);*/

	PaymentSegment_DTO paymentSegmentDTO = (PaymentSegment_DTO) this.createDTO(PaymentSegment.class);
	for (Entry<ServiceAgreement, Money> obliMoneyma : obligationMoneyMap.entrySet()){
		paymentSegmentDTO.setServiceAgreementId(obliMoneyma.getKey().getId());
		paymentSegmentDTO.setCurrencyId(currency.getId());
		paymentSegmentDTO.setPaySegmentAmount(obliMoneyma.getValue());
		paymentSegmentDTO.setPaymentId(payment.getId());
		paymentSegmentDTO.newEntity();
		moneyToSubtract = moneyToSubtract.add(obliMoneyma.getValue());
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

}}
