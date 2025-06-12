package br.com.wm.pagbankapi.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
	
	private static byte[] convertAssinaturaToBytes(String token, String payload) {
		String assinatura = token+"-"+payload;
				 
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found", e);
		}
		byte[] encodedhash = digest.digest(
		assinatura.getBytes(StandardCharsets.UTF_8));
		
		return encodedhash;
	}
	
	public static String convertAssinaturaToHex(String token, String payload) {
		byte[] hashBytes = convertAssinaturaToBytes(token, payload);
		StringBuilder hexString = new StringBuilder();
		for (byte b : hashBytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
