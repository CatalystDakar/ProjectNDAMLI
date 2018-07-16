package com.splwg.cm.domain.common.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.splwg.base.api.changehandling.CharacteristicValueInterface;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateTime;
import com.splwg.base.domain.common.characteristicType.Characteristic;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.tax.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.tax.domain.customerinfo.customerContact.CustomerContactCharacteristic;

/**
 * Classe utilitaire
 * @author $Author$
 * @version $Revision$ : $Date$
 */
public abstract class CmUtils {
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmUtils.class );








    /**
     * Recuperer la valeur d'une caracteristique en fonction de sont type
     *
     * @param pObjectCharacteristics Set
     * @param pCharacteristicType CharacteristicType
     * @return La valeur de la caracteristique au format String
     */
    public static String getValueCharacteristicByNameType( Set<?> pObjectCharacteristics, CharacteristicType pCharacteristicType ) {
        LOGGER.info( "+ getValueCharacteristicByNameType => " + pObjectCharacteristics + " | " + pCharacteristicType );

        if ( pObjectCharacteristics == null || pCharacteristicType == null ) {
            LOGGER.info( "- getValueCharacteristicByNameType => null" );

            return null;
        }

        if ( pCharacteristicType.getCharacteristicType().isPredefinedValue() ) {
            if ( "CODE-MSE".equals( pCharacteristicType.getId().getIdValue().trim() ) ) {
                return getCharacteristicLastEffectDate( pObjectCharacteristics, pCharacteristicType );
            } else {
                for ( final Object vObjectCharacteristic : pObjectCharacteristics ) {
                    final Characteristic vCharacteristic = ( Characteristic ) vObjectCharacteristic;
                    final CharacteristicValueInterface vInterface = vCharacteristic.toCharacteristicInterface();
                    if ( vInterface != null && pCharacteristicType.equals( vInterface.getCharacteristicType() ) ) {
                        LOGGER.info( "- getValueCharacteristicByNameType => " + vInterface.getCharacteristicValueString().trim() );

                        return vInterface.getCharacteristicValueString().trim();
                    }
                }
            }
        } else if ( pCharacteristicType.getCharacteristicType().isAdhocValue() ) {
            for ( final Object vObjectCharacteristic : pObjectCharacteristics ) {
                final Characteristic vCharacteristic = ( Characteristic ) vObjectCharacteristic;
                final CharacteristicValueInterface vInterface = vCharacteristic.toCharacteristicInterface();
                if ( vInterface != null && pCharacteristicType.equals( vInterface.getCharacteristicType() ) ) {
                    LOGGER.info( "- getValueCharacteristicByNameType =>" + vInterface.getAdhocCharacteristicValue().trim() );

                    return vInterface.getAdhocCharacteristicValue().trim();
                }
            }
        } else if ( pCharacteristicType.getCharacteristicType().isForeignKeyValue() ) {
            for ( final Object vObjectCharacteristic : pObjectCharacteristics ) {
                final Characteristic vCharacteristic = ( Characteristic ) vObjectCharacteristic;
                final CharacteristicValueInterface vInterface = vCharacteristic.toCharacteristicInterface();
                if ( vInterface != null && pCharacteristicType.equals( vInterface.getCharacteristicType() ) ) {
                    if ( !"".equals( vInterface.getCharacteristicValueForeignKey1().trim() ) ) {
                        LOGGER.info( "- getValueCharacteristicByNameType => " + vInterface.getCharacteristicValueForeignKey1().trim() );

                        return vInterface.getCharacteristicValueForeignKey1().trim();
                    } else if ( !"".equals( vInterface.getCharacteristicValueFK2().trim() ) ) {
                        LOGGER.info( "- getValueCharacteristicByNameType => " + vInterface.getCharacteristicValueFK2().trim() );

                        return vInterface.getCharacteristicValueFK2().trim();
                    } else if ( !"".equals( vInterface.getCharacteristicValueFK3().trim() ) ) {
                        LOGGER.info( "- getValueCharacteristicByNameType => " + vInterface.getCharacteristicValueFK3().trim() );

                        return vInterface.getCharacteristicValueFK3().trim();
                    } else if ( !"".equals( vInterface.getCharacteristicValueFk4().trim() ) ) {
                        LOGGER.info( "- getValueCharacteristicByNameType => " + vInterface.getCharacteristicValueFk4().trim() );

                        return vInterface.getCharacteristicValueFk4().trim();
                    } else if ( !"".equals( vInterface.getCharacteristicValueFK5().trim() ) ) {
                        LOGGER.info( "- getValueCharacteristicByNameType => " + vInterface.getCharacteristicValueFK5().trim() );

                        return vInterface.getCharacteristicValueFK5().trim();
                    }
                }
            }
        }

        LOGGER.info( "- getValueCharacteristicByNameType => null" );
        return null;
    }

    /**
     * Transforme une date CC&B en date java
     * @param pDate une com.splwg.base.api.datatypes.Date
     * @return une java.util.Date
     */
    public static java.util.Date getJavaDate( Date pDate ) {
        //LOGGER.info( "+ getJavaDate => " + pDate );

        java.util.Date vReturn = null;
        final Calendar vCal = Calendar.getInstance();
        vCal.set( pDate.getYear(), pDate.getMonth() - 1, pDate.getDay() );
        vReturn = vCal.getTime();

        //LOGGER.info( "- getJavaDate => " + vReturn );
        return vReturn;
    }
    
    /**
     * Transforme une DateTime CC&B en date java
     * @param pDate une com.splwg.base.api.datatypes.DateTime
     * @return une java.util.Date
     */
    public static java.util.Date getJavaDate( DateTime pDate ) {
        LOGGER.info( "getJavaDate +" );

        LOGGER.info( "Datetime : " + pDate );

        java.util.Date vReturn = null;
        final Calendar vCal = Calendar.getInstance();
        vCal.set( pDate.getYear(), pDate.getMonth() - 1, pDate.getDay(), pDate.getHours(), pDate.getMinutes(), pDate.getSeconds() );
        vReturn = vCal.getTime();

        LOGGER.info( "getJavaDate -" );

        return vReturn;
    }
    
    
    /**
    *
    * @param pObjectCharacteristics list.
    * @param pCharacteristicType caracType.
    * @return vValueChar vValueChar.
    */
   public static String getCharacteristicLastEffectDate( final Set<?> pObjectCharacteristics, final CharacteristicType pCharacteristicType ) {

       List<AccountCharacteristic> vCharactersticLastEffectDateList = new ArrayList<AccountCharacteristic>();
       AccountCharacteristic vCharactersticLastEffectDate = null;
       String vValueChar = null;
       for ( final Object vObjectCharacteristic : pObjectCharacteristics ) {
           final Characteristic vCharacteristic = ( Characteristic ) vObjectCharacteristic;
           final CharacteristicValueInterface vInterface = vCharacteristic.toCharacteristicInterface();
           if ( vInterface != null && pCharacteristicType.equals( vInterface.getCharacteristicType() ) ) {
               vCharactersticLastEffectDateList.add( ( AccountCharacteristic ) vObjectCharacteristic );
           }
       }
       if ( !vCharactersticLastEffectDateList.isEmpty() ) {
           //on récupére la valeur de la caractéristique qui a la date d'effet la plus récente.
           vCharactersticLastEffectDate = Collections.max( vCharactersticLastEffectDateList, new Comparator<AccountCharacteristic>() {
               public int compare( final AccountCharacteristic pChar1, final AccountCharacteristic pChar2 ) {
                   return pChar1.fetchIdEffectiveDate().compareTo( pChar2.fetchIdEffectiveDate() );
               }
           } );
           vValueChar = vCharactersticLastEffectDate.toCharacteristicInterface().getCharacteristicValueString().trim();
       }
       return vValueChar;
   }
    
   /**
    * Ajoute des espaces e droite pour que la chaine fasse la longueur desiree
    * @param pChaine la chaine e completer
    * @param pLongueur la longueur desiree
    * @return la chaine completee
    */
   public static String rPad( String pChaine, int pLongueur ) {
       if ( pChaine == null ) {
           pChaine = "";
       }
       if ( pChaine.length() >= pLongueur ) {
           return pChaine.substring( 0, pLongueur );
       }
       return String.format( "%-" + pLongueur + "s", pChaine ).substring( 0, pLongueur );
   }
    
   /**
    * Retourne la liste des valeurs d'une caracteristique d'un contact client
    * @param pCustomerContact le contact client
    * @param pCharType le type de caracteristique
    * @return la liste des valeurs de la caracteristique
    */
   public static List<String> searchCustomerContactCharacteristicValue( CustomerContact pCustomerContact, CharacteristicType pCharType ) {
       LOGGER.info( "searchCustomerContactCharacteristicValue +" );

       final List<String> vRes = new ArrayList<String>();
       if ( pCustomerContact.getCharacteristics() != null ) {
           final Set<CustomerContactCharacteristic> vCustomerContactChars = pCustomerContact.getCharacteristics().asSet();

           if ( vCustomerContactChars != null ) {
               for ( final CustomerContactCharacteristic vCustomerContactChar : vCustomerContactChars ) {
                   if ( vCustomerContactChar.fetchIdCharacteristicType().equals( pCharType ) ) {
                       if ( vCustomerContactChar.getCharacteristicValue() != null
                                       && !"".equals( vCustomerContactChar.getCharacteristicValue().trim() ) ) {
                           vRes.add( vCustomerContactChar.getCharacteristicValue().trim() );
                       } else if ( vCustomerContactChar.getAdhocCharacteristicValue() != null
                                       && !"".equals( vCustomerContactChar.getAdhocCharacteristicValue().trim() ) ) {
                           vRes.add( vCustomerContactChar.getAdhocCharacteristicValue().trim() );
                       } else if ( vCustomerContactChar.getCharacteristicValueFK2() != null
                                       && !"".equals( vCustomerContactChar.getCharacteristicValueFK2().trim() ) ) {
                           vRes.add( vCustomerContactChar.getCharacteristicValueFK2().trim() );
                       } else if ( vCustomerContactChar.getCharacteristicValueFK3() != null
                                       && !"".equals( vCustomerContactChar.getCharacteristicValueFK3().trim() ) ) {
                           vRes.add( vCustomerContactChar.getCharacteristicValueFK3().trim() );
                       } else if ( vCustomerContactChar.getCharacteristicValueFk4() != null
                                       && !"".equals( vCustomerContactChar.getCharacteristicValueFk4().trim() ) ) {
                           vRes.add( vCustomerContactChar.getCharacteristicValueFk4().trim() );
                       } else if ( vCustomerContactChar.getCharacteristicValueFK5() != null
                                       && !"".equals( vCustomerContactChar.getCharacteristicValueFK5().trim() ) ) {
                           vRes.add( vCustomerContactChar.getCharacteristicValueFK5().trim() );
                       } else if ( vCustomerContactChar.getCharacteristicValueForeignKey1() != null
                                       && !"".equals( vCustomerContactChar.getCharacteristicValueForeignKey1().trim() ) ) {
                           vRes.add( vCustomerContactChar.getCharacteristicValueForeignKey1().trim() );
                       }
                   }
               }
           }
       }

       LOGGER.info( "searchCustomerContactCharacteristicValue -" );

       return vRes;
   }

}