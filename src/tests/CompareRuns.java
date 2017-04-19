package tests;

import java.io.File;
import java.util.HashSet;

public class CompareRuns {

	public static void main(String[] args) {
		File dir = new File("T:/usage-patterns/indeterministic/closure");
		HashSet<String> preRunPatterns = new HashSet<>();
		File preDirRun = null;
		File[] subs = dir.listFiles();
		for (int i = 0; i < subs.length; i++) {
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
