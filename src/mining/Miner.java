/**
 * 
 */
package mining;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import exas.ExasFeature;

import utils.FileIO;

import groum.GROUMBuilder;
import groum.GROUMGraph;
import groum.GROUMNode;

/**
 * @author hoan
 *
 */
public class Miner {
	private String path = "";
	private HashSet<String> exploredLables = new HashSet<String>();
	
	public Miner(String path) {
		Hash.init(1, 1, 1, 1000000);
		this.path = path;
	}
	
	public void mineJava()
	{
		GROUMBuilder gb = new GROUMBuilder(path);
		gb.build();
		ArrayList<GROUMGraph> groums = gb.getGroums();
		mine(groums);
	}
	
	public void mine(ArrayList<GROUMGraph> groums)
	{
		
		/*for (GROUMGraph g : groums)
			g.toGraphics("output");*/
		
		HashMap<String, HashSet<GROUMNode>> nodesOfLabel = new HashMap<String, HashSet<GROUMNode>>();
		for (GROUMGraph groum : groums)
		{
			for (GROUMNode node : groum.getNodes())
			{
				node.setGraph(groum);
				String label = node.getLabel();
				if (node.isFunctionInvocation())
				{
					HashSet<GROUMNode> nodes = nodesOfLabel.get(label);
					if (nodes == null)
						nodes = new HashSet<GROUMNode>();
					nodes.add(node);
					nodesOfLabel.put(label, nodes);
				}
			}
		}
		Lattice l = new Lattice();
		l.setStep(1);
		Lattice.all.add(l);
		for (String label : new HashSet<String>(nodesOfLabel.keySet()))
		{
			HashSet<GROUMNode> nodes = nodesOfLabel.get(label);
			if (nodes.size() < Pattern.minFreq)
			{
				for (GROUMNode node : nodes)
					node.delete();
				nodesOfLabel.remove(label);
			}
		}
		for (String label : nodesOfLabel.keySet())
		{
			HashSet<GROUMNode> nodes = nodesOfLabel.get(label);
			Pattern p = new Pattern();
			for (GROUMNode node : nodes)
			{
				Fragment f = new Fragment(node);
				p.getFragments().add(f);
			}
			if (Lattice.allContains(p))
				continue;
			p.setId();
			p.setSize(1);
			l.add(p);
			System.out.println("{Extending pattern of size " + p.getSize()
					+ " occurences: " + p.getFragments().size()
					+ " with label " + label
					+ " features: " + ExasFeature.numOfFeatures 
					+ " patterns: " + Pattern.nextID
					+ " fragments: " + Fragment.numofFragments
					+ " next fragment: " + Fragment.nextFragmentId);
			extend(p);
			System.out.println("}");
			exploredLables.add(label);
		}
		Lattice.filter();
		for(int step = Pattern.minSize - 1; step < Lattice.all.size(); step++)
		{
			Lattice lat = Lattice.all.get(step);
			int c = 0;
			for (Pattern p : lat.getPatterns())
			{
				c++;
				File patternDir = new File("output/patterns/" + FileIO.getSimpleFileName(this.path) + "/" + lat.getStep() + "/" + c + "_" + p.getId());
				if (!patternDir.exists())
					patternDir.mkdirs();
				/*for (Fragment f : p.getFragments())
					f.toGraphics(patternDir.getAbsolutePath(), f.getId() + "_" + f.getGraph().getName());*/
				Fragment f = p.getRepresentative();
				f.toGraphics(patternDir.getAbsolutePath(), f.getId() + "_" + f.getGraph().getName());
			}
		}
		
	}
	
	private void extend(Pattern pattern)
	{
		/*System.out.println("Extending pattern of size " + pattern.getSize()
				+ " occurences: " + pattern.getFragments().size()
				+ " features: " + ExasFeature.numOfFeatures 
				+ " patterns: " + Pattern.nextID
				+ " fragments: " + Fragment.numofFragments
				+ " next fragment: " + Fragment.nextFragmentId);*/
		/*if (pattern.getFragments().size() > 10000)
			System.err.println();*/
		HashMap<String, Integer> nodesOfLabel = new HashMap<String, Integer>();
		for (Fragment fragment : pattern.getFragments())
		{
			for (String label : fragment.getNeighbors().keySet())
			{
				if (!exploredLables.contains(label))
				{
					int c = fragment.getNeighbors().get(label).size();
					if (nodesOfLabel.containsKey(label))
						c += nodesOfLabel.get(label);
					nodesOfLabel.put(label, c);
				}
			}
		}
		HashSet<String> allNeighborLabels = new HashSet<String>(nodesOfLabel.keySet());
		for (String label : new HashSet<String>(nodesOfLabel.keySet()))
			if (nodesOfLabel.get(label) < Pattern.minFreq)
			{
				//exploredLables.add(label);
				nodesOfLabel.remove(label);
			}
		boolean allExtended = false;
		for (String label : nodesOfLabel.keySet())
		{
			System.out.println("\tLabel: " + label);
			System.out.print("\t\tGenerate fragments ... ");
			HashMap<Integer, HashSet<Fragment>> mapFragments = new HashMap<Integer, HashSet<Fragment>>();
			for (Fragment fragment : pattern.getFragments())
			{
				HashSet<GROUMNode> nodes = fragment.getNeighbors().get(label);
				if (nodes != null)
				{
					for (GROUMNode node : nodes)
					{
						Fragment xFragment = new Fragment(fragment, node);
						int hashCode = xFragment.getHashCode();
						HashSet<Fragment> fs = mapFragments.get(hashCode);
						if (fs == null)
							fs = new HashSet<Fragment>();
						else
						{
							boolean isSame = false;
							for (Fragment f : fs)
								if (f.isSameAs(xFragment))
								{
									isSame = true;
									break;
								}
							if (isSame)
							{
								xFragment.delete();
								continue;
							}
						}
						xFragment.addNeighbors(fragment, node);
						fs.add(xFragment);
						mapFragments.put(hashCode, fs);
					}
				}
			}
			System.out.println("done");
			for (int hashCode : mapFragments.keySet())
			{
				HashSet<Fragment> fs = mapFragments.get(hashCode);
				if (fs.size() < Pattern.minFreq)
					continue;
				Pattern xp = new Pattern();
				xp.setFragments(fs);
				xp.computeFrequency();
				if (xp.getFreq() >= Pattern.minFreq)
				{
					if (Lattice.allContains(xp))
						continue;
					xp.setId();
					for (Fragment f : fs)
					{
						xp.setRepresentative(f);
						xp.setSize(f.getNodes().size());
						break;
					}
					Lattice l = new Lattice();
					if (Lattice.all.size() < xp.getSize())
					{
						l.setStep(xp.getSize());
						Lattice.all.add(l);
					}
					else
						l = Lattice.all.get(xp.getSize() - 1);
					l.add(xp);
					if (xp.getFragments().size() <= Pattern.maxFreq)
					{
						String prefix = "";
						for (int i = 1; i < xp.getSize(); i++)
							prefix += " ";
						System.out.println(prefix + "{Extended pattern of size " + pattern.getSize()
								+ " occurences: " + pattern.getFragments().size()
								+ " with label " + label
								+ " features: " + ExasFeature.numOfFeatures 
								+ " patterns: " + Pattern.nextID
								+ " fragments: " + Fragment.numofFragments
								+ " next fragment: " + Fragment.nextFragmentId);
						extend(xp);
						System.out.println(prefix + "}");
					}
					if (xp.extendsAll(pattern))
					{
						pattern.delete();
						for (Fragment f : xp.getFragments())
							f.setGenParent(null);
						allExtended = true;
								break;
					}
				}
			}
			if (allExtended)
				break;
			exploredLables.add(label);
		}
		//exploredLables.removeAll(allNeighborLabels);
		exploredLables.removeAll(nodesOfLabel.keySet());
	}
}
