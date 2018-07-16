package com.splwg.cm.domain.common.dao.constants;

/**
 * This class will contain only the SQL or HQL queries for generic uses
 *  
 * @author ADA
 *
 */
public class CmGenericRequest {

    /**
     * Get Object By Id
     */
    public static String GET_CLASS_BY_ID = "FROM :pName clazz WHERE clazz.id = :pId ";

}
