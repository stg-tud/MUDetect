/**
 * 
 */
package edu.iastate.cs.mudetect.mining;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class Pattern {
	public static int nextID = 1;
	private int id;
	private int size = 0;
	private Fragment representative;
	private int freq = 0;
	private HashSet<Fragment> fragments = new HashSet<Fragment>();
	protected Pattern subPattern;
	
	public Pattern(HashSet<Fragment> group, int freq) {
		fragments = group;
		for (Fragment f : fragments) {
			size = f.getNodes().size();
			representative = f;
			break;
		}
		this.freq = freq;
		//computeFrequency();
	}
	
	@SuppressWarnings("null")
	public void add2Lattice(ArrayList<Lattice> lattices) {
		setId();
		Lattice l = null;
		if (lattices.size() < size) {
			int s = size - lattices.size();
			while (s > 0) {
				l = new Lattice();
				l.setStep(lattices.size() + 1);
				lattices.add(l);
				s--;
			}
		} else
			l = lattices.get(size - 1);
		l.add(this);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId() {
		this.id = nextID++;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Fragment getRepresentative() {
		return representative;
	}

	public void setRepresentative(Fragment representative) {
		this.representative = representative;
		//representative.toGraphics("D:/temp/output/patterns/changes", String.valueOf(id));
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public HashSet<Fragment> getFragments() {
		return fragments;
	}

	public void setFragments(HashSet<Fragment> fragments) {
		this.fragments = fragments;
	}
	
	public boolean contains(Fragment fragment) {
		if (this.size < fragment.getNodes().size())
			return false;
		for (Fragment f : fragments)
			if (f.contains(fragment))
				return true;
		return false;
	}
	
	public boolean contains(Pattern other) {
		if (this.size < other.getSize())
			return false;
		for (Fragment f : other.getFragments())
			if (!contains(f))
				return false;
		return true;
	}

	public boolean contains(HashSet<Fragment> g) {
		for (Fragment f : g)
			if (!contains(f))
				return false;
		return true;
	}

	public void clear() {
		this.representative = null;
		for (Fragment f : this.fragments)
			f.delete();
		this.fragments.clear();
	}
	
	@Override
	protected void finalize() throws Throwable {
		clear();
	}
}
