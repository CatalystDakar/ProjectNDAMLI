package com.splwg.cm.domain.common.dao;

import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.GenericBusinessObject;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.tenderType.TenderType_Id;
import com.splwg.tax.domain.payment.paymentEvent.PaymentTender;
import com.splwg.tax.domain.payment.tenderControl.TenderControl_Id;

/**
 * 
 * @author ADA
 */

public class CmPaymentTenderDao extends GenericBusinessObject {

	/*
	 * Constructeur 
	 * */
	public CmPaymentTenderDao() {
        super();
    }
	
    /**
     * Recuperer les lots de reglement en fonction du tenderControlId et de tenderType
     */
    public static final String GET_PAYMENTTENDER_BY_TENDERCONTROLID_AND_TENDERTYPE =
        "SELECT tndr.PAY_TENDER_ID FROM CI_PAY_TNDR tndr "
                        + "INNER JOIN CI_PAY_EVENT evt ON evt.PAY_EVENT_ID = tndr.PAY_EVENT_ID "
                        + "Where tndr.TENDER_TYPE_CD = :pTenderTypeId " + "AND tndr.TNDR_CTL_ID = :pTenderControlId "
                        + "ORDER BY evt.CRE_DTTM ASC ";
	
   
    private static final Logger LOGGER = LoggerFactory.getLogger( CmPaymentTenderDao.class );

    
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
            createPreparedStatement( GET_PAYMENTTENDER_BY_TENDERCONTROLID_AND_TENDERTYPE );
        try {
        	LOGGER.info(" control id "+pTenderControlId);
        	LOGGER.info(" tender type id "+pTenderTypeId);
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
	
    


}
