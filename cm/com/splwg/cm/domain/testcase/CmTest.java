package com.splwg.cm.domain.testcase;

import static org.junit.Assert.*;

import com.splwg.base.api.serviceScript.ServiceScriptDispatcher;
import com.splwg.base.api.serviceScript.ServiceScriptInstance;
import com.splwg.base.api.testers.ContextTestCase;
import org.junit.Test;

public class CmTest extends ContextTestCase{

	@Test
	public void test() {
		
		//List<String> listSalaries=getSalariedEmploer("7155490847");
		//listSalaries.contains("7155490847");
		//System.out.println("*******"+listSalaries.contains("1594507718"));
		//System.out.println("*******"+businessServiceInstance.getDocument().asXML());
		
		//this.createToDoSalariedExistence("khadim", "Cisse", "Catalyst", "SCI", "146489789789");
		//BusinessObject_Id boId= new BusinessObject_Id("14649153584845");
		//BusinessObject bo=boId.getEntity();
		//BusinessObjectInstance
		ServiceScriptInstance serviceScriptInstance=new ServiceScriptInstance("CM-CrtEmpFrm");
		System.out.println("*********************"+serviceScriptInstance.getSchemaName());
		serviceScriptInstance.getFieldAndMDForPath("input/registrationFormInfos/receiveDate").setXMLValue("2018-07-18");
		serviceScriptInstance.getFieldAndMDForPath("input/registrationFormInfos/documentLocator").setXMLValue("TEST IMMAT EMPL");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/regType").setXMLValue("BVOLN");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/employerType").setXMLValue("ASSO");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/typeEtablissement").setXMLValue("HDQT");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/employerName").setXMLValue("KHADIM IT");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/hqId").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/nineaNumber").setXMLValue("878718986");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/ninetNumber").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/companyOriginId").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/legalStatus").setXMLValue("PVT");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/taxId").setXMLValue("9T5");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/taxIdDate").setXMLValue("2018-07-02");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/tradeRegisterNumber").setXMLValue("SN.GUY.2018.A.78548");
		serviceScriptInstance.getFieldAndMDForPath("input/employerQuery/tradeRegisterDate").setXMLValue("2018-07-03");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/dateOfInspection").setXMLValue("2018-07-04");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/dateOfFirstHire").setXMLValue("2018-07-05");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/shortName").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/businessSector").setXMLValue("Activités de fabrication");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/mainLineOfBusiness").setXMLValue("ABATTAGE BETAIL");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/atRate").setXMLValue("3");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/noOfWorkersInGenScheme").setXMLValue("2");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/noOfWorkersInBasicScheme").setXMLValue("1");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/region").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/department").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/arondissement").setXMLValue("ALMADIES");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/commune").setXMLValue("NGOR");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/qartier").setXMLValue("ILE DE NGOR");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/address").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/postboxNo").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/telephone").setXMLValue("774279865");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/email").setXMLValue("test2018180701@test.sn");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/website").setXMLValue("www.test.sn");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/zoneCss").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/zoneIpres").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/sectorCss").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/sectorIpres").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/agencyCss").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/mainRegistrationForm/agencyIpres").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/legalRepPerson").setXMLValue("8874790509");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/lastName").setXMLValue("Cisse");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/firstName").setXMLValue("Khadim");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/birthdate").setXMLValue("1990-01-01");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/nationality").setXMLValue("SN");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/nin").setXMLValue("15464867897987");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/placeOfBirth").setXMLValue("SN");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/cityOfBirth").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/typeOfIdentity").setXMLValue("NIN");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/identityIdNumber").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/ninCedeo").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/issuedDate").setXMLValue("2017-02-09");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/expiryDate").setXMLValue("2020-07-02");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/region").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/department").setXMLValue("ALMADIES");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/arondissement").setXMLValue("NGOR");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/commune").setXMLValue("MERMOZ SACRE COEUR");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/qartier").setXMLValue("BAOBAB");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/address").setXMLValue("Dakar");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/landLineNumber").setXMLValue("");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/mobileNumber").setXMLValue("774646544");
		serviceScriptInstance.getFieldAndMDForPath("input/legalRepresentativeForm/email").setXMLValue("");
	
		ServiceScriptDispatcher.invoke(serviceScriptInstance);
		saveChanges();
		getSession().commit();

	}
	

}
