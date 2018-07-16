package com.splwg.cm.domain.common.dao;

import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.cm.domain.common.dao.constants.CmPaymentTenderRequest;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.tenderType.TenderType_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentTender;
import com.splwg.tax.domain.payment.tenderControl.TenderControl_Id;

/**
 * 
 * @author ADA
 */

public class CmPaymentTenderDao extends CmGenericDao<CmPaymentTenderDao> {

	
    /**
     *le LOGGER permet d afficher dans les log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmPaymentTenderDao.class );

    /**
     * Format de date
     */
    private final DateFormat DATE_FORMAT = new DateFormat( "dd-MM-yyyy" );

	
 /**
     * 
     * @param pTenderControlId l'id TenderControl
     * @param pTenderTypeId L'id TenderType
     * @return PaymentTender
     */
    public List<PaymentTender> getPaymentTenderByTenderControlIdAndTenderType( TenderControl_Id pTenderControlId,
                                                                               TenderType_Id pTenderTypeId ) {
        LOGGER.info( "getPaymentTenderByTenderControlIdAndTenderType  TenderControl_Id : " + pTenderControlId + " | TenderType_Id : "
                        + pTenderTypeId );
        List<PaymentTender> vPaymentTenders = new ArrayList<PaymentTender>();

        final PreparedStatement vPrepareStatement =
            createPreparedStatement( CmPaymentTenderRequest.GET_PAYMENTTENDER_BY_TENDERCONTROLID_AND_TENDERTYPE );
        try {
            vPrepareStatement.bindId( "pTenderControlId", pTenderControlId );
            vPrepareStatement.bindId( "pTenderTypeId", pTenderTypeId );
            List<SQLResultRow> vResultRows = vPrepareStatement.list();
            for ( SQLResultRow vRow : vResultRows ) {
                vPaymentTenders.add( vRow.getEntity( "PAY_TENDER_ID", PaymentTender.class ) );

            }
        } catch ( Exception vException ) {
            LOGGER.error( "ERROR ", vException );
        } finally {
            vPrepareStatement.close();
        }
        return vPaymentTenders;
    }
	
    /**
     * Constructeur
     */
    public CmPaymentTenderDao() {
        super( CmPaymentTenderDao.class );
    }


}
