package com.splwg.cm.domain.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.SubmissionParameters;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.api.testers.BatchJobTestCase;
import com.splwg.base.domain.batch.batchControl.BatchControl_Id;
import com.splwg.base.domain.batch.batchRun.BatchRun;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;


public class TestBatchUpload extends BatchJobTestCase{

	@Override
	protected SubmissionParameters setupRun(SubmissionParameters arg0) {
		// TODO Auto-generated method stub
/*		HashMap<String, HashMap<String,String>> map = new HashMap<String, HashMap<String,String>>();
		map.get(key); 
		map.put("BO", value);*/
		
		arg0.setBatchControlId(new BatchControl_Id("C1-FBHMD"));
		//arg0.setBatchControlId(new BatchControl_Id("TSTBAT"));
		Properties  properties = new Properties();
		//properties.setProperty("formType", "CREGFORM");
		//properties.setProperty("formType", "CSTBATCHFORM");
		properties.setProperty("formType", "REGFORM");
		//properties.setProperty("filePaths", "D:\\PSRM\\Denash\\Immatriculation.xlsx");
		properties.setProperty("filePaths", "D:\\PSRM\\Bala\\");
		properties.setProperty("pathToMove", "D:\\PSRM\\Success\\");
		properties.setProperty("errorFilePathToMove", "D:\\PSRM\\Failure\\");
		arg0.setExtraParameters(properties);
		arg0.setIsTracingProgramEnd(Bool.TRUE);
		arg0.setIsTracingProgramStart(Bool.TRUE);
		arg0.setIsTracingSQL(Bool.TRUE);
		arg0.setIsTracingStandardOut(Bool.TRUE);
		
		/*PreparedStatement psPreparedStatement = null;
		StringBuilder stringBuilder = null;
		
		stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT * FROM CI_PER_PER WHERE PER_ID1=:PER_ID1 AND END_DT IS NULL");
		
		psPreparedStatement = createPreparedStatement(stringBuilder.toString());
		psPreparedStatement.setAutoclose(false);
		psPreparedStatement.bindString("PER_ID1", "0421056590", null);
		
		
		try {
			QueryIterator<SQLResultRow> result = psPreparedStatement.iterate();
			while(result.hasNext()) {
				System.out.println("I am IN");
				SQLResultRow sqlRoq= result.next();
				System.out.println(sqlRoq.getString("PER_ID1"));
				System.out.println(sqlRoq.getString("PER_ID2"));
			}
			//lookUpValue = result.getString("PER_ID1");
			//lookUpValue = result.getString("PER_ID2");
		} catch (Exception exception) {
			System.out.println("Unable to get Lookup value for the Description:: "+exception.getMessage());
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}*/
		
		
		//boolean isValidEmail = EmailValidator.getInstance().isValid("pape@catalyst-us.com");
		//System.out.println(isValidEmail);
		/*PreparedStatement psPreparedStatement = null;
		StringBuilder stringBuilder = null;
		
		stringBuilder = new StringBuilder();
		stringBuilder.append("select BUS_OBJ_CD, F1_EXT_LOOKUP_VALUE, DESCR from F1_EXT_LOOKUP_VAL_L where LANGUAGE_CD = 'ENG' and")
		.append(" UPPER(DESCR) = UPPER(:DESCR) AND BUS_OBJ_CD =:BUS_OBJ_CD ");
				
		psPreparedStatement = createPreparedStatement(stringBuilder.toString());
		psPreparedStatement.setAutoclose(false);
		psPreparedStatement.bindString("DESCR", "Projet de d�veloppement", null);
		psPreparedStatement.bindEntity("BUS_OBJ_CD", new BusinessObject_Id("CmLegalStatus").getEntity());
		
		String lookUpValue = "";
		try {
			SQLResultRow result = psPreparedStatement.firstRow();
			lookUpValue = result.getString("F1_EXT_LOOKUP_VALUE");
			System.out.println("lookUpValue:: " + lookUpValue);
		} catch (Exception exception) {
			System.out.println("Unable to get Lookup value for the Description:: "+exception.getMessage());
			exception.printStackTrace();
		} finally {
			psPreparedStatement.close();
			psPreparedStatement = null;
		}*/
		
		return arg0;
	}
	
	

	@Override
	protected void validateResults(BatchRun arg0) {
		// TODO Auto-generated method stub
		
	}

}
