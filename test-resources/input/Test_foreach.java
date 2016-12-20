package input;

import java.util.HashMap;

class Test_foreach {
    HashMap<String, Object> attr = getAttributes();
	
	void m() {
        for (Map.Entry<String, Object> entry : entrySet()) {
            String name = entry.getKey();
            System.out.println(name);
        }
	}
}