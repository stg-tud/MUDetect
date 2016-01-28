/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import exas.ExasGraph;
import exas.ExasNode;

/**
 * @author hoan
 *
 */
public class GraphGenerator {
	public ExasGraph generate(int numOfNodes, int maxDegree)
	{
		Random r = new Random();
		ArrayList<ExasNode> nodes = new ArrayList<ExasNode>();
		for (int i = 0; i < numOfNodes; i++)
		{
			nodes.add(new ExasNode(String.valueOf(r.nextInt(numOfNodes))));
		}
		for (int j = 0; j < nodes.size(); j++)
		{
			ExasNode node = nodes.get(j);
			ArrayList<ExasNode> remain = new ArrayList<ExasNode>(nodes);
			remain.remove(j);
			for (int i = 0; i < maxDegree; i++)
			{
				int index = r.nextInt(remain.size());
				//if (r.nextBoolean())
				{
					if (r.nextBoolean())
					{
						if (!node.getIncomingNodes().contains(remain.get(index)))
						{
							node.getIncomingNodes().add(remain.get(index));
							remain.get(index).getOutgoingNodes().add(node);
						}
					}
					else
					{
						if (!node.getOutgoingNodes().contains(remain.get(index)))
						{
							node.getOutgoingNodes().add(remain.get(index));
							remain.get(index).getIncomingNodes().add(node);
						}
					}
					remain.remove(index);
				}
			}
		}
		return new ExasGraph(new HashSet<ExasNode>(nodes));
	}
	
	public void print(ExasGraph graph)
	{
		int numOfEdges = 0;
		for (ExasNode node : graph.getNodes())
		{
			System.out.println(node);
			System.out.println("\tIn: " + node.getIncomingNodes());
			System.out.println("\tOut: " + node.getOutgoingNodes());
			numOfEdges += node.getIncomingNodes().size();
			numOfEdges += node.getOutgoingNodes().size();
		}
		System.out.println(graph.getNodes().size() + " nodes " + (numOfEdges/2) + " edges");
	}
}
