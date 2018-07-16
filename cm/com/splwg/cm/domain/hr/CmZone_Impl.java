package com.splwg.cm.domain.hr;

/**
 * @author HP PROBOOK 450
 *
@BusinessEntity (tableName = CMZONE ,
 *      oneToManyCollections = { @Child (collectionName = zoneagence, childTableName = CMZONEAGENCE,
 *                  orderByColumnNames = { "AGENCE"})
 *            , @Child (collectionName = zonesecteur, childTableName = CMZONESECTEUR,
 *                  orderByColumnNames = { "ZONE"})
 *            
 *                  })
 */
public class CmZone_Impl extends CmZone_Gen {

}
