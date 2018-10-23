package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;

/**
 * @author Deepak P
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = obligationTypeRejectedFeesPv, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeRejectedFeesAtmp, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeRejectedFeesPf, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeOverpayment, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeInterest, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypePenalty, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeContribution, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeContributionEr, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeContributionAtmp, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeContributionPf, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeOverpaymentEr, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeOverpaymentAtmp, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeOverpaymentPf, type = string)})
 */
public class CmDistributeOverpaymentObligationAmount_Impl extends CmDistributeOverpaymentObligationAmount_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {


    
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	Money epfAmount = Money.ZERO;
	Money erAmount = Money.ZERO;
	Money atmpAmount = Money.ZERO;
	
	private static final Logger logger = LoggerFactory
			.getLogger(CmDistributeOverpaymentObligationAmount_Impl.class);
	private Money overPaymentTotalAmount = Money.ZERO;
	String overpaymentObligationId = null;
	List<String> saList = new ArrayList<String>();
	String adjustmentTypeContributionArr[] = null;
	
	@SuppressWarnings("deprecation")
	@Override
	public void invoke() {


		//this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		//String taxFormId = this.boInstance.getFieldAndMDForPath("taxFormId").getXMLValue();
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		String taxFormId = formBoInstance.getString("taxFormId");
		//String taxFormId = "768493889427";
		logger.info("Tax form ID: " + taxFormId);
		startChanges();
		PreparedStatement psPreparedStatement = null;
		String accountId = null;

		psPreparedStatement = createPreparedStatement("select acc.ACCT_ID from ci_acct_per acc,ci_tax_form "
				+ "tax where acc.per_id=tax.per_id and tax.tax_form_id=\'"+taxFormId+"\'","select");
		QueryIterator<SQLResultRow> result = null;
		HashMap<String,String> oblTotalAmountMap =  new HashMap<String,String>();
		
		try{
			
			result = psPreparedStatement.iterate();
			
			while(result.hasNext())
			{
				SQLResultRow lookUpValue = result.next();
				accountId = lookUpValue.getString("ACCT_ID");
				//accountId = "5811295034";
				System.out.println(lookUpValue.getString("ACCT_ID"));
				
			HashMap<String,List<String>> hashMapPayDetails = getOverPaymentOblDetails(accountId);
		if(!hashMapPayDetails.isEmpty())
		{
			for(Map.Entry<String, List<String>> payDetObj : hashMapPayDetails.entrySet()){
				List<String> obList = payDetObj.getValue();
				overpaymentObligationId = payDetObj.getValue().get(0);
				String totalAmount = null;
				for(int index=0 ; index<obList.size();index++){
					String ObliId  = obList.get(index);
					ServiceAgreement_Id saId = new ServiceAgreement_Id(ObliId);
					String saType = saId.getEntity().getServiceAgreementType().getId().getSaType().trim();
					psPreparedStatement = createPreparedStatement("select sum(cur_amt) as TOTAL_AMOUNT from ci_ft where sa_id=\'"+ObliId+"\'","select");
					QueryIterator<SQLResultRow> resultTotal = null;
					
					try{
						resultTotal = psPreparedStatement.iterate();
						
						while(resultTotal.hasNext()){
							SQLResultRow lookUpVal = resultTotal.next();
							totalAmount = lookUpVal.getString("TOTAL_AMOUNT");
							Money money = new Money(totalAmount);
							if("E-AVPF".equalsIgnoreCase(saType)) {
								epfAmount = money.negate();
							} else if("E-AVATMP".equalsIgnoreCase(saType)) {
								atmpAmount = money.negate();
							} else if("E-AVCR".equalsIgnoreCase(saType)) {
								erAmount = money.negate();
							}
							overPaymentTotalAmount = overPaymentTotalAmount.add(money);
							oblTotalAmountMap.put(ObliId, totalAmount); //
						}
					} catch(Exception exception){
						logger.error("Exception in getting overpayment obligation amount from FT "+ exception);
					}finally{
						resultTotal.close();
					}
					
				}
			}
		
			
			LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> debtOblMapObj = getDebtObligation(accountId);
			  
			  //logger.info("debtOblMap: " + debtOblMap.size());
			  System.out.println("debtOblMapObj: " + debtOblMapObj.size());
			  ServiceAgreement debtObligation = null;
			  ServiceAgreement overPayObligation = null;
			  String debtObligationType = null;
			  String overPayObligationType = null;
			  Money debtMoney = Money.ZERO;
			  Money moneyValue = Money.ZERO;
			  Money actualMoneyValue = overPaymentTotalAmount;
			  String periodValue = null;
			  String adjustmentTypeValue = null;
			  String debtOblID = null;
			  
			  if(!debtOblMapObj.isEmpty()) {
			  for(Map.Entry<HashMap<String, Money>, HashMap<String, HashMap<List<String>,List<Money>>>> debtMapObj : debtOblMapObj.entrySet()){
			   HashMap<String,Money> moneyMapkey = debtMapObj.getKey();
			   HashMap<String, HashMap<List<String>,List<Money>>> moneyMap = debtMapObj.getValue();
			   for(Map.Entry<String, Money> moneyMapObj : moneyMapkey.entrySet() ){
			    Money moneyMapList = moneyMapObj.getValue();
			    moneyValue = moneyMapList.add(moneyValue);
			   }
			   logger.info("Sum of Obligation Amount:: " + moneyValue);
			   System.out.println("Sum of Obligation Amount:: " + moneyValue);
            if(!overPaymentTotalAmount.isZero())
            {
			   if(!moneyValue.isZero() && overPaymentTotalAmount.negate().isGreaterThan(moneyValue)){ 
			    logger.info("###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##" );
			    System.out.println("###Input Amount is greater than sum of obligation amount.Creating payment for equal distribution##" );
			    HashMap<String,Money> moneyMapValue = debtMapObj.getKey();
			    for (Map.Entry<String, Money> moneyEntry : moneyMapValue.entrySet()) {
			     ServiceAgreement_Id sa_id = new ServiceAgreement_Id(moneyEntry.getKey());
			     logger.info("ServiceAgreement_Id: " + sa_id);
			     System.out.println("ServiceAgreement_Id, : " + sa_id);
			     debtObligation = (ServiceAgreement) sa_id.getEntity();
			     //ServiceAgreement_Id saId = new ServiceAgreement_Id(monthObligation);
			     debtObligationType = sa_id.getEntity().getServiceAgreementType().getId().getSaType().trim();
			     //debtObligationType = (ServiceAgreementType) sa_id.getEntity().getServiceAgreementType().getId().getSaType();
			     logger.info("ServiceAgreement: " + debtObligation);
			     System.out.println("ServiceAgreement: " + debtObligation);
			     debtOblID = moneyEntry.getKey();
			     debtMoney = moneyEntry.getValue();
			     System.out.println("DebtMoney: " + debtMoney);
			     logger.info("Amount before the payment creation:: " + overPaymentTotalAmount);
			     System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			     System.out.println(debtObligationType);
			     //ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overpaymentObligationId);
			     overPayObligation = (ServiceAgreement) sa_id.getEntity();
			     //overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim();
			     if(debtObligationType.equalsIgnoreCase("O-EPF") && debtObligationType!=null && epfAmount.isGreaterThan(Money.ZERO) )
		    	 {
		    		 adjustmentTypeValue = "A-AVPF";
		    		 if(debtMoney.isGreaterThan(epfAmount))
			    	 {
			    		 debtMoney = epfAmount;
			    	 }
		    		 for(String overPayId :  saList){
		    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
		    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
		    		
		    		 if(overPayObligationType.equalsIgnoreCase("E-AVPF")){
		    			 
		    			 overpaymentObligationId = overPayId;
		    			 
		    		 }
		    		 }
		    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
				     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
				     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
				     if(debtMoney.isGreaterThan(epfAmount))
			    	 {
			    		 epfAmount = epfAmount.subtract(epfAmount);
			    	 }else{
			    	 epfAmount = epfAmount.subtract(debtMoney);
			    	 }
		    	 }
		    	 if(String.valueOf(debtObligationType).equalsIgnoreCase("O-EATMP") && String.valueOf(debtObligationType)!=null && atmpAmount.isGreaterThan(Money.ZERO))
		    	 {
		    		 adjustmentTypeValue = "A-AVATMP";
		    		 if(debtMoney.isGreaterThan(atmpAmount))
			    	 {
			    		 debtMoney = atmpAmount;
			    	 }
		    		 for(String overPayId :  saList){
		    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
		    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
		    		
		    		 if(overPayObligationType.equalsIgnoreCase("E-AVATMP")){
		    			 
		    			 overpaymentObligationId = overPayId;
		    			 
		    		 }
		    		 }
		    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
				     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
				     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
				     if(debtMoney.isGreaterThan(atmpAmount))
			    	 {
				    	 atmpAmount = atmpAmount.subtract(atmpAmount);
			    	 }else{
			    		 atmpAmount = atmpAmount.subtract(debtMoney);
			    	 }
		    	 }
		    	 if(String.valueOf(debtObligationType).equalsIgnoreCase("O-ER") && String.valueOf(debtObligationType)!=null && erAmount.isGreaterThan(Money.ZERO))
		    	 {
		    		 adjustmentTypeValue = "A-AVCR";
		    		 if(debtMoney.isGreaterThan(erAmount))
			    	 {
			    		 debtMoney = erAmount;
			    	 }
		    		 for(String overPayId :  saList){
		    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
		    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
		    		
		    		 if(overPayObligationType.equalsIgnoreCase("E-AVCR")){
		    			 
		    			 overpaymentObligationId = overPayId;
		    			 
		    		 }
		    		 }
		    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
				     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
				     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
				     if(debtMoney.isGreaterThan(erAmount))
			    	 {
				    	 erAmount = erAmount.subtract(erAmount);
			    	 }else{
			    		 erAmount = erAmount.subtract(debtMoney);
			    	 }
		    	 }
			          }
			     
			   } 
			   else {
			    for(Entry<String, HashMap<List<String>, List<Money>>> moneyMapObj : moneyMap.entrySet()){
			       periodValue = moneyMapObj.getKey(); 
			       actualMoneyValue = overPaymentTotalAmount;
			       HashMap<List<String>,List<Money>>  finalMoneyMap = moneyMapObj.getValue();
			       Money monthObligationMoney = Money.ZERO;
			       for(Map.Entry<List<String>, List<Money>> moneyEntry : finalMoneyMap.entrySet()){
			        List<String> obligIdList = moneyEntry.getKey();
			        if(!isNull(moneyEntry) && moneyEntry.getValue().size()>=1){ 
			         List<Money> moneyList = moneyEntry.getValue();
			         for(int i=0;i<moneyList.size();i++){
			          monthObligationMoney = moneyList.get(i).add(monthObligationMoney);
			         }
			         
			         if(!monthObligationMoney.isZero() && overPaymentTotalAmount.negate().isLessThanOrEqual(monthObligationMoney) && !overPaymentTotalAmount.negate().isZero()){
			          logger.info("###Creating payment for same month obligations####" );
			         System.out.println("###Creating payment for same month obligations####" );
			          for(int i=0;i<moneyList.size();i++){
			           Money obligationMoney = moneyList.get(i);
			           String oblStr = obligIdList.get(i);
			        //int prorateMoney = Math.round(overPaymentTotalAmount.negate().getAmount().floatValue()/monthObligationMoney.getAmount().floatValue()*obligationMoney.getAmount().floatValue());
			        //debtMoney = new Money(String.valueOf(prorateMoney));
			           debtMoney = obligationMoney;
			           ServiceAgreement_Id sa_id = new ServiceAgreement_Id(oblStr);
			           logger.info("obligation Money: " + obligationMoney);
			           logger.info("Screen Amount: " + overPaymentTotalAmount);
			           logger.info("Month Obligation Money: " + monthObligationMoney);
			           logger.info("Actual Money Value: " + actualMoneyValue);
			          
			          logger.info("ServiceAgreement_Id: " + sa_id);
			          System.out.println("ServiceAgreement_Id: " + sa_id);
			          debtObligation = (ServiceAgreement) sa_id.getEntity();
			          debtObligationType = sa_id.getEntity().getServiceAgreementType().getId().getSaType().trim();
			          //debtObligationType = (ServiceAgreementType) sa_id.getEntity().getServiceAgreementType();
			          logger.info("ServiceAgreement: " + debtObligation);
			          System.out.println("ServiceAgreement: " + debtObligation);
			          System.out.println("DebtMoney: " + debtMoney);
			          logger.info("Amount before the payment creation:: " + overPaymentTotalAmount);
			          System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			          if(debtObligationType.equalsIgnoreCase("O-EPF") && debtObligationType!=null && epfAmount.isGreaterThan(Money.ZERO) )
				    	 {
				    		 adjustmentTypeValue = "A-AVPF";
				    		 if(debtMoney.isGreaterThan(epfAmount))
					    	 {
					    		 debtMoney = epfAmount;
					    	 }
				    		 for(String overPayId :  saList){
				    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
				    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
				    		
				    		 if(overPayObligationType.equalsIgnoreCase("E-AVPF")){
				    			 
				    			 overpaymentObligationId = overPayId;
				    			 
				    		 }
				    		 }
				    		 debtOblID = oblStr;
				    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
						     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
						     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
						     if(debtMoney.isGreaterThan(epfAmount))
					    	 {
					    		 epfAmount = epfAmount.subtract(epfAmount);
					    	 }else{
					    	 epfAmount = epfAmount.subtract(debtMoney);
					    	 }
				    	 }
				    	 if(String.valueOf(debtObligationType).equalsIgnoreCase("O-EATMP") && String.valueOf(debtObligationType)!=null && atmpAmount.isGreaterThan(Money.ZERO))
				    	 {
				    		 adjustmentTypeValue = "A-AVATMP";
				    		 if(debtMoney.isGreaterThan(atmpAmount))
					    	 {
					    		 debtMoney = atmpAmount;
					    	 }
				    		 for(String overPayId :  saList){
				    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
				    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
				    		
				    		 if(overPayObligationType.equalsIgnoreCase("E-AVATMP")){
				    			 
				    			 overpaymentObligationId = overPayId;
				    			 
				    		 }
				    		 }
				    		 debtOblID = oblStr;
				    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
						     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
						     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
						     if(debtMoney.isGreaterThan(atmpAmount))
					    	 {
						    	 atmpAmount = atmpAmount.subtract(atmpAmount);
					    	 }else{
					    		 atmpAmount = atmpAmount.subtract(debtMoney);
					    	 }
				    	 }
				    	 if(String.valueOf(debtObligationType).equalsIgnoreCase("O-ER") && String.valueOf(debtObligationType)!=null && erAmount.isGreaterThan(Money.ZERO))
				    	 {
				    		 adjustmentTypeValue = "A-AVCR";
				    		 if(debtMoney.isGreaterThan(erAmount))
					    	 {
					    		 debtMoney = erAmount;
					    	 }
				    		 for(String overPayId :  saList){
				    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
				    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
				    		
				    		 if(overPayObligationType.equalsIgnoreCase("E-AVCR")){
				    			 
				    			 overpaymentObligationId = overPayId;
				    			 
				    		 }
				    		 }
				    		 debtOblID = oblStr;
				    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
						     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
						     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
						     if(debtMoney.isGreaterThan(erAmount))
					    	 {
						    	 erAmount = erAmount.subtract(erAmount);
					    	 }else{
					    		 erAmount = erAmount.subtract(debtMoney);
					    	 }
				    	 }
			          }
			         }
			         else
			         { 
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
			           debtObligationType = sa_id.getEntity().getServiceAgreementType().getId().getSaType().trim();
			           //debtObligationType = (ServiceAgreementType) sa_id.getEntity().getServiceAgreementType();
			           logger.info("ServiceAgreement: " + debtObligation);
			           System.out.println("ServiceAgreement: " + debtObligation);
			           debtOblID = obligIdListt.get(i);
			           debtMoney = moneyListt.get(i);
			           System.out.println("DebtMoney: " + debtMoney);
			           logger.info("DebtMoney:" + debtMoney);
			           logger.info("Amount before the payment creation :: " + overPaymentTotalAmount);
			           System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			           if(debtObligationType.equalsIgnoreCase("O-EPF") && debtObligationType!=null && epfAmount.isGreaterThan(Money.ZERO) )
				    	 {
				    		 adjustmentTypeValue = "A-AVPF";
				    		 if(debtMoney.isGreaterThan(epfAmount))
					    	 {
					    		 debtMoney = epfAmount;
					    	 }
				    		 for(String overPayId :  saList){
				    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
				    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
				    		
				    		 if(overPayObligationType.equalsIgnoreCase("E-AVPF")){
				    			 
				    			 overpaymentObligationId = overPayId;
				    			 
				    		 }
				    		 }
				    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
						     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
						     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
						     if(debtMoney.isGreaterThan(epfAmount))
					    	 {
					    		 epfAmount = epfAmount.subtract(epfAmount);
					    	 }else{
					    	 epfAmount = epfAmount.subtract(debtMoney);
					    	 }
				    	 }
				    	 if(String.valueOf(debtObligationType).equalsIgnoreCase("O-EATMP") && String.valueOf(debtObligationType)!=null && atmpAmount.isGreaterThan(Money.ZERO))
				    	 {
				    		 adjustmentTypeValue = "A-AVATMP";
				    		 if(debtMoney.isGreaterThan(atmpAmount))
					    	 {
					    		 debtMoney = atmpAmount;
					    	 }
				    		 for(String overPayId :  saList){
				    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
				    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
				    		
				    		 if(overPayObligationType.equalsIgnoreCase("E-AVATMP")){
				    			 
				    			 overpaymentObligationId = overPayId;
				    			 
				    		 }
				    		 }
				    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
						     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
						     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
						     if(debtMoney.isGreaterThan(atmpAmount))
					    	 {
						    	 atmpAmount = atmpAmount.subtract(atmpAmount);
					    	 }else{
					    		 atmpAmount = atmpAmount.subtract(debtMoney);
					    	 }
				    	 }
				    	 if(String.valueOf(debtObligationType).equalsIgnoreCase("O-ER") && String.valueOf(debtObligationType)!=null && erAmount.isGreaterThan(Money.ZERO))
				    	 {
				    		 adjustmentTypeValue = "A-AVCR";
				    		 if(debtMoney.isGreaterThan(erAmount))
					    	 {
					    		 debtMoney = erAmount;
					    	 }
				    		 for(String overPayId :  saList){
				    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
				    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
				    		
				    		 if(overPayObligationType.equalsIgnoreCase("E-AVCR")){
				    			 
				    			 overpaymentObligationId = overPayId;
				    			 
				    		 }
				    		 }
				    		 createAdjustment(overpaymentObligationId,debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
						     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
						     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
						     if(debtMoney.isGreaterThan(erAmount))
					    	 {
						    	 erAmount = erAmount.subtract(erAmount);
					    	 }else{
					    		 erAmount = erAmount.subtract(debtMoney);
					    	 }
				    	 }
			           //}
			          }
			         }
			        }
			       }
			      }
			     }
			    }
            }else {
			    logger.info("There is no overpayment obligation money to pay");
			   } 
            for(Map.Entry<String, Money> moneyMapObj : moneyMapkey.entrySet() ){
			    Money rejectFeesMoney = moneyMapObj.getValue();
			    String rejectFeesObligation = moneyMapObj.getKey();
			    debtMoney = rejectFeesMoney;
			    ServiceAgreement_Id rejectFeesOblId = new ServiceAgreement_Id(rejectFeesObligation);
			    String rejectFeesOblType = rejectFeesOblId.getEntity().getServiceAgreementType().getId().getSaType().trim();
			    if(rejectFeesOblType.equalsIgnoreCase("FREJ-PF") && rejectFeesOblType!=null && epfAmount.isGreaterThan(Money.ZERO) )
		    	 {
		    		 adjustmentTypeValue = "A-AVPF";
		    		 if(debtMoney.isGreaterThan(epfAmount))
			    	 {
			    		 debtMoney = epfAmount;
			    	 }
		    		 for(String overPayId :  saList){
		    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
		    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
		    		
		    		 if(overPayObligationType.equalsIgnoreCase("E-AVPF")){
		    			 
		    			 overpaymentObligationId = overPayId;
		    			 
		    		 }
		    		 }
		    		 
		    		 createAdjustment(overpaymentObligationId,rejectFeesObligation,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
				     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
				     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
				     if(debtMoney.isGreaterThan(epfAmount))
			    	 {
			    		 epfAmount = epfAmount.subtract(epfAmount);
			    	 }else{
			    	 epfAmount = epfAmount.subtract(debtMoney);
			    	 }
		    	 }
			    if(String.valueOf(rejectFeesOblType).equalsIgnoreCase("FREJ-AT") && String.valueOf(rejectFeesOblType)!=null && atmpAmount.isGreaterThan(Money.ZERO))
		    	 {
		    		 adjustmentTypeValue = "A-AVATMP";
		    		 if(debtMoney.isGreaterThan(atmpAmount))
			    	 {
			    		 debtMoney = atmpAmount;
			    	 }
		    		 for(String overPayId :  saList){
		    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
		    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
		    		
		    		 if(overPayObligationType.equalsIgnoreCase("E-AVATMP")){
		    			 
		    			 overpaymentObligationId = overPayId;
		    			 
		    		 }
		    		 }
		    		 
		    		 createAdjustment(overpaymentObligationId,rejectFeesObligation,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
				     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
				     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
				     if(debtMoney.isGreaterThan(atmpAmount))
			    	 {
				    	 atmpAmount = atmpAmount.subtract(atmpAmount);
			    	 }else{
			    		 atmpAmount = atmpAmount.subtract(debtMoney);
			    	 }
		    	 }
			    if(String.valueOf(rejectFeesOblType).equalsIgnoreCase("FREJ-PV") && String.valueOf(rejectFeesOblType)!=null && erAmount.isGreaterThan(Money.ZERO))
		    	 {
		    		 adjustmentTypeValue = "A-AVCR";
		    		 if(debtMoney.isGreaterThan(erAmount))
			    	 {
			    		 debtMoney = erAmount;
			    	 }
		    		 for(String overPayId :  saList){
		    			 ServiceAgreement_Id overpaySa_id = new ServiceAgreement_Id(overPayId);
		    			 overPayObligationType = overpaySa_id.getEntity().getServiceAgreementType().getId().getSaType().trim(); 
		    		
		    		 if(overPayObligationType.equalsIgnoreCase("E-AVCR")){
		    			 
		    			 overpaymentObligationId = overPayId;
		    			 
		    		 }
		    		 }
		    		 createAdjustment(overpaymentObligationId,rejectFeesObligation,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
				     //String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
				     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
				     if(debtMoney.isGreaterThan(erAmount))
			    	 {
				    	 erAmount = erAmount.subtract(erAmount);
			    	 }else{
			    		 erAmount = erAmount.subtract(debtMoney);
			    	 }
		    	 }
			    
			    //moneyValue = moneyMapList.add(moneyValue);
			   }
            
			  }
			   }else {
			    logger.info("There is no oblogation to pay");
			   } 
			
			}}} catch(Exception exception){
			logger.error("Error in getting per id in tax form " +exception);
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
			result.close();
			saveChanges();
		}
		
		
	

	}
	
	@SuppressWarnings("deprecation")
	 private LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> getDebtObligation(String accountId) {

		  
		  PreparedStatement psPreparedStatement = null;

		  
		  String oblType1 = getObligationTypeContributionPf();
		  String oblType2 = getObligationTypeContributionAtmp();
		  String oblType3 = getObligationTypeContributionEr();
		  //String adjType1 = "CPF";
		  //String adjType2 = "CATMP";
		  //String adjType3 = "CR";
		  String adjustmentTypeContribution = getAdjustmentTypeContribution();
		  /*String adjType1 = "CPF";
		  String adjType2 = "CATMP";
		  String adjType3 = "CR";
		  String adjType4 = "CATMPHE";
		  String adjType5 = "CPFHE";
		  String adjType6 = "CRHE";	*/	  
		  
		  adjustmentTypeContributionArr = adjustmentTypeContribution.split(","); 
		  adjustmentTypeContribution = "'" + StringUtils.join(adjustmentTypeContributionArr,"','") + "'";
		
		  String period = null;
		  LinkedHashMap<String, Money> debtOblMap = new LinkedHashMap<String, Money>();
		  LinkedHashMap<String, HashMap<List<String>,List<Money>>> periodMap = new LinkedHashMap<String, HashMap<List<String>,List<Money>>>();
		     LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>,List<Money>>>> debtPriorityMap = new LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>,List<Money>>>>();
		  
		     psPreparedStatement = createPreparedStatement("select OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG, "
		                +" ADJ.ADJ_TYPE_CD,ADJ.ADJ_ID,ADJ.ADJ_AMT,OBL.START_DT,ADJ.CRE_DT from CI_SA OBL,CI_ADJ ADJ "
		                +" where ADJ.SA_ID=OBL.SA_ID "
		                +" and OBL.acct_id = \'"+accountId+"\' "
		                +" and ADJ.ADJ_TYPE_CD IN("+adjustmentTypeContribution.trim()+") "
		                +" and OBL.SA_TYPE_CD in(\'"+oblType1+"\',\'"+oblType2+"\',\'"+oblType3+"\') "
		                +" and OBL.SA_STATUS_FLG=40 ORDER BY OBL.START_DT","select");
		     
		     /*psPreparedStatement = createPreparedStatement("select OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG, "
		                +" ADJ.ADJ_TYPE_CD,ADJ.ADJ_ID,ADJ.ADJ_AMT,OBL.START_DT,ADJ.CRE_DT from CI_SA OBL,CI_ADJ ADJ "
		                +" where ADJ.SA_ID=OBL.SA_ID "
		                +" and OBL.acct_id = \'"+accountId+"\' "
		                +" and ADJ.ADJ_TYPE_CD IN(\'"+adjType1+"\',\'"+adjType2+"\',\'"+adjType3+"\',\'"+adjType4+"\',\'"+adjType5+"\',\'"+adjType6+"\') "
		                +" and OBL.SA_TYPE_CD in(\'"+oblType1+"\',\'"+oblType2+"\',\'"+oblType3+"\') "
		                +" and OBL.SA_STATUS_FLG=40 ORDER BY OBL.START_DT","select");*/
		                
		     
		     psPreparedStatement.setAutoclose(false);
		     QueryIterator<SQLResultRow> result = null;
		   try {
		    
			 result = psPreparedStatement.iterate();
			 QueryIterator<SQLResultRow> oblResultIterator = null;
		    List<Money> moneyList = new ArrayList<Money>();
		    List<String> oblgList = new ArrayList<String>();
		    List<String> saIdList = new  ArrayList<String>();
		    HashMap<List<String>, List<Money>> oblMoneyMap = new HashMap<List<String>, List<Money>>();
		    while (result.hasNext()) {
		     System.out.println("I am In");
		     SQLResultRow lookUpValue = result.next();
		     System.out.println(lookUpValue.getString("SA_ID"));
		     if(!saIdList.contains(lookUpValue.getString("SA_ID"))){
		      saIdList.add(lookUpValue.getString("SA_ID"));
		     try {
		      psPreparedStatement = createPreparedStatement("SELECT SUM(CUR_AMT) AS \"Total\" from CI_FT where SA_ID = "+ lookUpValue.getString("SA_ID"), "select");
		      psPreparedStatement.setAutoclose(false);
		      oblResultIterator = psPreparedStatement.iterate();
		      while (oblResultIterator.hasNext()) {
		       System.out.println("I am In");
		       SQLResultRow oblResult = oblResultIterator.next();
		       System.out.println("Total :: "+oblResult.getString("Total"));
		       System.out.println(lookUpValue.getString("SA_ID"));
		       if (oblResult.getString("Total") != null && Integer.parseInt(oblResult.getString("Total")) > 0) {
		        debtOblMap.put(lookUpValue.getString("SA_ID"), new Money(oblResult.getString("Total")));
		        
		        if(null == period || lookUpValue.getString("START_DT").equalsIgnoreCase(period)){
		         period = lookUpValue.getString("START_DT");
		         moneyList.add(new Money(oblResult.getString("Total")));
		         oblgList.add(lookUpValue.getString("SA_ID"));
		         oblMoneyMap = new HashMap<List<String>,List<Money>>();
		         oblMoneyMap.put(oblgList, moneyList);
		         periodMap.put(period, oblMoneyMap);
		        } else if(!lookUpValue.getString("START_DT").equalsIgnoreCase(period)) {
		         moneyList = new ArrayList<Money>();
		         oblgList = new ArrayList<String>();
		         oblMoneyMap = new HashMap<List<String>,List<Money>>();
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
		     }finally {
		    	 oblResultIterator.close();
		     }
		    }
		     
		   }
		    if (!debtOblMap.isEmpty() && !periodMap.isEmpty()) {
		    debtPriorityMap.put(debtOblMap, periodMap);
		    }else{
		    	addError(StandardMessages.fieldInvalid("The DNS ID mentioned for payment is not linked with ID-DNS characteristic type"));
		    }
		  } catch (Exception exception) {
		    exception.printStackTrace();
		   } finally {
		    psPreparedStatement.close();
		    psPreparedStatement = null;
		    result.close();
		   }
		  //}
		  return debtPriorityMap;
		 
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
	
	/**
	 * @param obligationId
	 * @param adjustmentType
	 * @param debtMoney
	 * @param debtCat
	 * @param date
	 * @return
	 */
	private String createAdjustment(String obligationId, String overpaymentObligationId, String adjustmentType, Money debtMoney,String debtCat,
			com.splwg.base.api.datatypes.Date date) { 
		
		    BusinessServiceInstance businessServiceInstanc = BusinessServiceInstance.create("CM-AdjustmentAddFreeze");
			COTSInstanceNode cotsGroup = businessServiceInstanc.getGroupFromPath("input");
			cotsGroup.set("serviceAgreement", obligationId);
			cotsGroup.set("adjustmentType", adjustmentType);
			cotsGroup.set("adjustmentAmount", debtMoney);
			cotsGroup.set("debtCategory", debtCat); //	
			cotsGroup.set("adjustmentDate", date);
			cotsGroup.set("transferServiceAgreement", overpaymentObligationId);
			

		  return executeBSAndCreateAdjustment(businessServiceInstanc);
		  
	}
	
	/**
	 * @param bsInstance
	 * @return
	 */
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
	
	@SuppressWarnings("deprecation")
	private HashMap<String,List<String>> getOverPaymentOblDetails(String accountId) {
		
		PreparedStatement psPreparedStatement = null;
		String saId = null;
		String accntId = null;
		//String oblTypePf="E-AVPF";
		String oblTypePf=getObligationTypeOverpaymentPf();
		String oblTypeAtmp=getObligationTypeOverpaymentAtmp();
		String oblTypeVr=getObligationTypeOverpaymentEr();
		psPreparedStatement = createPreparedStatement("SELECT OBL.SA_ID,OBL.ACCT_ID FROM  "      
        +" CI_SA OBL WHERE "
        +" OBL.ACCT_ID =\'"+accountId+"\' "
        +" AND OBL.SA_TYPE_CD in(\'"+oblTypePf+"\',\'"+oblTypeAtmp+"\',\'"+oblTypeVr+"\')","select");
		
		QueryIterator<SQLResultRow> result = null;
		HashMap<String,List<String>> mapOblID =  new HashMap<String,List<String>>();
		
		
		try{
			//psPreparedStatement.bindString("PER_ID", perId, null);
			result = psPreparedStatement.iterate();
			
			while(result.hasNext()){
				SQLResultRow lookUpValue = result.next();
				
				saId = lookUpValue.getString("SA_ID"); // 1235
				accntId = lookUpValue.getString("ACCT_ID"); //1234
				if(!mapOblID.containsKey(accntId)){
					saList = new ArrayList<String>();
					saList.add(saId);
					mapOblID.put(accntId, saList);
				} else {
					saList = mapOblID.get(accntId);
					saList.add(saId);
					mapOblID.put(accntId, saList);
				}
			}
			
		} catch(Exception exception){
			logger.error("Error in getOverPaymentOblDetails " +exception);
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
			saveChanges();
		}
		
		return mapOblID;
	}

	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		// TODO Auto-generated method stub
		return this.inputOutputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData) {
		// TODO Auto-generated method stub
		this.inputData = applyFormRuleAlgorithmInputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData) {
		// TODO Auto-generated method stub
		this.inputOutputData = applyFormRuleAlgorithmInputOutputData;
	}

}
