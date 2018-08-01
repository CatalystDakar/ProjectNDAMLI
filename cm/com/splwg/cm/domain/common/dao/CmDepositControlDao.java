package com.splwg.cm.domain.common.dao;

import com.splwg.base.api.GenericBusinessObject;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.security.user.User_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.tax.domain.payment.depositControl.DepositControl;
import com.splwg.tax.domain.payment.depositControl.DepositControl_Id;

/**
 * 
 * @author ADA
 */
public class CmDepositControlDao extends GenericBusinessObject {

    
    public static final String GET_DEPOSITECONTROLE_ELIGIBLE_TO_PRINT_PDF = 
                 new StringBuilder()
                 .append( " SELECT ci_tndr_ctl.dep_ctl_id " )
                 .append( " FROM ci_tndr_ctl " )
                 .append( " JOIN ci_tndr_srce " )
                 .append( " ON ci_tndr_ctl.tndr_source_cd = ci_tndr_srce.tndr_source_cd " )
                 .append( " JOIN ci_dep_ctl "  )
                 .append( " ON ci_tndr_ctl.dep_ctl_id = ci_dep_ctl.dep_ctl_id " )
                 //.append( "AND ci_tndr_ctl.tndr_ctl_st_flg = '30'  " )
                 //.append( "AND ci_dep_ctl.dep_ctl_status_flg = '30'  " )
                 //.append( " WHERE  ci_dep_ctl.user_id =':pUserId' " )
                 //.append( " AND EXISTS ( " )
                 //.append( " SELECT * " )
                 //.append( " FROM ci_pay_tndr " )
                 //.append( " WHERE ci_pay_tndr.TENDER_TYPE_CD=':tenderType'" )
                 //.append( " and ci_pay_tndr.TNDR_CTL_ID = ci_tndr_ctl.TNDR_CTL_ID " )
                 //.append( " ) " )
                 .append( " ORDER BY ci_dep_ctl.CRE_DTTM   " ).toString();
    
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmDepositControlDao.class );
    
    /*
     * Message
     * */    
    private static String vMultipleDepositControl="Plusieurs controles de lot de réglement trouvés";


    /**
     * Constructeur
     */
    public CmDepositControlDao() {
        super();
    }

    /**
     * Recherche du Controle de remise en banque elligible pour inpression de dpf
     * 
     * @param pUserId User_Id
     * @param pTenderType String
     * @return DepositControl     
     */
    public DepositControl searchDepositControlEligibleToPrintPdf( final User_Id pUserId,   String pTenderType ) {
        LOGGER.info( "START - searchDepositControlEligibleToPrintPdf( final User_Id pUserId : " + pUserId
                       + " , String pTenderType : " + pTenderType + ")" );

        String vQuery = GET_DEPOSITECONTROLE_ELIGIBLE_TO_PRINT_PDF;

        //TODO PERF Presence de replace
        vQuery = vQuery.replaceAll( ":pUserId", pUserId.getTrimmedValue() );
        vQuery = vQuery.replaceAll( ":tenderType", pTenderType );

        DepositControl vDepositControl = null;
        final PreparedStatement vPreparedStatement = createPreparedStatement( vQuery );

        if(vPreparedStatement.list().size()>1)
        	LOGGER.error(vMultipleDepositControl);
        try {
            final SQLResultRow vRow = vPreparedStatement.firstRow();
            if ( vRow != null && vRow.getString( "DEP_CTL_ID" ) != null ) {
                vDepositControl = new DepositControl_Id( vRow.getString( "DEP_CTL_ID" ) ).getEntity();
            }
        } catch ( Exception vException ) { 
            LOGGER.error( CmConstants.EXCEPTION , vException );
        } finally {
            vPreparedStatement.close();
        }
        
        return vDepositControl;
    }
}
