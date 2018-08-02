package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * @author Catalyst-Demo
 *
@AlgorithmComponent ()
 */
public class CmCheckEmployeesExistence_Impl extends CmCheckEmployeesExistence_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {
	
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	//private BusinessObjectInstance ruleBoInstance;
	Logger logger = LoggerFactory.getLogger(CmCheckEmployeesExistence_Impl.class);
	
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData) {
		this.inputData = applyFormRuleAlgorithmInputData;
	}

	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData) {
		this.inputOutputData = applyFormRuleAlgorithmInputOutputData;
	}

	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return this.inputOutputData;
	}

	@Override
	public void invoke() {
		BusinessObjectInstance formBusinessObjectInstance = (BusinessObjectInstance) this.inputOutputData
				.getFormBusinessObject();
		String personId=formBusinessObjectInstance.getString("taxpayerPersonID");
		String idFormulaire = formBusinessObjectInstance.getFieldAndMDForPath("taxFormId").getXMLValue();		
		System.out.println("*******taxFormId*******"+idFormulaire);
		System.out.println("*******taxpayerPersonID*******"+personId);
		//liste des salariés rattachés à l'employeur
		List<String> lisSalarieds=this.getSalariedsEmployer(personId);
		
		COTSInstanceNode groupInsInfosSalariedNode=formBusinessObjectInstance.getGroup("informationSalaries");
		
		COTSInstanceList cOTSInstanceListSalarieds=groupInsInfosSalariedNode.getList("informationSalariesList");
		Iterator<COTSInstanceListNode> listInsNodeIterator=  cOTSInstanceListSalarieds.iterator();
		
		COTSInstanceNode groupInsInfosEmployerNode=formBusinessObjectInstance.getGroup("informationEmployeur");
		COTSInstanceNode groupInsInfosRaisonSocialNode=groupInsInfosEmployerNode.getGroup("raisonSociale");
		String raisonSocial=groupInsInfosRaisonSocialNode.getString("asCurrent");

		
		while(listInsNodeIterator.hasNext()){
			COTSInstanceListNode cOTSInstanceListSalariedNode=listInsNodeIterator.next();
			COTSInstanceNode cotsInsIdTypeGroup=cOTSInstanceListSalariedNode.getGroup("typePieceIdentite");
			String typeIdentifiant=cotsInsIdTypeGroup.getFieldAndMD("asCurrent").getXMLValue();
			COTSInstanceNode cotsInsIdValueGroup=cOTSInstanceListSalariedNode.getGroup("numeroPieceIdentite");
			String numeroIdentifiant=cotsInsIdValueGroup.getFieldAndMD("asCurrent").getXMLValue();
			COTSInstanceNode cotsInsPrenomGroup=cOTSInstanceListSalariedNode.getGroup("prenom");
			String prenom=cotsInsPrenomGroup.getFieldAndMD("asCurrent").getXMLValue();
			COTSInstanceNode cotsInsNomGroup=cOTSInstanceListSalariedNode.getGroup("nom");
			String nom=cotsInsNomGroup.getFieldAndMD("asCurrent").getXMLValue();
			
			Person salaried=this.getPersonByIdNumber(typeIdentifiant, numeroIdentifiant);
			if(isNull(salaried) || !lisSalarieds.contains(salaried.getId().getTrimmedValue()) ){
				this.createToDoSalariedExistence(prenom, nom, raisonSocial, typeIdentifiant, salaried,idFormulaire);
			}
		}
		
	}

	
	/**
	 * Creation To Do si le salarie n'existe pas ou s'il n'est pas rattache l'employeur
	 * @param prenom salarié
	 * @param nom salarié
	 * @param raisonSocial employeur
	 * @param typeIdentifiant type d'identifiant telque NINEA
	 * @param valeurIdentifiant valeur de l'identifiant
	 */
	private void createToDoSalariedExistence(String prenom, String nom, String raisonSocial,String typeIdentifiant, Person employee,String taxFormId ){
		BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
		Role_Id toDoRoleId = new Role_Id("CMRDNS");
		Role toDoRole = toDoRoleId.getEntity();
		businessServiceInstance.set("toDoType", "CMDNS");
		businessServiceInstance.set("toDoRole", toDoRole.getId().getTrimmedValue());
		businessServiceInstance.set("drillKey1", taxFormId);
		System.out.println("******************************ID FORM"+taxFormId);
		//Person person=getPersonByIdNumber(typeIdentifiant, valeurIdentifiant);
		if(isNull(employee)){
			businessServiceInstance.set("messageCategory", BigDecimal.valueOf(90000));
			businessServiceInstance.set("messageNumber", BigDecimal.valueOf(10002));
			businessServiceInstance.set("messageParm1", prenom);
			businessServiceInstance.set("messageParm2", nom);
			businessServiceInstance.set("messageParm3", raisonSocial);
		}else{
			businessServiceInstance.set("messageCategory", BigDecimal.valueOf(90000));
			businessServiceInstance.set("messageNumber", BigDecimal.valueOf(10003));
			businessServiceInstance.set("messageParm1", prenom);
			businessServiceInstance.set("messageParm2", nom);
			businessServiceInstance.set("messageParm3", raisonSocial);
     	}
		BusinessServiceDispatcher.execute(businessServiceInstance);
	}
	
	/**
	 * Recupere la liste des identifiants des employés de l'employeur
	 * @param personId identifiant system de l'employeur
	 * @return liste des identifiant des salariés
	 */
	private List<String> getSalariedsEmployer(String personId){
		List<String> listIdSalaried = new ArrayList<String>();
		BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("C1-GETRELPER");
		businessServiceInstance.set("person", personId);
		businessServiceInstance.set("relationshipType", "EMPL-EMP");
		businessServiceInstance=BusinessServiceDispatcher.execute(businessServiceInstance);
		Iterator<COTSInstanceListNode> iterator = businessServiceInstance.getList("results").iterator();
		
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			String perId=nextElt.getString("relatedPerson");
			listIdSalaried.add(perId);
		}
		return listIdSalaried;
	}
	
	
	/**
	 * Recupére une person en fonction de son identifant
	 * @param IdType
	 * @param idNumber
	 * @return person
	 */
	private Person getPersonByIdNumber(String IdType, String idNumber) {
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		return perSearch.searchPerson(idType.getEntity(), idNumber);  
	}

}
