package egroum;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
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
import utils.JavaASTUtil;

public class EGroumGraph implements Serializable {
	private static final long serialVersionUID = -5128703931982211886L;
	
	private String filePath, name;
	private EGroumBuildingContext context;
	private HashMap<String, HashSet<EGroumDataNode>> defStore = new HashMap<>();
	protected EGroumNode entryNode, endNode;
	protected EGroumDataNode[] parameters;
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
		int numOfParameters = 0;
		if (Modifier.isStatic(md.getModifiers()))
			parameters = new EGroumDataNode[md.parameters().size()];
		else {
			parameters = new EGroumDataNode[md.parameters().size() + 1];
			parameters[numOfParameters++] = new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this", "this", "this");
		}
		entryNode = new EGroumEntryNode(md, ASTNode.METHOD_DECLARATION, "START");
		nodes.add(entryNode);
		statementNodes.add(entryNode);
		for (int i = 0; i < md.parameters().size(); i++) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) md
					.parameters().get(i);
			String id = d.getName().getIdentifier();
			//context.addLocalVariable(id, "" + d.getStartPosition());
			mergeSequential(buildPDG(entryNode, "", d));
			String[] info = context.getLocalVariableInfo(id);
			this.parameters[numOfParameters++] = new EGroumDataNode(
					d.getName(), ASTNode.SIMPLE_NAME, info[0], info[1],
					"PARAM_" + d.getName().getIdentifier(), false, true);
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
		adjustReturnNodes();
		adjustControlEdges();
		context.removeScope();
		addDefinitions();
		pruneDataNodes();
		pruneEmptyStatementNodes();
		buildClosure();
		deleteTemporaryDataNodes();
		deleteReferences();
		deleteAssignmentNodes();
		cleanUp();
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
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
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
		ArrayList<EGroumActionNode> rets = pdg.getReturns();
		if (rets.size() > 0) {
			for (EGroumActionNode ret : rets) {
				ret.astNodeType = ASTNode.ASSIGNMENT;
				ret.name = "=";
				pdg.extend(ret, new EGroumDataNode(node), Type.DEFINITION);
			}
			return pdg;
		}
		ArrayList<EGroumDataNode> defs = pdg.getDefinitions();
		if (defs.isEmpty()) {
			pdg.mergeSequentialData(new EGroumActionNode(control, branch,
					astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
			pdg.mergeSequentialData(node, Type.DEFINITION);
		} else {
			if (defs.get(0).isDummy()) {
				for (EGroumDataNode def : defs) {
					defStore.remove(def.key);
					def.copyData(node);
					HashSet<EGroumDataNode> ns = defStore.get(def.key);
					if (ns == null) {
						ns = new HashSet<>();
						defStore.put(def.key, ns);
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
		if (astNode.getBody().statements().isEmpty())
			return new EGroumGraph(context);
		context.pushTry();
		EGroumGraph pdg = buildPDG(control, branch, astNode.getBody());
		ArrayList<EGroumActionNode> triedMethods = context.popTry();
		EGroumGraph[] gs = new EGroumGraph[astNode.catchClauses().size()];
		for (int i = 0; i < astNode.catchClauses().size(); i++) {
			CatchClause cc = (CatchClause) astNode.catchClauses().get(i);
			gs[i] = buildPDG(control, branch, cc, triedMethods);
		}
		EGroumGraph cg = new EGroumGraph(context);
		cg.mergeBranches(gs);
		pdg.mergeSequential(cg);
		if (astNode.getFinally() != null) {
			EGroumControlNode fn = new EGroumControlNode(control, branch, astNode.getFinally(), astNode.getFinally().getNodeType());
			EGroumGraph fg = new EGroumGraph(context, fn);
			fg.mergeSequential(buildPDG(fn, "", astNode.getFinally()));
			pdg.mergeSequential(fg);
		}
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
		return pdg;
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
		pdg.mergeSequentialData(node, Type.PARAMETER);
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
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
		pdg.adjustBreakNodes("");
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SuperMethodInvocation astNode) {
		EGroumGraph[] pgs = new EGroumGraph[astNode.arguments().size() + 1];
		pgs[0] = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.THIS_EXPRESSION, "this",
				"super", "super"));
		for (int i = 0; i < astNode.arguments().size(); i++)
			pgs[i+1] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, "super." + astNode.getName().getIdentifier() + "()", 
				astNode.getName().getIdentifier());
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
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, "super()", "<init>");
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
		if (astNode.getInitializer() == null)
			return new EGroumGraph(context, node);
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(null, ASTNode.NULL_LITERAL, "null", "", "null"));
		pdg.mergeSequentialData(new EGroumActionNode(control, branch,
				astNode, ASTNode.ASSIGNMENT, null, null, "="), Type.PARAMETER);
		pdg.mergeSequentialData(node, Type.DEFINITION);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, SimpleName astNode) {
		String name = astNode.getIdentifier();
		String[] info = context.getLocalVariableInfo(name);
		if (info != null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					astNode, astNode.getNodeType(), info[0], info[1],
					astNode.getIdentifier(), false, false));
			return pdg;
		}
		String type = context.getFieldType(name);
		if (type != null) {
			EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
					null, ASTNode.THIS_EXPRESSION, "this",
					"this", "this"));
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
				"this", "this"));
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
		if (node.dataType.startsWith("UNKNOWN")) {
			String name = astNode.getName().getIdentifier();
			if (Character.isUpperCase(name.charAt(0))) {
				return new EGroumGraph(context, new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.getFullyQualifiedName(),
						astNode.getFullyQualifiedName(), astNode.getName().getIdentifier(), true, false));
			}
		} else
			pdg.mergeSequentialData(
					new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.getFullyQualifiedName(),
							node.dataType + "." + astNode.getName().getIdentifier(),
							astNode.getName().getIdentifier(), true, false), Type.QUALIFIER);
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
					null, ASTNode.NUMBER_LITERAL, "1", node.dataType, "1"));
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
		// FIXME handling postfix expression more precisely
		EGroumGraph rg = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.NUMBER_LITERAL, "1", node.dataType, "1"));
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
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), astNode.getToken(), "number",
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
					"this", "this"));
		for (int i = 0; i < astNode.arguments().size(); i++)
			pgs[i + 1] = buildArgumentPDG(control, branch,
					(Expression) astNode.arguments().get(i));
		String type = pgs[0].getOnlyOut().dataType;
		HashSet<String> exceptions = EGroumBuildingContext.getExceptions(type, astNode.getName().getIdentifier() + "(" + astNode.arguments().size() + ")");
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, 
				type + "." + astNode.getName().getIdentifier() + "()", 
				astNode.getName().getIdentifier(), exceptions);
		if (exceptions != null)
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
				astNode, astNode.getNodeType(), null, null, astNode.getOperator().toString());
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
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
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
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
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
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
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
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, "this()", "<init>");
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
		EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + astNode.getStartPosition() + "_"
						+ astNode.getLength(), "boolean", EGroumNode.PREFIX_DUMMY, false, true);
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
		/*
		 * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
		 */
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
		HashSet<String> exceptions = EGroumBuildingContext.getExceptions(type, "<init>" + "(" + astNode.arguments().size() + ")");
		EGroumActionNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, type + ".<init>", "<init>", exceptions);
		if (exceptions != null)
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
		// skip astNode.getAnonymousClassDeclaration()
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
		if (astNode.getException().getType().isUnionType()) {
			UnionType ut = (UnionType) astNode.getException().getType();
			for (int i = 0; i < ut.types().size(); i++) {
				org.eclipse.jdt.core.dom.Type t = (org.eclipse.jdt.core.dom.Type) ut.types().get(i);
				nodes.addAll(context.getTrys(JavaASTUtil.getSimpleType(t), triedMethods));
			}
		}
		else
			nodes = context.getTrys(JavaASTUtil.getSimpleType(astNode.getException().getType()), triedMethods);
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
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, JavaASTUtil.getSimpleType(astNode.getType()), JavaASTUtil.getSimpleType(astNode.getType()) + ".<cast>");
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

	public EGroumGraph buildPDG(EGroumNode control, String branch, List<?> l) {
		EGroumGraph g = new EGroumGraph(context);
		for (int i = 0; i < l.size(); i++) {
			if (l.get(i) instanceof EmptyStatement) continue;
			EGroumGraph pdg = buildPDG(control, branch, (ASTNode) l.get(i));
			if (!pdg.isEmpty())
				g.mergeSequential(pdg);
			if (l.get(i) instanceof ReturnStatement || l.get(i) instanceof ThrowStatement || l.get(i).toString().startsWith("System.exit(")) {
				g.clearDefStore();
				return g;
			}
		}
		return g;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch, Assignment astNode) {
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
			ArrayList<EGroumActionNode> rets = pdg.getReturns();
			if (rets.size() > 0) {
				for (EGroumActionNode ret : rets) {
					ret.astNodeType = ASTNode.ASSIGNMENT;
					ret.name = "=";
					pdg.extend(ret, new EGroumDataNode(lnode), Type.DEFINITION);
				}
				pdg.nodes.addAll(lg.nodes);
				pdg.statementNodes.addAll(lg.statementNodes);
				lg.dataSources.remove(lnode);
				pdg.dataSources.addAll(lg.dataSources);
				pdg.statementSources.addAll(lg.statementSources);
				lg.clear();
				return pdg;
			}
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
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumNode node = new EGroumActionNode(control, branch,
				astNode, astNode.getNodeType(), null, null, "assert");
		pdg.mergeSequentialData(node, Type.PARAMETER);
		// skip astNode.getMessage()
		return pdg;
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

	private void delete(EGroumNode node) {
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
		if (qual != null)
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
			pdg.mergeSequentialData(new EGroumDataNode(null, def.astNodeType, def.key,
					((EGroumDataNode) def).dataType, ((EGroumDataNode) def).dataName,
					def.isField, false), Type.REFERENCE);
			return pdg;
		}
		ArrayList<EGroumActionNode> rets = pdg.getReturns();
		if (rets.size() > 0) {
			EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
					EGroumNode.PREFIX_DUMMY + exp.getStartPosition() + "_"
							+ exp.getLength(), rets.get(0).dataType, EGroumNode.PREFIX_DUMMY, false, true);
			for (EGroumActionNode ret : rets) {
				ret.astNodeType = ASTNode.ASSIGNMENT;
				ret.name = "=";
				pdg.extend(ret, new EGroumDataNode(dummy), Type.DEFINITION);
			}
			pdg.mergeSequentialData(new EGroumDataNode(null, dummy.astNodeType,
					dummy.key, dummy.dataType, dummy.dataName), Type.REFERENCE);
			return pdg;
		}
		EGroumNode node = pdg.getOnlyOut();
		if (node instanceof EGroumDataNode)
			return pdg;
		EGroumDataNode dummy = new EGroumDataNode(null, ASTNode.SIMPLE_NAME,
				EGroumNode.PREFIX_DUMMY + exp.getStartPosition() + "_"
						+ exp.getLength(), node.dataType, EGroumNode.PREFIX_DUMMY, false, true);
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
		EGroumNode node = new EGroumActionNode(control, branch, astNode, astNode.getNodeType(), null, type + "[.]", "[.]");
		ag.mergeSequentialData(node, Type.RECEIVER);
		EGroumGraph ig = buildArgumentPDG(control, branch, astNode.getIndex());
		ig.mergeSequentialData(node, Type.PARAMETER);
		EGroumGraph pdg = new EGroumGraph(context);
		pdg.mergeBranches(ag, ig);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			FieldAccess astNode) {
		EGroumGraph pdg = buildArgumentPDG(control, branch,
				astNode.getExpression());
		EGroumDataNode node = pdg.getOnlyDataOut();
		pdg.mergeSequentialData(
				new EGroumDataNode(astNode, astNode.getNodeType(), astNode.toString(),
						node.dataType + "." + astNode.getName().getIdentifier(),
						astNode.getName().getIdentifier(), true, false), Type.QUALIFIER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			SuperFieldAccess astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				null, ASTNode.THIS_EXPRESSION, "this", "super", "super"));
		pdg.mergeSequentialData(
				new EGroumDataNode(astNode, ASTNode.FIELD_ACCESS, astNode.toString(),
						"super." + astNode.getName().getIdentifier(),
						astNode.getName().getIdentifier(), true, false), Type.QUALIFIER);
		return pdg;
	}

	private EGroumGraph buildPDG(EGroumNode control, String branch,
			ThisExpression astNode) {
		EGroumGraph pdg = new EGroumGraph(context, new EGroumDataNode(
				astNode, astNode.getNodeType(), "this", "this",
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
			new EGroumDataEdge(node, next, type);
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

	private void extend(EGroumNode ret, EGroumDataNode node, Type type) {
		HashSet<EGroumDataNode> ns = new HashSet<>();
		ns.add((EGroumDataNode) node);
		defStore.put(node.key, ns);
		nodes.add(node);
		sinks.remove(ret);
		sinks.add(node);
		new EGroumDataEdge(ret, node, type);
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

	private void adjustReturnNodes() {
		for (EGroumNode ret : returns)
			delete(ret);
		returns.clear();
		sinks.clear();
		statementSinks.clear();
		statementNodes.remove(entryNode);
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

	public void buildClosure() {
		HashSet<EGroumNode> doneNodes = new HashSet<EGroumNode>();
		for (EGroumNode node : nodes)
			if (!doneNodes.contains(node))
				buildDataClosure(node, doneNodes);
		buildSequentialClosure();
		doneNodes.clear();
		pruneTemporaryDataDependence();
		doneNodes = new HashSet<EGroumNode>();
		for (EGroumNode node : nodes)
			if (!doneNodes.contains(node))
				buildControlClosure(node, doneNodes);
	}

	private void buildSequentialClosure() {
		HashMap<EGroumNode, HashSet<EGroumNode>> preNodesOfNode = new HashMap<>();
		preNodesOfNode.put(entryNode, new HashSet<EGroumNode>());
		HashSet<EGroumNode> doneNodes = new HashSet<>();
		doneNodes.add(entryNode);
		for (EGroumNode node : nodes) {
			if (!doneNodes.contains(node))
				buildSequentialClosure(node, doneNodes, preNodesOfNode);
		}
		for (EGroumNode node : preNodesOfNode.keySet()) {
			if (node.isCoreAction()) {
				for (EGroumNode preNode : preNodesOfNode.get(node)) {
					if (preNode.isCoreAction() && !node.hasInNode(preNode) && ((EGroumActionNode) node).hasBackwardDataDependence((EGroumActionNode) preNode)) {
						HashSet<EGroumNode> preNodes = new HashSet<>(preNodesOfNode.get(preNode));
						boolean inDifferentCatches = false;
						HashSet<EGroumNode> cns = node.getCatchClauses();
						for (EGroumNode cn : cns) {
							for (EGroumEdge e : cn.inEdges) {
								if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.CONDITION) {
									EGroumNode en = e.source.getDefinition();
									if (en == null) continue;
									for (EGroumEdge e1 : en.inEdges) {
										if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type == Type.THROW) {
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
						if (!inDifferentCatches)
							new EGroumDataEdge(preNode, node, Type.ORDER);
					}
				}
			}
		}
	}

	private void buildSequentialClosure(EGroumNode node, HashSet<EGroumNode> doneNodes, HashMap<EGroumNode, HashSet<EGroumNode>> preNodesOfNode) {
		HashSet<EGroumNode> preNodes = new HashSet<>();
		for (EGroumEdge e : node.inEdges) {
			preNodes.add(e.source);
			if (!doneNodes.contains(e.source))
				buildSequentialClosure(e.source, doneNodes, preNodesOfNode);
			preNodes.addAll(preNodesOfNode.get(e.source));
		}
		preNodesOfNode.put(node, preNodes);
		doneNodes.add(node);
	}

	private void buildDataClosure(EGroumNode node, HashSet<EGroumNode> doneNodes) {
		if (node.getDefinitions().isEmpty()) {
			for (EGroumEdge e : new HashSet<EGroumEdge>(node.getInEdges())) {
				if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type != Type.THROW && ((EGroumDataEdge) e).type != Type.DEPENDENCE) {
					String label = e.getLabel();
					EGroumDataEdge de = (EGroumDataEdge) e;
					ArrayList<EGroumNode> inNodes = e.source.getDefinitions();
					if (inNodes.isEmpty())
						inNodes.add(e.source);
					else
						for (EGroumNode inNode : inNodes)
							if (!node.hasInEdge(inNode, label))
								new EGroumDataEdge(inNode, node, de.type);
					for (EGroumNode inNode : inNodes) {
						if (!doneNodes.contains(inNode))
							buildDataClosure(inNode, doneNodes);
						for (EGroumEdge e1 : inNode.inEdges) {
							if (e1 instanceof EGroumDataEdge && ((EGroumDataEdge) e1).type != Type.DEPENDENCE && ((EGroumDataEdge) e1).type != Type.THROW && !(e1.source instanceof EGroumDataNode)) {
								if (!node.hasInEdge(e1.source, label))
									new EGroumDataEdge(e1.source, node, de.type);
							}
						}
					}
				}
			}
		}
		doneNodes.add(node);
	}

	private void buildControlClosure(EGroumNode node, HashSet<EGroumNode> doneNodes) {
		for (EGroumEdge e : new HashSet<EGroumEdge>(node.getInEdges())) {
			if (e instanceof EGroumControlEdge) {
				EGroumNode inNode = e.getSource();
				if (!doneNodes.contains(inNode))
					buildControlClosure(inNode, doneNodes);
				for (EGroumEdge e1 : inNode.getInEdges()) {
					if ((node instanceof EGroumActionNode || node instanceof EGroumControlNode) && !node.hasInEdge(e1)) {
						if (e1 instanceof EGroumControlEdge)
							new EGroumControlEdge(e1.source, node, ((EGroumControlEdge) e1).label);
						else if (e1 instanceof EGroumDataEdge) {
							if (node instanceof EGroumActionNode) { 
								if (((EGroumActionNode) node).hasBackwardDataDependence(e1.source))
									new EGroumDataEdge(e1.source, node, ((EGroumDataEdge) e1).type);
							}
							else
								new EGroumDataEdge(e1.source, node, ((EGroumDataEdge) e1).type);
						}
					}
				}
			}
		}
		doneNodes.add(node);
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

	@SuppressWarnings("unused")
	private void pruneIsolatedNodes() {
		HashSet<EGroumNode> isoNodes = new HashSet<EGroumNode>(nodes);
		isoNodes.removeAll(getReachableNodes());
		for (EGroumNode node : isoNodes)
			delete(node);
	}

	private HashSet<EGroumNode> getReachableNodes() {
		HashSet<EGroumNode> reachableNodes = new HashSet<>();
		Stack<EGroumNode> stk = new Stack<>();
		stk.push(entryNode);
		while (!stk.isEmpty()) {
			EGroumNode node = stk.pop();
			reachableNodes.add(node);
			for (EGroumEdge e : node.getOutEdges())
				if (!reachableNodes.contains(e.getTarget()))
					stk.push(e.getTarget());
			for (EGroumEdge e : node.getInEdges())
				if (!reachableNodes.contains(e.getSource()))
					stk.push(e.getSource());
		}
		return reachableNodes;
	}

	private void pruneDataNodes() {
		//pruneDefNodes();
		pruneDummyNodes();
	}

	private void pruneDummyNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node instanceof EGroumDataNode) {
				EGroumDataNode dn = (EGroumDataNode) node;
				if (dn.isDummy() && dn.isDefinition() && dn.outEdges.size() <= 1) {
					EGroumNode a = dn.inEdges.get(0).source;
					EGroumNode s = a.inEdges.get(0).source;
					if (s instanceof EGroumDataNode) {
						EGroumNode dr = dn.outEdges.get(0).target;
						if (dr.inEdges.size() == 1) {
							EGroumDataEdge e = (EGroumDataEdge) dr.outEdges.get(0);
							new EGroumDataEdge(s, e.target, e.type);
							delete(dr);
							delete(dn);
							delete(a);
						}
					}
				}
			}
		}
	}

	private void deleteTemporaryDataNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node.isDefinition()) {
				EGroumDataNode dn = (EGroumDataNode) node;
				ArrayList<EGroumNode> refs = dn.getReferences();
				if (refs.size() == 1 && refs.get(0).getDefinitions().size() == 1) {
					delete(dn);
					delete(refs.get(0));
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
							if (!node.outEdges.isEmpty()) {
								EGroumDataEdge out = (EGroumDataEdge) node.outEdges.get(0);
								if (!in.source.hasOutNode(out.target))
									new EGroumDataEdge(in.source, out.target, out.type);
							}
						}
					}
				}
				if (isRef) {
					node.outEdges.clear();
					delete(node);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void pruneDefNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node.isDefinition() && ((EGroumDataNode) node).isDeclaration
					&& node.outEdges.isEmpty()) {
				for (EGroumEdge e1 : new HashSet<EGroumEdge>(node.inEdges)) {
					for (EGroumEdge e2 : new HashSet<EGroumEdge>(e1.source.inEdges)) {
						if (e2 instanceof EGroumDataEdge
								&& ((EGroumDataEdge) e2).type == Type.PARAMETER)
							delete(e2.source);
					}
					delete(e1.source);
				}
				delete(node);
			}
		}
	}

	private void pruneEmptyStatementNodes() {
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
		HashMap<String, EGroumNode> defs = new HashMap<>();
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node instanceof EGroumDataNode && !node.isLiteral() && !node.isDefinition() && !((EGroumDataNode) node).isException())
				addDefinitions((EGroumDataNode) node, defs);
	}

	private void addDefinitions(EGroumDataNode node, HashMap<String, EGroumNode> defs) {
		if (node.getDefinitions().isEmpty()) {
			EGroumNode def = defs.get(node.key);
			if (def == null) {
				def = new EGroumDataNode(null, node.astNodeType, node.key, node.dataType, node.dataName, true, true);
				defs.put(node.key, def);
				nodes.add(def);
			}
			new EGroumDataEdge(def, node, Type.REFERENCE);
			for (EGroumEdge e : node.outEdges)
				if (!def.hasOutNode(e.getTarget()))
					new EGroumDataEdge(def, e.getTarget(), ((EGroumDataEdge) e).type);
		}
	}

	public void cleanUp() {
		clearDefStore();
		for (EGroumNode node : new HashSet<EGroumNode>(nodes)) {
			if (node instanceof EGroumEntryNode || node instanceof EGroumControlNode)
				delete(node);
			node.astNode = null;
		}
	}

	public void deleteAssignmentNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node.isAssignment()) {
				for (EGroumEdge ie : node.getInEdges()) {
					if (ie.isParameter()) {
						EGroumNode n = ie.source;
						for (EGroumEdge oe : node.getOutEdges())
							if ((/*n instanceof EGroumDataNode || */oe.isDef()) && !n.getOutNodes().contains(oe.target))
								new EGroumDataEdge(n, oe.target, ((EGroumDataEdge) oe).type); // shortcut definition edges before deleting this assignment
					}
				}
				delete(node);
			}
	}

	public void deleteUnaryOperationNodes() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node.astNodeType == ASTNode.PREFIX_EXPRESSION || node.astNodeType == ASTNode.POSTFIX_EXPRESSION)
				node.delete();
	}

	@SuppressWarnings("unused")
	private void deleteAssignmentEdges() {
		for (EGroumNode node : new HashSet<EGroumNode>(nodes))
			if (node.isAssignment()) {
				for (EGroumEdge e : new HashSet<EGroumEdge>(node.getOutEdges()))
					if (!e.isDef())
						e.delete();
				if (node.getOutEdges().isEmpty())
					node.delete();
			}
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
				if (lits.size() > 1) {
					((EGroumDataNode) lits.get(1)).dataName = label + "*";
					for (int i = 2; i < lits.size(); i++)
						lits.get(i).delete();
				}
			}
		}
	}

	public void toGraphics(String path){
		DotGraph graph = new DotGraph(this);
		graph.toDotFile(new File(path + "/" + name + ".dot"));
		graph.toGraphics(path + "/" + name, "png");
	}
}
