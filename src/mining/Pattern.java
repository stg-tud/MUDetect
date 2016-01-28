/**
 * 
 */
package mining;

import exas.ExasFeature;
import groum.GROUMGraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class Pattern implements Serializable {
	private static final long serialVersionUID = 2L;
	public static final int minSize = 2, maxSize = 20;
	public static int minFreq = 6, maxFreq = 10000;
	
	public static HashMap<String, HashSet<Pattern>> all = new HashMap<String, HashSet<Pattern>>();
	
	public static int nextID = 1;
	private int id;
	private int size = 0;
	private Fragment representative;
	private int freq = 0;
	private HashSet<Fragment> fragments = new HashSet<Fragment>();
	private int hashCode;
	
	public Pattern()
	{
		
	}
	/**
	 * @return the index
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param index the index to set
	 */
	public void setId(int index) {
		this.id = index;
	}
	public void setId() {
		this.id = nextID++;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the representative
	 */
	public Fragment getRepresentative() {
		return representative;
	}

	/**
	 * @param representative the representative to set
	 */
	public void setRepresentative(Fragment representative) {
		this.representative = representative;
		representative.toGraphics("output/patterns/changes", String.valueOf(id));
	}

	/**
	 * @return the freq
	 */
	public int getFreq() {
		return freq;
	}

	/**
	 * @param freq the freq to set
	 */
	public void setFreq(int freq) {
		this.freq = freq;
	}

	/**
	 * @return the genPatterns
	 */
	public HashSet<Fragment> getFragments() {
		return fragments;
	}

	/**
	 * @param genPatterns the genPatterns to set
	 */
	public void setFragments(HashSet<Fragment> fragments) {
		this.fragments = fragments;
	}

	/**
	 * @return the hashCode
	 */
	public int getHashCode() {
		return hashCode;
	}

	/**
	 * @param hashCode the hashCode to set
	 */
	public void setHashCode() {
		Hash hash = new Hash();
		Hash.reset(1, 1, ExasFeature.numOfFeatures);
		this.hashCode = hash.hashEuclidean(this.representative)[0];
	}
	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}
	
	public void computeFrequency()
	{
		HashMap<GROUMGraph, HashSet<Fragment>> fragmentOfGraph = new HashMap<GROUMGraph, HashSet<Fragment>>();
		for (Fragment f : fragments)
		{
			GROUMGraph g = f.getGraph();
			HashSet<Fragment> fs = fragmentOfGraph.get(g);
			if (fs == null)
				fs = new HashSet<Fragment>();
			fs.add(f);
			fragmentOfGraph.put(g, fs);
		}
		if (fragmentOfGraph.size() >= Pattern.minFreq)
		{
			this.freq = fragmentOfGraph.size();
		}
		else
		{
			this.freq = 0;
			for (GROUMGraph g : fragmentOfGraph.keySet())
			{
				HashSet<Fragment> fs = fragmentOfGraph.get(g);
				HashSet<Fragment> cluster = new HashSet<Fragment>();
				for (Fragment f : fs)
				{
					boolean isOverlap = false;
					for (Fragment c : cluster)
						if (c.overlap(f))
						{
							isOverlap = true;
							break;
						}
					if (!isOverlap)
						cluster.add(f);
				}
				this.freq += cluster.size();
			}	
		}
	}
	
	public boolean contains(Fragment fragment)
	{
		if (this.size < fragment.getNodes().size())
			return false;
		for (Fragment f : fragments)
			if (f.contains(fragment))
				return true;
		return false;
	}
	public boolean contains(Pattern other)
	{
		if (this.size < other.getSize())
			return false;
		for (Fragment f : other.getFragments())
			if (!contains(f))
				return false;
		return true;
	}
	public boolean extendsAll(Pattern p)
	{
		HashSet<Fragment> genFragments = new HashSet<Fragment>();
		for (Fragment f : fragments)
			genFragments.add(f.getGenParent());
		return genFragments.equals(p.getFragments());
	}
	public void delete()
	{
		Lattice.all.get(size - 1).remove(this);
		this.representative = null;
		for (Fragment f : this.fragments)
		{
			f.delete();
		}
		this.fragments.clear();
		//this.fragments = null;
	}
}
