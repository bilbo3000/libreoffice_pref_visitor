package edu.unl.cse.liboconfig;

import java.lang.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.container.XHierarchicalName;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;

import com.sun.star.uno.*;

public class LiboConfigVisitor {
	private XMultiComponentFactory mcServiceManager = null;
	private XComponentContext mxContext = null;
	private XMultiServiceFactory xProvider = null;

	/**
	 * Visitor that visit libreoffice configuration hierarchy 
	 * and process each value in the hierarchy. 
	 */
	public void visit() throws com.sun.star.uno.Exception {
		final String sFilterKey = "/";  // Start walk from the root
		
		// browse the configuration, dumping filter information
		IConfigurationProcessor processor = new ConfigurationProcessor(); 
		browseConfiguration(sFilterKey, processor); 
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
	
	/**
	 * Create ConfigurationAccess instance with given path. 
	 */
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
	 * Internal method to browse a structural element recursively in preorder
	 */
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
				String fullPath = xElementPath.composeHierarchicalName(aElementNames[i]);
				// Process the value
				aProcessor.processValueElement(fullPath, aElementNames[i], aChild);
			}
		}
	}

	/**
	 * @descr Connect to a already running StarOffice application that has been
	 *        started with a command line argument like
	 *        "-accept=socket,host=localhost,port=5678;urp;"
	 */
	public void connect(String hostname, int portnumber) {
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
		}

		catch (Exception e) {
			System.err.println("Could not connect with " + sConnectString
					+ " : " + e);
			e.printStackTrace();

			System.err.println("Please start OpenOffice/StarOffice with "
					+ "\"-accept=socket,host=localhost,port=5678;urp;\"");
		}
	}

}
