package com.splwg.cm.domain.admin.formRule;

import com.splwg.base.api.datatypes.Date;

import java.util.EnumMap;
import java.util.Iterator;
import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Money;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;


/**
 * @author Khadim Cisse
 *
 * @AlgorithmComponent ()
 */
public class CmCalcCotisation_Impl extends CmCalcCotisation_Gen implements FormRuleBORuleProcessingAlgorithmSpot {

	ApplyFormRuleAlgorithmInputData inputData;
	ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	private static final String delarationTrimestrielle = "TRIMESTRIEL";
	private static final String delarationMensuelle = "MENSUEL";
	private static final String SMIG_PATH = "smig/asCurrent";
	private static final String PLAFOND_CSS_ATMP_PATH = "plafondCssAtMp/asCurrent";
	private static final String TAUX_CSS_ATMP_PATH = "tauxCssCatmp/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS1_PATH = "totSalAssCssAtmpe1/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS2_PATH = "totSalAssCssAtmpe2/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS3_PATH = "totSalAssCssAtmpe3/asCurrent";
	private static final String MONTANT_CSS_ATMP_PATH = "montantCssAtMp/asCurrent";
	private static final String PLAFOND_IPRES_REGIME_GENERAL_PATH = "plafondIpresCrrg/asCurrent";
	private static final String TAUX_IPRES_REGIME_GENERAL = "tauxIpresCrrg/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS1_PATH = "totSalAssIpresRge1/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS2_PATH = "totSalAssIpresRge2/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS3_PATH = "totSalAssIpresRge3/asCurrent";
	private static final String MONTANT_IPRES_REGIME_GENERAL_PATH = "montantIpresCrrg/asCurrent";
	private static final String PLAFOND_IPRES_REGIME_CADRE_PATH = "plafondIpresCrcc/asCurrent";
	private static final String TAUX_IPRES_REGIME_CADRE_PATH = "tauxIpresCrcc/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS1_PATH = "totSalAssIpresRcce1/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS2_PATH = "totSalAssIpresRcce2/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS3_PATH = "totSalAssIpresRcce3/asCurrent";
	private static final String REGIME_CADRE_MOIS1_PATH = "regimCompCadre1/asCurrent";
	private static final String REGIME_CADRE_MOIS2_PATH = "regimCompCadre2/asCurrent";
	private static final String REGIME_CADRE_MOIS3_PATH = "regimCompCadre3/asCurrent";
	private static final String MONTANT_IPRES_REGIME_CADRE_PATH = "montantIpresCrcc/asCurrent";
	private static final String PLAFOND_CSS_PF_PATH = "plafondCssCpf/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_CSS_PF1_PATH = "totSalAssCssPfe1/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_CSS_PF2_PATH = "totSalAssCssPfe2/asCurrent";
	private static final String TOTAL_SALAIRE_ASSUJETIS_CSS_PF3_PATH = "totSalAssCssPfe3/asCurrent";
	private static final String TAUX_CSS_PF_PATH = "tauxCssCpf/asCurrent";
	private static final String MONTANT_CSS_PF_PATH = "montantCssCpf/asCurrent";
	private static final String INFORMATION_SALARIES_GROUP = "informationSalaries";
	private static final String DATE_DEBUT_COTISATION_PATH = "informationEmployeur/dateDebutCotisation/asCurrent";
	private static final String DATE_FIN_COTISATION_PATH = "informationEmployeur/dateFinCotisation/asCurrent";
	private static final String TYPE_DECLARATION_PATH = "informationEmployeur/typeDeclaration/asCurrent";
	private static final String INFORMATION_SALARIES_LIST = "informationSalariesList";
	private static final String MONTANT_ATMP_PATH = "synthese/montantATMP/asCurrent";
	private static final String MONTANT_COTISATION_ATMP_DIFF_PATH = "synthese/mntCotAtMpDiff/asCurrent";
	private static final String MONTANT_COTISATION_ATMP_EMP_PATH = "synthese/mntCotAtMpEmp/asCurrent";
	private static final String MONTANT_PF_PATH = "synthese/montantPF/asCurrent";
	private static final String MONTANT_COTISATION_PF_DIFF_PATH = "synthese/mntCotPfDiff/asCurrent";
	private static final String MONTANT_COTISATION_PF_EMP_PATH = "synthese/mntCotPfEmp/asCurrent";
	private static final String MONTANT_RRG_PATH = "synthese/montantRRG/asCurrent";
	private static final String MONTANT_COTISATION_RRG_DIFF_PATH = "synthese/mntCotRrgDiff/asCurrent";
	private static final String MONTANT_COTISATION_RG_EMP_PATH = "synthese/mntCotRgEmp/asCurrent";
	private static final String MONTANT_RCC_PATH = "synthese/montantRCC/asCurrent";
	private static final String MONTANT_COTISATION_RCC_DIFF_PATH = "synthese/mntCotRrccDiff/asCurrent";
	private static final String MONTANT_COTISATION_RCC_EMP = "synthese/mntCotRccEmp/asCurrent";
	private static final String TYPE_PIECE_IDENTITE_PATH= "typePieceIdentite/asCurrent";
	private static final String NUMERO_PIECE_IDENTITE_PATH= "numeroPieceIdentite/asCurrent";
	private static final EnumMap<ClassMois, String> pathRegimeCadre = new EnumMap<ClassMois, String>(ClassMois.class);
	private static final EnumMap<ClassMois, String> pathDateEffectiveRegime = new EnumMap<ClassMois, String>(ClassMois.class);
	
	
	
	@Override
	public void invoke() {
		initialisationPath();
		//Related Transaction BO du form Type FORM_DNS
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		COTSInstanceNode group = formBoInstance.getGroupFromPath(INFORMATION_SALARIES_GROUP);
		Date dateDebutCotisation = (Date) formBoInstance.getFieldAndMDForPath(DATE_DEBUT_COTISATION_PATH).getValue();
		Date dateFinCotisation = (Date) formBoInstance.getFieldAndMDForPath(DATE_FIN_COTISATION_PATH).getValue();
		String typeDeclaration = (String) formBoInstance.getFieldAndMDForPath(TYPE_DECLARATION_PATH).getValue();
	
		//Chargement montant cotisation Accident Travail au niveau du formulaire DNS
		double montantAtMp = this.getMontantCotisationAT_MP(group.getList(INFORMATION_SALARIES_LIST).iterator(), typeDeclaration, dateDebutCotisation, dateFinCotisation);
		@SuppressWarnings("unchecked")
		COTSFieldDataAndMD<Money> fieldMontantAtMp = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_ATMP_PATH);
		fieldMontantAtMp.setXMLValue(String.valueOf(montantAtMp));
		
		
		//Difference entre les montants
		@SuppressWarnings("unchecked")
		COTSFieldDataAndMD<Money> fieldMontantAtMpDiff = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_ATMP_DIFF_PATH);
		if(!isNull(fieldMontantAtMpDiff)){
			Money montantAtMpEmp = (Money) inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_ATMP_EMP_PATH).getValue();
			//Difference entre les montants
			fieldMontantAtMpDiff.setXMLValue(String.valueOf(montantAtMpEmp.getAmount().doubleValue()-montantAtMp));
		}
		
		
		//Chargement montant cotisation Prestation Familiale au niveau du formulaire DNS
		double montantPF = this.getMontantCotisationPF(group.getList(INFORMATION_SALARIES_LIST).iterator(), typeDeclaration, dateDebutCotisation, dateFinCotisation);
		@SuppressWarnings("unchecked")
		COTSFieldDataAndMD<Money> fieldMontantPF = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_PF_PATH);
		fieldMontantPF.setXMLValue(String.valueOf(montantPF));
		
		//Montant ATMP calcule par l'employeur
		@SuppressWarnings("unchecked")
		COTSFieldDataAndMD<Money> fieldMontantPfDiff = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_PF_DIFF_PATH);
		if(!isNull(fieldMontantPfDiff)){
			Money montantPfEmp = (Money) inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_PF_EMP_PATH).getValue();
			//Difference entre les montants
			fieldMontantPfDiff.setXMLValue(String.valueOf(montantPfEmp.getAmount().doubleValue()-montantPF));
		}
	
		
		//Chargement montant cotisation Retraite Regime General au niveau du formulaire DNS
		double montantCotisationRetraiteRG = this.getMontantCotisationRetraiteRG(group.getList(INFORMATION_SALARIES_LIST).iterator(), typeDeclaration, dateDebutCotisation, dateFinCotisation);
		COTSFieldDataAndMD<?> fieldMontantCotisationRetraiteRG = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_RRG_PATH);
		fieldMontantCotisationRetraiteRG.setXMLValue(String.valueOf(montantCotisationRetraiteRG));
		
		//Montant Regime General calcule par l'employeur
		@SuppressWarnings("unchecked")
		COTSFieldDataAndMD<Money> fieldMontantRgDiff = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_RRG_DIFF_PATH);
		if(!isNull(fieldMontantRgDiff)){
			Money montantRgEmp = (Money) inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_RG_EMP_PATH).getValue();
			//Difference entre les montants
			fieldMontantRgDiff.setXMLValue(String.valueOf(montantRgEmp.getAmount().doubleValue()-montantCotisationRetraiteRG));
		}
		
		//Chargement montant Cotisation Retraite Regime Cadre au niveau du formulaire DNS
		double montantCotisationRetraiteRC = this.getMontantCotisationRetraiteRC(group.getList(INFORMATION_SALARIES_LIST).iterator(), typeDeclaration, dateDebutCotisation, dateFinCotisation);
		COTSFieldDataAndMD<?> fieldMontantCotisationRetraiteRC = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_RCC_PATH);
		fieldMontantCotisationRetraiteRC.setXMLValue(String.valueOf(montantCotisationRetraiteRC));
		
		//Montant Regime General calcule par l'employeur
		@SuppressWarnings("unchecked")
		COTSFieldDataAndMD<Money> fieldMontantRccDiff = inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_RCC_DIFF_PATH);
		if(!isNull(fieldMontantRccDiff)){
			Money montantRccEmp = (Money) inputOutputData.getFormBusinessObject().getFieldAndMDForPath(MONTANT_COTISATION_RCC_EMP).getValue();
			//Difference entre les montants
			fieldMontantRccDiff.setXMLValue(String.valueOf(montantRccEmp.getAmount().doubleValue()-montantCotisationRetraiteRC));
		}
	}
	
	private void initialisationPath(){
		
		pathRegimeCadre.put(ClassMois.MOIS1, "regimCompCadre1/asCurrent");
		pathRegimeCadre.put(ClassMois.MOIS2, "regimCompCadre2/asCurrent");
		pathRegimeCadre.put(ClassMois.MOIS3, "regimCompCadre3/asCurrent");
		
		pathDateEffectiveRegime.put(ClassMois.MOIS1, "dateEffetRegime1/asCurrent");
		pathDateEffectiveRegime.put(ClassMois.MOIS2, "dateEffetRegime2/asCurrent");
		pathDateEffectiveRegime.put(ClassMois.MOIS3, "dateEffetRegime3/asCurrent");
	}

	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return inputOutputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData) {
		this.inputData = applyFormRuleAlgorithmInputData;

	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData) {
		this.inputOutputData = applyFormRuleAlgorithmInputOutputData;
	}

	/**
	 * Cette methode calcule le montant de cotisation de la prestation familiale d'un enployeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries
	 * @return  montant de la prestation familiale
	 */
	private double getMontantCotisationPF(Iterator<COTSInstanceListNode> listSalaries, String typeDeclaration, Date dateDebutCotisation, Date dateFinCotisation) {
		
		  //Montant total PF employeur 
		  double montantTotal = 0; 
		  
		  double tauxProrotaMois1 = 0;
		  double tauxProrotaMois2 = 0;
		  double tauxProrotaMois3 = 0;
		  //pour declaration mensuelle
		  double tauxProrota = 0;
		  Money smig;
		  Money plafondCssPf;
		  Money totalAmontMois1;
		  Money totalAmontMois2;
		  Money totalAmontMois3;
		  BigDecimal tauxCssPf;
		  Money totalAmontMois = null;
		  
		  while (listSalaries.hasNext()) {
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			 //Montant Cotisation PF par salarie
			  double montantCotisation = 0;
			   if (nextSalarie != null) {
				   if(typeDeclaration.equalsIgnoreCase(delarationTrimestrielle)){
					    //Recuperation des informations necessaire pour les calcules à partir du formulaire
						smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
						plafondCssPf = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_CSS_PF_PATH).getValue();
					    totalAmontMois1 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_PF1_PATH).getValue();
					    totalAmontMois2 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_PF2_PATH).getValue();
					    totalAmontMois3 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_PF3_PATH).getValue();
					    tauxCssPf = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_CSS_PF_PATH).getValue();
					    tauxProrotaMois1 = getTauxProrota(nextSalarie, ClassMois.MOIS1.name());
					    tauxProrotaMois2 = getTauxProrota(nextSalarie, ClassMois.MOIS2.name());
					    tauxProrotaMois3 = getTauxProrota(nextSalarie, ClassMois.MOIS3.name());
					    if(totalAmontMois1 != null && !totalAmontMois1.isZero())
					    	montantCotisation = applyRuleCalculPrestationFamiliale(totalAmontMois1, plafondCssPf, smig, tauxCssPf) * tauxProrotaMois1;
					    if (totalAmontMois2 != null && !totalAmontMois2.isZero())
					    	montantCotisation += applyRuleCalculPrestationFamiliale(totalAmontMois2, plafondCssPf, smig, tauxCssPf) * tauxProrotaMois2;
					    if (totalAmontMois3 != null && !totalAmontMois3.isZero())
					    	montantCotisation += applyRuleCalculPrestationFamiliale(totalAmontMois3, plafondCssPf, smig, tauxCssPf) * tauxProrotaMois3;
				   } else if (typeDeclaration.equalsIgnoreCase(delarationMensuelle)) {
					   	
					    smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
						plafondCssPf = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_CSS_PF_PATH).getValue();
						tauxCssPf = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_CSS_PF_PATH).getValue();
						String classMois = this.getClassMois(dateDebutCotisation, dateFinCotisation);
						tauxProrota = getTauxProrota(nextSalarie, classMois);
						if (ClassMois.MOIS1.name().equalsIgnoreCase(classMois)) {
							  totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_PF1_PATH).getValue();
						} else if (ClassMois.MOIS2.name().equalsIgnoreCase(classMois)) {
							   
							 totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_PF2_PATH).getValue();
						} else if (ClassMois.MOIS3.name().equalsIgnoreCase(classMois)) {
							   
							 totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_PF3_PATH).getValue();
						}
					    if (totalAmontMois != null && !totalAmontMois.isZero())
					    	montantCotisation = applyRuleCalculPrestationFamiliale(totalAmontMois, plafondCssPf, smig, tauxCssPf) * tauxProrota;
				   }
			   }
			   
			   montantTotal += montantCotisation;
			   int intMontantCotisation = (int) Math.round(montantCotisation);
			   //Chargement montant cotisation PF du salarie au niveau du formulaire
			   nextSalarie.getFieldAndMDForPath(MONTANT_CSS_PF_PATH).setXMLValue(Integer.toString(intMontantCotisation));
		  }  

		  int intMontantTotal = (int) Math.round(montantTotal);
		  return Double.valueOf(intMontantTotal);
		}
	
	
	
	
	    /**
	     * Cette methode applique la régle de calcule de prestation familiale
	     * @param totalAmont Salaire assujetis PF
	     * @param plafondCssPf plafond prestation familiale
	     * @param smig
	     * @param tauxCssPf taux prestation familiale
	     * @return montant prestation familiale
	     */
		private double applyRuleCalculPrestationFamiliale(Money totalAmont, Money plafondCssPf, Money smig, BigDecimal tauxCssPf){
			double montantCotisation = 0;
			 if(totalAmont.getAmount().doubleValue() <= smig.getAmount().doubleValue()){
			    	//TOTAL_MNT_COT_CSS < PLF_SMIG
			    	//MONTANT_COTIS_SALARIE=PLF_SMIG*TXE_CSS_CPF
			    	montantCotisation = smig.getAmount().doubleValue()*tauxCssPf.doubleValue()/100;
			    }
			    else if(plafondCssPf.getAmount().doubleValue() > totalAmont.getAmount().doubleValue() && totalAmont.getAmount().doubleValue() >= smig.getAmount().doubleValue()){
			    	//PLF_CSS_CPF > TOTAL_MNT_COT_CSS > = PLF_SMIG
			    	//MONTANT_COTIS_SALARIE=TOTAL_MNT_COT_CSS * TXE_CSS_CPF
			    	montantCotisation = totalAmont.getAmount().doubleValue()*tauxCssPf.doubleValue()/100;
			    }
			    else{
			    	//TOTAL_MNT_COT_CSS > PLF_CSS_CPF
			    	//MONTANT_COTIS_SALARIE=PLF_CSS_CPF * TXE_CSS_CPF
			    	montantCotisation = plafondCssPf.getAmount().doubleValue()*tauxCssPf.doubleValue()/100;
			    }
			 
			 return montantCotisation;
		}
	
	
	/**
	 * Cette methode calcule le montant de cotisation AT/MP d'un enployeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries de l'employeur
	 * @return montant de cotisation AT/MP
	 */
	private double getMontantCotisationAT_MP(Iterator<COTSInstanceListNode> listSalaries, String typeDeclaration, Date dateDebutCotisation, Date dateFinCotisation ) {
		//Montant total AT/MP employeur 
		double montantTotal = 0;
		double tauxProrotaMois1 = 0;
		double tauxProrotaMois2 = 0;
		double tauxProrotaMois3 = 0;
		double tauxProrota = 0;
		Money smig;
		Money plafondCssAtMp;
		BigDecimal tauxCssCatmp;
		Money totalAmontMois1;
		Money totalAmontMois2;
		Money totalAmontMois3;
		Money totalAmontMois = null;
		
		  while (listSalaries.hasNext()) {
		   COTSInstanceListNode nextSalarie = listSalaries.next();
			//Montant Cotisation AT/MP par salarie
			double montantCotisation = 0;

		   if (nextSalarie != null) {
			   if(typeDeclaration.equalsIgnoreCase(delarationTrimestrielle)) {
				   //Recuperation des informations necessaire pour les calcules à partir du formulaire
				   smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
				   plafondCssAtMp = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_CSS_ATMP_PATH).getValue();
				   tauxCssCatmp = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_CSS_ATMP_PATH).getValue();
				   totalAmontMois1 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS1_PATH).getValue();
				   totalAmontMois2 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS2_PATH).getValue();
				   totalAmontMois3 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS3_PATH).getValue();
				   tauxProrotaMois1 = getTauxProrota(nextSalarie, ClassMois.MOIS1.name());
				   tauxProrotaMois2 = getTauxProrota(nextSalarie, ClassMois.MOIS2.name());
				   tauxProrotaMois3 = getTauxProrota(nextSalarie, ClassMois.MOIS3.name());
				   if(totalAmontMois1 != null && !totalAmontMois1.isZero())
					   montantCotisation = applyRuleCalculPrestationFamiliale(totalAmontMois1, plafondCssAtMp, smig, tauxCssCatmp) * tauxProrotaMois1;
				   if(totalAmontMois2 != null && !totalAmontMois2.isZero())
					   montantCotisation += applyRuleCalculPrestationFamiliale(totalAmontMois2, plafondCssAtMp, smig, tauxCssCatmp) * tauxProrotaMois2;
				   if(totalAmontMois3 != null && !totalAmontMois3.isZero())
					   montantCotisation += applyRuleCalculPrestationFamiliale(totalAmontMois3, plafondCssAtMp, smig, tauxCssCatmp) * tauxProrotaMois3;
			    } else if (typeDeclaration.equalsIgnoreCase(delarationMensuelle)) {
			       smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
				   plafondCssAtMp = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_CSS_ATMP_PATH).getValue();
				   tauxCssCatmp = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_CSS_ATMP_PATH).getValue();
				   String classMois = this.getClassMois(dateDebutCotisation, dateFinCotisation);
				   tauxProrota = getTauxProrota(nextSalarie, classMois);
				   if (ClassMois.MOIS1.name().equalsIgnoreCase(classMois)) {
						totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS1_PATH).getValue();
				   } else if (ClassMois.MOIS2.name().equalsIgnoreCase(classMois)) {
						totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS2_PATH).getValue();
				   } else if (ClassMois.MOIS3.name().equalsIgnoreCase(classMois)) {
						totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_CSS_ATMP_MOIS3_PATH).getValue();
				   }
				   if (totalAmontMois != null && !totalAmontMois.isZero())
					   montantCotisation = applyRuleCalculAccidentTravail(totalAmontMois, plafondCssAtMp, smig, tauxCssCatmp) * tauxProrota;
				} 
		   	}
		   //Multiplier le montant de cotisation par 3 si le calendrier est trimestriel
		   montantTotal += montantCotisation; 
		   int intMontantCotisation = (int) Math.round(montantCotisation);
    	   //Montant AT MP Salarié
 		   nextSalarie.getFieldAndMDForPath(MONTANT_CSS_ATMP_PATH).setXMLValue(String.format(Integer.toString(intMontantCotisation)));	 
		}  
		  
		int intMontantTotal = (int) Math.round(montantTotal);
		return Double.valueOf(intMontantTotal);
	}
	
	
	/**
	 * Cette methode applique la régle de calcule de l'accident de travail
	 * @param totalAmont salaire assujetis ATMP
	 * @param plafondCssAtMp plafond ATMP
	 * @param smig
	 * @param tauxCssCatmp taux ATMP
	 * @return montant ATMP
	 */
	private double applyRuleCalculAccidentTravail(Money totalAmont , Money plafondCssAtMp, Money smig, BigDecimal tauxCssCatmp){
		double montantCotisation = 0;
		  if(totalAmont.getAmount().doubleValue() <= smig.getAmount().doubleValue()){ 
		    	//TOTAL_MNT_COT_CSS < PLF_SMIG
		    	//MONTANT_COTIS_SALARIE=TAUX_AT/MP*PLF_SMIG
		    	montantCotisation = smig.getAmount().doubleValue()*tauxCssCatmp.doubleValue()/100;
		    }else{
		    	if(totalAmont.getAmount().doubleValue() > smig.getAmount().doubleValue() && totalAmont.getAmount().doubleValue() < plafondCssAtMp.getAmount().doubleValue()){
		    		//PLF_CSS_CATMP > TOTAL_MNT_COT_CSS > = PLF_SMIG
			    	//MONTANT_COTIS_SALARIE=TAUX_AT/MP*TOTAL_MNT_COT_CSS
		    		montantCotisation = totalAmont.getAmount().doubleValue() * tauxCssCatmp.doubleValue() / 100;
		    	}else{
		    		//MONTANT_COTIS_SALARIE=TAUX_AT/MP*PLF_CSS_CATMP
		    		montantCotisation=plafondCssAtMp.getAmount().doubleValue() * tauxCssCatmp.doubleValue() / 100;
		    	}
		    }
		  
		  return montantCotisation;
	}
	
	
	

	/**
	 * Cette methode calcule le montant de cotisation retraite regime general d'un enployeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries de l'employeur
	 * @return montant de cotisation retraite regime general
	 */
	private double getMontantCotisationRetraiteRG(Iterator<COTSInstanceListNode> listSalaries, String typeDeclaration, Date dateDebutCotisation, Date dateFinCotisation ) {
		//DecimalFormat roundMontant = new DecimalFormat(".##");
		double montantTotal = 0;
		
		double tauxProrotaMois1 = 0;
		double tauxProrotaMois2 = 0;
		double tauxProrotaMois3 = 0;
		double tauxProrota = 0;
		Money smig;
		Money plafondRetraiteRG;
		BigDecimal tauxRetraiteRG;
		Money totalAmontMois1;
		Money totalAmontMois2;
		Money totalAmontMois3;
		Money totalAmontMois = null;
		
		while (listSalaries.hasNext()) {
			   //List des salaries
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			   double montantCotisation = 0;
			   if (nextSalarie != null) {
				 if(typeDeclaration.equalsIgnoreCase(delarationTrimestrielle)) {
				   //Recuperation des informations necessaire pour les calcules à partir du formulaire
				   smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
				   plafondRetraiteRG = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_IPRES_REGIME_GENERAL_PATH).getValue();
				   tauxRetraiteRG = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_IPRES_REGIME_GENERAL).getValue();
				   totalAmontMois1 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS1_PATH).getValue(); 
				   totalAmontMois2 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS2_PATH).getValue(); 
				   totalAmontMois3 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS3_PATH).getValue(); 
				   tauxProrotaMois1 = getTauxProrota(nextSalarie, ClassMois.MOIS1.name());
				   tauxProrotaMois2 = getTauxProrota(nextSalarie, ClassMois.MOIS2.name());
				   tauxProrotaMois3 = getTauxProrota(nextSalarie, ClassMois.MOIS3.name());
				   if(totalAmontMois1 != null && !totalAmontMois1.isZero())
					   montantCotisation = applyRuleRetraiteRegimeGeneral(totalAmontMois1, plafondRetraiteRG, smig, tauxRetraiteRG) * tauxProrotaMois1;
				   if(totalAmontMois2 != null && !totalAmontMois2.isZero())
					   montantCotisation += applyRuleRetraiteRegimeGeneral(totalAmontMois2, plafondRetraiteRG, smig, tauxRetraiteRG) * tauxProrotaMois2;
				   if(totalAmontMois3 != null && !totalAmontMois3.isZero())
					   montantCotisation += applyRuleRetraiteRegimeGeneral(totalAmontMois3, plafondRetraiteRG, smig, tauxRetraiteRG) * tauxProrotaMois3;
		 
				 } else if (typeDeclaration.equalsIgnoreCase(delarationMensuelle)) {
					   
				   smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
				   plafondRetraiteRG = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_IPRES_REGIME_GENERAL_PATH).getValue();
				   tauxRetraiteRG = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_IPRES_REGIME_GENERAL).getValue();
				   String classMois = this.getClassMois(dateDebutCotisation, dateFinCotisation);
				   tauxProrota = getTauxProrota(nextSalarie, classMois);
				   if (ClassMois.MOIS1.name().equalsIgnoreCase(classMois)) {
					   totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS1_PATH).getValue();
				   } else if (ClassMois.MOIS2.name().equalsIgnoreCase(classMois)) {
					   totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS2_PATH).getValue();
				   } else if (ClassMois.MOIS3.name().equalsIgnoreCase(classMois)) {
					   totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_IPRES_REGIME_GENERAL_MOIS3_PATH).getValue();
				   }
				   if(totalAmontMois != null && !totalAmontMois.isZero())
					   montantCotisation = applyRuleRetraiteRegimeGeneral(totalAmontMois, plafondRetraiteRG, smig, tauxRetraiteRG) * tauxProrota;
				}
				  
				montantTotal += montantCotisation;
				int intMontantCotisation = (int) Math.round(montantCotisation);
				//Montant Cotisé par le salarié
				nextSalarie.getFieldAndMDForPath(MONTANT_IPRES_REGIME_GENERAL_PATH).setXMLValue(String.format(Integer.toString(intMontantCotisation)));
			}
		}  
	
		int intMontantTotal = (int) Math.round(montantTotal);
		return Double.valueOf(intMontantTotal);
	}
	
	/**
	 * Cette methode applique la régle de calcule du regime général
	 * @param totalAmont salaire assujetis regime general
	 * @param plafondRetraiteRG plafond retraite regime général
	 * @param smig
	 * @param tauxRetraiteRG taux retraite regime general
	 * @return montant regime general
	 */
	private double applyRuleRetraiteRegimeGeneral(Money totalAmont , Money plafondRetraiteRG, Money smig, BigDecimal tauxRetraiteRG){
		double montantCotisation = 0;
		if(totalAmont.getAmount().doubleValue() <= smig.getAmount().doubleValue()){
			 //TOTAL_MNT_COT_IPRES<PLF_SMIG
			 //MONTANT_COTIS_SALARIE=TXE_IPRES_CRRG*TOTAL_MNT_COT_IPRES
			 montantCotisation = smig.getAmount().doubleValue() * tauxRetraiteRG.doubleValue() / 100;
		 }else{
			 	if(totalAmont.getAmount().doubleValue() > smig.getAmount().doubleValue() && totalAmont.getAmount().doubleValue() < plafondRetraiteRG.getAmount().doubleValue()){
			 		//PLF_SMIG<TOTAL_MNT_COT_IPRES<PLF_IPRES_CRRG
					//MONTANT_COTIS_SALARIE=TXE_IPRES_CRRG*PLF_IPRES_CRRG
			 		montantCotisation = totalAmont.getAmount().doubleValue() * tauxRetraiteRG.doubleValue() / 100;
			 	}else{
			 		//TOTAL_MNT_COT_IPRES>PLF_IPRES_CRRG
					//MONTANT_COTIS_SALARIE=TXE_IPRES_CRRG*TOTAL_MNT_COT_IPRES
			 		montantCotisation = plafondRetraiteRG.getAmount().doubleValue() * tauxRetraiteRG.doubleValue() / 100;
			 	}
		 }
		 return montantCotisation;
	}
	
	
	/**
	 * Cette methode calcule le montant de cotisation retraite regime cadre d'un employeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries de l'employeur
	 * @return montant de cotisation retraite regime cadre
	 */
	private double getMontantCotisationRetraiteRC(Iterator<COTSInstanceListNode> listSalaries,String typeDeclaration, Date dateDebutCotisation, Date dateFinCotisation ) {
		double montantTotal = 0;
		double tauxProrotaMois1 = 0;
		double tauxProrotaMois2 = 0;
		double tauxProrotaMois3 = 0;
		double tauxProrota = 0;
		Money smig;
		Money plafondRetraiteRC;
		BigDecimal tauxRetraiteRC;
		Money totalAmontMois1;
		Money totalAmontMois2;
		Money totalAmontMois3;
		Bool salarieCadre1;
		Bool salarieCadre2;
		Bool salarieCadre3;
		Bool salarieCadre = null;
		while (listSalaries.hasNext()) {
			   //List des salaries
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			   double montantCotisation = 0;
			   if (nextSalarie != null) {
				  // if (salarieCadre != null && salarieCadre.isTrue()) {
				   if(typeDeclaration.equalsIgnoreCase(delarationTrimestrielle)){
					   //Recuperation des informations necessaire pour les calcules à partir du formulaire
					   smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
					   plafondRetraiteRC = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_IPRES_REGIME_CADRE_PATH).getValue();
					   tauxRetraiteRC = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_IPRES_REGIME_CADRE_PATH).getValue();
					   totalAmontMois1 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS1_PATH).getValue();
					   totalAmontMois2 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS2_PATH).getValue();
					   totalAmontMois3 = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS3_PATH).getValue();
					   salarieCadre1 = (Bool) nextSalarie.getFieldAndMDForPath(REGIME_CADRE_MOIS1_PATH).getValue();
					   salarieCadre2 = (Bool) nextSalarie.getFieldAndMDForPath(REGIME_CADRE_MOIS2_PATH).getValue();
					   salarieCadre3 = (Bool) nextSalarie.getFieldAndMDForPath(REGIME_CADRE_MOIS3_PATH).getValue();
					   tauxProrotaMois1 = getTauxProrota(nextSalarie, ClassMois.MOIS1.name());
					   tauxProrotaMois2 = getTauxProrota(nextSalarie, ClassMois.MOIS2.name());
					   tauxProrotaMois3 = getTauxProrota(nextSalarie, ClassMois.MOIS3.name());
					   if(totalAmontMois1 != null && !totalAmontMois1.isZero())
						   montantCotisation = applyRuleRetraiteRegimeCadre(totalAmontMois1, plafondRetraiteRC, smig, tauxRetraiteRC, salarieCadre1) * tauxProrotaMois1;
					   if(totalAmontMois2 != null && !totalAmontMois2.isZero())
						   montantCotisation += applyRuleRetraiteRegimeCadre(totalAmontMois2, plafondRetraiteRC, smig, tauxRetraiteRC, salarieCadre2) * tauxProrotaMois2;
					   if(totalAmontMois3 != null && !totalAmontMois3.isZero())
						   montantCotisation += applyRuleRetraiteRegimeCadre(totalAmontMois3, plafondRetraiteRC, smig, tauxRetraiteRC, salarieCadre3) * tauxProrotaMois3;
				   } else if (typeDeclaration.equalsIgnoreCase(delarationMensuelle)) {
					    smig = (Money) nextSalarie.getFieldAndMDForPath(SMIG_PATH).getValue();
					    plafondRetraiteRC = (Money) nextSalarie.getFieldAndMDForPath(PLAFOND_IPRES_REGIME_CADRE_PATH).getValue();
					    tauxRetraiteRC = (BigDecimal) nextSalarie.getFieldAndMDForPath(TAUX_IPRES_REGIME_CADRE_PATH).getValue();
					   Money totalAmontMois = null;
					   //Bool salarieCadre = null;
					   String classMois = this.getClassMois(dateDebutCotisation, dateFinCotisation);
					   tauxProrota = getTauxProrota(nextSalarie, classMois);
					   if (ClassMois.MOIS1.name().equalsIgnoreCase(classMois)) {
						   totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS1_PATH).getValue();
						   salarieCadre = (Bool) nextSalarie.getFieldAndMDForPath(REGIME_CADRE_MOIS1_PATH).getValue();
					   } else if (ClassMois.MOIS2.name().equalsIgnoreCase(classMois)) {
						   totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS2_PATH).getValue();
						   salarieCadre = (Bool) nextSalarie.getFieldAndMDForPath(REGIME_CADRE_MOIS2_PATH).getValue();
					   } else if (ClassMois.MOIS3.name().equalsIgnoreCase(classMois)) {
						   totalAmontMois = (Money) nextSalarie.getFieldAndMDForPath(TOTAL_SALAIRE_ASSUJETIS_REGIME_CADRE_MOIS3_PATH).getValue();
						   salarieCadre = (Bool) nextSalarie.getFieldAndMDForPath(REGIME_CADRE_MOIS3_PATH).getValue();
					   }
					   if(totalAmontMois != null && !totalAmontMois.isZero())
						   montantCotisation = applyRuleRetraiteRegimeCadre(totalAmontMois, plafondRetraiteRC, smig, tauxRetraiteRC, salarieCadre) * tauxProrota;
				   }
				  
				   montantTotal += montantCotisation;
				   int intMontantCotisation = (int) Math.round(montantCotisation);
				   //Montant Cotisé par le salarié
				   nextSalarie.getFieldAndMDForPath(MONTANT_IPRES_REGIME_CADRE_PATH).setXMLValue(String.format(Integer.toString(intMontantCotisation)));
			   }
			}
		
		  int intMontantTotal = (int) Math.round(montantTotal);
		  return Double.valueOf(intMontantTotal);
	}
	
	
	/**
	 * Cette methode applique la régle de calcule du regime cadre
	 * @param totalAmont salaire assujetis regime cadre
	 * @param plafondRetraiteRC plafond retraite regime cadre
	 * @param smig
	 * @param tauxRetraiteRC taux retraite regime cadre
	 * @param salarieCadre boolean pour vérifier le régime cadre
	 * @return montant retraite regime cadre
	 */
	private double applyRuleRetraiteRegimeCadre(Money totalAmont , Money plafondRetraiteRC, Money smig, BigDecimal tauxRetraiteRC,  Bool salarieCadre){
		double montantCotisation = 0;
		if (salarieCadre != null && salarieCadre.isTrue()) {
			   if(totalAmont.getAmount().doubleValue() <= smig.getAmount().doubleValue()){ 
				   //TOTAL_MNT_COT_IPRES<PLF_SMIG
				   //MONTANT_COTIS_SALARIE=TXE_IPRES_CRCC*TOTAL_MNT_COT_IPRES
				   montantCotisation = smig.getAmount().doubleValue() * tauxRetraiteRC.doubleValue() / 100;
				 } else {
					  	if(totalAmont.getAmount().doubleValue()>smig.getAmount().doubleValue() && totalAmont.getAmount().doubleValue()<plafondRetraiteRC.getAmount().doubleValue()){
					  	    //PLF_SMIG<TOTAL_MNT_COT_IPRES<PLF_IPRES_CRCC
							//MONTANT_COTIS_SALARIE=TXE_IPRES_CRCC*PLF_IPRES_CRCC
					  		montantCotisation=totalAmont.getAmount().doubleValue()*tauxRetraiteRC.doubleValue()/100;
					    }else{
					    	//TOTAL_MNT_COT_IPRES>PLF_IPRES_CRCC
							//MONTANT_COTIS_SALARIE=TXE_IPRES_CRCC*TOTAL_MNT_COT_IPRES
					    	montantCotisation=plafondRetraiteRC.getAmount().doubleValue()*tauxRetraiteRC.doubleValue()/100;
					    }
				 }
		}
	  
		return montantCotisation;	   
	}
	
	/**
	 * Cette methode permet dans le cadre d'une déclaration mensuelle de récupérer
	 * le mois de cotisation.
	 * @param dateDebutCotisation date début de la période de cotisation
	 * @param dateFinCotisation date fin de la période de cotisation
	 * @return MOIS1, MOIS2 ou MOIS3
	 */
	private String getClassMois(Date dateDebutCotisation, Date dateFinCotisation) {
		
		if (dateDebutCotisation.getMonth() != dateFinCotisation.getMonth())
			throw new RuntimeException("Dans le cas d'une déclaration mensuelle, les dates de début et de fin doit être dans le meme mois");
		
		ClassMois classMois = null;
		
		switch (dateDebutCotisation.getMonth()) {
		
			case 1 : classMois = ClassMois.MOIS1;
					break;
			
			case 2 : classMois = ClassMois.MOIS2;	
			 		break;
			
			case 3 : classMois = ClassMois.MOIS3;
			 		break;
			
			case 4 : classMois = ClassMois.MOIS1;
			 		break;
			
			case 5 : classMois = ClassMois.MOIS2;
			 		break;
			
			case 6 : classMois = ClassMois.MOIS3;
			 		break;
			
			case 7 : classMois = ClassMois.MOIS1;
			 		break;
			
			case 8 : classMois = ClassMois.MOIS2;
			 		break;
			
			case 9 : classMois = ClassMois.MOIS3;
			 		break;
			
			case 10 : classMois = ClassMois.MOIS1;
			 		break;
			
			case 11 : classMois = ClassMois.MOIS2;
			 		break;
			
			case 12 : classMois = ClassMois.MOIS3;
			 		break;
		}
		return classMois != null ? classMois.name() : null;
	}

	public enum ClassMois{
		MOIS1 ,MOIS2 ,MOIS3
	}
	
	
	
	
	private double getTauxProrota(COTSInstanceListNode salarie, String classMoisParam) {
		
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		COTSInstanceNode group = formBoInstance.getGroupFromPath(INFORMATION_SALARIES_GROUP);
		Iterator<COTSInstanceListNode> listSalaries = group.getList(INFORMATION_SALARIES_LIST).iterator();
		ClassMois classMois;
		if(ClassMois.MOIS1.name().equalsIgnoreCase(classMoisParam))
			classMois = ClassMois.MOIS1;
		else if (ClassMois.MOIS2.name().equalsIgnoreCase(classMoisParam)) {
			classMois = ClassMois.MOIS2;
		} else {
			classMois = ClassMois.MOIS3;
		}
		double tauxProrota = 1 ;
		 while (listSalaries.hasNext()) {
			 
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			   if(isSamePersonWithProrota(salarie , nextSalarie)){
				   Date dateEffectiveRegime;
				   int totalDayMonth ;
				   int nbreDayProrota ;
				   if( salarie.getFieldAndMDForPath(pathRegimeCadre.get(classMois)).getValue() != null && ((Bool) salarie.getFieldAndMDForPath(pathRegimeCadre.get(classMois)).getValue()).isTrue() )
					   {
						   dateEffectiveRegime = (Date) salarie.getFieldAndMDForPath(pathDateEffectiveRegime.get(classMois)).getValue();
						   if(getClassMois(dateEffectiveRegime, dateEffectiveRegime).equalsIgnoreCase(classMois.name()))
						   {
							   totalDayMonth = dateEffectiveRegime.getMonthValue().getDaysInMonth();
							   nbreDayProrota = totalDayMonth - dateEffectiveRegime.getDay() + 1;
							   tauxProrota = (double)nbreDayProrota/totalDayMonth; 
						   }
					   }
				   else if ( nextSalarie.getFieldAndMDForPath(pathRegimeCadre.get(classMois)).getValue() !=null && ((Bool) nextSalarie.getFieldAndMDForPath(pathRegimeCadre.get(classMois)).getValue()).isTrue() ) 
				   	  {
					   	dateEffectiveRegime = (Date) nextSalarie.getFieldAndMDForPath(pathDateEffectiveRegime.get(classMois)).getValue();
					   	if(getClassMois(dateEffectiveRegime, dateEffectiveRegime).equalsIgnoreCase(classMois.name()))
						 {
					   		totalDayMonth = dateEffectiveRegime.getMonthValue().getDaysInMonth();
							nbreDayProrota = dateEffectiveRegime.getDay() - 1;
							tauxProrota = (double)nbreDayProrota/totalDayMonth ;
							return tauxProrota;
					     }
				     }
			   }
		}
		return tauxProrota;
	}
	
	private Boolean isSamePersonWithProrota(COTSInstanceListNode salarie1 , COTSInstanceListNode salarie2) {
		
		String typePieceIdentite1 = salarie1.getFieldAndMDForPath(TYPE_PIECE_IDENTITE_PATH).getXMLValue();
		String numeroPiece1 = salarie1.getFieldAndMDForPath(NUMERO_PIECE_IDENTITE_PATH).getXMLValue();
		String pk1 = salarie1.getFieldAndMDForPath("pk").getXMLValue();
		String typePieceIdentite2 = salarie2.getFieldAndMDForPath(TYPE_PIECE_IDENTITE_PATH).getXMLValue();
		String numeroPiece2 = salarie2.getFieldAndMDForPath(NUMERO_PIECE_IDENTITE_PATH).getXMLValue();
		String pk2 = salarie2.getFieldAndMDForPath("pk").getXMLValue();
		Boolean valeurBool = false;
		if(typePieceIdentite1 != null && numeroPiece1 != null && typePieceIdentite2 != null && numeroPiece2 != null && pk1 != pk2) {
			
			valeurBool = typePieceIdentite1.equals(typePieceIdentite2) ? numeroPiece1.equals(numeroPiece2) : false ;
		}
		
		return valeurBool;
	}

}
