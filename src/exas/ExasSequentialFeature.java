package exas;

import java.util.ArrayList;
import java.util.List;

public class ExasSequentialFeature extends ExasFeature {
	private ArrayList<ExasSingleFeature> sequence = new ArrayList<ExasSingleFeature>();
	
	/*public ExasSequentialFeature(ArrayList<ExasSingleFeature> sequence) {
		this.sequence = new ArrayList<ExasSingleFeature>(sequence);
		ExasFeature pre = sequence.get(0);
		int i = 1;
		while (i < sequence.size())
		{
			ExasSingleFeature s = sequence.get(i);
			if (pre.next.containsKey(s))
			{
				pre = pre.next.get(s);
			}
			else
			{
				pre = new ExasSequentialFeature(sequence.subList(0, i+1), pre, s);
			}
			i++;
		}
	}*/
	
	public ExasSequentialFeature(List<ExasSingleFeature> sequence, ExasFeature pre, ExasSingleFeature s) {
		super();
		this.sequence = new ArrayList<ExasSingleFeature>(sequence);
		pre.next.put(s, this);
		if (pre.next.size() > numOfBranches)
			numOfBranches = pre.next.size();
	}

	public ArrayList<ExasSingleFeature> getSequence() {
		return sequence;
	}

	public void setSequence(ArrayList<ExasSingleFeature> sequence) {
		this.sequence = sequence;
	}

	@Override
	public int getFeatureLength() {
		return sequence.size();
	}

	@Override
	public String toString() {
		return sequence.toString();
	}
}
