package input;

class Test_data_node extends BufferedIndexOutput {
	
	  void pattern(InputStream in) throws IOException {
	      byte[] spoolBuffer = new byte[0x2000];
	      int read;
	      try {
	        while ((read = in.read(spoolBuffer)) > 0) {
	          // do something...
	        }
	      } finally {
	          in.close();
	      }
	  }
	  
	  int v1, v2, v3, v4;
	  void m() {
		  v1 = m1();
		  v2 = m2(v1);
		  v3 = m3(v1);
		  m4(v2, v3);
		  v1 = m1();
		  v2 = m2(v1);
		  v3 = m3(v1);
		  m4(v2, v3);
		  m4(v2, v3);
	  }
}