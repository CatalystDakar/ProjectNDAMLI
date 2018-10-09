/****************************************************************
 * PROGRAM DESCRIPTION:
 *
 * Custom Message Repository class for Message Category 90002.
 *
 * Class Generated on 2017-07-26-12.24.10. DO NOT MODIFY
 * Any change performed manually to this class will be
 * overridden by the execution of the batch job CMMSGGEN.
 *
 ****************************************************************/
package com.splwg.cm.domain.customMessages;

import com.splwg.base.domain.common.message.AbstractMessageRepository;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.shared.common.ServerMessage;

/**
 * Custom Message Repository class for Message Category 90002.
 *
 * Class Generated on 2017-07-26-12.24.10. DO NOT MODIFY
 * Any change performed manually to this class will be
 * overridden by the execution of the batch job CMMSGGEN.
*/

public class CmMessageRepository90002 extends AbstractMessageRepository {

    /**
    * Message Category Number 90002
    */
    public static final int MESSAGE_CATEGORY = 90002;

    private static CmMessageRepository90002 instance;

    public CmMessageRepository90002() {
        super(MESSAGE_CATEGORY);
    }

    private static CmMessageRepository90002 getCommonInstance() {
        if (instance == null) {
          instance = new CmMessageRepository90002();
        }
        return instance;
    }

    /**
    * Message Text: "Variable '%1' must be initialized in '%2' '%3'"
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @param param3 String Value for message parameter %3
    * @return ServerMessage
    */
    public static ServerMessage MSG_10(String param1, String param2, String param3) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        params.addRawString(param3);
        return getCommonInstance().getMessage(Messages.MSG_10, params);
    }

    /**
    * Message Text: "Invalid XPath '%1' in Form Rule '%2' from Form Type '%3'"
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @param param3 String Value for message parameter %3
    * @return ServerMessage
    */
    public static ServerMessage MSG_20(String param1, String param2, String param3) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        params.addRawString(param3);
        return getCommonInstance().getMessage(Messages.MSG_20, params);
    }

    /**
    * Message Text: "'%1' parameter value is required in Form Rule '%2' from Form Type '%3'"
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @param param3 String Value for message parameter %3
    * @return ServerMessage
    */
    public static ServerMessage MSG_21(String param1, String param2, String param3) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        params.addRawString(param3);
        return getCommonInstance().getMessage(Messages.MSG_21, params);
    }

    /**
    * Message Text: "'%1' parameter value is not allowed in Form Rule '%2' from Form Type '%3'"
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @param param3 String Value for message parameter %3
    * @return ServerMessage
    */
    public static ServerMessage MSG_22(String param1, String param2, String param3) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        params.addRawString(param3);
        return getCommonInstance().getMessage(Messages.MSG_22, params);
    }

    /**
    * Message Text: "%1 is an invalid filter name. Filter names must be in the form of filter1, filter2.. to filter25"
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_31(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_31, params);
    }

    /**
    * Message Text: "%1 is an invalid column name. Column names must be in the form of column1, column2... to column20"
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_32(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_32, params);
    }
    
    /**
     * Message Text: "The selected value %1 (%2) from Extendable Lookup %5, does not have the proper parent; it should be %3 instead of %4."
     */
    public static ServerMessage MSG_250() {
        MessageParameters params = new MessageParameters();

        return getCommonInstance().getMessage(Messages.MSG_250, params);
    }

    /**
    * Message Text: "Person Type %1 does not have a Related Transaction BO"
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_100(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_100, params);
    }

    /**
    * Message Text: "A name must be provided for the Person. First Name and Last Name must be provided for Individual persons or Business Name for Legal persons"
    * @return ServerMessage
    */
    public static ServerMessage MSG_101() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_101, params);
    }

    /**
    * Message Text: "Person: %1 does not have an account of type: %2."
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @return ServerMessage
    */
    public static ServerMessage MSG_200(String param1, String param2) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        return getCommonInstance().getMessage(Messages.MSG_200, params);
    }

    /**
    * Message Text: "File: %1 is not a file or does not exist."
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_201(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_201, params);
    }

    /**
    * Message Text: "Unable to open file: %1"
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_202(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_202, params);
    }

    /**
    * Message Text: "Unable to to open workbook for file %1"
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_203(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_203, params);
    }

    /**
    * Message Text: "There is no Identification Type associated with column %1."
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_700(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_700, params);
    }

    /**
    * Message Text: "The specified object type %1 is not valid."
    * @param param1 String Value for message parameter %1
    * @return ServerMessage
    */
    public static ServerMessage MSG_800(String param1) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        return getCommonInstance().getMessage(Messages.MSG_800, params);
    }

    /**
    * Message Text: "The specified %1 %2 does not exist."
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @return ServerMessage
    */
    public static ServerMessage MSG_801(String param1, String param2) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        return getCommonInstance().getMessage(Messages.MSG_801, params);
    }

    /**
    * Message Text: "The selected value %1 (%2) from Extendable Lookup %5, does not have the proper parent; it should be %3 instead of %4."
    * @param param1 String Value for message parameter %1
    * @param param2 String Value for message parameter %2
    * @param param3 String Value for message parameter %3
    * @param param4 String Value for message parameter %4
    * @param param5 String Value for message parameter %5
    * @return ServerMessage
    */
    public static ServerMessage MSG_802(String param1, String param2, String param3, String param4, String param5) {
        MessageParameters params = new MessageParameters();
        params.addRawString(param1);
        params.addRawString(param2);
        params.addRawString(param3);
        params.addRawString(param4);
        params.addRawString(param5);
        return getCommonInstance().getMessage(Messages.MSG_802, params);
    }
    
    
    /**
     * @return
     */
    public static ServerMessage MSG_300() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_300, params);
    }
    
    /**
     * Message Text: "test  message hilda"
     * @return ServerMessage
     */
     public static ServerMessage MSG_88888() {
         MessageParameters params = new MessageParameters();
         return getCommonInstance().getMessage(Messages.MSG_88888, params);
     }
    
    /**
     * @return
     */
    public static ServerMessage MSG_301() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_301, params);
    }
    
    /**
     * @return
     */
    public static ServerMessage MSG_401() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_401, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_402() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_402, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_403() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_403, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_404() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_404, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_405() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_405, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_406() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_406, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_407() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_407, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_408() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_408, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_409() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_409, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_410() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_410, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_411() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_411, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_412() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_412, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_413() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_413, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_414() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_414, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_415() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_415, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_416() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_416, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_417() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_417, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_418() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_418, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_419() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_419, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_420() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_420, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_421() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_421, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_422() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_422, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_423() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_423, params);
    }
    /**
     * @return
     */
    public static ServerMessage MSG_424() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_424, params);
    }
    

    /*
  	 * Author : ADA
  	 * Algorithme de création de remise en banque
  	 * */

    //Aucun controle de lot de reglement touves
  	public static ServerMessage NoDepositControleFound(String pMessage){
          
          MessageParameters parms = new MessageParameters();        
          parms.addField(pMessage);
          return getCommonInstance().getMessage(Messages.AUCUN_CONTROL_LOT_REGLEMENT, parms);
      }
  	
  	//Plusieurs controle de lot de reglement touves
  	public static ServerMessage MultipleDepositControleFound(String pMessage){
          
          MessageParameters parms = new MessageParameters();        
          parms.addField(pMessage);
          return getCommonInstance().getMessage(Messages.PLUSIEURS_CONTROL_LOT_REGLEMENT, parms);
      }
  	
  	/**
     * Message Text: "Le type de compte n’est pas adapté à un paiement non identifié "
     * Author : DEEPAK
     * @return ServerMessage
     */
     public static ServerMessage MSG_117() {
         MessageParameters params = new MessageParameters();
         return getCommonInstance().getMessage(Messages.MSG_117, params);
     }
     
     /**
      * Message Text: "Les statuts de la tax_bill et/ou de l’obligation ne sont pas au bon statut"
      * Author : DEEPAK
      * @return ServerMessage
      */
      public static ServerMessage MSG_350() {
          MessageParameters params = new MessageParameters();
          return getCommonInstance().getMessage(Messages.MSG_350, params);
      }
      
      /**
       * Message Text: "Le montant est positif. Cela signifie que vous êtes en train d’encaisser l’assuré(e). Pour payer l’assuré(e), merci de saisir un montant négatif. Etes-vous sur de poursuivre?"
       * Author : DEEPAK
       * @return ServerMessage
       */
       public static ServerMessage MSG_351() {
           MessageParameters params = new MessageParameters();
           return getCommonInstance().getMessage(Messages.MSG_351, params);
       }
  	/*
  	 * Author : ADA
  	 * Algorithme de création de remise en banque
  	 * */
  	
    public static class Messages {
        /**
        * Message Text: "Variable '%1' must be initialized in '%2' '%3'"
        */
        public static final int MSG_10 = 10;

        /**
        * Message Text: "Invalid XPath '%1' in Form Rule '%2' from Form Type '%3'"
        */
        public static final int MSG_20 = 20;

        /**
        * Message Text: "'%1' parameter value is required in Form Rule '%2' from Form Type '%3'"
        */
        public static final int MSG_21 = 21;

        /**
        * Message Text: "'%1' parameter value is not allowed in Form Rule '%2' from Form Type '%3'"
        */
        public static final int MSG_22 = 22;

        /**
        * Message Text: "%1 is an invalid filter name. Filter names must be in the form of filter1, filter2.. to filter25"
        */
        public static final int MSG_31 = 31;

        /**
        * Message Text: "%1 is an invalid column name. Column names must be in the form of column1, column2... to column20"
        */
        public static final int MSG_32 = 32;

        /**
        * Message Text: "Person Type %1 does not have a Related Transaction BO"
        */
        public static final int MSG_100 = 100;

        /**
        * Message Text: "A name must be provided for the Person. First Name and Last Name must be provided for Individual persons or Business Name for Legal persons"
        */
        public static final int MSG_101 = 101;

        /**
        * Message Text: "Person: %1 does not have an account of type: %2."
        */
        public static final int MSG_200 = 200;

        /**
        * Message Text: "File: %1 is not a file or does not exist."
        */
        public static final int MSG_201 = 201;

        /**
        * Message Text: "Unable to open file: %1"
        */
        public static final int MSG_202 = 202;

        /**
        * Message Text: "Unable to to open workbook for file %1"
        */
        public static final int MSG_203 = 203;

        /**
         * Message Text: "The selected value %1 (%2) from Extendable Lookup %5, does not have the proper parent; it should be %3 instead of %4."
         */
         public static final int MSG_250 = 250;
         
         
        /**
        * Message Text: "There is no Identification Type associated with column %1."
        */
        public static final int MSG_700 = 700;

        /**
        * Message Text: "The specified object type %1 is not valid."
        */
        public static final int MSG_800 = 800;

        /**
        * Message Text: "The specified %1 %2 does not exist."
        */
        public static final int MSG_801 = 801;

        /**
        * Message Text: "The selected value %1 (%2) from Extendable Lookup %5, does not have the proper parent; it should be %3 instead of %4."
        */
        public static final int MSG_802 = 802;

        
        /**
         * Message Text: "Les règlements partiels ne sont pas autorisés."
         */
         public static final int MSG_300 = 300;
         
         /**
          * Message Text: "Le type de compte n’est pas adapté à un paiement non identifié"
          */
          public static final int MSG_117 = 117;
          
          /**
           * Message Text: "Le type de compte n’est pas adapté à un paiement non identifié"
           */
           public static final int MSG_350 = 350;
           
           /**
            * Message Text: "Le type de compte n’est pas adapté à un paiement non identifié"
            */
            public static final int MSG_351 = 351;
         
        /**
        * Message Text: "test  message hilda"
        */
        public static final int MSG_88888 = 88888;
        public static final int MSG_301 = 301;
        public static final int MSG_401 = 401;
        public static final int MSG_402 = 402;
        public static final int MSG_403 = 403;
        public static final int MSG_404 = 404;
        public static final int MSG_405 = 405;
        public static final int MSG_406 = 406;
        public static final int MSG_407 = 407;
        public static final int MSG_408 = 408;
        public static final int MSG_409 = 409;
        public static final int MSG_410 = 410;
        public static final int MSG_411 = 411;
        public static final int MSG_412 = 412;
        public static final int MSG_413 = 413;
        public static final int MSG_414 = 414;
        public static final int MSG_415 = 415;
        public static final int MSG_416 = 416;
        public static final int MSG_417 = 417;
        public static final int MSG_418 = 418;
        public static final int MSG_419 = 419;
        public static final int MSG_420 = 420;
        public static final int MSG_421 = 421;
        public static final int MSG_422 = 422;
        public static final int MSG_423 = 423;
        public static final int MSG_424 = 424;
        
        /*
      	 * Author : ADA
      	 * Algorithme de création de remise en banque
      	 * */
        
        // Aucun controle de lot de reglements n'est trouvé      
        public static final int AUCUN_CONTROL_LOT_REGLEMENT = 30005;
        
        // Plusieurs controle de lot de reglements n'est trouvé      
        public static final int PLUSIEURS_CONTROL_LOT_REGLEMENT = 30006;
        
        /*
      	 * Author : ADA
      	 * Algorithme de création de remise en banque
      	 * */

    }

}
