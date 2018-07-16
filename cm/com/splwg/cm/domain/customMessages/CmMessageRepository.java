package com.splwg.cm.domain.customMessages;

import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.shared.common.ServerMessage;

public class CmMessageRepository extends CmMessages{
//  variable/object declaration
   private static CmMessageRepository instance;

   private static CmMessageRepository getInstance() {
       if (instance == null) instance = new CmMessageRepository();
       return instance;
   }
   

//Ad'hoc Characteristic Value Format Not Valid
   public static ServerMessage cmAdhocCharacteristicValueFormatIsInvalid(String adhocValue){
       
       MessageParameters parms = new MessageParameters();        
       parms.addField(adhocValue);
       return getInstance().getMessage(CM_ADHOC_CHARACTERISTIC_VALUE_FORMAT_IS_INVALID, parms);
   }
}
