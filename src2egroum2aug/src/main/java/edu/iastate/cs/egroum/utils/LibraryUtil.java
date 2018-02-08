package edu.iastate.cs.egroum.utils;

import java.util.HashSet;
import java.util.Scanner;

public class LibraryUtil {
	public static HashSet<String> jdkClassQualifiedNames = new HashSet<String>();
	public static HashSet<String> jdkLangClassSimpleNames = new HashSet<String>();
	
	static {
		String content = FileIO.readStringFromFile("jdk-class-qual-names.csv");
		Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			jdkClassQualifiedNames.add(sc.nextLine());
		}
		sc.close();
		content = FileIO.readStringFromFile("jdk-lang-class-names.csv");
		sc = new Scanner(content);
		while (sc.hasNextLine()) {
			jdkLangClassSimpleNames.add(sc.nextLine());
		}
		sc.close();
	}
}
