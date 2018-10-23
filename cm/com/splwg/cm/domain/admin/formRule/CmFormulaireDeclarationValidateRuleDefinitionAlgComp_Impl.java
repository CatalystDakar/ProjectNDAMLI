package com.splwg.cm.domain.admin.formRule;

import java.util.HashSet;
import java.util.Iterator;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.ValidateBusinessObjectAlgorithmSpot;
import com.splwg.base.domain.common.extendedLookupValue.ExtendedLookupValue_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * @author Catalyst-Demo
 *
@AlgorithmComponent ()
 */
public class CmFormulaireDeclarationValidateRuleDefinitionAlgComp_Impl extends
		CmFormulaireDeclarationValidateRuleDefinitionAlgComp_Gen implements ValidateBusinessObjectAlgorithmSpot {
	
	private static final Logger logger = LoggerFactory
			.getLogger(CmFormulaireDeclarationValidateRuleDefinitionAlgComp_Impl.class);
	
	private static String PERIODE_COTISATION_MENSUELLE = "MENSUEL";
	private static String PERIODE_COTISATION_TRIMESTRIELLE = "TRIMESTRIEL";
	private BusinessObjectActionLookup action;
	private EntityId entityId;
	private BusinessObject businessObject;
	private BusinessObjectInstanceKey businessObjectInstanceKey;
	private MaintenanceObject maintenanceObject;
	private BusinessObjectInstance newBOInstance;
	private BusinessObjectInstance originalBusinessObjectInstance;
	
	
	public void setAction(BusinessObjectActionLookup boAction) {
		this.action = boAction;
	}

	public void setBusinessObject(BusinessObject bo) {
		this.businessObject = bo;
	}

	public void setBusinessObjectKey(BusinessObjectInstanceKey boKey) {
		this.businessObjectInstanceKey = boKey;
	}

	public void setEntityId(EntityId id) {
		this.entityId = id;
	}

	public void setMaintenanceObject(MaintenanceObject mo) {
		this.maintenanceObject = mo;
	}

	public void setNewBusinessObject(BusinessObjectInstance boRequest) {
		this.newBOInstance = boRequest;
	}

	public void setOriginalBusinessObject(BusinessObjectInstance boRequest) {
		this.originalBusinessObjectInstance = boRequest;
	}
	
	
	
	@Override
	public void invoke() {
	if (!this.action.equals(BusinessObjectActionLookup.constants.DELETE)) {
			
			//BusinessObject businessObject = businessObjectInstanceKey.getBusinessObject();
			BusinessObjectInstance boInstance = BusinessObjectDispatcher.read(businessObjectInstanceKey, false);
			if (isNull(boInstance)) {
				logger.error("L'Instance du business object est null ", new Throwable());
			}
			
			COTSInstanceNode boGroupeInformationEmployeur = boInstance.getGroup("informationEmployeur");
			ExtendedLookupValue_Id idType = boGroupeInformationEmployeur.getGroup("typeIdentifiant").getExtendedLookupId("asCurrent");
			String idNumber = boGroupeInformationEmployeur.getGroup("idNumber").getString("asCurrent");
			if (isNull(idType)) {
				addError(CmMessageRepository90000.MSG_7000());
			}
			
			if(isBlankOrNull(idNumber)) addError(CmMessageRepository90000.MSG_7001());
			Person employeur = searchPerson(idType.getValue(), idNumber);
			if(isNull(employeur)) addError(CmMessageRepository90000.MSG_7003(idNumber));
			
			ExtendedLookupValue_Id typeDeclaration = boGroupeInformationEmployeur.getGroup("typeDeclaration").getExtendedLookupId("asCurrent");
			if(isNull(typeDeclaration)) addError(CmMessageRepository90000.MSG_7004());
			
			Date dateDebutCotisation = boGroupeInformationEmployeur.getGroup("dateDebutCotisation").getDate("asCurrent");
			Date dateFinCotisation = boGroupeInformationEmployeur.getGroup("dateFinCotisation").getDate("asCurrent");
			
			if(isNull(dateDebutCotisation)) 
				addError(CmMessageRepository90000.MSG_7005());
			
			if(isNull(dateFinCotisation)) 
				addError(CmMessageRepository90000.MSG_7006());
			
			if(!dateDebutCotisation.isFirstDayOfMonth())  
				addError(CmMessageRepository90000.MSG_7007(dateDebutCotisation));
			
			if(!dateFinCotisation.isLastDayOfMonth())  
				addError(CmMessageRepository90000.MSG_7008(dateFinCotisation));
		
			if(typeDeclaration.getValue().equalsIgnoreCase(PERIODE_COTISATION_MENSUELLE)) {
				
				if(dateDebutCotisation.getMonth() != dateFinCotisation.getMonth()) 
					addError(CmMessageRepository90000.MSG_7009(dateDebutCotisation, dateFinCotisation));
			
			} else if (typeDeclaration.getValue().equalsIgnoreCase(PERIODE_COTISATION_TRIMESTRIELLE)){
				if(!isPeriodeCotisationValid(dateDebutCotisation, dateFinCotisation)) 
					addError(CmMessageRepository90000.MSG_7010(dateDebutCotisation, dateFinCotisation));
			}
			// Validation des employes
			COTSInstanceNode boGroupeinformationSalaries = boInstance.getGroup("informationSalaries");
			COTSInstanceList informationSalariesList  = boGroupeinformationSalaries.getList("informationSalariesList");
			Iterator<COTSInstanceListNode> listeEmployesIterator = informationSalariesList.iterator();
		    while(listeEmployesIterator.hasNext()) {
		    	COTSInstanceListNode employeeNode = listeEmployesIterator.next();
		    	ExtendedLookupValue_Id typePieceIdentite = employeeNode.getGroup("typePieceIdentite").getExtendedLookupId("asCurrent");
		    	String numeroPieceIdentite = employeeNode.getGroup("numeroPieceIdentite").getString("asCurrent");
		    	//String nomEmployee = employeeNode.getGroup("nom").getString("asCurrent");
		    	//String prenomEmployee = employeeNode.getGroup("prenom").getString("asCurrent");
		    	if (!isValidIdentifiant(numeroPieceIdentite, typePieceIdentite.getValue()))
		    		addError(CmMessageRepository90000.MSG_7011(numeroPieceIdentite));
		    	validateDateEffectiveRegimeCadre(employeeNode, numeroPieceIdentite);
		    	validateMontantInfosEmployees(employeeNode, numeroPieceIdentite,  typeDeclaration, dateDebutCotisation);
		    	
		    }
			
		}

	}
	
	private void validateDateEffectiveRegimeCadre (COTSInstanceListNode employeeNode, String identifiant) {
		
		Bool regime1 = employeeNode.getGroup("regimCompCadre1").getBoolean("asCurrent");
		Date dateEffetRegime1 = employeeNode.getGroup("dateEffetRegime1").getDate("asCurrent");
		Bool regime2 = employeeNode.getGroup("regimCompCadre2").getBoolean("asCurrent");
		Date dateEffetRegime2 = employeeNode.getGroup("dateEffetRegime2").getDate("asCurrent");
		Bool regime3 = employeeNode.getGroup("regimCompCadre3").getBoolean("asCurrent");
		Date dateEffetRegime3 = employeeNode.getGroup("dateEffetRegime3").getDate("asCurrent");
		if (notNull(regime1) && regime1.isTrue() && isNull(dateEffetRegime1)) 
			addError(CmMessageRepository90000.MSG_7012(identifiant));
		else if (notNull(regime2) && regime2.isTrue() && isNull(dateEffetRegime2)) 			
			addError(CmMessageRepository90000.MSG_7013(identifiant));
		else if (notNull(regime3) && regime3.isTrue() && isNull(dateEffetRegime3))
			addError(CmMessageRepository90000.MSG_7014(identifiant));
	}
	
	
	private void validateMontantInfosEmployees (COTSInstanceListNode employeeNode, String identifiant,ExtendedLookupValue_Id typeDeclaration, Date dateDebutPeriode ) {		
		
		Bool regimeCadre1 = employeeNode.getGroup("regimCompCadre1").getBoolean("asCurrent");
		Bool regimeCadre2 = employeeNode.getGroup("regimCompCadre2").getBoolean("asCurrent");
		Bool regimeCadre3 = employeeNode.getGroup("regimCompCadre3").getBoolean("asCurrent");
		
		System.out.println("***************** TYPE DECLQRQTION = "+ typeDeclaration.getValue());
		
		if (typeDeclaration.getValue().equalsIgnoreCase(PERIODE_COTISATION_TRIMESTRIELLE)) {
			//Salaire assujetis mois 1
			if (isNull(employeeNode.getGroup("totSalAssCssPfe1").getMoney("asCurrent")))
				addError(CmMessageRepository90000.MSG_7016(identifiant));
			if (isNull(employeeNode.getGroup("totSalAssCssAtmpe1").getMoney("asCurrent")))
				addError(CmMessageRepository90000.MSG_7017(identifiant));
			if (isNull(employeeNode.getGroup("totSalAssIpresRge1").getMoney("asCurrent")))
				addError(CmMessageRepository90000.MSG_7018(identifiant));
			if (isNull(employeeNode.getGroup("totSalAssIpresRcce1").getMoney("asCurrent")) && notNull(regimeCadre1) && regimeCadre1.isTrue())
				addError(CmMessageRepository90000.MSG_7019(identifiant));
			
			//Salaire assujetis mois 2
			if (isNull(employeeNode.getGroup("totSalAssCssPfe2").getMoney("asCurrent")))
				addError(CmMessageRepository90000.MSG_7020(identifiant));
			if (isNull(employeeNode.getGroup("totSalAssCssAtmpe2").getMoney("asCurrent"))) 
				addError(CmMessageRepository90000.MSG_7021(identifiant));
			if (isNull(employeeNode.getGroup("totSalAssIpresRge2").getMoney("asCurrent"))) 
				addError(CmMessageRepository90000.MSG_7022(identifiant));
			if (isNull(employeeNode.getGroup("totSalAssIpresRcce2").getMoney("asCurrent")) && notNull(regimeCadre2) && regimeCadre2.isTrue())
				addError(CmMessageRepository90000.MSG_7023(identifiant));
			
			//Salaire assujetis mois 3
			if (isNull(employeeNode.getGroup("totSalAssCssPfe3").getMoney("asCurrent"))) 
				addError(CmMessageRepository90000.MSG_7024(identifiant));			
			if (isNull(employeeNode.getGroup("totSalAssCssAtmpe3").getMoney("asCurrent")))
				addError(CmMessageRepository90000.MSG_7025(identifiant));			
			if (isNull(employeeNode.getGroup("totSalAssIpresRge3").getMoney("asCurrent")))
				addError(CmMessageRepository90000.MSG_7026(identifiant));	
			if (isNull(employeeNode.getGroup("totSalAssIpresRcce3").getMoney("asCurrent")) && notNull(regimeCadre3) && regimeCadre3.isTrue())
				addError(CmMessageRepository90000.MSG_7027(identifiant));
			
		} else if (typeDeclaration.getValue().equalsIgnoreCase(PERIODE_COTISATION_MENSUELLE)) {
			//System.out.println("********************************** "+ employeeNode.getGroup("totSalAssCssPfe1").getMoney("asCurrent").getAmount());
			if (dateDebutPeriode.getMonth()== 1 || dateDebutPeriode.getMonth() == 4 || dateDebutPeriode.getMonth() == 7 || dateDebutPeriode.getMonth() == 10 ) {
				if (isNull(employeeNode.getGroup("totSalAssCssPfe1").getMoney("asCurrent")))
						addError(CmMessageRepository90000.MSG_7016(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssCssAtmpe1").getMoney("asCurrent")))
						addError(CmMessageRepository90000.MSG_7017(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssIpresRge1").getMoney("asCurrent")))
						addError(CmMessageRepository90000.MSG_7018(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssIpresRcce1").getMoney("asCurrent")) && notNull(regimeCadre1) && regimeCadre1.isTrue())
					    addError(CmMessageRepository90000.MSG_7019(identifiant));
				
				else if (notNull(employeeNode.getGroup("totSalAssCssPfe2").getMoney("asCurrent")))
							addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("totSalAssCssAtmpe2").getMoney("asCurrent")))
							addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("totSalAssIpresRge2").getMoney("asCurrent")))
							addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("totSalAssIpresRcce2").getMoney("asCurrent")))
						    addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("salaireBrut2").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("nombreJours2").getNumber("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("nombreHeures2").getNumber("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("tempsTravail2").getExtendedLookupId("asCurrent")))
				    addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("trancheTravail2").getString("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("regimeGeneral2").getBoolean("asCurrent")) && employeeNode.getGroup("regimeGeneral2").getBoolean("asCurrent").isTrue())
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("regimCompCadre2").getBoolean("asCurrent")) && employeeNode.getGroup("regimCompCadre2").getBoolean("asCurrent").isTrue())
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("dateEffetRegime2").getDate("asCurrent")))
				    addError(CmMessageRepository90000.MSG_7028());
				
				else if (notNull(employeeNode.getGroup("totSalAssCssPfe3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("totSalAssCssAtmpe3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("totSalAssIpresRge3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("totSalAssIpresRcce3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("salaireBrut3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("nombreJours3").getNumber("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("nombreHeures3").getNumber("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("tempsTravail3").getExtendedLookupId("asCurrent")))
				    addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("trancheTravail3").getString("asCurrent")))
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("regimeGeneral3").getBoolean("asCurrent")) && employeeNode.getGroup("regimeGeneral3").getBoolean("asCurrent").isTrue())
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("regimCompCadre3").getBoolean("asCurrent")) && employeeNode.getGroup("regimCompCadre3").getBoolean("asCurrent").isTrue())
					addError(CmMessageRepository90000.MSG_7028());
				else if (notNull(employeeNode.getGroup("dateEffetRegime3").getDate("asCurrent")))
				    addError(CmMessageRepository90000.MSG_7028());

						
			} else if (dateDebutPeriode.getMonth()== 2 || dateDebutPeriode.getMonth() == 5 || dateDebutPeriode.getMonth() == 8 || dateDebutPeriode.getMonth() == 11) {
				
				if (isNull(employeeNode.getGroup("totSalAssCssPfe2").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7029(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssCssAtmpe2").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7030(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssIpresRge2").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7031(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssIpresRcce2").getMoney("asCurrent")) && notNull(regimeCadre2) && regimeCadre2.isTrue())
				    addError(CmMessageRepository90000.MSG_7032(identifiant));
				
				else if (notNull(employeeNode.getGroup("totSalAssCssPfe3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("totSalAssCssAtmpe3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("totSalAssIpresRge3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("totSalAssIpresRcce3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("salaireBrut3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("nombreJours3").getNumber("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("nombreHeures3").getNumber("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("tempsTravail3").getExtendedLookupId("asCurrent")))
				    addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("trancheTravail3").getString("asCurrent")))
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("regimeGeneral3").getBoolean("asCurrent")) && employeeNode.getGroup("regimeGeneral3").getBoolean("asCurrent").isTrue())
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("regimCompCadre3").getBoolean("asCurrent")) && employeeNode.getGroup("regimCompCadre3").getBoolean("asCurrent").isTrue())
					addError(CmMessageRepository90000.MSG_7033());
				else if (notNull(employeeNode.getGroup("dateEffetRegime3").getDate("asCurrent")))
				    addError(CmMessageRepository90000.MSG_7033());
				
			} else if (dateDebutPeriode.getMonth()== 3 || dateDebutPeriode.getMonth() == 6 || dateDebutPeriode.getMonth() == 9 || dateDebutPeriode.getMonth() == 12) {
				
				if (isNull(employeeNode.getGroup("totSalAssCssPfe3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7029(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssCssAtmpe3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7030(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssIpresRge3").getMoney("asCurrent")))
					addError(CmMessageRepository90000.MSG_7031(identifiant));
				else if (isNull(employeeNode.getGroup("totSalAssIpresRcce3").getMoney("asCurrent"))&& notNull(regimeCadre3) && regimeCadre3.isTrue())
				    addError(CmMessageRepository90000.MSG_7032(identifiant));
			}
		}
	}
	
	
	private boolean isValidIdentifiant(String numero, String type) {
		if (type.equalsIgnoreCase("NIN"))
		    return numero.matches("[0-9]{13}") ||  numero.matches("[0-9]{14}");
		
		return true;
	}
	
	public boolean isPeriodeCotisationValid(Date datedebutCotisation, Date dateFinCotisation) {
		
		if(datedebutCotisation.getMonth() ==1 && dateFinCotisation.getMonth() == 3)
			return true;
		else if (datedebutCotisation.getMonth() ==4 && dateFinCotisation.getMonth() == 6)
			return true;
		else if (datedebutCotisation.getMonth() ==7 && dateFinCotisation.getMonth() == 9)
			return true;
		else if (datedebutCotisation.getMonth() ==10 && dateFinCotisation.getMonth() == 12)
			return true;
		
		return false;
		
	}
	
	public Person searchPerson(String idType, String idNumber) {
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-PersonSearchByIdTypeNumber");
		// Populate BS parameters if available
		if (!isNull(idType))
			bsInstance.set("idType", idType);
		if (!isBlankOrNull(idNumber))
			bsInstance.set("idNumber", idNumber);
		// Execute BS and return the person Id if exists
		return executeBSAndRetrievePerson(bsInstance, idType, idNumber);
	}
	
	private Person executeBSAndRetrievePerson(BusinessServiceInstance bsInstance, String idType, String idNumber) {
		// Executing BS
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		//logger.info(bsInstance.getDocument().asXML());
		// Getting the list of results
		COTSInstanceList list = bsInstance.getList("results");
		// If list IS NOT empty
		if (!list.isEmpty()) {			
			if(list.getSize()>1) addError(CmMessageRepository90000.MSG_7002(idType, idNumber));
			// Get the first result
			COTSInstanceListNode firstRow = list.iterator().next();
			// Return the person entity
			return new Person_Id(firstRow.getString("personId")).getEntity(); 
		}
		
		return null;
	}
	




}
