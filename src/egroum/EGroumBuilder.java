package egroum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.ClassFormatException;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.classfile.Utility;
import com.sun.org.apache.bcel.internal.generic.Type;

import utils.FileIO;
import utils.JavaASTUtil;

public class EGroumBuilder {
	private ArrayList<EGroumGraph> egroums = new ArrayList<>();
	private String path;
	private int consumed_chars;
	
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
				for (int i = 0 ; i < cu.types().size(); i++)
					buildHierarchy((AbstractTypeDeclaration) cu.types().get(i), cu.getPackage().getName().getFullyQualifiedName() + ".");
			}
		}
	}

	private void buildHierarchy(AbstractTypeDeclaration type, String prefix) {
		if (type instanceof TypeDeclaration)
			buildHierarchy((TypeDeclaration) type, prefix);
		else if (type instanceof EnumDeclaration)
			buildHierarchy((EnumDeclaration) type, prefix);
		else if (type instanceof AnnotationTypeDeclaration)
			buildHierarchy((AnnotationTypeDeclaration) type, prefix);
	}

	private void buildHierarchy(TypeDeclaration type, String prefix) {
		String className = prefix + type.getName().getIdentifier();
		HashMap<String, String> fieldTypes = EGroumBuildingContext.typeFieldType.get(className);
		if (fieldTypes == null)
			fieldTypes = new HashMap<>();
		for (FieldDeclaration field : type.getFields())
			buildHierarchy(field, fieldTypes);
		if (!fieldTypes.isEmpty())
			EGroumBuildingContext.typeFieldType.put(className, fieldTypes);
		HashMap<String, HashSet<String>> methodExceptions = EGroumBuildingContext.typeMethodExceptions.get(className);
		if (methodExceptions == null)
			methodExceptions = new HashMap<>();
		for (MethodDeclaration method : type.getMethods())
			buildHierarchy(method, methodExceptions);
		if (!methodExceptions.isEmpty())
			EGroumBuildingContext.typeMethodExceptions.put(className, methodExceptions);
		for (TypeDeclaration inner : type.getTypes())
			buildHierarchy(inner, className + ".");
	}

	private void buildHierarchy(EnumDeclaration ed, String prefix) {
		String className = prefix + ed.getName().getIdentifier();
		HashMap<String, String> fieldTypes = EGroumBuildingContext.typeFieldType.get(className);
		if (fieldTypes == null)
			fieldTypes = new HashMap<>();
		HashMap<String, HashSet<String>> methodExceptions = EGroumBuildingContext.typeMethodExceptions.get(className);
		if (methodExceptions == null)
			methodExceptions = new HashMap<>();
		for (int i = 0; i < ed.bodyDeclarations().size(); i++) {
			BodyDeclaration bd = (BodyDeclaration) ed.bodyDeclarations().get(i);
			if (bd instanceof FieldDeclaration)
				buildHierarchy((FieldDeclaration) bd, fieldTypes);
			else if (bd instanceof MethodDeclaration)
				buildHierarchy((MethodDeclaration) bd, methodExceptions);
		}
		if (!fieldTypes.isEmpty())
			EGroumBuildingContext.typeFieldType.put(className, fieldTypes);
		if (!methodExceptions.isEmpty())
			EGroumBuildingContext.typeMethodExceptions.put(className, methodExceptions);
	}

	private void buildHierarchy(AnnotationTypeDeclaration type, String prefix) {
		// TODO
	}

	private void buildHierarchy(FieldDeclaration f, HashMap<String, String> fieldTypes) {
		String type = JavaASTUtil.getSimpleType(f.getType());
		for (int j = 0; j < f.fragments().size(); j++) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) f.fragments().get(j);
			fieldTypes.put(vdf.getName().getIdentifier(), type);
		}
	}

	private void buildHierarchy(MethodDeclaration method, HashMap<String, HashSet<String>> methodExceptions) {
		String name = method.getName().getIdentifier();
		HashSet<String> exceptions = methodExceptions.get(name);
		if (exceptions == null)
			exceptions = new HashSet<>();
		for (int i = 0; i < method.thrownExceptions().size(); i++)
			exceptions.add(JavaASTUtil.getSimpleName((Name)method.thrownExceptions().get(i)));
		if (!exceptions.isEmpty())
			methodExceptions.put(name, exceptions);
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
						className = className.replace('$', '.');
						HashMap<String, String> fieldTypes = EGroumBuildingContext.typeFieldType.get(className);
						if (fieldTypes == null)
							fieldTypes = new HashMap<>();
						for (Field field : jc.getFields())
							buildJar(field, fieldTypes);
						if (!fieldTypes.isEmpty())
							EGroumBuildingContext.typeFieldType.put(className, fieldTypes);
						HashMap<String, HashSet<String>> methodExceptions = EGroumBuildingContext.typeMethodExceptions.get(className);
						if (methodExceptions == null)
							methodExceptions = new HashMap<>();
						for (Method method : jc.getMethods())
							buildJar(method, methodExceptions);
						if (!methodExceptions.isEmpty())
							EGroumBuildingContext.typeMethodExceptions.put(className, methodExceptions);
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

	private void buildJar(Field field, HashMap<String, String> fieldTypes) {
		String name = field.getName();
		if (name.startsWith("this$"))
			return;
		String type = getSimpleType(field.getType());
		fieldTypes.put(name, type);
	}

	private void buildJar(Method method, HashMap<String, HashSet<String>> methodExceptions) {
		String name = method.getName();
		HashSet<String> exceptions = methodExceptions.get(name);
		if (exceptions == null)
			exceptions = new HashSet<>();
		if (method.getExceptionTable() != null)
			for (String exception: method.getExceptionTable().getExceptionNames())
				exceptions.add(exception.substring(exception.lastIndexOf('.') + 1));
		if (!exceptions.isEmpty())
			methodExceptions.put(name, exceptions);
	}
	
	/*
	 * Modify com.sun.org.apache.bcel.internal.generic.Type.toString()
	 */
	private String getSimpleType(Type type) {
	    return ((type.equals(Type.NULL) || (type.getType() >= Constants.T_UNKNOWN)))? type.getSignature() : signatureToString(type.getSignature());
	}
	
	/*
	 * Modify com.sun.org.apache.bcel.internal.classfile.Utility.signatureToString(signature, false)
	 */
	private String signatureToString(String signature) {
	    consumed_chars = 1; // This is the default, read just one char like `B'

	    try {
	      switch(signature.charAt(0)) {
	      case 'B' : return "number"; //return "byte";
	      case 'C' : return "char";
	      case 'D' : return "number"; //return "double";
	      case 'F' : return "number"; //return "float";
	      case 'I' : return "number"; //return "int";
	      case 'J' : return "number"; //return "long";

	      case 'L' : { // Full class name
	        int    index = signature.indexOf(';'); // Look for closing `;'

	        if(index < 0)
	          throw new ClassFormatException("Invalid signature: " + signature);

	        consumed_chars = index + 1; // "Lblabla;" `L' and `;' are removed

	        return compactClassName(signature.substring(1, index));
	      }

	      case 'S' : return "number"; //return "short";
	      case 'Z' : return "boolean";

	      case '[' : { // Array declaration
	        int n;
	        StringBuffer brackets;
	        int consumed_chars; // Shadows global var

	        brackets = new StringBuffer(); // Accumulate []'s

	        // Count opening brackets and look for optional size argument
	        for(n=0; signature.charAt(n) == '['; n++)
	          brackets.append("[]");

	        consumed_chars = n; // Remember value

	        // The rest of the string denotes a `<field_type>'
	        String type = signatureToString(signature.substring(n));

	        this.consumed_chars += consumed_chars;
	        return type + brackets.toString();
	      }

	      case 'V' : return "void";

	      default  : throw new ClassFormatException("Invalid signature: `" +
	                                            signature + "'");
	      }
	    } catch(StringIndexOutOfBoundsException e) { // Should never occur
	      throw new ClassFormatException("Invalid signature: " + e + ":" + signature);
	    }
	}
	
	/*
	 * Modify com.sun.org.apache.bcel.internal.classfile.Utility.compactClassName(long class name, false)
	 */
	public static final String compactClassName(String className) {
		int index = className.indexOf('<');
		if (index > -1)
			className = className.substring(0, index);
		className = className.replace('/', '.'); // Is `/' on all systems, even DOS
		className = className.substring(className.lastIndexOf('.') + 1);
		return className;
	}

	private void build(File file) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				build(sub);
		} else if (file.isFile() && file.getName().endsWith(".java")) {
			
		}
	}
}
