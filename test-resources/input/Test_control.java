package input;

class Test_control {
	public void misuse(Object obj) throws InterruptedException {
		synchronized (obj) {
			if (isAvailable(obj)) {
				obj.wait();
			}
			// Perform action appropriate to condition
		}
	}

	public void misuse(Object obj) throws InterruptedException {
		synchronized (obj) {
			while (isAvailable(obj)) {
				obj.wait();
			}
			// Perform action appropriate to condition
		}
	}
	  
	private static boolean isAvailable(Object o) { return false; }
}