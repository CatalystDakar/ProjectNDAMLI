package com.splwg.cm.domain.batch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;

/**
 * @author Ramanjaneyulu  K
 *
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = errorFileExtension, required = true, type = string)
 *            , @BatchJobSoftParameter (name = outPutFilePath, required = true, type = string)})
 */
public class CmGlDlAccount extends CmGlDlAccount_Gen {

	public JobWork getJobWork() {
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit unit = new ThreadWorkUnit();
		
		unit.addSupplementalData("outPutFilePath", this.getParameters().getOutPutFilePath());
		unit.addSupplementalData("errorFileExtension", this.getParameters().getErrorFileExtension());
		
		listOfThreadWorkUnit.add(unit);

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;


	}

	public Class<CmGlDlAccountWorker> getThreadWorkerClass() {
		return CmGlDlAccountWorker.class;
	}

	public static class CmGlDlAccountWorker extends CmGlDlAccountWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new CommitEveryUnitStrategy(this);
		}

		@SuppressWarnings("deprecation")
		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			//Prepared Statement Declaration
			PreparedStatement selectPrepareStatement = null;
			PreparedStatement fileSeqNoPrepareStatement = null;
			PreparedStatement insertPrepareStatement = null;
			
			startChanges();
	        //Insert Query formation
			final String INSERT_QUERY="INSERT INTO CM_GLDL_INT (FT_ID,FT_TYPE_FLG,ACCT_ID,PER_ID,GL_ACCT,AMOUNT,STATUS_FLAG,ERROR_DESC) VALUES (:FT_ID,:FT_TYPE_FLG,:ACCT_ID,:PER_ID,:GL_ACCT,:AMOUNT,:STATUS_FLAG,:ERROR_DESC)";
			
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date date = new Date();
			
			//Calling File Seq for writing into File name
			fileSeqNoPrepareStatement= createPreparedStatement("SELECT CMGlDl_seq.nextval FROM dual");
			QueryIterator<SQLResultRow> file_seq = null;
			SQLResultRow file_seq_no = null;
			file_seq = fileSeqNoPrepareStatement.iterate();
			while (file_seq.hasNext()) {
				file_seq_no = file_seq.next();
			}
			int seq_no= Integer.parseInt(file_seq_no.getString("NEXTVAL"));
			//File sequence Completed.
			
			BufferedWriter bw = null;
			FileWriter fw = null;
			try{
			
			//Select query for fetching the data.
			selectPrepareStatement = createPreparedStatement("select ftGl.FT_ID,ft.FT_TYPE_FLG,sa.ACCT_ID,acctPer.PER_ID, cdcf.GL_ACCT, ftGl.AMOUNT from ci_ft_gl ftGl, ci_ft ft, CI_SA sa, CI_ACCT acct, CI_ACCT_PER acctPer,ci_dst_code_eff cdcf where ftGl.FT_ID=ft.FT_ID and ft.SA_ID=sa.SA_ID and sa.ACCT_ID=acct.ACCT_ID and acct.ACCT_ID=acctPer.ACCT_ID and ftgl.DST_ID=cdcf.DST_ID");
			QueryIterator<SQLResultRow> selectresult = null;
			selectresult  = selectPrepareStatement.iterate();
			
			//File Name formation.
			final  String FILENAME = this.getParameters().getOutPutFilePath()+ "//MHSI.COMPTA."+seq_no+"."+dateFormat.format(date)+".txt";
			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);
			
			//Iterating Select result
			while (selectresult.hasNext()) {
				
				SQLResultRow rowValues = selectresult.next();
				String ftId = rowValues.getString("FT_ID");
				String ftTypeFlg = rowValues.getString("FT_TYPE_FLG");
				String acctId = rowValues.getString("ACCT_ID");
				String perID = rowValues.getString("PER_ID");
				String glAcct = rowValues.getString("GL_ACCT");
				String amount = rowValues.getString("AMOUNT");
				
				
				//Insert into EBS Integration table
				insertPrepareStatement = createPreparedStatement(INSERT_QUERY);
				
				insertPrepareStatement.setAutoclose(false);
				insertPrepareStatement.bindString("FT_ID", ftId, null);
				insertPrepareStatement.bindString("FT_TYPE_FLG", ftTypeFlg, null);
				insertPrepareStatement.bindString("ACCT_ID", acctId, null);
				insertPrepareStatement.bindString("PER_ID", perID, null);
				insertPrepareStatement.bindString("GL_ACCT", glAcct, null);
				insertPrepareStatement.bindString("AMOUNT", amount, null);
				insertPrepareStatement.bindString("STATUS_FLAG", "N", null);
				insertPrepareStatement.bindString("ERROR_DESC", " ", null);
				
				bw.write(ftId+"|");
				bw.write(ftTypeFlg+"|");
				bw.write(acctId+"|");
				bw.write(perID+"|");
				bw.write(glAcct+"|");
				bw.write(amount+"\n");
				
				int result = insertPrepareStatement.executeUpdate();
				System.out.println("Data Insert Count : " + result);
			saveChanges();
			}
			
			
			}catch(Exception  e)
			{
				try{
				
				//Creating Error file and writing error information.	
				String errorFileName = this.getParameters().getOutPutFilePath()+ "//MHSI.COMPTA."+seq_no+dateFormat.format(date)+this.getParameters().getErrorFileExtension();
				FileWriter fstream=new FileWriter(errorFileName);
		        BufferedWriter out=new BufferedWriter(fstream);
		        out.write("Batch Name::CM-GLDL"+"\n");
		        out.write("Processing Date::"+dateFormat.format(date)+"\n");
		        out.write("Error::"+e.toString());
		        out.close();
		     
				}catch(Exception error)
				{
					e.printStackTrace();
				}
				
			}finally
			{
				
				
				selectPrepareStatement.close();
				selectPrepareStatement = null;
				
				fileSeqNoPrepareStatement.close();
				fileSeqNoPrepareStatement = null;
				
				insertPrepareStatement.close();
				insertPrepareStatement = null;
				
				try {
					if (fw != null)
						bw.close();
					if (bw != null)
						fw.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
			}
			return true;
	    	}


		}

	}


