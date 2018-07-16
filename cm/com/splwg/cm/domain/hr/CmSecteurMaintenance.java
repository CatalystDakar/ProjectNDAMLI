package com.splwg.cm.domain.hr;

/**
 * @author HP PROBOOK 450
 *
 *
@EntityPageMaintenance (entity = cmSecteur, service = CMSECTEURAF, program = CMSECTEURAF, secured = false,
 *      body = @DataElement (contents = { @ListField (name = SECTEURQUARTIER, owner = cm, property = secteurquartier)
 *                  , @ListField (name = ZONESECTEUR, owner = cm, property = zonesecteur)
 *                  , @RowField (entity = cmSecteur, name = cmSecteur)}),
 *      modules = {},
 *      lists = { @List (name = ZONESECTEUR, size = 50, service = CMZOSECTQ,
 *                  body = @DataElement (contents = { @RowField (entity = cmZoneSecteur, name = cmZoneSecteur)}))
 *            , @List (name = SECTEURQUARTIER, size = 50, service = CMSECTQUAR,
 *                  body = @DataElement (contents = { @RowField (entity = cmSecteurQuartier, name = cmSecteurQuartier)}))
 *                  })
 */
public class CmSecteurMaintenance extends CmSecteurMaintenance_Gen {

}
