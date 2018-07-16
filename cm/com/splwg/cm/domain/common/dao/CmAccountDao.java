package com.splwg.cm.domain.common.dao;




import com.splwg.base.api.Query;

import com.splwg.cm.domain.common.dao.constants.CmAccountRequest;

import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * 
 * @author ADA
 */
public class CmAccountDao extends CmGenericDao<CmAccountDao> {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmAccountDao.class );



    

    /**
     * Constructeur
     */
    public CmAccountDao() {
        super( CmAccountDao.class );
    }
	
	    /**
     * Retourne le compte principal d'un acteur grÃ¢ce Ã  un AccountId
     * 
     * @param pPersonId identifiant de l'acteur
     * @return le compte 
     */
    public Account searchMainAccountByPersonId( Person_Id pPersonId ) {
        final long vTime = LOGGER.infoStart( "+ searchMainAccountByPersonId => " + pPersonId );

        final Query<Account> vQuery = createQuery( CmAccountRequest.GET_MAIN_ACCOUNT_BY_PERSONID );
        vQuery.bindId( "pPersonId", pPersonId );
        final Account vAccount = vQuery.firstRow();

        LOGGER.info( "- searchMainAccountByPersonId ( " + LOGGER.getElapsedTime( vTime ) + " |ms ) => " + vAccount );
        return vAccount;
    }

	/**
     * Recherche le compte dont la personne passé en paramètre est l'utilisateur principal
     * 
     * @param pPerson un acteur
     * @return le compte
     */
    public Account searchAccountByMainPerson( Person pPerson ) {
        final long vTime = LOGGER.infoStart( "+ searchAccountByMainPerson => " + pPerson );

        final Query<Account> vQuery = createQuery( CmAccountRequest.GET_PDB_BY_MAIN_PERSON );
        vQuery.bindId( "personId", pPerson.getId() );
        final Account vAccount = vQuery.firstRow();

        LOGGER.info( "- searchAccountByMainPerson ( " + LOGGER.getElapsedTime( vTime ) + " |ms ) => " + vAccount );
        return vAccount;
    }



}