/**
 * 
 */
package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import egroum.EGroumBuilder;
import mining.Anomaly;
import mining.Fragment;
import mining.Miner;
import utils.FileIO;

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
		String path = "T:/repos/itext/5090/original-src", name = "itext";
//		String path = "input/Test2.java", name = "";
//		String path = "test/input/Test_mine.java", name = "";
		EGroumBuilder gb = new EGroumBuilder(null);
		Miner miner = new Miner(name);
		miner.mine(new ArrayList<>(gb.build(path)));
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
			for (Fragment f : a.getInstances()) {
				sb.append(f.getNodes());
				break;
			}
			sb.append("::");
			sb.append(a.getPattern().getNodes());
			FileIO.logStream.println(sb.toString());
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000);
	}

}
