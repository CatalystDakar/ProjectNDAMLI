package com.splwg.cm.domain.testcase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.api.testers.AlgorithmImplementationTestCase;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.Algorithm_Id;
import com.splwg.cm.domain.admin.formRule.CmEmployerRegistrationAlgo;

public class AlgorTest extends AlgorithmImplementationTestCase{
	@SuppressWarnings("deprecation")
	public void testInvoke() {
		Algorithm alg = new Algorithm_Id("CM-EMPLREGAL").getEntity();
		
		CmEmployerRegistrationAlgo cmEmployerRegAlgo = alg.getAlgorithmComponent(CmEmployerRegistrationAlgo.class);
		BusinessObjectInstance busInst = BusinessObjectInstance.create("CM-DemandeServicePflwTransBO");
		busInst.set("processFlowId", "59273964034676");
		//busInst = BusinessObjectDispatcher.execute(busInst, BusinessObjectActionLookup.constants.UPDATE);//read(busInst);
		//COTSFieldDataAndMD cots = busInst.getFieldAndMDForPath("employerDetails/ninea");
		//BusinessObjectInstanceKey bus = null;
		//cmEmployerRegAlgo.setBusinessObjectKey(busInst.get);
		
		startChanges();		
		//PreparedStatement psPreparedStatement = null;
		/*Map<String,String> docMap = new HashMap<String, String>();
		String nineaNumber = "909090909";

		
		startChanges();			
		PreparedStatement psPreparedStatement = null;
		psPreparedStatement = createPreparedStatement("SELECT SECTEUR, DESCR FROM CMSECTEUR_L", "select");
		psPreparedStatement.setAutoclose(false);
		Map<String,String> lookUpMap = new HashMap<String, String>();
		
		try {
			QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
			List<String> queryList = new ArrayList<String>();
			while(result.hasNext()) {
			SQLResultRow lookUpValue= result.next();
			String key = lookUpValue.getString("SECTEUR").trim().replaceAll("\\s{2,}", " ");
			String value = lookUpValue.getString("DESCR").trim().replaceAll("\\s{2,}", " ");//\'"+accId+"\')
			String sql = "UPDATE CMSECTEUR_L SET SECTEUR = \'"+key+"\', DESCR= \'"+value+"\' WHERE SECTEUR = \'"+lookUpValue.getString("SECTEUR").trim()+"\';";
			
			System.out.println("SQL Query:: " + sql);
			queryList.add(sql);
			//write(sql);
				
			//System.out.println("COMMUNE:: " +  lookUpValue.getString("COMMUNE").trim().replaceAll("\\s{2,}", " "));
			//System.out.println("DESCR:: " +  lookUpValue.getString("DESCR").trim().replaceAll("\\s{2,}", " "));
			lookUpMap.put(lookUpValue.getString("SECTEUR").trim().replaceAll("\\s{2,}", " "),
			lookUpValue.getString("DESCR").trim().replaceAll("\\s{2,}", " "));
			}
			
			System.out.println(queryList);
			
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}*/
	
		
		
		/*try {
			//psPreparedStatement = createPreparedStatement("INSERT INTO CM_SOAGED select "+nineaNumber+",'','','N',1,max(id_soaged)+1 from CM_SOAGED", "insert");
			psPreparedStatement = createPreparedStatement("INSERT INTO CM_SOAGED (ID_SOAGED,NINEANUMBER, STATUS) VALUES ((select max(id_soaged)+1 from CM_SOAGED),"+nineaNumber+",'N')", "insert");
			
				int result = psPreparedStatement.executeUpdate();
				//System.out.println("Insert Result:: " + result);
				//Thread.sleep(10000);
				
				try {
					psPreparedStatement = createPreparedStatement("SELECT * FROM CM_SOAGED where NINEANUMBER ="+nineaNumber, "select");
					psPreparedStatement.setAutoclose(false);
					QueryIterator<SQLResultRow> resultIterator = psPreparedStatement.iterate();
					while (resultIterator.hasNext()) {
						SQLResultRow lookUpValue = resultIterator.next();
						docMap.put(lookUpValue.getString("DOCNAME"), lookUpValue.getString("DOCURL"));
					}
				} catch(Exception exception) {
					exception.printStackTrace();
					
				}
				
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				psPreparedStatement.close();
				psPreparedStatement = null;
			}
		*/
		
		cmEmployerRegAlgo.invoke();
	}
	
	public static void appendStrToFile(String fileName,String str)
    {
        try {
 
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                   new FileWriter(fileName, true));
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }
	
	//String fileName = "D:\\PSRM\\Denash\\Registration\\filename.txt";
	public void write(String sql) {
		String fileName = "D:\\PSRM\\Denash\\Registration\\filename.txt";
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(sql+"\n");
            out.close();
        }
        catch (IOException e) {
            System.out.println("Exception Occurred" + e);
        }
 
        // Let us append given str to above
        // created file.
        String str = sql;
        appendStrToFile(fileName, str);
 
        // Let us print modified file
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
 
            String mystring;
            while ((mystring = in.readLine()) != null) {
                System.out.println(mystring);
            }
        }
        catch (IOException e) {
            System.out.println("Exception Occurred" + e);
        }
	}
	
	@Override
	protected Class getAlgorithmImplementationClass() {
		// TODO Auto-generated method stub
		return CmEmployerRegistrationAlgo.class;
	}

}
