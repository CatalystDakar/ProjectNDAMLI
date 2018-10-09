package com.splwg.cm.domain.customMessages;

import com.splwg.base.domain.common.message.AbstractMessageRepository;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.shared.common.ServerMessage;

public class CmMessageRepository1001 extends AbstractMessageRepository {

    /**
    * Message Category Number 1001
    */
    public static final int MESSAGE_CATEGORY = 1001;

    private static CmMessageRepository1001 instance;

    public CmMessageRepository1001() {
        super(MESSAGE_CATEGORY);
    }
    
    public static ServerMessage MSG_101() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_101, params);
    }

    public static ServerMessage MSG_103() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_103, params);
    }
    
    public static ServerMessage MSG_105(String spouseNin, String employeeNin) {
        MessageParameters params = new MessageParameters();
        params.addRawString(spouseNin);
        params.addRawString(employeeNin);
        return getCommonInstance().getMessage(Messages.MSG_105, params);
    }
    
    public static ServerMessage MSG_106(String spouseNin) {
        MessageParameters params = new MessageParameters();
        params.addRawString(spouseNin);
        return getCommonInstance().getMessage(Messages.MSG_106, params);
    }
    
    private static CmMessageRepository1001 getCommonInstance() {
        if (instance == null) {
          instance = new CmMessageRepository1001();
        }
        return instance;
    }
    
    public static class Messages {
    	
        /**
         * Message Text: "SQL Error code"
         */
         public static final int MSG_101 = 101;
         
         public static final int MSG_103 = 103;
         
         public static final int MSG_105 = 105;
         
         public static final int MSG_106 = 106;
    }

}
