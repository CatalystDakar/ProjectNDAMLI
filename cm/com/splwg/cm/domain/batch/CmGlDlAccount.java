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
 * @author Ramanjaneyulu K
 *
 * @BatchJob (modules = {},softParameters = { @BatchJobSoftParameter (name =ipresObligationList, required = true, type = string)
 *           ,@BatchJobSoftParameter (name = cssObligationList, required =true, type = string)  
 *           ,@BatchJobSoftParameter (entityName = featureConfiguration, name = ledgerNameFeatureConfig, required = true, type = entity) 
 *           ,@BatchJobSoftParameter (entityName = featureConfiguration, name = codeOperationFeatureConfig, required = true, type = entity) 
 *           ,@BatchJobSoftParameter (entityName = featureConfiguration, name = Featureconfigpaiementcaisse01, required = true, type = entity) 
 *           ,@BatchJobSoftParameter (entityName = featureConfiguration, name = Featureconfigpaiementcaisse02, required = true, type = entity)
 *           ,@BatchJobSoftParameter (entityName = featureConfiguration, name = Featureconfigpaiementcaisse03, required = true, type = entity)
 *           })
 */
public class CmGlDlAccount extends CmGlDlAccount_Gen {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmGlDlAccount.class);

	public JobWork getJobWork() {

		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit unit = new ThreadWorkUnit();

		unit.addSupplementalData("cssObligationList", this.getParameters().getCssObligationList());
		unit.addSupplementalData("IpresObligationList", this.getParameters().getIpresObligationList());
		unit.addSupplementalData("ledgerNameFeatureConfig", this.getParameters().getLedgerNameFeatureConfig().getId().getIdValue().toString());
		unit.addSupplementalData("codeOperationFeatureConfig", this.getParameters().getCodeOperationFeatureConfig().getId().getIdValue().toString());
		unit.addSupplementalData("Featureconfigpaiementcaisse01", this.getParameters().getFeatureconfigpaiementcaisse01().getId().getIdValue().toString());
		unit.addSupplementalData("Featureconfigpaiementcaisse02", this.getParameters().getFeatureconfigpaiementcaisse02().getId().getIdValue().toString());
		unit.addSupplementalData("Featureconfigpaiementcaisse03", this.getParameters().getFeatureconfigpaiementcaisse03().getId().getIdValue().toString());
				
		
		listOfThreadWorkUnit.add(unit);

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;

	}

	public Class<CmGlDlAccountWorker> getThreadWorkerClass() {
		return CmGlDlAccountWorker.class;
	}

	public String getNop(String column, String prestation, String featureName) {
		PreparedStatement categoryPrepareStatement = null;
		String value = null;
		String Query = "select wfm_opt_val from CI_WFM_OPT where seq_num=("
				+ "select seq_num from CI_WFM_OPT where WFM_NAME=\'" + featureName
				+ "\' and EXT_OPT_TYPE='CPR' and  WFM_OPT_VAL=\'" + prestation + "\') and EXT_OPT_TYPE='NOP'";

		try {
			categoryPrepareStatement = createPreparedStatement(Query);
			SQLResultRow sqlResultRow = categoryPrepareStatement.firstRow();
			if (sqlResultRow != null) {
				value = sqlResultRow.getString("WFM_OPT_VAL");
			}
		} catch (Exception e) {
			LOGGER.info("Error fetching the vCotisation" + e);
		} finally {
			if(categoryPrepareStatement!=null)
			{
			categoryPrepareStatement.close();
			categoryPrepareStatement = null;
			}
		}
		return value;
	}

	public String getInfo(String query, String dbColumn, String ftId) {
		PreparedStatement selectPrepareStatement = null;
		String value = null;
		QueryIterator<SQLResultRow> selectresult = null;
		try {
			// Select query for fetching the data.
			selectPrepareStatement = createPreparedStatement(query);
			selectPrepareStatement.bindString("FTID", ftId, null);

			selectresult = selectPrepareStatement.iterate();
			while (selectresult.hasNext()) {
				SQLResultRow rowValues = selectresult.next();
				value = rowValues.getString(dbColumn);
				LOGGER.info("Value::" + value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			selectPrepareStatement.close();
			selectPrepareStatement = null;
			selectresult.close();
			selectresult = null;
		}
		return value;
	}

	public String getAmount(String ftId, BigInteger seq) {

		PreparedStatement selectPrepareStatement = null;
		String amount = null;
		QueryIterator<SQLResultRow> selectresult = null;
		try {
			// Select query for fetching BANK Data
			selectPrepareStatement = createPreparedStatement("SELECT GL.AMOUNT FROM CI_FT_GL GL WHERE GL.FT_ID=:FTID AND GL_SEQ_NBR=:GLSEQ");
			selectPrepareStatement.bindString("FTID", ftId, null);
			selectPrepareStatement.bindBigInteger("GLSEQ", seq);

			selectresult = selectPrepareStatement.iterate();
			while (selectresult.hasNext()) {
				SQLResultRow rowValues = selectresult.next();
				amount = rowValues.getString("AMOUNT");
				LOGGER.info("amount::" + amount);
			}

		} catch (Exception e)

		{
			e.printStackTrace();
		} finally {
			if(selectPrepareStatement!=null)
			{
			selectPrepareStatement.close();
			selectPrepareStatement = null;
			}
			if(selectresult!=null)
			{
			selectresult.close();
			selectresult = null;
			}
		}

		return amount;

	}


	public String getLedgername(String featureName, String ext_opt_type, int seq_no) {
		PreparedStatement categoryPrepareStatement = null;
		String Query = "select wfm_opt_val from CI_WFM_OPT where wfm_name=\'" + featureName + "\'"
				+ "and ext_opt_type=\'" + ext_opt_type + "\' and seq_num=\'" + seq_no + "\'";
		String LedgerName = null;
		try {
			categoryPrepareStatement = createPreparedStatement(Query);
			SQLResultRow sqlResultRow = categoryPrepareStatement.firstRow();
			if (sqlResultRow != null) {
				LedgerName = sqlResultRow.getString("WFM_OPT_VAL");
			}
		} catch (Exception e) {
			LOGGER.info("Error fetching the LedgerName" + e);
		} finally {
			categoryPrepareStatement.close();
			categoryPrepareStatement = null;
		}
		return LedgerName;
	}

	/**
	 * Retrieve Bank code
	 */
	private String getBankCode(String ftid) throws Exception {
		PreparedStatement bankCodeStatement = null;
		SQLResultRow bankCodeRow;
		String bankCode = null;
		try {

			String bankBranchquery = "select distinct DFI_ID_NUM from ci_ft a, ci_pay b, ci_pay_tndr c, ci_tndr_ctl d, ci_tndr_srce e, ci_bank_account f "
					+ "where a.ft_type_flg in('PS','PX') " + "and a.parent_id=b.pay_id "
					+ "and b.pay_event_id=c.pay_event_id " + "and c.tndr_ctl_id =d.tndr_ctl_id "
					+ "and d.tndr_source_cd=e.tndr_source_cd " + "and e.bank_acct_key=f.bank_acct_key "
					+ "and a.ft_id =\'" + ftid + "\'";

			bankCodeStatement = createPreparedStatement(bankBranchquery);
			bankCodeRow = bankCodeStatement.firstRow();
			if (bankCodeRow != null) {
				bankCode = bankCodeRow.getString("DFI_ID_NUM");
			}
		} catch (Exception e) {
			LOGGER.info("Exception ::" + e);
		} finally {
			if (bankCodeStatement != null) {
				bankCodeStatement.close();
				bankCodeStatement = null;
			}
		}
		return bankCode;
	}

	/**
	 * Retrieve BankjournalCode
	 */
	private String getBankjournalCode(String featureConfig, String branOption, String branValue, String siteOption,
			String siteValue, String codejournal) throws Exception {
		PreparedStatement bankJournalStatement = null;
		SQLResultRow bankJournalrow;
		String bankJournalCode = null;
		try {
			String bankJournalquery = "select * from CI_WFM_OPT where seq_num="
					+ "(SELECT table_1.SEQ_NUM FROM(SELECT SEQ_NUM FROM CI_WFM_OPT WHERE WFM_NAME=\'" + featureConfig
					+ "\' and  EXT_OPT_TYPE=\'" + branOption + "\' and WFM_OPT_VAL=\'" + branValue + "\') table_1 "
					+ "INNER JOIN " + "(SELECT SEQ_NUM FROM CI_WFM_OPT WHERE WFM_NAME=\'" + featureConfig
					+ "\' and  EXT_OPT_TYPE=\'" + siteOption + "\' and WFM_OPT_VAL=\'" + siteValue + "\') table_2 "
					+ "ON table_2.SEQ_NUM = table_1.SEQ_NUM) and EXT_OPT_TYPE=\'" + codejournal + "\'";

			bankJournalStatement = createPreparedStatement(bankJournalquery);
			bankJournalrow = bankJournalStatement.firstRow();
			if (bankJournalrow != null) {
				bankJournalCode = bankJournalrow.getString("WFM_OPT_VAL");

			}
		} catch (Exception e) {
			LOGGER.info("Exception ::" + e);
		} finally {
			if (bankJournalStatement != null) {
				bankJournalStatement.close();
				bankJournalrow = null;
			}
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
			//String date = "4-SEP-18";
			int count =1;
			int setSeqNumber = 1;
			startChanges();
			try
			{
			//Select query for fetching the data.
			//selectPrepareStatement = createPreparedStatement("SELECT GL.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID AND  FT.ACCOUNTING_DT=  TO_CHAR(TO_DATE" + "(\'"+date+"\', 'DD-MON-YY')) and length(gl.gl_acct) in (85,87) order by gl.ft_id");
			//selectPrepareStatement = createPreparedStatement("SELECT GL.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID AND  FT.ACCOUNTING_DT=:today_date order by gl.ft_id");
			selectPrepareStatement = createPreparedStatement("SELECT gl.FT_ID,GL.GL_SEQ_NBR,FT.FT_TYPE_FLG,GL.GL_ACCT FROM CI_FT FT,CI_FT_GL GL WHERE FT.FT_ID = GL.FT_ID  and gl.gl_acct=' ' order by gl.ft_id desc");
			final String INSERT_QUERY="INSERT INTO CM_INT_GL (LEDGER_NAME,LEDGER_ID,ACCOUNTING_DATE,ACTUAL_FLAG,USER_JE_SOURCE_NAME,USER_JE_CATEGORY_NAME,CURRENCY_CODE,HEADER_NAME,JE_HEADER_ID,HEADER_DESCRIPTION,JE_BATCH_NAME,PERIOD_NAME,CURRENCY_CONVERSION_DATE,CURRENCY_CONVERSION_RATE,JE_LINE_NUM,SEGMENT1,SEGMENT2,SEGMENT3,SEGMENT4,SEGMENT5,SEGMENT6,SEGMENT7,SEGMENT8,SEGMENT9,SEGMENT10,SEGMENT11,SEGMENT12,ACCOUNT_COMBINATION,ENTERED_DR,ENTERED_CR,ACCOUNTED_DR,ACCOUNTED_CR,LINE_DESCRIPTION,CONTEXT,TRANSACTION_DATE,PSRM_TRANSACTION_NUMBER,PSRM_TRANSACTION_STATUS,JGZZ_RECON_REF,BANK_ACCOUNT_NAME,TRX_CODE,TRX_TYPE,REVERSAL_IND,USER_NUM_PSRM,DATE_EXTRACT,STATUS_LINE,ERROR_MESSAGE,DATE_PROCESSED,REQUEST_ID,BUSINESS_GROUP,BUSINESS_GROUP_ID,USER_CURRENCY_CONVERSION_TYPE) VALUES (:LEDGER_NAME,:LEDGER_ID,:ACCOUNTING_DATE,:ACTUAL_FLAG,:USER_JE_SOURCE_NAME,:USER_JE_CATEGORY_NAME,:CURRENCY_CODE,:HEADER_NAME,:JE_HEADER_ID,:HEADER_DESCRIPTION,:JE_BATCH_NAME,:PERIOD_NAME,:CURRENCY_CONVERSION_DATE,:CURRENCY_CONVERSION_RATE,:JE_LINE_NUM,:SEGMENT1,:SEGMENT2,:SEGMENT3,:SEGMENT4,:SEGMENT5,:SEGMENT6,:SEGMENT7,:SEGMENT8,:SEGMENT9,:SEGMENT10,:SEGMENT11,:SEGMENT12,:ACCOUNT_COMBINATION,:ENTERED_DR,:ENTERED_CR,:ACCOUNTED_DR,:ACCOUNTED_CR,:LINE_DESCRIPTION,:CONTEXT,:TRANSACTION_DATE,:PSRM_TRANSACTION_NUMBER,:PSRM_TRANSACTION_STATUS,:JGZZ_RECON_REF,:BANK_ACCOUNT_NAME,:TRX_CODE,:TRX_TYPE,:REVERSAL_IND,:USER_NUM_PSRM,:DATE_EXTRACT,:STATUS_LINE,:ERROR_MESSAGE,:DATE_PROCESSED,:REQUEST_ID,:BUSINESS_GROUP,:BUSINESS_GROUP_ID,:USER_CURRENCY_CONVERSION_TYPE)";
			//selectPrepareStatement.bindDate("today_date", getSystemDateTime().getDate());

			selectresult  = selectPrepareStatement.iterate();
			String LedgerName,ledgerId,actual_flag,taxType,svcTypeCd,Prestation,currencyCd,headerName,ft_Id,jeBatchName,userJeCategoryName,currencyConvertionType,currencyConvertionRate,codeBranch,codeGestion,codeSite,CodeComptable,CentreCoutIpres,CodePrestation,DateExercice,project,interBranch,siteBenificiary,reserve1,reserve2,vide,lineDescription,transctionNumber,jgzzReconRef,fzUser,status,errorDescription,processedDate,requestId,businessGroup,businessGroupId,bank_name,chequeNumber,cssObgType,ipresObgType,trxCode,trxType,JE_HEADER_ID,bankCode,bankCodeValue,nop,enterdDr,enterdCr,transactionStatus,ftId,ftTypeFlg,glAccount,periodDed,amount;
			ledgerId =LedgerName= actual_flag = taxType = svcTypeCd=currencyCd=Prestation=headerName=ft_Id=jeBatchName=userJeCategoryName=currencyConvertionType=currencyConvertionRate=codeBranch=codeGestion=codeSite=CodeComptable=CentreCoutIpres=CodePrestation=DateExercice=project=interBranch=siteBenificiary=reserve1=reserve2=vide=lineDescription=transctionNumber=jgzzReconRef=fzUser=status=errorDescription=processedDate=requestId=businessGroup=businessGroupId=bank_name=chequeNumber=cssObgType=ipresObgType=trxCode=trxType=nop=JE_HEADER_ID=bankCode=bankCodeValue=nop=enterdDr=enterdCr=transactionStatus=ftId=ftTypeFlg=glAccount=periodDed=amount=null;
		    Date Accounting_date,today_date,dateTransction;
			CmGlDlAccount c = new CmGlDlAccount();
			ServiceAgreement sa;
			FinancialTransaction ft;
			List<String> cssObligationList,ipresObligationList;
			int glSeqNo;
			
			//Getting the CSS and Ipres Obligation List from soft parameters.
			cssObgType = this.getParameters().getCssObligationList();
			String cssArrList[] = cssObgType.split(",");
			cssObligationList = Arrays.asList(cssArrList);
			LOGGER.info("CSS Obligation List::"+cssObligationList);

			ipresObgType= this.getParameters().getIpresObligationList();
			String ipresArrLisst[]=ipresObgType.split(",");
			ipresObligationList=Arrays.asList(ipresArrLisst);
			LOGGER.info("Ipres Obligation List::"+ipresObligationList);

			//Iterating Select result
			while (selectresult.hasNext()) {
			SQLResultRow rowValues = selectresult.next();
			
			ftId = rowValues.getString("FT_ID");
			LOGGER.info("Ft Id::"+ftId);
			ftTypeFlg = rowValues.getString("FT_TYPE_FLG").trim();
			LOGGER.info("Ft Type Flag::"+ftTypeFlg);
			glAccount = rowValues.getString("GL_ACCT");
			LOGGER.info("GL Account No:"+glAccount);

			ft = new FinancialTransaction_Id(ftId.trim()).getEntity();
			sa = ft.getServiceAgreement();
					
			//1.Setting up Ledger Name
			ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
			if((ftType.fetchIdSaType()!=null) && cssObligationList.contains(ftType.fetchIdSaType().trim()))
			{
			LedgerName = c.getLedgername(this.getParameters().getLedgerNameFeatureConfig().getId().getIdValue().trim(), CmGlConstant.EXT_OPT_TYPE, 1) ;
			LOGGER.info("Ledger Name::"+LedgerName);
			}
			else
			{
			LedgerName = c.getLedgername(this.getParameters().getLedgerNameFeatureConfig().getId().getIdValue().trim(), CmGlConstant.EXT_OPT_TYPE, 3) ;
			LOGGER.info("Ledger Name::"+LedgerName);
			}
			
			//2.Setting up Ledger Id
			ledgerId= "";
			LOGGER.info("Ledger Id::"+ledgerId);

			//3.Setting up Accounting Date
			Accounting_date = ft.getAccountingDate();
			LOGGER.info("Accounting Date::"+Accounting_date);

			//4.Setting Up Actual flag
			actual_flag = CmGlConstant.ACTUAL_FLAG;
			LOGGER.info("Actual flag::"+actual_flag);

			//5.Setting up USER_JE_SOURCE_NAME
			svcTypeCd = "PSRM";
			LOGGER.info("User Je Source Name::"+svcTypeCd);

			String accountingArrayValues[] =glAccount.split("\\.");

			//gl Sequence number
			glSeqNo = Integer.parseInt(rowValues.getString("GL_SEQ_NBR"));
			LOGGER.info("GlSeqNo::"+glSeqNo);

			//6.setting up USER_JE_CATEGORY_NAME
			userJeCategoryName=accountingArrayValues[0];
			LOGGER.info("User Je Category Name::"+userJeCategoryName);

			//7.Setting Currency Code
			currencyCd =CmGlConstant.CURRENCY_CD;
			LOGGER.info("Currency Value::"+currencyCd);

			//9. Setting up JE_HEADER_ID
			JE_HEADER_ID = accountingArrayValues[1];
			LOGGER.info("HeaderId::"+JE_HEADER_ID);

			//Setting Up FT ID
			ft_Id = ft.getId().getIdValue().toString().trim();
			LOGGER.info("Ft Id ::"+ft_Id);

			//12.Setting up PERIOD_NAME
			periodDed =accountingArrayValues[2];
			LOGGER.info("Period Name::"+periodDed);

			//13.Setting up CURRENCY_CONVERSION_DATE

			//14.Setting currencyConvertionType
			currencyConvertionType="";
			LOGGER.info("Currency Convertion Type::"+currencyConvertionType);

			//15.Setting up currencyConvertionRate
			currencyConvertionRate="";
			LOGGER.info("Currency Convertion Rate::"+currencyConvertionRate);

			//16.Setting JE_LINE_NUM
			LOGGER.info("Je Line Num::"+glSeqNo);

			//17.Setting Code branch as per the latest change.
			codeBranch = accountingArrayValues[4];
			LOGGER.info("Segment1::"+codeBranch);

			//18.Settting Code gestion
			codeGestion = accountingArrayValues[5];
			LOGGER.info("Segment2::"+codeGestion);

			//19.Setting Code site
			codeSite =accountingArrayValues[6].toString();
			if(codeSite.contains(" "))
			{
			codeSite = codeSite.replaceAll(" ", "0");
			LOGGER.info("Segment3::"+codeSite);
			}
			else
			{
			codeSite = accountingArrayValues[6].toString();
			LOGGER.info("Segment3::"+codeSite);
			}

			//20.Setting code comptable
			CodeComptable = accountingArrayValues[7];
			LOGGER.info("Segment4::"+CodeComptable);

			//21.Setting CentreCoutIpres
			CentreCoutIpres = accountingArrayValues[8];
			if(CentreCoutIpres.equals("        "))
			{
			CentreCoutIpres = CentreCoutIpres.replaceAll(" ","0");
			LOGGER.info("Segment5::"+CentreCoutIpres);
			}
			else
			{
			CentreCoutIpres = accountingArrayValues[8];	
			LOGGER.info("Segment5::"+CentreCoutIpres);
			}

			//22.Setting CodePrestation
			CodePrestation = accountingArrayValues[9];
			LOGGER.info("Segment6::"+CodePrestation);

			//23.Setting date
			DateExercice = accountingArrayValues[10];
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
				
			//11.Seeting up JE_BATCH_NAME
			if(ft.getFinancialTransactionType().isAdjustment()||ft.getFinancialTransactionType().isAdjustmentCancellation())
			{
			jeBatchName = nop+"-"+getSystemDateTime().getDate().toString();
			}
			else if(ft.getFinancialTransactionType().isPaySegment()||ft.getFinancialTransactionType().isPayCancellation())
			{
					bankCode= c.getBankCode(ft.getId().getIdValue().trim().toString());
					if(bankCode.equals("01"))
					{
					bankCodeValue= c.getBankjournalCode(this.getParameters().getFeatureconfigpaiementcaisse01().getId().getIdValue().trim().toString(),CmGlConstant.BRN1_OPTION,codeBranch,CmGlConstant.SIT1_OPTION,codeSite,CmGlConstant.CODE_JOURNAL1);
					jeBatchName = bankCodeValue+"-"+getSystemDateTime().getDate().toString();
					}else if(bankCode.equals("02"))
					{
					bankCodeValue=c.getBankjournalCode(this.getParameters().getFeatureconfigpaiementcaisse01().getId().getIdValue().trim().toString(),CmGlConstant.BRN2_OPTION,codeBranch,CmGlConstant.SIT2_OPTION,codeSite,CmGlConstant.CODE_JOURNAL2);
					jeBatchName = bankCodeValue+"-"+getSystemDateTime().getDate().toString();
					}else
					{
					bankCodeValue=c.getBankjournalCode(this.getParameters().getFeatureconfigpaiementcaisse01().getId().getIdValue().trim().toString(),CmGlConstant.BRN3_OPTION,codeBranch,CmGlConstant.SIT3_OPTION,codeSite,CmGlConstant.CODE_JOURNAL3);
					jeBatchName = bankCodeValue+"-"+getSystemDateTime().getDate().toString();
					}
			}
			else
			{
				LOGGER.info("This part is for Benefits in future");
			}
			LOGGER.info("Je Batch Name::"+jeBatchName);
			
			//8.Setting Up HEADER_NAME 
			//10.Setting up Header Description
			Prestation =accountingArrayValues[9];
			if(ft.getFinancialTransactionType().isAdjustment()||ft.getFinancialTransactionType().isAdjustmentCancellation())
			{
				nop = c.getNop(CmGlConstant.WFM_OPT_VAL, Prestation,this.getParameters().getCodeOperationFeatureConfig().getId().getIdValue().trim());
				headerName = nop+"-"+getSystemDateTime().getDate().toString()+"-"+setSeqNumber;
			}else if(ft.getFinancialTransactionType().isPaySegment()||ft.getFinancialTransactionType().isPayCancellation())
			{
				bankCode= c.getBankCode(ft.getId().getIdValue().trim().toString());
				if(bankCode.equals("01"))
				{
				bankCodeValue= c.getBankjournalCode(this.getParameters().getFeatureconfigpaiementcaisse01().getId().getIdValue().trim().toString(),CmGlConstant.BRN1_OPTION,codeBranch,CmGlConstant.SIT1_OPTION,codeSite,CmGlConstant.CODE_JOURNAL1);
				headerName = bankCodeValue+"-"+getSystemDateTime().getDate().toString()+"-"+setSeqNumber;
				}else if(bankCode.equals("02"))
				{
				bankCodeValue= c.getBankjournalCode(this.getParameters().getFeatureconfigpaiementcaisse02().getId().getIdValue().trim().toString(),CmGlConstant.BRN2_OPTION,codeBranch,CmGlConstant.SIT2_OPTION,codeSite,CmGlConstant.CODE_JOURNAL2);
				headerName = bankCodeValue+"-"+getSystemDateTime().getDate().toString()+"-"+setSeqNumber;
				}else
				{
				bankCodeValue= c.getBankjournalCode(this.getParameters().getFeatureconfigpaiementcaisse03().getId().getIdValue().trim().toString(),CmGlConstant.BRN2_OPTION,codeBranch,CmGlConstant.SIT3_OPTION,codeSite,CmGlConstant.CODE_JOURNAL3);
				headerName = bankCodeValue+"-"+getSystemDateTime().getDate().toString()+"-"+setSeqNumber;
				}
				
			}else
			{
				LOGGER.info("This part is benefits billing in future");
			}
			LOGGER.info("Header Name::"+headerName);
			if(count%2==0){
				setSeqNumber++; 
			}
			

			//29. Setting  ACCOUNT_COMBINATION
			vide ="";
			LOGGER.info("ACCOUNT_COMBINATION::"+vide);

			//30,31,32,33Setting Enterd DR
			amount = c.getAmount(ftId,BigInteger.valueOf(glSeqNo));
			double l = Double.parseDouble(amount);
			if(l >= 0)
			{
					enterdCr = amount;
					enterdDr = null;
					LOGGER.info("enterdCr::"+enterdCr);
					LOGGER.info("enterdDr::"+enterdDr);
			}
			if(l<0)
			{
					enterdDr = new BigDecimal(amount).abs().toString();
					enterdCr = null;
					LOGGER.info("enterdCr::"+enterdCr);
					LOGGER.info("enterdDr::"+enterdDr);
			}

			//34.Setting up LINE_DESCRIPTION
			if(ftTypeFlg.equals("PX")||ftTypeFlg.equals("PS")||ftTypeFlg.equals("AD")||ftTypeFlg.equals("AX")||ftTypeFlg.equals("TS")||ftTypeFlg.equals("TX"))
			{
			lineDescription = ftTypeFlg+"-"+ft_Id+"-"+Accounting_date+"-"+ft.getServiceAgreement().getAccount().getId().getIdValue()+"-"+ft.getAccountingDate().getMonth();
			LOGGER.info("lineDescription::"+lineDescription);
			}
			
			//35 Setting Tax Type
			taxType = sa.fetchTaxRole().getServiceType().getId().getIdValue().trim();
			LOGGER.info("Tax Type::"+taxType);

			//36.Setting TRANSACTION_DATE
			dateTransction=ft.getArrearsDate().toDate();
			LOGGER.info("TransctionDate::"+dateTransction);

			//37. Setting PSRM_TRANSACTION_NUMBER.
			transctionNumber = ft_Id;
			LOGGER.info("Ft Id ::"+transctionNumber);

			//38.Setting PSRM Transaction Status
			transactionStatus = ftTypeFlg;
			LOGGER.info("Ft Id ::"+transactionStatus);

			//39.Setting up JGZZ_RECON_REF
			if(ftTypeFlg.equals("AD")||ftTypeFlg.equals("AX"))
			{
				jgzzReconRef=accountingArrayValues[1];
			}
			else if(ftTypeFlg.equals("PS")||ftTypeFlg.equals("PX"))
			{
					jgzzReconRef = c.getInfo(CmGlConstant.TENDER_ID_QUERY, CmGlConstant.PAY_TENDER_ID, ftId);
			}else
			{
				LOGGER.info("This part is for Future benefits purpose");
			}
			LOGGER.info("jgzz Recon Ref ::"+jgzzReconRef);

			//40.setting up BANK_ACCOUNT_NAME  
			String tenderType=c.getInfo(CmGlConstant.TENDER_TYPE_QUERY,CmGlConstant.TENDER_TYPE_CD,ftId);
			if((ftTypeFlg.equals("PS") || ftTypeFlg.equals("PX")) && (tenderType!=null))
			{
				//bank_name = c.getInfo(CmGlConstant.BANK_ACCOUNT_NAME_QUERY,CmGlConstant.DESCR,ftId);
				bank_name="Compte bancaire Attijariwafa Bank Europe Siège";
				
			}else	
			{
				bank_name = null;
			}
			LOGGER.info("Bank Account Name::"+bank_name);
			
			//42.Setting up TRX_TYPE,TRX_CODE
			if(ftTypeFlg.equals("PS")||ftTypeFlg.equals("PX"))
			{
					trxType = ftTypeFlg;
					trxCode = ftId;
			}
			else
			{
					trxType="";
					trxCode="";
			}
			LOGGER.info("trxType::"+trxType);
			LOGGER.info("trxCode::"+trxCode);

			//41. Setting REVERSAL_IND
			if(ftTypeFlg.trim().equals("PX") && (tenderType !=null && tenderType.equals("CHEC")))
			{
				chequeNumber = c.getInfo(CmGlConstant.REJECT_CHEQUE_NBR_QUERY,CmGlConstant.CHECK_NBR,ftId);
				if(chequeNumber!=null)
				{
				   chequeNumber = chequeNumber.trim();
					
				   LOGGER.info("Reversal Ind::"+chequeNumber);
				}
		   }else
				{
					chequeNumber = null;
				}

			//42. Setting up USER_NUM_PSRM
			fzUser = ft.getFrozenByUserId().getIdValue().trim();
			LOGGER.info("user Num Psrm::"+fzUser);

			//43.Setting up DATE_EXTRACT
			today_date = getSystemDateTime().getDate();
			LOGGER.info("Date Extraction::"+today_date);

			//44.Setting up STATUS_LINE
			status = "N";
			LOGGER.info("Status ::"+status);
			
			//45.Setting ERROR_MESSAGE
			errorDescription = "ERROR_DESC";
			LOGGER.info("Status ::"+errorDescription);

			//46.Setting Up DATE_PROCESSED
			processedDate = "";
			LOGGER.info("Date Processed ::"+processedDate);

			//47.Setting RequestId
			requestId = "";
			LOGGER.info("Request Id ::"+requestId);

			//48.Setting businessGroup
			businessGroup = "";
			LOGGER.info("Business Group ::"+businessGroup);

			//49.Setting business Group Id
			businessGroupId="";
			LOGGER.info("Business Group Id::"+businessGroupId);
			
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
			insertPrepareStatement.bindString("BANK_ACCOUNT_NAME", bank_name, null);
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
			count++;
			saveChanges();

			}

		}catch(Exception e)
		{
			e.printStackTrace();
		} finally

		{
			if(selectPrepareStatement!=null)
			{
			selectPrepareStatement.close();
			selectPrepareStatement = null;
			}
			if(selectresult!=null)
			{
			selectresult.close();
			selectresult = null;
			}
		} return true;
	}
}}
