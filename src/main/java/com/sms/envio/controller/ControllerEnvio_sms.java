package com.sms.envio.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sms.envio.entity.Parametros;
import com.sms.envio.entity.Url;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/sms/")
public class ControllerEnvio_sms {
	private static final String WSSE_HEADER_FORMAT = "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\",Created=\"%s\"";
	private static final String AUTH_HEADER_VALUE = "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"";

	@PostMapping
	@ResponseBody
	public void envio_sms(@RequestBody Parametros parametros) throws Exception {
		
		
		List<Map<String, Object>> smsContent = new ArrayList<Map<String, Object>>();
		//se obtienen los parametros.
		Map<String, Object> item1 = initDiffSms(parametros.getReceptor(), parametros.getMensajeLatinoAmerica(), Url.templateId1, "");
		//se agrega de la lista del smsContent al map item los paramentros
		
		if (null != item1 && !item1.isEmpty()) {
			smsContent.add(item1);
		}

		String body = buildRequestBody(Url.sender, smsContent, Url.statusCallBack);
		if (null == body || body.isEmpty()) {
			System.out.println("body is null.");
			return;
		}

		String wsseHeader = buildWsseHeader(Url.appKey, Url.appSecret);
		if (null == wsseHeader || wsseHeader.isEmpty()) {
			System.out.println("wsse header is null.");
		}

		Writer out = null;
		BufferedReader in = null;
		StringBuffer result = new StringBuffer();
		HttpsURLConnection connection = null;
		InputStream is = null;

		HostnameVerifier hv = new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		trustAllHttpsCertificates();

		try {
			//se hace la conexión.
			URL realUrl = new URL(Url.url);
			connection = (HttpsURLConnection) realUrl.openConnection();

			connection.setHostnameVerifier(hv);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", AUTH_HEADER_VALUE);
			connection.setRequestProperty("X-WSSE", wsseHeader);

			connection.connect();
			out = new OutputStreamWriter(connection.getOutputStream());
			out.write(body);
			out.flush();
			out.close();

			int status = connection.getResponseCode();
			if (200 == status) {
				is = connection.getInputStream();
			} else {
				is = connection.getErrorStream();
			}
			in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = "ds";
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
			System.out.println(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
				if (null != is) {
					is.close();
				}
				if (null != in) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	 /**
     * Construct the value of smsContent.
     * @param receptor
     * @param msjlatam
     * @param templateId
     * @param templateParas
     * @return
     */
	
	//se agrega los parametros obtenidos del envio
	static Map<String, Object> initDiffSms(String[] receptor, String[] msjlatam,  String templateId,String signature) {
		if (null == receptor || null == templateId || templateId.isEmpty()) {
			System.out.println("initDiffSms(): receiver or templateId is null.");
			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("to", receptor);
		map.put("templateId", templateId);

		if (null != msjlatam && msjlatam.length >0) {
			map.put("templateParas", msjlatam);
		}
		if (null != msjlatam && !signature.isEmpty()) {
			map.put("signature", signature);
		}

		return map;
	}
	
	/**
     * Construct the request body.
     * @param sender
     * @param smsContent
     * @param statusCallBack
     * @return
     */
	
	//se envia el cuerpo
	static String buildRequestBody(String sender, List<Map<String, Object>> smsContent, String statusCallBack) {
		if (null == sender || null == smsContent || sender.isEmpty() || smsContent.isEmpty()) {
			System.out.println("buildRequestBody(): sender or smsContent is null.");
			return null;
		}
		JSONArray jsonArr = new JSONArray();

		for (Map<String, Object> it : smsContent) {
			jsonArr.put(it);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("from", sender);
		data.put("smsContent", jsonArr);
		if (null != statusCallBack && !statusCallBack.isEmpty()) {
			data.put("statusCallback", statusCallBack);
		}

		return new JSONObject(data).toString();
	}

	static String buildWsseHeader(String appKey, String appSecret) {
		if (null == appKey || null == appSecret || appKey.isEmpty() || appSecret.isEmpty()) {
			System.out.println("buildWsseHeader(): appKey or appSecret is null.");
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String time = sdf.format(new Date());
		String nonce = UUID.randomUUID().toString().replace("-", "");

		MessageDigest md;
		byte[] passwordDigest = null;

		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update((nonce + time + appSecret).getBytes());
			passwordDigest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String passwordDigestBase64Str = Base64.getEncoder().encodeToString(passwordDigest); // PasswordDigest
		return String.format(WSSE_HEADER_FORMAT, appKey, passwordDigestBase64Str, nonce, time);
	}

	static void trustAllHttpsCertificates() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return;
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return;
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

}
