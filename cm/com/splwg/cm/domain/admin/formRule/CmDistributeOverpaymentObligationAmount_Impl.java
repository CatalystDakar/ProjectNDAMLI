package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.ValidateBusinessObjectAlgorithmSpot;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.PaymentStatusLookup;
import com.splwg.tax.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCode;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.tax.domain.financial.matchEvent.MatchEvent_Id;
import com.splwg.tax.domain.payment.payment.CreateDistributeFreezePayment;
import com.splwg.tax.domain.payment.payment.CreateDistributeFreezePayment.Factory;
import com.splwg.tax.domain.payment.payment.Payment;
import com.splwg.tax.domain.payment.payment.PaymentSegment;
import com.splwg.tax.domain.payment.payment.PaymentSegment_DTO;
import com.splwg.tax.domain.payment.payment.Payment_DTO;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent_Id;

/**
 * @author Anita M
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = adjustmentTypeInterest2, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeInterest1, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypePenalty1, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeContribution3, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeContribution2, type = string)
 *            , @AlgorithmSoftParameter (name = adjustmentTypeContribution1, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeContribution3, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeContribution2, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeContribution1, type = string)
 *            , @AlgorithmSoftParameter (name = obligationTypeOverpayment, type = string)})
 */


public class CmDistributeOverpaymentObligationAmount_Impl extends CmDistributeOverpaymentObligationAmount_Gen
		implements ValidateBusinessObjectAlgorithmSpot {

	//private String taxFormId = "739448654599";
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	private static final Logger logger = LoggerFactory
			.getLogger(CmDistributeOverpaymentObligationAmount_Impl.class);
	private Money overPaymentTotalAmount = Money.ZERO;
	String overpaymentObligationId = null;
	
	@SuppressWarnings("deprecation")
	@Override
	public void invoke() {

		//String taxFormID="739448654599";
		startChanges();
		PreparedStatement psPreparedStatement = null;
		String accountId = null;
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		COTSFieldDataAndMD tax = formBoInstance.getFieldAndMDForPath(("taxFormId"));
		String taxFormId = tax.getXMLValue();
		//psPreparedStatement = createPreparedStatement("select per_id from ci_tax_form where tax_form_id=:tax_form_id");
		psPreparedStatement = createPreparedStatement("select acc.ACCT_ID from ci_acct_per acc,ci_tax_form "
				+ "tax where acc.per_id=tax.per_id and tax.tax_form_id=\'"+tax+"\'");
		QueryIterator<SQLResultRow> result = null;
		//List<String> accList = new ArrayList<String>();
		HashMap<String,String> oblTotalAmountMap =  new HashMap<String,String>();
		
		try{
			//psPreparedStatement.bindString("tax_form_id", taxFormId, null);
			result = psPreparedStatement.iterate();
			
			while(result.hasNext())
			{
				SQLResultRow lookUpValue = result.next();
				accountId = lookUpValue.getString("ACCT_ID");
				//accountId = "5811295034";
				System.out.println(lookUpValue.getString("ACCT_ID"));
				//accList.add(lookUpValue.getString("ACCT_ID"));
	    //for(String acctId : accList){
			HashMap<String,List<String>> hashMapPayDetails = getOverPaymentOblDetails(accountId);
		if(!hashMapPayDetails.isEmpty())
		{
			for(Map.Entry<String, List<String>> payDetObj : hashMapPayDetails.entrySet()){
				List<String> obList = payDetObj.getValue();
				overpaymentObligationId = payDetObj.getValue().get(0);
				String totalAmount = null;
				for(int index=0 ; index<obList.size();index++){
					String ObliId  = obList.get(index);
					psPreparedStatement = createPreparedStatement("select sum(cur_amt) as TOTAL_AMOUNT from ci_ft where sa_id=\'"+ObliId+"\'");
					QueryIterator<SQLResultRow> resultTotal = null;
					
					try{
						//psPreparedStatement.bindString("SA_ID", ObliId, null);
						result = psPreparedStatement.iterate();
						
						while(result.hasNext()){
							SQLResultRow lookUpVal = result.next();
							totalAmount = lookUpVal.getString("TOTAL_AMOUNT");
							Money money = new Money(totalAmount);
							overPaymentTotalAmount = overPaymentTotalAmount.add(money);
							oblTotalAmountMap.put(ObliId, totalAmount); //
						}
					} catch(Exception exception){
						logger.error("Exception in getting overpayment obligation amount from FT "+ exception);
					}
					
				}
			}
		
			
			LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> debtOblMap = getDebtObligation(accountId);
			  
			  //logger.info("debtOblMap: " + debtOblMap.size());
			  System.out.println("debtOblMap: " + debtOblMap.size());
			  ServiceAgreement debtObligation = null;
			  Money debtMoney = Money.ZERO;
			  Money moneyValue = Money.ZERO;
			  Money actualMoneyValue = overPaymentTotalAmount;
			  String periodValue = null;
			  String adjustmentTypeValue = null;
			  String debtOblID = null;
			  
			  if(!debtOblMap.isEmpty()) {
			  for(Map.Entry<HashMap<String, Money>, HashMap<String, HashMap<List<String>,List<Money>>>> debtMapObj : debtOblMap.entrySet()){
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
			     logger.info("ServiceAgreement: " + debtObligation);
			     System.out.println("ServiceAgreement: " + debtObligation);
			     debtOblID = moneyEntry.getKey();
			     debtMoney = moneyEntry.getValue();
			     System.out.println("DebtMoney: " + debtMoney);
			     logger.info("Amount before the payment creation:: " + overPaymentTotalAmount);
			     System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			     if (!overPaymentTotalAmount.isZero() && overPaymentTotalAmount.isNegative()) {
			    	 
			    	 String adjTypeRetrieve = getAllAdjustmentType(accountId);
			    	 if(adjTypeRetrieve.equalsIgnoreCase("PF") && adjTypeRetrieve!=null )
			    	 {
			    		 adjustmentTypeValue = "CPF";
			    	 }
			    	 if(adjTypeRetrieve.equalsIgnoreCase("ATMP") && adjTypeRetrieve!=null)
			    	 {
			    		 adjustmentTypeValue = "CATMP";
			    	 }
			    	 if(adjTypeRetrieve.equalsIgnoreCase("OLDAGE") && adjTypeRetrieve!=null)
			    	 {
			    		 adjustmentTypeValue = "CR";
			    	 }
			     String adjustmentIdPendingOblg = createAdjustment(debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
			     String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
			     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
			     }
			          }
			    
/*			    if(!overPaymentTotalAmount.isZero() && overPaymentTotalAmount.isPositive()){
			     logger.info("********Creating OverPayment Adjustment*******");
			     
			     List<String> accountDetailList = getAllAccountDetailsFromAccountId();
			     if(!accountDetailList.isEmpty()){
			      BigDecimal bg = new BigDecimal(accountDetailList.size());
			      BigDecimal splitMoneyDec  = overPaymentTotalAmount.getAmount().divide(bg);
			      for(String accntId : accountDetailList){
			       String obligationId = createObligation(accntId,"DOR", "E-TPERCU");
			       logger.info("OverPayment Created against the Obligation ID"+ obligationId +"for account ID:"+accntId);
			       
			       ServiceAgreement_Id sa_id = new ServiceAgreement_Id(obligationId);
			       logger.info("ServiceAgreement_Id: " + sa_id);
			       System.out.println("ServiceAgreement_Id, : " + sa_id);
			       debtObligation = (ServiceAgreement) sa_id.getEntity();
			       logger.info("ServiceAgreement: " + debtObligation);
			       System.out.println("ServiceAgreement: " + debtObligation);
			       Money splitMoney = new Money(splitMoneyDec);
			       logger.info("DebtMoney: " + splitMoneyDec);
			       logger.info("DebtMoney: " + splitMoney);
			       logger.info("Amount before the payment creation:: " + overPaymentTotalAmount);
			       System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			       if (!overPaymentTotalAmount.isZero() && overPaymentTotalAmount.isPositive()) {
			        this.createFrozenPayment(debtObligation, splitMoney);
			       }
			      }
			      
			      //String adjustmentId = createAdjustment(obligationId,  getAdjustmentType7(), this.amount, "OVERPAY", getSystemDateTime().getDate());
			      //logger.info("OverPayment Created against the Adjustment ID: " + adjustmentId);
			     }
			    }*/
			     
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
			         
			         if(!monthObligationMoney.isZero() && overPaymentTotalAmount.negate().isLessThanOrEqual(monthObligationMoney)){
			          logger.info("###Creating payment for same month obligations####" );
			         System.out.println("###Creating payment for same month obligations####" );
			          for(int i=0;i<moneyList.size();i++){
			           Money obligationMoney = moneyList.get(i);
			           String oblStr = obligIdList.get(i);
			           int prorateMoney = Math.round(overPaymentTotalAmount.negate().getAmount().floatValue()/monthObligationMoney.getAmount().floatValue()*obligationMoney.getAmount().floatValue());
			           debtMoney = new Money(String.valueOf(prorateMoney));
			           ServiceAgreement_Id sa_id = new ServiceAgreement_Id(oblStr);
			           logger.info("obligation Money: " + obligationMoney);
			           logger.info("Screen Amount: " + overPaymentTotalAmount);
			           logger.info("Month Obligation Money: " + monthObligationMoney);
			           logger.info("Actual Money Value: " + actualMoneyValue);
			          
			          logger.info("ServiceAgreement_Id: " + sa_id);
			          System.out.println("ServiceAgreement_Id: " + sa_id);
			          debtObligation = (ServiceAgreement) sa_id.getEntity();
			          logger.info("ServiceAgreement: " + debtObligation);
			          System.out.println("ServiceAgreement: " + debtObligation);
			          System.out.println("DebtMoney: " + debtMoney);
			          logger.info("Amount before the payment creation:: " + overPaymentTotalAmount);
			          System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			          if (!overPaymentTotalAmount.isZero() && overPaymentTotalAmount.isNegative()) {
			           //this.createFrozenPayment(debtObligation, debtMoney);
			        	  String adjTypeRetrieve = getAllAdjustmentType(accountId);
					    	 if(adjTypeRetrieve.equalsIgnoreCase("PF") && adjTypeRetrieve!=null )
					    	 {
					    		 adjustmentTypeValue = "CPF";
					    	 }
					    	 if(adjTypeRetrieve.equalsIgnoreCase("ATMP") && adjTypeRetrieve!=null)
					    	 {
					    		 adjustmentTypeValue = "CATMP";
					    	 }
					    	 if(adjTypeRetrieve.equalsIgnoreCase("OLDAGE") && adjTypeRetrieve!=null)
					    	 {
					    		 adjustmentTypeValue = "CR";
					    	 }
			             String adjustmentIdPendingOblg = createAdjustment(oblStr,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
					     String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
					     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
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
			           logger.info("ServiceAgreement: " + debtObligation);
			           System.out.println("ServiceAgreement: " + debtObligation);
			           debtOblID = obligIdListt.get(i);
			           debtMoney = moneyListt.get(i);
			           System.out.println("DebtMoney: " + debtMoney);
			           logger.info("DebtMoney:" + debtMoney);
			           logger.info("Amount before the payment creation :: " + overPaymentTotalAmount);
			           System.out.println("Amount before the payment creation:: " + overPaymentTotalAmount);
			           if (!overPaymentTotalAmount.isZero() && overPaymentTotalAmount.isNegative()) {
			            //this.createFrozenPayment(debtObligation, debtMoney);
			        	   String adjTypeRetrieve = getAllAdjustmentType(accountId);
					    	 if(adjTypeRetrieve.equalsIgnoreCase("PF") && adjTypeRetrieve!=null )
					    	 {
					    		 adjustmentTypeValue = "CPF";
					    	 }
					    	 if(adjTypeRetrieve.equalsIgnoreCase("ATMP") && adjTypeRetrieve!=null)
					    	 {
					    		 adjustmentTypeValue = "CATMP";
					    	 }
					    	 if(adjTypeRetrieve.equalsIgnoreCase("OLDAGE") && adjTypeRetrieve!=null)
					    	 {
					    		 adjustmentTypeValue = "CR";
					    	 }
			             String adjustmentIdPendingOblg = createAdjustment(debtOblID,adjustmentTypeValue,debtMoney.negate(),"COTISATION",getSystemDateTime().getDate());
					     String adjustmentIdOverpayOblg = createAdjustment(overpaymentObligationId,"A-TPERCU",debtMoney,"COTISATION",getSystemDateTime().getDate());
					     overPaymentTotalAmount = overPaymentTotalAmount.add(debtMoney);
			           }
			          }
			         }
			        }
			       }
			      }
			     }
			    }
            }else {
			    logger.info("There is no overpayment obligation money to pay");
			   } }
			   }else {
			    logger.info("There is oblogation to pay");
			   } 
			
			}}} catch(Exception exception){
			logger.error("Error in getting per id in tax form " +exception);
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
			saveChanges();
		}
		
		
	}
	
	
	@SuppressWarnings("deprecation")
	 private LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>, List<Money>>>> getDebtObligation(String accountId) {
	  
	  PreparedStatement psPreparedStatement = null;

	  //String dnsId   = getDnsId();
	  String perID  =  "9200542892";
	  String oblType1 = "O-EPF";
	  String oblType2 = "O-EATMP";
	  String oblType3 = "O-ER";
	  String oblType4 = "CPF";
	  String adjType1 = "CPF";
	  String adjType2 = "CATMP";
	  String adjType3 = "CR";
	  //String adjType4 = getAdjustmentType4();
	  //String adjType5 = getAdjustmentType5();
	  //String adjType6 = getAdjustmentType6();
	  //String adjType7 = getAdjustmentType7();

	  String period = null;
	  HashMap<String, Money> debtOblMap = new HashMap<String, Money>();
	  HashMap<String, HashMap<List<String>,List<Money>>> periodMap = new HashMap<String, HashMap<List<String>,List<Money>>>();
	     LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>,List<Money>>>> debtPriorityMap = new LinkedHashMap<HashMap<String, Money>, HashMap<String, HashMap<List<String>,List<Money>>>>();
	   //psPreparedStatement = createPreparedStatement("SELECT CAP.ACCT_ID, CS.SA_ID, CS.SA_TYPE_CD, CS.SA_STATUS_FLG ,"
	    // + "CADJ.ADJ_TYPE_CD, CADJ.ADJ_ID, CADJ.ADJ_AMT, CS.START_DT , CADJ.CRE_DT FROM CI_ACCT_PER CAP,"
	    // + " CI_ACCT CA ,CI_SA CS,CI_ADJ CADJ WHERE CAP.PER_ID IN (SELECT PER_ID from CI_ACCT_PER where ACCT_ID = :accId)"
	    //   + " AND CAP.ACCT_ID=CA.ACCT_ID AND CA.CUST_CL_CD IN(:acc1,:acc2,:acc3) AND CAP.ACCT_ID = CS.ACCT_ID "
	    //   + " AND CS.SA_TYPE_CD in(:oblType1,:oblType2,:oblType3) AND CS.SA_ID = CADJ.SA_ID AND CADJ.ADJ_TYPE_CD IN "
	    //   + " (:adjType1,:adjType2,:adjType3,:adjType4,:adjType5,:adjType6,:adjType7) AND CS.SA_STATUS_FLG=40 ORDER BY CS.START_DT");
	  
	     psPreparedStatement = createPreparedStatement("select OBL.acct_id,OBL.SA_ID,OBL.SA_TYPE_CD,OBL.SA_STATUS_FLG, "
	                +" ADJ.ADJ_TYPE_CD,ADJ.ADJ_ID,ADJ.ADJ_AMT,OBL.START_DT,ADJ.CRE_DT from CI_SA OBL,CI_ADJ ADJ,ci_ft FT "
	                +" where ADJ.SA_ID=OBL.SA_ID "
	                +" and FT.SA_ID=OBL.SA_ID "
	                +" and OBL.acct_id = \'"+accountId+"\' "
	                +" and ADJ.ADJ_TYPE_CD IN(\'"+adjType1+"\',\'"+adjType2+"\',\'"+adjType3+"\') "
	                +" and OBL.SA_TYPE_CD in(\'"+oblType1+"\',\'"+oblType2+"\',\'"+oblType3+"\') "
	                +" and OBL.SA_STATUS_FLG=40 ORDER BY OBL.START_DT");
	                
	     
	     psPreparedStatement.setAutoclose(false);
	   try {
	    //psPreparedStatement.bindString("accId", accId, null);
	    //psPreparedStatement.bindString("acc1", acc1, null);
	    //psPreparedStatement.bindString("acc2", acc2, null);
	    //psPreparedStatement.bindString("acc3", acc3, null);
	    //psPreparedStatement.bindString("oblType1", oblType1, null);
	    //psPreparedStatement.bindString("oblType2", oblType2, null);
	    //psPreparedStatement.bindString("oblType3", oblType3, null);
	    //psPreparedStatement.bindString("adjType1", adjType1, null);
	    //psPreparedStatement.bindString("adjType2", adjType2, null);
	    //psPreparedStatement.bindString("adjType3", adjType3, null);
	    //psPreparedStatement.bindString("adjType4", adjType4, null);
	    //psPreparedStatement.bindString("adjType5", adjType5, null);
	    //psPreparedStatement.bindString("adjType6", adjType6, null);
	    //psPreparedStatement.bindString("adjType7", adjType7, null);
	    
	    QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
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
	      QueryIterator<SQLResultRow> oblResultIterator = psPreparedStatement.iterate();
	      while (oblResultIterator.hasNext()) {
	       System.out.println("I am In");
	       SQLResultRow oblResult = oblResultIterator.next();
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
	     }
	    }
	     
	   }
	    debtPriorityMap.put(debtOblMap, periodMap);
	  } catch (Exception exception) {
	    exception.printStackTrace();
	   } finally {
	    psPreparedStatement.close();
	    psPreparedStatement = null;
	   }
	  //}
	  return debtPriorityMap;
	 }

	
	private List<String> getAllAccountDetailsFromAccountId() {
		  
		  PreparedStatement accntPreparedStatement = null;
		  QueryIterator<SQLResultRow> accntResultIterator = null;
		  List<String> accountDetailsList = null;
		  String accountId = null;
		  String perID1 = "9200542892";
		  accntPreparedStatement = createPreparedStatement("select ACCT_ID from CI_ACCT_PER where PER_ID =\'"+perID1+"\'", "select");
		  accntPreparedStatement.setAutoclose(false);
		  try {
		   startChanges();
		   accountDetailsList = new ArrayList<String>();
		   accntResultIterator = accntPreparedStatement.iterate();
		   while (accntResultIterator.hasNext()) {
		    SQLResultRow lookUpValue = accntResultIterator.next();
		    accountId = lookUpValue.getString("ACCT_ID");
		    accountDetailsList.add(accountId);
		   }
		  } catch (Exception excep) {
		   logger.error("Exception in getting  getAllAccountDetailsFromAccountId : " + excep);
		  } finally {
		   saveChanges();
		   accntPreparedStatement.close();
		   accntPreparedStatement = null;
		  }
		  return accountDetailsList;
		  
		 }
	
	private String getAllAdjustmentType(String accountId) {
		  
		  PreparedStatement adjTypePreparedStatement = null;
		  QueryIterator<SQLResultRow> adjTypeResultIterator = null;
		  //List<String> adjTypeList = null;
		  String adjustmentTypeForAccount = null;
		  //String perID1 = "9200542892";
		  adjTypePreparedStatement = createPreparedStatement("select CUST_CL_CD from ci_acct where acct_id =\'"+accountId+"\'", "select");
		  adjTypePreparedStatement.setAutoclose(false);
		  try {
		   startChanges();
		   //adjTypeList = new ArrayList<String>();
		   adjTypeResultIterator = adjTypePreparedStatement.iterate();
		   while (adjTypeResultIterator.hasNext()) {
		    SQLResultRow lookUpValue = adjTypeResultIterator.next();
		    adjustmentTypeForAccount = lookUpValue.getString("CUST_CL_CD").trim();
		    //adjTypeList.add(accountId);
		   }
		  } catch (Exception excep) {
		   logger.error("Exception in getting  getAllAccountDetailsFromAccountId : " + excep);
		  } finally {
		   saveChanges();
		   adjTypePreparedStatement.close();
		   adjTypePreparedStatement = null;
		  }
		  return adjustmentTypeForAccount;
		  
		 }
	
	/**
	 * @param obligation
	 * @param money
	 */
	private void createFrozenPayment(ServiceAgreement obligation, Money money) {

		logger.info("Money: " + money);
		System.out.println("Money: " + money);
		
		logger.info("Money String: " + String.valueOf(money));
		System.out.println("Money String: " + String.valueOf(money));
		
		Payment_DTO paymentDTO = (Payment_DTO) this.createDTO(Payment.class);
		paymentDTO.setAccountId(obligation.getAccount().getId());
		PaymentSegment_DTO paymentSegmentDTO = (PaymentSegment_DTO) this.createDTO(PaymentSegment.class);
		if(overPaymentTotalAmount.isLessThanOrEqual(money)) { //money = 4000, amount-screen = 5000
			paymentDTO.setPaymentAmount(overPaymentTotalAmount);
			paymentSegmentDTO.setPaySegmentAmount(overPaymentTotalAmount);
			overPaymentTotalAmount = overPaymentTotalAmount.subtract(money);
		} else if(overPaymentTotalAmount.isGreaterThan(money)){
			overPaymentTotalAmount = overPaymentTotalAmount.subtract(money);
			paymentDTO.setPaymentAmount(money);
			paymentSegmentDTO.setPaySegmentAmount(money);
		}
		paymentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());
		//paymentDTO.setSequence(this.sequence);
		paymentDTO.setPaymentEventId(new PaymentEvent_Id("289098567345"));
		//paymentDTO.setSequence(this.sequence);
		//paymentDTO.setPaymentEventId(this.paymentEvent.getId());
		paymentDTO.setPaymentStatus(PaymentStatusLookup.constants.FREEZABLE);
		paymentSegmentDTO.setServiceAgreementId(obligation.getId());
		paymentSegmentDTO.setCurrencyId(obligation.getAccount().getCurrency().getId());	
		MatchEvent_Id match = paymentSegmentDTO.getAdjustmentId().getEntity().getRelatedFinancialTransaction().getMatchEventId();
		paymentSegmentDTO.setMatchEventId(match);
		Adjustment_Id linkedAdjustmentId = paymentSegmentDTO.getAdjustmentId();
		CreateDistributeFreezePayment createDistributeFreezePayment = Factory.newInstance();
		Payment payment = createDistributeFreezePayment.process(paymentDTO, paymentSegmentDTO, (Date) null, (Date) null,
				(GeneralLedgerDistributionCode) null, linkedAdjustmentId);
		if (this.notNull(payment)) {
			//this.paymentId = payment.getId();
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
	private String createAdjustment(String obligationId, String adjustmentType, Money debtMoney,String debtCat,
			com.splwg.base.api.datatypes.Date date) { 
		
		    BusinessServiceInstance businessServiceInstanc = BusinessServiceInstance.create("CM-AdjustmentAddFreeze");
			COTSInstanceNode cotsGroup = businessServiceInstanc.getGroupFromPath("input");
			cotsGroup.set("serviceAgreement", obligationId);
			cotsGroup.set("adjustmentType", adjustmentType);
			cotsGroup.set("adjustmentAmount", debtMoney);
			cotsGroup.set("debtCategory", debtCat); //	
			cotsGroup.set("adjustmentDate", date);

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
		String oblType="E-TPERCU";
		psPreparedStatement = createPreparedStatement("SELECT OBL.SA_ID,OBL.ACCT_ID FROM  "      
        +" CI_SA OBL WHERE "
        +" OBL.ACCT_ID =\'"+accountId+"\' "
        +" AND OBL.SA_TYPE_CD =\'"+oblType+"\'");
		
		QueryIterator<SQLResultRow> result = null;
		HashMap<String,List<String>> mapOblID =  new HashMap<String,List<String>>();
		List<String> saList = new ArrayList<String>();
		
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

	}

	@Override
	public void setEntityId(EntityId arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaintenanceObject(MaintenanceObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNewBusinessObject(BusinessObjectInstance arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOriginalBusinessObject(BusinessObjectInstance arg0) {
		// TODO Auto-generated method stub

	}

}
