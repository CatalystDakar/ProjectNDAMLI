package com.splwg.cm.domain.testcase;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.api.testers.AlgorithmImplementationTestCase;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.Algorithm_Id;
import com.splwg.cm.domain.admin.formRule.CmDistributionRuleCreatePaymentOnAccountAlgoComp;
import com.splwg.cm.domain.admin.formRule.CmDistributionRuleCreatePaymentOnAccountAlgoComp_Impl;
import com.splwg.tax.domain.admin.distributionRule.DistributionRule;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.payment.payment.Payment_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent_Id;

public class AlgoTest extends AlgorithmImplementationTestCase{

@SuppressWarnings("deprecation")
public void testInvoke() {
		
	 Algorithm alg = new Algorithm_Id("CM-PAYACCALG").getEntity();
	 //alg.getAlgorithmComponent(arg0, arg1)
	 CmDistributionRuleCreatePaymentOnAccountAlgoComp cc = alg.getAlgorithmComponent(CmDistributionRuleCreatePaymentOnAccountAlgoComp.class);
	 PaymentEvent paymentEvent;
	 DistributionRule distributionRule;
	 Money amount;
	 String characteristicValueFk1;
	 BigInteger sequence;
	 Payment_Id paymentId;
	 
		System.out.println("***test start***");
		//Account_Id id = new Account_Id("2456607326");
		//Account tenderObligation =  id.getEntity();
		cc.setCharacteristicValueFk1(new Account_Id("0718403322").getEntity().getId().getIdValue());
		cc.setAmount(new Money("30000"));
		cc.setPaymentEvent(new PaymentEvent_Id("245660788074").getEntity());
		cc.setSequence(new java.math.BigInteger("1"));
		//cc.setCharacteristicValueFk1("2456607326");
		cc.invoke();
		startChanges();			
		/*PreparedStatement psPreparedStatement = null;
		psPreparedStatement = createPreparedStatement("select ACCT_ID from CI_ACCT_PER where PER_ID = (select PER_ID from CI_ACCT_PER where ACCT_ID = '2456607326')");
		psPreparedStatement.setAutoclose(false);
		List<String> accList = new ArrayList<String>();
		
		try {
			QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
			while(result.hasNext()) {
			System.out.println("I am In");
			SQLResultRow lookUpValue= result.next();
			System.out.println(lookUpValue.getString("ACCT_ID"));
			accList.add(lookUpValue.getString("ACCT_ID"));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}
		
		List<String> saList = getObligationList(accList);
		List<String> debtObList = getDebtObligation(saList);//1000
		
		
		System.out.println("***test end***");
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


	private List<String> getDebtObligation(List<String> saList) {
	
	PreparedStatement psPreparedStatement = null;
	
	Iterator its = saList.iterator();
	String sa_id = "";
	List<String> oblId = new ArrayList<String>();
	while(its.hasNext()) {
		sa_id = (String) its.next();
		psPreparedStatement = createPreparedStatement("SELECT SUM(CUR_AMT) AS \"Total\" FROM CI_FT WHERE SA_ID = "+sa_id);
		psPreparedStatement.setAutoclose(false);
		try {
			QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
			while(result.hasNext()) {
			System.out.println("I am In");
			SQLResultRow lookUpValue= result.next();
			System.out.println(lookUpValue.getString("Total"));
			if(lookUpValue.getString("Total") !=null && Integer.parseInt(lookUpValue.getString("Total")) > 0) {
				oblId.add(lookUpValue.getString("Total"));
			}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}
	}
	return oblId;
*/	}


	@Override
	protected Class getAlgorithmImplementationClass() {
		// TODO Auto-generated method stub
		return CmDistributionRuleCreatePaymentOnAccountAlgoComp.class;
	}

}
