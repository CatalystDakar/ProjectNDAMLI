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
    * Message Text: "test  message hilda"
    * @return ServerMessage
    */
    public static ServerMessage MSG_88888() {
        MessageParameters params = new MessageParameters();
        return getCommonInstance().getMessage(Messages.MSG_88888, params);
    }

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
        * Message Text: "test  message hilda"
        */
        public static final int MSG_88888 = 88888;

    }

}
