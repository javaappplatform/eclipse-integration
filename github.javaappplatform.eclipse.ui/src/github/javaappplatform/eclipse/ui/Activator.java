package github.javaappplatform.eclipse.ui;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Activator implements BundleActivator
{
	
	public static final String PLUGIN_ID = "github.javaappplatform.eclipse.ui";

	// The shared instance
	private static Activator PLUGIN;
	private static BundleContext CONTEXT;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		CONTEXT = context;
		PLUGIN = this;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		PLUGIN = null;
	}


	/**
	 * Returns the shared activator instance.
	 * @return The shared activator instance.
	 */
	public static Activator getDefault()
	{
		return PLUGIN;
	}

	/**
	 * Returns the overall bundle context.
	 * @return The overall bundle context.
	 */
	public static BundleContext getContext()
	{
		return CONTEXT;
	}

//	public static final ILaunchPreset[] availablePresets()
//	{
//		ArrayList<ILaunchPreset> search = new ArrayList<ILaunchPreset>();
//
//		try
//		{
//			ServiceReference<?>[] refs = CONTEXT.getAllServiceReferences(ILaunchPreset.class.getName(), null);
//			if (refs != null)
//			{
//				for (ServiceReference<?> ref : refs)
//				{
//					search.add((ILaunchPreset) CONTEXT.getService(ref));
//				}
//			}
//		} catch (InvalidSyntaxException e)
//		{
//			//does not happen
//		}
//		return search.toArray(new ILaunchPreset[search.size()]);
//	}
}
