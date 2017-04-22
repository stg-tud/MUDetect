package input;

import java.util.LinkedHashMap;

class Test_non_determinism3 {
	
	class Doc {
		LinkedHashMap<String, Integer> map;
	}
	
	Doc doc;

	boolean documentParam(String parameter, Integer description) {
		if (!m())
			return true;
		
		if (doc.map == null) {
			doc = new Doc();
			doc.map = new LinkedHashMap<>();
		}
		
		if (!doc.map.containsKey(parameter)) {
			doc.map.put(parameter, description + Integer.MIN_VALUE);
			return true;
		} else
			return false;
	}
}