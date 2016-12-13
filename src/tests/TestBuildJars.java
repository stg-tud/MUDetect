package tests;

import egroum.AUGConfiguration;
import egroum.EGroumBuilder;

public class TestBuildJars {
	String i;
	
	class Inner {
		int i;
		
		void m() {
			i = 0;
		}
	}
	
	public static void main(String[] args) {
		EGroumBuilder b = new EGroumBuilder(new AUGConfiguration());
		b.build("", new String[]{});
	}

}
