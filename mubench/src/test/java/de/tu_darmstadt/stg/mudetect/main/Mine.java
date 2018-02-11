/**
 * 
 */
package de.tu_darmstadt.stg.mudetect.main;

import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import edu.iastate.cs.mudetect.mining.Anomaly;
import edu.iastate.cs.mudetect.mining.Configuration;
import edu.iastate.cs.mudetect.mining.Fragment;
import edu.iastate.cs.mudetect.mining.Miner;
import edu.iastate.cs.egroum.aug.AUGBuilder;
import edu.iastate.cs.egroum.aug.AUGConfiguration;
import edu.iastate.cs.egroum.utils.FileIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author hoan
 *
 */
public class Mine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		String path = "T:/repos/itext/5091/original-src", name = "itext";
//		String path = "T:/repos/closure-compiler/src", name = "closure";
//		String path = "input/Test2.java", name = "";
//		String path = "test/input/Test_mine.java", name = "";
		AUGBuilder gb = new AUGBuilder(new AUGConfiguration(){{removeImplementationCode = 2;}});
		Miner miner = new Miner(name, new Configuration(){{outputPath = "T:/usage-patterns/patterns";}});
		miner.mine(new ArrayList<>(gb.build(path, null)));
		ArrayList<Anomaly> anomalies = miner.anomalies;
		Collections.sort(anomalies, new Comparator<Anomaly>() {

			@Override
			public int compare(Anomaly a1, Anomaly a2) {
				if (a1.getScore() > a2.getScore())
					return -1;
				if (a1.getScore() < a2.getScore())
					return 1;
				return 0;
			}
		});
		for (Anomaly a : anomalies) {
			StringBuilder sb = new StringBuilder();
			sb.append(a.getScore());
			sb.append("::");
			sb.append(a.getInstances().size());
			sb.append("::");
			sb.append(a.getPatternFreq());
			sb.append("::");
			int len = 0;
			for (Fragment f : a.getInstances()) {
				sb.append(f.getNodes());
				len = f.getNodes().size();
				break;
			}
			ArrayList<Node> nodes = a.getPattern().getNodes();
			boolean missDataNodes = true;
			for (int i = len; i < nodes.size(); i++)
				if (!(nodes.get(i) instanceof DataNode)) {
					missDataNodes = false;
					break;
				}
			sb.append("::");
			sb.append(a.getPattern().getNodes());
			if (!missDataNodes)
				FileIO.logStream.println(sb.toString());
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000);
	}

}
