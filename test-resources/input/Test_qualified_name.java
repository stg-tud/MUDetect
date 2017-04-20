package input;

class Test_qualified_name {
	int f0;
	class C {
		F f;
	}
	
	private synchronized void setRollbackSegmentInfos(C c) {
		f0 = 1;
		c.f = (F) c.clone();
		c.f.hashCode(f0);
	}
}