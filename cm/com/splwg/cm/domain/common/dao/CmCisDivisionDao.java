package com.splwg.cm.domain.common.dao;

import com.splwg.base.api.Query;
import com.splwg.cm.domain.common.dao.constants.CmCisDivisionRequest;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.cisDivision.CisDivision;

/**
 * @author ADA
 */
public class CmCisDivisionDao extends CmGenericDao<CmCisDivisionDao> {

    /**
     * LOGGER par defaut
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmCisDivisionDao.class );

    /**
     * Constructeur
     */
    public CmCisDivisionDao() {
        super( CmCisDivisionDao.class );
    }

    /**
     * Recherche le seul et unique CisDivision
     * 
     * @return le cis division
     */
    public CisDivision searchCisDivision() {
        final long vTime = LOGGER.infoStart( "+ searchCisDivision" );

        final Query<CisDivision> vQuery = createQuery( CmCisDivisionRequest.GET_CISDIVISION );
        final CisDivision vCisDivision = vQuery.firstRow();

        LOGGER.info( "- searchCisDivision ( " + LOGGER.getElapsedTime( vTime ) + " |ms ) => " + vCisDivision );
        return vCisDivision;
    }
}
