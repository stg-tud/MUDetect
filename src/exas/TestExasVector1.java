/**
 * 
 */
package exas;

import java.util.HashSet;

/**
 * @author hoan
 *
 */
public class TestExasVector1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashSet<ExasNode> nodes = new HashSet<ExasNode>();
		ExasNode node1 = new ExasNode("1"), node2 = new ExasNode("1"), node3 = new ExasNode("3"), node4 = new ExasNode("2");
		nodes.add(node1);
		nodes.add(node2);
		nodes.add(node3);
		nodes.add(node4);
		node1.getIncomingNodes().add(node2); node1.getIncomingNodes().add(node3);
		node1.getOutgoingNodes().add(node2);
		node2.getIncomingNodes().add(node1); node2.getIncomingNodes().add(node3);
		node2.getOutgoingNodes().add(node1); node2.getOutgoingNodes().add(node4);
		node3.getIncomingNodes().add(node4);
		node3.getOutgoingNodes().add(node1); node3.getOutgoingNodes().add(node2); node3.getOutgoingNodes().add(node4);
		node4.getIncomingNodes().add(node2); node4.getIncomingNodes().add(node3);
		node4.getOutgoingNodes().add(node3);
		
		ExasGraph graph = new ExasGraph(nodes);
		graph.print();
		graph.buildVectors();
		graph.printVector();
	}

}
