package com.splwg.cm.domain.common.dao.constants;

/**
 * 
 * @author ADA
 */
public class CmPaymentTenderRequest {
    /**
     * Recuperer les lots de reglement en fonction du tenderControlId et de tenderType
     */
    public static final String GET_PAYMENTTENDER_BY_TENDERCONTROLID_AND_TENDERTYPE =
        "SELECT PAY_TENDER_ID FROM CI_PAY_TNDR tndr "
                        + "INNER JOIN CI_PAY_EVENT evt ON evt.PAY_EVENT_ID = tndr.PAY_EVENT_ID "
                        + "Where tndr.TENDER_TYPE_CD = :pTenderTypeId " + "AND tndr.TNDR_CTL_ID = :pTenderControlId "
                        + "ORDER BY evt.CRE_DTTM ASC ";

}