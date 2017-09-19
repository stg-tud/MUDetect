package main;

import de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilder;
import de.tu_darmstadt.stg.mudetect.src2aug.AUGConfiguration;

public class TestBuildJars {
	String i;
	
	class Inner {
		int i;
		
		void m() {
			i = 0;
		}
	}
	
	public static void main(String[] args) {
		AUGBuilder b = new AUGBuilder(new AUGConfiguration());
		b.build("", new String[]{});
	}

}
