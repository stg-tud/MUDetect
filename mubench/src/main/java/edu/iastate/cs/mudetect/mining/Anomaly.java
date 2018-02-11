package edu.iastate.cs.mudetect.mining;

import java.util.ArrayList;
import java.util.HashSet;

public class Anomaly {
	double score;
	int patternFreq;
	HashSet<Fragment> instances = new HashSet<>();
	Fragment pattern;

	public Anomaly(double rareness, int pfreq, HashSet<Fragment> inextensibles, HashSet<Fragment> group) {
		this.score = rareness;
		this.patternFreq = pfreq;
		for (Fragment f : inextensibles)
			instances.add(new Fragment(f, new ArrayList<>()));
		for (Fragment f : group) {
			pattern = new Fragment(f, new ArrayList<>());
			break;
		}
	}

	public double getScore() {
		return score;
	}

	public int getPatternFreq() {
		return patternFreq;
	}

	public HashSet<Fragment> getInstances() {
		return instances;
	}

	public Fragment getPattern() {
		return pattern;
	}
}
