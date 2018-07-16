package com.splwg.cm.domain.common.dao.constants;

/**
 * @author ADA
 */
public class CmPersonRequest {

    /**
     *  Recherche l acteur principal d un compte
     */
    public static final String GET_MAIN_PER_BY_ACCT =
        "select acct_per.per_id "
                        + "from ci_acct_per acct_per "
                        + "where acct_per.acct_id = :pAccountId "
                        + "and ( acct_per.acct_rel_type_cd = :pMainAcctPer or acct_per.acct_rel_type_cd = :pPrincAcctPer ) "
                        + "AND ROWNUM = 1";
    
}
