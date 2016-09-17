/**
 * 
 */
package mining;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import egroum.EGroumActionNode;
import egroum.EGroumBuilder;
import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import egroum.EGroumDataEdge.Type;
import utils.FileIO;

/**
 * @author hoan
 * 
 */
public class Miner {
	public double maxSingleNodePrevalence = 0.5;
	private String path = "", projectName;
	public ArrayList<Lattice> lattices = new ArrayList<Lattice>();
	public String output_path = "output/patterns";
	
	public Miner(String path, String projectName) {
		this.path = path;
		this.projectName = projectName;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void mine() {
		EGroumBuilder gb = new EGroumBuilder();
		mine(new ArrayList<>(gb.build(path)));
	}

	public Set<Pattern> mine(ArrayList<EGroumGraph> groums) {
		for (EGroumGraph groum : groums) {
			//groum.deleteUnaryOperationNodes();
			groum.collapseLiterals();
		}
		HashSet<String> coreLabels = new HashSet<>();
		HashMap<String, HashSet<EGroumNode>> nodesOfLabel = new HashMap<String, HashSet<EGroumNode>>();
		for (EGroumGraph groum : groums) {
			for (EGroumNode node : groum.getNodes()) {
				node.setGraph(groum);
				String label = node.getLabel();
				HashSet<EGroumNode> nodes = nodesOfLabel.get(label);
				if (nodes == null)
					nodes = new HashSet<EGroumNode>();
				nodes.add(node);
				nodesOfLabel.put(label, nodes);
				if (node.isCoreAction()
						&& node.getAstNodeType() != ASTNode.ASSERT_STATEMENT
						&& node.getAstNodeType() != ASTNode.BREAK_STATEMENT
						&& node.getAstNodeType() != ASTNode.CONTINUE_STATEMENT
						&& node.getAstNodeType() != ASTNode.RETURN_STATEMENT
						&& node.getAstNodeType() != ASTNode.THROW_STATEMENT)
					coreLabels.add(label);
			}
		}
		Lattice l = new Lattice();
		l.setStep(1);
		lattices.add(l);
		for (String label : new HashSet<String>(nodesOfLabel.keySet())) {
			HashSet<EGroumNode> nodes = nodesOfLabel.get(label);
			if (nodes.size() < Pattern.minFreq/* || EGroumNode.isThisMethodCall(label)*/) {
				// FIXME
				for (EGroumNode node : nodes) {
					boolean isDefAction = false;
					if (node instanceof EGroumActionNode) {
						for (EGroumEdge e : node.getOutEdges())
							if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).getType() == Type.DEFINITION) {
								isDefAction = true;
								break;
							}
					}
					if (!isDefAction)
						node.getGraph().delete(node);
				}
				nodesOfLabel.remove(label);
			} else if (!coreLabels.contains(label))
				nodesOfLabel.remove(label);
			/*else if (nodes.size() > groums.size() * maxSingleNodePrevalence)
				nodesOfLabel.remove(label);*/
		}
		for (String label : nodesOfLabel.keySet()) {
			HashSet<EGroumNode> nodes = nodesOfLabel.get(label);
			HashSet<Fragment> fragments = new HashSet<>();
			for (EGroumNode node : nodes) {
				Fragment f = new Fragment(node);
				fragments.add(f);
			}
			Pattern p = new Pattern(fragments, fragments.size());
			extend(p);
		}
		System.out.println("Done mining.");
		Lattice.filter(lattices);
		System.out.println("Done filtering.");
		
		if (output_path != null) {
			report();
		}
		return getPatterns();
	}

	private void report() {
		File dir = new File(output_path, FileIO.getSimpleFileName(this.path) + "-" + (System.currentTimeMillis() / 1000));
		for (int step = Pattern.minSize; step <= lattices.size(); step++) {
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
					String fileName = f.getGraph().getFilePath();
					String name = f.getGraph().getName();
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
	
	private Set<Pattern> getPatterns() {
		Set<Pattern> patterns = new HashSet<>();
		for (int step = Pattern.minSize - 1; lattices.size() > step; step++) {
			Lattice lattice = lattices.get(step);
			patterns.addAll(lattice.getPatterns());
		}
		return patterns;
	}

	private void extend(Pattern pattern) {
		int patternSize = 0;
		if (pattern.getSize() >= Pattern.maxSize)
			for(EGroumNode node : pattern.getRepresentative().getNodes())
				if(node.isCoreAction())
					patternSize++;
		if(patternSize >= Pattern.maxSize) {
			pattern.add2Lattice(lattices);
			return;
		}
		HashMap<String, HashMap<Fragment, HashSet<ArrayList<EGroumNode>>>> labelFragmentExtendableNodes = new HashMap<>();
		for (Fragment f : pattern.getFragments()) {
			HashMap<String, HashSet<ArrayList<EGroumNode>>> xns = f.extend();
			for (String label : xns.keySet()) {
				HashMap<Fragment, HashSet<ArrayList<EGroumNode>>> fens = labelFragmentExtendableNodes.get(label);
				if (fens == null) {
					fens = new HashMap<>();
					labelFragmentExtendableNodes.put(label, fens);
				}
				fens.put(f, xns.get(label));
			}
		}
		for (String label : new HashSet<String>(labelFragmentExtendableNodes.keySet())) {
			HashMap<Fragment, HashSet<ArrayList<EGroumNode>>> fens = labelFragmentExtendableNodes.get(label);
			if (fens.size() < Pattern.minFreq)
				labelFragmentExtendableNodes.remove(label);
		}
		HashSet<Fragment> group = new HashSet<>(), frequentFragments = new HashSet<>();
		int xfreq = Pattern.minFreq - 1;
		for (String label : labelFragmentExtendableNodes.keySet()) {
			HashMap<Fragment, HashSet<ArrayList<EGroumNode>>> fens = labelFragmentExtendableNodes.get(label);
			HashSet<Fragment> xfs = new HashSet<>();
			for (Fragment f : fens.keySet()) {
				for (ArrayList<EGroumNode> ens : fens.get(f)) {
					Fragment xf = new Fragment(f, ens);
					xfs.add(xf);
				}
			}
			boolean isGiant = isGiant(xfs, pattern, label);
			System.out.println("\tTrying with label " + label + ": " + xfs.size());
			HashSet<Fragment> g = new HashSet<>();
			int freq = mine(g, xfs, pattern, isGiant, frequentFragments);
			if (freq > xfreq && !Lattice.contains(lattices, g)) {
				group = g;
				xfreq = freq;
			}
		}
		System.out.println("Done trying all labels");
		if (xfreq >= Pattern.minFreq) {
			HashSet<Fragment> inextensibles = new HashSet<>(pattern.getFragments());
			for (Fragment xf : frequentFragments) {
				inextensibles.remove(xf.getGenFragmen());
			}
			if (inextensibles.size() >= Pattern.minFreq) {
				int freq = computeFrequency(inextensibles, false);
				if (freq >= Pattern.minFreq) {
					Pattern ip = new Pattern(inextensibles, freq);
					ip.add2Lattice(lattices);
					pattern.getFragments().removeAll(inextensibles);
				}
			}
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
			pattern.clear();
			extend(xp);
			System.out.println("}");
		} else
			pattern.add2Lattice(lattices);
	}

	private boolean isGiant(HashSet<Fragment> xfs, Pattern pattern, String label) {
		return /*(EGroumNode.isMethod(label) || EGroumNode.isLiteral(label)) && */isGiant(xfs, pattern);
	}

	private boolean isGiant(HashSet<Fragment> xfs, Pattern pattern) {
		return pattern.getSize() > 1 
				&& (xfs.size() > Pattern.maxFreq || xfs.size() > pattern.getFragments().size() * pattern.getSize() * pattern.getSize());
	}

	private int mine(HashSet<Fragment> result, HashSet<Fragment> fragments, Pattern pattern, boolean isGiant, HashSet<Fragment> frequentFragments) {
		HashMap<Integer, HashSet<Fragment>> buckets = new HashMap<>();
		for (Fragment f : fragments) {
			int h = f.getVectorHashCode();
			HashSet<Fragment> bucket = buckets.get(h);
			if (bucket == null) {
				bucket = new HashSet<>();
				buckets.put(h, bucket);
			}
			bucket.add(f);
		}
		HashSet<HashSet<Fragment>> groups = new HashSet<>();
		for (int h : buckets.keySet()) {
			HashSet<Fragment> bucket = buckets.get(h);
			group(groups, bucket);
		}
		HashSet<Fragment> group = new HashSet<>();
		int xfreq = Pattern.minFreq - 1;
		for (HashSet<Fragment> g : groups) {
			int freq = computeFrequency(g, isGiant && isGiant(g, pattern));
			if (freq >= Pattern.minFreq)
				frequentFragments.addAll(g);
			if (freq > xfreq) {
				group = g;
				xfreq = freq;
			}
		}
		result.addAll(group);
		return xfreq;
	}

	private int computeFrequency(HashSet<Fragment> fragments, boolean isGiant) {
		HashMap<EGroumGraph, ArrayList<Fragment>> fragmentsOfGraph = new HashMap<EGroumGraph, ArrayList<Fragment>>();
		for (Fragment f : fragments) {
			EGroumGraph g = f.getGraph();
			ArrayList<Fragment> fs = fragmentsOfGraph.get(g);
			if (fs == null)
				fs = new ArrayList<Fragment>();
			fs.add(f);
			fragmentsOfGraph.put(g, fs);
		}
		int freq = 0;
		for (EGroumGraph g : fragmentsOfGraph.keySet()) {
			ArrayList<Fragment> fs = fragmentsOfGraph.get(g);
			int i = 0;
			while (i < fs.size()) {
				Fragment f = fs.get(i);
				int j = i + 1;
				while (j < fs.size()) {
					if (f.overlap(fs.get(j))) {
						if (isGiant)
							fragments.remove(fs.get(j));
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
		HashSet<Fragment> fs = new HashSet<>();
		group.add(f);
		fs.add(f.getGenFragmen());
		bucket.remove(f);
		for (Fragment g : new HashSet<Fragment>(bucket)) {
			if (f.getVector().equals(g.getVector())) {
				group.add(g);
				fs.add(g.getGenFragmen());
				bucket.remove(g);
			}
		}
		if (fs.size() >= Pattern.minFreq && group.size() >= Pattern.minFreq) {
			removeDuplicates(group);
			if (group.size() >= Pattern.minFreq)
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
