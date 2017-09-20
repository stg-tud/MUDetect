/**
 * 
 */
package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.*;
import egroum.DenseAUGPredicate;
import exas.ExasFeature;
import mining.Configuration.Level;
import utils.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.LiteralNode;
import de.tu_darmstadt.stg.mudetect.src2aug.DenseAUGPredicate;
import de.tu_darmstadt.stg.mudetect.utils.FileIO;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;

/**
 * @author hoan
 * 
 */
public class Miner {
	public static boolean EXTEND_SOURCE_DATA_NODES = true;
	private String projectName;
	private final Configuration config;
	public ArrayList<Lattice> lattices = new ArrayList<Lattice>();
	public ArrayList<Anomaly> anomalies = new ArrayList<>();

	public Miner(String projectName, Configuration config) {
		this.projectName = projectName;
		this.config = config;
		if (config.abstractConditionEdges)
			ExasFeature.abstractConditionEdges();
	}
	
	public String getProjectName() {
		return projectName;
	}

	public Set<Pattern> mine(ArrayList<APIUsageExample> augs) {
		augs.removeIf(DenseAUGPredicate::isTooDense);
		for (APIUsageExample aug : augs) {
			//aug.deleteUnaryOperationNodes();
			collapseLiterals(aug);
		}
		HashSet<String> coreLabels = new HashSet<>();
		HashMap<String, HashSet<Node>> nodesOfLabel = new HashMap<>();
		for (APIUsageExample aug : augs) {
			for (Node node : aug.vertexSet()) {
				node.setGraph(aug);
				String label = config.nodeToLabel.apply(node);
				HashSet<Node> nodes = nodesOfLabel.get(label);
				if (nodes == null)
					nodes = new HashSet<>();
				nodes.add(node);
				nodesOfLabel.put(label, nodes);
				if (node instanceof MethodCallNode && node.isCoreAction())
					coreLabels.add(label);
			}
		}
		Lattice l = new Lattice();
		l.setStep(1);
		lattices.add(l);
		for (String label : new HashSet<>(nodesOfLabel.keySet())) {
			HashSet<Node> nodes = nodesOfLabel.get(label);
			if (nodes.size() < config.minPatternSupport) {
				for (Node node : nodes) {
					boolean isDefAction = false;
					if (node instanceof ActionNode) {
						for (Edge e : node.getGraph().outgoingEdgesOf(node))
							if (e.getType() == DEFINITION) {
								isDefAction = true;
								break;
							}
					}
					if (!isDefAction)
						node.getGraph().removeVertex(node);
				}
				nodesOfLabel.remove(label);
			}
			if (!coreLabels.contains(label))
				nodesOfLabel.remove(label);
		}
		ArrayList<String> list = new ArrayList<>(nodesOfLabel.keySet());
		list.sort((l1, l2) -> {
            int c1 = nodesOfLabel.get(l1).size(), c2 = nodesOfLabel.get(l2).size();
            if (c1 != c2)
                return c2 - c1;
            return l2.compareTo(l1);
        });
		for (String label : list) {
			HashSet<Node> nodes = nodesOfLabel.get(label);
			HashSet<Fragment> fragments = new HashSet<>();
			for (Node node : nodes) {
				if (node instanceof MethodCallNode && node.isCoreAction()) {
					Fragment f = new Fragment(node, config);
					fragments.add(f);
				}
			}
			Pattern p = new Pattern(fragments, fragments.size());
			extend(p);
		}
		System.out.println("Done mining.");
		Lattice.filter(lattices, config.minPatternSize);
		System.out.println("Done filtering.");

		report();

		return getPatterns();
	}

	private void collapseLiterals(APIUsageExample aug) {
		for (Node node : new HashSet<Node>(aug.vertexSet())) {
			HashMap<String, ArrayList<Node>> labelLiterals = new HashMap<>();
			for (Node n : aug.incomingNodesOf(node)) {
				if (n instanceof LiteralNode) {
					String label = n.getLabel();
					ArrayList<Node> lits = labelLiterals.computeIfAbsent(label, k -> new ArrayList<>());
					lits.add(n);
				}
			}
			for (String label : labelLiterals.keySet()) {
				ArrayList<Node> lits = labelLiterals.get(label);
				if (lits.size() > 0) {
					for (int i = 1; i < lits.size(); i++)
						aug.removeVertex(lits.get(i));
				}
			}
		}
	}

	private void report() {
		if (config.outputPath != null) {
			File dir = new File(config.outputPath, this.projectName + "-" + (System.currentTimeMillis() / 1000));
			for (int step = config.minPatternSize; step <= lattices.size(); step++) {
				Lattice lat = lattices.get(step - 1);
				int c = 0;
				for (Pattern p : lat.getPatterns()) {
					c++;
					File patternDir = new File(dir.getAbsolutePath() + "/" + step + "/" + c + "_" + p.getId());
					if (!patternDir.exists())
						patternDir.mkdirs();
					Fragment rf = p.getRepresentative();
					rf.toGraphics(patternDir.getAbsolutePath(), rf.getId() + "");
					StringBuilder sb = new StringBuilder();
					for (Fragment f : p.getFragments()) {
						APIUsageExample graph = f.getGraph();
						String fileName = graph.getLocation().getFilePath();
						String name = graph.getLocation().getMethodSignature();
						sb.append(fileName + "," + name + "\n");
						/*String[] parts = name.split(",");
						sb.append("https://github.com/" + projectName + "/commit/"
								+ parts[0].substring(0, parts[0].indexOf('.')) + "/"
								+ parts[1]
								+ "\n");*/
					}
					FileIO.writeStringToFile(sb.toString(),
							patternDir.getAbsolutePath() + "/locations.txt");
				}
			}
			System.out.println("Done reporting.");
		}
	}
	
	private Set<Pattern> getPatterns() {
		Set<Pattern> patterns = new HashSet<>();
		for (int step = config.minPatternSize - 1; lattices.size() > step; step++) {
			Lattice lattice = lattices.get(step);
			patterns.addAll(lattice.getPatterns());
		}
		return patterns;
	}

	private void extend(Pattern pattern) {
		int patternSize = 0;
		if (pattern.getSize() >= config.maxPatternSize)
			for(Node node : pattern.getRepresentative().getNodes())
				if(node.isCoreAction())
					patternSize++;
		if(patternSize >= config.maxPatternSize) {
			pattern.add2Lattice(lattices);
			return;
		}
		HashMap<String, HashMap<Fragment, HashSet<ArrayList<Node>>>> labelFragmentExtendableNodes = new HashMap<>();
		for (Fragment f : pattern.getFragments()) {
			HashMap<String, HashSet<ArrayList<Node>>> xns = f.extend();
			for (String label : xns.keySet()) {
				HashMap<Fragment, HashSet<ArrayList<Node>>> fens = labelFragmentExtendableNodes.computeIfAbsent(label, k -> new HashMap<>());
				fens.put(f, xns.get(label));
			}
		}
		for (String label : new HashSet<>(labelFragmentExtendableNodes.keySet())) {
			HashMap<Fragment, HashSet<ArrayList<Node>>> fens = labelFragmentExtendableNodes.get(label);
			if (fens.size() < config.minPatternSupport)
				labelFragmentExtendableNodes.remove(label);
		}
		HashSet<Fragment> group = new HashSet<>(), frequentFragments = new HashSet<>();
		int xfreq = config.minPatternSupport - 1;
		String xlabel = "";
		boolean extensible = false;
		for (String label : labelFragmentExtendableNodes.keySet()) {
			HashMap<Fragment, HashSet<ArrayList<Node>>> fens = labelFragmentExtendableNodes.get(label);
			HashSet<Fragment> xfs = new HashSet<>();
			for (Fragment f : fens.keySet()) {
				for (ArrayList<Node> ens : fens.get(f)) {
					Fragment xf = new Fragment(f, ens);
					xfs.add(xf);
				}
			}
			HashSet<Fragment> g = new HashSet<>();
			int freq = mine(g, xfs, pattern, frequentFragments);
			System.out.println("\tTrying with label " + label + ": " + xfs.size() + "\t" + freq + "\t" + xfreq);
			if (freq >= config.minPatternSupport && isBetter(freq, label, xfreq, xlabel)) {
				extensible = true;
				group = g;
				xfreq = freq;
				xlabel = label;
			} else if (freq == -1)
				extensible = true;
		}
		System.out.println("Done trying all labels");
		if (extensible) {
			HashSet<Fragment> inextensibles = new HashSet<>(pattern.getFragments());
			for (Fragment xf : frequentFragments) {
				inextensibles.remove(xf.getGenFragment());
			}
			Pattern ip = null;
			if (inextensibles.size() >= config.minPatternSupport) {
				int freq = computeFrequency(inextensibles);
				if (freq >= config.minPatternSupport && !Lattice.contains(lattices, inextensibles)) {
					ip = new Pattern(inextensibles, freq);
					ip.subPattern = pattern.subPattern;
					ip.add2Lattice(lattices);
					pattern.getFragments().removeAll(inextensibles);
				}
			} else if (xfreq >= config.minPatternSupport && !inextensibles.isEmpty() /*&& inextensibles.size() <= 2*/){
				// report anomalies
				double rareness = 1 - inextensibles.size() * 1.0 / pattern.getFreq();
				anomalies.add(new Anomaly(rareness, pattern.getFreq(), inextensibles, group));
			}
			if (xfreq >= config.minPatternSupport) {
				Pattern xp = new Pattern(group, xfreq);
				ArrayList<String> labels = new ArrayList<>();
				Fragment rep = null, xrep = null;
				for (Fragment f : group) {
					xrep = f;
					break;
				}
				for (Fragment f : pattern.getFragments()) {
					rep = f;
					break;
				}
				if (rep != null && xrep != null) {
					for (int j = rep.getNodes().size(); j < xrep.getNodes().size(); j++)
						labels.add(xrep.getNodes().get(j).getLabel());
					System.out.println("{Extending pattern of size " + rep.getNodes().size()
							+ " " + rep.getNodes()
							+ " occurences: " + pattern.getFragments().size()
							+ " frequency: " + pattern.getFreq()
							+ " with label " + labels
							+ " occurences: " + group.size()
							+ " frequency: " + xfreq
							+ " patterns: " + Pattern.nextID 
							+ " fragments: " + Fragment.numofFragments 
							+ " next fragment: " + Fragment.nextFragmentId);
				}
				if (ip == null)
					xp.subPattern = pattern.subPattern;
				else
					xp.subPattern = ip;
				extend(xp);
				System.out.println("}");
			}
			pattern.clear();
		} else
			pattern.add2Lattice(lattices);
	}

	private boolean isBetter(int freq, String label, int xfreq, String xlabel) {
		if (label.endsWith(")") && !xlabel.endsWith(")"))
			return true;
		if (!label.endsWith(")") && xlabel.endsWith(")"))
			return false;
		if (label.contains("(") && label.contains(")") && (!xlabel.contains("(") || !xlabel.contains(")")))
			return true;
		if (xlabel.contains("(") && xlabel.contains(")") && (!label.contains("(") || !label.contains(")")))
			return false;
		if (freq > xfreq)
			return true;
		if (freq < xfreq)
			return false;
		return label.compareTo(xlabel) > 0;
	}

	private int mine(HashSet<Fragment> result, HashSet<Fragment> fragments, Pattern pattern, HashSet<Fragment> frequentFragments) {
		HashMap<Integer, HashSet<Fragment>> buckets = new HashMap<>();
		for (Fragment f : fragments) {
			int h = f.getVectorHashCode();
			HashSet<Fragment> bucket = buckets.computeIfAbsent(h, k -> new HashSet<>());
			bucket.add(f);
		}
		HashSet<HashSet<Fragment>> groups = new HashSet<>();
		for (int h : buckets.keySet()) {
			HashSet<Fragment> bucket = buckets.get(h);
			group(groups, bucket);
		}
		HashSet<Fragment> group = new HashSet<>();
		int xfreq = config.minPatternSupport - 1;
		boolean extensible = false;
		for (HashSet<Fragment> g : groups) {
			int freq = computeFrequency(g);
			if (freq >= config.minPatternSupport)
				frequentFragments.addAll(g);
			if (freq > xfreq) {
				extensible = true;
				if (!Lattice.contains(lattices, g)) {
					group = g;
					xfreq = freq;
				}
			}
		}
		result.addAll(group);
		if (extensible && xfreq < config.minPatternSupport)
			return -1;
		return xfreq;
	}

	private int computeFrequency(HashSet<Fragment> fragments) {
		HashSet<String> projectNames = new HashSet<>();
		HashMap<APIUsageGraph, ArrayList<Fragment>> fragmentsOfGraph = new HashMap<>();
		for (Fragment f : fragments) {
			APIUsageExample g = f.getGraph();
			projectNames.add(g.getLocation().getProjectName());
			ArrayList<Fragment> fs = fragmentsOfGraph.get(g);
			if (fs == null)
				fs = new ArrayList<>();
			fs.add(f);
			fragmentsOfGraph.put(g, fs);
		}
		if (config.occurenceLevel == Configuration.Level.CROSS_PROJECT)
			return projectNames.size();
		if (config.occurenceLevel == Configuration.Level.CROSS_METHOD)
			return fragmentsOfGraph.size();
		int freq = 0;
		for (APIUsageGraph g : fragmentsOfGraph.keySet()) {
			ArrayList<Fragment> fs = fragmentsOfGraph.get(g);
			int i = 0;
			while (i < fs.size()) {
				Fragment f = fs.get(i);
				int j = i + 1;
				while (j < fs.size()) {
					if (f.overlap(fs.get(j))) {
						fs.remove(j);
					}
					else
						j++;
				}
				i++;
			}
			freq += i;
		}	
		return freq;
	}

	private void group(HashSet<HashSet<Fragment>> groups, HashSet<Fragment> bucket) {
		while (!bucket.isEmpty()) {
			Fragment f = null;
			for (Fragment fragment : bucket) {
				f = fragment;
				break;
			}
			group(f, groups, bucket);
		}
	}

	private void group(Fragment f, HashSet<HashSet<Fragment>> groups, HashSet<Fragment> bucket) {
		HashSet<Fragment> group = new HashSet<>();
		HashSet<Fragment> gens = new HashSet<>();
		group.add(f);
		gens.add(f.getGenFragment());
		bucket.remove(f);
		for (Fragment g : new HashSet<Fragment>(bucket)) {
			if (f.getVector().equals(g.getVector())) {
				group.add(g);
				gens.add(g.getGenFragment());
				bucket.remove(g);
			}
		}
		if (gens.size() >= config.minPatternSupport && group.size() >= config.minPatternSupport) {
			removeDuplicates(group);
			if (group.size() >= config.minPatternSupport)
				groups.add(group);
		}
	}

	private void removeDuplicates(HashSet<Fragment> group) {
		ArrayList<Fragment> l = new ArrayList<>(group);
		int i = 0;
		while (i < l.size() - 1) {
			Fragment f = l.get(i);
			int j = i + 1;
			while (j < l.size()) {
				if (f.isSameAs(l.get(j)))
					l.remove(j);
				else
					j++;
			}
			i++;
		}
		group.retainAll(l);
	}
}
