package com.example.apigee.proxygen.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

@Service
public class MessageDigestService {
	
	public String getChecksum(String filePath) {

		String checksum = null;
		MessageDigest md = null;
		
		try {
			md = MessageDigest.getInstance("SHA-512"); // SHA, MD2, MD5, SHA-256, SHA-384...
			checksum = checksum(filePath, md);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

		return checksum;
	}

	private static String checksum(String filePath, MessageDigest md) throws IOException {
		
		// file hashing with DigestInputStream
		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath), md)) {
			while (dis.read() != -1)
				; // empty loop to clear the data
			md = dis.getMessageDigest();
		}

		// bytes to hex
		StringBuilder result = new StringBuilder();
		result.append("SHA-512:");
		for (byte b : md.digest()) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}

}
