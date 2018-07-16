package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_DTO;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * @author Papa
 *
 * @AlgorithmComponent ()
 */
public class CmAccountTaxRoleCheck_Impl extends CmAccountTaxRoleCheck_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {

	private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
	private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;
	
	private Person getPersonByIdNumber(String IdType, String idNumber) {
		// log.info("*****Starting getpersonId");
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		// log.info("*****ID Type: " + idType.getTrimmedValue());
		return perSearch.searchPerson(idType.getEntity(), idNumber);  
	}
	
	
	@Override
	public void invoke() {

		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData
				.getFormBusinessObject();
		
		FormRule formRule = applyFormRuleAlgorithmInputData.getFormRuleId().getEntity();
		// Reading Form Rule Information
		BusinessObjectInstance formRuleBoInstance = BusinessObjectInstance.create(formRule.getBusinessObject());
		formRuleBoInstance.set("bo", formRule.getBusinessObject().getId().getTrimmedValue());
		formRuleBoInstance.set("formRuleGroup", formRule.getId().getFormRuleGroup().getId().getTrimmedValue());
		formRuleBoInstance.set("formRule", formRule.getId().getFormRule());
		formRuleBoInstance.set("sequence", BigDecimal.valueOf(formRule.getSequence().longValue()));
		formRuleBoInstance = BusinessObjectDispatcher.read(formRuleBoInstance);
		// Form Rule Details Group
//		COTSInstanceNode ruleDetails = formRuleBoInstance.getGroup("ruleDetails");
		String idType=formBoInstance.getFieldAndMDForPath("informationEmployeur/typeIdentifiant/asCurrent").getXMLValue();
		String idNumber = (String) formBoInstance.getFieldAndMDForPath("informationEmployeur/idNumber/asCurrent").getValue();
		Person person=getPersonByIdNumber(idType, idNumber);
		if(person==null){
			addError(CmMessageRepository90000.MSG_10001());       
		}

		String idEmployeur = person.getId().getIdValue();
		List<BigDecimal> listeComptes=getAccountsByIdPerson(idEmployeur);
		if(listeComptes==null){  
			// message erreur
			addError(CmMessageRepository90000.MSG_10()); 
		}
		else{
			for(BigDecimal element: listeComptes){
				String idTaxrole=getTaxRolesByIdAccount(element.toString());
				if(idTaxrole==null){
					// message erreur
					addError(CmMessageRepository90000.MSG_10()); 
				}
			}
		}
  
	}
	public List<BigDecimal> getAccountsByIdPerson(String personId) {
		List<BigDecimal> listeAccounts = new ArrayList<BigDecimal>();
		;
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GetPersonAccounts");

		bsInstance.set("personId", personId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);

		Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			System.out.println("AccountId: " + nextElt.getNumber("accountId"));
			System.out.println("AccountInfo: " + nextElt.getString("accountInfo"));
			listeAccounts.add(nextElt.getNumber("accountId"));

		}
		return listeAccounts;
	}

	public String getTaxRolesByIdAccount(String accountId) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-RetTaxRolesOfAccountList");

		bsInstance.set("accountId", accountId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		String resultat = null;
		COTSInstanceList list = bsInstance.getList("results");
		System.out.println("liste: " + list);
		// If list IS NOT empty
		if (!list.isEmpty()) {
			COTSInstanceListNode fistRow = list.iterator().next();
			if (fistRow != null) {
				System.out.println("TaxRoleId: " + fistRow.getString("taxRoleId"));
				System.out.println("TaxRoleInformation: " + fistRow.getString("taxRoleInformation"));
				resultat = fistRow.getString("taxRoleId");
			}

		}
		return resultat;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(
			ApplyFormRuleAlgorithmInputData paramApplyFormRuleAlgorithmInputData) {
		applyFormRuleAlgorithmInputData = paramApplyFormRuleAlgorithmInputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData paramApplyFormRuleAlgorithmInputOutputData) {
		applyFormRuleAlgorithmInputOutputData = paramApplyFormRuleAlgorithmInputOutputData;
	}

	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return applyFormRuleAlgorithmInputOutputData;
	}

}

