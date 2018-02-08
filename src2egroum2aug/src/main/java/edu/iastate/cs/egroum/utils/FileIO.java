/**
 * 
 */
package edu.iastate.cs.egroum.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * @author hoan
 *
 */
public class FileIO {
	public static final String outputDirPath = "D:/Subject systems/webpatterns/output";
	public static PrintStream logStream;
	static {
		try {
			logStream = new PrintStream(new FileOutputStream("log.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static String getSimpleFileName(String fileName)
	{
		char separator = '/';
		if(fileName.lastIndexOf('\\') > -1)
			separator = '\\';
		int start = fileName.lastIndexOf(separator) + 1;
		int end = fileName.lastIndexOf('.');
		if(end <= start)
			end = fileName.length();
		
		return fileName.substring(start, end);
	}
	
	/*public static String getSimpleClassName(String className)
	{
		String name = className.substring(className.lastIndexOf('.') + 1);
		int start = 0;
		while (start < name.length() && !Character.isJavaIdentifierPart(name.charAt(start)))
			start++;
		int end = name.length()-1;
		while (end >= 0 && !Character.isJavaIdentifierPart(name.charAt(end)))
			end--;
		if (end < start)
			return null;
		return name.substring(start, end+1);
	}*/
	public static String getSimpleClassName(String className)
	{
		String name = className.substring(className.lastIndexOf('.') + 1);
		return name;
	}
	
	public static String getSVNRepoRootName(String url)
	{
		String name = "";
		int end = url.length() - 1;
		while (url.charAt(end) == '/' && end >= 0)
			end--;
		if (end >= 0)
		{
			int start = url.lastIndexOf('/', end);
			if (start <= end)
				name = url.substring(start+1, end+1);
		}
		
		return name;
	}
	
	public static String[] splitFileName(String fileName)
	{
		char separator = '/';
		if(fileName.lastIndexOf('\\') > -1)
			separator = '\\';
		int start = fileName.lastIndexOf(separator) + 1;
		int end = fileName.lastIndexOf('.');
		if(end <= start)
			end = fileName.length() + 1;
		String[] names = new String[2];
		names[0] = fileName.substring(0, start-1);
		names[1] = fileName.substring(start, end);
		return names;
	}
	
	/*public static String getFileContent(File file)
	{
		StringBuffer strBuf = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
	    	String line = "";
	    	while ((line = in.readLine()) != null) { 
	    		strBuf.append(line + "\r\n");
	    	}
	    	in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}*/
	
	public static String readStringFromFile(String inputFile) {
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));
			byte[] bytes = new byte[(int) new File(inputFile).length()];
			in.read(bytes);
			in.close();
			return new String(bytes);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void writeStringToFile(String string, String outputFile) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write(string);
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			/*e.printStackTrace();
			System.exit(0);*/
			System.err.println(e.getMessage());
		}
	}
	
	/*
	 * Read/write an object
	 */
	
	public static void writeObjectToFile(Object object, String objectFile, boolean append) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(objectFile, append)));
			out.writeObject(object);
			out.flush();
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static Object readObjectFromFile(String objectFile) {
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(objectFile)));
			Object object = in.readObject();
			in.close();
			return object;
		}
		catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<File> getPaths(File dir) {
		ArrayList<File> files = new ArrayList<>();
		if (dir.isDirectory())
			for (File sub : dir.listFiles())
				files.addAll(getPaths(sub));
		else if (dir.getName().endsWith(".java"))
			files.add(dir);
		return files;
	}
	
	public static int countLOC(File file, String extension)
	{
		int numOfLines = 0;
		if(file.isDirectory())
		{
			for(File sub : file.listFiles())
				numOfLines += countLOC(sub, extension);
		}
		else if(file.getName().endsWith("." + extension))
		{
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
		    	while (in.readLine() != null) {
		    		numOfLines++;
		    	}
		    	in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return numOfLines;
	}
	
	public static String getHtmlPageContent(String url, String query, String charset) throws MalformedURLException, IOException
	{
		URLConnection connection = new URL(url + "?" + query).openConnection();
		connection.setRequestProperty("Accept-Charset", charset);
		//System.out.println(connection.getReadTimeout());
		connection.setReadTimeout(10000);
		InputStream response = connection.getInputStream();
		StringBuilder sb = new StringBuilder();
		BufferedInputStream in = new BufferedInputStream(response);
		byte[] bytes = new byte[10000];
		int len = in.read(bytes);
		while (len != -1)
		{
			//System.out.println(len);
			//System.out.println(new String(bytes, 0, len));
			sb.append(new String(bytes, 0, len));
			//Thread.sleep(100);
			len = in.read(bytes);
		}
		in.close();
		//System.out.println(len);
		//System.out.println(sb.toString());
		/*Scanner sc = new Scanner(response);
		while (sc.hasNextLine())
		{
			System.out.println(sc.nextLine());
		}*/
		/*BufferedReader in = new BufferedReader(new InputStreamReader(response));
		String inputLine;
        while ((inputLine = in.readLine()) != null)
        {
            System.out.println(inputLine);
            System.out.println("Here!!!");
        }
        in.close();*/
		return sb.toString();
	}
	
	public static ArrayList<String> getAllFilesInFolder(String folder) {
		ArrayList<String> allFiles = new ArrayList<String>();
		for (File file : new File(folder).listFiles()) {
			if (file.isFile())
			{
				System.out.println(file.getName() + ":" + file.length());
				allFiles.add(file.getPath());
			}
			else
				allFiles.addAll(getAllFilesInFolder(file.getPath()));
		}
		return allFiles;
	}

	public static int countLOC(String source) {
		int num = 0;
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == '\n')
				num++;
		}	
		return num;
	}
	
}
