package graphics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import groum.GROUMEdge;
import groum.GROUMGraph;
import groum.GROUMNode;

public class DotGraph {
	public static final String SHAPE_BOX = "box";
	public static final String SHAPE_DIAMOND = "diamond";
	public static final String SHAPE_ELLIPSE = "ellipse";
	public static final String COLOR_BLACK = "black";
	public static final String COLOR_RED = "red";
	public static final String STYLE_ROUNDED = "rounded";
	public static final String STYLE_DOTTED = "dotted";
	public static String EXEC_DOT = "D:/Program Files (x86)/Graphviz2.36/bin/dot.exe";	// Windows

	private StringBuilder graph = new StringBuilder();

	public DotGraph(StringBuilder sb) {
		this.graph = sb;
	}
	public DotGraph(GROUMGraph groum) {
		graph.append(addStart());

		HashMap<GROUMNode, Integer> ids = new HashMap<GROUMNode, Integer>();
		// add nodes
		int id = 0;
		for(GROUMNode node : groum.getNodes())
		{
			id++;
			ids.put(node, id);
			String label = node.getLabel();
			/*if(node.getType() == GROUMNode.TYPE_ENTRY)
				//graph.append(addNode(id, GROUMNode.labelOfID.get(node.getMethodID()), SHAPE_DIAMOND, null, null, null));
				graph.append(addNode(id, node.getLabel(), SHAPE_ELLIPSE, null, null, null));
			else */if(node.getType() == GROUMNode.TYPE_CONTROL)
				//graph.append(addNode(id, GROUMNode.labelOfID.get(node.getMethodID()), SHAPE_DIAMOND, null, null, null));
				graph.append(addNode(id, label, SHAPE_DIAMOND, null, null, null));
			/*else if(node.getType() == GROUMNode.TYPE_DATA)
				//graph.append(addNode(id, GROUMNode.labelOfID.get(node.getClassNameId()) + "." + node.getMethod(), SHAPE_BOX, STYLE_ROUNDED, null, null));
				graph.append(addNode(id, node.getLabel(), SHAPE_BOX, null, null, null));*/
			else if(node.getType() == GROUMNode.TYPE_METHOD)
				//graph.append(addNode(id, GROUMNode.labelOfID.get(node.getClassNameId()) + "." + node.getMethod(), SHAPE_BOX, STYLE_ROUNDED, null, null));
				graph.append(addNode(id, label, SHAPE_BOX, STYLE_ROUNDED, null, null));
			else
				//graph.append(addNode(id, GROUMNode.labelOfID.get(node.getMethodID()), SHAPE_DIAMOND, null, null, null));
				graph.append(addNode(id, label, SHAPE_ELLIPSE, null, null, null));
		}
		// add file name
		//String fileName = GROUMNode.fileNames.get(groum.getFileID());
		//graph.append(addNode(++id, fileName.replace('\\', '#'), DotGraph.STYLE_ROUNDED, null, null, null));
		// add edges
		for(GROUMNode node : groum.getNodes())
		{
			int sId = ids.get(node);
			for(GROUMEdge out : node.getOutEdges())
			{
				int eId = ids.get(out.getDest());
				graph.append(addEdge(sId, eId, null, null, null));
			}
		}

		graph.append(addEnd());
	}

	public String addStart()
	{
		return "digraph G {\n";
	}
	public String addNode(int id, String label, String shape, String style, String borderColor, String fontColor)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(id + " [label=\"" + label + "\"");
		if(shape != null && !shape.isEmpty())
			buf.append(" shape=" + shape);
		if(style != null && !style.isEmpty())
			buf.append(" style=" + style);
		if(borderColor != null && !borderColor.isEmpty())
			buf.append(" color=" + borderColor);
		if(fontColor != null && !fontColor.isEmpty())
			buf.append(" fontcolor=" + fontColor);
		buf.append("]\n");

		return buf.toString();
	}
	public String addEdge(int sId, int eId, String style, String color, String label)
	{
		StringBuffer buf = new StringBuffer();
		if(label == null)
			label = "";
		buf.append(sId + " -> " + eId + " [label=\"" + label + "\"");
		if(style != null && !style.isEmpty())
			buf.append(" style=" + style);
		if(color != null && !color.isEmpty())
			buf.append(" color=" + color);
		buf.append("];\n");

		return buf.toString();
	}
	public String addEnd()
	{
		return "}";
	}
	public String getGraph()
	{
		return this.graph.toString();
	}
	public void toDotFile(File file)
	{
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(file));
			fout.append(this.graph.toString());
			fout.flush();
			fout.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void toGraphics(String file, String type)
	{
		Runtime rt = Runtime.getRuntime();

		String[] args = {EXEC_DOT, "-T"+type, file+".dot", "-o", file+"."+type};
		try {
			Process p = rt.exec(args);
			p.waitFor();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
