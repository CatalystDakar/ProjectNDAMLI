package com.splwg.cm.domain.common.dao.constants;

/**
 * 
 * @author ADA
 */
public class CmDepositControlRequest {
    //@formatter:off
    /**
     * Recuperer le controle de remise en banque en fonction  du l userID
     */
    public static final String  GET_DEPOSITCONTROLE_BY_USERID = 
            "FROM DepositControl AS dc " +
            "WHERE dc.userId = :pUserId";
    
    public static final String GET_DEPOSITECONTROLE_ELIGIBLE_TO_PRINT_PDF = 
                 new StringBuilder()
                 .append( " SELECT ci_dep_ctl.dep_ctl_id " )
                 .append( " FROM ci_tndr_ctl " )
                 .append( " JOIN ci_tndr_srce " )
                 .append( " ON ci_tndr_ctl.tndr_source_cd = ci_tndr_srce.tndr_source_cd " )
                 .append( " JOIN ci_dep_ctl "  )
                 .append( " ON ci_tndr_ctl.dep_ctl_id = ci_dep_ctl.dep_ctl_id " )
                 .append( "AND ci_tndr_ctl.tndr_ctl_st_flg = '30'  " )
                 .append( "AND ci_dep_ctl.dep_ctl_status_flg = '30'  " )
                 .append( " WHERE  ci_tndr_srce.sa_id  =':pSaNonIden' " )
                 .append( " AND ci_dep_ctl.user_id =':pUserId' " )       
                 .append( " AND  NOT EXISTS " )
                 .append( " (SELECT * " )
                 .append( "   FROM ci_dep_ctl_char " )
                 .append( "   WHERE ci_dep_ctl_char.char_type_cd='CC-ID'  " )
                 .append( "   AND ci_dep_ctl_char.dep_ctl_id = ci_dep_ctl.dep_ctl_id ) " )
                 .append( " AND EXISTS ( " )
                 .append( " SELECT * " )
                 .append( " FROM ci_pay_tndr " )
                 .append( " WHERE ci_pay_tndr.TENDER_TYPE_CD=':tenderType'" )
                 .append( " and ci_pay_tndr.TNDR_CTL_ID = ci_tndr_ctl.TNDR_CTL_ID " )
                 .append( " ) " )
                 .append( " ORDER BY ci_dep_ctl.cre_dttm   " ).toString();
                          
                                 
                                 
                           
}
