import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

class Test_alibaba2_old {
	public static String decrypt(PublicKey publicKey, String cipherText)
			throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		try {
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
		} catch (InvalidKeyException e) {
			// for ibm jdk
			RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
			RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
			Key fakePublicKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
			cipher.init(Cipher.DECRYPT_MODE, fakePublicKey);
		}

		if (cipherText == null || cipherText.length() == 0) {
			return cipherText;
		}
		byte[] cipherBytes = Base64.base64ToByteArray(cipherText);
		byte[] plainBytes = cipher.doFinal(cipherBytes);
		return new String(plainBytes);
	}
}