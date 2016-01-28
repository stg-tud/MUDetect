package exas;

import java.util.HashMap;

public class ExasSingleFeature extends ExasFeature {
	public static HashMap<String, ExasSingleFeature> features = new HashMap<String, ExasSingleFeature>();
	private String label;
	
	public ExasSingleFeature(String label) {
		super();
		this.label = label;
		features.put(label, this);
	}
	
	public ExasSingleFeature(int labelId) {
		super();
		this.label = String.valueOf(labelId);
		features.put(label, this);
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int getFeatureLength() {
		return 1;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
}
