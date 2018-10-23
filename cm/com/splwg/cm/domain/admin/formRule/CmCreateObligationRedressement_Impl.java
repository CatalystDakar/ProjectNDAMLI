package com.splwg.cm.domain.admin.formRule;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.customerinfo.person.Person;

/**
 * @author SKone
 *
@AlgorithmComponent ()
 */
public class CmCreateObligationRedressement_Impl extends CmCreateObligationRedressement_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {
	
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	Logger logger = LoggerFactory.getLogger(CmCreateObligation_Impl.class);

	@Override
	public void invoke() {
		// TODO Auto-generated method stub
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		
		//Retrieve sums written in the form
		Money montantPF = (Money) formBoInstance.getFieldAndMDForPath("MontantsDeRedressement/cmMontantRedressePf/asCurrent").getValue();
		Money montantAT = (Money) formBoInstance.getFieldAndMDForPath("MontantsDeRedressement/cmMontantRedresseAtMp/asCurrent").getValue();
		Money montantRE = (Money) formBoInstance.getFieldAndMDForPath("MontantsDeRedressement/cmMontantRedresse/asCurrent").getValue();
		Money montantMajPF = (Money) formBoInstance.getFieldAndMDForPath("MontantsDeRedressement/cmMajorationPF/asCurrent").getValue();
		Money montantMajAT = (Money) formBoInstance.getFieldAndMDForPath("MontantsDeRedressement/cmMajorationAtMp/asCurrent").getValue();
		Money montantMajRE = (Money) formBoInstance.getFieldAndMDForPath("MontantsDeRedressement/cmMajorRetraite/asCurrent").getValue();
		
		//Take the person's (employer's) informations
		String typeIdentifiant = (String) formBoInstance
				.getFieldAndMDForPath("informationEmployeur/typeIdentifiant/asCurrent").getValue();
		String idNumber = (String) formBoInstance.getFieldAndMDForPath("informationEmployeur/idNumber/asCurrent")
				.getValue();
		
		
		//Some default value testing
		/*String typeIdentifiant = "SCI";
		String idNumber = "189702555";
		Money montantPF = new Money("5000");*/
		
		String idEmployeur = getPersonByNinea(typeIdentifiant, idNumber).getId().getIdValue();
		

		String compte = getAccountsByIdPerson(idEmployeur);
		
		//Get the 3 divisions from the 3 obligation types
		String division1 = getDivisionByObligationType("CM-REDPF");
		String division2 = getDivisionByObligationType("CM-REDAT");
		String division3 = getDivisionByObligationType("CM-REDRE");
		
		//Create the 3 obligations
		String o1 = createObligation(compte, division1, "CM-REDPF");
		String o2 = createObligation(compte, division2, "CM-REDAT");
		String o3 = createObligation(compte, division3, "CM-REDRE");
		
		//Create the 6 adjustments
		String idAjustementCree1 = createAjustementBS("CM-REDPF", o1, montantPF.getAmount(), getSystemDateTime().getDate());
		System.out.println("Voila adj1 "+idAjustementCree1);
		
		String idAjustementCree2 = createAjustementBS("CM-REDAT", o2, montantAT.getAmount(), getSystemDateTime().getDate());
		System.out.println("Voila adj2 "+idAjustementCree2);
		
		String idAjustementCree3 = createAjustementBS("CM-REDRE", o3, montantRE.getAmount(), getSystemDateTime().getDate());
		System.out.println("Voila adj3 "+idAjustementCree3);
		
		String idAjustementMaj1 = createAjustementBS("CM-RMPF", o1, montantMajAT.getAmount(), getSystemDateTime().getDate());
		System.out.println("Voila adjMaj1 "+idAjustementMaj1);
		
		String idAjustementMaj2 = createAjustementBS("CM-RMAT", o2, montantMajPF.getAmount(), getSystemDateTime().getDate());
		System.out.println("Voila adjMaj2 "+idAjustementMaj2);
		
		String idAjustementMaj3 = createAjustementBS("CM-RMRE", o3, montantMajRE.getAmount(), getSystemDateTime().getDate());
		System.out.println("Voila adjMaj3 "+idAjustementMaj3);
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
	
	private Person getPersonByNinea(String IdType, String idNumber) {
		// log.info("*****Starting getpersonId");
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		// log.info("*****ID Type: " + idType.getTrimmedValue());
		return perSearch.searchPerson(idType.getEntity(), idNumber);
	}
	
	public String getAccountsByIdPerson(String personId) {
		String result = null;
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GetPersonAccounts");

		bsInstance.set("personId", personId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		COTSInstanceList list = bsInstance.getList("results");
		if (!list.isEmpty()) {
			COTSInstanceListNode fistRow = list.iterator().next();
			if (fistRow != null) {
				result=fistRow.getFieldAndMDForPath("accountId").getXMLValue(); 
			}

		}
		return result;
	}
	
	private String getDivisionByObligationType(String obligationType) {
		// TODO Auto-generated method stub
		String resultat = null;
		String query = "SELECT CIS_DIVISION FROM CI_SA_TYPE WHERE SA_TYPE_CD='" + obligationType + "'";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		// preparedStatement.bindString("obligationType", obligationTypes,
		// null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();

		if (sqlResultRow != null) {
			resultat = sqlResultRow.getString("CIS_DIVISION");
			System.out.println("RESULTAT SQL= " + resultat);
		}
		return resultat;
	}
	
	private String createAjustementBS(String adjustType, String obligationId, BigDecimal adjustmentAmount,
			Date effectiveDate) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-AdjustmentAddFreeze");
		if (null != adjustType && null != obligationId && null != adjustmentAmount) {
			COTSInstanceNode group = bsInstance.getGroupFromPath("input");
			group.set("serviceAgreement", obligationId);
			group.set("adjustmentType", adjustType);
			group.set("adjustmentAmount", adjustmentAmount);
			System.out.println(getSystemDateTime().getDate());
			group.set("adjustmentDate", getSystemDateTime().getDate());// getSystemDateTime().getDate()
			group.set("arrearsDate", effectiveDate);
		}
		// Execute BS and return the Ninea if exists
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		COTSInstanceNode output = bsInstance.getGroupFromPath("output");
		return output.getString("adjustment");

	}
	
	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return this.inputOutputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData) {
		this.inputData = applyFormRuleAlgorithmInputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData) {
		this.inputOutputData = applyFormRuleAlgorithmInputOutputData;
	}

}
