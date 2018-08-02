package com.splwg.cm.domain.admin.formRule;
import java.math.BigInteger;
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
 * @author ayoub.damir
 * @AlgorithmComponent ( softParameters = { @AlgorithmSoftParameter (name = nbreCharacCodeBranch, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = nbreCharacCodeGestion, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = ctreCoutIpresCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = codeTypeRegul, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = typeRegulBrancheOption, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = typeRegulGestionOption, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = obligationType, required = true, type = string)
 *            
})
 */


public class CmGeneralLederAccountConstructionAlgComp_Impl extends
		CmGeneralLederAccountConstructionAlgComp_Gen implements
		GeneralLedgerDistributionCodeGlAccountConstructionAlgorithmSpot {

	private static final Logger LOGGER = LoggerFactory.getLogger( CmGeneralLederAccountConstructionAlgComp_Impl.class );
	
	private FinancialTransaction ft;
	private ServiceAgreement sa;
	private GeneralLedgerDistributionCode glDist;
	
	
	private String vCodeGestion="";
	private String vCodeBranche="";
	private String vDateExercice="";
	private String vCodeSite="";
	private String vCodePrestation="";
	private String vCentreCoutIpres="";
	private String vCodeComptable="";
	
	/** Cle comptable */
    private String vGlAccount;
    private String glAccountOutput="";
	
	@SuppressWarnings("deprecation")
	@Override
	public void invoke() {
		
			LOGGER.info( "INVOKE - START" );
			
			// TODO Auto-generated method stub
	    	String DEF208_PERIOD = ".";
	    	LOGGER.info( "INVOKE - START" +ft.getId().getIdValue());
			sa = ft.getServiceAgreement();

			Account account = sa.getAccount();
			ServiceAgreementType ftType=ft.getServiceAgreement().getServiceAgreementType();
			
			//recuperation du codeprestation
			StringBuilder sb = null;
			sb = new StringBuilder("from FinancialTransactionGeneralLedger ftgl,GeneralLedgerDistributionCodeCharacteristic char ");
			sb.append("where ftgl.id.financialTransaction=:ftid and ftgl.id.glSequence=:glSeq and char.id.characteristicType=:charType  ");
			sb.append(" and ftgl.distributionCode =char.id.generalLedgerDistributionEff.id.distributionCode");
			Query<QueryResultRow> qu = createQuery(sb.toString(),this.getClass().getName());
			
			qu.bindId("charType", new CharacteristicType_Id("CM-PRES"));
			qu.bindBigInteger("glSeq", new BigInteger("2"));
			qu.bindId("ftid",new FinancialTransaction_Id(ft.getId().getIdValue()));
			
			qu.addResult("charVal","char.characteristicValue");
            qu.addResult("adhocCharVal","char.adhocCharacteristicValue");
            
            if(new CharacteristicType_Id("CM-PRES").getEntity().getCharacteristicType().isAdhocValue())
            {
            	System.out.println("AdhocChar Value"+qu.firstRow().getString("adhocCharVal"));
            }
            else
            {
            	if(qu.firstRow()!=null)
            	{
            	vCodePrestation = qu.firstRow().getString("charVal").trim();
            	}
            }
            
            
			//recuperation du code comptable
            if(notNull(glDist.getId()))
			{
            	StringBuilder sb1 = null;
            	sb1 = new StringBuilder("from GeneralLedgerDistributionEff glde ");
            	sb1.append("where glde.id.distributionCode =:dst_id_value and glde.id.effectiveDate <=:curr_Date");
            	Query<QueryResultRow> query = createQuery(sb1.toString(),this.getClass().getName());
            	query.bindId("dst_id_value", glDist.getId());
            	query.bindDate("curr_Date",getSystemDateTime().getDate());
            	
            	query.addResult("effDate","glde.id.effectiveDate");
            	query.addResult("gl_Account", "glde.glAccount");
            	
            	query.orderBy("effDate",Query.DESCENDING);
            	if(query.firstRow().get("gl_Account")!=null)
            	{
            	vCodeComptable = query.firstRow().getString("gl_Account").trim();
            	}
			}
		
			LOGGER.info( "vCodeComptable : "+vCodeComptable );
			
			//recuperation de l exercice
			if(notNull(ft.getAccountingDate()))
			{
			PreparedStatement yearPrepareStatement = null;
			QueryIterator<SQLResultRow> yearresult = null;
			String yearquery;
		   if(ft.getFinancialTransactionType().isAdjustment() && ft.getDebtCategoryId().getIdValue().toString().equals("COTISATION"))//ft.getDebtCategoryId().getIdValue().toString().equals("COTISATION"))
			{
			   yearquery= "select fiscal_year from ci_cal_period where end_dt=(select end_dt from ci_sa where sa_id =(select sa_id from ci_ft where ft_id=:FtId and FT_TYPE_FLG='AD' and DEBT_CAT_CD='COTISATION'))";
			   yearPrepareStatement = createPreparedStatement(yearquery);
			   yearPrepareStatement.bindString("FtId",ft.getId().getIdValue(), null);
			   yearresult = yearPrepareStatement.iterate();
			   while (yearresult.hasNext()) {
			   SQLResultRow row = yearresult.next();	
			   vDateExercice = row.getString("FISCAL_YEAR");
				}
			}
			else
			{
				vDateExercice = String.valueOf(ft.getAccountingDate().getYear());
			}
			}
			LOGGER.info( "Exercice : "+vDateExercice );
			
			//recuperation du code site
			if(notNull(account.getMainPerson()))
			{
			PreparedStatement selectPrepareStatement = null;
			QueryIterator<SQLResultRow> selectresult = null;
			String per_id =new Person_Id(account.getMainPerson().getId().getIdValue()).getEntity().getId().getIdValue();
			
	        ServiceAgreementType ftType1=ft.getServiceAgreement().getServiceAgreementType();
	        
	        String query;
	        try{
	        if(ftType1.fetchIdSaType().trim().equals("O-EPF") ||ftType.fetchIdSaType().trim().equals("O-EATMP"))
	        {
			query= "SELECT AGEN.CODE_AGENCE FROM CI_PER_CHAR PERCHAR,CMAGENCE AGEN WHERE PERCHAR.CHAR_TYPE_CD IN ('CM-CSAG') AND TRIM(AGEN.AGENCE) = TRIM(PERCHAR.ADHOC_CHAR_VAL) AND PERCHAR.PER_ID =:PER_ID";
	        }
	        else{
	        query= "SELECT AGEN.CODE_AGENCE FROM CI_PER_CHAR PERCHAR,CMAGENCE AGEN WHERE PERCHAR.CHAR_TYPE_CD IN ('CM-IPAG') AND TRIM(AGEN.AGENCE) = TRIM(PERCHAR.ADHOC_CHAR_VAL) AND PERCHAR.PER_ID =:PER_ID";
	        }
		   selectPrepareStatement = createPreparedStatement(query);
		   selectPrepareStatement.bindString("PER_ID", per_id, null);
		   selectresult = selectPrepareStatement.iterate();
		  
			while (selectresult.hasNext()) {
			SQLResultRow row = selectresult.next();	
			vCodeSite = row.getString("CODE_AGENCE");
			}
			vCentreCoutIpres = vCodeSite;
		    LOGGER.info( "Code Site : "+vCodeSite );
		    LOGGER.info( "CenterCoutIpres : "+vCentreCoutIpres );
			}catch(final Exception vException)
			{
				 LOGGER.error( "Une erreur durant la recuperation du code site", vException );
			}
			}
		    
		    
			//recuperation du code Branche
			try {        
		     vCodeBranche = this.getCodeBrancheValue();
			
			} catch ( final Exception vException ) {
	            LOGGER.error( "Une erreur durant la recuperation du code branche", vException );
	            return;
	        }
			 
			//recuperation du code Branche
			try{
			  vCodeGestion = this.getCodeGestionValue();
				
			} catch ( final Exception vException ) {
	            LOGGER.error( "Une erreur durant la recuperation du code gestion", vException );
	            return;
	        }
			
	        try {
		      	this.setGlAccount();
	        	
	            } catch ( final Exception vException ) {
		           LOGGER.error( "Une erreur durant la concatenation des donnees", vException );
		           return;
	            }
	        
	        LOGGER.info( "INVOKE - END" );
	}
	
	/**
     * Recupere le code branche
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
					LOGGER.infoStart( "setGlAccount - START | clé comptable: Cle comptable | vCodeBranche:" + vCodeBranche
					         + " | vCodeGestion:" + vCodeGestion + " | vCodeSite:" + vCodeSite + " | vCodeComptable:"
					         + vCodeComptable +" | vCentreCoutIpres:"+vCentreCoutIpres+" | vCodePrestation:"+vCodePrestation+" | vDateExercice:"+vDateExercice);
					
					final StringBuffer vGlAccountInfos = new StringBuffer();
										
					vGlAccountInfos.append(rPad(vCodeBranche,2));
					vGlAccountInfos.append(rPad(vCodeGestion,2));
					vGlAccountInfos.append(rPad(vCodeSite,2));
					vGlAccountInfos.append(rPad(vCodeComptable,6));
					vGlAccountInfos.append(rPad(vCentreCoutIpres,2));
					vGlAccountInfos.append(rPad(vCodePrestation,6));
					vGlAccountInfos.append(rPad(vDateExercice,4));
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