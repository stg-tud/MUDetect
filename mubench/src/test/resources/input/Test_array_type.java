package input;

class Test_array_type extends BufferedIndexOutput {
	
	void m1() {
		int[] a = new int[2];
		System.out.println(a);
	}
	
	void m2() {
		int[] a = new int[]{1 , 2};
		System.out.println(a);
	}
	
	void m3() {
		Test[] a = new Test[]{t1 , t2};
		System.out.println(a);
	}
}