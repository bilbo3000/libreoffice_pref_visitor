package edu.unl.cse.liboconfig;

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
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
