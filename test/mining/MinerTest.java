package mining;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.*;

import egroum.AUGConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import egroum.EGroumNode;

public class MinerTest {
	
	@Rule
	public TestName testName = new TestName();
	
	@Test
	public void mineExceptionNodes() {
		ArrayList<EGroumGraph> groums = buildGroumsFromFile("test-resources/input/Test_try.java", null);
		
		List<Pattern> patterns = mine(groums, null);
		
		print(patterns.get(0));
	}
	
	@Test
	public void mineKeepDataNodes() {
		ArrayList<EGroumGraph> groums = buildGroumsFromFile("test-resources/input/Test_keep_data.java", null);
		
		List<Pattern> patterns = mine(groums, null);
		
		print(patterns.get(0));
	}
	
	@Test
	public void mineSinglePattern() {
		ArrayList<EGroumGraph> groums = buildGroumsFromFile("test-resources/input/Test_mine_single.java", null);
		
		List<Pattern> patterns = mine(groums, null);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void mineMinimalCode() {
		ArrayList<EGroumGraph> groums = buildGroums(new String[]{
				"class C { void m(Object o) { o.hashCode(); } }",
				"class C { void m(Object o) { o.hashCode(); } }"}, null);
		
		List<Pattern> patterns = mine(groums, null);
		
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineLargerCode() {
		ArrayList<EGroumGraph> groums = buildGroums(new String[]{
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }",
				"class C { void m(Object o) { if (o != null) { o.hashCode(); } o.equals(this); } }"}, null);
		
		List<Pattern> patterns = mine(groums, null);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}

	@Test
	public void mineClone() {
		ArrayList<EGroumGraph> groums = buildGroums(new String[]{
				"class C { void m(Object o, Object p) {  o = getObj(); o.hashCode(); o.hashCode();} }",
				"class C { void m(Object o, Object p) {  o.hashCode();} }"}, null);
		System.out.println(groums);
		List<Pattern> patterns = mine(groums, null);
		
		print(patterns);
		assertThat(patterns.size(), is(1));
		print(patterns.get(0));
	}
	
	@Test
	public void minePattern_aclang2() {
		ArrayList<EGroumGraph> groums = buildGroums(new String[]{
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
				"}"}, 
				null);
		
		System.out.println(groums);
		List<Pattern> patterns = mine(groums, null);
		
		print(patterns);
	}
	
	@Test
	public void jackrabbit1() {
		String targetSource = "public class ItemManager {\n" + 
				"    private SessionImpl session;" + 
				"    private boolean canRead(ItemData data, Path path) throws AccessDeniedException, RepositoryException {\n" + 
				"        if (data.getState().getStatus() == ItemState.STATUS_NEW && !data.getDefinition().isProtected()) {\n" + 
				"            return true;\n" + 
				"        } else {\n" + 
				"            return (path == null) ? canRead(data.getId()) : session.getAccessManager().canRead(path);\n" + 
				"        }\n" + 
				"    }\n" + 
				"  private boolean canRead(ItemId id) { return true; }\n" + 
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
		ArrayList<EGroumGraph> groums = buildGroums(new String[]{targetSource, patternSource}, null);
		for (EGroumGraph g : groums)
			g.toGraphics("temp");
		
		List<Pattern> patterns = mine(groums, null);

		print(patterns);
		assertThat(patterns.size(), is(1));
	}
	
	@Test
	public void acmath_1() {
		String targetSource = "class SubLine {\n" + 
				"    private Line line;" +
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
		ArrayList<EGroumGraph> groums = buildGroums(new String[]{targetSource, patternSource}, null);
		List<Pattern> patterns = mine(groums, null);
		
		print(patterns);
		assertThat(patterns, hasSize(2));
	}

	@Test
	public void mineAlternativeChecks() throws Exception {
		String firstHasNext = "class C { void m(Collection c) { Iterator i = c.iterator(); if (i.hasNext()) i.next(); } }";
		String firstIsEmpty = "class C { void n(Collection c) { if (!c.isEmpty()) { Iterator i = c.iterator(); i.next(); } } }";
		ArrayList<EGroumGraph> augs = buildGroums(new String[]{firstHasNext, firstHasNext, firstIsEmpty, firstIsEmpty}, null);
		List<Pattern> patterns = mine(augs, null);

		assertThat(patterns, hasSize(2));
	}

	@Test
	public void mineAlternativeCond() throws Exception {
		String iteratorIf = "class C { void m(Iterator i) { if (i.hasNext()) { i.next(); } } }";
		String iteratorWhile = "class C { void n(Iterator i) { while (i.hasNext()) { i.next(); } } }";
		ArrayList<EGroumGraph> augs = buildGroums(new String[]{iteratorIf, iteratorIf, iteratorIf, iteratorWhile, iteratorWhile}, null);
		for (EGroumGraph aug : augs)
			System.out.println(aug);
		List<Pattern> patterns = mine(augs, null);
		
		System.out.println(patterns.size());
		for (Pattern p : patterns)
			System.out.println(p.getRepresentative().getNodes());
		assertThat(patterns, hasSize(2));
	}

	private ArrayList<EGroumGraph> buildGroumsFromFile(String path, String[] classpaths) {
		return new EGroumBuilder(new AUGConfiguration()).build(path, classpaths);
	}

	private ArrayList<EGroumGraph> buildGroums(String[] sourceCodes, String[] classpaths) {
		EGroumBuilder builder = new EGroumBuilder(new AUGConfiguration());
		ArrayList<EGroumGraph> groums = new ArrayList<>();
		for (String sourceCode : sourceCodes) {
			groums.addAll(builder.buildGroums(sourceCode, "", "", classpaths));
		}
		return groums;
	}

	private List<Pattern> mine(ArrayList<EGroumGraph> groums, String[] classpaths) {
		Miner miner = new Miner("test", new Configuration() {{ minPatternSupport = 2; maxPatternSize = 300; }});
		return new ArrayList<>(miner.mine(groums));
	}
	
	private void print(Pattern pattern) {
		print(Collections.singletonList(pattern));
	}
	
	private void print(List<Pattern> patterns) {
		System.err.println("Test '" + testName.getMethodName() + "':");
		for (Pattern pattern : patterns) {
			HashSet<EGroumNode> set = new HashSet<>(pattern.getRepresentative().getNodes());
			assertThat(set.size(), is(pattern.getRepresentative().getNodes().size()));
			EGroumGraph g = new EGroumGraph(pattern.getRepresentative());
			System.err.println(" - " + g);
			File dir = new File("temp/" + pattern.getId() + "/");
			dir.mkdirs();
			g.toGraphics(dir.getAbsolutePath());
		}
	}
}
