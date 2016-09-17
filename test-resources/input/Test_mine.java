package input;

import java.util.Iterator;
import java.util.List;

class Test_mine {
	
	public void m1(List l) {
		Iterator it = l.iterator();
		if (it.hasNext()) {
			it.next();
			it.remove();
		}
	}
	
	public void m2(List l) {
		Iterator it = l.iterator();
		if (it.hasNext()) {
			it.next();
			it.remove();
		}
	}
	
	public void m3(List l) {
		Iterator it = l.iterator();
		if (it.hasNext()) {
			it.next();
		}
	}
	
	public void m4(List l) {
		Iterator it = l.iterator();
		if (it.hasNext()) {
			it.next();
		}
	}
}