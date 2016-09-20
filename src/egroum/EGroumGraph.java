package egroum;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import egroum.EGroumDataEdge.Type;
import graphics.DotGraph;
import mining.Fragment;
import utils.JavaASTUtil;

public class EGroumGraph implements Serializable {
	private static final long serialVersionUID = -5128703931982211886L;
	private static final int MAX_BRANCHES = 100;
	
	private String filePath, name;
	private EGroumBuildingContext context;
	private HashMap<String, HashSet<EGroumDataNode>> defStore = new HashMap<>();
	protected EGroumNode entryNode, endNode;
	protected HashSet<EGroumNode> nodes = new HashSet<EGroumNode>();
	protected HashSet<EGroumNode> statementNodes = new HashSet<>();
	protected HashSet<EGroumDataNode> dataSources = new HashSet<>();
	protected HashSet<EGroumNode> statementSources = new HashSet<>();
	protected HashSet<EGroumNode> sinks = new HashSet<EGroumNode>();
	protected HashSet<EGroumNode> statementSinks = new HashSet<>();
	protected HashSet<EGroumNode> breaks = new HashSet<>();
	protected HashSet<EGroumNode> returns = new HashSet<>();
	
	public EGroumGraph(MethodDeclaration md, EGroumBuildingContext context) {
		this(context);
		context.addScope();
		context.setMethod(md);
		entryNode = new EGroumEntryNode(md, ASTNode.METHOD_DECLARATION, "START");
		nodes.add(entryNode);
		statementNodes.add(entryNode);
		EGroumDataNode thisNode = new EGroumDataNode(null, ASTNode.THIS_EXPRESSION, "this", context.getType(), "this", false, true);
		nodes.add(thisNode);
		HashSet<EGroumDataNode> thisDef = new HashSet<>();
		thisDef.add(thisNode);
		defStore.put(thisNode.key, thisDef);
		for (int i = 0; i < md.parameters().size(); i++) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) md.parameters().get(i);
			mergeSequential(buildPDG(entryNode, "", d));
		}
		if (context.interprocedural)
			context.pushTry();
		if (md.getBody() != null) {
			Block block = md.getBody();
			if (!block.statements().isEmpty())
				mergeSequential(buildPDG(entryNode, "", block));
		}
		if (context.interprocedural)
			statementSinks.addAll(context.popTry());
		adjustControlEdges();
		context.removeScope();
		addDefinitions();
		deleteTemporaryDataNodes();
		deleteEmptyStatementNodes();
		if (isTooDense()) {
			nodes.clear();
			cleanUp();
			return;
		}
		buildClosure();
		deleteReferences();
		deleteAssignmentNodes();
		deleteUnreachableNodes();
		deleteControlNodes();
		cleanUp();
	}

	private boolean isTooDense() {
		for (EGroumNode node : nodes)
			if (node instanceof EGroumDataNode && node.outEdges.size() > MAX_BRANCHES)
				return true;
		return false;
	}

	public EGroumGraph(EGroumBuildingContext context) {
		this.context = context;
	}

	public EGroumGraph(EGroumBuildingContext context, EGroumNode node) {
		this(context);
		init(node);
	}

	private void init(EGroumNode node) {
		if (node instanceof EGroumDataNode && !node.isLiteral())
			dataSources.add((EGroumDataNode) node);
		sinks.add(node);
		nodes.add(node);
		if (node.isStatement()) {
			statementNodes.add(node);
			statementSources.add(node);
			statementSinks.add(node);
		}
	}

	public EGroumGraph(Fragment f) {
		this.name = f.getGraph().getName();
		this.filePath = f.getGraph().filePath;
		HashMap<EGroumNode, EGroumNode> map = new HashMap<>();
		for (EGroumNode node : f.getNodes()) {
			EGroumNode cn = EGroumNode.createNode(node);
			cn.setGraph(this);
			map.put(node, cn);
			this.nodes.add(cn);
		}
		for (EGroumNode node : f.getNodes()) {
			EGroumNode cn = map.get(node);
			for (EGroumEdge e : node.getInEdges()) {
				EGroumNode s = e.source;
				if (map.containsKey(s))
					EGroumEdge.createEdge(map.get(s), cn, e);
			}
		}
	}
	
	public EGroumGraph(EGroumGraph g) {
		this.name = g.getName();
		this.filePath = g.getFilePath();
		HashMap<EGroumNode, EGroumNode> map = new HashMap<>();
		for (EGroumNode node : g.getNodes()) {
			EGroumNode cn = EGroumNode.createNode(node);
			cn.setGraph(this);
			map.put(node, cn);
			this.nodes.add(cn);
		}
		for (EGroumNode node : g.getNodes()) {
			EGroumNode cn = map.get(node);
			for (EGroumEdge e : node.getInEdges()) {
				EGroumNode s = e.source;
				EGroumEdge.createEdge(map.get(s), cn, e);
			}
		}
	}

	public EGroumGraph(HashSet<EGroumNode> nodes, HashMap<EGroumNode, ArrayList<EGroumEdge>> inEdges, HashMap<EGroumNode, ArrayList<EGroumEdge>> outEdges, EGroumGraph g) {
		this.name = g.name;
		this.filePath = g.filePath;
		HashMap<EGroumNode, EGroumNode> map = new HashMap<>();
		for (EGroumNode node : nodes) {
			EGroumNode cn = EGroumNode.createNode(node);
			cn.setGraph(this);
			map.put(node, cn);
			this.nodes.add(cn);
		}
		for (EGroumNode node : nodes) {
			EGroumNode cn = map.get(node);
			for (EGroumEdge e : inEdges.get(node)) {
				EGroumNode s = e.source;
				if (map.containsKey(s))
					EGroumEdge.createEdge(map.get(s), cn, e);
			}
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filepath) {
		this.filePath = filepath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashSet<EGroumNode> getNodes() {
		return nodes;
	}

	public HashSet<EGroumEdge> getEdges() {
		HashSet<EGroumEdge> edges = new HashSet<>();
		for (EGroumNode node : nodes) {
			edges.addAll(node.getInEdges());
			edges.addAll(node.getOutEdges());
		}
		return edges;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ASTNode node) {
		if (node instanceof ArrayAccess)
			return buildPDG(control, branch, (ArrayAccess) node);
		if (node instanceof ArrayCreation)
			return buildPDG(control, branch, (ArrayCreation) node);
		if (node instanceof ArrayInitializer)
			return buildPDG(control, branch, (ArrayInitializer) node);
		if (node instanceof AssertStatement)
			return buildPDG(control, branch, (AssertStatement) node);
		if (node instanceof Assignment)
			return buildPDG(control, branch, (Assignment) node);
		if (node instanceof Block)
			return buildPDG(control, branch, (Block) node);
		if (node instanceof BooleanLiteral)
			return buildPDG(control, branch, (BooleanLiteral) node);
		if (node instanceof BreakStatement)
			return buildPDG(control, branch, (BreakStatement) node);
		if (node instanceof CastExpression)
			return buildPDG(control, branch, (CastExpression) node);
		if (node instanceof CatchClause)
			return buildPDG(control, branch, (CatchClause) node);
		if (node instanceof CharacterLiteral)
			return buildPDG(control, branch, (CharacterLiteral) node);
		if (node instanceof ClassInstanceCreation)
			return buildPDG(control, branch, (ClassInstanceCreation) node);
		if (node instanceof ConditionalExpression)
			return buildPDG(control, branch, (ConditionalExpression) node);
		if (node instanceof ConstructorInvocation)
			return buildPDG(control, branch, (ConstructorInvocation) node);
		if (node instanceof ContinueStatement)
			return buildPDG(control, branch, (ContinueStatement) node);
		if (node instanceof DoStatement)
			return buildPDG(control, branch, (DoStatement) node);
		if (node instanceof EnhancedForStatement)
			return buildPDG(control, branch, (EnhancedForStatement) node);
		if (node instanceof ExpressionStatement)
			return buildPDG(control, branch, (ExpressionStatement) node);
		if (node instanceof FieldAccess)
			return buildPDG(control, branch, (FieldAccess) node);
		if (node instanceof ForStatement)
			return buildPDG(control, branch, (ForStatement) node);
		if (node instanceof IfStatement)
			return buildPDG(control, branch, (IfStatement) node);
		if (node instanceof InfixExpression)
			return buildPDG(control, branch, (InfixExpression) node);
		if (node instanceof Initializer)
			return buildPDG(control, branch, (Initializer) node);
		if (node instanceof InstanceofExpression)
			return buildPDG(control, branch, (InstanceofExpression) node);
		if (node instanceof LabeledStatement)
			return buildPDG(control, branch, (LabeledStatement) node);
		if (node instanceof MethodDeclaration)
			return buildPDG(control, branch, (MethodDeclaration) node);
		if (node instanceof MethodInvocation)
			return buildPDG(control, branch, (MethodInvocation) node);
		if (node instanceof NullLiteral)
			return buildPDG(control, branch, (NullLiteral) node);
		if (node instanceof NumberLiteral)
			return buildPDG(control, branch, (NumberLiteral) node);
		if (node instanceof ParenthesizedExpression)
			return buildPDG(control, branch, (ParenthesizedExpression) node);
		if (node instanceof PostfixExpression)
			return buildPDG(control, branch, (PostfixExpression) node);
		if (node instanceof PrefixExpression)
			return buildPDG(control, branch, (PrefixExpression) node);
		if (node instanceof QualifiedName)
			return buildPDG(control, branch, (QualifiedName) node);
		if (node instanceof ReturnStatement)
			return buildPDG(control, branch, (ReturnStatement) node);
		if (node instanceof SimpleName)
			return buildPDG(control, branch, (SimpleName) node);
		if (node instanceof SingleVariableDeclaration)
			return buildPDG(control, branch, (SingleVariableDeclaration) node);
		if (node instanceof StringLiteral)
			return buildPDG(control, branch, (StringLiteral) node);
		if (node instanceof SuperConstructorInvocation)
			return buildPDG(control, branch, (SuperConstructorInvocation) node);
		if (node instanceof SuperFieldAccess)
			return buildPDG(control, branch, (SuperFieldAccess) node);
		if (node instanceof SuperMethodInvocation)
			return buildPDG(control, branch, (SuperMethodInvocation) node);
		if (node instanceof SwitchCase)
			return buildPDG(control, branch, (SwitchCase) node);
		if (node instanceof SwitchStatement)
			return buildPDG(control, branch, (SwitchStatement) node);
		if (node instanceof SynchronizedStatement)
			return buildPDG(control, branch, (SynchronizedStatement) node);
		if (node instanceof ThisExpression)
			return buildPDG(control, branch, (ThisExpression) node);
		if (node instanceof ThrowStatement)
			return buildPDG(control, branch, (ThrowStatement) node);
		if (node instanceof TryStatement)
			return buildPDG(control, branch, (TryStatement) node);
		if (node instanceof TypeLiteral)
			return buildPDG(control, branch, (TypeLiteral) node);
		if (node instanceof VariableDeclarationExpression)
			return buildPDG(control, branch,
					(VariableDeclarationExpression) node);
		if (node instanceof VariableDeclarationFragment)
			return buildPDG(control, branch, (VariableDeclarationFragment) node);
		if (node instanceof VariableDeclarationStatement)
			return buildPDG(control, branch,
					(VariableDeclarationStatement) node);
		if (node instanceof WhileStatement)
			return buildPDG(control, branch, (WhileStatement) node);
		if (node instanceof WhileStatement)
			return buildPDG(control, branch, (WhileStatement) node);
		return new EGroumGraph(context);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			WhileStatement astNode) {
		context.addScope();
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, Type.CONDITION);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(node, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		EGroumGraph bg = buildPDG(node, "T", astNode.getBody());
		if (!bg.isEmpty())
			ebg.mergeSequential(bg);
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		pdg.mergeBranches(ebg, eg);
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			VariableDeclarationStatement astNode) {
		EGroumGraph pdg = buildPDG(control, branch, (ASTNode) astNode.fragments()
				.get(0));
		for (int i = 1; i < astNode.fragments().size(); i++)
			pdg.mergeSequential(buildPDG(control, branch, (ASTNode) astNode
					.fragments().get(i)));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			VariableDeclarationFragment astNode) {
		SimpleName name = astNode.getName();
		String type = JavaASTUtil.getSimpleType(astNode);
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode node = new EGroumDataNode(name, name.getNodeType(),
				"" + name.getStartPosition(), type,
				name.getIdentifier(), false, true);
		if (astNode.getInitializer() == null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.NULL_LITERAL, "null", "", "null"));
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
			pdg.mergeSequentialData(node, Type.DEFINITION);
			return pdg;
		}
		EGroumGraph pdg = buildPDG(control, branch, astNode.getInitializer());
		ArrayList<EGroumDataNode> defs = pdg.getDefinitions();
		if (defs.isEmpty()) {
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
			pdg.mergeSequentialData(node, Type.DEFINITION);
		} else {
			if (defs.get(0).isDummy()) {
				for (EGroumDataNode def : defs) {
					pdg.defStore.remove(def.key);
					def.copyData(node);
					HashSet<EGroumDataNode> ns = pdg.defStore.get(def.key);
					if (ns == null) {
						ns = new HashSet<>();
						pdg.defStore.put(def.key, ns);
					}
					ns.add(def);
				}
			} else {
				EGroumDataNode def = defs.get(0);
				pdg.mergeSequentialData(
						new EGroumDataNode(def.astNode, def.astNodeType, def.key, def.dataType,
								def.dataName, def.isField, false),
						Type.REFERENCE);
				pdg.mergeSequentialData(new EGroumActionNode(control, branch,
						astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
				pdg.mergeSequentialData(node, Type.DEFINITION);
			}
		}
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			VariableDeclarationExpression astNode) {
		EGroumGraph pdg = buildPDG(control, branch, (ASTNode) astNode.fragments()
				.get(0));
		for (int i = 1; i < astNode.fragments().size(); i++)
			pdg.mergeSequential(buildPDG(control, branch, (ASTNode) astNode
					.fragments().get(i)));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			TypeLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), "class", JavaASTUtil.getSimpleType(astNode.getType()), "class"));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			TryStatement astNode) {
		context.pushTry();
		context.addScope();
		List resources = astNode.resources();
		HashSet<String> resourceNames = new HashSet<>();
		EGroumGraph pdg = null;
		if (resources != null && !resources.isEmpty()) {
			pdg = buildPDG(control, branch, resources);
			for (int i = 0; i < resources.size(); i++) {
				VariableDeclarationExpression res = (VariableDeclarationExpression) resources.get(i);
				List fragments = res.fragments();
				for (int j = 0; j < fragments.size(); j++) {
					VariableDeclarationFragment f = (VariableDeclarationFragment) fragments.get(j);
					SimpleName name = f.getName();
					resourceNames.add(name.getIdentifier());
				}
			}
		} else {
			if (astNode.getBody().statements().isEmpty())
				return new EGroumGraph(context);
			pdg = new EGroumGraph(context);
		}
		EGroumGraph[] gs = new EGroumGraph[astNode.catchClauses().size() + 1];
		gs[0] = buildPDG(control, branch, astNode.getBody());
		ArrayList<EGroumActionNode> triedMethods = context.popTry();
		if (!resourceNames.isEmpty()) {
			HashMap<String, EGroumActionNode> closeNodes = new HashMap<>();
			EGroumGraph[] cgs = new EGroumGraph[resourceNames.size()];
			int i = 0;
			for (String rn : resourceNames) {
				EGroumGraph g = buildPDG(control, branch, astNode.getAST().newSimpleName(rn));
				EGroumActionNode close = new EGroumActionNode(control, branch, null, ASTNode.METHOD_INVOCATION, null, "AutoCloseable.close()", "close");
				g.mergeSequentialData(close, Type.RECEIVER);
				closeNodes.put(rn, close);
				cgs[i++] = g;
			}
			EGroumGraph cg = new EGroumGraph(context);
			cg.mergeParallel(cgs);
			gs[0].mergeSequential(cg);
			for (EGroumActionNode m : triedMethods) {
				ASTNode mn = m.getAstNode();
				String[] rns = getResourceName(mn, resourceNames);
				if (rns != null) {
					String rn = rns[0];
					EGroumActionNode close = closeNodes.get(rn);
					if (triedMethods.size() == 1 || (m.exceptionTypes != null && !m.exceptionTypes.isEmpty()))
						new EGroumDataEdge(m, close, Type.FINALLY);
					if (rns.length == 2 && !close.hasInDataNode(m, Type.ORDER))
						new EGroumDataEdge(m, close, Type.ORDER);
				}
			}
		}
		for (int i = 0; i < astNode.catchClauses().size(); i++) {
			CatchClause cc = (CatchClause) astNode.catchClauses().get(i);
			gs[i+1] = buildPDG(control, branch, cc, triedMethods);
		}
		pdg.mergeBranches(gs);
		if (astNode.getFinally() != null) {
			EGroumControlNode fn = new EGroumControlNode(control, branch, astNode.getFinally(), astNode.getFinally().getNodeType());
			EGroumGraph fg = new EGroumGraph(context, fn);
			fg.mergeSequential(buildPDG(fn, "", astNode.getFinally()));
			pdg.mergeSequential(fg);
			for (EGroumActionNode m : triedMethods) {
				if (triedMethods.size() == 1 || (m.exceptionTypes != null && !m.exceptionTypes.isEmpty()))
					new EGroumDataEdge(m, fn, Type.FINALLY);
			}
		}
		context.removeScope();
		return pdg;
	}

	public String[] getResourceName(ASTNode mn, HashSet<String> resourceNames) {
		if (mn instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) mn;
			if (mi.getExpression() != null && mi.getExpression() instanceof SimpleName && resourceNames.contains(mi.getExpression().toString()))
				return new String[]{mi.getExpression().toString(), EGroumDataEdge.getLabel(Type.ORDER)};
		}
		if (mn.getParent() instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment p = (VariableDeclarationFragment) mn.getParent();
			return resourceNames.contains(p.getName().getIdentifier()) ? new String[]{p.getName().getIdentifier()} : null;
		}
		return null;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ThrowStatement astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, "throw");
		pdg.mergeSequentialData(node, Type.PARAMETER);
		pdg.returns.add(node);
		pdg.sinks.remove(node);
		pdg.statementSinks.remove(node);
		pdg.clearDefStore();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SynchronizedStatement astNode) {
		EGroumGraph pdg = buildPDG(control, branch, astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, Type.CONDITION);
		if (!astNode.getBody().statements().isEmpty()) {
			pdg.mergeSequentialControl(new EGroumActionNode(node, "",
					null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
			pdg.mergeSequential(buildPDG(node, "", astNode.getBody()));
			return pdg;
		}
		return new EGroumGraph(context);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SwitchStatement astNode) {
		// FIXME
		if (true) return new EGroumGraph(context);
		EGroumControlNode snode = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		EGroumGraph pdg = new EGroumGraph(context, snode);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(snode, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		List<?> statements = astNode.statements();
		int s = 0;
		while (s < statements.size()) {
			if (statements.get(s) instanceof SwitchCase)
				break;
		}
		for (int e = s + 1; e < statements.size(); e++) {
			if (statements.get(e) instanceof SwitchCase || e == statements.size() - 1) {
				if (!(statements.get(e) instanceof SwitchCase))
						e = statements.size();
				if (e > s + 1) {
					SwitchCase sc = (SwitchCase) statements.get(s);
					EGroumGraph cg = null;
					if (sc.isDefault()) {
						cg = buildPDG(snode, "T", statements.subList(s+1, e));
						ebg.mergeSequential(cg);
					} else {
						EGroumActionNode ccnode = new EGroumActionNode(snode, "T", null, ASTNode.INFIX_EXPRESSION, null, null, "==");
						EGroumGraph exg = buildArgumentPDG(snode, "T", astNode.getExpression());
						exg.mergeSequentialData(ccnode, Type.PARAMETER);
						EGroumGraph cexg = buildArgumentPDG(snode, "T", ((SwitchCase) statements.get(s)).getExpression());
						cexg.mergeSequentialData(ccnode, Type.PARAMETER);
						cg = new EGroumGraph(context);
						cg.mergeParallel(exg, cexg);
						cg.mergeSequentialData(new EGroumActionNode(snode, "T", null, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
						EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
								EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
										+ astNode.getLength(), "boolean", EGroumNode.PREFIX_DUMMY, false, true);
						cg.mergeSequentialData(dummy, Type.DEFINITION);
						cg.mergeSequentialData(new EGroumDataNode(dummy.astNode, dummy.astNodeType, dummy.key, dummy.dataType, dummy.dataName), Type.REFERENCE);
					
						EGroumControlNode cnode = new EGroumControlNode(snode, "T", sc, ASTNode.IF_STATEMENT);
						cg.mergeSequentialData(cnode, Type.CONDITION);
		
						EGroumGraph etg = new EGroumGraph(context, new EGroumActionNode(cnode, "T",
								null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
						EGroumGraph tg = buildPDG(cnode, "T", statements.subList(s+1, e));
						if (!tg.isEmpty()) {
							etg.mergeSequential(tg);
							EGroumGraph efg = new EGroumGraph(context, new EGroumActionNode(cnode, "F",
									null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
							cg.mergeBranches(etg, efg);
							ebg.mergeSequential(cg);
						}
					}
				}
				s = e;
			}
		}
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(snode, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		pdg.mergeBranches(ebg, eg);
		pdg.adjustBreakNodes("");
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SuperMethodInvocation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size() + 1];
		String type = context.getSuperType();
		HashSet<String> exceptions = null;
		if (astNode.resolveMethodBinding() != null) {
			IMethodBinding mb = astNode.resolveMethodBinding().getMethodDeclaration();
			String sig = JavaASTUtil.buildSignature(mb);
			ITypeBinding tb = getBase(mb.getDeclaringClass().getTypeDeclaration(), mb, sig);
			type = tb.getName();
			exceptions = new HashSet<>();
			for (ITypeBinding etb : mb.getExceptionTypes())
				exceptions.add(etb.getName());
		}
		if (exceptions == null)
			exceptions = EGroumBuildingContext.getExceptions(type, astNode.getName().getIdentifier() + "(" + astNode.arguments().size() + ")");
		pgs[0] = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.THIS_EXPRESSION, "this",
				type, "super"));
		for (int i = 0; i < astNode.arguments().size(); i++)
			pgs[i+1] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + "." + astNode.getName().getIdentifier() + "()", 
				astNode.getName().getIdentifier(), exceptions);
		context.addMethodTry(node);
		EGroumGraph pdg = null;
		pgs[0].mergeSequentialData(node, Type.RECEIVER);
		if (pgs.length > 0) {
			for (int i = 1; i < pgs.length; i++)
				pgs[i].mergeSequentialData(node, Type.PARAMETER);
			pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node);
		// skip astNode.getQualifier()
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SuperConstructorInvocation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size()];
		for (int i = 0; i < astNode.arguments().size(); i++) {
			pgs[i] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
		}
		String type = context.getSuperType();
		HashSet<String> exceptions = null;
		if (astNode.resolveConstructorBinding() != null) {
			IMethodBinding mb = astNode.resolveConstructorBinding();
			String sig = JavaASTUtil.buildSignature(mb);
			ITypeBinding tb = getBase(mb.getDeclaringClass().getTypeDeclaration(), mb, sig);
			type = tb.getName();
			exceptions = new HashSet<>();
			for (ITypeBinding etb : mb.getExceptionTypes())
				exceptions.add(etb.getName());
		}
		if (exceptions == null)
			exceptions = EGroumBuildingContext.getExceptions(type, "<init>" + "(" + astNode.arguments().size() + ")");
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + "()", "<init>", exceptions);
		EGroumGraph pdg = null;
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, Type.PARAMETER);
			pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node);
		// skip astNode.getExpression()
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			StringLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.getEscapedValue(), "String",
				astNode.getLiteralValue()));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SingleVariableDeclaration astNode) {
		SimpleName name = astNode.getName();
		String type = JavaASTUtil.getSimpleType(astNode.getType());
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode node = new EGroumDataNode(name, name.getNodeType(),
				"" + name.getStartPosition(), type,
				name.getIdentifier(), false, true);
		EGroumGraph pdg = new EGroumGraph(context);
		pdg.mergeSequentialData(node, Type.DEFINITION);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, SimpleName astNode) {
		String name = astNode.getIdentifier();
		String type = null;
		if (astNode.resolveTypeBinding() != null) {
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		}
		String[] info = context.getLocalVariableInfo(name);
		if (info != null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					astNode, astNode.getNodeType(), info[0], type == null ? info[1] : type,
					astNode.getIdentifier(), false, false));
			return pdg;
		}
		if (type == null)
			type = context.getFieldType(astNode);
		if (type != null) {
			if (type.equals(name))
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
					"this." + name, type, name, true,
					false));
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this",
					context.getType(), "this"));
			pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
					"this." + name, type, name, true,
					false), Type.QUALIFIER);
			return pdg;
		}
		if (Character.isUpperCase(name.charAt(0))) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					astNode, astNode.getNodeType(), name, name,
					name, false, false));
			return pdg;
		}
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.THIS_EXPRESSION, "this",
				context.getType(), "this"));
		pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
				"this." + name, "UNKNOWN", name, true,
				false), Type.QUALIFIER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ReturnStatement astNode) {
		EGroumGraph pdg = null;
		EGroumActionNode node = null;
		if (astNode.getExpression() != null) {
			pdg = buildArgumentPDG(control, branch, astNode.getExpression());
			node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
					null, null, "return");
			pdg.mergeSequentialData(node, Type.PARAMETER);
		} else {
			node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
					null, null, "return");
			pdg = new EGroumGraph(context, node);
		}
		pdg.returns.add(node);
		pdg.sinks.clear();
		pdg.statementSinks.clear();
		pdg.clearDefStore();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			QualifiedName astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getQualifier());
		EGroumDataNode node = pdg.getOnlyDataOut();
		String type = node.dataType;
		if (astNode.getQualifier().resolveTypeBinding() != null)
			type = astNode.getQualifier().resolveTypeBinding().getTypeDeclaration().getName();
		String name = astNode.getName().getIdentifier();
		if (type.startsWith("UNKNOWN")) {
			if (Character.isUpperCase(name.charAt(0))) {
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, node.key + "." + name,
						astNode.getFullyQualifiedName(), astNode.getFullyQualifiedName(), true, false));
			}
		} else
			if (Character.isUpperCase(name.charAt(0))) {
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, node.key + "." + name,
						type + "." + name, astNode.getFullyQualifiedName(), true, false));
			}
		pdg.mergeSequentialData(
				new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, node.key + "." + name,
						type + "." + name, astNode.getFullyQualifiedName(), true, false),
				Type.QUALIFIER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			PrefixExpression astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getOperand());
		EGroumDataNode node = pdg.getOnlyDataOut();
		if (astNode.getOperator() == PrefixExpression.Operator.PLUS)
			return pdg;
		if (astNode.getOperator() == PrefixExpression.Operator.INCREMENT
				|| astNode.getOperator() == PrefixExpression.Operator.DECREMENT) {
			EGroumGraph rg = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.NUMBER_LITERAL, "1", "number", "1"));
			EGroumActionNode op = new EGroumActionNode(control, branch,
					astNode, astNode.getNodeType(), null, null, 
					astNode.getOperator().toString().substring(0, 1));
			pdg.mergeSequentialData(op, Type.PARAMETER);
			rg.mergeSequentialData(op, Type.PARAMETER);
			pdg.mergeParallel(rg);
		} else
			pdg.mergeSequentialData(
					new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
							null, null, astNode.getOperator().toString()),
					Type.PARAMETER);
		if (astNode.getOperator() == PrefixExpression.Operator.INCREMENT
				|| astNode.getOperator() == PrefixExpression.Operator.DECREMENT) {
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
			pdg.mergeSequentialData(new EGroumDataNode(node.astNode, node.astNodeType, node.key,
					node.dataType, node.dataName), Type.DEFINITION);
		}
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			PostfixExpression astNode) {
		EGroumGraph lg = buildArgumentPDG(control, branch, astNode.getOperand());
		EGroumDataNode node = lg.getOnlyDataOut();
		EGroumGraph rg = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.NUMBER_LITERAL, "1", "number", "1"));
		EGroumActionNode op = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, astNode.getOperator().toString()
						.substring(0, 1));
		lg.mergeSequentialData(op, Type.PARAMETER);
		rg.mergeSequentialData(op, Type.PARAMETER);
		EGroumGraph pdg = new EGroumGraph(context);
		pdg.mergeParallel(lg, rg);
		pdg.mergeSequentialData(new EGroumActionNode(control, branch,
				astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
		pdg.mergeSequentialData(new EGroumDataNode(node.astNode, node.astNodeType, node.key,
				node.dataType, node.dataName), Type.DEFINITION);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ParenthesizedExpression astNode) {
		return buildPDG(control, branch, astNode.getExpression());
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			NumberLiteral astNode) {
		String type = "number";
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getName();
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.getToken(), type,
				astNode.getToken()));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			NullLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.toString(), "null",
				astNode.toString()));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			MethodInvocation astNode) {
		if (astNode.getName().getIdentifier().equals("exit")
				&& astNode.getExpression() != null && astNode.getExpression().toString().equals("System")) {
			EGroumActionNode node = new EGroumActionNode(control, branch,
					astNode, astNode.getNodeType(), null, "Sytem.exit()", astNode.getName().getIdentifier());
			EGroumGraph pdg = new EGroumGraph(context, node);
			pdg.returns.add(node);
			pdg.sinks.remove(node);
			pdg.statementSinks.remove(node);
			pdg.clearDefStore();
			return pdg;
		}
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size() + 1];
		if (astNode.getExpression() != null)
			pgs[0] = buildArgumentPDG(control, branch,
					astNode.getExpression());
		else
			pgs[0] = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this",
					context.getType(), "this"));
		for (int i = 0; i < astNode.arguments().size(); i++)
			pgs[i + 1] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
		String type = pgs[0].getOnlyOut().dataType;
		HashSet<String> exceptions = null;
		if (astNode.resolveMethodBinding() != null) {
			IMethodBinding mb = astNode.resolveMethodBinding().getMethodDeclaration();
			String sig = JavaASTUtil.buildSignature(mb);
			ITypeBinding tb = getBase(mb.getDeclaringClass().getTypeDeclaration(), mb, sig);
			type = tb.getName();
			exceptions = new HashSet<>();
			for (ITypeBinding etb : mb.getExceptionTypes())
				exceptions.add(etb.getName());
		}
		if (exceptions == null)
			exceptions = EGroumBuildingContext.getExceptions(type, astNode.getName().getIdentifier() + "(" + astNode.arguments().size() + ")");
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, 
				type + "." + astNode.getName().getIdentifier() + "()", 
				astNode.getName().getIdentifier(), exceptions);
		context.addMethodTry(node);
		EGroumGraph pdg = null;
		pgs[0].mergeSequentialData(node, Type.RECEIVER);
		if (pgs.length > 0) {
			for (int i = 1; i < pgs.length; i++)
				pgs[i].mergeSequentialData(node, Type.PARAMETER);
			pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node);
		return pdg;
	}

	private ITypeBinding getBase(ITypeBinding tb, IMethodBinding mb, String sig) {
		if (tb.getSuperclass() != null) {
			ITypeBinding stb = getBase(tb.getSuperclass().getTypeDeclaration(), mb, sig);
			if (stb != null)
				return stb;
		}
		for (ITypeBinding itb : tb.getInterfaces()) {
			ITypeBinding stb = getBase(itb.getTypeDeclaration(), mb, sig);
			if (stb != null)
				return stb;
		}
		if (mb.getDeclaringClass().getTypeDeclaration() == tb)
			return tb;
		for (IMethodBinding smb : tb.getDeclaredMethods()) {
			if (JavaASTUtil.buildSignature(smb).equals(sig))
				return tb;
		}
		return null;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			MethodDeclaration astNode) {
		EGroumGraph pdg = new EGroumGraph(context);
		// skip
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			LabeledStatement astNode) {
		adjustBreakNodes(astNode.getLabel().getIdentifier());
		return buildPDG(control, branch, astNode.getBody());
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			InstanceofExpression astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getLeftOperand());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, 
				JavaASTUtil.getSimpleType(astNode.getRightOperand()) + ".<instanceof>");
		pdg.mergeSequentialData(node, Type.PARAMETER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			Initializer astNode) {
		return buildPDG(control, branch, astNode.getBody());
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			InfixExpression astNode) {
		EGroumGraph pdg = new EGroumGraph(context);
		EGroumGraph lg = buildArgumentPDG(control, branch,
				astNode.getLeftOperand());
		EGroumGraph rg = buildArgumentPDG(control, branch,
				astNode.getRightOperand());
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, JavaASTUtil.buildLabel(astNode.getOperator()));
		lg.mergeSequentialData(node, Type.PARAMETER);
		rg.mergeSequentialData(node, Type.PARAMETER);
		pdg.mergeParallel(lg, rg);
		if (astNode.hasExtendedOperands())
			for (int i = 0; i < astNode.extendedOperands().size(); i++) {
				EGroumGraph tmp = buildArgumentPDG(control, branch,
						(Expression) astNode.extendedOperands().get(i));
				tmp.mergeSequentialData(node, Type.PARAMETER);
				pdg.mergeParallel(tmp);
			}
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			IfStatement astNode) {
		context.addScope();
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, Type.CONDITION);
		EGroumGraph etg = new EGroumGraph(context, new EGroumActionNode(node, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		EGroumGraph tg = buildPDG(node, "T", astNode.getThenStatement());
		if (!tg.isEmpty())
			etg.mergeSequential(tg);
		EGroumGraph efg = new EGroumGraph(context, new EGroumActionNode(node, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		if (astNode.getElseStatement() != null) {
			EGroumGraph fg = buildPDG(node, "F", astNode.getElseStatement());
			if (!fg.isEmpty())
				efg.mergeSequential(fg);
		}
		pdg.mergeBranches(etg, efg);
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ForStatement astNode) {
		context.addScope();
		EGroumGraph pdg = null;
		if (astNode.initializers() != null && astNode.initializers().size() > 0) {
			pdg = buildPDG(control, branch, (ASTNode) astNode.initializers()
					.get(0));
			for (int i = 1; i < astNode.initializers().size(); i++)
				pdg.mergeSequential(buildPDG(control, branch, (ASTNode) astNode
						.initializers().get(i)));
		}
		EGroumGraph middleG = null;
		if (astNode.getExpression() != null) {
			middleG = buildArgumentPDG(control, branch, astNode.getExpression());
		}
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		if (middleG != null)
			middleG.mergeSequentialData(node, Type.CONDITION);
		else
			middleG = new EGroumGraph(context, node);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(node, "T", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		EGroumGraph bg = buildPDG(node, "T", astNode.getBody());
		if (!bg.isEmpty()) {
			ebg.mergeSequential(bg);
		}
		if (astNode.updaters() != null && astNode.updaters().size() > 0) {
			EGroumGraph ug = buildPDG(node, "T", (ASTNode) astNode.updaters()
					.get(0));
			for (int i = 1; i < astNode.updaters().size(); i++) {
				ug.mergeSequential(buildPDG(node, "T", (ASTNode) astNode
						.updaters().get(i)));
			}
			ebg.mergeSequential(ug);
		}
		
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		middleG.mergeBranches(ebg, eg);
		if (pdg == null)
			pdg = middleG;
		else
			pdg.mergeSequential(middleG);
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ExpressionStatement astNode) {
		EGroumGraph pdg = buildPDG(control, branch, astNode.getExpression());
		ArrayList<EGroumActionNode> rets = pdg.getReturns();
		if (rets.size() > 0) {
			for (EGroumNode ret : new HashSet<EGroumNode>(rets)) {
				for (EGroumEdge e : new HashSet<EGroumEdge>(ret.inEdges))
					if (e.source instanceof EGroumDataNode)
						pdg.delete(e.source);
				pdg.delete(ret);
			}
		}
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			EnhancedForStatement astNode) {
		context.addScope();
		SimpleName name = astNode.getParameter().getName();
		String type = JavaASTUtil.getSimpleType(astNode.getParameter().getType());
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode var = new EGroumDataNode(name, name.getNodeType(),
				"" + name.getStartPosition(), type,
				name.getIdentifier(), false, true);
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		pdg.mergeSequentialData(new EGroumActionNode(control, branch,
				astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
		pdg.mergeSequentialData(var, Type.DEFINITION);
		pdg.mergeSequentialData(new EGroumDataNode(null, var.astNodeType, var.key, var.dataType, var.dataName), Type.REFERENCE);
		EGroumControlNode node = new EGroumControlNode(control, branch, astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, Type.CONDITION);
		pdg.mergeSequentialControl(new EGroumActionNode(node, "",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
		EGroumGraph bg = buildPDG(node, "", astNode.getBody());
		if (!bg.isEmpty())
			pdg.mergeSequential(bg);
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			DoStatement astNode) {
		context.addScope();
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		pdg.mergeSequentialData(node, Type.CONDITION);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(node, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		EGroumGraph bg = buildPDG(node, "T", astNode.getBody());
		if (!bg.isEmpty())
			ebg.mergeSequential(bg);
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"));
		pdg.mergeBranches(ebg, eg);
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ContinueStatement astNode) {
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), astNode.getLabel() == null ? "" : astNode.getLabel().getIdentifier(), null,
				"continue");
		EGroumGraph pdg = new EGroumGraph(context, node);
		pdg.breaks.add(node);
		pdg.sinks.remove(node);
		pdg.statementSinks.remove(node);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ConstructorInvocation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size()];
		int numOfParameters = 0;
		for (int i = 0; i < astNode.arguments().size(); i++) {
			pgs[numOfParameters] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
			numOfParameters++;
		}
		String type = context.getType();
		HashSet<String> exceptions = null;
		if (astNode.resolveConstructorBinding() != null) {
			IMethodBinding mb = astNode.resolveConstructorBinding();
			String sig = JavaASTUtil.buildSignature(mb);
			ITypeBinding tb = getBase(mb.getDeclaringClass().getTypeDeclaration(), mb, sig);
			type = tb.getName();
			exceptions = new HashSet<>();
			for (ITypeBinding etb : mb.getExceptionTypes())
				exceptions.add(etb.getName());
		}
		if (exceptions == null)
			exceptions = EGroumBuildingContext.getExceptions(type, "<init>" + "(" + astNode.arguments().size() + ")");
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + "()", "<init>", exceptions);
		EGroumGraph pdg = null;
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, Type.PARAMETER);
			pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ConditionalExpression astNode) {
		String type = "UNKNOWN";
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
						+ astNode.getLength(), type, EGroumNode.PREFIX_DUMMY, false, true);
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, ASTNode.IF_STATEMENT);
		pdg.mergeSequentialData(node, Type.CONDITION);
		EGroumGraph tg = buildArgumentPDG(node, "T", astNode.getThenExpression());
		tg.mergeSequentialData(new EGroumActionNode(node, "T", null, ASTNode.ASSIGNMENT,
				null, null, "="), Type.PARAMETER);
		tg.mergeSequentialData(new EGroumDataNode(dummy), Type.DEFINITION);
		EGroumGraph fg = buildArgumentPDG(node, "F", astNode.getElseExpression());
		fg.mergeSequentialData(new EGroumActionNode(node, "F", null, ASTNode.ASSIGNMENT,
				null, null, "="), Type.PARAMETER);
		fg.mergeSequentialData(new EGroumDataNode(dummy), Type.DEFINITION);
		pdg.mergeBranches(tg, fg);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ClassInstanceCreation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size()];
		int numOfParameters = 0;
		for (int i = 0; i < astNode.arguments().size(); i++) {
			pgs[numOfParameters] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
			numOfParameters++;
		}
		String type = JavaASTUtil.getSimpleType(astNode.getType());
		HashSet<String> exceptions = null;
		if (astNode.resolveConstructorBinding() != null) {
			IMethodBinding b = astNode.resolveConstructorBinding();
			exceptions = new HashSet<>();
			for (ITypeBinding tb : b.getExceptionTypes())
				exceptions.add(tb.getName());
			String sig = JavaASTUtil.buildSignature(b);
			ITypeBinding tb = getBase(b.getDeclaringClass().getTypeDeclaration(), b, sig);
			type = tb.getName();
			if (type.isEmpty())
				type = JavaASTUtil.getSimpleType(astNode.getType());
		}
		if (exceptions == null)
			exceptions = EGroumBuildingContext.getExceptions(type, "<init>" + "(" + astNode.arguments().size() + ")");
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + ".<init>"/*"<init>"*/, "<init>", exceptions);
		context.addMethodTry(node);
		EGroumGraph pdg = null;
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, Type.PARAMETER);
			pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node);
		// skip astNode.getExpression()
		AnonymousClassDeclaration acd = astNode.getAnonymousClassDeclaration();
		if (acd != null) {
			EGroumGraph acg = new EGroumGraph(context);
			EGroumDataNode acn = new EGroumDataNode(acd, ASTNode.TYPE_LITERAL, "" + acd.getStartPosition(), type, type, false, true);
			new EGroumDataEdge(acn, node, Type.RECEIVER);
			for (int i = 0; i < acd.bodyDeclarations().size(); i++) {
				if (acd.bodyDeclarations().get(i) instanceof MethodDeclaration) {
					MethodDeclaration md = (MethodDeclaration) acd.bodyDeclarations().get(i);
					if (md.getBody() != null && !md.getBody().statements().isEmpty()) {
						EGroumDataNode mdn = new EGroumDataNode(md, md.getNodeType(), "" + md.getStartPosition(), type + "." + md.getName().getIdentifier() + "()", type + "." + md.getName().getIdentifier() + "()", false, true);
						new EGroumDataEdge(acn, mdn, Type.CONTAINS);
						EGroumControlNode dummy = new EGroumControlNode(control, branch, null, 0);
						EGroumGraph mg = buildPDG(dummy, "", md.getBody());
						for (EGroumNode mgn : mg.nodes) {
							if (mgn instanceof EGroumActionNode)
								new EGroumDataEdge(mdn, mgn, Type.CONTAINS);
							if (mgn.control == dummy)
								mgn.control = null;
						}
						dummy.delete();
						mg.nodes.add(mdn);
						acg.mergeParallel(mg);
					}
				}
			}
			acg.nodes.add(acn);
			acg.breaks.clear();
			acg.clearDefStore();
			acg.returns.clear();
			acg.sinks.clear();
			acg.statementSinks.clear();
			acg.statementSources.clear();
			pdg.mergeParallel(acg);
		}
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			CharacterLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.getEscapedValue(), "char",
				astNode.getEscapedValue()));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			CatchClause astNode, ArrayList<EGroumActionNode> triedMethods) {
		context.addScope();
		SimpleName name = astNode.getException().getName();
		String type = JavaASTUtil.getSimpleType(astNode.getException().getType());
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode cn = new EGroumDataNode(name, name.getNodeType(),
				"" + name.getStartPosition(), type, name.getIdentifier(), false, true);
		EGroumGraph pdg = new EGroumGraph(context);
		pdg.mergeSequentialData(cn, Type.DEFINITION);
		pdg.mergeSequentialData(new EGroumDataNode(null, cn.astNodeType, cn.key, cn.dataType, cn.dataName), Type.REFERENCE);
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, Type.CONDITION);
		EGroumGraph bg = buildPDG(node, "", astNode.getBody());
		if (!bg.isEmpty())
			pdg.mergeSequential(bg);
		HashSet<EGroumActionNode> nodes = new HashSet<>();
		if (triedMethods.size() == 1) {
			nodes.add(triedMethods.get(0));
		} else {
			if (astNode.getException().getType().isUnionType()) {
				UnionType ut = (UnionType) astNode.getException().getType();
				for (int i = 0; i < ut.types().size(); i++) {
					org.eclipse.jdt.core.dom.Type t = (org.eclipse.jdt.core.dom.Type) ut.types().get(i);
					nodes.addAll(context.getTrys(JavaASTUtil.getSimpleType(t), triedMethods));
				}
			}
			else
				nodes = context.getTrys(JavaASTUtil.getSimpleType(astNode.getException().getType()), triedMethods);
		}
		for (EGroumActionNode n : nodes) {
			new EGroumDataEdge(n, cn, Type.THROW);
		}
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			CastExpression astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		String type = JavaASTUtil.getSimpleType(astNode.getType());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + ".<cast>", type + ".<cast>");
		pdg.mergeSequentialData(node, Type.PARAMETER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			BreakStatement astNode) {
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), astNode.getLabel() == null ? "" : astNode.getLabel().getIdentifier(), null,
				"break");
		EGroumGraph pdg = new EGroumGraph(context, node);
		pdg.breaks.add(node);
		pdg.sinks.remove(node);
		pdg.statementSinks.remove(node);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			BooleanLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.toString(), "boolean",
				astNode.toString()));
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, Block astNode) {
		if (astNode.statements().size() > 0) {
			context.addScope();
			EGroumGraph pdg = buildPDG(control, branch, astNode.statements());
			context.removeScope();
			return pdg;
		}
		return new EGroumGraph(context);
	}

	public EGroumGraph buildPDG(EGroumNode control, String branch, List<?> list) {
		EGroumGraph g = new EGroumGraph(context);
		for (int i = 0; i < list.size(); i++) {
			Object s = list.get(i);
			if (s instanceof EmptyStatement) continue;
			EGroumGraph pdg = buildPDG(control, branch, (ASTNode) s);
			if (!pdg.isEmpty())
				g.mergeSequential(pdg);
			if (list.get(i) instanceof ReturnStatement || list.get(i) instanceof ThrowStatement || list.get(i).toString().startsWith("System.exit(")) {
				g.clearDefStore();
				return g;
			}
		}
		return g;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, Assignment astNode) {
		if (astNode.getLeftHandSide() instanceof ArrayAccess) {
			ArrayAccess aa = (ArrayAccess) astNode.getLeftHandSide();
			EGroumGraph ag = buildArgumentPDG(control, branch, aa.getArray());
			String type = ag.getOnlyOut().dataType;
			if (aa.getArray().resolveTypeBinding() != null)
				type = aa.getArray().resolveTypeBinding().getTypeDeclaration().getName();
			EGroumNode node = new EGroumActionNode(control, branch, aa, aa.getNodeType(), null, type + ".arrayset()", "arrayset()");
			ag.mergeSequentialData(node, Type.RECEIVER);
			EGroumGraph ig = buildArgumentPDG(control, branch, aa.getIndex());
			ig.mergeSequentialData(node, Type.PARAMETER);
			EGroumGraph vg = null;
			if (astNode.getOperator() == Assignment.Operator.ASSIGN) {
				vg = buildArgumentPDG(control, branch, astNode.getRightHandSide());
			} else {
				String op = JavaASTUtil.getInfixOperator(astNode.getOperator());
				EGroumGraph g1 = buildPDG(control, branch, astNode.getLeftHandSide());
				EGroumGraph g2 = buildArgumentPDG(control, branch, astNode.getRightHandSide());
				EGroumActionNode opNode = new EGroumActionNode(control, branch, null, ASTNode.INFIX_EXPRESSION, null, null, op);
				g1.mergeSequentialData(opNode, Type.PARAMETER);
				g2.mergeSequentialData(opNode, Type.PARAMETER);
				vg = new EGroumGraph(context);
				vg.mergeParallel(g1, g2);
				EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
						EGroumNode.PREFIX_DUMMY + astNode.getRightHandSide().getStartPosition() + "_"
								+ astNode.getRightHandSide().getLength(), null, EGroumNode.PREFIX_DUMMY, false, true);
				vg.mergeSequentialData(dummy, Type.DEFINITION);
				vg.mergeSequentialData(new EGroumDataNode(null, dummy.astNodeType,
						dummy.key, dummy.dataType, dummy.dataName), Type.REFERENCE);
			}
			vg.mergeSequentialData(node, Type.PARAMETER);
			EGroumGraph pdg = new EGroumGraph(context);
			pdg.mergeParallel(ag, ig, vg);
			return pdg;
		}
		if (!(astNode.getLeftHandSide() instanceof Name))
			return buildPDG(control, branch, astNode.getRightHandSide());
		EGroumGraph lg = buildPDG(control, branch, astNode.getLeftHandSide());
		EGroumDataNode lnode = lg.getOnlyDataOut();
		EGroumGraph pdg = null;
		if (astNode.getOperator() != Assignment.Operator.ASSIGN) {
			String op = JavaASTUtil.getInfixOperator(astNode.getOperator());
			EGroumGraph g1 = buildPDG(control, branch, astNode.getLeftHandSide());
			EGroumGraph g2 = buildArgumentPDG(control, branch,
					astNode.getRightHandSide());
			EGroumActionNode opNode = new EGroumActionNode(control, branch,
					null, ASTNode.INFIX_EXPRESSION, null, null, op);
			g1.mergeSequentialData(opNode, Type.PARAMETER);
			g2.mergeSequentialData(opNode, Type.PARAMETER);
			pdg = new EGroumGraph(context);
			pdg.mergeParallel(g1, g2);
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
			pdg.mergeSequentialData(lnode, Type.DEFINITION);
		} else {
			pdg = buildPDG(control, branch, astNode.getRightHandSide());
			ArrayList<EGroumDataNode> defs = pdg.getDefinitions();
			if (defs.isEmpty()) {
				pdg.mergeSequentialData(new EGroumActionNode(control, branch,
						astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
				pdg.mergeSequentialData(lnode, Type.DEFINITION);
			} else {
				if (defs.get(0).isDummy()) {
					for (EGroumDataNode def : defs) {
						pdg.defStore.remove(def.key);
						def.copyData(lnode);
						HashSet<EGroumDataNode> ns = pdg.defStore.get(def.key);
						if (ns == null) {
							ns = new HashSet<>();
							pdg.defStore.put(def.key, ns);
						}
						ns.add(def);
					}
				} else {
					EGroumDataNode def = defs.get(0);
					pdg.mergeSequentialData(new EGroumDataNode(null, def.astNodeType,
							def.key, def.dataType, def.dataName),
							Type.REFERENCE);
					pdg.mergeSequentialData(new EGroumActionNode(control, branch,
							astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
					pdg.mergeSequentialData(lnode, Type.DEFINITION);
				}
			}
		}
		pdg.nodes.addAll(lg.nodes);
		pdg.statementNodes.addAll(lg.statementNodes);
		lg.dataSources.remove(lnode);
		pdg.dataSources.addAll(lg.dataSources);
		pdg.statementSources.addAll(lg.statementSources);
		lg.clear();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			AssertStatement astNode) {
		// skip assert statement
		return new EGroumGraph(context);
		/*EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, "assert");
		pdg.mergeSequentialData(node, Type.PARAMETER);
		// skip astNode.getMessage()
		return pdg;*/
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ArrayInitializer astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.expressions().size()];
		for (int i = 0; i < astNode.expressions().size(); i++) {
			pgs[i] = buildArgumentPDG(control, branch,
					(Expression) astNode.expressions().get(i));
		}
		EGroumNode node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
				null, "{}", "{}");
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, Type.PARAMETER);
			EGroumGraph pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
			return pdg;
		} else
			return new EGroumGraph(context, node);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ArrayCreation astNode) {
		if (astNode.getInitializer() != null) {
			return buildPDG(control, branch, astNode.getInitializer());
		}
		EGroumGraph[] pgs = new EGroumGraph[astNode.dimensions().size()];
		for (int i = 0; i < astNode.dimensions().size(); i++)
			pgs[i] = buildArgumentPDG(control, branch,
					(Expression) astNode.dimensions().get(i));
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, "{}", "<init>");
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, Type.PARAMETER);
			EGroumGraph pdg = new EGroumGraph(context);
			pdg.mergeParallel(pgs);
			return pdg;
		} else
			return new EGroumGraph(context, node);
	}

	private void mergeBranches(EGroumGraph... pdgs) {
		HashMap<String, HashSet<EGroumDataNode>> defStore = new HashMap<>();
		HashMap<String, Integer> defCounts = new HashMap<>();
		sinks.clear();
		statementSinks.clear();
		boolean hasNoStatementSources = statementSources.isEmpty();
		for (EGroumGraph pdg : pdgs) {
			nodes.addAll(pdg.nodes);
			statementNodes.addAll(pdg.statementNodes);
			if (hasNoStatementSources)
				statementSources.addAll(pdg.statementSources);
			sinks.addAll(pdg.sinks);
			statementSinks.addAll(pdg.statementSinks);
			for (EGroumDataNode source : new HashSet<EGroumDataNode>(pdg.dataSources)) {
				HashSet<EGroumDataNode> defs = this.defStore.get(source.key);
				if (defs != null) {
					for (EGroumDataNode def : defs)
						if (def != null)
							new EGroumDataEdge(def, source, Type.REFERENCE);
					if (!defs.contains(null))
						pdg.dataSources.remove(source);
				}
			}
			dataSources.addAll(pdg.dataSources);
			// statementSources.addAll(pdg.statementSources);
			breaks.addAll(pdg.breaks);
			returns.addAll(pdg.returns);
		}
		for (EGroumGraph pdg : pdgs) {
			HashMap<String, HashSet<EGroumDataNode>> localStore = copyDefStore();
			updateDefStore(localStore, defCounts, pdg.defStore);
			add(defStore, localStore);
			pdg.clear();
		}
		for (String key : defCounts.keySet())
			if (defCounts.get(key) < pdgs.length)
				defStore.get(key).add(null);
		clearDefStore();
		this.defStore = defStore;
	}

	private void mergeParallel(EGroumGraph... pdgs) {
		HashMap<String, HashSet<EGroumDataNode>> defStore = new HashMap<>();
		HashMap<String, Integer> defCounts = new HashMap<>();
		for (EGroumGraph pdg : pdgs) {
			HashMap<String, HashSet<EGroumDataNode>> localStore = copyDefStore();
			nodes.addAll(pdg.nodes);
			statementNodes.addAll(pdg.statementNodes);
			sinks.addAll(pdg.sinks);
			statementSinks.addAll(pdg.statementSinks);
			dataSources.addAll(pdg.dataSources);
			statementSources.addAll(pdg.statementSources);
			breaks.addAll(pdg.breaks);
			returns.addAll(pdg.returns);
			updateDefStore(localStore, defCounts, pdg.defStore);
			add(defStore, localStore);
			pdg.clear();
		}
		clearDefStore();
		this.defStore = defStore;
	}

	private void clear() {
		nodes.clear();
		statementNodes.clear();
		dataSources.clear();
		statementSources.clear();
		sinks.clear();
		statementSinks.clear();
		breaks.clear();
		returns.clear();
		clearDefStore();
	}

	public void delete(EGroumNode node) {
		if (statementSinks.contains(node))
			for (EGroumEdge e : node.inEdges)
				if (e instanceof EGroumDataEdge) {
					if (((EGroumDataEdge) e).type == Type.DEPENDENCE)
						statementSinks.add(e.source);
					else if (((EGroumDataEdge) e).type == Type.PARAMETER)
						sinks.add(e.source);
				}
		if (sinks.contains(node) && node instanceof EGroumDataNode) {
			for (EGroumEdge e : node.inEdges)
				if (e.source instanceof EGroumDataNode)
					sinks.add(e.source);
		}
		if (statementSources.contains(node))
			for (EGroumEdge e : node.outEdges)
				if (e instanceof EGroumDataEdge
						&& ((EGroumDataEdge) e).type == Type.DEPENDENCE)
					statementSources.add(e.target);
		nodes.remove(node);
		statementNodes.remove(node);
		dataSources.remove(node);
		statementSources.remove(node);
		sinks.remove(node);
		statementSinks.remove(node);
		EGroumNode qual = node.getQualifier();
		if (qual != null && !qual.isDeclaration())
			delete(qual);
		node.delete();
	}

	private EGroumGraph buildArgumentPDG(EGroumNode control, String branch,
			ASTNode exp) {
		EGroumGraph pdg = buildPDG(control, branch, exp);
		if (pdg.isEmpty())
			return pdg;
		if (pdg.nodes.size() == 1)
			for (EGroumNode node : pdg.nodes)
				if (node instanceof EGroumDataNode)
					return pdg;
		ArrayList<EGroumDataNode> defs = pdg.getDefinitions();
		if (!defs.isEmpty()) {
			EGroumDataNode def = defs.get(0);
			String type = ((EGroumDataNode) def).dataType;
			if (((Expression) exp).resolveTypeBinding() != null)
				type = ((Expression) exp).resolveTypeBinding().getTypeDeclaration().getName();
			pdg.mergeSequentialData(new EGroumDataNode(null, def.astNodeType, def.key,
					type, ((EGroumDataNode) def).dataName,
					def.isField, false), Type.REFERENCE);
			return pdg;
		}
		EGroumNode node = pdg.getOnlyOut();
		if (node instanceof EGroumDataNode)
			return pdg;
		String type = node.dataType;
		if (((Expression) exp).resolveTypeBinding() != null)
			type = ((Expression) exp).resolveTypeBinding().getTypeDeclaration().getName();
		EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + exp.getStartPosition() + "_"
						+ exp.getLength(), type, EGroumNode.PREFIX_DUMMY, false, true);
		pdg.mergeSequentialData(new EGroumActionNode(control, branch,
				null, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
		pdg.mergeSequentialData(dummy, Type.DEFINITION);
		pdg.mergeSequentialData(new EGroumDataNode(null, dummy.astNodeType, dummy.key,
				dummy.dataType, dummy.dataName), Type.REFERENCE);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ArrayAccess astNode) {
		EGroumGraph ag = buildArgumentPDG(control, branch, astNode.getArray());
		String type = ag.getOnlyOut().dataType;
		if (astNode.getArray().resolveTypeBinding() != null)
			type = astNode.getArray().resolveTypeBinding().getTypeDeclaration().getName();
		EGroumNode node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(), null, type + ".arrayget()", "arrayget()");
		ag.mergeSequentialData(node, Type.RECEIVER);
		EGroumGraph ig = buildArgumentPDG(control, branch, astNode.getIndex());
		ig.mergeSequentialData(node, Type.PARAMETER);
		EGroumGraph pdg = new EGroumGraph(context);
		pdg.mergeParallel(ag, ig);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			FieldAccess astNode) {
		if (astNode.getExpression() instanceof ThisExpression) {
			String name = astNode.getName().getIdentifier();
			String type = null;
			if (astNode.resolveTypeBinding() != null)
				type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
			if (type == null)
				type = context.getFieldType(astNode.getName());
			if (type != null) {
				EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
						null, ASTNode.THIS_EXPRESSION, "this",
						context.getType(), "this"));
				pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME,
						"this." + name, type, name, true,
						false), Type.QUALIFIER);
				return pdg;
			}
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this",
					context.getType(), "this"));
			pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME,
					"this." + name, "UNKNOWN", name, true,
					false), Type.QUALIFIER);
			return pdg;
		} else {
			EGroumGraph pdg = buildArgumentPDG(control, branch,
					astNode.getExpression());
			EGroumDataNode qual = pdg.getOnlyDataOut();
			String type = qual.dataType;
			if (astNode.getExpression().resolveTypeBinding() != null)
				type = astNode.getExpression().resolveTypeBinding().getTypeDeclaration().getName();
			EGroumDataNode node = new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, qual.key == null ? astNode.toString() : qual.key + "." + astNode.getName().getIdentifier(),
					type + "." + astNode.getName().getIdentifier(),
					astNode.getName().getIdentifier(), true, false);
			pdg.mergeSequentialData(node, Type.QUALIFIER);
			return pdg;
		}
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SuperFieldAccess astNode) {
		String name = astNode.getName().getIdentifier();
		String type = null;
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		if (type == null)
			type = context.getFieldType(astNode.getName());
		if (type != null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this",
					context.getSuperType(), "super"));
			pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME,
					"this." + name, type, name, true,
					false), Type.QUALIFIER);
			return pdg;
		}
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.THIS_EXPRESSION, "this", context.getSuperType(), "super"));
		pdg.mergeSequentialData(
				new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, astNode.toString(),
						context.getSuperType() + "." + astNode.getName().getIdentifier(),
						astNode.getName().getIdentifier(), true, false), Type.QUALIFIER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ThisExpression astNode) {
		String type = context.getType();
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), "this", type,
				"this"));
		return pdg;
	}

	private void mergeSequential(EGroumGraph pdg) {
		if (pdg.statementNodes.isEmpty())
			return;
		for (EGroumDataNode source : new HashSet<EGroumDataNode>(pdg.dataSources)) {
			HashSet<EGroumDataNode> defs = defStore.get(source.key);
			if (defs != null) {
				for (EGroumDataNode def : defs)
					if (def != null)
						new EGroumDataEdge(def, source, Type.REFERENCE);
				if (!defs.contains(null))
					pdg.dataSources.remove(source);
			}
		}
		updateDefStore(pdg.defStore);
		for (EGroumNode sink : statementSinks) {
			for (EGroumNode source : pdg.statementSources) {
				new EGroumDataEdge(sink, source, Type.DEPENDENCE);
			}
		}
		/*if (this.statementNodes.isEmpty() || pdg.statementNodes.isEmpty()) {
			System.err.println("Merge an empty graph!!!");
			System.exit(-1);
		}*/
		this.dataSources.addAll(pdg.dataSources);
		this.sinks.clear();
		this.statementSinks.clear();
		this.statementSinks.addAll(pdg.statementSinks);
		this.nodes.addAll(pdg.nodes);
		this.statementNodes.addAll(pdg.statementNodes);
		this.breaks.addAll(pdg.breaks);
		this.returns.addAll(pdg.returns);
		pdg.clear();
	}

	private static <E> void add(HashMap<String, HashSet<E>> target,
			HashMap<String, HashSet<E>> source) {
		for (String key : source.keySet())
			if (target.containsKey(key))
				target.get(key).addAll(new HashSet<E>(source.get(key)));
			else
				target.put(key, new HashSet<E>(source.get(key)));
	}

	private static <E> void clear(HashMap<String, HashSet<E>> map) {
		for (String key : map.keySet())
			map.get(key).clear();
		map.clear();
	}

	private static <E> void update(HashMap<String, HashSet<E>> target,
			HashMap<String, HashSet<E>> source) {
		for (String key : source.keySet()) {
			HashSet<E> s = source.get(key);
			if (s.contains(null)) {
				if (target.containsKey(key)) {
					s.remove(null);
					target.get(key).addAll(new HashSet<E>(s));
				} else
					target.put(key, new HashSet<E>(s));
			} else
				target.put(key, new HashSet<E>(s));
		}
	}

	private void clearDefStore() {
		clear(defStore);
	}

	private HashMap<String, HashSet<EGroumDataNode>> copyDefStore() {
		HashMap<String, HashSet<EGroumDataNode>> store = new HashMap<>();
		for (String key : defStore.keySet())
			store.put(key, new HashSet<>(defStore.get(key)));
		return store;
	}

	private void updateDefStore(HashMap<String, HashSet<EGroumDataNode>> store) {
		update(defStore, store);
	}

	private void updateDefStore(HashMap<String, HashSet<EGroumDataNode>> target,
			HashMap<String, Integer> defCounts,
			HashMap<String, HashSet<EGroumDataNode>> source) {
		for (String key : source.keySet())
			target.put(key, new HashSet<>(source.get(key)));
		for (String key : target.keySet()) {
			int c = 1;
			if (defCounts.containsKey(key))
				c += defCounts.get(key);
			defCounts.put(key, c);
		}
	}

	private void mergeSequentialControl(EGroumNode next, String label) {
		sinks.clear();
		sinks.add(next);
		statementSinks.clear();
		statementSinks.add(next);
		if (statementNodes.isEmpty())
			statementSources.add(next);
		nodes.add(next);
		statementNodes.add(next);
	}

	private void mergeSequentialData(EGroumNode next, Type type) {
		if (next.isStatement())
			for (EGroumNode sink : statementSinks) {
				new EGroumDataEdge(sink, next, Type.DEPENDENCE);
			}
		if (type == Type.DEFINITION) {
			HashSet<EGroumDataNode> ns = new HashSet<>();
			ns.add((EGroumDataNode) next);
			defStore.put(next.key, ns);
		} else if (type == Type.QUALIFIER) {
			dataSources.add((EGroumDataNode) next);
		} else if (type != Type.REFERENCE && next instanceof EGroumDataNode) {
			HashSet<EGroumDataNode> ns = defStore.get(next.key);
			if (ns != null)
				for (EGroumDataNode def : ns)
					new EGroumDataEdge(def, next, Type.REFERENCE);
		}
		for (EGroumNode node : sinks)
			new EGroumDataEdge(node, next, type, type == Type.CONDITION ? next.getConditionLabel() : null);
		sinks.clear();
		sinks.add(next);
		if (nodes.isEmpty() && next instanceof EGroumDataNode)
			dataSources.add((EGroumDataNode) next);
		nodes.add(next);
		if (next.isStatement()) {
			statementNodes.add(next);
			if (statementSources.isEmpty())
				statementSources.add(next);
			statementSinks.clear();
			statementSinks.add(next);
		}
	}

	private void adjustBreakNodes(String id) {
		for (EGroumNode node : new HashSet<EGroumNode>(breaks)) {
			if ((node.key == null && id == null) || node.key.equals(id)) {
				sinks.add(node);
				statementSinks.add(node);
				breaks.remove(node);
			}
		}
	}

	private void adjustControlEdges() {
		for (EGroumNode node : statementNodes) {
			ArrayList<EGroumNode> ens = node.getIncomingEmptyNodes();
			if (ens.size() == 1 && node.getInDependences().size() == 1) {
				EGroumNode en = ens.get(0);
				if (node.control != en.control) {
					node.control.adjustControl(node, en);
				}
			}
		}
	}

	private ArrayList<EGroumDataNode> getDefinitions() {
		ArrayList<EGroumDataNode> defs = new ArrayList<>();
		for (EGroumNode node : sinks)
			if (node.isDefinition())
				defs.add((EGroumDataNode) node);
		return defs;
	}

	private ArrayList<EGroumActionNode> getReturns() {
		ArrayList<EGroumActionNode> nodes = new ArrayList<>();
		for (EGroumNode node : statementSinks)
			if (node.astNodeType == ASTNode.RETURN_STATEMENT)
				nodes.add((EGroumActionNode) node);
		return nodes;
	}

	private EGroumNode getOnlyOut() {
		if (sinks.size() == 1)
			for (EGroumNode n : sinks)
				return n;
		System.err.println("ERROR in getting the only output node!!!");
		System.exit(-1);
		return null;
	}

	private EGroumDataNode getOnlyDataOut() {
		if (sinks.size() == 1)
			for (EGroumNode n : sinks)
				if (n instanceof EGroumDataNode)
					return (EGroumDataNode) n;
		System.err.println("ERROR in getting the only data output node!!!");
		System.exit(-1);
		return null;
	}

	private boolean isEmpty() {
		return nodes.isEmpty();
	}
	
	public EGroumGraph collapse() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			for (EGroumEdge e : node.outEdges) {
				if (e.target.getLabel().equals(node.getLabel()) && e.isDirect()) {
					for (EGroumEdge e1 : node.inEdges)
						if (!e.target.hasInEdge(e1) && e1 instanceof EGroumDataEdge)
							new EGroumDataEdge(e1.source, e.target, ((EGroumDataEdge) e1).type, ((EGroumDataEdge) e1).label);
					delete(node);
					break;
				}
			}
		}
		return this;
	}

	public void buildClosure() {
		HashSet<EGroumNode> doneNodes = new HashSet<EGroumNode>();
		for (EGroumNode node : nodes)
			if (!doneNodes.contains(node))
				node.buildDataClosure(doneNodes);
		buildSequentialClosure();
		doneNodes.clear();
		pruneTemporaryDataDependence();
		doneNodes = new HashSet<EGroumNode>();
		for (EGroumNode node : nodes)
			if (!doneNodes.contains(node))
				node.buildControlClosure(doneNodes);
	}

	private void buildSequentialClosure() {
		HashMap<EGroumNode, HashSet<EGroumNode>> preNodesOfNode = new HashMap<>();
		preNodesOfNode.put(entryNode, new HashSet<EGroumNode>());
		HashSet<EGroumNode> visitedNodes = new HashSet<>();
		visitedNodes.add(entryNode);
		for (EGroumNode node : nodes) {
			if (!visitedNodes.contains(node))
				node.buildPreSequentialNodes(visitedNodes, preNodesOfNode);
		}
		visitedNodes.clear();
		for (EGroumNode node : preNodesOfNode.keySet()) {
			if (node.isCoreAction()) {
				for (EGroumNode preNode : preNodesOfNode.get(node)) {
					if (preNode.isCoreAction() && !node.hasInNode(preNode) && ((EGroumActionNode) node).hasBackwardDataDependence((EGroumActionNode) preNode)) {
						HashSet<EGroumNode> preNodes = new HashSet<>(preNodesOfNode.get(preNode));
						boolean inDifferentCatches = false;
						HashSet<EGroumNode> cns = node.getCatchClauses();
						boolean isCatched = cns.isEmpty();
						for (EGroumNode cn : cns) {
							for (EGroumEdge e : cn.inEdges) {
								if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.CONDITION) {
									EGroumNode en = e.source.getDefinition();
									if (en == null) continue;
									for (EGroumEdge e1 : en.inEdges) {
										if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == Type.THROW) {
											isCatched = true;
											if (preNodes.contains(e1.source)) {
												inDifferentCatches = true;
												break;
											}
										}
										if (inDifferentCatches)
											break;
									}
								}
								if (inDifferentCatches)
									break;
							}
							if (inDifferentCatches)
								break;
						}
						if (isCatched && !inDifferentCatches)
							new EGroumDataEdge(preNode, node, Type.ORDER);
					}
				}
			}
		}
	}

	private void pruneTemporaryDataDependence() {
		for (EGroumNode node : nodes) {
			if (node == entryNode || node == endNode)
				continue;
			int i = 0;
			while (i < node.inEdges.size()) {
				EGroumEdge e = node.inEdges.get(i);
				if (e.source != entryNode && e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.DEPENDENCE) {
					node.inEdges.remove(i);
					e.source.outEdges.remove(e);
					e.source = null;
					e.target = null;
				}
				else
					i++;
			}
			i = 0;
			while (i < node.outEdges.size()) {
				EGroumEdge e = node.outEdges.get(i);
				if (e.target != endNode && e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.DEPENDENCE) {
					node.outEdges.remove(i);
					e.target.inEdges.remove(e);
					e.source = null;
					e.target = null;
				}
				else
					i++;
			}
		}
	}

	private void deleteTemporaryDataNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node.isDefinition()) {
				EGroumDataNode dn = (EGroumDataNode) node;
				ArrayList<EGroumNode> refs = dn.getReferences();
				if (refs.size() == 1 && refs.get(0).getDefinitions().size() == 1) {
					boolean del = false;
					EGroumNode ref = refs.get(0);
					for (EGroumEdge ie : dn.inEdges)
						if (ie instanceof EGroumDataEdge && ((EGroumDataEdge) ie).type == Type.DEFINITION) {
							EGroumNode an = ie.source;
							if (!(an.control instanceof EGroumControlNode) || ((EGroumControlNode) an.control).controlsAnotherNode(an)) {
								for (EGroumEdge e : an.inEdges) {
									if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.PARAMETER)
										for (EGroumEdge oe : ref.outEdges)
											if (!oe.target.hasInNode(e.source))
												new EGroumDataEdge(e.source, oe.target, ((EGroumDataEdge) oe).type, ((EGroumDataEdge) oe).label);
								}
								delete(an);
								del = true;
							} else  {
								for (EGroumEdge in : an.getInEdges()) {
									if (in.isParameter()) {
										EGroumNode n = in.source;
										for (EGroumEdge out : an.getOutEdges())
											if ((out.isDef()) && !n.getOutNodes().contains(out.target))
												new EGroumDataEdge(n, out.target, ((EGroumDataEdge) out).type, ((EGroumDataEdge) out).label); // shortcut definition edges before deleting this assignment
									}
								}
							}
							break;
						}
					if (del) {
						delete(dn);
						delete(ref);
					}
				}
			}
		}
	}

	private void deleteReferences() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node instanceof EGroumDataNode) {
				boolean isRef = false;
				for (EGroumEdge e : node.inEdges) {
					if (e instanceof EGroumDataEdge) {
						EGroumDataEdge in = (EGroumDataEdge) e;
						if (in.type == Type.REFERENCE) {
							isRef = true;
							for (int i = 0; i < node.outEdges.size(); i++) {
								EGroumDataEdge out = (EGroumDataEdge) node.outEdges.get(i);
								new EGroumDataEdge(in.source, out.target, out.type, out.label);
							}
						}
					}
				}
				if (isRef)
					delete(node);
			}
		}
	}

	private void deleteEmptyStatementNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(statementNodes)) {
			if (node.isEmptyNode()) {
				int index = node.control.getOutEdgeIndex(node);
				for (EGroumEdge out : new HashSet<EGroumEdge>(node.outEdges)) {
					if (out.target.control != node.control) {
						out.source.outEdges.remove(out);
						out.source = node.control;
						index++;
						out.source.outEdges.add(index, out);
					}
				}
				delete(node);
				node = null;
			}
		}
	}

	private void addDefinitions() {
		HashMap<EGroumNode, HashMap<String, EGroumNode>> defs = new HashMap<>();
		defs.put(null, new HashMap<String, EGroumNode>());
		for (EGroumNode node : new HashSet<EGroumNode>(this.dataSources))
			if (this.dataSources.contains(node) && !((EGroumDataNode) node).isException())
				addDefinitions((EGroumDataNode) node, defs);
	}

	private void addDefinitions(EGroumDataNode node, HashMap<EGroumNode, HashMap<String, EGroumNode>> defs) {
		this.dataSources.remove(node);
		EGroumNode qual = node.getQualifier();
		if (qual == null) {
			EGroumNode def = defs.get(null).get(node.key);
			if (def == null) {
				def = new EGroumDataNode(null, node.astNodeType, node.key, node.dataType, node.dataName, true, true);
				defs.get(null).put(node.key, def);
				nodes.add(def);
			}
			new EGroumDataEdge(def, node, Type.REFERENCE);
		} else if (qual instanceof EGroumDataNode) {
			if (this.dataSources.contains(qual))
				addDefinitions((EGroumDataNode) qual, defs);
			ArrayList<EGroumNode> qualDefs = qual.getDefinitions();
			for (EGroumNode def : node.getDefinitions()) {
				EGroumNode qualDef = def.getQualifier();
				if (qualDef != null)
					qualDefs.removeAll(qualDef.getDefinitions());
			}
			for (EGroumNode qualDef : qualDefs) {
				HashMap<String, EGroumNode> ds = defs.get(qualDef);
				if (ds == null) {
					ds = new HashMap<>();
					defs.put(qualDef, ds);
				}
				EGroumNode def = ds.get(node.key);
				if (def == null) {
					def = new EGroumDataNode(null, node.astNodeType, node.key, node.dataType, node.dataName, node.isField, true);
					ds.put(node.key, def);
					nodes.add(def);
					EGroumDataNode qualRef = new EGroumDataNode(null, qualDef.astNodeType, qualDef.key, qualDef.dataType, ((EGroumDataNode) qualDef).dataName, ((EGroumDataNode) qualDef).isField, false);
					nodes.add(qualRef);
					new EGroumDataEdge(qualDef, qualRef, Type.REFERENCE);
					new EGroumDataEdge(qualRef, def, Type.QUALIFIER);
				}
				new EGroumDataEdge(def, node, Type.REFERENCE);
			}
			delete(qual);
		}
	}

	public void cleanUp() {
		clearDefStore();
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			node.astNode = null;
	}

	public void deleteControlNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node instanceof EGroumEntryNode || node instanceof EGroumControlNode)
				delete(node);
	}

	public void deleteUnreachableNodes() {
		HashSet<EGroumNode> reachableNodes = new HashSet<>();
		LinkedList<EGroumNode> q = new LinkedList<>();
		q.add(entryNode);
		while (!q.isEmpty()) {
			EGroumNode n = q.removeFirst();
			reachableNodes.add(n);
			for (EGroumNode next : n.getInNodes())
				if (!reachableNodes.contains(next))
					q.add(next);
			for (EGroumNode next : n.getOutNodes())
				if (!reachableNodes.contains(next))
					q.add(next);
		}
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (!reachableNodes.contains(node))
				delete(node);
	}

	public void deleteAssignmentNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node.isAssignment()) {
				for (EGroumEdge ie : node.getInEdges()) {
					if (ie.isParameter()) {
						EGroumNode n = ie.source;
						for (EGroumEdge oe : node.getOutEdges())
							if ((oe.isDef()) && !n.getOutNodes().contains(oe.target))
								new EGroumDataEdge(n, oe.target, ((EGroumDataEdge) oe).type, ((EGroumDataEdge) oe).label); // shortcut definition edges before deleting this assignment
					}
				}
				if (!(node.control instanceof EGroumControlNode) || ((EGroumControlNode) node.control).controlsAnotherNode(node))
					delete(node);
			}
	}

	public void deleteUnaryOperationNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node.astNodeType == ASTNode.PREFIX_EXPRESSION || node.astNodeType == ASTNode.POSTFIX_EXPRESSION)
				node.delete();
	}
	
	public void collapseLiterals() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			HashMap<String, ArrayList<EGroumNode>> labelLiterals = new HashMap<>();
			for (EGroumNode n : node.getInNodes()) {
				if (n.isLiteral()) {
					String label = n.getLabel();
					ArrayList<EGroumNode> lits = labelLiterals.get(label);
					if (lits == null) {
						lits = new ArrayList<>();
						labelLiterals.put(label, lits);
					}
					lits.add(n);
				}
			}
			for (String label : labelLiterals.keySet()) {
				ArrayList<EGroumNode> lits = labelLiterals.get(label);
				if (lits.size() > 0) {
//					EGroumDataNode lit = (EGroumDataNode) lits.get(1);
//					lit.dataName = lit.dataName + "*";
					for (int i = 1; i < lits.size(); i++)
						lits.get(i).delete();
				}
			}
		}
	}

	public void toGraphics(String path){
		DotGraph graph = toDotGraph();
		graph.toDotFile(new File(path + "/" + name + ".dot"));
		graph.toGraphics(path + "/" + name, "png");
	}

	public void toGraphics(String path, HashSet<EGroumNode> missingNodes, HashSet<EGroumEdge> missingEdges) {
		DotGraph graph = toDotGraph(missingNodes, missingEdges);
		graph.toDotFile(new File(path + "/" + name + ".dot"));
		graph.toGraphics(path + "/" + name, "png");
	}

	public DotGraph toDotGraph() {
		return new DotGraph(this);
	}

	private DotGraph toDotGraph(HashSet<EGroumNode> missingNodes, HashSet<EGroumEdge> missingEdges) {
		return new DotGraph(this, missingNodes, missingEdges);
	}
	
	@Override
	public String toString() {
		return toDotGraph().getGraph();
	}

	public void toGraphics(String s, String path) {
		DotGraph graph = toDotGraph(s);
		graph.toDotFile(new File(path + "/" + name + ".dot"));
		graph.toGraphics(path + "/" + name, "png");
	}

	private DotGraph toDotGraph(String s) {
		return new DotGraph(this, s);
	}
}
