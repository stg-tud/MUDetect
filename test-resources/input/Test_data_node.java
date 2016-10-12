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
}