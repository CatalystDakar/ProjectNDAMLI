package com.splwg.cm.domain.hr;

/**
 * @author HP PROBOOK 450
 *
@EntityPageMaintenance (entity = cmZone, service = CMZONEAF, program = CMZONEAF, secured = false,
 *      body = @DataElement (contents = { @ListField (name = ZONEAGENCE, owner = cm, property = zoneagence)
 *                  , @ListField (name = ZONESECTEUR, owner = cm, property = zonesecteur)
 *                  , @RowField (entity = cmZone, name = cmZone)}),
 *      modules = {},
 *      lists = { @List (name = ZONESECTEUR, size = 50, service = CMZOSECT,
 *                  body = @DataElement (contents = { @RowField (entity = cmZoneSecteur, name = cmZoneSecteur)}))
 *            , @List (name = ZONEAGENCE, size = 50, service = CMZOAGE,
 *                  body = @DataElement (contents = { @RowField (entity = cmZoneAgence, name = cmZoneAgence)}))
 *                  })
 */
public class CmZoneMaintenance extends CmZoneMaintenance_Gen {

}
