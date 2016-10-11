package mcs;

import java.util.ArrayList;
import java.util.HashSet;
/**
 * @author Nguyen Anh Hoan
 *
 */
public class Lattice {
	int step;
	ArrayList<CISGraph> patterns = new ArrayList<CISGraph>();

	public int getStep() {
		return step;
	}

	public void add(CISGraph p) {
		patterns.add(p);
	}
	
	public void remove(CISGraph p) {
		patterns.remove(p);
	}
	
	private boolean contains(CISGraph pattern) {
		for (CISGraph p : patterns)
			if (p.contains(pattern))
				return true;
		return false;
	}

	private boolean contains(HashSet<MCSFragment> g) {
		for (CISGraph p : patterns)
			if (p.contains(g))
				return true;
		return false;
	}
	
	public static void filter(ArrayList<Lattice> lattices) {
		for (int size = 0; size < lattices.size(); size++) {
			Lattice l1 = lattices.get(size);
			for (CISGraph p1 : new ArrayList<CISGraph>(l1.patterns)) {
				boolean found = false;
				for (int i = size; i < lattices.size(); i++) {
					Lattice l2 = lattices.get(i);
					for (CISGraph p2 : l2.patterns) {
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
	
	public static boolean contains(ArrayList<Lattice> lattices, CISGraph p) {
		for (int i =  lattices.size() - 1; i >= p.size; i--) {
			Lattice l = lattices.get(i);
			if (l.contains(p))
				return true;
		}
		return false;
	}

	public static boolean contains(ArrayList<Lattice> lattices, HashSet<MCSFragment> g) {
		int size = 0;
		for (MCSFragment f : g) {
			size = f.nodes.size();
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
