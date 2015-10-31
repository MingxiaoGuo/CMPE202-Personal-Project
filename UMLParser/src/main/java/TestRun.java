import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;


public class TestRun {
	private static String inputPath = "";
	private static String outputFileName = "";
	private static HashSet<String> classList = new HashSet<String>();
	private static ArrayList<RealClass> classes = new ArrayList<RealClass>();
	private static ArrayList<RelationList> relationLists = new ArrayList<RelationList>();
	private static Properties properties = System.getProperties();
	
	public static void main(String[] args) {
		inputPath = args[0];
		outputFileName = args[1] + ".jpg";
		String folderPath = inputPath;
		
		ArrayList<File> files = getFiles(folderPath);
		String filePath;
		String osName = properties.getProperty("os.name").toLowerCase();
		// Windows and Mac has two different representations of file path
		// Change the path according to OS
		for (int i = 0; i < files.size(); i++) {
			if (osName.contains("windows")) {
				filePath = folderPath + "\\" + files.get(i).getName();
			} else {
				filePath = folderPath + "/" + files.get(i).getName();
			}
			classes.add(new RealClass(filePath, classList));
		}
		
		for (RealClass realClass : classes) {
			realClass.alterMethodList(classes);
			realClass.alterAttributeList(classes);
			realClass.findDependBody(classes);
		}
		CheckRelations checkRelations;
		if (classes != null && classes.size() != 0) {
			for (RealClass realClass : classes) {
				relationLists.add(realClass.getRelationList());
			}
			checkRelations = new CheckRelations(relationLists);
			checkRelations.checkAll();
			checkRelations.checkExtends(classes);
			checkRelations.checkImplements(classes);
			checkRelations.freshClassRelation(classes);
		}
		String secondURL = "";
		if (classes != null && classes.size() != 0) {
			String allClassUML = "";
			String allRelationUML = "";
			for (RealClass realClass : classes) {
				realClass.convertUMLQuery(classes);
				allClassUML += realClass.getUMLClassDetail();
				allRelationUML += realClass.getUMLClassRelation();
				realClass.displayClass();			
			}
			secondURL += allClassUML + allRelationUML;
			//System.out.println(secondURL);
		}
		String firstURL = "http://yuml.me/diagram/scruffy/class/";
		try {
			String getURL = firstURL + secondURL;
			URL getUrl = new URL(getURL);  
			GetUML getUML = new GetUML(getUrl, outputFileName);
			getUML.saveImage();
			System.out.println("image saved");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	

	@SuppressWarnings("unused")
	private static void writeFile(String UMLString) {
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream("a.txt", true);
			fileOutputStream.write(UMLString.getBytes());
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static ArrayList<File> getFiles(String dir) {
		classList = new HashSet<String>();
		File f = new File(dir);
		File[] allFiles = f.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isFile() ? true : false;
			}
		});
		String fileName;
		ArrayList<File> javaFiles = new ArrayList<File>();
		for (File file : allFiles) {
			fileName = file.getName();
			if (fileName.endsWith(".java")) {
				javaFiles.add(file);
				classList.add(fileName.substring(0, fileName.length() - 1 - 4));
			}
		}
		return javaFiles;
	}

}
