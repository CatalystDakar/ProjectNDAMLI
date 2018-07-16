package com.splwg.cm.domain.common.dao.constants;

import com.splwg.cm.domain.common.constant.CmConstants;


public class CmAccountRequest {
    
    /**
     * Recherche un compte point de balance grace a  l id de la personne principale
     */
    public static final String GET_PDB_BY_MAIN_PERSON = "FROM Account a " + "WHERE EXISTS (SELECT ap.id.account.id "
                    + "FROM AccountPerson ap " + "WHERE ap.accountRelationshipType = '"
                    + CmConstants.PRINC_ACCT_PER_RELATIONSHIP + "' AND ap.id.person.id = :personId "
                    + "AND a.id = ap.id.account.id)";

    /**
     * Recuperation du compte principal d'un acteur grace a un AccountId
     */
    public static final String GET_MAIN_ACCOUNT_BY_PERSONID = "FROM Account a "
                    + "WHERE EXISTS (SELECT ap.id.account.id " + "FROM AccountPerson ap "
                    + "WHERE ap.id.person.id = :pPersonId " + "AND ap.isMainCustomer='Y' "
                    + "AND a.id = ap.id.account.id)";
}