package com.splwg.cm.domain.common.dao.constants;


/**
 * 
 * @author ADA
 */

public class CmServiceAgreementServicePointRequest {
    
    /**
     * 
     */
	     public static final String GET_SA_BY_SATYPEACCT = "FROM ServiceAgreement sa "
                    + "WHERE sa.serviceAgreementType = :type AND sa.account.id = :accountId";

}