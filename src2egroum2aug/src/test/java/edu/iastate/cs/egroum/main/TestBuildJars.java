package edu.iastate.cs.egroum.main;


import edu.iastate.cs.egroum.aug.AUGBuilder;
import edu.iastate.cs.egroum.aug.AUGConfiguration;

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
