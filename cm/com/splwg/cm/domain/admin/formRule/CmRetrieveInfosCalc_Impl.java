package com.splwg.cm.domain.admin.formRule;

import java.util.Iterator;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessObject.SchemaInstance;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.MaintenanceObjectInfo;
import com.splwg.base.support.schema.MaintenanceObjectInfoCache;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.customerinfo.person.Person;

/**
 * @author Khadim Cisse
 *
 * @AlgorithmComponent ()
 */
public class CmRetrieveInfosCalc_Impl extends CmRetrieveInfosCalc_Gen implements FormRuleBORuleProcessingAlgorithmSpot {
	ApplyFormRuleAlgorithmInputData inputData;
	ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	private BusinessObjectInstance ruleInstance;

	@Override
	public void invoke() {
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		COTSInstanceNode group = formBoInstance.getGroupFromPath("informationSalaries");
		Iterator<COTSInstanceListNode> listSalaries = group.getList("informationSalariesList").iterator();
		this.initInfosSalaries(listSalaries);
	}

	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return inputOutputData;
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
	
	private String getFactorVal(String factor, String dateDebutCotisation, String dateFinCotisation ){
		PreparedStatement preparedStatement = createPreparedStatement("SELECT FACTOR_VAL FROM C1_FACTOR_VALUE where FACTOR_CD=:factor and TO_CHAR(EFFDT,'DD/MM/YYYY') <=:effectiveDate order by EFFDT DESC");
		preparedStatement.bindString("factor",factor, null);
		preparedStatement.bindString("effectiveDate", dateFinCotisation,null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		return sqlResultRow.getString("FACTOR_VAL");
	}
	
	private void initIdPerson(String idType, String idNumber){
		
		
	}
	
	private void initInfosSalaries(Iterator<COTSInstanceListNode> listSalaries){
		SchemaInstance formInstance = this.inputOutputData.getFormBusinessObject();
		String dateDebutCotisation=formInstance.getFieldAndMDForPath("informationEmployeur/dateDebutCotisation/asCurrent").getXMLValue();
		String dateFinCotisation=formInstance.getFieldAndMDForPath("informationEmployeur/dateFinCotisation/asCurrent").getXMLValue();
		String idType=formInstance.getFieldAndMDForPath("informationEmployeur/typeIdentifiant/asCurrent").getXMLValue();
		String idNumber=formInstance.getFieldAndMDForPath("informationEmployeur/idNumber/asCurrent").getXMLValue();
		String raisonSociale=formInstance.getFieldAndMDForPath("informationEmployeur/raisonSociale/asCurrent").getXMLValue();
		String address1=formInstance.getFieldAndMDForPath("informationEmployeur/adresse/asCurrent").getXMLValue();
		
		this.ruleInstance = this.getRuleDetails();
		String pflSmig = this.getFactorVal(this.ruleInstance.getString("pflSmig"), dateDebutCotisation, dateFinCotisation);
		String plfCssCpf = this.getFactorVal(this.ruleInstance.getString("plfCssCpf"), dateDebutCotisation, dateFinCotisation);
		String plfCssCatmp = this.getFactorVal(this.ruleInstance.getString("plfCssCatmp"), dateDebutCotisation, dateFinCotisation);
		String plfIpresCrrg = this.getFactorVal(this.ruleInstance.getString("plfIpresCrrg"), dateDebutCotisation, dateFinCotisation);
		String plfIpresCrrg01 = this.getFactorVal(this.ruleInstance.getString("plfIpresCrrg01"), dateDebutCotisation, dateFinCotisation);
		String plfIpresCrcc = this.getFactorVal(this.ruleInstance.getString("plfIpresCrcc"), dateDebutCotisation, dateFinCotisation);
		String plfIpresCrcc01 = this.getFactorVal(this.ruleInstance.getString("plfIpresCrcc01"), dateDebutCotisation, dateFinCotisation);
		String txeCssCpf = this.getFactorVal(this.ruleInstance.getString("txeCssCpf"), dateDebutCotisation, dateFinCotisation);
		String txeIpresCrrg = this.getFactorVal(this.ruleInstance.getString("txeIpresCrrg"), dateDebutCotisation, dateFinCotisation);
		String txeIpresCrcc = this.getFactorVal(this.ruleInstance.getString("txeIpresCrcc"), dateDebutCotisation, dateFinCotisation);
		String txeCssCatmp = this.getAtRateEmployer(idType, idNumber);
		String txsIpresCrrg = this.getFactorVal(this.ruleInstance.getString("txsIpresCrrg"), dateDebutCotisation, dateFinCotisation);
		String txsIpresCrcc = this.getFactorVal(this.ruleInstance.getString("txsIpresCrcc"), dateDebutCotisation, dateFinCotisation);

		 while (listSalaries.hasNext()) {
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			   if (nextSalarie != null) {
				   nextSalarie.getFieldAndMDForPath("smig/asCurrent").setXMLValue(pflSmig);
				   nextSalarie.getFieldAndMDForPath("plafondCssCpf/asCurrent").setXMLValue(plfCssCpf);
				   nextSalarie.getFieldAndMDForPath("plafondCssAtMp/asCurrent").setXMLValue(plfCssCatmp);
				   nextSalarie.getFieldAndMDForPath("plafondIpresCrrg/asCurrent").setXMLValue(plfIpresCrrg);
				   nextSalarie.getFieldAndMDForPath("plafondIpresCrcc/asCurrent").setXMLValue(plfIpresCrcc);
				   nextSalarie.getFieldAndMDForPath("tauxCssCpf/asCurrent").setXMLValue(txeCssCpf);
				   nextSalarie.getFieldAndMDForPath("tauxCssCatmp/asCurrent").setXMLValue(txeCssCatmp);
				   nextSalarie.getFieldAndMDForPath("tauxIpresCrrg/asCurrent").setXMLValue(txeIpresCrrg);
				   nextSalarie.getFieldAndMDForPath("tauxIpresCrcc/asCurrent").setXMLValue(txeIpresCrcc);
				   
				   }  
			   }
		 
		 //Chargement person Id dans le formulaire
		 String personId=getPersonId(idType, idNumber);
		 formInstance.getFieldAndMDForPath("taxpayerPersonID").setXMLValue(personId);
		 formInstance.getFieldAndMDForPath("primaryTaxpayerName").setXMLValue(raisonSociale);
		 formInstance.getFieldAndMDForPath("primaryTaxpayerIdType").setXMLValue(idType);
		 formInstance.getFieldAndMDForPath("primaryTaxpayerIdValue").setXMLValue(idNumber);
		 formInstance.getFieldAndMDForPath("address1").setXMLValue(address1);
		 
	}
	/**
	 * Permet de recuperer le taux AT de l'employeur
	 * @param idType type d'identifiant de l'employeur
	 * @param idNumber numero d'identifiant de l'employeur
	 * @return le taux AT de l'employeur
	 */
	private String getAtRateEmployer(String idType, String idNumber ){
		//Business Servivce pour recupérer la personne
	    BusinessServiceInstance businessServiceInstance=BusinessServiceInstance.create("C1-PersonSearchByIdTypeNumber");      
        businessServiceInstance.getFieldAndMDForPath("idType").setXMLValue(idType);
        businessServiceInstance.getFieldAndMDForPath("idNumber").setXMLValue(idNumber);
        businessServiceInstance=BusinessServiceDispatcher.execute(businessServiceInstance);
        String personId=businessServiceInstance.getFieldAndMDForPath("results[1]/personId").getXMLValue();
        
        //Invocation du BO pour récupérer la valeur du Taux AT
        BusinessObjectInstance businessObjectInstance=BusinessObjectInstance.create("CM-PersonIndividualChar");
        businessObjectInstance.getFieldAndMDForPath("personId").setXMLValue(personId);
        businessObjectInstance= BusinessObjectDispatcher.read(businessObjectInstance);
        
        //Il faut verifier si null
        String tauxAt=businessObjectInstance.getFieldAndMDForPath("personChar[charTypeCD='CM-ATRAT']/adhocCharVal").getXMLValue();
        return tauxAt;
	}
	

	private String getPersonId(String IdType, String idNumber){
				CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
				IdType_Id idType = new IdType_Id(IdType);
				Person person=perSearch.searchPerson(idType.getEntity(), idNumber);
				if (notNull(person))
					return person.getId().getTrimmedValue();
				return null;
	}


	/**
	 * Permet de recuperer une instance du BO du Form Rule
	 * @return BusinessObjectInstance du BO du Form Rule
	 */
	private BusinessObjectInstance getRuleDetails() {
		MaintenanceObjectInfo moInfo = MaintenanceObjectInfoCache
				.getMaintenanceObjectInfo(this.inputData.getFormRuleId().metaInfo().getTableId());
		BusinessObjectInfo boInfo = moInfo.determineBusinessObjectInfo(this.inputData.getFormRuleId());
		BusinessObjectInstance boInstance = BusinessObjectInstance.create(boInfo.getBusinessObject());
		boInstance.set("formRuleGroup", this.inputData.getFormRuleId().getFormRuleGroup().getId().getIdValue());
		boInstance.set("formRule", this.inputData.getFormRuleId().getFormRule());
		boInstance = BusinessObjectDispatcher.read(boInstance);
		return boInstance;
	}

}
