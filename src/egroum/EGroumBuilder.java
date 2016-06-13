package egroum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.sun.org.apache.bcel.internal.classfile.ClassFormatException;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.generic.Type;

import utils.FileIO;
import utils.JavaASTUtil;

public class EGroumBuilder {
	private ArrayList<EGroumGraph> egroums = new ArrayList<>();
	private String path;
	
	public EGroumBuilder(String path) {
		this.path = path;
	}
	
	public void build() {
		buildStandardJars();
		buildHierarchy(new File(path));
		build(new File(path));
	}

	private void buildStandardJars() {
		String jrePath = System.getProperty("java.home") + "/lib";
		buildJar(jrePath + "/rt.jar");
	}

	private void buildHierarchy(File file) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				buildHierarchy(sub);
		} else if (file.isFile()) {
			if (file.getName().endsWith(".jar"))
				buildJar(file.getAbsolutePath());
			else if (file.getName().endsWith(".java")) {
				CompilationUnit cu = (CompilationUnit) JavaASTUtil.parseSource(FileIO.readStringFromFile(file.getAbsolutePath()));
			}
		}
	}

	private void buildJar(String jarFilePath) {
		try {
			JarFile jarFile = new JarFile(jarFilePath);
			Enumeration<JarEntry> entries = jarFile.entries();
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if(entry.getName().endsWith(".class") && entry.getName().startsWith("java")) {
					try {
						ClassParser parser = new ClassParser(jarFilePath, entry.getName());
						JavaClass jc = parser.parse();
						String className = jc.getClassName();
						HashMap<String, String> fieldTypes = EGroumBuildingContext.typeFieldTypes.get(className);
						if (fieldTypes == null)
							fieldTypes = new HashMap<>();
						for (Field field : jc.getFields()) {
							String name = field.getName();
							if (name.startsWith("this$"))
								continue;
							String type = getSimpleType(field.getType());
							fieldTypes.put(name, type);
						}
						if (!fieldTypes.isEmpty())
							EGroumBuildingContext.typeFieldTypes.put(className, fieldTypes);
					} catch (IOException | ClassFormatException e) {
						System.err.println(jarFilePath);
						e.printStackTrace();
					}
				}
			}
			jarFile.close();
		} catch (IOException e) {
			System.err.println(jarFilePath);
			e.printStackTrace();
		}
	}

	private String getSimpleType(Type type) {
		return type.toString();
	}

	private void build(File file) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				build(sub);
		} else if (file.isFile() && file.getName().endsWith(".java")) {
			
		}
	}
}
