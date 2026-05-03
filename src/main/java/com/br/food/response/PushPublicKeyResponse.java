package com.br.food.response;

public class PushPublicKeyResponse {

	private final String publicKey;

	public PushPublicKeyResponse(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPublicKey() {
		return publicKey;
	}
}
