package com.splwg.cm.domain.admin.formRule;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.cm.domain.common.entities.Employee;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formType.FormType;
import com.splwg.tax.domain.admin.formType.FormType_Id;

/**
 * @author Denash Kumar M
 *
 * @AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name =formType, type = string) 
 * 						, @AlgorithmSoftParameter (name= filePath, type = string)
 * 						, @AlgorithmSoftParameter (name = successFilePath, type = string)
 *                     	, @AlgorithmSoftParameter (name = errorFilePath, type =string)})
 */

public class CmEmployeeRegAlgo_Impl extends CmEmployeeRegAlgo_Gen implements BusinessObjectExitStatusAlgorithmSpot {

	private BusinessObjectInstanceKey boKey;
	private BusinessObjectInstance boInstance;
	private final static Logger log = LoggerFactory.getLogger(CmEmployeeRegAlgo_Impl.class);
	private String fileName = null;

	public static final String AS_CURRENT = "asCurrent";
	String nineaNumber=null;

	@Override
	public void invoke() {

		 System.out.println("I am In Invoke method " + this.boKey);
		 log.info("I am In Invoke method BO intance Key " + this.boKey); 
		 this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		 log.info("I am In Invoke method BO intance " + this.boInstance);  
		 COTSFieldDataAndMD cots =
		 this.boInstance.getFieldAndMDForPath("employerDetails/ninea");
		 nineaNumber = cots.getValue().toString();

		//String nineaNumber = "156542685";
		fileName = nineaNumber + "EMPLE" + ".csv";
		boolean fileExist = verifyExistFileInFolder(this.getFilePath() + fileName);
		if (fileExist) {
			// this.processLookup();
			readExcelFileAndPostForm(this.getFilePath() + fileName);
		} else {
			// createToDo("", nineaNumber, "", fileName);
			// addError(CmMessageRepository90000.MSG_10001());
		}
	}

	private boolean verifyExistFileInFolder(String fileName) {
		Path path = Paths.get(fileName);
		boolean isExits = false;
		if (Files.exists(path)) {
			isExits = true;
		}
		return isExits;

	} 

	private void readExcelFileAndPostForm(String regFileName) {

		log.info("I am in readExcelFileAndPostForm: " + regFileName);
		System.out.println("I am in readExcelFileAndPostForm: " + regFileName);  

		File file = null;
		file = new File(regFileName);
		boolean processed = false;
		List<Employee> listValues = new ArrayList<Employee>();

		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("File Not Found ");
			System.out.println("File Not Found ");
		}

		int count = 0;
		Employee empCurrent = null;
		while (scanner.hasNextLine()) {
			String csvValues = scanner.nextLine();
			csvValues = csvValues.replace("é", "e");
			csvValues = csvValues.replace("è", "e");
			csvValues = csvValues.replace("à", "a");
			System.out.println("***Reading From File****Line Number**** " + csvValues);
			if (count >= 1) {
				String[] terms = csvValues.split(",");
				empCurrent = chargementDonnees(terms);
				listValues.add(empCurrent);
			}			
			count++;

		}
		System.out.println("ArrayList: " + listValues);
		log.info("ArrayList: " + listValues);
		formCreator(listValues);

		/*
		 * if (processed) { customHelper.moveFileToProcessedFolder(fileName,
		 * this.getParameters().getPathToMove()); } else {
		 * customHelper.moveFileToFailuireFolder(fileName,
		 * this.getParameters().getErrorFilePathToMove()); }
		 */

		System.out.println("######################## Terminer executeWorkUnit ############################");
	}

	private String convertDateFormat(String dateObject) {
		String parsedDate = "";

		try {
			if (dateObject.contains("GMT")) {
				DateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss 'GMT' yyyy");
				java.util.Date date = inputFormat.parse(dateObject);
				DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				parsedDate = outputFormat.format(date);
			} else if (dateObject.contains("UTC")) {
				DateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss 'UTC' yyyy", Locale.ENGLISH);
				java.util.Date date = inputFormat.parse(dateObject);
				DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				parsedDate = outputFormat.format(date);
			} else if (dateObject.contains("CST")) {
				DateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss 'CST' yyyy", Locale.ENGLISH);
				java.util.Date date = inputFormat.parse(dateObject);
				DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				parsedDate = outputFormat.format(date);
			} else if (dateObject.contains("CDT")) {
				DateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss 'CDT' yyyy", Locale.ENGLISH);
				java.util.Date date = inputFormat.parse(dateObject);
				DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				parsedDate = outputFormat.format(date);
			} else {
				DateFormat inputFormat1 = new SimpleDateFormat("dd/MM/yyyy");
				java.util.Date input = inputFormat1.parse(dateObject);
				DateFormat outputFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				parsedDate = outputFormat1.format(input);
			}
		} catch (Exception exception) {
			parsedDate = "invalidate";
			log.error("Error while parsing the date format" + exception);
		}
		return parsedDate;
	}

	private void formCreator(List<Employee> listeEmployes) {

		BusinessObjectInstance boInstance = null;

		boInstance = createFormBOInstance(this.getFormType(), "EMPLOYEE_REG-" + getSystemDateTime().toString());

		COTSInstanceNode employeurSection = boInstance.getGroup("employeur");
		COTSInstanceNode employeeSection = boInstance.getGroup("employe");

		COTSFieldDataAndMD<?> ninea = employeurSection.getFieldAndMDForPath("ninea/asCurrent");
		ninea.setXMLValue(nineaNumber);
		
		COTSFieldDataAndMD<BigDecimal> maxPk = employeeSection.getFieldAndMDForPath("maxPk");
		maxPk.setValue(new BigDecimal(listeEmployes.size()));
		int i=1;
		
		for(Employee emp: listeEmployes){
			COTSInstanceListNode element = employeeSection.getList("employeList").newChild();
			
			COTSFieldDataAndMD<BigDecimal> pk = element.getFieldAndMDForPath("pk");
			pk.setValue(new BigDecimal(i)); 
			
			COTSFieldDataAndMD<?> nomEmploye = element.getFieldAndMDForPath("nomEmploye/asCurrent");
			nomEmploye.setXMLValue(emp.getNomEmploye());
		
			COTSFieldDataAndMD<?> prenomEmploye = element.getFieldAndMDForPath("prenomEmploye/asCurrent");
			prenomEmploye.setXMLValue(emp.getPrenomEmploye());
		
			
			COTSFieldDataAndMD<?> sexe = element.getFieldAndMDForPath("sexe/asCurrent");
			sexe.setXMLValue(emp.getSexe());

			COTSFieldDataAndMD<?> etatCivil = element.getFieldAndMDForPath("etatCivil/asCurrent");
			etatCivil.setXMLValue(emp.getEtatCivil());

			COTSFieldDataAndMD<?> dateDeNaissance = element.getFieldAndMDForPath("dateDeNaissance/asCurrent");
			dateDeNaissance.setXMLValue(convertDateFormat(emp.getDateDeNaissance()));

			COTSFieldDataAndMD<?> numRegNaiss = element.getFieldAndMDForPath("numRegNaiss/asCurrent");
			numRegNaiss.setXMLValue(emp.getNumRegNaiss());
		

			COTSFieldDataAndMD<?> nomPere = element.getFieldAndMDForPath("nomPere/asCurrent");
			nomPere.setXMLValue(emp.getNomPere());


			COTSFieldDataAndMD<?> prenomPere = element.getFieldAndMDForPath("prenomPere/asCurrent");
			prenomPere.setXMLValue(emp.getPrenomPere());
	

			COTSFieldDataAndMD<?> nomMere = element.getFieldAndMDForPath("nomMere/asCurrent");
			nomMere.setXMLValue(emp.getNomMere());
			

			COTSFieldDataAndMD<?> prenomMere = element.getFieldAndMDForPath("prenomMere/asCurrent");
			prenomMere.setXMLValue(emp.getPrenomMere());
			

			COTSFieldDataAndMD<?> nationalite = element.getFieldAndMDForPath("nationalite/asCurrent");
			nationalite.setXMLValue(emp.getNationalite());
			

			COTSFieldDataAndMD<?> typePieceIdentite = element.getFieldAndMDForPath("typePieceIdentite/asCurrent");
			typePieceIdentite.setXMLValue(emp.getTypePieceIdentite());
			

			COTSFieldDataAndMD<?> nin = element.getFieldAndMDForPath("nin/asCurrent");
			nin.setXMLValue(emp.getNin());
			

			COTSFieldDataAndMD<?> ninCEDEAO = element.getFieldAndMDForPath("ninCEDEAO/asCurrent");
			ninCEDEAO.setXMLValue(emp.getNinCEDEAO());
			

			COTSFieldDataAndMD<?> numPieceIdentite = element.getFieldAndMDForPath("numPieceIdentite/asCurrent");
			numPieceIdentite.setXMLValue(emp.getNumPieceIdentite());
			

			COTSFieldDataAndMD<?> delivreLe = element.getFieldAndMDForPath("delivreLe/asCurrent");
			delivreLe.setXMLValue(convertDateFormat(emp.getDelivreLe()));
			

			COTSFieldDataAndMD<?> lieuDelivrance = element.getFieldAndMDForPath("lieuDelivrance/asCurrent");
			lieuDelivrance.setXMLValue(emp.getLieuDelivrance());
			

			COTSFieldDataAndMD<?> expireLe = element.getFieldAndMDForPath("expireLe/asCurrent");
			expireLe.setXMLValue(convertDateFormat(emp.getExpireLe()));
			

			COTSFieldDataAndMD<?> paysDeNaissance = element.getFieldAndMDForPath("paysDeNaissance/asCurrent");
			paysDeNaissance.setXMLValue(emp.getPaysDeNaissance());
			

			COTSFieldDataAndMD<?> villeDeNaissance = element.getFieldAndMDForPath("villeDeNaissance/asCurrent");
			villeDeNaissance.setXMLValue(emp.getVilleDeNaissance());
			

			COTSFieldDataAndMD<?> pays = element.getFieldAndMDForPath("pays/asCurrent");
			pays.setXMLValue(emp.getPays());
			

			COTSFieldDataAndMD<?> region = element.getFieldAndMDForPath("region/asCurrent");
			region.setXMLValue(emp.getRegion());
			

			COTSFieldDataAndMD<?> departement = element.getFieldAndMDForPath("departement/asCurrent");
			departement.setXMLValue(emp.getDepartement());
			

			COTSFieldDataAndMD<?> arrondissement = element.getFieldAndMDForPath("arrondissement/asCurrent");
			arrondissement.setXMLValue(emp.getArrondissement());
			

			COTSFieldDataAndMD<?> commune = element.getFieldAndMDForPath("commune/asCurrent");
			commune.setXMLValue(emp.getCommune());
			

			COTSFieldDataAndMD<?> quartier = element.getFieldAndMDForPath("quartier/asCurrent");
			quartier.setXMLValue(emp.getQuartier());
			

			COTSFieldDataAndMD<?> adresse = element.getFieldAndMDForPath("adresse/asCurrent");
			adresse.setXMLValue(emp.getAdresse());
			

			COTSFieldDataAndMD<?> boitePostale = element.getFieldAndMDForPath("boitePostale/asCurrent");
			boitePostale.setXMLValue(emp.getBoitePostale());
			

			COTSFieldDataAndMD<?> typeMouvement = element.getFieldAndMDForPath("typeMouvement/asCurrent");
			typeMouvement.setXMLValue(emp.getTypeMouvement());
			

			COTSFieldDataAndMD<?> natureContrat = element.getFieldAndMDForPath("natureContrat/asCurrent");
			natureContrat.setXMLValue(emp.getNatureContrat());
			

			COTSFieldDataAndMD<?> dateDebut = element.getFieldAndMDForPath("dateDebut/asCurrent");
			dateDebut.setXMLValue(convertDateFormat(emp.getDateDebut()));
			

			COTSFieldDataAndMD<?> dateFinContrat = element.getFieldAndMDForPath("dateFinContrat/asCurrent");
			dateFinContrat.setXMLValue(emp.getDateFinContrat());
			

			COTSFieldDataAndMD<?> profession = element.getFieldAndMDForPath("profession/asCurrent");
			profession.setXMLValue(emp.getProfession());
			

			COTSFieldDataAndMD<?> emploi = element.getFieldAndMDForPath("emploi/asCurrent");
			emploi.setXMLValue(emp.getEmploi());
			

			COTSFieldDataAndMD<?> employeCadre = element.getFieldAndMDForPath("nonEmp/asCurrent");
			employeCadre.setXMLValue(emp.getEmployeCadre());
			

			COTSFieldDataAndMD<?> conventionApplicable = element.getFieldAndMDForPath("conventionApplicable/asCurrent");
			conventionApplicable.setXMLValue(emp.getConventionApplicable());
			

			COTSFieldDataAndMD<?> salaireContractuel = element.getFieldAndMDForPath("salaireContractuel/asCurrent");
			salaireContractuel.setXMLValue(emp.getSalaireContractuel());
			

			COTSFieldDataAndMD<?> tpsDeTravail = element.getFieldAndMDForPath("tpsDeTravail/asCurrent");
			tpsDeTravail.setXMLValue(emp.getTpsDeTravail());
			

			COTSFieldDataAndMD<?> categorie = element.getFieldAndMDForPath("categorie/asCurrent");
			categorie.setXMLValue(emp.getCategorie());		
			i++;
		}
		
		
		 if (boInstance != null) {
		 boInstance = validateAndPostForm(boInstance);
		 }

	}

	/**
	 * Method to create FormBOInstance
	 * 
	 * @param formType
	 * @param string
	 * @return
	 */
	private BusinessObjectInstance createFormBOInstance(String formTypeString, String documentLocator) {

		FormType formType = new FormType_Id(formTypeString).getEntity();
		String formTypeBo = formType.getRelatedTransactionBOId().getTrimmedValue();

		log.info("#### Creating BO for " + formType);

		BusinessObjectInstance boInstance = BusinessObjectInstance.create(formTypeBo);

		log.info("#### Form Type BO MD Schema: " + boInstance.getSchemaMD());

		boInstance.set("bo", formTypeBo);
		boInstance.set("formType", formType.getId().getTrimmedValue());
		boInstance.set("receiveDate", getSystemDateTime().getDate());
		boInstance.set("documentLocator", documentLocator);

		return boInstance;

	}

	private Employee chargementDonnees(String[] terms) {
		int numero = 1;
		Employee emp = new Employee();
		while (numero == 1) {
			System.out.println("FIRST " +terms[numero]); 
			emp.setNomEmploye(terms[numero]);
			numero++;
			emp.setPrenomEmploye(terms[numero]);
			numero++;
			emp.setSexe(terms[numero]);
			numero++;
			emp.setEtatCivil(terms[numero]);
			numero++;
			emp.setDateDeNaissance(terms[numero]);
			numero++;
			emp.setNumRegNaiss(terms[numero]);
			numero++;
			emp.setNomPere(terms[numero]);
			numero++;
			emp.setPrenomPere(terms[numero]);
			numero++;
			emp.setNomMere(terms[numero]);
			numero++;
			emp.setPrenomMere(terms[numero]);
			numero++;
			emp.setNationalite(terms[numero]);
			numero++;
			emp.setTypePieceIdentite(terms[numero]);
			numero++;
			emp.setNin(terms[numero]);
			numero++;
			emp.setNinCEDEAO(terms[numero]);
			numero++;
			emp.setNumPieceIdentite(terms[numero]);
			numero++;
			emp.setDelivreLe(terms[numero]);
			numero++;
			emp.setLieuDelivrance(terms[numero]);
			numero++;
			emp.setExpireLe(terms[numero]);
			numero++;
			emp.setPaysDeNaissance(terms[numero]);
			numero++;
			emp.setVilleDeNaissance(terms[numero]);
			numero++;
			emp.setPays(terms[numero]);
			numero++;
			emp.setRegion(terms[numero]);
			numero++;
			emp.setDepartement(terms[numero]);
			numero++;
			emp.setArrondissement(terms[numero]);
			numero++;
			emp.setCommune(terms[numero]);
			numero++;
			emp.setQuartier(terms[numero]);
			numero++;
			emp.setAdresse(terms[numero]);
			numero++;
			emp.setBoitePostale(terms[numero]);
			numero++;
			emp.setTypeMouvement(terms[numero]);
			numero++;
			emp.setNatureContrat(terms[numero]);
			numero++;
			emp.setDateDebut(terms[numero]);
			numero++;
			emp.setDateFinContrat(terms[numero]);
			numero++;
			emp.setProfession(terms[numero]);
			numero++;
			emp.setEmploi(terms[numero]);
			numero++;
			emp.setEmployeCadre(terms[numero]);
			numero++;
			emp.setConventionApplicable(terms[numero]);
			numero++;
			emp.setSalaireContractuel(terms[numero]);
			numero++;
			emp.setTpsDeTravail(terms[numero]);
			numero++;
			emp.setCategorie(terms[numero]);
			numero++;
		}
		return emp;
	}

	private BusinessObjectInstance validateAndPostForm(BusinessObjectInstance boInstance) {

		log.info("#### BO Instance Schema before ADD: " + boInstance.getDocument().asXML());
		boInstance = BusinessObjectDispatcher.add(boInstance);
		log.info("#### BO Instance Schema after ADD: " + boInstance.getDocument().asXML());

		boInstance.set("boStatus", "VALIDATE");
		boInstance = BusinessObjectDispatcher.update(boInstance);
		log.info("#### BO Instance Schema after VALIDATE: " + boInstance.getDocument().asXML());

		boInstance.set("boStatus", "READYFORPOST");
		boInstance = BusinessObjectDispatcher.update(boInstance);
		log.info("#### BO Instance Schema after READYFORPOST: " + boInstance.getDocument().asXML());

		boInstance.set("boStatus", "POSTED");
		boInstance = BusinessObjectDispatcher.update(boInstance);
		log.info("#### BO Instance Schema after POSTED: " + boInstance.getDocument().asXML());

		COTSFieldDataAndMD cots = this.boInstance.getFieldAndMDForPath("employees/formId");
		cots.setXMLValue(boInstance.getFieldAndMDForPath("registrationFormId").getXMLValue());
		BusinessObjectDispatcher.update(this.boInstance);

		return boInstance;

	}


	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		// TODO Auto-generated method stub
		this.boKey = arg0;
	}

}
