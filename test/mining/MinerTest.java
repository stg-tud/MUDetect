package mining;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import egroum.EGroumBuilder;
import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
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
		String system = "joda";
		ArrayList<EGroumGraph> groums = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			groums.addAll(buildGroums(FileIO.readStringFromFile("input/Test_" + system + "_new.java")));
		
		if (groums.size() <= 2)
			for (EGroumGraph g : groums){
				System.out.println(g);
				g.toGraphics("T:/temp");
			}
		
		List<Pattern> patterns = mine(groums);
		
		for (EGroumGraph g : groums) {
			System.out.println(g);
			g.toGraphics("T:/temp");
		}
		print(patterns);
		assertThat(patterns.size(), is(1));
		
		groums = buildGroums(FileIO.readStringFromFile("input/Test_" + system + "_old.java"));
		groums.add(new EGroumGraph(patterns.get(0).getRepresentative()));
		
		for (EGroumGraph g : groums) {
			System.out.println(g);
			g.toGraphics("T:/temp");
		}
		
		HashSet<EGroumNode> patternNodes = new HashSet<>(groums.get(1).getNodes());
		HashSet<EGroumEdge> patternEdges = new HashSet<>();
		HashMap<EGroumNode, ArrayList<EGroumEdge>> patternInEdges = new HashMap<>(), patternOutEdges = new HashMap<>();
		for (EGroumNode node : patternNodes) {
			patternEdges.addAll(node.getInEdges());
			patternEdges.addAll(node.getOutEdges());
			patternInEdges.put(node, new ArrayList<>(node.getInEdges()));
			patternOutEdges.put(node, new ArrayList<>(node.getOutEdges()));
		}
		
		patterns = mine(groums);
		
		print(patterns);
//		assertThat(patterns.size(), is(1));
		
		for (Pattern p: patterns)
			printMissing(p, groums.get(1), patternNodes, patternEdges, patternInEdges, patternOutEdges);
		
		Pattern.maxSize = tempMaxSize;
	}

	private void printMissing(Pattern p, EGroumGraph g, HashSet<EGroumNode> patternNodes, HashSet<EGroumEdge> patternEdges, HashMap<EGroumNode, ArrayList<EGroumEdge>> patternInEdges, HashMap<EGroumNode, ArrayList<EGroumEdge>> patternOutEdges) {
		Fragment f = null;
		for (Fragment t : p.getFragments())
			if (t.getGraph() == g) {
				f = t;
				break;
			}
		HashSet<EGroumNode> nodes = new HashSet<>(patternNodes);
		nodes.removeAll(f.getNodes());
		HashSet<EGroumEdge> edges = new HashSet<>(patternEdges);
		edges.removeAll(f.getEdges());
		EGroumGraph mg = new EGroumGraph(nodes, patternInEdges, patternOutEdges, g);
		mg.setName(p.getId() + "#" + mg.getName());
		System.out.println(mg);
		System.out.println("Missing edges:");
		print(edges);
		mg.toGraphics("T:/temp");
	}

	private void print(HashSet<EGroumEdge> edges) {
		for (EGroumEdge e : edges) {
			System.out.println(e);
		}
	}

	@Test
	public void mineClone() {
		ArrayList<EGroumGraph> groums = buildGroums(
				"class C { void m(Object o, Object p) {  o = getObj(); o.hashCode(); o.hashCode();} }",
				"class C { void m(Object o, Object p) {  o.hashCode();} }");
		System.out.println(groums);
		List<Pattern> patterns = mine(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void minePattern_aclang2() {
		ArrayList<EGroumGraph> groums = buildGroums(
				"class NullTextNull extends StrBuilder {\n" + 
				"  String pattern(Object obj) {\n" + 
				"    String str = (obj == null ? this.getNullText() : obj.toString());\n" + 
				"    if (str == null) {\n" + 
				"      str = \"\";\n" + 
				"    }\n" + 
				"    return str;\n" + 
				"  }\n" + 
				"}",
				"class NullTextNull extends StrBuilder {\n" + 
				"  String pattern(Object obj) {\n" + 
				"    String str = (obj == null ? this.getNullText() : obj.toString());\n" + 
				"    if (str == null) {\n" + 
				"      str = \"\";\n" + 
				"    }\n" + 
				"    return str;\n" + 
				"  }\n" + 
				"}");
		
		System.out.println(groums);
		List<Pattern> patterns = mine(groums);
		
		print(patterns);
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
		assertThat(patterns.size(), is(2));
	}
	
	@Test
	public void jackrabbit1() {
		String targetSource = "public class ItemManager {\n" + 
				"    private boolean canRead(ItemData data, Path path) throws AccessDeniedException, RepositoryException {\n" + 
				"        if (data.getState().getStatus() == ItemState.STATUS_NEW && !data.getDefinition().isProtected()) {\n" + 
				"            return true;\n" + 
				"        } else {\n" + 
				"            return (path == null) ? canRead(data.getId()) : session.getAccessManager().canRead(path);\n" + 
				"        }\n" + 
				"    }\n" + 
				"}";
		String patternSource = "class CheckStateNotNull {\n" + 
			"  boolean canRead(ItemData data, Path path, SessionImpl session) throws AccessDeniedException, RepositoryException {\n" + 
			"    ItemState state = data.getState();\n" + 
			"    if (state == null) {\n" + 
			"        throw new InvalidItemStateException(data.getId() + \": the item does not exist anymore\");\n" + 
			"    }\n" + 
			"    if (state.getStatus() == ItemState.STATUS_NEW && !data.getDefinition().isProtected()) {\n" + 
			"        return true;\n" + 
			"    } else {\n" + 
			"        return (path == null) ? canRead(data.getId()) : session.getAccessManager().canRead(path);\n" + 
			"    }\n" + 
			"  }\n" + 
			"  \n" + 
			"  private boolean canRead(ItemId id) { return true; }\n" + 
			"}";
		ArrayList<EGroumGraph> groums = buildGroums(targetSource, patternSource);
		System.err.println(groums);
		List<Pattern> patterns = mine(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(2));
	}
	
	@Test
	public void acmath_1() {
		String targetSource = "class SubLine {\n" + 
				"    public Vector3D intersection(final SubLine subLine, final boolean includeEndPoints) {\n" + 
				"        // compute the intersection on infinite line\n" + 
				"        Vector3D v1D = line.intersection(subLine.line);\n" + 
				"        // check location of point with respect to first sub-line\n" + 
				"        Location loc1 = remainingRegion.checkPoint(line.toSubSpace(v1D));\n" + 
				"        // check location of point with respect to second sub-line\n" + 
				"        Location loc2 = subLine.remainingRegion.checkPoint(subLine.line.toSubSpace(v1D));\n" + 
				"        if (includeEndPoints) {\n" + 
				"            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v1D : null;\n" + 
				"        } else {\n" + 
				"            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v1D : null;\n" + 
				"        }\n" + 
				"    }\n" + 
				"}";
		String patternSource = "class CheckStateNotNull {\n" + 
			"  public Vector3D pattern(Line line, Line other) {\n" + 
			"    Vector3D v1D = line.intersection(other);\n" + 
			"    if (v1D == null) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"    line.toSubSpace(v1D);\n" + 
			"    other.toSubSpace(v1D);\n" + 
			"    return v1D;\n" + 
			"  }\n" + 
			"}";
		ArrayList<EGroumGraph> groums = buildGroums(targetSource, patternSource);
		List<Pattern> patterns = mine(groums);
		
		print(patterns);
		assertThat(patterns.size(), is(2));
	}

	private ArrayList<EGroumGraph> buildGroums(String... sourceCodes) {
		EGroumBuilder builder = new EGroumBuilder();
		ArrayList<EGroumGraph> groums = new ArrayList<>();
		for (String sourceCode : sourceCodes) {
			groums.addAll(builder.buildGroums(sourceCode, "", ""));
		}
		return groums;
	}

	private List<Pattern> mine(ArrayList<EGroumGraph> groums) {
		Pattern.minFreq = 2;
		Pattern.minSize = 1;
		Pattern.maxSize = 30;
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
			HashSet<EGroumNode> set = new HashSet<>(pattern.getRepresentative().getNodes());
			assertThat(set.size(), is(pattern.getRepresentative().getNodes().size()));
			EGroumGraph g = new EGroumGraph(pattern.getRepresentative());
			System.err.println(" - " + g);
			g.toGraphics("T:/temp");
		}
	}
}
