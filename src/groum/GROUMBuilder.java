package groum;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import utils.FileIO;

public class GROUMBuilder {
	private ArrayList<GROUMGraph> groums = new ArrayList<GROUMGraph>();
	private String path;
	private int numMethod = 0;
	private int maxGroumSize = 0;
	private int totalGroumSize = 0;

	private HashMap<ASTNode,ArrayList<ASTNode>> mapTree;
	private String className;
	private int fileID;
	private ArrayList<Integer> lines;
	
	private HashMap<Integer, HashSet<Integer>> dataDependencies = new HashMap<Integer, HashSet<Integer>>();

	public GROUMBuilder(String path)
	{
		this.path = path;
	}
	
	public void build()
	{
		build(new File(path));
	}
	
	private void build(File file)
	{
		if (file.isDirectory())
		{
			for (File sub : file.listFiles())
				build(sub);
		}
		else if (file.isFile() && file.getName().endsWith(".java"))
		{
			String content = FileIO.readStringFromFile(file.getAbsolutePath());
			fileID = GROUMNode.fileNames.size();
			GROUMNode.fileNames.add(file.getAbsolutePath());
			lines = new ArrayList<Integer>();
			int charCount = -1;
	    	do
	    	{
	    		charCount++;
	    		lines.add(charCount);
	    		charCount = content.indexOf("\n", charCount);
	    	} while (charCount > -1);
			
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(content.toCharArray());
			//parser.setBindingsRecovery(true);
			//parser.setResolveBindings(true);
			//parser.setStatementsRecovery(true);
			//parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
			ASTNode ast = null; 
			try {
				ast = parser.createAST(null);
			}
			catch (Exception e) {
				System.err.println("Error parsing " + file);
				//e.printStackTrace();
				return;
			}
			/*
			 * Traverse through the tree to build graph
			 */
			
			ASTNode root = ast.getRoot();
			//BINDING TYPE HERE
			ASTFieldVisitor fieldVisitor = new ASTFieldVisitor();
			root.accept(fieldVisitor);
			mapTree = fieldVisitor.getMapTree();
			ASTTypeBindingVisitor nodeTypeVisitor = new ASTTypeBindingVisitor(fieldVisitor.getFieldType());
			root.accept(nodeTypeVisitor);
			traverseMethodOnly(root);
			/*for (GROUMGraph graph:groums) {
				System.out.println(graph);
			}*/
		}
	}

	private void traverseMethodOnly(ASTNode root) {
		if(root.getNodeType() == ASTNode.TYPE_DECLARATION)
		{
			TypeDeclaration node = (TypeDeclaration)root;
			if(node.getParent().getNodeType() == ASTNode.COMPILATION_UNIT)
				className = node.getName().toString();
		}
		if (root.getNodeType() == ASTNode.METHOD_DECLARATION){
			GROUMGraph aGraph = traverse(null, root);
			aGraph.setFileID(fileID);
			MethodDeclaration method = (MethodDeclaration)root;
			aGraph.setName(className + "." + method.getName().getIdentifier());
			aGraph.prune();
			aGraph.addDataDependency();
			aGraph.removeNonDependents();
			aGraph.cleanUp();
			if(aGraph.getNodes().size() > 0)
			{
				aGraph.setId(groums.size()+1);
				groums.add(aGraph);
				if(maxGroumSize < aGraph.getNodes().size())
					maxGroumSize = aGraph.getNodes().size();
				totalGroumSize += aGraph.getNodes().size();
			}
			numMethod++;
		}
		else {
			ArrayList<ASTNode> children = mapTree.get(root);	
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				traverseMethodOnly(child);
			}
		}
	}


	public ArrayList<GROUMGraph> getGroums() {
		return groums;
	}

	/*
	 * Give a root of tree build a CFG
	 * by first build a graph for root 
	 * and merge this graph to graphs of its child sequentially. 
	 */
	private GROUMGraph traverse(GROUMNode cfgNode, ASTNode root) {
		GROUMGraph graph = new GROUMGraph();
		
		/*if(root.getProperty("TypeBinding") == null && 
				(root.getNodeType() == ASTNode.METHOD_INVOCATION || 
						root.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION ||
						root.getNodeType() == ASTNode.FIELD_ACCESS
				))
			return graph;*/
		
		GROUMNode branch = null;

		ArrayList<ASTNode> children = mapTree.get(root);	
		
		//go through all children
		String label;
		GROUMNode aNode = null;
		HashSet<Integer> parameters;
		
		switch (root.getNodeType()){

		/*
		 * for if statement, the tree structure like this:
		 * if_node -> infixExpression -> block -> block  
		 */
		/*case ASTNode.TYPE_DECLARATION:
			return graph;
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
			return graph;	*/

		case ASTNode.CLASS_INSTANCE_CREATION:
			ClassInstanceCreation nNode = (ClassInstanceCreation) root;

			for (int i = 0; i < children.size(); i++){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			HashSet<ASTNode> paraExprs = getParameters(nNode.arguments());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			/*for(int i = 0; i < nNode.arguments().size(); i++)
			{
				//if(nNode.arguments().get(i).getClass().getSimpleName().equalsIgnoreCase("SimpleName")) {
					int id = GROUMNode.convertLabel(nNode.arguments().get(i).toString());
					parameters.add(id);
				//}	
			}*/
			String nObjectname;
			if(nNode.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
			{
				VariableDeclarationFragment pNode = (VariableDeclarationFragment)(nNode.getParent());
				nObjectname = pNode.getName().toString();
				parameters.add(GROUMNode.convertLabel(nObjectname));
			}
			else if(nNode.getParent().getNodeType() == ASTNode.ASSIGNMENT)
			{
				Assignment pNode = (Assignment)(nNode.getParent());
				Expression ex = pNode.getLeftHandSide();
				if(ex.getNodeType() == ASTNode.FIELD_ACCESS)
				{
					FieldAccess fa = (FieldAccess)ex;
					nObjectname = fa.getName().toString();
				}
				else
					nObjectname = ex.toString();
				parameters.add(GROUMNode.convertLabel(ex.toString()));
			}
			else
			{
				//nObjectname = nNode.getProperty("TypeBinding").toString();
				nObjectname = "anonymous";
				//parameters.add(GROUMNode.convertLabel(nObjectname));
			}
			aNode = new GROUMNode("<init>", GROUMNode.TYPE_METHOD, nNode.getProperty("TypeBinding").toString(), 
					nObjectname, parameters);
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength()-1, lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);
			
			return graph;
			
		/*case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			SuperConstructorInvocation sciNode = (SuperConstructorInvocation)root;
			paraExprs = getParameters(sciNode.arguments());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			parameters.add(GROUMNode.convertLabel("super"));
			aNode = new GROUMNode("<init>",
					GROUMNode.TYPE_METHOD, sciNode.getProperty("TypeBinding").toString(),
					"super", parameters);
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);

			return graph;*/

		case ASTNode.CONSTRUCTOR_INVOCATION:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			ConstructorInvocation ciNode = (ConstructorInvocation)root;
			paraExprs = getParameters(ciNode.arguments());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			/*for(int i = 0; i < ciNode.arguments().size(); i++)
			{
				//if(mNode.arguments().get(i).getClass().getSimpleName().equalsIgnoreCase("SimpleName")) 
					parameters.add(GROUMNode.convertLabel(ciNode.arguments().get(i).toString()));
			}*/
			parameters.add(GROUMNode.convertLabel("this"));
			aNode = new GROUMNode("<init>",
					GROUMNode.TYPE_METHOD, ciNode.getProperty("TypeBinding").toString(),
					"this", parameters);
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);

			return graph;

		case ASTNode.CAST_EXPRESSION:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}
			parameters = new HashSet<Integer>();
			CastExpression cNode = (CastExpression)root;
			parameters.add(GROUMNode.convertLabel(cNode.getExpression().toString()));
			ASTNode leftHandSide = getLeftHandSide(root);
			if (leftHandSide != null)
				parameters.add(GROUMNode.convertLabel(leftHandSide.toString()));
			/*if(root.getParent().getNodeType() == ASTNode.ASSIGNMENT)
			{
				Assignment pNode = (Assignment)root.getParent();
				parameters.add(GROUMNode.convertLabel(pNode.getLeftHandSide().toString()));
			}
			else if(root.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
			{
				VariableDeclarationFragment pNode = (VariableDeclarationFragment)(root.getParent());
				parameters.add(GROUMNode.convertLabel(pNode.getName().toString()));
			}*/
			aNode = new GROUMNode("cast", GROUMNode.TYPE_METHOD, root.getProperty("TypeBinding").toString(), 
					cNode.getExpression().toString(), parameters);
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength()-1, lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);
			
			return graph;
			
		case ASTNode.INSTANCEOF_EXPRESSION:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}
			parameters = new HashSet<Integer>();
			InstanceofExpression ieNode = (InstanceofExpression)root;
			parameters.add(GROUMNode.convertLabel(ieNode.getLeftOperand().toString()));
			leftHandSide = getLeftHandSide(root);
			if (leftHandSide != null)
				parameters.add(GROUMNode.convertLabel(leftHandSide.toString()));
			/*if(root.getParent().getNodeType() == ASTNode.ASSIGNMENT)
			{
				Assignment pNode = (Assignment)root.getParent();
				parameters.add(GROUMNode.convertLabel(pNode.getLeftHandSide().toString()));
			}
			else if(root.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
			{
				VariableDeclarationFragment pNode = (VariableDeclarationFragment)(root.getParent());
				parameters.add(GROUMNode.convertLabel(pNode.getName().toString()));
			}*/
			aNode = new GROUMNode("instance", GROUMNode.TYPE_METHOD, root.getProperty("TypeBinding").toString(), 
					ieNode.getLeftOperand().toString(), parameters);
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength()-1, lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);
			
			return graph;
			
		/*case ASTNode.FIELD_ACCESS:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			FieldAccess fNode = (FieldAccess) root;
			//CFGNode Index#LabelField#Type#ClassName#Object Name#
			aNode = new CFGNode(index, fNode.getName().toString(),
					CFGNode.TYPE_FIELD, fNode.getProperty("TypeBinding").toString(),
					fNode.getExpression().toString());
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getIndex() + ".");
			graph.mergeSeq(aNode);

			//check whether there is an edge from root to node			
			return graph;

		case ASTNode.SUPER_FIELD_ACCESS:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			SuperFieldAccess sfNode = (SuperFieldAccess) root;
			//CFGNode Index#LabelField#Type#ClassName#Object Name#
			aNode = new CFGNode(index, sfNode.getName().toString(),
					CFGNode.TYPE_FIELD, sfNode.getProperty("TypeBinding").toString(),
					"super");
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getIndex() + ".");
			graph.mergeSeq(aNode);

			//check whether there is an edge from root to node			
			return graph;

		case ASTNode.SIMPLE_NAME:
			if(root.getProperty("TypeBinding") != null)
			{
				String[] snField = root.getProperty("TypeBinding").toString().split("\\.");
				//CFGNode Index#LabelField#Type#ClassName#Object Name#
				aNode = new CFGNode(index, root.toString(),
						CFGNode.TYPE_FIELD, snField[0],
						snField[1]);
				aNode.setFileID(this.fileID);
				aNode.setStartLine(binSearch(root.getStartPosition(), lines));
				aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
				aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getIndex() + ".");
				graph.mergeSeq(aNode);
	
				//check whether there is an edge from root to node
			}
			return graph;
			*/
		case ASTNode.METHOD_INVOCATION:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			MethodInvocation mNode = (MethodInvocation) root;
			String ex;
			if(mNode.getExpression() != null)
			{
				ex = mNode.getExpression().toString(); 
				if(ex.contains("System.") || ex.contains("java."))
					return graph;
			}
			
			paraExprs = getParameters(mNode.arguments());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			/*for(int i = 0; i < mNode.arguments().size(); i++)
			{
				//if(mNode.arguments().get(i).getClass().getSimpleName().equalsIgnoreCase("SimpleName")) 
					parameters.add(GROUMNode.convertLabel(mNode.arguments().get(i).toString()));
			}*/
			leftHandSide = getLeftHandSide(root);
			if (leftHandSide != null)
				parameters.add(GROUMNode.convertLabel(leftHandSide.toString()));
			/*if(root.getParent().getNodeType() == ASTNode.ASSIGNMENT)
			{
				Assignment pNode = (Assignment)root.getParent();
				parameters.add(GROUMNode.convertLabel(pNode.getLeftHandSide().toString()));
			}
			else if(root.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
			{
				VariableDeclarationFragment pNode = (VariableDeclarationFragment)(root.getParent());
				parameters.add(GROUMNode.convertLabel(pNode.getName().toString()));
			}*/
			
			/*for(int i = 2; i < children.size(); i++)
			{
				ASTNode child = children.get(i);
				if(child.getNodeType() == ASTNode.SIMPLE_NAME)
					parameters.add(CFGNode.idOfLabel.get(child.toString()));
			}*/
			if (mNode.getExpression() != null){
				parameters.add(GROUMNode.convertLabel(mNode.getExpression().toString()));
				if(children.get(0).getNodeType() == ASTNode.FIELD_ACCESS)
				{
					FieldAccess faNode = (FieldAccess)children.get(0);
					aNode = new GROUMNode(mNode.getName().toString(),
							GROUMNode.TYPE_METHOD, faNode.getProperty("TypeBinding").toString(),
							faNode.getName().toString(), parameters);
				}
				else {
					aNode = new GROUMNode(mNode.getName().toString(),
							GROUMNode.TYPE_METHOD, mNode.getProperty("TypeBinding").toString(),
							mNode.getExpression().toString(), parameters);
				}
			}
			else {
				parameters.add(GROUMNode.convertLabel("this"));
				aNode = new GROUMNode(mNode.getName().toString(),
						GROUMNode.TYPE_METHOD, className,
						"this", parameters);
			}
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);

			return graph;

		/*case ASTNode.SUPER_METHOD_INVOCATION:
			for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}

			SuperMethodInvocation smNode = (SuperMethodInvocation) root;
			parameters = new HashSet<Integer>();
			for(int i = 0; i < smNode.arguments().size(); i++)
			{
				//if(mNode.arguments().get(i).getClass().getSimpleName().equalsIgnoreCase("SimpleName")) 
					parameters.add(GROUMNode.convertLabel(smNode.arguments().get(i).toString()));
			}
			leftHandSide = getLeftHandSide(root);
			if (leftHandSide != null)
				parameters.add(GROUMNode.convertLabel(leftHandSide.toString()));
			parameters.add(GROUMNode.convertLabel("super"));
			aNode = new GROUMNode(smNode.getName().toString(),
					GROUMNode.TYPE_METHOD, smNode.getProperty("TypeBinding").toString(),
					"super", parameters);
			aNode.setFileID(this.fileID);
			aNode.setStartLine(binSearch(root.getStartPosition(), lines));
			aNode.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			graph.mergeSeq(aNode);

			return graph;*/

		case ASTNode.IF_STATEMENT://branch 
			IfStatement iNode = (IfStatement) root;
			label = "CONTROL";
			paraExprs = getParameters(iNode.getExpression());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("IF", GROUMNode.TYPE_CONTROL, label, "IF", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			/*
			 * assumption node 0 is conditional node 				
			 */
			if (iNode.getExpression() != null){
				graph.mergeSeq(traverse(cfgNode, iNode.getExpression()));
			}

			graph.mergeSeq(branch);

			graph.mergeABranch(traverse(branch, iNode.getThenStatement()), branch);

			if (iNode.getElseStatement() != null){
				graph.mergeABranch(traverse(branch, iNode.getElseStatement()), branch);
				graph.getOuts().remove(branch);
			}

			return graph;

		case ASTNode.CONDITIONAL_EXPRESSION://branch 
			ConditionalExpression ceNode = (ConditionalExpression) root;
			label = "CONTROL";
			paraExprs = getParameters(ceNode.getExpression());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("IF", GROUMNode.TYPE_CONTROL, label, "IF", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			/*
			 * assumption node 0 is conditional node 				
			 */
			if (ceNode.getExpression() != null){
				graph.mergeSeq(traverse(cfgNode, ceNode.getExpression()));
			}

			graph.mergeSeq(branch);

			graph.mergeABranch(traverse(branch, ceNode.getThenExpression()), branch);

			if (ceNode.getElseExpression() != null){
				graph.mergeABranch(traverse(branch, ceNode.getElseExpression()), branch);
				graph.getOuts().remove(branch);
			}

			return graph;

		case ASTNode.FOR_STATEMENT:
			ForStatement forNode = (ForStatement) root;
			label = "CONTROL";
			paraExprs = getParameters(forNode.initializers());
			paraExprs.addAll(getParameters(forNode.getExpression()));
			paraExprs.addAll(getParameters(forNode.updaters()));
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("FOR", GROUMNode.TYPE_CONTROL, label, "FOR", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			
			/*if (forNode.getExpression() != null){
				graph.mergeSeq(traverse(cfgNode, forNode.getExpression()));
			}*/
			for (int i = 0; i <= children.size()-2; i++){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}
			
			graph.mergeSeq(branch);

			graph.mergeABranch(traverse(branch, forNode.getBody()), branch);

			return graph;

		case ASTNode.ENHANCED_FOR_STATEMENT:
			EnhancedForStatement eforNode = (EnhancedForStatement) root;
			label = "CONTROL";
			paraExprs = getParameters(eforNode.getParameter());
			paraExprs.addAll(getParameters(eforNode.getExpression()));
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("FOR", GROUMNode.TYPE_CONTROL, label, "FOR", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			
			if (eforNode.getExpression() != null){
				graph.mergeSeq(traverse(cfgNode, eforNode.getExpression()));
			}

			graph.mergeSeq(branch);

			graph.mergeABranch(traverse(branch, eforNode.getBody()), branch);

			return graph;

		case ASTNode.WHILE_STATEMENT:
			WhileStatement whileNode = (WhileStatement) root;
			label = "CONTROL";
			paraExprs = getParameters(whileNode.getExpression());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("WHILE", GROUMNode.TYPE_CONTROL, label, "WHILE", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			
			if (whileNode.getExpression() != null){
				graph.mergeSeq(traverse(cfgNode, whileNode.getExpression()));
			}

			graph.mergeSeq(branch);

			graph.mergeABranch(traverse(branch, whileNode.getBody()), branch);
			return graph;

		case ASTNode.DO_STATEMENT:
			DoStatement doNode = (DoStatement) root;
			label = "CONTROL";
			paraExprs = getParameters(doNode.getExpression());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("DOWHILE", GROUMNode.TYPE_CONTROL, label, "DOWHILE", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			
			graph.mergeSeq(branch);
			
			graph.mergeABranch(traverse(branch, doNode.getBody()), branch);
			
			if (doNode.getExpression() != null)
				graph.mergeABranch(traverse(branch, doNode.getExpression()), branch);

			return graph;

		case ASTNode.SWITCH_STATEMENT://branch
			SwitchStatement sNode = (SwitchStatement) root;
			label = "CONTROL";
			paraExprs = getParameters(sNode.getExpression());
			parameters = getIds(paraExprs);
			getDependents(parameters);
			branch = new GROUMNode("SWITCH", GROUMNode.TYPE_CONTROL, label, "SWITCH", parameters);
			branch.setFileID(this.fileID);
			branch.setStartLine(binSearch(root.getStartPosition(), lines));
			branch.setEndLine(binSearch(root.getStartPosition()+root.getLength(), lines));
			branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
			
			graph.mergeSeq(traverse(cfgNode, sNode.getExpression()));

			graph.mergeSeq(branch);

			for (int i = 1;i < children.size();i++){
				ASTNode child = children.get(i);
				graph.mergeABranch(traverse(branch, child),branch);
			}

			//remove branching node from outs node
			graph.getOuts().remove(branch);
			return graph;
		
		case ASTNode.TRY_STATEMENT://branch 
			TryStatement tNode = (TryStatement) root;
			if (tNode.getBody() != null){
				graph.mergeSeq(traverse(cfgNode, tNode.getBody()));
			}

			if (tNode.catchClauses() != null && !tNode.catchClauses().isEmpty())
			{
				HashSet<GROUMNode> saveOuts = new HashSet<GROUMNode>();
				saveOuts.addAll(graph.getOuts());
				for (int i = 0; i < tNode.catchClauses().size(); i++)
				{
					CatchClause clause = (CatchClause)(tNode.catchClauses().get(i));
					graph.mergeBranches(traverse(branch, clause), saveOuts);
				}
				graph.getOuts().removeAll(saveOuts);
			}
			if (tNode.getFinally() != null){
				graph.mergeSeq(traverse(cfgNode, tNode.getFinally()));
			}

			return graph;

		case ASTNode.INFIX_EXPRESSION://branch
			HashSet<GROUMNode> saveOuts = new HashSet<GROUMNode>();
			saveOuts.addAll(graph.getOuts());
			for (int i = children.size() -1; i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeBranches(traverse(cfgNode, child),saveOuts);
			}
			graph.getOuts().removeAll(saveOuts);
			return graph;


		/*
		 * assignement: left = rigt 
		 * execute right and left after that
		 */

		/*
		 *  a == b : a and b in the same level
		 */
		case ASTNode.ASSIGNMENT:
			Assignment asgnNode = (Assignment)root;
			/*for (int i = children.size()-1;i >= 0;i--){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}*/
			graph.mergeSeq(traverse(cfgNode, asgnNode.getRightHandSide()));
			dataDependencies.put(getId(asgnNode.getLeftHandSide()), getIds(getParameters(asgnNode.getRightHandSide())));
			return graph;
		
		case ASTNode.SINGLE_VARIABLE_DECLARATION:
			SingleVariableDeclaration svdNode = (SingleVariableDeclaration)root;
			if (svdNode.getInitializer() != null)
			{
				graph.mergeSeq(traverse(cfgNode, svdNode.getInitializer()));
			}
			dataDependencies.put(getId(svdNode.getName()), getIds(getParameters(svdNode.getInitializer())));
			return graph;
		
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
			VariableDeclarationFragment vdfNode = (VariableDeclarationFragment)root;
			if (vdfNode.getInitializer() != null)
			{
				graph.mergeSeq(traverse(cfgNode, vdfNode.getInitializer()));
			}
			dataDependencies.put(getId(vdfNode.getName()), getIds(getParameters(vdfNode.getInitializer())));
			return graph;
		
		default:
			for (int i = 0;i < children.size();i++){
				ASTNode child = children.get(i);
				graph.mergeSeq(traverse(cfgNode, child));
			}
		return graph;
		}
	}	
	/*
	 * 
	 */
	int binSearch(int x, ArrayList<Integer> al) {
		if (al.size() <= 1) return al.size()-1;
		int low = 0, high = al.size()-1, mid;
		while(low < high) {
			mid = (low + high) / 2;
            if(al.get(mid) < x)
                low = mid + 1;
            else if(al.get(mid) > x)
                high = mid - 1;
            else
                return mid;
        }
		if (al.get(low) > x) return low - 1;
		else return low;
	}
	
	private HashSet<ASTNode> getParameters(List arguments)
	{
		HashSet<ASTNode> parameters = new HashSet<ASTNode>();
		for (int i = 0; i < arguments.size(); i++)
		{
			ASTNode arg = (ASTNode) arguments.get(i);
			parameters.addAll(getParameters(arg));
		}
		
		return parameters;
	}
	private HashSet<ASTNode> getParameters(ASTNode arg) {
		HashSet<ASTNode> parameters = new HashSet<ASTNode>();
		if (arg != null)
		{
			/*if (arg.getNodeType() == ASTNode.INFIX_EXPRESSION)
			{
				InfixExpression expr = (InfixExpression)arg;
				parameters.addAll(getParameters(expr.getLeftOperand()));
				parameters.addAll(getParameters(expr.getRightOperand()));
			}
			else if (arg.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION)
			{
				ParenthesizedExpression expr = (ParenthesizedExpression)arg;
				parameters.addAll(getParameters(expr.getExpression()));
			}
			else if (arg.getNodeType() == ASTNode.PREFIX_EXPRESSION)
			{
				PrefixExpression expr = (PrefixExpression)arg;
				parameters.addAll(getParameters(expr.getOperand()));
			}
			else if (arg.getNodeType() == ASTNode.POSTFIX_EXPRESSION)
			{
				PostfixExpression expr = (PostfixExpression)arg;
				parameters.addAll(getParameters(expr.getOperand()));
			}
			else if (arg.getNodeType() == ASTNode.ASSIGNMENT)
			{
				Assignment expr = (Assignment)arg;
				parameters.addAll(getParameters(expr.getLeftHandSide()));
				parameters.addAll(getParameters(expr.getRightHandSide()));
			}
			else if (arg.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION)
			{
				VariableDeclarationExpression expr = (VariableDeclarationExpression)arg;
				parameters.addAll(getParameters(expr.fragments()));
			}
			else if (arg.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
			{
				VariableDeclarationFragment expr = (VariableDeclarationFragment)arg;
				parameters.add(expr.getName());
				parameters.addAll(getParameters(expr.getInitializer()));
			}
			else if (arg.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION)
			{
				SingleVariableDeclaration expr = (SingleVariableDeclaration)arg;
				parameters.add(expr.getName());
				parameters.addAll(getParameters(expr.getInitializer()));
			}*/
			if (arg.getNodeType() == ASTNode.QUALIFIED_NAME)
			{
				parameters.add(arg);
			}
			else if (arg.getNodeType() == ASTNode.METHOD_INVOCATION)
			{
				MethodInvocation expr = (MethodInvocation)arg;
				parameters.add(arg);
				parameters.addAll(getParameters(expr.getExpression()));
				parameters.addAll(getParameters(expr.arguments()));
			}
			else if (arg.getNodeType() == ASTNode.SIMPLE_NAME)
			{
				parameters.add(arg);
			}
			else
			{
				ArrayList<ASTNode> children = mapTree.get(arg);
				if (!children.isEmpty())
					parameters.addAll(getParameters(children));
			}
		}
		
		return parameters;
	}
	
	private int getId(Object obj)
	{
		return GROUMNode.convertLabel(obj.toString());
	}
	
	private HashSet<Integer> getIds(Set set)
	{
		HashSet<Integer> ids = new HashSet<Integer>();
		Iterator iter = set.iterator();
		while (iter.hasNext())
		{
			ids.add(getId(iter.next()));
		}
		
		return ids;
	}
	
	private void getDependents(HashSet<Integer> dataItems)
	{
		ArrayList<Integer> q = new ArrayList<Integer>(dataItems);
		while (!q.isEmpty())
		{
			int item = q.get(0);
			q.remove(0);
			if (dataDependencies.containsKey(item))
			{
				HashSet<Integer> dependents = new HashSet<Integer>(dataDependencies.get(item));
				dependents.removeAll(dataItems);
				q.addAll(dependents);
				dataItems.addAll(dependents);
			}
		}
	}
	private ASTNode getLeftHandSide(ASTNode node)
	{
		ASTNode p = node.getParent();
		while (p != null)
		{
			if (p.getNodeType() == ASTNode.ASSIGNMENT)
				return ((Assignment)p).getLeftHandSide();
			if (p.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)
				return ((VariableDeclarationFragment)p).getName();
			p = p.getParent();
		}
		return null;
	}
}
