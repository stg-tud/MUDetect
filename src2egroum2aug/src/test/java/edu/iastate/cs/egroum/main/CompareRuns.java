package edu.iastate.cs.egroum.main;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import edu.iastate.cs.egroum.utils.FileIO;

public class CompareRuns {

	public static void main(String[] args) {
//		comparePatterns();
		compareGroums();
	}

	private static void compareGroums() {
		File dir = new File("T:/usage-patterns/patterns");
		HashMap<String, String[]> preGroums = null;
		for (File file : dir.listFiles()) {
			if (!file.isDirectory() && file.getName().endsWith(".dat")) {
				HashMap<String, String[]> groums = (HashMap<String, String[]>) FileIO.readObjectFromFile(file.getAbsolutePath());
				if (preGroums != null) {
//					assert groums.keySet().equals(preGroums.keySet());
					for (String key : groums.keySet()) {
						String[] pre = preGroums.get(key), cur = groums.get(key);
						String[] parts = pre[0].split("\t");
						int size = Integer.parseInt(parts[0]);
						if (size <= 10 && !pre[0].equals(cur[0])) {
							System.err.println(pre[1]);
							System.err.println(cur[1]);
							System.err.println(pre[0] + "\t" + cur[0]);
							System.out.println();
						}
					}
				}
				preGroums = groums;
			}
		}
	}

	public static void comparePatterns() {
		File dir = new File("T:/usage-patterns/indeterministic/closure");
		HashSet<String> preRunPatterns = new HashSet<>();
		File preDirRun = null;
		File[] subs = dir.listFiles();
		for (int i = 0; i < Math.min(5, subs.length); i++) {
			File dirRun = subs[i];
			HashSet<String> runPatterns = new HashSet<>();
			File dirPatterns = dirRun; // new File(dirRun, "patterns").listFiles()[0];
			for (File dirSize : dirPatterns.listFiles()) {
				for (File dirPattern : dirSize.listFiles()) {
					runPatterns.add(dirSize.getName() + "/" + dirPattern.getName());
				}
			}
			if (preDirRun != null) {
				HashSet<String> common = new HashSet<>(preRunPatterns);
				common.retainAll(runPatterns);
				HashSet<String> inPre = new HashSet<>(preRunPatterns);
				inPre.removeAll(common);
				HashSet<String> inCurrent = new HashSet<>(runPatterns);
				inCurrent.removeAll(common);
				print(preDirRun, dirRun, inPre);
				print(dirRun, preDirRun, inCurrent);
			}
			preRunPatterns = new HashSet<>(runPatterns);
			preDirRun = dirRun;
		}
	}

	public static void print(File preDirRun, File dirRun, HashSet<String> inPre) {
		if (!inPre.isEmpty()) {
			System.out.println("In " + preDirRun.getName() + " not in "+ dirRun.getName());
			for (String p : inPre)
				System.out.println(p);
			System.out.println();
			System.out.println();
		}
	}

}
