package input;

import java.util.Collection;
import java.util.Iterator;

class Test_filter_API {
	public <E> void m(Collection<E> c) {
		Iterator<E> i = c.iterator();
		while (i.hasNext())
			i.next();
	}
}