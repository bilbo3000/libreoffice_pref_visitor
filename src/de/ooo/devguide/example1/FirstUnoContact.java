package de.ooo.devguide.example1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.configuration.XTemplateInstance;
import com.sun.star.container.XHierarchicalName;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNamed;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;

import com.sun.star.uno.*;

public class FirstUnoContact {
	private XMultiComponentFactory mcServiceManager = null;
	private XComponentContext mxContext = null;
	private XMultiServiceFactory xProvider = null;

	private File outFile;
	private FileOutputStream fileOutputStream;
	private PrintWriter outStream;

	private ArrayList<String> allPrefs = new ArrayList<String>();
	private Set<String> distinctPrefs = new HashSet<String>();
	private Map<String, String> otherTypes = new HashMap<String, String>(); 
	private Map<String, ArrayList<String> > sameName = new HashMap<String, ArrayList<String> >(); 

	private int boolCount = 0;
	private int intCount = 0;

	void openFile() {
		try {
			outFile = new File("output.txt");
			fileOutputStream = new FileOutputStream(outFile);
			outStream = new PrintWriter(fileOutputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void closeFile() {
		outStream.close();
	}

	/**
	 * Method to browse the filter configuration.
	 * 
	 * Information about installed filters will be printed.
	 */
	public void printRegisteredFilters() throws com.sun.star.uno.Exception {
		final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
		// final String sFilterKey =
		// "/org.openoffice.TypeDetection.Filter/Filters";
		final String sFilterKey = "/";
		// browse the configuration, dumping filter information
		browseConfiguration(sFilterKey, new IConfigurationProcessor() {
			// / prints Path and Value of properties
			public void processValueElement(String sPath_, String sName_, Object aValue_) {
				allPrefs.add(sPath_);
				distinctPrefs.add(sPath_);
				if (!sameName.containsKey(sName_)){
					ArrayList<String> value = new ArrayList<String>(); 
					value.add(sPath_ + "=" + aValue_.toString()); 
					sameName.put(sName_, value); 
				}
				else {
					sameName.get(sName_).add(sPath_ + "=" + aValue_.toString()); 
				}
				
				String value = aValue_.toString();

				// Categorization
				if (value.equalsIgnoreCase("true")
						|| value.equalsIgnoreCase("false")) {  // Boolean
					boolCount++;
				} else {
					try {  // Integer
						int temp = (Integer) aValue_;
						// System.out.println("int: " + intCount);
						intCount++;
					} catch (Exception e) {  // Other types
						// System.out.println("Other: " + aValue_.toString());
						otherTypes.put(sPath_, aValue_.toString()); 
					}
				}

				if (AnyConverter.isArray(aValue_)) {
					// final Object[] aArray = (Object[]) aValue_;
					outStream.write(sPath_ + '=' + aValue_ + '\n');
					// System.out.print("\tValue: " + sPath_ + " = { ");
					// for (int i = 0; i < aArray.length; ++i) {
					// if (i != 0)
					// System.out.print(", ");
					// System.out.print(aArray[i]);
					// }
					// System.out.println(" }");
				} else
					outStream.write(sPath_ + '=' + aValue_ + '\n');
				// System.out.println("\tValue: " + sPath_ + " = " + aValue_);
			}

			// / prints the Filter entries
			public void processStructuralElement(String sPath_,
					XInterface xElement_) {
				// get template information, to detect instances of the 'Filter'
				// template
				XTemplateInstance xInstance = UnoRuntime.queryInterface(
						XTemplateInstance.class, xElement_);

				// only select the Filter entries
				if (xInstance != null
						&& xInstance.getTemplateName().endsWith("Filter")) {
					XNamed xNamed = UnoRuntime.queryInterface(XNamed.class,
							xElement_);
					// System.out.println("Filter " + xNamed.getName() + " ("
					// + sPath_ + ")");
				}
			}
		});
	}

	public Object createConfigurationView(String sPath)
			throws com.sun.star.uno.Exception {

		// The service name: Need only read access:
		final String sReadOnlyView = "com.sun.star.configuration.ConfigurationAccess";

		// creation arguments: nodepath
		com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
		aPathArgument.Name = "nodepath";
		aPathArgument.Value = sPath;

		Object[] aArguments = new Object[1];
		aArguments[0] = aPathArgument;

		// create the view
		Object xViewRoot = xProvider.createInstanceWithArguments(sReadOnlyView,
				aArguments);

		return xViewRoot;
	}

	/**
	 * Method to browse the part rooted at sRootPath of the configuration that
	 * the Provider provides.
	 * 
	 * All nodes will be processed by the IConfigurationProcessor passed.
	 */
	public void browseConfiguration(String sRootPath,
			IConfigurationProcessor aProcessor)
			throws com.sun.star.uno.Exception {
		// create the root element
		XInterface xViewRoot = (XInterface) createConfigurationView(sRootPath);

		// now do the processing
		browseElementRecursively(xViewRoot, aProcessor);

		// we are done with the view - dispose it
		// This assumes that the processor
		// does not keep a reference to the elements in processStructuralElement

		UnoRuntime.queryInterface(XComponent.class, xViewRoot).dispose();
		xViewRoot = null;
	}

	// Internal method to browse a structural element recursively in preorder
	public void browseElementRecursively(XInterface xElement,
			IConfigurationProcessor aProcessor)
			throws com.sun.star.uno.Exception {
		// First process this as an element (preorder traversal)
		XHierarchicalName xElementPath = (XHierarchicalName) UnoRuntime
				.queryInterface(XHierarchicalName.class, xElement);

		String sPath = xElementPath.getHierarchicalName();

		// call configuration processor object
		aProcessor.processStructuralElement(sPath, xElement);

		// now process this as a container of named elements
		XNameAccess xChildAccess = (XNameAccess) UnoRuntime.queryInterface(
				XNameAccess.class, xElement);

		// get a list of child elements
		String[] aElementNames = xChildAccess.getElementNames();

		// and process them one by one
		for (int i = 0; i < aElementNames.length; ++i) {
			Object aChild = xChildAccess.getByName(aElementNames[i]);

			// is it a structural element (object) ...
			if (aChild instanceof XInterface) {
				// then get an interface
				XInterface xChildElement = (XInterface) aChild;

				// and continue processing child elements recursively
				browseElementRecursively(xChildElement, aProcessor);
			}
			// ... or is it a simple value
			else {
				// Build the path to it from the path of
				// the element and the name of the child
				String sChildPath;
				sChildPath = xElementPath
						.composeHierarchicalName(aElementNames[i]);

				// and process the value
				aProcessor.processValueElement(sChildPath, aElementNames[i], aChild);
			}
		}
	}

	/**
	 * @descr Connect to a already running StarOffice application that has been
	 *        started with a command line argument like
	 *        "-accept=socket,host=localhost,port=5678;urp;"
	 */
	private void connect(String hostname, int portnumber) {
		// Set up connection string.
		String sConnectString = "uno:socket,host=" + hostname + ",port="
				+ portnumber + ";urp;StarOffice.ServiceManager";

		// connect to a running office and get the ServiceManager
		try {
			// Create a URL Resolver.
			XMultiServiceFactory aLocalServiceManager = com.sun.star.comp.helper.Bootstrap
					.createSimpleServiceManager();

			XUnoUrlResolver aURLResolver = (XUnoUrlResolver) UnoRuntime
					.queryInterface(
							XUnoUrlResolver.class,
							aLocalServiceManager
									.createInstance("com.sun.star.bridge.UnoUrlResolver"));

			// Get multiConponent service manager
			mcServiceManager = (XMultiComponentFactory) UnoRuntime
					.queryInterface(XMultiComponentFactory.class,
							aURLResolver.resolve(sConnectString));

			XPropertySet xProperySet = (XPropertySet) UnoRuntime
					.queryInterface(XPropertySet.class, mcServiceManager);

			// Get context
			mxContext = (XComponentContext) UnoRuntime.queryInterface(
					XComponentContext.class,
					xProperySet.getPropertyValue("DefaultContext"));

			// Get multi service factory
			xProvider = UnoRuntime.queryInterface(XMultiServiceFactory.class,
					mcServiceManager.createInstanceWithContext(
							"com.sun.star.configuration.ConfigurationProvider",
							mxContext));

			XServiceInfo info = UnoRuntime.queryInterface(XServiceInfo.class,
					xProvider);
			if (info.supportsService("com.sun.star.configuration.ConfigurationProvider")) {
				System.out.println("ConfigurationProvider is OK. ");
			}
		}

		catch (Exception e) {
			System.err.println("Could not connect with " + sConnectString
					+ " : " + e);
			e.printStackTrace();

			System.err.println("Please start OpenOffice/StarOffice with "
					+ "\"-accept=socket,host=localhost,port=5678;urp;\"");
		}
	}

	private ArrayList<String> getAllPrefs() {
		return this.allPrefs;
	}

	private Set<String> getDistinctPrefs() {
		return this.distinctPrefs;
	}
	
	private Map<String, String> getOtherTypes() {
		return this.otherTypes; 
	}

	private Map<String, ArrayList<String>> getSameName() {
		return this.sameName; 
	}
	
	private int getBoolCount() {
		return this.boolCount;
	}

	private int getIntCount() {
		return this.intCount;
	}

	public static void main(String[] args) {
		try {

			// get the remote office component context
			// com.sun.star.uno.XComponentContext xContext =
			// com.sun.star.comp.helper.Bootstrap.bootstrap();
			FirstUnoContact firstContact = new FirstUnoContact();
			firstContact.connect("localhost", 5678);

			// mxContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
			// String oooExecFolder =
			// "/home/djin/work/git/libo/install/program";
			// XComponentContext xContext =
			// BootstrapSocketConnector.bootstrap(oooExecFolder);
			System.out.println("Connected to a running office ...");

			firstContact.openFile();
			firstContact.printRegisteredFilters();
			firstContact.closeFile();
			// com.sun.star.lang.XMultiComponentFactory xMCF =
			// xContext.getServiceManager();

			// String available = (xMCF != null ? "available" :
			// "not available");
			// System.out.println("remote ServiceManager is " + available);

			ArrayList<String> allPrefs = firstContact.getAllPrefs();
			Set<String> distinctPrefs = firstContact.getDistinctPrefs();
			System.out.println("all prefs size: " + allPrefs.size());
			System.out.println("distinct pref size: " + distinctPrefs.size());
			System.out.println("boolean count: " + firstContact.getBoolCount());
			System.out.println("int count: " + firstContact.getIntCount());
			
			// Print other types
			File otherFile = new File("othertypes.txt");
			FileOutputStream otherFileOutputStream = new FileOutputStream(otherFile);
			PrintWriter pw = new PrintWriter(otherFileOutputStream); 
			Map<String, String> otherTypes = firstContact.getOtherTypes(); 
			Set<String> keys = otherTypes.keySet(); 
			
			for (String key : keys) {
				pw.write(key + "=" + otherTypes.get(key) + '\n');  
			}
			
			pw.close(); 
			
			// Print same name but different prefs
			File sameNameFile = new File("samename.txt");
			FileOutputStream sameNameFileOutputStream = new FileOutputStream(sameNameFile); 
			PrintWriter pw2 = new PrintWriter(sameNameFileOutputStream);
			Map<String, ArrayList<String> > sameName = firstContact.getSameName(); 
			keys = sameName.keySet(); 
			
			for (String key : keys) {
				if (sameName.get(key).size() > 1 && sameName.get(key).size() <= 5 ){  // Only print ones with multiple names
					pw2.write(key + ": {\n"); 
					for (String item : sameName.get(key)) {
						pw2.write("---" + item + '\n');
					}
					pw2.write("}\n\n\n"); 
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
