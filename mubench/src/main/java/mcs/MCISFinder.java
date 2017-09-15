package mcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import mining.Configuration;

public class MCISFinder {

	private ArrayList<Lattice> lattices = new ArrayList<Lattice>();
	private Configuration config;

	public MCISFinder(Configuration config) {
		this.config = config;
	}

	public ArrayList<CISGraph> match(HashSet<EGroumGraph> graphs) {
		HashMap<String, HashSet<EGroumNode>> nodesOfLabel = new HashMap<>();
		HashMap<String, HashSet<EGroumGraph>> graphsOfLabel = new HashMap<>();
		for (EGroumGraph groum : graphs) {
			for (EGroumNode node : groum.getNodes()) {
				node.setGraph(groum);
				String label = config.nodeToLabel.apply(node);
				if (node.isCoreAction() && label.endsWith(")")) {
					HashSet<EGroumNode> nodes = nodesOfLabel.get(label);
					if (nodes == null) {
						nodes = new HashSet<EGroumNode>();
						nodesOfLabel.put(label, nodes);
					}
					nodes.add(node);
					HashSet<EGroumGraph> gs = graphsOfLabel.get(label);
					if (gs == null) {
						gs = new HashSet<>();
						graphsOfLabel.put(label, gs);
					}
					gs.add(groum);
				}
			}
		}
		Lattice l = new Lattice();
		l.step = 1;
		lattices.add(l);
		for (String label : new HashSet<String>(nodesOfLabel.keySet())) {
			HashSet<EGroumGraph> gs = graphsOfLabel.get(label);
			if (!gs.equals(graphs))
				nodesOfLabel.remove(label);
		}
		for (String label : nodesOfLabel.keySet()) {
			HashSet<EGroumNode> nodes = nodesOfLabel.get(label);
			HashSet<MCSFragment> fragments = new HashSet<>();
			for (EGroumNode node : nodes) {
				MCSFragment f = new MCSFragment(node, config);
				fragments.add(f);
			}
			CISGraph p = new CISGraph(fragments, fragments.size());
			extend(p, graphs);
		}
		System.out.println("Done mining.");
		Lattice.filter(lattices);
		System.out.println("Done filtering.");
		
		return getPatterns();
	}
	
	private void extend(CISGraph p, HashSet<EGroumGraph> graphs) {
		HashSet<MCSFragment> xfs = extend(p.fragments, graphs);
		if (xfs != null) {
			CISGraph xp = new CISGraph(xfs, xfs.size());
			extend(xp, graphs);
			p.clear();
		} else {
			p.add2Lattice(lattices);
		}
	}

	private HashSet<MCSFragment> extend(HashSet<MCSFragment> fragments, HashSet<EGroumGraph> graphs) {
		HashMap<String, HashMap<MCSFragment, HashSet<EGroumEdge>>> labelFragmentEdges = new HashMap<>();
		for (MCSFragment f : fragments) {
			HashMap<String, HashSet<EGroumEdge>> labelEdges = f.extend();
			for (String label : labelEdges.keySet()) {
				HashMap<MCSFragment, HashSet<EGroumEdge>> fragmentEdges = labelFragmentEdges.get(label);
				if (fragmentEdges == null) {
					fragmentEdges = new HashMap<>();
					labelFragmentEdges.put(label, fragmentEdges);
				}
				fragmentEdges.put(f, new HashSet<>(labelEdges.get(label)));
			}
		}
		for (String label : labelFragmentEdges.keySet()) {
			HashMap<MCSFragment, HashSet<EGroumEdge>> fragmentEdges = labelFragmentEdges.get(label);
			HashSet<EGroumGraph> gs = getGraphs(fragmentEdges.keySet());
			if (gs.equals(graphs)) {
				HashSet<MCSFragment> xfs = new HashSet<>();
				for (MCSFragment f : fragmentEdges.keySet()) {
					for (EGroumEdge e : fragmentEdges.get(f)) {
						MCSFragment xf = new MCSFragment(f, e, config);
						xfs.add(xf);
					}
				}
				HashSet<MCSFragment> group = mine(xfs, graphs);
				if (group != null)
					return group;
			}
		}
		return null;
	}

	private HashSet<MCSFragment> mine(HashSet<MCSFragment> fragments, HashSet<EGroumGraph> graphs) {
		HashMap<Integer, HashSet<MCSFragment>> buckets = new HashMap<>();
		for (MCSFragment f : fragments) {
			int h = f.getVectorHashCode();
			HashSet<MCSFragment> bucket = buckets.get(h);
			if (bucket == null) {
				bucket = new HashSet<>();
				buckets.put(h, bucket);
			}
			bucket.add(f);
		}
		for (int h : buckets.keySet()) {
			HashSet<MCSFragment> bucket = buckets.get(h);
			HashSet<MCSFragment> group = group(bucket, graphs);
			if (group != null)
				return group;
		}
		return null;
	}

	private HashSet<EGroumGraph> getGraphs(Collection<MCSFragment> g) {
		HashSet<EGroumGraph> gs = new HashSet<>();
		for (MCSFragment f : g)
			gs.add(f.graph);
		return gs;
	}

	private HashSet<MCSFragment> group(HashSet<MCSFragment> bucket, HashSet<EGroumGraph> graphs) {
		while (!bucket.isEmpty()) {
			MCSFragment f = null;
			for (MCSFragment fragment : bucket) {
				f = fragment;
				break;
			}
			HashSet<MCSFragment> group = group(f, bucket, graphs);
			if (group != null)
				return group;
		}
		return null;
	}

	private HashSet<MCSFragment> group(MCSFragment f, HashSet<MCSFragment> bucket, HashSet<EGroumGraph> graphs) {
		HashSet<MCSFragment> group = new HashSet<>();
		group.add(f);
		bucket.remove(f);
		for (MCSFragment g : new HashSet<MCSFragment>(bucket)) {
			if (f.vector.equals(g.vector)) {
				group.add(g);
				bucket.remove(g);
			}
		}
		if (group.size() >= graphs.size() && getGraphs(group).equals(graphs)) {
			return removeDuplicates(group);
		}
		return null;
	}

	private HashSet<MCSFragment> removeDuplicates(HashSet<MCSFragment> group) {
		ArrayList<MCSFragment> l = new ArrayList<>(group);
		int i = 0;
		while (i < l.size() - 1) {
			MCSFragment f = l.get(i);
			int j = i + 1;
			while (j < l.size()) {
				if (f.isSameAs(l.get(j)))
					l.remove(j);
				else
					j++;
			}
			i++;
		}
		return new HashSet<>(l);
	}

	private ArrayList<CISGraph> getPatterns() {
		ArrayList<CISGraph> patterns = new ArrayList<>();
		for (int step = 0; lattices.size() > step; step++) {
			Lattice lattice = lattices.get(step);
			patterns.addAll(lattice.patterns);
		}
		return patterns;
	}
}
