/**
 * 
 */
package com.splwg.cm.domain.common.webservice;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType_Id;
import com.splwg.base.domain.security.user.User_Id;
import com.splwg.base.domain.workflow.notificationExternalId.NotificationExternalId_Id;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.customerContact.CustomerContact;
import com.splwg.cm.domain.common.constant.CmConstants;
import com.splwg.cm.domain.common.utils.CmUtils;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author ADA
 *
 */
public class CmWebServiceDeclic {

    /**
     * code de la balise Message
     */
    public static final String RESPONSE_MESSAGE = "Message";

    /**
     * Code de la balise Code
     */
    public static final String RESPONSE_CODE = "Code";

    /**
     * Code de la balise Resultat
     */
    public static final String RESPONSE_RESULTAT = "Resultat";

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmWebServiceDeclic.class );

    /** code d'application dource */
    private static final String CODE_APP_SOURCE = "Y";

    /** Modèle de lettre utilisé pour le courrier */
    private static final String CD_MODELE_LETTRE = "09-099";

    /** Format date */
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );

    /**

    /**
     * Type d'erreur
     */
    public static enum ErrorTypeEnum {
        /**
         *erreur retourné en cas de timeoutException 
         */
        TIME_OUT,
        /**
         * erreur fonctionnelle/technique retournée par le webservice
         */
        ERROR,
        /**
         * pas d'erreur
         */
        NO_ERROR
    };

    /**
     * Appel le webservice DECLIC pour établir une liaison 
     * entre le contact client odyssee et le courrier coté DECLIC
     * @param pCustomerContact contact client
     * @param pAccountId id du compte lié au contact client
     * @param pUserId utilisateur lié au contact client
     * @return la reponse du webservice
     */
    public static Map<String, String> linkContactForDeclic( CustomerContact pCustomerContact, Account_Id pAccountId, User_Id pUserId ) {

        LOGGER.info( "Debut linkContactForDeclic" );

        final Map<String, String> responseMap = new HashMap<String, String>();

        //Initialisation du Business Object - CMDECLIC_BOLIAISON
        final BusinessObject_Id vBoDecLiaisonId = new BusinessObject_Id( CmConstants.CMDECLIC_BOLIAISON );

        //Création d'une instance de BO - CMDECLIC_BOLIAISON
        BusinessObjectInstance vBoDecLiaisonInstance = BusinessObjectInstance.create( vBoDecLiaisonId.getTrimmedValue() );

        //Initialisation du Outbound Message Type 
        final OutboundMessageType_Id vOutMsgTypeLiaison = new OutboundMessageType_Id( CmConstants.CMDECLIC_OMTLIAISON );

        //Initialisation du External System 
        final NotificationExternalId_Id vExternalSystemLiaison = new NotificationExternalId_Id( CmConstants.CMDECLIC_ESLIAISON );

        //Initialisation du External System et du OMT dans le BO - WS DECLIC 
        vBoDecLiaisonInstance.set( "externalSystem", vExternalSystemLiaison.getEntity() );
        vBoDecLiaisonInstance.set( "outboundMessageType", vOutMsgTypeLiaison.getEntity() );

        //On récupère le noeud nommé LinkCourrierEgreneToClientAsk qui représente l'enveloppe de requête
        final COTSInstanceNode vRequestGroupLiaison = vBoDecLiaisonInstance.getGroupFromPath( "LinkCourrierEgreneToClientAsk" );
        //On initialise les valeurs de requête
        vRequestGroupLiaison.set( "ref_courrier", pCustomerContact.getId().getTrimmedValue() );
        vRequestGroupLiaison.set( "cd_application_src", CODE_APP_SOURCE );
        vRequestGroupLiaison.set( "ref_client", "98" + pAccountId.getTrimmedValue() );
        vRequestGroupLiaison.set( "cd_modele_lettre", CD_MODELE_LETTRE );
        vRequestGroupLiaison.set( "dt_edition", FORMATTER.format( CmUtils.getJavaDate( pCustomerContact.getContactDateTime() ) ) );
        vRequestGroupLiaison.set( "login", pUserId.getTrimmedValue() );

        vBoDecLiaisonInstance = BusinessObjectDispatcher.execute( vBoDecLiaisonInstance, BusinessObjectActionLookup.constants.ADD );
        //On récupère le resultat 
        final COTSInstanceNode vResultGroupDeclic = vBoDecLiaisonInstance.getGroupFromPath( "LinkCourrierEgreneToClientAskResult" );

        responseMap.put( RESPONSE_RESULTAT, vResultGroupDeclic.getString( RESPONSE_RESULTAT ) );
        responseMap.put( RESPONSE_CODE, vResultGroupDeclic.getString( RESPONSE_CODE ) );
        responseMap.put( RESPONSE_MESSAGE, vResultGroupDeclic.getString( RESPONSE_MESSAGE ) );
        LOGGER.info( "Fin linkContactForDeclic :" + responseMap.toString() );
        return responseMap;
    }
}
