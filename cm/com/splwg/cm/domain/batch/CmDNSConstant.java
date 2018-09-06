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
	public static final String TYPE_DE_DECLARATION  = "Type de déclaration";
	public static final String INFORMATION_EMPLOYEUR  = "Informations de L'employeur";
	public static final String SYNTHESE  = "Synthèse";
	public static final String TOTAL_SALARIES  = "Total salariés";
	public static final String TOTAL_SALARIES_VERSES = "Total des salaires versés";
	public static final String MONTANT_DE_COTISATION_PF = "Montant de cotisation PF";
	public static final String MONTANT_DE_COTISATION_ATMP = "Montant de cotisation AT/MP";
	public static final String MONTANT_DE_COTISATION_RET_PF = "Montant de cotisation retraite RG";
	public static final String MONTANT_DE_COTISATION_RET_ATMP = "Montant de cotisation retraite RCC";
	public static final String INFORMATION_SALARIES = "Informations des salariés";
	public static final String NUMERO_ASSURE_SOCIAL = "Numéro Assuré Social";
	public static final String DATE_DE_NASISSANCE = "Date de naissance";
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
	public static final String TOTAL_SALARIE_IPRES_RG = "Total des salaires assujettis IPRES - RG";
	public static final String TOTAL_SALARIE_IPRES_RCC = "Total des salaires assujettis IPRES - RCC";
	public static final String TOTAL_SALARIE_CSS_PF = "Total des salaires assujettis CSS - PF";
	public static final String TOTAL_SALARIE_CSS_ATMP = "Total des salaires assujettis CSS - ATMP";
	public static final String MOIS1= "Mois1";
	public static final String MOIS2= "Mois2";
	public static final String MOIS3= "Mois3";
	

	public static final String TOTAL_NOUVEUX_SALARIES = "Total nouveaux salariés";

	
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
	public static final String RG = "RG";
	public static final String RCC = "RCC";
	
	
	
	public static final String STAGING_SQL_QUERY = "INSERT into CI_FORM_UPLD_STG (C1_FORM_UPLD_STG_ID,FORM_BATCH_HDR_ID,"
			+ "C1_EXT_FORM_SUBM_SEQ,C1_FORM_SRCE_CD,EXT_FORM_TYPE,C1_FORM_YEAR,C1_FORM_UPLD_STG_TYP_CD,BUS_OBJ_CD,"
			+ "BO_STATUS_CD,C1_FORM_PYMNT_FLG, C1_FORM_PYMNT_AMT,VERSION,STATUS_UPD_DTTM,CRE_DTTM,BO_DATA_AREA) values (:C1_FORM_UPLD_STG_ID,"
			+ ":FORM_BATCH_HDR_ID,:C1_EXT_FORM_SUBM_SEQ,:C1_FORM_SRCE_CD,:EXT_FORM_TYPE,:C1_FORM_YEAR,:C1_FORM_UPLD_STG_TYP_CD,"
			+ ":BUS_OBJ_CD,:BO_STATUS_CD,:C1_FORM_PYMNT_FLG,:C1_FORM_PYMNT_AMT,"
			+ ":VERSION,to_date(:STATUS_UPD_DTTM,'DD/MM/YYYY'),to_date(:CRE_DTTM,'DD/MM/YYYY'),:BO_DATA_AREA)";
	
	public static final String STAGING_FILE_GEN_QUERY =  "select "
			+ "   per_moral_name.entity_name as raison_sociale, "
			+ "   address.address1 || ' ' || address.city || ' ' || address.country as adresse, "
			+ "   hist.id_employeur as id_immatriculation, "
			+ "   per_moral_id.per_id_nbr as NINEA, "
			+ "   null as type_de_declaration, "
			+ "   date_debut_periode_cotisation, "
			+ "   date_fin_periode_cotisation, "
			+ "   null as total_nouveaux_salaries, "
			+ "   null as total_salaries, "
			+ "   null as syn_total_sal_plaf_ipres_RG, "
			+ "   null as syn_total_sal_plaf_ipres_RCC, "
			+ "   null as syn_total_sal_plaf_css_PF, "
			+ "   null as syn_total_sal_plaf_css_ATMP, "
			+ "   null as syn_tal_sal_verses, "
			+ "   null as mont_de_cotisation_PF, "
			+ "   null as mont_de_cotisation_ATMP, "
			+ "   null as mont_de_cotisation_ret_PF, "
			+ "   null as mont_de_cotisation_ret_ATMP, "
			+ "   hist.id_travailleur as Numero_assure_social, "
			+ "   hist.TYPE_PIECE, "
			+ "   hist.NUMERO_PIECE, "
			+ "   per_phys_name.C1_last_name as nom, "
			+ "   per_phys_name.C1_first_name as prenom, "
			+ "   TO_CHAR(TO_DATE(per_char_phys.adhoc_char_val, 'YYYY-MM-DD'), 'DD/MM/YYYY') as date_naissance, "
			+ "   case "
			+ "      when "
			+ "         hist.EMPLOYE_CADRE = 'true' "
			+ "      then "
			+ "         'Cadre' "
			+ "      else "
			+ "         'General' "
			+ "   end "
			+ "   as regime , null as TOTAL_SAL_IPRES_RG1 , null as TOTAL_SAL_IPRES_RCC1 , null as TOTAL_SAL_CSS_ATMP1 , null as TOTAL_SAL_CSS_PF1 , hist.SAL_BRUT_PERCU_1 as salaire_reel_brut_percu1 , hist.TEMPS_PRESENCE_JOUR_1 as TEMPS_PRESENCE_JOUR1 , hist.TEMPS_PRESENCE_HEURES_1 as TEMPS_PRESENCE_HEURES1 , hist.TEMPS_TRAVAIL_1 as TEMPS_DE_TRAVAIL1 , hist.TRANCHE_TRAVAIL_1 as TRANCHE_TRAVAIL1 , hist.DATE_EFFET_REGIME_1 as DATE_EFFET_REGIME1 , 'Oui' as RG1 , "
			+ "   case "
			+ "      when "
			+ "         hist.EMPLOYE_CADRE = 'true' "
			+ "      then "
			+ "         'Oui' "
			+ "      else "
			+ "         'Non' "
			+ "   end "
			+ "   as RCC1 , null as TOTAL_SAL_IPRES_RG2 , null as TOTAL_SAL_IPRES_RCC2 , null as TOTAL_SAL_CSS_ATMP2 , null as TOTAL_SAL_CSS_PF2 , hist.SAL_BRUT_PERCU_1 as salaire_reel_brut_percu2 , hist.TEMPS_PRESENCE_JOUR_2 as TEMPS_PRESENCE_JOUR2 , hist.TEMPS_PRESENCE_HEURES_2 as TEMPS_PRESENCE_HEURES2 , hist.TEMPS_TRAVAIL_2 as TEMPS_DE_TRAVAIL2 , hist.TRANCHE_TRAVAIL_2 as TRANCHE_TRAVAIL2 , hist.DATE_EFFET_REGIME_2 as DATE_EFFET_REGIME2 , 'Oui' as RG2 , "
			+ "   case "
			+ "      when "
			+ "         hist.EMPLOYE_CADRE = 'true' "
			+ "      then "
			+ "         'Oui' "
			+ "      else "
			+ "         'Non' "
			+ "   end "
			+ "   as RCC2 , null as TOTAL_SAL_IPRES_RG3 , null as TOTAL_SAL_IPRES_RCC3 , null as TOTAL_SAL_CSS_ATMP3 , null as TOTAL_SAL_CSS_PF3 , hist.SAL_BRUT_PERCU_1 as salaire_reel_brut_percu3 , hist.TEMPS_PRESENCE_JOUR_3 as TEMPS_PRESENCE_JOUR3 , hist.TEMPS_PRESENCE_HEURES_3 as TEMPS_PRESENCE_HEURES3 , hist.TEMPS_TRAVAIL_3 as TEMPS_DE_TRAVAIL3 , hist.TRANCHE_TRAVAIL_3 as TRANCHE_TRAVAIL3 , hist.DATE_EFFET_REGIME_3 as DATE_EFFET_REGIME3 , 'Oui' as RG3 , "
			+ "   case "
			+ "      when "
			+ "         hist.EMPLOYE_CADRE = 'true' "
			+ "      then "
			+ "         'Oui' "
			+ "      else "
			+ "         'Non' "
			+ "   end "
			+ "   as RCC3 , hist.NATURE_CONTRAT as type_contrat , hist.DATE_DEBUT_CONTRAT as date_entree , hist.DATE_FIN_CONTRAT as date_sortie , hist.MOTIF_SORTIE , hist.id_travailleur "
			+ "from "
			+ "   cm_dmt_historique hist "
			+ "   inner join "
			+ "      ci_per per_phys "
			+ "      on (hist.id_travailleur = per_phys.per_id "
			+ "      and per_phys.per_type_cd = 'PERSON_PHYS') "
			+ "   inner join "
			+ "      ci_per per_moral "
			+ "      on hist.id_employeur = per_moral.per_id "
			+ "   inner join "
			+ "      ci_per_name per_moral_name "
			+ "      on per_moral_name.per_id = per_moral.per_id "
			+ "   inner join "
			+ "      ci_per_id per_moral_id "
			+ "      on (hist.id_employeur = per_moral_id.per_id "
			+ "      and per_moral_id.id_type_cd = 'SCI') "
			+ "   inner join "
			+ "      ci_per_addr per_addr "
			+ "      on per_addr.per_id = per_moral.per_id "
			+ "   inner join "
			+ "      c1_address address "
			+ "      on address.address_id = per_addr.address_id "
			+ "   inner join "
			+ "      ci_per_name per_phys_name "
			+ "      on per_phys_name.per_id = per_phys.per_id "
			+ "   left join "
			+ "      ci_per_char per_char_moral "
			+ "      on (per_char_moral.per_id = per_moral.per_id "
			+ "      and per_char_moral.CHAR_TYPE_CD = 'CM-ACTPR') "
			+ "   left join "
			+ "      ci_per_char per_char_phys "
			+ "      on (per_char_phys.per_id = per_phys.per_id "
			+ "      and per_char_phys.CHAR_TYPE_CD = 'CM-DOB')";
	
}
