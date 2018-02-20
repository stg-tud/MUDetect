package edu.iastate.cs.egroum.aug;

import edu.iastate.cs.egroum.dot.DotGraph;
import edu.iastate.cs.egroum.utils.JavaASTUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static edu.iastate.cs.egroum.aug.EGroumDataEdge.Type.*;
import static java.util.stream.Collectors.toList;

public class EGroumGraph implements Serializable {
	private static final long serialVersionUID = -5128703931982211886L;

	private final AUGConfiguration configuration;

	private String filePath, name, projectName;
	private EGroumBuildingContext context;
	protected EGroumNode entryNode, endNode;
	protected HashSet<EGroumNode> nodes = new HashSet<>();
	protected HashSet<EGroumNode> statementNodes = new HashSet<>();
	protected HashSet<EGroumDataNode> dataSources = new HashSet<>();
	protected HashSet<EGroumNode> statementSources = new HashSet<>();
	protected HashSet<EGroumNode> sinks = new HashSet<>();
	protected HashSet<EGroumNode> statementSinks = new HashSet<>();
	protected HashSet<EGroumNode> breaks = new HashSet<>();
	protected HashSet<EGroumNode> returns = new HashSet<>();
	
	public EGroumGraph(MethodDeclaration md, EGroumBuildingContext context, AUGConfiguration configuration) {
		this(context, configuration);
		if (isTooSmall(md))
			return;
		context.addScope();
		context.setMethod(md);
		entryNode = new EGroumEntryNode(md, ASTNode.METHOD_DECLARATION, "START");
		nodes.add(entryNode);
		statementNodes.add(entryNode);
		sinks.add(entryNode);
		EGroumDataNode thisNode = new EGroumDataNode(null, ASTNode.THIS_EXPRESSION, "this", context.getType(), "this", false, true);
		nodes.add(thisNode);
		HashSet<EGroumDataNode> thisDef = new HashSet<>();
		thisDef.add(thisNode);
		thisNode.defStore.put(thisNode.key, thisDef);
		for (int i = 0; i < md.parameters().size(); i++) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) md.parameters().get(i);
			EGroumGraph pg = buildPDG(entryNode, "", d);
			this.nodes.addAll(pg.nodes);
			entryNode.consumeDefStore(pg);
		}
		entryNode.consumeDefStore(thisNode.defStore);
		if (context.interprocedural)
			context.pushTry();
		if (md.getBody() != null) {
			Block block = md.getBody();
			if (!block.statements().isEmpty()) {
				EGroumGraph pdg = buildPDG(entryNode, "", block);
				addDefinitions(pdg);
				mergeSequential(pdg);
			}
		}
		if (context.interprocedural)
			statementSinks.addAll(context.popTry());
		adjustControlEdges();
		context.removeScope();
		deleteOperators();
		if (configuration.collapseTemporaryDataNodes)
			deleteTemporaryDataNodes();
		else if (configuration.collapseTemporaryDataNodesIncomingToControlNodes)
			deleteTemporaryDataNodesIncomingToControlNodes();
		deleteEmptyStatementNodes();
		if (configuration.collapseIsomorphicSubgraphs)
			collapseIsomorphicSubgraphs();
		buildClosure();
		if (configuration.removeImplementationCode > 0)
			removeThisMembers();
		deleteReferences();
		deleteAssignmentNodes();
		if (configuration.removeTransitiveDefinitionEdgesFromMethodCalls)
			deleteTransitiveDefinitionEdgesFromMethodCalls();
		deleteUnreachableNodes();
		deleteControlNodes();
		deleteUnusedDataNodes();
		if (configuration.removeIndependentControlEdges)
			deleteIndependentControlEdges();
		if (configuration.groum) {
			deleteDataNodes();
			deleteNonCoreActionNodes();
			renameEdges();
		}
		cleanUp();
	}

	private void deleteTransitiveDefinitionEdgesFromMethodCalls() {
		for (EGroumNode node : nodes) {
			if (node instanceof EGroumActionNode && node.astNodeType != ASTNode.INFIX_EXPRESSION && node.astNodeType != ASTNode.PREFIX_EXPRESSION && node.astNodeType != ASTNode.POSTFIX_EXPRESSION) {
				for (EGroumEdge e : new HashSet<EGroumEdge>(node.outEdges)) {
					if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == DEFINITION) {
						HashSet<EGroumNode> defs = new HashSet<>();
						for (EGroumEdge e1 : e.target.inEdges)
							if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == DEFINITION)
								defs.add(e1.source);
						HashSet<EGroumNode> outs = new HashSet<>();
						for (EGroumEdge e1 : node.outEdges)
							if (e1 instanceof EGroumDataEdge)
								outs.add(e1.target);
						defs.retainAll(outs);
						boolean isRemoved = false;
						for (EGroumNode def : defs)
							if (def.astNodeType != ASTNode.INFIX_EXPRESSION && def.astNodeType != ASTNode.PREFIX_EXPRESSION && def.astNodeType != ASTNode.POSTFIX_EXPRESSION) {
								isRemoved = true;
								break;
							}
						if (isRemoved)
							e.delete();
					}
				}
			}
		}
	}

	private void deleteIndependentControlEdges() {
		for (EGroumNode node : nodes) {
			if (!(node instanceof EGroumActionNode))
				continue;
			if (node.getAstNodeType() == ASTNode.BREAK_STATEMENT
					|| node.getAstNodeType() == ASTNode.CONTINUE_STATEMENT
					|| node.getAstNodeType() == ASTNode.RETURN_STATEMENT
					|| node.getAstNodeType() == ASTNode.THROW_STATEMENT
					|| node.getAstNodeType() == ASTNode.CATCH_CLAUSE)
				continue;
			for (EGroumEdge e : new HashSet<EGroumEdge>(node.inEdges)) {
				if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == CONDITION) {
					if (e.isExceptionHandling())
						continue;
					if (e.source instanceof EGroumDataNode && ((EGroumDataNode) e.source).isException())
						continue;
					HashSet<EGroumNode> closure = e.source.buildTransitiveParameterClosure(), 
							inter = new HashSet<>(closure),
							closure2 = node.buildTransitiveParameterClosure();
					inter.retainAll(closure2);
					if (inter.isEmpty()) {
						HashSet<String> names = new HashSet<>();
						for (EGroumNode n : closure)
							names.add(n.key);
						names.remove(null);
						for (EGroumEdge out : node.outEdges) {
							if (out instanceof EGroumDataEdge && !out.isTransitive && ((EGroumDataEdge) out).type == DEFINITION) {
								if (names.contains(out.target.key)) {
									inter.add(out.target);
									break;
								}
							}
						}
						if (inter.isEmpty()) {
							for (EGroumNode n : closure2) {
								if (names.contains(n.key)) {
									inter.add(n);
									break;
								}
							}
							if (inter.isEmpty()) {
								e.delete();
							}
						}
					}
				}
			}
		}
	}

	private void addDefinitions(EGroumGraph pdg) {
		LinkedList<EGroumDataNode> sources = new LinkedList<>(pdg.dataSources);
		while (!sources.isEmpty()) {
			EGroumDataNode source = sources.pop();
			if (!entryNode.defStore.containsKey(source.key)) {
				EGroumDataNode def = new EGroumDataNode(source);
				def.isDeclaration = true;
				nodes.add(def);
				HashSet<EGroumDataNode> defs = new HashSet<>();
				defs.add(def);
				entryNode.defStore.put(source.key, defs);
				EGroumNode qual = source.getQualifier();
				while (qual != null && qual instanceof EGroumDataNode) {
					EGroumDataNode q = new EGroumDataNode((EGroumDataNode) qual);
					new EGroumDataEdge(q, def, QUALIFIER);
					sources.push(q);
					pdg.dataSources.add(q);
					pdg.nodes.add(q);
					def = q;
					qual = qual.getQualifier();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void markDirectEdges() {
		for (EGroumNode node : nodes) {
			for (EGroumEdge e : node.inEdges)
				e.isTransitive = false;
			for (EGroumEdge e : node.outEdges)
				e.isTransitive = false;
		}
	}

	private int count = 0;
	private boolean isTooSmall(MethodDeclaration md) {
		md.accept(new ASTVisitor(false) {
			@Override
			public boolean preVisit2(ASTNode node) {
				if (node instanceof Statement) {
					count++;
				}
				return true;
			}
		});
		return count < configuration.minStatements;
	}

	private void collapseIsomorphicSubgraphs() {
		
	}

	public EGroumGraph(EGroumBuildingContext context, AUGConfiguration configuration) {
		this.context = context;
        this.configuration = configuration;
    }

	public EGroumGraph(EGroumBuildingContext context, EGroumNode node, AUGConfiguration configuration) {
		this(context, configuration);
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
	
	public EGroumGraph(EGroumGraph g) {
        this.configuration = g.configuration;
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
        this.configuration = g.configuration;
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

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
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

	public Set<String> getAPIs() {
		return getNodes().stream().map(EGroumNode::getAPI)
				.filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toSet());
	}

	public Set<String> getNodeLabels() {
		return getNodes().stream()
				.filter(EGroumNode::isMeaningfulAction)
				.map(EGroumNode::getLabel)
				.collect(Collectors.toSet());
//		Set<String> labels = new HashSet<>();
//		for (EGroumNode node : nodes)
//			if (node.isMeaningfulAction())
//				labels.add(node.getLabel());
//		return labels;
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
		if (node instanceof LambdaExpression)
			return buildPDG(control, branch, (LambdaExpression) node);
		if (node instanceof MethodDeclaration)
			return buildPDG(control, branch, (MethodDeclaration) node);
		if (node instanceof MethodInvocation)
			return buildPDG(control, branch, (MethodInvocation) node);
		if (node instanceof MethodReference)
			return buildPDG(control, branch, (MethodReference) node);
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
		return new EGroumGraph(context, configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, WhileStatement astNode) {
		context.addScope();
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, CONDITION);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(node, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
		EGroumGraph bg = buildPDG(node, "T", astNode.getBody());
		if (!bg.isEmpty())
			ebg.mergeSequential(bg);
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
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

	private EGroumGraph buildPDG(EGroumNode control, String branch, VariableDeclarationFragment astNode) {
		SimpleName name = astNode.getName();
		String type = JavaASTUtil.getSimpleType(astNode);
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode node = new EGroumDataNode(name, name.getNodeType(), "" + name.getStartPosition(), type, name.getIdentifier(), false, true);
		if (astNode.getInitializer() == null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.NULL_LITERAL, "null", "", "null"), configuration);
			pdg.mergeSequentialData(new EGroumActionNode(control, branch, astNode, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(node, DEFINITION);
			return pdg;
		}
		EGroumGraph pdg = buildDefinitionPDG(control, branch, astNode.getInitializer(), node);
		pdg.mergeSequentialData(new EGroumDataNode(node), REFERENCE);
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
		return new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), "class", "Class", JavaASTUtil.getSimpleType(astNode.getType()) + ".class"), configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			TryStatement astNode) {
		context.pushTry();
		context.addScope();
		List<?> resources = astNode.resources();
		HashSet<String> resourceNames = new HashSet<>();
		EGroumGraph pdg = null;
		if (resources != null && !resources.isEmpty()) {
			pdg = buildPDG(control, branch, resources);
			for (int i = 0; i < resources.size(); i++) {
				VariableDeclarationExpression res = (VariableDeclarationExpression) resources.get(i);
				List<?> fragments = res.fragments();
				for (int j = 0; j < fragments.size(); j++) {
					VariableDeclarationFragment f = (VariableDeclarationFragment) fragments.get(j);
					SimpleName name = f.getName();
					resourceNames.add(name.getIdentifier());
				}
			}
		} else {
			if (astNode.getBody().statements().isEmpty())
				return new EGroumGraph(context, configuration);
			pdg = new EGroumGraph(context, configuration);
		}
		EGroumGraph bg = buildPDG(control, branch, astNode.getBody());
		ArrayList<EGroumActionNode> triedMethods = context.popTry();
		if (!resourceNames.isEmpty()) {
			HashMap<String, EGroumActionNode> closeNodes = new HashMap<>();
			EGroumGraph[] rgs = new EGroumGraph[resourceNames.size()];
			int i = 0;
			for (String rn : resourceNames) {
				EGroumGraph g = buildPDG(control, branch, astNode.getAST().newSimpleName(rn));
				EGroumActionNode close = new EGroumActionNode(control, branch, null, ASTNode.METHOD_INVOCATION, null, "AutoCloseable.close()", "close");
				g.mergeSequentialData(close, RECEIVER);
				closeNodes.put(rn, close);
				rgs[i++] = g;
			}
			EGroumGraph cg = new EGroumGraph(context, configuration);
			cg.mergeParallel(rgs);
			bg.mergeSequential(cg);
			for (EGroumActionNode m : triedMethods) {
				ASTNode mn = m.getAstNode();
				String[] rns = getResourceName(mn, resourceNames);
				if (rns != null) {
					String rn = rns[0];
					EGroumActionNode close = closeNodes.get(rn);
					if (triedMethods.size() == 1 || (m.exceptionTypes != null && !m.exceptionTypes.isEmpty()))
						new EGroumDataEdge(m, close, FINALLY);
					if (rns.length == 2 && !close.hasInDataNode(m, ORDER))
						new EGroumDataEdge(m, close, ORDER);
				}
			}
		}
		pdg.mergeSequential(bg);
		EGroumGraph[] gs = new EGroumGraph[astNode.catchClauses().size()];
		for (int i = 0; i < astNode.catchClauses().size(); i++) {
			CatchClause cc = (CatchClause) astNode.catchClauses().get(i);
			gs[i] = buildPDG(control, branch, cc, triedMethods);
		}
		HashSet<EGroumNode> sinks = new HashSet<>(pdg.statementSinks);
		pdg.mergeBranches(gs);
		for (EGroumNode sink : sinks)
			if (sink.outEdges.isEmpty())
				pdg.statementSinks.add(sink);
		if (astNode.getFinally() != null) {
			// TODO append finally block to all possible throw points
			EGroumControlNode fn = new EGroumControlNode(control, branch, astNode.getFinally(), astNode.getFinally().getNodeType());
			EGroumGraph fg = new EGroumGraph(context, fn, configuration);
            EGroumGraph pdg1 = buildPDG(fn, "", astNode.getFinally());
            pdg1.statementSources.clear();
            fg.mergeSequential(pdg1);
			pdg.mergeSequential(fg);
			for (EGroumActionNode m : triedMethods) {
				if (triedMethods.size() == 1 || (m.exceptionTypes != null && !m.exceptionTypes.isEmpty()))
					new EGroumDataEdge(m, fn, FINALLY);
			}
		}
		context.removeScope();
		return pdg;
	}

	public String[] getResourceName(ASTNode mn, HashSet<String> resourceNames) {
		if (mn instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) mn;
			if (mi.getExpression() != null && mi.getExpression() instanceof SimpleName && resourceNames.contains(mi.getExpression().toString()))
				return new String[]{mi.getExpression().toString(), EGroumDataEdge.getLabel(ORDER)};
		}
		if (mn.getParent() instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment p = (VariableDeclarationFragment) mn.getParent();
			return resourceNames.contains(p.getName().getIdentifier()) ? new String[]{p.getName().getIdentifier()} : null;
		}
		return null;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ThrowStatement astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, "throw");
		pdg.mergeSequentialData(node, PARAMETER);
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
		pdg.mergeSequentialData(node, CONDITION);
		if (!astNode.getBody().statements().isEmpty()) {
			pdg.mergeSequentialControl(new EGroumActionNode(node, "",
					null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
			pdg.mergeSequential(buildPDG(node, "", astNode.getBody()));
			return pdg;
		}
		return new EGroumGraph(context, configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, SwitchStatement astNode) {
		List<?> statements = astNode.statements();
		if (statements.isEmpty() || !(statements.get(0) instanceof SwitchCase))
			return new EGroumGraph(context, configuration);
		context.addScope();
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch, astNode, ASTNode.IF_STATEMENT);
		pdg.mergeSequentialData(node, CONDITION);
		ArrayList<EGroumGraph> cgs = new ArrayList<>();
		int i = 1, s = 0;
		while (i < statements.size()) {
			if (statements.get(i) instanceof SwitchCase || i == statements.size()-1) {
				if (!(statements.get(i) instanceof SwitchCase))
					i++;
				int end = s + 1;
				while (end < i && !(statements.get(end) instanceof BreakStatement))
					end++;
				SwitchCase sc = (SwitchCase) statements.get(s);
				List<?> subs = statements.subList(s+1, end);
				EGroumGraph cg = buildPDG(node, sc.isDefault() ? "default" : sc.getExpression().toString(), subs);
				cgs.add(cg);
				s = i;
			}
			i++;
		}
		pdg.mergeBranches(cgs.toArray(new EGroumGraph[]{}));
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, SuperMethodInvocation astNode) {
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
				type, "super"), configuration);
		for (int i = 0; i < astNode.arguments().size(); i++)
			pgs[i+1] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + "." + astNode.getName().getIdentifier() + "()", 
				astNode.getName().getIdentifier(), exceptions);
		context.addMethodTry(node);
		EGroumGraph pdg = null;
		pgs[0].mergeSequentialData(node, RECEIVER);
		if (pgs.length > 0) {
			for (int i = 1; i < pgs.length; i++)
				pgs[i].mergeSequentialData(node, PARAMETER);
			pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node, configuration);
		// skip astNode.getQualifier()
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, SuperConstructorInvocation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size()];
		for (int i = 0; i < astNode.arguments().size(); i++) {
			pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
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
				pg.mergeSequentialData(node, PARAMETER);
			pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node, configuration);
		// skip astNode.getExpression()
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			StringLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.getEscapedValue(), "String", null,
				astNode.getLiteralValue()), configuration);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SingleVariableDeclaration astNode) {
		SimpleName name = astNode.getName();
		String type = JavaASTUtil.getSimpleType(astNode.getType());
		for (int i = 0; i < astNode.getExtraDimensions(); i++)
			type += "[]";
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode node = new EGroumDataNode(name, name.getNodeType(),
				"" + name.getStartPosition(), type,
				name.getIdentifier(), false, true);
		EGroumGraph pdg = new EGroumGraph(context, configuration);
		pdg.mergeSequentialData(node, DEFINITION);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, SimpleName astNode) {
		String constantName = null, constantValue = null, type = null;
		int astNodeType = ASTNode.SIMPLE_NAME;
		IBinding b = astNode.resolveBinding();
		if (b != null && b instanceof IVariableBinding) {
			IVariableBinding vb = (IVariableBinding) b;
			vb = vb.getVariableDeclaration();
			if (vb.getType().getTypeDeclaration() != null)
				type = vb.getType().getTypeDeclaration().getName();
			else
				type = vb.getType().getName();
			if (JavaASTUtil.isConstant(vb)) {
				if (vb.getDeclaringClass().getTypeDeclaration() != null)
					constantName = vb.getDeclaringClass().getTypeDeclaration().getName() + "." + vb.getName();
				else
					constantName = vb.getName();
				if (vb.getConstantValue() != null) {
					ITypeBinding tb = vb.getType();
					if (tb.isPrimitive()) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = getPrimitiveConstantType(tb);
					} else if (tb.getName().equals("String")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.STRING_LITERAL;
					} else if (tb.getName().equals("Boolean")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.BOOLEAN_LITERAL;
					} else if (tb.getName().equals("Character")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.CHARACTER_LITERAL;
					} else if (tb.getSuperclass() != null && tb.getSuperclass().getName().equals("Number")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.NUMBER_LITERAL;
					}
				}
			}
		}
		if (constantName != null) {
			EGroumDataNode node = new EGroumDataNode(astNode, astNodeType, constantName, type, constantName, constantValue, true, false, configuration.encodeConstants);
			return new EGroumGraph(context, node, configuration);
		}
		String name = astNode.getIdentifier();
		if (astNode.resolveTypeBinding() != null) {
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		}
		String[] info = context.getLocalVariableInfo(name);
		if (info != null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					astNode, ASTNode.SIMPLE_NAME, info[0], type == null ? info[1] : type,
					astNode.getIdentifier(), false, false), configuration);
			return pdg;
		}
		if (type == null)
			type = context.getFieldType(astNode);
		if (type != null) {
			if (configuration.keepQualifierEdges) {
				EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
						null, ASTNode.THIS_EXPRESSION, "this",
						context.getType(), "this"), configuration);
				pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
						"this." + name, type, name, true, false), QUALIFIER);
				return pdg;
			} else {
				EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
						"this." + name, type, name, true, false), configuration);
				return pdg;
			}
		}
		if (name.equals(name.toUpperCase()))
			return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, name, name, name, null, true, false, configuration.encodeConstants), configuration);
		if (configuration.keepQualifierEdges && astNodeType == -1) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this",
					context.getType(), "this"), configuration);
			pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
					"this." + name, "UNKNOWN", name, true, false),QUALIFIER);
			return pdg;
		} else {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS,
					"this." + name, "UNKNOWN", name, true, false), configuration);
			return pdg;
		}
	}

	private int getPrimitiveConstantType(ITypeBinding tb) {
		String type = tb.getName();
		if (type.equals("boolean"))
			return ASTNode.BOOLEAN_LITERAL;
		if (type.equals("char"))
			return ASTNode.CHARACTER_LITERAL;
		if (type.equals("void"))
			return -1;
		return ASTNode.NUMBER_LITERAL;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ReturnStatement astNode) {
		EGroumGraph pdg = null;
		EGroumActionNode node = null;
		if (astNode.getExpression() != null) {
			pdg = buildArgumentPDG(control, branch, astNode.getExpression());
			node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
					null, null, "return");
			pdg.mergeSequentialData(node, PARAMETER);
		} else {
			node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
					null, null, "return");
			pdg = new EGroumGraph(context, node, configuration);
		}
		pdg.returns.add(node);
		pdg.sinks.clear();
		pdg.statementSinks.clear();
		pdg.clearDefStore();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, QualifiedName astNode) {
		String constantName = null, constantValue = null, type = null;
		int astNodeType = ASTNode.FIELD_ACCESS;
		IBinding b = astNode.resolveBinding();
		if (b != null && b instanceof IVariableBinding) {
			IVariableBinding vb = (IVariableBinding) b;
			vb = vb.getVariableDeclaration();
			if (vb.getType().getTypeDeclaration() != null)
				type = vb.getType().getTypeDeclaration().getName();
			else
				type = vb.getType().getName();
			if (JavaASTUtil.isConstant(vb)) {
				if (vb.getDeclaringClass().getTypeDeclaration() != null)
					constantName = vb.getDeclaringClass().getTypeDeclaration().getName() + "." + vb.getName();
				else
					constantName = vb.getName();
				if (vb.getConstantValue() != null) {
					ITypeBinding tb = vb.getType();
					if (tb.isPrimitive()) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = getPrimitiveConstantType(tb);
					} else if (tb.getName().equals("String")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.STRING_LITERAL;
					} else if (tb.getName().equals("Boolean")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.BOOLEAN_LITERAL;
					} else if (tb.getName().equals("Character")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.CHARACTER_LITERAL;
					} else if (tb.getSuperclass() != null && tb.getSuperclass().getName().equals("Number")) {
						constantValue = vb.getConstantValue().toString();
						astNodeType = ASTNode.NUMBER_LITERAL;
					}
				}
			}
		}
		if (constantName != null) {
			EGroumDataNode node = new EGroumDataNode(astNode, astNodeType, constantName, type, constantName, constantValue, true, false, configuration.encodeConstants);
			return new EGroumGraph(context, node, configuration);
		} else {
			if (astNode.resolveTypeBinding() != null)
				type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
			String name = astNode.getName().getIdentifier();
			if (name.equals(name.toUpperCase()))
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.getFullyQualifiedName(), type == null ? astNode.getFullyQualifiedName() : type, astNode.getFullyQualifiedName(), null, true, false, configuration.encodeConstants), configuration);
			if (type == null)
				type = "UNKNOWN";
			EGroumDataNode node = new EGroumDataNode(astNode, astNodeType, astNode.getFullyQualifiedName(), type, astNode.getFullyQualifiedName(), true, false);
			return new EGroumGraph(context, node, configuration);
		}
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, PrefixExpression astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getOperand());
		EGroumDataNode node = pdg.getOnlyDataOut();
		if (astNode.getOperator() == PrefixExpression.Operator.PLUS)
			return pdg;
		if (astNode.getOperator() == PrefixExpression.Operator.INCREMENT
				|| astNode.getOperator() == PrefixExpression.Operator.DECREMENT) {
			EGroumActionNode op = new EGroumActionNode(control, branch,
					astNode, astNode.getNodeType(), null, "<a>", 
					astNode.getOperator().toString().substring(0, 1));
			pdg.mergeSequentialData(op, PARAMETER);
			EGroumDataNode lit = new EGroumDataNode(null, ASTNode.NUMBER_LITERAL, "1", "int", "1");
			new EGroumDataEdge(lit, op, PARAMETER);
			pdg.nodes.add(lit);
		} else {
			if (!configuration.encodeUnaryOperators)
				return pdg;
			pdg.mergeSequentialData(
					new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
							null, astNode.getOperator().toString(), astNode.getOperator().toString()),
					PARAMETER);
		}
		if (astNode.getOperator() == PrefixExpression.Operator.INCREMENT
				|| astNode.getOperator() == PrefixExpression.Operator.DECREMENT) {
			pdg.mergeSequentialData(new EGroumActionNode(control, branch, astNode, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(new EGroumDataNode(node.astNode, node.astNodeType, node.key, node.dataType, node.dataName), DEFINITION);
		}
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, PostfixExpression astNode) {
		EGroumGraph lg = buildArgumentPDG(control, branch, astNode.getOperand());
		EGroumDataNode node = lg.getOnlyDataOut();
		EGroumGraph rg = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.NUMBER_LITERAL, "1", "int", "1"), configuration);
		EGroumActionNode op = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, "<a>", astNode.getOperator().toString().substring(0, 1));
		lg.mergeSequentialData(op, PARAMETER);
		rg.mergeSequentialData(op, PARAMETER);
		EGroumGraph pdg = new EGroumGraph(context, configuration);
		pdg.mergeParallel(lg, rg);
		pdg.mergeSequentialData(new EGroumActionNode(control, branch, astNode, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
		pdg.mergeSequentialData(new EGroumDataNode(node.astNode, node.astNodeType, node.key, node.dataType, node.dataName), DEFINITION);
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
				astNode, astNode.getNodeType(), astNode.getToken(), type, null,
				astNode.getToken()), configuration);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			NullLiteral astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.toString(), "null", null,
				astNode.toString()), configuration);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, MethodInvocation astNode) {
		if (astNode.getName().getIdentifier().equals("exit")
				&& astNode.getExpression() != null && astNode.getExpression().toString().equals("System")) {
			EGroumActionNode node = new EGroumActionNode(control, branch,
					astNode, astNode.getNodeType(), null, "Sytem.exit()", astNode.getName().getIdentifier());
			EGroumGraph pdg = new EGroumGraph(context, node, configuration);
			pdg.returns.add(node);
			pdg.sinks.remove(node);
			pdg.statementSinks.remove(node);
			pdg.clearDefStore();
			return pdg;
		}
		boolean isStatic = false;
		String type = null;
		if (astNode.resolveMethodBinding() != null) {
			IMethodBinding mb = astNode.resolveMethodBinding().getMethodDeclaration();
			isStatic = Modifier.isStatic(mb.getModifiers());
		} else if (astNode.getExpression() != null && astNode.getExpression() instanceof SimpleName) {
			String r = ((SimpleName) astNode.getExpression()).getIdentifier();
			if (r.length() > 1 && Character.isUpperCase(r.charAt(0)) && !r.equals(r.toUpperCase())) {
				isStatic = true;
				type = r;
			}
		}
		EGroumGraph[] pgs;
		if (isStatic) {
			pgs = new EGroumGraph[astNode.arguments().size()];
			for (int i = 0; i < astNode.arguments().size(); i++)
				pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
		} else {
			pgs = new EGroumGraph[astNode.arguments().size() + 1];
			if (astNode.getExpression() != null)
				pgs[0] = buildArgumentPDG(control, branch, astNode.getExpression());
			else
				pgs[0] = new EGroumGraph(context, new EGroumDataNode(
						null, ASTNode.THIS_EXPRESSION, "this",
						context.getType(), "this"), configuration);
			for (int i = 0; i < astNode.arguments().size(); i++)
				pgs[i + 1] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
			type = pgs[0].getOnlyOut().dataType;
		}
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
		if (isStatic) {
			for (int i = 0; i < pgs.length; i++)
				pgs[i].mergeSequentialData(node, PARAMETER);
		} else {
			pgs[0].mergeSequentialData(node, RECEIVER);
			for (int i = 1; i < pgs.length; i++)
				pgs[i].mergeSequentialData(node, PARAMETER);
		}
		if (pgs.length > 0) {
			pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node, configuration);
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
		// skip
		return new EGroumGraph(context, configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, LabeledStatement astNode) {
		adjustBreakNodes(astNode.getLabel().getIdentifier());
		return buildPDG(control, branch, astNode.getBody());
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, LambdaExpression astNode) {
		// TODO
		return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.NULL_LITERAL, "null", "null", "LAMBDA"), configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, MethodReference astNode) {
		// TODO
		return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.NULL_LITERAL, "null", "null", "LAMBDA"), configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, InstanceofExpression astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getLeftOperand());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, JavaASTUtil.getSimpleType(astNode.getRightOperand()) + ".<instanceof>", 
				JavaASTUtil.getSimpleType(astNode.getRightOperand()) + ".<instanceof>");
		pdg.mergeSequentialData(node, PARAMETER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			Initializer astNode) {
		return buildPDG(control, branch, astNode.getBody());
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, InfixExpression astNode) {
		EGroumGraph pdg = null;
		if (astNode.getLeftOperand() instanceof NullLiteral)
			pdg = buildArgumentPDG(control, branch, astNode.getRightOperand());
		else if (astNode.getRightOperand() instanceof NullLiteral)
			pdg = buildArgumentPDG(control, branch, astNode.getLeftOperand());
		if (pdg != null) {
			EGroumActionNode node = new EGroumActionNode(control, branch, astNode, ASTNode.METHOD_INVOCATION, null, "<nullcheck>", "<nullcheck>");
			pdg.mergeSequentialData(node, PARAMETER);
			return pdg;
		}
		pdg = new EGroumGraph(context, configuration);
		EGroumGraph lg = buildArgumentPDG(control, branch, astNode.getLeftOperand());
		EGroumGraph rg = buildArgumentPDG(control, branch, astNode.getRightOperand());
		connectDependence(lg, rg);
		InfixExpression.Operator op = astNode.getOperator();
		if (op == Operator.CONDITIONAL_AND || op == Operator.CONDITIONAL_OR)
			connectControl(lg, rg, "sel");
		String label = JavaASTUtil.buildLabel(op);
		EGroumActionNode node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(), null, label, label);
		lg.mergeSequentialData(node, PARAMETER);
		rg.mergeSequentialData(node, PARAMETER);
		if (astNode.hasExtendedOperands()) {
			EGroumGraph[] egs = new EGroumGraph[2 + astNode.extendedOperands().size()];
			egs[0] = lg;
			egs[1] = rg;
			for (int i = 0; i < astNode.extendedOperands().size(); i++) {
				EGroumGraph tmp = buildArgumentPDG(control, branch, (Expression) astNode.extendedOperands().get(i));
				tmp.mergeSequentialData(node, PARAMETER);
				egs[2+i] = tmp;
				connectDependence(egs[1+i], egs[2+i]);
				if (op == Operator.CONDITIONAL_AND || op == Operator.CONDITIONAL_OR)
					connectControl(egs[1+i], egs[2+i], "sel");
			}
			pdg.mergeParallel(egs);
		}
		else
			pdg.mergeParallel(lg, rg);
		return pdg;
	}

	private void connectDependence(EGroumGraph lg, EGroumGraph rg) {
		for (EGroumNode sink : lg.statementSinks) {
			for (EGroumNode source : rg.statementSources) {
				new EGroumDataEdge(sink, source, DEPENDENCE);
			}
		}
	}

	private void connectControl(EGroumGraph lg, EGroumGraph rg, String label) {
		if (lg.statementNodes.isEmpty()) {
			for (EGroumNode sink : lg.sinks) {
				for (EGroumNode source : rg.statementNodes) {
					new EGroumDataEdge(sink, source, CONDITION, label);
				}
			}
		} else {
			for (EGroumNode sink : lg.statementNodes) {
				for (EGroumNode source : rg.statementNodes) {
					new EGroumDataEdge(sink, source, CONDITION, label);
				}
			}
		}
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, IfStatement astNode) {
		context.addScope();
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, CONDITION);
		EGroumGraph etg = new EGroumGraph(context, new EGroumActionNode(node, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
		EGroumGraph tg = buildPDG(node, "T", astNode.getThenStatement());
		if (!tg.isEmpty())
			etg.mergeSequential(tg);
		EGroumGraph efg = new EGroumGraph(context, new EGroumActionNode(node, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
		if (astNode.getElseStatement() != null) {
			EGroumGraph fg = buildPDG(node, "F", astNode.getElseStatement());
			if (!fg.isEmpty())
				efg.mergeSequential(fg);
		}
		pdg.mergeBranches(etg, efg);
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ForStatement astNode) {
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
			middleG.mergeSequentialData(node, CONDITION);
		else
			middleG = new EGroumGraph(context, node, configuration);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(node, "T", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
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
		
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
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
		boolean isArray = false;
		if (astNode.getExpression().resolveTypeBinding() != null) {
			ITypeBinding tb = astNode.getExpression().resolveTypeBinding();
			isArray = tb.isArray();
		} else {
			for (EGroumNode sink : pdg.sinks) {
				if (sink.dataType != null && sink.dataType.endsWith("[]")) {
					isArray = true;
					break;
				}
			}
		}
		if (isArray) {
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(var, DEFINITION);
			pdg.mergeSequentialData(new EGroumDataNode(null, var.astNodeType, var.key, var.dataType, var.dataName), REFERENCE);
			EGroumControlNode node = new EGroumControlNode(control, branch, astNode, astNode.getNodeType());
			pdg.mergeSequentialData(node, CONDITION);
			pdg.mergeSequentialControl(new EGroumActionNode(node, "", null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), "");
			EGroumGraph bg = buildPDG(node, "", astNode.getBody());
			if (!bg.isEmpty())
				pdg.mergeSequential(bg);
		} else {
			EGroumActionNode iteratorCall = new EGroumActionNode(control, branch, null, ASTNode.METHOD_INVOCATION, null, "Iterable.iterator()", "iterator");
			pdg.mergeSequentialData(iteratorCall, RECEIVER);
			pdg.mergeSequentialData(new EGroumActionNode(control, branch, null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(new EGroumDataNode(null, ASTNode.SIMPLE_NAME, EGroumNode.PREFIX_DUMMY + astNode.getExpression().getStartPosition() + "_" + astNode.getExpression().getLength(), "Iterator", EGroumNode.PREFIX_DUMMY, false, true), DEFINITION);
			EGroumDataNode iterator = new EGroumDataNode(null, ASTNode.SIMPLE_NAME, EGroumNode.PREFIX_DUMMY + astNode.getExpression().getStartPosition() + "_" + astNode.getExpression().getLength(), "Iterator", EGroumNode.PREFIX_DUMMY, false, false);
			pdg.mergeSequentialData(iterator, REFERENCE);
			pdg.mergeSequentialData(new EGroumActionNode(control, branch, null, ASTNode.METHOD_INVOCATION, null, "Iterator.hasNext()", "hasNext"), RECEIVER);
			pdg.mergeSequentialData(new EGroumActionNode(control, branch, null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(new EGroumDataNode(null, ASTNode.SIMPLE_NAME, EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_" + astNode.getLength(), "boolean", EGroumNode.PREFIX_DUMMY, false, true), DEFINITION);
			pdg.mergeSequentialData(new EGroumDataNode(null, ASTNode.SIMPLE_NAME, EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_" + astNode.getLength(), "boolean", EGroumNode.PREFIX_DUMMY, false, false), REFERENCE);
			EGroumControlNode node = new EGroumControlNode(control, branch, astNode, ASTNode.WHILE_STATEMENT);
			pdg.mergeSequentialData(node, CONDITION);
			EGroumGraph bg = new EGroumGraph(context, new EGroumDataNode(iterator), configuration);
			bg.mergeSequentialData(new EGroumActionNode(node, "T", null, ASTNode.METHOD_INVOCATION, null, "Iterator.next()", "next"), RECEIVER);
			bg.mergeSequentialData(new EGroumActionNode(node, "T", null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			bg.mergeSequentialData(var, DEFINITION);
			bg.mergeSequential(buildPDG(node, "T", astNode.getBody()));
			EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F",
					null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
			pdg.mergeBranches(bg, eg);
		}
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, DoStatement astNode) {
		context.addScope();
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		pdg.mergeSequentialData(node, CONDITION);
		EGroumGraph ebg = new EGroumGraph(context, new EGroumActionNode(node, "T",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
		EGroumGraph bg = buildPDG(node, "T", astNode.getBody());
		if (!bg.isEmpty())
			ebg.mergeSequential(bg);
		EGroumGraph eg = new EGroumGraph(context, new EGroumActionNode(node, "F",
				null, ASTNode.EMPTY_STATEMENT, null, null, "empty"), configuration);
		pdg.mergeBranches(ebg, eg);
		pdg.adjustBreakNodes("");
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ContinueStatement astNode) {
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), astNode.getLabel() == null ? "" : astNode.getLabel().getIdentifier(), null,
				"continue");
		EGroumGraph pdg = new EGroumGraph(context, node, configuration);
		pdg.breaks.add(node);
		pdg.sinks.remove(node);
		pdg.statementSinks.remove(node);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ConstructorInvocation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size()];
		int numOfParameters = 0;
		for (int i = 0; i < astNode.arguments().size(); i++) {
			pgs[numOfParameters] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
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
				pg.mergeSequentialData(node, PARAMETER);
			pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node, configuration);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ConditionalExpression astNode) {
		context.addScope();
		String type = "UNKNOWN";
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		EGroumControlNode node = new EGroumControlNode(control, branch, astNode, ASTNode.IF_STATEMENT);
		pdg.mergeSequentialData(node, CONDITION);
		EGroumGraph tg = buildArgumentPDG(node, "T", astNode.getThenExpression());
		tg.mergeSequentialData(new EGroumActionNode(node, "T", null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
		tg.mergeSequentialData(new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
						+ astNode.getLength(), type, EGroumNode.PREFIX_DUMMY, false, true), DEFINITION);
		EGroumGraph fg = buildArgumentPDG(node, "F", astNode.getElseExpression());
		fg.mergeSequentialData(new EGroumActionNode(node, "F", null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
		fg.mergeSequentialData(new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
						+ astNode.getLength(), type, EGroumNode.PREFIX_DUMMY, false, true), DEFINITION);
		pdg.mergeBranches(tg, fg);
		pdg.mergeSequentialData(new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
						+ astNode.getLength(), type, EGroumNode.PREFIX_DUMMY, false, false), REFERENCE);
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ClassInstanceCreation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size()];
		int numOfParameters = 0;
		for (int i = 0; i < astNode.arguments().size(); i++) {
			pgs[numOfParameters] = buildArgumentPDG(control, branch, (Expression) astNode.arguments().get(i));
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
				pg.mergeSequentialData(node, PARAMETER);
			pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
		} else
			pdg = new EGroumGraph(context, node, configuration);
		// skip astNode.getExpression()
		AnonymousClassDeclaration acd = astNode.getAnonymousClassDeclaration();
		if (acd != null) {
			EGroumGraph acg = new EGroumGraph(context, configuration);
			EGroumDataNode acn = new EGroumDataNode(acd, ASTNode.TYPE_LITERAL, "" + acd.getStartPosition(), type, type, false, true);
			new EGroumDataEdge(acn, node, RECEIVER);
			ArrayList<EGroumGraph> mgs = new ArrayList<>();
			for (int i = 0; i < acd.bodyDeclarations().size(); i++) {
				if (acd.bodyDeclarations().get(i) instanceof MethodDeclaration) {
					MethodDeclaration md = (MethodDeclaration) acd.bodyDeclarations().get(i);
					if (md.getBody() != null && !md.getBody().statements().isEmpty()) {
						EGroumDataNode mdn = new EGroumDataNode(md, md.getNodeType(), "" + md.getStartPosition(), type + "." + md.getName().getIdentifier() + "()", type + "." + md.getName().getIdentifier() + "()", false, true);
						new EGroumDataEdge(acn, mdn, EGroumDataEdge.Type.CONTAINS);
						EGroumEntryNode dummy = new EGroumEntryNode(null, ASTNode.METHOD_DECLARATION, mdn.dataType);
						EGroumGraph mg = buildPDG(dummy, "", md.getBody());
						mg.nodes.add(dummy);
						for (EGroumNode mgn : mg.nodes) {
							if (mgn instanceof EGroumActionNode)
								new EGroumDataEdge(mdn, mgn, EGroumDataEdge.Type.CONTAINS);
						}
						mg.nodes.add(mdn);
						mgs.add(mg);
					}
				}
			}
			if (!mgs.isEmpty())
				acg.mergeParallel(mgs.toArray(new EGroumGraph[0]));
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
		return new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.getEscapedValue(), "char", null,
				astNode.getEscapedValue()), configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			CatchClause astNode, ArrayList<EGroumActionNode> triedMethods) {
		context.addScope();
		SimpleName name = astNode.getException().getName();
		String type = JavaASTUtil.getSimpleType(astNode.getException().getType());
		context.addLocalVariable(name.getIdentifier(), "" + name.getStartPosition(), type);
		EGroumDataNode en = new EGroumDataNode(name, name.getNodeType(),
				"" + name.getStartPosition(), type, name.getIdentifier(), false, true);
		EGroumGraph pdg = new EGroumGraph(context, configuration);
		pdg.mergeSequentialData(en, DEFINITION);
		pdg.mergeSequentialData(new EGroumDataNode(null, en.astNodeType, en.key, en.dataType, en.dataName), REFERENCE);
		EGroumActionNode cn = new EGroumActionNode(control, branch, null, ASTNode.METHOD_INVOCATION, null, type + ".<catch>", type + ".<catch>");
		pdg.mergeSequentialData(cn, PARAMETER);
		EGroumControlNode node = new EGroumControlNode(control, branch,
				astNode, astNode.getNodeType());
		pdg.mergeSequentialData(node, CONDITION);
		EGroumGraph bg = buildPDG(node, "", astNode.getBody());
		if (!bg.isEmpty()) {
		    bg.statementSources.clear();
            pdg.mergeSequential(bg);
		}
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
			new EGroumDataEdge(n, en, THROW);
		}
		context.removeScope();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, CastExpression astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
		String type = JavaASTUtil.getSimpleType(astNode.getType());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + ".<cast>", type + ".<cast>");
		pdg.mergeSequentialData(node, PARAMETER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			BreakStatement astNode) {
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), astNode.getLabel() == null ? "" : astNode.getLabel().getIdentifier(), null,
				"break");
		EGroumGraph pdg = new EGroumGraph(context, node, configuration);
		pdg.breaks.add(node);
		pdg.sinks.remove(node);
		pdg.statementSinks.remove(node);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			BooleanLiteral astNode) {
		return new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.toString(), "boolean", null,
				astNode.toString()), configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, Block astNode) {
		if (astNode.statements().size() > 0) {
			context.addScope();
			EGroumGraph pdg = buildPDG(control, branch, astNode.statements());
			context.removeScope();
			return pdg;
		}
		return new EGroumGraph(context, configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, List<?> list) {
		EGroumGraph g = new EGroumGraph(context, configuration);
		for (Object s : list) {
			if (s instanceof EmptyStatement) continue;
			EGroumGraph pdg = buildPDG(control, branch, (ASTNode) s);
			if (!pdg.isEmpty()) {
				g.mergeSequential(pdg);
			}
			if (s instanceof ReturnStatement
					|| s instanceof ThrowStatement
					|| s.toString().startsWith("System.exit(")
					) {
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
			ag.mergeSequentialData(node, RECEIVER);
			EGroumGraph ig = buildArgumentPDG(control, branch, aa.getIndex());
			ig.mergeSequentialData(node, PARAMETER);
			EGroumGraph vg = null;
			if (astNode.getOperator() == Assignment.Operator.ASSIGN) {
				vg = buildArgumentPDG(control, branch, astNode.getRightHandSide());
			} else {
				String op = JavaASTUtil.getAssignOperator(astNode.getOperator());
				EGroumGraph g1 = buildPDG(control, branch, astNode.getLeftHandSide());
				EGroumGraph g2 = buildArgumentPDG(control, branch, astNode.getRightHandSide());
				EGroumActionNode opNode = new EGroumActionNode(control, branch, null, ASTNode.INFIX_EXPRESSION, null, op, op);
				g1.mergeSequentialData(opNode, PARAMETER);
				g2.mergeSequentialData(opNode, PARAMETER);
				vg = new EGroumGraph(context, configuration);
				vg.mergeParallel(g1, g2);
			}
			vg.mergeSequentialData(node, PARAMETER);
			EGroumGraph pdg = new EGroumGraph(context, configuration);
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
			EGroumGraph g2 = buildArgumentPDG(control, branch, astNode.getRightHandSide());
			EGroumActionNode opNode = new EGroumActionNode(control, branch,
					null, ASTNode.INFIX_EXPRESSION, null, null, op);
			g1.mergeSequentialData(opNode, PARAMETER);
			g2.mergeSequentialData(opNode, PARAMETER);
			pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(g1, g2);
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(lnode, DEFINITION);
		} else {
			pdg = buildDefinitionPDG(control, branch, astNode.getRightHandSide(), lnode);
		}
		pdg.mergeSequentialData(new EGroumDataNode(lnode), REFERENCE);
		if (!pdg.nodes.contains(lnode))
			lg.delete(lnode);
		pdg.nodes.addAll(lg.nodes);
		pdg.statementNodes.addAll(lg.statementNodes);
		lg.dataSources.remove(lnode);
		pdg.dataSources.addAll(lg.dataSources);
		pdg.statementSources.addAll(lg.statementSources);
		lg.clear();
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, AssertStatement astNode) {
		// skip assert statement
		return new EGroumGraph(context, configuration);
		/*EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, "assert");
		pdg.mergeSequentialData(node, Type.PARAMETER);
		// skip astNode.getMessage()
		return pdg;*/
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ArrayInitializer astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.expressions().size()];
		for (int i = 0; i < astNode.expressions().size(); i++) {
			pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.expressions().get(i));
		}
		String type = "";
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getElementType().getName();
		EGroumNode node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(),
				null, "{" + type + "}", "{" + type + "}");
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, PARAMETER);
			EGroumGraph pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
			return pdg;
		} else
			return new EGroumGraph(context, node, configuration);
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ArrayCreation astNode) {
		if (astNode.getInitializer() != null) {
			return buildPDG(control, branch, astNode.getInitializer());
		}
		EGroumGraph[] pgs = new EGroumGraph[astNode.dimensions().size()];
		for (int i = 0; i < astNode.dimensions().size(); i++)
			pgs[i] = buildArgumentPDG(control, branch, (Expression) astNode.dimensions().get(i));
		String type = JavaASTUtil.getSimpleType(astNode.getType().getElementType());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, "{" + type + "}", "{" + type + "}");
		if (pgs.length > 0) {
			for (EGroumGraph pg : pgs)
				pg.mergeSequentialData(node, PARAMETER);
			EGroumGraph pdg = new EGroumGraph(context, configuration);
			pdg.mergeParallel(pgs);
			return pdg;
		} else
			return new EGroumGraph(context, node, configuration);
	}

	private void mergeBranches(EGroumGraph... pdgs) {
		for (EGroumGraph pdg : pdgs) {
            connectSinksToSourcesOf(pdg);
			for (EGroumNode sink : pdg.sinks)
				sink.consumeDefStore(this);
		}
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
			dataSources.addAll(pdg.dataSources);
			// statementSources.addAll(pdg.statementSources);
			breaks.addAll(pdg.breaks);
			returns.addAll(pdg.returns);
		}
	}

    private void connectSinksToSourcesOf(EGroumGraph pdg) {
        if (!sinks.isEmpty()) {
            HashSet<EGroumDataNode> remains = new HashSet<EGroumDataNode>();
            for (EGroumDataNode source : pdg.dataSources) {
                for (EGroumNode sink : sinks) {
                    HashSet<EGroumDataNode> defs = sink.defStore.get(source.key);
                    if (defs == null || defs.isEmpty())
                        remains.add(source);
                    else
                        for (EGroumDataNode def : defs)
                            if (!source.hasInDataNode(def, REFERENCE))
                                new EGroumDataEdge(def, source, REFERENCE);
                }
            }
            pdg.dataSources = remains;
        }
    }

	private void mergeParallel(EGroumGraph... pdgs) {
		for (EGroumGraph pdg : pdgs)
			for (EGroumNode sink : pdg.sinks)
				sink.consumeDefStore(this);
		for (EGroumGraph pdg : pdgs) {
			nodes.addAll(pdg.nodes);
			statementNodes.addAll(pdg.statementNodes);
			sinks.addAll(pdg.sinks);
			statementSinks.addAll(pdg.statementSinks);
			dataSources.addAll(pdg.dataSources);
			statementSources.addAll(pdg.statementSources);
			breaks.addAll(pdg.breaks);
			returns.addAll(pdg.returns);
			pdg.clear();
		}
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

	private void delete(EGroumNode node) {
		if (statementSinks.contains(node))
			for (EGroumEdge e : node.inEdges)
				if (e instanceof EGroumDataEdge) {
					if (((EGroumDataEdge) e).type == DEPENDENCE)
						statementSinks.add(e.source);
					else if (((EGroumDataEdge) e).type == PARAMETER)
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
						&& ((EGroumDataEdge) e).type == DEPENDENCE)
					statementSources.add(e.target);
		nodes.remove(node);
		statementNodes.remove(node);
		dataSources.remove(node);
		statementSources.remove(node);
		sinks.remove(node);
		statementSinks.remove(node);
//		EGroumNode qual = node.getQualifier();
//		if (qual != null && !qual.isDeclaration())
//			delete(qual);
		node.delete();
	}

	private EGroumGraph buildDefinitionPDG(EGroumNode control, String branch, ASTNode exp, EGroumDataNode data) {
		EGroumGraph pdg = buildArgumentPDG(control, branch, exp);
		EGroumDataNode out = pdg.getOnlyDataOut();
		if (out.isDummy()) {
			for (String key : data.defStore.keySet()) {
				if (out.defStore.containsKey(key))
					out.defStore.get(key).addAll(data.defStore.get(key));
				else
					out.defStore.put(key, data.defStore.get(key));
			}
			out.defStore.put(data.key, out.defStore.get(out.key));
			out.defStore.remove(out.key);
			((EGroumDataNode) out).copyData(data);
			for (EGroumNode def : out.getDefinitions()) {
				((EGroumDataNode) def).copyData(data);
				def.defStore = new HashMap<>(out.defStore);
				for (EGroumEdge e : data.inEdges) {
					if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == QUALIFIER) {
						new EGroumDataEdge(e.source, def, QUALIFIER);
					}
				}
			}
			pdg.delete(out);
		} else {
			pdg.mergeSequentialData(new EGroumActionNode(control, branch, exp.getParent(), ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
			pdg.mergeSequentialData(data, DEFINITION);
		}
		return pdg;
	}

	private EGroumGraph buildArgumentPDG(EGroumNode control, String branch, ASTNode exp) {
		EGroumGraph pdg = buildPDG(control, branch, exp);
		if (pdg.isEmpty())
			return pdg;
		if (pdg.nodes.size() == 1)
			for (EGroumNode node : pdg.nodes)
				if (node instanceof EGroumDataNode)
					return pdg;
		EGroumNode node = pdg.getOnlyOut();
		if (node instanceof EGroumDataNode) {
			if (node.isDefinition())
				pdg.mergeSequentialData(new EGroumDataNode(node.astNode, node.astNodeType, node.key, node.dataType, ((EGroumDataNode) node).dataName, ((EGroumDataNode) node).dataValue, ((EGroumDataNode) node).isField, false, ((EGroumDataNode) node).encodeLevel), REFERENCE);
			return pdg;
		}
		String type = node.dataType;
		if (type.equals("<a>")) {
			type = "int";
			for (EGroumEdge e : node.inEdges) {
				if (e.source instanceof EGroumDataNode && e.source.dataType.equals("String")) {
					type = "String";
					break;
				}
			}
		} else if (type.equals("<b>")) {
			type = "int";
			for (EGroumEdge e : node.inEdges) {
				if (e.source instanceof EGroumDataNode && e.source.dataType.equals("boolean")) {
					type = "boolean";
					break;
				}
			}
		} else if (type.equals("<r>") || type.equals("<c>"))
			type = "boolean";
		else if (type.equals(PrefixExpression.Operator.COMPLEMENT) || type.equals(PrefixExpression.Operator.MINUS))
			type = "int";
		else if (type.equals(PrefixExpression.Operator.NOT))
			type = "boolean";
		if (((Expression) exp).resolveTypeBinding() != null) {
			ITypeBinding itb = ((Expression) exp).resolveTypeBinding().getTypeDeclaration();
			if (itb.isAnonymous()) {
				if (exp instanceof ClassInstanceCreation)
					type = JavaASTUtil.getSimpleType(((ClassInstanceCreation) exp).getType());
			} else
				type = itb.getName();
		}
		EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + exp.getStartPosition() + "_"
						+ exp.getLength(), type, EGroumNode.PREFIX_DUMMY, false, true);
		pdg.mergeSequentialData(new EGroumActionNode(control, branch,
				null, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
		pdg.mergeSequentialData(dummy, DEFINITION);
		pdg.mergeSequentialData(new EGroumDataNode(null, dummy.astNodeType, dummy.key,
				dummy.dataType, dummy.dataName), REFERENCE);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, ArrayAccess astNode) {
		EGroumGraph ag = buildArgumentPDG(control, branch, astNode.getArray());
		String type = ag.getOnlyOut().dataType;
		if (astNode.getArray().resolveTypeBinding() != null)
			type = astNode.getArray().resolveTypeBinding().getTypeDeclaration().getName();
		EGroumNode node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(), null, type + ".arrayget()", "arrayget()");
		ag.mergeSequentialData(node, RECEIVER);
		EGroumGraph ig = buildArgumentPDG(control, branch, astNode.getIndex());
		ig.mergeSequentialData(node, PARAMETER);
		EGroumGraph pdg = new EGroumGraph(context, configuration);
		pdg.mergeParallel(ag, ig);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, FieldAccess astNode) {
		String name = astNode.getName().getIdentifier();
		if (astNode.getExpression() instanceof ThisExpression) {
			String type = null;
			if (astNode.resolveTypeBinding() != null)
				type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
			if (type == null)
				type = context.getFieldType(astNode.getName());
			if (type != null) {
				if (configuration.keepQualifierEdges) {
					EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.THIS_EXPRESSION, "this", context.getType(), "this"), configuration);
					pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, "this." + name, type, name, true, false), QUALIFIER);
					return pdg;
				} else {
					return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, "this." + name, type, name, true, false), configuration);
				}
			}
			if (configuration.keepQualifierEdges) {
				EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.THIS_EXPRESSION, "this", context.getType(), "this"), configuration);
				pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, "this." + name, "UNKNOWN", name, true, false), QUALIFIER);
				return pdg;
			} else {
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, "this." + name, "UNKNOWN", name, true, false), configuration);
			}
		} else {
			EGroumGraph pdg = buildArgumentPDG(control, branch, astNode.getExpression());
			EGroumDataNode qual = pdg.getOnlyDataOut();
			String type = qual.dataType;
			if (astNode.getExpression().resolveTypeBinding() != null)
				type = astNode.getExpression().resolveTypeBinding().getTypeDeclaration().getName();
			EGroumDataNode node = new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, qual.key == null ? astNode.toString() : qual.key + "." + astNode.getName().getIdentifier(), type + "." + name, name, true, false);
			if (configuration.keepQualifierEdges)
				pdg.mergeSequentialData(node, QUALIFIER);
			else
				pdg = new EGroumGraph(context, node, configuration);
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
			if (configuration.keepQualifierEdges) {
				EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.THIS_EXPRESSION, "this", context.getSuperType(), "super"), configuration);
				pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, "this." + name, type, name, true, false), QUALIFIER);
				return pdg;
			}
			else {
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, "this." + name, type, name, true, false), configuration);
			}
		}
		if (configuration.keepQualifierEdges) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.THIS_EXPRESSION, "this", context.getSuperType(), "super"), configuration);
			pdg.mergeSequentialData(new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, astNode.toString(), context.getSuperType() + "." + name, name, true, false), QUALIFIER);
			return pdg;
		} else {
			return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.SIMPLE_NAME, astNode.toString(), context.getSuperType() + "." + name, name, true, false), configuration);
		}
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ThisExpression astNode) {
		String type = context.getType();
		if (astNode.resolveTypeBinding() != null)
			type = astNode.resolveTypeBinding().getTypeDeclaration().getName();
		return new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), "this", type,
				"this"), configuration);
	}

	private void mergeSequential(EGroumGraph pdg) {
		if (pdg.statementNodes.isEmpty())
			return;
        if (this.isEmpty()) {
            // if the left side of the join is empty, the entire right side becomes the result, since there are not
            // sinks to connect to the sources of the right side.
            this.statementSources.addAll(pdg.statementSources);
        }
        connectSinksToSourcesOf(pdg);
		for (EGroumNode sink : pdg.sinks)
			sink.consumeDefStore(this);
		for (EGroumNode sink : statementSinks) {
			for (EGroumNode source : pdg.statementSources) {
				new EGroumDataEdge(sink, source, DEPENDENCE);
			}
		}
		/*if (this.statementNodes.isEmpty() || pdg.statementNodes.isEmpty()) {
			System.err.println("Merge an empty graph!!!");
			System.exit(-1);
		}*/
		this.dataSources.addAll(pdg.dataSources);
		this.sinks.clear();
		this.sinks.addAll(pdg.sinks);
		this.statementSinks.clear();
		this.statementSinks.addAll(pdg.statementSinks);
		this.nodes.addAll(pdg.nodes);
		this.statementNodes.addAll(pdg.statementNodes);
		this.breaks.addAll(pdg.breaks);
		this.returns.addAll(pdg.returns);
		pdg.clear();
	}

	private static <E> void clear(HashMap<String, HashSet<E>> map) {
		for (String key : map.keySet())
			map.get(key).clear();
		map.clear();
	}

	private void clearDefStore() {
		for (EGroumNode sink : sinks)
			clear(sink.defStore);
	}
	
	private void mergeSequentialControl(EGroumNode next, String label) {
		next.consumeDefStore(this);
		sinks.clear();
		sinks.add(next);
		statementSinks.clear();
		statementSinks.add(next);
		if (statementNodes.isEmpty())
			statementSources.add(next);
		nodes.add(next);
		statementNodes.add(next);
	}

	private void mergeSequentialData(EGroumNode next, EGroumDataEdge.Type type) {
		if (next.isStatement())
			for (EGroumNode sink : statementSinks)
				new EGroumDataEdge(sink, next, DEPENDENCE);
		next.consumeDefStore(this);
		if (type == QUALIFIER) {
			dataSources.add((EGroumDataNode) next);
		} else if (type != DEFINITION && type != REFERENCE && next instanceof EGroumDataNode) {
			HashSet<EGroumDataNode> ns = next.defStore.get(next.key);
			if (ns != null)
				for (EGroumDataNode def : ns)
					if (!next.hasInDataNode(def, REFERENCE))
						new EGroumDataEdge(def, next, REFERENCE);
		}
		if (type == DEFINITION) {
			HashSet<EGroumDataNode> defs = new HashSet<>();
			defs.add((EGroumDataNode) next);
			next.defStore.put(next.key, defs);
		}
		for (EGroumNode node : sinks)
			new EGroumDataEdge(node, next, type, type == CONDITION ? next.getConditionLabel() : null);
		sinks.clear();
		sinks.add(next);
		if (nodes.isEmpty() && next instanceof EGroumDataNode && type != DEFINITION)
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
		for (EGroumNode node : new HashSet<>(breaks)) {
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
//		System.exit(-1);
		return null;
	}

	private EGroumDataNode getOnlyDataOut() {
		if (sinks.size() == 1)
			for (EGroumNode n : sinks)
				if (n instanceof EGroumDataNode)
					return (EGroumDataNode) n;
		System.err.println("ERROR in getting the only data output node!!!");
		return null;
	}

	private boolean isEmpty() {
		return nodes.isEmpty();
	}

	private void buildClosure() {
		HashSet<EGroumNode> doneNodes = new HashSet<>();
		for (EGroumNode node : nodes)
			if (!doneNodes.contains(node))
				node.buildDataClosure(doneNodes);

		// SMELL doesn't the condition closure need to be built predecessors-first as well? All the other closures do.
		for (EGroumNode node : nodes)
			if (node instanceof EGroumControlNode && node.astNodeType != ASTNode.CATCH_CLAUSE && node.astNodeType != ASTNode.SYNCHRONIZED_STATEMENT)
				((EGroumControlNode) node).buildConditionClosure();

		if (!configuration.buildTransitiveDataEdges) {
			for (EGroumNode node : nodes) {
				for (EGroumEdge e : new HashSet<>(node.inEdges))
					if (e.isTransitive && e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type != CONDITION)
						e.delete();
			}
		}

		buildSequentialClosure();
		pruneTemporaryDataDependence();

		doneNodes = new HashSet<>();
		for (EGroumNode node : nodes)
			if (!doneNodes.contains(node))
				node.buildControlClosure(doneNodes);
	}

	private void buildSequentialClosure() {
        Map<EGroumNode, HashSet<EGroumNode>> predRelation = buildPredecessorRelation();

        for (EGroumNode node : predRelation.keySet()) {
            if (node.getAstNodeType() == ASTNode.METHOD_INVOCATION || node.isCoreAction()) {
                for (EGroumNode preNode : predRelation.get(node)) {
                    if (preNode.getAstNodeType() == ASTNode.METHOD_INVOCATION || preNode.isCoreAction()) {
                        new EGroumDataEdge(preNode, node, ORDER);
                    }
                }
            }
        }
	}

    private Map<EGroumNode, HashSet<EGroumNode>> buildPredecessorRelation() {
        HashMap<EGroumNode, HashSet<EGroumNode>> preNodesOfNode = new HashMap<>();
        preNodesOfNode.put(entryNode, new HashSet<>());
        HashSet<EGroumNode> visitedNodes = new HashSet<>();
        entryNode.buildPreSequentialNodes(visitedNodes, preNodesOfNode);
        for (EGroumNode node : nodes) {
            if (!visitedNodes.contains(node))
                node.buildPreSequentialNodes(visitedNodes, preNodesOfNode);
        }
        return preNodesOfNode;
    }

    private int compareBySize(HashSet<EGroumNode> preds1, HashSet<EGroumNode> preds2) {
        return Integer.compareUnsigned(preds1.size(), preds2.size());
    }

    private boolean areInDifferentCatches(EGroumNode node, HashSet<EGroumNode> preNodes) {
        boolean inDifferentCatches = false;
        HashSet<EGroumNode> cns = node.getCatchClauses();
        for (EGroumNode cn : cns) {
            for (EGroumEdge e : cn.inEdges) {
                if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == CONDITION) {
                    EGroumNode en = e.source.getDefinition();
                    if (en == null) continue;
                    for (EGroumEdge e1 : en.inEdges) {
                        if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == EGroumDataEdge.Type.THROW) {
                            if (!preNodes.contains(en) && preNodes.contains(e1.source)) {
                                inDifferentCatches = true;
                                break;
                            }
                        }
                    }
                }
                if (inDifferentCatches)
                    break;
            }
            if (inDifferentCatches)
                break;
        }
        return inDifferentCatches;
    }

    private void pruneTemporaryDataDependence() {
		for (EGroumNode node : nodes) {
			if (node == entryNode || node == endNode)
				continue;
			int i = 0;
			while (i < node.inEdges.size()) {
				EGroumEdge e = node.inEdges.get(i);
				if (e.source != entryNode && e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == DEPENDENCE) {
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
				if (e.target != endNode && e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == DEPENDENCE) {
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

	private void deleteOperators() {
		for (EGroumNode node : new HashSet<>(nodes)) {
			if (node.astNodeType != ASTNode.INFIX_EXPRESSION)
				continue;
			if ((!configuration.encodeArithmeticOperators && node.getLabel().equals("<a>"))
					|| (!configuration.encodeBitwiseOperators && node.getLabel().equals("<b>"))
					|| (!configuration.encodeConditionalOperators && node.getLabel().equals("<c>"))) {
				HashSet<EGroumNode> delNodes = new HashSet<>();
				Set<EGroumNode> inDeps = new HashSet<>();
                for (EGroumEdge inEdge : node.inEdges) {
                    if (inEdge instanceof EGroumDataEdge && ((EGroumDataEdge) inEdge).type == DEPENDENCE) {
                        inDeps.add(inEdge.source);
                    }
                }
				for (EGroumEdge e1 : node.outEdges) {
					if (e1.target.isAssignment() && e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == PARAMETER) {
						for (EGroumEdge e2 : e1.target.outEdges) {
							if (e2 instanceof EGroumDataEdge && ((EGroumDataEdge) e2).type == DEFINITION) {
								for (EGroumEdge e3 : e2.target.outEdges) {
									if (e3 instanceof EGroumDataEdge && ((EGroumDataEdge) e3).type == REFERENCE) {
										for (EGroumEdge e4 : e3.target.outEdges) {
											for (EGroumEdge e : node.inEdges)
												if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == PARAMETER)
													new EGroumDataEdge(e.source, e4.target, ((EGroumDataEdge) e4).type, e4.label);
										}
										delNodes.add(e3.target);
									}
								}
								delNodes.add(e2.target);
							} else if (e2 instanceof EGroumDataEdge && ((EGroumDataEdge) e2).type == DEPENDENCE) {
                                for (EGroumNode inDep : inDeps) {
                                    new EGroumDataEdge(inDep, e2.target, DEPENDENCE);
                                }
                            }
						}
						delNodes.add(e1.target);
						break;
					}
				}
				delNodes.add(node);
				for (EGroumNode dn : delNodes)
					delete(dn);
			}
		}
	}
	
	private void removeThisMembers() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node instanceof EGroumDataNode) {
				if (node.key.startsWith("this") || node.key.startsWith("super")) {
					String[] parts = node.key.split("\\.");
					if ((parts[0].equals("this") || parts[0].equals("super")) && parts.length <= configuration.removeImplementationCode) {
						for (EGroumEdge e : new HashSet<EGroumEdge>(node.outEdges))
							if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == RECEIVER)
								delete(e.target);
					}
				} else if (configuration.removeImplementationCode >= 2 && isInClosure((EGroumDataNode) node)) {
					for (EGroumEdge e : new HashSet<EGroumEdge>(node.outEdges))
						if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == RECEIVER)
							delete(e.target);
				}
			}
		}
	}

	private boolean isInClosure(EGroumDataNode node) {
		if (node.isDeclaration)
			return false;
		if (node.isField)
			return false;
		if (node.astNode == null)
			return false;
		if (!(node.astNode instanceof SimpleName))
			return false;
		IBinding b = ((SimpleName)(node.astNode)).resolveBinding();
		if (b == null || !(b instanceof IVariableBinding))
			return false;
		IVariableBinding vb = (IVariableBinding) b;
		vb = vb.getVariableDeclaration();
		String vbKey = vb.getKey();
		MethodDeclaration md = getContainingMethod(node);
		if (md == null)
			return false;
		IMethodBinding mb = md.resolveBinding();
		if (mb == null)
			return false;
		mb = mb.getMethodDeclaration();
		String mbKey = mb.getKey();
		return !vbKey.startsWith(mbKey);
	}

	private MethodDeclaration getContainingMethod(EGroumNode node) {
		return getContainingMethod(node.astNode);
	}

	private MethodDeclaration getContainingMethod(ASTNode node) {
		if (node == null)
			return null;
		if (node instanceof MethodDeclaration)
			return (MethodDeclaration) node;
		return getContainingMethod(node.getParent());
	}

	private void deleteTemporaryDataNodesIncomingToControlNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node instanceof EGroumControlNode && node.astNodeType != ASTNode.CATCH_CLAUSE && node.astNodeType != ASTNode.ENHANCED_FOR_STATEMENT && node.astNodeType != ASTNode.SYNCHRONIZED_STATEMENT) {
				int i = 0;
				while (i < node.inEdges.size()) {
					EGroumEdge e = node.inEdges.get(i);
					i++;
					if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == CONDITION) {
						if (e.source instanceof EGroumDataNode) {
							ArrayList<EGroumNode> inits = e.source.getInitActions();
							for (EGroumNode init : inits)
								if (!node.hasInNode(init))
									new EGroumDataEdge(init, node, CONDITION, e.label);
							delete(e.source);
							i--;
						}
					}
				}
			}
		}
	}

	private void deleteTemporaryDataNodes() {
		for (EGroumNode node : new HashSet<>(nodes)) {
			if (node.isDefinition()) {
				EGroumDataNode dn = (EGroumDataNode) node;
				ArrayList<EGroumNode> refs = dn.getReferences();
				if (refs.size() == 1 && refs.get(0).getDefinitions().size() == 1) {
					boolean del = false;
					EGroumNode ref = refs.get(0);
					for (EGroumEdge ie : dn.inEdges)
						if (ie instanceof EGroumDataEdge && ((EGroumDataEdge) ie).type == DEFINITION) {
							EGroumNode an = ie.source;
							if (!(an.control instanceof EGroumControlNode) || ((EGroumControlNode) an.control).controlsAnotherNode(an)) {
								for (EGroumEdge e : an.inEdges) {
									if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == PARAMETER)
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
		for (EGroumNode node : new HashSet<>(nodes)) {
			if (node instanceof EGroumDataNode) {
				boolean isRef = false;
				for (EGroumEdge e : node.inEdges) {
					if (e instanceof EGroumDataEdge) {
						EGroumDataEdge in = (EGroumDataEdge) e;
						if (in.type == REFERENCE) {
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
		for (EGroumNode node : new HashSet<>(statementNodes)) {
			if (node.isEmptyNode()) {
				int index = node.control.getOutEdgeIndex(node);
				for (EGroumEdge out : new HashSet<>(node.outEdges)) {
					if (out.target.control != node.control) {
						out.source.outEdges.remove(out);
						out.source = node.control;
						index++;
						out.source.outEdges.add(index, out);
					}
				}
				delete(node);
			}
		}
	}

	private void addDefinitions(EGroumDataNode node, HashMap<EGroumNode, HashMap<String, EGroumNode>> defs) {
		this.dataSources.remove(node);
		EGroumNode qual = node.getQualifier();
		if (qual == null) {
			EGroumNode def = defs.get(null).get(node.key);
			if (def == null) {
				def = new EGroumDataNode(null, node.astNodeType, node.key, node.dataType, node.dataName, node.dataValue, true, true, node.encodeLevel);
				defs.get(null).put(node.key, def);
				nodes.add(def);
			}
			new EGroumDataEdge(def, node, REFERENCE);
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
				HashMap<String, EGroumNode> ds = defs.computeIfAbsent(qualDef, k -> new HashMap<>());
				EGroumNode def = ds.get(node.key);
				if (def == null) {
					def = new EGroumDataNode(null, node.astNodeType, node.key, node.dataType, node.dataName, node.dataValue, node.isField, true, node.encodeLevel);
					ds.put(node.key, def);
					nodes.add(def);
					EGroumDataNode qualRef = new EGroumDataNode(null, qualDef.astNodeType, qualDef.key, qualDef.dataType, ((EGroumDataNode) qualDef).dataName, ((EGroumDataNode) qualDef).dataValue, ((EGroumDataNode) qualDef).isField, false, ((EGroumDataNode) qualDef).encodeLevel);
					nodes.add(qualRef);
					new EGroumDataEdge(qualDef, qualRef, REFERENCE);
					new EGroumDataEdge(qualRef, def, QUALIFIER);
				}
				new EGroumDataEdge(def, node, REFERENCE);
			}
			delete(qual);
		}
	}

	private void cleanUp() {
		clearDefStore();
		for (EGroumNode node : new HashSet<>(nodes))
			node.astNode = null;
	}

	private void renameEdges() {
		for (EGroumNode node : nodes) {
			for (EGroumEdge e : node.inEdges) {
				EGroumDataEdge de = (EGroumDataEdge) e;
				if (de.type != CONDITION)
					de.type = ORDER;
			}
		}
	}

	private void deleteNonCoreActionNodes() {
		for (EGroumNode node : new HashSet<>(nodes))
			if (node.astNodeType != ASTNode.METHOD_INVOCATION)
				delete(node);
	}

	private void deleteDataNodes() {
		for (EGroumNode node : new HashSet<>(nodes))
			if (node instanceof EGroumDataNode)
				delete(node);
	}

	private void deleteControlNodes() {
		for (EGroumNode node : new HashSet<>(nodes))
			if (node instanceof EGroumEntryNode || node instanceof EGroumControlNode)
				delete(node);
	}

	private void deleteUnusedDataNodes() {
		for (EGroumNode node : new HashSet<>(nodes)) {
			if (node instanceof EGroumDataNode) {
				if (node.outEdges.isEmpty()) {
					LinkedList<EGroumNode> q = new LinkedList<>();
					q.add(node);
					while (!q.isEmpty()) {
						EGroumNode n = q.remove();
						for (EGroumEdge e : n.inEdges) {
							if (e.source instanceof EGroumDataNode && e.source.outEdges.size() == 1)
								q.add(e.source);
						}
						delete(n);
					}
				}
			}
		}
	}

	private void deleteUnreachableNodes() {
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
		for (EGroumNode node : new HashSet<>(nodes))
			if (!reachableNodes.contains(node))
				delete(node);
	}

	private void deleteAssignmentNodes() {
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