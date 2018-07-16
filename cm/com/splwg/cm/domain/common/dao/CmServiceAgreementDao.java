package com.splwg.cm.domain.common.dao;


import java.util.List;


import com.splwg.base.api.Query;
import com.splwg.cm.domain.common.dao.constants.CmServiceAgreementServicePointRequest;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;

/**
 * @author ADA
 *
 */
public class CmServiceAgreementDao extends CmGenericDao<CmServiceAgreementDao> {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmServiceAgreementDao.class );

    /**
     * Constructeur
     */
    public CmServiceAgreementDao() {
        super( CmServiceAgreementDao.class );
    }
/**
     * Recuperer la liste des types SA via type SA
     *
     * @param pServiceAgreementType ServiceAgreementType
     * @param pAccountId ServiceAgreementType
     * @return Liste
     */
    public List<ServiceAgreement> getSABySATypeByAccount( ServiceAgreementType pServiceAgreementType, Account_Id pAccountId ) {
        LOGGER.info( "+ getSABySATypeByAccount  => " + pServiceAgreementType + " | " + pAccountId );

        final Query<ServiceAgreement> vSAQuery = this.createQuery( CmServiceAgreementServicePointRequest.GET_SA_BY_SATYPEACCT );
        vSAQuery.bindEntity( "type", pServiceAgreementType );
        vSAQuery.bindId( "accountId", pAccountId );
        final List<ServiceAgreement> vServiceAgreements = vSAQuery.list();

        LOGGER.info( "- getSABySATypeByAccount => size : " + vServiceAgreements.size() );
        return vServiceAgreements;
    }

}