package input;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

class Test_order {
	public void misuse(JFrame f, Dimension d) {
		f.pack();
		f.setPreferredSize(d);
	}

	public void pattern(JFrame f, Dimension d) {
		f.setPreferredSize(d);
		f.pack();
	}

	public String getIdGeneratorMap() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, List<Replacement>> entry :
			idGeneratorMaps.entrySet()) {
			sb.append("[");
			sb.append(entry.getKey());
			sb.append("]\n\n");
			for (Replacement replacement : entry.getValue()) {
				sb.append(replacement.toString());
				sb.append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}