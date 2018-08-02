package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.extendedLookupValue.ExtendedLookupValue;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.cm.domain.customMessages.CmMessageRepository1001;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.DeliverableLookup;
import com.splwg.tax.api.lookup.NameTypeLookup;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.admin.personRelationshipType.PersonRelationshipType_Id;
import com.splwg.tax.domain.admin.personType.PersonType;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.PersonPerson;
import com.splwg.tax.domain.customerinfo.person.PersonPerson_DTO;
import com.splwg.tax.domain.customerinfo.person.PersonPerson_Id;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * @author Balaganesh M
 *
@AlgorithmComponent ()
 */
public class CmPrenatalAllowance_Impl extends CmPrenatalAllowance_Gen implements FormRuleBORuleProcessingAlgorithmSpot {

	Logger logger = LoggerFactory.getLogger(CmPrenatalAllowance_Impl.class);

	private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
	private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;

	/**
	 * @param personId1
	 * @param personId2
	 * @param date
	 * 
	 */
	private void createRelationSpouse_Child(String personId1, String personId2, Date date, String typeRel) {
		Person_Id person1 = new Person_Id(personId1);
		Person_Id person2 = new Person_Id(personId2);
		PersonRelationshipType_Id perRel = new PersonRelationshipType_Id(typeRel);
		PersonPerson_DTO perperDTO = (PersonPerson_DTO) createDTO(PersonPerson.class);
		PersonPerson_Id person = new PersonPerson_Id(perRel, person1, person2, date);
		perperDTO.setId(person);
		PersonPerson perperReg = perperDTO.newEntity();
	}
	
	private Person getPersonById(String personId) {
		// log.info("*****Starting getpersonId");
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		return perSearch.searchPerson(personId);
	}

	private Person getPersonByNin(String IdType, String idNumber) {
		// log.info("*****Starting getpersonId");
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		// log.info("*****ID Type: " + idType.getTrimmedValue());
		return perSearch.searchPerson(idType.getEntity(), idNumber);
	}
	

	/**
	 * @param personId
	 */
	private boolean verifyRelationship(String personId,String personId2, String relationType) {

		String query = "select * from CI_PER_PER where PER_ID1 = \'" + personId + "\' and PER_ID2 = \'" + personId2
				+ "\'  and PER_REL_TYPE_CD = \'" + relationType + "\'";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		QueryIterator<SQLResultRow> perResultIterator = preparedStatement.iterate();
		return perResultIterator.hasNext();
	}
	
	/**
	 * @param personId
	 */
	private String getAddresIDFrmPersonId(String personId) {

		String query = "select ADDRESS_ID from CI_PER_ADDR where PER_ID = \'" + personId + "\'";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		QueryIterator<SQLResultRow> perResultIterator = preparedStatement.iterate();
		String addressID = null;
		while(perResultIterator.hasNext()){
			SQLResultRow sqlResultRow = perResultIterator.next();
			addressID = sqlResultRow.getString("ADDRESS_ID");
		}
		return addressID;
	}
	

	private void createToDo(String messageNumber, String oldEmp, String employe, String newEmp, String paramName) {
		startChanges();
		BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
		Role_Id toDoRoleId = new Role_Id("CM-DMTTODO");
		Role toDoRole = toDoRoleId.getEntity();
		// businessServiceInstance.getFieldAndMDForPath("sendTo").setXMLValue("SNDR");
		// businessServiceInstance.getFieldAndMDForPath("subject").setXMLValue("Batch
		// Update from PSRM");
		businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue("CM-DMTTO");
		businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
		businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue(paramName);
		businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue("90007");
		businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue(messageNumber);
		businessServiceInstance.getFieldAndMDForPath("messageParm1").setXMLValue(oldEmp);
		businessServiceInstance.getFieldAndMDForPath("messageParm2").setXMLValue(employe);
		businessServiceInstance.getFieldAndMDForPath("messageParm3").setXMLValue(newEmp);
		businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue(paramName);

		BusinessServiceDispatcher.execute(businessServiceInstance);
		saveChanges();
		// getSession().commit();
	}
	
	private void createAccntPersonForSpouse(Iterator<COTSInstanceListNode> conjointIterator,PersonType personType,ExtendedLookupValue addressType,
			Map<String,List<String>> spouseNinChildIdMap,Map<String,Date> enfantPersonBirthDateIdMap){
		
		while(conjointIterator.hasNext()){
			
			BusinessObjectInstance personConjointBoInstance = null;
			BusinessObjectInstance accountConjointBoInstance = null;
			BusinessObjectInstance addresConjointBoInstance = null;
			COTSInstanceListNode addressConjointCharInstance = null;
			COTSInstanceListNode personConjointAddressInstance = null;
			COTSInstanceListNode personConjointNameInstance = null;
			COTSInstanceListNode personConjointIdInstance = null;
			COTSInstanceListNode accountConjointIdInstance = null;
			
			COTSInstanceListNode nextConjointElement = conjointIterator.next();
			
			if (nextConjointElement != null) {
				String filiationConjoint = (String) nextConjointElement.getFieldAndMDForPath("filiation/asCurrent").getValue();
				logger.info("filiationConjoint: " + filiationConjoint);
				String typeDeRelation = (String) nextConjointElement.getFieldAndMDForPath("typeDeRelation/asCurrent").getValue();
				logger.info("typeDeRelation: " + typeDeRelation);
				String leRang = (String) nextConjointElement.getFieldAndMDForPath("leRang/asCurrent").getValue();
				logger.info("leRang: " + leRang);
				Date dateDeDebutDuMariage = (Date) nextConjointElement.getFieldAndMDForPath("dateDeDebutDuMariage/asCurrent").getValue();
				logger.info("dateDeDebutDuMariage: " + dateDeDebutDuMariage);
				Date dateDeFinDuMariage = (Date) nextConjointElement.getFieldAndMDForPath("dateDeFinDuMariage/asCurrent").getValue();
				logger.info("dateDeFinDuMariage: " + dateDeFinDuMariage);
				String lieuDuMariage = (String) nextConjointElement.getFieldAndMDForPath("lieuDuMariage/asCurrent").getValue();
				logger.info("lieuDuMariage: " + lieuDuMariage);
				String numDuCertificateDeMariage = (String) nextConjointElement.getFieldAndMDForPath("numDuCertificateDeMariage/asCurrent").getValue();
				logger.info("numDuCertificateDeMariage: " + numDuCertificateDeMariage);
				String prenomDuConjoint = (String) nextConjointElement.getFieldAndMDForPath("prenomDuConjoint/asCurrent").getValue();
				logger.info("prenomDuConjoint: " + prenomDuConjoint);
				String nomDuConjoint = (String) nextConjointElement.getFieldAndMDForPath("nomDuConjoint/asCurrent").getValue();
				logger.info("nomDuConjoint: " + nomDuConjoint);
				Date dateDeNaissanceDuMarie = (Date) nextConjointElement.getFieldAndMDForPath("dateDeNaissanceDuMarie/asCurrent").getValue();
				logger.info("dateDeNaissanceDuMarie: " + dateDeNaissanceDuMarie);
				String prenomDuPereDuConjoint = (String) nextConjointElement.getFieldAndMDForPath("prenomDuPereDuConjoint/asCurrent").getValue();
				logger.info("prenomDuPereDuConjoint: " + prenomDuPereDuConjoint);
				String nomDuPereDuConjoint = (String) nextConjointElement.getFieldAndMDForPath("nomDuPereDuConjoint/asCurrent").getValue();
				logger.info("nomDuPereDuConjoint: " + nomDuPereDuConjoint);
				String prenomDeLaMereDuConjoint = (String) nextConjointElement.getFieldAndMDForPath("prenomDeLaMereDuConjoint/asCurrent").getValue();
				logger.info("prenomDeLaMereDuConjoint: " + prenomDeLaMereDuConjoint);
				String nomDeLaMereDuConjoint = (String) nextConjointElement.getFieldAndMDForPath("nomDeLaMereDuConjoint/asCurrent").getValue();
				logger.info("nomDeLaMereDuConjoint: " + nomDeLaMereDuConjoint);
				BigDecimal ninDuConjoint = (BigDecimal) nextConjointElement.getFieldAndMDForPath("ninDuConjoint/asCurrent").getValue();
				logger.info("ninDuConjoint: " + ninDuConjoint);
				String ninDuConjointStr = ninDuConjoint.toString();
				Date delivreLe = (Date) nextConjointElement.getFieldAndMDForPath("delivreLe/asCurrent").getValue();
				logger.info("delivreLe: " + delivreLe);
				String conjointAdresse = (String) nextConjointElement.getFieldAndMDForPath("adresse/asCurrent").getValue();
				logger.info("conjointAdresse: " + conjointAdresse);
				
				String spousePersonId = isNull(getPersonByNin("NIN", ninDuConjointStr)) ? null : getPersonByNin("NIN", ninDuConjointStr).getId().getIdValue();
				
				if(isNullOrEmpty(spousePersonId)){
					//Create Conjoint address
					addresConjointBoInstance = BusinessObjectInstance.create("CM-PrenatalAddressCharBO");
					logger.info("Creating addresBoInstance create: " + addresConjointBoInstance.getDocument().asXML());
					addresConjointBoInstance.set("address1", conjointAdresse);
					//addresConjointBoInstance.set("status", "C1AC");
					addresConjointBoInstance.set("bo", addresConjointBoInstance.getBusinessObject());
					
					addresConjointBoInstance = BusinessObjectDispatcher.add(addresConjointBoInstance);
					logger.info("conjointAdresseId: " + addresConjointBoInstance.getString("addressId"));
					//create Conjoint person Bo Instance
					personConjointBoInstance =  BusinessObjectInstance.create("CM-PersonIndividualChar");
					personConjointBoInstance.set("personType", personType);
					//personConjointBoInstance.set("language", "ENG");
					//Conjoint Person Name
					personConjointNameInstance = personConjointBoInstance.getList("personName").newChild();
					personConjointNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
					personConjointNameInstance.set("firstName", prenomDuConjoint);
					personConjointNameInstance.set("lastName", nomDuConjoint);
					personConjointNameInstance.set("isPrimaryName", Bool.TRUE);
					//personConjoint
					personConjointAddressInstance =  personConjointBoInstance.getList("personAddress").newChild();
					personConjointAddressInstance.set("addressId",addresConjointBoInstance.getString("addressId"));
					personConjointAddressInstance.set("addressType",addressType.getId());
					personConjointAddressInstance.set("startDate",getSystemDateTime().getDate());
					//Person Id conjoint
					personConjointIdInstance =   personConjointBoInstance.getList("personIds").newChild();
					personConjointIdInstance.set("idType","NIN");
					personConjointIdInstance.set("personIdNumber",ninDuConjointStr);
					personConjointIdInstance.set("isPrimaryId",Bool.TRUE);
					//person Char
					if(!isNull(dateDeNaissanceDuMarie)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						String dateDeNaissanceDuMarieStr = dateDeNaissanceDuMarie.toString();
						addressConjointCharInstance.set("charTypeCD","CM-DOB");
						addressConjointCharInstance.set("adhocCharVal",dateDeNaissanceDuMarieStr);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(prenomDuPereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","PRE_PERE");
						addressConjointCharInstance.set("adhocCharVal",prenomDuPereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(nomDuPereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","NOM_PERE");
						addressConjointCharInstance.set("adhocCharVal",nomDuPereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(prenomDeLaMereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","PRE_MERE");
						addressConjointCharInstance.set("adhocCharVal",prenomDeLaMereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(nomDeLaMereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","NOM_MERE");
						addressConjointCharInstance.set("adhocCharVal",nomDeLaMereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNull(delivreLe)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						String delivreLeStr = delivreLe.toString();
						addressConjointCharInstance.set("charTypeCD","CM-ISSDT");
						addressConjointCharInstance.set("adhocCharVal",delivreLeStr);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					
					personConjointBoInstance = BusinessObjectDispatcher.add(personConjointBoInstance);
					logger.info("Spouse Person Id created:"+personConjointBoInstance.getString("personId"));
					
					//Account Creation for Spouse
					accountConjointBoInstance = BusinessObjectInstance.create("CM-PreBenifitsAccount");
					accountConjointBoInstance.set("setupDate", getSystemDateTime().getDate());
					accountConjointBoInstance.set("currency", "XOF");
					accountConjointBoInstance.set("accountType", "PF");
					
					accountConjointIdInstance = accountConjointBoInstance.getList("accountPersonList").newChild();
					accountConjointIdInstance.set("person", personConjointBoInstance.getString("personId"));
					accountConjointIdInstance.set("accountRelationship", "SPOUSE");
					accountConjointIdInstance.set("mainCustomer", Bool.TRUE);
					accountConjointIdInstance.set("isFinanciallyResponsible", Bool.TRUE);
					
					accountConjointBoInstance = BusinessObjectDispatcher.add(accountConjointBoInstance);
					logger.info("Spouse Account Id created"+accountConjointBoInstance.getString("accountId"));
					
					// linking Spouse and Child
					if(!spouseNinChildIdMap.isEmpty() && !enfantPersonBirthDateIdMap.isEmpty()){
						String conjointPersonId = personConjointBoInstance.getString("personId");
						logger.info("Inside link spouse and child ninDuConjointStr::"+ninDuConjointStr);
						logger.info("Inside link spouse and child spouseNinChildIdMap::"+spouseNinChildIdMap);
						logger.info("Inside link spouse and child enfantPersonBirthDateIdMap::"+enfantPersonBirthDateIdMap);
						
						List<String> childPerIdList = spouseNinChildIdMap.get(ninDuConjointStr);
						logger.info("childPerIdList::"+childPerIdList);
						if(!isNull(childPerIdList)){
							for(String childPerId : childPerIdList){
								Date birthDate = enfantPersonBirthDateIdMap.get(childPerId);
								logger.info("conjointPersonId::"+conjointPersonId);
								logger.info("childPerId::"+childPerId);
								logger.info("birthDate::"+birthDate);
								if(!verifyRelationship(conjointPersonId,childPerId,"PAR-ENF")){
									createRelationSpouse_Child(conjointPersonId, childPerId, birthDate ,"PAR-ENF");
								}
								if(!verifyRelationship(childPerId,conjointPersonId,"ENF-PAR")){
									createRelationSpouse_Child(childPerId, conjointPersonId, birthDate ,"ENF-PAR");
								}
							}
						}
					}
				} else {
					logger.info("####Spouse Update Details####");
					//Create Conjoint address
					addresConjointBoInstance = BusinessObjectInstance.create("CM-PrenatalAddressCharBO");
					logger.info("Creating addresBoInstance create:Update " + addresConjointBoInstance.getDocument().asXML());
					addresConjointBoInstance.set("address1", conjointAdresse);
					//addresConjointBoInstance.set("status", "C1AC");
					addresConjointBoInstance.set("bo", addresConjointBoInstance.getBusinessObject());
					String addressIdUpdate = getAddresIDFrmPersonId(spousePersonId);
					if(isNullOrEmpty(addressIdUpdate)){
						addError(CmMessageRepository1001.MSG_103());
					}
					addresConjointBoInstance.set("addressId", addressIdUpdate);
					addresConjointBoInstance = BusinessObjectDispatcher.update(addresConjointBoInstance);
					logger.info("conjointAdresseId: Update" + addresConjointBoInstance.getString("addressId"));
					//create Conjoint person Bo Instance
					personConjointBoInstance =  BusinessObjectInstance.create("CM-PersonIndividualChar");
					personConjointBoInstance.set("personType", personType);
					//personConjointBoInstance.set("language", "ENG");
					//Conjoint Person Name
					personConjointNameInstance = personConjointBoInstance.getList("personName").newChild();
					personConjointNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
					personConjointNameInstance.set("firstName", prenomDuConjoint);
					personConjointNameInstance.set("lastName", nomDuConjoint);
					personConjointNameInstance.set("isPrimaryName", Bool.TRUE);
					//personConjoint
					personConjointAddressInstance =  personConjointBoInstance.getList("personAddress").newChild();
					personConjointAddressInstance.set("addressId",addresConjointBoInstance.getString("addressId"));
					personConjointAddressInstance.set("addressType",addressType.getId());
					personConjointAddressInstance.set("startDate",getSystemDateTime().getDate());
					//Person Id conjoint
					personConjointIdInstance =   personConjointBoInstance.getList("personIds").newChild();
					personConjointIdInstance.set("idType","NIN");
					personConjointIdInstance.set("personIdNumber",ninDuConjointStr);
					personConjointIdInstance.set("isPrimaryId",Bool.TRUE);
					//person Char
					if(!isNull(dateDeNaissanceDuMarie)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						String dateDeNaissanceDuMarieStr = dateDeNaissanceDuMarie.toString();
						addressConjointCharInstance.set("charTypeCD","CM-DOB");
						addressConjointCharInstance.set("adhocCharVal",dateDeNaissanceDuMarieStr);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(prenomDuPereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","PRE_PERE");
						addressConjointCharInstance.set("adhocCharVal",prenomDuPereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(nomDuPereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","NOM_PERE");
						addressConjointCharInstance.set("adhocCharVal",nomDuPereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(prenomDeLaMereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","PRE_MERE");
						addressConjointCharInstance.set("adhocCharVal",prenomDeLaMereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNullOrEmpty(nomDeLaMereDuConjoint)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						addressConjointCharInstance.set("charTypeCD","NOM_MERE");
						addressConjointCharInstance.set("adhocCharVal",nomDeLaMereDuConjoint);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					if(!isNull(delivreLe)){
						addressConjointCharInstance = personConjointBoInstance.getList("personChar").newChild();
						String delivreLeStr = delivreLe.toString();
						addressConjointCharInstance.set("charTypeCD","CM-ISSDT");
						addressConjointCharInstance.set("adhocCharVal",delivreLeStr);
						addressConjointCharInstance.set("effectiveDate",getSystemDateTime().getDate());
					}
					personConjointBoInstance.set("personId", spousePersonId);
					personConjointBoInstance = BusinessObjectDispatcher.update(personConjointBoInstance);
					logger.info("Spouse Person Id created: Update"+personConjointBoInstance.getString("personId"));
					
					//Account Creation for Spouse
					accountConjointBoInstance = BusinessObjectInstance.create("CM-PreBenifitsAccount");
					accountConjointBoInstance.set("setupDate", getSystemDateTime().getDate());
					accountConjointBoInstance.set("currency", "XOF");
					accountConjointBoInstance.set("accountType", "PF");
					
					accountConjointIdInstance = accountConjointBoInstance.getList("accountPersonList").newChild();
					accountConjointIdInstance.set("person", personConjointBoInstance.getString("personId"));
					accountConjointIdInstance.set("accountRelationship", "SPOUSE");
					accountConjointIdInstance.set("mainCustomer", Bool.TRUE);
					accountConjointIdInstance.set("isFinanciallyResponsible", Bool.TRUE);
					
					accountConjointBoInstance = BusinessObjectDispatcher.update(accountConjointBoInstance);
					logger.info("Spouse Account Id created:Update"+accountConjointBoInstance.getString("accountId"));
					
					// linking Spouse and Child
					if(!spouseNinChildIdMap.isEmpty() && !enfantPersonBirthDateIdMap.isEmpty()){
						String conjointPersonId = personConjointBoInstance.getString("personId");
						logger.info("Inside link spouse and child ninDuConjointStr::Update"+ninDuConjointStr);
						logger.info("Inside link spouse and child spouseNinChildIdMap::Update"+spouseNinChildIdMap);
						logger.info("Inside link spouse and child enfantPersonBirthDateIdMap::Update"+enfantPersonBirthDateIdMap);
						
						List<String> childPerIdList = spouseNinChildIdMap.get(ninDuConjointStr);
						logger.info("childPerIdList::Update"+childPerIdList);
						if(!isNull(childPerIdList)){
							for(String childPerId : childPerIdList){
								Date birthDate = enfantPersonBirthDateIdMap.get(childPerId);
								logger.info("conjointPersonId::Update"+conjointPersonId);
								logger.info("childPerId::Update"+childPerId);
								logger.info("birthDate::Update"+birthDate);
								if(!verifyRelationship(conjointPersonId,childPerId,"PAR-ENF")){
									createRelationSpouse_Child(conjointPersonId, childPerId, birthDate ,"PAR-ENF");
								}
								if(!verifyRelationship(childPerId,conjointPersonId,"ENF-PAR")){
									createRelationSpouse_Child(childPerId, conjointPersonId, birthDate ,"ENF-PAR");
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void invoke() {
		// Form Data BO Instance
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData
				.getFormBusinessObject();
		logger.info("Input Form BO: " + formBoInstance.getDocument().asXML());
		logger.info("formBoInstance: " + formBoInstance.getSchemaName());
		// Form Rule
		FormRule formRule = applyFormRuleAlgorithmInputData.getFormRuleId().getEntity();
		//Reading Form Rule Information
		BusinessObjectInstance formRuleBoInstance = BusinessObjectInstance.create(formRule.getBusinessObject());
		logger.info("formRuleBoInstance: " + formRuleBoInstance.getSchemaName());
		logger.info("formRuleBoInstance: " + formRuleBoInstance.getDocument().asXML());
		formRuleBoInstance.set("bo", formRule.getBusinessObject().getId().getTrimmedValue());
		formRuleBoInstance.set("formRuleGroup", formRule.getId().getFormRuleGroup().getId().getTrimmedValue());
		formRuleBoInstance.set("formRule", formRule.getId().getFormRule());
		formRuleBoInstance.set("sequence", BigDecimal.valueOf(formRule.getSequence().longValue()));
		formRuleBoInstance = BusinessObjectDispatcher.read(formRuleBoInstance);
		Map<String,List<String>> spouseNinChildIdMap = new HashMap<String,List<String>>();
		Map<String,Date> enfantPersonBirthDateIdMap = new HashMap<String,Date>();
		List<String> childPersonList = null;
		COTSInstanceNode conjointGroup = formBoInstance.getGroupFromPath("identiteDuConjoint");
		Iterator<COTSInstanceListNode> conjointIterator = conjointGroup.getList("identiteDuConjointList").iterator();
		COTSInstanceNode group = formBoInstance.getGroupFromPath("identiteDesEnfants");
		Iterator<COTSInstanceListNode> iterator = group.getList("identiteDesEnfantsList").iterator();
		
		// Form Rule Details Group
		COTSInstanceNode ruleDetails = formRuleBoInstance.getGroup("ruleDetails");
		logger.info("formRuleBoInstance Rule Details: " + ruleDetails);
		PersonType personType = ruleDetails.getEntity("personType", PersonType.class);
		logger.info("PersonType: " + personType);
		BusinessObject_Id relatedTransactionBOId = personType.getRelatedTransactionBOId();
		logger.info("relatedTransactionBOId: " + relatedTransactionBOId);
		ExtendedLookupValue addressType = ruleDetails.getExtendedLookupId("addressType")
				.getEntity();
		logger.info("addressType " + addressType);
		
		if (notNull(relatedTransactionBOId)) {
			
			if(group.getList("identiteDesEnfantsList").getSize()>0){
				while (iterator.hasNext()) {
					logger.info("inside Child iterator: ");
					COTSInstanceListNode nextElt = iterator.next();
					BusinessObjectInstance personChildBoInstance = null;
					BusinessObjectInstance addresChildBoInstance = null;
					COTSInstanceListNode addressChildCharInstance = null;
					COTSInstanceListNode personChildAddressInstance = null;
					COTSInstanceListNode personChildNameInstance = null;
					COTSInstanceListNode personChildIdInstance = null;
					
					if (nextElt != null) {
						String filiationChild = (String) nextElt.getFieldAndMDForPath("filiation/asCurrent").getValue();
						logger.info("filiationChild: " + filiationChild);
						BigDecimal ninConjointChild = (BigDecimal) nextElt.getFieldAndMDForPath("ninDuConjoint/asCurrent").getValue();
						logger.info("ninConjointChild: " + ninConjointChild);
						String ninConjointStr = ninConjointChild.toString();
						BigDecimal ninDeEnfant = (BigDecimal) nextElt.getFieldAndMDForPath("ninDeEnfant/asCurrent").getValue();
						logger.info("ninDeEnfant: " + ninDeEnfant);
						String ninDeEnfantStr = ninDeEnfant.toString();
						String prenomEnfant = (String) nextElt.getFieldAndMDForPath("prenomEnfant/asCurrent").getValue();
						logger.info("prenomEnfant: " + prenomEnfant);
						Date dateDeNaissance = (Date) nextElt.getFieldAndMDForPath("dateDeNaissanceEnfant/asCurrent").getValue();
						logger.info("dateDeNaissance: " + dateDeNaissance);
						String villeDeNaissance = (String) nextElt.getFieldAndMDForPath("villeDeNaissanceEnfant/asCurrent").getValue();
						logger.info("villeDeNaissance: " + villeDeNaissance);
						
						String childPersonId = isNull(getPersonByNin("NIN", ninDeEnfantStr)) ? null : getPersonByNin("NIN", ninDeEnfantStr).getId().getIdValue();
						logger.info("childPersonId: " + childPersonId);
						
						if(isNullOrEmpty(childPersonId)){
							addresChildBoInstance = BusinessObjectInstance.create("CM-PrenatalAddressCharBO");
							logger.info("Creating addresBoInstance create: " + addresChildBoInstance.getDocument().asXML());
							COTSInstanceNode groupConjoint = formBoInstance.getGroupFromPath("identiteDuConjoint");
							Iterator<COTSInstanceListNode> iteratorConjoint = groupConjoint.getList("identiteDuConjointList").iterator();
							logger.info("Creating addresBoInstance create: " + addresChildBoInstance.getDocument().asXML());
							boolean checkConjointFlag = false;
							while(iteratorConjoint.hasNext()){
								logger.info("inside conjoint iterator: ");
								COTSInstanceListNode nextConjointElt = iteratorConjoint.next();
								if (nextConjointElt != null) {
									BigDecimal ninDuConjoint = (BigDecimal) nextConjointElt.getFieldAndMDForPath("ninDuConjoint/asCurrent").getValue();
									logger.info("ninDuConjoint: " + ninDuConjoint);
									String ninDuConjointStr = ninDuConjoint.toString();
									
									if(ninConjointStr.equalsIgnoreCase(ninDuConjointStr)){
										checkConjointFlag = true;
										String adresse = (String) nextConjointElt.getFieldAndMDForPath("adresse/asCurrent").getValue();
										addresChildBoInstance.set("address1", adresse);
									}
								}
							}
							logger.info("checkConjointFlag: " + checkConjointFlag);
							if(!checkConjointFlag){
								addError(CmMessageRepository1001.MSG_101());
							}
							
							if(!isNullOrEmpty(villeDeNaissance)){
								addressChildCharInstance = addresChildBoInstance.getList("addressChar").newChild();
								addressChildCharInstance.set("charTypeCD", "VILENAIS");
								addressChildCharInstance.set("adhocCharVal", villeDeNaissance);
								addressChildCharInstance.set("searchCharVal", villeDeNaissance);
							}
						 
							addresChildBoInstance.set("bo", addresChildBoInstance.getBusinessObject());
							//addresChildBoInstance.set("status","C1AC");
							addresChildBoInstance = BusinessObjectDispatcher.add(addresChildBoInstance);
							logger.info("addresChildBoInstance::addressId " +  addresChildBoInstance.getString("addressId"));
							//create person Bo Instance
							personChildBoInstance =  BusinessObjectInstance.create("CM-PersonIndividualChar");
							personChildBoInstance.set("personType",personType);
							//personChildBoInstance.set("language","ENG");
						
							//Person Name
							personChildNameInstance = personChildBoInstance.getList("personName").newChild();
							personChildNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
							personChildNameInstance.set("firstName", prenomEnfant);
							personChildNameInstance.set("isPrimaryName", Bool.TRUE);
							//Person Address
							personChildAddressInstance = personChildBoInstance.getList("personAddress").newChild();
							personChildAddressInstance.set("addressId", addresChildBoInstance.getString("addressId"));
							personChildAddressInstance.set("addressType", addressType.getId());
							personChildAddressInstance.set("startDate", getSystemDateTime().getDate());
							personChildAddressInstance.set("deliverable", DeliverableLookup.constants.YES);
							//person Id
							personChildIdInstance = personChildBoInstance.getList("personIds").newChild();
							personChildIdInstance.set("idType", "NIN");
							personChildIdInstance.set("personIdNumber", ninDeEnfantStr);
							personChildIdInstance.set("isPrimaryId", Bool.TRUE);

							if(!isNull(dateDeNaissance)){
								COTSInstanceListNode personCharInstance = personChildBoInstance.getList("personChar").newChild();
								String dateNaissance = dateDeNaissance.toString();
								personCharInstance.set("charTypeCD", "CM-DOB");
								personCharInstance.set("adhocCharVal", dateNaissance);
								personCharInstance.set("effectiveDate", getSystemDateTime().getDate());
							}
							//Add Child Bo Instance
							personChildBoInstance = BusinessObjectDispatcher.add(personChildBoInstance);
							logger.info("Child person Created: " + personChildBoInstance.getString("personId"));
							
							if(spouseNinChildIdMap.containsKey(ninConjointStr)){
								childPersonList = spouseNinChildIdMap.get(ninConjointStr);
								childPersonList.add(personChildBoInstance.getString("personId"));
								spouseNinChildIdMap.put(ninConjointStr, childPersonList);
							} else {
								childPersonList = new ArrayList<String>();
								childPersonList.add(personChildBoInstance.getString("personId"));
								spouseNinChildIdMap.put(ninConjointStr, childPersonList);
							}
							enfantPersonBirthDateIdMap.put(personChildBoInstance.getString("personId"), dateDeNaissance);
						} else {// Update Code
							logger.info("###########Update the Child Details##############");
							
							String childPerId = isNull(getPersonByNin("NIN", ninDeEnfantStr)) ? null : getPersonByNin("NIN", ninDeEnfantStr).getId().getIdValue();
							addresChildBoInstance = BusinessObjectInstance.create("CM-PrenatalAddressCharBO");
							COTSInstanceNode groupConjoint = formBoInstance.getGroupFromPath("identiteDuConjoint");
							Iterator<COTSInstanceListNode> iteratorConjoint = groupConjoint.getList("identiteDuConjointList").iterator();
							logger.info("Creating addresBoInstance create in Update : " + addresChildBoInstance.getDocument().asXML());
							boolean checkConjointFlag = false;
							while(iteratorConjoint.hasNext()){
								logger.info("inside conjoint iterator: Update ");
								COTSInstanceListNode nextConjointElt = iteratorConjoint.next();
								if (nextConjointElt != null) {
									BigDecimal ninDuConjoint = (BigDecimal) nextConjointElt.getFieldAndMDForPath("ninDuConjoint/asCurrent").getValue();
									logger.info("ninDuConjoint:UPdate " + ninDuConjoint);
									String ninDuConjointStr = ninDuConjoint.toString();
									
									if(ninConjointStr.equalsIgnoreCase(ninDuConjointStr)){
										checkConjointFlag = true;
										String adresse = (String) nextConjointElt.getFieldAndMDForPath("adresse/asCurrent").getValue();
										addresChildBoInstance.set("address1", adresse);
									}
								}
							}
							logger.info("checkConjointFlag:Update " + checkConjointFlag);
							if(!checkConjointFlag){
								addError(CmMessageRepository1001.MSG_101());
							}
							if(!isNullOrEmpty(villeDeNaissance)){
								addressChildCharInstance = addresChildBoInstance.getList("addressChar").newChild();
								addressChildCharInstance.set("charTypeCD", "VILENAIS");
								addressChildCharInstance.set("adhocCharVal", villeDeNaissance);
								addressChildCharInstance.set("searchCharVal", villeDeNaissance);
							}
						 
							addresChildBoInstance.set("bo", addresChildBoInstance.getBusinessObject());
							//addresChildBoInstance.set("status","C1AC");
							String addressIdUpdate = getAddresIDFrmPersonId(childPerId);
							if(isNullOrEmpty(addressIdUpdate)){
								addError(CmMessageRepository1001.MSG_103());
							}
							addresChildBoInstance.set("addressId", addressIdUpdate);
							addresChildBoInstance = BusinessObjectDispatcher.update(addresChildBoInstance);
							
							logger.info("addresChildBoInstance::addressId:: Update " +  addresChildBoInstance.getString("addressId"));
							//create person Bo Instance
							personChildBoInstance =  BusinessObjectInstance.create("CM-PersonIndividualChar");
							personChildBoInstance.set("personType",personType);
							//personChildBoInstance.set("language","ENG");
						
							//Person Name
							personChildNameInstance = personChildBoInstance.getList("personName").newChild();
							personChildNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
							personChildNameInstance.set("firstName", prenomEnfant);
							personChildNameInstance.set("isPrimaryName", Bool.TRUE);
							//Person Address
							personChildAddressInstance = personChildBoInstance.getList("personAddress").newChild();
							personChildAddressInstance.set("addressId", addresChildBoInstance.getString("addressId"));
							personChildAddressInstance.set("addressType", addressType.getId());
							personChildAddressInstance.set("startDate", getSystemDateTime().getDate());
							personChildAddressInstance.set("deliverable", DeliverableLookup.constants.YES);
							//person Id
							personChildIdInstance = personChildBoInstance.getList("personIds").newChild();
							personChildIdInstance.set("idType", "NIN");
							personChildIdInstance.set("personIdNumber", ninDeEnfantStr);
							personChildIdInstance.set("isPrimaryId", Bool.TRUE);

							if(!isNull(dateDeNaissance)){
								COTSInstanceListNode personCharInstance = personChildBoInstance.getList("personChar").newChild();
								String dateNaissance = dateDeNaissance.toString();
								personCharInstance.set("charTypeCD", "CM-DOB");
								personCharInstance.set("adhocCharVal", dateNaissance);
								personCharInstance.set("effectiveDate", getSystemDateTime().getDate());
							}
							//Add Child Bo Instance
							personChildBoInstance.set("personId", childPerId);
							personChildBoInstance = BusinessObjectDispatcher.update(personChildBoInstance);
							logger.info("Child person Created:Update " + personChildBoInstance.getString("personId"));
							
							if(spouseNinChildIdMap.containsKey(ninConjointStr)){
								childPersonList = spouseNinChildIdMap.get(ninConjointStr);
								childPersonList.add(personChildBoInstance.getString("personId"));
								spouseNinChildIdMap.put(ninConjointStr, childPersonList);
							} else {
								childPersonList = new ArrayList<String>();
								childPersonList.add(personChildBoInstance.getString("personId"));
								spouseNinChildIdMap.put(ninConjointStr, childPersonList);
							}
							enfantPersonBirthDateIdMap.put(personChildBoInstance.getString("personId"), dateDeNaissance);
						}
					}
				}
				//create Account and Person for spouse Details 
				logger.info("spouseNinChildMap::Update" + spouseNinChildIdMap);
				logger.info("enfantPersonBirthDateIdMap::Update" + enfantPersonBirthDateIdMap);
				createAccntPersonForSpouse(conjointIterator,personType,addressType,spouseNinChildIdMap,enfantPersonBirthDateIdMap);
			} else {
				//create Account and Person for spouse Details without enfant details
				createAccntPersonForSpouse(conjointIterator,personType,addressType,spouseNinChildIdMap,enfantPersonBirthDateIdMap);
			}
		}
	}

	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return applyFormRuleAlgorithmInputOutputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData arg0) {
		applyFormRuleAlgorithmInputData = arg0;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(ApplyFormRuleAlgorithmInputOutputData arg0) {
		applyFormRuleAlgorithmInputOutputData = arg0;
	}

}
