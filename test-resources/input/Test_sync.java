package input;

import java.util.HashMap;

class Test_sync {
	private HashMap<String, String> map;

	public void onEvent(String sender, String event) {
		synchronized (map) {
			map.put(sender, event);
		}
	}

	public void pattern(Object o) {
		synchronized (o) {
			o.hashCode();
			o.hashCode();
		}
	}
}