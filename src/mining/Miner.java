/**
 * 
 */
package mining;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import egroum.EGroumNode;
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

	public void mine(ArrayList<EGroumGraph> groums) {
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
			if (nodes.size() < Pattern.minFreq || EGroumNode.isThisMethodCall(label)) {
				for (EGroumNode node : nodes)
					node.getGraph().delete(node);
				nodesOfLabel.remove(label);
			} else if (!coreLabels.contains(label))
				nodesOfLabel.remove(label);
			else if (nodes.size() > groums.size() * maxSingleNodePrevalence)
				nodesOfLabel.remove(label);
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
			if (Pattern.minSize > 1)
				p.clear();
		}
		System.out.println("Done mining.");
		Lattice.filter(lattices);
		System.out.println("Done filtering.");
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
		HashSet<Fragment> group = new HashSet<>();
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
			int freq = mine(g, xfs, pattern, isGiant);
			if (freq > xfreq && !Lattice.contains(lattices, g)) {
				group = g;
				xfreq = freq;
			}
		}
		System.out.println("Done trying all labels");
		if (xfreq >= Pattern.minFreq) {
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

	private int mine(HashSet<Fragment> result, HashSet<Fragment> fragments, Pattern pattern, boolean isGiant) {
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
		f.setGenFragmen(null);
		for (Fragment g : new HashSet<Fragment>(bucket)) {
			if (f.getVector().equals(g.getVector())) {
				group.add(g);
				fs.add(g.getGenFragmen());
				bucket.remove(g);
				g.setGenFragmen(null);
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
