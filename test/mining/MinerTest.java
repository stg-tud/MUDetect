package mining;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import utils.FileIO;

public class MinerTest {

	
	@Rule
	public TestName testName = new TestName(); 
	
	@Test
	public void mineMinimalCode() {
		ArrayList<EGroumGraph> groums = buildGroums(
				"class C { void m(Object o) { o.hashCode(); } }",
				"class C { void m(Object o) { o.hashCode(); } }");
		
		List<Pattern> patterns = mine(groums);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineLargerCode() {
		ArrayList<EGroumGraph> groums = buildGroums(
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }",
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }");
		
		List<Pattern> patterns = mine(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineDuplicatedCode() {
		int tempMaxSize = Pattern.maxSize;
		Pattern.maxSize = Integer.MAX_VALUE;
		ArrayList<EGroumGraph> groums = buildGroums(
				FileIO.readStringFromFile("input/Test_adempiere.java"));
		
		for (EGroumGraph g : groums)
			System.out.println(g);
		
		List<Pattern> patterns = mine(groums);
		
		for (EGroumGraph g : groums)
			System.out.println(g);
		print(patterns);
		assertThat(patterns.size(), is(1));
//		print(patterns.get(0));
		Pattern.maxSize = tempMaxSize;
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
		System.err.println(groums);
		List<Pattern> patterns = mine(groums);
		
		print(patterns);
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
	
	private void print(Pattern pattern) {
		print(Arrays.asList(pattern));
	}
	
	private void print(List<Pattern> patterns) {
		System.err.println("Test '" + testName.getMethodName() + "':");
		for (Pattern pattern : patterns) {
			System.err.println(" - " + new EGroumGraph(pattern.getRepresentative()));
		}
	}
}
