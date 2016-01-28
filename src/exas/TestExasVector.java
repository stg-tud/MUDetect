/**
 * 
 */
package exas;

import utils.GraphGenerator;

/**
 * @author hoan
 *
 */
public class TestExasVector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*ExasSingleFeature sf = new ExasSingleFeature("A");
		HashMap<ExasFeature, Integer> vector = new HashMap<ExasFeature, Integer>();
		vector.put(sf, 10);
		System.out.println(vector.get(sf));
		ArrayList<ExasSingleFeature> seq = new ArrayList<ExasSingleFeature>();
		seq.add(sf); seq.add(sf);
		ExasSequentialFeature seqF = new ExasSequentialFeature(seq);
		System.out.println(ExasFeature.getFeature(seq));
		if (true) return;*/
		GraphGenerator gg = new GraphGenerator();
		ExasGraph graph = gg.generate(2, 1);
		gg.print(graph);
		graph.buildVectors();
		graph.printVector();
	}

}
