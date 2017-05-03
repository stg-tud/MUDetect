class Test_alibaba2_new {
	void pattern(PublicKey publicKey, String text) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidKeySpecException {
	    Cipher cipher = Cipher.getInstance("RSA");
			try {
				cipher.init(Cipher.DECRYPT_MODE, publicKey);
			} catch (InvalidKeyException e) {
	      RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
	      RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
	      Key fakePrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
	      cipher = Cipher.getInstance("RSA");
	      cipher.init(Cipher.DECRYPT_MODE, fakePrivateKey);
			}
	  }
}