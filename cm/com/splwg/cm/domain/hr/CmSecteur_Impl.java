package com.splwg.cm.domain.hr;

/**
 * @author HP PROBOOK 450
 *
@BusinessEntity (tableName = CMSECTEUR ,
 *      oneToManyCollections = { @Child (collectionName = secteurquartier, childTableName = CMSECTEURQUARTIER,
 *                  orderByColumnNames = { "QUARTIER"})
 *            , @Child (collectionName = zonesecteur, childTableName = CMZONESECTEUR,
 *                  orderByColumnNames = { "ZONE"})
 *            
 *                  })
 */
public class CmSecteur_Impl extends CmSecteur_Gen {

}
