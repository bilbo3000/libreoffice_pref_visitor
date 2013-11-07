package edu.unl.cse.liboconfig;

import java.util.*; 
import java.io.*;  

/**
 * Singleton class of data analyzer. 
 * @author djin
 *
 */
public class DataAnalyzer {	
	
	private Map<String, ArrayList<String> > appValues; 
	private Map<String, int[]> appValuesCount = new HashMap<String, int[]>(); 
	private static DataAnalyzer instance = new DataAnalyzer();
	
	// A map of <fullPath, value> pairs of all the preferences
	private Map<String, String> allPrefs = new HashMap<String, String>();  
	
	// A map of <category, output_dir> pairs
	private Map<String, String> outputPathMap = new HashMap<String, String>(); 
	
	// A list of preference categories 
	private ArrayList<String> appCategory = new ArrayList<String>(); 
	
	public static DataAnalyzer getInstance() {
		return instance; 
	}
	
	/*
	 * Private constructor, prevent instantiation
	 */
	private DataAnalyzer(){
		// Create categories 
		this.appCategory.add("org.openoffice.Office.Writer");
		this.appCategory.add("org.openoffice.Office.Calc"); 
		this.appCategory.add("org.openoffice.Office.Impress");
		this.appCategory.add("org.openoffice.Office.Draw");
		this.appCategory.add("org.openoffice.Office.Math");
		this.appCategory.add("org.openoffice.Office.DataAccess");
	
		// Create output directories
		createOutputDirs();
	}; 
	
	/* 
	 * Analyze libreoffice configurations. 
	 */
	public void analyze() throws IOException{ 
		// Loop through all the prefs. 
		Set<String> keys = this.allPrefs.keySet(); 
		
		for (String key : keys) {
			String value = allPrefs.get(key); 
			String firstSegment = key.substring(0, key.indexOf('/'));
			String outputDir = new String(); 
			
			// Categorize the value
			String type = this.getType(value); 
			
			// Get output directory
			if (this.appCategory.contains(firstSegment)) { 
				outputDir = this.outputPathMap.get(firstSegment);
			} 
			else {  // Others
				outputDir = this.outputPathMap.get("others"); 
				 
			}
			
			// Build file path
			String path = new File(outputDir, type + ".txt").getPath(); 
			
			// Append to output file 
			this.appendToFile(path, key, value);
		}
	}
	
	/*
	 * Insert a fullPath-value pair into the map
	 */
	public void insert(String fullPath, String value) {
		this.allPrefs.put(fullPath, value); 
	}
	
	/*
	 * Append data to output files. 
	 */
	private void appendToFile(String path, String prefName, String value) {
		File file = new File(path);
		
		try {
			if (!file.exists()) {
				file.createNewFile(); 
			}
			
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw); 
			bw.write(prefName + "=" + value + "\n");
			bw.close(); 
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	/*
	 * Categorization of given value into boolean, integer, or others
	 */
	private String getType(String value) {
		String result = new String(); 
		if (value.equalsIgnoreCase("true")
				|| value.equalsIgnoreCase("false")) {  // Boolean
			result = "boolean"; 
		} else {
			try {  // Integer
				Integer.parseInt(value);
				result = "integer"; 
			} catch (Exception e) {  // Other types
				result = "others"; 
			}
		}
		return result; 
	}
	
	/* 
	 * Create output file directories. 
	 */
	private void createOutputDirs(){
		// Names of applications 
		String [] appNames = new String[] {
				"writer", 
				"calc", 
				"impress", 
				"draw", 
				"math", 
				"database", 
				"others" };
		
		for (String app : appNames) {
			File dir = new File("output/libodata/", app); 
			
			// Create output directory if not exist
			if (!dir.exists()) {
				boolean success = dir.mkdirs(); 
				
				if (!success) {
					System.err.println("Fail to create directory: " + dir.getPath()); 
				}
			}
			
			// Delete previous output files 
			new File(dir.getPath(), "boolean.txt").delete(); 
			new File(dir.getPath(), "integer.txt").delete(); 
			new File(dir.getPath(), "others.txt").delete(); 			
			
			// Map output directories to category
			if (app.equalsIgnoreCase("writer")){
				this.outputPathMap.put("org.openoffice.Office.Writer", dir.getPath()); 
			}
			else if (app.equalsIgnoreCase("calc")){
				this.outputPathMap.put("org.openoffice.Office.Calc", dir.getPath()); 
			}
			else if (app.equalsIgnoreCase("impress")){
				this.outputPathMap.put("org.openoffice.Office.Impress", dir.getPath()); 
			}
			else if (app.equalsIgnoreCase("draw")){
				this.outputPathMap.put("org.openoffice.Office.Draw", dir.getPath()); 
			}
			else if (app.equalsIgnoreCase("math")){
				this.outputPathMap.put("org.openoffice.Office.Math", dir.getPath()); 
			}
			else if (app.equalsIgnoreCase("database")){
				this.outputPathMap.put("org.openoffice.Office.DataAccess", dir.getPath()); 
			}
			else {
				this.outputPathMap.put("others", dir.getPath()); 
			}
		}
	}
	
	public void setAppValues(Map<String, ArrayList<String> > appValues) {
		this.appValues = appValues; 
	}
	
	public void genAppValuesData(){
		Set<String> keys = this.appValues.keySet(); 
		for (String key : keys) {  // Loop through each category
			if (!appValuesCount.containsKey(key)) {
				int[] counter = new int[3]; 
				Arrays.fill(counter, 0); 
				appValuesCount.put(key, counter); 
			}
			
			for (String value : this.appValues.get(key)) {  // Loop through each value
				if (value.equalsIgnoreCase("true")
						|| value.equalsIgnoreCase("false")) {  // Boolean
					appValuesCount.get(key)[0]++;
				} else {
					try {  // Integer
						int temp = Integer.parseInt(value);
						appValuesCount.get(key)[1]++; 
					} catch (Exception e) {  // Other types
						appValuesCount.get(key)[2]++;   
					}
				}
			}
		}
	}
	
	public void printAppValueData(){
		int[] others = new int[3]; 
		Arrays.fill(others, 0); 
		
		Set<String> keys = this.appValuesCount.keySet(); 
		
		for (String key : keys) {
			if (key.equalsIgnoreCase("org.openoffice.Office.Writer")){
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				} 
			}
			else if (key.equalsIgnoreCase("org.openoffice.Office.Calc")) {
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				}
			}
			else if (key.equalsIgnoreCase("org.openoffice.Office.Impress")) {
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				}
			}
			else if (key.equalsIgnoreCase("org.openoffice.Office.Draw")) {
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				} 
			}
			else if (key.equalsIgnoreCase("org.openoffice.Office.Math")) {
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				} 
			}
			else if (key.equalsIgnoreCase("org.openoffice.Office.DataAccess")) {
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				} 
			}
			else {
				int[] temp = this.appValuesCount.get(key); 
				others[0] += temp[0]; 
				others[1] += temp[1]; 
				others[2] += temp[2]; 
				
				System.out.println(key); 
				for (int item : this.appValuesCount.get(key)) {
					System.out.println(item); 
				} 
			}
		}
		
		System.out.println("Others:"); 
		System.out.println(others[0]); 
		System.out.println(others[1]); 
		System.out.println(others[2]); 
	}
}
