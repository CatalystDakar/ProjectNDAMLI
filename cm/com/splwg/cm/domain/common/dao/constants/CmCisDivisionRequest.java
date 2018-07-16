package com.splwg.cm.domain.common.dao.constants;


public class CmCisDivisionRequest {
    /**
     *  Recherche le seul et unique CisDivision
     */
    public static final String GET_CISDIVISION =
        "FROM CisDivision";
    
    /**
     * Recuperer la valeur de la colonne ADHOC_CHAR_VAL via l id de la division et le type de carac
     */
    public static final String GET_ADHOCVAL_BY_DIV_AND_CHAR_TYPE_CD = 
                    "select adhoc_char_val AS VAL from CI_CIS_DIV_CHAR where cis_division = :pCisDiv and char_type_cd = :pCharType ";

}
