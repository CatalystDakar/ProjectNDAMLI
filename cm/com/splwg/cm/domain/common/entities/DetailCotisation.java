package com.splwg.cm.domain.common.entities;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.datatypes.Date;

public class DetailCotisation {
	
	private String nomEmployeur;
	private String annee;
	private Date dateDebut;
	private Date dateFin;
	private BigDecimal toTalSalaire;
	
	public DetailCotisation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DetailCotisation(String nomEmployeur, String annee, Date dateDebut, Date dateFin, BigDecimal toTalSalaire) {
		super();
		this.nomEmployeur = nomEmployeur;
		this.annee = annee;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.toTalSalaire = toTalSalaire;
	}

	public String getNomEmployeur() {
		return nomEmployeur;
	}

	public void setNomEmployeur(String nomEmployeur) {
		this.nomEmployeur = nomEmployeur;
	}

	public String getAnnee() {
		return annee;
	}

	public void setAnnee(String annee) {
		this.annee = annee;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public BigDecimal getToTalSalaire() {
		return toTalSalaire;
	}

	public void setToTalSalaire(BigDecimal toTalSalaire) {
		this.toTalSalaire = toTalSalaire;
	}
	
	

}
