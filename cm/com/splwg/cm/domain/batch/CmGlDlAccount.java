package com.splwg.cm.domain.batch;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.tax.domain.financial.financialTransaction.FinancialTransaction_Id;

/**
 * @author Ramanjaneyulu  K
 *
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = ipresObligationList, required = true, type = string)
 *            , @BatchJobSoftParameter (name = cssObligationList, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = featureConfiguration, name = ledgerNameFeatureConfig, required = true, type = entity)
 *            , @BatchJobSoftParameter (entityName = featureConfiguration, name = codeOperationFeatureConfig, required = true, type = entity)
 *            })
 */
public class CmGlDlAccount extends CmGlDlAccount_Gen {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmGlDlAccount.class );
	
	public JobWork getJobWork() {
		
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit unit = new ThreadWorkUnit();
		
		unit.addSupplementalData("cssObligationList", this.getParameters().getCssObligationList());
		unit.addSupplementalData("IpresObligationList", this.getParameters().getIpresObligationList());
		
		listOfThreadWorkUnit.add(unit);

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;

	}

	public Class<CmGlDlAccountWorker> getThreadWorkerClass() {
		return CmGlDlAccountWorker.class;
	}
	
	
	public String getNop(String query,String column,String prestation,String featureName)
	{
		PreparedStatement categoryPrepareStatement = null;
		String value=null;
		String Query = "select wfm_opt_val from CI_WFM_OPT where seq_num=("
		 	+ "select seq_num from CI_WFM_OPT where WFM_NAME=\'"+featureName+"\' and EXT_OPT_TYPE='CPR' and  WFM_OPT_VAL=\'"+prestation+"\') and EXT_OPT_TYPE='NOP'";
		 
		 
	        try{
	        	categoryPrepareStatement = createPreparedStatement(Query);
	        	SQLResultRow sqlResultRow = categoryPrepareStatement.firstRow();
	        	 if(sqlResultRow != null){
	        		 value = sqlResultRow.getString("WFM_OPT_VAL");
	        	  }
	        }catch(Exception e)
	        {
	        	LOGGER.info( "Error fetching the vCotisation" +e );
	        }finally
			{
	        	categoryPrepareStatement.close();
	        	categoryPrepareStatement = null;
	        }
	        return value;
	}

	public String getInfo(String query,String dbColumn,String ftId)
	{
		PreparedStatement selectPrepareStatement = null;
		String value=null;
		QueryIterator<SQLResultRow> selectresult = null;
		try
		{
		//Select query for fetching the data.
		selectPrepareStatement = createPreparedStatement(query);
		selectPrepareStatement.bindString("FTID",ftId,null);
        
        selectresult  = selectPrepareStatement.iterate();
        while (selectresult.hasNext()) {
            SQLResultRow rowValues = selectresult.next();
             value = rowValues.getString(dbColumn);
             LOGGER.info("accountType::"+value);
        }
        }catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			selectPrepareStatement.close();
			selectPrepareStatement = null;
			selectresult.close();
			selectresult = null;
		}
		return value;
		}
	
	
	public String getAmount(String ftId,BigInteger seq)
	{
	
		PreparedStatement selectPrepareStatement = null;
		String amount=null;
		  QueryIterator<SQLResultRow> selectresult = null;
		try
		{
		//Select query for fetching BANK Data
		selectPrepareStatement = createPreparedStatement("SELECT GL.AMOUNT FROM CI_FT_GL GL WHERE GL.FT_ID=:FTID AND GL_SEQ_NBR=:GLSEQ");
		selectPrepareStatement.bindString("FTID",ftId,null);
		selectPrepareStatement.bindBigInteger("GLSEQ", seq);
		
        selectresult  = selectPrepareStatement.iterate();
        while (selectresult.hasNext()) {
            SQLResultRow rowValues = selectresult.next();
            amount = rowValues.getString("AMOUNT");
            LOGGER.info("amount::"+amount);
        }
        
		}catch(Exception e)
		
		{
			e.printStackTrace();
		}finally
		{	selectPrepareStatement.close();
			selectPrepareStatement = null;
			selectresult.close();
			selectresult = null;
		}
		
		return amount;
		
	}
	
	
	public String getJournalNumber(String bankAccNo)
	{
		
		PreparedStatement selectPrepareStatement1=null;
		QueryIterator<SQLResultRow> selectresult1 =null;
		String bankJournalCode=null;
		try
		{
		selectPrepareStatement1 = createPreparedStatement("select WFM_OPT_VAL from CI_WFM_OPT where seq_num = (SELECT SEQ_NUM from CI_WFM_OPT where WFM_OPT_VAL=:bankAccNo and WFM_NAME='CM-BANK_LEDG') and EXT_OPT_TYPE='BAJO'");
	    selectPrepareStatement1.bindString("bankAccNo", bankAccNo, null);
	    selectresult1 = selectPrepareStatement1.iterate();
	    while (selectresult1.hasNext()) {
	            SQLResultRow rowValues = selectresult1.next();
	            bankJournalCode = rowValues.getString("WFM_OPT_VAL");
	            LOGGER.info("bankJournalCode::"+bankJournalCode);
	        }
		}catch(Exception e)
		
		{
			e.printStackTrace();
		}finally
		{	
			selectPrepareStatement1.close();
			selectPrepareStatement1 = null;
			selectresult1.close();
			selectresult1 = null;
		}
		return bankJournalCode;
	}
	
	public String getLedgername(String featureName,String ext_opt_type,int seq_no)
	{
		PreparedStatement categoryPrepareStatement = null;
        String Query = "select wfm_opt_val from CI_WFM_OPT where wfm_name=\'"+featureName+"\'"
   	        		+ "and ext_opt_type=\'"+ext_opt_type+"\' and seq_num=\'"+seq_no+"\'";
        String LedgerName = null;
        try{
        	categoryPrepareStatement = createPreparedStatement(Query);
        	SQLResultRow sqlResultRow = categoryPrepareStatement.firstRow();
        	 if(sqlResultRow != null){
        		  LedgerName = sqlResultRow.getString("WFM_OPT_VAL");
        	  }
        }catch(Exception e)
        {
        	LOGGER.info( "Error fetching the LedgerName" +e );
        }finally
		{
        	categoryPrepareStatement.close();
        	categoryPrepareStatement = null;
        }
        return LedgerName;
	}
	
	public static class CmGlDlAccountWorker extends CmGlDlAccountWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new CommitEveryUnitStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			//Fetching the FT Id from CI_FT table.
			PreparedStatement selectPrepareStatement = null;
			PreparedStatement insertPrepareStatement = null;
			QueryIterator<SQLResultRow> selectresult = null;
			//String date = "4-SEP-18";
			startChanges();
			try
			{
			//Select query for fetching the data.
			//selectPrepareStatement = createPreparedStatement("SELECT GL.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID AND  FT.ACCOUNTING_DT=  TO_CHAR(TO_DATE" + "(\'"+date+"\', 'DD-MON-YY')) and length(gl.gl_acct) in (85,87) order by gl.ft_id");
			selectPrepareStatement = createPreparedStatement("SELECT GL.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID AND  FT.ACCOUNTING_DT=:today_date and length(gl.gl_acct) in (85,87) order by gl.ft_id");
			
			final String INSERT_QUERY="INSERT INTO CM_INT_GL (LEDGER_NAME,LEDGER_ID,ACCOUNTING_DATE,ACTUAL_FLAG,USER_JE_SOURCE_NAME,USER_JE_CATEGORY_NAME,CURRENCY_CODE,HEADER_NAME,JE_HEADER_ID,HEADER_DESCRIPTION,JE_BATCH_NAME,PERIOD_NAME,CURRENCY_CONVERSION_DATE,CURRENCY_CONVERSION_RATE,JE_LINE_NUM,SEGMENT1,SEGMENT2,SEGMENT3,SEGMENT4,SEGMENT5,SEGMENT6,SEGMENT7,SEGMENT8,SEGMENT9,SEGMENT10,SEGMENT11,SEGMENT12,ACCOUNT_COMBINATION,ENTERED_DR,ENTERED_CR,ACCOUNTED_DR,ACCOUNTED_CR,LINE_DESCRIPTION,CONTEXT,TRANSACTION_DATE,PSRM_TRANSACTION_NUMBER,PSRM_TRANSACTION_STATUS,JGZZ_RECON_REF,BANK_ACCOUNT_NAME,TRX_CODE,TRX_TYPE,REVERSAL_IND,USER_NUM_PSRM,DATE_EXTRACT,STATUS_LINE,ERROR_MESSAGE,DATE_PROCESSED,REQUEST_ID,BUSINESS_GROUP,BUSINESS_GROUP_ID,USER_CURRENCY_CONVERSION_TYPE) VALUES (:LEDGER_NAME,:LEDGER_ID,:ACCOUNTING_DATE,:ACTUAL_FLAG,:USER_JE_SOURCE_NAME,:USER_JE_CATEGORY_NAME,:CURRENCY_CODE,:HEADER_NAME,:JE_HEADER_ID,:HEADER_DESCRIPTION,:JE_BATCH_NAME,:PERIOD_NAME,:CURRENCY_CONVERSION_DATE,:CURRENCY_CONVERSION_RATE,:JE_LINE_NUM,:SEGMENT1,:SEGMENT2,:SEGMENT3,:SEGMENT4,:SEGMENT5,:SEGMENT6,:SEGMENT7,:SEGMENT8,:SEGMENT9,:SEGMENT10,:SEGMENT11,:SEGMENT12,:ACCOUNT_COMBINATION,:ENTERED_DR,:ENTERED_CR,:ACCOUNTED_DR,:ACCOUNTED_CR,:LINE_DESCRIPTION,:CONTEXT,:TRANSACTION_DATE,:PSRM_TRANSACTION_NUMBER,:PSRM_TRANSACTION_STATUS,:JGZZ_RECON_REF,:BANK_ACCOUNT_NAME,:TRX_CODE,:TRX_TYPE,:REVERSAL_IND,:USER_NUM_PSRM,:DATE_EXTRACT,:STATUS_LINE,:ERROR_MESSAGE,:DATE_PROCESSED,:REQUEST_ID,:BUSINESS_GROUP,:BUSINESS_GROUP_ID,:USER_CURRENCY_CONVERSION_TYPE)";
			selectPrepareStatement.bindDate("today_date", getSystemDateTime().getDate());
			
			selectresult  = selectPrepareStatement.iterate();
            String LedgerName,ledgerId,actual_flag,taxType,svcTypeCd,Prestation,currencyCd,headerName,ft_Id,jeBatchName,userJeCategoryName,currencyConvertionType,currencyConvertionRate,currencyPayment,codeBranch,codeGestion,codeSite,CodeComptable,CentreCoutIpres,CodePrestation,DateExercice,project,interBranch,siteBenificiary,reserve1,reserve2,vide,lineDescription,tax_Type,transctionNumber,jgzzReconRef,vide2,fzUser,status,errorDescription,processedDate,requestId,businessGroup,businessGroupId,accno,bankName,bankJournalCode,chequeNumber,cssObgType,ipresObgType,trxCode,trxType;
            ledgerId =LedgerName= actual_flag = taxType = svcTypeCd=currencyCd=Prestation=headerName=ft_Id=jeBatchName=userJeCategoryName=currencyConvertionType=currencyConvertionRate=currencyPayment=codeBranch=codeGestion=codeSite=CodeComptable=CentreCoutIpres=CodePrestation=DateExercice=project=interBranch=siteBenificiary=reserve1=reserve2=vide=lineDescription=tax_Type=transctionNumber=jgzzReconRef=vide2=fzUser=status=errorDescription=processedDate=requestId=businessGroup=businessGroupId=bankName=accno=bankJournalCode=chequeNumber=cssObgType=ipresObgType=trxCode=trxType=null;
            String enterdDr = null ,enterdCr=null,transactionStatus=null;
            String ext_opt_type="NOC";
            
            Date Accounting_date,today_date;
            CmGlDlAccount c = new CmGlDlAccount();
            
           //Iterating Select result
           while (selectresult.hasNext()) {
           SQLResultRow rowValues = selectresult.next();
           String ftId = rowValues.getString("FT_ID");
           LOGGER.info("Ft Id::"+ftId);
           String ftTypeFlg = rowValues.getString("FT_TYPE_FLG").trim();
           
           String glAccount = rowValues.getString("GL_ACCT");
           LOGGER.info("GL Account No:"+glAccount);
   
           FinancialTransaction ft = new FinancialTransaction_Id(ftId.trim()).getEntity();
           ServiceAgreement sa;
           sa = ft.getServiceAgreement();
           //Account account = sa.getAccount();
           //String per_id =new Person_Id(account.getMainPerson().getId().getIdValue()).getEntity().getId().getIdValue().trim();
           
           //1.Setting up Ledger Name
           ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
           cssObgType = this.getParameters().getCssObligationList();
           String cssArrList[] = cssObgType.split(",");
           List<String> cssList = Arrays.asList(cssArrList);
           
           ipresObgType= this.getParameters().getIpresObligationList();
           String ipresArrLisst[]=ipresObgType.split(",");
           List<String> ipresList=Arrays.asList(ipresArrLisst);
           LOGGER.info("Ipres Obligation List::"+ipresList);
           
          
	       if((ftType.fetchIdSaType()!=null) && cssList.contains(ftType.fetchIdSaType().trim()))
           {
	    	   LedgerName = c.getLedgername(this.getParameters().getLedgerNameFeatureConfig().getId().getIdValue().trim(), ext_opt_type, 1) ;  
           }
           else
           {
        	   LedgerName = c.getLedgername(this.getParameters().getLedgerNameFeatureConfig().getId().getIdValue().trim(), ext_opt_type, 3) ;
           }
           
           //2.Setting up Ledger Id
           ledgerId= "";
           LOGGER.info("Ledger Id"+ledgerId);
           
           //3.Setting up Accounting Date
           Accounting_date = ft.getAccountingDate();
           LOGGER.info("Accounting Date::"+Accounting_date);
           
           //4.Setting Up Actual flag
           actual_flag = CmGlConstant.ACTUAL_FLAG;
           LOGGER.info("Actual flag::"+actual_flag);
           
           //5.Setting up USER_JE_SOURCE_NAME
           svcTypeCd = "PSRM";
           LOGGER.info("Origin::"+svcTypeCd);
           
           String accountingArrayValues[] =glAccount.split("\\.");
           
           //gl Sequence number
           int glSeqNo = Integer.parseInt(rowValues.getString("GL_SEQ_NBR"));
           LOGGER.info("GlSeqNo::"+glSeqNo);
           
           //6.setting up USER_JE_CATEGORY_NAME
            userJeCategoryName=accountingArrayValues[0];
            LOGGER.info("userJeCategoryName::"+userJeCategoryName);
        	              
           //7.Setting Currency Code
           currencyCd ="XOF";
           LOGGER.info("Currency Value::"+currencyCd);
           
           //8.Setting Up HEADER_NAME 
           //10.Setting up Header Description
           Prestation =accountingArrayValues[6];
           String nop = null ;
           String nopFeatureConfig = this.getParameters().getCodeOperationFeatureConfig().getId().getIdValue().trim();
           if(!(Prestation.equals("      ")))
           {
            nop = c.getNop(CmGlConstant.PRESTATION_TYPE_QUERY, CmGlConstant.WFM_OPT_VAL, Prestation,nopFeatureConfig);
           }
           headerName = nop+"-"+getSystemDateTime().getDate().toString()+"-"+glSeqNo;
           LOGGER.info("HeaderName::"+headerName);
          
           //9. Setting up JE_HEADER_ID
           String JE_HEADER_ID = accountingArrayValues[1];
           LOGGER.info("HeaderId::"+JE_HEADER_ID);
           
           //Setting Up FT ID
           ft_Id = ft.getId().getIdValue().toString().trim();
           LOGGER.info("Ft Id ::"+ft_Id);
           
           //11.Seeting up JE_BATCH_NAME
           jeBatchName = nop+"-"+getSystemDateTime().getDate().toString();
           LOGGER.info("Concat String::"+jeBatchName);
           
           //12.Setting up PERIOD_NAME
           //String periodDed = c.getInfo(CmGlConstant.PERIOD_DESC_QUERY,CmGlConstant.PERIOD_DESCR,ft_Id);
           String periodDed =accountingArrayValues[2];
           LOGGER.info("PeriodDesc"+periodDed);
           
           //13.Setting up CURRENCY_CONVERSION_DATE
           
           //14.Setting currencyConvertionType
           currencyConvertionType="";
           LOGGER.info("user value"+currencyConvertionType);
           
           //15.Setting up currencyConvertionRate
           currencyConvertionRate="";
           LOGGER.info("currencyConvertionRate"+currencyConvertionRate);
           
           //16.Setting JE_LINE_NUM
           LOGGER.info("JE_LINE_NUM"+glSeqNo);
          
         
           //17.Setting Code branch as per the latest change.
           codeBranch = accountingArrayValues[4];
           LOGGER.info("Segment1::"+codeBranch);
           
           //18.Settting Code gestion
           codeGestion = accountingArrayValues[5];
           LOGGER.info("Segment2::"+codeGestion);
           
           //19.Setting Code site
           codeSite =accountingArrayValues[9].toString();
           if(codeSite.contains(" "))
           {
        	   codeSite = codeSite.replaceAll(" ", "0");
           }
           else
           {
           codeSite = accountingArrayValues[9];
           LOGGER.info("Segment3::"+codeSite);
           }
           
           //20.Setting code comptable
           CodeComptable = accountingArrayValues[10];
           LOGGER.info("Segment4::"+CodeComptable);
           
           //21.Setting CentreCoutIpres
           CentreCoutIpres = accountingArrayValues[8];
           if(CentreCoutIpres.equals("        "))
           {
        	   CentreCoutIpres = CentreCoutIpres.replaceAll(" ","0");
        	   //CentreCoutIpres = StringUtils.rightPad(CentreCoutIpres, 8, "0");
        	   LOGGER.info("Segment5::"+CentreCoutIpres);
        	}
           else
           {
        	   ///CentreCoutIpres = CentreCoutIpres.replace(" ","0");
        	   //CentreCoutIpres=StringUtils.rightPad(CentreCoutIpres, 8, "0");
        	   LOGGER.info("Segment5::"+CentreCoutIpres);
           }
           
           //22.Setting CodePrestation
           CodePrestation = accountingArrayValues[6];
           LOGGER.info("Segment6::"+CodePrestation);
           
           //23.Setting date
           DateExercice = accountingArrayValues[7];
           LOGGER.info("Segment7::"+DateExercice);
           
           //24.Setting Project
           project =accountingArrayValues[11];
           LOGGER.info("Segment8::"+project);
           
           //25.Setting Interbranch
           interBranch =  codeBranch;
           LOGGER.info("Segment9::"+interBranch);
           
           //26.Setting site benificiary
           siteBenificiary = accountingArrayValues[13];
           LOGGER.info("Segment10::"+siteBenificiary);
           
           
           //27.Setting reserve 1
           reserve1 =accountingArrayValues[14];
           LOGGER.info("Segment11::"+reserve1);
           
           //28.Setting reserve 2
           reserve2=accountingArrayValues[15];
           LOGGER.info("Segment12::"+reserve2);
         
           //29. Setting  ACCOUNT_COMBINATION
           vide ="";
           LOGGER.info("ACCOUNT_COMBINATION::"+vide);
           
           //30,31,32,33Setting Enterd DR
           String amount = c.getAmount(ftId,BigInteger.valueOf(glSeqNo));
           double l = Double.parseDouble(amount);
           if(l >= 0)
           {
        	   enterdCr = amount;
        	   enterdDr = null;
        	   LOGGER.info("enterdCr"+enterdCr);
        	   LOGGER.info("enterdDr"+enterdDr);
           }
           if(l<0)
           {
        	   enterdDr = new BigDecimal(amount).abs().toString();
        	   enterdCr = null;
        	   LOGGER.info("enterdCr"+enterdCr);
        	   LOGGER.info("enterdDr"+enterdDr);
        	}
        	  		   
           //34.Setting up LINE_DESCRIPTION
           if(ftTypeFlg.equals("PX")||ftTypeFlg.equals("PS")||ftTypeFlg.equals("AD")||ftTypeFlg.equals("AX")||ftTypeFlg.equals("TS")||ftTypeFlg.equals("TX"))
           {
           lineDescription = ftTypeFlg+"-"+ft_Id+"-"+Accounting_date+"-"+ft.getServiceAgreement().getAccount().getId().getIdValue()+"-"+ft.getAccountingDate().getMonth();
           LOGGER.info("lineDescription::"+lineDescription);
           }
          
           //35 Setting Context
           taxType = sa.fetchTaxRole().getServiceType().getId().getIdValue().trim();
           LOGGER.info("Tax Type::"+taxType);
           
           //36.Setting TRANSACTION_DATE
           Date dateTransction=ft.getFreezeDateTime().getDate();
           LOGGER.info("TransctionDate::"+dateTransction);
          
           //37. Setting PSRM_TRANSACTION_NUMBER.
           transctionNumber = ft_Id;
           LOGGER.info("Ft Id ::"+transctionNumber);
           
           //38.Setting PSRM Transaction Status
           transactionStatus = ftTypeFlg;
            
           //39.Setting up JGZZ_RECON_REF
           if(ftTypeFlg.equals("AD")||ftTypeFlg.equals("AX"))
           {
           jgzzReconRef=accountingArrayValues[1];
           }
           if(ftTypeFlg.equals("PS")||ftTypeFlg.equals("PX"))
           {
           jgzzReconRef = c.getInfo(CmGlConstant.TENDER_ID_QUERY, CmGlConstant.PAY_TENDER_ID, ftId);
           }
           
           //40.setting up BANK_ACCOUNT_NAME  -- 
           String tenderType=c.getInfo(CmGlConstant.TENDER_TYPE_QUERY,CmGlConstant.TENDER_TYPE_CD,ftId);
           if((ftTypeFlg.equals("PS") && (tenderType!= null && tenderType.equals("VEPO")))||ftTypeFlg.equals("PX"))
           {
           accno = c.getInfo(CmGlConstant.BANK_ACCOUNT_NBR_QUERY,CmGlConstant.ACCOUNT_NBR,ftId);
           bankJournalCode = c.getJournalNumber(accno);
           }else
           {
        	   bankJournalCode = null;
           }
           //42.Setting up TRX_TYPE
           if(ftTypeFlg.equals("PS")||ftTypeFlg.equals("PX"))
           {
        	   trxType = ftTypeFlg;
           }
           else
           {
        	   trxType="";
           }
           //41.Setting up  TRX_CODE
           if(ftTypeFlg.equals("PS")||ftTypeFlg.equals("PX"))
           {
        	   trxCode = ftId;
           }
           else
           {
        	   trxCode ="";
           }
           
           //41. Setting REVERSAL_IND
           if(ftTypeFlg.trim().equals("PX") && (tenderType !=null && tenderType.equals("CHEQ")))
           {
           chequeNumber = c.getInfo(CmGlConstant.REJECT_CHEQUE_NBR_QUERY,CmGlConstant.CHECK_NBR,ftId);
           if(chequeNumber!=null)
           {
           chequeNumber = chequeNumber.trim();
           LOGGER.info("Cheque Number::"+chequeNumber);
           }
           }else
           {
        	   chequeNumber = null;
           }
           
           //42. Setting up USER_NUM_PSRM
           fzUser = ft.getFrozenByUserId().getIdValue().trim();
           LOGGER.info("Frozen User Id::"+fzUser);
           
           //43.Setting up DATE_EXTRACT
           today_date = getSystemDateTime().getDate();
           LOGGER.info("Date Extraction::"+today_date);
           
           //44.Setting up STATUS_LINE
           status = "N";
           
           //45.Setting ERROR_MESSAGE
           errorDescription = "ERROR_DESC";
           
           //46.Setting Up DATE_PROCESSED
           processedDate = "";
           
           //47.Setting RequestId
           requestId = "";
           
           //48.Setting businessGroup
           businessGroup = "";
           
           //49.Setting business Group Id
           businessGroupId="";

           insertPrepareStatement = createPreparedStatement(INSERT_QUERY);;
           insertPrepareStatement.setAutoclose(false);
           insertPrepareStatement.bindString("LEDGER_NAME",LedgerName, null);
           insertPrepareStatement.bindString("LEDGER_ID", ledgerId, null);
   		   insertPrepareStatement.bindDate("ACCOUNTING_DATE", Accounting_date);
   		   insertPrepareStatement.bindString("ACTUAL_FLAG",actual_flag , null);
           insertPrepareStatement.bindString("USER_JE_SOURCE_NAME", svcTypeCd, null);
           insertPrepareStatement.bindString("USER_JE_CATEGORY_NAME", userJeCategoryName, null);
           insertPrepareStatement.bindString("CURRENCY_CODE",currencyCd, null);
           insertPrepareStatement.bindString("HEADER_NAME", headerName, null);
           insertPrepareStatement.bindString("JE_HEADER_ID", JE_HEADER_ID,null);
           insertPrepareStatement.bindString("HEADER_DESCRIPTION", headerName, null);
           insertPrepareStatement.bindString("JE_BATCH_NAME", jeBatchName, null);
           insertPrepareStatement.bindString("PERIOD_NAME", periodDed, null);
           insertPrepareStatement.bindDate("CURRENCY_CONVERSION_DATE",null);
           insertPrepareStatement.bindString("CURRENCY_CONVERSION_RATE",currencyConvertionRate,null);
           insertPrepareStatement.bindBigInteger("JE_LINE_NUM", BigInteger.valueOf(glSeqNo));
           insertPrepareStatement.bindString("SEGMENT1",codeBranch,null);
           insertPrepareStatement.bindString("SEGMENT2",codeGestion,null);
           insertPrepareStatement.bindString("SEGMENT3",codeSite,null);
           insertPrepareStatement.bindString("SEGMENT4",CodeComptable,null);
           insertPrepareStatement.bindString("SEGMENT5",CentreCoutIpres,null);
           insertPrepareStatement.bindString("SEGMENT6",CodePrestation,null);
           insertPrepareStatement.bindString("SEGMENT7",DateExercice,null);
           insertPrepareStatement.bindString("SEGMENT8",project,null);
           insertPrepareStatement.bindString("SEGMENT9",interBranch,null);
           insertPrepareStatement.bindString("SEGMENT10",siteBenificiary,null);
           insertPrepareStatement.bindString("SEGMENT11",reserve1,null);
           insertPrepareStatement.bindString("SEGMENT12",reserve2,null);
           insertPrepareStatement.bindString("ACCOUNT_COMBINATION",vide,null);
           insertPrepareStatement.bindString("ENTERED_DR", enterdDr,null);
           insertPrepareStatement.bindString("ENTERED_CR", enterdCr,null);
           insertPrepareStatement.bindString("ACCOUNTED_DR",enterdDr,null);
           insertPrepareStatement.bindString("ACCOUNTED_CR",enterdCr,null);
           insertPrepareStatement.bindString("LINE_DESCRIPTION",lineDescription,null);
           insertPrepareStatement.bindString("CONTEXT",taxType,null);
           insertPrepareStatement.bindDate("TRANSACTION_DATE",dateTransction);
           insertPrepareStatement.bindString("PSRM_TRANSACTION_NUMBER",transctionNumber,null);
           insertPrepareStatement.bindString("PSRM_TRANSACTION_STATUS", transactionStatus, null);
           insertPrepareStatement.bindString("JGZZ_RECON_REF",jgzzReconRef,null);
           insertPrepareStatement.bindString("BANK_ACCOUNT_NAME", bankJournalCode, null);
           insertPrepareStatement.bindString("TRX_CODE", trxCode, null);
           insertPrepareStatement.bindString("TRX_TYPE", trxType, null);
           insertPrepareStatement.bindString("REVERSAL_IND",chequeNumber,null);
           insertPrepareStatement.bindString("USER_NUM_PSRM",fzUser,null);
           insertPrepareStatement.bindDate("DATE_EXTRACT",today_date);
           insertPrepareStatement.bindString("STATUS_LINE",status,null);
           insertPrepareStatement.bindString("ERROR_MESSAGE",errorDescription,null);
           insertPrepareStatement.bindString("DATE_PROCESSED",processedDate,null);
           insertPrepareStatement.bindString("REQUEST_ID",requestId,null);
           insertPrepareStatement.bindString("BUSINESS_GROUP",businessGroup,null);
           insertPrepareStatement.bindString("BUSINESS_GROUP_ID",businessGroupId,null);
           insertPrepareStatement.bindString("USER_CURRENCY_CONVERSION_TYPE", currencyConvertionType, null);
           
           int result = insertPrepareStatement.executeUpdate();
           LOGGER.info("Data Insert Count : " + result);
         
           saveChanges();
           
            }
           
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			finally{
				selectPrepareStatement.close();
                selectPrepareStatement = null;
                selectresult.close();
                selectresult =null;
                
			}
			return true;
		}
	}
}
				



