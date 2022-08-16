package com.sms.envio.entity;

public class Parametros {

	private String[] receptor;
	private String[] mensajeLatinoAmerica;

	public Parametros(String[] receptor, String[] mensajeLatinoAmerica) {
		super();

		this.receptor = receptor;
		this.mensajeLatinoAmerica = mensajeLatinoAmerica;
	}

	public Parametros() {

	}

	public String[] getReceptor() {
		return receptor;
	}

	public void setReceptor(String[] receptor) {
		this.receptor = receptor;
	}

	public String[] getMensajeLatinoAmerica() {
		return mensajeLatinoAmerica;
	}

	public void setMensajeLatinoAmerica(String[] mensajeLatinoAmerica) {
		this.mensajeLatinoAmerica = mensajeLatinoAmerica;
	}
}
