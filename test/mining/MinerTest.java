package mining;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import graphics.DotGraph;

public class MinerTest {

	@Test
	public void mine() {
		ArrayList<EGroumGraph> groums = buildGroums(
				"class C { void m(Object o) { o.hashCode(); } }",
				"class C { void m(Object o) { o.hashCode(); } }");
		System.out.println(new DotGraph(groums.iterator().next()).getGraph());
		
		List<Pattern> patterns = mine(groums);
		
		assertThat(patterns.size(), is(1));
	}
	
	@Test
	public void OOM() {
		String targetSource = "class C { public static String decrypt(PublicKey publicKey, String cipherText)\n" + 
				"			throws Exception {\n" + 
				"		Cipher cipher = Cipher.getInstance(\"RSA\");\n" + 
				"		try {\n" + 
				"			cipher.init(Cipher.DECRYPT_MODE, publicKey);\n" + 
				"		} catch (InvalidKeyException e) {\n" + 
				"			// for ibm jdk\n" + 
				"			RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;\n" + 
				"			RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());\n" + 
				"			Key fakePublicKey = KeyFactory.getInstance(\"RSA\").generatePrivate(spec);\n" + 
				"			cipher.init(Cipher.DECRYPT_MODE, fakePublicKey);\n" + 
				"		}\n" + 
				"		\n" + 
				"		if (cipherText == null || cipherText.length() == 0) {\n" + 
				"			return cipherText;\n" + 
				"		}\n" + 
				"\n" + 
				"		byte[] cipherBytes = Base64.base64ToByteArray(cipherText);\n" + 
				"		byte[] plainBytes = cipher.doFinal(cipherBytes);\n" + 
				"\n" + 
				"		return new String(plainBytes);\n" + 
				"	}}";
		String patternSource = "class C { Cipher patter(PublicKey publicKey, String text) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidKeySpecException {\n" + 
				"    Cipher cipher = Cipher.getInstance(\"RSA\");\n" + 
				"		try {\n" + 
				"			cipher.init(Cipher.DECRYPT_MODE, publicKey);\n" + 
				"		} catch (InvalidKeyException e) {\n" + 
				"      RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;\n" + 
				"      RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());\n" + 
				"      Key fakePrivateKey = KeyFactory.getInstance(\"RSA\").generatePrivate(spec);\n" + 
				"      cipher = Cipher.getInstance(\"RSA\");\n" + 
				"      cipher.init(Cipher.DECRYPT_MODE, fakePrivateKey);\n" + 
				"		}\n" + 
				"    return cipher;\n" + 
				"  }}";
		ArrayList<EGroumGraph> groums = buildGroums(targetSource, patternSource);
		List<Pattern> patterns = mine(groums);
		
		assertThat(patterns.size(), is(1));
	}

	private ArrayList<EGroumGraph> buildGroums(String... sourceCodes) {
		EGroumBuilder builder = new EGroumBuilder();
		ArrayList<EGroumGraph> groums = new ArrayList<>();
		for (String sourceCode : sourceCodes) {
			groums.addAll(builder.buildGroums(sourceCode, ""));
		}
		return groums;
	}

	private List<Pattern> mine(ArrayList<EGroumGraph> groums) {
		Pattern.minFreq = 2;
		Pattern.minSize = 1;
		Miner miner = new Miner("", "test");
		miner.maxSingleNodePrevalence = 100;
		miner.mine(groums);
		
		List<Pattern> patterns = new ArrayList<>();
		for (int step = Pattern.minSize - 1; miner.lattices.size() > step; step++) {
			Lattice lattice = miner.lattices.get(step);
			patterns.addAll(lattice.getPatterns());
		}
		return patterns;
	}
}
