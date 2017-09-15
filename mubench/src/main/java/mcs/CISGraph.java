/**
 * 
 */
package mcs;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class CISGraph {
	public static int nextID = 1;
	int id;
	int size = 0;
	MCSFragment representative;
	int freq = 0;
	HashSet<MCSFragment> fragments = new HashSet<MCSFragment>();
	
	public CISGraph(HashSet<MCSFragment> group, int freq) {
		fragments = group;
		for (MCSFragment f : fragments) {
			size = f.nodes.size();
			representative = f;
			break;
		}
		this.freq = freq;
		//computeFrequency();
	}
	
	public int getId() {
		return id;
	}

	public void setId() {
		this.id = nextID++;
	}
	
	public HashSet<MCSFragment> getFragments() {
		return fragments;
	}

	public MCSFragment getRepresentative() {
		return representative;
	}

	@SuppressWarnings("null")
	public void add2Lattice(ArrayList<Lattice> lattices) {
		setId();
		Lattice l = null;
		if (lattices.size() < size) {
			int s = size - lattices.size();
			while (s > 0) {
				l = new Lattice();
				l.step = lattices.size() + 1;
				lattices.add(l);
				s--;
			}
		} else
			l = lattices.get(size - 1);
		l.add(this);
	}
	
	public boolean contains(MCSFragment fragment) {
		if (this.size < fragment.nodes.size())
			return false;
		for (MCSFragment f : fragments)
			if (f.contains(fragment))
				return true;
		return false;
	}
	
	public boolean contains(CISGraph other) {
		if (this.size < other.size)
			return false;
		for (MCSFragment f : other.fragments)
			if (!contains(f))
				return false;
		return true;
	}

	public boolean contains(HashSet<MCSFragment> g) {
		for (MCSFragment f : g)
			if (!contains(f))
				return false;
		return true;
	}

	public void clear() {
		this.representative = null;
		for (MCSFragment f : this.fragments)
			f.delete();
		this.fragments.clear();
	}
	
	@Override
	protected void finalize() throws Throwable {
		clear();
	}
}
