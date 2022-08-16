package com.sms.envio.	entity;

public class Url {
	
	public static String url = "https://smsapi.ap-southeast-1.myhuaweicloud.com:443/sms/batchSendDiffSms/v1";
	public static String appKey = "037WGImPNGdp4mt140xQVR1Krn7h";
	public static String appSecret = "Mrx2F7Un3s6ArCEj8yF9tx30NG2l";
	public static String sender = "isms0000000182";
	public static String templateId1="9998ff9a80eb463dbd01b7999ade5d7f";
	//public static String templateId1 = "ae04b0422b39452e8ed88251b6d18994";
	
	//Opcional. Dirección para recibir informes de estado de SMS. Se recomienda el nombre de dominio. Si este parámetro 
	//se establece en un valor vacío o no se especifica, los clientes no reciben informes de estado.
	public static String statusCallBack = "";
	
	;
	//
	public static String[] templateParas1 = {};
	
	
}
