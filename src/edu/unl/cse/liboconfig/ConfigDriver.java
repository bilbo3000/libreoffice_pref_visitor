package edu.unl.cse.liboconfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/** 
 * Driver class that initiate the traversal. 
 * @author Dongpu Jin
 *
 */
public class ConfigDriver {
	public static void main(String[] args) {
		try {
			// Connect to running office process 
			LiboConfigVisitor firstContact = new LiboConfigVisitor();
			System.out.println("Connected to a running office ...");
			
			firstContact.connect("localhost", 5678);
			System.out.println("Connection succeed!");
			
			// Traverse libreoffice configuration heirarchy
			firstContact.visit();
			
			// Analyze the data
			DataAnalyzer analyzer = DataAnalyzer.getInstance();
			analyzer.analyze(); 

//			ArrayList<String> allPrefs = firstContact.getAllPrefs();
//			Set<String> distinctPrefs = firstContact.getDistinctPrefs();
//			Set<String> distinctPrefNames = firstContact.getDistinctPrefNames(); 
//			System.out.println("all prefs size: " + allPrefs.size());
//			System.out.println("distinct pref size: " + distinctPrefs.size());
//			System.out.println("boolean count: " + firstContact.getBoolCount());
//			System.out.println("int count: " + firstContact.getIntCount());
//			System.out.println("distinct pref name size: " + distinctPrefNames.size()); 
//			
//			// Print other types
//			File otherFile = new File("othertypes.txt");
//			FileOutputStream otherFileOutputStream = new FileOutputStream(otherFile);
//			PrintWriter pw = new PrintWriter(otherFileOutputStream); 
//			Map<String, String> otherTypes = firstContact.getOtherTypes(); 
//			Set<String> keys = otherTypes.keySet(); 
//			
//			for (String key : keys) {
//				pw.write(key + "=" + otherTypes.get(key) + '\n');  
//			}
//			
//			pw.close(); 
//			
//			// Print same name but different prefs
//			File sameNameFile = new File("samename.txt");
//			FileOutputStream sameNameFileOutputStream = new FileOutputStream(sameNameFile); 
//			PrintWriter pw2 = new PrintWriter(sameNameFileOutputStream);
//			Map<String, ArrayList<String> > sameName = firstContact.getSameName(); 
//			keys = sameName.keySet(); 
//			
//			for (String key : keys) {
//				if (sameName.get(key).size() > 1 && sameName.get(key).size() <= 5 ){  // Only print ones with multiple names
//					pw2.write(key + ": {\n"); 
//					for (String item : sameName.get(key)) {
//						pw2.write("---" + item + '\n');
//					}
//					pw2.write("}\n\n\n"); 
//				}
//			}
//			
//			// Print distinct categories
//			File distinctCategoryFile = new File("distinctCategory.txt");
//			FileOutputStream distinctCategoryOutputStream = new FileOutputStream(distinctCategoryFile); 
//			PrintWriter pw3 = new PrintWriter(distinctCategoryOutputStream); 
//			Map<String, Map<String, ArrayList<String> > > category = firstContact.getDistinctCategory();
//			System.out.println("Number of categories: " + category.size()); 
//			
//			keys = category.keySet(); 
//			String[] keysArr = keys.toArray(new String[0]);
//			Arrays.sort(keysArr); 
//			for (String key : keysArr) {
//				if (key.equals("org.openoffice.Office.Writer")) {  // Only look at writer for now
//					pw3.write(key + ":"); 
//					Map<String, ArrayList<String> > tempMap = category.get(key); 
//					// pw3.write(Integer.toString(tempSet.size()) + '\n'); 
//					pw3.write("{\n"); 
//					Set<String> tempMapKeys = tempMap.keySet(); 
//					for (String name : tempMapKeys) {
//						pw3.write("\t\t" + name + ":\n");
//						for (String path : tempMap.get(name)) {
//							pw3.write("\t\t\t" + path + '\n'); 
//						}
//					}
//					pw3.write("}\n"); 
//				}
//			}
//			pw3.close(); 
//			
//			// Collect prefs for prefs for different applications.  
//			Map<String, ArrayList<String> > appValues = firstContact.getAppValues(); 
//			DataCollector dc = new DataCollector(); 
//			dc.setAppValues(appValues); 
//			dc.genAppValuesData(); 
//			dc.printAppValueData(); 
			
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
