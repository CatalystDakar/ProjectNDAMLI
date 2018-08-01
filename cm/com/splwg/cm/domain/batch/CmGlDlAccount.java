package com.splwg.cm.domain.batch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
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
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.person.Person_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.tax.domain.financial.financialTransaction.FinancialTransaction_Id;

/**
 * @author Ramanjaneyulu  K
 *
@BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name = ipresObligationList, required = true, type = string)
 *            , @BatchJobSoftParameter (name = cssObligationList, required = true, type = string)})
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
			//String date = "11-JUL-18";
			int count =1;
			int setSeqNumber = 1;
			startChanges();
			try
			{
			//Select query for fetching the data.
			//selectPrepareStatement = createPreparedStatement("SELECT GL.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID AND  FT.ACCOUNTING_DT=  TO_CHAR(TO_DATE(\'"+date+"\', 'DD-MON-YY'))");
			selectPrepareStatement = createPreparedStatement("SELECT GL.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID AND  FT.ACCOUNTING_DT=:today_date");
				
			
			final String INSERT_QUERY="INSERT INTO CM_INT_GL (LEDGER_NAME,LEDGER_ID,ACCOUNTING_DATE,ACTUAL_FLAG,USER_JE_SOURCE_NAME,USER_JE_CATEGORY_NAME,CURRENCY_CODE,HEADER_NAME,JE_HEADER_ID,HEADER_DESCRIPTION,JE_BATCH_NAME,PERIOD_NAME,CURRENCY_CONVERSION_DATE,CURRENCY_CONVERSION_RATE,JE_LINE_NUM,SEGMENT1,SEGMENT2,SEGMENT3,SEGMENT4,SEGMENT5,SEGMENT6,SEGMENT7,SEGMENT8,SEGMENT9,SEGMENT10,SEGMENT11,SEGMENT12,ACCOUNT_COMBINATION,ENTERED_DR,ENTERED_CR,ACCOUNTED_DR,ACCOUNTED_CR,LINE_DESCRIPTION,CONTEXT,TRANSACTION_DATE,PSRM_TRANSACTION_NUMBER,PSRM_TRANSACTION_STATUS,JGZZ_RECON_REF,BANK_ACCOUNT_NAME,REVERSAL_IND,USER_NUM_PSRM,DATE_EXTRACT,STATUS_LINE,ERROR_MESSAGE,DATE_PROCESSED,REQUEST_ID,BUSINESS_GROUP,BUSINESS_GROUP_ID,USER_CURRENCY_CONVERSION_TYPE) VALUES (:LEDGER_NAME,:LEDGER_ID,:ACCOUNTING_DATE,:ACTUAL_FLAG,:USER_JE_SOURCE_NAME,:USER_JE_CATEGORY_NAME,:CURRENCY_CODE,:HEADER_NAME,:JE_HEADER_ID,:HEADER_DESCRIPTION,:JE_BATCH_NAME,:PERIOD_NAME,:CURRENCY_CONVERSION_DATE,:CURRENCY_CONVERSION_RATE,:JE_LINE_NUM,:SEGMENT1,:SEGMENT2,:SEGMENT3,:SEGMENT4,:SEGMENT5,:SEGMENT6,:SEGMENT7,:SEGMENT8,:SEGMENT9,:SEGMENT10,:SEGMENT11,:SEGMENT12,:ACCOUNT_COMBINATION,:ENTERED_DR,:ENTERED_CR,:ACCOUNTED_DR,:ACCOUNTED_CR,:LINE_DESCRIPTION,:CONTEXT,:TRANSACTION_DATE,:PSRM_TRANSACTION_NUMBER,:PSRM_TRANSACTION_STATUS,:JGZZ_RECON_REF,:BANK_ACCOUNT_NAME,:REVERSAL_IND,:USER_NUM_PSRM,:DATE_EXTRACT,:STATUS_LINE,:ERROR_MESSAGE,:DATE_PROCESSED,:REQUEST_ID,:BUSINESS_GROUP,:BUSINESS_GROUP_ID,:USER_CURRENCY_CONVERSION_TYPE)";
			selectPrepareStatement.bindDate("today_date", getSystemDateTime().getDate());
			selectresult  = selectPrepareStatement.iterate();
            String LedgerName,ledgerId,actual_flag,taxType,svcTypeCd,currencyCd,headerName,ft_Id,accType,svct,currencyCodeLabel,user,currencyPayment,codeBranch,codeGestion,codeSite,CodeComptable,CentreCoutIpres,CodePrestation,DateExercice,project,interBranch,siteBenificiary,reserve1,reserve2,vide,drCurrency,crCurrency,scpf,tax_Type,transctionNumber,vide1,vide2,fzUser,status,errorDescription,processedDate,requestId,businessGroup,businessGroupId,bankName,bankJournalCode,chequeNumber,date_check,cssObgType,ipresObgType;
            ledgerId =LedgerName= actual_flag = taxType = svcTypeCd=currencyCd=headerName=ft_Id=accType=svct=currencyCodeLabel=user=currencyPayment=codeBranch=codeGestion=codeSite=CodeComptable=CentreCoutIpres=CodePrestation=DateExercice=project=interBranch=siteBenificiary=reserve1=reserve2=vide=drCurrency=crCurrency=scpf=tax_Type=transctionNumber=vide1=vide2=fzUser=status=errorDescription=processedDate=requestId=businessGroup=businessGroupId=bankName=bankJournalCode=chequeNumber=date_check=cssObgType=ipresObgType=null;
            String enterdDr = null ,enterdCr=null,transactionStatus=null;


            Date Accounting_date,today_date;
            CmGlDlAccount c = new CmGlDlAccount();
            
           //Iterating Select result
           while (selectresult.hasNext()) {
           SQLResultRow rowValues = selectresult.next();
           String ftId = rowValues.getString("FT_ID");
           LOGGER.info("Ft Id::"+ftId);
           String ftTypeFlg = rowValues.getString("FT_TYPE_FLG").trim();
           int glSeqNo = Integer.parseInt(rowValues.getString("GL_SEQ_NBR"));
           LOGGER.info("GlSeqNo::"+glSeqNo);
           String glAccount = rowValues.getString("GL_ACCT");
           LOGGER.info("GL Account No:"+glAccount);
   
           FinancialTransaction ft = new FinancialTransaction_Id(ftId.trim()).getEntity();
           ServiceAgreement sa;
           sa = ft.getServiceAgreement();
           Account account = sa.getAccount();
           String per_id =new Person_Id(account.getMainPerson().getId().getIdValue()).getEntity().getId().getIdValue().trim();
           
           //1.Setting up ledger Label Text
           ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
           
           cssObgType = this.getParameters().getCssObligationList();
           String cssArrList[] = cssObgType.split(",");
           List<String> cssList = Arrays.asList(cssArrList);
           
           ipresObgType= this.getParameters().getIpresObligationList();
           String ipresArrLisst[]=ipresObgType.split(",");
           List<String> ipresList=Arrays.asList(ipresArrLisst);
           LOGGER.info("Ipres Obligation List::"+ipresList);
           
           //if(ftType.fetchIdSaType().trim().equals("O-EPF") || ftType.fetchIdSaType().trim().equals("O-EATMP"))
           if(cssList.contains(ftType.fetchIdSaType().trim()))
           {
        	   LedgerName = CmGlConstant.CSS_LABEL;
        	   LOGGER.info("Css Label Text::"+LedgerName);
           }
           else
           {
        	   LedgerName = CmGlConstant.IPRES_LABEL;
        	   LOGGER.info("Ipres Label Text::"+LedgerName);
           }
           
           //2.Setting up ledger id
           ledgerId= "";
           LOGGER.info("Ledger Id"+ledgerId);
           
           
           //3.Setting up Accounting Date
           Accounting_date = ft.getAccountingDate();
           LOGGER.info("Accounting Date::"+Accounting_date);
           
           //4.Setting Up Actual flag
           actual_flag = CmGlConstant.ACTUAL_FLAG;
           LOGGER.info("Actual flag::"+actual_flag);
           
           //5.Setting up SVC_TYPE_CD
           taxType = ftType.getServiceType().getId().getIdValue().trim();
           LOGGER.info("Tax Type::"+taxType);
           svcTypeCd = "PSRM";
           
           LOGGER.info("Origin::"+svcTypeCd);
           
           //6.setting up USER_JE_CATEGORY_NAME
           
          String tenderType=c.getInfo(CmGlConstant.TENDER_TYPE_QUERY,CmGlConstant.TENDER_TYPE_CD,ftId);
           if((ftTypeFlg.equals("PS") && tenderType.equals("VEPO"))||ftTypeFlg.equals("PX"))
           {
        	   String accno = c.getInfo(CmGlConstant.BANK_ACCOUNT_NBR_QUERY,CmGlConstant.ACCOUNT_NBR,ftId);
               bankJournalCode = c.getJournalNumber(accno);
               LOGGER.info("bankJournalCode::"+bankJournalCode);
        	   
           }
           
           
           //7.Setting Currency
           //currencyCd = ft.getCurrency().getId().getIdValue().trim();
           currencyCd ="XOF";
           LOGGER.info("Currency Value::"+currencyCd);
           
         
           
           //8.Setting Up SVC_TYPE_CD-DATEDUJOUR-Pn glSeqNo
           date_check = ft.getFreezeDateTime().getDate().toString();
           headerName = svcTypeCd+date_check+"P"+setSeqNumber;
           LOGGER.info("HeaderName::"+headerName);
           if(count%2==0){
        	   setSeqNumber++; 
           }
           //9.Setting Up FT ID
           ft_Id = ft.getId().getIdValue().toString().trim();
           LOGGER.info("Ft Id ::"+ft_Id);
           
           //10.Setting up SVC_TYPE_CD-DATEDUJOUR-Pn
           LOGGER.info("Ft Id ::"+ft_Id);
           
           
           
           //11.SVC_TYPE_CD-DATEDUJOUR
           //accType= c.getInfo(ft_Id);
             accType= c.getInfo(CmGlConstant.ACCOUNT_TYPE_QUERY,CmGlConstant.CUST_CL_CD,ft_Id);
           LOGGER.info("Account Type::"+accType);
           if(accType!=null)
           {
        	   accType=accType.trim();
           }
           svct = svcTypeCd +accType+ft.getFreezeDateTime().getDate();
           LOGGER.info("Concat String::"+svct);
           
           //12.PERIOD_NAME
           //
           String periodDed = c.getInfo(CmGlConstant.PERIOD_DESC_QUERY,CmGlConstant.PERIOD_DESCR,ft_Id);
           LOGGER.info("PeriodDesc"+periodDed);
           
           
           //13.Setting CURRENCY_CODE_LBL   
           currencyCodeLabel ="";
           
           //14.Setting user
           user="";
           LOGGER.info("user value"+user);
           
           /*//15.Setting CURRENCY_PYMNT 
           currencyPayment="";
           LOGGER.info("Value::"+currencyPayment);*/
           
           //16.Setting GL_SEQ_NBR
           LOGGER.info(glSeqNo);
           LOGGER.info("GL ACC key::"+glAccount);
           if(glAccount !=null && glAccount.length()==24)
           {	   
           //Setting Code branch
           codeBranch = glAccount.substring(0,2);
           LOGGER.info("Code Branch::"+codeBranch);
           
           //Settting Code gestion
           codeGestion = glAccount.substring(2,4);
           LOGGER.info("Code Gestion::"+codeGestion);
           
           //Setting Code site
           codeSite = glAccount.substring(4, 6);
           LOGGER.info("Code Site::"+codeSite);
           
           //Setting code comptable
           CodeComptable = glAccount.substring(6,12);
           LOGGER.info("CodeComptable::"+CodeComptable);
           
           //Setting CentreCoutIpres
           CentreCoutIpres = glAccount.substring(12,14);
           if(CentreCoutIpres.equals("  "))
           {
        	   CentreCoutIpres = CentreCoutIpres.replace("  ","0");
        	   CentreCoutIpres = StringUtils.rightPad(CentreCoutIpres, 8, "0");
        	   LOGGER.info("CentreCoutIpres::"+CentreCoutIpres);
        	}
           else
           {
        	   CentreCoutIpres = CentreCoutIpres.replace(" ","0");
        	   CentreCoutIpres=StringUtils.rightPad(CentreCoutIpres, 8, "0");
        	   LOGGER.info("CentreCoutIpres::"+CentreCoutIpres);
           }
           //Setting CodePrestation
           CodePrestation = glAccount.substring(14,20);
           LOGGER.info("CodePrestation::"+CodePrestation);
           
           //Setting date
           DateExercice = glAccount.substring(20, 24);
           LOGGER.info("DateExercice::"+DateExercice);
           
           //Setting Project
           project ="0";
           LOGGER.info("Project::"+project);
           
           //Setting Interbranch
           interBranch =  codeBranch;
           LOGGER.info("Inter Branch::"+interBranch);
           
           //Setting site benificiary
           siteBenificiary = codeGestion;
           LOGGER.info("SiteBenificiary::"+siteBenificiary);
           }
           //Setting reserve 1
           reserve1 =CmGlConstant.RESERVE1;
           LOGGER.info("Reserve1::"+reserve1);
           
           //Setting reserve 2
           reserve2=CmGlConstant.RESERVE2;
           LOGGER.info("Reserve2::"+reserve2);
         
           //29. Setting VIDE
           vide ="";
           
           //30,31Setting Enterd DR
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
        	   
      
           //32.Setting Enterd DR currency code
           drCurrency ="XOF";
           LOGGER.info("drCurrency::"+drCurrency);
           
           //33.Setting Enterd CR currency Code
           //XOF
           crCurrency="XOF";
           LOGGER.info("crCurrencyCode::"+crCurrency);
        		   
           //34.Setting SVC_TYPE_CD-ACCOUNTING_DT-PER_ID-FT_ID
           if(ftTypeFlg.equals("PX")||ftTypeFlg.equals("PS")||ftTypeFlg.equals("AD")||ftTypeFlg.equals("AP")||ftTypeFlg.equals("BS")||ftTypeFlg.equals("BX"))
           {
           scpf = ftTypeFlg+"-"+Accounting_date+"-"+per_id+"-"+ft_Id;
           LOGGER.info("Concate String::"+scpf);
           }
           //35Setting Context
           tax_Type = svcTypeCd;
           LOGGER.info("Tax Type ::"+tax_Type);
           
           //36.Setting ACCOUNTING_DT
           Date dateTransction=ft.getFreezeDateTime().getDate();
           LOGGER.info("TransctionDate::"+dateTransction);
           
           //37.Setting PSRM Transaction Status
           transactionStatus = ftTypeFlg;
           
           //38. Setting FT ID.
           transctionNumber = ft_Id;
           LOGGER.info("Ft Id ::"+transctionNumber);
           
           //39.Setting vide
           vide1="";
           
           //43. Setting Vide
           //String tender = c.getTenderType(ftId);
           String tender = c.getInfo(CmGlConstant.TENDER_TYPE_QUERY, CmGlConstant.TENDER_TYPE_CD, ftId);
           if(ftTypeFlg.trim().equals("PX") && tender.equals("CHEQ"))
           {
           //chequeNumber = c.getChequeNumber(ftId);
          chequeNumber = c.getInfo(CmGlConstant.REJECT_CHEQUE_NBR_QUERY,CmGlConstant.CHECK_NBR,ftId);
        	if(chequeNumber!=null)
        	{
        		chequeNumber = chequeNumber.trim();
        		LOGGER.info("Cheque Number::"+chequeNumber);
        		
        	}
     
           }           
           //44. Setting up User that freeze the FT.
           fzUser = ft.getFrozenByUserId().getIdValue().trim();
           LOGGER.info("Frozen User Id::"+fzUser);
           
           //45.Setting up the date
           today_date = getSystemDateTime().getDate();
           LOGGER.info("Date Extraction::"+today_date);
           
           //46.Setting the Status
           status = "NEW";
           
           //47.Setting Error Description
           errorDescription = "";
           
           //48.Setting Up process Date.
           processedDate = "";
           
           //49.Setting RequestId
           requestId = "";
           
           //50.Setting businessGroup
           businessGroup = "";
           
           //51.Setting business Group Id
           businessGroupId="";

           insertPrepareStatement = createPreparedStatement(INSERT_QUERY);;
           insertPrepareStatement.setAutoclose(false);
           insertPrepareStatement.bindString("LEDGER_NAME",LedgerName, null);
           //insertPrepareStatement.bindString("LEDGER_ID", ledgerId, null);
           insertPrepareStatement.bindBigInteger("LEDGER_ID", BigInteger.valueOf(0));
           insertPrepareStatement.bindDate("ACCOUNTING_DATE", Accounting_date);
           insertPrepareStatement.bindString("ACTUAL_FLAG",actual_flag , null);
           insertPrepareStatement.bindString("USER_JE_SOURCE_NAME", svcTypeCd, null);
           insertPrepareStatement.bindString("USER_JE_CATEGORY_NAME", bankJournalCode, null);
           insertPrepareStatement.bindString("CURRENCY_CODE",currencyCd, null);
           insertPrepareStatement.bindString("HEADER_NAME", headerName, null);
           insertPrepareStatement.bindBigInteger("JE_HEADER_ID", BigInteger.valueOf(Long.parseLong(ft_Id)));
           insertPrepareStatement.bindString("HEADER_DESCRIPTION", headerName, null);
           insertPrepareStatement.bindString("JE_BATCH_NAME", svct, null);
           insertPrepareStatement.bindString("PERIOD_NAME", periodDed, null);
           insertPrepareStatement.bindDate("CURRENCY_CONVERSION_DATE",null);
           insertPrepareStatement.bindBigInteger("CURRENCY_CONVERSION_RATE", BigInteger.valueOf(1));
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
           
           insertPrepareStatement.bindString("LINE_DESCRIPTION",scpf,null);
           insertPrepareStatement.bindString("CONTEXT",tax_Type,null);
           insertPrepareStatement.bindDate("TRANSACTION_DATE",dateTransction);
           insertPrepareStatement.bindString("PSRM_TRANSACTION_STATUS", transactionStatus, null);
           insertPrepareStatement.bindString("PSRM_TRANSACTION_NUMBER",transctionNumber,null);
           insertPrepareStatement.bindString("JGZZ_RECON_REF",vide1,null);
           insertPrepareStatement.bindString("BANK_ACCOUNT_NAME", bankJournalCode, null);
           insertPrepareStatement.bindString("REVERSAL_IND",chequeNumber,null);
           insertPrepareStatement.bindString("USER_NUM_PSRM",fzUser,null);
           insertPrepareStatement.bindDate("DATE_EXTRACT",today_date);
           insertPrepareStatement.bindString("STATUS_LINE",status,null);
           insertPrepareStatement.bindString("ERROR_MESSAGE",errorDescription,null);
           insertPrepareStatement.bindDate("DATE_PROCESSED",today_date);
           insertPrepareStatement.bindBigInteger("REQUEST_ID",BigInteger.valueOf(0));
           insertPrepareStatement.bindString("BUSINESS_GROUP",businessGroup,null);
           insertPrepareStatement.bindBigInteger("BUSINESS_GROUP_ID",BigInteger.valueOf(0));
           insertPrepareStatement.bindString("USER_CURRENCY_CONVERSION_TYPE", user, null);
           
           int result = insertPrepareStatement.executeUpdate();
           LOGGER.info("Data Insert Count : " + result);
           count++;
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
				



