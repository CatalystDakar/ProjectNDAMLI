package com.splwg.cm.domain.common.dao;


import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.GenericBusinessObject;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;

/**
 * @author ADA
 *
 */
public class CmServiceAgreementDao extends GenericBusinessObject {


	/*public static final String GET_SA_BY_SATYPEACCT = "select * FROM CI_SA sa "
                    + " WHERE sa.SA_TYPE_CD = :type AND sa.ACCT_ID = :accountId ";*/
	public static final String GET_SA_BY_SATYPEACCT = " select SA_ID FROM CI_SA sa "
            + " WHERE  sa.ACCT_ID = '8085194566' ";

	     
	private static final Logger LOGGER = LoggerFactory.getLogger( CmServiceAgreementDao.class );

	
    public CmServiceAgreementDao() {
        super();
    }




    public List<ServiceAgreement> getSABySATypeByAccount( Account_Id pAccountId ) {
        LOGGER.info( "+ getSABySATypeByAccount  => SA_TYPE | " + pAccountId );

        final List<ServiceAgreement> vServiceAgreements =new ArrayList<ServiceAgreement>();
        String vQuery = GET_SA_BY_SATYPEACCT;

        vQuery = vQuery.replaceAll( ":accountId", pAccountId.getEntity().getId().toString());
        
        final PreparedStatement vPreparedStatement = createPreparedStatement( vQuery );
        ServiceAgreement vSA=null;
        
        try {
	            for(SQLResultRow vRow:vPreparedStatement.list())
	            {
	            	if ( vRow != null && vRow.getString( "SA_ID" ) != null ) {
		            	vServiceAgreements.add(new ServiceAgreement_Id( vRow.getString( "SA_ID" ) ).getEntity());
		            }
	            }
        	} catch ( Exception vException ) { 
        			LOGGER.error( CmConstants.EXCEPTION , vException );
        	} finally {
            vPreparedStatement.close();
        }
       
        LOGGER.info( "- getSABySATypeByAccount => size : " + vServiceAgreements.size() );
        return vServiceAgreements;
    }
}