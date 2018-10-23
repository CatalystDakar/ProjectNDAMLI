package com.splwg.cm.domain.admin.formRule;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOption;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOptions;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.FinancialTransactionTypeLookup;
import com.splwg.tax.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCode;
import com.splwg.tax.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot;
import com.splwg.tax.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.tax.domain.customerinfo.person.Person_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.tax.domain.financial.financialTransaction.FinancialTransaction_Id;

/**
 * @author Ramanjaneyulu k
 * @AlgorithmComponent ( softParameters = {@AlgorithmSoftParameter (entityName = featureConfiguration, name = cotisationFeatureConfig, required = true, type = entity)
 *      , @AlgorithmSoftParameter (name = characteristicIdentifier, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = codeTypeRegul, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = typeRegulBrancheOption, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = typeRegulGestionOption, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = obligationType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = cssObligationList, required = true ,type=string)
 *            , @AlgorithmSoftParameter (name = ipresObligationList, required = true,type=string) 
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration ,name = centreCoutFeatureConfig, required = true,type=entity)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = Featureconfigpaiementcaisse01, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = Featureconfigpaiementcaisse02, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = Featureconfigpaiementcaisse03, required = true, type = entity) 
 *            , @AlgorithmSoftParameter (name = adjustmentTypeList, required = true,type=string) })
 *           
 */
//Champs de récupération de de la branche de la caisse  « DFI_ID_NUM » This parameter is used to bank code in the program. Its just as part of SFD only.

public class CmGeneralLedgerAccountConstructionAlgComp_Impl extends
		CmGeneralLedgerAccountConstructionAlgComp_Gen implements
		GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot {

	private static final Logger LOGGER = LoggerFactory.getLogger( CmGeneralLedgerAccountConstructionAlgComp_Impl.class );
	
	private FinancialTransaction ft;
	private ServiceAgreement sa;
	private GeneralLedgerDistributionCode glDist;
	private BigInteger sequence;
	
	private static final String OPTION_TYPE="CJC";
	private static final String BRAN_OPTION="BRAN";
	private static final String OPTION_TYPE1 ="COP";
	private static final String OPTION_TYPE2="CCC";
	private static final String OPTION_TYPE3="CS";
	private static final String BRN1_OPTION="BRN1";
	private static final String SIT1_OPTION="SIT1";
	private static final String CODE_JOURNAL1="CJC1";
	private static final String BRN2_OPTION="BRN2";
	private static final String SIT2_OPTION="SIT2";
	private static final String CODE_JOURNAL2="CJC2";
	private static final String BRN3_OPTION="BRN3";
	private static final String SIT3_OPTION="SIT3";
	private static final String CODE_JOURNAL3="CJC3";
	
	private String vCotisation="";
	private String vHeadervalue="";
	private String vPeriodName="";
	private String vCodeGestion="";
	private String vCodeBranche="";
	private String vDateExercice="";
	private String vCodeSite="";
	private String vCodePrestation="";
	private String vCentreCoutIpres="";
	private String vCodeComptable="";
	private String vsitebeneficiary;
	private String bankCode;
	private String bankJournalCode;
	private String bankCodeValue;
	private String cotisationValue;
	private String bankSiteValue;
	private String vGlAccount;
    private String glAccountOutput="";
    private String bankBranchquery;
    private String bankJournalquery;
    private String bankSitequery;
    private String yearquery;
    private String adjType;
    private String adjTypeValues[];
    private List<String> adjTypeList;
	
	@SuppressWarnings("deprecation")
	@Override
	public void invoke() {
		
			LOGGER.info( "INVOKE - START" );
			// TODO Auto-generated method stub
	    	LOGGER.info( "INVOKE - START" +ft.getId().getIdValue());
			sa = ft.getServiceAgreement();

			Account account = sa.getAccount();
			ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
			
			vCodePrestation = getCodePrestation();
			
			//recuperation du codeprestation
			
            LOGGER.info( "vCodePrestation : "+vCodePrestation );
			//recuperation du code comptable
            vCodeComptable = getvCodeComptable();
            
           
		
			LOGGER.info( "vCodeComptable : "+vCodeComptable );
			
			//recuperation de l exercice
			
			vDateExercice = getvDateExercice();
			LOGGER.info( "Exercice : "+vDateExercice );
			
			//Retrieving the Code site
			if(notNull(account.getMainPerson())) { 
				getCodeSite(account.getMainPerson().getId().getIdValue());
			}
				
			
		    //Retrieving the vCentreCoutIpres
			PreparedStatement centreCoutStatement = null;
			SQLResultRow sqlResultRow1;
			String centerCoutQuery=null;
	        try{
	        	centerCoutQuery = "select WFM_OPT_VAL from CI_WFM_OPT where seq_num=("
	        	+"SELECT table_1.SEQ_NUM FROM(SELECT SEQ_NUM FROM CI_WFM_OPT WHERE WFM_NAME=\'"+this.getCentreCoutFeatureConfig().getId().getIdValue().trim().toString()+"\' and  EXT_OPT_TYPE=\'"+OPTION_TYPE1+"\' and WFM_OPT_VAL=\'"+vCodePrestation+"\') table_1"
	        	+" INNER JOIN "
	        	+"(SELECT SEQ_NUM FROM CI_WFM_OPT WHERE WFM_NAME=\'"+this.getCentreCoutFeatureConfig().getId().getIdValue().trim().toString()+"\' and  EXT_OPT_TYPE=\'"+OPTION_TYPE3+"\' and WFM_OPT_VAL=\'"+vCodeSite+"\') table_2"
	        	+" ON table_2.SEQ_NUM = table_1.SEQ_NUM) and EXT_OPT_TYPE=\'"+OPTION_TYPE2+"\'";
	        	centreCoutStatement  = createPreparedStatement(centerCoutQuery);
	        	sqlResultRow1 = centreCoutStatement.firstRow();
	        	 if(sqlResultRow1 != null){
	        	 vCentreCoutIpres = sqlResultRow1.getString("WFM_OPT_VAL");   
	        	 }	
	        }catch(Exception e)
	        {
	        	LOGGER.info( "Error fetching the vCentreCoutIpres" +e );
	        }finally
			{
	        	if(centreCoutStatement!=null)
	        	{
	        	centreCoutStatement.close();
	        	centreCoutStatement = null;
	        	}
	        }
	        
	       LOGGER.info( "- vCentreCoutIpres => " + vCentreCoutIpres );
			
	       
			//recuperation du code Branche
			try {        
		     vCodeBranche = this.getCodeBrancheValue();
			
			} catch ( final Exception vException ) {
	            LOGGER.error( "Une erreur durant la recuperation du code branche", vException );
	            return;
	        }
			
			//Retrieving the USER_JE_CATEGORY_NAME
			try {
			adjType= this.getAdjustmentTypeList();
		    adjTypeValues=adjType.split(",");
		    adjTypeList=Arrays.asList(adjTypeValues);
		    //if(adjTypeList.contains(ft.fetchSiblingAdjustment().getAdjustmentType().getId().getIdValue().trim().toString()))
		    if((ft.getFinancialTransactionType().isAdjustment()||ft.getFinancialTransactionType().isAdjustmentCancellation())) {
		    	if(adjTypeList.contains(ft.fetchSiblingAdjustment().getAdjustmentType().getId().getIdValue().trim().toString())) {
		    		vCotisation = this.getCotisation(this.getCotisationFeatureConfig().getId().getIdValue().trim().toString(), 
			    			BRAN_OPTION,vCodeBranche,OPTION_TYPE);
		    	} else {
		    		
		    	}
		    	
//		    	System.exit(1);//throw new Exception("Exception for FT ID:"+ft.getId().getIdValue()+"The given FT ID not there in the specified adjustment list"); 	
        	}
		    else if((ft.getFinancialTransactionType().isPaySegment()||ft.getFinancialTransactionType().isPayCancellation()))
        	{	
        	bankCodeValue = this.getBankCode(ft.getId().getIdValue().trim().toString());	
        	if(bankCodeValue.equals("01"))
        	{
        	vCotisation = this.getBankjournalCode(this.getFeatureconfigpaiementcaisse01().getId().getIdValue().trim().toString(),BRN1_OPTION,vCodeBranche,SIT1_OPTION,vCodeSite,CODE_JOURNAL1);
        	}else if(bankCodeValue.equals("02"))
        	{
        	vCotisation = this.getBankjournalCode(this.getFeatureconfigpaiementcaisse02().getId().getIdValue().trim().toString(),BRN2_OPTION,vCodeBranche,SIT2_OPTION,vCodeSite,CODE_JOURNAL2);
        	}
        	else
        	{
        	vCotisation = this.getBankjournalCode(this.getFeatureconfigpaiementcaisse03().getId().getIdValue().trim().toString(),BRN3_OPTION,vCodeBranche,SIT3_OPTION,vCodeSite,CODE_JOURNAL3);
        	}
        	}
        	else
        	{
        		LOGGER.info("This part is for Billing purpose");
        	}
		    }catch(Exception e)
			{
        		LOGGER.info("Exception::"+e);
			}
	       LOGGER.info( "- vCotisation => " + vCotisation );
	        
	     //Retrieving the JE_HEADER_ID
			PreparedStatement header_query_statement = null;
			String headerQuery=null;
	        try{
	        	headerQuery = "select char_val_fk1 from ci_sa_char where sa_id=(select sa_id from ci_ft where ft_id=\'"+ft.getId().getIdValue()+"\') and char_type_cd=\'"+this.getCharacteristicIdentifier().toString()+"\'";
	        	header_query_statement =  createPreparedStatement(headerQuery);
	        	SQLResultRow sqlResultRow = header_query_statement.firstRow();
	        	if(sqlResultRow != null){
	        		vHeadervalue = sqlResultRow.getString("CHAR_VAL_FK1");   
	        	}
	        	
	        }catch(Exception e)
	        {
	        	LOGGER.info( "Error fetching the vHeadervalue" +e );
	        }finally
			{
	        	if(header_query_statement!=null)
	        	{
	        	header_query_statement.close();
	        	header_query_statement = null;
	        	}
			}
	        LOGGER.info( "- vHeadervalue => " + vHeadervalue );
	       
	       //Retrieving Period_name
	       PreparedStatement periodStatement = null;
		   QueryIterator<SQLResultRow> periodResult = null;
			try
			{
			//Select query for fetching the data.
			periodStatement = createPreparedStatement("select distinct(pl.period_descr) from CI_CAL_PERIOD_L pl where pl.fiscal_year=(select p.fiscal_year from CI_CAL_PERIOD p,ci_ft ft where ft.accounting_dt between p.begin_dt and p.end_Dt and ft.ft_id=:FTID)and pl.accounting_period= (select p.accounting_period  from CI_CAL_PERIOD p,ci_ft ft where ft.accounting_dt between p.begin_dt and p.end_Dt and ft.ft_id=:FTID) AND pl.language_cd='FRA'");
			periodStatement.bindString("FTID",ft.getId().getIdValue(),null);
			periodResult  = periodStatement.iterate();
	        while (periodResult.hasNext()) {
	            SQLResultRow rowValues = periodResult.next();
	            vPeriodName = rowValues.getString("PERIOD_DESCR");
	             LOGGER.info("Period Name::"+vPeriodName);
	        }
	        }catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(periodStatement!=null)
				{
				periodStatement.close();
				periodStatement = null;
				}
				if(periodResult!=null)
				{
				periodResult.close();
				periodResult = null;
				}
			}
	       LOGGER.info("vPeriodName"+vPeriodName);
			
			//recuperation du code gestion
		try {
			vCodeGestion = this.getCodeGestionValue();

		} catch (Exception vException) {
			LOGGER.error("Une erreur durant la recuperation du code gestion", vException);
			return;
		}
			
			//Setting the site benecifiary.
			try{
			if(ft.getFinancialTransactionType().isAdjustment()||ft.getFinancialTransactionType().isAdjustmentCancellation())
			vsitebeneficiary = "0";
			else if(ft.getFinancialTransactionType().isPaySegment()|| ft.getFinancialTransactionType().isPayCancellation())
			{
			if(vCodeSite.equals(getBankSiteValue()))
			{
				vsitebeneficiary = "0";
			}else
			{
				vsitebeneficiary = getBankSiteValue();
			}
			}else{
				LOGGER.info("This is for future purpose for Benefits");
			}
			}catch(Exception e)
			{
				LOGGER.info("Exception while getting the site beneficiary");
			}
			
	        try {
		      	this.setGlAccount();
	        	
	            } catch ( final Exception vException ) {
		           LOGGER.error( "Une erreur durant la concatenation des donnees", vException );
		           return;
	            }
	        
	        LOGGER.info( "INVOKE - END" );
	}
			
	private void getCodeSite(String account) {
		PreparedStatement selectPrepareStatement = null;
		QueryIterator<SQLResultRow> selectresult = null;
		String perId = account;
		
		ServiceAgreementType ftType1 = ft.getServiceAgreement().getServiceAgreementType();
		String cssObgType = this.getCssObligationList();
		String cssArrList[] = cssObgType.split(",");
		List<String> cssList = Arrays.asList(cssArrList);
		String query;
		try {
			if (cssList.contains(ftType1.fetchIdSaType().trim()))
			// if(ftType1.fetchIdSaType().trim().equals("O-EPF")
			// ||ftType.fetchIdSaType().trim().equals("O-EATMP"))
			{
				query = "SELECT AGEN.CODE_AGENCE FROM CI_PER_CHAR PERCHAR,CMAGENCE AGEN WHERE PERCHAR.CHAR_TYPE_CD IN ('CM-CSAG') AND TRIM(AGEN.AGENCE) = TRIM(PERCHAR.ADHOC_CHAR_VAL) AND PERCHAR.PER_ID =:PER_ID";
			} else {
				query = "SELECT AGEN.CODE_AGENCE FROM CI_PER_CHAR PERCHAR,CMAGENCE AGEN WHERE PERCHAR.CHAR_TYPE_CD IN ('CM-IPAG') AND TRIM(AGEN.AGENCE) = TRIM(PERCHAR.ADHOC_CHAR_VAL) AND PERCHAR.PER_ID =:PER_ID";
			}
			selectPrepareStatement = createPreparedStatement(query, "SELECT");
			selectPrepareStatement.bindString("PER_ID", perId, null);
			selectresult = selectPrepareStatement.iterate();

			while (selectresult.hasNext()) {
				SQLResultRow row = selectresult.next();
				if (row != null) {
					vCodeSite = row.getString("CODE_AGENCE").trim();
				}
			}

			LOGGER.info("Code Site : " + vCodeSite);
		} catch (final Exception vException) {
			LOGGER.error("Une erreur durant la recuperation du code site", vException);
		} finally {
			if (selectPrepareStatement != null) {
				selectPrepareStatement.close();
				selectPrepareStatement = null;
			}
			if (selectresult != null) {
				selectresult.close();
				selectresult = null;
			}
		}
	
	}

	private String getvDateExercice() {

		PreparedStatement yearPrepareStatement = null;
		QueryIterator<SQLResultRow> yearresult = null;
		try {
			if (notNull(ft.getAccountingDate())) {
				if (ft.getFinancialTransactionType().isAdjustment() && (ft.getDebtCategoryId().getIdValue() != null
						&& ft.getDebtCategoryId().getIdValue().trim().toString().equals("COTISATION"))) {

					yearquery = "select fiscal_year from ci_cal_period where end_dt=(select end_dt from ci_sa where sa_id =(select sa_id from ci_ft where ft_id=:FtId and FT_TYPE_FLG='AD' and DEBT_CAT_CD='COTISATION'))";
					yearPrepareStatement = createPreparedStatement(yearquery);
					yearPrepareStatement.bindString("FtId", ft.getId().getIdValue(), null);
					yearresult = yearPrepareStatement.iterate();
					while (yearresult.hasNext()) {
						SQLResultRow row = yearresult.next();
						vDateExercice = row.getString("FISCAL_YEAR");
					}
				} else {
					vDateExercice = String.valueOf(ft.getAccountingDate().getYear());
				}
			}
		} catch (Exception e) {
			LOGGER.info("Exception::" + e);
		} finally {
			if (yearPrepareStatement != null) {
				yearPrepareStatement.close();
				yearPrepareStatement = null;
			}
			if (yearresult != null) {
				yearresult.close();
				yearresult = null;

			}
		}
		return vDateExercice;
	}


	private String getvCodeComptable() {

		if (notNull(glDist.getId())) {
			StringBuilder sb1 = null;
			sb1 = new StringBuilder("from GeneralLedgerDistributionEff glde ");
			sb1.append("where glde.id.distributionCode =:dst_id_value and glde.id.effectiveDate <=:curr_Date");
			Query<QueryResultRow> query = createQuery(sb1.toString(), this.getClass().getName());
			query.bindId("dst_id_value", glDist.getId());
			query.bindDate("curr_Date", getSystemDateTime().getDate());

			query.addResult("effDate", "glde.id.effectiveDate");
			query.addResult("gl_Account", "glde.glAccount");

			query.orderBy("effDate", Query.DESCENDING);
			if (query.firstRow().get("gl_Account") != null) {
				vCodeComptable = query.firstRow().getString("gl_Account").trim();
			}
		}
		return vCodeComptable;
	}


	private String getCodePrestation() {
		StringBuilder sb = null;
		sb = new StringBuilder(
				"from FinancialTransactionGeneralLedger ftgl,GeneralLedgerDistributionCodeCharacteristic char ");
		sb.append(
				"where ftgl.id.financialTransaction=:ftid and ftgl.id.glSequence=:glSeq and char.id.characteristicType=:charType  ");
		sb.append(" and ftgl.distributionCode =char.id.generalLedgerDistributionEff.id.distributionCode");
		Query<QueryResultRow> qu = createQuery(sb.toString(), this.getClass().getName());
		qu.bindId("charType", new CharacteristicType_Id("CM-PRES"));
		qu.bindBigInteger("glSeq", new BigInteger("2"));
		qu.bindId("ftid", new FinancialTransaction_Id(ft.getId().getIdValue()));
		qu.addResult("charVal", "char.characteristicValue");
		qu.addResult("adhocCharVal", "char.adhocCharacteristicValue");
		if (new CharacteristicType_Id("CM-PRES").getEntity().getCharacteristicType().isAdhocValue()) {
			System.out.println("AdhocChar Value" + qu.firstRow().getString("adhocCharVal"));
		} else {
			if (qu.firstRow() != null) {
				vCodePrestation = qu.firstRow().getString("charVal").trim();
			}
		}
		return vCodePrestation;

	}


	/**
     * Retrieve code site of bank.
     */	
	private String getBankSiteValue() throws Exception
	{
		PreparedStatement bankSiteStatement = null;
    	SQLResultRow bankSiteRow;
    	try
    	{
    	
    	bankSitequery = "SELECT CODE_AGENCE FROM cmagence "  
    					+"WHERE TYPE LIKE '%'||(select case WFM_OPT_VAL "
    		            +"when 'O-EPF' then 'CSS' "
    		            +"WHEN 'O-EATMP' then 'CSS' "
    		            +"WHEN 'O-ER' THEN 'IPRES' "
    		            +"END CODE"
    		            +" FROM (select WFM_OPT_VAL from ci_wfm_opt where wfm_name=\'"+this.getCodeTypeRegul().getId().getIdValue().trim().toString()+"\'" 
    		            +" and WFM_OPT_VAL in (\'"+ft.getServiceAgreement().getServiceAgreementType().fetchIdSaType().trim().toString()+"\'"
    		            +")))||'%' AND AGENCE LIKE '%'||(select   branch_id from ci_ft a, ci_pay b, ci_pay_tndr c, ci_tndr_ctl d, ci_tndr_srce e, ci_bank_account f "
    		+"where a.ft_type_flg in('PS','PX') "
    		+"and a.parent_id=b.pay_id "
    		+"and b.pay_event_id=c.pay_event_id "
    		+"and c.tndr_ctl_id =d.tndr_ctl_id "
    		+"and d.tndr_source_cd=e.tndr_source_cd "
    		+"and e.bank_acct_key=f.bank_acct_key "
    		+"and a.ft_id =\'"+ft.getId().getIdValue().toString()+"\'"+")||'%'" ;

    	bankSiteStatement = createPreparedStatement(bankSitequery);
    	bankSiteRow = bankSiteStatement.firstRow();
    		if(bankSiteRow!=null)
    		{
    			bankSiteValue = bankSiteRow.getString("CODE_AGENCE");
    		}
    	}catch(Exception e)
    	{
    		LOGGER.info("Exception ::"+ e);
    	}finally
    	{
    		if(bankSiteStatement!=null)
    		{
    		bankSiteStatement.close();
    		bankSiteStatement = null;
    		}
    	}
    	return bankSiteValue;	
		
	}
	
	/**
     * Retrieve code branche
     */
    private String getCodeBrancheValue() throws Exception {
        long time = LOGGER.infoStart( "getCodeBranche - START" );

	        String vCodeBranche = null;
	        sa = ft.getServiceAgreement();//obligation
	        ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
           
	        // On cherche le code branche dans une feature config a partir du type de regul
            final BigInteger vSeq = getSequence(this.getCodeTypeRegul(),ftType.fetchIdSaType().trim(),this.getTypeRegulBrancheOption());
          
            vCodeBranche = searchValue( this.getCodeTypeRegul(), vSeq, this.getTypeRegulBrancheOption() );
	        LOGGER.info( "getCodeBranche - END (" + LOGGER.getElapsedTime( time ) + ") | CodeBranche: " + vCodeBranche );
	        return vCodeBranche;
    }

    /**
     * Retrieve Cotisation Value for Adjustments
     */ 
  private String getCotisation(String featureConfig,String branchOption,String CodeBranche,String optionType) throws Exception
    {
    	PreparedStatement cotisationStatement = null;
    	SQLResultRow cotisationrow = null;
    	try
    	{
    		String Query = "select WFM_OPT_VAL from CI_WFM_OPT where "
	                + "seq_num=(select seq_num from CI_WFM_OPT where "
	                + "WFM_NAME=\'"+featureConfig+"\' "
	                + "and EXT_OPT_TYPE=\'"+branchOption+"\' and WFM_OPT_VAL=\'"+CodeBranche+"\') "
	                + "and EXT_OPT_TYPE=\'"+optionType+"\'";	
    		cotisationStatement = createPreparedStatement(Query);
    		cotisationrow = cotisationStatement.firstRow();
    		if(cotisationrow != null){
      		  cotisationValue = cotisationrow.getString("WFM_OPT_VAL");
      	 }
    	
    	}catch(Exception e)
    	{
    		LOGGER.info("Exception ::"+e);
    	}finally
    	{
    		if(cotisationStatement!=null)
    		{
    		cotisationStatement.close();
    		cotisationStatement = null;
    		}
    	}
    	return cotisationValue;
    	
    }
    
    /**
     * Retrieve Bank code
     */
    private String getBankCode(String ftid) throws Exception
    {
    	PreparedStatement bankCodeStatement = null;
    	SQLResultRow bankCodeRow;
    	try
    	{
    	
    	bankBranchquery = "select distinct DFI_ID_NUM from ci_ft a, ci_pay b, ci_pay_tndr c, ci_tndr_ctl d, ci_tndr_srce e, ci_bank_account f "
    			+"where a.ft_type_flg in('PS','PX') "
    			+"and a.parent_id=b.pay_id "
    			+"and b.pay_event_id=c.pay_event_id "
    			+"and c.tndr_ctl_id =d.tndr_ctl_id "
    			+"and d.tndr_source_cd=e.tndr_source_cd "
    			+"and e.bank_acct_key=f.bank_acct_key "
    			+"and a.ft_id =\'"+ftid+"\'";

    		bankCodeStatement = createPreparedStatement(bankBranchquery);
    		bankCodeRow = bankCodeStatement.firstRow();
    		if(bankCodeRow!=null)
    		{
    			bankCode = bankCodeRow.getString("DFI_ID_NUM");
    		}
    	}catch(Exception e)
    	{
    		LOGGER.info("Exception ::"+ e);
    	}finally
    	{
    		if(bankCodeStatement!=null)
    		{
    		bankCodeStatement.close();
    		bankCodeStatement = null;
    		}
    	}
    	return bankCode;
    }
    
    /**
     * Retrieve BankjournalCode
     */
    private String getBankjournalCode(String featureConfig,String branOption,String branValue,String siteOption,String siteValue,String codejournal) throws Exception
    {
    	PreparedStatement bankJournalStatement = null;
    	SQLResultRow bankJournalrow;
    	try
    	{
    		bankJournalquery ="select * from CI_WFM_OPT where seq_num="
				     +"(SELECT table_1.SEQ_NUM FROM(SELECT SEQ_NUM FROM CI_WFM_OPT WHERE WFM_NAME=\'"+featureConfig+"\' and  EXT_OPT_TYPE=\'"+branOption+"\' and WFM_OPT_VAL=\'"+branValue+"\') table_1 "
				     +"INNER JOIN "
				     +"(SELECT SEQ_NUM FROM CI_WFM_OPT WHERE WFM_NAME=\'"+featureConfig+"\' and  EXT_OPT_TYPE=\'"+siteOption+"\' and WFM_OPT_VAL=\'"+siteValue+"\') table_2 "
				     +"ON table_2.SEQ_NUM = table_1.SEQ_NUM) and EXT_OPT_TYPE=\'"+codejournal+"\'";	

    				
    		bankJournalStatement = createPreparedStatement(bankJournalquery);
    		bankJournalrow = bankJournalStatement.firstRow();
    		if(bankJournalrow!=null)
    		{
    			bankJournalCode = bankJournalrow.getString("WFM_OPT_VAL");
    		
    		}
    	}catch(Exception e)
    	{
    		LOGGER.info("Exception ::"+ e);
    	}finally
    	{
    		if(bankJournalStatement!=null)
    		{
    		bankJournalStatement.close();
    		bankJournalrow = null;
    		}
    	}
    	return bankJournalCode;
    }
    
    
	/**
     * Recupere le code gestion
     */
    private String getCodeGestionValue() throws Exception {
        	long time = LOGGER.infoStart( "getCodeGestion - START" );

	        String vCodeGestion = null;
	        final FinancialTransactionTypeLookup vFTType = ft.getFinancialTransactionType();
	        
	        ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
	      
            // On cherche le code gestion dans une feature config a partir du feature config
            final BigInteger vSeq =getSequence( this.getCodeTypeRegul(),ftType.fetchIdSaType(),this.getTypeRegulGestionOption().trim() );
           
            vCodeGestion = searchValue( this.getCodeTypeRegul(), vSeq, this.getTypeRegulGestionOption() );

	        LOGGER.info( "getCodeGestion - END (" + LOGGER.getElapsedTime( time ) + ") | CodeGestion: " + vCodeGestion );
	        return vCodeGestion;
    }

    
    /**
     * Retourne la valeur de la carac du compte
     */
    
    public  String getAccountCharacteristicValue( Set<AccountCharacteristic> pAccountCharacteristic,
                                                        CharacteristicType pCharacteristicType ) {
        LOGGER.info( "getAccountCharacteristicValue + ; Set" );

        LOGGER.info( "pAccountCharacteristic " + pCharacteristicType );
        LOGGER.info( "pCharacteristicType " + pCharacteristicType );
        if ( pCharacteristicType.getCharacteristicType().isPredefinedValue() ) {
            for ( final AccountCharacteristic vAccountCharacteristic : pAccountCharacteristic ) {
                LOGGER.info( "vAccountCharacteristic " + vAccountCharacteristic );
                if ( pCharacteristicType.getId().getIdValue().trim().equals( vAccountCharacteristic.fetchIdCharacteristicType().getId().getIdValue().trim() ) ) {
                    LOGGER.info( "getAccountCharacteristicValue -" );

                    return vAccountCharacteristic.getCharacteristicValue().trim();
                }
            }
        } else if ( pCharacteristicType.getCharacteristicType().isAdhocValue() ) {
            for ( final AccountCharacteristic vAccountCharacteristic : pAccountCharacteristic ) {
                if ( pCharacteristicType.getId().getIdValue().trim().equals( vAccountCharacteristic.fetchIdCharacteristicType().getId().getIdValue().trim() ) ) {
                    LOGGER.info( "vAccountCharacteristic " + vAccountCharacteristic );

                    LOGGER.info( "getAccountCharacteristicValue -" );

                    return vAccountCharacteristic.getAdhocCharacteristicValue().trim();
                }
            }
        } else if ( pCharacteristicType.getCharacteristicType().isForeignKeyValue() ) {
            for ( final AccountCharacteristic vAccountCharacteristic : pAccountCharacteristic ) {
                LOGGER.info( "vAccountCharacteristic " + vAccountCharacteristic );
                if ( pCharacteristicType.getId().getIdValue().trim().equals( vAccountCharacteristic.fetchIdCharacteristicType().getId().getIdValue().trim() ) ) {
                    if ( !"".equals( vAccountCharacteristic.getCharacteristicValueForeignKey1() ) ) {
                        LOGGER.info( " 123 " + vAccountCharacteristic.getCharacteristicValueForeignKey1().trim() );

                        LOGGER.info( "getAccountCharacteristicValue -" );

                        return vAccountCharacteristic.getCharacteristicValueForeignKey1().trim();
                    } else if ( !"".equals( vAccountCharacteristic.getCharacteristicValueFK2().trim() ) ) {
                        LOGGER.info( "getAccountCharacteristicValue -" );

                        return vAccountCharacteristic.getCharacteristicValueFK2().trim();
                    } else if ( !"".equals( vAccountCharacteristic.getCharacteristicValueFK3().trim() ) ) {
                        LOGGER.info( "getAccountCharacteristicValue -" );

                        return vAccountCharacteristic.getCharacteristicValueFK3().trim();
                    } else if ( !"".equals( vAccountCharacteristic.getCharacteristicValueFk4().trim() ) ) {
                        LOGGER.info( "getAccountCharacteristicValue -" );

                        return vAccountCharacteristic.getCharacteristicValueFk4().trim();
                    } else if ( !"".equals( vAccountCharacteristic.getCharacteristicValueFK5().trim() ) ) {
                        LOGGER.info( "getAccountCharacteristicValue -" );

                        return vAccountCharacteristic.getCharacteristicValueFK5().trim();
                    }
                }
            }
        }

        LOGGER.info( "getAccountCharacteristicValue -" );

        return null;
    }

	
	private void setGlAccount() {
					long time =
					LOGGER.infoStart( "setGlAccount - START | clé comptable: Cle comptable | vCotisation: "+vCotisation + "| vHeadervalue:"+vHeadervalue +"| vPeriodName:"+vPeriodName+"| vCodeBranche:"+vCodeBranche
					         + " | vCodeGestion:" + vCodeGestion + " | vCodeSite:" + vCodeSite + " | vCodeComptable:"
					         + vCodeComptable +" | vCentreCoutIpres:"+vCentreCoutIpres+" | vCodePrestation:"+vCodePrestation+" | vDateExercice:"+vDateExercice);
					
					final StringBuffer vGlAccountInfos = new StringBuffer();
					if(vCotisation!=null)
					{
					vGlAccountInfos.append(vCotisation);//user_je_category_name
					}
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vHeadervalue,12));//je_header_id
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vPeriodName,6));//period_Name
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(sequence);
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCodeBranche,2));//code branch seg1
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCodeGestion,2));//code gestion seg2
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCodeSite,2));//code site seg 3
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCodeComptable,6));// seg 4
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCentreCoutIpres,8));// seg 5
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCodePrestation,6));//seg 6
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vDateExercice,4));//seg 7
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(0); //seg 8 project
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(rPad(vCodeBranche,2));//seg 9
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(vsitebeneficiary);// seg 10
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(0);//seg 11
					vGlAccountInfos.append(".");
					vGlAccountInfos.append(0);//seg 12
					
					
					this.setGlAccount( vGlAccountInfos.toString() );
					glAccountOutput=vGlAccountInfos.toString();
					LOGGER.info( "setGlAccount - END (" + LOGGER.getElapsedTime( getProcessDateTime().getTime().getSeconds() ) + ") | cle comptable : " + this.getGlAccount() );
				}
	
    @SuppressWarnings("deprecation")
	public BigInteger getSequence( FeatureConfiguration pFeatureConfiguration, String pValue, String pTypeOption ) {
        LOGGER.info( "+ getSequence => " + pFeatureConfiguration + " | String pValue : " + pValue + " | pTypeOption : " + pTypeOption );
        final FeatureConfigurationOptions vFeatureConfigurationOption = pFeatureConfiguration.getOptions();
         
        LOGGER.info( "Options => " + vFeatureConfigurationOption );
        BigInteger vNumSequence = null;
        

        if ( pValue != null ) {
            pValue = pValue.trim();
        }
        PreparedStatement selectPrepareStatement = null;
        QueryIterator<SQLResultRow> selectresult = null;
        try{
        	selectPrepareStatement = createPreparedStatement("select SEQ_NUM from CI_WFM_OPT where seq_num = (SELECT SEQ_NUM from CI_WFM_OPT where WFM_OPT_VAL=:PVALUE and WFM_NAME=:FEATURE_NAME) and EXT_OPT_TYPE=:OPTION_TYPE");
        	selectPrepareStatement.bindString("FEATURE_NAME", pFeatureConfiguration.getId().getIdValue(), null);
        	selectPrepareStatement.bindString("PVALUE", pValue, null);
        	selectPrepareStatement.bindString("OPTION_TYPE", pTypeOption, null);
        	selectresult = selectPrepareStatement.iterate();
        	while (selectresult.hasNext()) {
    			SQLResultRow row = selectresult.next();	
    			vNumSequence =BigInteger.valueOf(Long.valueOf(row.getString("SEQ_NUM")));
    			}
        	
        }catch(Exception e)
        {
        	LOGGER.info( "Error fetching the Sequence" +e );
        }finally
		{
        	selectPrepareStatement.close();
        	selectPrepareStatement = null;
        	selectresult.close();
			selectresult = null;
		}
        
       LOGGER.info( "- getSequence => " + vNumSequence );
        return vNumSequence;
    }
    
    public  String searchValue( FeatureConfiguration pFeatureConfiguration, BigInteger pSequence, String pTypeOption ) {
        LOGGER.info( "+ searchValue => " + pFeatureConfiguration + " | sequence : " + pSequence + " | type option : " + pTypeOption );

        String vSearchValue = null;
        if ( pFeatureConfiguration != null ) {
        	
        	
            final FeatureConfigurationOptions vFeatureConfigurationOption = pFeatureConfiguration.getOptions();
          
            for ( final FeatureConfigurationOption vFeatureConfigurat : vFeatureConfigurationOption ) 
            {	
                if ( vFeatureConfigurat.fetchIdOptionType().equals( pTypeOption.trim() )
                                && vFeatureConfigurat.fetchIdSequence().equals( pSequence ) )
                {
                    vSearchValue = vFeatureConfigurat.getValue();
                    break;
                }
            }
        }
        LOGGER.info( "- searchValue => valeur recherche : " + vSearchValue );
        return vSearchValue;
    }
	
    //Pour formater les chaines de charactere
	public  String rPad( String pChaine, int pLongueur ) {
	       if ( pChaine == null ) {
	           pChaine = "";
	       }
	       if ( pChaine.length() >= pLongueur ) {
	           return pChaine.substring( 0, pLongueur );
	       }
	       return String.format( "%-" + pLongueur + "s", pChaine ).substring( 0, pLongueur );
	}
	
	
	
	@Override
	public String getGlAccount() {
		// TODO Auto-generated method stub
		return glAccountOutput;
	}
	
	
	/**
     * @param pGlAccount the vGlAccount to set
     */
    public void setGlAccount( String pGlAccount ) {
        vGlAccount = pGlAccount;
    }

	@Override
	public void setFinancialTransaction(FinancialTransaction arg0) {
		// TODO Auto-generated method stub
		ft = arg0;
	}

	@Override
	public void setGlDistribution(GeneralLedgerDistributionCode arg0) {
		// TODO Auto-generated method stub
		glDist = arg0;
	}

	@Override
	public void setGlSequenceNumber(BigInteger arg0) {
		// TODO Auto-generated method stub
		sequence = arg0;
	}

	private String getTheContract() {
		// TODO Auto-generated method stub
		return null;
	}

	private String getTheDestination() {
		// TODO Auto-generated method stub
		return vCodePrestation;
	}

	private String getTheTrade() {
		// TODO Auto-generated method stub
		return null;
	}

}