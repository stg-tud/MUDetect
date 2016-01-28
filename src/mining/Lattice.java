package mining;

import java.util.ArrayList;
import java.util.HashSet;
/**
 * @author Nguyen Anh Hoan
 *
 */
public class Lattice {
	public static ArrayList<Lattice> all = new ArrayList<Lattice>();
	
	private int step;
	
	public HashSet<Fragment> fragments = new HashSet<Fragment>();
	
	//public HashSet<Integer> groups = new  HashSet<Integer>();
	
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

	public void setPatterns(ArrayList<Pattern> patterns) {
		this.patterns = patterns;
	}
	
	public void add(Pattern p)
	{
		patterns.add(p);
	}
	
	public void remove(Pattern p)
	{
		patterns.remove(p);
	}
	
	public boolean contains(Pattern pattern)
	{
		for (Pattern p : patterns)
			if (p.contains(pattern))
				return true;
		return false;
	}
	
	public static void filter()
	{
		for (int size = Pattern.minSize-1; size < all.size() - 1; size++)
		{
			Lattice l1 = all.get(size), l2 = all.get(size + 1);
			for (Pattern p1 : new ArrayList<Pattern>(l1.getPatterns()))
			{
				for (Pattern p2 : l2.getPatterns())
				{
					if (p2.contains(p1))
						l1.remove(p1);
				}
			}
		}
	}
	
	public static boolean allContains(Pattern p)
	{
		System.out.print("Check occurences in minded patterns ... ");
		for (int i =  all.size() - 1; i >= p.getSize(); i--)
		{
			Lattice l = all.get(i);
			if (l.contains(p))
			{
				System.out.println("yes");
				return true;
			}
		}
		/*Lattice l = all.get(p.getSize());
		if (l.contains(p))
			return true;*/
		System.out.println("no");
		return false;
	}
}
