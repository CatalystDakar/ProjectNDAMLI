package com.splwg.cm.domain.customMessages;

import com.splwg.base.domain.common.message.AbstractMessageRepository;

import sun.reflect.generics.repository.AbstractRepository;

public class CmMessages extends AbstractMessageRepository {
    //Custom Message Category Number
    public static final int MESSAGE_CATEGORY = 90000;
    
    public CmMessages(){
        super(MESSAGE_CATEGORY);
    }

//  Ad'hoc Characteristic Value Format Not valid      
public static final int CM_ADHOC_CHARACTERISTIC_VALUE_FORMAT_IS_INVALID = 1000;
}
