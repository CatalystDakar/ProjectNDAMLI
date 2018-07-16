package com.splwg.cm.domain.common.dao;

import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.security.user.User_Id;
import com.splwg.cm.domain.common.constant.CmConstants;
import com.splwg.cm.domain.common.dao.constants.CmDepositControlRequest;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.tax.domain.payment.depositControl.DepositControl;
import com.splwg.tax.domain.payment.depositControl.DepositControl_Id;

/**
 * 
 * @author ADA
 */
public class CmDepositControlDao extends CmGenericDao<CmDepositControlDao> {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmDepositControlDao.class );

    /**
     * Constructeur
     */
    public CmDepositControlDao() {
        super( CmDepositControlDao.class );
    }

    /**
     * Recherche du Controle de remise en banque éligible pour inpression de dpf
     * 
     * @param pUserId User_Id
     * @param pSaNonIden ServiceAgreement_Id
     * @param pCharacteristicType CharacteristicType
     * @param pTenderType String
     * @return DepositControl     
     */
    public DepositControl searchDepositControlEligibleToPrintPdf( final User_Id pUserId, final ServiceAgreement_Id pSaNonIden,
                                                                  CharacteristicType pCharacteristicType, String pTenderType ) {
        LOGGER.info( "START - searchDepositControlEligibleToPrintPdf( final User_Id pUserId : " + pUserId
                        + " , final ServiceAgreement_Id pSaNonIden : " + pSaNonIden + " , String pTenderType : " + pTenderType + ")" );

        String vQuery = CmDepositControlRequest.GET_DEPOSITECONTROLE_ELIGIBLE_TO_PRINT_PDF;

        //TODO PERF Presence de replace
        vQuery = vQuery.replaceAll( ":pSaNonIden", pSaNonIden.getTrimmedValue() );
        vQuery = vQuery.replaceAll( ":pUserId", pUserId.getTrimmedValue() );
        vQuery = vQuery.replaceAll( ":pCharType", pCharacteristicType.getId().getTrimmedValue() );
        vQuery = vQuery.replaceAll( ":tenderType", pTenderType );

        DepositControl vDepositControl = null;
        final PreparedStatement vPreparedStatement = createPreparedStatement( vQuery );

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
