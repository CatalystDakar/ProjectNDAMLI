package com.splwg.cm.domain.testcase;

import org.junit.Test;

import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.testers.ContextTestCase;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_Id;
import com.splwg.tax.domain.processFlow.ProcessFlow_Id;

public class ToDoTest extends ContextTestCase {

	@Test
	public void test() {
				
		startChanges();
		BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
		Role_Id toDoRoleId = new Role_Id("CM-REGTODO");
		
		ProcessFlow_Id processFlowId = new ProcessFlow_Id("90133679228305");
		System.out.println(processFlowId.getIdValue());
		Role toDoRole = toDoRoleId.getEntity();
		businessServiceInstance.getFieldAndMDForPath("sendTo").setXMLValue("SNDR");
		businessServiceInstance.getFieldAndMDForPath("subject").setXMLValue("Batch Update from PSRM");
		businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue("CM-REGTO");
		businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
		businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue("90133679228305");
		businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue("90007");
		businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue("301");
		businessServiceInstance.getFieldAndMDForPath("messageParm1").setXMLValue("denashkumar@5iapps");
		businessServiceInstance.getFieldAndMDForPath("messageParm2").setXMLValue(String.valueOf(908070));
		businessServiceInstance.getFieldAndMDForPath("messageParm3").setXMLValue("Reg.CSV");
		businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue("90133679228305");
		BusinessServiceDispatcher.execute(businessServiceInstance);
		saveChanges();
		getSession().commit();
		
		 /*BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-PersonSearchByIdTypeNumber");
		  String personId = "";
		  bsInstance.set("idType", "SCI" );
		  bsInstance.set("idNumber", "156542689" );
		  bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		  
		     // Getting the list of results
	        COTSInstanceList list = bsInstance.getList("results");
	        
	        // If list IS NOT empty
	        if(!list.isEmpty()){

	            // Get the first result
	            COTSInstanceListNode firstRow = list.iterator().next();
	            
	            // Return the person entity
	            System.out.println(firstRow.getString("personId"));
	            
	        }*/

		/*  Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		  while (iterator.hasNext()) {
		   COTSInstanceListNode nextElt = iterator.next();
		   //System.out.println("AccountId: " + nextElt.getNumber("accountId"));
		   System.out.println("PersonId: " + nextElt.getString("personId"));

		  }*/
		

		//BusinessServiceDispatcher.execute(businessServiceInstance);
	
		
	        
	        // Business Service Instance
	       // BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GETRELPER");
	        
	        //Person_Id person = new Person_Id("3547540160");
	        //bsInstance.getFieldAndMDForPath("person").setXMLValue(person);
	        //bsInstance.set("person", new Person_id(person));
	        
	        // Execute BS and return the person Id if exists
	         //executeBSAndRetrievePerson(bsInstance);
	        
		
	}
	
	private Person executeBSAndRetrievePerson(BusinessServiceInstance bsInstance) {

        // Executing BS
        bsInstance = BusinessServiceDispatcher.execute(bsInstance);
        
        // Getting the list of results
        COTSInstanceList list = bsInstance.getList("results");
        
        // If list IS NOT empty
        if(!list.isEmpty()){

            // Get the first result
            COTSInstanceListNode firstRow = list.iterator().next();
            
            // Return the person entity
            return new Person_Id(firstRow.getString("perId")).getEntity();
            
        }
        
        return null;
    }
	
	
	
	/*@Test
	public void givenUsingJDK7Nio2_whenMovingFile_thenCorrect() throws IOException {
		String fineName = "";
	    Path fileToMovePath = Files.createFile(Paths.get("D:\\PSRM\\Bala\\cnt1790655.ppt"));
	    Path targetPath = Paths.get("D:\\PSRM\\");
	 
	    Files.move(fileToMovePath, targetPath.resolve(fileToMovePath.getFileName()));
	}*/

}
