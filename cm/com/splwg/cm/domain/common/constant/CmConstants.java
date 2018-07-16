package com.splwg.cm.domain.common.constant;

/**
 * Constantes generales.
 * @author ADA
 */
public class CmConstants {

	public static final String EXCEPTION = "Exception";
	
	public static final String vDateFormat = "dd/MM/yyyy";
	
	/** Type de relation "principale" en anglais entre un compte et un acteur */
    public static final String MAIN_ACCT_PER_RELATIONSHIP = "MAIN";
    
    /** Type de relation "principale" en français entre un compte et un acteur */
    public static final String PRINC_ACCT_PER_RELATIONSHIP = "PRINC";
    
    /** Identifiant du type de relation compte - acteur "point de balance" */
    public static final String ACCOUNT_PERSON_RELATIONSHIPTYPE_PBALANCE = "PB";
    
    public static final String TENDER_TYPE_CHEQUE = "CHEQ";

    public static final String TENDER_TYPE_CASH_COMPTE = "CASH";
    
    public static final String IDTYPE_CM_PB = "CM-PB";
    
    /** Type de caracteristique id facture origine sur facture */
    public static final String CHAR_TYPE_ID_FAC_OR = "ID_FACOR";
    
    /** Business Object  */
    public static final String CMDECLIC_BOLIAISON = "CMDECLIC_BOLIAISON            ";
    
    /** Outbound Message Type */
    public static final String CMDECLIC_OMTLIAISON = "CMDECLIC_OMT";

    /** External System */
    public static final String CMDECLIC_ESLIAISON = "CMDECLIC_ESLIAISON";

}