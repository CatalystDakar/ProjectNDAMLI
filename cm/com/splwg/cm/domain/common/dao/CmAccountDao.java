package com.splwg.cm.domain.common.dao;


import com.splwg.base.api.GenericBusinessObject;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * 
 * @author ADA
 */

public class CmAccountDao extends GenericBusinessObject {
    
    private static final Logger LOGGER = LoggerFactory.getLogger( CmAccountDao.class );

    
    /**
     * Recuperation du compte principal via Id de dcompte
     */
    public static final String GET_MAIN_ACCOUNT_BY_PERSONID = " select ACCT_ID FROM ci_acct a "
                    + " WHERE EXISTS (SELECT ap.ACCT_ID FROM CI_ACCT_PER ap "
                    + " WHERE ap.per_id = :pPersonId AND ap.main_cust_sw='"+CmConstants.YES+"' "
                    + " AND a.ACCT_ID = ap.ACCT_ID) ";

    /**
     * Constructeur
     */
    public CmAccountDao() {
        super();
    }
	
	 /**
     * Retourne le compte principal via Id du personne
     * 
     * @param pPersonId identifiant de l'acteur
     * @return le compte 
     */
    public Account getMainAccountByPerId( Person_Id pPersonId ) {

        String vQuery = GET_MAIN_ACCOUNT_BY_PERSONID;

        vQuery = vQuery.replaceAll( ":pPersonId", pPersonId.getIdValue());
        final PreparedStatement vPreparedStatement = createPreparedStatement( vQuery );
        Account vAccount=null;
        
        try {
            final SQLResultRow vRow = vPreparedStatement.firstRow();
            if ( vRow != null && vRow.getString( "ACCT_ID" ) != null ) {
            	vAccount  = new Account_Id( vRow.getString( "ACCT_ID" ) ).getEntity();
            }
        } catch ( Exception vException ) { 
            LOGGER.error( CmConstants.EXCEPTION , vException );
        } finally {
            vPreparedStatement.close();
        }
        return vAccount;
    }





}