package com.splwg.cm.domain.batch;

public final class CmDNSConstant {
	
	
	public static final String IDENTIFIANT_IMMATRICULATION = "Identifiant immatriculation";
	public static final String ACTIVITE_PRINCIPALE = "Activité principale";
	public static final String MAISON_MERE = "Maison mère (Oui/Non)";
	public static final String DATE_DEBUT_PERIOD = "Date de début de période de cotisation";
	public static final String DATE_FIN_PERIOD = "Date de fin de période de cotisation";

	public static final String TOTAL_SALARIE_COTISATION = "Total des salaires soumis à cotisations";
	public static final String TEMPS_SUR_PERIODE = "Temps de présence sur la période(en jour)";
	public static final String RAISON_SOCIALE = "Raison Sociale";
	public static final String ADDRESS  = "Adresse";
	public static final String INFORMATION_EMPLOYEUR  = "Informations de L'employeur";
	public static final String SYNTHESE  = "Synthèse";
	public static final String TOTAL_SALARIES  = "Total salariés";
	public static final String TOTAL_SALARIES_VERSES = "Total des salaires versés";
	public static final String INFORMATION_SALARIES = "Informations des salariés";
	
	public static final String TYPE_PIECE = "Type de pièce";
	public static final String NUMERO_PIECE = "Numéro de pièce";
	public static final String NOM = "Nom";
	public static final String PRENOM = "Prénom";
	public static final String REGIME = "Régime";
	public static final String DATE_REGIME = "Date d'effet du régime";
	public static final String TOTAL_SALARIES_IPRES  = "Total des salaires plafonnés IPRES";
	public static final String TOTAL_SALARIES_CSS = "Total des salaires plafonnés CSS";
	public static final String SALARIE_REEL = "Salaire réel (Brut perçu)";
	public static final String TYPE_DE_CONTRACT  = "Type de contrat";
	public static final String DATE_ENTREE = "Date d'entrée";
	public static final String DATE_SORTIE = "Date de sortie";
	public static final String TEMPS_JOURS = "Temps de présence Jours";
	public static final String TEMPS_HEURES = "Temps de présence Heures";
	public static final String MOTIF_SORTIE = "Motif de sortie";
	public static final String TEMPS_TRAVAIL = "Temps de travail (plein/partiel)";
	public static final String TRANCHE_TRAVAIL = "Tranche de travail";
	
	public static final String UTF ="UTF-8";
	public static final String INVALID_DATE_STRING  = "invalidate";
	
	
	public static final String INVALID_DATE= "312";
	public static final String NINEA = "NINEA";
	public static final String TYPE_IDENTIFIANT = "Type Identifiant";
	public static final String NUMERO_IDENTIFANT = "Numéro d'identifiant";
	public static final String SCI = "SCI";
	public static final String NINEA_PREFIX ="00";
	public static final String NINEA_INVALID = "314";
	public static final String EMPTY = "328";
	public static final String BLANK = "BLANK";
	
	
	
	public static final String STAGING_SQL_QUERY = "INSERT into CI_FORM_UPLD_STG (C1_FORM_UPLD_STG_ID,FORM_BATCH_HDR_ID,"
			+ "C1_EXT_FORM_SUBM_SEQ,C1_FORM_SRCE_CD,EXT_FORM_TYPE,C1_FORM_YEAR,C1_FORM_UPLD_STG_TYP_CD,BUS_OBJ_CD,"
			+ "BO_STATUS_CD,C1_FORM_PYMNT_FLG, C1_FORM_PYMNT_AMT,VERSION,STATUS_UPD_DTTM,CRE_DTTM,BO_DATA_AREA) values (:C1_FORM_UPLD_STG_ID,"
			+ ":FORM_BATCH_HDR_ID,:C1_EXT_FORM_SUBM_SEQ,:C1_FORM_SRCE_CD,:EXT_FORM_TYPE,:C1_FORM_YEAR,:C1_FORM_UPLD_STG_TYP_CD,"
			+ ":BUS_OBJ_CD,:BO_STATUS_CD,:C1_FORM_PYMNT_FLG,:C1_FORM_PYMNT_AMT,"
			+ ":VERSION,to_date(:STATUS_UPD_DTTM,'DD/MM/YYYY'),to_date(:CRE_DTTM,'DD/MM/YYYY'),:BO_DATA_AREA)";
	
	public static final String STAGING_FILE_GEN_QUERY = " select "+ //--Employeur
			" per_moral_name.entity_name as raison_sociale "+
			" ,address.address1 || ' ' || address.city ||  ' ' || address.country as adresse "+
			" ,hist.id_employeur as id_immatriculation "+
			" ,per_moral_id.per_id_nbr as NINEA "+
			" ,per_char_moral.adhoc_char_val as activite_principale "+
			" ,date_debut_periode_cotisation "+
			" ,date_fin_periode_cotisation "+   //--synthes part
			" ,null as total_salaries "+
			" ,null as syn_total_sal_plaf_ipres "+
			" ,null as syn_total_sal_plaf_css "+
			" ,null as syn_tal_sal_verses "+
			" ,hist.TYPE_PIECE "+ //empoyee part
			" ,hist.NUMERO_PIECE "+
			" ,per_phys_name.C1_last_name as nom "+
			" ,per_phys_name.C1_first_name as prenom "+
			" , case when hist.EMPLOYE_CADRE = 'true' then 'Cadre' else 'General' end  as regime "+
			" ,hist.DATE_EFFET_REGIME "+
			" ,hist.TOTAL_SAL_IPRES "+
			" ,hist.TOTAL_SAL_CSS "+
			" ,hist.SALAIRE_CONTRACTUEL as salaire_reel_brut_percu "+
			" ,hist.NATURE_CONTRAT as type_contrat "+ 
			" ,hist.DATE_DEBUT_CONTRAT as date_entree "+
			" ,hist.DATE_FIN_CONTRAT as date_sortie "+
			" ,hist.TEMPS_PRESENCE_JOUR "+
			" ,hist.TEMPS_PRESENCE_HEURES "+
			" ,hist.MOTIF_SORTIE "+
			" ,hist.id_travailleur "+
			" ,hist.TEMPS_DE_TRAVAIL "+
			" ,hist.TRANCHE_TRAVAIL "+
			" from cm_dmt_historique hist "+
			" inner join ci_per per_phys on (hist.id_travailleur = per_phys.per_id and per_phys.per_type_cd = 'PERSON_PHYS') "+
			" inner join ci_per per_moral on hist.id_employeur = per_moral.per_id "+
			" inner join ci_per_name per_moral_name on per_moral_name.per_id = per_moral.per_id "+
			" inner join ci_per_id per_moral_id on (hist.id_employeur = per_moral_id.per_id and per_moral_id.id_type_cd = 'SCI') "+
			" inner join ci_per_addr per_addr on per_addr.per_id =  per_moral.per_id "+
			" inner join c1_address address on address.address_id = per_addr.address_id "+
			" inner join ci_per_name per_phys_name on per_phys_name.per_id = per_phys.per_id "+
			" left join ci_per_char per_char_moral on (per_char_moral.per_id = per_moral.per_id and per_char_moral.CHAR_TYPE_CD= 'CM-ACTPR') ";

}
