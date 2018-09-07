package com.splwg.cm.domain.testcase;

import java.math.BigInteger;

import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.testers.AlgorithmImplementationTestCase;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.Algorithm_Id;
import com.splwg.cm.domain.admin.formRule.CmAllocationFamilyBillGenerationAlgo;
import com.splwg.tax.domain.admin.distributionRule.DistributionRule;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.payment.payment.Payment_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentEvent;

public class AlgoTest extends AlgorithmImplementationTestCase{

@SuppressWarnings("deprecation")
public void testInvoke() {
		
	 Algorithm alg = new Algorithm_Id("CM-FALBILALG").getEntity();
	 //alg.getAlgorithmComponent(arg0, arg1)CmFamilyBenefitsBillGenerationAlgo_Impl
	 CmAllocationFamilyBillGenerationAlgo cc = alg.getAlgorithmComponent(CmAllocationFamilyBillGenerationAlgo.class);
	 //CmDistributionRuleCreatePaymentOnAccountAlgoComp cc = alg.getAlgorithmComponent(CmDistributionRuleCreatePaymentOnAccountAlgoComp.class);
	 PaymentEvent paymentEvent;
	 DistributionRule distributionRule;
	 Money amount;
	 String characteristicValueFk1;
	 BigInteger sequence;
	 Payment_Id paymentId;
	 
		System.out.println("***test start***");
		Account_Id id = new Account_Id("0959861870");
		Account tenderObligation =  id.getEntity();
		//cc.setCharacteristicValueFk1("0959861870");
		//cc.setAmount(new Money("2000000"));//18422211
		//cc.setPaymentEvent(new PaymentEvent_Id("123456789012").getEntity());
		//paymentDTO.setPaymentEventId(new PaymentEvent_Id("245693748074"));
		//cc.setSequence(new BigInteger("1"));
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
		return CmAllocationFamilyBillGenerationAlgo.class;
	}

}
