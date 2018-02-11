package edu.iastate.cs.mudetect.mining;

import java.util.ArrayList;
import java.util.HashSet;
/**
 * @author Nguyen Anh Hoan
 *
 */
public class Lattice {
	private int step;
	private ArrayList<Pattern> patterns = new ArrayList<Pattern>();

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public ArrayList<Pattern> getPatterns() {
		return patterns;
	}
	
	public void add(Pattern p) {
		patterns.add(p);
	}
	
	public void remove(Pattern p) {
		patterns.remove(p);
	}
	
	private boolean contains(Pattern pattern) {
		for (Pattern p : patterns)
			if (p.contains(pattern))
				return true;
		return false;
	}

	private boolean contains(HashSet<Fragment> g) {
		for (Pattern p : patterns)
			if (p.contains(g))
				return true;
		return false;
	}
	
	public static void filter(ArrayList<Lattice> lattices, int minPatternSize) {
		for (int size = minPatternSize-1; size < lattices.size(); size++) {
			Lattice l1 = lattices.get(size);
			for (Pattern p1 : new ArrayList<Pattern>(l1.getPatterns())) {
				boolean found = false;
				for (int i = size; i < lattices.size(); i++) {
					Lattice l2 = lattices.get(i);
					for (Pattern p2 : l2.getPatterns()) {
						if (p1 != p2 && p2.contains(p1)) {
							l1.remove(p1);
							found = true;
							break;
						}
					}
					if (found) break;
				}
			}
		}
	}
	
	public static boolean contains(ArrayList<Lattice> lattices, Pattern p) {
		for (int i =  lattices.size() - 1; i >= p.getSize(); i--) {
			Lattice l = lattices.get(i);
			if (l.contains(p))
				return true;
		}
		return false;
	}

	public static boolean contains(ArrayList<Lattice> lattices, HashSet<Fragment> g) {
		int size = 0;
		for (Fragment f : g) {
			size = f.getNodes().size();
			break;
		}
		for (int i = lattices.size() - 1; i >= size - 1; i--) {
			Lattice l = lattices.get(i);
			if (l.contains(g))
				return true;
		}
		return false;
	}
}
