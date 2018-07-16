package com.splwg.cm.domain.common.utils;

import java.math.BigInteger;



import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOption;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOptions;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * 
 * @author ADA
 */
public class CmFeatureConfigurationManager {

    /**
     * Constante LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmFeatureConfigurationManager.class );


  


    /**
     * Recherche la valeur dun type option d'une configuration en fonction du numéro de séquence
     * 
     * @param pFeatureConfiguration Configuration de mutualisation de paramètre
     * @param pSequence numéro de séquence
     * @param pTypeOption type d'option
     * @return la valeur de l'option pTypeOption
     */
    public static String searchValue( FeatureConfiguration pFeatureConfiguration, BigInteger pSequence, String pTypeOption ) {
        LOGGER.info( "+ searchValue => " + pFeatureConfiguration + " | sequence : " + pSequence + " | type option : " + pTypeOption );

        String vSearchValue = null;
        if ( pFeatureConfiguration != null ) {
            final FeatureConfigurationOptions vFeatureConfigurationOption = pFeatureConfiguration.getOptions();
            for ( final FeatureConfigurationOption vFeatureConfigurat : vFeatureConfigurationOption ) {
                if ( vFeatureConfigurat.fetchIdOptionType().trim().equals( pTypeOption.trim() )
                                && vFeatureConfigurat.fetchIdSequence().equals( pSequence ) ) {
                    vSearchValue = vFeatureConfigurat.getValue();
                    break;
                }
            }
        }

        LOGGER.info( "- searchValue => valeur recherchée : " + vSearchValue );
        return vSearchValue;
    }



}
