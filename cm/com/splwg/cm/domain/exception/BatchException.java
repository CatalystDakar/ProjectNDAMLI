package com.splwg.cm.domain.exception;

/**
 * 
 * @author ADA
 */
public class BatchException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String msg;

    /**
     * 
     * @param pMsg message
     */
    public BatchException( String pMsg ) {
        super();
        this.msg = pMsg;
    }

    /**
     * @return the msg
     */
    @Override
    public String getMessage() {
        return this.msg;
    }
}
