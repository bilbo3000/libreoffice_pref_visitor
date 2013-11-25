package edu.unl.cse.liboconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.star.configuration.XTemplateInstance;
import com.sun.star.container.XNamed;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XInterface;

/**
 * Processor that implements the processor interface.
 * 
 * @author djin
 * 
 */

public class ConfigurationProcessor implements IConfigurationProcessor {

	private ArrayList<String> allPrefs = new ArrayList<String>();
	private Set<String> distinctPrefs = new HashSet<String>();
	private Set<String> distinctPrefNames = new HashSet<String>();
	private Map<String, String> otherTypes = new HashMap<String, String>();
	private Map<String, ArrayList<String>> sameName = new HashMap<String, ArrayList<String>>();
	private Map<String, Map<String, ArrayList<String>>> distinctCategory = new HashMap<String, Map<String, ArrayList<String>>>();
	private Map<String, ArrayList<String>> appValues = new HashMap<String, ArrayList<String>>();

	private int boolCount = 0;
	private int intCount = 0;

	private DataAnalyzer analyzer;

	public ConfigurationProcessor() {
		// Get singleton analyzer object
		analyzer = DataAnalyzer.getInstance();
	}

	// prints Path and Value of properties
	public void processValueElement(String fullPath, String childName,
			Object aValue_) {
		System.out.println(fullPath); 
		allPrefs.add(fullPath);
		distinctPrefs.add(fullPath);

		analyzer.insert(fullPath, aValue_.toString());

		if (!sameName.containsKey(childName)) {
			ArrayList<String> value = new ArrayList<String>();
			value.add(fullPath + "=" + aValue_.toString());
			sameName.put(childName, value);
		} else {
			sameName.get(childName).add(fullPath + "=" + aValue_.toString());
		}

		String value = aValue_.toString();

		// Categorization
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { // Boolean
			boolCount++;
		} else {
			try { // Integer
				int temp = (Integer) aValue_;
				intCount++;
			} catch (Exception e) { // Other types
				otherTypes.put(fullPath, aValue_.toString());
			}
		}

		// Process array type
		if (AnyConverter.isArray(aValue_)) {
			// final Object[] aArray = (Object[]) aValue_;
			// outStream.write(sPath_ + '=' + aValue_ + '\n');
			// System.out.print("\tValue: " + sPath_ + " = { ");
			// for (int i = 0; i < aArray.length; ++i) {
			// if (i != 0)
			// System.out.print(", ");
			// System.out.print(aArray[i]);
			// }
			// System.out.println(" }");
		}
	}

	// / prints the Filter entries
	public void processStructuralElement(String sPath_, XInterface xElement_) {
		// get template information, to detect instances of the 'Filter'
		// template
		XTemplateInstance xInstance = UnoRuntime.queryInterface(
				XTemplateInstance.class, xElement_);

		// only select the Filter entries
		if (xInstance != null && xInstance.getTemplateName().endsWith("Filter")) {
			XNamed xNamed = UnoRuntime.queryInterface(XNamed.class, xElement_);
		}
	}

	public Set<String> getDistinctPrefs() {
		return this.distinctPrefs;
	}

	public Map<String, String> getOtherTypes() {
		return this.otherTypes;
	}

	public Map<String, ArrayList<String>> getSameName() {
		return this.sameName;
	}

	public Map<String, Map<String, ArrayList<String>>> getDistinctCategory() {
		return this.distinctCategory;
	}

	public Set<String> getDistinctPrefNames() {
		return this.distinctPrefNames;
	}

	public Map<String, ArrayList<String>> getAppValues() {
		return this.appValues;
	}

	public int getBoolCount() {
		return this.boolCount;
	}

	public int getIntCount() {
		return this.intCount;
	}

	public ArrayList<String> getAllPrefs() {
		return this.allPrefs;
	}
}
