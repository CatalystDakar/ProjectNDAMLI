package com.splwg.cm.domain.admin.formRule;

import java.text.DecimalFormat;
import java.util.Iterator;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Money;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;

/**
 * @author Khadim Cisse
 *
 * @AlgorithmComponent ()
 */
public class CmCalcCotisation_Impl extends CmCalcCotisation_Gen implements FormRuleBORuleProcessingAlgorithmSpot {

	private static final Logger logger = LoggerFactory.getLogger(CmCalcCotisation.class);
	ApplyFormRuleAlgorithmInputData inputData;
	ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	private static int facteur=3;
	

	@Override
	public void invoke() {
		//Related Transaction BO du form Type FORM_DNS
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		COTSInstanceNode group = formBoInstance.getGroupFromPath("informationSalaries");
		BigDecimal nombreSalaries = (BigDecimal) formBoInstance.getFieldAndMDForPath("synthese/totalSalaries/asCurrent").getValue();
		//Nombre employes qui permet de determiner le calendrier utilise
		int valeurFacteurNombreSalarie=10;
		
		//Chargement montant cotisation Accident Travail au niveau du formulaire DNS
		double montantAtMp=this.getMontantCotisationAT_MP(group.getList("informationSalariesList").iterator(),valeurFacteurNombreSalarie, nombreSalaries.intValue());
		COTSFieldDataAndMD<Money> fieldMontantAtMp = inputOutputData.getFormBusinessObject().getFieldAndMDForPath("synthese/montantATMP/asCurrent");
		fieldMontantAtMp.setXMLValue(String.valueOf(montantAtMp));
		
		//Chargement montant cotisation Prestation Familiale au niveau du formulaire DNS
		double montantPF=this.getMontantCotisationPF(group.getList("informationSalariesList").iterator(),valeurFacteurNombreSalarie, nombreSalaries.intValue());
		COTSFieldDataAndMD<Money> fieldMontantPF = inputOutputData.getFormBusinessObject().getFieldAndMDForPath("synthese/montantPF/asCurrent");
		fieldMontantPF.setXMLValue(String.valueOf(montantPF));
		
		//Chargement montant cotisation Retraite Regime General au niveau du formulaire DNS
		double montantCotisationRetraiteRG=this.getMontantCotisationRetraiteRG(group.getList("informationSalariesList").iterator(),valeurFacteurNombreSalarie, nombreSalaries.intValue());
		COTSFieldDataAndMD<?> fieldMontantCotisationRetraiteRG = inputOutputData.getFormBusinessObject().getFieldAndMDForPath("synthese/montantRRG/asCurrent");
		fieldMontantCotisationRetraiteRG.setXMLValue(String.valueOf(montantCotisationRetraiteRG));
		
		//Chargement montant Cotisation Retraite Regime Cadre au niveau du formulaire DNS
		double montantCotisationRetraiteRC=this.getMontantCotisationRetraiteRC(group.getList("informationSalariesList").iterator(),valeurFacteurNombreSalarie, nombreSalaries.intValue());
		COTSFieldDataAndMD<?> fieldMontantCotisationRetraiteRC = inputOutputData.getFormBusinessObject().getFieldAndMDForPath("synthese/montantRCC/asCurrent");
		fieldMontantCotisationRetraiteRC.setXMLValue(String.valueOf(montantCotisationRetraiteRC));
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
	private double getMontantCotisationPF(Iterator<COTSInstanceListNode> listSalaries, int valeurFacteurNombreSalarie,int nombreSalaries) {
		  //Montant total PF employeur 
		  double montantTotal=0; 
		  //Montant Cotisation PF par salarie
		  double montantCotisation=0;
		  
		  while (listSalaries.hasNext()) {
		   COTSInstanceListNode nextSalarie = listSalaries.next();
		   if (nextSalarie != null) {
			//Recuperation des informations necessaire pour les calcules à partir du formulaire
			Money smig = (Money) nextSalarie.getFieldAndMDForPath("smig/asCurrent").getValue();
			Money plafondCssPf = (Money) nextSalarie.getFieldAndMDForPath("plafondCssCpf/asCurrent").getValue();
			if(nombreSalaries<valeurFacteurNombreSalarie){
				smig=mutiplyParFacteur(smig,facteur);
				plafondCssPf=mutiplyParFacteur(plafondCssPf,facteur);
			}

		    Money totalAmont = (Money) nextSalarie.getFieldAndMDForPath("totSalAssCssPfe/asCurrent").getValue();
		    BigDecimal tauxCssPf = (BigDecimal) nextSalarie.getFieldAndMDForPath("tauxCssCpf/asCurrent").getValue();

		    if(totalAmont.getAmount().doubleValue()<=smig.getAmount().doubleValue()){
		    	//TOTAL_MNT_COT_CSS < PLF_SMIG
		    	//MONTANT_COTIS_SALARIE=PLF_SMIG*TXE_CSS_CPF
		    	montantCotisation= smig.getAmount().doubleValue()*tauxCssPf.doubleValue()/100;
		    }
		    else if(plafondCssPf.getAmount().doubleValue()>totalAmont.getAmount().doubleValue() && totalAmont.getAmount().doubleValue()>=smig.getAmount().doubleValue()){
		    	//PLF_CSS_CPF > TOTAL_MNT_COT_CSS > = PLF_SMIG
		    	//MONTANT_COTIS_SALARIE=TOTAL_MNT_COT_CSS * TXE_CSS_CPF
		    	montantCotisation = totalAmont.getAmount().doubleValue()*tauxCssPf.doubleValue()/100;
		    }
		    else{
		    	//TOTAL_MNT_COT_CSS > PLF_CSS_CPF
		    	//MONTANT_COTIS_SALARIE=PLF_CSS_CPF * TXE_CSS_CPF
		    	montantCotisation = plafondCssPf.getAmount().doubleValue()*tauxCssPf.doubleValue()/100;
		    }
		   }
		   //if(nombreSalaries<valeurFacteurNombreSalarie)montantCotisation=montantCotisation*3;
		   montantTotal+= montantCotisation;
		   
		   int intMontantCotisation = (int) Math.round(montantCotisation);
		   //Chargement montant cotisation PF du salarie au niveau du formulaire
		   nextSalarie.getFieldAndMDForPath("montantCssCpf/asCurrent").setXMLValue(Integer.toString(intMontantCotisation));
		  }  

		  int intMontantTotal = (int) Math.round(montantTotal);
		  return Double.valueOf(intMontantTotal);
		 }

	/**
	 * Cette methode calcule le montant de cotisation AT/MP d'un enployeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries de l'employeur
	 * @return montant de cotisation AT/MP
	 */
	private double getMontantCotisationAT_MP(Iterator<COTSInstanceListNode> listSalaries, int valeurFacteurNombreSalarie,int nombreSalaries ) {
		//DecimalFormat roundMontant = new DecimalFormat(".##");
		
		//Montant total AT/MP employeur 
		double montantTotal=0;
		//Montant Cotisation AT/MP par salarie
		double montantCotisation=0;
		  while (listSalaries.hasNext()) {
		   COTSInstanceListNode nextSalarie = listSalaries.next();
		   if (nextSalarie != null) {
			   //Recuperation des informations necessaire pour les calcules à partir du formulaire
			   Money smig = (Money) nextSalarie.getFieldAndMDForPath("smig/asCurrent").getValue();
			   Money plafondCssAtMp = (Money) nextSalarie.getFieldAndMDForPath("plafondCssAtMp/asCurrent").getValue();
			   if(nombreSalaries<valeurFacteurNombreSalarie){
					smig=mutiplyParFacteur(smig,facteur);
					plafondCssAtMp=mutiplyParFacteur(plafondCssAtMp,facteur);
				}
			   BigDecimal tauxCssCatmp = (BigDecimal) nextSalarie.getFieldAndMDForPath("tauxCssCatmp/asCurrent").getValue();
			   Money totalAmont=(Money) nextSalarie.getFieldAndMDForPath("totSalAssCssAtmpe/asCurrent").getValue();
			   
			   
			    if(totalAmont.getAmount().doubleValue()<=smig.getAmount().doubleValue()){ 
			    	//TOTAL_MNT_COT_CSS < PLF_SMIG
			    	//MONTANT_COTIS_SALARIE=TAUX_AT/MP*PLF_SMIG
			    	montantCotisation=smig.getAmount().doubleValue()*tauxCssCatmp.doubleValue()/100;
			    }else{
			    	if(totalAmont.getAmount().doubleValue()>smig.getAmount().doubleValue() && totalAmont.getAmount().doubleValue()<plafondCssAtMp.getAmount().doubleValue()){
			    		//PLF_CSS_CATMP > TOTAL_MNT_COT_CSS > = PLF_SMIG
				    	//MONTANT_COTIS_SALARIE=TAUX_AT/MP*TOTAL_MNT_COT_CSS
			    		montantCotisation=totalAmont.getAmount().doubleValue()*tauxCssCatmp.doubleValue()/100;
			    	}else{
			    		//MONTANT_COTIS_SALARIE=TAUX_AT/MP*PLF_CSS_CATMP
			    		montantCotisation=plafondCssAtMp.getAmount().doubleValue()*tauxCssCatmp.doubleValue()/100;
			    	}
			    }
		   	}
		   //Multiplier le montant de cotisation par 3 si le calendrier est trimestriel
		  // if(nombreSalaries<valeurFacteurNombreSalarie) montantCotisation=montantCotisation*3;
		   montantTotal+=montantCotisation; 
		   int intMontantCotisation = (int) Math.round(montantCotisation);
    	   //Montant AT MP Salarié
 		   nextSalarie.getFieldAndMDForPath("montantCssAtMp/asCurrent").setXMLValue(String.format(Integer.toString(intMontantCotisation)));	 
		  }  
		  
		  int intMontantTotal = (int) Math.round(montantTotal);
		  return Double.valueOf(intMontantTotal);
	}
	/**
	 * Cette methode calcule le montant de cotisation retraite regime general d'un enployeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries de l'employeur
	 * @return montant de cotisation retraite regime general
	 */
	private double getMontantCotisationRetraiteRG(Iterator<COTSInstanceListNode> listSalaries,int valeurFacteurNombreSalarie,int nombreSalaries ) {
		//DecimalFormat roundMontant = new DecimalFormat(".##");
		double montantTotal=0;
		double montantCotisation=0;
		while (listSalaries.hasNext()) {
			   //List des salaries
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			   if (nextSalarie != null) {
				 //Recuperation des informations necessaire pour les calcules à partir du formulaire
				   Money smig = (Money) nextSalarie.getFieldAndMDForPath("smig/asCurrent").getValue();
				   Money plafondRetraiteRG = (Money) nextSalarie.getFieldAndMDForPath("plafondIpresCrrg/asCurrent").getValue();
				   if(nombreSalaries<valeurFacteurNombreSalarie){
						smig=mutiplyParFacteur(smig,facteur);
						plafondRetraiteRG=mutiplyParFacteur(plafondRetraiteRG,facteur);
					}
				   BigDecimal tauxRetraiteRG = (BigDecimal) nextSalarie.getFieldAndMDForPath("tauxIpresCrrg/asCurrent").getValue();
				   Money totalAmont=(Money) nextSalarie.getFieldAndMDForPath("totSalAssIpresRge/asCurrent").getValue();  
					 if(totalAmont.getAmount().doubleValue()<=smig.getAmount().doubleValue()){
						 //TOTAL_MNT_COT_IPRES<PLF_SMIG
						 //MONTANT_COTIS_SALARIE=TXE_IPRES_CRRG*TOTAL_MNT_COT_IPRES
						 montantCotisation=smig.getAmount().doubleValue()*tauxRetraiteRG.doubleValue()/100;
					 }else{
						 	if(totalAmont.getAmount().doubleValue()>smig.getAmount().doubleValue() && totalAmont.getAmount().doubleValue()<plafondRetraiteRG.getAmount().doubleValue()){
						 		//PLF_SMIG<TOTAL_MNT_COT_IPRES<PLF_IPRES_CRRG
								//MONTANT_COTIS_SALARIE=TXE_IPRES_CRRG*PLF_IPRES_CRRG
						 		montantCotisation=totalAmont.getAmount().doubleValue()*tauxRetraiteRG.doubleValue()/100;
						 	}else{
						 		//TOTAL_MNT_COT_IPRES>PLF_IPRES_CRRG
								//MONTANT_COTIS_SALARIE=TXE_IPRES_CRRG*TOTAL_MNT_COT_IPRES
						 		montantCotisation=plafondRetraiteRG.getAmount().doubleValue()*tauxRetraiteRG.doubleValue()/100;
						 	}
					 }
					 //if(nombreSalaries < valeurFacteurNombreSalarie) montantCotisation=montantCotisation*3;
					 montantTotal+=montantCotisation;
					 int intMontantCotisation = (int) Math.round(montantCotisation);
					 //Montant Cotisé par le salarié
					 nextSalarie.getFieldAndMDForPath("montantIpresCrrg/asCurrent").setXMLValue(String.format(Integer.toString(intMontantCotisation)));
			   	}
			  }  
	
		  int intMontantTotal = (int) Math.round(montantTotal);
		  return Double.valueOf(intMontantTotal);
	}
	
	
	
	/**
	 * Cette methode calcule le montant de cotisation retraite regime cadre d'un enployeur.
	 * Elle calcule le montant pour chaque salarie et faire la somme.
	 * @param listSalaries liste des salaries de l'employeur
	 * @return montant de cotisation retraite regime cadre
	 */
	private double getMontantCotisationRetraiteRC(Iterator<COTSInstanceListNode> listSalaries,int valeurFacteurNombreSalarie,int nombreSalaries ) {
		//DecimalFormat roundMontant = new DecimalFormat(".##");
		double montantTotal=0;
		double montantCotisation=0;
		while (listSalaries.hasNext()) {
			   //List des salaries
			   COTSInstanceListNode nextSalarie = listSalaries.next();
			   if (nextSalarie != null) {
				   //Recuperation des informations necessaire pour les calcules à partir du formulaire
				   Money smig = (Money) nextSalarie.getFieldAndMDForPath("smig/asCurrent").getValue();
				   Money plafondRetraiteRC = (Money) nextSalarie.getFieldAndMDForPath("plafondIpresCrcc/asCurrent").getValue();
				   if(nombreSalaries<valeurFacteurNombreSalarie){
						smig=mutiplyParFacteur(smig,facteur);
						plafondRetraiteRC=mutiplyParFacteur(plafondRetraiteRC,facteur);
					}
				   BigDecimal tauxRetraiteRC = (BigDecimal) nextSalarie.getFieldAndMDForPath("tauxIpresCrcc/asCurrent").getValue();
				   Money totalAmont=(Money) nextSalarie.getFieldAndMDForPath("totSalAssIpresRcce/asCurrent").getValue();
				   Bool salarieCadre=(Bool) nextSalarie.getFieldAndMDForPath("regimCompCadre/asCurrent").getValue();
				 
				   if(salarieCadre.isTrue())
				   {   
					   if(totalAmont.getAmount().doubleValue()<=smig.getAmount().doubleValue()){ 
						   //TOTAL_MNT_COT_IPRES<PLF_SMIG
						   //MONTANT_COTIS_SALARIE=TXE_IPRES_CRCC*TOTAL_MNT_COT_IPRES
						   montantCotisation=smig.getAmount().doubleValue()*tauxRetraiteRC.doubleValue()/100;
						 }else{
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
					   //if(nombreSalaries < valeurFacteurNombreSalarie) montantCotisation=montantCotisation*3;
					   montantTotal+=montantCotisation;
					   
					   int intMontantCotisation = (int) Math.round(montantCotisation);
					 //Montant Cotisé par le salarié
					 nextSalarie.getFieldAndMDForPath("montantIpresCrcc/asCurrent").setXMLValue(String.format(Integer.toString(intMontantCotisation)));
				   }
			   	}
			  }  
		  int intMontantTotal = (int) Math.round(montantTotal);
		  return Double.valueOf(intMontantTotal);
	}
	/**
	 * Utiliser pour nultiplier les valeurs de type Money par un facteur
	 * @param montant
	 * @param facteur
	 * @return
	 */
	public Money mutiplyParFacteur(Money montant, int facteur){
		BigDecimal factBigDecimal=new BigDecimal(facteur);
		BigDecimal montantBigDecimal=montant.getAmount();
		BigDecimal resultBigDecimal=montantBigDecimal.multiply(factBigDecimal);
		Money resultMoney=new Money(resultBigDecimal);
		return resultMoney;
	}

}
